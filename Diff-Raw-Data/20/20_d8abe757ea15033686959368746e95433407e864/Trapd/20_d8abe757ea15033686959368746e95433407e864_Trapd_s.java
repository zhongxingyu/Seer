 /*******************************************************************************
  * This file is part of OpenNMS(R).
  *
  * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
  * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
  *
  * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
  *
  * OpenNMS(R) is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * OpenNMS(R) is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OpenNMS(R).  If not, see:
  *      http://www.gnu.org/licenses/
  *
  * For more information contact:
  *     OpenNMS(R) Licensing <license@opennms.org>
  *     http://www.opennms.org/
  *     http://www.opennms.com/
  *******************************************************************************/
 
 package org.opennms.ng.services.trapd;
 
 import java.io.IOException;
 import java.lang.reflect.UndeclaredThrowableException;
 import java.net.InetAddress;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 
 import org.apache.camel.ProducerTemplate;
 import org.apache.log4j.Category;
 import org.apache.log4j.Logger;
 import org.opennms.core.utils.InetAddressUtils;
 import org.opennms.core.utils.LogUtils;
 import org.opennms.netmgt.snmp.SnmpStrategy;
 import org.opennms.netmgt.snmp.SnmpV3User;
 import org.opennms.netmgt.snmp.TrapNotification;
 import org.opennms.netmgt.snmp.TrapNotificationListener;
 import org.opennms.netmgt.snmp.TrapProcessor;
 import org.opennms.netmgt.snmp.TrapProcessorFactory;
 import org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy;
 
 /**
  * <p>
  * The Trapd listens for SNMP traps on the standard port(162). Creates a
  * SnmpTrapSession and implements the SnmpTrapHandler to get callbacks when
  * traps are received.
  * </p>
  *
  * <p>
  * The received traps are converted into XML and sent to eventd.
  * </p>
  *
  * <p>
  * <strong>Note: </strong>Trapd is a PausableFiber so as to receive control
  * events. However, a 'pause' on Trapd has no impact on the receiving and
  * processing of traps.
  * </p>
  *
  * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
  * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
  * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
  * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
  * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
  * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
  */
 public class Trapd implements TrapProcessorFactory, TrapNotificationListener {
 
     public static final int START_PENDING = 0;
     public static final int STARTING = 1;
     public static final int RUNNING = 2;
     public static final int STOP_PENDING = 3;
     public static final int STOPPED = 4;
     public static final int PAUSE_PENDING = 5;
     public static final int PAUSED = 6;
     public static final int RESUME_PENDING = 7;
 

     private SnmpStrategy strategy;
 
     /**
      * The last status sent to the service control manager.
      */
     private int status = START_PENDING;
 
     /**
      * Trapd IP manager.  Contains IP address -> node ID mapping.
      */
     private TrapdIpMgr trapdIpMgr;
 
     private String snmpTrapAddress;
 
     private Integer snmpTrapPort;
 
     private List<SnmpV3User> snmpV3Users;
 
     private boolean registeredForTraps;
 
     private ExecutorService backlogQ;
 
     private TrapQueueProcessorFactory processorFactory;
 
    private ProducerTemplate template;

     /**
      * <P>
      * Constructs a new Trapd object that receives and forwards trap messages
      * via JSDT. The session is initialized with the default client name of <EM>
      * OpenNMS.trapd</EM>. The trap session is started on the default port, as
      * defined by the SNMP library.
      * </P>
      *
      */
     public Trapd() {
     }
 
     /**
      * <p>createTrapProcessor</p>
      *
      * @return a {@link org.opennms.netmgt.snmp.TrapProcessor} object.
      */
     @Override
     public TrapProcessor createTrapProcessor() {
         return new EventCreator(trapdIpMgr);
     }
 
     /** {@inheritDoc} */
     @Override
     public void trapReceived(TrapNotification trapNotification) {
 
         backlogQ.submit(processorFactory.getInstance(trapNotification));
 
     }
 
     /**
      * <p>onInit</p>
      */
     public synchronized void onInit() {
 
         try {
             trapdIpMgr.dataSourceSync();
         } catch (final SQLException e) {
             LogUtils.errorf(this, e, "init: Failed to load known IP address list");
             throw new UndeclaredThrowableException(e);
         }
 
         try {
             InetAddress address = getInetAddress();
             LogUtils.infof(this, "Listening on %s:%d", address == null ? "[all interfaces]" : InetAddressUtils.str(address), snmpTrapPort);
             //SnmpUtils.registerForTraps(this, this, address, snmpTrapPort, snmpV3Users);
 
             strategy = new Snmp4JStrategy();
             strategy.registerForTraps(this, this, address, snmpTrapPort, snmpV3Users);
 
             registeredForTraps = true;
 
             LogUtils.debugf(this, "init: Creating the trap session");
         } catch (final IOException e) {
             if (e instanceof java.net.BindException) {
                 managerLog().error("init: Failed to listen on SNMP trap port, perhaps something else is already listening?", e);
                 LogUtils.errorf(this, e, "init: Failed to listen on SNMP trap port, perhaps something else is already listening?");
             } else {
                 LogUtils.errorf(this, e, "init: Failed to initialize SNMP trap socket");
             }
             throw new UndeclaredThrowableException(e);
         }
 
     }
 
     private InetAddress getInetAddress() {
         if (snmpTrapAddress.equals("*")) {
             return null;
         }
         return InetAddressUtils.addr(snmpTrapAddress);
     }
 
     private Category managerLog() {
         return Logger.getLogger("OpenNMS.Manager");
     }
 
     /**
      * Create the SNMP trap session and create the communication channel
      * to communicate with eventd.
      *
      * @exception java.lang.reflect.UndeclaredThrowableException
      *                if an unexpected database, or IO exception occurs.
      */
     public synchronized void onStart() {
         status = STARTING;
 
         LogUtils.debugf(this, "start: Initializing the trapd config factory");
 
         status = RUNNING;
 
         LogUtils.debugf(this, "start: Trapd ready to receive traps");
     }
 
     /**
      * Pauses Trapd
      */
     public void onPause() {
         if (status != RUNNING) {
             return;
         }
 
         status = PAUSE_PENDING;
 
         LogUtils.debugf(this, "pause: Calling pause on processor");
 
         status = PAUSED;
 
         LogUtils.debugf(this, "pause: Trapd paused");
     }
 
     /**
      * Resumes Trapd
      */
     public void onResume() {
         if (status != PAUSED) {
             return;
         }
 
         status = RESUME_PENDING;
 
         LogUtils.debugf(this, "resume: Calling resume on processor");
 
         status = RUNNING;
 
         LogUtils.debugf(this, "resume: Trapd resumed");
     }
 
     /**
      * Stops the currently running service. If the service is not running then
      * the command is silently discarded.
      */
     public synchronized void onStop() {
         status = STOP_PENDING;
 
         // shutdown and wait on the background processing thread to exit.
         LogUtils.debugf(this, "stop: closing communication paths.");
 
         try {
             if (registeredForTraps) {
                 LogUtils.debugf(this, "stop: Closing SNMP trap session.");
 
                 strategy.unregisterForTraps(this,getInetAddress(),snmpTrapPort);
              //   SnmpUtils.unregisterForTraps(this, getInetAddress(), snmpTrapPort);
                 LogUtils.debugf(this, "stop: SNMP trap session closed.");
             } else {
                 LogUtils.debugf(this, "stop: not attemping to closing SNMP trap session--it was never opened");
             }
 
         } catch (final IOException e) {
             LogUtils.warnf(this, e, "stop: exception occurred closing session");
         } catch (final IllegalStateException e) {
             LogUtils.debugf(this, e, "stop: The SNMP session was already closed");
         }
 
         LogUtils.debugf(this, "stop: Stopping queue processor.");
 
         backlogQ.shutdown();
 
         status = STOPPED;
 
         LogUtils.debugf(this, "stop: Trapd stopped");
     }
 
     /**
      * Returns the current status of the service.
      *
      * @return The service's status.
      */
     public synchronized int getStatus() {
         return status;
     }
 
     /** {@inheritDoc} */
     @Override
     public void trapError(final int error, final String msg) {
         LogUtils.warnf(this, "Error Processing Received Trap: error = " + error + (msg != null ? ", ref = " + msg : ""));
     }
 
     public void setTrapdIpMgr(TrapdIpMgr trapdIpMgr) {
         this.trapdIpMgr = trapdIpMgr;
     }
 
     public void setSnmpTrapAddress(String snmpTrapAddress) {
         this.snmpTrapAddress = snmpTrapAddress;
     }
 
     public void setSnmpTrapPort(Integer snmpTrapPort) {
         this.snmpTrapPort = snmpTrapPort;
     }
 
     public void setSnmpV3Users(List<SnmpV3User> snmpV3Users) {
         this.snmpV3Users = snmpV3Users;
     }
 
     public void setBacklogQ(ExecutorService backlogQ) {
         this.backlogQ = backlogQ;
     }
 
     public void setProcessorFactory(TrapQueueProcessorFactory processorFactory) {
         this.processorFactory = processorFactory;
     }
 
    public void setTemplate(ProducerTemplate template) {
        this.template = template;
    }
 }

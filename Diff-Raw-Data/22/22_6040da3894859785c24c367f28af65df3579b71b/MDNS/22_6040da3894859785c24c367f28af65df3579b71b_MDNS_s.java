 /* ODISP -- Message Oriented Middleware
  * Copyright (C) 2003-2005 Valentin A. Alekseev
  * Copyright (C) 2003-2005 Andrew A. Porohin 
  * 
  * ODISP is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 2.1 of the License.
  * 
  * ODISP is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with ODISP.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.valabs.stdobj.mdns;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.jmdns.JmDNS;
 import javax.jmdns.ServiceEvent;
 import javax.jmdns.ServiceInfo;
 import javax.jmdns.ServiceListener;
 
 import org.doomdark.uuid.UUID;
 import org.valabs.odisp.common.Message;
 import org.valabs.odisp.common.MessageHandler;
 import org.valabs.odisp.common.StandartODObject;
 import org.valabs.stdmsg.CopyrightGetMessage;
 import org.valabs.stdmsg.CopyrightGetReplyMessage;
 import org.valabs.stdmsg.MDNSAdvertiseServiceMessage;
 import org.valabs.stdmsg.MDNSListServicesMessage;
 import org.valabs.stdmsg.MDNSListServicesReplyMessage;
 import org.valabs.stdmsg.MDNSRegisterTypeNotifyMessage;
 import org.valabs.stdmsg.MDNSTypeNotifyMessage;
 import org.valabs.stdmsg.ODObjectLoadedMessage;
 
 
 /** ,    Zeroconf/Randezvous.
  * @author <a href="mailto:valeks@valabs.spb.ru">  .</a>
  * @version $Id: MDNS.java,v 1.10 2005/11/20 12:33:04 valeks Exp $
  */
 public class MDNS extends StandartODObject implements MessageHandler, ServiceListener {
   private Map listeners = new HashMap();
   public static final String NAME = "mdns";
   public static final String FULLNAME = "Multicast DNS service discovery interface";
   public static final String VERSION = "0.1.0";
   public static final String COPYRIGHT = "(C) 2005 Valentin A. Alekseev";
   private JmDNS jmdns;
 
   public MDNS() {
     super(NAME, FULLNAME, VERSION, COPYRIGHT);
     setBlockedState(true);
   }
   
   public void registerHandlers() {
     addHandler(ODObjectLoadedMessage.NAME, this);
     addHandler(MDNSAdvertiseServiceMessage.NAME, this);
     addHandler(MDNSRegisterTypeNotifyMessage.NAME, this);
     addHandler(MDNSListServicesMessage.NAME, this);
     addHandler(CopyrightGetMessage.NAME, this);
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.MessageHandler#messageReceived(org.valabs.odisp.common.Message)
    */
   public void messageReceived(Message msg) {
     if (ODObjectLoadedMessage.equals(msg)) {
       try {
         Logger log = Logger.getLogger(JmDNS.class.toString());
         log.setLevel(Level.FINE);
        jmdns = new JmDNS();
       } catch (IOException e) {
         dispatcher.getExceptionHandler().signalException(e);
       }
       setBlockedState(false);
     } else if (MDNSListServicesMessage.equals(msg)) {
       String type = MDNSListServicesMessage.getType(msg);
       Set siResult = new HashSet();
       for (int i = 0; i < 5; i++) {
         ServiceInfo[] si = jmdns.list(type);
         for (int j = 0; j < si.length; j++) {
           System.out.println(si[j]);
         }
         siResult.addAll(Arrays.asList(si));
         try {
           Thread.sleep(1000);
         } catch (InterruptedException e) { /*  . */ }
       }
       Message m = dispatcher.getNewMessage();
       MDNSListServicesReplyMessage.setup(m, msg.getOrigin(), getObjectName(), msg.getId());
       MDNSListServicesReplyMessage.setType(m, type);
       MDNSListServicesReplyMessage.setServices(m, siResult);
       dispatcher.send(m);
     } else if (MDNSAdvertiseServiceMessage.equals(msg)) {
      logger.fine("MDNSAdvertiseServiceMessage received.");
       String name = MDNSAdvertiseServiceMessage.getName(msg);
       String type = MDNSAdvertiseServiceMessage.getType(msg);
       String text = MDNSAdvertiseServiceMessage.getText(msg);
       Integer port = MDNSAdvertiseServiceMessage.getPort(msg);
       try {
        ServiceInfo si = new ServiceInfo(type, name, port.intValue(), text);
         jmdns.registerService(si);
        logger.fine("ServiceInfo [" + si + "] registered.");
       } catch (IOException e) {
         dispatcher.getExceptionHandler().signalException(e);
       }
     } else if (MDNSRegisterTypeNotifyMessage.equals(msg)) {
       String type = MDNSRegisterTypeNotifyMessage.getType(msg);
       listeners.put(type, msg.getOrigin());
       jmdns.addServiceListener(type, this);
     } else if (CopyrightGetReplyMessage.equals(msg)) {
     	List result = new ArrayList();
    	result.add("JmDNS Copyright 2003-2005 Arthur van Hoff, Rick Blair");
     	Message m = dispatcher.getNewMessage();
     	CopyrightGetReplyMessage.setup(m, msg.getOrigin(), getObjectName(), msg.getId());
     	CopyrightGetReplyMessage.setCopyrights(m, result);
     	dispatcher.send(m);
     }
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.ODObject#getDepends()
    */
   public String[] getDepends() {
     String[] depends = {"dispatcher",};
     return depends;
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.ODObject#getProviding()
    */
   public String[] getProviding() {
     String[] providing = { NAME, };
     return providing;
   }
 
   /* (non-Javadoc)
    * @see javax.jmdns.ServiceListener#serviceAdded(javax.jmdns.ServiceEvent)
    */
   public void serviceAdded(ServiceEvent event) {
     
     // TODO Auto-generated method stub
     
   }
 
   /* (non-Javadoc)
    * @see javax.jmdns.ServiceListener#serviceRemoved(javax.jmdns.ServiceEvent)
    */
   public void serviceRemoved(ServiceEvent event) {
     // TODO Auto-generated method stub
     
   }
 
   /* (non-Javadoc)
    * @see javax.jmdns.ServiceListener#serviceResolved(javax.jmdns.ServiceEvent)
    */
   public void serviceResolved(ServiceEvent event) {
     String type = event.getType();
     ServiceInfo si = event.getInfo();
     Message m = dispatcher.getNewMessage();
     MDNSTypeNotifyMessage.setup(m, (String) listeners.get(type), getObjectName(), UUID.getNullUUID());
     MDNSTypeNotifyMessage.initAll(m, si.getType(), si.getName(), new Integer(si.getPort()), si.getAddress().getHostName(), si.getTextString());
     dispatcher.send(m);
   }
   
   public int cleanUp(int code) {
    logger.fine("Closing everything.");
     if (jmdns != null) {
       jmdns.close();
     }
     return 0;
   }
 }

 /**
  * This file is part of GoldenGate Project (named also GoldenGate or GG).
  * 
  * Copyright 2009, Frederic Bregier, and individual contributors by the @author
  * tags. See the COPYRIGHT.txt in the distribution for a full listing of
  * individual contributors.
  * 
  * All GoldenGate Project is free software: you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * GoldenGate is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * GoldenGate . If not, see <http://www.gnu.org/licenses/>.
  */
 package goldengate.snmp;
 
 import goldengate.common.logging.GgInternalLogger;
 import goldengate.common.logging.GgInternalLoggerFactory;
 import goldengate.snmp.GgInterfaceMib.TrapLevel;
 import goldengate.snmp.SnmpConfiguration.TargetElement;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.snmp4j.TransportMapping;
 import org.snmp4j.agent.BaseAgent;
 import org.snmp4j.agent.CommandProcessor;
 import org.snmp4j.agent.DuplicateRegistrationException;
 import org.snmp4j.agent.MOGroup;
 import org.snmp4j.agent.mo.MOTableRow;
 import org.snmp4j.agent.mo.snmp.RowStatus;
 import org.snmp4j.agent.mo.snmp.SNMPv2MIB;
 import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
 import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
 import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
 import org.snmp4j.agent.mo.snmp.StorageType;
 import org.snmp4j.agent.mo.snmp.VacmMIB;
 import org.snmp4j.agent.security.MutableVACM;
 import org.snmp4j.mp.MPv3;
 import org.snmp4j.mp.MessageProcessingModel;
 import org.snmp4j.mp.SnmpConstants;
 import org.snmp4j.security.SecurityLevel;
 import org.snmp4j.security.SecurityModel;
 import org.snmp4j.security.USM;
 import org.snmp4j.security.UsmUser;
 import org.snmp4j.smi.Address;
 import org.snmp4j.smi.GenericAddress;
 import org.snmp4j.smi.Integer32;
 import org.snmp4j.smi.OID;
 import org.snmp4j.smi.OctetString;
 import org.snmp4j.smi.Variable;
 import org.snmp4j.smi.VariableBinding;
 import org.snmp4j.transport.TransportMappings;
 import org.snmp4j.util.ThreadPool;
 import org.snmp4j.util.WorkerPool;
 
 /**
  * This Agent contains some functionalities for running a version 2c and 3 of SNMP
  * agent.
  * 
  * @author Frederic Bregier
  * 
  */
 public class GgSnmpAgent extends BaseAgent {
     /**
      * Internal Logger
      */
     private static GgInternalLogger logger = GgInternalLoggerFactory
             .getLogger(GgSnmpAgent.class);
 
     private String [] address = new String[] {SnmpConfiguration.DEFAULTADDRESS};
     private int nbThread = 4;
     private boolean isFilterAccessEnabled = false;
     private boolean useTrap = false;
     public int trapLevel = 0;
     private List<UsmUser> listUsmUser;
     private List<TargetElement> listTargetElements;
     private boolean hasV2 = false;
     private boolean hasV3 = false;
 
     private WorkerPool workerPool = null;
     
     public GgInterfaceMonitor monitor;
     
     public GgInterfaceMib mib;
     
     
     /**
      * 
      * @param configurationFile
      * @param monitor the monitor associated  
      * @param mib the associated MIB
      * 
      * @throws IllegalArgumentException
      */
     public GgSnmpAgent(File configurationFile, GgInterfaceMonitor monitor, GgInterfaceMib mib)
     throws IllegalArgumentException{
         /**
          * Creates a base agent with boot-counter, config file, and a
          * CommandProcessor for processing SNMP requests.
          * 
          * Parameters:
          * 
          * These files does not exist and are not used but has to be
          * specified. Read snmp4j docs for more info
          * 
          * "bootCounterFile" - a file with serialized boot-counter information
          * (read/write). If the file does not exist it is created on shutdown of
          * the agent.
          * 
          * "configFile" - a file with serialized configuration information
          * (read/write). If the file does not exist it is created on shutdown of
          * the agent.
          * 
          * "commandProcessor" - the CommandProcessor instance that handles the
          * SNMP requests.
          */
         super(new File(configurationFile.getParentFile(),"conf.agent"), 
                 new File(configurationFile.getParentFile(), "bootCounter.agent"),
                 new CommandProcessor(new OctetString(MPv3.createLocalEngineID())));
         if (! SnmpConfiguration.setConfigurationFromXml(configurationFile)) {
             throw new IllegalArgumentException("Cannot load configuration");
         }
         this.address = SnmpConfiguration.address;
         this.nbThread = SnmpConfiguration.nbThread;
         this.isFilterAccessEnabled = SnmpConfiguration.isFilterAccessEnabled;
         this.useTrap = SnmpConfiguration.isUsingTrap;
         this.trapLevel = SnmpConfiguration.trapLevel;
         this.listUsmUser = SnmpConfiguration.listUsmUser;
         this.listTargetElements = SnmpConfiguration.listTargetElements;
         this.hasV2 = SnmpConfiguration.hasV2;
         this.hasV3 = SnmpConfiguration.hasV3;
 
         logger.debug("SNMP Configuration loaded: "+this.address[0]+":"+this.nbThread);
         this.workerPool = ThreadPool.create("SnmpRequestPool", nbThread);
         agent.setWorkerPool(this.workerPool);
         this.monitor = monitor;
         this.monitor.setAgent(this);
         this.mib = mib;
         this.mib.setAgent(this);
     }
     
     /**
      * @return the monitor
      */
     public GgInterfaceMonitor getMonitor() {
         return monitor;
     }
 
     /**
      * @return the mib
      */
     public GgInterfaceMib getMib() {
         return mib;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.snmp4j.agent.BaseAgent#registerManagedObjects()
      * 
      * Register additional managed objects at the agent's server.
      */
     protected void registerManagedObjects() {
         logger.debug("Registers");
         try {
             mib.registerMOs(server, null);
         } catch (DuplicateRegistrationException e) {
             logger.error("Cannot register Mib", e);
         }        
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.snmp4j.agent.BaseAgent#unregisterManagedObjects()
      * 
      * Unregister the basic MIB modules from the agent's MOServer.
      */
     protected void unregisterManagedObjects() {
         logger.debug("Unregisters");
         mib.unregisterMOs(server, null);
     }
 
     /**
      * 
      * @param moGroup
      */
     public void unregisterManagedObject(MOGroup moGroup) {
         logger.debug("Unregister "+moGroup);
         moGroup.unregisterMOs(server, getContext(moGroup));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.snmp4j.agent.BaseAgent#addUsmUser(org.snmp4j.security.USM)
      * 
      * Adds all the necessary initial users to the USM. Only applicable to SNMP
      * V3
      */
     protected void addUsmUser(USM usm) {
         for (UsmUser userlist : listUsmUser) {
             logger.debug("User: "+userlist);
             usm.addUser(userlist.getSecurityName(), usm.getLocalEngineID(), userlist);
         }
         /*
          * Example
     user = new UsmUser(new OctetString("TEST"),
                                AuthSHA.ID,
                                new OctetString("maplesyrup"),
                                PrivDES.ID,
                                new OctetString("maplesyrup"));
     usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
     user = new UsmUser(new OctetString("SHA"),
                                AuthSHA.ID,
                                new OctetString("SHAAuthPassword"),
                                null,
                                null);
     usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
     user = new UsmUser(new OctetString("SHADES"),
                                AuthSHA.ID,
                                new OctetString("SHADESAuthPassword"),
                                PrivDES.ID,
                                new OctetString("SHADESPrivPassword"));
     usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
     user = new UsmUser(new OctetString("MD5DES"),
                                AuthMD5.ID,
                                new OctetString("MD5DESAuthPassword"),
                                PrivDES.ID,
                                new OctetString("MD5DESPrivPassword"));
     usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
     user = new UsmUser(new OctetString("SHAAES128"),
                                AuthSHA.ID,
                                new OctetString("SHAAES128AuthPassword"),
                                PrivAES128.ID,
                                new OctetString("SHAAES128PrivPassword"));
     usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
     user = new UsmUser(new OctetString("SHAAES192"),
                                AuthSHA.ID,
                                new OctetString("SHAAES192AuthPassword"),
                                PrivAES192.ID,
                                new OctetString("SHAAES192PrivPassword"));
     usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
     user = new UsmUser(new OctetString("SHAAES256"),
                                AuthSHA.ID,
                                new OctetString("SHAAES256AuthPassword"),
                                PrivAES256.ID,
                                new OctetString("SHAAES256PrivPassword"));
     usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
 
     user = new UsmUser(new OctetString("MD5AES128"),
                                AuthMD5.ID,
                                new OctetString("MD5AES128AuthPassword"),
                                PrivAES128.ID,
                                new OctetString("MD5AES128PrivPassword"));
     usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
     user = new UsmUser(new OctetString("MD5AES192"),
                                AuthMD5.ID,
                                new OctetString("MD5AES192AuthPassword"),
                                PrivAES192.ID,
                                new OctetString("MD5AES192PrivPassword"));
     usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
     user = new UsmUser(new OctetString("MD5AES256"),
                                AuthMD5.ID,
                                new OctetString("MD5AES256AuthPassword"),
                                PrivAES256.ID,
                                new OctetString("MD5AES256PrivPassword"));
     usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
 
          */
         UsmUser usernotify = new UsmUser(new OctetString(SnmpConfiguration.V3NOTIFY), 
                 null, null, null, null);
         usm.addUser(usernotify.getSecurityName(), null, usernotify);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.snmp4j.agent.BaseAgent#addNotificationTargets(org.snmp4j.agent.mo
      * .snmp.SnmpTargetMIB, org.snmp4j.agent.mo.snmp.SnmpNotificationMIB)
      * 
      * Adds initial notification targets and filters.
      */
     protected void addNotificationTargets(SnmpTargetMIB targetMIB,
             SnmpNotificationMIB notificationMIB) {
         targetMIB.addDefaultTDomains();
         
         for (TargetElement element : listTargetElements) {
             logger.debug("AddTarget: "+element);
             targetMIB.addTargetAddress(element.name, 
                     element.transportDomain, 
                     element.address,
                     element.timeout,
                     element.retries,
                     element.tagList,
                     element.params,
                     element.storageType);
         }
         /**
          * Example
 
     targetMIB.addTargetAddress(new OctetString("notificationV2c"),
                                TransportDomains.transportDomainUdpIpv4,
                                new OctetString(new UdpAddress("127.0.0.1/162").getValue()),
                                200, 1,
                                new OctetString("notify"),
                                new OctetString("v2c"),
                                StorageType.permanent);
     targetMIB.addTargetAddress(new OctetString("notificationV3"),
                                TransportDomains.transportDomainUdpIpv4,
                                new OctetString(new UdpAddress("127.0.0.1/1162").getValue()),
                                200, 1,
                                new OctetString("notify"),
                                new OctetString("v3notify"),
                                StorageType.permanent);
 
          */
         logger.debug("HasV2: "+hasV2+" HasV3: "+hasV3);
         if (hasV2) {
             targetMIB.addTargetParams(new OctetString(SnmpConfiguration.V2C),
                     MessageProcessingModel.MPv2c,
                     SecurityModel.SECURITY_MODEL_SNMPv2c, 
                     new OctetString("cpublic"), 
                     SecurityLevel.AUTH_PRIV,
                     StorageType.permanent);
         }
         if (hasV3) {
             targetMIB.addTargetParams(new OctetString(SnmpConfiguration.V3NOTIFY),
                     MessageProcessingModel.MPv3,
                     SecurityModel.SECURITY_MODEL_USM, 
                     new OctetString("v3notify"), 
                     SecurityLevel.NOAUTH_NOPRIV,
                     StorageType.permanent);
         }
         int trapOrInform = SnmpNotificationMIB.SnmpNotifyTypeEnum.inform;
         if (useTrap){
             trapOrInform = SnmpNotificationMIB.SnmpNotifyTypeEnum.trap;
         }
         notificationMIB.addNotifyEntry(new OctetString("default"),
                 new OctetString(SnmpConfiguration.NOTIFY),
                 trapOrInform,
                 StorageType.permanent);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.snmp4j.agent.BaseAgent#addViews(org.snmp4j.agent.mo.snmp.VacmMIB)
      * 
      * Minimal View based Access Control
      * 
      * http://www.faqs.org/rfcs/rfc2575.html
      */
     protected void addViews(VacmMIB vacm) {
         vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv1,
                 new OctetString("cpublic"),
                 new OctetString("v1v2group"),
                 StorageType.nonVolatile);
         vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c,
                 new OctetString("cpublic"),
                 new OctetString("v1v2group"),
                 StorageType.nonVolatile);
         vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                 new OctetString("v3notify"),
                 new OctetString("v3group"),
                 StorageType.nonVolatile);
         
         for (UsmUser user : listUsmUser) {
             logger.debug("Groups: "+user.getSecurityName()+" Restricted? "+(user.getPrivacyProtocol() == null));
             if (user.getPrivacyProtocol() == null) {
                 vacm.addGroup(SecurityModel.SECURITY_MODEL_USM, 
                         new OctetString(user.getSecurityName()), 
                         new OctetString("v3restricted"), 
                         StorageType.nonVolatile);
             } else {
                 vacm.addGroup(SecurityModel.SECURITY_MODEL_USM, 
                         new OctetString(user.getSecurityName()), 
                         new OctetString("v3group"), 
                         StorageType.nonVolatile);
             }
         }
 
         vacm.addAccess(new OctetString("v1v2group"), new OctetString("public"),
                 SecurityModel.SECURITY_MODEL_ANY,
                 SecurityLevel.NOAUTH_NOPRIV,
                 MutableVACM.VACM_MATCH_EXACT,
                 new OctetString("fullReadView"),
                 new OctetString("fullWriteView"),
                 new OctetString("fullNotifyView"),
                 StorageType.nonVolatile);
         vacm.addAccess(new OctetString("v3group"), new OctetString(),
                 SecurityModel.SECURITY_MODEL_USM,
                 SecurityLevel.AUTH_PRIV,
                 MutableVACM.VACM_MATCH_EXACT,
                 new OctetString("fullReadView"),
                 new OctetString("fullWriteView"),
                 new OctetString("fullNotifyView"),
                 StorageType.nonVolatile);
         vacm.addAccess(new OctetString("v3restricted"), new OctetString(),
                 SecurityModel.SECURITY_MODEL_USM,
                 SecurityLevel.NOAUTH_NOPRIV,
                 MutableVACM.VACM_MATCH_EXACT,
                 new OctetString("restrictedReadView"),
                 new OctetString("restrictedWriteView"),
                 new OctetString("restrictedNotifyView"),
                 StorageType.nonVolatile);
 
         vacm.addViewTreeFamily(new OctetString("fullReadView"), new OID("1.3"),
                 new OctetString(), VacmMIB.vacmViewIncluded,
                 StorageType.nonVolatile);
         vacm.addViewTreeFamily(new OctetString("fullWriteView"), new OID("1.3"),
                 new OctetString(), VacmMIB.vacmViewIncluded,
                 StorageType.nonVolatile);
         vacm.addViewTreeFamily(new OctetString("fullNotifyView"), new OID("1.3"),
                 new OctetString(), VacmMIB.vacmViewIncluded,
                 StorageType.nonVolatile);
 
         vacm.addViewTreeFamily(new OctetString("restrictedReadView"),
                 new OID("1.3.6.1.2"),
                 new OctetString(), VacmMIB.vacmViewIncluded,
                 StorageType.nonVolatile);
         vacm.addViewTreeFamily(new OctetString("restrictedWriteView"),
                 new OID("1.3.6.1.2.1"),
                 new OctetString(),
                 VacmMIB.vacmViewIncluded,
                 StorageType.nonVolatile);
         vacm.addViewTreeFamily(new OctetString("restrictedNotifyView"),
                 new OID("1.3.6.1.2"),
                 new OctetString(), VacmMIB.vacmViewIncluded,
                 StorageType.nonVolatile);
         vacm.addViewTreeFamily(new OctetString("restrictedNotifyView"),
                 new OID("1.3.6.1.6.3.1"),
                 new OctetString(), VacmMIB.vacmViewIncluded,
                 StorageType.nonVolatile);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.snmp4j.agent.BaseAgent#addCommunities(org.snmp4j.agent.mo.snmp.
      * SnmpCommunityMIB)
      * 
      * The table of community strings configured in the SNMP engine's Local
      * Configuration Datastore (LCD).
      * 
      * We only configure one, "public".
      */
     protected void addCommunities(SnmpCommunityMIB communityMIB) {
         Variable[] com2sec = new Variable[] {
                 new OctetString("public"),              // community name
                 new OctetString("cpublic"),              // security name
                 getAgent().getContextEngineID(),        // local engine ID
                 new OctetString("public"),              // default context name
                 new OctetString(),                      // transport tag
                 new Integer32(StorageType.nonVolatile), // storage type
                 new Integer32(RowStatus.active)         // row status
             };
         MOTableRow row =
             communityMIB.getSnmpCommunityEntry().createRow(
               new OctetString("public2public").toSubIndex(true), com2sec);
         communityMIB.getSnmpCommunityEntry().addRow(row);
         if (isFilterAccessEnabled) {
             snmpCommunityMIB.setSourceAddressFiltering(true);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.snmp4j.agent.BaseAgent#initTransportMappings()
      */
     @Override
     protected void initTransportMappings() throws IOException {
         TransportMapping []testMappings = new TransportMapping[address.length];
         int nb = 0;
         for (int i = 0; i < address.length; i++) {
             Address addr = GenericAddress.parse(address[i]);
             if (addr != null) {
                 logger.warn("SNMP Agent InitTransport: {} {}", addr.getClass().getSimpleName(), addr);
                 TransportMapping tm = TransportMappings.getInstance()
                         .createTransportMapping(addr);
                 if (tm != null) {
                     testMappings[nb] = tm;
                     nb++;
                 }
             }
         }
         transportMappings = new TransportMapping[nb];
         for (int i = 0; i < nb ; i++) {
             transportMappings[i] = testMappings[i];
         }
         testMappings = null;
     }
 
     /**
      * Start method invokes some initialization methods needed to start the
      * agent
      * 
      * @throws IOException
      */
     public void start() throws IOException {
         logger.debug("GgSnmpAgent starting: " + address[0]+" 1 on "+address.length);
         try {
             init();
         } catch (IOException e) {
             logger.warn("Error while SNMP starts ", e);
             throw e;
         }
         // This method reads some old config from a file and causes
         // unexpected behavior.
         // loadConfig(ImportModes.REPLACE_CREATE);
         addShutdownHook();
         getServer().addContext(new OctetString("public"));
         finishInit();
         run();
         if (trapLevel > TrapLevel.None.ordinal())
             sendColdStartNotification();
     }
 
     /* (non-Javadoc)
      * @see org.snmp4j.agent.BaseAgent#sendColdStartNotification()
      */
     @Override
     protected void sendColdStartNotification() {
         logger.warn("ColdStartNotification: {}",mib.getBaseOid());
         SNMPv2MIB snmpv2 = this.mib.getSNMPv2MIB();
         notificationOriginator.notify(
                 new OctetString("public"), SnmpConstants.coldStart,
                 new VariableBinding[] {
                     new VariableBinding(mib.getBaseOid(), new OctetString("Startup Service")),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getDescr()),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getObjectID()),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getUpTime()),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getContact()),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getName()),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getLocation()),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getServices())
             });
     }
     /**
      * Use Mib implementation to send the message
      * 
      * @param oid
      * @param message
      * @param number
      */
     public void notify(OID oid, String message, int number) {
         if (trapLevel < TrapLevel.Alert.ordinal())
             return;
         mib.notify(notificationOriginator, oid, message, number);
     }
     protected void sendShutdownNotification() {
         SNMPv2MIB snmpv2 = this.mib.getSNMPv2MIB();
         notificationOriginator.notify(
                 new OctetString("public"), SnmpConstants.linkDown,
                 new VariableBinding[] {
                     new VariableBinding(mib.getBaseOid(), new OctetString("Shutdown Service")),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getDescr()),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getObjectID()),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getUpTime()),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getContact()),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getName()),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getLocation()),
                     new VariableBinding(mib.getBaseOid(), snmpv2.getServices())
             });
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
         }
     }
     /*
      * (non-Javadoc)
      * 
      * @see org.snmp4j.agent.BaseAgent#stop()
      */
     @Override
     public void stop() {
         logger.warn("Stopping SNMP support");
         if (trapLevel > TrapLevel.None.ordinal()) {
             sendShutdownNotification();
         }
         super.stop();
        monitor.releaseResources();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
         }
         this.workerPool.cancel();
     }
 }

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
 package goldengate.snmp.r66;
 
 import goldengate.common.logging.GgInternalLogger;
 import goldengate.common.logging.GgInternalLoggerFactory;
 import goldengate.snmp.GgSnmpAgent;
 import goldengate.snmp.interf.GgInterfaceMib;
 import goldengate.snmp.utils.GgEntry;
 import goldengate.snmp.utils.GgMORow;
 import goldengate.snmp.utils.GgMOScalar;
 import goldengate.snmp.utils.GgUptime;
 import goldengate.snmp.utils.MemoryGauge32;
 import goldengate.snmp.utils.MemoryGauge32.MemoryType;
 
 import org.snmp4j.agent.DuplicateRegistrationException;
 import org.snmp4j.agent.MOServer;
 import org.snmp4j.agent.mo.MOAccessImpl;
 import org.snmp4j.agent.mo.snmp.SNMPv2MIB;
 import org.snmp4j.agent.mo.snmp.SysUpTime;
 import org.snmp4j.smi.Integer32;
 import org.snmp4j.smi.OID;
 import org.snmp4j.smi.OctetString;
 import org.snmp4j.smi.SMIConstants;
 import org.snmp4j.smi.TimeTicks;
 
 /**
  * Private MIB for GoldenGate OpenR66
  * 
  * @author Frederic Bregier
  * 
  */
 public abstract class GgPrivateMib implements GgInterfaceMib {
     /**
      * Internal Logger
      */
     private static GgInternalLogger logger = GgInternalLoggerFactory
             .getLogger(GgPrivateMib.class);
 
     // These are both standard in RFC-1213
     //SnmpConstants.sysDescr
     public String textualSysDecr = null;
     //SnmpConstants.sysObjectID
     public OID ggObjectId = null; // will be smiPrivateCode.typeGoldenGate
     //SnmpConstants.sysContact
     public String contactName = "Nobody";
     //SnmpConstants.sysName
     public String textualName = "OpenR66";
     //SnmpConstants.sysLocation
     public String address = "somewhere";
     //SnmpConstants.sysServices
     public int service = 72; // transport + application
     //SnmpConstants.sysUpTime
     public SysUpTime upTime = null;
     
     // need to add ".port" like "6666" Only in TCP (no UDP supported for GoldenGate)
     // example: rootEnterpriseMib+"66666"+".1.1.4.";
     public String applicationProtocolBase = null; 
     // will be = new OID(applicationProtocolBase+port);
     public OID applicationProtocol = null;
 
     // Private MIB: not published so take an OID probably not attributed
     public int smiPrivateCode = 66666;
     // identification of GoldenGate module
     public int smiTypeGoldengate = 66; // default = 66 = R66
     
     public String srootOIDGoldenGate;
     public OID rootOIDGoldenGate;
     // Used in Notify
     public OID rootOIDGoldenGateNotif;
     // Used in Notify Start or Shutdown
     public OID rootOIDGoldenGateNotifStartOrShutdown;
     // Info static part
     public OID rootOIDGoldenGateInfo;
     public GgMORow rowInfo;
     // General dynamic part
     public OID rootOIDGoldenGateGlobal;
     public GgMORow rowGlobal;
     // Uptime OID
     public OID rootOIDGoldenGateGlobalUptime;
     // Corresponding UpTime in Mib
     public GgMOScalar scalarUptime = null;
     // Detailed dynamic part
     public OID rootOIDGoldenGateDetailed;
     public GgMORow rowDetailed;
     // Error dynamic part
     public OID rootOIDGoldenGateError;
     public GgMORow rowError;
     
     // New SNMPV2 MIB
     public SNMPv2MIB snmpv2;
     // Corresponding agent
     public GgSnmpAgent agent;
 
     public GgPrivateMib(String sysdesc, int port, int smiPrivateCodeFinal,
                 int typeGoldenGateObject, String scontactName, String stextualName, 
                 String saddress, int iservice) {
         textualSysDecr = sysdesc;
         smiPrivateCode = smiPrivateCodeFinal;
         smiTypeGoldengate = typeGoldenGateObject;
         contactName = scontactName;
         textualName = stextualName;
         address = saddress;
         service = iservice;
        srootOIDGoldenGate = rootEnterpriseMib.toString()+"."+smiPrivateCode+"."+smiTypeGoldengate;
         applicationProtocolBase = srootOIDGoldenGate+".1.1.4.";
         ggObjectId = new OID(srootOIDGoldenGate+"."+smiTypeGoldengate);
         applicationProtocol = new OID(applicationProtocolBase+port);
         rootOIDGoldenGate = new OID(srootOIDGoldenGate);
         rootOIDGoldenGateInfo = new OID(srootOIDGoldenGate+".1");
         rootOIDGoldenGateGlobal = new OID(srootOIDGoldenGate+".2");
         rootOIDGoldenGateGlobalUptime = new OID(rootOIDGoldenGateGlobal.toString()+"."+
                 goldenGateGlobalValuesIndex.applUptime.getOID()+".0");
         rootOIDGoldenGateDetailed = new OID(srootOIDGoldenGate+".3");
         rootOIDGoldenGateError = new OID(srootOIDGoldenGate+".4");
         rootOIDGoldenGateNotif = new OID(srootOIDGoldenGate+".5.1");
         rootOIDGoldenGateNotifStartOrShutdown = new OID(srootOIDGoldenGate+".5.1.1.1");
     }
     
     /* (non-Javadoc)
      * @see goldengate.snmp.GgInterfaceMib#setAgent(goldengate.snmp.GgSnmpAgent)
      */
     @Override
     public void setAgent(GgSnmpAgent agent) {
         this.agent = agent;
     }
 
     
     /* (non-Javadoc)
      * @see goldengate.snmp.GgInterfaceMib#getBaseOidStartOrShutdown()
      */
     @Override
     public OID getBaseOidStartOrShutdown() {
         return rootOIDGoldenGateNotifStartOrShutdown;
     }
 
     /* (non-Javadoc)
      * @see goldengate.snmp.GgInterfaceMib#getSNMPv2MIB()
      */
     @Override
     public SNMPv2MIB getSNMPv2MIB() {
         return snmpv2;
     }
 
     /**
      * Unregister and Register again the SNMPv2MIB with System adapted to this Mib
      * @throws DuplicateRegistrationException
      */
     protected void agentRegisterSystem() throws DuplicateRegistrationException {
         // Since BaseAgent registers some mibs by default we need to unregister
         // one before we register our own sysDescr. Normally you would
         // override that method and register the mibs that you need
 
         agent.unregisterManagedObject(agent.getSnmpv2MIB());
         
         // Register a system description, use one from you product environment
         // to test with
         snmpv2 = new SNMPv2MIB(
                 new OctetString(textualSysDecr), 
                 ggObjectId, new Integer32(service));
         snmpv2.setContact(new OctetString(contactName));
         snmpv2.setLocation(new OctetString(address));
         snmpv2.setName(new OctetString(textualName));
         snmpv2.registerMOs(agent.getServer(), null);
         if (logger.isDebugEnabled()) {
             logger.debug("SNMPV2: "+snmpv2.getContact()+":"+snmpv2.getDescr()+":"+
                     snmpv2.getLocation()+":"+snmpv2.getName()+":"+
                     snmpv2.getObjectID()+":"+snmpv2.getServices()+":"+snmpv2.getUpTime());
         }
         // Save UpTime reference since used everywhere
         upTime = snmpv2.getSysUpTime();
     }
     
     /**
      * Register this MIB
      * @throws DuplicateRegistrationException
      */
     protected void defaultAgentRegisterGoldenGateMib() throws DuplicateRegistrationException {
         // register Static info
         rowInfo = new GgMORow(this, rootOIDGoldenGateInfo, goldenGateDefinition, 
                 MibLevel.staticInfo.ordinal());
         rowInfo.registerMOs(agent.getServer(), null);
         // register General info
         rowGlobal = new GgMORow(this, rootOIDGoldenGateGlobal, goldenGateGlobalValues, 
                 MibLevel.globalInfo.ordinal());
         GgMOScalar memoryScalar = rowGlobal.row[goldenGateGlobalValuesIndex.memoryTotal.ordinal()];
         memoryScalar.setValue(new MemoryGauge32(MemoryType.TotalMemory));
         memoryScalar = rowGlobal.row[goldenGateGlobalValuesIndex.memoryFree.ordinal()];
         memoryScalar.setValue(new MemoryGauge32(MemoryType.FreeMemory));
         memoryScalar = rowGlobal.row[goldenGateGlobalValuesIndex.memoryUsed.ordinal()];
         memoryScalar.setValue(new MemoryGauge32(MemoryType.UsedMemory));
         rowGlobal.registerMOs(agent.getServer(), null);
         // setup UpTime to SysUpTime and change status
         scalarUptime = rowGlobal.row[goldenGateGlobalValuesIndex.applUptime.ordinal()];
         scalarUptime.setValue(new GgUptime(upTime));
         changeStatus(OperStatus.restarting);
         changeStatus(OperStatus.up);
         // register Detailed info
         rowDetailed = new GgMORow(this, rootOIDGoldenGateDetailed, goldenGateDetailedValues, 
                 MibLevel.detailedInfo.ordinal());
         rowDetailed.registerMOs(agent.getServer(), null);
         // register Error info
         rowError = new GgMORow(this, rootOIDGoldenGateError, goldenGateErrorValues, 
                 MibLevel.errorInfo.ordinal());
         rowError.registerMOs(agent.getServer(), null);
     }
     /**
      * Register this MIB
      * @throws DuplicateRegistrationException
      */
     protected abstract void agentRegisterGoldenGateMib() throws DuplicateRegistrationException;
     /**
      * Unregister this MIB
      */
     protected void agentUnregisterMibs() {
         logger.debug("UnRegisterGoldenGate");
         rowInfo.unregisterMOs(agent.getServer(), agent.getDefaultContext());
         rowGlobal.unregisterMOs(agent.getServer(), agent.getDefaultContext());
         rowDetailed.unregisterMOs(agent.getServer(), agent.getDefaultContext());
         rowError.unregisterMOs(agent.getServer(), agent.getDefaultContext());
     }
     
     /* (non-Javadoc)
      * @see org.snmp4j.agent.MOGroup#registerMOs(org.snmp4j.agent.MOServer, org.snmp4j.smi.OctetString)
      */
     @Override
     public void registerMOs(MOServer server, OctetString context)
             throws DuplicateRegistrationException {
         agentRegisterSystem();
         agentRegisterGoldenGateMib();
     }
     /* (non-Javadoc)
      * @see org.snmp4j.agent.MOGroup#unregisterMOs(org.snmp4j.agent.MOServer, org.snmp4j.smi.OctetString)
      */
     @Override
     public void unregisterMOs(MOServer server, OctetString context) {
         agentUnregisterMibs();
     }
     /**
      * Change the status and the LastChange Timeticks
      * @param status
      */
     public void changeStatus(OperStatus status) {
         GgMOScalar statusScalar = 
             rowGlobal.row[goldenGateGlobalValuesIndex.applOperStatus.ordinal()];
         Integer32 var = (Integer32) statusScalar.getValue();
         if (var.getValue() != status.status) {
             var.setValue(status.status);
             GgMOScalar lastTimeScalar =
                 rowGlobal.row[goldenGateGlobalValuesIndex.applLastChange.ordinal()];
             TimeTicks time = (TimeTicks) lastTimeScalar.getValue();
             time.setValue(upTime.get().getValue());
         }
     }
     // MIB entry levels
     public static enum MibLevel {
         staticInfo,
         globalInfo,
         detailedInfo,
         errorInfo,
         trapInfo
     }
     // From now the MIB definition
     public static enum NotificationElements {
         TrapShutdown(1),
         TrapError(2),
         TrapWarning(3),
         TrapOverloaded(4),
         InfoTask(5);
         
         public int []oid;
         private NotificationElements(int oid) {
             this.oid = new int[]{oid};
         }
         public OID getOID(OID oidBase) {
             return new OID(oidBase.getValue(), this.oid);
         }
         public OID getOID(OID oidBase, int rank) {
             int []ids = new int[] { this.oid[0], rank };
             return new OID(oidBase.getValue(), ids);
         }
     }
 
     public static enum NotificationTasks {
         globalStepInfo, stepInfo, rankFileInfo, stepStatusInfo, filenameInfo, 
         originalNameInfo, idRuleInfo, modeTransInfo, retrieveModeInfo, startTransInfo, 
         infoStatusInfo, requesterInfo, requestedInfo, specialIdInfo;
         
         public int getOID() {
             return this.ordinal()+1;
         }
     }
     public static enum goldenGateDefinitionIndex {
         applName,
         applServerName,
         applVersion,
         applDescription,
         applURL,
         applApplicationProtocol;
         
         public int getOID() {
             return this.ordinal()+1;
         }
     }
     public static GgEntry [] goldenGateDefinition =
     {
         //applName
         new GgEntry(SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_ONLY),
         //applServerName
         new GgEntry(SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_ONLY),
         //applVersion
         new GgEntry(SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_ONLY),
         //applDescription
         new GgEntry(SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_ONLY),
         //applURL
         new GgEntry(SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_ONLY),
         //applApplicationProtocol
         new GgEntry(SMIConstants.SYNTAX_OBJECT_IDENTIFIER, MOAccessImpl.ACCESS_READ_ONLY)
     };
     
     public static enum goldenGateGlobalValuesIndex {
         applUptime,
         applOperStatus,
         applLastChange,
         applInboundAssociations,
         applOutboundAssociations,
         applAccumInboundAssociations,
         applAccumOutboundAssociations,
         applLastInboundActivity,
         applLastOutboundActivity,
         applRejectedInboundAssociations,
         applFailedOutboundAssociations,
         applInboundBandwidthKBS,
         applOutboundBandwidthKBS,
         nbInfoUnknown,
         nbInfoNotUpdated,
         nbInfoInterrupted,
         nbInfoToSubmit,
         nbInfoError,
         nbInfoRunning,
         nbInfoDone,
         nbStepAllTransfer,
         memoryTotal,
         memoryFree,
         memoryUsed,
         nbThreads,
         nbNetworkConnection;
         
         public int getOID() {
             return this.ordinal()+1;
         }        
     }
     public static GgEntry [] goldenGateGlobalValues =
     {
         //applUptime
         new GgEntry(SMIConstants.SYNTAX_TIMETICKS, MOAccessImpl.ACCESS_READ_ONLY),
         //applOperStatus
         new GgEntry(SMIConstants.SYNTAX_INTEGER, MOAccessImpl.ACCESS_READ_ONLY),
         //applLastChange
         new GgEntry(SMIConstants.SYNTAX_TIMETICKS, MOAccessImpl.ACCESS_READ_ONLY),
         //applInboundAssociations
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //applOutboundAssociations
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //applAccumInboundAssociations
         new GgEntry(SMIConstants.SYNTAX_COUNTER32, MOAccessImpl.ACCESS_READ_ONLY),
         //applAccumOutboundAssociations
         new GgEntry(SMIConstants.SYNTAX_COUNTER32, MOAccessImpl.ACCESS_READ_ONLY),
         //applLastInboundActivity
         new GgEntry(SMIConstants.SYNTAX_TIMETICKS, MOAccessImpl.ACCESS_READ_ONLY),
         //applLastOutboundActivity
         new GgEntry(SMIConstants.SYNTAX_TIMETICKS, MOAccessImpl.ACCESS_READ_ONLY),
         //applRejectedInboundAssociations
         new GgEntry(SMIConstants.SYNTAX_COUNTER32, MOAccessImpl.ACCESS_READ_ONLY),
         //applFailedOutboundAssociations
         new GgEntry(SMIConstants.SYNTAX_COUNTER32, MOAccessImpl.ACCESS_READ_ONLY),
         // Bandwidth
         //applInboundBandwidthKBS
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //applOutboundBandwidthKBS
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         // Overall status including past, future and current transfers
         //nbInfoUnknown
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbInfoNotUpdated
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbInfoInterrupted
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbInfoToSubmit
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbInfoError
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbInfoRunning
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbInfoDone
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         // Current situation of all transfers, running or not
         //nbStepAllTransfer
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //memoryTotal
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //memoryFree
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //memoryUsed
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbThreads
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbNetworkConnection
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY)
     };
     
     public static enum goldenGateDetailedValuesIndex {
         nbStepNotask,
         nbStepPretask,
         nbStepTransfer,
         nbStepPosttask,
         nbStepAllDone,
         nbStepError,
         nbAllRunningStep,
         nbRunningStep,
         nbInitOkStep,
         nbPreProcessingOkStep,
         nbTransferOkStep,
         nbPostProcessingOkStep,
         nbCompleteOkStep;
         
         public int getOID() {
             return this.ordinal()+1;
         }
     }
     public static GgEntry [] goldenGateDetailedValues =
     {
         //nbStepNotask
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStepPretask
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStepTransfer
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStepPosttask
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStepAllDone
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStepError
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         // First on Running Transfers only
         //nbAllRunningStep
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbRunningStep
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbInitOkStep
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbPreProcessingOkStep
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbTransferOkStep
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbPostProcessingOkStep
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbCompleteOkStep
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY)
     };
     public static enum goldenGateErrorValuesIndex {
         nbStatusConnectionImpossible,
         nbStatusServerOverloaded,
         nbStatusBadAuthent,
         nbStatusExternalOp,
         nbStatusTransferError,
         nbStatusMD5Error,
         nbStatusDisconnection,
         nbStatusFinalOp,
         nbStatusUnimplemented,
         nbStatusInternal,
         nbStatusWarning,
         nbStatusQueryAlreadyFinished,
         nbStatusQueryStillRunning,
         nbStatusNotKnownHost,
         nbStatusQueryRemotelyUnknown,
         nbStatusCommandNotFound,
         nbStatusPassThroughMode,
         nbStatusRemoteShutdown,
         nbStatusShutdown,
         nbStatusRemoteError,
         nbStatusStopped,
         nbStatusCanceled,
         nbStatusFileNotFound,
         nbStatusUnknown;
         
         public int getOID() {
             return this.ordinal()+1;
         }
     }
     public static GgEntry [] goldenGateErrorValues =
     {
         // Error Status on all transfers
         //nbStatusConnectionImpossible
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusServerOverloaded
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusBadAuthent
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusExternalOp
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusTransferError
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusMD5Error
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusDisconnection
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusFinalOp
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusUnimplemented
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusInternal
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusWarning
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusQueryAlreadyFinished
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusQueryStillRunning
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusNotKnownHost
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusQueryRemotelyUnknown
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusCommandNotFound
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusPassThroughMode
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusRemoteShutdown
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusShutdown
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusRemoteError
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusStopped
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusCanceled
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusFileNotFound
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY),
         //nbStatusUnknown
         new GgEntry(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY)
     };
 
     public static enum OperStatus {
        up(1),
        down(2),
        halted(3),
        congested(4),
        restarting(5),
        quiescing(6);
        
        public int status;
        
        private OperStatus(int status) {
            this.status = status;
        }
     }
 }

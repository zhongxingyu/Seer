 package com.dwipal;
 
 import java.util.*;
 import org.snmp4j.*;
 import org.snmp4j.event.*;
 import org.snmp4j.mp.*;
 import org.snmp4j.smi.*;
 import org.snmp4j.transport.*;
 
 
 public class SnmpLib implements ISnmpLib {
 
   private String m_host;
   private int m_port;
   private String m_readCommunity;
   private String m_writeCommunity;
   private ISnmpResponseHandler m_snmpResponseHandler;
   private boolean m_sessionDestroyed;
   private CommunityTarget m_target;
 
   public SnmpLib() {
     m_target=null;
   }
 
   public String getHost() {
     return m_host;
   }
   public int getPort() {
     return m_port;
   }
   public String getReadCommunity() {
     return m_readCommunity;
   }
   public String getWriteCommunity() {
     return m_writeCommunity;
   }
   public void setCommunity(String readCommunity, String writeCommunity) {
     m_readCommunity=readCommunity;
     m_writeCommunity=writeCommunity;
   }
   public void setHost(String host) {
     m_target=null;
     m_host=host;
   }
   public void setPort(int port) {
     m_target=null;
     m_port=port;
   }
   public void setSnmpResponseHandler(ISnmpResponseHandler snmpResponseHandler) {
     m_snmpResponseHandler=snmpResponseHandler;
   }
   public ISnmpResponseHandler getSnmpResponseHandler() {
     return m_snmpResponseHandler;
   }
   public void destroySession() {
     m_sessionDestroyed=true;
   }
  private Target getTarget(String strCommunity) {
     if(m_target==null) {
       Address addr = GenericAddress.parse("udp:" + getHost() + "/" + getPort());
       m_target = new CommunityTarget();
      m_target.setCommunity(new OctetString(strCommunity));
       m_target.setAddress(addr);
       m_target.setVersion(SnmpConstants.version1);
       m_target.setRetries(3);
     }
     return m_target;
   }
 
   public void snmpWalk(String oidFrom, String oidTo) throws SnmpException {
     snmpWalk(oidFrom);
   }
 
   public void snmpWalk(String oidFrom) throws SnmpException {
     PDU request=new PDU();
     request.setType(PDU.GETNEXT);
     request.add(new VariableBinding(new OID(oidFrom)));
     request.setNonRepeaters(0);
     OID rootOID = request.get(0).getOid();
     PDU response = null;
 
     int objects = 0;
     int requests = 0;
     long startTime = System.currentTimeMillis();
 
     try {
       Snmp snmp=new Snmp(new DefaultUdpTransportMapping());
       snmp.listen();
       m_sessionDestroyed=false;
       do {
         requests++;
        ResponseEvent responseEvent = snmp.send(request, getTarget(getReadCommunity()));
         response = responseEvent.getResponse();
         if (response != null) {
           objects += response.size();
         }
       }
       while (!processWalk(response, request, rootOID) && !m_sessionDestroyed);
 
     } catch (SnmpException e) {
       throw e;
     } catch (Exception e) {
       e.printStackTrace();
       throw new SnmpException(e.getMessage());
     }
     if(m_snmpResponseHandler != null) {
       m_snmpResponseHandler.requestStats(requests, objects, System.currentTimeMillis()-startTime);
     }
   }
 
   private boolean processWalk(PDU response, PDU request, OID rootOID) throws SnmpException {
     if ((response == null) || (response.getErrorStatus() != 0) ||
         (response.getType() == PDU.REPORT)) {
       return true;
     }
     boolean finished = false;
     OID lastOID = request.get(0).getOid();
     for (int i=0; (!finished) && (i<response.size()); i++) {
       VariableBinding vb = response.get(i);
       if ((vb.getOid() == null) ||
           (vb.getOid().size() < rootOID.size()) ||
           (rootOID.leftMostCompare(rootOID.size(), vb.getOid()) != 0)) {
         finished = true;
       }
       else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {
         outputResponse(vb);
         finished = true;
       }
       else if (vb.getOid().compareTo(lastOID) <= 0) {
         throw new SnmpException("Variable received is not lexicographic successor of requested one:" + vb.toString() + " <= "+lastOID);
       }
       else {
         outputResponse(vb);
         lastOID = vb.getOid();
       }
     }
     if (response.size() == 0) {
       finished = true;
     }
     if (!finished) {
       VariableBinding next = response.get(response.size()-1);
       next.setVariable(new Null());
       request.set(0, next);
       request.setRequestID(new Integer32(0));
     }
     return finished;
   }
 
   private SnmpOidValuePair outputResponse(VariableBinding vb) {
     SnmpOidValuePair oidval=new SnmpOidValuePair();
     oidval.oid=vb.getOid().toString();
     oidval.value_str=vb.getVariable().toString();
     if(m_snmpResponseHandler != null) {
       m_snmpResponseHandler.responseReceived(oidval);
     }
     return oidval;
   }
 
   void snmpSetValue(String oid, int syntax, String value) throws SnmpException {
     VariableBinding varbind = getVarBindForSetRequest(oid, syntax, value);
 
     PDU request = new PDU();
     request.setType(PDU.SET);
     request.add(varbind);
     PDU response = null;
 
     long startTime = System.currentTimeMillis();
 
     try {
       Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
       snmp.listen();
      ResponseEvent responseEvent = snmp.send(request, getTarget(getWriteCommunity()));
       response = responseEvent.getResponse();
       System.out.println(response);
     }
     catch (Exception e) {
       e.printStackTrace();
       throw new SnmpException(e.getMessage());
     }
   }
 
 
 
 
   VariableBinding getVarBindForSetRequest(String oid, int type, String value) {
     VariableBinding vb = new VariableBinding(new OID(oid));
 
     if (value != null) {
       Variable variable;
       switch (type) {
         case DwSnmpMibRecord.VALUE_TYPE_INTEGER32:
           variable = new Integer32(Integer.parseInt(value));
           break;
         case DwSnmpMibRecord.VALUE_TYPE_UNSIGNED_INTEGER32:
           variable = new UnsignedInteger32(Long.parseLong(value));
           break;
         case DwSnmpMibRecord.VALUE_TYPE_OCTET_STRING:
           variable = new OctetString(value);
           break;
         case DwSnmpMibRecord.VALUE_TYPE_NULL:
           variable = new Null();
           break;
         case DwSnmpMibRecord.VALUE_TYPE_OID:
           variable = new OID(value);
           break;
         case DwSnmpMibRecord.VALUE_TYPE_TIMETICKS:
           variable = new TimeTicks(Long.parseLong(value));
           break;
         case DwSnmpMibRecord.VALUE_TYPE_IP_ADDRESS:
           variable = new IpAddress(value);
           break;
         default:
           throw new IllegalArgumentException("Variable type " + type +
                                              " not supported");
       }
       vb.setVariable(variable);
     }
     return vb;
   }
 
 }

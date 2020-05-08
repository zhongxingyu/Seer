 package nl.surfnet.bod.snmp;
 
 import java.util.Date;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.snmp4j.CommunityTarget;
 import org.snmp4j.PDU;
 import org.snmp4j.Snmp;
 import org.snmp4j.TransportMapping;
 import org.snmp4j.mp.SnmpConstants;
 import org.snmp4j.smi.IpAddress;
 import org.snmp4j.smi.OID;
 import org.snmp4j.smi.OctetString;
 import org.snmp4j.smi.UdpAddress;
 import org.snmp4j.smi.VariableBinding;
 import org.snmp4j.transport.DefaultUdpTransportMapping;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 @Component
 public class SnmpAgent {
 
   private final Logger log = LoggerFactory.getLogger(getClass());
 
   public static final String SEVERITY_MAJOR = "Major";
 
   @Value("${snmp.community}")
   private String community;
 
   @Value("${snmp.oid.nms.port.disappeared}")
   private String oidNmsPortDisappeared;
 
   @Value("${snmp.oid.idd.institute.disappeared}")
   private String oidIddInstituteDisappeared;
 
   @Value("${snmp.host}")
   private String host;
 
   @Value("${snmp.port}")
   private String port;
 
   @Value("${snmp.retries}")
   private int retries;
 
   @Value("${snmp.timeout.millis}")
   private long timeoutInMillis;
 
   public void sendPdu(final PDU pdu) {
     try {
      final TransportMapping transportMapping = new DefaultUdpTransportMapping();
       transportMapping.listen();
 
       final CommunityTarget communityTarget = new CommunityTarget();
       communityTarget.setCommunity(new OctetString(community));
       communityTarget.setVersion(SnmpConstants.version2c);
 
       communityTarget.setAddress(new UdpAddress(host + port));
       communityTarget.setRetries(retries);
       communityTarget.setTimeout(timeoutInMillis);
 
       final Snmp snmp = new Snmp(transportMapping);
       log.info("Sending v2 trap: {} to community: {}", pdu, communityTarget);
       snmp.send(pdu, communityTarget);
       snmp.close();
     }
     catch (Exception e) {
       log.error("Error: ", e);
     }
   }
 
   public PDU getPdu(final String oid, final String severity, final int pduType) {
     final PDU pdu = new PDU();
     pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new OctetString(new Date().toString())));
     pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
     pdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress, new IpAddress(host)));
     pdu.add(new VariableBinding(new OID(oid), new OctetString(severity)));
     pdu.setType(pduType);
     return pdu;
   }
 
   public final String getOidNmsPortDisappeared(final String nmsPortId) {
     return oidNmsPortDisappeared + "." + nmsPortId;
   }
 
   public final String getOidIddInstituteDisappeared(final String instituteId) {
     return oidIddInstituteDisappeared + "." + instituteId;
   }
 
 }

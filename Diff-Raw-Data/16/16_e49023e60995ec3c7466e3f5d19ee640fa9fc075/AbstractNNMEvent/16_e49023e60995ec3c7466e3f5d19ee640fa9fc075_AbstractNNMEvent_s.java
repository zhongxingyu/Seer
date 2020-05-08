 package org.opennms.opennmsd;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 public class AbstractNNMEvent implements NNMEvent {
 
     protected static long m_startTime = System.currentTimeMillis();
     private String m_category;
     private String m_name;
     private String m_severity;
     private String m_sourceAddress;
     private Date m_timeStamp;
     private String m_community;
     private String m_enterpriseId;
     private int m_generic;
     private int m_specific;
     private String m_snmpHost;
     private int m_version;
     private List m_varBinds;
 
     protected void addVarBind(NNMVarBind varBind) {
         if (m_varBinds == null) {
             m_varBinds = new LinkedList();
         }
         
         m_varBinds.add(varBind);
         
     }
 
     public String getCategory() {
         return m_category;
     }
 
     public void setCategory(String category) {
         m_category = category;
     }
 
     public String getName() {
         return m_name;
     }
 
     public void setName(String name) {
         m_name = name;
     }
 
     public String getSeverity() {
         return m_severity;
     }
 
     public void setSeverity(String severity) {
         m_severity = severity;
     }
 
     public String getSourceAddress() {
         return m_sourceAddress;
     }
 
     public void setSourceAddress(String sourceAddress) {
         m_sourceAddress = sourceAddress;
     }
 
     public Date getTimeStamp() {
         return m_timeStamp;
     }
 
     public void setTimeStamp(Date timeStamp) {
         m_timeStamp = timeStamp;
     }
 
     public String getCommunity() {
         return m_community;
     }
 
     public void setCommunity(String community) {
         m_community = community;
     }
 
     public String getEnterpriseId() {
         return m_enterpriseId;
     }
 
     public void setEnterpriseId(String enterpriseId) {
         m_enterpriseId = enterpriseId;
     }
 
     public int getGeneric() {
         return m_generic;
     }
 
     public void setGeneric(int generic) {
         m_generic = generic;
     }
 
     public int getSpecific() {
         return m_specific;
     }
 
     public void setSpecific(int specific) {
         m_specific = specific;
     }
 
     public String getSnmpHost() {
         return m_snmpHost;
     }
 
     public void setSnmpHost(String snmpHost) {
         m_snmpHost = snmpHost;
     }
 
     public int getVersion() {
         return m_version;
     }
 
     public void setVersion(int version) {
         m_version = version;
     }
 
     public List getVarBinds() {
         return m_varBinds;
     }
 
     public void setVarBinds(List varBinds) {
         m_varBinds = varBinds;
     }
 
     public String getEventConfigurationKey() {
         if (m_generic == 6) {
             return m_enterpriseId+".0."+m_specific;
         } else {
            return m_enterpriseId+"."+(m_specific+1);
         }
     }
     
     public String toString() {
         return "NNMEvent[name="+getName()+", address="+getSourceAddress()+", category="+getCategory()+", severity="+getSeverity()+", key="+getEventConfigurationKey()+"]";
     }
 
 
 }

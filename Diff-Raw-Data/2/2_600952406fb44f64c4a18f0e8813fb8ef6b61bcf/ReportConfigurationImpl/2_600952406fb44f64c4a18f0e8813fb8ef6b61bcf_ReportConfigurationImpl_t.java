 package info.mikaelsvensson.ftpbackup.model.impl;
 
 import info.mikaelsvensson.ftpbackup.model.ReportConfiguration;
 import info.mikaelsvensson.ftpbackup.model.ReportType;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlValue;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 public class ReportConfigurationImpl implements ReportConfiguration {
     private ReportType type;
     private Collection<ReportConfigurationParameter> params;
 
     public ReportConfigurationImpl() {
     }
 
     @Override
     @XmlAttribute
     public ReportType getType() {
         return type;
     }
 
     @Override
     public Map<String, String> getParameters() {
         HashMap<String, String> map = new HashMap<>();
         for (ReportConfigurationParameter param : params) {
            map.put(param.getKey().trim(), param.getValue().trim());
         }
         return map;
     }
 
     public void setType(ReportType type) {
         this.type = type;
     }
 
     @XmlElement(name = "param")
     public Collection<ReportConfigurationParameter> getParams() {
         return params;
     }
 
     public void setParams(Collection<ReportConfigurationParameter> params) {
         this.params = params;
     }
 
     public static class ReportConfigurationParameter {
         private String key;
         private String value;
 
         @XmlAttribute(name = "name", required = true)
         public String getKey() {
             return key;
         }
 
         public void setKey(String key) {
             this.key = key;
         }
 
         @XmlValue
         public String getValue() {
             return value;
         }
 
         public void setValue(String value) {
             this.value = value;
         }
     }
 }

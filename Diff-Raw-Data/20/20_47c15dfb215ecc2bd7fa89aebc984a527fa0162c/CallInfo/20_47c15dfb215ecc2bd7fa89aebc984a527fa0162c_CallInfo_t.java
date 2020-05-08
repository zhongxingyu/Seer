 package ar.edu.unicen.ringo.agent.api;
 
 import java.io.Serializable;
 
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * Represents the information logged into the agent about a service call.
  *
  * @author psaavedra
  */
 @XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
 public class CallInfo implements Serializable {
 
     /**
      * Serial version.
      */
     private static final long serialVersionUID = -8547778257252654854L;
 
     @XmlElement
     private String sla;
     @XmlElement
     private String node;
     @XmlElement
     private String method;
     @XmlElement
     private int executionTime;
     @XmlElement
     private long timestamp;
     public String getSla() {
         return sla;
     }
     public void setSla(String sla) {
         this.sla = sla;
     }
     public String getNode() {
         return node;
     }
     public void setNode(String node) {
         this.node = node;
     }
     public String getMethod() {
         return method;
     }
     public void setMethod(String method) {
         this.method = method;
     }
     public int getExecutionTime() {
         return executionTime;
     }
     public void setExecutionTime(int executionTime) {
         this.executionTime = executionTime;
     }
     public long getTimestamp() {
         return timestamp;
     }
     public void setTimestamp(long timestamp) {
         this.timestamp = timestamp;
     }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CallInfo [sla=").append(sla).append(", node=")
                .append(node).append(", method=").append(method)
                .append(", executionTime=").append(executionTime)
                .append(", timestamp=").append(timestamp).append("]");
        return builder.toString();
    }
 
 }

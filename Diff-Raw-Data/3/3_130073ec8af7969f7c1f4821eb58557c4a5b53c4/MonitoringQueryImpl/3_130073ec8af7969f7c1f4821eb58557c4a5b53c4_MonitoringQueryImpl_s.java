 /**
  * 
  */
 package eu.indenica.monitoring;
 
 import java.util.Arrays;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.osoa.sca.annotations.Property;
 import org.slf4j.Logger;
 
 import eu.indenica.common.LoggerFactory;
 
 /**
  * Default monitoring query implementation
  * 
  * @author Christian Inzinger
  * 
  */
 @XmlRootElement
 public class MonitoringQueryImpl implements MonitoringQuery {
     private final static Logger LOG = LoggerFactory.getLogger();
 
     protected String name;
     protected String[] inputEventTypes;
     protected String[] outputEventTypes;
     protected String statement;
 
     /**
      * @see eu.indenica.monitoring.MonitoringQuery#getName()
      */
     @Override
     public String getName() {
        // TODO Auto-generated method stub
        return null;
     }
 
     /**
      * @param name
      *            the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * @see eu.indenica.monitoring.MonitoringQuery#getOutputEventTypes()
      */
     @Override
     public String[] getOutputEventTypes() {
         return outputEventTypes;
     }
 
     /**
      * @param outputEventTypes
      *            the outputEventTypes to set
      */
     @Property
     public void setOutputEventTypes(String[] outputEventTypes) {
         this.outputEventTypes = outputEventTypes;
     }
 
     /**
      * @see eu.indenica.monitoring.MonitoringQuery#getInputEventTypes()
      */
     @Override
     public String[] getInputEventTypes() {
         return inputEventTypes;
     }
 
     /**
      * @param inputEventTypes
      *            the inputEventTypes to set
      */
     @Property
     public void setInputEventTypes(String[] inputEventTypes) {
         LOG.debug("Set input event types to {}", inputEventTypes);
         this.inputEventTypes = inputEventTypes;
     }
 
     /**
      * @see eu.indenica.monitoring.MonitoringQuery#getStatement()
      */
     @Override
     public String getStatement() {
         return statement;
     }
 
     /**
      * @param statement
      *            the statement to set
      */
     @Property
     public void setStatement(String statement) {
         this.statement = statement;
     }
 
     /**
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         StringBuilder result =
                 new StringBuilder().append("#<")
                         .append(getClass().getSimpleName()).append(":");
         if(getStatement() != null)
             result.append(" statement: '")
                     .append(getStatement().replaceAll("\\s+", " ").trim())
                     .append("'");
 
         if(getInputEventTypes() != null)
             result.append(", inputEventTypes: ").append(
                     Arrays.toString(getInputEventTypes()));
 
         if(getOutputEventTypes() != null)
             result.append(", outputEventTypes: ").append(
                     Arrays.toString(getOutputEventTypes()));
 
         result.append(">");
         return result.toString();
     }
 }

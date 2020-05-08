 package org.xbrlapi.aspects.alt;
 
 import java.net.URI;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Element;
 import org.xbrlapi.Scenario;
 import org.xbrlapi.utilities.XBRLException;
 
 public class ScenarioAspectValue extends AspectValueImpl implements AspectValue {
 
     /**
      * 
      */
     private static final long serialVersionUID = 6282654205736211983L;
 
     protected final static Logger logger = Logger
     .getLogger(ScenarioAspectValue.class);
 
     /** 
      * The list of child elements of the segment or null if
      * the segment aspect value is missing.
      */
     List<Element> children = null;
     
     /**
      * @return the list of child elements in the segment, in document order.
      */
     public List<Element> getChildren() {
         return children;
     }
 
     /**
      * Missing aspect value constructor.
      */
     public ScenarioAspectValue() {
         super();
     }
 
     /**
     * @param scenario The scenario fragment.
      */
     public ScenarioAspectValue(Scenario scenario) throws XBRLException {
         super();
         if (scenario != null) {
             this.children = scenario.getChildElements();
         }
     }
     
     /**
      * @see AspectHandler#getAspectId()
      */
     public URI getAspectId() {
         return ScenarioAspect.ID;
     }
     
     /**
      * @see AspectValue#isMissing()
      */
     public boolean isMissing() {
         return getChildren() == null;
     }
 
     /**
      * The missing aspect value ID is the empty string.
      * @see AspectValue#getId()
      */
     String id = null;
     public String getId() {
         if (id != null) return id;
         if (isMissing()) id = "";
         else id = IDGenerator.getLabel(children);
         return id;
     }
     
 }

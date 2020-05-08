 /**
  * 
  */
 package eu.indenica.events;
 
 /**
  * Sample event
  * 
  * @author Christian Inzinger
  */
 public class EventOne extends Event {
     private static final long serialVersionUID = 2114845083753269316L;
     private static final long typeId = System.currentTimeMillis();
     private String attr1;
     private int anAttribute;
 
     public EventOne() {
         super("test.EventOne." + typeId);
     }
 
     public void setAttr1(String attr1) {
         this.attr1 = attr1;
     }
 
     public void setAnAttribute(int anAttribute) {
         this.anAttribute = anAttribute;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return new StringBuilder().append("#<")
                 .append(getClass().getName()).append(": ")
                 .append("attr1: ").append(attr1).append(", anAttribute: ")
                 .append(anAttribute).append(">").toString();
     }
 }

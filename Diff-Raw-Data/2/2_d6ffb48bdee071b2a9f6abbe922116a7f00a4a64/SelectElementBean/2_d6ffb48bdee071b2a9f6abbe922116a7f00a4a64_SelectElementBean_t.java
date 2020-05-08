 /**
  * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.tags.web.element;
 
 public abstract class SelectElementBean extends NameValueDisableBean implements Selectable {
 
     protected boolean selected = false;
 
     public SelectElementBean() {
         super();
     }
 
     public SelectElementBean(String name, String value, boolean selected, boolean disabled) {
         super(name, value, disabled);
         this.selected = selected;
     }
 
 
     protected String checkSelected(String select) {
         if (selected) {
             return " " + select + "='" + select + "' ";
         } else {
             return "";
         }
     }
 
     /**
      * Sets the selected status of the bean.
      * @param flag status of the bean
      */
     public void setSelected(boolean flag) {
         this.selected = flag;
     }
 
     /**
      * Returns the selected status of the bean
      * @return selected status
      */
     public boolean isSelected() {
         return selected;
     }
 
     public String toString(String type) {
         return "<input type='" + type + "' name='" + getTagName()+name + "' value='" + value + "' " + checkDisabled() + " " + checkSelected("checked") +
                 "/>";
     }
 
     /**
      * Updates the value of the selectelement to a certain status.
      *
      * @param values array containing updated values for the element
      */
     public void update(String[] values) {
        this.selected = (values == null) ? false : true;
     }
 }

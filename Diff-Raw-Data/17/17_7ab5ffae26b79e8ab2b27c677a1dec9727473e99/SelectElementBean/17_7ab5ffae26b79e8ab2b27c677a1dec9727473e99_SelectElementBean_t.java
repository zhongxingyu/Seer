 /**
  * @author <a href="mailto:wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.provider.portletui.beans;
 
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The abstract <code>SelectElementBean</code> provides a base selectable element that is subclassed by
  * <code>RadioButtonBean</code>, <code>CheckBoxBean</code> and <code>ListBoxItemBean</code>
  */
 public abstract class SelectElementBean extends BaseComponentBean implements TagBean {
 
     protected boolean selected = false;
     protected List results = new ArrayList();
 
     /**
      * Constructs a default select element bean
      */
     public SelectElementBean() {
         super();
     }
 
     /**
      * Constructs a select element bean from a supplied visual bean type
      *
      * @param vbName the visual bean type
      */
     public SelectElementBean(String vbName) {
         super(vbName);
     }
 
     /**
      * Returns a String used in the final markup indicating if this bean is selected or not
      *
      * @param select the selected String
      * @return the String used in the final markup indicating if this bean is selected or not
      */
     protected String checkSelected(String select) {
        if (!beanId.equals("") && value != null) {
            for (int ii = 0; ii < results.size(); ++ii) {
                String selectedValue = (String)results.get(ii);
                if (value.equals(selectedValue)) {
                    return " " + select + "='" + select + "' ";
                }
            }
            return "";
        }
         if (selected) {
             return " " + select + "='" + select + "' ";
         } else {
             return "";
         }
     }
 
     /**
      * Sets the selected status of the bean
      *
      * @param flag true if the bean is selected, false otherwise
      */
     public void setSelected(boolean flag) {
         this.selected = flag;
     }
 
     /**
      * Returns the selected status of the bean
      *
      * @return true if the bean is selected, false otherwise
      */
     public boolean isSelected() {
         return selected;
     }
 
     /**
      * Returns the selected values as a list
      *
      * @return selected values as a the list
      */
     public List getSelectedValues() {
         return results;
     }
 
     /**
      * Returns the selected values as a list
      *
      * @return selected values as a the list
      */
     public String getSelectedValue() {
         if (results.isEmpty()) {
             return null;
         } else {
             return (String) results.get(0);
         }
     }
 
     /**
      * Adds a selected value to this bean
      *
      * @param value a selected value
      */
     public void addSelectedValue(String value) {
         results.add(value);
     }
 
     /**
      * Clears the selected values in this bean
      */
     public void clearSelectedValues() {
         selected = false;
         results.clear();
     }
 
      public String toStartString(String type) {
         String pname = (name == null) ? "" : name;
         String sname = pname;
         if (!beanId.equals("")) {
             if (request == null) {
                 sname = "ui_" + vbName + "_" + beanId + "_" + pname;
             } else {
                 String compId = (String) request.getAttribute(SportletProperties.GP_COMPONENT_ID);
                 if (compId == null) {
                     sname = "ui_" + vbName + "_" + beanId + "_" + pname;
                 } else {
                     sname = "ui_" + vbName + "_" + compId + "%" + beanId + "_" + pname;
                 }
             }
         }
         return "<input " + getFormattedCss() + " type='"
                 + type
                 + "' name='"
                 + sname
                 + "' value='"
                 + value + "' "
                 + checkDisabled()
                 + " "
                 + checkSelected("checked")
                 + "/>";
     }
 
     public String toEndString() {
         return "";
     }
 }

 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.provider.portletui.beans;
 
 import org.gridlab.gridsphere.portlet.PortletRequest;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * A <code>ListBoxBean</code> represents a visual list box element
  */
 public class ListBoxBean extends BeanContainer implements TagBean {
 
     protected String LISTBOX_STYLE = "portlet-form-field";
     public static final String NAME = "lb";
 
     protected int size = 0;
     protected boolean isMultiple = false;
     protected String onChange = null;
 
     /**
      * Constructs a default list box bean
      */
     public ListBoxBean() {
         super(NAME);
         this.cssClass = LISTBOX_STYLE;
     }
 
     /**
      * Constructs a list box bean with the supplied bean identifier
      *
      * @param beanId the bean identifier
      */
     public ListBoxBean(String beanId) {
         super(NAME);
         this.beanId = beanId;
         this.cssClass = LISTBOX_STYLE;
     }
 
     /**
      * Constructs a list box bean with a supplied portlet request and bean identifier
      *
      * @param request the portlet request
      * @param beanId the bean identifier
      */
     public ListBoxBean(PortletRequest request, String beanId) {
         super(NAME);
         this.cssClass = LISTBOX_STYLE;
         this.request = request;
         this.beanId = beanId;
     }
 
     /**
      * Returns the size of the list box
      *
      * @return the size of the list box
      */
     public int getSize() {
         return size;
     }
 
     /**
      * Sets the size of the list box
      *
      * @param size the size of the list box
      */
     public void setSize(int size) {
         this.size = size;
     }
 
     /**
      * Sets multiple selection
      *
      * @param isMultiple is true if listbox provides multiple selections, false otherwise
      */
     public void setMultipleSelection(boolean isMultiple) {
         this.isMultiple = isMultiple;
     }
 
     /**
      * Indicates if multiple selection is provided
      *
      * @return true if this listbox supports multiple selection, false otherwise
      */
     public boolean getMultipleSelection() {
         return isMultiple;
     }
 
     public void setOnChange(String onChange) {
         this.onChange = onChange;
     }
 
     public String toStartString() {
         StringBuffer sb = new StringBuffer();
         String pname = (name == null) ? "" : name;
         String sname = pname;
         if (!beanId.equals("")) {
             sname = "ui_" + vbName + "_" + beanId + "_" + pname;
         }
         sb.append("<select name='" + sname + "' size='" + size + "'");
         if (isMultiple) {
             sb.append(" multiple='multiple'");
         }
         if (onChange != null) {
             sb.append(" onChange='" + onChange + "'");
         }
         sb.append(">");
         return sb.toString();
     }
 
     public String toEndString() {
         StringBuffer sb = new StringBuffer();
         Iterator it = container.iterator();
         while (it.hasNext()) {
             ListBoxItemBean itemBean = (ListBoxItemBean) it.next();
             sb.append(itemBean.toStartString());
             sb.append(itemBean.toEndString());
         }
         sb.append("</select>");
         return sb.toString();
     }
 
     /**
      * Returns the selected value of the list. This is only useful with multiple selection disabled.
      * @return selected value of the list, null if nothing is selected
      */
     public String getSelectedValue() {
         Iterator it = container.iterator();
         while (it.hasNext()) {
             ListBoxItemBean item = (ListBoxItemBean) it.next();
             if (item.isSelected()) {
                 return item.getValue();
             }
         }
         return null;
     }
 
     /**
      * Returns true if the listbox has a selected value, false otherwise
      *
      * @return true if an item is selected, otherwise false
      */
     public boolean hasSelectedValue() {
         Iterator it = container.iterator();
         while (it.hasNext()) {
             ListBoxItemBean item = (ListBoxItemBean) it.next();
             if (item.isSelected()) {
                 return true;
             }
         }
         return false;
     }
 
     private List getSelectedNamesValues(boolean names) {
         List result = new ArrayList();
         Iterator it = container.iterator();
         while (it.hasNext()) {
             ListBoxItemBean item = (ListBoxItemBean) it.next();
             if (item.isSelected()) {
                 if (names) {
                     result.add(item.getName());
                 } else {
                     result.add(item.getValue());
                 }
             }
         }
         return result;
 
     }
 
     /**
      * Returns the selected values of the list.
      * @return selected values of the list
      */
     public List getSelectedValues() {
         return getSelectedNamesValues(false);
     }
 
     /**
      * Returns the selected names of the list
      * @return selected names of the list
      */
     public List getSelectedNames() {
         return getSelectedNamesValues(true);
     }
 
     /**
      * Returns the selected values of the list.
      * @return selected values of the list
      */
     public String getSelectedName() {
         Iterator it = container.iterator();
         while (it.hasNext()) {
             ListBoxItemBean item = (ListBoxItemBean) it.next();
             if (item.isSelected()) {
                 return item.getName();
             }
         }
         return null;
     }
 
     /**
      * Returns the selected items of the list
      * @return  the selected item of the list
      */
     public List getSelectedItems() {
         ArrayList result = new ArrayList();
         Iterator it = container.iterator();
         while (it.hasNext()) {
             ListBoxItemBean item = (ListBoxItemBean) it.next();
             if (item.isSelected()) {
                 result.add(item);
             }
         }
         return result;
     }
 
 }

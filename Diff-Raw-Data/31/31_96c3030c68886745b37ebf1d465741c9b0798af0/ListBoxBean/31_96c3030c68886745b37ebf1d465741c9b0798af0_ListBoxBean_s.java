 /*
  * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.provider.portletui.beans;
 
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class ListBoxBean extends BeanContainer implements TagBean {
 
     protected String LISTBOX_STYLE = "portlet-frame-input";
     public static final String NAME = "lb";
 
     protected transient static PortletLog log = SportletLog.getInstance(ListBoxBean.class);
 
     protected int size = 0;
     protected boolean isMultiple = false;
 
     public ListBoxBean() {
         super(NAME);
         this.cssStyle = LISTBOX_STYLE;
     }
 
     public ListBoxBean(String beanId) {
         super(NAME);
         this.beanId = beanId;
        System.err.println("beanId=" + beanId);
         this.cssStyle = LISTBOX_STYLE;
     }
 
     public ListBoxBean(PortletRequest request, String beanId) {
         super(NAME);
         this.cssStyle = LISTBOX_STYLE;
         this.request = request;
         this.beanId = beanId;
     }
 
     public int getSize() {
         return size;
     }
 
     public void setSize(int size) {
         this.size = size;
     }
 
     public void setMultipleSelection(boolean isMultiple) {
         this.isMultiple = isMultiple;
     }
 
     public boolean getMultipleSelection() {
         return isMultiple;
     }
 
     public String toString() {
         String pname = (name == null) ? "" : name;
         String sname = pname;
         if (!beanId.equals("")) {
            System.err.println("beanId is non-null");
             sname = "ui_" + vbName + "_" + beanId + "_" + pname;
        } else {
            System.err.println("in list box bean: beanId is null");
         }
         System.err.println("in ListBoxBean: toString: name= " + sname);
         String result = "<select name='"+sname+"' size='"+size+"'";
         if (isMultiple) {
             result = result + " multiple='multiple'" ;
         }
         result = result +">";
         result += super.toString();
         result = result +"</select>";
         return result;
     }
 
     /**
      * Returns the selected values of the list.
      * @return selected values of the list
      */
     public String getSelectedValue() {
         Iterator it = container.iterator();
         while (it.hasNext()) {
             ListBoxItemBean item = (ListBoxItemBean)it.next();
             if (item.isSelected()) {
                 return item.getValue();
             }
         }
         return null;
     }
 
     public boolean hasSelectedValue() {
         Iterator it = container.iterator();
         while (it.hasNext()) {
             ListBoxItemBean item = (ListBoxItemBean)it.next();
             if (item.isSelected()) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns the selected values of the list.
      * @return selected values of the list
      */
     public List getSelectedValues() {
         List result = new ArrayList();
         Iterator it = container.iterator();
         while (it.hasNext()) {
             ListBoxItemBean item = (ListBoxItemBean)it.next();
             if (item.isSelected()) {
                 result.add(item.getValue());
             }
         }
         return result;
     }
 
     /**
      * Returns the selected items of the list
      * @return  the selected item of the list
      */
     public List getSelectedItems() {
         ArrayList result = new ArrayList();
         Iterator it = container.iterator();
         while (it.hasNext()) {
             ListBoxItemBean item = (ListBoxItemBean)it.next();
             if (item.isSelected()) {
                 result.add(item);
             }
         }
         return result;
     }
 
 }

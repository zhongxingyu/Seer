 /*
  * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id: PasswordTag.java 4666 2006-03-27 17:47:56Z novotny $
  */
 
 package org.gridsphere.provider.portletui.tags;
 
 import org.gridsphere.provider.portletui.beans.PasswordBean;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 
 /**
  * A <code>PasswordTag</code> provides a password tag for represnting HTML password input elements
  */
public class PasswordTag extends BaseComponentTag {
 
     protected PasswordBean passwordBean = null;
 
     protected int size = 10;
     protected int maxlength = 15;
 
     /**
      * Returns the (html) size of the field
      *
      * @return size of the field
      */
     public int getSize() {
         return size;
     }
 
     /**
      * Sets the (html) size of the field
      *
      * @param size size of the field
      */
     public void setSize(int size) {
         this.size = size;
     }
 
     /**
      * Returns the (html) max length of the field
      *
      * @return the max length of the field
      */
     public int getMaxlength() {
         return maxlength;
     }
 
     /**
      * Sets the (html) max length of the field
      *
      * @param maxlength the max length of the field
      */
     public void setMaxlength(int maxlength) {
         this.maxlength = maxlength;
     }
 
     public int doStartTag() throws JspException {
 
         if (!beanId.equals("")) {
             passwordBean = (PasswordBean) getTagBean();
             if (passwordBean == null) {
                 passwordBean = new PasswordBean();
                 this.setBaseComponentBean(passwordBean);
             } else {
                 this.updateBaseComponentBean(passwordBean);
             }
         } else {
             passwordBean = new PasswordBean();
             passwordBean.setMaxLength(maxlength);
             passwordBean.setSize(size);
             this.setBaseComponentBean(passwordBean);
         }
 
         try {
             JspWriter out = pageContext.getOut();
             out.print(passwordBean.toStartString());
         } catch (Exception e) {
             throw new JspException(e.getMessage());
         }
         return SKIP_BODY;
     }
 
 }

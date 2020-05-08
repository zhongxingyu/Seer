 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.tags;
 
 import org.gridlab.gridsphere.provider.portletui.beans.CheckBoxBean;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.Tag;
 
 /**
  * A <code>CheckBoxTag</code> provides a checkbox element
  */
 public class CheckboxTag extends BaseComponentTag {
 
     protected CheckBoxBean checkbox = null;
     protected boolean selected = false;
 
     /**
      * Sets the selected status of the bean
      *
      * @param flag status of the bean
      */
     public void setSelected(boolean flag) {
         this.selected = flag;
     }
 
     /**
      * Returns the selected status of the bean
      *
      * @return selected status
      */
     public boolean isSelected() {
         return selected;
     }
 
     public int doStartTag() throws JspException {
         if (!beanId.equals("")) {
             checkbox = (CheckBoxBean) pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
             if (checkbox == null) {
                 checkbox = new CheckBoxBean();
                 checkbox.setSelected(selected);
                 this.setBaseComponentBean(checkbox);
             } else {
                this.overrideBaseComponentBean(checkbox);
             }
         } else {
             checkbox = new CheckBoxBean();
             checkbox.setSelected(selected);
             this.setBaseComponentBean(checkbox);
         }
 
         //debug();
 
         Tag parent = getParent();
         if (parent instanceof DataGridColumnTag) {
             DataGridColumnTag dataGridColumnTag = (DataGridColumnTag) parent;
             dataGridColumnTag.addTagBean(this.checkbox);
         } else {
 
             try {
                 JspWriter out = pageContext.getOut();
                 out.print(checkbox.toStartString());
             } catch (Exception e) {
                 throw new JspException(e.getMessage());
             }
         }
         return SKIP_BODY;
     }
 
 }

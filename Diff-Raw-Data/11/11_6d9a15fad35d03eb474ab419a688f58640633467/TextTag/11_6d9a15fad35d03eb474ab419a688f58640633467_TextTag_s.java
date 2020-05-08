 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.provider.portletui.tags;
 
 import org.gridlab.gridsphere.provider.portletui.beans.TextBean;
 import org.gridlab.gridsphere.provider.portletui.tags.BaseComponentTag;
 import org.gridlab.gridsphere.provider.portletui.tags.DataGridColumnTag;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.Tag;
 
 /**
  * A <code>TextTag</code> represents text to be displayed
  */
 public class TextTag extends BaseComponentTag {
 
     protected TextBean textBean = null;
     protected String key = null;
     protected String style = TextBean.MSG_INFO;
     protected String format = null;
 
     /**
      * Sets the style of the text: Available styles are
      * <ul>
      * <li>nostyle</li>
      * <li>error</li>
      * <li>info</li>
      * <li>status</li>
      * <li>alert</li>
      * <li>success</li>
      *
      * @param style the text style
      */
     public void setStyle(String style) {
         this.style = style;
     }
 
     /**
      * Returns the style of the text: Available styles are
      * <ul>
      * <li>nostyle</li>
      * <li>error</li>
      * <li>info</li>
      * <li>status</li>
      * <li>alert</li>
      * <li>success</li>
      * <li>bold</li>
      * <li>italic</li>
      * <li>underline</li>
      *
      * @return the text style
      */
     public String getStyle() {
         return style;
     }
 
     /**
      * Sets the format of the text
      *
      * @param format
      */
     public void setFormat(String format) {
         this.format = format;
     }
 
     /**
      * Returns the format of the text
      *
      * @return the format of the text
      */
     public String getFormat() {
         return format;
     }
 
     /**
      * Returns the key used to identify localized text
      *
      * @return the key used to identify localized text
      */
     public String getKey() {
         return key;
     }
 
     /**
      * Sets the key used to identify localized text
      * @param key the key used to identify localized text
      */
     public void setKey(String key) {
         this.key = key;
     }
 
     public int doEndTag() throws JspException {
 
         if (!beanId.equals("")) {
             textBean = (TextBean) pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
             if (textBean == null) {
                 textBean = new TextBean();
                 this.setBaseComponentBean(textBean);
             } else {
                 this.updateBaseComponentBean(textBean);
             }
         } else {
             textBean = new TextBean();
             this.setBaseComponentBean(textBean);
             textBean.setStyle(style);
             textBean.setCssClass(this.cssClass);
             textBean.setCssStyle(this.cssStyle);
         }
 
         if (key != null) {
             textBean.setValue(getLocalizedText(key));
         }
 
 

        // this doesn't work anymore
        if ((bodyContent != null) && (value == null)) {
            textBean.setValue(bodyContent.getString());
         }
 
         Tag parent = getParent();
         if (parent instanceof DataGridColumnTag) {
             DataGridColumnTag dataGridColumnTag = (DataGridColumnTag) parent;
             dataGridColumnTag.addTagBean(this.textBean);
         } else {
 
             try {
                 JspWriter out = pageContext.getOut();
                 out.print(textBean.toEndString());
             } catch (Exception e) {
                 throw new JspException(e.getMessage());
             }
         }
 
         return EVAL_PAGE;
     }
 
 }

 package org.icemobile.jsp.tags;
 
 
 import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
 import javax.servlet.jsp.tagext.TagSupport;
 import java.io.IOException;
 import java.io.Writer;
import java.util.logging.Logger;
 
 /**
  *
  */
 public class FieldSetRowTag extends TagSupport {
 
     public static final String FIELDSETROW_CLASS = "mobi-fieldset-row";
     public static final String FIELDSETGROUP_CLASS = "mobi-fieldset-row-group";
     private String id;
     private String styleClass;
     private String style;
     private boolean group;
    private static Logger logger = Logger.getLogger(FieldSetRowTag.class.getName());
 
    public void setParent(Tag parent) {
        if (!(parent instanceof FieldSetGroupTag)) {
            throw new IllegalArgumentException("FieldSetRow must be child of FieldSetGroup");
         }
    }
 
    public int doStartTag() throws JspTagException {
 
        Writer out = pageContext.getOut();
         try {
             out.write("<div");
             if (id != null && !"".equals(id)) {
                 out.write(" id=\'" + getId() + "\'");
             }
 
             // Apply default Field Row Class.
             StringBuilder classBuilder = new StringBuilder(FIELDSETROW_CLASS);
 
             // apply user defined class
             String styleClass = getStyleClass();
             if (styleClass != null && !"".equals(styleClass)) {
                 classBuilder.append(" ").append(styleClass);
             }
 
             // todo: ??
             if (isGroup()) {
                 classBuilder.append(" ").append(FIELDSETGROUP_CLASS);
             }
 
             out.write(" class=\'" + classBuilder.toString() + "\'");
 
             String inlineStyle = getStyle();
 
             if (inlineStyle != null && !"".equals(inlineStyle)) {
                 if (inlineStyle.endsWith(";")) {
                     inlineStyle = inlineStyle.substring(0, inlineStyle.length() - 1);
                 }
                 out.write(" style=\'" + inlineStyle + ";\'");
 
 
             }
             out.write(">");
 
         } catch (IOException ieo) {
            logger.severe("Exception creating FieldSetRowTag: " + ieo);
         }
         return (EVAL_BODY_INCLUDE);
 
     }
 
     /**
      * implement after body tags here.
      *
      * @return
      */
     public int doAfterBody() {
 
         Writer out = pageContext.getOut();
         try {
             out.write("</div>");
         } catch (IOException ieo) {
         }
 
         return SKIP_BODY;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getStyleClass() {
         return styleClass;
     }
 
     public void setStyleClass(String styleClass) {
         this.styleClass = styleClass;
     }
 
     public String getStyle() {
         return style;
     }
 
     public void setStyle(String style) {
         this.style = style;
     }
 
     public boolean isGroup() {
         return group;
     }
 
     public void setGroup(String group) {
         this.group = Boolean.getBoolean(group);
     }
 
     public void setGroup(boolean group) {
         this.group = group;
     }
 }

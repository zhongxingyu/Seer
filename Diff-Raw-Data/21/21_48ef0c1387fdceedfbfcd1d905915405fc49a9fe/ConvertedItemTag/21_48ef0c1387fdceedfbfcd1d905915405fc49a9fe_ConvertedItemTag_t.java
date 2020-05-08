 package jpaoletti.jpm.struts.tags;
 
 import java.io.IOException;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspTagException;
 import jpaoletti.jpm.converter.ConverterException;
import jpaoletti.jpm.core.*;
 import jpaoletti.jpm.struts.PMEntitySupport;
 
 /**
  * Tag for converted item
 *
  * @author jpaoletti
  */
 public class ConvertedItemTag extends PMTags {
 
     private PMContext ctx;
     private EntityContainer entityContainer;
     private Field field;
     private Operation operation;
     private Object item;
     private Object fieldValue;
     //old context values
     private EntityContainer oldEntityContainer;
     private Field oldField;
     private Object oldFieldValue;
     private Object oldEntityInstance;
 
     @Override
     public int doStartTag() throws JspException {
         try {
             final PMEntitySupport es = PMEntitySupport.getInstance();
             final String fieldId = getField().getId();
             try {
                 prepareContext();
                 final String highlight = highlight(getEntity(), getField(), getItem(), getFieldValue());
                 print("<div id='f_" + fieldId + "_div' class='cell " + highlight + "'>");
                 final Object visualize = getField().visualize(getCtx(), getOperation(), getEntity());
                 if (visualize != null) {
                     if (!visualize.toString().contains(".jsp")) {
                         println(visualize);
                     } else {
                         pageContext.include("../converters/"
                                 + visualize
                                 + "&f=" + fieldId);
                     }
                 }
                 println("</div>", "<span class='field_message_container_" + getEntity().getId() + "_" + fieldId + "' />");
             } catch (ConverterException e) {
                println("<img width='16px' src='" + es.getTemplatePath() + "/images/m_error.png' alt='error' />",
                        "<span class='pm_message_ERROR'>",
                        PresentationManager.getMessage(e.getMsg().getKey(), e.getMsg().getArgs()),
                        "</span>");
             } catch (Exception e) {
                es.getPM().warn(e);
                 println("<img width='16px' src='" + getContextPath() + "/templates/" + getTemplate() + "/images/m_error.png' alt='error' />");
             }
         } catch (IOException ex) {
             throw new JspTagException("ConvertedItemTag: " + ex.getMessage());
         }
         return SKIP_BODY;
     }
 
     protected void prepareContext() throws PMException {
         oldEntityContainer = getCtx().getEntityContainer(true);
         oldField = getCtx().getField();
         oldFieldValue = getCtx().getFieldValue();
         oldEntityInstance = getCtx().getEntityInstance();
 
         getCtx().setEntityContainer(getEntityContainer());
         getCtx().setField(getField());
         getCtx().setFieldValue(getFieldValue());
         getCtx().setEntityInstance(getItem());
     }
 
     @Override
     public int doEndTag() {
         getCtx().setEntityContainer(oldEntityContainer);
         getCtx().setField(oldField);
         getCtx().setFieldValue(oldFieldValue);
         getCtx().setEntityInstance(oldEntityInstance);
         return EVAL_PAGE;
     }
 
     public EntityContainer getEntityContainer() throws PMException {
         if (entityContainer == null) {
             return getCtx().getEntityContainer();
         }
         return entityContainer;
     }
 
     public void setEntityContainer(EntityContainer entityContainer) {
         this.entityContainer = entityContainer;
     }
 
     public Object getFieldValue() throws PMException {
         if (fieldValue == null) {
             return getCtx().getPresentationManager().get(getItem(), getField().getProperty());
         }
         return fieldValue;
     }
 
     public void setFieldValue(Object fieldValue) {
         this.fieldValue = fieldValue;
     }
 
     public Field getField() {
         return field;
     }
 
     public void setField(Field field) {
         this.field = field;
     }
 
     public Object getItem() throws PMException {
         if (item == null) {
             if (getCtx().getSelected() != null) {
                 return getCtx().getSelected().getInstance();
             }
         }
         return item;
     }
 
     public void setItem(Object item) {
         this.item = item;
     }
 
     public Operation getOperation() {
         if (operation == null) {
             return getCtx().getOperation();
         }
         return operation;
     }
 
     public void setOperation(Operation operation) {
         this.operation = operation;
     }
 
     private Entity getEntity() throws PMException {
         return getEntityContainer().getEntity();
     }
 
     public PMContext getCtx() {
         return ctx;
     }
 
     public void setCtx(PMContext ctx) {
         this.ctx = ctx;
     }
 }

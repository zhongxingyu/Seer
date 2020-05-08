 package org.echosoft.framework.ui.extjs.widgets.form;
 
 import org.echosoft.common.json.JsonWriter;
 import org.echosoft.framework.ui.core.ComponentContext;
 import org.echosoft.framework.ui.core.Scope;
 
 /**
  * Отвечает за ввод одной текстовой строки.
  * @author Anton Sharapov
  */
 public class TextField extends AbstractTextField {
 
     private String value;                   // текст в поле ввода.
     private InputType inputType;            // значение атрибута "type" для тега "input".
 
     public TextField() {
         this(null);
     }
     public TextField(final ComponentContext ctx) {
         super(ctx);
         inputType = InputType.TEXT;
     }
 
     /**
      * Возвращает значение данного поля.
      * @return  значение данного поля.
      */
     public String getValue() {
         return value;
     }
     /**
      * Указывает текст отображаемый в данном поле.
      * @param value значение данного поля.
      */
     public void setValue(final String value) {
         this.value = value;
     }
 
     /**
      * Возвращает тип данного поля ввода.
     * @return тип данного поля ввода.  По умолчанию возвращает {@link AbstractTextField.InputType#TEXT}.
      */
     public InputType getInputType() {
         return inputType;
     }
     /**
      * Указывает тип данного поля ввода.
      * @param inputType  тип данного поля ввода.
      */
     public void setInputType(final InputType inputType) {
         this.inputType = inputType!=null ? inputType : InputType.TEXT;
     }
 
 
     @Override
     public void invoke(final JsonWriter out) throws Exception {
         final ComponentContext ctx = getContext();
         setName( ctx.getClientId() + ".value" );
         if (isStateful()) {
             final String svalue = ctx.getAttribute("value", Scope.PR_ST);
             if (svalue!=null) {
                 ctx.setAttribute("value", svalue, Scope.STATE);
                 value = svalue;
             }
         }
         out.beginObject();
         out.writeProperty("xtype", "textfield");
         renderAttrs(out);
         out.endObject();
     }
 
     @Override
     protected void renderAttrs(final JsonWriter out) throws Exception {
         super.renderAttrs(out);
         if (value!=null)
             out.writeProperty("value", value);
         if (inputType!=InputType.TEXT)
             out.writeProperty("inputType", inputType);
     }
 
 }

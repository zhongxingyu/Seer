 package org.echosoft.framework.ui.extjs.widgets.form;
 
 import java.util.Set;
 
 import org.echosoft.common.json.JsonWriter;
 import org.echosoft.common.utils.StringUtil;
 import org.echosoft.framework.ui.core.ComponentContext;
 import org.echosoft.framework.ui.core.Scope;
 
 /**
  * @author Anton Sharapov
  */
 public class CheckBox extends AbstractField {
 
     public static final Set<String> EVENTS =
             StringUtil.asUnmodifiableSet(AbstractField.EVENTS, "check");
 
     private String name;                    // имя параметра в котором будет на сервер отправлено значение поля.
     private String boxLabel;                // текст расположенный справа от переключателя.
     private boolean tristate;               // поддерживается ли компонентом третье, "неопределенное" состояние.
     private Boolean checked;                // значение компонента.
 
     public CheckBox() {
         this(null);
     }
     public CheckBox(final ComponentContext ctx) {
         super(ctx);
     }
 
     /**
      * Возвращает имя параметра в запросе где будет сохраняться значение данного компонента.
      * По умолчанию вычисляется как:
      * <pre>
      *      <code>name = getContext().getClientId() + ".value";</code>
      * </pre>
      * @return имя параметра в запросе где будет сохраняться значение данного компонента.
      */
     public String getName() {
         return name;
     }
     /**
      * Указывает имя параметра в запросе где будет сохраняться значение данного компонента.
      * По умолчанию вычисляется как:
      * <pre>
      *      <code>name = getContext().getClientId() + ".value";</code>
      * </pre>
      * @param name имя параметра в запросе где будет сохраняться значение данного компонента.
      */
     public void setName(final String name) {
         this.name = name;
     }
 
     /**
      * Возвращает текст, отображаемый справа от переключателя.
      * @return текст, отображаемый справа от переключателя.
      */
     public String getBoxLabel() {
         return boxLabel;
     }
     /**
      * Устанавливает текст, который должен отображаться справа от переключателя.
      * @param boxLabel текст, отображаемый справа от переключателя.
      */
     public void setBoxLabel(final String boxLabel) {
         this.boxLabel = boxLabel;
     }
 
     /**
      * Возвращает <code>true</code> если компонент поддерживает третье состояние ("значение неопределенно").
      * @return <code>true</code> если компонент поддерживает третье состояние ("значение неопределенно").
      *  По умолчанию возвращает <code>false</code>.
      */
     public boolean isTristate() {
         return tristate;
     }
     /**
      * Разрешает или запрещает поддержку третьего состояния ("значение неопределенно").
      * @param tristate <code>true</code> если компонент поддерживает третье состояние ("значение неопределенно").
      */
     public void setTristate(final boolean tristate) {
         this.tristate = tristate;
     }
 
     /**
      * Возвращает значение данного поля.
      * @return  значение данного поля.
      */
     public Boolean isChecked() {
         return checked;
     }
     /**
      * Указывает текст отображаемый в данном поле.
      * @param checked значение данного поля.
      */
     public void setChecked(final Boolean checked) {
         this.checked = checked;
     }
 
     @Override
     public void invoke(final JsonWriter out) throws Exception {
         final ComponentContext ctx = getContext();
         setName( ctx.getClientId() + ".value" );
         if (isStateful()) {
            final String svalue = StringUtil.trim( (String)ctx.getAttribute("value", Scope.PR_ST) );
             if (svalue!=null) {
                checked = "on".equals(svalue);
             }
             ctx.setAttribute("value", svalue, Scope.STATE);
         }
         out.beginObject();
         out.writeProperty("xtype", "tri-checkbox");
         renderContent(out);
         out.endObject();
     }
 
     @Override
     public void renderContent(final JsonWriter out) throws Exception {
         super.renderContent(out);
         out.writeProperty("name", name);
         if (boxLabel!=null)
             out.writeProperty("boxLabel", boxLabel);
         if (tristate) {
             out.writeProperty("tristate", true);
             out.writeProperty("checked", checked);
         } else {
             out.writeProperty("checked", checked!=null && checked);
         }
         final ComponentContext ctx = getContext();
         ctx.getResources().attachScript(ctx.encodeThemeURL("/ux/form.js", false));
     }
 
     @Override
     protected Set<String> getSupportedEvents() {
         return CheckBox.EVENTS;
     }
 
 }

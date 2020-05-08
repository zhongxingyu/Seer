 package org.echosoft.framework.ui.extjs.widgets.form;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.echosoft.common.json.JsonWriter;
 import org.echosoft.common.utils.StringUtil;
 import org.echosoft.framework.ui.core.ComponentContext;
 import org.echosoft.framework.ui.extjs.layout.FormLayout;
 import org.echosoft.framework.ui.extjs.widgets.Panel;
 
 /**
  * Панель с формой.
  * @author Anton Sharapov
  */
 public class FormPanel extends Panel {
 
     public static enum Method {
         GET, POST
     }
     public static Set<String> EVENTS =
             StringUtil.asUnmodifiableSet(Panel.EVENTS, "clientvalidation", "actioncomplete", "actionfailed", "beforeaction");
 
     private Map<String,Object> baseParams;  // дополнительные параметры, отправляемые на сервер.
     private String url;                     // URL ресурса на который будет делаться запрос.
     private Method method;                  // метод отправки запроса (либо GET либо POST).
     private boolean fileUpload;             // признак что надо аплоадить файл - т.е. отдельный алгоритм отправки данных на сервер.
     private boolean standardSubmit;         // использовать классическую форму отправки данных на сервер или посредством AJAX.
     private boolean trackResetOnLoad;       // form.reset() откатывает до тех данных что были получены при загрузке страницы или до данных полученных последним AJAX запросом.
     private int timeout;                    // время ожидания ответа (в секундах)
     private String waitTitle;               // используется при AJAX - сообщение что идет ожидание данных от сервера.
     private String formId;                  // id формы.
     private boolean monitorValid;           // использовать фоновую валидацию полей формы
     private int monitorPoll;                // частота проверок полей формы (мсек).
     private boolean hideLabels;             // показывать или нет по умолчанию метки к компонентам на форме.
     private FormLayout.LabelAlign labelAlign;// способ выравнивания меток компонент.
     private int labelWidth;                 // Ширина региона отведенного для отрисовки меток компонент.
     private int labelPad;                   // Определяет отступы слева для компонент относительно их меток (имеет смысл только при установленном labelWidth и labelAlign=RIGHT)
 
     public FormPanel() {
         this(null);
     }
     public FormPanel(final ComponentContext ctx) {
         super(ctx);
         timeout = 30;
         monitorPoll = 200;
         labelAlign = FormLayout.LabelAlign.LEFT;
         labelWidth = 100;
         labelPad = 5;
         setLayout( new FormLayout() );
     }
 
     /**
      * Возвращает перечень дополнительных параметров которые должны отправляться на сервер вместе с основным содержимым формы.
      * @return перечень дополнительных параметров которые будут отправляться на сервер вместе с основным содержимым формы.
      */
     public Map<String,Object> getBaseParams() {
         return baseParams;
     }
     /**
      * Указывает перечень дополнительных параметров которые должны отправляться на сервер вместе с основным содержимым формы.
      * @param baseParams перечень дополнительных параметров которые будут отправляться на сервер вместе с основным содержимым формы.
      */
     public void setBaseParams(final Map<String,Object> baseParams) {
         this.baseParams = baseParams;
     }
 
     /**
      * Возвращает ссылку на серверный ресурс куда будет отправляться запрос с формы. Может быть представлен в виде относительной или абсолютной ссылки.
      * @return путь по которому будет отправляться запрос с формы.
      */
     public String getUrl() {
         return url;
     }
     /**
      * Указывает ссылку на серверный ресурс куда будет отправляться запрос с формы. Может быть представлен в виде относительной или абсолютной ссылки.
      * @param url путь по которому будет отправляться запрос с формы.
      */
     public void setUrl(final String url) {
         this.url = url;
     }
 
     /**
      * Возвращает способ отправки запроса с формы на сервер.
      * @return способ отправки запроса
      */
     public Method getMethod() {
         return method;
     }
     /**
      * Указывает способ отправки запроса с формы на сервер.
      * @param method способ отправки запроса
      */
     public void setMethod(final Method method) {
         this.method = method;
     }
 
     /**
      * Возвращает <code>true</code> если форма требует отправки файла(ов) на сервер. Данная ситуация обрабатывается в ExtJS совершенно особым образом.
      * @return <code>true</code> если форма требует отправки файла(ов) на сервер.
      */
     public boolean isFileUpload() {
         return fileUpload;
     }
     /**
      * Указывает что форма требует отправки файла(ов) на сервер. Данная ситуация обрабатывается в ExtJS совершенно особым образом.
      * @param fileUpload <code>true</code> если форма требует отправки файла(ов) на сервер.
      */
     public void setFileUpload(final boolean fileUpload) {
         this.fileUpload = fileUpload;
     }
 
     /**
      * Возвращает <code>true</code> если форма будет отправляться классическим способом, в противном случае (по умолчанию)
      * форма будет отправляться средставми AJAX.
      * @return <code>true</code> если требуется отправлять содержимое формы на сервер стандартным способом (с последующей перезагрузкой страницы)
      * в противном случае, если отправка данных должна осуществляться средствами AJAX то свойство должно быть установлено в <code>false</code>.
      *  По умолчанию метод возвращает <code>false</code>. 
      */
     public boolean isStandardSubmit() {
         return standardSubmit;
     }
     /**
      * Определяет каким способом (стандартный с перезагрузкой страницы, AJAX) будет отправляться запрос на сервер.
      * @param standardSubmit <code>true</code> если требуется отправлять содержимое формы на сервер стандартным способом (с последующей перезагрузкой страницы)
      * в противном случае, если отправка данных должна осуществляться средствами AJAX то свойство должно быть установлено в <code>false</code>.
      *  По умолчанию метод возвращает <code>false</code>.
      */
     public void setStandardSubmit(final boolean standardSubmit) {
         this.standardSubmit = standardSubmit;
     }
 
     /**
      * Влияет на выполнение метода <code>reset()</code> формы.
      * Если <code>true</code> то состояние формы будет откатываться на момент ее последней успешной загрузки.
      * Если <code>false</code> (по умолчанию) то состояние формы будет откатываться на момент создания формы.
      * @return <code>true</code> если состояние формы при вызове <code>reset()</code> будет откатываться на момент
      * ее последней загрузки, в противном случае форма будет восстанавливать свое состояние на момент ее создания (загрузки страницы).
      * По умолчанию возвращает <code>false</code>.
      */
     public boolean isTrackResetOnLoad() {
         return trackResetOnLoad;
     }
     /**
      * Влияет на выполнение метода <code>reset()</code> формы.
      * Если <code>true</code> то состояние формы будет откатываться на момент ее последней успешной загрузки.
      * Если <code>false</code> (по умолчанию) то состояние формы будет откатываться на момент создания формы.
      * @param trackResetOnLoad <code>true</code> если состояние формы при вызове <code>reset()</code> должно откатываться на момент
      * ее последней загрузки, в противном случае форма будет восстанавливать свое состояние на момент ее создания (загрузки страницы).  
      */
     public void setTrackResetOnLoad(final boolean trackResetOnLoad) {
         this.trackResetOnLoad = trackResetOnLoad;
     }
 
     /**
      * Время максимального ожидания результата действий формы (отправка данных на сервер, загрузка данных с сервера)
      * @return Время максимального ожидания результата действий формы (отправка данных на сервер, загрузка данных с сервера).
      * Значение по умолчанию: <code>30</code> сек.
      */
     public int getTimeout() {
         return timeout;
     }
     /**
      * Время максимального ожидания результата действий формы (отправка данных на сервер, загрузка данных с сервера)
      * @param timeout Время максимального ожидания результата действий формы (отправка данных на сервер, загрузка данных с сервера).
      */
     public void setTimeout(final int timeout) {
         this.timeout = timeout;
     }
 
     /**
      * Сообщение появляющееся в момент ожидания ответа от сервера.
      * @return текст сообщения появляющегося во всплываюшем окошке в момент ожидания ответа от сервера.
      */
     public String getWaitTitle() {
         return waitTitle;
     }
     /**
      * Сообщение появляющееся в момент ожидания ответа от сервера.
      * @param waitTitle текст сообщения появляющегося во всплываюшем окошке в момент ожидания ответа от сервера.
      */
     public void setWaitTitle(final String waitTitle) {
         this.waitTitle = StringUtil.trim(waitTitle);
     }
 
     /**
      * Id элемента <code>form</code>.
      * @return идентификатор присвоеный DOM элементу <code>form</code>.
      */
     public String getFormId() {
         return formId;
     }
     /**
      * Id элемента <code>form</code>.
      * @param formId идентификатор присвоеный DOM элементу <code>form</code>.
      */
     public void setFormId(String formId) {
         this.formId = formId;
     }
 
     /**
      * Возвращает <code>true</code> если включен мониторинг состояния формы путем регулярного инициирования
      * события <code>clientvalidation</code> через определенный промежуток времени.
      * @return <code>true</code> если включен мониторинг состояния формы.
      *  Значение свойства по умолчанию: <code>false</code>.
      */
     public boolean isMonitorValid() {
         return monitorValid;
     }
     public void setMonitorValid(final boolean monitorValid) {
         this.monitorValid = monitorValid;
     }
 
     /**
      * Если включен мониторинг состояния формы (<code>monitorValid=true</code>) то через каждые N милисекунд
      * будет происходить опрос состояния формы путем инициирования события <code>clientvalidation</code>.
      * @return временной интервал (в милисекундах) через который происходит инициирование события <code>clientvalidation</code>.
      *  Значение по умолчанию: 200.
      */
     public int getMonitorPoll() {
         return monitorPoll;
     }
     /**
      * Если включен мониторинг состояния формы (<code>monitorValid=true</code>) то через каждые N милисекунд
      * будет происходить опрос состояния формы путем инициирования события <code>clientvalidation</code>.
      * @param monitorPoll временной интервал (в милисекундах) через который происходит инициирование события <code>clientvalidation</code>.
      *  Значение по умолчанию: 200.
      */
     public void setMonitorPoll(final int monitorPoll) {
         this.monitorPoll = monitorPoll;
     }
 
     /**
      * Возвращает <code>true</code> если требуется по умолчанию не показывать метки к компонентам находящимся под управлением данного компоновщика.
      * Данное свойство используется в качестве значения по умолчанию для свойства <code>hideLabel</code> в компонентах.
      * @return <code>true</code> если требуется по умолчанию не отображать метки к компонентам на форме.
      *      По умолчанию свойство возвращает <code>false</code>.
      */
     public boolean isHideLabels() {
         return hideLabels;
     }
     /**
      * Определяет требуется ли по умолчанию не показывать метки к компонентам находящимся под управлением данного компоновщика.
      * Данное свойство используется в качестве значения по умолчанию для свойства <code>hideLabel</code> в компонентах.
      * @param hideLabels <code>true</code> если требуется по умолчанию не отображать метки к компонентам на форме.
      *      По умолчанию свойство равно <code>false</code>.
      */
     public void setHideLabels(final boolean hideLabels) {
         this.hideLabels = hideLabels;
     }
 
     /**
      * Возвращает используемый данным компоновщиком способ выравнивания меток компонент по отношению к самим компонентам.
     * @return текущий способ выравнивания меток. По умолчанию возвращает {@link org.echosoft.framework.ui.extjs.layout.FormLayout.LabelAlign#LEFT}.
      */
     public FormLayout.LabelAlign getLabelAlign() {
         return labelAlign;
     }
     /**
      * Задает способ выравнивания меток компонент по отношению к самим компонентам.
     * @param labelAlign текущий способ выравнивания меток. По умолчанию свойство равно {@link org.echosoft.framework.ui.extjs.layout.FormLayout.LabelAlign#LEFT}.
      */
     public void setLabelAlign(final FormLayout.LabelAlign labelAlign) {
         this.labelAlign = labelAlign!=null ? labelAlign : FormLayout.LabelAlign.LEFT;
     }
 
     /**
      * Возвращает ширину области экрана отведенную для отрисовки меток компонент. По умолчанию резервируется 100px.
      * @return ширина области зарезервированной для отрисовки меток компонент на форме. По умолчанию возвращает 100px.
      */
     public int getLabelWidth() {
         return labelWidth;
     }
     /**
      * Задает ширину области экрана отведенную для отрисовки меток компонент. По умолчанию резервируется 100px.
      * @param labelWidth ширина области зарезервированной для отрисовки меток компонент на форме.
      */
     public void setLabelWidth(final int labelWidth) {
         this.labelWidth = labelWidth;
     }
 
     /**
      * Возвращает величину отступа левой границы компонента от метки. Имеет смысл только если задан параметр {@link #getLabelWidth()}
     * и свойство {@link #getLabelAlign()} равно {@link org.echosoft.framework.ui.extjs.layout.FormLayout.LabelAlign#RIGHT}.
      * @return отступ левой границы компонента от метки. По умолчанию равно <code>5</code>.
      */
     public int getLabelPad() {
         return labelPad;
     }
     /**
      * Задает величину отступа левой границы компонента от метки. Имеет смысл только если задан параметр {@link #getLabelWidth()}
     * и свойство {@link #getLabelAlign()} равно {@link org.echosoft.framework.ui.extjs.layout.FormLayout.LabelAlign#RIGHT}.
      * @param labelPad отступ левой границы компонента от метки. По умолчанию равно <code>5</code>.
      */
     public void setLabelPad(final int labelPad) {
         this.labelPad = labelPad;
     }
 
 
 
     @Override
     public void invoke(final JsonWriter out) throws Exception {
         final ComponentContext ctx = getContext();
         if (ctx!=null) {
             ctx.getResources().attachScript( ctx.encodeThemeURL("/pkgs/pkg-forms.js",false) );
             ctx.getResources().attachScript( ctx.encodeThemeURL("/ux/form-plugins.js",false) );
         }
         addPlugin("Ext.ux.wui.plugins.FormPanel");
 
         out.beginObject();
         out.writeProperty("xtype", "form");
         renderAttrs(out);
         out.endObject();
     }
 
     @Override
     protected void renderAttrs(final JsonWriter out) throws Exception {
         super.renderAttrs(out);
         if (baseParams!=null)
             out.writeProperty("baseParams", baseParams);
         if (url!=null)
             out.writeProperty("url", getContext().encodeURL(url));
         if (method!=null)
             out.writeProperty("method", method);
         if (fileUpload)
             out.writeProperty("fileUpload", true);
         if (standardSubmit)
             out.writeProperty("standardSubmit", true);
         if (trackResetOnLoad)
             out.writeProperty("trackResetOnLoad", true);
         if (timeout!=30)
             out.writeProperty("timeout", timeout);
         if (waitTitle!=null)
             out.writeProperty("waitTitle", waitTitle);
         if (formId!=null)
             out.writeProperty("formId", formId);
         if (monitorValid)
             out.writeProperty("monitorValid", true);
         if (monitorPoll!=200)
             out.writeProperty("monitorPoll", monitorPoll);
         if (hideLabels)
             out.writeProperty("hideLabels", true);
         if (labelAlign!=FormLayout.LabelAlign.LEFT)
             out.writeProperty("labelAlign", labelAlign);
         if (labelWidth!=100)
             out.writeProperty("labelWidth", labelWidth);
         if (labelPad!=5)
             out.writeProperty("labelPad", labelPad);
     }
 
     @Override
     protected Set<String> getSupportedEvents() {
         return FormPanel.EVENTS;
     }
 }

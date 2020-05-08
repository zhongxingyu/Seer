 package soadev.component;
 
 import javax.el.ELContext;
 import javax.el.ExpressionFactory;
 import javax.el.MethodExpression;
 import javax.el.ValueExpression;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 
 import javax.faces.event.ValueChangeEvent;
 
 import oracle.adf.model.bean.DCDataRow;
 import oracle.adf.view.rich.component.rich.RichPopup;
 import oracle.adf.view.rich.component.rich.data.RichTable;
 import oracle.adf.view.rich.component.rich.fragment.RichDeclarativeComponent;
 import oracle.adf.view.rich.component.rich.input.RichInputText;
 import oracle.adf.view.rich.context.AdfFacesContext;
 import oracle.adf.view.rich.event.DialogEvent;
 import oracle.adf.view.rich.event.QueryEvent;
 import oracle.adf.view.rich.model.ListOfValuesModel;
 import oracle.adf.view.rich.model.QueryDescriptor;
 import oracle.adf.view.rich.render.ClientEvent;
 
 import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;
 
 import org.apache.myfaces.trinidad.model.RowKeySet;
 import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
 import org.apache.myfaces.trinidad.util.Service;
 
 
 public abstract class InputListOfValues extends RichDeclarativeComponent {
     private RichTable richTable;
     private RichPopup richPopup;
     private RichInputText richInputText;
 
     public InputListOfValues() {
     }
 
     public void processQuery(QueryEvent event) {
         invokeMethod("#{attrs.model.performQuery}", QueryDescriptor.class,
                      event.getDescriptor());
     }
 
     private static Object invokeMethod(String expr, Class paramType,
                                        Object param) {
         FacesContext fc = FacesContext.getCurrentInstance();
         ELContext elc = fc.getELContext();
         ExpressionFactory ef = fc.getApplication().getExpressionFactory();
         MethodExpression me =
             ef.createMethodExpression(elc, expr, Object.class,
                                       new Class[] { paramType });
         return me.invoke(elc, new Object[] { param });
     }
 
     private static Object getValue(String expr) {
         FacesContext fc = FacesContext.getCurrentInstance();
         ELContext elc = fc.getELContext();
         ExpressionFactory ef = fc.getApplication().getExpressionFactory();
         ValueExpression ve = ef.createValueExpression(elc, expr, Object.class);
         return ve.getValue(elc);
     }
 
     private static void setValue(String expr, Object value) {
         FacesContext fc = FacesContext.getCurrentInstance();
         ELContext elc = fc.getELContext();
         ExpressionFactory ef = fc.getApplication().getExpressionFactory();
         ValueExpression ve = ef.createValueExpression(elc, expr, Object.class);
         if (!ve.isReadOnly(elc)) {
             try {
                 ve.setValue(elc, value);
             } catch (javax.el.PropertyNotWritableException e) {
                 e.printStackTrace();
             }
 
         }
     }
 
     protected void closeDialog(RichPopup popup) {
         FacesContext context = FacesContext.getCurrentInstance();
         String popupId = popup.getClientId(context);
         StringBuilder jscript = new StringBuilder();
         jscript.append("var popup = AdfPage.PAGE.findComponent('").append(popupId).append("'); ").append("if (popup.isPopupVisible()==true) { ").append("popup.hide();}");
         ExtendedRenderKitService erks =
             Service.getService(context.getRenderKit(),
                                ExtendedRenderKitService.class);
         erks.addScript(context, jscript.toString());
     }
 
 
     public void handleDialogReturn(DialogEvent event) {
         if (event.getOutcome() == DialogEvent.Outcome.ok) {
             applySelectedValue();
         }
     }
 
     public void handleTableDoubleClick(ClientEvent ce) {
         applySelectedValue();
     }
 
     //    private void applySelectedValue() {
     //        RichTable richTable = getRichTable();
     //        JUCtrlHierNodeBinding selectedNode =
     //            (JUCtrlHierNodeBinding)richTable.getSelectedRowData();
     //        if(selectedNode == null){
     //            return;
     //        }
     //        DCDataRow row = (DCDataRow)selectedNode.getRow();
     //        Object newValue = row.getDataProvider();
     //        Object oldValue = getValue("#{attrs.value}");
     //        setValue("#{attrs.value}", newValue);
     //        raiseValueChangeEvent(oldValue, newValue);
     //        closeDialog(getRichPopup());
     //        AdfFacesContext.getCurrentInstance().addPartialTarget(getRichInputText());
     //    }
 
     private void applySelectedValue() {
         RichTable richTable = getRichTable();
         RowKeySet selectedRowKeys = richTable.getSelectedRowKeys();
         if (selectedRowKeys == null || selectedRowKeys.isEmpty()) {
             return;
         }
         //use the LOV to get the value
         //to take advantage of caching and lov special logic on getting its selected row.
         Object newValue = getModel().getValueFromSelection(selectedRowKeys);
         Object oldValue = getValue("#{attrs.value}");
         setValue("#{attrs.value}", newValue);
         raiseValueChangeEvent(oldValue, newValue);
         closeDialog(getRichPopup());
         AdfFacesContext.getCurrentInstance().addPartialTarget(getRichInputText());
     }
 
     private void raiseValueChangeEvent(Object oldValue, Object newValue) {
        MethodExpression me = getValueChangeListener();
        if (me != null) {
            ValueChangeEvent event =
                new ValueChangeEvent(this, oldValue, newValue);
            //        this.broadcast(event);
            ELContext elc = FacesContext.getCurrentInstance().getELContext();
            getValueChangeListener().invoke(elc, new Object[] { event });
        }
     }
 
     public void setRichTable(RichTable richTable) {
         this.richTable = richTable;
     }
 
     public RichTable getRichTable() {
         return richTable;
     }
 
     public void setRichPopup(RichPopup richPopup) {
         this.richPopup = richPopup;
     }
 
     public RichPopup getRichPopup() {
         return richPopup;
     }
 
     public void setRichInputText(RichInputText richInputText) {
         this.richInputText = richInputText;
     }
 
     public RichInputText getRichInputText() {
         return richInputText;
     }
 
     public abstract ListOfValuesModel getModel();

    public abstract MethodExpression getValueChangeListener();
 }

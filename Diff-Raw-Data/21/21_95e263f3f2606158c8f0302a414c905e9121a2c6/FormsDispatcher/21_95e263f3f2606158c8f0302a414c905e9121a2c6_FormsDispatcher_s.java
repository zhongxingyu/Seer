 package examples.ui.dispatchers;
 
 import java.math.BigDecimal;
 
 import org.echosoft.common.json.JSFunction;
 import org.echosoft.common.types.Types;
 import org.echosoft.framework.ui.core.ComponentContext;
 import org.echosoft.framework.ui.core.Message;
 import org.echosoft.framework.ui.core.UIContext;
 import org.echosoft.framework.ui.extjs.ExtJSPage;
 import org.echosoft.framework.ui.extjs.layout.FitLayout;
 import org.echosoft.framework.ui.extjs.widgets.Button;
 import org.echosoft.framework.ui.extjs.widgets.ButtonGroup;
 import org.echosoft.framework.ui.extjs.widgets.TabPanel;
 import org.echosoft.framework.ui.extjs.widgets.Toolbar;
 import org.echosoft.framework.ui.extjs.widgets.form.AbstractField;
 import org.echosoft.framework.ui.extjs.widgets.form.CheckBox;
 import org.echosoft.framework.ui.extjs.widgets.form.CheckBoxGroup;
 import org.echosoft.framework.ui.extjs.widgets.form.DateField;
 import org.echosoft.framework.ui.extjs.widgets.form.DateRangeField;
 import org.echosoft.framework.ui.extjs.widgets.form.DecimalField;
 import org.echosoft.framework.ui.extjs.widgets.form.DecimalRangeField;
 import org.echosoft.framework.ui.extjs.widgets.form.FieldSet;
 import org.echosoft.framework.ui.extjs.widgets.form.FormPanel;
 import org.echosoft.framework.ui.extjs.widgets.form.IntegerField;
 import org.echosoft.framework.ui.extjs.widgets.form.IntegerRangeField;
 import org.echosoft.framework.ui.extjs.widgets.form.LongField;
 import org.echosoft.framework.ui.extjs.widgets.form.LongRangeField;
 import org.echosoft.framework.ui.extjs.widgets.form.Radio;
 import org.echosoft.framework.ui.extjs.widgets.form.TextAreaField;
 import org.echosoft.framework.ui.extjs.widgets.form.TextField;
 
 import examples.ui.Dispatcher;
 
 /**
  * @author Anton Sharapov
  */
 public class FormsDispatcher implements Dispatcher {
 
     public void dispatch(final UIContext uctx) throws Exception {
         final ExtJSPage page = new ExtJSPage(uctx);
         final ComponentContext ctx = page.getContext();     // or  ctx = new ComponentContext(uctx)
         page.setTitle("My first page");
         page.setIcon("/img/favicon.ico");
         page.setLayout( new FitLayout() );
 
         final TabPanel tabs = page.append( new TabPanel(ctx.getChild("tp")) );
         tabs.setResizeTabs(true);
         tabs.setActiveItem("fp1");
 
         final FormPanel fp1 = tabs.append( new FormPanel(ctx.getChild("fp1")) );
         fp1.setTitle("Form 1");
         fp1.setTabTip("Hint for pane 1");
         fp1.setWidth(400);
         fp1.setHeight(600);
         fp1.setFrame(true);
         fp1.setUrl("/examples/forms");
         fp1.setStandardSubmit(true);
         fp1.setMethod(FormPanel.Method.POST);
 
         final FormPanel fp2 = tabs.append( new FormPanel(ctx.getChild("fp2")) );
         fp2.setTitle("Form 2");
         fp2.setTabTip("Hint for pane 2");
         fp2.setWidth(400);
         fp2.setFrame(true);
         fp2.setUrl("/examples/forms");
         fp2.setStandardSubmit(true);
         fp2.setMethod(FormPanel.Method.POST);
 
         final FieldSet fs1 = fp1.append( new FieldSet(ctx.getChild("fs1")) );
         fs1.setTitle("Regular fields");
         fs1.setCollapsible(true);
         fs1.setTitleCollapse(true);
         fs1.setCollapsed(false);
         fs1.setAnchor("100%");
 
         final TextField txt1 = fs1.append( new TextField(ctx.getChild("txt1")) );
         txt1.setFieldLabel("Text");
         txt1.setValue("Vasya");
         txt1.setAllowBlank(false);
         txt1.setMinLength(3);
         txt1.setMaxLength(10);
         txt1.setMsgTarget(AbstractField.MsgTarget.UNDER);
         txt1.setStateful(true);
 
         final TextAreaField txt2 = fs1.append( new TextAreaField(ctx.getChild("txt2")) );
         txt2.setFieldLabel("Text Area");
         txt2.setEmptyText("enter something...");
         txt2.setMaxLength(1000);
         txt2.setHeight(150);
         txt2.setAnchor("100% ");
         txt2.setMsgTarget(AbstractField.MsgTarget.UNDER);
         txt2.setStateful(true);
 
         final IntegerField num1 = fs1.append( new IntegerField(ctx.getChild("num1")) );
         num1.setFieldLabel("Integer");
         num1.setValue(5);
         num1.setMinValue(-1);
         num1.setMaxLength(3);
         num1.setMsgTarget(AbstractField.MsgTarget.UNDER);
         num1.setStateful(true);
         ctx.getMessages().addMessage( new Message("num1", Message.Severity.ERROR, "my serverside message") );
 
         final LongField num2 = fs1.append( new LongField(ctx.getChild("num2")) );
         num2.setFieldLabel("Long");
         num2.setValue((long)5);
         num2.setMinValue((long)-1);
         num2.setMsgTarget(AbstractField.MsgTarget.UNDER);
         num2.setStateful(true);
 
         final DecimalField num3 = fs1.append( new DecimalField(ctx.getChild("num3")) );
         num3.setFieldLabel("Decimal 3");
         num3.setPrecision(3);
         num3.setMinValue( new BigDecimal(0.0009) );
         num3.setMaxValue( new BigDecimal(100.1) );
         num3.setMsgTarget(AbstractField.MsgTarget.SIDE);
         num3.setStateful(true);
         ctx.getMessages().addMessage( new Message("num3", Message.Severity.ERROR, "my serverside message 3") );
 
         final DateField dat1 = fs1.append( new DateField(ctx.getChild("dat1")) );
         dat1.setFieldLabel("Date");
         dat1.setDisabledDays(new int[]{0,6});
         dat1.setMaxValue( Types.DATE.decode("20.05.2010") );
         dat1.setMsgTarget(AbstractField.MsgTarget.SIDE);
         dat1.setStateful(true);
 
         final FieldSet fs2 = fp1.append( new FieldSet(ctx.getChild("fs2")) );
         fs2.setTitle("Range fields");
         fs2.setTitleCollapse(true);
         fs2.setCollapsible(true);
         fs2.setCollapsed(false);
         fs2.setAnchor("100%");
 
         final IntegerRangeField rng1 = fs2.append( new IntegerRangeField(ctx.getChild("rng1")) );
         rng1.setFieldLabel("Int range");
         rng1.setMinValue(0);
         rng1.setMaxValue(100);
         rng1.setFrom(1);
         rng1.setStateful(true);
         rng1.setFieldWidth(75);
         rng1.setMsgTarget(AbstractField.MsgTarget.UNDER);
 
         final LongRangeField rng2 = fs2.append( new LongRangeField(ctx.getChild("rng2")) );
         rng2.setFieldLabel("Long range");
         rng2.setMinValue(-10000L);
         rng2.setMaxValue(10000L);
         rng2.setFrom(-10L);
         rng2.setTo(10L);
         rng2.setStateful(true);
         rng2.setFieldWidth(75);
         rng2.setMsgTarget(AbstractField.MsgTarget.UNDER);
 
         final DecimalRangeField rng3 = fs2.append( new DecimalRangeField(ctx.getChild("rng3")) );
         rng3.setFieldLabel("Decimal range");
         rng3.setPrecision(3);
         rng3.setFrom( BigDecimal.ZERO );
         rng3.setTo( new BigDecimal(3.1415926) );
         rng3.setStateful(true);
         rng3.setFieldWidth(75);
         rng3.setMsgTarget(AbstractField.MsgTarget.UNDER);
 
         final DateRangeField rng4 = fs2.append( new DateRangeField(ctx.getChild("rng4")) );
         rng4.setFieldLabel("Date range");
         rng4.setStateful(true);
         rng4.setMsgTarget( AbstractField.MsgTarget.UNDER );
 
         final Toolbar fbar1 = fp1.assignFooter( new Toolbar(ctx.getChild("fb1")) );
         final Button bt11 = fbar1.addButton("submit 1");
         bt11.setHandler( new JSFunction(null,"this.ownerCt.ownerCt.getForm().submit()") );
         final Button bt12 = fbar1.addButton("cancel 1");
         bt12.setHandler( new JSFunction(null,"this.ownerCt.ownerCt.getForm().reset()") );
 
         final Button bt1 = fp2.append( new Button(ctx.getChild("bt1")) );
         bt1.setFieldLabel("Action");
         bt1.setText("click me");
         bt1.setHandler( new JSFunction(new String[]{"b", "e"}, "console.log(b,e);") );
 
 
         final CheckBox chb1 = fp2.append( new CheckBox(ctx.getChild("chb1")) );
        chb1.setFieldLabel("CheckBox");
         chb1.setBoxLabel("something <b>text</b> for checkbox.");
         chb1.setTristate(true);
         chb1.setStateful(true);
 
         final CheckBox chb2 = fp2.append( new CheckBox(ctx.getChild("chb2")) );
        chb2.setFieldLabel("CheckBox");
         chb2.setBoxLabel("something text for 2nd checkbox.");
         chb2.setTristate(true);
         chb2.setChecked(true);
         chb2.setStateful(true);
 
         final Radio rad1 = fp2.append( new Radio(ctx.getChild("rad1")) );
         rad1.setFieldLabel("Radio");
         rad1.setBoxLabel("something <b>text</b> for radio.");
         rad1.setChecked(true);
         rad1.setStateful(true);
 
         final CheckBoxGroup cbg1 = fp2.append( new CheckBoxGroup(ctx.getChild("cbg1")) );
         cbg1.setFieldLabel("CheckBoxGroup");
         cbg1.setVertical(true);
         cbg1.setAllowBlank(false);
         cbg1.setColumnsCount(3);
         cbg1.setStateful(true);
         cbg1.setStyle("border:1px dotted green");
         cbg1.setMsgTarget(AbstractField.MsgTarget.UNDER);
         cbg1.append( new CheckBox(ctx.getChild("cbi1")) ).setBoxLabel("item 1");
         cbg1.append( new CheckBox(ctx.getChild("cbi2")) ).setBoxLabel("item 2");
         cbg1.append( new CheckBox(ctx.getChild("cbi3")) ).setBoxLabel("item 3");
         cbg1.append( new CheckBox(ctx.getChild("cbi4")) ).setBoxLabel("item 4");
         cbg1.append( new CheckBox(ctx.getChild("cbi5")) ).setBoxLabel("item 5");
         cbg1.append( new CheckBox(ctx.getChild("cbi6")) ).setBoxLabel("item 6");
         cbg1.append( new CheckBox(ctx.getChild("cbi7")) ).setBoxLabel("item 7");
 
         final Toolbar fbar2 = fp2.assignFooter( new Toolbar(ctx.getChild("fb2")) );
         final Button bt21 = fbar2.addButton("submit 2");
         bt21.setHandler( new JSFunction(null,"this.ownerCt.ownerCt.getForm().submit()") );
         final Button bt31 = fbar2.addButton("cancel 2");
         bt31.setHandler( new JSFunction(null,"this.ownerCt.ownerCt.getForm().reset()") );
 
         final Toolbar tbar = tabs.assignTopToolbar( new Toolbar(ctx.getChild("tb1")) );
         final ButtonGroup bg1 = new ButtonGroup(ctx.getChild("bg1"));
         bg1.setTitle("My Actions");
         bg1.setColumns(2);
         tbar.append( bg1 );
         final Button bt2 = bg1.addButton("Action 1");
         final Button bt3 = bg1.addButton("Action 2");
 
         page.invokePage();
     }
 
 }

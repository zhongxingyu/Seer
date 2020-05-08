 /*
  * Created on 24 Oct 2006
  */
 package uk.ac.cam.caret.rsf.evolverimpl;
 
 import java.util.Date;
 
 import uk.org.ponder.beanutil.BeanGetter;
 import uk.org.ponder.dateutil.FieldDateTransit;
 import uk.org.ponder.dateutil.LocalSDF;
 import uk.org.ponder.htmlutil.DateSymbolJSEmitter;
 import uk.org.ponder.rsf.builtin.UVBProducer;
 import uk.org.ponder.rsf.components.ELReference;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInitBlock;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
 import uk.org.ponder.rsf.util.RSFUtil;
 import uk.org.ponder.stringutil.StringGetter;
 import uk.org.ponder.stringutil.StringHolder;
 
 public class FieldDateInputEvolver implements FormatAwareDateInputEvolver {
   public static final String COMPONENT_ID = "date-field-input:";
 
   private DateSymbolJSEmitter jsemitter;
 
   private StringGetter title = new StringHolder("Select Date");
 
   private String transitbase = "fieldDateTransit";
 
   private BeanGetter rbg;
 
   private String JSInitName = "RSF_Calendar.initYahooCalendar_Datefield";
 
   private String style = DATE_INPUT;
 
   public void setJSEmitter(DateSymbolJSEmitter jsemitter) {
     this.jsemitter = jsemitter;
   }
 
   public void setTitle(StringGetter title) {
     this.title = title;
   }
 
   public void setTransitBase(String transitbase) {
     this.transitbase = transitbase;
   }
 
   public void setRequestBeanGetter(BeanGetter rbg) {
     this.rbg = rbg;
   }
 
   public void setJSInitName(String JSInitName) {
     this.JSInitName = JSInitName;
   }
 
   public void setStyle(String style) {
     this.style = style;
   }
 
   public UIJointContainer evolveDateInput(UIInput toevolve, Date value) {
     toevolve.parent.remove(toevolve);
     UIJointContainer togo = new UIJointContainer(toevolve.parent, toevolve.ID,
         COMPONENT_ID);
 
     String ttbo = transitbase + "." + togo.getFullID();
 
     FieldDateTransit transit = (FieldDateTransit) rbg.getBean(ttbo);
     if (value == null) {
       value = (Date) rbg.getBean(toevolve.valuebinding.value);
     }
    else {
       transit.setDate(value);
     }
 
     String ttb = ttbo + ".";
 
     String jsblock = jsemitter.emitDateSymbols();
     UIVerbatim.make(togo, "datesymbols", jsblock);
 
     if (style.equals(DATE_INPUT) || style.equals(DATE_TIME_INPUT)) {
       UIInput field = UIInput.make(togo, "date-field", ttb + "short", transit.getShort());
       field.mustapply = true;
       UIOutput.make(togo, "date-annotation", null, ttb + "shortFormat");
     }
 
     if (style.equals(TIME_INPUT) || style.equals(DATE_TIME_INPUT)) {
       UIInput field = UIInput.make(togo, "time-field", ttb + "time", transit.getTime());
       field.mustapply = true;
       UIOutput.make(togo, "time-annotation", null, ttb + "timeFormat");
     }
 
     String truedateval = value == null ? null
         : LocalSDF.w3cformat.format(value);
     UIInput truedate = UIInput.make(togo, "true-date", ttb + "ISO8601TZ",
         truedateval);
     truedate.willinput = false;
 
     UIForm form = RSFUtil.findBasicForm(togo);
 
     form.parameters.add(new UIELBinding(toevolve.valuebinding.value,
         new ELReference(ttb + "date")));
 
     UIOutput.make(togo, "date-container");
     UIOutput.make(togo, "date-link");
 
     UIInitBlock.make(togo, "init-date", JSInitName, new Object[] { togo,
         title.get(), ttb, UVBProducer.PARAMS });
 
     return togo;
   }
 
   public UIJointContainer evolveDateInput(UIInput toevolve) {
     return evolveDateInput(toevolve, null);
   }
 
 }

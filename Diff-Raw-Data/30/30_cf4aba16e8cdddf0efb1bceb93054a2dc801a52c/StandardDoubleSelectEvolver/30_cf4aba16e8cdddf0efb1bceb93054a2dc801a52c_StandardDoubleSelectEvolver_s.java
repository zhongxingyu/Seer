 /*
  * Created on 30 Nov 2006
  */
 package uk.ac.cam.caret.rsf.evolverimpl;
 
 import uk.org.ponder.htmlutil.HTMLUtil;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIOutputMany;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.stringutil.StringSet;
 
 public class StandardDoubleSelectEvolver implements DoubleSelectEvolver {
   
   private String JSInitName = "DoubleList.init_DoubleList";
   private String sourcePickText = "Source pick list";
   private String destPickText = "Destination pick list";
   
   public static final String COMPONENT_ID = "input-doublelist:";
   
   public void setDestPickText(String destPickText) {
     this.destPickText = destPickText;
   }
   public void setSourcePickText(String sourcePickText) {
     this.sourcePickText = sourcePickText;
   }
   
   public UIJointContainer evolveSelect(UISelect toevolve) {
     UIJointContainer togo = new UIJointContainer(toevolve.parent, toevolve.ID, 
         COMPONENT_ID);
    toevolve.parent.remove(toevolve);
     
     // Separate out the multiple selection into left and right 
     StringList leftnames = new StringList();
     StringList leftvals = new StringList();
     
     StringList rightnames = new StringList();
     StringList rightvals = new StringList();
     
     String[] optionnames = toevolve.optionnames.getValue();
     String[] optionlist = toevolve.optionlist.getValue();
     
     StringSet selhash = UISelect.computeSelectionSet(toevolve.selection);
     
     for (int i = 0; i < optionnames.length; ++ i) {
       if (selhash.contains(optionlist[i])) {
         leftnames.add(optionnames[i]);
         leftvals.add(optionlist[i]);
       }
       else {
         rightnames.add(optionnames[i]);
         rightvals.add(optionlist[i]);
       }
     }
     
     UISelect leftselect = UISelect.makeMultiple(togo, "list1", 
         leftnames.toStringArray(), 
         toevolve.selection.valuebinding.value, null);
     leftselect.optionlist = UIOutputMany.make(leftvals.toStringArray());
     
     UISelect rightselect = UISelect.makeMultiple(togo, "list2", 
         rightnames.toStringArray(), 
         toevolve.selection.valuebinding.value, null);
     rightselect.optionlist = UIOutputMany.make(rightvals.toStringArray());
     rightselect.selection.willinput = false;
     rightselect.selection.fossilize = false;
     
     String initdate = HTMLUtil.emitJavascriptCall(JSInitName, togo.getFullID());
     UIVerbatim.make(togo, "init-select", initdate);
     
     UIOutput.make(togo, "move-left");
     UIOutput.make(togo, "move-right");
     UIOutput.make(togo, "move-all-left");
     UIOutput.make(togo, "move-all-right");
     
     UIOutput label1 = UIOutput.make(togo, "label1", sourcePickText);
     label1.decorators = new DecoratorList(new UILabelTargetDecorator(leftselect));
     UIOutput label2 = UIOutput.make(togo, "label2", destPickText);
     label2.decorators = new DecoratorList(new UILabelTargetDecorator(rightselect));
     
     return togo;
   }
 
 }

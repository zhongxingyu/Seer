 /*
  * Created on 6 Mar 2007
  */
 package uk.ac.cam.caret.rsf.evolverimpl;
 
 import uk.org.ponder.beanutil.BeanGetter;
 import uk.org.ponder.rsf.components.UIBasicListMember;
 import uk.org.ponder.rsf.components.UIBoundString;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UIInitBlock;
 import uk.org.ponder.rsf.components.UIInputMany;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.evolvers.BoundedDynamicListInputEvolver;
 import uk.org.ponder.rsf.uitype.UITypes;
 
 public class StandardDynamicListInputEvolver implements
     BoundedDynamicListInputEvolver {
 
   public static final String COMPONENT_ID = "dynamic-list-input:";
   public static final String CORE_ID = "dynamic-list-input-core:";
 
   private BeanGetter rbg;
   private UIBoundString removelabel;
   private UIBoundString addlabel;
   private int maxlength = 1000;
   private int minlength = 0;
 
   public void setRequestBeanGetter(BeanGetter rbg) {
     this.rbg = rbg;
   }
 
   public UIJointContainer evolve(UIInputMany toevolve) {
     toevolve.parent.remove(toevolve);
     UIJointContainer togo = new UIJointContainer(toevolve.parent, toevolve.ID,
         COMPONENT_ID);
     toevolve.ID = "list-control";
     togo.addComponent(toevolve);
 
     String[] value = toevolve.getValue();
     // Note that a bound value is NEVER null, an unset value is detected via
     // this standard call
     if (UITypes.isPlaceholder(value)) {
       value = (String[]) rbg.getBean(toevolve.valuebinding.value);
       // May as well save on later fixups
       toevolve.setValue(value);
     }
     UIBranchContainer core = UIBranchContainer.make(togo, CORE_ID);
     int limit = Math.max(minlength, value.length);
     for (int i = 0; i < limit; ++i) {
       UIBranchContainer row = UIBranchContainer.make(core,
           "dynamic-list-input-row:", Integer.toString(i));
       String thisvalue = i < value.length? value[i] : "";
       UIOutput.make(row, "input", thisvalue);
       UIBasicListMember.makeBasic(row, "input", toevolve.getFullID(), i);
       UIOutput.make(row, "remove", removelabel.getValue(),
           removelabel.valuebinding == null ? null
               : removelabel.valuebinding.value);
     }
     UIOutput.make(core, "add-row", addlabel.getValue(),
         addlabel.valuebinding == null ? null
             : addlabel.valuebinding.value);
     UIInitBlock.make(togo, "init-script", 
         "DynamicListInput.init_DynamicListInput", 
        new Object[] {core, limit, minlength, maxlength});
    
     return togo;
   }
 
   public void setLabels(UIBoundString removelabel, UIBoundString addlabel) {
     this.removelabel = removelabel;
     this.addlabel = addlabel;
   }
 
   public void setMaximumLength(int maxlength) {
     this.maxlength = maxlength;
   }
 
   public void setMinimumLength(int minlength) {
     this.minlength = minlength;
   }
 
 }

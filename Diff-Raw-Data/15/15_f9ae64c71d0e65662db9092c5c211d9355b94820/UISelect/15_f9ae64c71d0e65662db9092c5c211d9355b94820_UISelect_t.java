 /*
  * Created on 13-Jan-2006
  */
 package uk.org.ponder.rsf.components;
 
 import uk.org.ponder.stringutil.StringSet;
 
 /**
  * Backs a selection control of some kind, where named values are presented in a
  * list to the user. The returned value which is submitted may be a single
  * selection, multiple selection, or empty, depending on the component type in
  * the <code>selection</code> field.
  * <p>
  * The value binding <code>valuebinding</code> in the superclass, if
  * non-empty, will retrieve an object list, which will be supplied during fixup
  * to the resolving beans referenced by <code>nameresolver</code> (which must
  * not be empty in this case) and <code>idresolver</code>. If idresolver is
  * empty, the list is assumed to be a list of IDs already.
  * 
  */
 
 public class UISelect extends UIComponent implements FixableComponent {
   public UIBoundList optionlist;
 
   /** A component representing the rendered labels for the list control */
   public UIBoundList optionnames;
   /**
    * The input component representing the actual selected value. Expected to be
    * either UIInput or UIInputMany.
    */
   public UIBound selection;
 
   /**
    * This field is set during fixup for reference of the renderer. Do not set
    * this manually.
    */
   public StringSet selected;
 
   /** Creates a selection control without a value binding, either 
    * non-submitting, or suitable for use in a GET form */
   public static UISelect make(UIContainer tofill, String ID, String[] values,
       String[] labels, String value, boolean willinput) {
     UISelect togo = new UISelect();
     togo.optionlist = new UIOutputMany();
     togo.ID = ID;
     togo.optionlist.setValue(values);
     togo.optionnames = new UIBoundList();
     togo.optionnames.setValue(labels);
     togo.selection = new UIBoundString();
     if (value != null) {
       ((UIBoundString) togo.selection).setValue(value);
     }
     togo.selection.willinput = willinput;
     tofill.addComponent(togo);
     return togo;
   }
 
   /** A "skeleton" make method to prepare for more complex constructions */
   public static UISelect make(UIContainer tofill, String ID) {
     UISelect togo = new UISelect();
     togo.ID = ID;
     tofill.addComponent(togo);
     return togo;
   }
 // TODO: perhaps split this as a "postConstruct()" and "postFixup()" 
 // method. Will we actually ever have complex components?
   public void fixupComponent() {
     if (optionnames != null) {
       if (optionnames.valuebinding == null) {
         optionnames.valuebinding = optionlist.valuebinding;
       }
     }
     else {
       throw new IllegalArgumentException("UISelect component with full ID " + 
           getFullID() + " does not have optionnames set");
     }
     selected = computeSelectionSet(selection);
   }
 
   public static StringSet computeSelectionSet(UIBound selection) {
     StringSet togo = new StringSet();
     if (selection instanceof UIBoundList) {
       togo.addAll(((UIBoundList) selection).getValue());
     }
     else if (selection instanceof UIBoundString) {
       togo.add(((UIBoundString) selection).getValue());
     }
     return togo;
   }
   
   protected static UISelect make(UIContainer tofill, String ID,
      String[] options) {
     UISelect togo = new UISelect();
     togo.ID = ID;
     togo.optionlist = togo.optionnames = UIOutputMany.make(options);
     tofill.addComponent(togo);
     return togo;
   }
 
   /**
    * Constructs a single selection control, where the submitted values are
    * identical with the rendered labels
    */
   public static UISelect make(UIContainer tofill, String ID, String[] options,
       String valuebinding, String initvalue) {
    UISelect togo = make(tofill, ID, options);
     UIInput selection = UIInput.make(valuebinding);
     if (initvalue != null) {
       selection.setValue(initvalue);
     }
     togo.selection = selection;
     return togo;
   }
 
   /**
    * Constructs a single selection control, with labels distinct from the 
    * submitting values.
    */
   
   public static UISelect make(UIContainer tofill, String ID, String[] options,
       String[] labels, String valuebinding, String initvalue) {
     UISelect togo = make(tofill, ID, options, valuebinding, initvalue);
     if (labels != null) {
       togo.optionnames = UIOutputMany.make(labels);
     }
     return togo;
   }
 
   /**
    * Constructs a multiple selection control, where the submitted values are
    * identical with the rendered labels. Named differently to allow overload
    * where the final parameter is null.
    */
   public static UISelect makeMultiple(UIContainer tofill, String ID,
       String[] options, String valuebinding, String[] initvalue) {
    UISelect togo = make(tofill, ID, options);
     UIInputMany selection = UIInputMany.make(valuebinding);
     if (initvalue != null) {
       selection.setValue(initvalue);
     }
     togo.selection = selection;
     return togo;
   }
 
 }

 package org.wyona.yanel.impl.jelly;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.wyona.yanel.core.api.attributes.creatable.AbstractResourceInputItem;
 
 import org.apache.log4j.Logger;
 
 /**
  * This InputItem is used to specify an input which has a predefined selection
  * from where one value can be selected.
  */
 public abstract class InputItemWithOneSelectableOption extends AbstractResourceInputItem {
 
     private static Logger log = Logger.getLogger(InputItemWithOneSelectableOption.class);
 
     private List<Option> options = new ArrayList<Option>();
     private int selected = -1;
 
     /**
      * Creates a InputItemWithOneSelectableOption with given name and no options.
      * @param name
      */
     public InputItemWithOneSelectableOption(String name) {
         super(name);
     }
 
     /**
      * Creates a InputItemWithOneSelectableOption with given name and options.
      * 
      * @param name
      * @param options
      */
     public InputItemWithOneSelectableOption(String name, Option[] options) {
         this(name, options, -1);
     }
     
     public InputItemWithOneSelectableOption(String name, Option[] options, int selectIndex) {
         super(name);
         if(options == null ){
             options = new Option[0];
         }
         this.options = Arrays.asList(options);
         
         selectIndex = (selectIndex < 0 || selectIndex >= this.options.size() ? -1 : selectIndex);
         this.selected = selectIndex;
     }
 
     /**
      * Add an option to the list of possible values.
      * @param option
      */
     public void add(Option option){
         if(!options.contains(option)){
             options.add(option);
         }
     }
 
     /**
     * @deprecated Do use doSetValue(Object) instead, because it already existed before this method was unknowingly introduced.
      * Append an option to the list of existing options and select this particular option.
      * @param option Option
      */
/*
     public void addSelected(Option option) {
         if(!options.contains(option)){
             options.add(option);
             selected = options.size() - 1;
         } else {
             log.error("Option " + option.getLabel() + " already exists!");
         }
     }
*/
     
     /**
      * Removes an option from the list of possible values.
      * @param option
      */
     public void remove(Option option){
         int index = options.indexOf(option);
         options.remove(option);
         if(selected >= 0 && index >= 0 && index <= selected){
             selected--;
         }
     }
     
     public void clearOptions(){
         options.clear();
         selected = -1;
     }
     
     /**
      * @return a Option
      * */
     public Object getValue() {
         if(options.size() > selected && selected >= 0){
             return options.get(selected);
         }
 		return null;
         
     }
 
     /**
     * Set a particular object to 'selected', whereas handles instance of String and Option, but the actual value will NOT be overwritten
     * @param value Object which shall be selected
     */
     public void doSetValue(Object value) {
        if(value == null) {
             selected = -1;
             return;
         }
         
         if (value instanceof Option) {
             Option v = (Option) value;
             selected = options.indexOf(v);
         }else if(value instanceof String){
             int index = 0;
             for (Iterator<Option> i = options.iterator(); i.hasNext();index++) {
                 Option o = i.next();
                 if (value.equals(o.getValue())) {
                     selected = index;
                     break;
                 }else if(!i.hasNext()){
                     selected = -1;
                 }
             }
         }else{
             throw new IllegalArgumentException("Passed argument is not a String nor Option: " + value.toString());
         }
     }
 
     /**
      * @return the list of options.
      */
     public Option[] getOptions() {
         return options.toArray(new Option[options.size()]);
     }
     
     public void sort(){
         Collections.sort(options);
     } 
 }

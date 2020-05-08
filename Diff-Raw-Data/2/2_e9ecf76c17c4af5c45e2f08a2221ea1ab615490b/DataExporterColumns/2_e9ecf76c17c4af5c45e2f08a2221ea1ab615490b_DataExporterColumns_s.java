 /*
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations under
  * the License.
  *
  * The Original Code is ICEfaces 1.5 open source software code, released
  * November 5, 2006. The Initial Developer of the Original Code is ICEsoft
  * Technologies Canada, Corp. Portions created by ICEsoft are Copyright (C)
  * 2004-2011 ICEsoft Technologies Canada, Corp. All Rights Reserved.
  *
  * Contributor(s): _____________________.
  */
 
 package org.icefaces.samples.showcase.example.ace.dataExporter;
 
 import org.icefaces.samples.showcase.metadata.annotation.*;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 @ComponentExample(
         parent = DataExporterBean.BEAN_NAME,
         title = "example.ace.dataExporter.columns.title",
         description = "example.ace.dataExporter.columns.description",
         example = "/resources/examples/ace/dataExporter/dataExporterColumns.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="dataExporterColumns.xhtml",
                     resource = "/resources/examples/ace/dataExporter/dataExporterColumns.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="DataExporterColumns.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/showcase"+
                     "/example/ace/dataExporter/DataExporterColumns.java")
         }
 )
 @ManagedBean(name= DataExporterColumns.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class DataExporterColumns extends ComponentExampleImpl<DataExporterColumns> implements Serializable {
     public static final String BEAN_NAME = "dataExporterColumns";
     
     private String[] chosenColumns;
     private String chosenColumnsString;
     private Map<String, Integer> options;
     private String type;
     
     public DataExporterColumns() {
         super(DataExporterColumns.class);
         initializeVariables();
     }
     
     /////////////---- PRIVATE METHODS BEGIN
     
     private void initializeVariables() {
         this.options = new LinkedHashMap<String, Integer> ();
         this.options.put("ID", 0);
         this.options.put("Name", 1);
         this.options.put("Chassis", 2);
         this.options.put("Weight", 3);
         this.options.put("Accel", 4);
         this.options.put("MPG", 5);
         this.options.put("Cost", 6);
         
         chosenColumns = new String[]{"0", "3", "5"};
        chosenColumnsString = "0,3,5";
         type = "csv";
     }
     
     /*
      * This method creates an outcome in such a way that next is true:
      * outcome and choosenOptions are subsets of availableOptions set
      * outcome and choosenOptions does not have common elements (you can say that they are oposite in the context of availableOptions set)
      */
     private ArrayList<Integer> processSelection(Map<String, Integer> availableOptions, String[] choosenOptions) 
     {
         ArrayList<Integer> outcome = new ArrayList<Integer>();
         boolean conditionExist;
         for (Integer availableOption: availableOptions.values()) {
             conditionExist = false;
             for (String choosenOption : choosenOptions) {
                 if(availableOption == Integer.parseInt(choosenOption)) {
                     conditionExist = true;
                     break;
                 }
             }
             if(!conditionExist)
                 outcome.add(availableOption);
         }
         return outcome;
     }
     
     private String parseArrayToCommas(Integer[] array) {
         if ((array != null) && (array.length > 0)) {
             Arrays.sort(array);
             
             StringBuilder sb = new StringBuilder(array.length * 2);
             for (Integer currentColumn : array) {
                 sb.append(currentColumn);
                 sb.append(",");
             }
             sb.deleteCharAt(sb.length()-1); // Remove the trailing comma
             return sb.toString();
         }
         return "";
     }
     
     /////////////---- GETTERS & SETTERS BEGIN
     public String getChosenColumnsString() { return chosenColumnsString; }
     public Map<String, Integer> getOptions() { return options; }
     public String getType() { return type; }
     public String[] getChosenColumns() { return chosenColumns; }
     
     public void setChosenColumnsString(String chosenColumnsString) { this.chosenColumnsString = chosenColumnsString; }
     public void setOptions(Map<String, Integer> options) { this.options = options; }
     public void setType(String type) { this.type = type; }
     public void setChosenColumns(String[] chosenColumns) {
         this.chosenColumns = chosenColumns;
         //creaate an array with unselected columns based on all options and chosen columns
         ArrayList<Integer> unselectedColumns = processSelection(options, chosenColumns);
         //convert ArrayList<Integer> into Integer[] and parse it as String
         Integer[] arrayWithIntegers = convertIntoArray(unselectedColumns);
         
         //Integer[] testArray = Arrays.copyOf(array, array.length, Integer[].class); -- does not work on JDK 1.5
         chosenColumnsString = parseArrayToCommas( arrayWithIntegers);
     }
 
     private Integer[] convertIntoArray(ArrayList<Integer> unselectedColumns) 
     {
         Integer[] outcome = new Integer[unselectedColumns.size()];
         for(int i=0; i <unselectedColumns.size(); i++)
             outcome[i] = unselectedColumns.get(i);
         
         return outcome;
     }
 }

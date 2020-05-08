 /*
  * Copyright (C) 2012 Red Hat, Inc.     
  * 
  * This copyrighted material is made available to anyone wishing to use, 
  * modify, copy, or redistribute it subject to the terms and conditions of the 
  * GNU General Public License v.2.
  * 
  * Authors: Adam Saleh (asaleh at redhat dot com)
  *          Jan Rusnacko (jrusnack at redhat dot com)
  */
 package NitrateIntegration;
 
 import NitrateIntegration.TcmsReport.PropertyTransform.Tuple;
 import com.redhat.engineering.jenkins.testparser.results.TestResults;
 import com.redhat.nitrate.TcmsException;
 import hudson.model.AbstractBuild;
 import java.util.*;
 import java.util.Map.Entry;
 
 /**
  * @author jrusnack
  * @author asaleh
  */
 public class TcmsReport {
     
     private Set<TestRunResults> testRuns;
     
    private HashSet<Map.Entry<String,String>> propertyValueSet;
    private HashMap<Map.Entry<String,String>,String> wrongPropertyValueMap;
     boolean wrongProperty;
     Set<String> propertyWithWrongValue = new HashSet<String>();
     /* Store mapping current name -> old Jenkins name */
     private PropertyTransform propertyTransformations;
         
     /**
     * Class that defines transformations (key, value) -> (key, value). This is
     * used in case when user renames Jenkins`s axes to some new names - new
     * transformation from original names and values to new ones is added.
     */
     public static  class PropertyTransform {
 
         public static class Tuple<K, V> implements Map.Entry<K, V> {
 
             private K key;
             private V val;
 
             public Tuple(K key, V val) {
                 this.key = key;
                 this.val = val;
             }
 
             public K getKey() {
                 return key;
             }
 
             public V getValue() {
                 return val;
             }
 
             public Object setValue(Object v) {
                 throw new UnsupportedOperationException("Not supported yet.");
             }
         }
 
         public void clearTransformations() {
             propertyTransform.clear();
         }
 
         public void addTransformation(String oldprop, String oldval, String newprop, String newval) {
             propertyTransform.put(new Tuple(oldprop, oldval), new Tuple(newprop, newval));
         }
         
         public void editTransformation(String oldprop, String oldval, String newprop, String newval) {
             if(propertyTransform.containsKey(new Tuple(oldprop, oldval)) == false){
                  throw new IllegalArgumentException("Nonexistent property");
             }
             if(newprop==null){
                 newprop = getTransformation(oldprop, oldval).getKey();
             }
             if(newval==null){
                 newval = getTransformation(oldprop, oldval).getValue();
             }
             propertyTransform.put(new Tuple(oldprop, oldval), new Tuple(newprop, newval));
         }
         
         public  Map.Entry<String, String> getTransformation(String oldprop, String oldval) {
             return propertyTransform.get(new Tuple(oldprop, oldval));
         }
         
         public  Map.Entry<String, String> getTransformation(Map.Entry<String, String> l) {
             return propertyTransform.get(l);
         }
         
        private HashMap<Map.Entry<String, String>, Map.Entry<String, String>> propertyTransform;
 
         public Set<Map.Entry<String, String>> transformVariables(Set<Map.Entry<String, String>> old) {
 
             HashSet<Map.Entry<String, String>> transformed = new HashSet<Map.Entry<String, String>>();
 
             for (Map.Entry<String, String> prop_value : old) {
 
                 Map.Entry<String, String> newprop_value = prop_value;
                 if (propertyTransform.containsKey(prop_value)) {
                     newprop_value = propertyTransform.get(prop_value);
                 }
 
                 transformed.add(new Tuple(newprop_value.getKey(), newprop_value.getValue()));
             }
             return transformed;
         }
         
         public Map<String, String> transformVariables(Map<String, String> old) {
 
             Map<String, String> transformed = new HashMap<String, String>();
             for(Map.Entry<String,String> e:transformVariables(old.entrySet())){
                 transformed.put(e.getKey(), e.getValue());
             }
             return transformed;
             
         }
         
         public Map.Entry<String, String> transformVariable(Map.Entry<String, String> prop_value){
             return propertyTransform.get(prop_value);
         }
     }
     
     /**
      * Formerly known as GatherFiles
      * 
      * Stores all important information about one TestRun (in Jenkins`s language
      * one MatrixRun for multiconf project or just build in freestyle project)
      */
     public static class TestRunResults{
 
         public TestResults results;
         public AbstractBuild build;
         public Map<String, String> variables;
 
         public TestRunResults(TestResults results, AbstractBuild build, Map<String, String> variables) {
             this.results = results;
             this.build = build;
             this.variables = variables;
         }
     }
     
     public TcmsReport(){
         testRuns = new HashSet<TestRunResults>();
         
         propertyTransformations = new PropertyTransform();        
     }
     
     public HashSet<Entry<String, String>> getPropertyValueSet(){
         return propertyValueSet;
     }
     public Map.Entry<String,String> transformPropertyValue(Map.Entry<String,String> property_value){
         return propertyTransformations.transformVariable(property_value);
     }
   
 
     /**
      * Adds one TestRun to report. Formerly known as addGatherPath
      * 
      * @param results
      * @param build
      * @param variables 
      */
     public void addTestRun(TestResults results, AbstractBuild build, Map<String, String> variables){
         testRuns.add(new TestRunResults(results, build, variables));
         for(Map.Entry<String,String> envProperty: variables.entrySet()){
             propertyValueSet.add(envProperty);
             propertyTransformations.addTransformation(envProperty.getKey(), envProperty.getValue(),envProperty.getKey(), envProperty.getValue());
         }
     }
    
      public HashSet<String> updateReportFromReq(Map params){
         
         HashSet<String> problems = new HashSet<String>();
         
         /*
          * update values first
          */
         for (Iterator it = params.entrySet().iterator(); it.hasNext();) {
             Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
             
             if (entry.getKey().startsWith("value-")) {
                 String newValue = ((String[]) entry.getValue())[0];
                 String property = entry.getKey().replaceFirst("value-", "");
                 String value = property.split("=>")[1];
                 property = property.split("=>")[0];
 
                 if (!value.equals(newValue)) {
                     try {
                         propertyTransformations.editTransformation(property, value, null, newValue);
                     } catch (IllegalArgumentException ex) {
                         problems.add(ex.getMessage());
                     }
                 }
             }
         }
         
         /*
          * change property-names second
          */
         for (Iterator it = params.entrySet().iterator(); it.hasNext();) {
             Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
             
             if (entry.getKey().startsWith("property-")) {
                 String newProperty = ((String[]) entry.getValue())[0];
                 String property = entry.getKey().replaceFirst("property-", "");
                 String value = property.split("=>")[1];
                 property = property.split("=>")[0];
                 
                 if (!property.equals(newProperty)) {
                     try {
                         propertyTransformations.editTransformation(property, value, newProperty, null);
                     } catch (IllegalArgumentException ex) {
                         problems.add(ex.getMessage());
                     }
                 }
             }
         }
         
         return problems;
     }
     
     public void checkEnvironmentMapping(TcmsEnvironment environment) throws TcmsException {
         if (!environment.isEmpty()) {
             wrongPropertyValueMap.clear();
             wrongProperty = false;
             propertyWithWrongValue.clear();
             environment.fetchAvailableProperties();
 
             Set<String> reloaded=new HashSet<String>();
             for (Map.Entry<String,String> envProperty :propertyTransformations.transformVariables( propertyValueSet )) {
                 if (environment.containsProperty(envProperty.getKey())) {
                     /*
                      * When property is OK, check its values
                      */
                    if(reloaded.contains(envProperty.getKey())){
                         environment.reloadProperty(envProperty.getKey());
                         reloaded.add(envProperty.getKey());
                     }
                     
                     if (!environment.containsValue(envProperty.getKey(), envProperty.getValue())) {
                         wrongPropertyValueMap.put(envProperty, "WRONG VALUE");
                         propertyWithWrongValue.add(envProperty.getKey());
                     }
 
                 } else {
                     wrongPropertyValueMap.put(envProperty, "WRONG PROPERTY");
                     wrongProperty = true;
                 }
             }
         }
     }
 
     public boolean existsWrongEnvProperty(){
         return wrongProperty;
     }
     
     public boolean existWrongEnvValue(){
         return !propertyWithWrongValue.isEmpty();
     }
     
     public boolean isWrongEnvPropertyValue(String envProperty,String envValue){
         return wrongPropertyValueMap.containsKey(new Tuple(envProperty,envValue));
     }
     public String getWrongEnvPropertyValue(String envProperty,String envValue){
         return wrongPropertyValueMap.get(new Tuple(envProperty,envValue));
     }
     public String getWrongEnvPropertyValue(Map.Entry<String,String> pv){
         return wrongPropertyValueMap.get(pv);
     }
     
     public Set getWrongEnvPropertyValues(){
         return wrongPropertyValueMap.keySet();
     }
     
     
     public Set getEnvPropertiesWithWrongValues(){
         return propertyWithWrongValue;
     }
     /*
     public Set<TestRunResults> getTestRuns(){
         return testRuns;
     }
     */
     
     public Set<TestRunResults> getTestRuns_withAppliedVariableTransformations(){
         HashSet<TestRunResults> transformed = new HashSet<TestRunResults>();
         for(TestRunResults r:testRuns){
             transformed.add(new TestRunResults(r.results,r.build, 
                     propertyTransformations.transformVariables(r.variables)
                     )
             );
         }
         return transformed;
     }
     
 }

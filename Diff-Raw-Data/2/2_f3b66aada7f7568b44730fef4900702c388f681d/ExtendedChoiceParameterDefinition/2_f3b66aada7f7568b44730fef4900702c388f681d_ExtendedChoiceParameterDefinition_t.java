 package com.cwctravel.hudson.plugins.extended_choice_parameter;
 
 import hudson.Extension;
 import hudson.model.ParameterValue;
 import hudson.model.ParameterDefinition;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 public class ExtendedChoiceParameterDefinition extends ParameterDefinition {
 
     private static final long serialVersionUID = -2946187268529865645L;
     public static final String PARAMETER_TYPE_SINGLE_SELECT = "PT_SINGLE_SELECT";
     public static final String PARAMETER_TYPE_MULTI_SELECT = "PT_MULTI_SELECT";
 
     @Extension
     public static class DescriptorImpl extends ParameterDescriptor {
 
         @Override
         public String getDisplayName() {
            return "Extended Choice Parameter with Database Inquery";
         }
     }
     private String value;
     private String filename;
 
 //    public ExtendedChoiceParameterDefinition(String name, String type, String value, String propertyFile, String propertyKey, String defaultValue,
 //            String defaultPropertyFile, String defaultPropertyKey, boolean quoteValue, String description) {
 //        super(name, description);
 //        this.value = computeValue(value, propertyFile, propertyKey);
 //        this.filename = filename;
 //        this.defaultValue = computeValue(defaultValue, defaultPropertyFile, defaultPropertyKey);
 //        computeDefaultValueMap();
 //    }
     @DataBoundConstructor
     public ExtendedChoiceParameterDefinition(String value, String filename, String name, String description) {
         super(name, description);
 
         this.value = this.computeValue(value, filename);
 
         this.filename = filename;
     }
 
 //    private void computeDefaultValueMap() {
 //        if (!StringUtils.isBlank(defaultValue)) {
 //            defaultValueMap = new HashMap<String, Boolean>();
 //            String[] defaultValues = StringUtils.split(defaultValue, ',');
 //            for (String value : defaultValues) {
 //                defaultValueMap.put(value, true);
 //            }
 //        }
 //    }
     @Override
     public ParameterValue createValue(StaplerRequest request) {
         String value[] = request.getParameterValues(getName());
         if (value == null) {
             return getDefaultParameterValue();
         }
         return null;
     }
 
     @Override
     public ParameterValue createValue(StaplerRequest request, JSONObject jO) {
         Object value = jO.get("value");
         String strValue = "";
         if (value instanceof String) {
             strValue = (String) value;
         } else if (value instanceof JSONArray) {
             JSONArray jsonValues = (JSONArray) value;
             for (int i = 0; i < jsonValues.size(); i++) {
                 strValue += jsonValues.getString(i);
                 if (i < jsonValues.size() - 1) {
                     strValue += ",";
                 }
             }
         }
 
         ExtendedChoiceParameterValue extendedChoiceParameterValue = new ExtendedChoiceParameterValue(jO.getString("name"), strValue);
         return extendedChoiceParameterValue;
     }
 
     @Override
     public ParameterValue getDefaultParameterValue() {
         return new ExtendedChoiceParameterValue(getName(), "");
     }
 
     private String computeValue(String value, String scriptFile) {
         System.out.println("Compute Value");
         String productList = GenerateProducts.generateProduct(scriptFile);
         System.out.println("\n" + productList);
 
         if (productList != null && !StringUtils.isBlank(productList)) {
             return productList;
 //            BufferedReader in = null;
 //            try {
 //                in = new BufferedReader(new FileReader(productList));
 //                String result = in.readLine();
 //                return result;
 //            } catch (FileNotFoundException ex) {
 //                Logger.getLogger(ExtendedChoiceParameterDefinition.class.getName()).log(Level.SEVERE, null, ex);
 //                System.out.println("File not found");
 //            } catch (IOException e) {
 //                Logger.getLogger(ExtendedChoiceParameterDefinition.class.getName()).log(Level.SEVERE, null, e);
 //                System.out.println("IOException");
 //            } finally {
 //                try {
 //                    if (in != null) {
 //                        in.close();
 //                    }
 //                } catch (IOException ex) {
 //                    Logger.getLogger(ExtendedChoiceParameterDefinition.class.getName()).log(Level.SEVERE, null, ex);
 //                }
 //            }
         }
         if (!StringUtils.isBlank(value)) {
             return value;
         }
 
         return null;
     }
 
     public String getFilename() {
         return filename;
     }
 
     public void setFilename(String filename) {
         this.filename = filename;
     }
 
     public String getValue() {
         this.value = this.computeValue(this.value, this.filename);
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 }

 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2013, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 
 package com.janrain.android.capture;
 
 import android.util.Pair;
 import com.janrain.android.Jump;
 import org.json.JSONObject;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static com.janrain.android.utils.CollectionUtils.Function;
 import static com.janrain.android.utils.CollectionUtils.filter;
 import static com.janrain.android.utils.LogUtils.throwDebugException;
 
 public class CaptureFlowUtils {
     public static Set<Pair<String, String>> getFormFields(JSONObject newUser,
                                                           String formName,
                                                           Map<String, Object> captureFlow) {
         HashSet<Pair<String, String>> retval = new HashSet<Pair<String, String>>();
         if (formName == null || captureFlow == null) return null;
         Map form = (Map) ((Map) captureFlow.get("fields")).get(formName);
         final List fieldNames = (List) form.get("fields");
        Map fieldEntries = filter(((Map) captureFlow.get("fields")), new Function<Boolean, Map.Entry>() {
             public Boolean operate(Map.Entry arg) {
                 return fieldNames.contains(arg.getKey());
             }
         });
 
         for (Object fieldEntry_ : fieldEntries.entrySet()) {
             Map.Entry fieldEntry = (Map.Entry) fieldEntry_;
             if (!(fieldEntry.getValue() instanceof Map)) {
                 throwDebugException(new RuntimeException("unrecognized field defn: " +
                         fieldEntry.getValue()));
                 continue;
             }
 
             Object schemaId = ((Map) fieldEntry.getValue()).get("schemaId");
 
             String key = (String) fieldEntry.getKey();
 
             if (schemaId instanceof String) {
                 String dotPath = (String) schemaId;
 
                 String type = (String) ((Map) fieldEntry.getValue()).get("type");
                 if ("dateselect".equals(type)) {
                     addDateForDotPathToParams(retval, dotPath, newUser, key);
                 } else {
                     addValueForDotPathToParams(retval, dotPath, newUser, key);
                 }
             } else if (Map.class.isAssignableFrom(schemaId.getClass())) {
                 for (Object schemaEntry_ : ((Map) schemaId).entrySet()) {
                     Map.Entry schemaEntry = (Map.Entry) schemaEntry_;
                     String dotPath = (String) schemaEntry.getValue();
                     String subscript = (String) schemaEntry.getKey();
                     String paramName = String.format("%s[%s]", key, subscript);
 
                     addValueForDotPathToParams(retval, dotPath, newUser, paramName);
                 }
             }
         }
 
         return retval;
     }
 
     public static Set<Pair<String, String>> getTraditionalSignInCredentials(String user, String password) {
         final String formName = Jump.getCaptureTraditionalSignInFormName();
         final Map captureFlow = Jump.getCaptureFlow();
 
         if (formName == null || captureFlow == null) {
             throwDebugException(new RuntimeException("Cannot get traditional sign-in credentials without" +
                                                      " captureTraditionalSignInFormName and a captureFlow"));
             return null;
         }
 
         final Map fields = (Map) captureFlow.get("fields");
         final Map form = (Map) fields.get(formName);
         final List fieldNames = (List) form.get("fields");
 
         if (fieldNames.size() != 2) {
             throwDebugException(new RuntimeException("Sign-in form must have exactly two parameters"));
             return null;
         }
 
         String passwordFieldName = null;
         String userFieldName = null;
 
         for (Object fieldName : fieldNames) {
             Map field = (Map) fields.get(fieldName);
             String type = (String) field.get("type");
 
             if (type.equals("password")) {
                 passwordFieldName = (String) fieldName;
             } else {
                 userFieldName = (String) fieldName;
             }
         }
 
         if (passwordFieldName != null && userFieldName != null && user != null && password != null) {
 
             HashSet<Pair<String, String>> out = new HashSet<Pair<String, String>>();
             out.add(new Pair(passwordFieldName, password));
             out.add(new Pair(userFieldName, user));
             return out;
         } else {
             throwDebugException(new RuntimeException("Could not get traditional sign-in credentials from " +
                                                      "form " + formName));
             return null;
         }
     }
 
     private static void addValueForDotPathToParams(Set<Pair<String, String>> params, String dotPath,
                                                    JSONObject user, String key) {
         String formFieldValue = CaptureJsonUtils.valueForAttrByDotPath(user, dotPath);
 
         if (formFieldValue != null) {
             params.add(new Pair<String, String>(key, formFieldValue));
         }
     }
 
     private static void addDateForDotPathToParams(Set<Pair<String, String>> params, String dotPath,
                                                   JSONObject user, String key) {
         String formFieldValue = CaptureJsonUtils.valueForAttrByDotPath(user, dotPath);
 
         if (formFieldValue != null) {
             SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
             Date date = null;
             try {
                 // Make sure that the date is being set in the correct format
                 date = formatter.parse(formFieldValue);
             } catch (ParseException e) {
                 throw(new RuntimeException(key + " must be in yyyy-MM-dd format"));
             }
 
             Calendar cal = Calendar.getInstance();
             cal.setTime(date);
 
             params.add(new Pair<String, String>(key + "[dateselect_year]",
                                                 String.format("%04d", cal.get(Calendar.YEAR))));
             params.add(new Pair<String, String>(key + "[dateselect_month]",
                                                 String.format("%02d", cal.get(Calendar.MONTH) + 1)));
             params.add(new Pair<String, String>(key + "[dateselect_day]",
                                                 String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))));
         }
     }
 
     public static String getFlowVersion(Map<String, Object> captureFlow) {
         Object version = captureFlow.get("version");
         if (version instanceof String) return (String) version;
         throwDebugException(new RuntimeException("Error parsing flow version: " + version));
         return null;
     }
 
     public static Map<String, Object> getFieldDefinition(Map<String, Object> flow, String fieldName) {
         return (Map<String, Object>) ((Map<String, Object>) flow.get("fields")).get(fieldName);
     }
 }

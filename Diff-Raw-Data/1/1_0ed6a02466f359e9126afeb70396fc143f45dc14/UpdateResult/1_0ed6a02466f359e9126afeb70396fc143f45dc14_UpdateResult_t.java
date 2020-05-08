 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.legacy;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.validator.EmailValidator;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.extension.JSONConstructor;
 import org.json.extension.JSONEncoder;
 import org.json.extension.NoObjectContext;
 
 import com.google.gag.annotation.disclaimer.LegacySucks;
 
 import de.cosmocode.palava.bridge.content.ContentConverter;
 import de.cosmocode.palava.bridge.content.ConversionException;
 import de.cosmocode.palava.bridge.content.Convertible;
 import de.cosmocode.palava.bridge.content.KeyValueState;
 import de.cosmocode.rendering.Renderable;
 
 /**
  * I don't know why this is here.
  *
  * @deprecated dont use this
  */
 @Deprecated
 @LegacySucks
 /* CHECKSTYLE:OFF */
 public class UpdateResult implements Convertible, JSONEncoder {
 
     public static final String ERR_NULL = "missing";
     public static final String ERR_DIFFERENT = "different";
     public static final String ERR_FORMAT = "format";
     public static final String ERR_BADPASSWORD = "badpassword";
     public static final String ERR_NOTFOUND = "notfound";
     public static final String ERR_DUPLICATE = "duplicate";
    public static final String ERR_COMBINATION = "combination";
 
     private static final Pattern PHONE = Pattern.compile("^[+ 0-9()/]*$");
 
     private Map<String, List<String>> errors;
     private Object result;
     private Set<String> erroneousFields;
     private Exception exception;
     
     public boolean isError(){
         return errors != null || exception != null;
     }
     
     public Map<String, List<String>> getErrors() {
         return errors;
     }
     
     public Set<String> getFields() {
         return erroneousFields;
     }
     
     public boolean hasError(String field) {
         return erroneousFields != null && erroneousFields.contains(field);
     }
     
     /** adds an error 
      * @param error a description of the error
      * @param fields the list of affected fields (i.e. some of the Result.args keys)
      */
     public void addError( String error, List<String> fields ) throws IllegalStateException {
         if ( error == null ) throw new NullPointerException("error");
         if ( fields == null ) throw new NullPointerException("fields");
 
         if ( errors == null ) errors = new HashMap<String,List<String>>();
         if ( erroneousFields == null ) erroneousFields = new HashSet<String>();
         
         List<String> _fields = errors.get(error);
         
         if ( _fields != null ) {
             _fields.addAll( fields );
         } else {
             errors.put(error, fields);
         }
 
         // fields
 
         erroneousFields.addAll( fields ) ;
 
     }
     public void setException( Exception e ) {
         this.exception = e;
     }
     /** adds an error 
      * @param error a description of the error
      * @param fields the list of affected fields (i.e. some of the Result.args keys)
      */
     public void addError( String error, String... fields   ) throws IllegalStateException {
         if ( error == null ) throw new NullPointerException("error");
         if ( fields == null ) throw new NullPointerException("fields");
 
         List<String> list = new ArrayList<String>();
         for ( String f : fields) list.add(f);
         
         addError( error, list );
     }
     
     @Override
     public void convert(StringBuilder sb, ContentConverter converter)
             throws ConversionException {
     
         if ( errors == null && result == null && exception == null ) {
             converter.convertKeyValue(sb, "status", "success", KeyValueState.SINGLE);            
         } else {
             converter.convertKeyValue(sb, "status", isError() ? "error" : "success", KeyValueState.START);            
             if ( errors != null )
                 converter.convertKeyValue(sb, "errors", errors, KeyValueState.INSIDE);            
             if ( exception != null )
                 converter.convertKeyValue(sb, "exception", exception.getMessage(), KeyValueState.INSIDE);            
             converter.convertKeyValue(sb, "result", result, KeyValueState.LAST);            
         }
     }
     public void encodeJSON (JSONConstructor json) throws JSONException {
         
         json.object();
         if ( errors == null && result == null && exception == null ) {
             json.key("status").value("success");
         } else {
             json.key("status").value( isError() ? "error" : "success" );
             if ( errors != null ) 
                 json.key("errors").value(errors);
             if ( exception != null )
                 json.key("exception").value(exception.toString());
             if ( result != null ) {
                 json.key("result");
                 if ( result instanceof JSONEncoder ) {
                      final boolean objects = result instanceof NoObjectContext;
                      if (objects) json.object();
                     ((JSONEncoder)result).encodeJSON(json);
                     if (objects) json.endObject();
                 }
                 else
                     json.value( result ) ;
             } 
         }
 
         json.endObject();
     }
 
 
     public Object getResult() {
         return result;
     }
     public void setResult(Object result) {
         this.result = result;
     }
     
     public boolean validateString( String field, String value ) {
         if ( value != null && value.length() > 0 ) return true;
         
         addError(ERR_NULL, field);
         
         return false;
     }
     
     public boolean validateEmail( String field, String value) {
         if ( ! validateString(field, value) ) return false;
         
         int idx;
         if ( (idx=value.indexOf('@')) <= 0 || value.indexOf('.',idx) == -1 ) {
             addError(ERR_FORMAT, field);
             return false;
         }
         
         if (!EmailValidator.getInstance().isValid(value)) {
             addError(ERR_FORMAT, field);
             return false;
             
         }
         return true;
     }
     /** checks the validity of a phone number
      * adds a ERR_FORMAT on bad phone numbers
      * @param field
      * @param value
      * @param mandatory if true and value is null or empty: ERR_NULL is added
      * @return
      */
     public boolean validatePhone( String field, String value, boolean mandatory) {
         if ( mandatory ) {
             if( hasError(field) || ! validateString(field, value) ) return false;
         } else {
             if ( value == null || value.trim().length() == 0 ) return true;
         }
         Matcher m = PHONE.matcher(value);
 
         if(!m.matches()) {
             addError(UpdateResult.ERR_FORMAT, field);    
             return false;
         }
         return true;
     }
     public boolean validatePassword( String field, String password, int minLength ) {
         if ( password == null || password.length() < minLength ) {
             addError(UpdateResult.ERR_BADPASSWORD, field);
             return false;
         }
         return true;
     }
     /** lookup the field in the args and validate that the value is not different to compareTo
      * if the value is not equals, the ERR_DIFFERENT code is set on the field
      * example:
      *      String email = ur.lookupEmail(args,"email");
      *      if ( email != null ) validateEquals(args,"email2",email);
      *
      * example2:
      *      String password = ur.lookupPassword(args,"password",6);
      *      if ( password != null ) validateEquals(args,"password2",password);
      *
      * (note: we dont use the return value, because we check ur.isError anywehere else)
      * 
      *
      * @param args - the lookup map
      * @param field - the name of the field to lookup (eg "password2")
      * @param compareTo - the value to compare to
      *
      * @throws NullpointerException, if compareTo is null
      */
     public boolean validateEquals( Map<String, Object> args, String field, String compareTo ) {
         if (compareTo == null) throw new NullPointerException("compareTo");
 
         String value = lookupString(args,field);
         if ( value != null && ! value.equals(compareTo) ) {
             addError(ERR_DIFFERENT,field);
             return false;
         }
         return true;
     }
     public String lookupPhone(Map<String, Object> args, String field, boolean mandatory) {
         
         String value = lookupString( args, field, mandatory);
         
         return validatePhone(field, value,mandatory) ? value : null;
     }
     
     public String lookupEmail(Map<String, Object> args, String field) {
         return lookupEmail(args,field,true);
     }
     
     public String lookupEmail(Map<String, Object> args, String field,boolean mandatory) {
         String value = lookupString( args, field,mandatory);
         if (value==null) return null;
         if (value.length()==0) return null;
         else return validateEmail(field,value) ? value : null;
     }
     public String[] lookupEmails(Map<String, Object> args, String field) {
         String value = lookupString( args, field);
         if ( value == null ) return null;
         
         String[] values = value.split(",");
         for ( String v : values )
             if( !validateEmail(field, v) ) return null;
 
         return values;
     }
     public Long lookupLong(Map<String, Object> args, String field) {
         final Object v = args.get(field);
         String value = v == null ? null : v.toString();
         if ( value != null && ( value = value.trim()).length() == 0 ) return null;
         try {
             return Long.parseLong( value ) ;
         } catch ( NumberFormatException e ) {
             addError(field, ERR_FORMAT);
             return null;
         }
     }
     public Long lookupLong(Map<String, Object> args, String field, boolean mandatory) {
         Long value = lookupLong(args,field);
         if ( value == null && mandatory )
             addError(ERR_NULL,field);
         return value;
     }
     public String lookupString(Map<String, Object> args, String field) {
         return lookupString(args,field,true);
         
     }
     public String lookupString(Map<String, Object> args, String field, boolean mandatory) {
         final Object v = args.get(field);
         String value = v == null ? null : v.toString();
         if ( value != null ) value = value.trim();
         
         if ( mandatory ) return validateString(field, value ) ? value : null;
         else return value;
     }
     public Boolean lookupBoolean(Map<String, Object> args, String field) {
         String value = (String) args.get(field);
         return Boolean.parseBoolean(value);
     }
     public String lookupPassword(Map<String, Object> args, String field, int minLength) {
         String password = lookupString(args,field);
         if (hasError(field)) return password;
         return validatePassword(field,password,minLength) ? password : null;
     }
     
     public String lookupString(JSONObject json, String field) {
         try {
             return json.getString(field);
         } catch (JSONException e) {
             addError(ERR_NULL, field);
             return null;
         }
     }
     
     public Long lookupLong(JSONObject json, String field) {
         try {
             Long value = json.getLong(field);
             return value;
         } catch ( NullPointerException e ) {
             addError(ERR_NULL, field);
             return null;
         } catch ( JSONException e ) {
             addError(ERR_FORMAT, field);
             return null;
         }
     }
 }
 /* CHECKSTYLE:ON */

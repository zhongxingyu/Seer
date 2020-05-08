 package com.mobeelizer.java.errors;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.mobeelizer.java.api.MobeelizerOperationError;
 
 public class MobeelizerOperationErrorImpl implements MobeelizerOperationError {
 
     private final String code;
 
     private final String message;
 
     private final List<Object> arguments;
 
     private MobeelizerOperationErrorImpl(final String code, final String message, final List<Object> arguments) {
         this.code = code;
         this.message = message;
         if (arguments == null) {
             this.arguments = new ArrayList<Object>();
         } else {
             this.arguments = arguments;
         }
     }
 
    @Override
     public String getCode() {
         return code;
     }
 
    @Override
     public String getMessage() {
         return message;
     }
 
    @Override
     public List<Object> getArguments() {
         return arguments;
     }
 
     @Override
     public String toString() {
         return "MobeelizerOperationError: " + message;
     }
 
     public static MobeelizerOperationError sendFileCreationError() {
         return new MobeelizerOperationErrorImpl("other", "Send file haven't been created.", null);
     }
 
     public static MobeelizerOperationError notLoggedError() {
         return new MobeelizerOperationErrorImpl("notLoggedIn", "User is not logged in", null);
     }
 
     public static MobeelizerOperationError inputFileError() {
         return new MobeelizerOperationErrorImpl("other", "Error while processing synchronization file", null);
     }
 
     public static MobeelizerOperationError missingConnectionError() {
         return new MobeelizerOperationErrorImpl("missingConnection", "Internet connection required", null);
     }
 
     public static MobeelizerOperationError authenticationFailure() {
         return new MobeelizerOperationErrorImpl("authenticationFailure", "Authentication failure", null);
     }
 
     public static MobeelizerOperationError syncRejected(final String result, final String message) {
         List<Object> args = new ArrayList<Object>();
         args.add(result);
         args.add(message);
         return new MobeelizerOperationErrorImpl("syncRejected", "Synchronization rejected: result: " + result + ", message: "
                 + message, args);
     }
 
     public static MobeelizerOperationError connectionError() {
         return new MobeelizerOperationErrorImpl("connectionFailure", "Connection failure", null);
     }
 
     public static MobeelizerOperationError connectionError(final int status) {
         List<Object> args = new ArrayList<Object>();
         args.add(status);
         return new MobeelizerOperationErrorImpl("connectionFailure", "Connection failure: " + status, args);
     }
 
     public static MobeelizerOperationError serverError(final JSONObject json) {
         try {
            List<Object> args = null;
            if (json.has("arguments") && !json.isNull("arguments")) {
                args = new ArrayList<Object>();
                JSONArray jsonArgs = json.getJSONArray("arguments");
                for (int i = 0; i < jsonArgs.length(); i++) {
                    args.add(jsonArgs.getString(i));
                }
             }
             return new MobeelizerOperationErrorImpl(json.getString("code"), json.getString("message"), args);
         } catch (JSONException e) {
             return connectionError();
         }
     }
 
     public static MobeelizerOperationError other(final String message) {
         return new MobeelizerOperationErrorImpl("other", message, null);
     }
 
     public static MobeelizerOperationError exception(final Exception e) {
         if (e.getMessage() != null && !e.getMessage().trim().equals("")) {
             return other(e.getMessage());
         }
         return other(e.getClass().getCanonicalName());
     }
 }

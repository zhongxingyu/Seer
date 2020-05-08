 package com.agatteclient;
 
 import java.util.Collection;
 
 /**
  * Created by remi on 06/10/13.
  */
 public class AgatteResponse {
 
 
     public Code getCode() {
         return code;
     }
 
     public enum Code {
         IOError(true),//IOError happened
         LoginFailed(true),//Login into server failed
         NetworkNotAuthorized(true),//the server refused to give the data because the network is not authorized
         TemporaryOK(false),//To be used in intermediate responses : so far, transaction went OK
         QueryOK(false),//Query returned a valid result
         PunchOK(false),//Punch action returned a valid result
         UnknownError(true);
 
         private boolean isErr;
 
         Code(boolean err) {
             isErr = err;
         }
 
         public boolean isError() {
             //true for IOError, login failed, NetworkNotauthorized
             return isErr;
         }
 
         public boolean hasTops() {
             //true for 'OK' types
             return !isErr;
         }
     }
 
     private Code code;
     private String detail;
     private String[] tops;
 
     public AgatteResponse(Code code) {
         this.code = code;
         this.detail = null;
     }
 
     public AgatteResponse(Code code, String[] tops) {
         this(code);
         this.tops = tops;
     }
 
     public AgatteResponse(Code code, Collection<String> tops) {
         this(code);
         this.tops = tops.toArray(new String[tops.size()]);
     }
 
     public AgatteResponse(Code code, Exception cause) {
         this(code);
        this.detail = cause.getCause().getLocalizedMessage();
     }
 
     public AgatteResponse(Code code, String s) {
         this(code);
         this.detail = s;
     }
 
     public String[] getTops() {
         return tops;
     }
 
     public String getDetail() {
         return detail;
     }
 
     public boolean isError() {
         //true for IOError, login failed, NetworkNotauthorized
         return code.isError();
     }
 
     public boolean hasTops() {
         //true for 'OK' types
         return code.hasTops();
     }
 
     public boolean hasDetail() {
         //true if isError
         return (detail != null);
     }
 
 }

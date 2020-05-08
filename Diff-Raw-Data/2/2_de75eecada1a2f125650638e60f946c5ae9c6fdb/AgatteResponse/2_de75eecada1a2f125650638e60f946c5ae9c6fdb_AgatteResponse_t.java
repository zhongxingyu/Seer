 /*This file is part of AgatteClient.
 
     AgatteClient is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     AgatteClient is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with AgatteClient.  If not, see <http://www.gnu.org/licenses/>.*/
 
 package com.agatteclient;
 
 import java.util.Collection;
 
 /**
  * Created by remi on 06/10/13.
  */
 public class AgatteResponse {
 
 
     private Code code;
     private String detail;
     private String[] tops;
     private String[] virtual_tops;
     public AgatteResponse(Code code) {
         this.code = code;
         this.detail = null;
 
     }
     public AgatteResponse(Code code, String[] tops) {
         this(code);
         this.tops = tops;
         this.virtual_tops = new String[0];
     }
 
     public AgatteResponse(Code code, Collection<String> tops) {
         this(code);
         this.tops = tops.toArray(new String[tops.size()]);
     }
 
     public AgatteResponse(Code code, Collection<String> tops, Collection<String> virtual_tops) {
         this(code, tops);
         this.virtual_tops = virtual_tops.toArray(new String[tops.size()]);
     }
 
     public AgatteResponse(Code code, Exception cause) {
         this(code);
         this.virtual_tops = new String[0];
         if (cause.getCause() != null) {
             this.detail = cause.getCause().getLocalizedMessage();
         } else {
             this.detail = cause.getLocalizedMessage();
         }
     }
 
     public AgatteResponse(Code code, String s) {
         this(code);
         this.virtual_tops = new String[0];
         this.detail = s;
     }
 
     public Code getCode() {
         return code;
     }
 
     public String[] getTops() {
         return tops;
     }
 
     public String[] getVirtualTops() {
         return virtual_tops;
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
 
     public boolean hasVirtualTops() {
         //true for 'OK' types
        return (virtual_tops != null && virtual_tops.length != 0);
     }
 
     public boolean hasDetail() {
         //true if isError
         return (detail != null);
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
 
 }

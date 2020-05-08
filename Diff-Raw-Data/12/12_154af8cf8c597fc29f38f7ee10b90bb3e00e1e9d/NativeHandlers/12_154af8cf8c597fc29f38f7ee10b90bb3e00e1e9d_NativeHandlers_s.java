 /*
  * Copyright 2013 Simon Taddiken
  *
  * This file is part of Polly HTTP API.
  *
  * Polly HTTP API is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU General Public License as published by 
  * the Free Software Foundation, either version 3 of the License, or (at 
  * your option) any later version.
  *
  * Polly HTTP API is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
  * more details.
  *
  * You should have received a copy of the GNU General Public License along 
  * with Polly HTTP API. If not, see http://www.gnu.org/licenses/.
  */
 package de.skuzzle.polly.http.internal;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import de.skuzzle.polly.http.api.HttpException;
 import de.skuzzle.polly.http.api.ParameterHandler;
 
 
 class NativeHandlers {
     
     public final static ParameterHandler BOOLEAN = new ParameterHandler() {
         
         @Override
         public Object parse(String value) throws HttpException {
             if (value == null) {
                 return false;
             } else {
                 final String lc = value.toLowerCase();
                 if (lc.equals("true") || lc.equals("yes") || lc.equals("on")) {
                     return true;
                 } else if (lc.equals("false") || lc.equals("no") || lc.equals("off")) {
                     return false;
                 }
                 throw new IllegalArgumentException("invalid boolean: " + value);
             }
         }
         
        
        
         @Override
         public boolean canHandle(Class<?> type, Class<?> typeVar) {
            return Boolean.class.isAssignableFrom(type);
         }
     };
     
     
 
     public final static ParameterHandler STRING = new ParameterHandler() {
         @Override
         public Object parse(String value) {
             return value;
         }
         
         @Override
         public boolean canHandle(Class<?> type, Class<?> typeVar) {
             return String.class.isAssignableFrom(type);
         }
     };
     
     
     
     public final static ParameterHandler INTEGER = new ParameterHandler() {
         
         @Override
         public Object parse(String value) throws HttpException {
             try {
                 return Integer.parseInt(value);
             } catch (NumberFormatException e) {
                 throw new HttpException(e);
             }
         }
         
         @Override
         public boolean canHandle(Class<?> type, Class<?> typeVar) {
             return Integer.class.isAssignableFrom(type) || 
                 int.class.isAssignableFrom(type);
         }
     };
     
     
     
     public final static ParameterHandler STRING_LIST = new ParameterHandler() {
         
         @Override
         public Object parse(String value) throws HttpException {
             if (value.equals("")) {
                 return new ArrayList<String>();
             }
             return new ArrayList<String>(Arrays.asList(value.split(";")));
         }
         
        
        
         @Override
         public boolean canHandle(Class<?> type, Class<?> typeVar) {
             return List.class.isAssignableFrom(type) && typeVar != null && 
                 String.class.isAssignableFrom(typeVar);
         }
     };
     
     
     
     public final static ParameterHandler INT_LIST = new ParameterHandler() {
         
         @Override
         public Object parse(String value) throws HttpException {
             if (value.equals("")) {
                 return new ArrayList<Integer>();
             }
             final String[] parts = value.split(";");
             final List<Integer> result = new ArrayList<Integer>();
             for (final String p : parts) {
                 try {
                     result.add(Integer.parseInt(p));
                 } catch (NumberFormatException e) {
                     throw new HttpException(e);
                 }
             }
             return result;
         }
         
        
        
         @Override
         public boolean canHandle(Class<?> type, Class<?> typeVar) {
             return List.class.isAssignableFrom(type) && typeVar != null && 
                 Integer.class.isAssignableFrom(typeVar);
         }
     };
 }

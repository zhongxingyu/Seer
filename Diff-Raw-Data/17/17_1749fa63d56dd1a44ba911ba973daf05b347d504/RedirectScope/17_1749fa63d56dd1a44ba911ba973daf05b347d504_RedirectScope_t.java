 /*
  * Copyright 2004-2008 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package javax.faces.internal.scope;
 
 import java.util.Map;
 
 import javax.faces.FacesException;
 import javax.faces.context.FacesContext;
 
 /**
  * @author higa
  *
  */
 public abstract class RedirectScope {
 
     private static final String REDIRECTING_KEY = RedirectScope.class.getName();
 
     private static final String REDIRECTED_KEY = RedirectScope.class.getName() +
             ".REDIRECTED";
 
     private static final VariableScope scope = new VariableScope(
             REDIRECTING_KEY);
 
     protected RedirectScope() {
     }
 
     public static Map getContext(FacesContext context) throws FacesException {
         return scope.getContext(context);
     }
 
     public static Map getOrCreateContext(FacesContext context)
             throws FacesException {
         return scope.getOrCreateContext(context);
     }
 
     public static void removeContext(FacesContext context)
             throws FacesException {
         scope.removeContext(context);
     }
 
     public static void clearContext(FacesContext context) throws FacesException {
         scope.clearContext(context);
     }
 
     public static boolean isRedirecting(FacesContext context)
             throws FacesException {
         Map ctx = scope.getContext(context);
         if (ctx == null) {
             return false;
         }
         return ctx.containsKey(REDIRECTING_KEY);
     }
 
     public static void setRedirectingPath(FacesContext context, String path)
             throws FacesException {
         Map ctx = getOrCreateContext(context);
         ctx.put(REDIRECTING_KEY, path);
     }
 
     public static String getRedirectingPath(FacesContext context)
             throws FacesException {
         Map ctx = scope.getContext(context);
         if (ctx == null) {
             return null;
         }
         return (String) ctx.get(REDIRECTING_KEY);
     }
 
     public static void setRedirectedPath(FacesContext context, String path)
             throws FacesException {
         Map ctx = getOrCreateContext(context);
         ctx.put(REDIRECTED_KEY, path);
     }
 
     public static String getRedirectedPath(FacesContext context)
             throws FacesException {
         Map ctx = scope.getContext(context);
         if (ctx == null) {
             return null;
         }
         return (String) ctx.get(REDIRECTED_KEY);
     }
 }

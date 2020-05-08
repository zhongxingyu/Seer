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
  * @author shot
  */
 public abstract class PageScope {
 
     private static final VariableScope scope = new VariableScope(
             PageScope.class.getName());
 
     protected PageScope() {
     }
 
     public static Map getOrCreateContext(FacesContext context)
             throws FacesException {
         return scope.getOrCreateContext(context);
     }
 
     public static Map getContext(FacesContext context) throws FacesException {
         return scope.getContext(context);
     }
 
     public static void removeContext(FacesContext context)
             throws FacesException {
        scope.removeContext(context);
     }
 
     public static void removeContext(FacesContext context, String wid)
             throws FacesException {
        scope.removeContext(context, wid);
     }
 }

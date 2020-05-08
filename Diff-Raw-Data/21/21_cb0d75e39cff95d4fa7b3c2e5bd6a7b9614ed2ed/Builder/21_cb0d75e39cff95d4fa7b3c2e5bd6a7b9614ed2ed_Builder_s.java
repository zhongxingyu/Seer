 /*
  * Copyright (C) 2012 John Pritchard, Gap Data
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * Note: this file incorporates source code from 3d party entities. Such code 
  * is copyrighted by those entities as indicated below.
  */
 package json;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 /**
  * <h3>Methods</h3>
  * 
  * <p> An implementor returns JSON class instance from {@link #toJson
  * toJson} method, and may throw an unsupported operation exception
  * from the {@link #fromJson fromJson} method. </p>
  * 
  * <h3>Builder Constructor</h3>
  * 
  * <p> An implementor shall define a public constructor with one Json
  * parameter for construction within Builder protocol. </p>
  * 
  * <h3>Simple Constructor</h3>
  * 
  * <p> An implementor may define a public constructor with no
  * arguments for the external application of Builder protocol.  This
  * permits application specific initialization to occur before
  * application defined building from JSON. </p>
  * 
  * @author John Pritchard
  */		
 public interface Builder
 {
     /**
      * Exception thrown by {@link Builder#fromJson} to indicate the
      * use of a constructor.
      */
     public static class Immutable
         extends UnsupportedOperationException
     {
         public Immutable(){
             super();
         }
         /**
          * Builder constructor protocol
          */
        public final static <T extends Builder> T Construct(Class<T> clas, Json model){
             try {
                 /*
                  * Option permitting a JSON Object to specify a java
                  * class binding via a field named "class" or
                  * "class-java".
                  * 
                  * The java class binding is a subclass of the
                  * argument class.
                  * 
                  * The application has called 
                  * 
                  *    json.getValue("name",Some.class)
                  * 
                  */
                 String opt = model.getValue("class");
                 boolean second = true;
 
                 if (null == opt){
                     second = false;
                     opt = model.getValue("class-java");
                 }
 
                 while (null != opt){
                     try {
                         Class opc = Class.forName(opt);
 
                         if (clas.isAssignableFrom(opc)){
 
                             clas = (Class<T>)opc;
                         }
                         break;
                     }
                     catch (Throwable ignore){
                         //
                         if (second){
                             second = false;
 
                             opt = model.getValue("class-java");
                         }
                         else
                             break;
                     }
                 }
             }
             catch (RuntimeException ignore){
                 //
             }
             try {
                 /*
                  * Return a new instance constructed on Builder protocol
                  */
                 Constructor<T> ctor = clas.getConstructor(Json.class);
 
                 return ctor.newInstance(model);
             }
             catch (NoSuchMethodException exc){
                 /*
                  * Return a new instance without applying Builder protocol
                  */
                 try {
                     Constructor<T> ctor = clas.getConstructor();
 
                     return ctor.newInstance();
                 }
                 catch (NoSuchMethodException x){
 
                     throw new IllegalStateException(clas.getName(),x);
                 }
                 catch (SecurityException x){
 
                     throw new IllegalStateException(clas.getName(),x);
                 }
                 catch (InstantiationException x){
 
                     throw new IllegalStateException(clas.getName(),x);
                 }
                 catch (IllegalAccessException x){
 
                     throw new IllegalStateException(clas.getName(),x);
                 }
                 catch (IllegalArgumentException x){
 
                     throw new IllegalStateException(clas.getName(),x);
                 }
                 catch (InvocationTargetException x){
 
                     Throwable target = x.getCause();
                     if (null != target)
                         throw new IllegalStateException(clas.getName(),target);
                     else
                         throw new IllegalStateException(clas.getName(),x);
                 }
             }
             catch (SecurityException exc){
 
                 throw new IllegalStateException(clas.getName(),exc);
             }
             catch (InstantiationException exc){
 
                 throw new IllegalStateException(clas.getName(),exc);
             }
             catch (IllegalAccessException exc){
 
                 throw new IllegalStateException(clas.getName(),exc);
             }
             catch (IllegalArgumentException exc){
 
                 throw new IllegalStateException(clas.getName(),exc);
             }
             catch (InvocationTargetException exc){
 
                 Throwable target = exc.getCause();
                 if (null != target)
                     throw new IllegalStateException(clas.getName(),target);
                 else
                     throw new IllegalStateException(clas.getName(),exc);
             }
         }
        public final static <T> T Construct(Class<T> clas, String model){
             if (java.lang.Enum.class.isAssignableFrom(clas)){
                 try {
                     Method valueOf = clas.getMethod("valueOf",String.class);
 
                     return (T)valueOf.invoke(null,model);
                 }
                 catch (NoSuchMethodException exc){
 
                     throw new IllegalStateException(clas.getName(),exc);
                 }
                 catch (SecurityException exc){
 
                     throw new IllegalStateException(clas.getName(),exc);
                 }
                 catch (IllegalAccessException exc){
 
                     throw new IllegalStateException(clas.getName(),exc);
                 }
                 catch (IllegalArgumentException exc){
 
                     throw new IllegalStateException(clas.getName(),exc);
                 }
                 catch (InvocationTargetException exc){
 
                     Throwable target = exc.getCause();
                     if (null != target)
                         throw new IllegalStateException(clas.getName(),target);
                     else
                         throw new IllegalStateException(clas.getName(),exc);
                 }
             }
             else {
                 try {
                     Constructor<T> ctor = clas.getConstructor(String.class);
 
                     return ctor.newInstance(model);
                 }
                 catch (NoSuchMethodException exc){
 
                     throw new IllegalStateException(clas.getName(),exc);
                 }
                 catch (SecurityException exc){
 
                     throw new IllegalStateException(clas.getName(),exc);
                 }
                 catch (InstantiationException exc){
 
                     throw new IllegalStateException(clas.getName(),exc);
                 }
                 catch (IllegalAccessException exc){
 
                     throw new IllegalStateException(clas.getName(),exc);
                 }
                 catch (IllegalArgumentException exc){
 
                     throw new IllegalStateException(clas.getName(),exc);
                 }
                 catch (InvocationTargetException exc){
 
                     Throwable target = exc.getCause();
                     if (null != target)
                         throw new IllegalStateException(clas.getName(),target);
                     else
                         throw new IllegalStateException(clas.getName(),exc);
                 }
             }
         }
        public final static <T> T Construct(Class<T> clas, Object model){
             try {
                 Constructor<T> ctor = clas.getConstructor(model.getClass());
 
                 return ctor.newInstance(model);
             }
             catch (NoSuchMethodException exc){
 
                 throw new IllegalStateException(clas.getName(),exc);
             }
             catch (SecurityException exc){
 
                 throw new IllegalStateException(clas.getName(),exc);
             }
             catch (InstantiationException exc){
 
                 throw new IllegalStateException(clas.getName(),exc);
             }
             catch (IllegalAccessException exc){
 
                 throw new IllegalStateException(clas.getName(),exc);
             }
             catch (IllegalArgumentException exc){
 
                 throw new IllegalStateException(clas.getName(),exc);
             }
             catch (InvocationTargetException exc){
 
                 Throwable target = exc.getCause();
                 if (null != target)
                     throw new IllegalStateException(clas.getName(),target);
                 else
                     throw new IllegalStateException(clas.getName(),exc);
             }
         }
     }
 
 
     /**
      * @return Model of this
      */
     public Json toJson();
     /**
      * @param thisModel Model of this
      * @return Modified
      * @throws Immutable To indicate the use of a constructor
      */
     public boolean fromJson(Json thisModel);
 }

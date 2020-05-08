 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package cx.ath.mancel01.dependencyshot.injection.handlers;
 
 import cx.ath.mancel01.dependencyshot.api.annotations.ManagedBean;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 /**
  *
  * @author Mathieu ANCELIN
  */
 public final class ManagedBeanHandler {
 
     private ManagedBeanHandler() {}
 
     public static boolean isManagedBean(Object o) {
         return isManagedBean(o.getClass());
     }
 
     public static boolean isManagedBean(Class clazz) {
         return clazz.isAnnotationPresent(ManagedBean.class);
     }
 
     public static void registerManagedBeanJNDI(Object instance) {
 //        try {
 //            Class clazz = instance.getClass();
 //            if (clazz.isAnnotationPresent(ManagedBean.class)) {
 //                ManagedBean annotation = (ManagedBean) clazz.getAnnotation(ManagedBean.class);
 //                if (!annotation.value().equals("")) {
 //                    Hashtable<Object, Object> env = new Hashtable<Object, Object>();
 //                    env.put(Context.INITIAL_CONTEXT_FACTORY,
 //                            "com.sun.jndi.fscontext.RefFSContextFactory");
 //                    env.put(Context.PROVIDER_URL, "file:/");
 //                    Context context = new javax.naming.InitialContext(env);
 //                    context.bind(annotation.value(), instance);
 //                }
 //            }
 //        } catch (Exception ex) {
 //            Logger.getLogger(ManagedBeanHandler.class.getName()).log(Level.SEVERE, null, ex);
 //        }
     }
 }

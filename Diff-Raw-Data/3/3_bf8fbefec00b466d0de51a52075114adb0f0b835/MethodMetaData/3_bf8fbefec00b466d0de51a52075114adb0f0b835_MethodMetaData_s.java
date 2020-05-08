 /*
  * Copyright (c) 2010 Carman Consulting, Inc.
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
  */
 
 package org.metastopheles;
 
 import java.beans.MethodDescriptor;
 import java.io.Serializable;
 import java.lang.reflect.AnnotatedElement;
 
 /**
  * @author James Carman
  * @since 1.0
  */
 public class MethodMetaData extends MetaDataObject
 {
 //**********************************************************************************************************************
 // Fields
 //**********************************************************************************************************************
 
     private static final long serialVersionUID = 1L;
     private final MethodDescriptor methodDescriptor;
     private final BeanMetaData beanMetaData;
 
 //**********************************************************************************************************************
 // Constructors
 //**********************************************************************************************************************
 
     MethodMetaData(BeanMetaData beanMetaData, MethodDescriptor methodDescriptor)
     {
         this.beanMetaData = beanMetaData;
         this.methodDescriptor = methodDescriptor;
     }
 
 //**********************************************************************************************************************
 // Getter/Setter Methods
 //**********************************************************************************************************************
 
     public BeanMetaData getBeanMetaData()
     {
         return beanMetaData;
     }
 
     public MethodDescriptor getMethodDescriptor()
     {
         return methodDescriptor;
     }
 
 //**********************************************************************************************************************
 // Other Methods
 //**********************************************************************************************************************
 
     @Override
     protected AnnotatedElement getDefaultAnnotationSource()
     {
         return methodDescriptor.getMethod();
     }
 
     protected Object writeReplace()
     {
         return new SerializedForm(beanMetaData, methodDescriptor.getMethod().getName(), getMethodDescriptor().getMethod().getParameterTypes());
     }
 
 //**********************************************************************************************************************
 // Inner Classes
 //**********************************************************************************************************************
 
     private static class SerializedForm implements Serializable
     {
         private static final long serialVersionUID = 1L;
         private final BeanMetaData beanMetaData;
         private final String methodName;
         private final Class<?>[] parameterTypes;
 
 
         private SerializedForm(BeanMetaData beanMetaData, String methodName, Class<?>[] parameterTypes)
         {
             this.beanMetaData = beanMetaData;
             this.methodName = methodName;
            this.parameterTypes = parameterTypes;
         }
 
         protected Object readResolve()
         {
             return beanMetaData.getMethodMetaData(methodName, parameterTypes);
         }
     }
 }

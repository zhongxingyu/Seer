 package com.caeldev.resources.util;
 
 import com.caeldev.services.ServiceException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import javax.ws.rs.core.EntityTag;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 /**
  * Copyright (c) 2012 - 2013 Caeldev, Inc.
  * <p/>
  * User: cael
  * Date: 07/11/2013
  * Time: 10:46
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 @Component
 @Scope("prototype")
 public class ETagBuilderImpl<T extends Serializable> implements ETagBuilder<T> {
 
     private T entity;

     @Autowired
     private HashCodeGenerator hashCodeGenerator;
 
     @Override
     public EntityTag build() throws ServiceException {
         byte[] entityAsByteArray = getEntityAsByteArray();
         String hashMD5 = hashCodeGenerator.generateFrom(entityAsByteArray);
         return new EntityTag(hashMD5);
     }
 
     private byte[] getEntityAsByteArray() throws ServiceException {
         final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         try {
             ObjectOutputStream oos = new ObjectOutputStream(outputStream);
             oos.writeObject(entity);
             oos.close();
         } catch (IOException e) {
             throw new ServiceException(e);
         }
         return outputStream.toByteArray();
     }
 
     public void setHashCodeGenerator(HashCodeGenerator hashCodeGenerator) {
         this.hashCodeGenerator = hashCodeGenerator;
     }
 
     public ETagBuilder<T> withEntity(T entity) {
         this.entity = entity;
         return this;
     }
 }

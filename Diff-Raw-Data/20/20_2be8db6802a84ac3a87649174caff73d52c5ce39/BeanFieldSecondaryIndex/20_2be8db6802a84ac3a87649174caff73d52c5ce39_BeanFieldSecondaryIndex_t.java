 /*
  * Copyright 2010 Kodapan
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package se.kodapan.entitystore;
 
 import se.kodapan.collections.SetMap;
 import se.kodapan.lang.reflect.ReflectionUtil;
 
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 /**
  * @author kalle
  * @since 2010-jul-11 17:46:37
  */
 public class BeanFieldSecondaryIndex<PrimaryIndexIdentityType, EntityType, AttributeType> extends SimpleMapSetSecondaryIndex<PrimaryIndexIdentityType, EntityType> {
 
   private Class<AttributeType> fieldType;
 
   private String fieldName;
   private transient Method getter;
 
   public BeanFieldSecondaryIndex() {
   }
 
 
   public BeanFieldSecondaryIndex(String name, PrimaryIndex<PrimaryIndexIdentityType, EntityType> primaryIndex, Class<AttributeType> fieldType, String fieldName) {
     super(name, primaryIndex);
     this.fieldType = fieldType;
     this.fieldName = fieldName;
   }
 
   public BeanFieldSecondaryIndex(String name, PrimaryIndex<PrimaryIndexIdentityType, EntityType> primaryIndex, Class<AttributeType> fieldType, String fieldName, SetMap<Object, EntityType> map) {
     super(name, primaryIndex, map);
     this.fieldType = fieldType;
     this.fieldName = fieldName;
   }
 
   public Method getGetter() throws NoSuchFieldException, NoSuchMethodException {
     if (getter == null) {
       getter = ReflectionUtil.getGetter(getPrimaryIndex().getEntityType(), fieldName);
     }
     return getter;
   }
 
   @Override
   public Object getSecondaryKey(EntityType entity) {
     try {
       return getGetter().invoke(entity);
     } catch (IllegalAccessException e) {
       throw new RuntimeException(e);
     } catch (InvocationTargetException e) {
       throw new RuntimeException(e);
     } catch (NoSuchMethodException e) {
       throw new RuntimeException(e);
     } catch (NoSuchFieldException e) {
       throw new RuntimeException(e);
     }
   }
 
   @Override
   public Object getSecondaryKey(Object... parameters) {
     if (parameters.length != 1 || !fieldType.equals(parameters[0].getClass()) ) {
       throw new RuntimeException("Expected a single parameter of type " + fieldType.getName());
     }
     return parameters[0];
   }
   
 }

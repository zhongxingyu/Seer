 /*
  * Copyright open knowledge GmbH
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
 
 package de.openknowledge.util.dge.filter;
 
 import java.lang.reflect.Method;
 import java.math.BigDecimal;
 import java.util.Date;
 
 /**
  * Manages the meta data for a FilterField. Most of the meta data should normally be retrieved from a method which is annotated with {@link
  * de.openknowledge.util.dge.filter.annotation.FilterField}.
  *
  * @author Marc Petersen - open knowledge GmbH
  */
 public class FilterFieldMetaData {
 
   private int order;
   private Method targetMethod;
   private FilterFieldType type;
   private String displayName;
 
   /**
    * Allocates a <code>FilterFieldMetaData</code> object and initializes it. Most of the parameters should be retrieved over a {@link
    * de.openknowledge.util.dge.filter.annotation.FilterField} annotation.
    *
    * @param aType         The chosen {@link FilterFieldType}.
   * @param aTargetMethod A {@link java.lang.reflect.Method} which should normally be retrieved through the
   *                      {@link de.openknowledge.util.dge.filter.annotation.FilterField} annotation.
   * @param aOrder        The order number which should normally be defined by the responding
   *                      {@link de.openknowledge.util.dge.filter.annotation.FilterField} annotation.
   * @param aDisplayName  A displayable name which should normally be defined by the responding
   *                      {@link de.openknowledge.util.dge.filter.annotation.FilterField} annotation.
    */
   public FilterFieldMetaData(FilterFieldType aType, Method aTargetMethod, int aOrder, String aDisplayName) {
     targetMethod = aTargetMethod;
     type = aType;
     order = aOrder;
     displayName = aDisplayName;
   }
 
   /**
    * @param aFilterOperand
    * @param o
   * @return The assembled FilterExpression.
   * @deprecated Seems to be useless as it is only used by tests.
    */
   public FilterExpression createFilterExpression(FilterOperand aFilterOperand, Object o) {
     switch (type) {
       case BIGDECIMAL:
        return new FilterExpression<BigDecimal>(this, aFilterOperand, (BigDecimal) o);
       case DATE:
        return new FilterExpression<Date>(this, aFilterOperand, (Date) o);
       case SELECT:
        return new FilterExpression(this, aFilterOperand, (Comparable) o);
       default:
         throw new IllegalArgumentException("FilterFieldType " + type + " not supported");
     }
   }
 
   /**
    * Uses reflection to call the defined <code>targetMethod</code> for the given <code>targetObject</code> and return its return value.
    *
    * @param targetObject The object which contains the <code>targetMethod</code>.
    * @return The return value of the <code>targetMethod</code>.
    */
   public Object getValue(Object targetObject) {
     try {
       return targetMethod.invoke(targetObject);
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 
   public Method getTargetMethod() {
     return targetMethod;
   }
 
   public FilterFieldType getType() {
     return type;
   }
 
   public int getOrder() {
     return order;
   }
 
   public String getDisplayName() {
     return displayName;
   }
 }

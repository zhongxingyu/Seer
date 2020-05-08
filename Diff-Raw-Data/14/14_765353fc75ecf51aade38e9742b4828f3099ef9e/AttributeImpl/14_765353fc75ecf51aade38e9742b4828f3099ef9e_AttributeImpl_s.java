 package com.huskycode.jpaquery.link;
 
 import java.lang.reflect.Field;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Ta
  * Date: 3/26/12
  * Time: 8:37 PM
  * To change this template use File | Settings | File Templates.
  */
 public class AttributeImpl<E, T> implements Attribute<E, T> {
     
     private Class<E> cls;
     private Field field;
     
     private AttributeImpl(final Class<E> c, final Field field){
         this.cls = c;
         this.field = field;
     }
     
     public static <E, T> AttributeImpl<E, T> newInstance(Class<E> c, Field field) {
        return new AttributeImpl<E, T>(c, field);
     } 
     
     
     @Override
     public Class<E> getEntityClass() {
         return this.cls;
     }
 
     @Override
     public Field getField() {
         return this.field;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
        AttributeImpl attribute = (AttributeImpl) o;
 
         if (!cls.equals(attribute.cls)) return false;
         if (!field.equals(attribute.field)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = cls.hashCode();
         result = 31 * result + field.hashCode();
         return result;
     }
 }

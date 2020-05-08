 package org.codehaus.xfire.aegis.type;
 
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Method;
 
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.aegis.type.basic.BeanType;
 
 public class DefaultTypeCreator extends AbstractTypeCreator
 {
     public TypeClassInfo createClassInfo(Method m, int index)
     {
         TypeClassInfo info = new TypeClassInfo();
 
         if(index >= 0) info.setTypeClass(m.getParameterTypes()[index]);
         else info.setTypeClass(m.getReturnType());
 
         return info;
     }
 
     public TypeClassInfo createClassInfo(PropertyDescriptor pd)
     {
         return createBasicClassInfo(pd.getPropertyType());
     }
 
     public Type createCollectionType(TypeClassInfo info)
     {
         if(info.getGenericType() == null)
         {
             throw new XFireRuntimeException("Cannot create mapping for " + 
                                             info.getTypeClass().getName() + 
                                            ", unspecified component type");
         }
 
         return createCollectionType(info, (Class)info.getGenericType());
     }
 
     public Type createDefaultType(TypeClassInfo info)
     {
         BeanType type = new BeanType();
         type.setSchemaType(createQName(info.getTypeClass()));
         type.setTypeClass(info.getTypeClass());
         type.setTypeMapping(getTypeMapping());
 
         return type;
     }
 }

 package org.codehaus.xfire.aegis.type;
 
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Collection;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.aegis.type.basic.ArrayType;
 import org.codehaus.xfire.aegis.type.basic.BeanType;
 import org.codehaus.xfire.aegis.type.collection.CollectionType;
 import org.codehaus.xfire.util.NamespaceHelper;
 
 public class DefaultTypeCreator
     implements TypeCreator
 {
     private TypeMapping tm;
     
     public TypeMapping getTypeMapping()
     {
         return tm;
     }
 
     public void setTypeMapping(TypeMapping typeMapping)
     {
         this.tm = typeMapping;
     }
 
     /**
      * Create a Type for a Method parameter.
      * @param m the method to create a type for
      * @param index The parameter index. If the index is less than zero, the return type is used.
      */
     public Type createType(Method m, int index)
     {
         TypeClassInfo info = createClassInfo(m, index);
         
         return createTypeForClass(info);
     }
 
     protected TypeClassInfo createClassInfo(Method m, int index)
     {
         TypeClassInfo info = new TypeClassInfo();
         
         if (index >= 0) 
             info.setTypeClass(m.getParameterTypes()[index]);
         else
             info.setTypeClass(m.getReturnType());
 
         return info;
     }
 
     /**
      * Create type information for a PropertyDescriptor.
      * @param pd the propertydescriptor
      */
     public Type createType(PropertyDescriptor pd)
     {
         TypeClassInfo info = createClassInfo(pd);
         
         return createTypeForClass(info);
     }
 
     protected TypeClassInfo createClassInfo(PropertyDescriptor pd)
     {
         return createBasicClassInfo(pd.getPropertyType());
     }
 
     protected TypeClassInfo createBasicClassInfo(Class typeClass)
     {
         TypeClassInfo info = new TypeClassInfo();
         
         info.setTypeClass(typeClass);
 
         return info;
     }
 
     /**
      * Create type information for a <code>Field</code>.
      * @param f the field to create a type from
      */
     public Type createType(Field f)
     {
         TypeClassInfo info = createClassInfo(f);
         
         return createTypeForClass(info);
     }
 
     private TypeClassInfo createClassInfo(Field f)
     {
         return createBasicClassInfo(f.getType());
     }
 
     public Type createType(Class clazz)
     {
         TypeClassInfo info = createBasicClassInfo(clazz);
         
         return createTypeForClass(info);
     }
     
     protected Type createTypeForClass(TypeClassInfo info)
     {
         Class javaType = info.getTypeClass();
         if ( javaType.isArray() )
         {
             return createArrayType(info);
         }
         else if (isCollection(javaType))
         {
             return createCollectionType(info);
         }
         else if (isEnum(javaType))
         {
             return createEnumType(info);
         }
         else
         {
             return createDefaultType(info);
         }
     }
 
     protected boolean isCollection(Class javaType)
     {
        return Collection.class.isAssignableFrom(javaType);
     }
 
     protected boolean isEnum(Class javaType)
     {
         return false;
     }
 
     protected Type createEnumType(TypeClassInfo info)
     {
         return null;
     }
     
     protected Type createCollectionType(TypeClassInfo info)
     {
         return createCollectionType(info, String.class);
     }
 
     protected Type createCollectionType(TypeClassInfo info, Class component)
     {
         CollectionType type = new CollectionType(component);
         type.setSchemaType(createCollectionQName(info.getTypeClass(), component));
         type.setTypeClass(info.getTypeClass());
         
         return type;
     }
 
     protected Type createDefaultType(TypeClassInfo info)
     {
         BeanType type = new BeanType();
         type.setSchemaType(createQName(info.getTypeClass()));
         type.setTypeClass(info.getTypeClass());
         type.setTypeMapping(getTypeMapping());
         
         return type;
     }
 
     protected Type createArrayType(TypeClassInfo info)
     {
         ArrayType type = new ArrayType();
         type.setSchemaType(createArrayQName(info.getTypeClass()));
         type.setTypeClass(info.getTypeClass());
 
         return type;
     }
     
     protected QName createQName(Class javaType)
     {
         String clsName = javaType.getName();
         
         String ns = NamespaceHelper.makeNamespaceFromClassName(clsName, "http");
         String localName = clsName.substring( clsName.lastIndexOf(".")+1 );
         
         return new QName( ns, localName );
     }
 
     protected QName createCollectionQName(Class javaType, Class componentType)
     {
         // Forget about the [L which prefixes arrays. 
         String clsName = javaType.getName();
         
         if (clsName.startsWith("[L"))
         {
             clsName = clsName.substring(2, clsName.length() - 1);
         }
         
         Type type = tm.getType( componentType );
         String ns;
         
         if ( type.isComplex() )
         {
             ns = type.getSchemaType().getNamespaceURI();
         }
         else
         {
             ns = tm.getEncodingStyleURI();
         }
         
         String first = type.getSchemaType().getLocalPart().substring(0,1);
         String last = type.getSchemaType().getLocalPart().substring(1);
         String localName = "ArrayOf" + first.toUpperCase() + last;
         
         return new QName(ns, localName);
     }
     
     protected QName createArrayQName(Class javaType)
     {
         return createCollectionQName(javaType, javaType.getComponentType());
     }
 
     public static class TypeClassInfo
     {
         Class typeClass;
         Object[] annotations;
         Object genericType;
         
         public Object[] getAnnotations()
         {
             return annotations;
         }
         public void setAnnotations(Object[] annotations)
         {
             this.annotations = annotations;
         }
         public Object getGenericType()
         {
             return genericType;
         }
         public void setGenericType(Object genericType)
         {
             this.genericType = genericType;
         }
         public Class getTypeClass()
         {
             return typeClass;
         }
         public void setTypeClass(Class typeClass)
         {
             this.typeClass = typeClass;
         }
     }
     
 }

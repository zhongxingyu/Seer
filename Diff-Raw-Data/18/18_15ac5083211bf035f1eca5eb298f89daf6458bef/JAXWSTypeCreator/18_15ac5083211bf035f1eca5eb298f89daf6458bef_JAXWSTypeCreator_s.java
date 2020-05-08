 package org.codehaus.xfire.jaxws.type;
 
 import javax.xml.ws.Holder;
 
 import org.codehaus.xfire.aegis.type.DefaultTypeCreator;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.aegis.type.XMLTypeCreator;
 import org.codehaus.xfire.aegis.type.java5.Java5TypeCreator;
 import org.codehaus.xfire.jaxb2.JaxbType;
 import org.codehaus.xfire.jaxb2.JaxbTypeCreator;
 
 public class JAXWSTypeCreator extends Java5TypeCreator
 {
    public JAXWSTypeCreator()
     {
         super();
         
         XMLTypeCreator xc = new XMLTypeCreator();
        xc.setNextCreator(new DefaultTypeCreator());
         setNextCreator(xc);
     }
 
     @Override
     protected Type createTypeForClass(TypeClassInfo info)
     {
         Class clazz = info.getTypeClass();
         if (JaxbTypeCreator.isJaxbType(clazz))
         {
             JaxbType type = new JaxbType(clazz);
             type.getSchemaType();
             return type;
         }
         
         return super.createTypeForClass(info);
     }
 
     protected boolean isHolder(Class javaType)
     {
         return (javaType.equals(Holder.class) || super.isHolder(javaType));
     }
 
     protected Type createHolderType(TypeClassInfo info)
     {
         Class heldCls = getComponentType(info.getGenericType());
         
         info.setTypeClass(heldCls);
         Type delegate = createTypeForClass(info);
 
         HolderType type = new HolderType(delegate);
         return type;
     }
 }

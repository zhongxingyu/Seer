 package org.codehaus.xfire.aegis.type.basic;
 
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.aegis.MessageReader;
 import org.codehaus.xfire.aegis.MessageWriter;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.util.NamespaceHelper;
 import org.jdom.Attribute;
 import org.jdom.Element;
 
 /**
  * Serializes JavaBeans.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class BeanType
     extends Type
 {
     private BeanTypeInfo _info;
     
     public BeanType()
     {
         setNillable(true);
     }
     
     public BeanType(BeanTypeInfo info)
     {
         this._info = info;
         setNillable(true);
         
         this.setTypeClass(info.getTypeClass());
     }
 
     public Object readObject(MessageReader reader, MessageContext context)
         throws XFireFault
     {
         BeanTypeInfo info = getTypeInfo();
         
         try
         {
             Class clazz = getTypeClass();
             Object object = clazz.newInstance();
 
             // Read attributes
             while (reader.hasMoreAttributeReaders())
             {
                 MessageReader childReader = reader.getNextAttributeReader();
                 QName name = childReader.getName();
                 
                 Type type = info.getType(name.getLocalPart());
 
                 if (type != null)
                 {
                     Object writeObj = type.readObject(childReader, context);
     
                     writeProperty(name.getLocalPart(), object, writeObj);
                 }
             }
 
             // Read child elements
             while( reader.hasMoreElementReaders() )
             {
                 MessageReader childReader = reader.getNextElementReader();
                 QName name = childReader.getName();
                 
                 Type type = info.getType(name.getLocalPart());
 
                 if (type != null)
                 {
                     Object writeObj = type.readObject(childReader, context);
     
                     writeProperty(name.getLocalPart(), object, writeObj);
                 }
                 else
                 {
                     readToEnd(childReader);
                 }
             }
             
             return object;
         }
         catch (IllegalAccessException e)
         {
             throw new XFireFault("Illegal access. " + e.getMessage(), e, XFireFault.RECEIVER);
         }
 		catch (InstantiationException e)
 		{
             throw new XFireFault("Couldn't instantiate class. " + e.getMessage(), e, XFireFault.SENDER);
 		}
     }
 
     private void readToEnd(MessageReader childReader)
     {
         while (childReader.hasMoreElementReaders())
         {
             readToEnd(childReader.getNextElementReader());
         }
     }
 
     /**
      * Write the specified property to a field.
      */
     protected void writeProperty(String name, Object object, Object property)
         throws XFireFault
     {
         try
         {
             PropertyDescriptor desc = getTypeInfo().getPropertyDescriptorFromMappedName(name);
             
             Method m = desc.getWriteMethod();
             
            if (m == null) throw new XFireFault("No write method for property " + name, XFireFault.SENDER);
             
             m.invoke(object, new Object[] {property});
         }
         catch (Exception e)
         {
            throw new XFireFault("Couldn't set property " + name + ". " + e.getMessage(), e, XFireFault.SENDER);
         }
     }
 


     /**
     * @see org.codehaus.xfire.aegis.type.Type#writeObject(java.lang.Object)
      */
     public void writeObject(Object object, MessageWriter writer, MessageContext context)
         throws XFireFault
     {
         if (object == null)
             return;
         
         BeanTypeInfo info = getTypeInfo();
         
     	for (Iterator itr = info.getAttributes(); itr.hasNext(); )
         {
             String name = (String) itr.next();
 
             Object value = readProperty(object, name);
             if (value != null)
             {
                 Type type = getType( info, name );
                 
                 if ( type == null )
                     throw new XFireRuntimeException( "Couldn't find type for " + value.getClass() + " for property " + name );
     
                 MessageWriter cwriter;
                 
                 if (type.isAbstract())
                 {
                     cwriter = writer.getAttributeWriter(name, getSchemaType().getNamespaceURI());
                 }
                 else
                 {
                     cwriter = writer.getAttributeWriter(name, type.getSchemaType().getNamespaceURI());
                 }
 
                 type.writeObject(value, cwriter, context);
     
                 cwriter.close();
             }
         }
         
         for (Iterator itr = info.getElements(); itr.hasNext(); )
         {
             String name = (String) itr.next();
 
             Object value = readProperty(object, name);
             
             Type type = getType( info, name );
             MessageWriter cwriter;
             
             // Write the outer element
             if (type.isAbstract())
             {
                 cwriter = writer.getElementWriter(name, getSchemaType().getNamespaceURI());
             }
             else
             {
                 cwriter = writer.getElementWriter(name, type.getSchemaType().getNamespaceURI());
             }
 
             // Write the value if it is not null.
             if ( value != null)
             {
                 if ( type == null )
                     throw new XFireRuntimeException( "Couldn't find type for " + value.getClass() + " for property " + name );
 
                 type.writeObject(value, cwriter, context);
             }
             else if (info.isNillable(name))
             {
                 // Write the xsi:nil if it is null.
                 MessageWriter attWriter = cwriter.getAttributeWriter("nil", SoapConstants.XSI_NS);
                 attWriter.writeValue("true");
                 attWriter.close();
             }
             
             cwriter.close();
         }
     }
 
     protected Object readProperty(Object object, String name)
     {
         try
         {
             PropertyDescriptor desc = getTypeInfo().getPropertyDescriptorFromMappedName(name);
 
             Method m = desc.getReadMethod();
             
            if (m == null) throw new XFireFault("No write method for property " + name, XFireFault.SENDER);
 
             return m.invoke(object, new Object[0]);
         }
         catch (Exception e)
         {
            throw new XFireRuntimeException( "Couldn't get property " + name, e );
         }
     }
 
     /**
     * @see org.codehaus.xfire.aegis.type.Type#writeSchema()
      */
     public void writeSchema(Element root)
     {
         BeanTypeInfo info = getTypeInfo();
         
         Element complex = new Element("complexType",
                                       SoapConstants.XSD_PREFIX,
                                       SoapConstants.XSD);
         complex.setAttribute(new Attribute("name", getSchemaType().getLocalPart()));
         root.addContent(complex);
 
         Element seq = null;
         
         // Write out schema for elements
         for (Iterator itr = info.getElements(); itr.hasNext();)
         {
             if (seq == null)
             {
                 seq = new Element("sequence", SoapConstants.XSD_PREFIX, SoapConstants.XSD);
                 complex.addContent(seq);
             }
                             
             String name = (String) itr.next();
             
             Element element = new Element("element",
                                           SoapConstants.XSD_PREFIX,
                                           SoapConstants.XSD);
             seq.addContent(element);
             
             Type type = getType(info, name);
             
             String prefix = NamespaceHelper.getUniquePrefix((Element) root.getParent(), 
                                                             type.getSchemaType().getNamespaceURI() );
             
             writeTypeReference(name, element, type, prefix);
         }
         
         // Write out schema for attributes
         for (Iterator itr = info.getAttributes(); itr.hasNext();)
         {
             String name = (String) itr.next();
             
             Element element = new Element("attribute",
                                           SoapConstants.XSD_PREFIX,
                                           SoapConstants.XSD);
             complex.addContent(element);
             
             Type type = getType(info, name);
 
             String prefix = NamespaceHelper.getUniquePrefix((Element) root.getParent(), 
                                                             type.getSchemaType().getNamespaceURI() );
             
             element.setAttribute(new Attribute("name", name));
            element.setAttribute(new Attribute("type", prefix + ":" + type.getSchemaType().getLocalPart()));
         }
     }
 
     private Type getType(BeanTypeInfo info, String name)
     {
         Type type = info.getType(name);
         
         if (type == null)
         {
             throw new NullPointerException("Couldn't find type for" + name + " in class " + 
                                            getTypeClass().getName());
         }
         
         return type;
     }
 
     private void writeTypeReference(String name, Element element, Type type, String prefix)
     {
         if (type.isAbstract())
         {
             element.setAttribute(new Attribute("name", name));
            element.setAttribute(new Attribute("type", prefix + ":" + type.getSchemaType().getLocalPart()));
             
             if (getTypeInfo().isNillable(name))
             {
                 element.setAttribute(new Attribute("nillable", "true"));
             }
         }
         else
         {
            element.setAttribute(new Attribute("ref", prefix + ":" + type.getSchemaType().getLocalPart()));
         }
     }
     
     /**
      * We need to write a complex type schema for Beans, so return true.
      * 
      * @see org.codehaus.xfire.aegis.type.Type#isComplex()
      */
     public boolean isComplex()
     {
         return true;
     }
 
     public Set getDependencies()
     {
         Set deps = new HashSet();
 
         BeanTypeInfo info = getTypeInfo();
         
         for (Iterator itr = info.getAttributes(); itr.hasNext(); )
         {
             String name = (String) itr.next();
 
             deps.add(info.getType(name));
         }
 
         for (Iterator itr = info.getElements(); itr.hasNext(); )
         {
             String name = (String) itr.next();
 
             deps.add(info.getType(name));
         }
         
         return deps;
     }
 
     public BeanTypeInfo getTypeInfo()
     {
         if (_info == null)
         {
             _info = createTypeInfo();
         }
         
         // Delay initialization so things work in recursive scenarios (XFIRE-117)
         if (!_info.isInitialized())
         {
             _info.initialize();
         }
         
         return _info;
     }
 
     public BeanTypeInfo createTypeInfo()
     {
         BeanTypeInfo info = new BeanTypeInfo(getTypeClass());
 
         info.setTypeMapping(getTypeMapping());
 
         return info;
     }
 }

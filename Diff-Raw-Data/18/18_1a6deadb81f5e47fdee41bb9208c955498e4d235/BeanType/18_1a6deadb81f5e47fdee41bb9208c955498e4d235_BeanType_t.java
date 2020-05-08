 package org.codehaus.xfire.aegis.type.basic;
 
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Proxy;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLStreamReader;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.aegis.AegisBindingProvider;
 import org.codehaus.xfire.aegis.MessageReader;
 import org.codehaus.xfire.aegis.MessageWriter;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.aegis.type.TypeMapping;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.util.ClassLoaderUtils;
 import org.codehaus.xfire.util.NamespaceHelper;
 import org.jdom.Attribute;
 import org.jdom.Element;
 
 /**
  * Serializes JavaBeans.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @author <a href="mailto:jack.xu.hong@gmail.com">Jack Hong</a>
  */
 public class BeanType
     extends Type
 {
     private BeanTypeInfo _info;
 
     private boolean isInterface = false;
 
     private boolean isException = false;
 
     public BeanType()
     {
     }
 
     public BeanType(BeanTypeInfo info)
     {
         this._info = info;
         this.setTypeClass(info.getTypeClass());
     }
 
     private QName getXsiType(MessageReader reader)
     {
         XMLStreamReader xsr = reader.getXMLStreamReader();
         String value = xsr.getAttributeValue(SoapConstants.XSI_NS, "type");
         if (value == null)
         {
             return null;
         }
         else
         {
             return NamespaceHelper.createQName(xsr.getNamespaceContext(), value);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.codehaus.xfire.aegis.type.Type#readObject(org.codehaus.xfire.aegis.MessageReader,
      *      org.codehaus.xfire.MessageContext)
      */
     public Object readObject(MessageReader reader, MessageContext context)
         throws XFireFault
     {
         BeanTypeInfo info = getTypeInfo();
 
         try
         {
             Class clazz = getTypeClass();
             Object object = null;
             InterfaceInvocationHandler delegate = null;
             boolean isProxy = false;
 
             if (isInterface)
             {
                 String impl = null;
                 if (context.getService() != null)
                 {
                     impl = (String) context.getService().getProperty(clazz.getName()
                             + ".implementation");
                 }
 
                 if (impl == null)
                 {
                     delegate = new InterfaceInvocationHandler();
                     object = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                                                     new Class[] { clazz },
                                                     delegate);
                     isProxy = true;
                 }
                 else
                 {
                     try
                     {
                         clazz = ClassLoaderUtils.loadClass(impl, getClass());
                         object = clazz.newInstance();
                     }
                     catch (ClassNotFoundException e)
                     {
                         throw new XFireRuntimeException("Could not find implementation class "
                                 + impl + " for class " + clazz.getName());
                     }
                 }
             }
             else if (isException)
             {
                 object = createFromFault(context);
             }
             else
             {
                 object = clazz.newInstance();
             }
 
             // Read attributes
             while (reader.hasMoreAttributeReaders())
             {
                 MessageReader childReader = reader.getNextAttributeReader();
                 QName name = childReader.getName();
 
                 Type type = info.getType(name);
 
                 if (type != null)
                 {
                     Object writeObj = type.readObject(childReader, context);
                     if (isProxy)
                     {
                         delegate.writeProperty(name.getLocalPart(), writeObj);
                     }
                     else
                     {
                         writeProperty(name, object, writeObj, clazz, info);
                     }
                 }
             }
 
             // Read child elements
             while (reader.hasMoreElementReaders())
             {
                 MessageReader childReader = reader.getNextElementReader();
                 QName name = childReader.getName();
                 QName qn = getXsiType(childReader);
 
                 BeanType parent;
                 Type type;
                 if (qn == null)
                 {
                     parent = getBeanTypeWithProperty(name);
                     if (parent != null)
                     {
                         info = parent.getTypeInfo();
                         type = info.getType(name);
                     }
                     else
                     {
                         type = null;
                     }
                 }
                 else
                 {
                     type = getTypeMapping().getType(qn);
                 }
 
                 if (type != null)
                 {
                     if (!childReader.isXsiNil())
                     {
                         Object writeObj = type.readObject(childReader, context);
 
                         if (isProxy)
                         {
                             delegate.writeProperty(name.getLocalPart(), writeObj);
                         }
                         else
                         {
                             writeProperty(name, object, writeObj, clazz, info);
                         }
                     }
                     else
                     {
                         if (!info.isNillable(name))
                         {
                             throw new XFireFault(
                                     name.getLocalPart() + " is nil, but not nillable.",
                                     XFireFault.SENDER);
 
                         }
                         childReader.readToEnd();
                     }
                 }
                 else
                 {
                     childReader.readToEnd();
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
             throw new XFireFault("Couldn't instantiate class. " + e.getMessage(), e,
                     XFireFault.SENDER);
         }
         catch (SecurityException e)
         {
             throw new XFireFault("Illegal access. " + e.getMessage(), e, XFireFault.RECEIVER);
         }
         catch (IllegalArgumentException e)
         {
             throw new XFireFault("Illegal argument. " + e.getMessage(), e, XFireFault.RECEIVER);
         }
         catch (InvocationTargetException e)
         {
             throw new XFireFault("Couldn't create class: " + e.getMessage(), e, XFireFault.RECEIVER);
         }
 
     }
 
     /**
      * If the class is an exception, this will try and instantiate it with
      * information from the XFireFault (if it exists).
      */
     protected Object createFromFault(MessageContext context)
         throws SecurityException, InstantiationException, IllegalAccessException,
         IllegalArgumentException, InvocationTargetException
     {
         Class clazz = getTypeClass();
         Constructor ctr;
         Object o;
         Object body = context.getExchange().getFaultMessage().getBody();
 
         if (!(body instanceof XFireFault))
             return clazz.newInstance();
 
         XFireFault fault = (XFireFault) body;
 
         try
         {
             ctr = clazz.getConstructor(new Class[] { String.class, Throwable.class });
             o = ctr.newInstance(new Object[] { fault.getMessage(), fault });
         }
         catch (NoSuchMethodException e)
         {
             try
             {
                 ctr = clazz.getConstructor(new Class[] { String.class, Exception.class });
                 o = ctr.newInstance(new Object[] { fault.getMessage(), fault });
             }
             catch (NoSuchMethodException e1)
             {
                 try
                 {
                     ctr = clazz.getConstructor(new Class[] { String.class });
                     o = ctr.newInstance(new Object[] { fault.getMessage() });
                 }
                 catch (NoSuchMethodException e2)
                 {
                     return clazz.newInstance();
                 }
             }
         }
 
         return o;
     }
 
     /**
      * Write the specified property to a field.
      */
     protected void writeProperty(QName name,
                                  Object object,
                                  Object property,
                                  Class impl,
                                  BeanTypeInfo info)
         throws XFireFault
     {
         try
         {
             PropertyDescriptor desc = info.getPropertyDescriptorFromMappedName(name);
 
             Method m = desc.getWriteMethod();
 
             if (m == null)
             {
                 if (getTypeClass().isInterface())
                     m = getWriteMethodFromImplClass(impl, desc);
 
                 if (m == null)
                     throw new XFireFault("No write method for property " + name + " in "
                             + object.getClass(), XFireFault.SENDER);
             }
 
             Class propertyType = desc.getPropertyType();
             if ((property == null && !propertyType.isPrimitive()) || (property != null))
             {
                 m.invoke(object, new Object[] { property });
             }
         }
         catch (Exception e)
         {
             if (e instanceof XFireFault)
                 throw (XFireFault) e;
 
             throw new XFireFault("Couldn't set property " + name + " on " + object + ". "
                     + e.getMessage(), e, XFireFault.SENDER);
         }
     }
 
     /**
      * This is a hack to get the write method from the implementation class for
      * an interface.
      */
     private Method getWriteMethodFromImplClass(Class impl, PropertyDescriptor pd)
         throws Exception
     {
         String name = pd.getName();
         name = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
 
         return impl.getMethod(name, new Class[] { pd.getPropertyType() });
     }
 
     /**
      * @see org.codehaus.xfire.aegis.type.Type#writeObject(Object,
      *      org.codehaus.xfire.aegis.MessageWriter,
      *      org.codehaus.xfire.MessageContext)
      */
     public void writeObject(Object object, MessageWriter writer, MessageContext context)
         throws XFireFault
     {
         if (object == null)
             return;
 
         BeanTypeInfo info = getTypeInfo();
 
        if (context.getService() != null)
        {
            Object writeXsiType = context.getService()
                    .getProperty(AegisBindingProvider.WRITE_XSI_TYPE_KEY);
            if (Boolean.TRUE.equals(writeXsiType) || "true".equals(writeXsiType)
                 && object.getClass() == getTypeClass())
         {
             writer.writeXsiType(getSchemaType());
         }
 
        }

         /*
          * TODO: Replace this method with one split into two pieces so that we
          * can front-load the attributes and traverse down the list of super
          * classes.
          */
         for (Iterator itr = info.getAttributes(); itr.hasNext();)
         {
             QName name = (QName) itr.next();
 
             Object value = readProperty(object, name);
             if (value != null)
             {
                 Type type = getType(info, name);
 
                 if (type == null)
                     throw new XFireRuntimeException("Couldn't find type for " + value.getClass()
                             + " for property " + name);
 
                 MessageWriter cwriter = writer.getAttributeWriter(name);
 
                 type.writeObject(value, cwriter, context);
 
                 cwriter.close();
             }
         }
 
         for (Iterator itr = info.getElements(); itr.hasNext();)
         {
             QName name = (QName) itr.next();
 
             if (info.isExtension()
                     && info.getPropertyDescriptorFromMappedName(name).getReadMethod()
                             .getDeclaringClass() != info.getTypeClass())
             {
                 continue;
             }
             Object value = readProperty(object, name);
 
             Type type = getType(info, name);
             type = AegisBindingProvider.getWriteType(context, value, type);
             MessageWriter cwriter;
 
             // Write the value if it is not null.
             if (value != null)
             {
                 cwriter = getWriter(writer, name, type);
 
                 if (type == null)
                     throw new XFireRuntimeException("Couldn't find type for " + value.getClass()
                             + " for property " + name);
 
                 type.writeObject(value, cwriter, context);
 
                 cwriter.close();
             }
             else if (info.isNillable(name))
             {
                 cwriter = getWriter(writer, name, type);
 
                 // Write the xsi:nil if it is null.
                 cwriter.writeXsiNil();
 
                 cwriter.close();
             }
         }
         if (info.isExtension())
         {
             Type t = getSuperType();
             if (t != null)
             {
                 t.writeObject(object, writer, context);
             }
         }
     }
 
     private MessageWriter getWriter(MessageWriter writer, QName name, Type type)
     {
         MessageWriter cwriter;
         if (type.isAbstract())
         {
             cwriter = writer.getElementWriter(name);
         }
         else
         {
             cwriter = writer.getElementWriter(name);
         }
         return cwriter;
     }
 
     protected Object readProperty(Object object, QName name)
     {
         try
         {
             PropertyDescriptor desc = getTypeInfo().getPropertyDescriptorFromMappedName(name);
 
             Method m = desc.getReadMethod();
 
             if (m == null)
                 throw new XFireFault("No read method for property " + name + " in class "
                         + object.getClass().getName(), XFireFault.SENDER);
 
             return m.invoke(object, new Object[0]);
         }
         catch (Exception e)
         {
             throw new XFireRuntimeException("Couldn't get property " + name + " from bean "
                     + object, e);
         }
     }
 
     /**
      * @see org.codehaus.xfire.aegis.type.Type#writeSchema(org.jdom.Element)
      */
     public void writeSchema(Element root)
     {
         BeanTypeInfo info = getTypeInfo();
         Element complex = new Element("complexType", SoapConstants.XSD_PREFIX, SoapConstants.XSD);
         complex.setAttribute(new Attribute("name", getSchemaType().getLocalPart()));
         root.addContent(complex);
 
         Type sooperType = getSuperType();
 
         if (info.isExtension() && sooperType != null)
         {
             Element complexContent = new Element("complexContent", SoapConstants.XSD_PREFIX,
                     SoapConstants.XSD);
             complex.addContent(complexContent);
             complex = complexContent;
         }
 
         /*
          * See Java Virtual Machine specification:
          * http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#75734
          */
         if ((info.getTypeClass().getModifiers() & Modifier.ABSTRACT) != 0)
         {
             complex.setAttribute(new Attribute("abstract", "true"));
         }
 
         /*
          * Decide if we're going to extend another type. If we are going to
          * defer, then make sure that we extend the type for our superclass.
          */
         boolean isExtension = info.isExtension();
 
         Element dummy = complex;
 
         if (isExtension && sooperType != null)
         {
 
             Element extension = new Element("extension", SoapConstants.XSD_PREFIX,
                     SoapConstants.XSD);
             complex.addContent(extension);
             QName baseType = sooperType.getSchemaType();
             extension.setAttribute(new Attribute("base", getNameWithPrefix2(extension, baseType
                     .getNamespaceURI(), baseType.getLocalPart())));
 
             dummy = extension;
         }
 
         Element seq = null;
 
         // Write out schema for elements
         for (Iterator itr = info.getElements(); itr.hasNext();)
         {
 
             QName name = (QName) itr.next();
 
             if (isExtension)
             {
                 PropertyDescriptor pd = info.getPropertyDescriptorFromMappedName(name);
 
                 assert pd.getReadMethod() != null && pd.getWriteMethod() != null;
                 if (pd.getReadMethod().getDeclaringClass() != info.getTypeClass())
                 {
                     continue;
                 }
             }
 
             if (seq == null)
             {
                 seq = new Element("sequence", SoapConstants.XSD_PREFIX, SoapConstants.XSD);
                 dummy.addContent(seq);
             }
 
             Element element = new Element("element", SoapConstants.XSD_PREFIX, SoapConstants.XSD);
             seq.addContent(element);
 
             Type type = getType(info, name);
 
             String nameNS = name.getNamespaceURI();
             String nameWithPrefix = getNameWithPrefix(root, nameNS, name.getLocalPart());
 
             String prefix = NamespaceHelper.getUniquePrefix((Element) root.getParent(), type
                     .getSchemaType().getNamespaceURI());
 
             writeTypeReference(name, nameWithPrefix, element, type, prefix);
         }
 
         /**
          * if future proof then add <xsd:any/> element
          */
         if (info.isExtensibleElements())
         {
             if (seq == null)
             {
                 seq = new Element("sequence", SoapConstants.XSD_PREFIX, SoapConstants.XSD);
                 dummy.addContent(seq);
             }
             seq.addContent(createAnyElement());
         }
 
         // Write out schema for attributes
         for (Iterator itr = info.getAttributes(); itr.hasNext();)
         {
             QName name = (QName) itr.next();
 
             Element element = new Element("attribute", SoapConstants.XSD_PREFIX, SoapConstants.XSD);
             dummy.addContent(element);
 
             Type type = getType(info, name);
 
             String nameNS = name.getNamespaceURI();
             String nameWithPrefix = getNameWithPrefix(root, nameNS, name.getLocalPart());
 
             String prefix = NamespaceHelper.getUniquePrefix((Element) root.getParent(), type
                     .getSchemaType().getNamespaceURI());
             element.setAttribute(new Attribute("name", nameWithPrefix));
             element.setAttribute(new Attribute("type", prefix + ':'
                     + type.getSchemaType().getLocalPart()));
         }
 
         /**
          * If extensible attributes then add <xsd:anyAttribute/>
          */
         if (info.isExtensibleAttributes())
         {
             dummy.addContent(createAnyAttribute());
         }
     }
 
     private String getNameWithPrefix(Element root, String nameNS, String localName)
     {
         if (!nameNS.equals(getSchemaType().getNamespaceURI()))
         {
             String prefix = NamespaceHelper.getUniquePrefix((Element) root.getParent(), nameNS);
 
             if (prefix == null || prefix.length() == 0)
                 prefix = NamespaceHelper.getUniquePrefix(root, nameNS);
 
             return prefix + ":" + localName;
         }
         return localName;
     }
 
     private String getNameWithPrefix2(Element root, String nameNS, String localName)
     {
         String prefix = NamespaceHelper.getUniquePrefix((Element) root.getParent(), nameNS);
 
         if (prefix == null || prefix.length() == 0)
             prefix = NamespaceHelper.getUniquePrefix(root, nameNS);
 
         return prefix + ":" + localName;
     }
 
     private Type getType(BeanTypeInfo info, QName name)
     {
         Type type = info.getType(name);
 
         if (type == null)
         {
             throw new NullPointerException("Couldn't find type for" + name + " in class "
                     + getTypeClass().getName());
         }
 
         return type;
     }
 
     private void writeTypeReference(QName name,
                                     String nameWithPrefix,
                                     Element element,
                                     Type type,
                                     String prefix)
     {
         if (type.isAbstract())
         {
             element.setAttribute(new Attribute("name", nameWithPrefix));
             element.setAttribute(new Attribute("type", prefix + ':'
                     + type.getSchemaType().getLocalPart()));
 
             int minOccurs = getTypeInfo().getMinOccurs(name);
             if (minOccurs != 1)
             {
                 element.setAttribute(new Attribute("minOccurs", new Integer(minOccurs).toString()));
             }
 
             if (getTypeInfo().isNillable(name))
             {
                 element.setAttribute(new Attribute("nillable", "true"));
             }
         }
         else
         {
             element.setAttribute(new Attribute("ref", prefix + ':'
                     + type.getSchemaType().getLocalPart()));
         }
     }
 
     public void setTypeClass(Class typeClass)
     {
         super.setTypeClass(typeClass);
 
         isInterface = typeClass.isInterface();
         isException = Exception.class.isAssignableFrom(typeClass);
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
 
         for (Iterator itr = info.getAttributes(); itr.hasNext();)
         {
             QName name = (QName) itr.next();
             deps.add(info.getType(name));
         }
 
         for (Iterator itr = info.getElements(); itr.hasNext();)
         {
             QName name = (QName) itr.next();
             if (info.isExtension()
                     && info.getPropertyDescriptorFromMappedName(name).getReadMethod()
                             .getDeclaringClass() != info.getTypeClass())
                 continue;
             deps.add(info.getType(name));
         }
 
         /*
          * Automagically add chain of superclasses *if* this is an an extension.
          */
         if (info.isExtension())
         {
             Type sooperType = getSuperType();
             if (sooperType != null)
             {
                 deps.add(sooperType);
             }
         }
 
         return deps;
     }
 
     private BeanType getBeanTypeWithProperty(QName name)
     {
         BeanType sooper = this;
         Type type = null;
 
         while (type == null && sooper != null)
         {
             type = sooper.getTypeInfo().getType(name);
 
             if (type == null)
                 sooper = sooper.getSuperType();
         }
 
         return (BeanType) sooper;
     }
 
     private BeanType getSuperType()
     {
         BeanTypeInfo info = getTypeInfo();
         Class c = info.getTypeClass().getSuperclass();
         /*
          * Don't dig any deeper than Object or Exception
          */
         if (c != null && c != Object.class && c != Exception.class && c != RuntimeException.class)
         {
             TypeMapping tm = info.getTypeMapping();
             BeanType superType = (BeanType) tm.getType(c);
             if (superType == null)
             {
                 superType = (BeanType) getTypeMapping().getTypeCreator().createType(c);
                 Class cParent = c.getSuperclass();
                 if (cParent != null && cParent != Object.class)
                 {
                     superType.getTypeInfo().setExtension(true);
                 }
                 tm.register(superType);
             }
             return superType;
         }
         else
         {
             return null;
         }
     }
 
     public BeanTypeInfo getTypeInfo()
     {
         if (_info == null)
         {
             _info = createTypeInfo();
         }
 
         // Delay initialization so things work in recursive scenarios
         // (XFIRE-117)
         if (!_info.isInitialized())
         {
             _info.initialize();
         }
 
         return _info;
     }
 
     public BeanTypeInfo createTypeInfo()
     {
         BeanTypeInfo info = new BeanTypeInfo(getTypeClass(), getSchemaType().getNamespaceURI());
 
         info.setTypeMapping(getTypeMapping());
 
         return info;
     }
 
     /**
      * Create an element to represent any future elements that might get added
      * to the schema <xsd:any minOccurs="0" maxOccurs="unbounded"/>
      * 
      * @return
      */
     private Element createAnyElement()
     {
         Element result = new Element("any", SoapConstants.XSD_PREFIX, SoapConstants.XSD);
         result.setAttribute(new Attribute("minOccurs", "0"));
         result.setAttribute(new Attribute("maxOccurs", "unbounded"));
         return result;
     }
 
     public String toString()
     {
         StringBuffer sb = new StringBuffer();
         sb.append(getClass().getName());
         sb.append(": [class=");
         Class c = getTypeClass();
         sb.append((c == null) ? ("<null>") : (c.getName()));
         sb.append(",\nQName=");
         QName q = getSchemaType();
         sb.append((q == null) ? ("<null>") : (q.toString()));
         sb.append(",\ninfo=");
         sb.append(getTypeInfo().toString());
         sb.append("]");
         return sb.toString();
     }
 
     /**
      * Create an element to represent any future attributes that might get added
      * to the schema <xsd:anyAttribute/>
      * 
      * @return
      */
     private Element createAnyAttribute()
     {
         Element result = new Element("anyAttribute", SoapConstants.XSD_PREFIX, SoapConstants.XSD);
         return result;
     }
 
 }

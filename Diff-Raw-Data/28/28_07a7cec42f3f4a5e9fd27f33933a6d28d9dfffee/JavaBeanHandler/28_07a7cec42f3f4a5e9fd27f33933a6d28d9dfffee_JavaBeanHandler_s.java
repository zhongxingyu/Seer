 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, Red Hat Middleware LLC, and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.kernel.plugins.config.xml;
 
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.namespace.QName;
 
 import org.jboss.kernel.plugins.config.xml.Common.Ctor;
 import org.jboss.kernel.plugins.config.xml.Common.Holder;
 import org.jboss.xb.binding.sunday.unmarshalling.DefaultElementHandler;
 import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;
 import org.xml.sax.Attributes;
 
 /**
  * Handler for the javabean element.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @author Scott.Stark@jboss.org
  * @version $Revision: 43020 $
  */
 public class JavaBeanHandler extends DefaultElementHandler
 {
    /** The handler */
    public static final JavaBeanHandler HANDLER = new JavaBeanHandler();
 
    public Object startElement(Object parent, QName name, ElementBinding element)
    {
       return new Holder();
    }
 
    public void attributes(Object o, QName elementName, ElementBinding element, Attributes attrs, NamespaceContext nsCtx)
    {
       Holder holder = (Holder) o;
       String className = null;
       for (int i = 0; i < attrs.getLength(); ++i)
       {
          String localName = attrs.getLocalName(i);
          if ("class".equals(localName))
             className = attrs.getValue(i);
       }
       
       if (className == null)
          throw new IllegalArgumentException("No class attribute for " + elementName);
       
       try
       {
          Ctor ctor = new Ctor(className);
          holder.setValue(ctor);
       }
       catch (RuntimeException e)
       {
          throw e;
       }
       catch (Error e)
       {
          throw e;
       }
       catch (Throwable t)
       {
          throw new RuntimeException("Error instantiating class " + className, t);
       }
    }
 
    public Object endElement(Object o, QName qName, ElementBinding element)
    {
       Holder holder = (Holder) o;
      return holder.getValue();
    }
 }
 

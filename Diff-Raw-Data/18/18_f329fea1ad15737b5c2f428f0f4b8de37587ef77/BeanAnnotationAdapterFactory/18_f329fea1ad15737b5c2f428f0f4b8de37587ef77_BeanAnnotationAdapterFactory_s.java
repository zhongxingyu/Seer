 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.kernel.plugins.annotations;
 
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 
 import org.jboss.logging.Logger;
 import org.jboss.reflect.plugins.introspection.ReflectionUtils;
 
 /**
  * BeanAnnotationAdapter factory.
  *
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public class BeanAnnotationAdapterFactory
 {
    private static Logger log = Logger.getLogger(BeanAnnotationAdapterFactory.class);
    private static final BeanAnnotationAdapterFactory factory = new BeanAnnotationAdapterFactory();
 
   private String adapterClassName = BasicBeanAnnotationAdapter.class.getName();
    private BeanAnnotationAdapter adapter;
 
    private BeanAnnotationAdapterFactory()
    {
    }
 
    public static BeanAnnotationAdapterFactory getInstance()
    {
       return factory;
    }
 
    public void setAdapterClassName(String adapterClassName)
    {
       this.adapterClassName = adapterClassName;
    }
 
    /**
     * Get the BeanAnnotationAdapter instance.
     * @return the BeanAnnotationAdapter instance
     */
    public BeanAnnotationAdapter getBeanAnnotationAdapter()
    {
       if (adapter == null)
          adapter = AccessController.doPrivileged(new AdapterLookup());
 
       return adapter;
    }
 
    private class AdapterLookup implements PrivilegedAction<BeanAnnotationAdapter>
    {
       public BeanAnnotationAdapter run()
       {
          try
          {
             String adapterClass = System.getProperty("org.jboss.kernel.plugins.annotations.BeanAnnotationAdapter", adapterClassName);
            if (log.isTraceEnabled())
               log.trace("Instantiating bean annotation adapter: " + adapterClass);
            Object result = ReflectionUtils.newInstance(adapterClass);
            return BeanAnnotationAdapter.class.cast(result);
          }
          catch (Throwable t)
          {
             log.warn("Exception while creating bean annotation adapter instance: " + t);
            return BasicBeanAnnotationAdapter.INSTANCE;
          }
       }
    }
 
 }

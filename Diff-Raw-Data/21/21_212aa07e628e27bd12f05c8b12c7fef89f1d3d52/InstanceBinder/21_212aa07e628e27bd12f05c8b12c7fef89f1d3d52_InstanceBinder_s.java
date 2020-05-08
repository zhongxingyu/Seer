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
 package org.jboss.guice.plugins;
 
 import java.util.Map;
 
 import com.google.inject.Binder;
 import com.google.inject.spi.SourceProviders;
 import org.jboss.guice.spi.BinderHolder;
 
 /**
  * Microcontainer + Guice instance binder.
  *
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public class InstanceBinder extends BinderHolder
 {
    static
    {
       SourceProviders.skip(InstanceBinder.class);
    }
 
   private Map<Class, Object> bindings;
 
    public InstanceBinder(Binder binder)
    {
       super(binder);
    }
 
    @SuppressWarnings("unchecked")
    public void start()
    {
       if (bindings != null && bindings.isEmpty() == false)
       {
         for(Map.Entry<Class, Object> entry : bindings.entrySet())
          {
             Object value = entry.getValue();
             if (value == null)
                throw new IllegalArgumentException("Null value in map: " + bindings);
             Class clazz = entry.getKey();
             if (clazz == null)
                clazz = value.getClass();
             if (clazz.isAssignableFrom(value.getClass()) == false)
                throw new IllegalArgumentException("Value " + value + " doesn't implement class " + clazz);
             getBinder().bind(clazz).toInstance(value);
          }
       }
    }
 
    public void stop()
    {
       // todo - unbind all
    }
 
   public void setBindings(Map<Class, Object> bindings)
    {
       this.bindings = bindings;
    }
 }

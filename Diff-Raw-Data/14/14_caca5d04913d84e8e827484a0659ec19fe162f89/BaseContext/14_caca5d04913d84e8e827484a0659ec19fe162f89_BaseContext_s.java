 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.test.kernel.deployment.support.container;
 
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
 
 import org.jboss.logging.Logger;
 
 /**
  * @param <B> the bean type
  * @param <C> the container type
  * @author Scott.Stark@jboss.org
  * @version $Revision:$
  */
 public class BaseContext <B, C extends BeanContainer<B>>
    implements BeanContext<C>
 {
    protected static Logger log = Logger.getLogger(BaseContext.class);
    protected C container;
    protected B bean;
    
    protected List<Object> interceptorInstances = new ArrayList<Object>();
 
    public BaseContext(C container)
    {
       this.container = container;
       log.info("ctor, container: "+container);
    }
 
    public C getContainer()
    {
       return container;
    }
 
    public B getInstance()
    {
       return bean;
    }
    public void setInstance(B bean)
    {
       this.bean = bean;
       log.info("setInstance, bean: "+bean);
    }
 
   
   public HashMap<Class<?>, Object> getInterceptorInstances()
   {
      return null;
   }
    public List<Object> getInterceptors()
    {
       return interceptorInstances;
    }
 
    public void initialiseInterceptorInstances()
    {  
    }
    public void addInterceptor(Object interceptor)
    {
       interceptorInstances.add(interceptor);
       log.info("addInterceptor, "+interceptor);
    }
    public void removeInterceptor(Object interceptor)
    {
       interceptorInstances.remove(interceptor);
       log.info("removeInterceptor, "+interceptor);
    }
    public void remove()
    {
       // TODO Auto-generated method stub
       
    }
 }

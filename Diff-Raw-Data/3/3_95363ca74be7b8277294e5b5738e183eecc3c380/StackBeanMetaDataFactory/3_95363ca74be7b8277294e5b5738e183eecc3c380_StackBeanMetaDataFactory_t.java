 /*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
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
 package org.jboss.aop.microcontainer.beans.beanmetadatafactory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.aop.microcontainer.beans.Stack;
 import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
 import org.jboss.beans.metadata.plugins.AbstractInjectionValueMetaData;
 import org.jboss.beans.metadata.plugins.AbstractListMetaData;
 import org.jboss.beans.metadata.spi.BeanMetaData;
 
 /**
  * 
  * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
  * @version $Revision: 1.1 $
  */
 public class StackBeanMetaDataFactory extends AspectManagerAwareBeanMetaDataFactory
 {
    private static final long serialVersionUID = 1L;
    private List<BaseInterceptorData> interceptors = new ArrayList<BaseInterceptorData>();
 
    public StackBeanMetaDataFactory()
    {
       //Meeded to satisfy validation in BeanFactoryHandler.endElement()
       setBeanClass("IGNORED");
    }
    
    @Override
    public List<BeanMetaData> getBeans()
    {
       ArrayList<BeanMetaData> result = new ArrayList<BeanMetaData>();
 
       //Create AspectBinding
       AbstractBeanMetaData stack = new AbstractBeanMetaData();
       stack.setName(name);
       BeanMetaDataUtil.setSimpleProperty(stack, "name", name);
       stack.setBean(Stack.class.getName());
 
       util.setAspectManagerProperty(stack, "manager");
       result.add(stack);
       
       if (interceptors.size() > 0)
       {
          AbstractListMetaData almd = new AbstractListMetaData();
          int i = 0;
          for (BaseInterceptorData interceptor : interceptors)
          {
             AbstractBeanMetaData bmd = new AbstractBeanMetaData(interceptor.getBeanClassName());
             String intName = name + "$" + i++; 
             bmd.setName(intName);
             util.setAspectManagerProperty(bmd, "manager");
             BeanMetaDataUtil.setSimpleProperty(bmd, "forStack", Boolean.TRUE);
             
             if (interceptor instanceof AdviceData)
             {
                BeanMetaDataUtil.DependencyBuilder db = new BeanMetaDataUtil.DependencyBuilder(bmd, "aspect", interceptor.getRefName());
                BeanMetaDataUtil.setDependencyProperty(db);
                if (((AdviceData)interceptor).getAdviceMethod() != null)
                {
                   BeanMetaDataUtil.setSimpleProperty(bmd, "aspectMethod", ((AdviceData)interceptor).getAdviceMethod());
                }
                BeanMetaDataUtil.setSimpleProperty(bmd, "type", ((AdviceData)interceptor).getType());
                
             }
             else
             {
                BeanMetaDataUtil.DependencyBuilder db = new BeanMetaDataUtil.DependencyBuilder(bmd, "stack", interceptor.getRefName());
                BeanMetaDataUtil.setDependencyProperty(db);
             }
             result.add(bmd);
             almd.add(new AbstractInjectionValueMetaData(intName));
          }         
         BeanMetaDataUtil.setSimpleProperty(stack, "advices", almd);
       }
       
       return result;
    }
    
    public void addInterceptor(BaseInterceptorData interceptorData)
    {
       interceptors.add(interceptorData);
    }
 
 }

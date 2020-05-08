 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.struts.lessconfig.hotdeploy;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.aopalliance.intercept.MethodInvocation;
 import org.apache.struts.config.FormBeanConfig;
 import org.apache.struts.config.ModuleConfig;
 import org.seasar.framework.aop.interceptors.AbstractInterceptor;
 import org.seasar.framework.container.S2Container;
 import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
 import org.seasar.framework.container.util.Traversal;
 import org.seasar.struts.lessconfig.autoregister.ActionFormConfigCreator;
 
 /**
  * 
  * @author Katsuhiko Nagashima
  */
 public class OndemandFindFormBeanConfigsInterceptor extends AbstractInterceptor {
 
     private static final long serialVersionUID = -5732912846979542236L;
 
     private ActionFormConfigCreator formConfigCreator;
 
     public void setFormConfigCreator(ActionFormConfigCreator formBeanConfigCreator) {
         this.formConfigCreator = formBeanConfigCreator;
     }
 
     public Object invoke(MethodInvocation invocation) throws Throwable {
         FormBeanConfig[] result = (FormBeanConfig[]) invocation.proceed();
 
         ModuleConfig config = (ModuleConfig) invocation.getThis();
         List formBeanConfigs = getActionConfigs(config);
         if (result != null) {
             formBeanConfigs.addAll(Arrays.asList(result));
         }
         return formBeanConfigs.toArray(new FormBeanConfig[formBeanConfigs.size()]);
     }
 
     private List getActionConfigs(final ModuleConfig config) {
         final List formBeanConfigs = new ArrayList();
         final ActionFormConfigCreator creator = this.formConfigCreator;
 
         S2Container container = SingletonS2ContainerFactory.getContainer();
         Traversal.forEachContainer(container, new Traversal.S2ContainerHandler() {
             public Object processContainer(S2Container container) {
                 for (int i = 0; i < container.getComponentDefSize(); i++) {
                     Class clazz = container.getComponentDef(i).getComponentClass();
                    if (clazz == null) {
                        continue;
                    }
                     FormBeanConfig formBeanConfig = creator.createFormBeanConfig(config, clazz);
                     if (formBeanConfig != null) {
                         formBeanConfigs.add(formBeanConfig);
                     }
                 }
 
                 return null;
             }
         });
         return formBeanConfigs;
     }
 
 }

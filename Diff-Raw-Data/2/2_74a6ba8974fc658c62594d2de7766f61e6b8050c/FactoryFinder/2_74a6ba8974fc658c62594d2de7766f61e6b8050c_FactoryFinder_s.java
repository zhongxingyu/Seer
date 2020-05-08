 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
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
 package javax.faces;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.internal.FactoryFinderUtil;
 
 import org.seasar.framework.util.AssertionUtil;
 
 /**
  * @author shot
  * @author higa
  */
 public final class FactoryFinder {
 
     public static final String APPLICATION_FACTORY = "javax.faces.application.ApplicationFactory";
 
     public static final String FACES_CONTEXT_FACTORY = "javax.faces.context.FacesContextFactory";
 
     public static final String LIFECYCLE_FACTORY = "javax.faces.lifecycle.LifecycleFactory";
 
     public static final String RENDER_KIT_FACTORY = "javax.faces.render.RenderKitFactory";
 
     private static final Map factories = new HashMap();
 
     private static final Map factoryClassNames = new HashMap();
 
     public static Object getFactory(String factoryName) throws FacesException {
         AssertionUtil.assertNotNull("factoryName", factoryName);
         if (!factoryClassNames.containsKey(factoryName)) {
             throw new IllegalStateException("no factory " + factoryName
                    + " configured for this appliction");
         }
         Object factory = factories.get(factoryName);
         if (factory == null) {
             List classNames = (List) factoryClassNames.get(factoryName);
             factory = FactoryFinderUtil.createFactoryInstance(factoryName,
                     classNames);
             factories.put(factoryName, factory);
             return factory;
         } else {
             return factory;
         }
     }
 
     public static void setFactory(String factoryName, String implName) {
         AssertionUtil.assertNotNull("factoryName", factoryName);
         FactoryFinderUtil.checkValidFactoryNames(factoryName);
         List classNameList = (List) factoryClassNames.get(factoryName);
         if (classNameList == null) {
             classNameList = new ArrayList();
             factoryClassNames.put(factoryName, classNameList);
         }
         if (classNameList.contains(implName)) {
             return;
         }
         classNameList.add(implName);
     }
 
     public static void releaseFactories() throws FacesException {
         factories.clear();
         factoryClassNames.clear();
     }
 
 }

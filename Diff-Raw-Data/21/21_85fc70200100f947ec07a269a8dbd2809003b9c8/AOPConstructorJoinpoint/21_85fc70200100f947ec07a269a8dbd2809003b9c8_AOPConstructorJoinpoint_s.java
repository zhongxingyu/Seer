 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.aop.microcontainer.integration;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
 import java.util.List;
import java.util.Set;
 
 import org.jboss.aop.Advisor;
 import org.jboss.aop.AspectManager;
 import org.jboss.aop.advice.Interceptor;
 import org.jboss.aop.joinpoint.ConstructorInvocation;
 import org.jboss.aop.microcontainer.beans.AspectManagerFactory;
 import org.jboss.aop.proxy.container.AOPProxyFactory;
 import org.jboss.aop.proxy.container.AOPProxyFactoryParameters;
 import org.jboss.aop.proxy.container.ContainerCache;
 import org.jboss.aop.proxy.container.GeneratedAOPProxyFactory;
 import org.jboss.joinpoint.plugins.BasicConstructorJoinPoint;
 import org.jboss.metadata.spi.MetaData;
 import org.jboss.metadata.spi.annotation.InstanceAnnotation;
 import org.jboss.metadata.spi.scope.CommonLevels;
 import org.jboss.metadata.spi.scope.CommonLevelsUtil;
 import org.jboss.metadata.spi.scope.ScopeLevel;
 import org.jboss.metadata.spi.signature.MethodSignature;
 import org.jboss.metadata.spi.stack.MetaDataStack;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.ConstructorInfo;
 import org.jboss.reflect.spi.MethodInfo;
 import org.jboss.reflect.spi.TypeInfo;
 
 /**
  * An AOPConstructorJoinpoint.
  *
  * @TODO This is not correct if the target is already advised
  *       there is no need for the proxy advisor.
  *
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class AOPConstructorJoinpoint extends BasicConstructorJoinPoint
 {
    AOPProxyFactory proxyFactory = new GeneratedAOPProxyFactory();
 
    /**
     * Create a new AOPConstructorJoinpoint.
     *
     * @param constructorInfo the constructor info
     */
    public AOPConstructorJoinpoint(ConstructorInfo constructorInfo)
    {
       super(constructorInfo);
    }
 
    public Object dispatch() throws Throwable
    {
       Class<?> clazz = constructorInfo.getDeclaringClass().getType();
       MetaData metaData = MetaDataStack.peek();
       AspectManager manager = AspectManagerFactory.getAspectManager(metaData);
 
       MetaDataStack.mask();
       try
       {
          boolean hasInstanceMetaData = rootHasSubInstanceMetaData(metaData);
          ContainerCache cache = ContainerCache.initialise(manager, clazz, metaData, hasInstanceMetaData);
          AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
          Object target = createTarget(cache, params);
          params.setProxiedClass(target.getClass());
          params.setMetaData(metaData);
          params.setTarget(target);
          params.setContainerCache(cache);
          params.setMetaDataHasInstanceLevelData(hasInstanceMetaData);
          
          return proxyFactory.createAdvisedProxy(params);
       }
       finally
       {
          MetaDataStack.unmask();
       }
    }
 
    /**
     * Check for metadata at INSTANCE level and below.
     *
     * @param metaData the metadata
     * @return true if there is some metadata that needs to be considered
     */
    private boolean rootHasSubInstanceMetaData(MetaData metaData)
    {
       if (metaData == null)
       {
          return false;
       }
 
       if (checkForMetaDataAtSubInstanceLevel(metaData))
       {
          return true;
       }
       
       //Check for method annotations
       return rootHasMethodWithSubInstanceMetaData(metaData);
    }
       
    /**
     * Check for metadata at INSTANCE level and below for methods.
     *
     * @param metaData the metadata
     * @return true if there is some metadata that needs to be considered
     */
    private boolean rootHasMethodWithSubInstanceMetaData(MetaData metaData)
    {
       //Check for method annotations
       ClassInfo info = constructorInfo.getDeclaringClass();
       while (info != null)
       {
          MethodInfo[] methods = info.getDeclaredMethods();
          if (methods != null)
          {
             for (MethodInfo mi : methods)
             {
                if (methodHasSubInstanceMetaData(metaData, mi))
                {
                   return true;
                }
             }
          }
          info = info.getSuperclass();
       }
       
       return false;
    }
 
    /**
     * Check for metadata at INSTANCE level and below for methods.
     *
     * @param metaData the metadata
     * @param mi the method to check
     * @return true if there is some metadata that needs to be considered
     */
    private boolean methodHasSubInstanceMetaData(MetaData metaData, MethodInfo mi)
    {
       MethodSignature sig = new MethodSignature(mi);
       MetaData methodMD = metaData.getComponentMetaData(sig);
 
       if (checkForMetaDataAtSubInstanceLevel(methodMD))
       {
          return true;
       }
       return false;
    }
 
    /**
     * Check for metadata at INSTANCE level and below.
     *
     * @param metaData the metadata
     * @return true if there is some metadata that needs to be considered
     */
    private boolean checkForMetaDataAtSubInstanceLevel(MetaData metaData)
    {
       if (metaData != null)
       {
          // TODO - remove this after making tests work again
          // uncomment this for previous behavior
          // List<ScopeLevel> levels = Collections.singletonList(CommonLevels.INSTANCE);
          List<ScopeLevel> levels = CommonLevelsUtil.getSubLevels(CommonLevels.INSTANCE);
          for (ScopeLevel level : levels)
          {
             if (hasMetaDataAtLevel(metaData, level))
                return true;
          }
       }
       return false;
    }
 
    /**
     * Check for metadata at level param.
     *
     * @param metaData the metadata
     * @param level the scope level
     * @return true if there is some metadata that needs to be considered
     */
    protected boolean hasMetaDataAtLevel(MetaData metaData, ScopeLevel level)
    {
       MetaData levelMetaData = metaData.getScopeMetaData(level);
       if (levelMetaData != null && levelMetaData.isEmpty() == false)
       {
         Set<Object> checkSet = new HashSet<Object>();
          Object[] allMetaData = levelMetaData.getMetaData();
          Annotation[] annotations = levelMetaData.getAnnotations();
         // all meta data is not null, since instance metadata is not empty
         checkSet.addAll(Arrays.asList(allMetaData));
         checkSet.removeAll(Arrays.asList(annotations));
 
          // do we have something else than annotations
         if (checkSet.isEmpty() == false)
             return true;
         else
          {
             // do we have an annotation that's not marked with IA
             for (Annotation annotation : annotations)
             {
                InstanceAnnotation ia = annotation.annotationType().getAnnotation(InstanceAnnotation.class);
                if (ia == null || ia.value())
                   return true;
             }
          }
       }
       return false;
    }
 
    /**
     * Create the target.
     *
     * @param cache the cache
     * @param params the parameters
     * @return target instance
     * @throws Throwable for any error
     */
    private Object createTarget(ContainerCache cache, AOPProxyFactoryParameters params) throws Throwable
    {
       Advisor advisor = cache.getAdvisor();
       if (advisor != null)
       {
          org.jboss.aop.ConstructorInfo aopinfo = findAopConstructorInfo(advisor);
          
          Interceptor[] interceptors = (aopinfo != null) ? aopinfo.getInterceptors() : null;
 
          if (interceptors != null)
          {
             ConstructorInvocation inv = new ConstructorInvocation(aopinfo, aopinfo.getInterceptors());
             inv.setArguments(getArguments());
             return inv.invokeNext();
          }
          
          if (getConstructorInfo().getParameterTypes().length > 0)
          {
             Constructor<?> constructor = null;
             if (aopinfo == null)
             {
                //Fall back to using the class;
                Class<?> clazz = advisor.getClazz();
                Constructor<?>[] ctors = clazz.getConstructors();
                for (Constructor<?> ctor : ctors)
                {
                   if (matchConstructor(ctor))
                   {
                      constructor = ctor;
                      break;
                   }
                }
             }
             else
             {
                constructor = aopinfo.getConstructor();
             }
             params.setCtor(constructor.getParameterTypes(), getArguments());
          }
       }
 
       return super.dispatch();
    }
 
    /**
     * Find constructor info.
     *
     * @param advisor the advisor
     * @return matched constructor or null if no match
     */
    private org.jboss.aop.ConstructorInfo findAopConstructorInfo(Advisor advisor)
    {
       org.jboss.aop.ConstructorInfo[] infos = advisor.getConstructorInfos();
       for (int i = 0 ; i < infos.length ; i++)
       {
          if (matchConstructor(infos[i].getConstructor()))
          {
             return infos[i];
          }
       }
       return null;
    }
 
    /**
     * Match constructor.
     *
     * @param ctor the constructor
     * @return true if we have a match
     */
    private boolean matchConstructor(Constructor<?> ctor)
    {
       TypeInfo[] params = constructorInfo.getParameterTypes();
       Class<?>[] ctorParams = ctor.getParameterTypes();
       if (params.length == ctorParams.length)
       {
          boolean match = true;
          for (int p = 0 ; p < params.length ; p++)
          {
             if (!params[p].getName().equals(ctorParams[p].getName()))
             {
                match = false;
                break;
             }
          }
 
          if (match)
          {
             return true;
          }
       }
       return false;
    }
 }

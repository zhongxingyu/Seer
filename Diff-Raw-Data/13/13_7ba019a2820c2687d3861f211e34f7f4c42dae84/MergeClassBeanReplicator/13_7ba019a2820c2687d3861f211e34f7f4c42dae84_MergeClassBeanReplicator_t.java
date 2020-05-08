 /*
  * Copyright 2007 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License")
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.sf.gilead.core.beanlib.merge;
 
 import java.io.Serializable;
 
 import net.sf.beanlib.hibernate.UnEnhancer;
 import net.sf.beanlib.hibernate3.Hibernate3JavaBeanReplicator;
 import net.sf.beanlib.spi.BeanTransformerSpi;
 import net.sf.beanlib.spi.replicator.BeanReplicatorSpi;
 import net.sf.gilead.annotations.AnnotationsHelper;
 import net.sf.gilead.core.IPersistenceUtil;
 import net.sf.gilead.core.beanlib.IClassMapper;
 import net.sf.gilead.core.store.IProxyStore;
 import net.sf.gilead.exception.NotPersistentObjectException;
 import net.sf.gilead.exception.TransientObjectException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Bean replicator with different from and to classes for merge operation
  * @author bruno.marchesson
  *
  */
 public class MergeClassBeanReplicator extends Hibernate3JavaBeanReplicator
 {
 	//---
 	// Attributes
 	//---
 
 	/**
 	 * Log channel
 	 */
 	private static Log _log = LogFactory.getLog(MergeClassBeanReplicator.class);
 	
 	/**
 	 * The class mapper (can be null)
 	 */
 	private IClassMapper _classMapper;
 	
 	/**
 	 * The persistent util class
 	 */
 	private IPersistenceUtil _persistenceUtil;
 	
 	/**
 	 * The current proxy store
 	 */
 	private IProxyStore _proxyStore;
 	
 	
 	//----
 	// Factory
 	//----
 	public static final Factory factory = new Factory();
 	
     /**
      * Factory for {@link MergeClassBeanReplicator}
      * 
      * @author bruno.marchesson
      */
     public static class Factory implements BeanReplicatorSpi.Factory {
         private Factory() {}
         
         public MergeClassBeanReplicator newBeanReplicatable(BeanTransformerSpi beanTransformer) {
             return new MergeClassBeanReplicator(beanTransformer);
         }
     }
     
     public static MergeClassBeanReplicator newBeanReplicatable(BeanTransformerSpi beanTransformer) {
         return factory.newBeanReplicatable(beanTransformer);
     }
     
     //----
 	// Constructor
 	//----
     protected MergeClassBeanReplicator(BeanTransformerSpi beanTransformer) 
     {
         super(beanTransformer);
     }
     
     //----
     // Properties
     //----
     /**
 	 * @return the Class Mapper
 	 */
 	public IClassMapper getClassMapper() {
 		return _classMapper;
 	}
 
 	/**
 	 * @param mapper the classMapper to set
 	 */
 	public void setClassMapper(IClassMapper mapper) {
 		_classMapper = mapper;
 	}
 	
 	/**
 	 * @return the _persistenceUtil
 	 */
 	public IPersistenceUtil getPersistenceUtil()
 	{
 		return _persistenceUtil;
 	}
 
 	/**
 	 * @param util the persistence Util to set
 	 */
 	public void setPersistenceUtil(IPersistenceUtil util)
 	{
 		_persistenceUtil = util;
 	}
 
 	/**
 	 * @return the proxy store
 	 */
 	public IProxyStore getProxyStore() {
 		return _proxyStore;
 	}
 
 	/**
 	 * @param store the proxy Store to set
 	 */
 	public void setProxyStore(IProxyStore store) {
 		_proxyStore = store;
 	}
 
 	//----
     // Override
     //----
 	@Override
 	public <V extends Object, T extends Object> T replicateBean(V from, java.lang.Class<T> toClass)
 	{
 	//	Reset bean local
 	//
 		BeanlibThreadLocal.setProxyInformations(null);
 		
	// 	Force persistence map computation (useful for subclass)
	//
		_persistenceUtil.isPersistentPojo(from);
		
 	//	Add current bean to stack
 	//
 		BeanlibThreadLocal.getBeanStack().push(from);
 		T result = super.replicateBean(from, toClass);
 		BeanlibThreadLocal.getBeanStack().pop();
 		
 		return result;
 	}
 	
 	@Override
     @SuppressWarnings("unchecked")
     protected <T extends Object> T createToInstance(Object from, java.lang.Class<T> toClass) 
     		throws InstantiationException ,IllegalAccessException ,SecurityException ,NoSuchMethodException
     {	 
     //	Clone mapper indirection
     //
         if (_classMapper != null)
         {
         	Class sourceClass = _classMapper.getSourceClass(UnEnhancer.unenhanceClass(from.getClass())); 
         	if (sourceClass != null)
         	{
         		if (_log.isDebugEnabled())
         		{
         			_log.debug("Creating mapped class " + sourceClass);
         		}
         		toClass = sourceClass;
         	}
         	else
         	{
         		if (_log.isDebugEnabled())
         		{
         			_log.debug("Creating merged class " + toClass);
         		}
         	}
         }
         
     //	Get real target class
     //
         if (toClass.isInterface())
     	{
     	//	Keep the from class
     	//
     		toClass = (Class<T>) from.getClass();
     	}
         
         T result = null;
         
     //	Proxy informations
     //
         if ((AnnotationsHelper.hasServerOnlyOrReadOnlyAnnotations(from.getClass())) ||
         	(AnnotationsHelper.hasServerOnlyOrReadOnlyAnnotations(toClass)))
         {
         	// Load entity
         	try
         	{
 	        	Serializable id = _persistenceUtil.getId(from, toClass);
 	        	result = (T)_persistenceUtil.load(id, toClass);
         	}
         	catch(NotPersistentObjectException e)
         	{
         		_log.warn("Not an hibernate class (" + toClass + ") : annotated values will not be restored.");
         	}
         	catch(TransientObjectException e)
         	{
         		_log.warn("Transient value of class " + toClass + " : annotated values will not be restored.");
         	}
         }
     	
     	if (result != null)
     	{
     		return result;
     	}
     	else
     	{
     		result = super.createToInstance(from, toClass);
     		
     	//	Dynamic proxy workaround : for inheritance purpose
     	//	beanlib returns an instance of the proxy class
     	//	since it inherits from the source class...
     	//
     		if ((_classMapper != null) &&
     			(_classMapper.getSourceClass(result.getClass()) != null))
     		{
     			return newInstanceAsPrivileged(toClass);
     		}
     		return result;
     	}
     }
 }

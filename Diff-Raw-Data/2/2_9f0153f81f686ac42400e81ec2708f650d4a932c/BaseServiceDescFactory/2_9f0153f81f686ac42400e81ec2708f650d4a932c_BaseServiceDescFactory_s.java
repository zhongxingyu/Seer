 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.runtime.common.impl.internal.service;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 
 import javax.wsdl.Definition;
 import javax.xml.XMLConstants;
 import javax.xml.namespace.QName;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
 import org.ebayopensource.turmeric.runtime.binding.BindingConstants;
 import org.ebayopensource.turmeric.runtime.binding.impl.parser.json.JSONStreamWriter;
 import org.ebayopensource.turmeric.runtime.binding.schema.DataElementSchema;
 import org.ebayopensource.turmeric.runtime.binding.utils.BindingUtils;
 import org.ebayopensource.turmeric.runtime.common.binding.DataBindingDesc;
 import org.ebayopensource.turmeric.runtime.common.binding.Deserializer;
 import org.ebayopensource.turmeric.runtime.common.binding.DeserializerFactory;
 import org.ebayopensource.turmeric.runtime.common.binding.Serializer;
 import org.ebayopensource.turmeric.runtime.common.binding.SerializerFactory;
 import org.ebayopensource.turmeric.runtime.common.binding.TypeConverter;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceCreationException;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceNotFoundException;
 import org.ebayopensource.turmeric.runtime.common.impl.binding.jaxb.fastinfoset.JAXBFastInfosetDeserializerFactory;
 import org.ebayopensource.turmeric.runtime.common.impl.binding.jaxb.fastinfoset.JAXBFastInfosetSerializerFactory;
 import org.ebayopensource.turmeric.runtime.common.impl.binding.jaxb.json.JAXBJSONDeserializerFactory;
 import org.ebayopensource.turmeric.runtime.common.impl.binding.jaxb.json.JAXBJSONSerializerFactory;
 import org.ebayopensource.turmeric.runtime.common.impl.binding.jaxb.nv.JAXBNVDeserializerFactory;
 import org.ebayopensource.turmeric.runtime.common.impl.binding.jaxb.nv.JAXBNVSerializerFactory;
 import org.ebayopensource.turmeric.runtime.common.impl.binding.jaxb.validation.SchemaBaseDetails;
 import org.ebayopensource.turmeric.runtime.common.impl.binding.jaxb.validation.SchemaExtractor;
 import org.ebayopensource.turmeric.runtime.common.impl.binding.jaxb.xml.JAXBXMLDeserializerFactory;
 import org.ebayopensource.turmeric.runtime.common.impl.binding.jaxb.xml.JAXBXMLSerializerFactory;
 import org.ebayopensource.turmeric.runtime.common.impl.binding.protobuf.ProtobufDeserializerFactory;
 import org.ebayopensource.turmeric.runtime.common.impl.binding.protobuf.ProtobufSerializerFactory;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.CommonConfigHolder;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.CustomSerializerConfig;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.FrameworkHandlerConfig;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.HandlerConfig;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.MessageHeaderConfig;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.MessageProcessorConfigHolder;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.MessageTypeConfig;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.NameValue;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.OperationConfig;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.OperationPropertyConfigHolder;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.OptionList;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.ProtocolProcessorConfig;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.SerializerConfig;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.TypeConverterConfig;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.TypeMappingConfigHolder;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.monitoring.MetricsConfigManager;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.pipeline.LoggingHandlerInitContextImpl;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.pipeline.PipelineInitContextImpl;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.pipeline.ProtocolProcessorInitContextImpl;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.schema.BaseTypeDefsBuilder;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.schema.DataElementSchemaLoader;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.utils.ServiceNameUtils;
 import org.ebayopensource.turmeric.runtime.common.impl.pipeline.PipelineImpl;
 import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
 import org.ebayopensource.turmeric.runtime.common.impl.utils.ReflectionUtils;
 import org.ebayopensource.turmeric.runtime.common.pipeline.HandlerOptions;
 import org.ebayopensource.turmeric.runtime.common.pipeline.LoggingHandler;
 import org.ebayopensource.turmeric.runtime.common.pipeline.Pipeline;
 import org.ebayopensource.turmeric.runtime.common.pipeline.PipelineMode;
 import org.ebayopensource.turmeric.runtime.common.pipeline.ProtocolProcessor;
 import org.ebayopensource.turmeric.runtime.common.service.HeaderMappingsDesc;
 import org.ebayopensource.turmeric.runtime.common.service.ServiceContext;
 import org.ebayopensource.turmeric.runtime.common.service.ServiceId;
 import org.ebayopensource.turmeric.runtime.common.service.ServiceOperationDesc;
 import org.ebayopensource.turmeric.runtime.common.service.ServiceOperationParamDesc;
 import org.ebayopensource.turmeric.runtime.common.service.ServiceTypeMappings;
 import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
 import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
 import org.ebayopensource.turmeric.runtime.errorlibrary.ErrorConstants;
 
 import com.ebay.kernel.component.Registration;
 
 /**
  * Base class implementing common functions for both client and server ServiceDesc factories
  *
  * @author ichernyshev
  */
 public abstract class BaseServiceDescFactory<T extends ServiceDesc> {
 	private final String m_factoryName;
 	private final boolean m_isClientSide;
 	private final Map<ServiceId,T> m_descs = new HashMap<ServiceId,T>();
 	private final Map<String,QName> m_adminToQName = new HashMap<String,QName>();
 	private final Map<QName,String> m_qNameToAdmin = new HashMap<QName,String>();
 	private final Map<ServiceId,Throwable> m_failedNames = new HashMap<ServiceId,Throwable>();
 	private final boolean m_requiresLoadAll;
 	private boolean m_hasInitStarted;
 	private boolean m_wasInitSuccessful;
 	private List<Throwable> m_initExceptions = new ArrayList<Throwable>();
 	protected final Map<String,Boolean> m_rawModes = new ConcurrentHashMap<String,Boolean>();
 
 
 	private static BaseServiceDescFactory s_clientInstance;
 	private static BaseServiceDescFactory s_serverInstance;
 
 	protected BaseServiceDescFactory(String factoryName, boolean isClientSide, boolean requiresLoadAll) {
 		m_factoryName = factoryName;
 		m_isClientSide = isClientSide;
 		m_requiresLoadAll = requiresLoadAll;
 	}
 
 	public static BaseServiceDescFactory getClientInstance() {
 		if (s_clientInstance == null) {
 			throw new IllegalStateException("Client instance for BaseServiceDescFactory is not set");
 		}
 
 		return s_clientInstance;
 	}
 
 	public static BaseServiceDescFactory getServerInstance() {
 		if (s_serverInstance == null) {
 			throw new IllegalStateException("Server instance for BaseServiceDescFactory is not set");
 		}
 
 		return s_serverInstance;
 	}
 
 	protected static synchronized void setClientInstance(BaseServiceDescFactory value) {
 		if (value == null) {
 			throw new NullPointerException();
 		}
 
 		if (s_clientInstance != null) {
 			throw new IllegalStateException("Client instance for BaseServiceDescFactory is already set");
 		}
 
 		s_clientInstance = value;
 	}
 
 	protected static synchronized void setServerInstance(BaseServiceDescFactory value) {
 		if (value == null) {
 			throw new NullPointerException();
 		}
 
 		if (s_serverInstance != null) {
 			throw new IllegalStateException("Server instance for BaseServiceDescFactory is already set");
 		}
 
 		s_serverInstance = value;
 	}
 
 	public boolean isInRawMode(String adminName) {
 		return m_rawModes.containsKey(adminName);
 	}
 
 	public abstract ServiceContext getServiceContext(T desc) throws ServiceException;
 
 	/**
 	 * Returns a ServiceDesc, attempting to load if necessary
 	 */
 	protected final T getServiceDesc(ServiceId id) throws ServiceException {
 		return getServiceDesc(id, true, true, false);
 	}
 
 	protected final T getServiceDesc(ServiceId id, boolean rawMode) throws ServiceException {
 		return getServiceDesc(id, true, true, rawMode);
 	}
 	
 	protected final T getServiceDesc(ServiceId id, boolean rawMode, int useDefaultClientConfig) throws ServiceException {
 		boolean isZeroCC = useDefaultClientConfig == 1? true: false;
 		return getServiceDesc(id, true, true, rawMode, isZeroCC);
 	}
 
 	protected final T getServiceDesc(T serviceDesc, ServiceId id,
 			QName srvName, Definition wsdlDefinition, boolean rawMode)
 			throws ServiceException {
 		return getServiceDesc(serviceDesc, id, true, srvName, wsdlDefinition,
 				rawMode);
 	}
 
 
 	public final String findKnownAdminNameByQName(QName serviceQName) {
 		String result;
 		synchronized (m_descs) {
 			result = m_qNameToAdmin.get(serviceQName);
 		}
 		return result;
 	}
 
 	public final QName findKnownQNameByAdminName(String adminName) {
 		QName result;
 		synchronized (m_descs) {
 			result = m_adminToQName.get(adminName);
 		}
 		return result;
 	}
 
 	public final Collection<T> getKnownServiceDescsByAdminName(String adminName) {
 		Collection<T> result;
 		synchronized (m_descs) {
 			result = new ArrayList<T>(m_descs.size());
 
 			for (T svcDesc: m_descs.values()) {
 				if (svcDesc.getAdminName().equals(adminName)) {
 					result.add(svcDesc);
 				}
 			}
 		}
 		return result;
 	}
 
 	public final Collection<T> getKnownServiceDescs() {
 		synchronized (m_descs) {
 			return Collections.unmodifiableCollection(new ArrayList<T>(m_descs.values()));
 		}
 	}
 
 	final Map<ServiceId,Throwable> getFailedIds() {
 		synchronized (m_descs) {
 			return Collections.unmodifiableMap(new HashMap<ServiceId,Throwable>(m_failedNames));
 		}
 	}
 
 	private final T getServiceDesc(ServiceId id, boolean loadAllFirst, boolean logErrors)
 		throws ServiceException
 	{
 		return getServiceDesc(id, loadAllFirst, logErrors, isInRawMode(id.getAdminName()));
 	}
 
 	protected final void clearFromFailedCache(ServiceId id) {
 		synchronized (m_descs) {
 			m_failedNames.remove(id);
 		}
 	}
 	private T getServiceDesc(T serviceDesc, ServiceId id, boolean logErrors,
 			QName srvName, Definition wsdlDefinition, boolean rawMode)
 			throws ServiceException {
 		T result;
 		QName oldQName;
 		synchronized (m_descs) {
 			Throwable th = m_failedNames.get(id);
 			if (th != null) {
 				throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_SERVICE_INIT_FAILED,
 						ErrorConstants.ERRORDOMAIN, new Object[] {id.toString(), m_factoryName, th.toString()}), th);
 			}
 
 			result = m_descs.get(id);
 			oldQName = m_adminToQName.get(id.getAdminName());
 		}
 
 		if (result != null) {
 			return result;
 		}
 		try {
 			validateServiceName(id.getAdminName());
 			result = createServiceDesc(serviceDesc, id, srvName,
 					wsdlDefinition, rawMode);
 
 			if (oldQName != null && !oldQName.equals(result.getServiceQName())) {
 				throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INCONSISTENT_QNAME,
 						ErrorConstants.ERRORDOMAIN, new Object[] {id.toString(), result.getServiceQName(), oldQName }));
 			}
 		} catch (ExceptionInInitializerError e) {
 			Throwable t = e.getException();
 			handleServiceLoadError(id, t, logErrors);
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_CANNOT_CREATE_SVC,
 					ErrorConstants.ERRORDOMAIN, new Object[] {m_factoryName, id.toString(), t.toString()}), t);
 
 		} catch (Throwable t) {
 			handleServiceLoadError(id, t, logErrors);
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_CANNOT_CREATE_SVC,
 					ErrorConstants.ERRORDOMAIN, new Object[] {m_factoryName, id.toString(), t.toString()}), t);
 
 		}
 
 		boolean hasAdded = false;
 		synchronized (m_descs) {
 			T result2 = m_descs.get(id);
 			if (result2 != null) {
 				// someone else has created a ServiceDesc, use older one
 				result = result2;
 			} else {
 				m_descs.put(id, result);
 
 				if (oldQName == null) {
 					m_adminToQName.put(id.getAdminName(), result
 							.getServiceQName());
 					m_qNameToAdmin.put(result.getServiceQName(), id
 							.getAdminName());
 				}
 
 				hasAdded = true;
 			}
 			m_failedNames.remove(id);
 		}
 
 		if (hasAdded) {
 			updateMetrics(result);
 			postServiceDescLoad(result);
 		}
 
 		return result;
 	}
 
 
 	private final T getServiceDesc(ServiceId id, boolean loadAllFirst, boolean logErrors, boolean rawMode)
 		throws ServiceException
 	{
 		return getServiceDesc(id,  loadAllFirst,  logErrors,  rawMode, false);
 	}
 	
 	private final T getServiceDesc(ServiceId id, boolean loadAllFirst, boolean logErrors, boolean rawMode, boolean useDefaultConfig)
 	throws ServiceException
 	{
 		if (rawMode && !isInRawMode(id.getAdminName())) m_rawModes.put(id.getAdminName(), Boolean.TRUE);
 
 		boolean wasInitSuccessful;
 		if (loadAllFirst) {
 			wasInitSuccessful = loadAllServices();
 		} else {
 			wasInitSuccessful = true;
 		}
 
 		T result;
 		QName oldQName;
 		synchronized (m_descs) {
 			Throwable th = m_failedNames.get(id);
 			if (th != null) {
 				throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_SERVICE_INIT_FAILED,
 						ErrorConstants.ERRORDOMAIN, new Object[] {id.toString(), m_factoryName, th.toString()}), th);
 			}
 
 			result = m_descs.get(id);
 			oldQName = m_adminToQName.get(id.getAdminName());
 		}
 
 		if (result != null) {
 			return result;
 		}
 
 		if (!wasInitSuccessful) {
 			// we tried to load all services, but failed,
 			// do not try to load any more
 			List<Throwable> exceptions = m_initExceptions;
 			Throwable origException = null;
 			String exceptionText = "(See log for details)";
 			if (exceptions != null && !exceptions.isEmpty()) {
 				origException = exceptions.get(0);
 				exceptionText = origException.getMessage();
 			}
 
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INIT_FAILED,
 					ErrorConstants.ERRORDOMAIN, new Object[] {id.toString(), m_factoryName, exceptionText}), origException);
 		}
 
 		try {
 			validateServiceName(id.getAdminName());
 			id.setUseDefaultConfig(useDefaultConfig);
 			result = createServiceDesc(id, rawMode);
 
 			if (oldQName != null && !oldQName.equals(result.getServiceQName())) {
 				throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INCONSISTENT_QNAME,
 						ErrorConstants.ERRORDOMAIN, new Object[] {id.toString(), result.getServiceQName(), oldQName}));
 			}
 		} catch (ExceptionInInitializerError e) {
 			Throwable t = e.getException();
 			handleServiceLoadError(id, t, logErrors);
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_CANNOT_CREATE_SVC,
 					ErrorConstants.ERRORDOMAIN, new Object[] {m_factoryName, id.toString(), t.toString()}), t);
 		} catch (Throwable t) {
 			handleServiceLoadError(id, t, logErrors);
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_CANNOT_CREATE_SVC,
 					ErrorConstants.ERRORDOMAIN, new Object[] {m_factoryName, id.toString(), t.toString()}), t);
 		}
 
 		boolean hasAdded = false;
 		synchronized (m_descs) {
 			T result2 = m_descs.get(id);
 			if (result2 != null) {
 				// someone else has created a ServiceDesc, use older one
 				result = result2;
 			} else {
 				m_descs.put(id, result);
 
 				if (oldQName == null) {
 					m_adminToQName.put(id.getAdminName(), result.getServiceQName());
 					m_qNameToAdmin.put(result.getServiceQName(), id.getAdminName());
 				}
 
 				hasAdded = true;
 			}
 			m_failedNames.remove(id);
 		}
 
 		if (hasAdded) {
 			updateMetrics(result);
 			postServiceDescLoad(result);
 		}
 
 		return result;
 	}
 
 	private void handleServiceLoadError(ServiceId id, Throwable e, boolean logErrors) {
 		synchronized (m_descs) {
 			m_initExceptions.add(e);
 			m_failedNames.put(id, e);
 		}
 
 		if (logErrors) {
 			Level logLevel;
 			if (e instanceof ServiceNotFoundException) {
 				logLevel = Level.INFO;
 			} else {
 				logLevel = Level.SEVERE;
 			}
 
 			LogManager.getInstance(BaseServiceDescFactory.class).log(logLevel,
 				"Service " + id + " could not be loaded due to exception: " + e.toString(), e);
 		}
 	}
 
 	protected abstract void postServiceDescLoad(T svcDesc);
 
 	/**
 	 * Reloads ServiceDesc to reflect configuration change
 	 */
 	protected final void reloadServiceDesc(ServiceId id) throws ServiceException
 	{
 		validateServiceName(id.getAdminName());
 		T serviceDesc = createServiceDesc(id);
 
 		synchronized (m_descs) {
 			m_failedNames.remove(id);
 			m_descs.put(id, serviceDesc);
 		}
 
 		postServiceDescLoad(serviceDesc);
 	}
 
 	public final synchronized void resetFactoryForUnitTest() throws ServiceException {
 		m_hasInitStarted = false;
 		m_wasInitSuccessful = false;
 
 		synchronized (m_descs) {
 			m_failedNames.clear();
 			m_descs.clear();
 			m_initExceptions.clear();
 			m_adminToQName.clear();
 			m_qNameToAdmin.clear();
 		}
 	}
 
 	protected abstract Collection<ServiceId> loadAllServiceNames() throws ServiceException;
 
 	public final synchronized boolean loadAllServices() throws ServiceException {
 		if (m_hasInitStarted) {
 			return m_wasInitSuccessful;
 		}
 
 		m_hasInitStarted = true;
 
 		boolean allSuccess = true;
 		try {
 			Collection<ServiceId> serviceNames = loadAllServiceNames();
 
 			for (ServiceId id: serviceNames) {
 				try {
 					validateServiceName(id.getAdminName());
 					getServiceDesc(id, false, false);
 				} catch (ServiceException e) {
 					if (m_requiresLoadAll) {
 						throw e;
 					}
 
 					allSuccess = false;
 					LogManager.getInstance(this.getClass()).log(Level.SEVERE,
 						"Skipping service " + id + " due to error: " + e.toString(), e);
 				}
 			}
 		} catch (Throwable e) {
 			allSuccess = false;
 			LogManager.getInstance(this.getClass()).log(Level.SEVERE,
 				"Unable to initialize " + m_factoryName + ": " + e.toString(), e);
 			synchronized (m_descs) {
 				m_initExceptions.add(e);
 			}
 		}
 
 		m_wasInitSuccessful = allSuccess;
 		return m_wasInitSuccessful;
 	}
 
 	public Throwable getFirstInitException() {
 		if (!m_initExceptions.isEmpty()) {
 			return m_initExceptions.get(0);
 		}
 
 		return null;
 	}
 
 	protected abstract T createServiceDesc(ServiceId id) throws ServiceException;
 
 	protected abstract T createServiceDesc(ServiceId id, boolean rawMode) throws ServiceException;
 
 	protected abstract T createServiceDesc(ServiceDesc commonDesc,
 			ServiceId newServiceId, QName srvQName, Definition definition,
 			boolean rawMode) throws ServiceException;
 
 	protected void validateServiceName(String name) throws ServiceException {
 		if (name == null) {
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_SVC_NAME,
 					ErrorConstants.ERRORDOMAIN, new Object[] { "***null***" }));
 		}
 	}
 
 	private void validateOperationName(String name) throws ServiceException {
 		if (name == null) {
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_OPERATION_NAME,
 					ErrorConstants.ERRORDOMAIN, new Object[] { "***null***" }));
 		}
 	}
 
 	private void validateProtocolProcessorName(String name) throws ServiceException {
 		if (name == null) {
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_PROTOCOL_PROCESSOR_NAME,
 					ErrorConstants.ERRORDOMAIN, new Object[] { "***null***" }));
 		}
 	}
 
 	private void validateDataBindingNameAndType(String name, String mimeType) throws ServiceException {
 		if (name == null) {
 			throw new ServiceCreationException(
 					ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_DATA_BINDING_NAME,
 					ErrorConstants.ERRORDOMAIN, new Object[] { "***null***" }));
 		}
 		if (mimeType == null) {
 			throw new ServiceCreationException(
 					ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_DATA_BINDING_MIME_TYPE,
 							ErrorConstants.ERRORDOMAIN, new Object[] { "***null***" }));
 		}
 	}
 
 	protected final Pipeline createPipeline(ServiceId svcId, PipelineMode mode,
 		MessageProcessorConfigHolder processorConfig, ClassLoader cl)
 		throws ServiceException
 	{
 		String pipelineClassName;
 		List<HandlerConfig> handlerConfigs;
 		if (mode == PipelineMode.REQUEST) {
 			pipelineClassName = processorConfig.getRequestPipelineClassName();
 			handlerConfigs = processorConfig.getRequestHandlers();
 		} else {
 			pipelineClassName = processorConfig.getResponsePipelineClassName();
 			handlerConfigs = processorConfig.getResponseHandlers();
 		}
 
 		if (pipelineClassName == null) {
 			pipelineClassName = PipelineImpl.class.getName();
 		}
 
 		if (handlerConfigs == null) {
 			handlerConfigs = new ArrayList<HandlerConfig>();
 		}
 
 		List<HandlerOptions> handlerOptionsList = new ArrayList<HandlerOptions>(handlerConfigs.size());
 		for (HandlerConfig config: handlerConfigs) {
 			String handlerName = config.getName();
 			String handlerClassName = config.getClassName();
 
 			List<NameValue> options = null;
 			OptionList optionList = config.getOptions();
 			if (optionList != null) {
 				options = optionList.getOption();
 			}
 
 			Map<String,String> options2 = new HashMap<String,String>();
 			if (options != null && !options.isEmpty()) {
 				for (int j=0; j<options.size(); j++) {
 					NameValue nv = options.get(j);
 					options2.put(nv.getName(), nv.getValue());
 				}
 			}
 
 			Boolean continueOnError = config.isContinueOnError();
 			if (continueOnError == null) {
 				continueOnError = Boolean.FALSE;
 			}
 
 			Boolean runOnError = config.isRunOnError();
 			if (runOnError == null) {
 				runOnError = Boolean.FALSE;
 			}
 
 			HandlerOptions handlerOptions = new HandlerOptions(handlerName, handlerClassName,
 				options2, continueOnError.booleanValue(), runOnError.booleanValue());
 
 			handlerOptionsList.add(handlerOptions);
 		}
 
 		Pipeline result = ReflectionUtils.createInstance(pipelineClassName, Pipeline.class, cl);
 
 		PipelineInitContextImpl initCtx = new PipelineInitContextImpl(svcId, mode, cl, handlerOptionsList);
 		result.init(initCtx);
 		initCtx.kill();
 
 		return result;
 	}
 
 	protected final ServiceTypeMappings createFallbackTypeMappings()
 		throws ServiceException
 	{
 		Map<String,String> packageToNamespace = new HashMap<String,String>();
 
 		validateAndAddPkgNsMapping(BindingUtils.getPackageName(ErrorMessage.class),
 				BindingConstants.SOA_TYPES_NAMESPACE, packageToNamespace);
 
 		return new ServiceTypeMappingsImpl(packageToNamespace, null);
 	}
 
 	protected final ServiceTypeMappings createTypeMappings(ServiceId svcId,
 		TypeMappingConfigHolder mappings, Class intfClass, ClassLoader cl)
 		throws ServiceException
 	{
 		Map<String,String> packageToNamespace = new HashMap<String,String>();
 
 		Map<String,List<String>> pkgToNsCfg = (mappings != null ? mappings.getPackageToNsMap() : null);
 		if (pkgToNsCfg != null) {
 			for (Map.Entry<String,List<String>> e: pkgToNsCfg.entrySet()) {
 				String pkgName = e.getKey();
 				List<String> nsList = e.getValue();
 
 				if (pkgName == null) {
 					throw new ServiceCreationException(
 							ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_TYPEMAP_DEFINITION,
 							ErrorConstants.ERRORDOMAIN));
 				}
 
 				for (String ns: nsList) {
 					validateAndAddPkgNsMapping(pkgName, ns, packageToNamespace);
 				}
 			}
 		}
 
 		BaseTypeDefsBuilder typeDefsBuilder;
 		if (intfClass != null) {
 			String typeDefsBuilderClassName = ServiceNameUtils.getServiceTypeDefsBuilderClassName(
 				svcId.getAdminName(), intfClass.getName());
 			Class<BaseTypeDefsBuilder> typeDefsBuilderClass = ReflectionUtils.loadClass(
 				typeDefsBuilderClassName, BaseTypeDefsBuilder.class, true, cl);
 
 			if (typeDefsBuilderClass != null) {
 				typeDefsBuilder = ReflectionUtils.createInstance(typeDefsBuilderClass);
 				typeDefsBuilder.build();
 			} else {
 				typeDefsBuilder = null;
 				LogManager.getInstance(this.getClass()).log(Level.WARNING,
 					"Unable to find Type Defs Builder class '" + typeDefsBuilderClassName +
 					"' for '" + svcId.getAdminName() + "'. Will continue loading without " +
 					"XML Schema information");
 			}
 		} else {
 			// TODO: get TypeDefs builder class name from somewhere else
 			typeDefsBuilder = null;
 		}
 
 		return new ServiceTypeMappingsImpl(packageToNamespace, typeDefsBuilder, mappings.getEnableNamespaceFolding());
 	}
 
 	private void validateAndAddPkgNsMapping(String pkgName, String ns,
 		Map<String,String> packageToNamespace) throws ServiceException
 	{
 		if (ns == null) {
 			// use empty string when there is no namespace
 			ns = "";
 		}
 
 		boolean alreadyHasPkgName = false;
 		for (Map.Entry<String,String> e: packageToNamespace.entrySet()) {
 			String otherPkg = e.getKey();
 			String otherNs = e.getValue();
 
 			if (arePackagesOverlapping(pkgName, otherPkg) && !otherNs.equals(ns)) {
 				throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_PACKAGE_NS_REDEFINITION,
 						ErrorConstants.ERRORDOMAIN, new Object[] {pkgName, otherPkg}));
 			}
 
 			if (pkgName.equals(otherPkg)) {
 				alreadyHasPkgName = true;
 			}
 		}
 
 		if (!alreadyHasPkgName) {
 			packageToNamespace.put(pkgName, ns);
 		}
 	}
 
 	private boolean arePackagesOverlapping(String pkg1, String pkg2) {
 		if (pkg1.length() == 0 || pkg2.length() == 0 || pkg1.equals(pkg2)) {
 			return true;
 		}
 		/*  commnenting for TrackingAPI WSDL issue
 		if (pkg1.startsWith(pkg2 + ".") || pkg2.startsWith(pkg1 + ".")) {
 			return true;
 		}
 		*/
 
 		return false;
 	}
 
 	private void validateAndAddXmlNameMapping(List<DataElementSchema> rootElements,
 		List<Class> javaTypeList, Map<QName,Class> xmlToJavaMappings) throws ServiceException
 	{
 		if (rootElements == null) {
 			return;
 		}
 
 		for (int i=0; i<rootElements.size(); i++) {
 			DataElementSchema elementSchema = rootElements.get(i);
 			Class javaType = javaTypeList.get(i);
 			QName xmlName = elementSchema.getElementName();
 			validateAndAddXmlNameMapping(xmlName, javaType, xmlToJavaMappings);
 		}
 	}
 
 	private void validateAndAddXmlNameMapping(QName xmlName, Class javaType,
 		Map<QName,Class> xmlToJavaMappings)
 		throws ServiceException
 	{
 		if (xmlToJavaMappings.containsKey(xmlName)) {
 			Class oldJavaType = xmlToJavaMappings.get(xmlName);
 			if (oldJavaType != javaType) {
 				throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_TYPEMAP_REDEFINITION,
 						ErrorConstants.ERRORDOMAIN, new Object[] { xmlName.toString() } ));
 			}
 
 			return;
 		}
 
 		// TODO - this seems wrong. Why wouldn't we be able to have more than one instance of the same
 		// Java type as a parameter to an operation?
 /*
  *
  		if (xmlToJavaMappings.containsValue(javaType)) {
 			QName oldKey = findMapKey(xmlToJavaMappings, javaType);
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_TYPEMAP_REDEFINITION,
 					ErrorConstants.ERRORDOMAIN, new Object[] { String.valueOf(oldKey) }));
 			
 		}
 */
 		xmlToJavaMappings.put(xmlName, javaType);
 	}
 
 
 	@SuppressWarnings("unused")
 	private <K> K findMapKey(Map<K,?> map, Object value) {
 		for (Map.Entry<K,?> e: map.entrySet()) {
 			Object value2 = e.getValue();
 			if (BindingUtils.sameObject(value, value2)) {
 				return e.getKey();
 			}
 		}
 		return null;
 	}
 
 	protected final ServiceOperationParamDesc createOperationParamDesc(ServiceId svcId,
 		MessageTypeConfig cfg, String ns, ServiceTypeMappings typeMappings,
 		String opName, boolean isErrorType, ClassLoader cl)
 		throws ServiceException
 	{
 		List<Class> rootJavaTypes = new ArrayList<Class>();
 		List<DataElementSchema> rootElements = new ArrayList<DataElementSchema>();
 		Map<QName,Class> xmlToJavaMappings = new HashMap<QName,Class>();
 
 		if (cfg == null) {
 			if (isErrorType) {
 				DataElementSchema elementSchema = DataElementSchemaLoader.getErrorMessageSchema();
 				rootJavaTypes.add(ErrorMessage.class);
 				rootElements.add(elementSchema);
 				xmlToJavaMappings.put(SOAConstants.ERROR_MESSAGE_ELEMENT_NAME, ErrorMessage.class);
 			}
 
 			return new ServiceOperationParamDescImpl(rootJavaTypes, rootElements, xmlToJavaMappings, false);
 		}
 
 
 		Class clazz = loadClassForJavaType(opName, cfg.getJavaTypeName(), cl);
 		rootJavaTypes.add(clazz);
 
 		DataElementSchema elementSchema = loadElementSchema(svcId, opName, ns, typeMappings, clazz, cfg.getXmlElementName(), cfg.getXmlTypeName(), cl);
 		rootElements.add(elementSchema);
 
 		validateAndAddXmlNameMapping(rootElements, rootJavaTypes, xmlToJavaMappings);
 
 		boolean hasAttachments = cfg.hasAttachment();
 
 		return new ServiceOperationParamDescImpl(rootJavaTypes, rootElements, xmlToJavaMappings, hasAttachments);
 	}
 
 	protected final ServiceOperationParamDesc createOperationHeaders(ServiceId svcId,
 			List<MessageHeaderConfig> cfgList, QName svcQName, ServiceTypeMappings typeMappings,
 			String opName, ClassLoader cl,String ns)
 			throws ServiceException
 		{
 			List<Class> rootJavaTypes = new ArrayList<Class>();
 			List<DataElementSchema> rootElements = new ArrayList<DataElementSchema>();
 			Map<QName,Class> xmlToJavaMappings = new HashMap<QName,Class>();
 
 			if (cfgList == null) {
 				return new ServiceOperationParamDescImpl(rootJavaTypes, rootElements, xmlToJavaMappings, false);
 			}
 
 			for (MessageHeaderConfig cfg : cfgList) {
 				Class clazz = loadClassForJavaType(opName, cfg.getJavaTypeName(), cl);
 				rootJavaTypes.add(clazz);
 
 				// Raghu, added 1/12/2010
 				/*
 				 *  Bug fix: 650886. The pre SOA2.4 Typemappings did not have namespace
 				 *  associated with the element. The NS was defaulted to service namespace
 				 *  in the loadElementSchema() earlier. The defaulting code was removed as
 				 *  part of code changes for supporting root types from non service ns.
 				 *  The code here is to support the pre SOA2.4 Typemappings.
 				 */
 				QName xmlElementName = cfg.getXmlElementName();
 				if (XMLConstants.NULL_NS_URI.equals(xmlElementName.getNamespaceURI())) {
 					xmlElementName = new QName(ns, xmlElementName.getLocalPart());
 				}
 
 				DataElementSchema elementSchema = loadElementSchema(svcId, opName, ns, typeMappings, clazz, xmlElementName, cfg.getXmlTypeName(), cl);
 				rootElements.add(elementSchema);
 			}
 
 			validateAndAddXmlNameMapping(rootElements, rootJavaTypes, xmlToJavaMappings);
 
 			return new ServiceOperationParamDescImpl(rootJavaTypes, rootElements, xmlToJavaMappings, false);
 		}
 
 	private Class loadClassForJavaType(String opName, String className, ClassLoader cl) throws ServiceException {
 		// load the Java types
 		if (className == null) {
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_METHOD_PARAM_ARGS,
 					ErrorConstants.ERRORDOMAIN, new Object[] {opName}));
 		}
 		Class clazz = ReflectionUtils.loadClass(className, null, cl);
 		return clazz;
 	}
 
 	private DataElementSchema loadElementSchema(ServiceId svcId, String opName, String ns, ServiceTypeMappings typeMappings, Class clazz, QName xmlName, QName xmlType, ClassLoader cl) throws ServiceException {
 		String typeNS = typeMappings.getNsForJavaType(clazz);
 		if (typeNS == null) {
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_NO_NAMESPACE_FOR_CLASS,
 					ErrorConstants.ERRORDOMAIN, new Object[] {clazz.getName()}));
 		}
 
 		// load the XML element names
 		if (xmlName == null) {
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_METHOD_PARAM_ARGS,
 					ErrorConstants.ERRORDOMAIN, new Object[] {opName}));
 		}
 		//xmlName = new QName(ns, xmlName.getLocalPart());
 
 		// load the XML element names
 		if (xmlType == null) {
 			throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_METHOD_PARAM_ARGS,
 					ErrorConstants.ERRORDOMAIN, new Object[] {opName}));
 		}
 		xmlType = new QName(typeNS, xmlType.getLocalPart());
 
 		ServiceTypeMappingsImpl typeMappingsImpl = (ServiceTypeMappingsImpl)typeMappings;
 		BaseTypeDefsBuilder typeDefsBuilder = typeMappingsImpl.getTypeDefsBuilder();
 		DataElementSchema elementSchema = DataElementSchemaLoader.getInstance().loadSchema(
 			svcId, xmlName, xmlType, typeDefsBuilder, cl);
 		return elementSchema;
 	}
 
 	protected final Map<String,ServiceOperationDesc> createOperations(ServiceId svcId,
 		TypeMappingConfigHolder mappings, Collection<String> unsupportedOpNames,
 		ServiceTypeMappings typeMappings, QName svcQName, ClassLoader cl,
 		OperationPropertyConfigHolder propertyConfig
 		)
 		throws ServiceException
 	{
 		Collection<OperationConfig> opConfigs = mappings.getOperations();
 		Map<String,ServiceOperationDesc> result = new HashMap<String,ServiceOperationDesc>();
 		for (OperationConfig opConfig: opConfigs) {
 			String name = opConfig.getName();
 			String methodName = opConfig.getMethodName();
 			validateOperationName(name);
 
 			String svcNamespace = svcQName.getNamespaceURI();
 			ServiceOperationParamDesc requestParamDesc = createOperationParamDesc(svcId,
 				opConfig.getRequestMessage(), svcNamespace, typeMappings, name, false, cl);
 			ServiceOperationParamDesc responseParamDesc = createOperationParamDesc(svcId,
 				opConfig.getResponseMessage(), svcNamespace, typeMappings, name, false, cl);
 			ServiceOperationParamDesc errorParamDesc = createOperationParamDesc(svcId,
 				//opConfig.getErrorMessage(), null, typeMappings, name, true, cl);
 				opConfig.getErrorMessage(), svcNamespace, typeMappings, name, true, cl);
 			ServiceOperationParamDesc requestHeaderParamDesc = createOperationHeaders(svcId,
 				opConfig.getRequestHeader(), svcQName, typeMappings, name, cl,svcNamespace);
 			ServiceOperationParamDesc responseHeaderParamDesc = createOperationHeaders(svcId,
 					opConfig.getResponseHeader(), svcQName, typeMappings, name, cl,svcNamespace);
 
 			boolean isSupported = true;
 			if (unsupportedOpNames != null && unsupportedOpNames.contains(name)) {
 				isSupported = false;
 			}
 
 			// operation property map from operation.properties file - currently server
 			// side only, and string values only.
 			Map<String,Object> propMap;
 			if (propertyConfig != null) {
 				Map<String,String> opMap = propertyConfig.getOperationPropertyMap(name);
 				if (opMap != null) {
 					propMap = new HashMap<String,Object>();
 					propMap.putAll(opMap);
 				} else {
 					propMap = null;
 				}
 			} else {
 				propMap = null;
 			}
 			ServiceOperationDesc operation = new ServiceOperationDescImpl(svcId, name, methodName,
 				requestParamDesc, responseParamDesc, errorParamDesc, requestHeaderParamDesc,
 				responseHeaderParamDesc, propMap, true, isSupported);
 
 			result.put(name, operation);
 		}
 
 		return result;
 	}
 
 	protected final Map<String,ProtocolProcessorDesc> createProtocolProcessors(
 		ServiceId svcId, List<ProtocolProcessorConfig> processorConfigs, ClassLoader cl)
 		throws ServiceException
 	{
 		Map<String,ProtocolProcessorDesc> result = new HashMap<String,ProtocolProcessorDesc>();
 
 		for (int i=0; i<processorConfigs.size(); i++) {
 			ProtocolProcessorConfig config = processorConfigs.get(i);
 
 			String name = config.getName();
 			validateProtocolProcessorName(name);
 			name = name.toUpperCase();
 
 			String className = config.getClassName();
 			String version = config.getVersion();
 
 			if (className == null) {
 				throw new ServiceCreationException(
 						ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_PROTOCOL_PROCESSOR_DEFINITION,
 								ErrorConstants.ERRORDOMAIN, new Object[] { name } ));
 			}
 
 			ProtocolProcessor processor = ReflectionUtils.createInstance(
 				className, ProtocolProcessor.class, cl);
 
 			ProtocolProcessorInitContextImpl initCtx =
 				new ProtocolProcessorInitContextImpl(svcId, name, version);
 			processor.init(initCtx);
 			initCtx.kill();
 
 			Set<String> formats2;
 			Collection<String> dataFormats = processor.getSupportedDataFormats();
 			if (dataFormats != null && !dataFormats.isEmpty()) {
 				formats2 = new HashSet<String>();
 				for (String format: dataFormats) {
 					formats2.add(format.toUpperCase());
 				}
 			} else {
 				formats2 = null;
 			}
 
 			result.put(name, new ProtocolProcessorDesc(name, processor, formats2));
 		}
 
 		return result;
 	}
 
 	protected final Map<String,DataBindingDesc> createDataBindings(ServiceId svcId,
 		MessageProcessorConfigHolder processorConfig,
 		Collection<String> supportedBindingsNames,
 		Set<Class> rootClasses,
 		boolean requireDifferentPayloads, ClassLoader cl, boolean schemaCheck)
 		throws ServiceException
 	{
 
 		Map<String,DataBindingDesc> result = new HashMap<String,DataBindingDesc>();
 		SchemaFactory upaAwareSchemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 		Schema upaAwareMasterSchema = null;
 		Set<String> payloadTypes = null;
 
 
 
 		if (requireDifferentPayloads) {
 			payloadTypes = new HashSet<String>();
 		}
 
 		Map<String,SerializerConfig> bindingConfigs = processorConfig.getDataBindings();
 
 		for (Map.Entry<String,SerializerConfig> e: bindingConfigs.entrySet()) {
 			SerializerConfig config = e.getValue();
 			String name = config.getName();
 			String mimeType = config.getMimeType();
 			validateDataBindingNameAndType(name, mimeType);
 			name = name.toUpperCase();
 
 			String serClassName = config.getSerializerFactoryClassName();
 			String deserClassName = config.getDeserializerFactoryClassName();
 
 			if (serClassName == null || deserClassName == null) {
 				throw new ServiceCreationException(
 						ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_DATA_BINDING_DEFINITION,
 								ErrorConstants.ERRORDOMAIN, new Object[] { name } ));
 			}
 
 			Map<String,String> options = config.getOptions();
 			if (schemaCheck) {
 				boolean validate = false;
 				String payLoadValidation = options.get(BindingConstants.VALIDATE_PAYLOAD);
 				if(payLoadValidation != null && (payLoadValidation.equalsIgnoreCase("true") ||
 												 payLoadValidation.equalsIgnoreCase("false")))
 					validate = Boolean.TRUE;
 				if (validate){
 					SchemaExtractor schemaExtractor = new SchemaExtractor(svcId.getAdminName());
 					SchemaBaseDetails schemaDetails = schemaExtractor.getMasterSchemaFilesDetails();
 					if(upaAwareMasterSchema == null) {
 						try {
 							String masterSchemaLocation = schemaDetails.getFilePathForStrictValidation();
 							upaAwareSchemaFactory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
 							if(masterSchemaLocation != null){
 								File schemaFile = new File(masterSchemaLocation);
 									upaAwareMasterSchema = upaAwareSchemaFactory.newSchema(schemaFile);
 							}
 						} catch (Exception exception) {
 							throw new ServiceCreationException(
 									ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_UNABLE_TO_REGISTER_SCHEMA,
 											ErrorConstants.ERRORDOMAIN, new Object[] { svcId.getCanonicalServiceName() } ), exception);
 						}
 					}
 				}
 			}
 
 			Map<String,TypeConverter<?,?>> typeConvertersByBoundType = new HashMap<String,TypeConverter<?,?>>();
 			Map<String,TypeConverter<?,?>> typeConvertersByValueType = new HashMap<String,TypeConverter<?,?>>();
 
 			buildTypeConverters(name, processorConfig,
 				typeConvertersByBoundType, typeConvertersByValueType, cl);
 
 			Collection<TypeConverter<?,?>> converters = null;
 			if (null != typeConvertersByValueType) {
 				converters = typeConvertersByValueType.values();
 			}
 			Class[] rootClassArray = addDataBindingSpecificTypes(rootClasses, converters);
 			SerializerFactory serFactory = ReflectionUtils.createInstance(
 				serClassName, SerializerFactory.class, cl);
 			initSerializerFactory(serFactory, svcId, options, rootClassArray);
 			//String payloadType = serFactory.getPayloadType().toUpperCase();
 			String payloadType = serFactory.getPayloadType();
 
 			DeserializerFactory deserFactory = ReflectionUtils.createInstance(
 				deserClassName, DeserializerFactory.class, cl);
 			initDeserializerFactory(deserFactory, svcId, options, rootClassArray, upaAwareMasterSchema);
 			String otherPayloadType = deserFactory.getPayloadType().toUpperCase();
 
 			if (!payloadType.equals(otherPayloadType)) {
 				throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_BINDING_PAYLOAD_MISMATCH,
 						ErrorConstants.ERRORDOMAIN, new Object[] {name, payloadType, otherPayloadType}));
 			}
 
 			if (payloadTypes != null) {
 				if (payloadTypes.contains(payloadType)) {
 					throw new ServiceCreationException(
 							ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_DATA_BINDING_PAYLOAD_COLLISION,
 									ErrorConstants.ERRORDOMAIN, new Object[] {payloadType, name}));
 				}
 
 				payloadTypes.add(payloadType);
 			}
 
 			Map<String,Serializer> customSerializers = new HashMap<String,Serializer>();
 			Map<String,Deserializer> customDeserializers = new HashMap<String,Deserializer>();
 
 			buildCustomSerializers(name, processorConfig,
 				customSerializers, customDeserializers, cl);
 
 			DataBindingDesc bindingDesc = new DataBindingDesc(name, mimeType,
 				serFactory, deserFactory,
 				customSerializers, customDeserializers,
 				typeConvertersByBoundType, typeConvertersByValueType);
 
 			result.put(name, bindingDesc);
 		}
 
 		addDefaultDataBindings(svcId, result, payloadTypes, rootClasses, null, false);
 
 		if (supportedBindingsNames != null && !supportedBindingsNames.isEmpty()) {
 			// copy only supported bindings
 			Map<String,DataBindingDesc> result2 = new HashMap<String,DataBindingDesc>();
 			for (String name: supportedBindingsNames) {
 				if (name != null) {
 					name = name.toUpperCase();
 				}
 
 				if (name == null || name.length() == 0) {
 					continue;
 				}
 
 				DataBindingDesc bindingDesc = result.get(name);
 				if (bindingDesc == null) {
 					throw new ServiceCreationException(
 							ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_UNKNOWN_DATA_BINDING,
 									ErrorConstants.ERRORDOMAIN, new Object[] { name } ));
 				}
 
 				result2.put(name, bindingDesc);
 			}
 
 			if (!result2.isEmpty()) {
 				result = result2;
 			}
 		}
 
 		return result;
 	}
 
 	public static Set<Class> getRootClassesFromOperations(Collection<ServiceOperationDesc> operations) {
 		Set<Class> rootClasses = new HashSet<Class>();
 
 		for (ServiceOperationDesc opDesc: operations) {
 			rootClasses.addAll(opDesc.getRequestType().getRootJavaTypes());
 			rootClasses.addAll(opDesc.getResponseType().getRootJavaTypes());
 			rootClasses.addAll(opDesc.getErrorType().getRootJavaTypes());
 			ServiceOperationParamDesc headerDesc = opDesc.getRequestHeaders();
 			if (null != headerDesc) {
 				rootClasses.addAll(headerDesc.getRootJavaTypes());
 			}
 			headerDesc = opDesc.getResponseHeaders();
 			if (null != headerDesc) {
 				rootClasses.addAll(headerDesc.getRootJavaTypes());
 			}
 		}
 
 		// this should cover error message transmission in all fallback cases
 		// e.g. invalid service config, unknown operation name
 		rootClasses.add(ErrorMessage.class);
 
 		return rootClasses;
 	}
 
 	public static Class[] addDataBindingSpecificTypes(
 			Set<Class> rootClasses,
 			Collection<TypeConverter<?,?>> converters) {
 		Class[] rootClzArray;
 		if (null == converters) {
 			rootClzArray =  rootClasses.toArray(new Class[rootClasses.size()]);
 	//		rootClzArray =  rootClasses.toArray(new Class[rootClasses.size() + 1]);
 		} else {
 
 			Set<Class> rootClzes = new HashSet<Class>();
 			rootClzes.addAll(rootClasses);
 
 			for (Iterator<TypeConverter<?,?>> it = converters.iterator(); it.hasNext(); ) {
 				TypeConverter<?,?> converter = it.next();
 				rootClzes.add(converter.getValueType());
 			}
 
 			rootClzArray = rootClzes.toArray(new Class[rootClzes.size()]);
 		}
 		return rootClzArray;
 	}
 
 	private void initSerializerFactory(SerializerFactory factory,
 		ServiceId svcId, Map<String,String> options, Class[] rootClasses) throws ServiceException
 	{
 		SerializerFactoryInitContextImpl initCtx =
 			new SerializerFactoryInitContextImpl(svcId, options, rootClasses);
 		factory.init(initCtx);
 		initCtx.kill();
 	}
 
 	private void initDeserializerFactory(DeserializerFactory factory,
 		ServiceId svcId, Map<String,String> options, Class[] rootClasses, Schema upaAwaremasterSchema) throws ServiceException
 	{
 		DeserializerFactoryInitContextImpl initCtx =
 			new DeserializerFactoryInitContextImpl(svcId, options, rootClasses, upaAwaremasterSchema);
 		factory.init(initCtx);
 		initCtx.kill();
 	}
 
 	private boolean hasDefaultDataBinding(Map<String,DataBindingDesc> bindings,
 		Set<String> payloadTypes, String name)
 	{
 		// default data binding uses the same name for both binding name and payloadType
 		if (bindings.containsKey(name)) {
 			return true;
 		}
 
 		if (payloadTypes != null && payloadTypes.contains(name)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	private final static Map<String, String> NV_DATA_BINDING_OPTIONS;
 	private final static Map<String, String> JSON_DATA_BINDING_OPTIONS;
 	
 	static {
 		NV_DATA_BINDING_OPTIONS = new HashMap<String, String>();
 		NV_DATA_BINDING_OPTIONS.put(JSONStreamWriter.KEY_USE_SCHEMA_INFO, "false");
 		JSON_DATA_BINDING_OPTIONS = new HashMap<String, String>();
 		JSON_DATA_BINDING_OPTIONS.put(JSONStreamWriter.KEY_USE_SCHEMA_INFO, "false");
 	}
 
 	protected final void addDefaultDataBindings(ServiceId svcId,
 		Map<String,DataBindingDesc> bindings,
 		Set<String> payloadTypes, Set<Class> rootClasses, Schema upaAwareMasterSchema, boolean forFallback) throws ServiceException
 	{
 		Class[] rootClassArray = addDataBindingSpecificTypes(rootClasses, null);
 
 		if (!hasDefaultDataBinding(bindings, payloadTypes, BindingConstants.PAYLOAD_XML)) {
 			DataBindingDesc bindingDesc = new DataBindingDesc(BindingConstants.PAYLOAD_XML,
 				SOAConstants.MIME_XML,
 				new JAXBXMLSerializerFactory(),
 				new JAXBXMLDeserializerFactory(),
 				null, null, null, null);
 
 			initSerializerFactory(bindingDesc.getSerializerFactory(), svcId, null, rootClassArray);
 			initDeserializerFactory(bindingDesc.getDeserializerFactory(), svcId, null, rootClassArray,
 						upaAwareMasterSchema);
 
 			bindings.put(BindingConstants.PAYLOAD_XML, bindingDesc);
 
 			if (payloadTypes != null) {
 				payloadTypes.add(BindingConstants.PAYLOAD_XML);
 			}
 		}
 
 		if (!hasDefaultDataBinding(bindings, payloadTypes, BindingConstants.PAYLOAD_NV)) {
 			DataBindingDesc bindingDesc = new DataBindingDesc(BindingConstants.PAYLOAD_NV,
 				SOAConstants.MIME_NV,
 				new JAXBNVSerializerFactory(),
 				new JAXBNVDeserializerFactory(),
 				null, null, null, null);
 
 			initSerializerFactory(bindingDesc.getSerializerFactory(), svcId, (forFallback ? NV_DATA_BINDING_OPTIONS : null), rootClassArray);
 			initDeserializerFactory(bindingDesc.getDeserializerFactory(), svcId, null, rootClassArray,
 					upaAwareMasterSchema);
 
 			bindings.put(BindingConstants.PAYLOAD_NV, bindingDesc);
 
 			if (payloadTypes != null) {
 				payloadTypes.add(BindingConstants.PAYLOAD_NV);
 			}
 		}
 
 		if (!hasDefaultDataBinding(bindings, payloadTypes, BindingConstants.PAYLOAD_JSON)) {
 			DataBindingDesc bindingDesc = new DataBindingDesc(BindingConstants.PAYLOAD_JSON,
 				SOAConstants.MIME_JSON,
 				new JAXBJSONSerializerFactory(),
 				new JAXBJSONDeserializerFactory(),
 				null, null, null, null);
 
 			initSerializerFactory(bindingDesc.getSerializerFactory(), svcId, (forFallback ? JSON_DATA_BINDING_OPTIONS : null), rootClassArray);
 			initDeserializerFactory(bindingDesc.getDeserializerFactory(), svcId, null, rootClassArray,
 					upaAwareMasterSchema);
 
 			bindings.put(BindingConstants.PAYLOAD_JSON, bindingDesc);
 
 			if (payloadTypes != null) {
 				payloadTypes.add(BindingConstants.PAYLOAD_JSON);
 			}
 		}
 
 		if (!hasDefaultDataBinding(bindings, payloadTypes, BindingConstants.PAYLOAD_FAST_INFOSET)) {
 			DataBindingDesc bindingDesc = new DataBindingDesc(BindingConstants.PAYLOAD_FAST_INFOSET,
 				SOAConstants.MIME_FAST_INFOSET,
 				new JAXBFastInfosetSerializerFactory(),
 				new JAXBFastInfosetDeserializerFactory(),
 				null, null, null, null);
 
 			initSerializerFactory(bindingDesc.getSerializerFactory(), svcId, null, rootClassArray);
 			initDeserializerFactory(bindingDesc.getDeserializerFactory(), svcId, null, rootClassArray,
 					upaAwareMasterSchema);
 
 			bindings.put(BindingConstants.PAYLOAD_FAST_INFOSET, bindingDesc);
 
 			if (payloadTypes != null) {
 				payloadTypes.add(BindingConstants.PAYLOAD_FAST_INFOSET);
 			}
 		}
 		
 		if (!hasDefaultDataBinding(bindings, payloadTypes, BindingConstants.PAYLOAD_PROTOBUF)) {
 			DataBindingDesc bindingDesc = new DataBindingDesc(BindingConstants.PAYLOAD_PROTOBUF,
 				SOAConstants.MIME_PROTOBUF,
 				new ProtobufSerializerFactory(),
 				new ProtobufDeserializerFactory(),
 				null, null, null, null);
 
 			bindings.put(BindingConstants.PAYLOAD_PROTOBUF, bindingDesc);
 
 			if (payloadTypes != null) {
 				payloadTypes.add(BindingConstants.PAYLOAD_PROTOBUF);
 			}
 		}
 
 	}
 
 	private void buildCustomSerializers(String name,
 		MessageProcessorConfigHolder processorConfig,
 		Map<String,Serializer> customSerializers,
 		Map<String,Deserializer> customDeserializers,
 		ClassLoader cl)
 		throws ServiceException
 	{
 		Map<String,CustomSerializerConfig> customSerializerConfigs = processorConfig.getCustomSerializerMap(name);
 		if (customSerializerConfigs == null || customSerializerConfigs.isEmpty()) {
 			return;
 		}
 
 		for (CustomSerializerConfig info: customSerializerConfigs.values()) {
 			String serClassName = info.getSerializerClassName();
 			if (serClassName != null) {
 				Serializer ser = ReflectionUtils.createInstance(serClassName, Serializer.class, cl);
 				Class javaType = ser.getBoundType();
 				if (javaType == null) {
 					throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_CUSTOM_SER_NO_BOUND_TYPE,
 							ErrorConstants.ERRORDOMAIN, new Object[] {serClassName}));
 				}
 				customSerializers.put(javaType.getName(), ser);
 			}
 
 			String deserClassName = info.getDeserializerClassName();
 			if (deserClassName != null) {
 				Deserializer deser = ReflectionUtils.createInstance(deserClassName, Deserializer.class, cl);
 				Class javaType = deser.getBoundType();
 				if (javaType == null) {
 					throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_CUSTOM_DESER_NO_BOUND_TYPE,
 							ErrorConstants.ERRORDOMAIN, new Object[] {deserClassName}));
 				}
 				customDeserializers.put(javaType.getName(), deser);
 			}
 		}
 	}
 
 	private void buildTypeConverters(String name,
 		MessageProcessorConfigHolder processorConfig,
 		Map<String,TypeConverter<?,?>> typeConvertersByBoundType,
 		Map<String,TypeConverter<?,?>> typeConvertersByValueType,
 		ClassLoader cl) throws ServiceException
 	{
 		Map<String,TypeConverterConfig> convMap = processorConfig.getTypeConverterMap(name);
 		if (convMap == null || convMap.isEmpty()) {
 			return;
 		}
 
 		for (TypeConverterConfig convConfig: convMap.values()) {
 			String boundType = convConfig.getBoundJavaTypeName();
 			String valueType = convConfig.getValueJavaTypeName();
 			String className = convConfig.getTypeConverterClassName();
 
 			if (className == null || boundType == null || valueType == null) {
 				throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_DATA_BINDING_TYPE_CONV,
 						ErrorConstants.ERRORDOMAIN, new Object[] {name}));
 			}
 
 			TypeConverter<?,?> conv;
 			if (typeConvertersByBoundType.containsKey(boundType)) {
 				conv = typeConvertersByBoundType.get(boundType);
 				if (!conv.getClass().getName().equals(className)) {
 					throw new ServiceCreationException(
 							ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_DATA_BINDING_TYPE_CONV_COLLISION,
 									ErrorConstants.ERRORDOMAIN, new Object[] {name, boundType}));
 				}
 				// keep using old converter if class name matches
 			} else {
 				conv = ReflectionUtils.createInstance(className, TypeConverter.class, cl);
 				checkTypeConverter(conv, valueType, boundType);
 				typeConvertersByBoundType.put(boundType, conv);
 			}
 
 			if (typeConvertersByValueType.containsKey(valueType)) {
 				TypeConverter oldConv = typeConvertersByValueType.get(valueType);
 				if (!oldConv.getClass().getName().equals(className)) {
 					throw new ServiceCreationException(
 							ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_DATA_BINDING_TYPE_CONV_COLLISION,
 									ErrorConstants.ERRORDOMAIN, new Object[] {name, valueType}));
 				}
 				// keep using old converter if class name matches
 			} else {
 				// converter is either created or found in the previous operations, let's use it
 				typeConvertersByValueType.put(valueType, conv);
 			}
 		}
 	}
 
 	private void checkTypeConverter(TypeConverter conv, String valueType, String boundType)
 		throws ServiceException
 	{
 		Class valueTypeClass = conv.getValueType();
 		Class boundTypeClass = conv.getBoundType();
 
 		if (valueTypeClass == null || !valueTypeClass.getName().equals(valueType)) {
 			throw new ServiceCreationException(
 					ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_TYPE_CONV_CLASS_MISMATCH,
 							ErrorConstants.ERRORDOMAIN, new Object[] {conv.getClass().getName(), valueType}));
 		}
 
 		if (boundTypeClass == null || !boundTypeClass.getName().equals(boundType)) {
 			throw new ServiceCreationException(
 					ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_TYPE_CONV_CLASS_MISMATCH,
 					ErrorConstants.ERRORDOMAIN, new Object[] {conv.getClass().getName(), boundType}));
 		}
 	}
 
 	protected final List<LoggingHandler> createLoggingHandlers(
 		ServiceId svcId, MessageProcessorConfigHolder processorConfig, ClassLoader cl)
 		throws ServiceException
 	{
 		Set<String> handlerClasses = new HashSet<String>();
 		List<LoggingHandler> result = new ArrayList<LoggingHandler>();
 
 		List<FrameworkHandlerConfig> handlers = processorConfig.getLoggingHandlers();
 		if (handlers == null) {
 			handlers = new ArrayList<FrameworkHandlerConfig>();
 		}
 
 		boolean supportsErrorLogging = false;
 		for (FrameworkHandlerConfig logHandler: handlers) {
 			String className = logHandler.getClassName();
 			if (className == null || className.length() == 0) {
 				// Shouldn't happen - config checks for this
 				throw new ServiceCreationException(
 					ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_UNDEFINED_LOG_HANDLER_CLASS_NAME,
 							ErrorConstants.ERRORDOMAIN));
 			}
 
 			if (handlerClasses.contains(className)) {
 				throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_DUPLICATE_LOG_HANDLER_CLASS_NAME,
 						ErrorConstants.ERRORDOMAIN, new Object[] {className}));
 			}
 			handlerClasses.add(className);
 
 			LoggingHandler handler = ReflectionUtils.createInstance(className, LoggingHandler.class, cl);
 
 			Map<String,String> options = logHandler.getOptions();
 			LoggingHandlerInitContextImpl initCtx = new LoggingHandlerInitContextImpl(svcId, options);
 			handler.init(initCtx);
 			initCtx.kill();
 
 			supportsErrorLogging |= initCtx.supportsErrorLogging();
 
 			result.add(handler);
 		}
 
 		if (result.isEmpty() || !supportsErrorLogging) {
 			addDefaultLoggingHandler(svcId, cl, result);
 		}
 
 		return result;
 	}
 
 	protected final void addDefaultLoggingHandler(ServiceId svcId,
 		ClassLoader cl, List<LoggingHandler> list) throws ServiceException
 	{
 		String className = getDefaultLoggingHandlerClassName();
 		LoggingHandler handler = ReflectionUtils.createInstance(className, LoggingHandler.class, cl);
 
 		LoggingHandlerInitContextImpl initCtx = new LoggingHandlerInitContextImpl(svcId, null);
 		handler.init(initCtx);
 		initCtx.kill();
 
 		list.add(handler);
 	}
 
 	protected abstract String getDefaultLoggingHandlerClassName();
 
 	protected final Class loadServiceInterfaceClass(CommonConfigHolder config,
 		boolean isRequired, ClassLoader cl) throws ServiceException
 	{
 		String className = config.getServiceInterfaceClassName();
 		if (className == null) {
 			if (!isRequired) {
 				return null;
 			}
 			throw new ServiceCreationException(
 					ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_UNDEFINED_INTF_CLASS_NAME,
 							ErrorConstants.ERRORDOMAIN));
 		}
 
 		return ReflectionUtils.loadClass(className, null, cl);
 	}
 
 	protected HeaderMappingsDesc loadHeaderMappings(String adminName, OptionList options, boolean inbound) throws ServiceCreationException {
 		if (options == null) {
 			return HeaderMappingsDesc.EMPTY_MAPPINGS;
 		}
 		List<NameValue> nameValueList = options.getOption();
 		Map<String,String> headerMap = new HashMap<String,String>();
 		Set<String> suppressHeaderSet = new HashSet<String>();
 
 		for (NameValue nv : nameValueList) {
 			String rawname = nv.getName();
 			String name = SOAHeaders.normalizeName(rawname, true);
 
 			if (inbound && !SOAHeaders.isSOAHeader(name)) { // only validate incoming header names
 				throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_HEADER_NAME,
 						ErrorConstants.ERRORDOMAIN, new Object[] {adminName, name}));
 			}
 
 			String value = nv.getValue();
 			if (value.startsWith("header[")) {
 				if (!value.endsWith("]")) {
 					throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_MAPPING_VALUE,
 							ErrorConstants.ERRORDOMAIN, new Object[] {adminName, value}));
 				}
 				String indexval = value.substring(7, value.length()-1);
 				if (inbound) {
 					if( headerMap.containsKey(indexval)) {
 						throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_DUPLICATE_HEADER_KEY,
 								ErrorConstants.ERRORDOMAIN, new Object[] {adminName, indexval}));
 					}
					headerMap.put(indexval, name);
 				} else {
 					if( headerMap.containsKey(indexval)) {
 						throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_DUPLICATE_HEADER_KEY,
 								ErrorConstants.ERRORDOMAIN, new Object[] {adminName, indexval}));
 					}
 					headerMap.put(name, indexval);
 				}
 			} else if (value.equals("suppress")) {
 				suppressHeaderSet.add(name);
 			} else {
 				throw new ServiceCreationException(ErrorDataFactory.createErrorData(ErrorConstants.SVC_FACTORY_INVALID_MAPPING_VALUE,
 						ErrorConstants.ERRORDOMAIN, new Object[] {adminName, value}));
 			}
 
 		}
 		HeaderMappingsDesc result = new HeaderMappingsDesc(headerMap, suppressHeaderSet);
 		return result;
 	}
 
 	private void updateMetrics(ServiceDesc serviceDesc) {
 		MetricsConfigManager mgr;
 		if (m_isClientSide) {
 			mgr = MetricsConfigManager.getClientInstance();
 		} else {
 			mgr = MetricsConfigManager.getServerInstance();
 		}
 
 		// make sure matrics subsystem recalculates its levels
 		mgr.resetMonitoringLevel(serviceDesc.getAdminName());
 	}
 
 	protected final void initializeCompStatus(BaseServiceBrowserCompStatus comp) {
 		URL xslTemplate = BaseServiceBrowserCompStatus.class.getResource("ServiceBrowserCompStatus.xsl");
 		if (xslTemplate == null) {
 			throw new RuntimeException("Unable to find XSL template ServiceBrowserCompStatus.xsl" +
 				" for ServiceDesc browser " + comp.getClass().getName());
 		}
 
 		Registration.registerComponent(comp, xslTemplate);
 	}
 
 	static {
 		SOAVariableProvider.init();
 	}
 }

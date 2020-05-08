 /*
  * Copyright Adele Team LIG
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.liglab.adele.cilia.runtime.impl;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.felix.ipojo.ConfigurationException;
 import org.apache.felix.ipojo.PrimitiveHandler;
 import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
 import org.apache.felix.ipojo.metadata.Element;
 import org.apache.felix.ipojo.parser.MethodMetadata;
 import org.apache.felix.ipojo.parser.PojoMetadata;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.liglab.adele.cilia.Data;
 import fr.liglab.adele.cilia.exceptions.CiliaException;
 import fr.liglab.adele.cilia.framework.IDispatcher;
 import fr.liglab.adele.cilia.framework.ISender;
 import fr.liglab.adele.cilia.runtime.CiliaInstance;
 import fr.liglab.adele.cilia.runtime.IDispatcherHandler;
 import fr.liglab.adele.cilia.runtime.ProcessorMetadata;
 import fr.liglab.adele.cilia.runtime.WorkQueue;
 import fr.liglab.adele.cilia.util.Const;
 import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;
 import fr.liglab.adele.cilia.util.concurrent.WriterPreferenceReadWriteLock;
 /**
  * This class is in charge of acting as a bridge between the dispatcher logic, and the
  * sender instances.
  *
  * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
  *
  */
 @SuppressWarnings({"unchecked","rawtypes"})
 public class DispatcherHandler extends PrimitiveHandler implements IDispatcherHandler {
 
 	private String componentId = null;
 
 	private ThreadLocal thLProcessor = new ThreadLocal();
 	/**
 	 * Sender Manager reference.
 	 */
 	private DispatcherInstanceManager dispatcherManager;
 	/**
 	 * Method process Meta-data. To intercept.
 	 */
 	private MethodMetadata m_methodProcessMetadata;
 
 	private Logger logger = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);
 
 	private Logger rtlogger = LoggerFactory.getLogger(Const.LOGGER_RUNTIME);
 
 	private ReadWriteLock m_lock = new WriterPreferenceReadWriteLock();
 
 	/**
 	 * Cilia namespace.
 	 */
 
 	private final static String CILIA_DISPATCHER_HANDLERNAME = "dispatcher";
 
 	protected MonitorHandler monitor = null;
 
 	public String lastSenderName = "";
 
 	public Data lastDataSended = null;
 
 	/* This reference will be injected by iPOJO */
 	private WorkQueue m_applicationQueue;
 	/*
 	 * Private class holding asynchronous properties
 	 */
 	ExtendedProperties extendedProperties;
 
 
 	/**
 	 * Check if the handler is well configured.
 	 */
 	public void initializeComponentFactory(ComponentTypeDescription cd, Element metadata)
 			throws ConfigurationException {
 
 		Element[] dispatcherHanlerMetadata = metadata.getElements(
 				CILIA_DISPATCHER_HANDLERNAME, Const.CILIA_NAMESPACE);
 
 		if (dispatcherHanlerMetadata != null ) {
 
 			Element procesorMetadata = null;
 			if (metadata.containsElement("method", Const.CILIA_NAMESPACE)) {
 				procesorMetadata = metadata.getElements("method",
 						Const.CILIA_NAMESPACE)[0];
 			} else if (metadata.containsElement("method")) {
 				procesorMetadata = metadata.getElements("method")[0];
 			} else {
 				procesorMetadata = dispatcherHanlerMetadata[0];
 			}
 			ProcessorMetadata cm = new ProcessorMetadata(procesorMetadata);
 			PojoMetadata pojometadata = getPojoMetadata();
 
 			MethodMetadata methodMetadata;
 			methodMetadata = pojometadata.getMethod(cm.getMethod(),
 					cm.getParameterDataType());
 
 			if (methodMetadata == null) {
 				throw new ConfigurationException("Method " + cm.getMethod()
 						+ " in pojo should " + "receive "
 						+ cm.getReturnedDataType()[0]);
 			}
 			String returnDataType = methodMetadata.getMethodReturn();
 			if (returnDataType.compareTo(cm.getReturnedDataType()[0]) != 0) {
 				throw new ConfigurationException("Method " + cm.getMethod()
 						+ " in pojo should " + "return "
 						+ cm.getReturnedDataType()[0]);
 			}
 
 		} else {
 			throw new ConfigurationException("Error in configuration"
 					+ " this handler should be configured with one handler name:"
 					+ CILIA_DISPATCHER_HANDLERNAME + " and HandlerNamespace:"
 					+ Const.CILIA_NAMESPACE + metadata);
 
 		}
 	}
 
 	/**
 	 * Initialize handler properties.
 	 * 
 	 * @param dictionary
 	 */
 	private void initiainitializeProperties(Dictionary dictionary) {
 		componentId = (String)dictionary.get(Const.PROPERTY_COMPONENT_ID);
 	}
 
 	public void configure(Element metadata, Dictionary properties)
 			throws ConfigurationException {
 
 		extentedConfiguration();
 		initiainitializeProperties(properties);
 
 
 		Element handlerMetadata = metadata.getElements(CILIA_DISPATCHER_HANDLERNAME,
 				Const.CILIA_NAMESPACE)[0];
 
 		Element procesorMetadata = null;
 		if (metadata.containsElement("method", Const.CILIA_NAMESPACE)) {
 			procesorMetadata = metadata.getElements("method", Const.CILIA_NAMESPACE)[0];
 		} else if (metadata.containsElement("method")) {
 			procesorMetadata = metadata.getElements("method")[0];
 		} else {
 			procesorMetadata = handlerMetadata;
 		}
 		ProcessorMetadata dm = new ProcessorMetadata(procesorMetadata);
 
 		subscribeProcesor(dm);
 
 	}
 
 	public void setDispatcherManager(DispatcherInstanceManager disp){
 		this.dispatcherManager = disp;
 	}
 
 
 	private void subscribeProcesor(ProcessorMetadata dm) {
 		m_methodProcessMetadata = getPojoMetadata().getMethod(dm.getMethod(),
 				dm.getParameterDataType());
 		getInstanceManager().register(m_methodProcessMetadata, this);
 	}
 
 	public void onError(Object pojo, Method method, Throwable throwable) {
 		Exception ex = null;
 		if (Exception.class.isAssignableFrom(throwable.getClass())) {
 			ex = Exception.class.cast(throwable);
 		} else {
 			ex = new CiliaException("Unknown Error Exception");
 		}
 		notifyOnProcessError((List) thLProcessor.get(), ex);
 		throwable.printStackTrace();
 	}
 
 	public void onEntry(Object pojo, Method method, Object[] args) {
 		List list = null;
 
 		if ((args != null) && (args.length > 0)) {
 			if (args[0] instanceof List) {
 				list = new ArrayList((List) args[0]);
 			} else if (args[0] instanceof Data) {
 				list = new ArrayList();
 				Data ndata = (Data) args[0];
 				list.add(ndata);
 			}
 		}
 		thLProcessor.set(list);
 		notifyOnProcessEntry(list);
 	}
 
 	public void onExit(Object pojo, Method method, Object returnedObj) {
 		StringBuffer msg;

 		if (method.getName().compareTo(m_methodProcessMetadata.getMethodName()) == 0) {
 			List list = null;
 			Data ndata = null;
 			boolean isList = false;
 			if (returnedObj == null) {
 				logger.warn("[{}] Dispatching empty dataset", componentId);
 				list = null;
 			} else if (returnedObj instanceof List) {
 				list = new ArrayList((List) returnedObj);
 				isList = true;
 			} else if (returnedObj instanceof Data) {
 				list = new ArrayList();
 				ndata = (Data) returnedObj;
 				list.add(ndata);
 				isList = false;
 			} else {
 				msg = new StringBuffer().append("[{}] Unable to identify returned data type ")
 						.append(returnedObj);
 				logger.error(msg.toString(), componentId);
 			}
 			final List rList = list;
 			notifyOnProcessExit(list);
 			try {
 				if (isList) {
 					dispatch(rList);
 				} else {
 					dispatch(ndata);
 				}
 			} catch (CiliaException e) {
 				logger.error("[{}] Error while dispatching", componentId,e);
 				throw new RuntimeException(e.getMessage());
 			}
 			notifyOnDispatch(rList);
 		}
		//We notify that it stop processing and sending.
		dispatcherManager.getMediatorComponentManager().stopProcessing();
 	}
 
 
 	/**
 	 * Send the specified data to the sender specified by their name.
 	 * 
 	 * @param senderName
 	 *            Sender to be used to send data.
 	 * @param data
 	 *            Data to send.
 	 */
 	public void send(final String senderName, final Data data) throws CiliaException {
 		lastSenderName = senderName;
 		data.setLastDeliveryPort(senderName);
 		lastDataSended = data;
 		boolean synchronous = extendedProperties.isModeSynchronous;
 		List pojoList = (List) dispatcherManager.getPojo(senderName);
 		if (pojoList == null) {
 			logger.error("[{}] there is any sender present in port {}", componentId, String.valueOf(senderName));
 		} else {
 			List senders = new ArrayList(pojoList);
 			Iterator it = senders.iterator();
 			int iteration = 0;
 			//System.out.println("DispatcherHandler: Sending : " + senderName +" -->"+ it.hasNext());
 			while (it.hasNext()) {
 				CiliaInstance ci = (CiliaInstance) it.next();
 				ISender msender = (ISender) ci.getObject();
 				if (msender != null) {
 					logger.debug("[{}] [" + (iteration++) + "] Sending using to {}", componentId, ci.getName());
 					if (synchronous == true) {
 						msender.send(data);
 					} else {
 						m_applicationQueue.execute(new AsynchronousSend(msender, data));
 					}
 				} else {
 					logger.error("[{}][" + (iteration) + "] Sending using {} is not valid", componentId, ci.getName());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Send the specified data to the sender specified by their name.
 	 * 
 	 * @param senderName
 	 *            sender used to send data.
 	 * @param properties
 	 *            Properties to reconfigure sender before send.
 	 * @param data
 	 *            Data to send.
 	 */
 	public void send(final String senderName, final Properties properties, final Data data)
 			throws CiliaException {
 		synchronized (dispatcherManager) {
 			dispatcherManager.reconfigurePOJOS(properties);
 			send(senderName, data);
 		}
 	}
 
 	public MonitorHandler getMonitor() {
 		return (MonitorHandler) getInstanceManager().getHandler(
 				Const.ciliaQualifiedName("monitor-handler"));
 	}
 
 	/**
 	 * get Senders ids
 	 * 
 	 * 
 	 * @return List of senders ids
 	 */
 	public List getSendersIds() {
 		Set keys = dispatcherManager.getKeys();
 		List ports = new ArrayList();
 		Iterator it = keys.iterator();
 		while(it.hasNext()) {
 			String key = String.valueOf(it.next());
 			if (!key.startsWith("error")){
 				ports.add(key);
 			}
 		}
 		return new ArrayList(ports);
 	}
 
 	public void notifyOnProcessError(List data, Exception ex) {
 		logger.error("{} processing error with {}", componentId, data);
 		if (monitor == null) {
 			monitor = getMonitor();
 		}
 		if (monitor != null) {
 			monitor.notifyOnProcessError(data, ex);
 		}
 	}
 
 	protected void notifyOnProcessExit(List data) {
		logger.debug("[{}] has finish to process", componentId);
 		if (monitor == null) {
 			monitor = getMonitor();
 		}
 		if (monitor != null) {
 			monitor.notifyOnProcessExit(data);
 		}
 	}
 
 	protected void notifyOnProcessEntry(List data) {
 		logger.debug("[{}] will process data", componentId);
 		if (monitor == null) {
 			monitor = getMonitor();
 		}
 		if (monitor != null) {
 			monitor.notifyOnProcessEntry(data);
 		}
 	}
 
 	protected void notifyOnDispatch(List data) {
		logger.debug("[{}] will dispatch data", componentId);
 		if (monitor == null) {
 			monitor = getMonitor();
 		}
 		if (monitor != null) {
 			monitor.notifyOnDispatch(data);
 		}	
 	}
 
 
 	public void fireEvent(Map info) {
 		
 		logger.debug("[{}] will fire event with {}", componentId, info);
 		if (monitor == null) {
 			monitor = getMonitor();
 		}
 		if (monitor != null) {
 			monitor.fireEvent(info);
 		}
 	}
 
 
 	public void reconfigure(Dictionary props) {
 		extendedReconfiguration(props);
 		initiainitializeProperties(props);
 	}
 
 	private Dictionary getDispatcherProperties(Dictionary dictionary) {
 		Dictionary dispatcherProperties;
 		
 		Object properties = dictionary.get("dispatcher.properties");
 		if (properties != null && properties instanceof Dictionary) {
 			dispatcherProperties = (Dictionary) properties;
 		} else {
 			dispatcherProperties = dictionary;
 		}
 		return dispatcherProperties;
 	}
 
 
 	public void dispatch(Data data) throws CiliaException {
 		IDispatcher dispatcher =  dispatcherManager.getDispatcher();
 		if (dispatcher == null) {
 			logger.error("[{}] dispatcher is not valid when dispatching", componentId);
 			return;
 		}
 		dispatcher.dispatch(data);
 	}
 
 	private void dispatch(List dataset) throws CiliaException {
 		IDispatcher dispatcher = dispatcherManager.getDispatcher();
 		if (dispatcher == null) {
 			logger.error("[{}] dispatcher is not valid when dispatching", componentId);
 			return;
 		}
 		for (int i = 0; i < dataset.size(); i ++) {
 			Data data = (Data)dataset.get(i);
 			dispatch(data);
 		}
 	}
 
 	public void start() {
 	}
 
 	public void stop() {
 	}
 
 	public void unvalidate() {
 		rtlogger.debug("[{}] dispatcher stopped", componentId);
 	}
 
 	public void validate() {
 		rtlogger.debug("[{}] dispatcher started", componentId);
 	}
 
 	/* Initialization by default */
 	private void extentedConfiguration() {
 		extendedProperties = new ExtendedProperties();
 	}
 
 	/*
 	 * Extended reconfiguration decode parameters : "mediator.mode.synchrone",
 	 * value true ->set mode synchrone,value=false-> set mode asynchrone "
 	 */
 	private void extendedReconfiguration(Dictionary props) {
 		if (props != null) {
 			Object obj;
 			obj = props.get("mediator.mode.synchrone");
 			if ((obj != null) && (obj instanceof Boolean)) {
 				logger.debug("Mediator 'asychronous' mode");
 				extendedProperties.isModeSynchronous = ((Boolean) obj).booleanValue();
 			}
 			/* reset the queue size to the default 'norm' value */
 			String value = (String) props.get("task.queue.application");
 			if ((value != null) && (value instanceof String)) {
 				int i = Integer.parseInt(value);
 				extendedProperties.oldMaxjobQueued = i;
 				if (logger.isDebugEnabled())
 					logger.debug("set 'asynchronous queue' size to ("
 							+ extendedProperties.oldMaxjobQueued + ")");
 			}
 
 		}
 	}
 
 	/*
 	 * Class holding extended properties
 	 */
 	private class ExtendedProperties {
 		private boolean isModeSynchronous;
 		private int maxJobQueued;
 		private int oldMaxjobQueued;
 
 		ExtendedProperties() {
 			isModeSynchronous = true;
 			maxJobQueued = 0;
 			oldMaxjobQueued = 0;
 		}
 
 		public void evaluate() {
 			if (!isModeSynchronous) {
 				maxJobQueued = Math.max(maxJobQueued,
 						m_applicationQueue.sizeMaxjobQueued());
 				if (oldMaxjobQueued < maxJobQueued) {
 					fireEvent(Collections.singletonMap("task.queue.application",
 							new Integer(maxJobQueued)));
 					oldMaxjobQueued = maxJobQueued;
 				}
 			}
 		}
 	}
 
 	/* private class for sending in asynchronous way */
 	private class AsynchronousSend implements Runnable {
 		ISender msender;
 		Data data;
 
 		AsynchronousSend(ISender msender, final Data data) {
 			this.msender = msender;
 			this.data = data;
 		}
 
 		public void run() {
 			msender.send(data);
 			/* checks is the size of events queued has reach a new maximum */
 			extendedProperties.evaluate();
 		}
 	}
 
 }

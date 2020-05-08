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
 
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.felix.ipojo.ConfigurationException;
 import org.apache.felix.ipojo.PrimitiveHandler;
 import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
 import org.apache.felix.ipojo.architecture.PropertyDescription;
 import org.apache.felix.ipojo.metadata.Element;
 import org.apache.felix.ipojo.parser.MethodMetadata;
 import org.apache.felix.ipojo.parser.PojoMetadata;
 import org.apache.felix.ipojo.util.Callback;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.liglab.adele.cilia.AdminData;
 import fr.liglab.adele.cilia.Data;
 import fr.liglab.adele.cilia.framework.IScheduler;
 import fr.liglab.adele.cilia.model.Component;
 import fr.liglab.adele.cilia.model.MediatorComponent;
 import fr.liglab.adele.cilia.runtime.ISchedulerHandler;
 import fr.liglab.adele.cilia.runtime.ProcessorMetadata;
 import fr.liglab.adele.cilia.runtime.WorkQueue;
 import fr.liglab.adele.cilia.util.Const;
 import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;
 import fr.liglab.adele.cilia.util.concurrent.WriterPreferenceReadWriteLock;
 
 /**
  * 
  * 
  * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
  *         Team</a>
  * 
  */
 @SuppressWarnings({ "rawtypes", "unchecked" })
 public class SchedulerHandler extends PrimitiveHandler implements ISchedulerHandler, Runnable {
 
 	protected Logger rtLogger = LoggerFactory.getLogger(Const.LOGGER_RUNTIME);
 	
 	protected Logger appLogger = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);
 
 	protected MonitorHandler monitor;
 	private ReadWriteLock writeLock = new WriterPreferenceReadWriteLock();
 	private String componentId = null;
 	/**
 	 * Method process metadata, to invoke processor.
 	 * 
 	 */
 	private MethodMetadata methodMetadata;
 
 	private SchedulerInstanceManager schedulerManager;
 
 	private final Object lockObject = new Object();
 
 	private boolean islist;
 
 	private Callback callback;
 
 	private Component schedulerDescription;
 
 	private final static String HANDLER_NAME = "scheduler";
 
 	private boolean isLocked = false;
 
 	private ServiceReference m_refData = null;
 	/* This reference will be injected by iPOJO */
 	private WorkQueue m_systemQueue;
 
 	private Dictionary m_dictionary;
 
 	/**
 	 * This method is called by iPOJO to verify the metadata correctness.
 	 * 
 	 * @param cd
 	 *            Component description.
 	 * @param metadata
 	 *            iPOJO metadata.
 	 * @throws ConfigurationException
 	 *             when there is an error configuring handler with the iPOJO.
 	 */
 	public void initializeComponentFactory(ComponentTypeDescription cd, Element metadata)
 			throws ConfigurationException {
 
 		Element[] schedulerHanlerMetadata = metadata.getElements(HANDLER_NAME,
 				Const.CILIA_NAMESPACE);
 
 		if (schedulerHanlerMetadata != null ) {
 
 			Element procesorMetadata = null;
 			if (metadata.containsElement("method", Const.CILIA_NAMESPACE)) {
 				procesorMetadata = metadata.getElements("method",
 						Const.CILIA_NAMESPACE)[0];
 			} else if (metadata.containsElement("method")) {
 				procesorMetadata = metadata.getElements("method")[0];
 			} else {
 				procesorMetadata = schedulerHanlerMetadata[0];
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
 			// Add properties to component.
 			PropertyDescription[] phd1 = getHandlerManager().getFactory()
 					.getComponentDescription().getProperties();
 			for (int i = 0; phd1 != null && i < phd1.length; i++) {
 				if (!phd1[i].isImmutable()) {
 					cd.addProperty(new PropertyDescription(phd1[i].getName(), phd1[i]
 							.getType(), phd1[i].getValue()));
 				}
 			}
 
 		} else {
 			rtLogger.error("Invalid Componnent metadata" + metadata);
 			throw new ConfigurationException("Error in configuration"
 					+ " this handler should be configured with one handler name:"
 					+ HANDLER_NAME + " and HandlerNamespace:" + Const.CILIA_NAMESPACE);
 		}
 	}
 
 	protected void initializeProperties(Dictionary dictionary) {
 		componentId = (String)dictionary.get(Const.PROPERTY_COMPONENT_ID);
 	}
 
 	/**
 	 * Method called by iPOJO when creating an instance.
 	 */
 	public void configure(Element metadata, Dictionary dictionary)
 			throws ConfigurationException {
 
 		initializeProperties(dictionary);
 
 
 		Element subscribers = metadata.getElements(HANDLER_NAME, Const.CILIA_NAMESPACE)[0];
 		this.m_dictionary = dictionary;
 
 
 		Element procesorMetadata = null;
 		if (metadata.containsElement("method", Const.CILIA_NAMESPACE)) {
 			procesorMetadata = metadata.getElements("method", Const.CILIA_NAMESPACE)[0];
 		} else if (metadata.containsElement("method")) {
 			procesorMetadata = metadata.getElements("method")[0];
 		} else {
 			procesorMetadata = subscribers;
 		}
 
 		ProcessorMetadata sm = new ProcessorMetadata(procesorMetadata);
 		PojoMetadata pojometadata = getPojoMetadata();
 		methodMetadata = pojometadata
 				.getMethod(sm.getMethod(), sm.getParameterDataType());
 
 		islist = (sm.getParameterDataType()[0].compareTo(java.util.List.class.getName()) == 0);
 		callback = new Callback(methodMetadata, getInstanceManager());
 	}
 
 	public void setSchedulerManager(SchedulerInstanceManager sm) {
 		schedulerManager = sm;
 	}
 	
 	/**
 	 * This method will invoke the process method in mediator.
 	 * 
 	 * @param dataSet
 	 *            Data set used to invoke process in mediator.
 	 * @return null.
 	 */
 	public final void process(final List /* data */dataList) {
 		if (getInstanceManager().getState() >= MediatorComponent.VALID) {
 			schedulerManager.getMediatorComponentManager().startProcessing();
 			// stock triggered data to be used later (log/stats/...).
 			List list = null;
 			Object args[] = new Object[1];// it support only one parametter.
 			synchronized (dataList) {
 				list = new ArrayList(dataList);
 			}
 
 			try {
 				if (islist) {
 					args[0] = list;
 					callback.call(args);
 				} else { // iterate in all the elements.
 					for (int i = 0; i < list.size(); i++) {
 						args[0] = list.get(i);
 						callback.call(args);
 					}
 					if (list.size() == 0) { // It means that there is any Data,
 						// but we call it with null.
 						args[0] = null;
 						callback.call(args);
 					}
 				}
 			} catch (Exception e) {
 				appLogger.error("[{}] unable to process", componentId, e);
 			}
 		} else {
 			appLogger.error("[{}] Trying to process but processor is not valid",componentId );
 		}
 	}
 
 
 
 
 	public List getSourcesIds() {
 		synchronized (lockObject) {
 			return new ArrayList(schedulerManager.getKeys());
 		}
 	}
 
 	private Properties getProperties(Element element) {
 		Properties props = new Properties();
 		Element[] propertiesElement = element.getElements("property");
 		if (propertiesElement != null) {
 			for (int i = 0; i < propertiesElement.length; i++) {
 				if (propertiesElement[i].containsAttribute("name")) {
 					String propertyName = propertiesElement[i].getAttribute("name");
 					String propertyValueString = null;
 					Map propertyValueMap = null;
 					Object propertyValue = null;
 					// obtain the property value
 					if (propertiesElement[i].containsAttribute("value")) {
 						propertyValueString = propertiesElement[i].getAttribute("value");
 						propertyValue = propertyValueString;
 					} else {
 						// If there is not any value, so they value could be a
 						// Map
 						propertyValueMap = getProperties(propertiesElement[i]);
 						propertyValue = propertyValueMap;
 					}
 					props.put(propertyName, propertyValue);
 				}
 			}
 		}
 		return props;
 	}
 
 	public void reconfigure(Dictionary props) {
 		String value;
 
 		initializeProperties(props);
 		/* Check property lock */
 		value = (String) props.get(Const.PROPERTY_LOCK_UNLOCK);
 		if ((value != null) && (value.equals(Const.SET_LOCK)))
 			lock();
 		else {
 			if ((value != null) && (value.equals(Const.SET_UNLOCK)))
 				unlock();
 		}
 	}
 
 	public void notifyOnCollect(Data data) {
 		MonitorHandler mon = getMonitor();
 		if (mon != null) {
 			mon.notifyOnCollect(data);
 		} else {
 			appLogger.error("[{}]Monitor is not valid");
 		}
 
 	}
 
 	public void fireEvent(Map info) {
 		appLogger.debug("[{}] to fire event", componentId);
 		MonitorHandler mon = getMonitor();
 
 		if (mon != null) {
 			mon.fireEvent(info);
 		} else {
 			appLogger.error("[{}] Monitor is not valid", componentId);
 		}
 	}
 
 	private MonitorHandler getMonitor() {
 		if (monitor == null) {
 			monitor = (MonitorHandler) getInstanceManager().getHandler(
 					Const.ciliaQualifiedName("monitor-handler"));
 		}
 		return monitor;
 	}
 
 	// call scheduler object reference.
 	public void notifyData(Data data) {
 		appLogger.debug("[{}] Data has been received", componentId);
 		IScheduler scheduler = schedulerManager.getScheduler();
 		if (scheduler == null) {
 			appLogger.error("[{}] scheduler object is not valid when notifying the data arrive", componentId);
 			return;
 		}
 		if (isLocked())
 			storeMessage(data);
 		else {
 			try {
 				writeLock.readLock().acquire();
 				notifyOnCollect(data);
 				scheduler.notifyData(data);
 			} catch (InterruptedException e) {
 			} finally {
 				writeLock.readLock().release();
 			}
 		}
 	}
 	public void start() {
 	}
 
 	public void stop() {
 	}
 
 	public void unvalidate() {
 		rtLogger.debug("[{}] scheduler stopped", componentId);
 		m_refData = null;
 	}
 
 	public void validate() {
 		rtLogger.debug("[{}] scheduler started", componentId);
 		m_refData = null;
 	}
 
 	private void storeMessage(Data data) {
 		if (getAdminData(false).size() == 0) {
 			getAdminData(false).put("cilia.stored.messages", new ArrayList());
 		}
 		((ArrayList) getAdminData(false).get("cilia.stored.messages")).add(data);
 		appLogger.trace(" [{}]#Message stored ="
 				+ ((ArrayList) getAdminData(false).get("cilia.stored.messages")).size(), componentId);
 	}
 
 	private ArrayList getStoredMessage() {
 		ArrayList storedMessages = null;
 		Map map = getAdminData(false);
 
 		if (map != null) {
 			storedMessages = (ArrayList) map.get("cilia.stored.messages");
 		}
 		return storedMessages;
 	}
 
 	public void lock() {
 		isLocked = true;
 		rtLogger.debug("[{}] Scheduler now is locked", componentId);
 	}
 
 	private void injectDataStored(ArrayList dataStored) {
 		Data data;
 		IScheduler scheduler = schedulerManager.getScheduler();
 		if (scheduler != null) {
 			rtLogger.trace("[{}] inject Data size=" + dataStored.size(), componentId);
 			for (int i = 0; i < dataStored.size(); i++) {
 				data = (Data) dataStored.get(i);
 				rtLogger.trace("[{}] Inject data = " + data.getContent(), componentId);
 				notifyOnCollect(data);
 				scheduler.notifyData(data);
 			}
 		}
 
 	}
 
 	public void unlock() {
 		rtLogger.trace("[{}] unlock called", componentId);
 		m_systemQueue.execute(this);
 	}
 
 	public boolean isLocked() {
 		return isLocked;
 	}
 
 	private void retreiveAdminService() {
 		ServiceReference[] refs = null;
 		BundleContext context = getInstanceManager().getContext();
 		try {
 			refs = context.getServiceReferences(AdminData.class.getName(), "(chain.name="
 					+ (String) m_dictionary.get(Const.PROPERTY_CHAIN_ID) + ")");
 		} catch (InvalidSyntaxException e) {
 			rtLogger.warn("[{}] Admin data service lookup unrecoverable error", componentId);
 			refs = null;
 		}
 		if (refs != null)
 			m_refData = refs[0];
 		else {
 			rtLogger.warn("[{}] Admin data service not found", componentId);
 			return;
 		}
 	}
 
 	private Map getAdminData(boolean isRunning) {
 		AdminData dataContainer;
 		Map data;
 		if (m_refData == null)
 			retreiveAdminService();
 		dataContainer = (AdminData) getInstanceManager().getContext().getService(
 				m_refData);
 		data = dataContainer.getData(
 				(String) m_dictionary.get(Const.PROPERTY_COMPONENT_ID), isRunning);
 		getInstanceManager().getContext().ungetService(m_refData);
 		return data;
 	}
 
 	public Map getData() {
 		/* Return the buffer */
 		return getAdminData(true);
 	}
 
 	public void run() {
 		/* Firstly , re -treat datas stored */
 		init();
 		/* Reinject new data */
 		ArrayList storedMessages = getStoredMessage();
 		if (storedMessages != null) {
 			injectDataStored(storedMessages);
 		}
 		isLocked = false;
 	}
 
 	public void init() {
 		IScheduler scheduler = schedulerManager.getScheduler();
 		if (scheduler != null) {
 			rtLogger.trace("[{}] init called", componentId);
 			scheduler.init();
 		}
 	}
 
 }

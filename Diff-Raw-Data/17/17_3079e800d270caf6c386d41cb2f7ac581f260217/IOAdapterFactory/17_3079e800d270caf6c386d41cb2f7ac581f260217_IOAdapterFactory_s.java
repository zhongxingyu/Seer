 package fr.liglab.adele.cilia.internals.factories;
 
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.felix.ipojo.ComponentInstance;
 import org.apache.felix.ipojo.ConfigurationException;
 import org.apache.felix.ipojo.Handler;
 import org.apache.felix.ipojo.HandlerManager;
 import org.apache.felix.ipojo.IPojoContext;
 import org.apache.felix.ipojo.metadata.Attribute;
 import org.apache.felix.ipojo.metadata.Element;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.liglab.adele.cilia.Data;
 import fr.liglab.adele.cilia.framework.AbstractAsyncIOAdapter;
 import fr.liglab.adele.cilia.framework.AbstractIOAdapter;
 import fr.liglab.adele.cilia.framework.monitor.IMonitor;
 import fr.liglab.adele.cilia.model.impl.PatternType;
 import fr.liglab.adele.cilia.runtime.MediatorHandler;
 import fr.liglab.adele.cilia.runtime.impl.DispatcherHandler;
 import fr.liglab.adele.cilia.runtime.impl.MonitorHandler;
 import fr.liglab.adele.cilia.runtime.impl.SchedulerHandler;
 import fr.liglab.adele.cilia.util.Const;
 
 public class IOAdapterFactory extends MediatorComponentFactory implements AdapterFactoryI {
 
 	private static final String COMPONENT_TYPE = "adapter";
 
 	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_RUNTIME);
 
 	
 	public IOAdapterFactory(BundleContext context, Element element)
 			throws ConfigurationException {
 		super(context, element);
 	}
 
 	/**
 	 * @return the adapterType
 	 */
 	public PatternType getPattern() {
 		return PatternType.IN_OUT;
 	}
 	
 	protected void updateMetadata() {
 		Element scheduler = null;
 		Element dispatcher = null;
 		Class clazz = null;
 		if (!m_componentMetadata
				.containsElement("scheduler", DEFAULT_NAMESPACE)) {
 			scheduler = new Element("scheduler", DEFAULT_NAMESPACE);
 		} else {
 			scheduler = m_componentMetadata.getElements("scheduler",
 					DEFAULT_NAMESPACE)[0];
 		}
 
 		if (!scheduler.containsAttribute("name")) {
 			scheduler.addAttribute(new Attribute("name", "immediate-scheduler"));
 			scheduler.addAttribute(new Attribute("namespace", DEFAULT_NAMESPACE));
 		}
 
 		if (!m_componentMetadata.containsElement("dispatcher",
				DEFAULT_NAMESPACE)) {
 			dispatcher = new Element("dispatcher", DEFAULT_NAMESPACE);
 		} else {
 			dispatcher = m_componentMetadata.getElements("dispatcher",
 					DEFAULT_NAMESPACE)[0];
 		}
 		
 		if (!dispatcher.containsAttribute("name")) {
 			dispatcher.addAttribute(new Attribute("name", "multicast-dispatcher"));
 			dispatcher.addAttribute(new Attribute("namespace", DEFAULT_NAMESPACE));
 		}
 		
 		try {
 			clazz = super.loadClass(getClassName());
 		} catch (ClassNotFoundException e) {
 			logger.error("Error when analysing Class:" + getClassName(), e);
 			return;// anything to do.
 		}
 
 		// Check if the manipulated class herite from CiliaAdapter
 		if ((AbstractIOAdapter.class.isAssignableFrom(clazz)) || (AbstractAsyncIOAdapter.class.isAssignableFrom(clazz))) {
 			
 			dispatcher.addAttribute(new Attribute("method", "dispatchData"));
 			dispatcher.addAttribute(new Attribute("data.type", Data.class
 					.getName()));
 
 			scheduler.addAttribute(new Attribute("method", "receiveData"));
 			scheduler.addAttribute(new Attribute("in.data.type", Data.class
 					.getName()));
 		}

		m_componentMetadata.addElement(dispatcher);
		m_componentMetadata.addElement(scheduler);
 	}
 
 	/**
 	 * Check if the mediator component configuration is valid.
 	 */
 	public void check(Element element) throws ConfigurationException {
 		super.check(element);
 		updateMetadata();
 		computeConstituantsDescriptions();
 		computePorts();
 	}
 
 	protected void computeConstituantsDescriptions()
 			throws ConfigurationException {
 		schedulerDescription = computeDescription("scheduler");
 		dispatcherDescription = computeDescription("dispatcher");
 	}
 	
 	public String getComponentType() {
 		return COMPONENT_TYPE;
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public List getRequiredHandlerList() {
 		List handlerList;
 		List returnedList = new ArrayList();
 		handlerList = super.getRequiredHandlerList();
 		Iterator it = handlerList.iterator();
 		// Delete required handlers (processor, scheduler, dispatcher)
 		while (it.hasNext()) {
 			RequiredHandler req = (RequiredHandler) it.next();
 			if (!(req.equals(new RequiredHandler("method", null)))
 					&& !(req.equals(new RequiredHandler("method",
 							DEFAULT_NAMESPACE)))
 							&& !(req.equals(new RequiredHandler("ports",null))) // "org.apache.felix.ipojo:ports"
 							&& !(req.equals(new RequiredHandler("ports",DEFAULT_NAMESPACE))) // "fr.liglab.adele.cilia:ports"
 					) {
 				if (!returnedList.contains(req)) {
 					returnedList.add(req);
 				}
 			}
 			if (req.equals(new RequiredHandler("method", null))) {
 				logger.warn("Method in mediator must be configured with cilia namespace ("
 								+ DEFAULT_NAMESPACE + ")");
 			}
 		}
 		// Add requires handler.
 		RequiredHandler reqs = new RequiredHandler("scheduler",
 				DEFAULT_NAMESPACE);
 		if (!returnedList.contains(reqs)) {
 			returnedList.add(reqs);
 		}
 		RequiredHandler reqd = new RequiredHandler("dispatcher",
 				DEFAULT_NAMESPACE);
 		if (!returnedList.contains(reqd)) {
 			returnedList.add(reqd);
 		}
 		// Add requires handler.
 		RequiredHandler reqm = new RequiredHandler("monitor-handler",
 				DEFAULT_NAMESPACE);
 		if (!returnedList.contains(reqm)) {
 			returnedList.add(reqm);
 		}
 		return returnedList;
 	}
 
 	public ComponentInstance createInstance(Dictionary config,
 			IPojoContext context, HandlerManager[] handlers)
 					throws org.apache.felix.ipojo.ConfigurationException {
 
 		AdapterManager im = (AdapterManager) createAdapterInstance(config,
 				context, handlers);
 
 		SchedulerHandler sch = (SchedulerHandler) im.getHandler(Const
 				.ciliaQualifiedName("scheduler"));
 		MonitorHandler monitor = (MonitorHandler) im.getHandler(Const
 				.ciliaQualifiedName("monitor-handler"));
 		DispatcherHandler dsp = (DispatcherHandler) im.getHandler(Const
 				.ciliaQualifiedName("dispatcher"));
 		Handler m_handlers[] = im.getRegistredHandlers();
 		for (int i = 0; i < m_handlers.length; i++) {
 			// Add subscription.
 			Handler handler = m_handlers[i];
 			// add the monitor, to listen the scheduler/dispatcher events.
 			if (handler instanceof IMonitor) {
 				if (monitor != null) {
 					monitor.addListener((IMonitor) handler);
 				}
 			}
 			// Add the scheduler/dispatcher references to the mediator handler.
 			if (handler instanceof MediatorHandler) {
 				MediatorHandler mh = (MediatorHandler) handler;
 				if (dsp != null) {
 					mh.setDispatcher(dsp);
 				}
 				if (sch != null) {
 					mh.setScheduler(sch);
 				}
 			}
 		}
 
 		return im;
 	}
 
 	public ComponentInstance createAdapterInstance(Dictionary config,
 			IPojoContext context, HandlerManager[] handlers)
 					throws org.apache.felix.ipojo.ConfigurationException {
 		logger.debug("[{}] creating adaptor instance with {}", config.get(Const.PROPERTY_COMPONENT_ID), config);
 		AdapterManager instance = new AdapterManager(this, context, handlers);
 		try {
 			instance.configure(m_componentMetadata, config);
 			instance.start();
 			return instance;
 		} catch (ConfigurationException e) {
 			e.printStackTrace();
 			if (instance != null) {
 				instance.dispose();
 				instance = null;
 			}
 			throw e;
 		} catch (Throwable e) { // All others exception are handled here.
 			if (instance != null) {
 				instance.dispose();
 				instance = null;
 			}
 			e.printStackTrace();
 			logger.error(e.getMessage(), e);
 			throw new ConfigurationException(e.getMessage());
 		}
 
 	}
 
 }

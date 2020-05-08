 package evymind.vapor.deploy;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import evymind.vapor.core.utils.component.AggregateLifecycle;
 import evymind.vapor.deploy.bindings.StandardDeployer;
 import evymind.vapor.deploy.bindings.StandardStarter;
 import evymind.vapor.deploy.bindings.StandardStopper;
 import evymind.vapor.deploy.bindings.StandardUndeployer;
 import evymind.vapor.deploy.graph.Edge;
 import evymind.vapor.deploy.graph.Node;
 import evymind.vapor.deploy.graph.Path;
 import evymind.vapor.server.Server;
 import evymind.vapor.server.handler.ContextHandlerCollection;
 
 /**
  * The Deployment Manager.
  * <p>
  * Responsibilities:
  * <p>
  * <img src="doc-files/DeploymentManager_Roles.png">
  * <ol>
  * <li>Tracking Apps and their Lifecycle Location</li>
  * <li>Managing AppProviders and the Apps that they provide.</li>
  * <li>Executing AppLifecycle on App based on current and desired Lifecycle Location.</li>
  * </ol>
  * <p>
  * <img src="doc-files/DeploymentManager.png">
  */
 public class DeploymentManager extends AggregateLifecycle {
 	
 	private static final Logger log = LoggerFactory.getLogger(DeploymentManager.class);
 
 	/**
 	 * Represents a single tracked app within the deployment manager.
 	 */
 	public class AppEntry {
 		/**
 		 * Version of the app.
 		 * 
 		 * Note: Auto-increments on each {@link DeploymentManager#addApp(App)}
 		 */
 		private int version;
 
 		/**
 		 * The app being tracked.
 		 */
 		private App app;
 
 		/**
 		 * The lifecycle node location of this App
 		 */
 		private Node lifecyleNode;
 
 		/**
 		 * Tracking the various AppState timestamps (in system milliseconds)
 		 */
 		private Map<Node, Long> stateTimestamps = new HashMap<Node, Long>();
 
 		public App getApp() {
 			return app;
 		}
 
 		public Node getLifecyleNode() {
 			return lifecyleNode;
 		}
 
 		public Map<Node, Long> getStateTimestamps() {
 			return stateTimestamps;
 		}
 
 		public int getVersion() {
 			return version;
 		}
 
 		void setLifecycleNode(Node node) {
 			this.lifecyleNode = node;
 			this.stateTimestamps.put(node, Long.valueOf(System.currentTimeMillis()));
 		}
 	}
 
 	private final List<AppProvider> _providers = new ArrayList<AppProvider>();
 	private final AppLifecycle _lifecycle = new AppLifecycle();
 	private final Queue<AppEntry> _apps = new ConcurrentLinkedQueue<AppEntry>();
 //	private AttributesMap _contextAttributes = new AttributesMap();
 	private ContextHandlerCollection _contexts;
 	private boolean _useStandardBindings = true;
 	private String _defaultLifecycleGoal = AppLifecycle.STARTED;
 
 	/**
 	 * Receive an app for processing.
 	 * 
 	 * Most commonly used by the various {@link AppProvider} implementations.
 	 */
 	public void addApp(App app) {
 		log.info("Deployable added: " + app.getOriginId());
 		AppEntry entry = new AppEntry();
 		entry.app = app;
 		entry.setLifecycleNode(_lifecycle.getNodeByName("undeployed"));
 		_apps.add(entry);
 
 		if (isRunning() && _defaultLifecycleGoal != null) {
 			// Immediately attempt to go to default lifecycle state
 			this.requestAppGoal(entry, _defaultLifecycleGoal);
 		}
 	}
 
 
 	/**
 	 * Set the AppProviders. The providers passed are added via {@link #addBean(Object)} so that their lifecycles may be
 	 * managed as a {@link AggregateLifecycle}.
 	 * 
 	 * @param providers
 	 */
 	public void setAppProviders(Collection<AppProvider> providers) {
 		if (isRunning())
 			throw new IllegalStateException();
 
 		_providers.clear();
 		removeBeans();
 		for (AppProvider provider : providers)
 			if (_providers.add(provider))
 				addBean(provider);
 	}
 
 	public Collection<AppProvider> getAppProviders() {
 		return Collections.unmodifiableList(_providers);
 	}
 
 	public void addAppProvider(AppProvider provider) {
 		if (isRunning())
 			throw new IllegalStateException();
 
 //		List<AppProvider> old = new ArrayList<AppProvider>(_providers);
 		if (_providers.add(provider) && getServer() != null)
 			getServer().getContainer().update(this, null, provider, "provider");
 
 		addBean(provider);
 	}
 
 	public void setLifecycleBindings(Collection<AppLifecycle.Binding> bindings) {
 		if (isRunning())
 			throw new IllegalStateException();
 		for (AppLifecycle.Binding b : _lifecycle.getBindings())
 			_lifecycle.removeBinding(b);
 		for (AppLifecycle.Binding b : bindings)
 			_lifecycle.addBinding(b);
 	}
 
 	public Collection<AppLifecycle.Binding> getLifecycleBindings() {
 		return Collections.unmodifiableSet(_lifecycle.getBindings());
 	}
 
 	public void addLifecycleBinding(AppLifecycle.Binding binding) {
 		_lifecycle.addBinding(binding);
 	}
 
 	/**
 	 * Convenience method to allow for insertion of nodes into the lifecycle.
 	 * 
 	 * @param existingFromNodeName
 	 * @param existingToNodeName
 	 * @param insertedNodeName
 	 */
 	public void insertLifecycleNode(String existingFromNodeName, String existingToNodeName, String insertedNodeName) {
 		Node fromNode = _lifecycle.getNodeByName(existingFromNodeName);
 		Node toNode = _lifecycle.getNodeByName(existingToNodeName);
 		Edge edge = new Edge(fromNode, toNode);
 		_lifecycle.insertNode(edge, insertedNodeName);
 	}
 
 	@Override
 	protected void doStart() throws Exception {
 		if (_useStandardBindings) {
 			log.debug("DeploymentManager using standard bindings");
 			addLifecycleBinding(new StandardDeployer());
 			addLifecycleBinding(new StandardStarter());
 			addLifecycleBinding(new StandardStopper());
 			addLifecycleBinding(new StandardUndeployer());
 		}
 
 		// Start all of the AppProviders
 		for (AppProvider provider : _providers) {
 			startAppProvider(provider);
 		}
 		super.doStart();
 	}
 
 	@Override
 	protected void doStop() throws Exception {
 		// Stop all of the AppProviders
 		for (AppProvider provider : _providers) {
 			try {
 				provider.stop();
 			} catch (Exception e) {
 				log.warn("Unable to start AppProvider", e);
 			}
 		}
 		super.doStop();
 	}
 
 	private AppEntry findAppByOriginId(String originId) {
 		if (originId == null) {
 			return null;
 		}
 
 		for (AppEntry entry : _apps) {
 			if (originId.equals(entry.app.getOriginId())) {
 				return entry;
 			}
 		}
 		return null;
 	}
 
 	public App getAppByOriginId(String originId) {
 		AppEntry entry = findAppByOriginId(originId);
 		if (entry == null) {
 			return null;
 		}
 		return entry.app;
 	}
 
 	public Collection<AppEntry> getAppEntries() {
 		return _apps;
 	}
 
 	public Collection<App> getApps() {
 		List<App> ret = new ArrayList<App>();
 		for (AppEntry entry : _apps) {
 			ret.add(entry.app);
 		}
 		return ret;
 	}
 
 	/**
 	 * Get Set of {@link App}s by {@link Node}
 	 * 
 	 * @param node
 	 *            the node to look for.
 	 * @return the collection of apps for the node
 	 */
 	public Collection<App> getApps(Node node) {
 		List<App> ret = new ArrayList<App>();
 		for (AppEntry entry : _apps) {
 			if (entry.lifecyleNode == node) {
 				ret.add(entry.app);
 			}
 		}
 		return ret;
 	}
 
 	public List<App> getAppsWithSameContext(App app) {
 		List<App> ret = new ArrayList<App>();
 		if (app == null) {
 			return ret;
 		}
 
 		String contextId = app.getContextPath();
 		if (contextId == null) {
 			// No context? Likely not deployed or started yet.
 			return ret;
 		}
 
 		for (AppEntry entry : _apps) {
 			if (entry.app.equals(app)) {
 				// Its the input app. skip it.
 				// TODO: is this filter needed?
 				continue;
 			}
 
 			if (contextId.equals(entry.app.getContextPath())) {
 				ret.add(entry.app);
 			}
 		}
 		return ret;
 	}
 
 //	/**
 //	 * Get a contextAttribute that will be set for every Context deployed by this provider.
 //	 * 
 //	 * @param name
 //	 * @return the context attribute value
 //	 */
 //	public Object getContextAttribute(String name) {
 //		return _contextAttributes.getAttribute(name);
 //	}
 
 //	public AttributesMap getContextAttributes() {
 //		return _contextAttributes;
 //	}
 
 	public ContextHandlerCollection getContexts() {
 		return _contexts;
 	}
 
 	public String getDefaultLifecycleGoal() {
 		return _defaultLifecycleGoal;
 	}
 
 	public AppLifecycle getLifecycle() {
 		return _lifecycle;
 	}
 
 	public Server getServer() {
 		if (_contexts == null) {
 			return null;
 		}
 		return _contexts.getServer();
 	}
 
 	/**
 	 * Remove the app from the tracking of the DeploymentManager
 	 * 
 	 * @param app
 	 *            if the app is Unavailable remove it from the deployment manager.
 	 */
 	public void removeApp(App app) {
 		Iterator<AppEntry> it = _apps.iterator();
 		while (it.hasNext()) {
 			AppEntry entry = it.next();
 			if (entry.app.equals(app)) {
 				if (!AppLifecycle.UNDEPLOYED.equals(entry.lifecyleNode.getName()))
 					requestAppGoal(entry.app, AppLifecycle.UNDEPLOYED);
 				it.remove();
 				log.info("Deployable removed: " + entry.app);
 			}
 		}
 	}
 
 	public void removeAppProvider(AppProvider provider) {
 		if (_providers.remove(provider)) {
 			removeBean(provider);
 			if (getServer() != null)
 				getServer().getContainer().update(this, provider, null, "provider");
 		}
 		try {
 			provider.stop();
 		} catch (Exception e) {
 			log.warn("Unable to stop Provider", e);
 		}
 	}
 
 //	/**
 //	 * Remove a contextAttribute that will be set for every Context deployed by this provider.
 //	 * 
 //	 * @param name
 //	 */
 //	public void removeContextAttribute(String name) {
 //		_contextAttributes.removeAttribute(name);
 //	}
 
 	/**
 	 * Move an {@link App} through the {@link AppLifecycle} to the desired {@link Node}, executing each lifecycle step
 	 * in the process to reach the desired state.
 	 * 
 	 * @param app
 	 *            the app to move through the process
 	 * @param nodeName
 	 *            the name of the node to attain
 	 */
 	public void requestAppGoal(App app, String nodeName) {
 		AppEntry appentry = findAppByOriginId(app.getOriginId());
 		if (appentry == null) {
 			throw new IllegalStateException("App not being tracked by Deployment Manager: " + app);
 		}
 
 		requestAppGoal(appentry, nodeName);
 	}
 
 	/**
 	 * Move an {@link App} through the {@link AppLifecycle} to the desired {@link Node}, executing each lifecycle step
 	 * in the process to reach the desired state.
 	 * 
 	 * @param appentry
 	 *            the internal appentry to move through the process
 	 * @param nodeName
 	 *            the name of the node to attain
 	 */
 	private void requestAppGoal(AppEntry appentry, String nodeName) {
 		Node destinationNode = _lifecycle.getNodeByName(nodeName);
 		if (destinationNode == null) {
 			throw new IllegalStateException("Node not present in Deployment Manager: " + nodeName);
 		}
 		// Compute lifecycle steps
 		Path path = _lifecycle.getPath(appentry.lifecyleNode, destinationNode);
 		if (path.isEmpty()) {
 			// nothing to do. already there.
 			return;
 		}
 
 		// Execute each Node binding. Stopping at any thrown exception.
 		try {
 			Iterator<Node> it = path.getNodes().iterator();
 			if (it.hasNext()) // Any entries?
 			{
 				// The first entry in the path is always the start node
 				// We don't want to run bindings on that entry (again)
 				it.next(); // skip first entry
 				while (it.hasNext()) {
 					Node node = it.next();
					log.debug("Executing Node {}", node);
 					_lifecycle.runBindings(node, appentry.app, this);
 					appentry.setLifecycleNode(node);
 				}
 			}
 		} catch (Throwable t) {
 			log.warn("Unable to reach node goal: " + nodeName, t);
 		}
 	}
 
 	/**
 	 * Move an {@link App} through the {@link AppLifecycle} to the desired {@link Node}, executing each lifecycle step
 	 * in the process to reach the desired state.
 	 * 
 	 * @param appId
 	 *            the id of the app to move through the process
 	 * @param nodeName
 	 *            the name of the node to attain
 	 */
 	public void requestAppGoal(String appId, String nodeName) {
 		AppEntry appentry = findAppByOriginId(appId);
 		if (appentry == null) {
 			throw new IllegalStateException("App not being tracked by Deployment Manager: " + appId);
 		}
 		requestAppGoal(appentry, nodeName);
 	}
 
 //	/**
 //	 * Set a contextAttribute that will be set for every Context deployed by this provider.
 //	 * 
 //	 * @param name
 //	 * @param value
 //	 */
 //	public void setContextAttribute(String name, Object value) {
 //		_contextAttributes.setAttribute(name, value);
 //	}
 //
 //	public void setContextAttributes(AttributesMap contextAttributes) {
 //		this._contextAttributes = contextAttributes;
 //	}
 
 	public void setContexts(ContextHandlerCollection contexts) {
 		this._contexts = contexts;
 	}
 
 	public void setDefaultLifecycleGoal(String defaultLifecycleState) {
 		this._defaultLifecycleGoal = defaultLifecycleState;
 	}
 
 	private void startAppProvider(AppProvider provider) {
 		try {
 			provider.setDeploymentManager(this);
 			provider.start();
 		} catch (Exception e) {
 			log.warn("Unable to start AppProvider", e);
 		}
 	}
 
 	public void undeployAll() {
 		log.info("Undeploy All");
 		for (AppEntry appentry : _apps) {
 			requestAppGoal(appentry, "undeployed");
 		}
 	}
 
 	public boolean isUseStandardBindings() {
 		return _useStandardBindings;
 	}
 
 	public void setUseStandardBindings(boolean useStandardBindings) {
 		this._useStandardBindings = useStandardBindings;
 	}
 
 	public Collection<Node> getNodes() {
 		return _lifecycle.getNodes();
 	}
 
 	public Collection<App> getApps(String nodeName) {
 		return getApps(_lifecycle.getNodeByName(nodeName));
 	}
 }

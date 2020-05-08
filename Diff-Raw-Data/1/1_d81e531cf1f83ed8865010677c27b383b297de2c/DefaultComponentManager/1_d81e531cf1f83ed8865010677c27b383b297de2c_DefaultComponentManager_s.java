 package de.mxro.server.internal;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.mxro.server.ComponentConfiguration;
 import de.mxro.server.ComponentContext;
 import de.mxro.server.ServerComponent;
 import de.mxro.server.ShutdownCallback;
 import de.mxro.server.StartCallback;
 import de.mxro.server.manager.ComponentFactory;
 import de.mxro.server.manager.ComponentManager;
 
 public class DefaultComponentManager implements ComponentManager {
 
 	private final ComponentFactory factory;
 	private final List<ServerComponent> components;
 	private final List<ServerComponent> running;
 
 	@Override
 	public ServerComponent addComponent(final int index,
 			final ComponentContext context, final ComponentConfiguration conf) {
 		final ServerComponent component;
 		synchronized (components) {
 			component = createComponent(context, conf);
 			components.add(index, component);
 		}
 		return component;
 	}
 
 	@Override
 	public ServerComponent addComponent(final ComponentContext context,
 			final ComponentConfiguration conf) {
 		final ServerComponent component;
 		synchronized (components) {
 			component = createComponent(context, conf);
 			components.add(component);
 		}
 		return component;
 	}
 
 	@Override
 	public ServerComponent addRunningComponent(final ComponentContext context,
 			final ServerComponent component) {
 
 		synchronized (components) {
 
 			components.add(component);
 			this.running.add(component);
 		}
 		return component;
 	}
 
 	@Override
 	public List<ServerComponent> getComponents() {
 
 		return Collections.unmodifiableList(new ArrayList<ServerComponent>(
 				this.components));
 	}
 
 	private ServerComponent createComponent(final ComponentContext context,
 			final ComponentConfiguration conf) {
 		assert context != null;
 		assert conf != null;
 
 		if (getComponent(conf.getId()) != null) {
 			throw new IllegalStateException("A server with the id ["
 					+ conf.getId() + "] is already defined.");
 		}
 		final ServerComponent component = factory.createComponent(conf);
 
 		component.injectContext(context);
 		component.injectConfiguration(conf);
 
 		return component;
 	}
 
 	@Override
 	public void startComponent(final String componentId,
 			final StartCallback callback) {
 		synchronized (components) {
 
 			final ServerComponent component = getComponent(componentId);
 
 			if (component == null) {
 				throw new IllegalStateException("No server with id ["
 						+ componentId + "] is defined.");
 			}
 
 			synchronized (running) {
 				if (running.contains(component)) {
 					throw new IllegalStateException(
 							"Cannot start an already running server ["
 									+ componentId + "]");
 				}
 
 			}
 
 			component.start(new StartCallback() {
 
 				@Override
 				public void onStarted() {
 					synchronized (running) {
 						running.add(component);
 					}
 					callback.onStarted();
 				}
 
 				@Override
 				public void onFailure(final Throwable t) {
 					callback.onFailure(t);
 				}
 			});
 
 		}
 	}
 
 	private List<String> getComponentIds() {
 		final ArrayList<String> ids = new ArrayList<String>(this
 				.getComponents().size());
 		for (final ServerComponent comp : this.getComponents()) {
 			ids.add(comp.getConfiguration().getId());
 		}
 		return ids;
 	}
 
 	@Override
 	public void stopComponent(final String componentId,
 			final ShutdownCallback callback) {
 		synchronized (components) {
 
 			final ServerComponent component = getComponent(componentId);
 
 			if (component == null) {
 				throw new IllegalStateException("No server component with id ["
 						+ componentId
 						+ "] is defined. Defined components are: "
 						+ getComponentIds());
 			}
 
 			synchronized (running) {
 				if (!running.contains(component)) {
 					throw new IllegalStateException(
 							"Cannot stop an not running server [" + componentId
 									+ "]");
 				}
 			}
 
 			component.stop(new ShutdownCallback() {
 
 				@Override
 				public void onShutdownComplete() {
 					synchronized (running) {
 						running.remove(component);
 					}
 					callback.onShutdownComplete();
 				}
 
 				@Override
 				public void onFailure(final Throwable t) {
 					callback.onFailure(t);
 				}
 			});
 		}
 	}
 
 	@Override
 	public int removeComponent(final String componentId) {
 		synchronized (components) {
 			final ServerComponent component = getComponent(componentId);
 			if (component == null) {
 				throw new IllegalStateException("No server with the id ["
 						+ componentId + "] is defined.");
 			}
 
 			synchronized (running) {
 				if (running.contains(component)) {
 					throw new IllegalStateException(
 							"Cannot remove server. Server must first be stopped ["
 									+ componentId + "]");
 				}
 			}
 
 			final int idx = components.indexOf(component);
 
 			if (idx < 0) {
 				throw new IllegalStateException(
 						"Cannot remove server that is not defined.");
 			}
 
 			components.remove(component);
 			return idx;
 		}
 	}
 
 	@Override
 	public ServerComponent getComponent(final String componentId) {
 		synchronized (components) {
 			for (final ServerComponent comp : components) {
 				if (comp.getConfiguration().getId().equals(componentId)) {
 					return comp;
 				}
 			}
 			return null;
 		}
 	}
 
 	public DefaultComponentManager(final ComponentFactory factory) {
 		super();
 		this.factory = factory;
 
 		this.components = new LinkedList<ServerComponent>();
 		this.running = new LinkedList<ServerComponent>();
 	}
 
 	@Override
 	public boolean isRunning(final ServerComponent component) {
 
 		return this.running.contains(component);
 	}
 
 }

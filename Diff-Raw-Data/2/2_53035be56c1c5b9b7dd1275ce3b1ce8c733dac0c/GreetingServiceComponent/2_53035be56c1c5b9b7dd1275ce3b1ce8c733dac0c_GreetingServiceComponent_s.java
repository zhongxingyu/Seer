 /**
  * Copyright (c) 2012 AGETO Service GmbH and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Gunnar Wagenknecht - initial API and implementation
  */
 package hello.service;
 
 import java.util.Collection;
 
 import org.eclipse.gyrex.cloud.environment.INodeEnvironment;
 
 import org.osgi.service.component.ComponentContext;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * OSGi service component for providing a {@link GreetingService} OSGi service.
  * <p>
  * This class is specified in the {@code OSGI-INF/greeting-service.xml} OSGi
  * declarative service component definition. It will be lazily instantiated by
  * the OSGi runtime environment when all required dependencies are available.
  * </p>
  */
 public class GreetingServiceComponent implements GreetingService {
 
 	private static final Logger LOG = LoggerFactory.getLogger(GreetingServiceComponent.class);
 
 	private GreetingService service;
 
 	/**
 	 * Called by the OSGi DS runtime when this component is activated.
 	 * <p>
 	 * This requires to set the following in the component definition:
 	 * 
 	 * <pre>
 	 * &lt;scr:component .. <strong>activate="activate"</strong> ..&gt;
 	 * </pre>
 	 * 
 	 * </p>
 	 * 
 	 * @param context
 	 */
 	public void activate(final ComponentContext context) {
 		LOG.info("Activating GreetingServiceComponent.");
 	}
 
 	/**
 	 * Called by the OSGi DS runtime when this component is de-activated.
 	 * <p>
 	 * This requires to set the following in the component definition:
 	 * 
 	 * <pre>
 	 * &lt;scr:component .. <strong>deactivate="deactivate"</strong> ..&gt;
 	 * </pre>
 	 * 
 	 * </p>
 	 * 
 	 * @param context
 	 */
 	public void deactivate(final ComponentContext context) {
 		LOG.info("Deactivating GreetingServiceComponent.");
 	}
 
 	@Override
 	public Collection<String> getGreetings() throws Exception {
 		// delegate to the real service
 		return getService().getGreetings();
 	}
 
 	public GreetingService getService() {
 		return service;
 	}
 
 	@Override
 	public void processGreetings() throws Exception {
 		// delegate to the real service
 		getService().processGreetings();
 	}
 
 	@Override
 	public void sayHello(final String greeting) throws Exception {
 		// delegate to the real service
 		getService().sayHello(greeting);
 	}
 
 	/**
 	 * Sets the node environment.
 	 * <p>
	 * The node environment will be used to read the node id. The node ide
 	 * uniquely identifies the node the system is running on. It is a
 	 * <em>required</em> dependency as specified by <strong>
 	 * <code>cardinality="1..1"</code> </strong> in the OSGi component
 	 * definition file.
 	 * </p>
 	 * <p>
 	 * This method will be called by the OSGi component runtime <em>when</em>
 	 * the component is initialized as specified by <strong>
 	 * <code>policy="static"</code> </strong> in the OSGi component definition
 	 * file.
 	 * </p>
 	 * 
 	 * <pre>
 	 * &lt;reference bind="setEnvironment" cardinality="1..1" interface="org.eclipse.gyrex.cloud.environment.INodeEnvironment" name="INodeEnvironment" policy="static"/&gt;
 	 * </pre>
 	 * 
 	 * @param environment
 	 *            the environment to set
 	 */
 	public void setEnvironment(final INodeEnvironment environment) {
 		// immediatly create greeting service
 		service = new GreetingServiceImpl(environment.getNodeId());
 	}
 }

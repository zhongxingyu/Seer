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
 package org.jboss.test.kernel.junit;
 
 import java.net.URL;
 
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.dependency.spi.Controller;
 import org.jboss.dependency.spi.ControllerMode;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.kernel.Kernel;
 import org.jboss.kernel.plugins.bootstrap.AbstractBootstrap;
 import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
 import org.jboss.kernel.plugins.deployment.xml.BasicXMLDeployer;
 import org.jboss.kernel.spi.dependency.KernelController;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.kernel.spi.deployment.KernelDeployment;
 import org.jboss.kernel.spi.metadata.KernelMetaDataRepository;
 import org.jboss.test.AbstractTestDelegate;
 
 /**
  * A MicrocontainerTestDelegate.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class MicrocontainerTestDelegate extends AbstractTestDelegate
 {
    /** The kernel */
    protected Kernel kernel;
 
    /** The deployer */
    protected BasicXMLDeployer deployer;
    
    /** The default mode */
    protected ControllerMode defaultMode = ControllerMode.AUTOMATIC;
    
    /**
     * Create a new MicrocontainerTestDelegate.
     * 
     * @param clazz the test class
     * @throws Exception for any error
     */
    public MicrocontainerTestDelegate(Class<?> clazz) throws Exception
    {
       super(clazz);
    }
 
    public void setUp() throws Exception
    {
       super.setUp();
       
       try
       {
          // Bootstrap the kernel
          AbstractBootstrap bootstrap = getBootstrap();
          bootstrap.run();
          kernel = bootstrap.getKernel();
          
          // Create the deployer
          deployer = createDeployer();
 
          // Deploy
          deploy();
       }
       catch (Exception e)
       {
          super.tearDown();
          throw e;
       }
       catch (Error e)
       {
          super.tearDown();
          throw e;
       }
       catch (Throwable e)
       {
          super.tearDown();
          throw new RuntimeException(e);
       }
    }
 
    protected BasicXMLDeployer createDeployer()
    {
       return new BasicXMLDeployer(kernel, defaultMode);
    }
 
    public void tearDown() throws Exception
    {
      super.tearDown();
      undeploy();
    }
    
    /**
     * Get the defaultMode.
     * 
     * @return the defaultMode.
     */
    public ControllerMode getDefaultMode()
    {
       return defaultMode;
    }
 
    /**
     * Set the defaultMode.
     * 
     * @param defaultMode the defaultMode.
     */
    public void setDefaultMode(ControllerMode defaultMode)
    {
       this.defaultMode = defaultMode;
    }
 
    /**
     * Get the kernel bootstrap
     * 
     * @return the bootstrap
     * @throws Exception for any error
     */
    protected AbstractBootstrap getBootstrap() throws Exception
    {
       return new BasicBootstrap();
    }
    
    /**
     * Get a bean
     *
     * @param name the name of the bean
     * @param state the state of the bean
     * @return the bean
     * @throws IllegalStateException when the bean does not exist at that state
     */
    protected Object getBean(final Object name, final ControllerState state)
    {
       KernelControllerContext context = getControllerContext(name, state);
       return context.getTarget();
    }
    
    /**
     * Get a bean
     *
     * @param <T> the expected type
     * @param name the name of the bean
     * @param state the state of the bean
     * @param expected the expected type
     * @return the bean
     * @throws IllegalStateException when the bean does not exist at that state
     */
    protected <T> T getBean(final Object name, final ControllerState state, final Class<T> expected)
    {
       if (expected == null)
          throw new IllegalArgumentException("Null expected");
       Object bean = getBean(name, state);
       return expected.cast(bean);
    }
    
    /**
     * Get the metadata repository
     * @return the metadata repository
     * @throws IllegalStateException when the bean does not exist at that state
     */
    protected KernelMetaDataRepository getMetaDataRepository()
    {
       return kernel.getMetaDataRepository();
    }
    
    /**
     * Get a context
     *
     * @param name the name of the bean
     * @param state the state of the bean
     * @return the context
     * @throws IllegalStateException when the context does not exist at that state
     */
    protected KernelControllerContext getControllerContext(final Object name, final ControllerState state)
    {
       KernelController controller = kernel.getController();
       KernelControllerContext context = (KernelControllerContext) controller.getContext(name, state);
       if (context == null)
          return handleNotFoundContext(controller, name, state);
       return context;
    }
    
    /**
     * Assert there is no context at the given state
     * 
     * @param name the name of the bean
     * @param state the state of the bean
     * @throws IllegalStateException when the context exists at that state
     */
    protected void assertNoControllerContext(final Object name, final ControllerState state)
    {
       KernelController controller = kernel.getController();
       KernelControllerContext context = (KernelControllerContext) controller.getContext(name, state);
       if (context != null)
          throw new IllegalStateException("Did not expect context at state " + state + " ctx=" + context);
    }
 
    /**
     * Handle not found context.
     *
     * @param controller the controller
     * @param name the name of the bean
     * @param state the state of the bean
     * @return the context
     * @throws IllegalStateException when the context does not exist at that state
     */
    protected KernelControllerContext handleNotFoundContext(Controller controller, Object name, ControllerState state)
    {
       throw new IllegalStateException("Bean not found " + name + " at state " + state);
    }
 
    /**
     * Change the context to the given state
     * 
     * @param context the context
     * @param required the required state
     * @return the actual state
     * @throws Throwable for any error
     */
    protected ControllerState change(KernelControllerContext context, ControllerState required) throws Throwable
    {
       Controller controller = kernel.getController();
       controller.change(context, required);
       return context.getState();
    }
 
    /**
     * Validate
     * 
     * @throws Exception for any error
     */
    protected void validate() throws Exception
    {
       try
       {
          deployer.validate();
       }
       catch (RuntimeException e)
       {
          throw e;
       }
       catch (Exception e)
       {
          throw e;
       }
       catch (Error e)
       {
          throw e;
       }
       catch (Throwable t)
       {
          throw new RuntimeException(t);
       }
    }
    
    /**
     * Deploy a url
     *
     * @param url the deployment url
     * @return the deployment
     * @throws Exception for any error  
     */
    protected KernelDeployment deploy(URL url) throws Exception
    {
       try
       {
          log.debug("Deploying " + url);
          KernelDeployment deployment = deployer.deploy(url);
          log.trace("Deployed " + url);
          return deployment;
       }
       catch (RuntimeException e)
       {
          throw e;
       }
       catch (Exception e)
       {
          throw e;
       }
       catch (Error e)
       {
          throw e;
       }
       catch (Throwable t)
       {
          throw new RuntimeException(t);
       }
    }
    
    /**
     * Deploy a bean
     *
     * @param beanMetaData the bean metadata
     * @return the deployment
     * @throws Exception for any error  
     */
    protected KernelControllerContext deploy(BeanMetaData beanMetaData) throws Exception
    {
       KernelController controller = kernel.getController();
       log.debug("Deploying " + beanMetaData);
       try
       {
          KernelControllerContext result = controller.install(beanMetaData);
          log.debug("Deployed " + result.getName());
          return result;
       }
       catch (Exception e)
       {
          throw e;
       }
       catch (Error e)
       {
          throw e;
       }
       catch (Throwable t)
       {
          throw new RuntimeException("Error deploying bean: " + beanMetaData, t);
       }
    }
    
    /**
     * Deploy a deployment
     *
     * @param deployment the deployment
     * @throws Exception for any error  
     */
    protected void deploy(KernelDeployment deployment) throws Exception
    {
       log.debug("Deploying " + deployment);
       try
       {
          deployer.deploy(deployment);
          log.debug("Deployed " + deployment);
       }
       catch (Exception e)
       {
          throw e;
       }
       catch (Error e)
       {
          throw e;
       }
       catch (Throwable t)
       {
          throw new RuntimeException("Error deploying deployment: " + deployment, t);
       }
    }
    
    /**
     * Undeploy a bean
     *
     * @param context the context
     */
    protected void undeploy(KernelControllerContext context)
    {
       if (context == null)
          throw new IllegalArgumentException("Null context");
       log.debug("Undeploying " + context.getName());
       KernelController controller = kernel.getController();
       controller.uninstall(context.getName());
       log.debug("Undeployed " + context.getName());
    }
 
    /**
     * Undeploy a deployment
     * 
     * @param deployment the deployment
     */
    protected void undeploy(KernelDeployment deployment)
    {
       log.debug("Undeploying " + deployment.getName());
       try
       {
          deployer.undeploy(deployment);
          log.trace("Undeployed " + deployment.getName());
       }
       catch (Throwable t)
       {
          log.warn("Error during undeployment: " + deployment.getName(), t);
       }
    }
    
    /**
     * Undeploy a deployment
     * 
     * @param url the url
     */
    protected void undeploy(URL url)
    {
       log.debug("Undeploying " + url);
       try
       {
          deployer.undeploy(url);
          log.trace("Undeployed " + url);
       }
       catch (Throwable t)
       {
          log.warn("Error during undeployment: " + url, t);
       }
    }
    
    /**
     * Deploy the beans
     * 
     * @throws Exception for any error
     */
    protected void deploy() throws Exception
    {
       String testName = getTestName();
       URL url = getTestResource(testName);
       if (url != null)
          deploy(url);
       else
          log.debug("No test specific deployment " + testName);
    }
 
    protected void shutdown()
    {
       kernel.getController().shutdown();
    }
 
    protected String getTestName()
    {
       String testName = clazz.getName();
       return testName.replace('.', '/') + ".xml";
    }
 
    protected URL getTestResource(String testName)
    {
       return clazz.getClassLoader().getResource(testName);
    }
 
    /**
     * Undeploy all
     */
    protected void undeploy()
    {
       log.debug("Undeploying " + deployer.getDeploymentNames());
       deployer.shutdown();
    }
 }

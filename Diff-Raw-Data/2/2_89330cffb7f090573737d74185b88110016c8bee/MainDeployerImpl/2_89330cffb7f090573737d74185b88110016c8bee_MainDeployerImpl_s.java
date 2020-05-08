 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.deployers.plugins.main;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Arrays;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.jboss.deployers.client.spi.Deployment;
 import org.jboss.deployers.client.spi.main.MainDeployer;
 import org.jboss.deployers.spi.DeploymentException;
 import org.jboss.deployers.spi.DeploymentState;
 import org.jboss.deployers.spi.deployer.Deployers;
 import org.jboss.deployers.spi.deployer.managed.ManagedDeploymentCreator;
 import org.jboss.deployers.structure.spi.DeploymentContext;
 import org.jboss.deployers.structure.spi.DeploymentUnit;
 import org.jboss.deployers.structure.spi.StructuralDeployers;
 import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
 import org.jboss.logging.Logger;
 import org.jboss.managed.api.ManagedDeployment;
 import org.jboss.managed.api.ManagedObject;
 import org.jboss.util.graph.Graph;
 import org.jboss.util.graph.Vertex;
 
 /**
  * MainDeployerImpl.
  *
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @author <a href="scott.stark@jboss.org">Scott Stark</a>
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  * @version $Revision$
  */
 public class MainDeployerImpl implements MainDeployer, MainDeployerStructure
 {
    /** The log */
    private static final Logger log = Logger.getLogger(MainDeployerImpl.class);
 
    /** Whether we are shutdown */
    private AtomicBoolean shutdown = new AtomicBoolean(false);
 
    /** The deployers */
    private Deployers deployers;
 
    /** The structural deployers */
    private StructuralDeployers structuralDeployers;
 
    /** The ManagedDeploymentCreator plugin */
    private ManagedDeploymentCreator mgtDeploymentCreator = null;
 
    /** The deployments by name */
    private Map<String, DeploymentContext> topLevelDeployments = new ConcurrentHashMap<String, DeploymentContext>();
 
    /** All deployments by name */
    private Map<String, DeploymentContext> allDeployments = new ConcurrentHashMap<String, DeploymentContext>();
 
    /** Deployments in error by name */
    private Map<String, DeploymentContext> errorDeployments = new ConcurrentHashMap<String, DeploymentContext>();
 
    /** Deployments missing deployers */
    private Map<String, Deployment> missingDeployers = new ConcurrentHashMap<String, Deployment>();
 
    /** The undeploy work */
    private List<DeploymentContext> undeploy = new CopyOnWriteArrayList<DeploymentContext>();
 
    /** The deploy work */
    private List<DeploymentContext> deploy = new CopyOnWriteArrayList<DeploymentContext>();
 
    /** The process lock */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
 
    /**
     * Get the deployers
     *
     * @return the deployers
     */
    public synchronized Deployers getDeployers()
    {
       return deployers;
    }
 
    /**
     * Set the deployers
     *
     * @param deployers the deployers
     * @throws IllegalArgumentException for null deployers
     */
    public synchronized void setDeployers(Deployers deployers)
    {
       if (deployers == null)
          throw new IllegalArgumentException("Null deployers");
       this.deployers = deployers;
    }
 
    /**
     * Get the structural deployers
     *
     * @return the structural deployers
     */
    public synchronized StructuralDeployers getStructuralDeployers()
    {
       return structuralDeployers;
    }
 
    /**
     * Set the structural deployers
     *
     * @param deployers the deployers
     * @throws IllegalArgumentException for null deployers
     */
    public synchronized void setStructuralDeployers(StructuralDeployers deployers)
    {
       if (deployers == null)
          throw new IllegalArgumentException("Null deployers");
       structuralDeployers = deployers;
    }
 
    public ManagedDeploymentCreator getMgtDeploymentCreator()
    {
       return mgtDeploymentCreator;
    }
 
    public void setMgtDeploymentCreator(ManagedDeploymentCreator mgtDeploymentCreator)
    {
       this.mgtDeploymentCreator = mgtDeploymentCreator;
    }
 
    public Deployment getDeployment(String name)
    {
       DeploymentContext context = getTopLevelDeploymentContext(name);
       if (context == null)
          return null;
       return context.getDeployment();
    }
 
    public DeploymentContext getDeploymentContext(String name)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
 
       return allDeployments.get(name);
    }
 
    public DeploymentContext getDeploymentContext(String name, boolean errorNotFound) throws DeploymentException
    {
       DeploymentContext context = getDeploymentContext(name);
       if (errorNotFound && context == null)
          throw new DeploymentException("Context " + name + " not found");
       return context;
    }
 
    public DeploymentUnit getDeploymentUnit(String name)
    {
       DeploymentContext context = getDeploymentContext(name);
       if (context == null)
          return null;
       return context.getDeploymentUnit();
    }
 
    public DeploymentUnit getDeploymentUnit(String name, boolean errorNotFound) throws DeploymentException
    {
       DeploymentUnit unit = getDeploymentUnit(name);
       if (errorNotFound && unit == null)
          throw new DeploymentException("Unit " + name + " not found");
       return unit;
    }
 
    /**
     * Get a top level deployment context by name
     *
     * @param name the name
     * @return the context
     */
    public DeploymentContext getTopLevelDeploymentContext(String name)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
       return topLevelDeployments.get(name);
    }
 
    public Collection<DeploymentContext> getAll()
    {
       return Collections.unmodifiableCollection(allDeployments.values());
    }
 
    public Collection<DeploymentContext> getErrors()
    {
       return Collections.unmodifiableCollection(errorDeployments.values());
    }
 
    public Collection<Deployment> getMissingDeployer()
    {
       return Collections.unmodifiableCollection(missingDeployers.values());
    }
 
    public Collection<Deployment> getTopLevel()
    {
       List<Deployment> result = new ArrayList<Deployment>();
       for (DeploymentContext context : topLevelDeployments.values())
       {
          Deployment deployment = context.getDeployment();
          if (deployment != null)
             result.add(deployment);
          else
             throw new IllegalStateException("Context has no deployment? " + context.getName());
       }
       return result;
    }
 
    public void addDeployment(Deployment deployment) throws DeploymentException
    {
       addDeployment(deployment, true);
    }
 
    /**
     * Add a deployment
     *
     * @param deployment the deployment
     * @param addToDeploy should we add this deployment to deploy collection
     * @throws DeploymentException for any error
     */
    protected void addDeployment(Deployment deployment, boolean addToDeploy) throws DeploymentException
    {
       if (deployment == null)
          throw new DeploymentException("Null context");
 
       lockRead();
       try
       {
          if (shutdown.get())
             throw new DeploymentException("The main deployer is shutdown");
 
          String name = deployment.getName();
          log.debug("Add deployment: " + name);
 
          DeploymentContext previous = topLevelDeployments.get(name);
          boolean topLevelFound = false;
          if (previous != null)
          {
             log.debug("Removing previous deployment: " + previous.getName());
             removeContext(previous, addToDeploy);
             topLevelFound = true;
          }
 
          if (topLevelFound == false)
          {
             previous = allDeployments.get(name);
             if (previous != null)
                throw new IllegalStateException("Deployment already exists as a subdeployment: " + name);
          }
 
          DeploymentContext context = null;
          try
          {
             context = determineStructure(deployment);
             if (DeploymentState.ERROR.equals(context.getState()))
                errorDeployments.put(name, context);
 
             topLevelDeployments.put(name, context);
             addContext(context, addToDeploy);
          }
          catch (DeploymentException e)
          {
             missingDeployers.put(name, deployment);
             throw e;
          }
          catch (Throwable t)
          {
             // was structure determined?
            if (context != null)
                missingDeployers.put(name, deployment);
 
             throw DeploymentException.rethrowAsDeploymentException("Error determining deployment structure for " + name, t);
          }
       }
       finally
       {
          unlockRead();
       }
    }
 
    public boolean removeDeployment(Deployment deployment) throws DeploymentException
    {
       return removeDeployment(deployment, true);
    }
 
    /**
     * Remove a deployment by name
     *
     * @param deployment thedeployment
     * @param addToUndeploy should we add to undeploy collection
     * @return false when the context was previously unknown
     * @throws DeploymentException for any error
     */
    protected boolean removeDeployment(Deployment deployment, boolean addToUndeploy) throws DeploymentException
    {
       if (deployment == null)
          throw new DeploymentException("Null deployment");
 
       return removeDeployment(deployment.getName(), addToUndeploy);
    }
 
    public boolean removeDeployment(String name) throws DeploymentException
    {
       return removeDeployment(name, true);
    }
 
    /**
     * Remove a deployment by name
     *
     * @param name the name of the deployment
     * @param addToUndeploy should we add to undeploy collection
     * @return false when the context was previously unknown
     * @throws DeploymentException for any error
     */
    protected boolean removeDeployment(String name, boolean addToUndeploy) throws DeploymentException
    {
       if (name == null)
          throw new DeploymentException("Null name");
 
       lockRead();
       try
       {
          if (shutdown.get())
             throw new IllegalStateException("The main deployer is shutdown");
 
          log.debug("Remove deployment context: " + name);
 
          DeploymentContext context = topLevelDeployments.remove(name);
          if (context == null)
             return false;
 
          removeContext(context, addToUndeploy);
 
          return true;
       }
       finally
       {
          unlockRead();
       }
    }
 
    public void deploy(Deployment... deployments) throws DeploymentException
    {
       if (deployments == null)
          throw new IllegalArgumentException("Null deployments.");
 
       lockRead();
       try
       {
          if (shutdown.get())
             throw new IllegalStateException("The main deployer is shutdown");
 
          DeploymentContext[] contexts = new DeploymentContext[deployments.length];
          for(int i = 0; i < deployments.length; i++)
          {
             try
             {
                addDeployment(deployments[i], false);
                DeploymentContext context = getDeploymentContext(deployments[i].getName(), true);
                deployers.process(Collections.singletonList(context), null);
                contexts[i] = context;
             }
             catch(Throwable t)
             {
                DeploymentContext[] deployedContexts = new DeploymentContext[i];
                System.arraycopy(contexts, 0, deployedContexts, 0, i);
                deployers.process(null, Arrays.asList(deployedContexts));
                throw DeploymentException.rethrowAsDeploymentException("Unable to deploy deployments.", t);
             }
          }
          try
          {
             deployers.checkComplete(contexts);
          }
          catch (DeploymentException e)
          {
             deployers.process(null, Arrays.asList(contexts));
             throw e;
          }
       }
       finally
       {
          unlockRead();
       }
    }
 
    public void undeploy(Deployment... deployments) throws DeploymentException
    {
       if (deployments == null)
          throw new IllegalArgumentException("Null deployments.");
 
       lockRead();
       try
       {
          if (shutdown.get())
             throw new IllegalStateException("The main deployer is shutdown");
 
          for(Deployment deployment : deployments)
          {
             DeploymentContext context = getDeploymentContext(deployment.getName());
             if (context != null)
             {
                try
                {
                   removeDeployment(deployment, false);
                   deployers.process(null, Collections.singletonList(context));
                }
                catch (DeploymentException e)
                {
                   if (log.isTraceEnabled())
                      log.trace("Ignored exception while undeploying deployment " + deployment.getName() + ":" + e);
                }
             }
             else if (log.isTraceEnabled())
             {
                log.trace("No such deployment present: " + deployment.getName());
             }
          }
       }
       finally
       {
          unlockRead();
       }
    }
 
    public void undeploy(String... names) throws DeploymentException
    {
       if (names == null)
          throw new IllegalArgumentException("Null names.");
 
       List<Deployment> deployments = new ArrayList<Deployment>();
       for(String name : names)
       {
          DeploymentContext context = getDeploymentContext(name);
          if (context != null)
             deployments.add(context.getDeployment());
          else if (log.isTraceEnabled())
             log.trace("No such deployment present: " + name);
       }
       if (deployments.isEmpty() == false)
          undeploy(deployments.toArray(new Deployment[deployments.size()]));
    }
 
    public void process()
    {
       lockRead();
       try
       {
          if (shutdown.get())
             throw new IllegalStateException("The main deployer is shutdown");
 
          List<DeploymentContext> undeployContexts = null;
          List<DeploymentContext> deployContexts = null;
 
          if (deployers == null)
             throw new IllegalStateException("No deployers");
 
          if (undeploy.isEmpty() == false)
          {
             // Undeploy in reverse order (subdeployments first)
             undeployContexts = new ArrayList<DeploymentContext>(undeploy.size());
             for (int i = undeploy.size() - 1; i >= 0; --i)
                undeployContexts.add(undeploy.get(i));
             undeploy.clear();
          }
          if (deploy.isEmpty() == false)
          {
             deployContexts = new ArrayList<DeploymentContext>(deploy);
             deploy.clear();
          }
 
          if (undeployContexts == null && deployContexts == null)
          {
             log.debug("Asked to process() when there is nothing to do.");
             return;
          }
 
          try
          {
             deployers.process(deployContexts, undeployContexts);
          }
          catch (RuntimeException e)
          {
             throw e;
          }
          catch (Error e)
          {
             throw e;
          }
          catch (Throwable t)
          {
             throw new RuntimeException("Unexpected error in process()", t);
          }
       }
       finally
       {
          unlockRead();
       }
    }
 
    // enable locking - so that we don't pick up current single deployments
    public void shutdown()
    {
       lockWrite();
       try
       {
          while (topLevelDeployments.isEmpty() == false)
          {
             // Remove all the contexts
             for (DeploymentContext context : topLevelDeployments.values())
             {
                topLevelDeployments.remove(context.getName());
                removeContext(context, true);
             }
 
             // Do it
             process();
          }
 
          shutdown.set(true);
       }
       finally
       {
          unlockWrite();
       }
    }
 
    public void checkComplete() throws DeploymentException
    {
       if (deployers == null)
          throw new IllegalStateException("Null deployers");
 
       deployers.checkComplete(errorDeployments.values(), missingDeployers.values());
    }
 
    /**
     * Get the names from deployments.
     *
     * @param deployments the deployments
     * @return depolyment names
     */
    protected static String[] getDeploymentNames(Deployment... deployments)
    {
       if (deployments == null)
          throw new IllegalArgumentException("Null deployments");
 
       String[] names = new String[deployments.length];
       for(int i = 0; i < deployments.length; i++)
       {
          if (deployments[i] == null)
             throw new IllegalArgumentException("Null deployment: " + i);
          names[i] = deployments[i].getName();
       }
       return names;
    }
 
    /**
     * Get the deployment contexts.
     *
     * @param names the deployment names
     * @return depolyment contexts
     * @throws DeploymentException if context is not found
     */
    protected DeploymentContext[] getDeploymentContexts(String... names) throws DeploymentException
    {
       if (names == null)
          throw new IllegalArgumentException("Null names");
 
       DeploymentContext[] contexts = new DeploymentContext[names.length];
       for(int i = 0; i < names.length; i++)
          contexts[i] = getDeploymentContext(names[i], true);
 
       return contexts;
    }
 
    public void checkComplete(Deployment... deployments) throws DeploymentException
    {
       if (deployments == null)
          throw new IllegalArgumentException("Null deployments");
 
       checkComplete(getDeploymentNames(deployments));
    }
 
    public void checkComplete(String... names) throws DeploymentException
    {
       if (names == null)
          throw new IllegalArgumentException("Null names");
 
       if (deployers == null)
          throw new IllegalStateException("Null deployers");
 
       deployers.checkComplete(getDeploymentContexts(names));
    }
 
    public void checkStructureComplete(Deployment... deployments) throws DeploymentException
    {
       if (deployments == null)
          throw new IllegalArgumentException("Null deployments");
 
       checkStructureComplete(getDeploymentNames(deployments));
    }
 
    public void checkStructureComplete(String... names) throws DeploymentException
    {
       if (names == null)
          throw new IllegalArgumentException("Null names");
 
       if (deployers == null)
          throw new IllegalStateException("Null deployers");
 
       deployers.checkStructureComplete(getDeploymentContexts(names));
    }
 
    public DeploymentState getDeploymentState(String name)
    {
       DeploymentContext context = getDeploymentContext(name);
       if (context == null)
          return DeploymentState.UNDEPLOYED;
       return context.getState();
    }
 
    public ManagedDeployment getManagedDeployment(String name) throws DeploymentException
    {
       DeploymentContext context = getDeploymentContext(name, true);
       Map<String, ManagedObject> rootMOs = getManagedObjects(context);
       ManagedDeployment root = mgtDeploymentCreator.build(context.getDeploymentUnit(), rootMOs, null);
       for (DeploymentContext childContext : context.getChildren())
       {
          processManagedDeployment(childContext, root);
       }
       return root;
    }
 
    public Map<String, ManagedObject> getManagedObjects(String name) throws DeploymentException
    {
       DeploymentContext context = getDeploymentContext(name, true);
       return getManagedObjects(context);
    }
 
    public Map<String, ManagedObject> getManagedObjects(DeploymentContext context) throws DeploymentException
    {
       if (context == null)
          throw new IllegalArgumentException("Null context");
 
       if (deployers == null)
          throw new IllegalStateException("No deployers");
 
       return deployers.getManagedObjects(context);
    }
 
    public Graph<Map<String, ManagedObject>> getDeepManagedObjects(String name) throws DeploymentException
    {
       DeploymentContext context = getDeploymentContext(name);
       Graph<Map<String, ManagedObject>> managedObjectsGraph = new Graph<Map<String, ManagedObject>>();
       Vertex<Map<String, ManagedObject>> parent = new Vertex<Map<String, ManagedObject>>(context.getName());
       managedObjectsGraph.setRootVertex(parent);
       Map<String, ManagedObject> managedObjects = getManagedObjects(context);
       parent.setData(managedObjects);
       processManagedObjects(context, managedObjectsGraph, parent);
 
       return managedObjectsGraph;
    }
 
    /**
     * Get the managed objects for a context
     *
     * @param context the context
     * @param graph the graph
     * @param parent the parent node
     * @throws DeploymentException for any problem
     */
    protected void processManagedObjects(DeploymentContext context, Graph<Map<String, ManagedObject>> graph, Vertex<Map<String, ManagedObject>> parent)
       throws DeploymentException
    {
       List<DeploymentContext> children = context.getChildren();
       for(DeploymentContext child : children)
       {
          Vertex<Map<String, ManagedObject>> vertex = new Vertex<Map<String, ManagedObject>>(child.getName());
          Map<String, ManagedObject> managedObjects = getManagedObjects(context);
          vertex.setData(managedObjects);
          graph.addEdge(parent, vertex, 0);
          processManagedObjects(child, graph, vertex);
       }
    }
 
    /**
     * Recursively process the DeploymentContext into ManagedDeployments.
     *
     * @param context the context
     * @param parent the parent
     * @throws DeploymentException for any error
     */
    protected void processManagedDeployment(DeploymentContext context, ManagedDeployment parent)
       throws DeploymentException
    {
       DeploymentUnit unit = context.getDeploymentUnit();
       Map<String, ManagedObject> MOs = getManagedObjects(context);
       ManagedDeployment md = mgtDeploymentCreator.build(unit, MOs, parent);
       for (DeploymentContext childContext : context.getChildren())
       {
          processManagedDeployment(childContext, md);
       }
    }
 
    /**
     * Determine the structure of a deployment
     *
     * @param deployment the deployment
     * @return the deployment context
     * @throws DeploymentException for an error determining the deployment structure
     */
    private DeploymentContext determineStructure(Deployment deployment) throws DeploymentException
    {
       StructuralDeployers structuralDeployers = getStructuralDeployers();
       if (structuralDeployers != null)
       {
           DeploymentContext result = structuralDeployers.determineStructure(deployment);
           if (result != null)
              return result;
       }
       throw new DeploymentException("No structural deployers.");
    }
 
    /**
     * Add a context.
     *
     * @param context the context
     * @param addToDeploy should we add to deploy collection
     */
    private void addContext(DeploymentContext context, boolean addToDeploy)
    {
       allDeployments.put(context.getName(), context);
       if (context.getState() == DeploymentState.ERROR)
       {
          log.debug("Not scheduling addition of context already in error: " + context.getName() + " reason=" + context.getProblem());
          return;
       }
       context.setState(DeploymentState.DEPLOYING);
       DeploymentContext parent = context.getParent();
       log.debug("Scheduling deployment: " + context.getName() + " parent=" + parent);
 
       // Process the top level only
       if (context.isTopLevel() && addToDeploy)
          deploy.add(context);
 
       // Add all the children
       List<DeploymentContext> children = context.getChildren();
       if (children != null)
       {
          for (DeploymentContext child : children)
             addContext(child, addToDeploy);
       }
    }
 
    /**
     * Remove a context
     *
     * @param context the context
     * @param addToUndeploy add to undeploy collection
     */
    private void removeContext(DeploymentContext context, boolean addToUndeploy)
    {
       String name = context.getName();
       allDeployments.remove(name);
       errorDeployments.remove(name);
       missingDeployers.remove(name);
       if (DeploymentState.ERROR.equals(context.getState()) == false)
          context.setState(DeploymentState.UNDEPLOYING);
       DeploymentContext parent = context.getParent();
       log.debug("Scheduling undeployment: " + name + " parent=" + parent);
 
       // Process the top level only
       if (context.isTopLevel() && addToUndeploy)
          undeploy.add(context);
 
       // Remove all the children
       List<DeploymentContext> children = context.getChildren();
       if (children != null)
       {
          for (DeploymentContext child : children)
             removeContext(child, addToUndeploy);
       }
    }
 
    /**
     * Lock for read
     */
    protected void lockRead()
    {
       lock.readLock().lock();
    }
 
    /**
     * Unlock for read
     */
    protected void unlockRead()
    {
       lock.readLock().unlock();
    }
 
    /**
     * Lock for write
     */
    protected void lockWrite()
    {
       lock.writeLock().lock();
    }
 
    /**
     * Unlock for write
     */
    protected void unlockWrite()
    {
       lock.writeLock().unlock();
    }
 }

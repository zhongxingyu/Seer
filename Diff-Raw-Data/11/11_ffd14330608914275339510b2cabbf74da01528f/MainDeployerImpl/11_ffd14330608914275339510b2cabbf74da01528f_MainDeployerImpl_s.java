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
 package org.jboss.deployers.plugins.deployment;
 
import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.jboss.deployers.plugins.deployer.AbstractDeploymentUnit;
 import org.jboss.deployers.plugins.deployer.DeployerWrapper;
 import org.jboss.deployers.plugins.structure.BasicStructuredDeployers;
 import org.jboss.deployers.plugins.structure.DefaultStructureBuilder;
 import org.jboss.deployers.plugins.structure.StructureMetaDataImpl;
 import org.jboss.deployers.spi.DeploymentException;
 import org.jboss.deployers.spi.deployer.Deployer;
 import org.jboss.deployers.spi.deployer.DeploymentUnit;
 import org.jboss.deployers.spi.deployment.MainDeployer;
 import org.jboss.deployers.spi.structure.DeploymentContext;
 import static org.jboss.deployers.spi.structure.DeploymentState.*;
 import static org.jboss.deployers.spi.structure.StructureDetermined.*;
 import org.jboss.deployers.spi.structure.vfs.StructureBuilder;
 import org.jboss.deployers.spi.structure.vfs.StructureDeployer;
 import org.jboss.deployers.spi.structure.vfs.StructureMetaData;
 import org.jboss.logging.Logger;
 import org.jboss.managed.api.ManagedObject;
 import org.jboss.util.graph.Graph;
 import org.jboss.util.graph.Vertex;
 import org.jboss.virtual.VirtualFile;
 
 /**
  * MainDeployerImpl.
  * 
  * TODO full deployer protocol
  * 
  * TODO sort out a proper state machine
  * 
  * TODO implement attachment flow see comment in {@link Deployer}
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @author Scott.Stark@jboss.org
  * @version $Revision: 1.1 $
  */
 public class MainDeployerImpl implements MainDeployer
 {
    /** The log */
    private static final Logger log = Logger.getLogger(MainDeployerImpl.class);
    
    /** Whether we are shutdown */
    private AtomicBoolean shutdown = new AtomicBoolean(false);
    
    /** The structure deployers */
    private BasicStructuredDeployers structureDeployers = new BasicStructuredDeployers();
 
    /** The structure builder that translates structure metadata to deployments */
    private StructureBuilder structureBuilder = new DefaultStructureBuilder();
 
    /** The deployers */
    private SortedSet<DeployerWrapper> deployers = new TreeSet<DeployerWrapper>(Deployer.COMPARATOR);
    
    /** The deployments by name */
    private Map<String, DeploymentContext> topLevelDeployments = new ConcurrentHashMap<String, DeploymentContext>();
    
    /** All deployments by name */
    private Map<String, DeploymentContext> allDeployments = new ConcurrentHashMap<String, DeploymentContext>();
    
    /** Deployments in error by name */
    private Map<String, DeploymentContext> errorDeployments = new ConcurrentHashMap<String, DeploymentContext>();
    
    /** Deployments missing deployers */
    private Map<String, DeploymentContext> missingDeployers = new ConcurrentHashMap<String, DeploymentContext>();
 
    /** The undeploy work */
    private List<DeploymentContext> undeploy = new CopyOnWriteArrayList<DeploymentContext>();
    
    /** The deploy work */
    private List<DeploymentContext> deploy = new CopyOnWriteArrayList<DeploymentContext>();
    
    /**
     * Get the structure deployers
     * 
     * @return the structure deployers
     */
    public synchronized Set<StructureDeployer> getStructureDeployers()
    {
       return structureDeployers.getDeployers();
    }
 
    /**
     * Set the structure deployers
     * 
     * @param deployers the deployers
     * @throws IllegalArgumentException for null deployers
     */
    public synchronized void setStructureDeployers(Set<StructureDeployer> deployers)
    {
       if (deployers == null)
          throw new IllegalArgumentException("Null deployers");
       structureDeployers.setDeployers(deployers);
    }
    
    /**
     * Add a structure deployer
     * 
     * @param deployer the deployer
     */
    public synchronized void addStructureDeployer(StructureDeployer deployer)
    {
       if (deployer == null)
          throw new IllegalArgumentException("Null deployer");
       structureDeployers.addDeployer(deployer);
       // TODO recheck failed deployments
       log.debug("Added structure deployer: " + deployer);
    }
    
    /**
     * Remove a structure deployer
     * 
     * @param deployer the deployer
     */
    public synchronized void removeStructureDeployer(StructureDeployer deployer)
    {
       if (deployer == null)
          throw new IllegalArgumentException("Null deployer");
       structureDeployers.removeDeployer(deployer);
       log.debug("Remove structure deployer: " + deployer);
       // TODO remove deployments for this structure?
    }
    
    /**
     * Get the deployers
     * 
     * @return the deployers
     */
    public synchronized Set<Deployer> getDeployers()
    {
       TreeSet<Deployer> result = new TreeSet<Deployer>(Deployer.COMPARATOR);
       result.addAll(deployers);
       return result;
    }
    
    /**
     * Set the deployers
     * 
     * @param deployers the deployers
     * @throws IllegalArgumentException for null deployers
     */
    public synchronized void setDeployers(Set<Deployer> deployers)
    {
       if (deployers == null)
          throw new IllegalArgumentException("Null deployers");
       
       // Remove all the old deployers that are not in the new set
       HashSet<Deployer> oldDeployers = new HashSet<Deployer>(this.deployers);
       oldDeployers.removeAll(deployers);
       for (Deployer deployer : oldDeployers)
          removeDeployer(deployer);
       
       // Add all the new deployers that were not already present
       HashSet<Deployer> newDeployers = new HashSet<Deployer>(deployers);
       newDeployers.removeAll(this.deployers);
       for (Deployer deployer : newDeployers)
          addDeployer(deployer);
    }
    
    /**
     * Add a deployer
     * 
     * @param deployer the deployer
     */
    public synchronized void addDeployer(Deployer deployer)
    {
       if (deployer == null)
          throw new IllegalArgumentException("Null deployer");
       DeployerWrapper wrapper = new DeployerWrapper(deployer);
       deployers.add(wrapper);
       log.debug("Added deployer: " + deployer);
       // TODO process existing deployments
    }
    
    /**
     * Remove a deployer
     * 
     * @param deployer the deployer
     */
    public synchronized void removeDeployer(Deployer deployer)
    {
       if (deployer == null)
          throw new IllegalArgumentException("Null deployer");
       // TODO - currently unneccessary to wrap, but maybe wrapping changes once
       DeployerWrapper wrapper = new DeployerWrapper(deployer);
       deployers.remove(wrapper);
       log.debug("Removed deployer: " + deployer);
       // TODO unprocess existing deployments
    }
 
    public DeploymentContext getDeploymentContext(String name)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
       return allDeployments.get(name);
    }
 
    public Map<String, ManagedObject> getManagedObjects(DeploymentContext context) throws DeploymentException
    {
       if (context == null)
          throw new IllegalArgumentException("Null context");
       
       Map<String, ManagedObject> managedObjects = new HashMap<String, ManagedObject>();
       for (DeployerWrapper deployer : deployers)
          deployer.build(context.getDeploymentUnit(), managedObjects);
       
       return managedObjects;
    }
    /**
     * 
     */
    public Graph<Map<String, ManagedObject>> getManagedObjects(String name) throws DeploymentException
    {
       DeploymentContext context = getDeploymentContext(name);
       Graph<Map<String, ManagedObject>> managedObjectsGraph = new Graph<Map<String, ManagedObject>>();
       Vertex<Map<String, ManagedObject>> parent = new Vertex<Map<String, ManagedObject>>(context.getName());
       managedObjectsGraph.setRootVertex(parent);
       Map<String, ManagedObject> managedObjects = getManagedObjects(context);
       parent.setData(managedObjects);
       processContext(context, managedObjectsGraph, parent);
       
       return managedObjectsGraph;
    }
    protected void processContext(DeploymentContext context,
          Graph<Map<String, ManagedObject>> graph,
          Vertex<Map<String, ManagedObject>> parent)
       throws DeploymentException
    {
       Set<DeploymentContext> children = context.getChildren();
       for(DeploymentContext child : children)
       {
          Vertex<Map<String, ManagedObject>> vertex = new Vertex<Map<String, ManagedObject>>(child.getName());
          Map<String, ManagedObject> managedObjects = getManagedObjects(context);
          vertex.setData(managedObjects);
          graph.addEdge(parent, vertex, 0);
          processContext(child, graph, vertex);
       }
    }
 
    public StructureBuilder getStructureBuilder()
    {
       return structureBuilder;
    }
    public void setStructureBuilder(StructureBuilder builder)
    {
       this.structureBuilder = builder;
    }
 
    public synchronized void addDeploymentContext(DeploymentContext context) throws DeploymentException
    {
       if (context == null)
          throw new DeploymentException("Null context");
       
       if (shutdown.get())
          throw new DeploymentException("The main deployer is shutdown");
 
       log.debug("Add deployment context: " + context.getName());
       
       if (context.isTopLevel() == false)
          throw new DeploymentException("Context is not a top level deployment: " + context.getName());
       
       String name = context.getName();
       DeploymentContext previous = topLevelDeployments.get(name);
       boolean topLevelFound = false;
       if (previous != null)
       {
          log.debug("Removing previous deployment: " + previous.getName());
          removeContext(previous);
          topLevelFound = true;
       }
 
       if (topLevelFound == false)
       {
          previous = allDeployments.get(name);
          if (previous != null)
             throw new IllegalStateException("Deployment already exists as a subdeployment: " + context.getName()); 
       }
 
       reset(context);
 
       topLevelDeployments.put(name, context);
       try
       {
          determineStructure(context);
       }
       catch (Throwable t)
       {
          log.error("Unable to determine structure of deployment: " + name, t);
          context.setState(ERROR);
          context.setProblem(t);
          errorDeployments.put(name, context);
       }
       
       addContext(context);
    }
 
    public synchronized boolean removeDeploymentContext(String name) throws DeploymentException
    {
       if (name == null)
          throw new DeploymentException("Null name");
 
       if (shutdown.get())
          throw new IllegalStateException("The main deployer is shutdown");
 
       log.debug("Remove deployment context: " + name);
       
       DeploymentContext context = topLevelDeployments.remove(name);
       if (context == null)
          return false;
       
       removeContext(context);
       
       return true;
    }
 
    public Collection<DeploymentContext> getAll()
    {
       return Collections.unmodifiableCollection(allDeployments.values());
    }
 
    public Collection<DeploymentContext> getErrors()
    {
       return Collections.unmodifiableCollection(errorDeployments.values());
    }
 
    public Collection<DeploymentContext> getMissingDeployer()
    {
       return Collections.unmodifiableCollection(missingDeployers.values());
    }
 
    public Collection<DeploymentContext> getTopLevel()
    {
       return Collections.unmodifiableCollection(topLevelDeployments.values());
    }
 
    public void process()
    {
       process(-1, Integer.MAX_VALUE);
    }
    /**
     * Process the new deployments.
     * 
     * TODO: Only deployment uses begin/end. Does it make sense to have
     * partial undeployment?
     * 
     */
    public Collection<DeploymentContext> process(int begin, int end)
    {
       if (shutdown.get())
          throw new IllegalStateException("The main deployer is shutdown");
 
       List<DeploymentContext> undeployContexts = null;
       List<DeploymentContext> deployContexts = null;
       List<DeploymentContext> processedContexts = new ArrayList<DeploymentContext>();
       Deployer[] theDeployers;
       synchronized (this)
       {
          if (deployers.isEmpty())
             throw new IllegalStateException("No deployers");
          if (undeploy.isEmpty() == false)
          {
             // Undeploy in reverse order (subdeployments first)
             undeployContexts = new ArrayList<DeploymentContext>(undeploy.size());
             for (int i = undeploy.size() -1; i >= 0; --i)
                undeployContexts.add(undeploy.get(i));
             undeploy.clear();
          }
          if (deploy.isEmpty() == false)
          {
             deployContexts = new ArrayList<DeploymentContext>(deploy);
          }
          theDeployers = deployers.toArray(new Deployer[deployers.size()]); 
       }
       
       if (undeployContexts != null)
       {
          for (int i = theDeployers.length-1; i >= 0; --i)
          {
             Deployer deployer = theDeployers[i];
             for (DeploymentContext context : undeployContexts)
                prepareUndeploy(deployer, context, true);
          }
          for (DeploymentContext context : undeployContexts)
          {
             // TODO perform with the deployer that created the classloader?
             context.removeClassLoader();
 
             context.setState(UNDEPLOYED);
             log.debug("Undeployed: " + context.getName());
          }
       }
       
       if (deployContexts != null)
       {
          // Restrict deployers to begin/end range
          boolean validDeployersIncludesLastDeployer = false;
          ArrayList<Deployer> validDeployers = new ArrayList<Deployer>();
          for (int i = 0; i < theDeployers.length; ++i)
          {
             Deployer deployer = theDeployers[i];
             int order = deployer.getRelativeOrder();
             // If the deployer is in the [begin, end) range
             if( (begin <= order && order < end) ||
                   // Integer.MAX_VALUE has to be treated specially since its used by deployers
                   // TODO change the default deployer order to Integer.MAX_VALUE-1
                   (begin <= order && end == Integer.MAX_VALUE) )
             {
                validDeployers.add(deployer);
                if( i == theDeployers.length-1 )
                   validDeployersIncludesLastDeployer = true;
             }
          }
          // Clear the deployments if the all deployments
          if (validDeployersIncludesLastDeployer)
             deploy.clear();
 
          for (int i = 0; i < validDeployers.size(); ++i)
          {
             Deployer deployer = validDeployers.get(i);
 
             Set<DeploymentContext> errors = new HashSet<DeploymentContext>();
             for (DeploymentContext context : deployContexts)
             {
                try
                {
                   Set<DeploymentContext> components = context.getComponents();
                   commitDeploy(deployer, context, components);
                }
                catch (DeploymentException e)
                {
                   context.setState(ERROR);
                   context.setProblem(e);
                   errors.add(context);
                   errorDeployments.put(context.getName(), context);
                   // Unwind the deployment
                   for (int j = i-1; j >= 0; --j)
                   {
                      Deployer other = validDeployers.get(j);
                      prepareUndeploy(other, context, true);
                   }
                   context.removeClassLoader();
                }
             }
             deployContexts.removeAll(errors);
          }
          for (DeploymentContext context : deployContexts)
          {
             String name = context.getName();
             // TODO Need some metadata that says we expect a deployment to only provide classes
             boolean isJar = false;
             VirtualFile root = context.getRoot();
             if (root != null && root.getName().endsWith(".jar"))
                isJar = true;
             if (validDeployersIncludesLastDeployer && context.isDeployed() == false && isJar == false)
                missingDeployers.put(name, context);
             // TODO: Should there be a PARTIAL_DEPLOYED state for end < max deployer
             context.setState(DEPLOYED);
             processedContexts.add(context);
             log.debug("Deployed: " + name);
          }
       }
       return processedContexts;
    }
 
    private void prepareUndeploy(Deployer deployer, DeploymentContext context, boolean doComponents)
    {
       DeploymentUnit unit = context.getDeploymentUnit();
       deployer.prepareUndeploy(unit);
       
       if (doComponents)
       {
          Set<DeploymentContext> components = context.getComponents();
          if (components != null && components.isEmpty() == false)
          {
             DeploymentContext[] theComponents = components.toArray(new DeploymentContext[components.size()]);
             for (int i = theComponents.length-1; i >= 0; --i)
                prepareUndeploy(deployer, theComponents[i], true);
          }
       }
    }
    
    private void commitDeploy(Deployer deployer, DeploymentContext context, Set<DeploymentContext> components) throws DeploymentException
    {
       DeploymentContext[] theComponents = null;
       if (components != null && components.isEmpty() == false)
          theComponents = components.toArray(new DeploymentContext[components.size()]);
       
       DeploymentUnit unit = context.getDeploymentUnit();
       deployer.commitDeploy(unit);
       
       try
       {
          if (theComponents != null)
          {
             for (int i = 0; i < theComponents.length; ++i)
             {
                try
                {
                   Set<DeploymentContext> componentComponents = theComponents[i].getComponents();
                   commitDeploy(deployer, theComponents[i], componentComponents);
                }
                catch (DeploymentException e)
                {
                   // Unwind the previous components
                   for (int j = i-1; j >=0; --j)
                      prepareUndeploy(deployer, theComponents[j], true);
                   throw e;
                }
             }
          }
       }
       catch (DeploymentException e)
       {
          prepareUndeploy(deployer, context, false);
          throw e;
       }
    }
    
    public void shutdown()
    {
       while (topLevelDeployments.isEmpty() == false)
       {
          // Remove all the contexts
          for (DeploymentContext context : topLevelDeployments.values())
          {
             topLevelDeployments.remove(context.getName());
             removeContext(context);
          }
          
          // Do it
          process();
       }
       
       shutdown.set(true);
    }
 
    /**
     * Reset a deployment context
     * 
     * @param context the context
     * @throws IllegalArgumentException for a null context
     */
    protected void reset(DeploymentContext context)
    {
       if (context == null)
          throw new IllegalArgumentException("Null context");
 
       if (context.getStructureDetermined() == YES)
          context.setStructureDetermined(NO);
       context.setProblem(null);
       context.reset();
    }
 
    /**
     * Determine the deployment structure
     * 
     * @param context the context
     */
    private void determineStructure(DeploymentContext context)
       throws DeploymentException
    {
       if (context.getStructureDetermined() == PREDETERMINED)
          return;
 
       if (context.getRoot() == null)
          throw new DeploymentException("Unable to determine structure context has not root " + context.getName());
       
       synchronized (this)
       {
          if (structureDeployers.isEmpty())
             throw new IllegalStateException("No structure deployers");
       }
       VirtualFile root = context.getRoot();
 
       // TODO: does the StructureMetaData impl need to be externalized?
       StructureMetaData metaData = new StructureMetaDataImpl();
       boolean result = structureDeployers.determineStructure(root, metaData);
       if (result == false)
          throw new DeploymentException("No structural deployer recognised the deployment. " + context.getName());
       // Build the deployment context from the structure metadata
       structureBuilder.populateContext(context, metaData);
    }
 
    /**
     * Determine the structure
     * 
     * @param context the context
     * @param theDeployers the deployers
     * @return true when determined
     * @throws DeploymentException for any problem
    private boolean determineStructure(DeploymentContext context) throws DeploymentException
    {
       boolean trace = log.isTraceEnabled();
       if (trace)
          log.trace("Trying to determine structure: " + context.getName());
 
       boolean result = this.structureDeployers.determineStructure(context);
       for (StructureDeployer deployer : theDeployers)
       {
          if (deployer.determineStructure(context, deployers))
          {
             result = true;
             break;
          }
       }
       if (result == false && context.isCandidate() == false)
          throw new DeploymentException("No structural deployer recognised the deployment. " + context.getName());
       
       Set<DeploymentContext> children = context.getChildren();
       for (DeploymentContext child : children)
       {
          if (child.getRoot() == null)
             throw new DeploymentException("Unable to determine structure context has no root: " + context.getName());
          
          // This must be a candidate that doesn't match
          if (determineStructure(child, theDeployers) == false)
             context.removeChild(child);
       }
       
       return result;
    }
    */
    
    /**
     * Add a context
     * 
     * @param context the context
     */
    private void addContext(DeploymentContext context)
    {
       allDeployments.put(context.getName(), context);
       if (context.getState() == ERROR)
       {
          log.debug("Not scheduling addition of context already in error: " + context.getName());
          return;
       }
       context.setDeploymentUnit(new AbstractDeploymentUnit(context));
       context.setState(DEPLOYING);
       log.debug("Scheduling deployment: " + context.getName());
       deploy.add(context);
       
       // Add all the children
       Set<DeploymentContext> children = context.getChildren();
       if (children != null)
       {
          for (DeploymentContext child : children)
             addContext(child);
       }
    }
    
    /**
     * Remove a context
     * 
     * @param context the context
     */
    private void removeContext(DeploymentContext context)
    {
       String name = context.getName();
       allDeployments.remove(name);
       errorDeployments.remove(name);
       missingDeployers.remove(name);
       if (context.getState() == ERROR)
       {
          log.debug("Not scheduling removal of context already in error: " + name);
          return;
       }
       context.setState(UNDEPLOYING);
       log.debug("Scheduling undeployment: " + name);
       undeploy.add(context);
       
       // Remove all the children
       Set<DeploymentContext> children = context.getChildren();
       if (children != null)
       {
          for (DeploymentContext child : children)
             removeContext(child);
       }
    }
 }

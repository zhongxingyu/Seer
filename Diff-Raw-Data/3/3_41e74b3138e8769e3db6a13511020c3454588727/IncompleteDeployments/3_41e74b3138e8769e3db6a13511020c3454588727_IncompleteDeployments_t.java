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
 package org.jboss.deployers.client.spi;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.HashMap;
 
 /**
  * IncompleteDeployments.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class IncompleteDeployments implements Serializable
 {
    /** The serialVersionUID */
    private static final long serialVersionUID = -8413355643801749950L;
 
    /** Deployments in error */
    private Map<String, Throwable> deploymentsInError;
 
    /** Deployments missing deployer */
    private Collection<String> deploymentsMissingDeployer;
 
    /** Contexts in error */
    private Map<String, Throwable> contextsInError;
 
    /** Contexts missing dependencies */
    private Map<String, Set<MissingDependency>> contextsMissingDependencies;
 
    /** Deployments in error info */
    private String deploymentsInErrorInfo;
 
    /** Missing deployers info */
    private String deploymentsMissingDeployerInfo;
 
    /** Contexts in error info */
    private String contextsInErrorInfo;
 
    /** Contexts missing dependencies info */
    private String contextsMissingDependenciesInfo;
 
    /**
     * Create a new IncompleteDeploymentException.
     * 
     * @param deploymentsInError deployments in error
     * @param deploymentsMissingDeployer deployments missing deployer
     * @param contextsInError contexts in error
     * @param contextsMissingDependencies contexts missing dependencies
     */
    public IncompleteDeployments(Map<String, Throwable> deploymentsInError, Collection<String> deploymentsMissingDeployer, Map<String, Throwable> contextsInError, Map<String, Set<MissingDependency>> contextsMissingDependencies)
    {
       if (deploymentsInError != null && deploymentsInError.isEmpty() == false)
       {
          this.deploymentsInError = new TreeMap<String, Throwable>();
          this.deploymentsInError.putAll(deploymentsInError);
       }
       if (deploymentsMissingDeployer != null && deploymentsMissingDeployer.isEmpty() == false)
       {
          this.deploymentsMissingDeployer = new TreeSet<String>();
          this.deploymentsMissingDeployer.addAll(deploymentsMissingDeployer);
       }
       if (contextsInError != null && contextsInError.isEmpty() == false)
       {
          this.contextsInError = new TreeMap<String, Throwable>();
          this.contextsInError.putAll(contextsInError);
       }
       if (contextsMissingDependencies != null && contextsMissingDependencies.isEmpty() == false)
       {
          this.contextsMissingDependencies = new TreeMap<String, Set<MissingDependency>>();
          this.contextsMissingDependencies.putAll(contextsMissingDependencies);
       }
    }
 
    /**
     * Whether it is incomplete
     * 
     * @return true when incomplete
     */
    public boolean isIncomplete()
    {
       if (deploymentsInError != null)
          return true;
       if (deploymentsMissingDeployer != null )
          return true;
       if (contextsInError != null)
          return true;
       if (contextsMissingDependencies != null)
          return true;
       return false;
    }
 
    /**
     * Whether deployment unit is responsible for incomplete deployment.
     *
     * @param deploymentName deployment unit name
     * @return true when deployment unit is responsible
     */
    public boolean isInvalidDeployment(String deploymentName)
    {
       if (isIncomplete() == false)
          return false;
 
       if (matchComponentName(deploymentName, getDeploymentsInError().keySet()))
          return true;
       if (matchComponentName(deploymentName, getDeploymentsMissingDeployer()))
          return true;
 
       return false;
    }
 
    /**
     * Whether context is responsible for incomplete deployment.
     *
     * @param contextName context's name
     * @return true when context is responsible
     */
    public boolean isInvalidContext(String contextName)
    {
       if (isIncomplete() == false)
          return false;
 
       if (matchComponentName(contextName, getContextsInError().keySet()))
          return true;
       if (matchComponentName(contextName, getContextsMissingDependencies().keySet()))
          return true;
 
       return false;
    }
 
    /**
     * Get the info about deployments in error.
     *
     * @return string info
     */
    public String getDeploymentsInErrorInfo()
    {
       if (deploymentsInErrorInfo == null)
       {
          StringBuilder buffer = new StringBuilder();
          // Display all the incomplete deployments
          Map<String, Throwable> deploymentsInError = getDeploymentsInError();
          if (deploymentsInError.isEmpty() == false)
          {
             buffer.append("\n*** DEPLOYMENTS IN ERROR: Name -> Error\n\n");
             for (Map.Entry<String, Throwable> entry : deploymentsInError.entrySet())
                buffer.append(entry.getKey()).append(" -> ").append(entry.getValue().toString()).append("\n\n");
          }
          deploymentsInErrorInfo = buffer.toString();
       }
       return deploymentsInErrorInfo;
    }
 
    /**
     * Get the info about missing deployers.
     *
     * @return string info
     */
    public String getDeploymentsMissingDeployerInfo()
    {
       if (deploymentsMissingDeployerInfo == null)
       {
          StringBuilder buffer = new StringBuilder();
          // Display all the missing deployers
          Collection<String> deploymentsMissingDeployers = getDeploymentsMissingDeployer();
          if (deploymentsMissingDeployers.isEmpty() == false)
          {
             buffer.append("\n*** DEPLOYMENTS MISSING DEPLOYERS: Name\n\n");
             for (String name : deploymentsMissingDeployers)
                buffer.append(name).append('\n');
          }
          deploymentsMissingDeployerInfo = buffer.toString();
       }
       return deploymentsMissingDeployerInfo;
    }
 
    /**
     * Calculate upfront context errors.
     */
    protected void calculateContextsError()
    {
       // Popluate the potential root causes
       Map<String, String> rootCauses = new HashMap<String, String>();
 
       // Missing dependencies are root causes
       Map<String, Set<MissingDependency>> contextsMissingDependencies = getContextsMissingDependencies();
       if (contextsMissingDependencies.isEmpty() == false)
       {
          for (Map.Entry<String, Set<MissingDependency>> entry : contextsMissingDependencies.entrySet())
          {
             for (MissingDependency dependency : entry.getValue())
                rootCauses.put(dependency.getDependency(), dependency.getActualState());
          }
       }
 
       // Errors are root causes
       Map<String, Throwable> contextsInError = getContextsInError();
       if (contextsInError.isEmpty() == false)
       {
          for (Map.Entry<String, Throwable> entry : contextsInError.entrySet())
          {
             Throwable t = entry.getValue();
             if (t == null)
                rootCauses.put(entry.getKey(), "** UNKNOWN ERROR **");
             else
                rootCauses.put(entry.getKey(), t.toString());
          }
       }
 
       StringBuilder buffer = new StringBuilder();
 
       // Display all the missing dependencies
       if (contextsMissingDependencies.isEmpty() == false)
       {
          buffer.append("\n*** CONTEXTS MISSING DEPENDENCIES: Name -> Dependency{Required State:Actual State}\n\n");
          for (Map.Entry<String, Set<MissingDependency>> entry : contextsMissingDependencies.entrySet())
          {
             String name = entry.getKey();
             buffer.append(name).append("\n");
             for (MissingDependency dependency : entry.getValue())
             {
                buffer.append(" -> ").append(dependency.getDependency());
                buffer.append('{').append(dependency.getRequiredState());
                buffer.append(':').append(dependency.getActualState()).append("}");
                buffer.append("\n");
             }
             buffer.append('\n');
 
             // It is not a root cause if it has missing dependencies
             rootCauses.remove(name);
          }
       }
       contextsMissingDependenciesInfo = buffer.toString();
 
       // reset buffer
       buffer.setLength(0);
 
       if (rootCauses.isEmpty() == false)
       {
          buffer.append("\n*** CONTEXTS IN ERROR: Name -> Error\n\n");
          for (Map.Entry<String, String> entry : rootCauses.entrySet())
             buffer.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n\n");
       }
       contextsInErrorInfo = buffer.toString();
    }
 
    /**
     * Get the contexts in error info.
     *
     * @return string info
     */
    public String getContextsInErrorInfo()
    {
       if (contextsInErrorInfo == null)
          calculateContextsError();
 
       return contextsInErrorInfo;
    }
 
    /**
     * Get the contexts missing dependecies info
    * 
    * @return the contexts missing dependency
     */
    public String getContextsMissingDependenciesInfo()
    {
       if (contextsMissingDependenciesInfo == null)
          calculateContextsError();
 
       return contextsMissingDependenciesInfo;
    }
 
    /**
     * Search for componentName in strings.
     *
     * @param componentName component's name
     * @param strings collection of strings
     * @return true if strings contains component name
     */
    protected boolean matchComponentName(String componentName, Collection<String> strings)
    {
       return strings.contains(componentName);
    }
 
    /**
     * Get the contextsInError.
     * 
     * @return the contextsInError.
     */
    public Map<String, Throwable> getContextsInError()
    {
       if (contextsInError == null)
          return Collections.emptyMap();
       else
          return Collections.unmodifiableMap(contextsInError);
    }
 
    /**
     * Get the contextsMissingDependencies.
     * 
     * @return the contextsMissingDependencies.
     */
    public Map<String, Set<MissingDependency>> getContextsMissingDependencies()
    {
       if (contextsMissingDependencies == null)
          return Collections.emptyMap();
       else
          return Collections.unmodifiableMap(contextsMissingDependencies);
    }
 
    /**
     * Get the deploymentsInError.
     * 
     * @return the deploymentsInError.
     */
    public Map<String, Throwable> getDeploymentsInError()
    {
       if (deploymentsInError == null)
          return Collections.emptyMap();
       else
          return Collections.unmodifiableMap(deploymentsInError);
    }
 
    /**
     * Get the deploymentsMissingDeployer.
     * 
     * @return the deploymentsMissingDeployer.
     */
    public Collection<String> getDeploymentsMissingDeployer()
    {
       if (deploymentsMissingDeployer == null)
          return Collections.emptySet();
       else
          return Collections.unmodifiableCollection(deploymentsMissingDeployer);
    }
 }

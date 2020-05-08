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
 package org.jboss.deployers.spi;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * IncompleteDeploymentException.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class IncompleteDeploymentException extends DeploymentException
 {
    /** The serialVersionUID */
    private static final long serialVersionUID = 1433292979582684692L;
 
    /** Incomplete deployments */
    private IncompleteDeployments incompleteDeployments;
 
    /**
     * For serialization
     */
    public IncompleteDeploymentException()
    {
    }
    
    /**
     * Create a new IncompleteDeploymentException.
     * 
     * @param incompleteDeployments the incomplete deployments
     * @throws IllegalArgumentException for null incompleteDeployments
     */
    public IncompleteDeploymentException(IncompleteDeployments incompleteDeployments)
    {
       if (incompleteDeployments == null)
          throw new IllegalArgumentException("Null incompleteDeployments");
       this.incompleteDeployments = incompleteDeployments;
    }
 
    /**
     * Get the incompleteDeployments.
     * 
     * @return the incompleteDeployments.
     */
    public IncompleteDeployments getIncompleteDeployments()
    {
       return incompleteDeployments;
    }
 
   // TODO Some of the calculations done in this method should be done upfront in IncompleteDeployments instead!
   @Override
    public String getMessage()
    {
       StringBuilder buffer = new StringBuilder();
       buffer.append("Summary of incomplete deployments (SEE PREVIOUS ERRORS FOR DETAILS):\n");
 
       // Display all the missing deployers
       Collection<String> deploymentsMissingDeployers = incompleteDeployments.getDeploymentsMissingDeployer();
       if (deploymentsMissingDeployers.isEmpty() == false)
       {
          buffer.append("\n*** DEPLOYMENTS MISSING DEPLOYERS: Name\n\n");
          for (String name : deploymentsMissingDeployers)
             buffer.append(name).append('\n');
       }
 
       // Display all the incomplete deployments
       Map<String, Throwable> deploymentsInError = incompleteDeployments.getDeploymentsInError();
       if (deploymentsInError.isEmpty() == false)
       {
          buffer.append("\n*** DEPLOYMENTS IN ERROR: Name -> Error\n\n");
          for (Map.Entry<String, Throwable> entry : deploymentsInError.entrySet())
             buffer.append(entry.getKey()).append(" -> ").append(entry.getValue().toString()).append("\n\n");
       }
 
       // Popluate the potential root causes
       Map<String, String> rootCauses = new HashMap<String, String>();
 
       // Errors are root causes
       Map<String, Throwable> contextsInError = incompleteDeployments.getContextsInError();
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
       // Missing dependencies are root causes
       Map<String, Set<MissingDependency>> contextsMissingDependencies = incompleteDeployments.getContextsMissingDependencies();
       if (contextsMissingDependencies.isEmpty() == false)
       {
          for (Map.Entry<String, Set<MissingDependency>> entry : contextsMissingDependencies.entrySet())
          {
             for (MissingDependency dependency : entry.getValue())
                rootCauses.put(dependency.getDependency(), dependency.getActualState());
          }
       }
 
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
       if (rootCauses.isEmpty() == false)
       {
          buffer.append("\n*** CONTEXTS IN ERROR: Name -> Error\n\n");
          for (Map.Entry<String, String> entry : rootCauses.entrySet())
             buffer.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n\n");
       }
       return buffer.toString();
    }
 }

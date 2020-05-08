 /**************************************************************************************
  * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
  * http://fusesource.com                                                              *
  * ---------------------------------------------------------------------------------- *
  * The software in this package is published under the terms of the AGPL license      *
  * a copy of which has been included with this distribution in the license.txt file.  *
  **************************************************************************************/
 package org.fusesource.meshkeeper.distribution.provisioner;
 
 /**
  * MeshProvisioner
  * <p>
  * Interface used for deploying a meshkeeper control server and agents.
  * 
  * </p>
  * 
  * @author cmacnaug
  * @version 1.0
  */
 public interface Provisioner {
 
     /**
      * MeshProvisioningException
      * <p>
      * Description:
      * </p>
      * 
      * @author cmacnaug
      * @version 1.0
      */
     public static class MeshProvisioningException extends Exception {
         private static final long serialVersionUID = 1L;
 
         public MeshProvisioningException(String reason) {
             super(reason);
         }
 
         public MeshProvisioningException(String reason, Throwable cause) {
            super(reason, cause);
         }
     }
 
     /**
      * Sets the deployment uri. This is implementation specific, and and may be
      * used to connect to a remote provisioning agent.
      * 
      * @param agentHosts The deployment uri.
      */
     public void setDeploymentUri(String uri);
 
     /**
      * Gets the deployment Uri.
      */
     public String getDeploymentUri();
 
     /**
      * Set the preferred hostname on which to deploy the MeshKeeper controller.
      * @param the preferred control agent hostname
      */
     public void setPreferredControlHost(String preferredControlHost);
 
     /**
      * @return the preferred hostname on which to deploy the MeshKeeper
      *         controller.
      */
     public String getPreferredControlHost();
 
     /**
      * Sets the requested set of hostnames on which to deploy a meshkeeper
      * agent.
      * 
      * @param agentHosts
      *            the requested set of hostnames on which to deploy a meshkeeper
      *            agent.
      */
     public void setRequestedAgentHosts(String[] agentHosts);
 
     /**
      * Sets the request set of hostnames on which to deploy a meshkeeper agent.
      * 
      * @param agentHosts
      *            the requested set of hostnames on which to deploy a meshkeeper
      *            agent
      */
     public String[] getRequestedAgentHosts();
 
     /**
      * Find the registry connect uri which can be used to connect to the
      * provisionned meshkeeper controller.
      * 
      * @return the registry connect uri which can be used to connect to the
      *         provisionned meshkeeper controller
      * 
      * @throws MeshProvisioningException
      *             If there is an error determining the control uri.
      */
     public String findMeshRegistryUri() throws MeshProvisioningException;
 
     /**
      * Find the registry connect uri which can be used to connect to the
      * provisionned meshkeeper controller.
      * 
      * @return <code>true</code> if meshkeeper is deployed.
      * 
      * @throws MeshProvisioningException
      *             If there is an error determining the provisioning status
      */
     public boolean isDeployed() throws MeshProvisioningException;
 
     /**
      * Deploys a MeshKeeper controller and Agents.
      * 
      * @throws MeshProvisioningException
      *             If there is an error deploying meshkeeper.
      */
     public void deploy() throws MeshProvisioningException;
 
     /**
      * Undeploys meshkeeper.
      * 
      * @param force
      *            If true will undeploy meshkeeper even if it is in use.
      * 
      * @throws MeshProvisioningException
      *             If there is an error undeploying meshkeeper.
      */
     public void unDeploy(boolean force) throws MeshProvisioningException;
 
     /**
      * Undeploys and deploys meshkeeper.
      * 
      * @param force
      *            If true will undeploy meshkeeper even if it is in use.
      * 
      * @throws MeshProvisioningException
      *             If there is an error redeploying meshkeeper.
      */
     public void reDeploy(boolean force) throws MeshProvisioningException;
 
     /**
      * Gets a status summary of deployed MeshKeeper components.
      * 
      * @param buffer
      *            the buffer to dump the summary into.
      * 
      * @throws MeshProvisioningException
      *             If there is an error getting the status.
      */
     public StringBuffer getStatus(StringBuffer buffer) throws MeshProvisioningException;
 }

 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.ode.axis2.deploy;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.ode.axis2.ODEServer;
 import org.apache.ode.bpel.iapi.ProcessState;
 import org.intalio.deploy.deployment.ComponentId;
 import org.intalio.deploy.deployment.DeploymentMessage;
 import org.intalio.deploy.deployment.DeploymentMessage.Level;
 import org.intalio.deploy.deployment.spi.ComponentManager;
 import org.intalio.deploy.deployment.spi.ComponentManagerResult;
 import org.intalio.deploy.deployment.utils.DeploymentServiceRegister;
 
 import javax.xml.namespace.QName;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * The new deployment service based deployment poller. On start() or stop(), this object
  * starts or stops the default deployment poller plus, registering or un-registering itself to the 
  * deployment service.
  * <p>
  * The naming of the deployment unit has been changed to:
  * <p>
  * &lt;assembly_name&gt;-&lt;assembly_version&gt;/&lt;component_name&gt;
  * <p>
  * 
  * The assembly version is stripped off when the version is 0. The component name does
  * not include the component type.
  */
 public class ProcessComponentManager extends DeploymentPoller implements ComponentManager {
     private final static Log __log = LogFactory.getLog(ProcessComponentManager.class);
     private DeploymentServiceRegister register;
     
     private final static String COMPONENT_MANAGER_NAME = "ode"; 
 
     public ProcessComponentManager(File deployDir, ODEServer odeServer) {
     	super(deployDir, odeServer);
     }
 
     public void start() {
     	super.start();
 
     	// register the component manager to deployment service
     	if( register != null ) {
     		register.destroy();
     		register = null;
     	}
     	register = new DeploymentServiceRegister(this);
     	register.init();
     	
         __log.info("ProcessComponentManager started.");
     }
 
     public void stop() {
     	// un-register the component manager from deployment service
     	if( register != null ) {
     		register.destroy();
     		register = null;
     	}
     	
     	super.stop();
     }
 
     @Override
     protected boolean isDeploymentFromODEFileSystemAllowed() {
     	return !Boolean.getBoolean("org.intalio.pxe.disable.oldfsdeploy");
     }
 
     public ComponentManagerResult deploy(ComponentId name, File path, boolean activate) {
 		List<DeploymentMessage> messages = new ArrayList<DeploymentMessage>();
 		List<String> deployedResources = new ArrayList<String>();
 		
 		/**
 		 * 1. scan the path and compile processes
 		 * 2. retire all old revisions
 		 * 3. if "activate" is false, the new revision is set to "retired"
 		 */
         try {
         	String duName = resolvePackageNameFromComponentId(name);
            Collection<QName> deployed = _odeServer.getProcessStore().deploy(path, activate);
             __log.info("Deployment of artifact " + path.getName() + " successful: " + deployed );
             deployedResources.add(duName);
         } catch (Exception e) {
             __log.error("Deployment of " + path.getAbsolutePath() + " failed, aborting for now.", e);
             messages.add(new DeploymentMessage(Level.ERROR, e.getMessage()));
         }
         
         return new ComponentManagerResult(messages, deployedResources);
 	}
 
 	public void deployed(ComponentId cid, File path, List<String> deployedResources, boolean active) {
 		// nothing to do
 	}
 
 	public void initialize(ComponentId cid, File path, List<String> deployedResources, boolean active) {
 		// nothing to do
 	}
 
 	public void start(ComponentId cid, File path, List<String> deployedResources, boolean active) {
 		// start up the scheduler if not started up yet, not yet implemented
 	}
 
 	public void stop(ComponentId cid, File path, List<String> deployedResources, boolean active) {
 		// stop the scheduler if this is the last kind of process component, not yet implemented
 	}
 
 	public void dispose(ComponentId cid, File path, List<String> deployedResources, boolean active) {
 		// nothing to do
 	}
 
 	public void undeploy(ComponentId cid, File path, List<String> deployedResources) {
 		if( deployedResources.isEmpty() ) {
 			throw new IllegalArgumentException("Deployed resources that were passed in from Deployment Service do not contain ODE deployment unit name!!");
 		}
 		_odeServer.getProcessStore().undeploy(deployedResources.iterator().next());
 	}
 
 	public void undeployed(ComponentId cid, File path, List<String> deployedResources) {
 		// nothing to do
 	}
 
 	public String getComponentManagerName() {
 		return COMPONENT_MANAGER_NAME;
 	}
 
 	public void activate(ComponentId cid, File path, List<String> deployedResources) {
 		// activate all processes belonging to the component
 		for( QName pid : _odeServer.getProcessStore().listProcesses(resolvePackageNameFromComponentId(cid)) ) {
 			_odeServer.getProcessStore().setState(pid, ProcessState.ACTIVE);
 		}
 	}
 
 	public void activated(ComponentId cid, File path, List<String> deployedResources) {
 		// nothing to do
 	}
 
 	public void retire(ComponentId cid, File path, List<String> deployedResources) {
 		// retire all processes belonging to the component
 		for( QName pid : _odeServer.getProcessStore().listProcesses(resolvePackageNameFromComponentId(cid)) ) {
 			_odeServer.getProcessStore().setState(pid, ProcessState.RETIRED);
 		}
 	}
 
 	public void retired(ComponentId cid, File path, List<String> deployedResources) {
 		// nothing to do
 	}
 	
 	protected String resolvePackageNameFromComponentId(ComponentId name) {
 		return name.getAssemblyId() + "/" + name.getComponentName();
 	}
 	
 	public void finalize() {
 		stop();
 	}
 }

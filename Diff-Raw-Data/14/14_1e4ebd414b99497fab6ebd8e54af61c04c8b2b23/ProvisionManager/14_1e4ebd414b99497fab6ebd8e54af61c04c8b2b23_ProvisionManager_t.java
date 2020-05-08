 /*
  * Copyright 2012 the original author or authors.
  * Copyright 2012 SorcerSoft.org.
  *  
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.core.dispatch;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.jini.config.Configuration;
 import net.jini.config.ConfigurationException;
 import net.jini.config.ConfigurationProvider;
 
 import org.rioproject.deploy.DeployAdmin;
 import org.rioproject.monitor.ProvisionMonitor;
 import org.rioproject.opstring.OpString;
 import org.rioproject.opstring.OpStringLoader;
 import org.rioproject.opstring.OperationalString;
 import org.rioproject.opstring.OperationalStringManager;
 import org.rioproject.opstring.ServiceElement;
 
 import sorcer.core.exertion.NetTask;
import sorcer.service.Accessor;
 import sorcer.service.Exertion;
 import sorcer.service.Signature;
 import sorcer.util.Sorcer;
 
 /**
  * @author Mike Sobolewski
  */
 public class ProvisionManager {
 	private static final Logger logger = Logger.getLogger(ProvisionManager.class.getName());
 	private final Exertion exertion;
 	private final Configuration config;
 	private OperationalStringManager opStringManager;
 	private DeployAdmin deployAdmin;
 	private String opStringName;
 	
 	public ProvisionManager(final Exertion exertion, String... configuration) throws ConfigurationException {
 		this.exertion = exertion;
 //		System.out.println("ZZZZZZZZZZZZZZZ ProvisionManger configuration: " + Arrays.toString(configuration));
 		config = ConfigurationProvider.getInstance(configuration);
 	}
 	
 	private Iterable<Signature> getSignatures() {
 		List<Signature> signatures = new ArrayList<Signature>();
 		for(Exertion e : exertion.getAllExertions()) {			
 			if(e instanceof NetTask) {
 				signatures.add(e.getProcessSignature());
 			}
 		}
 		return signatures;
 	}
 	
 	private OperationalString getIGridDeployment() throws Exception {
		File iGridDeployment = new File(Sorcer.getHomeDir(), "configs/rio/SorcerCommon.groovy");
 		OpStringLoader opStringLoader = new OpStringLoader(); 
 		OperationalString[] loaded = opStringLoader.parseOperationalString(iGridDeployment);
 		return loaded[0];
 	}
 	
 	public void deployServices() throws DispatcherException {
 		try {
 			List<ServiceElement> services = new ArrayList<ServiceElement>();
 			for(Signature signature : getSignatures()) {				
 				ServiceElement service = (ServiceElement) config.getEntry("sorcer.core.exertion.deployment", 
 						                                                  "service", 
 						                                                  ServiceElement.class, 
 						                                                  null, 
 						                                                  signature.getServiceType().getName());
 				if(service!=null) {
 					services.add(service);
 				} else {
 					logger.warning("Configuration returned NULL for Signature service type: "+signature.getServiceType().getName());
 				}							                                                 
 			}
 			if(!services.isEmpty()) {
 				StringBuilder nameBuilder = new StringBuilder();
 				nameBuilder.append(exertion.getName()).append("-").append(System.getProperty("user.name"));
 				opStringName = nameBuilder.toString();
 				OpString opstring = new OpString(opStringName, null);
 				for(ServiceElement service : services) {
 					service.setOperationalStringName(opStringName);
 					opstring.addService(service);
 				}
 				opstring.addOperationalString(getIGridDeployment());
 
				ProvisionMonitor provisionMonitor = Accessor.getService(ProvisionMonitor.class);
 				if (provisionMonitor != null) {
 					deployAdmin = (DeployAdmin) provisionMonitor.getAdmin();
 					deployAdmin.deploy(opstring);
 					opStringName = opstring.getName();
 					opStringManager = deployAdmin.getOperationalStringManager(opStringName);
 				} else {
 					logger.warning(String.format("Unable to obtain a ProvisionMonitor for %s", exertion.getName()));
 				}
 			} else {
 				logger.warning(String.format("Unable to obtain a ServiceElement for %s", exertion.getName()));
 			}
 		} catch (Exception e) {
 			throw new DispatcherException(String.format("While trying to provision exertion %s", 
 					                                    exertion.getName()), 
 					                      e);
 		}
 	}
 	
 	public void undeploy() {
 		if(deployAdmin!=null) {
 			try {
 				deployAdmin.undeploy(opStringName);
 			} catch (Exception e) {
 				logger.log(Level.WARNING, "Unable to undeply "+opStringName, e);
 			}
 		} 
 	}
 	
 	public OperationalStringManager getOperationalStringManager() {
 		return opStringManager;
 	}
 }

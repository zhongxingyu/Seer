 /**
  * Copyright (C) 2011 / cube-team <https://cube.forge.osor.eu>
  *
  * Licensed under the Apache License, Version 2.0 (the "License").
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ch.admin.vbs.cube.core.vm.list;
 
 import java.net.ConnectException;
 import java.net.SocketTimeoutException;
 import java.security.KeyStore.Builder;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.net.ssl.SSLHandshakeException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.admin.vbs.cube.common.CubeClassification;
 import ch.admin.vbs.cube.common.MachineUuid;
 import ch.admin.vbs.cube.common.container.Container;
 import ch.admin.vbs.cube.core.vm.Vm;
 import ch.admin.vbs.cube.core.vm.VmModel;
 import ch.admin.vbs.cube.core.vm.list.VmDescriptor.RemoteConfig;
 import ch.admin.vbs.cube.core.webservice.WebServiceFactory;
 import cube.cubemanager.services.CubeManagerServicePortType;
 import cube.cubemanager.services.InstanceDescriptorDTO;
 import cube.cubemanager.services.MachineDTO;
 
 /**
  * This class is used to update the DescriptorModel with the WebService
  */
 public class WSDescriptorUpdater implements Runnable {
 	/** Logger */
 	private static final Logger LOG = LoggerFactory.getLogger(WSDescriptorUpdater.class);
 	private static final long REFRESH_TIMEOUT = 30000;
 	private static final long CONNECTION_REFUSED_TIMEOUT = 60000;
 	private Thread thread;
 	private final VmModel model;
 	private CubeManagerServicePortType srv;
 	private final Builder builder;
 	private MachineUuid machineId;
 	private boolean running;
 	private WebServiceFactory factory;
 	private boolean connected;
 	private boolean enabled = true;
 
 	/**
 	 * @param model
 	 *            model to update
 	 * @param builder
 	 *            necessary for SSL
 	 */
 	public WSDescriptorUpdater(VmModel model, Builder builder) {
 		this.model = model;
 		this.builder = builder;
 	}
 
 	public void start() {
 		machineId = MachineUuid.getMachineUuid();
 		// start web service client thread
 		thread = new Thread(this, "WSDescriptorUpdater");
 		thread.setDaemon(false);
 		running = true;
 		thread.start();
 	}
 
 	public void stop() {
 		running = false;
 	}
 
 	@Override
 	public void run() {
 		while (running) {
 			long t0;
 			// ensure WS is connected
 			connectWebService();
 			try {
 				if (srv != null) {
 					// request descriptors from WS
 					MachineDTO mid = new MachineDTO();
 					mid.setMachineUid(machineId.getUuidAsString());
 					t0 = System.currentTimeMillis();
 					HashMap<String, InstanceDescriptorDTO> remoteIndex = index(srv.listUserVm(mid));
 					LOG.debug("WebService call [{} ms]", System.currentTimeMillis() - t0);
 					// check all VMs already listed in the model
 					for (Vm vm : model.getVmList()) {
 						InstanceDescriptorDTO x = remoteIndex.remove(vm.getId());
 						if (x == null) {
 							// this VM is in the model (locally) but not in the
 							// server listing. It may be deleted on the server.
 							// Do not delete it locally if already staged to
 							// avoid loosing user data in case of error.
 							if (!staged(vm)) {
 								// remove
 								LOG.debug("VM {} not on the remote side (and not staged). remove fro model", vm.getId());
 								model.removeVm(vm);
 							} else {
 								LOG.debug("VM {} not on the remote side (but already staged). do not update", vm.getId());
 							}
 						} else {
 							// VM is present on the server. update values if
 							// necessary and eventually notify listenes for
 							// updates.
 							if (updateRemoteDescriptor(vm, x)) {
 								// change(s) detected
 								LOG.debug("VM {} different on the server than locally. Update local copy.", vm.getId());
 								model.fireVmUpdatedEvent(vm);
 							}
 						}
 					}
 					connected = true;
 					// VM on the server and not locally available
 					for (Object o : remoteIndex.values()) {
 						// not on local side
 						VmDescriptor d = createDescriptor((InstanceDescriptorDTO) o);
 						LOG.debug("VM {} only on server side. synchronize.", d.getRemoteCfg().getId());
 						model.addVm(new Vm(d));
 					}
 				}
 			} catch (Exception e) {
 				connected = false;
 				srv = null; // ensure reconnect next time
 				if (running) {
 					LOG.error("Failed to update model with webservice", e);
 					// handle unrecoverable errors. exit loop to avoid filling
 					// logs with exceptions.
 					if (handeConnecionRefusedException(e)) {
 						LOG.debug("Connection refused. Try later.");
 						try {
 							Thread.sleep(CONNECTION_REFUSED_TIMEOUT);
 						} catch (InterruptedException e1) {
 						}
 					} else if (handeConnecionTimeoutException(e)) {
 						enabled = false;
 					} else if (handeBadCertificateException(e)) {
 						LOG.debug("Unrecoverable error. Stop web service.");
 						running = false;
 					}
 				}
 			}
 			// wait next loop
 			try {
 				Thread.sleep(REFRESH_TIMEOUT);
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	private boolean staged(Vm vm) {
 		VmDescriptor desc = vm.getDescriptor();
 		Container vmCnt = null;
 		Container rtCnt = null;
 		if (desc.getLocalCfg().getVmContainerUid() != null) {
 			vmCnt = Container.initContainerObject(desc.getLocalCfg().getVmContainerUid());
 		}
 		if (desc.getLocalCfg().getRuntimeContainerUid() != null) {
 			rtCnt = Container.initContainerObject(desc.getLocalCfg().getRuntimeContainerUid());
 		}
 		return vmCnt != null && rtCnt != null && vm.getRuntimeContainer().exists() && vm.getVmContainer().exists();
 	}
 
 	private boolean checkEquals(String a, String b) {
 		if (a == null && b == null) {
 			return true;
 		} else if (a != null && b != null) {
 			return a.equals(b);
 		} else {
 			return false;
 		}
 	}
 
 	private boolean updateRemoteDescriptor(Vm vm, InstanceDescriptorDTO desc) {
 		int changes = 0;
 		RemoteConfig rcfg = vm.getDescriptor().getRemoteCfg();
 		if (!checkEquals(rcfg.getCfgVersion(), desc.getTemplateVersion())) {
 			changes++;
 			rcfg.setName(desc.getName());
 			rcfg.setDescription(desc.getDescription());
 			rcfg.setDomain(desc.getSecurityDomain());
 			rcfg.setClassification(CubeClassification.valueOf(desc.getClassification()));
 			rcfg.setType(desc.getType());
 			rcfg.setCfgVersion(desc.getTemplateVersion());
 		}
 		return changes > 0;
 	}
 
 	private boolean handeBadCertificateException(Exception e) {
 		Throwable t = e;
 		while (t != null) {
 			if (t instanceof SSLHandshakeException && "Received fatal alert: bad_certificate".equals(t.getMessage())) {
 				return true;
 			}
 			t = t.getCause();
 		}
 		return false;
 	}
 
 	private boolean handeConnecionRefusedException(Exception e) {
 		Throwable t = e;
 		while (t != null) {
 			if (t instanceof ConnectException && "Connection refused".equals(t.getMessage())) {
 				return true;
 			}
 			t = t.getCause();
 		}
 		return false;
 	}
 	
 	private boolean handeConnecionTimeoutException(Exception e) {
 		Throwable t = e;
 		while (t != null) {
 			if (t instanceof SocketTimeoutException) {
 				return true;
 			}
 			t = t.getCause();
 		}
 		return false;
 	}
 
 	/** Create a VmDescriptor based on a DTO object. */
 	private VmDescriptor createDescriptor(InstanceDescriptorDTO dto) {
 		if (dto == null) {
 			return null;
 		} else {
 			VmDescriptor desc = new VmDescriptor();
 			desc.getRemoteCfg().setClassification(CubeClassification.valueOf(dto.getClassification()));
 			desc.getRemoteCfg().setDomain(dto.getSecurityDomain());
 			desc.getRemoteCfg().setName(dto.getName());
 			desc.getRemoteCfg().setType(dto.getType());
 			desc.getRemoteCfg().setCfgVersion(dto.getTemplateVersion());
 			desc.getRemoteCfg().setId(dto.getUuid());
 			return desc;
 		}
 	}
 
 	private HashMap<String, InstanceDescriptorDTO> index(List<InstanceDescriptorDTO> remote) {
 		HashMap<String, InstanceDescriptorDTO> map = new HashMap<String, InstanceDescriptorDTO>();
 		for (InstanceDescriptorDTO d : remote) {
 			map.put(d.getUuid(), d);
 		}
 		return map;
 	}
 
 	public boolean isConnected() {
 		return connected;
 	}
 
 	private void connectWebService() {
 		if (enabled) {
 			if (srv == null) {
 				try {
 					factory = new WebServiceFactory(builder);
 					srv = factory.createCubeManagerService();
 				} catch (Exception e) {
 					connected = false;
 					srv = null;
 					LOG.error("Failed to init webservice client. Server probably unreachable.", e);
 				}
 			}
 		} else {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void enable(boolean enabled) {
 		this.enabled = enabled;
 	}
 }

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
 
 package ch.admin.vbs.cube.core.network.vpn;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.admin.vbs.cube.common.keyring.IKeyring;
 import ch.admin.vbs.cube.common.keyring.SafeFile;
 import ch.admin.vbs.cube.common.shell.ScriptUtil;
 import ch.admin.vbs.cube.common.shell.ShellUtil;
 import ch.admin.vbs.cube.core.network.INetworkManager;
 import ch.admin.vbs.cube.core.network.INetworkManager.Listener;
 import ch.admin.vbs.cube.core.network.INetworkManager.NetworkConnectionState;
 import ch.admin.vbs.cube.core.network.vpn.VpnConfig.VpnOption;
 import ch.admin.vbs.cube.core.vm.Vm;
 import ch.admin.vbs.cube.core.vm.VmException;
 import ch.admin.vbs.cube.core.webservice.InstanceParameterHelper;
 import cube.cubemanager.services.InstanceConfigurationDTO;
 
 public class VpnManager {
 	private static final Logger LOG = LoggerFactory.getLogger(VpnManager.class);
 	private ExecutorService exs = Executors.newCachedThreadPool();
 	private HashMap<String, CacheEntry> vpnCache = new HashMap<String, CacheEntry>();
 	private INetworkManager networkManager;
 
 	public void start() {
 		networkManager.addListener(new Listener() {
 			@Override
 			public void stateChanged(NetworkConnectionState old, NetworkConnectionState state) {
 				// copy of cache
 				Collection<CacheEntry> vpns = new ArrayList<VpnManager.CacheEntry>();
 				synchronized (vpnCache) {
 					vpns.addAll(vpnCache.values());
 				}
 				switch (state) {
 				case CONNECTED_TO_CUBE_BY_VPN:
 				case CONNECTED_TO_CUBE:
 					/* repoen all VM's VPNs. (in separated theads) */
 					LOG.debug("NetworkManager switch to CONNECTED. Restart VPNs [{}].", vpns.size());
 					for (final CacheEntry e : vpns) {
 						exs.execute(new Runnable() {
 							@Override
 							public void run() {
 								LOG.debug("reopen vpn...");
 								if (e.keyring.isOpen()) {
 									try {
 										openVpn(e.vm, e.keyring, e.listener);
 									} catch (VmException e1) {
 										LOG.error("Failed to re-open VPN");
 									}
 								} else {
 									// keyring has been closed in the mean time
 									LOG.error("Failed to re-open VPN because Keyring is closed.");
 								}
 							}
 						});
 					}
 					break;
 				case CONNECTING:
 					break;
 				case CONNECTING_VPN:
 					break;
 				case NOT_CONNECTED:
 					LOG.debug("Disconnect [{}] VM's VPN (because NetworkManager is disconnected))", vpns.size());
 					for (final CacheEntry e : vpns) {
 						exs.execute(new Runnable() {
 							@Override
 							public void run() {
 								try {
 									closeVpn(e.vm, true);
 								} catch (VmException e1) {
 									LOG.error("Failed to close VM's VPN");
 								}
 							}
 						});
 					}
 					break;
 				default:
 					break;
 				}
 			}
 		});
 	}
 
 	/**
	 * During stagging, th vpnmanager will just sotre its config in runtime
 	 * container.
 	 * 
 	 * @param vm
 	 * @param remoteCfg
 	 * @param keyring
 	 */
 	public void stagging(Vm vm, InstanceConfigurationDTO remoteCfg, IKeyring keyring) throws VmException {
 		try {
 			VpnConfig cfg = new VpnConfig(vm.getVmContainer(), vm.getRuntimeContainer());
 			cfg.load();
 			if (InstanceParameterHelper.getInstanceParameterAsBoolean("vpn.enabled", remoteCfg)) {
 				// update 'cfg' with values from 'remoteCfg'
 				cfg.setOption(VpnOption.Tap, InstanceParameterHelper.getInstanceParameter("vpn.tap", remoteCfg));
 				cfg.setOption(VpnOption.Name, InstanceParameterHelper.getInstanceParameter("vpn.name", remoteCfg));
 				cfg.setOption(VpnOption.Description, InstanceParameterHelper.getInstanceParameter("vpn.description", remoteCfg));
 				cfg.setOption(VpnOption.Hostname, InstanceParameterHelper.getInstanceParameter("vpn.hostname", remoteCfg));
 				cfg.setOption(VpnOption.Port, InstanceParameterHelper.getInstanceParameter("vpn.port", remoteCfg));
 				keyring.storeData(InstanceParameterHelper.getInstanceParameter("vpn.clientKey", remoteCfg).getBytes("UTF-8"), vm.getId() + "."
 						+ VpnOption.ClientKey.getName());
 				keyring.storeData(InstanceParameterHelper.getInstanceParameter("vpn.clientCert", remoteCfg).getBytes("UTF-8"), vm.getId() + "."
 						+ VpnOption.ClientCert.getName());
 				keyring.storeData(InstanceParameterHelper.getInstanceParameter("vpn.caCert", remoteCfg).getBytes("UTF-8"),
 						vm.getId() + "." + VpnOption.CaCert.getName());
 				cfg.setOption(VpnOption.Enabled, "true");
 			} else {
 				// reset all vpn config values
 				for (VpnOption o : VpnOption.values()) {
 					cfg.setOption(o, null);
 				}
 				cfg.setOption(VpnOption.Enabled, "false");
 			}
 			cfg.save();
 		} catch (Exception e) {
 			throw new VmException("Failed to stage VirtualBox VM.", e);
 		}
 	}
 
 	public void openVpn(final Vm vm, final IKeyring keyring, final VpnListener l) throws VmException {
 		exs.execute(new Runnable() {
 			/*
 			 * vpn-open.pl block until tunnel opening succeed or fail. Therefore
 			 * we start it in another thread.
 			 */
 			@Override
 			public void run() {
 				try {
 					l.connecting();
 					// load configuration
 					VpnConfig cfg = new VpnConfig(vm.getVmContainer(), vm.getRuntimeContainer());
 					cfg.load();
 					// check if VPN is enabled
 					if (!cfg.getOptionAsBoolean(VpnOption.Enabled)) {
 						return;
 					}
 					// hold in cache in order to be able to restart it later.
 					synchronized (vpnCache) {
 						vpnCache.put(vm.getId(), new CacheEntry(vm, keyring, l));
 					}
 					// retrieve keys from keyring
 					SafeFile tmpKey = keyring.retrieveDataAsFile(vm.getId() + "." + VpnOption.ClientKey.getName());
 					SafeFile tmpCert = keyring.retrieveDataAsFile(vm.getId() + "." + VpnOption.ClientCert.getName());
 					SafeFile tmpCa = keyring.retrieveDataAsFile(vm.getId() + "." + VpnOption.CaCert.getName());
 					if (!tmpKey.exists() || !tmpCert.exists() || !tmpCa.exists())
 						throw new VmException("Cert/keys have not been decrypted correctly");
 					// open VPN tunnel
 					ScriptUtil script = new ScriptUtil();
 					ShellUtil su = script.execute("sudo", "./vpn-open.pl", //
 							"--tap", cfg.getOption(VpnOption.Tap),//
 							"--hostname", cfg.getOption(VpnOption.Hostname),//
 							"--port", cfg.getOption(VpnOption.Port),//
 							"--ca", tmpCa.getAbsolutePath(),//
 							"--cert", tmpCert.getAbsolutePath(),//
 							"--key", tmpKey.getAbsolutePath() //
 							);
 					// shred keys since they are no more needed
 					tmpKey.shred();
 					tmpCa.shred();
 					tmpCert.shred();
 					//
 					if (su.getExitValue() == 0) {
 						LOG.debug("VPN opened");
 						// VPN opening succeed
 						l.opened();
 					} else {
 						LOG.warn("VPN failed");
 						// VPN failed
 						l.failed();
 					}
 				} catch (Exception e) {
 					LOG.error("Failed to start VPN connection", e);
 					l.failed();
 				}
 			}
 		});
 	}
 
 	public void closeVpn(Vm vm) throws VmException {
 		closeVpn(vm, false);
 	}
 
 	public void closeVpn(Vm vm, boolean keepInCache) throws VmException {
 		try {
 			CacheEntry e = null;
 			synchronized (vpnCache) {
 				if (keepInCache) {
 					e = vpnCache.get(vm.getId());
 				} else {
 					e = vpnCache.remove(vm.getId());
 				}
 			}
 			// load configuration
 			VpnConfig cfg = new VpnConfig(vm.getVmContainer(), vm.getRuntimeContainer());
 			cfg.load();
 			// check if vpn is enabled
 			if (!cfg.getOptionAsBoolean(VpnOption.Enabled)) {
 				return;
 			}
 			// close VPN tunnel
 			ScriptUtil script = new ScriptUtil();
 			script.execute("sudo", "./vpn-close.pl", //
 					"--tap", cfg.getOption(VpnOption.Tap)//
 			);
 			LOG.debug("VPN closed.");
 			if (e != null) {
 				e.listener.closed();
 			}
 		} catch (Exception e) {
 			throw new VmException("Failed to close VPN.", e);
 		}
 	}
 
 	public void setNetworkManager(INetworkManager networkManager) {
 		this.networkManager = networkManager;
 	}
 
 	private class CacheEntry {
 		private final VpnListener listener;
 		private final Vm vm;
 		private final IKeyring keyring;
 
 		public CacheEntry(Vm vm, IKeyring keyring, VpnListener listener) {
 			this.vm = vm;
 			this.keyring = keyring;
 			this.listener = listener;
 		}
 	}
 
 	public static interface VpnListener {
 		void connecting();
 
 		void closed();
 
 		void opened();
 
 		void failed();
 	}
 }

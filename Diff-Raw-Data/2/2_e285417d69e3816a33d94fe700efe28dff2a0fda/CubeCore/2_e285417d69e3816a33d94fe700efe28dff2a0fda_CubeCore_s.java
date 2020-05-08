 /**
  * Copyright (C) 2011 / manhattan <https://cube.forge.osor.eu>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
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
 package ch.admin.vbs.cube.core.impl;
 
 import java.net.URL;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.admin.vbs.cube.common.RelativeFile;
 import ch.admin.vbs.cube.core.IClientFacade;
 import ch.admin.vbs.cube.core.ICoreFacade;
 import ch.admin.vbs.cube.core.ILoginUI;
 import ch.admin.vbs.cube.core.ISession;
 import ch.admin.vbs.cube.core.ISession.IOption;
 import ch.admin.vbs.cube.core.ISession.VmCommand;
 import ch.admin.vbs.cube.core.ISessionManager;
 import ch.admin.vbs.cube.core.ISessionManager.ISessionManagerListener;
 import ch.admin.vbs.cube.core.ISessionUI;
 import ch.admin.vbs.cube.core.IUICallback;
 import ch.admin.vbs.cube.core.usb.UsbDevice;
 import ch.admin.vbs.cube.core.usb.UsbDeviceEntryList;
 import ch.admin.vbs.cube.core.vm.IVmModelChangeListener;
 import ch.admin.vbs.cube.core.vm.IVmStateChangeListener;
 import ch.admin.vbs.cube.core.vm.Vm;
 import ch.admin.vbs.cube.core.vm.VmModel;
 
 /**
  * CubeCore wrap around the SessionManager (which manages multi-sessions). It
  * maintains a reference on the current active session and make sure that only
  * this session may access the UI.
  * 
  * It also make sure that ILoginUI has priority over ISessionUI calls.
  * 
  * It react to VM events of the active session (model, state) and trigger UI
  * refresh.
  * 
  */
 public class CubeCore implements ICoreFacade, ISessionUI, ILoginUI, ISessionManagerListener, IVmModelChangeListener, IVmStateChangeListener {
 	/*
 	 * in 'login' mode, only login UI dialogs (message, enter UI dialog) are
 	 * displayed. In 'session' mode, VM and session generated message are
 	 * displayed to user.
 	 * 
 	 * In shutdown mode, no UI update are allowed aymore. Only CallbackShutdown,
 	 * which have a direct reference to IClientFacade, may display messages.
 	 */
 	private enum Mode {
 		LOGIN, SESSION, SHUTDOWN
 	}
 
 	/** Logger */
 	private static final Logger LOG = LoggerFactory.getLogger(CubeCore.class);
 	private IClientFacade clientFacade;
 	private ISessionManager smanager;
 	private ISession actSession; // active session
 	private Mode mode = Mode.LOGIN;
 	private IUICallback currentCallback;
 	private Object uiLock = new Object();
 
 	/**
 	 * Set current active session. Derergister-register event listener
 	 */
 	private void setActiveSession(ISession session) {
 		if (actSession != null) {
 			actSession.getModel().removeModelChangeListener(this);
 			actSession.getModel().removeStateChangeListener(this);
 		}
 		actSession = session;
 		if (session != null) {
 			session.getModel().addModelChangeListener(this);
 			session.getModel().addStateChangeListener(this);
 		}
 	}
 
 	private void setCurrentCallback(IUICallback callback) {
 		if (currentCallback != null) {
 			LOG.debug("Abort overwritten callback [{}]", currentCallback);
 			currentCallback.aborted();
 			currentCallback = null;
 		}
 		// logs...
 		if (callback != currentCallback) {
 			LOG.debug("Set current callback [{}]", callback);
 		}
 		// set current callback
 		currentCallback = callback;
 	}
 
 	// ==============================================
 	// ISessionManagerListener
 	// ==============================================
 	@Override
 	public void sessionOpened(ISession session) {
 		synchronized (uiLock) {
 			// set the new opened session the active one
 			setActiveSession(session);
 		}
 	}
 
 	@Override
 	public void sessionClosed(ISession session) {
 		synchronized (uiLock) {
 			if (actSession != null && actSession == session) {
 				setActiveSession(null);
 			}
 		}
 	}
 
 	@Override
 	public void sessionLocked(ISession session) {
 		synchronized (uiLock) {
 			if (actSession != null && actSession == session) {
 				LOG.debug("active session [{}] locked", session.getId());
 				setActiveSession(null);
 			}
 		}
 	}
 
 	// ==============================================
 	// IVmStatelListener (of active session only)
 	// ==============================================
 	@Override
 	public void vmStateUpdated(Vm vm) {
 		synchronized (uiLock) {
 			if (mode == Mode.SESSION) {
 				refreshSingleVmUI(vm);
 			}
 		}
 	}
 
 	// ==============================================
 	// ICoreFacade
 	// ==============================================
 	@Override
 	public void enteredPassword(char[] password, String requestId) {
 		synchronized (uiLock) {
 			if (currentCallback != null && currentCallback.getId().equals(requestId) && currentCallback instanceof CallbackPin) {
 				CallbackPin cb = (CallbackPin) currentCallback;
 				if (password == null || password.length == 0) {
 					LOG.warn("submitted password is null. CallbackPin will be aborted.");
 					// user do not enter its password or press cancel
 					cb.aborted();
 				} else {
 					// proceed login request with submitted password
 					cb.setPassword(password);
 					cb.process();
 				}
 				currentCallback = null;
 			} else {
 				LOG.warn("Invalid callback [{}] [{}]", requestId, currentCallback);
 			}
 		}
 	}
 
 	@Override
 	public void enteredConfirmation(int result, String requestId) {
 		synchronized (uiLock) {
 			if (currentCallback != null && currentCallback.getId().equals(requestId)) {
 				if (result > 0) {
 					if (currentCallback instanceof CallbackShutdown) {
 						mode = Mode.SHUTDOWN;
 					}
 					currentCallback.process();
 				} else {
 					currentCallback.aborted();
 				}
 				currentCallback = null;
 				displayVmsOfActiveSession();
 			} else {
 				LOG.warn("Invalid callback got[{}]  expected[{}]", requestId, currentCallback);
 			}
 		}
 	}
 
 	@Override
 	public void enteredUsbDevice(UsbDevice device, String requestId) {
 		synchronized (uiLock) {
 			if (currentCallback != null && currentCallback.getId().equals(requestId) && currentCallback instanceof CallbackUsb) {
 				CallbackUsb cb = (CallbackUsb) currentCallback;
 				if (device == null) {
 					// user do not enter its password or press cancel
 					cb.aborted();
 				} else {
 					// proceed login request with submitted password
 					cb.setDevice(device);
 					cb.process();
 				}
 				currentCallback = null;
 				displayVmsOfActiveSession();
 			} else {
 				LOG.warn("Invalid callback [{}] [{}]", requestId, currentCallback);
 			}
 		}
 	}
 
 	private void controlVm(String vmId, VmCommand cmd) {
 		controlVm(vmId, cmd, null);
 	}
 
 	private void controlVm(String vmId, VmCommand cmd, IOption option) {
 		if (actSession != null && mode == Mode.SESSION) {
 			if (cmd == VmCommand.DELETE) {
 				// ask confirmation first (handler will execute the command)
 				setCurrentCallback(new CallbackDeleteVm(vmId, actSession));
 				clientFacade.askConfirmation("messagedialog.confirmation.deleteVmConfirmation", currentCallback.getId());
 			} else {
 				// execute command
 				actSession.controlVm(vmId, cmd, option);
 			}
 		}
 	}
 
 	@Override
 	public void startVm(String vmId) {
 		synchronized (uiLock) {
 			controlVm(vmId, VmCommand.START);
 		}
 	}
 
 	@Override
 	public void standByVm(String vmId) {
 		synchronized (uiLock) {
 			controlVm(vmId, VmCommand.SAVE);
 		}
 	}
 
 	@Override
 	public void powerOffVm(String vmId) {
 		synchronized (uiLock) {
 			controlVm(vmId, VmCommand.POWER_OFF);
 		}
 	}
 
 	@Override
 	public void stageVm(String vmId, URL location) {
 		synchronized (uiLock) {
 			controlVm(vmId, VmCommand.STAGE);
 		}
 	}
 
 	@Override
 	public void logoutUser() {
 		synchronized (uiLock) {
 			if (actSession != null && mode == Mode.SESSION) {
 				LOG.debug("Set Logout callback");
 				setCurrentCallback(new CallbackLogout(actSession, smanager));
 				clientFacade.askConfirmation("messagedialog.confirmation.closeSessionConfirmation", currentCallback.getId());
 			} else {
 				LOG.warn("invalid session or mode");
 			}
 		}
 	}
 
 	@Override
 	public void fileTransfer(RelativeFile fileName, String vmIdFrom, String vmIdTo) {
 		LOG.error("FileTransfer not implemented");
 	}
 
 	@Override
 	public void cleanUpExportFolder(String vmId) {
 		LOG.error("cleanup not implemented");
 	}
 
 	@Override
 	public void deleteVm(String vmId) {
 		synchronized (uiLock) {
 			controlVm(vmId, VmCommand.DELETE);
 		}
 	}
 
 	@Override
 	public void installGuestAdditions(String vmId) {
 		synchronized (uiLock) {
 			controlVm(vmId, VmCommand.INSTALL_GUESTADDITIONS);
 		}
 	}
 
 	@Override
 	public void lockCube() {
 		LOG.error("lockCube not implemented");
 	}
 
 	@Override
 	public void shutdownMachine() {
 		synchronized (uiLock) {
 			// ask confirmation first (handler will execute the command)
 			setCurrentCallback(new CallbackShutdown(smanager, clientFacade));
 			clientFacade.askConfirmation("messagedialog.confirmation.shutdownCubeConfirmation", currentCallback.getId());
 		}
 	}
 
 	public void attachUsbDevice(String vmId, UsbDevice usbDevice) {
 		synchronized (uiLock) {
 			controlVm(vmId, VmCommand.ATTACH_USB, usbDevice);
 		}
 	}
 
 	public void detachUsbDevice(String vmId, UsbDevice usbDevice) {
 		synchronized (uiLock) {
 			controlVm(vmId, VmCommand.DETACH_USB, usbDevice);
 		}
 	}
 
 	// public List<UsbDeviceEntry> getUsbDevices(String vmId) {
 	// synchronized (uiLock) {
 	// UsbDeviceEntryList list = new UsbDeviceEntryList();
 	// controlVm(vmId, VmCommand.LIST_USB, list);
 	// return list;
 	// }
 	// }
 	public void setup(IClientFacade clientFacade, ISessionManager smanager) {
 		this.clientFacade = clientFacade;
 		this.smanager = smanager;
 		//
 		smanager.addListener(this);
 	}
 
 	@Override
 	public void attachUsbDeviceRequest(String vmId) {
 		synchronized (uiLock) {
 			if (actSession != null && mode == Mode.SESSION) {
 				setCurrentCallback(new CallbackUsb(actSession, vmId));
 				UsbDeviceEntryList list = new UsbDeviceEntryList();
 				controlVm(vmId, VmCommand.LIST_USB, list);
 				clientFacade.showUsbDeviceChooser(list, currentCallback.getId());
 			}
 		}
 	}
 
 	@Override
 	public UsbDeviceEntryList getUsbDeviceList(String vmId) {
 		synchronized (uiLock) {
 			UsbDeviceEntryList list = new UsbDeviceEntryList();
 			if (actSession != null && mode == Mode.SESSION) {
 				controlVm(vmId, VmCommand.LIST_USB, list);
 				long to = System.currentTimeMillis()+1000;
				while(!list.isUpdated() && to<System.currentTimeMillis()) {
 					try {
 						Thread.sleep(42);
 					} catch (InterruptedException e) {
 					}
 				}
 				LOG.debug("Got a list in [{} ms] with "+list.size()+" element(s).",to-System.currentTimeMillis());
 				return list;
 			} else {
 				LOG.debug("Core locked or no session active.");
 				return list;
 			}
 		}
 	}
 
 	// ==============================================
 	// ILoginUI
 	// ==============================================
 	@Override
 	public void closeDialog() {
 		synchronized (uiLock) {
 			if (mode == Mode.LOGIN || mode == Mode.SESSION) {
 				setCurrentCallback(null);
 				// set UI in 'session' mode
 				mode = Mode.SESSION;
 				// display current session VMs
 				displayVmsOfActiveSession();
 			}
 		}
 	}
 
 	@Override
 	public void showDialog(String message, LoginDialogType type) {
 		synchronized (uiLock) {
 			if (mode == Mode.LOGIN || mode == Mode.SESSION) {
 				setCurrentCallback(null);
 				// set UI in 'login' mode
 				mode = Mode.LOGIN;
 				// display dialog
 				switch (type) {
 				case NO_OPTION:
 					clientFacade.showMessage(message, IClientFacade.OPTION_NONE);
 					break;
 				case SHUTDOW_OPTION:
 					clientFacade.showMessage(message, IClientFacade.OPTION_SHUTDOWN);
 					break;
 				default:
 					LOG.error("Bad option [" + type + "]");
 					break;
 				}
 			} else {
 				LOG.debug("Do not show ILogin dialog [{}]", mode);
 			}
 		}
 	}
 
 	@Override
 	public void showPinDialog(String message, CallbackPin cb) {
 		synchronized (uiLock) {
 			if (mode == Mode.LOGIN || mode == Mode.SESSION) {
 				// set UI in 'login' mode
 				mode = Mode.LOGIN;
 				// display dialog
 				setCurrentCallback(cb);
 				clientFacade.showGetPIN(message, cb.getId());
 			}
 		}
 	}
 	
 	@Override
 	public void setVmProperty(String vmId, String key, String value, boolean refreshAllVms) {
 		// do not synchronize on uiLock since we do not change the UI directly.
 		if (actSession != null && mode == Mode.SESSION) {
 			VmModel m = actSession.getModel();
 			Vm vm = m.findByInstanceUid(vmId);
 			if (vm!=null) {
 				vm.getDescriptor().getLocalCfg().setPropertie(key, value);
 				m.fireVmUpdatedEvent(vm);
 				if (refreshAllVms) {
 					m.fireModelUpdatedEvent();
 				}
 			}
 		}	
 	}
 
 	// ==============================================
 	// ISessionUI
 	// ==============================================
 	@Override
 	public void showDialog(String message, ISession session) {
 		synchronized (uiLock) {
 			// only allow active session to do this and only if login do not
 			// use the UI.
 			if (mode == Mode.SESSION && session == actSession) {
 				setCurrentCallback(null);
 				clientFacade.showMessage(message, IClientFacade.OPTION_NONE);
 			}
 		}
 	}
 
 	@Override
 	public void showWorkspace(ISession session) {
 		synchronized (uiLock) {
 			// only allow active session to do this and only if login do not
 			// use the UI.
 			if (mode == Mode.SESSION && session == actSession) {
 				setCurrentCallback(null);
 				displayVmsOfActiveSession();
 			}
 		}
 	}
 
 	// ==============================================
 	// IVmModelListener (of active session only)
 	// ==============================================
 	public void listUpdated() {
 		// this event comes of the active session !!
 		// @see setActiveSession()
 		synchronized (uiLock) {
 			/*
 			 * display the list of VM of the active session only if no dialog
 			 * are actually displayed.
 			 */
 			if (mode == Mode.SESSION && currentCallback == null) {
 				displayVmsOfActiveSession();
 			}
 		}
 	}
 
 	@Override
 	public void vmUpdated(Vm vm) {
 		// this event comes of the active session !!
 		// @see setActiveSession()
 		synchronized (uiLock) {
 			/*
 			 * update VM details of the active session only if no dialog is
 			 * actually displayed.
 			 */
 			if (mode == Mode.SESSION && currentCallback == null) {
 				refreshSingleVmUI(vm);
 			}
 		}
 	}
 
 	private void refreshSingleVmUI(Vm vm) {
 		// actSession shall not be null (but let test it anyway) and also
 		// check that the given VM is referenced in the active session's
 		// model.
 		if (actSession != null && actSession.getModel().findByInstanceUid(vm.getId()) != null) {
 			LOG.debug("REFRESH UI [1 vm: " + vm.getDescriptor().getRemoteCfg().getName() + " / " + vm.getVmStatus() + "] (but refresh all tabs)");
 			clientFacade.notifiyVmUpdated(vm);
 		} else {
 			LOG.debug("No active session OR vm [" + vm.getId() + "] not in current active session");
 		}
 	}
 
 	/**
 	 * Display VM of active session
 	 */
 	private void displayVmsOfActiveSession() {
 		setCurrentCallback(null);
 		if (actSession == null) {
 			LOG.debug("No active session (null). Do not refresh VM list");
 		} else {
 			LOG.debug("Update VM list for active session [" + actSession + "]");
 			List<Vm> vms = actSession.getModel().getVmList();
 			clientFacade.displayTabs(vms);
 		}
 	}
 }

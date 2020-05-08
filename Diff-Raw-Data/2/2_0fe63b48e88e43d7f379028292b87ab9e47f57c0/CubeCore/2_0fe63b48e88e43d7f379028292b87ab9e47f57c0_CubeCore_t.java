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
 
 package ch.admin.vbs.cube.core.impl;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.admin.vbs.cube.common.RelativeFile;
 import ch.admin.vbs.cube.core.IClientFacade;
 import ch.admin.vbs.cube.core.ICoreFacade;
 import ch.admin.vbs.cube.core.ILoginUI;
 import ch.admin.vbs.cube.core.ISession;
 import ch.admin.vbs.cube.core.ISession.IOption;
 import ch.admin.vbs.cube.core.ISession.ISessionStateDTO;
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
  * Almost all method in this class use a lock in order to avoid concurrency
  * issue in the state-less UI and ensure consistency.
  * 
  */
 public class CubeCore implements ICoreFacade, ISessionUI, ILoginUI, ISessionManagerListener, IVmModelChangeListener, IVmStateChangeListener {
 	/*
 	 * in 'login' mode, only login UI dialogs (message, enter UI dialog) are
 	 * displayed. In 'session' mode, VM and session generated message are
 	 * displayed to user.
 	 * 
 	 * In shutdown mode, no UI update are allowed anymore. Only
 	 * CallbackShutdown, which have a direct reference to IClientFacade, may
 	 * display messages.
 	 */
 	private enum Mode {
 		LOGIN, SESSION, SHUTDOWN
 	}
 
 	/** Logger */
 	private static final Logger LOG = LoggerFactory.getLogger(CubeCore.class);
	private static final long CALL_TIMEOUT = 1000;// 1 second
 	private IClientFacade clientFacade;
 	private ISessionManager sessionManager;
 	private ISession activeSession; // active session
 	private Mode mode = Mode.LOGIN;
 	private IUICallback currentCallback;
 	private Lock uiLock = new ReentrantLock(true);
 	private long lockTimestamp;
 
 	/**
 	 * Set current active session. Derergister-register event listener
 	 * 
 	 * This method MUST be called from a synchronized block.
 	 */
 	private void setActiveSession(ISession session) {
 		LOG.debug("Set active session [{}]", session);
 		if (activeSession != null) {
 			activeSession.getModel().removeModelChangeListener(this);
 			activeSession.getModel().removeStateChangeListener(this);
 		}
 		activeSession = session;
 		if (session != null) {
 			session.getModel().addModelChangeListener(this);
 			session.getModel().addStateChangeListener(this);
 		}
 	}
 
 	/**
 	 * This method MUST be called from a synchronized block.
 	 */
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
 		lock();
 		try {
 			// set the new opened session the active one
 			setActiveSession(session);
 		} finally {
 			unlock();
 		}
 	}
 
 	@Override
 	public void sessionClosed(ISession session) {
 		lock();
 		try {
 			if (activeSession != null && activeSession == session) {
 				setActiveSession(null);
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	@Override
 	public void sessionLocked(ISession session) {
 		lock();
 		try {
 			if (activeSession != null && activeSession == session) {
 				LOG.debug("active session [{}] locked", session.getId());
 				clientFacade.displayTabs(new ArrayList<Vm>(0));
 				setActiveSession(null);
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	// ==============================================
 	// IVmStatelListener (of active session only)
 	// ==============================================
 	@Override
 	public void vmStateUpdated(Vm vm) {
 		lock();
 		try {
 			if (mode == Mode.SESSION) {
 				refreshSingleVmUI(vm);
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	// ==============================================
 	// ICoreFacade
 	// ==============================================
 	@Override
 	public void enteredPassword(char[] password, String requestId) {
 		lock();
 		try {
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
 		} finally {
 			unlock();
 		}
 	}
 
 	@Override
 	public void enteredConfirmation(int result, String requestId) {
 		lock();
 		try {
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
 		} finally {
 			unlock();
 		}
 	}
 
 	@Override
 	public void enteredUsbDevice(UsbDevice device, String requestId) {
 		lock();
 		try {
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
 		} finally {
 			unlock();
 		}
 	}
 
 	private void controlVm(String vmId, VmCommand cmd) {
 		controlVm(vmId, cmd, null);
 	}
 
 	private void controlVm(String vmId, VmCommand cmd, IOption option) {
 		lock();
 		try {
 			if (activeSession != null && mode == Mode.SESSION) {
 				if (cmd == VmCommand.DELETE) {
 					// ask confirmation first (handler will execute the
 					// command)
 					setCurrentCallback(new CallbackDeleteVm(vmId, activeSession));
 					clientFacade.askConfirmation("messagedialog.confirmation.deleteVmConfirmation", currentCallback.getId());
 				} else {
 					// execute command
 					activeSession.controlVm(vmId, cmd, option);
 				}
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	@Override
 	public void startVm(String vmId) {
 		controlVm(vmId, VmCommand.START);
 	}
 
 	@Override
 	public void standByVm(String vmId) {
 		controlVm(vmId, VmCommand.SAVE);
 	}
 
 	@Override
 	public void powerOffVm(String vmId) {
 		controlVm(vmId, VmCommand.POWER_OFF);
 	}
 
 	@Override
 	public void stageVm(String vmId, URL location) {
 		controlVm(vmId, VmCommand.STAGE);
 	}
 
 	@Override
 	public void logoutUser() {
 		lock();
 		try {
 			if (activeSession != null && mode == Mode.SESSION) {
 				LOG.debug("Set Logout callback");
 				setCurrentCallback(new CallbackLogout(activeSession, sessionManager));
 				clientFacade.askConfirmation("messagedialog.confirmation.closeSessionConfirmation", currentCallback.getId());
 			} else {
 				LOG.warn("invalid session or mode");
 			}
 		} finally {
 			unlock();
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
 		controlVm(vmId, VmCommand.DELETE);
 	}
 
 	@Override
 	public void installGuestAdditions(String vmId) {
 		controlVm(vmId, VmCommand.INSTALL_GUESTADDITIONS);
 	}
 
 	@Override
 	public void lockCube() {
 		LOG.error("lockCube not implemented");
 	}
 
 	@Override
 	public void shutdownMachine() {
 		lock();
 		try {
 			// ask confirmation first (handler will execute the command)
 			setCurrentCallback(new CallbackShutdown(sessionManager, clientFacade));
 			clientFacade.askConfirmation("messagedialog.confirmation.shutdownCubeConfirmation", currentCallback.getId());
 		} finally {
 			unlock();
 		}
 	}
 
 	public void attachUsbDevice(String vmId, UsbDevice usbDevice) {
 		controlVm(vmId, VmCommand.ATTACH_USB, usbDevice);
 	}
 
 	public void detachUsbDevice(String vmId, UsbDevice usbDevice) {
 		controlVm(vmId, VmCommand.DETACH_USB, usbDevice);
 	}
 
 	public void setup(IClientFacade clientFacade, ISessionManager smanager) {
 		this.clientFacade = clientFacade;
 		this.sessionManager = smanager;
 		//
 		smanager.addListener(this);
 	}
 
 	@Override
 	public UsbDeviceEntryList getUsbDeviceList(String vmId) {
 		lock();
 		try {
 			UsbDeviceEntryList list = new UsbDeviceEntryList();
 			if (activeSession != null && mode == Mode.SESSION) {
 				controlVm(vmId, VmCommand.LIST_USB, list);
 				long to = System.currentTimeMillis() + 1000;
 				while (!list.isUpdated() && to > System.currentTimeMillis()) {
 					try {
 						Thread.sleep(42);
 					} catch (InterruptedException e) {
 					}
 				}
 				LOG.debug("Got a list in [{} ms] with " + list.size() + " element(s).", to - System.currentTimeMillis());
 				return list;
 			} else {
 				LOG.debug("Core locked or no session active.");
 				return list;
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	// ==============================================
 	// ILoginUI
 	// ==============================================
 	@Override
 	public void closeDialog() {
 		lock();
 		try {
 			if (mode == Mode.LOGIN || mode == Mode.SESSION) {
 				setCurrentCallback(null);
 				// set UI in 'session' mode
 				mode = Mode.SESSION;
 				// display current session VMs
 				displayVmsOfActiveSession();
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	@Override
 	public void showDialog(String message, LoginDialogType type) {
 		lock();
 		try {
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
 		} finally {
 			unlock();
 		}
 	}
 
 	@Override
 	public void showPinDialog(String message, CallbackPin cb) {
 		lock();
 		try {
 			if (mode == Mode.LOGIN || mode == Mode.SESSION) {
 				// set UI in 'login' mode
 				mode = Mode.LOGIN;
 				// display dialog
 				setCurrentCallback(cb);
 				clientFacade.showGetPIN(message, cb.getId());
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	@Override
 	public void setVmProperty(String vmId, String key, String value, boolean refreshAllVms) {
 		// do not synchronize on uiLock since we do not change the UI directly.
 		lock();
 		try {
 			if (activeSession != null && mode == Mode.SESSION) {
 				VmModel m = activeSession.getModel();
 				Vm vm = m.findByInstanceUid(vmId);
 				if (vm != null) {
 					vm.getDescriptor().getLocalCfg().setPropertie(key, value);
 					m.fireVmUpdatedEvent(vm);
 					if (refreshAllVms) {
 						m.fireModelUpdatedEvent();
 					}
 				}
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	// ==============================================
 	// ISessionUI
 	// ==============================================
 	@Override
 	public void showDialog(String message, ISession session) {
 		lock();
 		try {
 			// only allow active session to do this and only if login do
 			// not
 			// use the UI.
 			if (mode == Mode.SESSION && session == activeSession) {
 				setCurrentCallback(null);
 				clientFacade.showMessage(message, IClientFacade.OPTION_NONE);
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	@Override
 	public void showWorkspace(ISession session) {
 		lock();
 		try {
 			// only allow active session to do this and only if login do
 			// not
 			// use the UI.
 			if (mode == Mode.SESSION && session == activeSession) {
 				setCurrentCallback(null);
 				displayVmsOfActiveSession();
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	@Override
 	public void notifySessionState(ISession session, ISessionStateDTO sessionStateDTO) {
 		lock();
 		try {
 			// only allow active session to do this and only if login do
 			// not
 			// use the UI.
 			if (mode == Mode.SESSION && session == activeSession) {
 				setCurrentCallback(null);
 				clientFacade.notifySessionStateUpdate(sessionStateDTO);
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	// ==============================================
 	// IVmModelListener (of active session only)
 	// ==============================================
 	public void listUpdated() {
 		// this event comes of the active session !!
 		// @see setActiveSession()
 		lock();
 		try {
 			/*
 			 * display the list of VM of the active session only if no dialog
 			 * are actually displayed.
 			 */
 			if (mode == Mode.SESSION && currentCallback == null) {
 				displayVmsOfActiveSession();
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	@Override
 	public void vmUpdated(Vm vm) {
 		// this event comes of the active session !!
 		// @see setActiveSession()
 		lock();
 		try {
 			/*
 			 * update VM details of the active session only if no dialog is
 			 * actually displayed.
 			 */
 			if (mode == Mode.SESSION && currentCallback == null) {
 				refreshSingleVmUI(vm);
 			}
 		} finally {
 			unlock();
 		}
 	}
 
 	private void refreshSingleVmUI(Vm vm) {
 		// actSession shall not be null (but let test it anyway) and also
 		// check that the given VM is referenced in the active session's
 		// model.
 		if (activeSession != null && activeSession.getModel().findByInstanceUid(vm.getId()) != null) {
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
 		if (activeSession == null) {
 			LOG.debug("No active session (null). Do not refresh VM list");
 		} else {
 			LOG.debug("Update VM list for active session [" + activeSession + "]");
 			List<Vm> vms = activeSession.getModel().getVmList();
 			clientFacade.displayTabs(vms);
 		}
 	}
 
 	private void lock() {
 		uiLock.lock();
 		lockTimestamp = System.currentTimeMillis();
 	}
 
 	private void unlock() {
 		final long delta = System.currentTimeMillis() - lockTimestamp;
 		if (delta > CALL_TIMEOUT) {
 			/**
 			 * Method call SHOULD be fast in order to guarantee a godd user
 			 * experience (avoid freezing UI)
 			 */
 			LOG.error("Method call duration timeout [" + delta + " ms].", new RuntimeException("Method call duration timeout [" + delta + " ms]."));
 		}
 		uiLock.unlock();
 	}
 }

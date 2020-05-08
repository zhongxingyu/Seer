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
 package ch.admin.vbs.cube.core.vm.vbox;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.security.KeyStore.Builder;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.virtualbox_4_1.AccessMode;
 import org.virtualbox_4_1.AudioControllerType;
 import org.virtualbox_4_1.AudioDriverType;
 import org.virtualbox_4_1.BIOSBootMenuMode;
 import org.virtualbox_4_1.CPUPropertyType;
 import org.virtualbox_4_1.CleanupMode;
 import org.virtualbox_4_1.ClipboardMode;
 import org.virtualbox_4_1.DeviceType;
 import org.virtualbox_4_1.HWVirtExPropertyType;
 import org.virtualbox_4_1.IConsole;
 import org.virtualbox_4_1.IHostUSBDevice;
 import org.virtualbox_4_1.IMachine;
 import org.virtualbox_4_1.IMedium;
 import org.virtualbox_4_1.INetworkAdapter;
 import org.virtualbox_4_1.IProgress;
 import org.virtualbox_4_1.ISession;
 import org.virtualbox_4_1.IStorageController;
 import org.virtualbox_4_1.IUSBDevice;
 import org.virtualbox_4_1.IVirtualBox;
 import org.virtualbox_4_1.LockType;
 import org.virtualbox_4_1.MachineState;
 import org.virtualbox_4_1.NetworkAdapterPromiscModePolicy;
 import org.virtualbox_4_1.NetworkAdapterType;
 import org.virtualbox_4_1.NetworkAttachmentType;
 import org.virtualbox_4_1.SessionState;
 import org.virtualbox_4_1.SessionType;
 import org.virtualbox_4_1.StorageBus;
 import org.virtualbox_4_1.StorageControllerType;
 import org.virtualbox_4_1.VBoxException;
 import org.virtualbox_4_1.VirtualBoxManager;
 
 import ch.admin.vbs.cube.common.CubeException;
 import ch.admin.vbs.cube.common.UuidGenerator;
 import ch.admin.vbs.cube.common.container.SizeFormatUtil;
 import ch.admin.vbs.cube.common.shell.ScriptUtil;
 import ch.admin.vbs.cube.common.shell.ShellUtil;
 import ch.admin.vbs.cube.common.shell.ShellUtilException;
 import ch.admin.vbs.cube.core.I18nBundleProvider;
 import ch.admin.vbs.cube.core.usb.UsbDevice;
 import ch.admin.vbs.cube.core.usb.UsbDeviceEntry;
 import ch.admin.vbs.cube.core.usb.UsbDeviceEntry.DeviceEntryState;
 import ch.admin.vbs.cube.core.usb.UsbDeviceEntryList;
 import ch.admin.vbs.cube.core.vm.IVmProduct.VmProductState;
 import ch.admin.vbs.cube.core.vm.IVmProductListener;
 import ch.admin.vbs.cube.core.vm.NicOption;
 import ch.admin.vbs.cube.core.vm.Vm;
 import ch.admin.vbs.cube.core.vm.VmException;
 import ch.admin.vbs.cube.core.vm.VmModel;
 import ch.admin.vbs.cube.core.vm.vbox.VBoxCache.VBoxCacheListener;
 import ch.admin.vbs.cube.core.vm.vbox.VBoxConfig.VBoxOption;
 import ch.admin.vbs.cube.core.webservice.FileDownloader;
 import ch.admin.vbs.cube.core.webservice.FileDownloader.State;
 import ch.admin.vbs.cube.core.webservice.InstanceParameterHelper;
 import cube.cubemanager.services.InstanceConfigurationDTO;
 
 public class VBoxProduct implements VBoxCacheListener {
 	public static final String ORIGINAL_NETWORK_CONFIG = "{original}";
 	private static final int API_RETRY = 5;
 	private static final String CONTROLLER_NAME = "IDE Controller";
 	public static final String SNAPSHOT_DIRECTORY = "snapshots";
 	private static final String DISK1 = "disk1";
 	private static final long MAX_SHUTDOWN_TIME = 60000; // 1 minute
 	private static final long MAX_SAVE_TIME = 60000; // 1 minute
 	private static final long MEGA = 1048576;
 	/** Logger */
 	private static final Logger LOG = LoggerFactory.getLogger(VBoxProduct.class);
 	//
 	private VirtualBoxManager mgr;
 	private IVirtualBox vbox;
 	private Lock lock = new ReentrantLock();
 	private VBoxCache cache;
 	private ArrayList<IVmProductListener> listeners = new ArrayList<IVmProductListener>(2);
 	private boolean connected = false;
 	private String lastMessage;
 
 	public VBoxProduct() {
 		cache = new VBoxCache(this);
 	}
 
 	public void start() {
 		// disable CXF logs. way to verbose and use "java.logging".
 		java.util.logging.Logger.getLogger("org").setLevel(Level.WARNING);
 		// start cache / monitor
 		cache.start();
 		// Connecting WebService may last up to 15 seconds. Therefore we connect
 		// it from another thread.
 		reconnect();
 	}
 
 	public void stop() {
 		cache.stop();
 		ShellUtil su = new ShellUtil();
 		try {
 			su.run(null, 0, "pkill", "-f", "vboxwebsrv");
 		} catch (Exception e) {
 			LOG.error("Failed to kill vboxwebsrv");
 		}
 	}
 
 	/** used by VBoxCache */
 	public List<IMachine> getMachines() {
 		if (!connected) {
 			return new ArrayList<IMachine>(0);
 		}
 		//
 		lock.lock();
 		try {
 			return vbox.getMachines();
 		} finally {
 			lock.unlock();
 		}
 	}
 
 	// ==========================================
 	// VBoxCacheListener
 	// ==========================================
 	@Override
 	public void notifyVmAdded(IMachine m) {
 		for (IVmProductListener l : listeners) {
 			l.vmStateChanged(m.getId(), VmProductState.STOPPED, getProductState(m));
 		}
 	}
 
 	@Override
 	public void notifyVmRemoved(IMachine m) {
 		for (IVmProductListener l : listeners) {
 			l.vmStateChanged(m.getId(), getProductState(m), VmProductState.STOPPED);
 		}
 	}
 
 	@Override
 	public void notifyVmStateChanged(IMachine machine, MachineState oldState) {
 		for (IVmProductListener l : listeners) {
 			l.vmStateChanged(machine.getId(), getProductState(oldState), getProductState(machine));
 		}
 	}
 
 	private VmProductState getProductState(IMachine m) {
 		if (m == null) {
 			return VmProductState.STOPPED;
 		} else {
 			return getProductState(m.getState());
 		}
 	}
 
 	private VmProductState getProductState(MachineState m) {
 		if (m == null) {
 			return VmProductState.STOPPED;
 		}
 		//
 		switch (m) {
 		case Running:
 		case Paused:
 			return VmProductState.RUNNING;
 		case Restoring:
 		case RestoringSnapshot:
 		case Starting:
 			return VmProductState.STARTING;
 		case Saving:
 		case Stopping:
 			return VmProductState.STOPPING;
 		case Saved:
 		case PoweredOff:
 			// these states could occurs during starting and stopping ...
 			// and it is problematic. We solved it in VmController.
 			return VmProductState.UNKNOWN;
 		case Aborted:
 			return VmProductState.ERROR;
 		default:
 			LOG.debug("The virtual box status '" + m + "' is not interpreted!");
 			return VmProductState.UNKNOWN;
 		}
 	}
 
 	public void addListener(IVmProductListener l) {
 		listeners.add(l);
 	}
 
 	public long getPreferredVmDiskSize(InstanceConfigurationDTO config) {
 		long dis1Size = Long.parseLong(InstanceParameterHelper.getInstanceParameter("vbox.disk1Size", config));
 		long cntSize = (long) ((dis1Size * 1.2) + (200 * MEGA));
 		LOG.debug("compute container size [{}] to hold file [{}]", SizeFormatUtil.format(cntSize), SizeFormatUtil.format(dis1Size));
 		return cntSize;
 	}
 
 	public long getPreferredRuntimeDiskSize(InstanceConfigurationDTO config) {
 		// vbox.baseMemory is received in MEGA unit not bytes
 		long baseMemory = Long.parseLong(InstanceParameterHelper.getInstanceParameter("vbox.baseMemory", config)) * MEGA;
 		// reserve twice its size
 		long cntSize = baseMemory * 2;
 		LOG.debug("compute container size [{}] to hold file [{}]", SizeFormatUtil.format(cntSize), SizeFormatUtil.format(baseMemory));
 		return cntSize;
 	}
 
 	public void preStagging(Vm vm) {
 		// not used with virtualbox
 	}
 
 	public void stagging(Vm vm, VmModel model, InstanceConfigurationDTO config, Builder builder) throws VmException {
 		try {
 			LOG.debug("Start VirtualBox Image Stagging [{}]", vm.getId());
 			fireVmStateUpdate(model, vm, I18nBundleProvider.getBundle().getString("staging.vbox.download_image"), -1);
 			// write config
 			VBoxConfig cfg = new VBoxConfig(vm.getVmContainer(), vm.getRuntimeContainer());
 			cfg.load();
 			cfg.setOption(VBoxOption.Disk1File, DISK1);
 			cfg.setOption(VBoxOption.OsType, InstanceParameterHelper.getInstanceParameter("vbox.operatingSystem", config));
 			cfg.setOption(VBoxOption.IoApic, InstanceParameterHelper.getInstanceParameter("vbox.ioApic", config));
 			cfg.setOption(VBoxOption.Acpi, InstanceParameterHelper.getInstanceParameter("vbox.acpi", config));
 			cfg.setOption(VBoxOption.HwUuid, InstanceParameterHelper.getInstanceParameter("vbox.hwUuid", config));
 			cfg.setOption(VBoxOption.Pae, InstanceParameterHelper.getInstanceParameter("vbox.paenx", config));
 			cfg.setOption(VBoxOption.BaseMemory, InstanceParameterHelper.getInstanceParameter("vbox.baseMemory", config));
 			cfg.setOption(VBoxOption.VideoMemory, InstanceParameterHelper.getInstanceParameter("vbox.videoMemory", config));
 			cfg.setOption(VBoxOption.Audio, "on");
 			// network
 			cfg.setOption(VBoxOption.Nic1, InstanceParameterHelper.getInstanceParameter("vbox.nic1", config));
 			cfg.setOption(VBoxOption.Nic2, InstanceParameterHelper.getInstanceParameter("vbox.nic2", config));
 			cfg.setOption(VBoxOption.Nic3, InstanceParameterHelper.getInstanceParameter("vbox.nic3", config));
 			cfg.setOption(VBoxOption.Nic4, InstanceParameterHelper.getInstanceParameter("vbox.nic4", config));
 			cfg.setOption(VBoxOption.Nic1Mac, InstanceParameterHelper.getInstanceParameter("vbox.nic1Mac", config));
 			cfg.setOption(VBoxOption.Nic2Mac, InstanceParameterHelper.getInstanceParameter("vbox.nic2Mac", config));
 			cfg.setOption(VBoxOption.Nic3Mac, InstanceParameterHelper.getInstanceParameter("vbox.nic3Mac", config));
 			cfg.setOption(VBoxOption.Nic4Mac, InstanceParameterHelper.getInstanceParameter("vbox.nic4Mac", config));
 			cfg.setOption(VBoxOption.Nic1Bridge, InstanceParameterHelper.getInstanceParameter("vbox.nic1Bridge", config));
 			cfg.setOption(VBoxOption.Nic2Bridge, InstanceParameterHelper.getInstanceParameter("vbox.nic2Bridge", config));
 			cfg.setOption(VBoxOption.Nic3Bridge, InstanceParameterHelper.getInstanceParameter("vbox.nic3Bridge", config));
 			cfg.setOption(VBoxOption.Nic4Bridge, InstanceParameterHelper.getInstanceParameter("vbox.nic4Bridge", config));
 			cfg.save();
 			// download VM from server via WebService
 			FileDownloader down = new FileDownloader(builder);
 			File tempFile = new File(vm.getVmContainer().getMountpoint(), DISK1 + ".tmp");
 			FileOutputStream fos = new FileOutputStream(tempFile);
 			down.setDestination(fos);
 			long size = InstanceParameterHelper.getInstanceParameterAsLong("vbox.disk1Size", config);
 			down.setRequest(config.getUuid(), size);
 			down.startDownload();
 			LOG.debug("Download started [{}]", SizeFormatUtil.format(size));
 			while (down.getState() == State.DOWNLOADING || down.getState() == State.IDLE) {
 				Thread.sleep(2000);
 				fireVmStateUpdate(model, vm, null, (int) (down.getProgress() * 100));
 			}
 			fos.close();
 			if (down.getState() == State.SUCCESS) {
 				tempFile.renameTo(new File(vm.getVmContainer().getMountpoint(), DISK1));
 			} else {
 				tempFile.delete();
 			}
 			fireVmStateUpdate(model, vm, "", -1);
 		} catch (Exception e) {
 			throw new VmException("Failed to stage VirtualBox VM.", e);
 		}
 	}
 
 	public void postStagging(Vm vm) throws VmException {
 		ShellUtil shell = new ShellUtil();
 		try {
 			// update disk UUID
 			shell.run(null, ShellUtil.NO_TIMEOUT, "VBoxManage", "-nologo", "internalcommands", "sethduuid",
 					new File(vm.getVmContainer().getMountpoint(), DISK1).getAbsolutePath());
 		} catch (ShellUtilException e) {
 			throw new VmException("Failed to set HD uuid.", e);
 		}
 	}
 
 	public void startVm(Vm vm, VmModel model) throws VmException {
 		checkConnected();
 		lock.lock();
 		try {
 			ISession session = mgr.getSessionObject();
 			IMachine machine = getIMachineReference(vm.getId());
 			LOG.debug("Start VM [{}]", vm.getDescriptor().getRemoteCfg().getName());
 			// use common internal sub-routine to power on VM
 			poweronMachine(machine, session, vm, model);
 			/*
 			 * Use perl script to start VM in another context (when using
 			 * SELinux)
 			 * 
 			 * 
 			 * // acquire the lock just to be sure it will be unlocked before we
 			 * // call VBoxSDL (or VBoxSDL will fail silently) ISession session
 			 * = mgr.getSessionObject(); IMachine machine =
 			 * getIMachineReference(vm.getId()); machine.lockMachine(session,
 			 * LockType.Shared); session.unlockMachine(); Thread.sleep(500); //
 			 * start vm using VBoxSDL in order to be // able to contains the VM
 			 * process in // another SELinux Context and Category ScriptUtil
 			 * script = new ScriptUtil(); script.execute( //
 			 * "./vbox-startvm.pl", // "--uuid", // vm.getId(), // "--snapshot",
 			 * // vm.getRuntimeContainer().getMountpoint().getAbsolutePath() //
 			 * ); // wait started (not more PoweredOff) IMachine imachine =
 			 * getIMachineReference(vm.getId()); for (int i = 0; i < 100; i++) {
 			 * MachineState state = imachine.getState(); if (state !=
 			 * MachineState.PoweredOff) { return; } Thread.sleep(100); }
 			 */
 		} catch (Exception e) {
 			throw new VmException("Failed to start VM", e);
 		} finally {
 			unlockSession();
 			lock.unlock();
 		}
 	}
 
 	/**
 	 * Register and configure VM in VirtualBox based on cube's config file
 	 */
 	public void registerVm(Vm vm, VmModel model) throws VmException {
 		checkConnected();
 		// Create VBoxConfig object and use it to load the configuration file.
 		VBoxConfig cfg = new VBoxConfig(vm.getVmContainer(), vm.getRuntimeContainer());
 		try {
 			cfg.load();
 		} catch (CubeException e) {
 			throw new VmException("Failed to load VM's configuration from VM's container", e);
 		}
 		// obtain lock
 		lock.lock();
 		try {
 			// check if VM is already registered. Clean it up if necessary.
 			IMachine machine = getIMachineReference(vm.getId());
 			if (machine != null) {
 				LOG.debug("An older VM with the UID [{}] is already registred. Cleanup this VM before registering the new one.", vm.getId());
 				ISession session = mgr.getSessionObject();
 				try {
 					cleanupExistingMachine(machine, session, vm, model);
 					LOG.debug("older VM has been removed");
 				} catch (Exception e) {
 					// abort here (do not try to re-register the VM) by throwing
 					// an Exception.
 					throw new CubeException("Failed to remove existing VM with ID [" + vm.getId() + "]. Therefor we are not able to register the VM again.", e);
 				}
 			}
 			// register VM (using web service)
 			LOG.debug("Register VM [{}].", vm.getId());
 			machine = vbox.createMachine(null, vm.getId(), cfg.getOption(VBoxOption.OsType), vm.getId(), true);
 			String hwUuid = cfg.getOption(VBoxOption.HwUuid);
 			if (UuidGenerator.validate(hwUuid)) {
 				machine.setHardwareUUID(hwUuid);
 			}
 			// configure VM
 			LOG.debug("Configure VM [{}].", vm.getId());
 			// BIOS
 			machine.getBIOSSettings().setIOAPICEnabled(cfg.getOptionAsBoolean(VBoxOption.IoApic));
 			machine.getBIOSSettings().setACPIEnabled(cfg.getOptionAsBoolean(VBoxOption.Acpi));
 			machine.getBIOSSettings().setBootMenuMode(BIOSBootMenuMode.MenuOnly);
 			machine.getBIOSSettings().setLogoDisplayTime(0l);
 			// HW
 			machine.setHWVirtExProperty(HWVirtExPropertyType.NestedPaging, cfg.getOptionAsBoolean(VBoxOption.Pae));
 			machine.setMemorySize(cfg.getOptionAsLong(VBoxOption.BaseMemory));
 			machine.setVRAMSize(cfg.getOptionAsLong(VBoxOption.VideoMemory));
 			machine.setAccelerate2DVideoEnabled(false);
 			machine.setAccelerate3DEnabled(false);
 			machine.setCPUCount(1l);
 			machine.setCPUProperty(CPUPropertyType.PAE, true);
 			machine.getUSBController().setEnabled(true);
 			// snapshot + clipboard
 			machine.setSnapshotFolder(vm.getRuntimeContainer().getMountpoint().getAbsolutePath());
 			machine.createSharedFolder("export", vm.getExportFolder().getAbsolutePath(), true, true);
 			machine.createSharedFolder("import", vm.getImportFolder().getAbsolutePath(), false, true);
 			machine.setClipboardMode(ClipboardMode.Bidirectional);
 			// configure sound card
 			machine.getAudioAdapter().setAudioController(AudioControllerType.AC97);
 			machine.getAudioAdapter().setAudioDriver(AudioDriverType.Pulse);
 			machine.getAudioAdapter().setEnabled(true);
 			// configure network interfaces
 			addNetworkIface(0, cfg.getOption(VBoxOption.Nic1), cfg.getOption(VBoxOption.Nic1Bridge), cfg.getOption(VBoxOption.Nic1Mac), machine);
 			addNetworkIface(1, cfg.getOption(VBoxOption.Nic2), cfg.getOption(VBoxOption.Nic2Bridge), cfg.getOption(VBoxOption.Nic2Mac), machine);
 			addNetworkIface(2, cfg.getOption(VBoxOption.Nic3), cfg.getOption(VBoxOption.Nic3Bridge), cfg.getOption(VBoxOption.Nic3Mac), machine);
 			addNetworkIface(3, cfg.getOption(VBoxOption.Nic4), cfg.getOption(VBoxOption.Nic4Bridge), cfg.getOption(VBoxOption.Nic4Mac), machine);
 			LOG.debug("Save VM settings [{}].", vm.getId());
 			machine.saveSettings();
 			vbox.registerMachine(machine);
 			// configure disks (need to lock the machine)
 			ISession session = mgr.getSessionObject();
 			machine.lockMachine(session, LockType.Write);
 			IMachine lMachine = session.getMachine();
 			try {
 				//
 				IStorageController store = lMachine.addStorageController(CONTROLLER_NAME, StorageBus.IDE);
 				store.setControllerType(StorageControllerType.PIIX4);
 				/*
 				 * in SDK 4.1, they had a 'forceNewUuid' option (boolean).
 				 * Setting it to 'false' should be OK for us since we strictly
 				 * control medium after use and they should be no risk to re-use
 				 * the same HDD.
 				 */
 				IMedium medium = vbox.openMedium(new File(vm.getVmContainer().getMountpoint(), cfg.getOption(VBoxOption.Disk1File)).getAbsolutePath(),
 						DeviceType.HardDisk, AccessMode.ReadWrite, false);
 				lMachine.attachDevice(CONTROLLER_NAME, 0, 0, DeviceType.HardDisk, medium);
 				lMachine.attachDevice(CONTROLLER_NAME, 1, 0, DeviceType.DVD, null);
 				lMachine.saveSettings();
 			} catch (Exception e) {
 				throw new CubeException("Fail to attach VM devices", e);
 			} finally {
 				safeUnlock(session);
 			}
 		} catch (Exception e) {
 			throw new VmException("Failed to register VM", e);
 		} finally {
 			unlockSession();
 			lock.unlock();
 		}
 		try {
 			// Create 'tun' devices (need sudo rights)
 			ArrayList<String> nics = new ArrayList<String>();
 			if ("vpn".equalsIgnoreCase(cfg.getOption(VBoxOption.Nic1))) {
 				nics.add(cfg.getOption(VBoxOption.Nic1Bridge));
 			}
 			if ("vpn".equalsIgnoreCase(cfg.getOption(VBoxOption.Nic2))) {
 				nics.add(cfg.getOption(VBoxOption.Nic2Bridge));
 			}
 			if ("vpn".equalsIgnoreCase(cfg.getOption(VBoxOption.Nic3))) {
 				nics.add(cfg.getOption(VBoxOption.Nic3Bridge));
 			}
 			if ("vpn".equalsIgnoreCase(cfg.getOption(VBoxOption.Nic4))) {
 				nics.add(cfg.getOption(VBoxOption.Nic4Bridge));
 			}
 			ScriptUtil script = new ScriptUtil();
 			for (String nic : nics) {
 				script.execute("sudo", "./vbox-tuncreate.pl", "--tun", nic);
 			}
 		} catch (Exception e) {
 			throw new VmException("Failed to create tun interfaces", e);
 		}
 	}
 
 	private void checkConnected() throws VmException {
 		if (vbox == null)
 			throw new VmException("Not connected to VirtualBox WebService yet.");
 	}
 
 	/**
 	 * power off machine
 	 * 
 	 * @see https://www.virtualbox.org/sdkref/interface_i_machine.html ->
 	 *      'IMachine::lockMachine' for details about locking
 	 * @param machine
 	 *            machine to power off
 	 * @param session
 	 *            used to obtain the lock
 	 * @param vm
 	 *            used to notify progress
 	 * @param model
 	 * @throws InterruptedException
 	 * @throws CubeException
 	 * @throws VmException
 	 */
 	private void poweroffMachine(IMachine machine, ISession session, Vm vm, VmModel model) throws InterruptedException, CubeException, VmException {
 		checkConnected();
 		// get shared lock (sufficient to control machine execution)
 		machine.lockMachine(session, LockType.Shared);
 		LOG.debug("Session locked [" + session.getState() + "," + session.getType() + "]");
 		// power off machine
 		fireVmStateUpdate(model, vm, "Powering off", 0);
 		IProgress progress = session.getConsole().powerDown();
 		// wait until completed
 		while (!progress.getCompleted()) {
 			/*
 			 * @TODO (?): is timeout check still necessary?? long to =
 			 * System.currentTimeMillis() + MAX_SHUTDOWN_TIME;
 			 */
 			// notify progress to model listeners
 			fireVmStateUpdate(model, vm, progress);
 			Thread.sleep(1000);
 		}
 		progress.waitForCompletion(10000);
 		fireVmStateUpdate(model, vm, "", -1);
 		// unlock machine
 		safeUnlock(session);
 	}
 
 	private void fireVmStateUpdate(VmModel model, Vm vm, IProgress progress) {
 		String msg = String.format("%s (%d/%d)", //
 				progress.getOperationDescription(), //
 				progress.getOperation() + 1, //
 				progress.getOperationCount()); //
 		lastMessage = msg;
 		vm.setProgressMessage(msg);
 		vm.setProgress(progress.getOperationPercent().intValue());
 		LOG.debug("[{}] [{}]", lastMessage, vm.getProgress());
 		if (model != null) {
 			model.fireVmStateUpdatedEvent(vm);
 		}
 	}
 
 	private void fireVmStateUpdate(VmModel model, Vm vm, String message, int i) {
 		if (message != null) {
 			vm.setProgressMessage(message);
 			lastMessage = message;
 		}
 		vm.setProgress(i);
 		LOG.debug("[{}] [{}]", lastMessage, i);
 		if (model != null) {
 			model.fireVmStateUpdatedEvent(vm);
 		}
 	}
 
 	/**
 	 * power off machine
 	 * 
 	 * @see https://www.virtualbox.org/sdkref/interface_i_machine.html ->
 	 *      'IMachine::lockMachine' for details about locking
 	 * @param machine
 	 *            machine to power on
 	 * @param session
 	 *            used to obtain the lock
 	 * @param vm
 	 *            used to notify progress
 	 * @param model
 	 * @throws CubeException
 	 * @throws InterruptedException
 	 * @throws VmException
 	 * @throws Exception
 	 */
 	private void poweronMachine(IMachine machine, ISession session, Vm vm, VmModel model) throws VBoxException, InterruptedException, CubeException,
 			VmException {
 		checkConnected();
 		// where the snaphot may be
 		File snapshot = new File(vm.getRuntimeContainer().getMountpoint().getAbsoluteFile(), "state.sav");
 		IProgress progress = null;
 		if (snapshot.exists()) {
 			// power on machine
 			LOG.debug("Restore VM from saved state [{}]", snapshot.getAbsolutePath());
 			fireVmStateUpdate(model, vm, "Restoring", 0);
 			// adopt saved state
 			machine.lockMachine(session, LockType.Write);
 			session.getConsole().adoptSavedState(snapshot.getAbsolutePath());
 			safeUnlock(session);
 			// launch VM
 			progress = machine.launchVMProcess(session, "sdl", null);
 		} else {
 			// power on machine
 			fireVmStateUpdate(model, vm, "Powering on", 0);
 			progress = machine.launchVMProcess(session, "sdl", null);
 		}
 		// wait until completed
 		while (!progress.getCompleted()) {
 			// notify progress to model listeners
 			String msg = String.format("[op:%d][op#:%d][desc:%s][%d%%][wght:%d]", //
 					progress.getOperation(), //
 					progress.getOperationCount(), //
 					progress.getOperationDescription(), //
 					progress.getOperationPercent(), //
 					progress.getOperationWeight());
 			fireVmStateUpdate(model, vm, progress);
 			Thread.sleep(1000);
 		}
 		progress.waitForCompletion(10000);
 		fireVmStateUpdate(model, vm, "", -1);
 		// unlock machine
 		safeUnlock(session);
 	}
 
 	/**
 	 * Sometimes VirtualBox unlock the session automatically (like after VM
 	 * power down) but not immediately. This method help to handle it since
 	 * unlocking an already unlocked session would fail with an exception.
 	 * 
 	 * @throws CubeException
 	 */
 	private void safeUnlock(ISession session) throws InterruptedException, CubeException {
 		int limit = 20;
 		while (session.getState() == SessionState.Locked) {
 			LOG.debug("Wait session to unlock [" + session.getType() + "]");
 			session.unlockMachine();
 			Thread.sleep(200);
 			if (limit-- < 0) {
 				throw new CubeException("Failed to unlock session (timeout)");
 			}
 		}
 		LOG.debug("Session to unlock [" + session.getState() + "]");
 	}
 
 	/*
 	 * private void removeMachineMediums(ISession session) throws CubeException
 	 * { if (session.getType() != SessionType.WriteLock) { throw new
 	 * CubeException( "Session did not get a WriteLock on the machine [" +
 	 * session.getType() + "]"); } // get locked machine IMachine machine =
 	 * session.getMachine(); for (IMediumAttachment atta :
 	 * machine.getMediumAttachments()) { if (atta.getMedium() != null) {
 	 * LOG.debug("Remove medium [{}]", atta.getMedium().getName());
 	 * machine.detachDevice(atta.getController(), atta.getPort(),
 	 * atta.getDevice()); machine.saveSettings(); atta.getMedium().close(); } }
 	 * }
 	 */
 	private void cleanupExistingMachine(IMachine machine, ISession session, Vm vm, VmModel model) throws InterruptedException, CubeException, VmException {
 		/**
 		 * Note about locking. Only lock the VM with a 'write' lock if
 		 * necessary. You may be able to power-off the VM with 'shared' lock
 		 * before to be able to lock it in 'write' mode to unregister it (if
 		 * someone else had a also a 'shared').
 		 */
 		LOG.debug("Cleanup machine [" + machine.getId() + "]");
 		// power-off the machine if necessary
 		MachineState state = machine.getState();
 		switch (state) {
 		case Paused:
 		case Running:
 			LOG.debug("power off '{}' machine before unregistering it", state);
 			poweroffMachine(machine, session, vm, model);
 			LOG.debug("Machine has been powered off [" + machine.getState() + "]");
 			break;
 		case Saved:
 		case PoweredOff:
 			// no need to power off
 			break;
 		default:
 			// experience will tell us if we should handle other states.
 			LOG.debug("Let VM with state ({}) and try to unregister it", state);
 		}
 		// try to get a write lock to be sure that no other session holds a
 		// lock on this machine. And unlock it again before calling unregister.
 		machine.lockMachine(session, LockType.Write);
 		LOG.debug("actual session state (should be WriteLock): " + session.getState() + ", " + session.getType());
 		// unregister does not require lock
 		safeUnlock(session);
 		List<IMedium> mediums = machine.unregister(CleanupMode.Full);
 		for (IMedium medium : mediums) {
 			String loc = medium.getLocation();
 			try {
 				medium.close();
 				LOG.debug("Close medium [{}]", loc);
 			} catch (Exception e) {
 				LOG.error("Failed to close medium [" + loc + "]", e);
 			}
 		}
 		// delete files (logs, etc in user's home directory)
 		IProgress progress = machine.delete(null);
 		while (!progress.getCompleted()) {
 			// notify progress to model listeners
 			fireVmStateUpdate(model, vm, progress);
 			Thread.sleep(1000);
 		}
 		progress.waitForCompletion(0);
 		fireVmStateUpdate(model, vm, "", -1);
 	}
 
 	private void addNetworkIface(long id, String nic, String bridge, String mac, IMachine machine) {
 		LOG.debug("configure nic [{}]", id + " / " + nic + " / mac:" + mac);
 		mac = mac == null ? null : mac.replaceAll(":", "").toLowerCase();
 		if ("vpn".equals(nic)) {
 			// use pre-configured mac + bridge
 			INetworkAdapter na = machine.getNetworkAdapter(id);
 			na.setAdapterType(NetworkAdapterType.I82540EM);
 			na.setAttachmentType(NetworkAttachmentType.Bridged);
 			na.setBridgedInterface(bridge);
 			na.setMACAddress(mac);
 			na.setCableConnected(false); // will be set by VpnListener
 			na.setPromiscModePolicy(NetworkAdapterPromiscModePolicy.Deny);
 			na.setEnabled(true);
 		} else if ("disabled".equals(nic)) {
 			// Do not use it
 			INetworkAdapter na = machine.getNetworkAdapter(id);
 			na.setAdapterType(NetworkAdapterType.I82540EM);
 			na.setAttachmentType(NetworkAttachmentType.Null);
 			na.setCableConnected(false);
 			na.setPromiscModePolicy(NetworkAdapterPromiscModePolicy.Deny);
 			na.setEnabled(false);
 		} else if ("bridged".equals(nic)) {
 			// use pre-configured mac + bridge
 			INetworkAdapter na = machine.getNetworkAdapter(id);
 			na.setAdapterType(NetworkAdapterType.I82540EM);
 			na.setAttachmentType(NetworkAttachmentType.Bridged);
 			na.setBridgedInterface(bridge);
 			na.setMACAddress(mac);
 			na.setCableConnected(true);
 			na.setPromiscModePolicy(NetworkAdapterPromiscModePolicy.Deny);
 			na.setEnabled(true);
 		} else if ("disconnected".equals(nic)) {
 			// use pre-configured mac
 			INetworkAdapter na = machine.getNetworkAdapter(id);
 			na.setAdapterType(NetworkAdapterType.I82540EM);
 			na.setAttachmentType(NetworkAttachmentType.Null);
 			na.setMACAddress(mac);
 			na.setCableConnected(true);
 			na.setPromiscModePolicy(NetworkAdapterPromiscModePolicy.Deny);
 			na.setEnabled(true);
 		} else {
 			LOG.error("unsupported nic [{}]", nic);
 		}
 	}
 
 	/**
 	 * Find machine by name. Return null instead an exception if no
 	 * corresponding VM is found.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	private IMachine getIMachineReference(String id) {
 		lock.lock();
 		try {
 			if (vbox == null || id == null)
 				return null;
 			return vbox.findMachine(id);
 		} catch (VBoxException e) {
 			return null;
 		} finally {
 			lock.unlock();
 		}
 	}
 
 	public void poweroffVm(Vm vm, VmModel model) throws VmException {
 		checkConnected();
 		lock.lock();
 		try {
 			IMachine machine = getIMachineReference(vm.getId());
 			poweroffMachine(machine, mgr.getSessionObject(), vm, model);
 		} catch (Exception e) {
 			throw new VmException("", e);
 		} finally {
 			unlockSession();
 			lock.unlock();
 		}
 	}
 
 	/**
 	 * Ensure that the session will be unlocked (typically in catch clause).
 	 * 
 	 * will NEVER throw an excetion since it is used in 'finally' clauses
 	 */
 	private void unlockSession() {
 		try {
 			if (vbox != null) {
 				ISession session = mgr.getSessionObject();
 				// it is not clear how/when to unlock session...
 				if (session != null && session.getState() == SessionState.Locked) {
 					SessionType type = session.getType();
 					try {
 						session.unlockMachine();
 						LOG.warn("Orphan session unlocked [was: " + type + "]");
 					} catch (Exception e) {
 						LOG.error("Failed to unlock session [was: " + type + "]", e);
 					}
 				} else {
 					LOG.debug("As excpected, session was not locked");
 				}
 			}
 		} catch (Exception e) {
 			LOG.error("Failed to unlock session", e);
 		}
 	}
 
 	public void unregisterVm(Vm vm, VmModel model) throws VmException {
 		checkConnected();
 		IMachine machine = getIMachineReference(vm.getId());
 		if (machine != null) {
 			lock.lock();
 			try {
 				cleanupExistingMachine(machine, mgr.getSessionObject(), vm, model);
 				LOG.debug("VM has been unregistred");
 			} catch (Exception e) {
 				// abort here (do not try to re-register the VM) by throwing
 				// an Exception.
 				throw new VmException("Failed to unregister VM with ID [" + vm.getId() + "]", e);
 			} finally {
 				unlockSession();
 				lock.unlock();
 			}
 		} else {
 			LOG.debug("No machine with ID [{}]. Skip unregister.", vm.getId());
 		}
 	}
 
 	public VmProductState getProductState(Vm vm) {
 		if (vm == null) {
 			return VmProductState.ERROR;
 		} else {
 			return getProductState(cache.getState(vm.getId()));
 		}
 	}
 
 	public void save(Vm vm, VmModel model) throws VmException {
 		checkConnected();
 		lock.lock();
 		try {
 			IMachine machine = getIMachineReference(vm.getId());
 			ISession session = mgr.getSessionObject();
 			// get shared lock (sufficient to control machine execution)
 			machine.lockMachine(session, LockType.Shared);
 			LOG.debug("Session locked [" + session.getState() + "," + session.getType() + "]");
 			// power off machine
 			fireVmStateUpdate(model, vm, "Save", 0);
 			IProgress progress = session.getConsole().saveState();
 			// wait until completed
 			while (!progress.getCompleted()) {
 				// notify progress to model listeners
 				/*
 				 * @TODO (?): is timeout check still necessary?? long to =
 				 * System.currentTimeMillis() + MAX_SAVE_TIME;;
 				 */
 				fireVmStateUpdate(model, vm, progress);
 				Thread.sleep(1000);
 			}
 			progress.waitForCompletion(10000);
 			// rename saved state file so it will not be deleted when VM will be
 			// unregistered
 			LOG.debug("VM save completed. Rename snapshot");
 			File mp = vm.getRuntimeContainer().getMountpoint();
 			for (File f : mp.listFiles()) {
 				if (f.getName().endsWith(".sav")) {
 					LOG.debug("Rename [{}] into [state.sav]", f.getAbsolutePath());
 					ShellUtil su = new ShellUtil();
 					su.run(null, ShellUtil.NO_TIMEOUT, "mv", "-f", //
 							f.getAbsolutePath(), //
 							new File(mp, "state.sav").getAbsolutePath());
 					if (su.getExitValue() != 0) {
 						throw new VmException("Failed to save VM state. Script exited with code [" + su.getExitValue() + "]");
 					}
 					break;
 				}
 			}
 			// just ensure unlocked (vbox used to automatically release lock)
 			safeUnlock(session);
 			// and discard state
 			machine.lockMachine(session, LockType.Shared);
 			session.getConsole().discardSavedState(true);
 			safeUnlock(session);
 			// unlock machine
 			fireVmStateUpdate(model, vm, "", -1);
 		} catch (Exception e) {
 			throw new VmException("", e);
 		} finally {
 			unlockSession();
 			lock.unlock();
 		}
 	}
 
 	public boolean isConnected() {
 		return connected;
 	}
 
 	public void reconnect() {
 		// restart virtualbox webservice in a new thread (sometimes it just stop
 		// working)
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				ShellUtil su = new ShellUtil();
 				try {
 					su.run(null, 0, "pkill", "-f", "vboxwebsrv");
 				} catch (Exception e) {
 					LOG.error("Failed to kill vboxwebsrv");
 				}
 				try {
 					su.run(null, 0, "VBoxManage", "setproperty", "websrvauthlibrary", "null");
 					su.run(null, 0, "vboxwebsrv");
 				} catch (Exception e) {
 					LOG.error("Failed to start vboxwebsrv");
 				}
 				LOG.error("Thread vboxsrv exit");
 			}
 		}, "vboxwebsrv").start();
 		// start webclient service in a new thead too
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				// Connect VirtualBox Web Service in a new thread to avoid
 				// blocking login for 5 seconds.
 				try {
 					mgr = VirtualBoxManager.createInstance(null);
 					mgr.connect("http://localhost:18083", "", "");
 					vbox = mgr.getVBox();
 					connected = true;
 					LOG.info("VirtualBox Web Service connected [{}]", vbox.getVersion());
 				} catch (Exception e) {
 					LOG.error("VirtualBox Web Service not connected (http://localhost:18083). Is vboxwebsrv running? [" + e.getMessage() + "]", e);
 				}
 			}
 		}, "WebService Connector").start();
 	}
 
 	public void listUsb(Vm vm, UsbDeviceEntryList list) throws VmException {
 		checkConnected();
 		if (lock.tryLock()) {
 			try {
 				// get IMachine reference
 				IMachine machine = getIMachineReference(vm.getId());
 				ISession session = mgr.getSessionObject();
 				// lock IMachine
 				machine.lockMachine(session, LockType.Shared);
 				machine = session.getMachine();
 				try {
 					IConsole console = session.getConsole();
 					// get list of usb from host
 					List<IHostUSBDevice> hostDevices = vbox.getHost().getUSBDevices();
 					// get list of attached usb devices
 					HashSet<String> attachedIds = new HashSet<String>();
 					for (IUSBDevice dev : console.getUSBDevices()) {
 						attachedIds.add(dev.getAddress());
 					}
 					// populate result list
 					for (IHostUSBDevice dev : hostDevices) {
 						// exclude GemPlus product from the list in order to
 						// avoid
 						// attaching the main smart-card reader to a guest
 						if (dev.getVendorId() == 0x08e6) {
 							continue;
 						}
 						// exclude product without description
 						String p = dev.getProduct();
 						String m = dev.getManufacturer();
 						if (p == null || p.trim().length() == 0) {
 							continue;
 						}
 						// format description : use product description and
 						// include
 						// manufacturer if available
 						String fmt = p;
 						if (m != null && m.trim().length() > 0) {
 							fmt += " (" + m + ")";
 						}
 						// add to list
 						if (attachedIds.contains(dev.getAddress())) {
 							list.add(new UsbDeviceEntry(vm.getId(),//
 									new UsbDevice(dev.getId(), Integer.toHexString(dev.getVendorId()), Integer.toHexString(dev.getProductId()), fmt),//
 									DeviceEntryState.ALREADY_ATTACHED));
 						} else {
 							list.add(new UsbDeviceEntry(vm.getId(),//
 									new UsbDevice(dev.getId(), Integer.toHexString(dev.getVendorId()), Integer.toHexString(dev.getProductId()), fmt),//
 									DeviceEntryState.AVAILABLE));
 						}
 					}
 				} finally {
 					// ensure that session is unlocked
 					unlockSession();
 				}
 			} catch (Exception e) {
 				throw new VmException("Failed to list USB devices", e);
 			} finally {
 				unlockSession();
 				list.setUpdated(true);
 				lock.unlock();
 			}
 		} else {
 			LOG.debug("VirtualBox webservice is busy. do not list USB.");
 		}
 	}
 
 	public void attachUsb(Vm vm, UsbDevice dev) throws VmException {
 		checkConnected();
 		if (lock.tryLock()) {
 			try {
 				// get IMachine reference
 				IMachine machine = getIMachineReference(vm.getId());
 				ISession session = mgr.getSessionObject();
 				// lock IMachine
 				machine.lockMachine(session, LockType.Shared);
 				machine = session.getMachine();
 				try {
 					session.getConsole().attachUSBDevice(dev.getId());
 				} finally {
 					// ensure that session is unlocked
 					unlockSession();
 				}
 			} catch (Exception e) {
 				throw new VmException("Failed to attach USB device [" + dev.getId() + "]", e);
 			} finally {
 				unlockSession();
 				lock.unlock();
 			}
 		} else {
 			LOG.debug("VirtualBox webservice is busy. do not attach USB.");
 		}
 	}
 
 	public void detachUsb(Vm vm, UsbDevice dev) throws VmException {
 		checkConnected();
 		if (lock.tryLock()) {
 			try {
 				// get IMachine reference
 				IMachine machine = getIMachineReference(vm.getId());
 				ISession session = mgr.getSessionObject();
 				// lock IMachine
 				machine.lockMachine(session, LockType.Shared);
 				machine = session.getMachine();
 				try {
 					session.getConsole().detachUSBDevice(dev.getId());
 				} finally {
 					// ensure that session is unlocked
 					unlockSession();
 				}
 			} catch (Exception e) {
 				throw new VmException("Failed to detach USB device [" + dev.getId() + "]", e);
 			} finally {
 				unlockSession();
 				lock.unlock();
 			}
 		} else {
 			LOG.debug("VirtualBox webservice is busy. do not dettach USB.");
 		}
 	}
 
 	public void connectNic(Vm vm, boolean connected) throws VmException {
 		checkConnected();
 		if (lock.tryLock()) {
 			try {
 				// get IMachine reference
 				IMachine machine = getIMachineReference(vm.getId());
 				ISession session = mgr.getSessionObject();
 				// lock IMachine
 				machine.lockMachine(session, LockType.Shared);
 				machine = session.getMachine();
 				for (long i = 0L; i < 4L; i++) {
 					INetworkAdapter nic = machine.getNetworkAdapter(i);
 					if (nic.getEnabled()) {
 						LOG.debug("Set nic [{}] CableConnected({})", nic.getSlot(), connected);
 						nic.setCableConnected(connected);
 					} else {
 						LOG.debug("Ignore disabled nic [{}] CableConnected({})", nic.getSlot(), connected);
 					}
 				}
 			} catch (Exception e) {
 				throw new VmException("Failed to connect/disconnect NIC", e);
 			} finally {
 				unlockSession();
 				lock.unlock();
 			}
 		} else {
 			LOG.debug("VirtualBox webservice is busy. cannot connect/disconnect NIC.");
 		}
 	}
 
 	public void connectNic(Vm vm, NicOption option) throws VmException {
 		checkConnected();
 		if (lock.tryLock()) {
 			try {
 				if (option.getNic().equals(ORIGINAL_NETWORK_CONFIG)) {
 					// restore original nic config
 					VBoxConfig cfg = new VBoxConfig(vm.getVmContainer(), vm.getRuntimeContainer());
 					try {
 						cfg.load();
 					} catch (CubeException e) {
 						throw new VmException("Failed to load VM's configuration from VM's container", e);
 					}
 					ISession session = mgr.getSessionObject();
 					IMachine machine = getIMachineReference(vm.getId());
 					machine.lockMachine(session, LockType.Shared);
 					IMachine mutable = session.getMachine();
 					addNetworkIface(0, cfg.getOption(VBoxOption.Nic1), cfg.getOption(VBoxOption.Nic1Bridge), cfg.getOption(VBoxOption.Nic1Mac), mutable);
 					addNetworkIface(1, cfg.getOption(VBoxOption.Nic2), cfg.getOption(VBoxOption.Nic2Bridge), cfg.getOption(VBoxOption.Nic2Mac), mutable);
 					addNetworkIface(2, cfg.getOption(VBoxOption.Nic3), cfg.getOption(VBoxOption.Nic3Bridge), cfg.getOption(VBoxOption.Nic3Mac), mutable);
 					addNetworkIface(3, cfg.getOption(VBoxOption.Nic4), cfg.getOption(VBoxOption.Nic4Bridge), cfg.getOption(VBoxOption.Nic4Mac), mutable);
 					mutable.saveSettings();
 					session.unlockMachine();
 				} else {
 					// connect first nic to the given interface and disconnect
 					// all other nics
 					ISession session = mgr.getSessionObject();
 					IMachine machine = getIMachineReference(vm.getId());
 					machine.lockMachine(session, LockType.Shared);
 					IMachine mutable = session.getMachine();
					// disable other devices
					for (long nic = 1l; nic < 4l; nic++) {
 						mutable.getNetworkAdapter(nic).setCableConnected(false);
 						mutable.getNetworkAdapter(nic).setEnabled(false);
 					}
 					INetworkAdapter na = mutable.getNetworkAdapter(0l);
 					na.setAttachmentType(NetworkAttachmentType.Bridged);
 					na.setBridgedInterface(option.getNic());
 					na.setEnabled(true);
 					na.setCableConnected(true);
 					na.setPromiscModePolicy(NetworkAdapterPromiscModePolicy.AllowAll);
 					mutable.saveSettings();
 					session.unlockMachine();
 				}
 			} catch (Exception e) {
 				throw new VmException("Failed to connect/disconnect NIC to [" + option.getNic() + "]", e);
 			} finally {
 				unlockSession();
 				lock.unlock();
 			}
 		} else {
 			LOG.debug("VirtualBox webservice is busy. do connect/disconnect NIC.");
 		}
 	}
 }

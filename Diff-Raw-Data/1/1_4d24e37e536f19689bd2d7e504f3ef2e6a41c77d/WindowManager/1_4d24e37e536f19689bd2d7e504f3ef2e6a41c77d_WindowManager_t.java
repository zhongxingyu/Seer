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
 package ch.admin.vbs.cube.client.wm.ui.wm;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 import javax.swing.SwingUtilities;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.admin.vbs.cube.client.wm.client.ICubeActionListener;
 import ch.admin.vbs.cube.client.wm.client.ICubeClient;
 import ch.admin.vbs.cube.client.wm.client.IUserInterface;
 import ch.admin.vbs.cube.client.wm.client.IVmChangeListener;
 import ch.admin.vbs.cube.client.wm.client.IVmMonitor;
 import ch.admin.vbs.cube.client.wm.client.VmChangeEvent;
 import ch.admin.vbs.cube.client.wm.client.VmHandle;
 import ch.admin.vbs.cube.client.wm.ui.CubeUI.CubeScreen;
 import ch.admin.vbs.cube.client.wm.ui.ICubeUI;
 import ch.admin.vbs.cube.client.wm.ui.IWindowsControl;
 import ch.admin.vbs.cube.client.wm.ui.dialog.BootPasswordDialog;
 import ch.admin.vbs.cube.client.wm.ui.dialog.BootPasswordDialog.BootPasswordListener;
 import ch.admin.vbs.cube.client.wm.ui.dialog.ButtonLessDialog;
 import ch.admin.vbs.cube.client.wm.ui.dialog.CubeConfirmationDialog;
 import ch.admin.vbs.cube.client.wm.ui.dialog.CubeInitialDialog;
 import ch.admin.vbs.cube.client.wm.ui.dialog.CubePasswordDialog;
 import ch.admin.vbs.cube.client.wm.ui.dialog.CubePasswordDialogListener;
 import ch.admin.vbs.cube.client.wm.ui.dialog.CubeWizard;
 import ch.admin.vbs.cube.client.wm.ui.x.IWindowManagerCallback;
 import ch.admin.vbs.cube.client.wm.ui.x.IXWindowManager;
 import ch.admin.vbs.cube.client.wm.ui.x.imp.X11.Window;
 import ch.admin.vbs.cube.client.wm.ui.x.imp.XWindowManager2;
 import ch.admin.vbs.cube.common.RelativeFile;
 import ch.admin.vbs.cube.core.IClientFacade;
 
 /**
  * This class is responsible to handle request for UI element (dialog, etc). It
  * ensure that only one dialog is visible (cancel other if needed).
  * 
  * 
  * 
  */
 public class WindowManager implements IWindowsControl, IUserInterface, IWindowManagerCallback, IVmChangeListener {
 	private static final Color BACKGROUND_COLOR = Color.DARK_GRAY;
 	private static final int WINDOW_LOCATION_Y = 25;
 	public static final int BORDER_SIZE = 5;
 	/** Logger */
 	private static final ArrayList<Window> EMPTY_WINDOW_LIST = new ArrayList<Window>();
 	private static final Logger LOG = LoggerFactory.getLogger(WindowManager.class);
 	private static final String VIRTUALMACHINE_WINDOWFMT = "^%s - .*Oracle VM VirtualBox.*$";
 	private Pattern windowPatternVirtualMachine = Pattern.compile(String.format(VIRTUALMACHINE_WINDOWFMT, "(.*)"));
 	private Object lock = new Object();
 	private CubeWizard dialog;
 	private VisibleWindows visibleWindows = new VisibleWindows();
 	private IXWindowManager xwm;
 	private IVmMonitor vmMon;
 	private ICubeActionListener cubeActionListener;
 	private ICubeClient client;
 	private ICubeUI cubeUI;
 	private ManagedWindowModel managedModel = new ManagedWindowModel();
 	private JFrame xframe;
 
 	public WindowManager() {
 	}
 
 	/**
 	 * Close current dialog (via Swing-Thread).
 	 */
 	private void closeCurrentDialog() {
 		if (dialog != null) {
 			// copy reference on current opened dialog
 			final CubeWizard tdial = dialog;
 			// use SwingUtilities to perform this action
 			// from swing thread.
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					LOG.trace("close dialog [" + tdial.getClass() + "]");
 					tdial.setVisible(false);
 					tdial.dispose();
 				}
 			});
 			dialog = null;
 		}
 	}
 
 	@Override
 	public void closeDialog() {
 		closeCurrentDialog();
 	}
 
 	/**
 	 * Hide navigation bar and VMs. Make the screen clean (only showing
 	 * background). Typically before displaying a dialog.
 	 */
 	private void hideNavigationBarAndVms(Window... windowsToShow) {
 		// build a list of x-window we want to hide and show
 		ArrayList<Window> hide = new ArrayList<Window>();
 		ArrayList<Window> show = new ArrayList<Window>();
 		// add all bordered windows into 'hide' list.
 		for (ManagedWindow managed : managedModel.list()) {
 			Window border = managed.border;
 			if (border != null)
 				hide.add(border);
 		}
 		// add also NavigationBar frames to 'hide' list
 		for (CubeScreen n : cubeUI.getScreens()) {
 			LOG.trace("Hide navigation bar [{}]", n.getNavigationBar().getTitle());
 			hide.add(getXWindow(n.getNavigationBar()));
 		}
 		// add window to show
 		for (Window window : windowsToShow) {
 			show.add(window);
 		}
 		// apply changes
 		synchronized (xwm) {
 			xwm.showOnlyTheseWindow(hide, show);
 		}
 	}
 
 	/** show navigation frames and bordered windows (containing VMs window). */
 	private void showNavigationBarAndVms(boolean raiseNavbar) {
 		synchronized (lock) {
 			if (dialog != null) {
 				LOG.trace("Skip showNavigationBarAndVms because a dialog is opened.");
 				return;
 			}
 			// index visible window's IDs
 			Set<String> visibleVmsIds = visibleWindows.getVisibleVmIds();
 			// show VM window
 			ArrayList<Window> show = new ArrayList<Window>();
 			ArrayList<Window> hide = new ArrayList<Window>();
 			synchronized (lock) {
 				for (ManagedWindow managed : managedModel.list()) {
 					if (visibleVmsIds.contains(managed.vmId)) {
 						show.add(managed.border);
 					} else {
 						hide.add(managed.border);
 					}
 				}
 			}
 			if (raiseNavbar) {
 				// add NavigationBar to show list
 				for (CubeScreen n : cubeUI.getScreens()) {
 					if (n.isActive()) {
 						LOG.trace("show NavigationBar");
 						show.add(getXWindow(n.getNavigationBar()));
 					} else {
 						hide.add(getXWindow(n.getNavigationBar()));
 					}
 				}
 			}
 			// show windows
 			// --
 			if (xframe != null) {
 				Window x = getXWindow(xframe);
 				if (x != null)
 					show.add(x);
 			}
 			// --
 			synchronized (xwm) {
 				xwm.showOnlyTheseWindow(hide, show);
 			}
 		}
 	}
 
 	// ###############################################
 	// Implements IWindowsManagerCallBack
 	// ###############################################
 	public class ManagedWindow {
 		private String title;
 		private final String vmId;
 		private VmHandle handle;
 		private Window client;
 		private final Window border;
 		private JFrame frame;
 		private Rectangle borderBounds;
 
 		public ManagedWindow(Window client, String title, String vmId, JFrame frame) {
 			this.client = client;
 			this.title = title;
 			this.vmId = vmId;
 			this.frame = frame;
 			// since we do not have a reference to the corresponding handle yet,
 			// we are unable to find out the right border color.
 			// @TODO update borderr color when handle is updated
 			borderBounds = computeBounds(frame);
 			Color borderColor = Color.GRAY;
 			border = xwm.createBorderWindow(xwm.findWindowByTitle(frame.getTitle()), BORDER_SIZE, borderColor, BACKGROUND_COLOR, getBorderBoundsForX());
 		}
 
 		public ManagedWindow(VmHandle handle, JFrame frame) {
 			this.frame = frame;
 			this.vmId = handle.getVmId();
 			this.handle = handle;
 			// initilaize border window
 			borderBounds = computeBounds(frame);
 			Color borderColor = BorderColorProvider.getBackgroundColor(vmMon.getVmClassification(handle));
 			border = xwm.createBorderWindow(xwm.findWindowByTitle(frame.getTitle()), BORDER_SIZE, borderColor, BACKGROUND_COLOR, getBorderBoundsForX());
 		}
 
 		public Window getClient() {
 			return client;
 		}
 
 		public Rectangle computeBounds(JFrame frame) {
 			return new Rectangle( //
 					0, WINDOW_LOCATION_Y, //
 					frame.getBounds().width - 0, //
 					frame.getBounds().height - WINDOW_LOCATION_Y);
 		}
 
 		public void dispose() {
 			xwm.removeWindow(border);
 		}
 
 		/** X does not include border bound in total width */
 		public Rectangle getBorderBoundsForX() {
 			return new Rectangle(borderBounds.x, borderBounds.y, borderBounds.width - 2 * BORDER_SIZE, borderBounds.height - 2 * BORDER_SIZE);
 		}
 
 		/** client window bounds within border window */
 		public Rectangle getClientBounds() {
 			return new Rectangle(0, 0, borderBounds.width - (2 * BORDER_SIZE), borderBounds.height - (2 * BORDER_SIZE));
 		}
 	}
 
 	public class ManagedWindowModel {
 		private ArrayList<ManagedWindow> managedWindows = new ArrayList<WindowManager.ManagedWindow>();
 
 		/** @return the UUID contained in the title or null */
 		public String isManagable(String windowTitle) {
 			Matcher appMx = windowPatternVirtualMachine.matcher(windowTitle);
 			if (appMx.matches()) {
 				// This is a VirtualBox Window. create a cache entry
 				return appMx.group(1);
 			}
 			return null;
 		}
 
 		public ManagedWindow getManaged(Window client) {
 			synchronized (managedWindows) {
 				for (ManagedWindow managed : managedWindows) {
 					if (managed.client != null && managed.client.longValue() == client.longValue()) {
 						return managed;
 					}
 				}
 				return null;
 			}
 		}
 
 		public ManagedWindow getManagedByBorder(Window border) {
 			synchronized (managedWindows) {
 				for (ManagedWindow managed : managedWindows) {
 					if (managed.border != null && managed.border.longValue() == border.longValue()) {
 						return managed;
 					}
 				}
 				return null;
 			}
 		}
 
 		public ManagedWindow getManaged(String vmId) {
 			synchronized (managedWindows) {
 				for (ManagedWindow managed : managedWindows) {
 					if (managed.vmId != null && managed.vmId.equals(vmId)) {
 						return managed;
 					}
 				}
 				return null;
 			}
 		}
 
 		public void add(ManagedWindow managed) {
 			synchronized (managedWindows) {
 				managedWindows.add(managed);
 			}
 		}
 
 		public ArrayList<ManagedWindow> list() {
 			synchronized (managedWindows) {
 				return (ArrayList<ManagedWindow>) managedWindows.clone();
 			}
 		}
 
 		public void remove(ManagedWindow managed) {
 			synchronized (managedWindows) {
 				managedWindows.remove(managed);
 				// dispose border window
 				managed.dispose();
 			}
 		}
 	}
 
 	public ManagedWindowModel getManagedModel() {
 		return managedModel;
 	}
 
 	@Override
 	public void windowTitleUpdated(Window client, String title) {
 		synchronized (managedModel) {
 			ManagedWindow managed = managedModel.getManaged(client);
 			if (managed == null) {
 				// window is not managed. try to retrieve a VM id from window
 				// title..
 				String vmId = managedModel.isManagable(title);
 				if (vmId != null) {
 					// this window is a VM window. search another managed window
 					// for this vmId (but where the window field does not match
 					// necessary the new client window.
 					managed = managedModel.getManaged(vmId);
 					if (managed == null) {
 						// No trace of this ID. create a new ManagedWindow
 						// object
 						managed = new ManagedWindow(client, title, vmId, getDefaultParentFrame());
 						managedModel.add(managed);
 					} else {
 						// Update the existing ManagedWindow object
 						managed.client = client;
 					}
 					// re-parent X window
 					synchronized (xwm) {
 						xwm.reparentClientWindow(managed.border, client, managed.getClientBounds());
 					}
 				}
 			} else {
 				// update title
 				managed.title = title;
 			}
 		}
 	}
 
 	@Override
 	public void windowDestroyed(Window window) {
 		synchronized (managedModel) {
 			ManagedWindow managed = managedModel.getManaged(window);
 			if (managed == null) {
 				LOG.debug("Unmanaged Window destroyed [{}/{}]", window, xwm.getWindowName(window));
 			} else {
 				LOG.debug("Managed Window [{}/{}] destroyed", managed.title, managed.vmId);
 				managed.client = null;
 			}
 		}
 	}
 
 	// ###############################################
 	// Implements IWindowsControl
 	// ###############################################
 	@Override
 	public void moveVmWindow(VmHandle h, String monitorIdxBeforeUpdate) {
 		// check if VM 'really' moved
 		if (h.getMonitorId().equals(monitorIdxBeforeUpdate)) {
 			// not moved
 			LOG.debug("Skip moving VM Window [{}] to monitor [{}]", h.getVmId(), h.getMonitorId());
 			return;
 		}
 		//
 		ManagedWindow managed = managedModel.getManaged(h.getVmId());
 		if (managed == null) {
 			LOG.debug("VM [{}] cannot be moved since it is not managed yet.", h.getVmId());
 		}
 		LOG.debug("Move VM Window [{}] to monitor [{}]", h.getVmId(), h.getMonitorId());
 		// update 'visibleWindow'
 		visibleWindows.set(h.getMonitorId(), h);
 		// adjust new bounds and re-parent border window
 		CubeScreen dstScreen = cubeUI.getScreen(h.getMonitorId());
 		managed.frame = dstScreen.getBackgroundFrame();
 		managed.borderBounds = managed.computeBounds(managed.frame);
 		synchronized (xwm) {
 			xwm.reparentWindowAndResize(getXWindow(managed.frame), managed.border, managed.getBorderBoundsForX(), managed.client, managed.getClientBounds());
 			// TODO resize client window to !!
 		}
 		// set in foreground
 		dstScreen.getNavigationBar().selectTab(h);
 	}
 
 	/**
 	 * Get corresponding XWindow for a given JFrame object (java). Use frame
 	 * name in order to find the right XWindow.
 	 */
 	private final Window getXWindow(JFrame jframe) {
 		if (jframe == null) {
 			throw new NullPointerException("Argument frame must be none-null");
 		}
 		Window w;
 		synchronized (xwm) {
 			w = xwm.findWindowByTitle(jframe.getTitle());
 			if (w == null) {
 				LOG.error("Not XWindow found for window [{}]", jframe.getTitle());
 			}
 		}
 		return w;
 	}
 
 	@Override
 	public void showVmWindow(VmHandle h) {
 		/*
 		 * Called by NavigationTabs -> stateChanged
 		 */
 		visibleWindows.set(h.getMonitorId(), h);
 		/*
 		 * do not show navigation bars or it will hide pop-up menu.
 		 */
 		showNavigationBarAndVms(false);
 	}
 
 	@Override
 	public void hideAllVmWindows(String monitorId) {
 		LOG.trace("hideAllVmWindows({})", monitorId);
 		visibleWindows.set(monitorId, null);
 		showNavigationBarAndVms(false);
 	}
 
 	// ###############################################
 	// Implements IVmChangelistener
 	// ###############################################
 	@Override
 	public void allVmsChanged() {
 		synchronized (managedModel) {
 			LOG.debug("All VMs changed...");
 			List<VmHandle> vms = client.listVms();
 			// list all VMs known on the client (running or not)
 			for (VmHandle handle : vms) {
 				// check if the client is already managed
 				ManagedWindow managed = managedModel.getManaged(handle.getVmId());
 				if (managed == null) {
 					// not already managed. add a new ManagedWindow to model.
 					managed = new ManagedWindow(handle, getDefaultParentFrame());
 					managedModel.add(managed);
 					LOG.debug("managed created [{}]", managed.vmId);
 				} else {
 					// update ManagedWindow
 					managed.handle = handle;
 					LOG.debug("managed updated [{}]", managed.vmId);
 				}
 			}
 			// lookup for ManagedWindow to remove
 			NEXT: for (ManagedWindow managed : managedModel.list()) {
 				for (VmHandle handle : vms) {
 					if (handle.getVmId().equals(managed.vmId)) {
 						continue NEXT;
 					}
 				}
 				// no VmHandle found for this managed window
 				if (managed.client == null) {
 					managedModel.remove(managed);
 					LOG.debug("managed removed [{}]", managed.vmId);
 				} else {
 					// client window is there -> just clear handle field
 					managed.handle = null;
 					LOG.debug("managed updated (handle cleared) [{}]", managed.vmId);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void vmChanged(VmChangeEvent event) {
 		// nothing to do.
 	}
 
 	// ###############################################
 	// Implements IUserInterface
 	// ###############################################
 	@Override
 	public void showMessageDialog(String message, int options) {
 		LOG.debug("showMessageDialog()");
 		synchronized (lock) {
 			closeCurrentDialog();
 			hideNavigationBarAndVms();
 			// dialog is non-blocking
 			if (options == IClientFacade.OPTION_SHUTDOWN) {
 				// only one option : OPTION_SHUTDOWN
 				final CubeInitialDialog dial = new CubeInitialDialog(getDefaultParentFrame(), message, cubeActionListener);
 				dialog = dial;
 				swingOpen(dial);
 			} else {
 				// default
 				final ButtonLessDialog msgdialog = new ButtonLessDialog(getDefaultParentFrame(), message);
 				dialog = msgdialog;
 				swingOpen(msgdialog);
 			}
 		}
 	}
 
 	@Override
 	public void showBootPasswordDialog() {
 		LOG.debug("showDiskPasswordChangeDialog()");
 		synchronized (lock) {
 			closeCurrentDialog();
 			hideNavigationBarAndVms();
 			// dialog is non-blocking
 			final BootPasswordDialog msgdialog = new BootPasswordDialog(getDefaultParentFrame());
 			msgdialog.addPasswordDialogListener(new BootPasswordListener() {
 				@Override
 				public void closed() {
					closeCurrentDialog();
 					showNavigationBarAndVms(true);
 				}
 			});
 			dialog = msgdialog;
 			swingOpen(msgdialog);
 		}
 	}
 
 	private void swingOpen(final CubeWizard msgdialog) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				LOG.trace("exec  [displayWizard] [{}]", msgdialog);
 				msgdialog.displayWizard();
 			}
 		});
 	}
 
 	@Override
 	public void showPinDialog(final String additionalMessage, final String requestId) {
 		LOG.debug("showPinDialog()");
 		synchronized (lock) {
 			LOG.trace("enter [showPinDialog] [{}]", additionalMessage);
 			closeCurrentDialog();
 			hideNavigationBarAndVms();
 			// create dialog (non-blocking)
 			final CubePasswordDialog passwordDialog = new CubePasswordDialog(getDefaultParentFrame());
 			passwordDialog.addPasswordDialogListener(new CubePasswordDialogListener() {
 				@Override
 				public void quit(final char[] password) {
 					cubeActionListener.enteredPassword(password, requestId);
 				}
 			});
 			// set as active dialog
 			dialog = passwordDialog;
 			// open dialog
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					LOG.debug("exec  [displayWizard] [{}]", passwordDialog.getClass());
 					passwordDialog.displayWizard(additionalMessage);
 				}
 			});
 			LOG.trace("exit  [showPinDialog] [{}]", additionalMessage);
 		}
 	}
 
 	@Override
 	public void showTransferDialog(VmHandle h, RelativeFile file) {
 		LOG.warn("[showTransferDialog()] not implemented");
 	}
 
 	@Override
 	public void showConfirmationDialog(final String messageKey, final String requestId) {
 		LOG.debug("showConfirmationDialog()");
 		synchronized (lock) {
 			closeCurrentDialog();
 			hideNavigationBarAndVms();
 			// create dialog
 			final CubeConfirmationDialog dial = new CubeConfirmationDialog(getDefaultParentFrame(), messageKey, CubeConfirmationDialog.TYPE_CANCEL_YES);
 			// set as active dialo
 			dialog = dial;
 			// display dialog
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					LOG.debug("exec  [showConfirmationDialog] [{}]", messageKey);
 					dial.displayWizard();
 					// return result
 					cubeActionListener.enteredConfirmation(dial.getDialogResult(), requestId);
 				}
 			});
 		}
 	}
 
 	private JFrame getDefaultParentFrame() {
 		return cubeUI.getDefaultScreen().getBackgroundFrame();
 	}
 
 	@Override
 	public void showVms() {
 		/*
 		 * Called by ClientFacade in order to display workspace when a dialog
 		 * has been closed.
 		 */
 		LOG.trace("ShowVMs()");
 		synchronized (lock) {
 			// close all dialogs
 			closeCurrentDialog();
 			// ensure that navigation bar are visible
 			showNavigationBarAndVms(true);
 		}
 	}
 
 	@Override
 	public void setSessionStateIcon(ConnectionIcon icon) {
 		LOG.debug("setSessionStateIcon(:" + icon + ")");
 		for (CubeScreen n : cubeUI.getScreens()) {
 			switch (icon) {
 			case CONNECTED:
 				n.getNavigationBar().setIcon("connected_small.png");
 				break;
 			case CONNECTED_VPN:
 				n.getNavigationBar().setIcon("connectedvpn_small.png");
 				break;
 			case CONNECTING:
 				n.getNavigationBar().setIcon("connecting_small.gif");
 				break;
 			case CONNECTING_VPN:
 				n.getNavigationBar().setIcon("connectingvpn_small.gif");
 				break;
 			case NOT_CONNECTED:
 				n.getNavigationBar().setIcon("offline_small.png");
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void refresh() {
 		LOG.trace("refresh()");
 		synchronized (lock) {
 			if (dialog == null) {
 				LOG.trace("Refresh WM: show VMs + navbar");
 				showNavigationBarAndVms(true);
 			} else {
 				LOG.trace("Refresh WM: show Dialog");
 				ArrayList<Window> show = new ArrayList<Window>();
 				ArrayList<Window> hide = new ArrayList<Window>();
 				// hide VMs' windows
 				hideNavigationBarAndVms(xwm.findWindowByTitle(CubeWizard.WIZARD_WINDOW_TITLE));
 			}
 		}
 	}
 
 	private class VisibleWindows {
 		private HashMap<String, VmHandle> layout = new HashMap<String, VmHandle>();
 		private HashMap<String, String> ilayout = new HashMap<String, String>();
 
 		private void set(String monitorId, VmHandle h) {
 			synchronized (layout) {
 				if (h == null) {
 					// if VmHanlde is null.. see if a VM was on this monitor
 					h = layout.remove(monitorId);
 					if (h != null) {
 						// remove old VM
 						ilayout.remove(h.getVmId());
 					}
 				} else {
 					// eventually remove handler from another monitor
 					String oldMon = ilayout.remove(h.getVmId());
 					if (oldMon != null) {
 						layout.remove(oldMon);
 					}
 					// put handler in new monitor
 					layout.put(monitorId, h);
 					ilayout.put(h.getVmId(), monitorId);
 				}
 				if (LOG.isTraceEnabled()) {
 					LOG.trace("VisibleWindows.set()");
 					for (Entry<String, VmHandle> e : layout.entrySet()) {
 						LOG.trace("- Visible window: [{}][{}]", e.getKey(), e.getValue());
 					}
 				}
 			}
 		}
 
 		private Set<String> getVisibleVmIds() {
 			HashSet<String> ids = new HashSet<String>();
 			synchronized (layout) {
 				for (VmHandle h : layout.values()) {
 					ids.add(h.getVmId());
 				}
 			}
 			if (LOG.isTraceEnabled()) {
 				LOG.trace("VisibleWindows.getVisibleVmIds()");
 				for (Entry<String, VmHandle> e : layout.entrySet()) {
 					LOG.trace("- Visible window: [{}][{}]", e.getKey(), e.getValue());
 				}
 			}
 			return ids;
 		}
 	}
 
 	@Override
 	public Rectangle getPreferedClientBounds(Window window) {
 		ManagedWindow managed = managedModel.getManaged(window);
 		if (managed == null) {
 			return null;
 		} else {
 			return managed.getClientBounds();
 		}
 	}
 
 	@Override
 	public void adjustGuestSize(String vmId) {
 		ManagedWindow m = managedModel.getManaged(vmId);
 		if (m != null && m.client != null) {
 			xwm.adjustClientSize(m.client, m.getClientBounds());
 		}
 	}
 
 	// ###############################################
 	// Injections
 	// ###############################################
 	public void setup(ICubeClient client, ICubeActionListener cubeActionListener, IVmMonitor vmMon, IXWindowManager xwm, ICubeUI cubeUI) {
 		this.client = client;
 		this.cubeUI = cubeUI;
 		client.addListener(this);
 		this.xwm = xwm;
 		xwm.setWindowManagerCallBack(this);
 		this.vmMon = vmMon;
 		// if (osdMgmt != null) {
 		// osdMgmt.setVmMon(vmMon);
 		// }
 		this.cubeActionListener = cubeActionListener;
 	}
 
 	public void openDebug() {
 		JPanel panel = new JPanel();
 		panel.setPreferredSize(new Dimension(600, 300));
 		SpringLayout layout = new SpringLayout();
 		panel.setLayout(layout);
 		if (xframe != null) {
 			xframe.setVisible(false);
 			xframe.dispose();
 		}
 		xframe = new JFrame("xyz");
 		xframe.setContentPane(panel);
 		JLabel lw = new JLabel("Width");
 		JLabel lh = new JLabel("Height");
 		JLabel lx = new JLabel("X");
 		JLabel ly = new JLabel("Y");
 		final JTextField fw = new JTextField("640");
 		final JTextField fh = new JTextField("480");
 		final JTextField fx = new JTextField("0");
 		final JTextField fy = new JTextField("0");
 		JButton bmove = new JButton("move");
 		JButton bresize = new JButton("move+resize");
 		JButton brefresh = new JButton("refresh");
 		JButton bclose = new JButton("close");
 		final DefaultListModel model = new DefaultListModel();
 		final JList windowl = new JList(model);
 		JScrollPane scroll = new JScrollPane(windowl);
 		panel.add(scroll);
 		panel.add(fx);
 		panel.add(fy);
 		panel.add(fw);
 		panel.add(fh);
 		panel.add(bmove);
 		panel.add(bresize);
 		panel.add(brefresh);
 		panel.add(bclose);
 		panel.add(lx);
 		panel.add(ly);
 		panel.add(lw);
 		panel.add(lh);
 		//
 		layout.putConstraint(SpringLayout.NORTH, lx, 25, SpringLayout.NORTH, panel);
 		layout.putConstraint(SpringLayout.NORTH, ly, 15, SpringLayout.SOUTH, lx);
 		layout.putConstraint(SpringLayout.NORTH, lw, 15, SpringLayout.SOUTH, ly);
 		layout.putConstraint(SpringLayout.NORTH, lh, 15, SpringLayout.SOUTH, lw);
 		layout.putConstraint(SpringLayout.WEST, lx, 25, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.WEST, ly, 25, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.WEST, lw, 25, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.WEST, lh, 25, SpringLayout.WEST, panel);
 		//
 		layout.putConstraint(SpringLayout.WEST, fx, 80, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.WEST, fy, 80, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.WEST, fw, 80, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.WEST, fh, 80, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.EAST, fx, 120, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.EAST, fy, 120, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.EAST, fw, 120, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.EAST, fh, 120, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.NORTH, fx, -3, SpringLayout.NORTH, lx);
 		layout.putConstraint(SpringLayout.NORTH, fy, -3, SpringLayout.NORTH, ly);
 		layout.putConstraint(SpringLayout.NORTH, fw, -3, SpringLayout.NORTH, lw);
 		layout.putConstraint(SpringLayout.NORTH, fh, -3, SpringLayout.NORTH, lh);
 		//
 		layout.putConstraint(SpringLayout.NORTH, scroll, 10, SpringLayout.NORTH, panel);
 		layout.putConstraint(SpringLayout.WEST, scroll, 130, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.SOUTH, scroll, 150, SpringLayout.NORTH, panel);
 		layout.putConstraint(SpringLayout.EAST, scroll, 300, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.NORTH, bmove, 10, SpringLayout.NORTH, panel);
 		layout.putConstraint(SpringLayout.WEST, bmove, 320, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.NORTH, bresize, 40, SpringLayout.NORTH, panel);
 		layout.putConstraint(SpringLayout.WEST, bresize, 320, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.NORTH, brefresh, 70, SpringLayout.NORTH, panel);
 		layout.putConstraint(SpringLayout.WEST, brefresh, 320, SpringLayout.WEST, panel);
 		layout.putConstraint(SpringLayout.NORTH, bclose, 100, SpringLayout.NORTH, panel);
 		layout.putConstraint(SpringLayout.WEST, bclose, 320, SpringLayout.WEST, panel);
 		brefresh.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				model.removeAllElements();
 				for (ManagedWindow w : managedModel.list()) {
 					if (w.getClient() != null)
 						model.addElement(w);
 				}
 			}
 		});
 		bresize.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ManagedWindow w = (ManagedWindow) windowl.getSelectedValue();
 				if (w != null) {
 					LOG.debug("move + resize!");
 					((XWindowManager2) xwm).moveResize(w.client, Integer.parseInt(fx.getText()), Integer.parseInt(fy.getText()),
 							Integer.parseInt(fw.getText()), Integer.parseInt(fh.getText()));
 				}
 			}
 		});
 		bmove.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ManagedWindow w = (ManagedWindow) windowl.getSelectedValue();
 				if (w != null) {
 					LOG.debug("move!");
 					((XWindowManager2) xwm).move(w.client, Integer.parseInt(fx.getText()), Integer.parseInt(fy.getText()));
 				}
 			}
 		});
 		bclose.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				xframe.setVisible(false);
 				xframe.dispose();
 			}
 		});
 		//
 		xframe.setLocation(100, 100);
 		xframe.pack();
 		xframe.setVisible(true);
 	}
 }

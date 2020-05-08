 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.riena.ui.core.uiprocess.UIProcess;
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.uiprocess.ProcessState;
 import org.eclipse.riena.ui.swt.uiprocess.ProgressInfoDataObject;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseTrackAdapter;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * The {@link StatuslineUIProcess} shows info about all currently running
  * {@link UIProcess}s in the {@link Statusline}
  */
 public class StatuslineUIProcess extends AbstractStatuslineComposite {
 
 	/**
 	 * the minimum value for the progress
 	 */
 	public static final int PROGRESS_MIN_VALUE = 0;
 
 	/**
 	 * the maximum value for the progress
 	 */
 	public static final int PROGRESS_MAX_VALUE = 100;
 
 	// the base progressbar rendererd inside the statusline
 	private ProgressBar progressBar;
 
 	// the window containing the info table
 	private PopupList popup;
 
 	private List<ProgressInfoDataObject> pidos = new ArrayList<ProgressInfoDataObject>();
 
 	private final Map<ProcessState, ILabelFormatter> stateValueMappers = new HashMap<ProcessState, ILabelFormatter>();
 
 	private Color listBackground;
 
 	/**
 	 * @param parent
 	 *            - the parent composite
 	 * @param style
 	 */
 	public StatuslineUIProcess(Composite parent, int style) {
 		super(parent, style);
 		initStateMappers();
 		listBackground = new Color(parent.getDisplay(), 183, 216, 236);
 		observeMoveAndResize(parent);
 	}
 
 	private void observeMoveAndResize(Composite parent) {
 		//observe shell movement and resize
 		ControlAdapter listener = new ControlAdapter() {
 
 			@Override
 			public void controlMoved(ControlEvent e) {
 				configureShell();
 			}
 
 			@Override
 			public void controlResized(ControlEvent e) {
 				configureShell();
 			}
 
 		};
 		addControlListener(listener);
 		parent.getShell().addControlListener(listener);
 
 		Control grabCorner = locateGrabCorner();
 		if (grabCorner != null) {
 			grabCorner.addMouseTrackListener(new MouseTrackAdapter() {
 
 				@Override
 				public void mouseExit(MouseEvent e) {
 					super.mouseExit(e);
 					configureShell();
 				}
 			});
 		}
 	}
 
 	private void configureShell() {
 		if (popup.getShell() != null && popup.getShell().isVisible()) {
 			placeShell();
 		}
 	}
 
 	private Control locateGrabCorner() {
 		Shell shell = getShell();
 		for (Control child : shell.getChildren()) {
			if (child.getData("sizeexecutor").equals("grabcorner")) { //$NON-NLS-1$ //$NON-NLS-2$
 				return child;
 			}
 		}
 		// no recursion
 		return null;
 	}
 
 	/**
 	 * init mappers for label formatting
 	 */
 	private void initStateMappers() {
 		stateValueMappers.put(ProcessState.PENDING, new ILabelFormatter() {
 
 			public String formatValue(int value) {
 				return "..."; //$NON-NLS-1$
 			}
 
 		});
 
 		stateValueMappers.put(ProcessState.RUNNING, new ILabelFormatter() {
 
 			public String formatValue(int value) {
 				return value + " %"; //$NON-NLS-1$
 			}
 
 		});
 		stateValueMappers.put(ProcessState.FINISHED, new ILabelFormatter() {
 
 			public String formatValue(int value) {
 				return "finished"; //$NON-NLS-1$
 			}
 
 		});
 
 		stateValueMappers.put(ProcessState.CANCELED, new ILabelFormatter() {
 
 			public String formatValue(int value) {
 				return "canceled"; //$NON-NLS-1$
 			}
 
 		});
 
 	}
 
 	/**
 	 * @return the progressBar
 	 */
 	public ProgressBar getProgressBar() {
 		return progressBar;
 	}
 
 	/**
 	 * <pre>
 	 * 
 	 * The content should look nearly like this:
 	 * 	 * 
 	 * +------------------------------------+
 	 * +  UIProcess Label    |[][][][]    | +
 	 * +------------------------------------+
 	 * </pre>
 	 */
 	@Override
 	protected void createContents() {
 		initLayout();
 
 		statusLabel = new Label(this, SWT.NONE);
 		statusLabel.setBackground(LnfManager.getLnf().getColor(ILnfKeyConstants.STATUSLINE_BACKGROUND));
 
 		openLabel = new Label(this, SWT.NONE);
 		openLabel.setImage(LnfManager.getLnf().getImage(ILnfKeyConstants.TITLELESS_SHELL_RESTORE_ICON));
 		openLabel.addMouseListener(new PopupController());
 		//		openLabel.setText("open"); //$NON-NLS-1$ TODO
 		openLabel.setBackground(LnfManager.getLnf().getColor(ILnfKeyConstants.STATUSLINE_BACKGROUND));
 
 		FormData formData = new FormData();
 		formData.top = new FormAttachment(18, 0);
 		formData.width = 60;
 		formData.height = 14;
 
 		progressBar = new ProgressBar(this, SWT.HORIZONTAL);
 		progressBar.setLayoutData(formData);
 		progressBar.setBackground(LnfManager.getLnf().getColor(ILnfKeyConstants.STATUSLINE_BACKGROUND));
 		formData.left = new FormAttachment(statusLabel, 5);
 
 		// minimum value is allways 0
 		progressBar.setMinimum(PROGRESS_MIN_VALUE);
 		//maximum is allayway 100
 		progressBar.setMaximum(PROGRESS_MAX_VALUE);
 
 		progressBar.setSelection(0);
 
 		formData = new FormData();
 		formData.top = new FormAttachment(18, 0);
 		formData.height = 14;
 		formData.width = 100;
 		statusLabel.setLayoutData(formData);
 
 		formData = new FormData();
 		formData.top = new FormAttachment(18, 0);
 		formData.left = new FormAttachment(progressBar, 5);
 		formData.height = 14;
 		openLabel.setLayoutData(formData);
 
 		// create the popup
 		popup = new PopupList(getShell());
 	}
 
 	/**
 	 * observes the open/close {@link Label}
 	 */
 	class PopupController extends MouseAdapter {
 
 		public void mouseDown(MouseEvent e) {
 			if (popup.getShell() == null) {
 				openPopup();
 				return;
 			}
 			closePopup();
 		}
 	}
 
 	private void openPopup() {
 		popup.setBlockOnOpen(false);
 		popup.open();
 		popup.getShell().setVisible(false);
 		triggerListUpdate(pidos, true);
 	}
 
 	private void closePopup() {
 		popup.close();
 		// clear the caches
 		pido2controlHolder.clear();
 		valueCache.clear();
 	}
 
 	private Composite popupContent;
 
 	/**
 	 * the {@link ApplicationWindow} for the list of processes
 	 */
 	class PopupList extends ApplicationWindow {
 
 		public PopupList(Shell parentShell) {
 			super(parentShell);
 		}
 
 		@Override
 		protected Control createContents(Composite parent) {
 			parent.setBackground(listBackground);
 			popupContent = parent;
 			FormLayout layout = new FormLayout();
 			layout.marginLeft = 5;
 			layout.marginRight = 5;
 			layout.marginTop = 5;
 			layout.marginBottom = 5;
 			parent.setLayout(layout);
 			return parent;
 		}
 
 		@Override
 		protected void configureShell(Shell shell) {
 			super.configureShell(shell);
 			Rectangle rect = StatuslineUIProcess.this.getBounds();
 			Point display = toDisplay(new Point(rect.x, rect.y));
 			shell.setBounds(rect.x, display.y + 100, 100, 400);
 		}
 
 		@Override
 		protected int getShellStyle() {
 			return SWT.NO_TRIM | SWT.NO_FOCUS | SWT.BORDER;
 		}
 
 		@Override
 		protected boolean showTopSeperator() {
 			return false;
 		}
 
 	}
 
 	private void initLayout() {
 		FormLayout layout = new FormLayout();
 		layout.marginRight = 5;
 		setLayout(layout);
 	}
 
 	/**
 	 * updates uiprocess visualization directly in the StatusLine
 	 */
 	public void triggerBaseUpdate(ProgressInfoDataObject pido) {
 		if (pidoComplete(pido)) {
 			resetStatusLineProgressBar();
 			return;
 		}
 		updateProgressBar(pido, getProgressBar());
 		updateLabel(pido, statusLabel);
 	}
 
 	private boolean pidoComplete(ProgressInfoDataObject pido) {
 		return pido.getValue() >= 100;
 	}
 
 	private void resetStatusLineProgressBar() {
 		if (!getProgressBar().isDisposed()) {
 			getProgressBar().setSelection(0);
 			statusLabel.setText(""); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * updates the given {@link ProgressBar} with the value of the given
 	 * {@link ProgressInfoDataObject}
 	 * 
 	 * @param pido
 	 * @param progressBar
 	 */
 	private static void updateProgressBar(ProgressInfoDataObject pido, ProgressBar progressBar) {
 		if (!progressBar.isDisposed()) {
 			progressBar.setMaximum(100);
 			progressBar.setSelection(pido.getValue());
 		}
 	}
 
 	/**
 	 * updates the list of visualized {@link UIProcess}es
 	 * 
 	 * @param pidos
 	 *            - the progress info data objects
 	 */
 	public void triggerListUpdate(List<ProgressInfoDataObject> pidos) {
 		triggerListUpdate(pidos, false);
 	}
 
 	/**
 	 * updates the list of visualized {@link UIProcess}es
 	 * 
 	 * @param pidos
 	 *            - the progress info data objects
 	 * @param forceListUpdate
 	 *            - determines if a list update should be forced even when there
 	 *            is no new data
 	 */
 	public void triggerListUpdate(List<ProgressInfoDataObject> pidos, boolean forceListUpdate) {
 		if (pidos.size() == 0) {
 			resetStatusLineProgressBar();
 		}
 		if (isVisible()) {
 			if (popVisible() && (forceListUpdate || !pidos.equals(this.pidos))) {
 				//  rebuild table
 				createProcessList(pidos);
 			}
 			// save pidos
 			this.pidos = new ArrayList<ProgressInfoDataObject>();
 			this.pidos.addAll(pidos);
 
 			if (popVisible()) {
 				// update progressbars
 				placeShell();
 
 				updateListProgressBars();
 
 				popup.getShell().setVisible(true);
 			}
 		} else {
 			this.pidos = new ArrayList<ProgressInfoDataObject>();
 			this.pidos.addAll(pidos);
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private boolean popVisible() {
 		Shell shell = popup.getShell();
 		return shell != null && !shell.isDisposed();
 	}
 
 	/**
 	 * TODO call every time the client is moved or resized
 	 */
 	private void placeShell() {
 		// pack it		
 		popup.getShell().pack();
 		Point point = toDisplay(0, 0);
 		Rectangle popupBounds = popup.getShell().getBounds();
 		popup.getShell().setBounds(point.x + getBounds().width - popupBounds.width, point.y - popupBounds.height - 4,
 				popupBounds.width, popupBounds.height);
 	}
 
 	private void updateListProgressBars() {
 		// update ProgressBars
 		for (ProgressInfoDataObject pido : pidos) {
 			if (freshValue(pido)) {
 				//update existing bar
 				ControlHolder holder = pido2controlHolder.get(pido.getKey());
 				updateProgressBar(pido, holder.progressBar);
 				updateLabel(pido, holder.label);
 				cacheValue(pido);
 			}
 
 		}
 	}
 
 	/**
 	 * updates the given {@link Label} with the value of the given
 	 * {@link ProgressInfoDataObject}. An {@link ILabelFormatter}-strategy
 	 * formats the value.
 	 * 
 	 * @param pido
 	 * @param label
 	 */
 	private void updateLabel(ProgressInfoDataObject pido, Label label) {
 		String strV = pido.getProcessName() + " " //$NON-NLS-1$
 				+ stateValueMappers.get(pido.getProcessState()).formatValue(pido.getValue());
 		label.setText(strV);
 	}
 
 	/**
 	 * strategy for formatting values of a {@link Label}
 	 */
 	interface ILabelFormatter {
 
 		/**
 		 * takes the value as {@link UIProcess} procentual progress
 		 */
 		String formatValue(int value);
 	}
 
 	/**
 	 * caches a {@link ProgressInfoDataObject}.
 	 * 
 	 * @param pido
 	 */
 	private void cacheValue(ProgressInfoDataObject pido) {
 		valueCache.put(pido.getKey(), pido);
 	}
 
 	/**
 	 * determines if the {@link ProgressInfoDataObject} has changed
 	 * 
 	 * @param pido
 	 * @return - true if the {@link ProgressInfoDataObject} has changed
 	 */
 	private boolean freshValue(ProgressInfoDataObject pido) {
 		ProgressInfoDataObject cached = valueCache.get(pido.getKey());
 		return cached == null || cached.compareTo(pido) != 0;
 	}
 
 	/**
 	 * just a container for a {@link ProgressBar} and a {@link Label} for
 	 * caching
 	 */
 	final static class ControlHolder {
 		ProgressBar progressBar;
 		Label label;
 	}
 
 	// maps the pidos key to a ControlHolder
 	private Map<Integer, ControlHolder> pido2controlHolder = new HashMap<Integer, ControlHolder>();
 	// cache for data objects
 	private Map<Integer, ProgressInfoDataObject> valueCache = new HashMap<Integer, ProgressInfoDataObject>();
 
 	/**
 	 * the process state label in the {@link Statusline}
 	 */
 	private Label statusLabel;
 
 	/**
 	 * the observed label for opening/closing the popup window
 	 */
 	private Label openLabel;
 
 	private Label noProcessActiveLable;
 
 	/**
 	 * creates a list of {@link UIProcess} infos in the popup
 	 */
 	private void createProcessList(List<ProgressInfoDataObject> pidos) {
 
 		checkNoActiveProcesses(pidos);
 		Control lastControl = null;
 		ProgressBar bar;
 		Label label;
 		Set<Integer> keys = new HashSet<Integer>();
 		for (ProgressInfoDataObject pido : pidos) {
 			keys.add(pido.getKey());
 			ControlHolder holder = pido2controlHolder.get(pido.getKey());
 			if (holder == null) {
 				holder = createControlHolder(pido);
 			}
 			bar = holder.progressBar;
 			label = holder.label;
 
 			//do layout stuff
 			FormData formData = new FormData();
 			formData.top = lastControl != null ? new FormAttachment(lastControl, 2) : new FormAttachment(5, 0);
 			formData.height = 14;
 			formData.width = 100;
 			label.setLayoutData(formData);
 
 			formData = new FormData();
 			formData.top = lastControl != null ? new FormAttachment(lastControl, 2) : new FormAttachment(5, 0);
 			formData.left = new FormAttachment(label, 3);
 			formData.height = 14;
 			formData.width = 60;
 			bar.setLayoutData(formData);
 
 			lastControl = bar;
 		}
 
 		// dispose and clear all controls of zombie pidos
 		clearZombies(keys);
 
 	}
 
 	private void checkNoActiveProcesses(List<ProgressInfoDataObject> pidos) {
 		if (pidos.size() == 0) {
 			noProcessRunning();
 		} else {
 			if (noProcessActiveLable != null) {
 				noProcessActiveLable.dispose();
 			}
 		}
 	}
 
 	private void noProcessRunning() {
 		FormData formData = new FormData();
 		formData.top = new FormAttachment(5, 0);
 		formData.height = 14;
 		formData.width = 160;
 		noProcessActiveLable = new Label(popupContent, SWT.NONE);
 		noProcessActiveLable.setBackground(listBackground);
 		noProcessActiveLable.setText("No active process"); //$NON-NLS-1$
 		noProcessActiveLable.setLayoutData(formData);
 	}
 
 	/**
 	 * Creates a basic {@link ControlHolder} with a {@link ProgressBar} and a
 	 * {@link Label}
 	 * 
 	 * @param pido
 	 * @return - the {@link ControlHolder}
 	 */
 	private ControlHolder createControlHolder(ProgressInfoDataObject pido) {
 		ControlHolder holder;
 		holder = new ControlHolder();
 		holder.progressBar = new ProgressBar(popupContent, SWT.NONE);
 		holder.progressBar.setBackground(LnfManager.getLnf().getColor(ILnfKeyConstants.STATUSLINE_BACKGROUND));
 		holder.progressBar.setMaximum(100);
 		holder.label = new Label(popupContent, SWT.NONE);
 		holder.label.setBackground(listBackground); //TODO get Color from lnf
 		pido2controlHolder.put(pido.getKey(), holder);
 		return holder;
 	}
 
 	/**
 	 * clears all {@link ControlHolder} related to
 	 * {@link ProgressInfoDataObject}-keys which are no more needed
 	 * 
 	 * @param activeKeys
 	 *            - the {@link Set} of keys defining the amount of active
 	 *            {@link ProgressInfoDataObject} instances
 	 */
 	private void clearZombies(Set<Integer> activeKeys) {
 
 		//clear all that are no more needed
 		Iterator<Integer> registeredKeys = pido2controlHolder.keySet().iterator();
 		while (registeredKeys.hasNext()) {
 			Integer key = registeredKeys.next();
 			if (!activeKeys.contains(key)) {
 				// dispose the ProgressBar
 				ControlHolder holder = pido2controlHolder.get(key);
 				holder.progressBar.dispose();
 				holder.label.dispose();
 				// remove entry
 				registeredKeys.remove();
 				// clean value cache
 				valueCache.remove(key);
 			}
 		}
 	}
 
 }

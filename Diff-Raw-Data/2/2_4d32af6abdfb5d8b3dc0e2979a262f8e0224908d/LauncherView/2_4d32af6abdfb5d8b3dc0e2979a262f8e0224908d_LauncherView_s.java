 /*******************************************************************************
  * Copyright (c) 2010 Freescale Semiconductor.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
  *******************************************************************************/
 package com.freescale.deadlockpreventer.agent;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.osgi.framework.Bundle;
 
 import com.freescale.deadlockpreventer.*;
 
 public class LauncherView extends ViewPart implements IAgent {
 	
 	private static final String CONTRIBUTOR_TAB_SELECTION = "contributor_tab_selection";
 
 	public static final String ID = "com.freescale.deadlockpreventer.agent.launcherView";
 	
 	private TableViewer viewer;
 	private Button terminate;
 	private Button throwsException;
 	private Button interactive;
 	private StyledText outputText;
 	private ArrayList<Conflict> conflictList = new ArrayList<LauncherView.Conflict>();
 
 	private Button logAndContinue;
 
 	private Button displayWarning;
 		
 	public String getPref(String key, String defaultValue) {
 		return new InstanceScope().getNode(Activator.PLUGIN_ID).get(key, defaultValue);
 	}
 
 	public String getViewID() {
 		return ID;
 	}
 	
 	public void setPref(String key, String value) {
 		new InstanceScope().getNode(Activator.PLUGIN_ID).put(key, value);
 	}
 	public void output(String string) {
 		if (!outputText.isDisposed())
 			outputText.append(string);
 	}
 	
 	public void resetOutput() {
 		outputText.setText("");
 		conflictList.clear();
 		viewer.refresh();
 	}
 
 	File deadlockPreventerJarPath = null;
 	File javaassistJarPath = null;
 	
 	public String getVMArg(int vmArgAgent) {
 		if (deadlockPreventerJarPath == null) {
 			Bundle bundle = Platform
 					.getBundle("com.freescale.deadlockpreventer.wrapper");
 			@SuppressWarnings("rawtypes")
 			Enumeration e = bundle.findEntries("/",
 					"com.freescale.deadlockpreventer.jar", false);
 			if (!e.hasMoreElements())
 				return null;
 			String installPath;
 			try {
 				installPath = FileLocator.toFileURL(((URL) e.nextElement())).getFile();
 				if (Platform.getOS().equals(Platform.OS_WIN32)) {
 					if (installPath.startsWith("/"))
 						installPath = installPath.substring(1);
 				}
 				deadlockPreventerJarPath = new File(installPath);
 			} catch (IOException e1) {
 			}
 		}
 		if (javaassistJarPath == null) {
 			Bundle bundle = Platform
 					.getBundle("javassist.wrapper");
 			@SuppressWarnings("rawtypes")
 			Enumeration e = bundle.findEntries("/",
 					"javassist.jar", false);
 			if (!e.hasMoreElements())
 				return null;
 			String installPath;
 			try {
 				installPath = FileLocator.toFileURL(((URL) e.nextElement())).getFile();
 				if (Platform.getOS().equals(Platform.OS_WIN32)) {
 					if (installPath.startsWith("/"))
 						installPath = installPath.substring(1);
 				}
 				javaassistJarPath = new File(installPath);
 			} catch (IOException e1) {
 			}
 		}
 
 		switch(vmArgAgent) {
 		case VM_ADDITIONAL_ARGUMENTS:
 			String exception = new InstanceScope().getNode(Activator.PLUGIN_ID).get(PREF_EXCEPTION_THROWS, null);
 			if (exception != null)
 				return "-Dcom.freescale.deadlockpreventer.throwingClass=" + exception;
 			return null;
 		case VM_ARG_AGENT:
 			return "-javaagent:" + deadlockPreventerJarPath.getAbsolutePath();
 		case VM_ARG_BOOT_CLASSPATH:
			return "-Xbootclasspath/a:" + deadlockPreventerJarPath.getAbsolutePath() + ":" + javaassistJarPath.getAbsolutePath();
 		case VM_ARG_BOOT_SERVER_PORT:
 			return "-Dcom.freescale.deadlockpreventer.connectToServer=localhost:" + Activator.getDefault().getServer().getListeningPort();
 		}
 		return null;
 	}
 
 	private final class ConflictHandler implements Runnable {
 		private final Conflict conflictItem;
 		public int result = 0;
 		
 		private ConflictHandler(Conflict conflictItem) {
 			this.conflictItem = conflictItem;
 		}
 
 		@Override
 		public void run() {
 			conflictList.add(conflictItem);
 			viewer.refresh();
 			result = handleConflict(conflictItem);
 		}
 	}
 
 	class Conflict {
 		public Conflict(String type, String threadID, String conflictThreadID,
 				String lock, String[] lockStack, String precedent,
 				String[] precedentStack, String conflict,
 				String[] conflictStack, String conflictPrecedent,
 				String[] conflictPrecedentStack, String message) {
 			super();
 			this.type = type;
 			this.threadID = threadID;
 			this.conflictThreadID = conflictThreadID;
 			this.lock = lock;
 			this.lockStack = lockStack;
 			this.precedent = precedent;
 			this.precedentStack = precedentStack;
 			this.conflict = conflict;
 			this.conflictStack = conflictStack;
 			this.conflictPrecedent = conflictPrecedent;
 			this.conflictPrecedentStack = conflictPrecedentStack;
 			this.message = message;
 		}
 		String type;
 		String threadID;
 		String conflictThreadID; 
 		String lock;
 		String[] lockStack; 
 		String precedent;
 		String[] precedentStack;
 		String conflict;
 		String[] conflictStack;
 		String conflictPrecedent;
 		String[] conflictPrecedentStack;
 		String message;
 		
 		public String toString() {
 			return type + ": '" + lock + "' conflicts with '" + precedent + "' in thread '" + threadID  + "' and '" + conflictThreadID + "'";
 		}
 
 
 		public boolean isError() {
 			return type.equals("ERROR");
 		}
 	}
 
 	class ViewContentProvider implements IStructuredContentProvider, 
 										   ITreeContentProvider {
 
         public void inputChanged(Viewer v, Object oldInput, Object newInput) {
 		}
         
 		public void dispose() {
 		}
         
 		public Object[] getElements(Object parent) {
 			return getChildren(parent);
 		}
         
 		public Object getParent(Object child) {
 			if (child instanceof Conflict) {
 				return conflictList;
 			}
 			return null;
 		}
         
 		public Object[] getChildren(Object parent) {
 			if (parent instanceof ArrayList) {
 				return getDisplayedConflicts();
 			}
 			return new Object[0];
 		}
 
         @SuppressWarnings("unchecked")
 		public boolean hasChildren(Object parent) {
 			if (parent instanceof ArrayList)
 				return ((ArrayList<Conflict>)parent).size() > 0;
 			return false;
 		}
 	}
 	
 	class ViewLabelProvider extends LabelProvider {
 
 		public String getText(Object obj) {
 			return obj.toString();
 		}
 		public Image getImage(Object obj) {
 			if (obj instanceof Conflict) {
 				String imageKey = ((Conflict) obj).isError() ? 
 						ISharedImages.IMG_OBJS_ERROR_TSK:
 							ISharedImages.IMG_OBJS_WARN_TSK;
 				return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
 			}
 			return null;
 		}
 	}
 
 	/**
      * This is a callback that will allow us to create the viewer and initialize
      * it.
      */
 	public void createPartControl(Composite parent) {
 		Composite borderComposite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout(1, false);
 		borderComposite.setLayout(layout);
 		
 		final TabFolder folder = new TabFolder(borderComposite, SWT.NONE);
 		folder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 
 		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("com.freescale.deadlockpreventer.agent.Configurator");
 		for (IConfigurationElement e : config) {
 			try {
 				Object o = e.createExecutableExtension("class");
 				if (o instanceof IConfigurator) {
 					IConfigurator configurator = (IConfigurator) o;
 					configurator.initialize(this);
 					TabItem tab = new TabItem(folder, SWT.NONE);
 					tab.setText(configurator.getName());
 					Composite tabComposite = new Composite(folder, SWT.NONE);
 					configurator.createPartControl(tabComposite);
 					tab.setControl(tabComposite);
 				}
 			} catch (CoreException e1) {
 				e1.printStackTrace();
 			}
 		}
 		
 		String selectedTab = getPref(CONTRIBUTOR_TAB_SELECTION, folder.getItem(0).getText());
 		TabItem[] items = folder.getItems();
 		for (int i = 0; i < items.length; i++) {
 			if (items[i].getText().equals(selectedTab)) {
 				folder.setSelection(i);
 				break;
 			}
 		}
 		
 		folder.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				TabItem item = folder.getItem(folder.getSelectionIndex());
 				setPref(CONTRIBUTOR_TAB_SELECTION, item.getText());
 			}
 		});
 		
 		createSettingsPart(borderComposite);
 		createResultPart(borderComposite);
 	}
 
 	private void createResultPart(Composite borderComposite) {
 		TabFolder folder = new TabFolder(borderComposite, SWT.NONE);
 		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		TabItem conflictTab = new TabItem(folder, SWT.NONE);
 		conflictTab.setText("Conflicts");
 		
 		Composite conflicts = new Composite(folder, SWT.NONE);
 		conflicts.setLayout(new GridLayout(3, false));
 		createConflictsPart(conflicts);
 		conflictTab.setControl(conflicts);
 
 		TabItem outputTab = new TabItem(folder, SWT.NONE);
 		outputTab.setText("Output");
 		
 		Composite output = new Composite(folder, SWT.NONE);
 		output.setLayout(new GridLayout(1, false));
 		createOutputPart(output);
 		outputTab.setControl(output);
 		
 	}
 
 	private void createConflictsPart(Composite conflicts) {
 		viewer = new TableViewer(conflicts, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		layoutData.horizontalSpan = 3;
 		viewer.getControl().setLayoutData(layoutData);
 		viewer.setContentProvider(new ViewContentProvider());
 		viewer.setLabelProvider(new ViewLabelProvider());
 		
 		Activator.getDefault().getServer().setListener(new NetworkServer.IListener() {
 			public int report(String type, String threadID, String conflictThreadID, 
 					String lock, String[] lockStack, 
 					String precedent, String[] precedentStack,
 					String conflict, String[] conflictStack,
 					String conflictPrecedent, String[] conflictPrecedentStack, String message) {
 				final Conflict conflictItem = new Conflict(type, threadID, conflictThreadID, 
 								lock, lockStack, 
 								precedent, precedentStack,
 								conflict, conflictStack,
 								conflictPrecedent, conflictPrecedentStack, message);
 				ConflictHandler handler = new ConflictHandler(conflictItem);
 				Display.getDefault().syncExec(handler);
 				return handler.result;
 			}
 		});
 		viewer.getControl().setLayoutData(layoutData);
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			@Override
 			public void doubleClick(DoubleClickEvent event) {
 				ISelection selection = viewer.getSelection();
 				if (selection instanceof IStructuredSelection) {
 					Conflict conflict = (Conflict) ((IStructuredSelection) selection).getFirstElement();
 					showInEditor(conflict);
 				}
 			}
 		});
 		viewer.getControl().addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				if (e.keyCode == SWT.DEL) {
 					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
 					if (!selection.isEmpty()) {
 						if (selection instanceof IStructuredSelection) {
 							Iterator<?> it = ((IStructuredSelection) selection).iterator();
 							while (it.hasNext()) {
 								Conflict conflict = (Conflict) it.next();
 								conflictList.remove(conflict);
 							}
 						}
 						viewer.refresh();
 					}
 				}
 			}
 		});
 		
 		displayWarning = new Button(conflicts, SWT.CHECK);
 		displayWarning.setText("Display warnings");
 		displayWarning.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 		displayWarning.setSelection(Boolean.parseBoolean(new InstanceScope().getNode(Activator.PLUGIN_ID).get(PREF_DISPLAY_WARNINGS, Boolean.toString(false))));
 		displayWarning.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				new InstanceScope().getNode(Activator.PLUGIN_ID).put(PREF_DISPLAY_WARNINGS, Boolean.toString(displayWarning.getSelection()));
 				viewer.refresh();
 			}
 		});
 		
 		Button editFilters = new Button(conflicts, SWT.PUSH);
 		editFilters.setText("Filters...");
 		editFilters.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 		editFilters.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				String filtersString = new InstanceScope().getNode(Activator.PLUGIN_ID).get(PREF_DISPLAY_FILTERS, defaultFilters);
 		        InputDialog dlg = new InputDialog(getSite().getShell(),
 		                "Conflict filters", "Conflicts matching the following list of regular expressions separated by semi-colons (;) will not be displayed:", 
 		                filtersString, new FilterValidator());
 		        if (dlg.open() == Window.OK)
 					new InstanceScope().getNode(Activator.PLUGIN_ID).put(PREF_DISPLAY_FILTERS, dlg.getValue());
 		        	viewer.refresh();
 		        }
 		});
 
 		Button copyToClipBoard = new Button(conflicts, SWT.PUSH);
 		copyToClipBoard.setText("Copy to clipboard");
 		copyToClipBoard.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 		copyToClipBoard.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				Conflict[] conflicts = getDisplayedConflicts();
 				if (conflicts.length > 0) {
 					StringBuffer buffer = new StringBuffer();
 					for (Conflict conflict : conflicts) {
 						buffer.append(conflict.message + "\n");
 					}
 				    Clipboard cb = new Clipboard(Display.getDefault());
 				    TextTransfer textTransfer = TextTransfer.getInstance();
 			        cb.setContents(new Object[] { buffer.toString() },
 			            new Transfer[] { textTransfer });
 				}
 			}
 		});
 		viewer.setInput(conflictList);
 	}
 	
 	static class FilterValidator implements IInputValidator {
 
 		@Override
 		public String isValid(String newText) {
 			try {
 				String[] filters = newText.split(";");
 				Pattern[] patterns = new Pattern[filters.length];
 				for (int i = 0; i < filters.length; i++) {
 					patterns[i] = Pattern.compile(filters[i]);
 				}
 			} catch (Exception e) {
 				return e.toString();
 			}
 			return null;
 		}
 		
 	}
 
 	protected void showInEditor(Conflict conflict) {
 		ConflictDialog dialog = new ConflictDialog(getSite().getShell(), conflict);
 		dialog.open();
 	}
 
 	protected String defaultFilters = "org\\.eclipse\\.osgi\\..*;";
 
 	private Button printToStdout;
 	
 	protected Conflict[] getDisplayedConflicts() {
 		String filtersString = new InstanceScope().getNode(Activator.PLUGIN_ID).get(PREF_DISPLAY_FILTERS, defaultFilters);
 		
 		String[] filters = filtersString.split(";");
 		Pattern[] patterns = new Pattern[filters.length];
 		for (int i = 0; i < filters.length; i++) {
 			patterns[i] = Pattern.compile(filters[i]);
 		}
 		boolean showWarning = displayWarning.getSelection();
 		ArrayList<Conflict> list = new ArrayList<LauncherView.Conflict>();
 		for (Conflict conflict : conflictList) {
 			if (!showWarning && !conflict.isError())
 				continue;
 			
 			boolean passFilters = true;
 			for (Pattern pattern : patterns) {
 				if (pattern.matcher(conflict.conflict).matches() ||
 						pattern.matcher(conflict.precedent).matches()) {
 					passFilters = false;
 					break;
 				}
 			}
 			if (passFilters)
 				list.add(conflict);
 		}
 		return list.toArray(new Conflict[0]);
 	}
 
 	private boolean passFilters(Conflict conflictItem) {
 		String filtersString = new InstanceScope().getNode(Activator.PLUGIN_ID).get(PREF_DISPLAY_FILTERS, defaultFilters);
 		
 		String[] filters = filtersString.split(";");
 		Pattern[] patterns = new Pattern[filters.length];
 		for (int i = 0; i < filters.length; i++) {
 			patterns[i] = Pattern.compile(filters[i]);
 		}
 		boolean showWarning = displayWarning.getSelection();
 		if (!showWarning && !conflictItem.isError())
 			return false;
 			
 		for (Pattern pattern : patterns) {
 			if (pattern.matcher(conflictItem.conflict).matches() ||
 					pattern.matcher(conflictItem.precedent).matches()) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	protected int handleConflict(Conflict conflictItem) {
 		if (!passFilters(conflictItem))
 			return IConflictListener.CONTINUE;
 		int flag = 0;
 		if (Boolean.parseBoolean(new InstanceScope().getNode(Activator.PLUGIN_ID).get(PREF_PRINT_TO_STDOUT, Boolean.toString(false))))
 			flag |= IConflictListener.LOG_TO_CONSOLE;
 		if (logAndContinue.getSelection())
 			return flag | IConflictListener.CONTINUE;
 		if (throwsException.getSelection())
 			return flag | IConflictListener.EXCEPTION;
 		if (terminate.getSelection())
 			return flag | IConflictListener.ABORT;
 		if (interactive.getSelection()) {
 			final String conflictMessage = conflictItem.message;
 			MessageDialog dialog = new MessageDialog(getSite().getShell(), 
 					"Deadlock confict occured", null, 
 					"An incorrect synchronization primitive acquisition order has occured.", 
 					MessageDialog.QUESTION, new String[] {"Continue", "Throw exception", "Terminate process"}, 0) {
 						protected Control createCustomArea(Composite parent) {
 							parent.setLayout(new GridLayout());
 							Text text = new Text(parent, SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 							text.setText(conflictMessage);
 							text.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 							GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
 							text.setLayoutData(layoutData);
 							return super.createCustomArea(parent);
 						}
 						protected Point getInitialSize() {
 							Point pt = super.getInitialSize();
 							pt.x = Math.min(pt.x, 600);
 							pt.y = Math.min(pt.y, 600);
 							return pt;
 						}
 			};
 			int index = dialog.open();
 			if (index == 1)
 				return flag | IConflictListener.EXCEPTION;
 			if (index == 2)
 				return flag | IConflictListener.ABORT;
 		}
 		return flag | IConflictListener.CONTINUE;
 	}
 
 	private void createOutputPart(Composite output) {
 		outputText = new StyledText(output, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL
 		        | SWT.H_SCROLL); 
 		outputText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 	}
 
 	private void createSettingsPart(Composite parent) {
 		Group group = new Group(parent, SWT.NONE);
 		group.setText("Deadlock conflict handling");
 		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		group.setLayoutData(gridData);
 
 		GridLayout layout = new GridLayout(1, false);
 		group.setLayout(layout);
 
 		logAndContinue = new Button(group, SWT.RADIO);
 		logAndContinue.setText("Log and continue");
 		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
 		logAndContinue.setLayoutData(gridData);
 		logAndContinue.setData(new Integer(1));
 
 		interactive = new Button(group, SWT.RADIO);
 		interactive.setText("Interactive");
 		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
 		interactive.setLayoutData(gridData);
 		interactive.setData(new Integer(0));
 
 		Composite exceptionGroup = new Composite(group, SWT.NONE);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.verticalIndent = 0;
 		gridData.verticalSpan = 0;
 		gridData.horizontalAlignment = 0;
 		gridData.horizontalIndent = 0;
 		exceptionGroup.setLayoutData(gridData);
 
 		layout = new GridLayout(2, false);
 		layout.marginBottom = 0;
 		layout.marginHeight = 0;
 		layout.marginLeft = 0;
 		layout.marginRight = 0;
 		layout.marginTop = 0;
 		layout.marginWidth = 0;
 		layout.horizontalSpacing = 0;
 		exceptionGroup.setLayout(layout);
 
 		throwsException = new Button(exceptionGroup, SWT.RADIO);
 		throwsException.setText("Throw");
 		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
 		throwsException.setLayoutData(gridData);
 		throwsException.setData(new Integer(2));
 
 		Link link = new Link(exceptionGroup, SWT.NONE);
 		link.setText("<a>exception</a>");
 		
 	    link.addListener(SWT.Selection, new Listener() {
 	        public void handleEvent(Event event) {
 				String exception = new InstanceScope().getNode(Activator.PLUGIN_ID).get(PREF_EXCEPTION_THROWS, "java.lang.RuntimeException");
 		        InputDialog dlg = new InputDialog(getSite().getShell(),
 		                "Exception thrown", "The following exception will be thrown when a conflict is detected:", 
 		                exception, new FilterValidator());
 		        if (dlg.open() == Window.OK)
 		        	new InstanceScope().getNode(Activator.PLUGIN_ID).put(PREF_EXCEPTION_THROWS, dlg.getValue());
 		        	viewer.refresh();
 		        }
 	        }
 	      );
 
 		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
 		link.setLayoutData(gridData);
 
 		terminate = new Button(group, SWT.RADIO);
 		terminate.setText("Terminate process");
 		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
 		terminate.setLayoutData(gridData);
 		terminate.setData(new Integer(3));
 
 		String defaultItem = new InstanceScope().getNode(Activator.PLUGIN_ID).get(PREF_DEFAULT_HANDLING, Integer.toString(1));
 		
 		Integer selectedItem = Integer.parseInt(defaultItem);
 		final Button[] radios = {interactive, logAndContinue, throwsException, terminate};
 		for (int i = 0; i < radios.length; i++) {
 			if (radios[i].getData().equals(selectedItem)) {
 				radios[i].setSelection(true);
 			}
 			radios[i].addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					Integer index = (Integer) e.widget.getData();
 					new InstanceScope().getNode(Activator.PLUGIN_ID).put(PREF_DEFAULT_HANDLING, index.toString());
 					if (e.widget == throwsException) {
 						interactive.setSelection(false);
 						logAndContinue.setSelection(false);
 						terminate.setSelection(false);
 					}
 					else {
 						if (throwsException.getSelection())
 							throwsException.setSelection(false);
 					}
 				}
 			});
 		}
 		printToStdout = new Button(group, SWT.CHECK);
 		printToStdout.setText("Print to process standard out");
 		printToStdout.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 		printToStdout.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				new InstanceScope().getNode(Activator.PLUGIN_ID).put(PREF_PRINT_TO_STDOUT, Boolean.toString(printToStdout.getSelection()));
 			}
 		});
 		printToStdout.setSelection(Boolean.parseBoolean(new InstanceScope().getNode(Activator.PLUGIN_ID).get(PREF_PRINT_TO_STDOUT, Boolean.toString(false))));
 
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 }

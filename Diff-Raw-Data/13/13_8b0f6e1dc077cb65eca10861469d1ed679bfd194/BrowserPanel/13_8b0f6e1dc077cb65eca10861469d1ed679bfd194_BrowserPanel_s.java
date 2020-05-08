 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.eclipse.views.server;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.jface.action.ContributionItem;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.browser.LocationAdapter;
 import org.eclipse.swt.browser.LocationEvent;
 import org.eclipse.swt.browser.ProgressAdapter;
 import org.eclipse.swt.browser.ProgressEvent;
 import org.eclipse.swt.browser.StatusTextEvent;
 import org.eclipse.swt.browser.StatusTextListener;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.oobium.console.ConsolePage;
 import org.oobium.eclipse.OobiumPlugin;
 import org.oobium.eclipse.views.server.actions.browser.GoAction;
 import org.oobium.eclipse.views.server.actions.browser.LaunchExternalBrowserAction;
 import org.oobium.eclipse.views.server.actions.browser.NavBackwardAction;
 import org.oobium.eclipse.views.server.actions.browser.NavForwardAction;
 import org.oobium.eclipse.views.server.actions.browser.RefreshAction;
 import org.oobium.eclipse.views.server.actions.browser.RefreshPathsAction;
 import org.oobium.eclipse.views.server.actions.browser.ShowSourceAction;
 import org.oobium.eclipse.views.server.actions.browser.StopAction;
 
 public class BrowserPanel extends Composite {
 
 	private ServerView view;
 	private Combo locationBar;
 	private Browser browser;
 	
 	private NavBackwardAction backwardAction;
 	private NavForwardAction forwardAction;
 	private RefreshAction refreshAction;
 	private StopAction stopAction;
 	private GoAction goAction;
 	private RefreshPathsAction refreshPathsAction;
 	private ShowSourceAction showSourceAction;
 	private LaunchExternalBrowserAction launchExternalAction;
 	
 	private boolean setUrlOnChange;
 	private boolean addHistory;
 	
 	private String[] history;
 	private int historyPosition;
 	
 	
 	public BrowserPanel(SashForm parent, ServerView view) {
 		super(parent, 0);
 		this.view = view;
 		history = new String[0];
 		createContents();
 	}
 	
 	public void createContents() {
 		backwardAction = new NavBackwardAction(this);
 		forwardAction = new NavForwardAction(this);
 		refreshAction = new RefreshAction(this);
 		stopAction = new StopAction(this);
 		goAction = new GoAction(this);
 		refreshPathsAction = new RefreshPathsAction(this);
 		showSourceAction = new ShowSourceAction(this);
 		launchExternalAction = new LaunchExternalBrowserAction(this);
 
 		backwardAction.setEnabled(false);
 		forwardAction.setEnabled(false);
 		refreshAction.setEnabled(false);
 		stopAction.setEnabled(false);
 		goAction.setEnabled(false);
 		refreshPathsAction.setEnabled(false);
 		launchExternalAction.setEnabled(false);
 
 		GridLayout layout = new GridLayout();
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		layout.verticalSpacing = 0;
 		setLayout(layout);
 
 		IContributionItem locationBarItem = new ContributionItem() {
 			@Override
 			public void fill(ToolBar parent, int index) {
 				ToolItem item = new ToolItem(parent, SWT.SEPARATOR);
 				locationBar = new Combo(parent, SWT.DROP_DOWN);
 				item.setControl(locationBar);
 				item.setWidth(400);
 				locationBar.addSelectionListener(new SelectionListener() {
 					@Override
 					public void widgetDefaultSelected(SelectionEvent e) {
 						go();
 					}
 					@Override
 					public void widgetSelected(SelectionEvent e) {
 						int len = locationBar.getText().length();
 						locationBar.setSelection(new Point(len, len));
 					}
 				});
 				locationBar.addModifyListener(new ModifyListener() {
 					@Override
 					public void modifyText(ModifyEvent e) {
 						String s = locationBar.getText();
 						if(history.length == 0) {
 							goAction.setEnabled(s.length() > 0);
 						} else {
 							goAction.setEnabled(!s.equals(history[historyPosition]));
 						}
 					}
 				});
 			}
 		};
 		
 		ToolBarManager tmanager = new ToolBarManager();
 		tmanager.add(backwardAction);
 		tmanager.add(forwardAction);
 		tmanager.add(refreshAction);
 		tmanager.add(stopAction);
 		tmanager.add(locationBarItem);
 		tmanager.add(goAction);
 		tmanager.add(new Separator());
 		tmanager.add(launchExternalAction);
 		tmanager.add(new Separator());
 		tmanager.add(refreshPathsAction);
 		ToolBar tb = tmanager.createControl(this);
 		tb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 
 		browser = new Browser(this, SWT.NONE);
 		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		MenuManager mmanager = new MenuManager();
 		mmanager.add(backwardAction);
 		mmanager.add(forwardAction);
 		mmanager.add(refreshAction);
 		mmanager.add(stopAction);
 		mmanager.add(new Separator());
 		mmanager.add(showSourceAction);
 		mmanager.add(launchExternalAction);
 		mmanager.add(new Separator());
 		mmanager.add(refreshPathsAction);
 		browser.setMenu(mmanager.createContextMenu(browser));
 		
 		browser.addProgressListener(new ProgressAdapter() {
 			@Override
 			public void completed(ProgressEvent event) {
 				stopAction.setEnabled(false);
 				refreshAction.setEnabled(true);
 				launchExternalAction.setEnabled(true);
 				if(historyPosition > 0) {
 					backwardAction.setEnabled(true);
 				} else {
 					backwardAction.setEnabled(false);
 				}
 				if(historyPosition == history.length-1) {
 					forwardAction.setEnabled(false);
 				} else {
 					forwardAction.setEnabled(true);
 				}
 				addHistory = true;
 				setUrlOnChange = true;
 			}
 		});
 		browser.addLocationListener(new LocationAdapter() {
 			@Override
 			public void changed(LocationEvent event) {
 				String url = browser.getUrl();
 				if(setUrlOnChange) {
 					go(url, true);
 				} else {
 					setLocationText(url);
 				}
 				if(addHistory) {
 					if(history.length == 0) {
 						historyPosition = 0;
						history = new String[1];
						history[0] = url;
					} else if(!url.equals(history[historyPosition])){
						if(historyPosition == history.length-1) {
 							historyPosition++;
 							history = Arrays.copyOf(history, historyPosition+1);
 						}
						history[historyPosition] = url;
 					}
 				}
 			}
 		});
 		browser.addStatusTextListener(new StatusTextListener() {
 			@Override
 			public void changed(final StatusTextEvent event) {
 				view.setStatus(OobiumPlugin.getImage("/icons/browser/browser_internal.gif"), event.text);
 			}
 		});
 	}
 	
 	Browser getBrowser() {
 		return browser;
 	}
 	
 	public void go() {
 		String location = locationBar.getText();
 		if(location.length() > 0 && location.indexOf("://") == -1) {
 			location = "http://" + location;
 		}
 		go(location, false);
 	}
 	
 	private void go(String url, boolean internal) {
 		if(url.length() > 0) {
 			goAction.setEnabled(false);
 			launchExternalAction.setEnabled(false);
 			backwardAction.setEnabled(false);
 			forwardAction.setEnabled(false);
 			if(internal) {
 				setLocationText(url);
 			} else {
 				addHistory = true;
 				setUrlOnChange = false;
 				browser.setUrl(url);
 			}
 			stopAction.setEnabled(true);
 		}
 	}
 
 	void updateAppState(boolean isRunning) {
 		refreshPathsAction.setEnabled(isRunning);
 	}
 	
 	public void launchExternalBrowser() {
 		String url = browser.getUrl();
 		url = url.startsWith("http://") ? url : ("http://" + url);
 		Program.launch(url);
 	}
 	
 	public void navBackward() {
 		if(historyPosition < 0) {
 			historyPosition = 0;
 		} else if(historyPosition > 0) {
 			historyPosition--;
 			setLocationText(history[historyPosition]);
 			addHistory = false;
 			setUrlOnChange = false;
 			browser.setUrl(history[historyPosition]);
 			backwardAction.setEnabled(false);
 			forwardAction.setEnabled(false);
 		}
 	}
 
 	public void navForward() {
 		if(historyPosition >= history.length) {
 			historyPosition = history.length - 1;
 		} else if(historyPosition < history.length - 1) {
 			historyPosition++;
 			setLocationText(history[historyPosition]);
 			addHistory = false;
 			setUrlOnChange = false;
 			browser.setUrl(history[historyPosition]);
 			backwardAction.setEnabled(false);
 			forwardAction.setEnabled(false);
 		}
 	}
 
 	public void refresh() {
 		stopAction.setEnabled(true);
 		setUrlOnChange = false;
 		Display.getDefault().syncExec(new Runnable() {
 			@Override
 			public void run() {
 				browser.refresh();
 			}
 		});
 	}
 
 	public void refreshPaths() {
 		refreshPaths(false);
 	}
 	
 	public void refreshPaths(final boolean load) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				String txt = locationBar.getText();
 				List<String> paths = view.getPaths();
 				locationBar.setItems(paths.toArray(new String[paths.size()]));
 				locationBar.setText(txt);
 				if(load) {
 					if(txt.length() == 0) {
 						for(int i = 0; i < locationBar.getItemCount(); i++) {
 							String path = locationBar.getItem(i);
 							if(path.indexOf('{') == -1) {
 								locationBar.select(i);
 								go();
 								return;
 							}
 						}
 					} else {
 						go();
 					}
 				}
 			}
 		});
 	}
 	
 	@Override
 	public boolean setFocus() {
 		return locationBar.setFocus();
 	}
 	
 	private void setLocationText(String location) {
 		locationBar.setText(location);
 		int i = location.length();
 		locationBar.setSelection(new Point(i, i));
 	}
 	
 	public void showSource() {
 		Shell shell = new Shell(getShell(), SWT.DIALOG_TRIM | SWT.RESIZE);
 		shell.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
 		shell.setLayout(new GridLayout());
 		
 		ConsolePage page = new ConsolePage(shell, SWT.READ_ONLY);
 		page.getConsole().setText(browser.getText());
 		page.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Rectangle bounds = getShell().getMonitor().getBounds();
 		shell.setSize(bounds.width/2, bounds.height/2);
 		
 		shell.open();
 	}
 	
 	public void stop() {
 		stopAction.setEnabled(false);
 		browser.stop();
 	}
 
 }

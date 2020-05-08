 package liquibase.eclipse.plugin.view.views;
 
 import java.sql.SQLException;
 
 import liquibase.eclipse.plugin.Activator;
 import liquibase.eclipse.plugin.controller.ChangeSetContentProvider;
 import liquibase.eclipse.plugin.controller.ChangeSetViewerLabelProvider;
 import liquibase.eclipse.plugin.controller.LiquibaseViewController;
 import liquibase.eclipse.plugin.model.ChangeSet;
 import liquibase.eclipse.plugin.model.DatabaseConfiguration;
 import liquibase.eclipse.plugin.view.wizards.LiqConfInspectWizard;
 import liquibase.eclipse.plugin.view.wizards.LiqConfWizard;
 import liquibase.exception.LiquibaseException;
 
 import org.eclipse.core.databinding.beans.BeanProperties;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.databinding.observable.set.IObservableSet;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.equinox.security.storage.ISecurePreferences;
 import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
 import org.eclipse.equinox.security.storage.StorageException;
 import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.part.ViewPart;
 
 
 /**
  * All the layout and data binding for the LiquibaseView.
  * 
  * @author afinke
  *
  */
 public class LiquibaseView extends ViewPart {
 
 	public static final String ID = "liquibase.eclipse.plugin.view.views.LiquibaseView";
 
 	private static final Image ADD_DB = 
 			Activator.getImageDescriptor("icons/database_add.png").createImage();
 	private static final Image DROP_DB = 
 			Activator.getImageDescriptor("icons/database_drop.png").createImage();
 	private static final Image INSPECT_DB = 
 			Activator.getImageDescriptor("icons/database_inspect.png").createImage();
 	
 	private TableViewer viewer;
 	private LiquibaseViewController liquibaseViewController;
 	private DatabaseConfiguration databaseConfiguration;
 	private String changeLogPath;
 	private Combo liqConfDropDown;
 	private Button displayAllChangeSetsCheckBox;
 	private Button releaseButton;
 	private Button restoreButton;
 
 	public LiquibaseView() throws ClassNotFoundException, SQLException {
 		liquibaseViewController = new LiquibaseViewController();
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		GridLayout layout = new GridLayout(2, true);
 		parent.setLayout(layout);
 		
 		Composite liqConfChooseComposite = new Composite(parent, SWT.NONE);
 		liqConfChooseComposite.setLayout(new GridLayout(1, true));
 		// Liquibase Configuration
 		// Drop Down
 		liqConfDropDown = new Combo(liqConfChooseComposite, SWT.DROP_DOWN | SWT.BORDER);
 		liqConfDropDown.setLayoutData(new GridData(
 				GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
 		liqConfDropDown.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				releaseButton.setEnabled(false);
 				changeLogPath = null;
 				databaseConfiguration = new DatabaseConfiguration();
 				ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
 				if (preferences.nodeExists(Activator.PLUGIN_ID)) {
 					ISecurePreferences node = preferences.node(Activator.PLUGIN_ID);
 					try {
 						String selection = liqConfDropDown.getText();
 						changeLogPath = node.get("changeLogPath:" + selection, "n/a");
 						databaseConfiguration.setUrl(node.get("url:" + selection, "n/a"));
 						databaseConfiguration.setUser(node.get("user:" + selection, "n/a"));
 						databaseConfiguration.setPassword(node.get("password:" + selection, "n/a"));
 					} catch (StorageException e1) {
 						e1.printStackTrace();
 					}
 				}
 				// initialize
 				if(initializeChangeLog(parent.getShell())) {
 					releaseButton.setEnabled(true);
 					restoreButton.setEnabled(true);
 				}
 				// add ResourceChangeListener
 				IWorkspace workspace = ResourcesPlugin.getWorkspace();
 				IResourceChangeListener listener = new IResourceChangeListener() {
 					@Override
 					public void resourceChanged(IResourceChangeEvent event) {
 						if(event.getType() == IResourceChangeEvent.POST_CHANGE) {
 							Display.getDefault().syncExec(new Runnable() {
 							    public void run() {
 							    	releaseButton.setEnabled(false);
 							    }
							});
							if(initializeChangeLog(parent.getShell())) {
								Display.getDefault().syncExec(new Runnable() {
								    public void run() {
								    	releaseButton.setEnabled(true);
								    }
								});
							}
 						}
 					}
 				};
 				workspace.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
 			}
 		});
 		liquibaseViewController.initializeliqConfDropDown(liqConfDropDown);
 		// Display mode
 		displayAllChangeSetsCheckBox = new Button(liqConfChooseComposite, SWT.CHECK);
 		displayAllChangeSetsCheckBox.setText("Display ran ChangeSets");
 		displayAllChangeSetsCheckBox.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent e) {
 						initializeChangeLog(parent.getShell());
 					}
 				});
 
 		// Configuration Buttons
 		Composite liqConfManageComposite = new Composite(parent, SWT.NONE);
 		liqConfManageComposite.setLayout(new GridLayout(3, true));
 		// Add
 		Button addLiqConfButton = new Button(liqConfManageComposite, SWT.PUSH);
 		addLiqConfButton.setImage(ADD_DB);
 		addLiqConfButton.setSize(32, 32);
 		addLiqConfButton.addSelectionListener(new SelectionAdapter() {
 		  @Override
 		  public void widgetSelected(SelectionEvent e) {
 		    LiqConfWizard liqConfWizard = new LiqConfWizard();
 		    WizardDialog wizardDialog = new WizardDialog(parent.getShell(), liqConfWizard);
 		    if (wizardDialog.open() == Window.OK) {
 		      liquibaseViewController.initializeliqConfDropDown(liqConfDropDown);
 		      String entry = liqConfWizard.getNewEntryName();
 		      selectComboEntry(entry);
 		    } 
 		  }
 		}); 
 		// Remove
 		Button dropLiqConfButton = new Button(liqConfManageComposite, SWT.PUSH);
 		dropLiqConfButton.setImage(DROP_DB);
 		dropLiqConfButton.setSize(32, 32);
 		dropLiqConfButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				String selection = liqConfDropDown.getText();
 				if(!selection.isEmpty()) {
 					boolean result = 
 							MessageDialog.openConfirm(parent.getShell(), "Confirm", 
 									"Are you sure to drop " + 
 									 "'" +  selection + "' " +
 									 "from Liquibase Configuration?");
 					if (result) {
 						liquibaseViewController.removeLiqConf(selection);
 						liquibaseViewController.initializeliqConfDropDown(liqConfDropDown);
 						releaseButton.setEnabled(false);
 						restoreButton.setEnabled(false);
 						viewer.setInput(null);
 					    viewer.refresh();
 					}
 				}
 			}
 		}); 
 		// Inspect
 		Button inspectLiqConfButton = new Button(liqConfManageComposite, SWT.PUSH);
 		inspectLiqConfButton.setImage(INSPECT_DB);
 		inspectLiqConfButton.setSize(32, 32);
 		Listener inspectLiqConfButtonListener = new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				String selection = liqConfDropDown.getText();
 					if(!selection.isEmpty()) {
 					WizardDialog wizardDialog = new WizardDialog(parent.getShell(),
 						      new LiqConfInspectWizard(selection));
 					if(wizardDialog.open() == Window.OK) {
 						// handle
 					}
 				}
 			}
 		};
 		inspectLiqConfButton.addListener(SWT.Selection, inspectLiqConfButtonListener);		
 		
 		// Operation Buttons
 		// Liquibase Release
 		releaseButton = new Button(parent, SWT.PUSH);
 		releaseButton.setText("Release");
 		releaseButton.setLayoutData(new GridData(
 				GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
 		Listener releaseButtonListener = new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				try {
 					liquibaseViewController.release(parent.getShell());
 				} catch (SQLException e) {
 					MessageDialog.openError(parent.getShell(), "Error", e.getMessage());
 				} catch (LiquibaseException e) {
 					MessageDialog.openError(parent.getShell(), "Error", e.getMessage());
 				}
 			}
 		};
 		releaseButton.addListener(SWT.Selection, releaseButtonListener);
 		releaseButton.setEnabled(false);
 		liquibaseViewController.setReleaseButton(releaseButton);
 		// Liquibase Restore
 		restoreButton = new Button(parent, SWT.PUSH);
 		restoreButton.setText("Restore");
 		restoreButton.setLayoutData(new GridData(
 				GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
 		Listener restoreButtonListener = new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				String versionToRollbackTo = liquibaseViewController.getLastVersion();
 				if(versionToRollbackTo != null) {
 					boolean result = 
 							MessageDialog.openConfirm(parent.getShell(), "Confirm", 
 									"Are you sure to restore version " + 
 									 "'" +  versionToRollbackTo + "' " + "?");
 					if (result) {
 						try {
 							liquibaseViewController.restore(versionToRollbackTo);
 						} catch (SQLException e) {
 							MessageDialog.openError(parent.getShell(), "Error", e.getMessage());
 						} catch (LiquibaseException e) {
 							MessageDialog.openError(parent.getShell(), "Error", e.getMessage());
 						}
 					} 
 				} else {
 					MessageDialog.openError(parent.getShell(), "Error", "There is no previous version.");
 				}
 				if(initializeChangeLog(parent.getShell())) {
 					releaseButton.setEnabled(true);
 				}
 			}
 		};
 		restoreButton.addListener(SWT.Selection, restoreButtonListener);
 		
 		createViewer(parent);
 	}
 
 	private void createViewer(Composite parent) {
 		viewer = new TableViewer(parent, 
 				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
 		
 		// data binding
 		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
 	    viewer.setContentProvider(contentProvider);
 	    
 	    IObservableSet knownElements = contentProvider.getKnownElements();
 	    IObservableMap ids = BeanProperties.value(ChangeSet.class,
 	        "id").observeDetail(knownElements);
 	    IObservableMap status = BeanProperties.value(ChangeSet.class,
 		        "status").observeDetail(knownElements);
 
 	    IObservableMap[] labelMaps = { ids, status };
 	    
 	    ILabelProvider labelProvider = new ChangeSetViewerLabelProvider(labelMaps);
 		
 		String[] titles = { "ID", "Status" };
 		int[] bounds = { 175, 100 };
 
 		createTableViewerColumn(titles[0], bounds[0], 0);
 		createTableViewerColumn(titles[1], bounds[1], 1);
 		
 		final Table table = viewer.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		// Layout the viewer
 		GridData gridData = new GridData();
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 2;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		viewer.getControl().setLayoutData(gridData);
 			
 	    viewer.setLabelProvider(labelProvider);		
 	}
 	
 	private TableViewerColumn createTableViewerColumn(
 			String title, int bound, final int colNumber) {
 		final TableViewerColumn tableViewerColumn = 
 				new TableViewerColumn(viewer, SWT.LEFT);
 		final TableColumn column = tableViewerColumn.getColumn();
 		column.setText(title);
 		column.setWidth(bound);
 		column.setResizable(true);
 		return tableViewerColumn;
 	}
 	
 	private boolean initializeChangeLog(Shell shell) {
 		try {
			liquibaseViewController.initializeChangeLog(changeLogPath, databaseConfiguration, displayAllChangeSetsCheckBox.getSelection());
 		} catch (LiquibaseException e) {
 			MessageDialog.openError(shell, "Error", e.getMessage());
 			return false;
 		} catch (SQLException e) {
 			MessageDialog.openError(shell, "Error", e.getMessage());
 			return false;
 		}
 		viewer.setInput(new WritableList(
 				ChangeSetContentProvider.getInstance().getChangeSets(), 
 				ChangeSet.class));
 		return true;
 	}
 
 	private void selectComboEntry(String entry) {
 		String[] items = liqConfDropDown.getItems();
 		for (int i = 0; i < items.length; i++) {
 			if(entry.equals(items[i])) {
 				liqConfDropDown.select(i);
 				Event event = new Event();
 				event.type = SWT.Selection;
 				event.widget = liqConfDropDown;
 				liqConfDropDown.notifyListeners(SWT.Selection, event);	
 			}
 		}
 	}
 
 	@Override
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 	
 }

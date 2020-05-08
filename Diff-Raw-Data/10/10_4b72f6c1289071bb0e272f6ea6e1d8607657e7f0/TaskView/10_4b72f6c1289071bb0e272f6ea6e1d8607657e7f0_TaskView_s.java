 package com.versionone.taskview.views;
 
 
 import java.util.ArrayList;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.part.ViewPart;
 
 import com.versionone.common.preferences.PreferenceConstants;
 import com.versionone.common.preferences.PreferencePage;
 import com.versionone.common.sdk.IProjectTreeNode;
 import com.versionone.common.sdk.IStatusCodes;
 import com.versionone.common.sdk.ProjectTreeNode;
 import com.versionone.common.sdk.Task;
 import com.versionone.common.sdk.V1Server;
 import com.versionone.taskview.Activator;
 
 /**
  * VersionOne Task View
  * 
  * @author Jerry D. Odenwelder Jr.
  *
  */
 public class TaskView extends ViewPart implements IPropertyChangeListener {
 	
 	private TableViewer viewer;
 	private StatusEditor statusEditor;
 	private Action selectProjectAction = null;
 	private Action refreshAction = null;
 	private Action saveAction = null;
 		
 	/**
 	 * The constructor.
 	 */
 	public TaskView() {
 		PreferencePage.getPreferences().addPropertyChangeListener(this);
 	}
 	
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialize it.
 	 */
 	public void createPartControl(Composite parent) {
 		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
 		configureTable();
 
 		makeActions();
 		contributeToActionBars();
 		
 //		hookContextMenu();
 //		hookDoubleClickAction();
 		
 		selectProvider();
 	}
 
 	/**
 	 * Select the content and label providers
 	 */
 	private void selectProvider() {
 
 		boolean isEnabled = isEnabled();
 		selectProjectAction.setEnabled(isEnabled);
 		refreshAction.setEnabled(isEnabled);
 		viewer.getTable().setEnabled(isEnabled);
 		viewer.setContentProvider(new ViewContentProvider());
 		
 		if(isEnabled) {		
 			loadTable();
 		}
 		else {
 			viewer.getTable().clearAll();
 			viewer.setSorter(null);
 			viewer.setInput(getViewSite());
 		}
 	}	
 
 	/**
 	 * Configure the table
 	 */
 	private void configureTable() {
 
 		TableViewerColumn column = createTableViewerColumn("ColumnTitle'ID", 70, SWT.LEFT);
 		column.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				try {
 					return ((Task)element).getID();
 				} catch (Exception e) {
 					Activator.logError(e);
 				}
 				return "*** Error ***";
 			}
 			
 			@Override
 			public Image getImage(Object element) {
 				return Activator.getDefault().getImageRegistry().get(Activator.TASK_IMAGE_ID);
 			}
 		});
 		column.setEditingSupport(new TaskIdEditor(viewer));
 
 		createTableViewerColumn("ColumnTitle'Parent", 200, SWT.LEFT).setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				try {
 					return ((Task)element).getStoryName();
 				} catch (Exception e) {
 					Activator.logError(e);
 				}
 				return "*** Error ***";
 			}
 		});
 		
 		column = createTableViewerColumn("ColumnTitle'Title", 150, SWT.LEFT);
 		column.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				try {
 					return ((Task)element).getName();
 				} catch (Exception e) {
 					Activator.logError(e);
 				}
 				return "*** Error ***";
 			}
 		});
 		column.setEditingSupport(new TaskEditor.NameEditor(viewer));
 
 		column = createTableViewerColumn("ColumnTitle'DetailEstimate", 100, SWT.CENTER);
 		column.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				try {
 					float temp = ((Task)element).getEstimate();
 					if(0 == temp)
 						return "";
 					return String.valueOf(temp);
 				} catch (Exception e) {
 					Activator.logError(e);
 				}
 				return "*** Error ***";
 			}			
 		});
 		column.setEditingSupport(new TaskEditor.EstimateEditor(viewer));
 		
 		column = createTableViewerColumn("ColumnTitle'ToDo", 50, SWT.CENTER);
 		column.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				try {
 					float temp = ((Task)element).getToDo();
 					if(0 == temp)
 						return "";
 					return String.valueOf(temp);
 				} catch (Exception e) {
 					Activator.logError(e);
 				}
 				return "*** Error ***";
 			}			
 		});
 		column.setEditingSupport(new TaskEditor.ToDoEditor(viewer));
 		
 		column = createTableViewerColumn("ColumnTitle'Status", 100, SWT.LEFT);
 		column.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				try {
 					return getStatusValues().getDisplayFromOid(((Task)element).getStatus());
 				} catch (Exception e) {
 					Activator.logError(e);
 				}
 				return "*** Error ***";
 			}			
 		});
 
 		if(this.isTrackEffort()) {
 			addEffortColumns();
 		}		
 
 		statusEditor = new StatusEditor(viewer, getStatusValues()); 
 		column.setEditingSupport(statusEditor);
 		
 		viewer.getTable().setLinesVisible(true);
 		viewer.getTable().setHeaderVisible(true);
 		viewer.getTable().setEnabled(isEnabled());
 
 	}
 
 	/**
 	 * Adds the columns needed to track effort
 	 */
 	private void addEffortColumns() {
 
 		createTableViewerColumn("ColumnTitle'Done", 50, SWT.CENTER, 4).setLabelProvider(new ColumnLabelProvider() {
 
 			@Override
 			public String getText(Object element)  {
 				try {
 					return ((Task)element).getDone();
 				} catch (Exception e) {
 					Activator.logError(e);
 				}
 				return "*** Error ***";
 			}			
 		});
 		
 		TableViewerColumn column = createTableViewerColumn("ColumnTitle'Effort", 50, SWT.CENTER, 5);
 		column.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				try {
 					float temp = ((Task)element).getEffort();
 					if(0 == temp)
 						return "";
 					return String.valueOf(temp);
 				} catch (Exception e) {
 					Activator.logError(e);
 				}
 				return "*** Error ***";
 			}			
 		});
 		column.setEditingSupport(new TaskEditor.EffortEditor(viewer));
 		
 		viewer.refresh();
 	}
 
 	/**
 	 * removes the columns needed when tracking effort
 	 */
 	private void removeEffortColumns() {
 
 		viewer.getTable().getColumn(5).dispose();
 		viewer.getTable().getColumn(4).dispose();		
 		viewer.refresh();
 	}
 	
 	private void makeActions() {
 		selectProjectAction  = new Action() {
 			public void run() {
 				IProjectTreeNode root = getProjectTreeNode();
 				IProjectTreeNode selectNode = null;
 				String projectToken = PreferencePage.getPreferences().getString(PreferenceConstants.P_PROJECT_TOKEN); 
 				if(!projectToken.equals("")) {
 					selectNode = new ProjectTreeNode("", projectToken);
 				}
 				ProjectSelectDialog projectSelectDialog = new ProjectSelectDialog(viewer.getControl().getShell(), root, selectNode);
 				projectSelectDialog.create();
 				projectSelectDialog.open();
 				loadTable();
 			}
 		};
 		selectProjectAction.setText("Select Project");
 		selectProjectAction.setToolTipText("Select Project");
 		selectProjectAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.FILTER_IMAGE_ID));
 		
 		refreshAction = new Action() {
 			public void run() {
 				loadTable();
 				statusEditor.setStatusCodes(getStatusValues());
 			}
 		};
 		refreshAction.setText("Refresh");
 		refreshAction.setToolTipText("Refresh");
 		refreshAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.REFRESH_IMAGE_ID));
 		
 		saveAction = new Action() {
 			public void run() {
 				TableItem[] rows = viewer.getTable().getItems();
 				ArrayList<Task> saveUs = new ArrayList<Task>();
 				for(int i = 0; i < rows.length; ++i) {
 					if(((Task)rows[i].getData()).isDirty()) {
 						saveUs.add((Task) rows[i].getData());
 					}
 				}
 				if(0 != saveUs.size()) {
 					try {
 						V1Server.getInstance().save(saveUs);
 					}
 					catch(Exception e) {
 						Activator.logError(e);
 						showMessage("Error saving task. Check Error log for more information.");
 					}		
 					loadTable();
 					statusEditor.setStatusCodes(getStatusValues());
 					showMessage(saveUs.size() + " Task Updated");
 				}
 			}			
 		};
 		saveAction.setText("Save");
 		saveAction.setToolTipText("Save");
 		saveAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.SAVE_IMAGE_ID));	
 	}
 
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown(bars.getMenuManager());
 		fillLocalToolBar(bars.getToolBarManager());
 	}
 	
 	private void fillLocalPullDown(IMenuManager manager) {
 		manager.add(selectProjectAction);
 		manager.add(refreshAction);
 		manager.add(saveAction);
 	}
 
 	private void fillLocalToolBar(IToolBarManager manager) {
 		manager.add(selectProjectAction);
 		manager.add(refreshAction);
 		manager.add(saveAction);
 	}
 	
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	/**
 	 * This method added for testing.  It provides access to the underlying TableViewer
 	 * @return TableViewer used in this control
 	 */	
 	public TableViewer getViewer() {return this.viewer;}
 
 	/**
 	 * Determine if VersionOne Task List is tracking effort
 	 * @return
 	 */
 	private boolean isTrackEffort() {
 		return PreferencePage.getPreferences().getBoolean(PreferenceConstants.P_TRACK_EFFORT);
 	}
 	
 	/**
 	 * Determine if VersionOne Task List is enabled 
 	 * @return
 	 */
 	private boolean isEnabled() {
 		return PreferencePage.getPreferences().getBoolean(PreferenceConstants.P_ENABLED);
 	}
 
 	/**
 	 * ContentProvider for VersionOne Task
 	 * @author jerry
 	 */
 	class ViewContentProvider implements IStructuredContentProvider {
 		public void inputChanged(Viewer v, Object oldInput, Object newInput) {}
 		public void dispose() {}
 		public Object[] getElements(Object parent) {
 			if(parent instanceof Task[]) {
 				return (Object[]) parent;
 			} else {
 				return new Object[]{};
 			}
 		}
 	}
 	
 	/**
 	 * Called when preferences change
 	 */
 	public void propertyChange(PropertyChangeEvent event) {
 		String property = event.getProperty();
 		if(property.equals(PreferenceConstants.P_ENABLED)) {
 			selectProvider();
 		}
 		else if(property.equals(PreferenceConstants.P_TRACK_EFFORT)) {
 			if(isTrackEffort()) {
 				this.addEffortColumns();
 			}
 			else {
 				this.removeEffortColumns();
 			}
 		}
 	}
 
 	/**
 	 * Get the projects from VersionOne 
 	 * @return
 	 */
 	private IProjectTreeNode getProjectTreeNode() {
 		try {
 			return V1Server.getInstance().getProjects();
 		} catch (Exception e) {
 			Activator.logError(e);
 			MessageDialog.openError(viewer.getControl().getShell(), "Project View Error", "Error Occurred Retrieving Projects. Check ErrorLog for more Details");			
 			return new ProjectTreeNode("root", "0");
 		}
 	}
 
 	/**
 	 * Load the Viewer with Task data
 	 */
 	private void loadTable() {
 		try {
 			viewer.setInput(V1Server.getInstance().getTasks());
 		} catch (Exception e) {
 			Activator.logError(e);
 			MessageDialog.openError(viewer.getControl().getShell(), "Task View Error", "Error Occurred Retrieving Task. Check ErrorLog for more Details");
 		}
 	}
 	
 
 	/**
 	 * Retrieve the StatusCodes from the server 
 	 * @return StatusCodes from the server or an empty collection
 	 */
 	private IStatusCodes getStatusValues() {
 		try {
 			return V1Server.getInstance().getTaskStatusValues();
 		} catch (Exception e) {
 			Activator.logError(e);
 			showMessage("Error retrieving Task Status from server. Additional informaiton available in Error log.");
 			return new IStatusCodes() {
 				String[] _data = new String[]{}; 
				@Override
 				public String getDisplayValue(int index) {
 					return "";
 				}
 
				@Override
 				public String[] getDisplayValues() {
 					return _data;
 				}
 
				@Override
 				public int getOidIndex(String oid) {
 					return 0;
 				}
 
				@Override
 				public String getID(int value) {
 					return "";
 				}
 
				@Override
 				public String getDisplayFromOid(String oid) {
 					return "";
 				}				
 			};
 		}
 	}
 	
 
 	/**
 	 * Create a TableViewerColumn with specified properties and append it to the end of the table
 	 * 
 	 * Calls createTableViewerColumn(String label, int width, int alignment, int index) with a -1 as the index
 	 * 
 	 * @param label - Column label
 	 * @param width - Column Width
 	 * @param alignment - Column alignment
 	 * @return new TableViewerColumn 
 	 */
 	TableViewerColumn createTableViewerColumn(String label, int width, int alignment) {
 		return createTableViewerColumn(label, width, alignment, -1);
 	}
 
 	/**
 	 * Create a TableViewerColumn at a specific column location
 	 * 
 	 * @param label - Column label
 	 * @param width - Column Width
 	 * @param alignment - Column alignment 
 	 * @param index - location for column.  -1 indicates the column goes at the end
 	 * @return new TableViewerColumn
 	 */
 	TableViewerColumn createTableViewerColumn(String label, int width, int alignment, int index) {
 		TableViewerColumn rc = null;
 		if(-1 == index) {
 			rc = new TableViewerColumn(viewer,SWT.NONE);	
 		}
 		else {
 			rc = new TableViewerColumn(viewer,SWT.NONE, index);
 		}		
 		rc.getColumn().setWidth(width);
 		rc.getColumn().setAlignment(alignment);
 		rc.getColumn().setText(V1Server.getInstance().getLocalString(label));
 		return rc;
 	}
 	
 	
 //	private void hookContextMenu() {
 //		MenuManager menuMgr = new MenuManager("#PopupMenu");
 //		menuMgr.setRemoveAllWhenShown(true);
 //		menuMgr.addMenuListener(new IMenuListener() {
 //			public void menuAboutToShow(IMenuManager manager) {
 //				TaskView.this.fillContextMenu(manager);
 //			}
 //		});
 //		Menu menu = menuMgr.createContextMenu(viewer.getControl());
 //		viewer.getControl().setMenu(menu);
 //		getSite().registerContextMenu(menuMgr, viewer);
 //	}
 //
 //	private void fillContextMenu(IMenuManager manager) {
 //		manager.add(action1);
 //		manager.add(action2);
 //		// Other plug-ins can contribute there actions here
 //		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 //	}
 //	
 //	private void hookDoubleClickAction() {
 //		viewer.addDoubleClickListener(new IDoubleClickListener() {
 //			public void doubleClick(DoubleClickEvent event) {
 //				doubleClickAction.run();
 //			}
 //		});
 //	}
 //
 	private void showMessage(String message) {
 		MessageDialog.openInformation(viewer.getControl().getShell(), "Task View", message);
 	}
 	
 }

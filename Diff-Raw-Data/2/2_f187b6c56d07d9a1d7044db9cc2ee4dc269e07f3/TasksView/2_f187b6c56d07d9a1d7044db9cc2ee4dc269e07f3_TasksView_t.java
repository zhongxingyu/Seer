 package com.uwusoft.timesheet;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.sql.Timestamp;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.DialogCellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 import com.uwusoft.timesheet.dialog.TaskListDialog;
 import com.uwusoft.timesheet.dialog.TimeDialog;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.extensionpoint.SubmissionService;
 import com.uwusoft.timesheet.model.Project;
 import com.uwusoft.timesheet.model.Task;
 import com.uwusoft.timesheet.util.ExtensionManager;
 import com.uwusoft.timesheet.util.MessageBox;
 
 public class TasksView extends ViewPart implements PropertyChangeListener {
 	public static final String ID = "com.uwusoft.timesheet.tasksview";
 	
     private static final String timeFormat = "HH:mm";
 
 	private StorageService storageService;
 	IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 	private TableViewer viewer;
 	
 	/**
 	 * The content provider class is responsible for providing objects to the
 	 * view. It can wrap existing objects in adapters or simply return objects
 	 * as-is. These objects may be sensitive to the current input of the view,
 	 * or ignore it and always show the same content (like Task List, for
 	 * example).
 	 */
 	class ViewContentProvider implements IStructuredContentProvider {
 		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
 		}
 
 		public void dispose() {
 		}
 
 		public Object[] getElements(Object parent) {
 			if (parent instanceof Object[]) {
 				return (Object[]) parent;
 			}
 			return new Object[0];
 		}
 	}
 
 	class ViewLabelProvider extends LabelProvider implements
 			ITableLabelProvider {
 		public String getColumnText(Object obj, int index) {
 			return getText(obj);
 		}
 
 		public Image getColumnImage(Object obj, int index) {
 			return getImage(obj);
 		}
 
 		public Image getImage(Object obj) {
 			return PlatformUI.getWorkbench().getSharedImages()
 					.getImage(ISharedImages.IMG_OBJ_ELEMENT);
 		}
 	}
 
 	/**
 	 * This is a callback that will allow us to create the viewer and initialize
 	 * it.
 	 */
 	public void createPartControl(Composite parent) {
 		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		createColumns(parent, viewer);
 		final Table table = viewer.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		viewer.setContentProvider(new ArrayContentProvider());
 
 		if((storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
 				.getService(preferenceStore.getString(StorageService.PROPERTY))) == null)
 			return;
 		
 		storageService.addPropertyChangeListener(this);
 		viewer.setInput(storageService.getTaskEntries(new Date()));
 		// Make the selection available to other views
 		getSite().setSelectionProvider(viewer);
 
 		// Layout the viewer
 		GridData gridData = new GridData();
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 2;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		viewer.getControl().setLayoutData(gridData);
 	}
 
 	private void createColumns(final Composite parent, final TableViewer viewer) {
 		String[] titles = { "Time", "Task", "Project", "Total" };
 		int[] bounds = { 70, 300, 100, 60 };
 
 		// First column is for the time
 		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
 		col.setEditingSupport(new EditingSupport(viewer) {
 
 		    protected boolean canEdit(Object element) {
 		        return true;
 		    }
 
 		    protected CellEditor getCellEditor(Object element) {
 		        return new TimeDialogCellEditor(viewer.getTable(), (Task) element);
 		    }
 
 		    protected Object getValue(Object element) {
		        return new SimpleDateFormat(timeFormat).format(((Task) element).getDateTime());
 		    }
 
 		    protected void setValue(Object element, Object value) {
 		    	Task entry = (Task) element;
 		    	if (value instanceof Timestamp)
 		    		entry.setDateTime((Timestamp) value);
 		    	else
 		    		try {
 		    			entry.setDateTime(new Timestamp(new SimpleDateFormat(timeFormat).parse((String) value).getTime()));
 		    		} catch (ParseException e) {
 		    			MessageBox.setError("Task's view", e.getLocalizedMessage());
 		    		}
 				storageService.updateTaskEntry(entry.getDateTime(), entry.getId());
 		        viewer.refresh(element);
 		    }
 		});
 		col.setLabelProvider(new ColumnLabelProvider() {
 			public String getText(Object element) {
 				return new SimpleDateFormat(timeFormat).format(((Task) element).getDateTime());
 			}
 			public Image getImage(Object obj) {
 				return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/clock.png").createImage();				
 			}
 		});
 
 		// Second column is for the task
 		col = createTableViewerColumn(titles[1], bounds[1], 1);
 		col.setEditingSupport(new EditingSupport(viewer) {
 
 		    protected boolean canEdit(Object element) {
 		        return !StorageService.CHECK_IN.equals(((Task) element).getTask());
 		    }
 
 		    protected CellEditor getCellEditor(Object element) {
 		        return new TaskListDialogCellEditor(viewer.getTable(), (Task) element);
 		    }
 
 		    protected Object getValue(Object element) {
 		        return ((Task) element).getTask();
 		    }
 
 		    protected void setValue(Object element, Object value) {
 		    	Task entry = (Task) element;
 		    	entry.setTask(String.valueOf(value));
 		        storageService.updateTaskEntry(entry, entry.getId());
 		        viewer.refresh(element);
 		    }
 		});
 		col.setLabelProvider(new ColumnLabelProvider() {
 			public String getText(Object element) {
 				Task task = (Task) element;
 				return task.getTask();
 			}
 			public Image getImage(Object obj) {
 				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
 			}
 		});
 		// Third column is for the project
 		col = createTableViewerColumn(titles[2], bounds[2], 2);
 		col.setEditingSupport(new EditingSupport(viewer) {
 
 			@Override
 			protected CellEditor getCellEditor(Object element) {
 				return null;
 			}
 
 			@Override
 			protected boolean canEdit(Object element) {
 				return false;
 			}
 
 			@Override
 			protected Object getValue(Object element) {
 		        return ((Task) element).getProject().getName();
 			}
 
 			@Override
 			protected void setValue(Object element, Object value) {
 			}
 			
 		});
 		col.setLabelProvider(new ColumnLabelProvider() {
 			public String getText(Object element) {
 				return ((Task) element).getProject().getName();
 			}
 			public Image getImage(Object obj) {
 				Task task = (Task) obj;
 				if (StorageService.CHECK_IN.equals(task.getTask()) || StorageService.BREAK.equals(task.getTask())) return null;
 				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
 			}
 		});
 		// Fourth column is for the total
 		col = createTableViewerColumn(titles[3], bounds[3], 3);
 		col.setEditingSupport(new EditingSupport(viewer) {
 
 			@Override
 			protected CellEditor getCellEditor(Object element) {
 				return null;
 			}
 
 			@Override
 			protected boolean canEdit(Object element) {
 				return false;
 			}
 
 			@Override
 			protected Object getValue(Object element) {
 		        return ((Task) element).getTotal();
 			}
 
 			@Override
 			protected void setValue(Object element, Object value) {
 			}			
 		});
 		col.setLabelProvider(new ColumnLabelProvider() {
 			public String getText(Object element) {
 				if (StorageService.CHECK_IN.equals(((Task) element).getTask()))	return "";
 				DecimalFormat df = new DecimalFormat( "0.00" );
 		        return df.format(((Task) element).getTotal());
 			}
 			public Image getImage(Object obj) {
 				if (StorageService.CHECK_IN.equals(((Task) obj).getTask())) return null;
 				return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/clock.png").createImage();				
 			}
 		});
 	}
 	
 	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
 		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
 		final TableColumn column = viewerColumn.getColumn();
 		column.setText(title);
 		column.setWidth(bound);
 		column.setResizable(true);
 		column.setMoveable(true);
 		return viewerColumn;
 
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 	
 	class TimeDialogCellEditor extends DialogCellEditor {
 
 		private Task entry;
 		
 		/**
 		 * @param parent
 		 */
 		public TimeDialogCellEditor(Composite parent, Task entry) {
 			super(parent);
 			this.entry = entry;
 		}
 
 		@Override
 		protected Object openDialogBox(Control cellEditorWindow) {
 			TimeDialog timeDialog = new TimeDialog(cellEditorWindow.getDisplay(), entry.getTask(), entry.getDateTime());
 			if (timeDialog.open() == Dialog.OK) {
 				return new SimpleDateFormat(timeFormat).format(timeDialog.getTime());
 			}
 			return null;
 		}		
 	}
 
 	class TaskListDialogCellEditor extends DialogCellEditor {
 
 		private Task entry;
 		
 		/**
 		 * @param parent
 		 */
 		public TaskListDialogCellEditor(Composite parent, Task entry) {
 			super(parent);
 			this.entry = entry;
 		}
 
 		@Override
 		protected Object openDialogBox(Control cellEditorWindow) {
 			TaskListDialog listDialog = new TaskListDialog(cellEditorWindow.getShell(), entry.getTask()
 					+ SubmissionService.separator + entry.getProject().getName()
 					+ SubmissionService.separator + entry.getProject().getSystem());
 			listDialog.setTitle("Tasks");
 			listDialog.setMessage("Select task");
 			listDialog.setContentProvider(ArrayContentProvider.getInstance());
 			listDialog.setLabelProvider(new ViewLabelProvider());
 			listDialog.setWidthInChars(70);
 			if (listDialog.open() == Dialog.OK) {
 			    String selectedTask = Arrays.toString(listDialog.getResult());
 			    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
 				if (StringUtils.isEmpty(selectedTask)) return null;
 				entry.setProject(new Project(listDialog.getProject(), listDialog.getSystem()));
 				return selectedTask;
 			}
 			return null;
 		}		
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		viewer.setInput(storageService.getTaskEntries(new Date()));
 		viewer.refresh();
 	}
 }

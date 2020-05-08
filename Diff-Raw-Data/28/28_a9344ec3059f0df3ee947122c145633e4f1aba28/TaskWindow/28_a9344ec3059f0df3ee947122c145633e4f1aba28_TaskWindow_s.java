 package ru.kt15.finomen.neerc.hall.desktop;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 import ru.kt15.finomen.neerc.core.LocaleManager;
 import ru.kt15.finomen.neerc.core.Localized;
 import ru.kt15.finomen.neerc.hall.Task;
 import ru.kt15.finomen.neerc.hall.Task.TaskPerformer;
 import ru.kt15.finomen.neerc.hall.Task.TaskState;
 import ru.kt15.finomen.neerc.hall.TaskListener;
 import ru.kt15.finomen.neerc.hall.TaskManager;
 
 public class TaskWindow extends Composite implements TaskListener, Localized {
 	private Table activeTasks;
 	private Table historyTasks;
 	private Map<Task.TaskPerformer, Integer> knownPerformerActive;
 	private Map<Task.TaskPerformer, Integer> knownPerformerHistory;
 	private Map<Task.TaskPerformer, TableColumn> performerActiveColumn;
 	private Map<Task.TaskPerformer, TableColumn> performerHistoryColumn;
 	private Map<Integer, TableItem> taskActiveRow;
 	private Map<Integer, TableItem> taskHistoryRow;
 	private LocaleManager localeManager;
 	private TableColumn tblclmnActiveText;
 	private TableColumn tblclmnActiveTime;
 	private TableColumn tblclmnActiveId;
 	private Group grpActiveTasks;
 	private TableColumn tblclmnId_1;
 	private TableColumn tblclmnTime_1;
 	private TableColumn tblclmnText_1;
 	private Group grpHistory;
 	
 	class StateChangeListener implements Listener {
 		private final Table table;
 		private final Shell shell;
 		public StateChangeListener(Shell shell, Table table) {
 			this.shell = shell;
 			this.table = table;
 			table.addListener(SWT.MouseDoubleClick, this);
 		}
 		
 		@Override
 		public void handleEvent(Event arg0) {
 			TableItem[] sel = table.getSelection();
 			if (sel.length == 1) {
 				Task task = (Task) sel[0].getData();
 				if (task.getState() != null) {
 					new TaskStateChange(localeManager, task, shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM).open();
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * Create the composite.
 	 * @param localeManager 
 	 * @param parent
 	 * @param style
 	 */
 	public TaskWindow(LocaleManager localeManager, Composite parent, int style) {
 		super(parent, style);
 		this.localeManager = localeManager;
 		
 		
 		knownPerformerActive = new HashMap<Task.TaskPerformer, Integer>();
 		knownPerformerHistory = new HashMap<Task.TaskPerformer, Integer>();
 		performerActiveColumn = new HashMap<Task.TaskPerformer, TableColumn>();
 		performerHistoryColumn = new HashMap<Task.TaskPerformer, TableColumn>();
 		taskActiveRow = new HashMap<Integer, TableItem>();
 		taskHistoryRow = new HashMap<Integer, TableItem>();
 		
 		setLayout(new FillLayout(SWT.VERTICAL));
 		
 		grpActiveTasks = new Group(this, SWT.NONE);
 		grpActiveTasks.setLayout(new FillLayout(SWT.HORIZONTAL));
 		
 		activeTasks = new Table(grpActiveTasks, SWT.BORDER | SWT.FULL_SELECTION);
 		activeTasks.setHeaderVisible(true);
 		activeTasks.setLinesVisible(true);
 		
 		int[] mask = {50, 100, 0};
 		new TableResizer(activeTasks, mask);
 		
 		tblclmnActiveId = new TableColumn(activeTasks, SWT.NONE);
 		tblclmnActiveId.setWidth(100);
 				
 		tblclmnActiveTime = new TableColumn(activeTasks, SWT.NONE);
 		tblclmnActiveTime.setWidth(100);
 		
 		tblclmnActiveText = new TableColumn(activeTasks, SWT.NONE);
 		tblclmnActiveText.setWidth(100);
 		
 		grpHistory = new Group(this, SWT.NONE);
 		grpHistory.setLayout(new FillLayout(SWT.HORIZONTAL));
 		
 		historyTasks = new Table(grpHistory, SWT.BORDER | SWT.FULL_SELECTION);
 		historyTasks.setHeaderVisible(true);
 		historyTasks.setLinesVisible(true);
 		new TableResizer(historyTasks, mask);
 		
 		tblclmnId_1 = new TableColumn(historyTasks, SWT.NONE);
 		tblclmnId_1.setWidth(100);
 				
 		tblclmnTime_1 = new TableColumn(historyTasks, SWT.NONE);
 		tblclmnTime_1.setWidth(100);
 		
 		tblclmnText_1 = new TableColumn(historyTasks, SWT.NONE);
 		tblclmnText_1.setWidth(100);
 		
 						
 		new StateChangeListener(getShell(), activeTasks);
 		new StateChangeListener(getShell(), historyTasks);
 				
 		localeManager.addLocalizedObject(this);
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 	
 	private void addTaskPerformers(Task.TaskPerformer[] list, Map<Task.TaskPerformer, Integer> knownPerformers, Map<Task.TaskPerformer, TableColumn> performerColumn, Table table) {
 		for (Task.TaskPerformer performer : list) {
 			if (knownPerformers.containsKey(performer)) {
 				knownPerformers.put(performer, knownPerformers.get(performer) + 1);
 			} else {
 				TableColumn tblclmn = new TableColumn(table, SWT.NONE);
 				tblclmn.setWidth(100);
 				tblclmn.setText(performer.getName());
 				tblclmn.setData(performer);
 				performerColumn.put(performer, tblclmn);
 				knownPerformers.put(performer, 1);
 			}
 		}
 	}
 	
 	private void removeTaskPerformers(Task.TaskPerformer[] list, Map<Task.TaskPerformer, Integer> knownPerformers, Map<Task.TaskPerformer, TableColumn> performerColumn) {
 		for (Task.TaskPerformer performer : list) {
 			knownPerformers.put(performer, knownPerformers.get(performer) - 1);
 			if (knownPerformers.get(performer) == 0) {
 				knownPerformers.remove(performer);
 				performerColumn.get(performer).dispose();
 				performerColumn.remove(performer);
 			}
 		}
 	}
 	
 	private void fillRowWithTask(TableItem row, Task task) {
 		row.setData(task);
 		row.setText(0, "#" + task.getId());
 		row.setText(1, task.getTime().toString());
 		row.setText(2, task.getText());
 		Table table = row.getParent();
 		
 		for (Task.TaskPerformer performer : task.getPerformerList()) {
 			for (int i = 3; i < table.getColumnCount(); ++i) {
 				if (performer.equals(table.getColumns()[table.getColumnOrder()[i]].getData())) {
					row.setText(table.getColumnOrder()[i], task.getState(performer).getMessage());
 					row.setImage(table.getColumnOrder()[i], new Image(getDisplay(), "resources/icons/STATUS_" + task.getState(performer).getId().name() + ".png"));
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void addTask(final Task task) {
 		getDisplay().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				
 				if (task.getState() != null) {
 					addTaskPerformers(task.getPerformerList(), knownPerformerActive, performerActiveColumn, activeTasks);
 					TableItem row = new TableItem(activeTasks, SWT.NONE);
 					fillRowWithTask(row, task);
 					taskActiveRow.put(task.getId(), row);
 				}
 				
 				addTaskPerformers(task.getPerformerList(), knownPerformerHistory, performerHistoryColumn, historyTasks);
 				TableItem row = new TableItem(historyTasks, SWT.NONE);
 				fillRowWithTask(row, task);
 				taskHistoryRow.put(task.getId(), row);
 			}
 			
 		});
 	}
 
 	@Override
 	public void removeTask(final int taskId) {
 		getDisplay().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				if (taskActiveRow.containsKey(taskId)) {
 					Task t = (Task) taskActiveRow.get(taskId).getData();
 					taskActiveRow.get(taskId).dispose();
 					removeTaskPerformers(t.getPerformerList(), knownPerformerActive, performerActiveColumn);
 				}
 				
 				Task t = (Task) taskHistoryRow.get(taskId).getData();
 				taskHistoryRow.get(taskId).dispose();
 				removeTaskPerformers(t.getPerformerList(), knownPerformerHistory, performerHistoryColumn);
 			}
 			
 		});
 	}
 
 	@Override
 	public void updateTask(final Task task) {
 		getDisplay().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				if (taskActiveRow.containsKey(task.getId())) {
 					Task t = (Task) taskActiveRow.get(task.getId()).getData();
 					if (task.getState() == null) {
 						taskActiveRow.get(task.getId()).dispose();
 					} else {
 						addTaskPerformers(task.getPerformerList(), knownPerformerActive, performerActiveColumn, activeTasks);
 						fillRowWithTask(taskActiveRow.get(task.getId()), task);
 					}
 					removeTaskPerformers(t.getPerformerList(), knownPerformerActive, performerActiveColumn);
 				}
 				
 				Task t = (Task) taskHistoryRow.get(task.getId()).getData();
 				addTaskPerformers(task.getPerformerList(), knownPerformerHistory, performerHistoryColumn, activeTasks);
 				fillRowWithTask(taskHistoryRow.get(task.getId()), task);
 				removeTaskPerformers(t.getPerformerList(), knownPerformerHistory, performerHistoryColumn);
 			}
 			
 		});
 	}
 
 	@Override
 	public void setLocaleStrings() {
 		tblclmnActiveId.setText(localeManager.localize("ID"));
 		tblclmnActiveTime.setText(localeManager.localize("Time"));
 		tblclmnActiveText.setText(localeManager.localize("Text"));
 		tblclmnId_1.setText(localeManager.localize("ID"));
 		tblclmnTime_1.setText(localeManager.localize("Time"));
 		tblclmnText_1.setText(localeManager.localize("Text"));
 		grpActiveTasks.setText(localeManager.localize("Active tasks"));
 		grpHistory.setText(localeManager.localize("History"));
 	}
 }

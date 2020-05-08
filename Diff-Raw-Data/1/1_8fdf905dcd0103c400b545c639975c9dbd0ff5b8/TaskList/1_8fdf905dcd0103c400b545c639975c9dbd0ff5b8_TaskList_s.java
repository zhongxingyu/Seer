 package ui.swing.mainScreen;
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Point;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.DropMode;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.TransferHandler;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import jira.JiraIssue;
 import jira.JiraOptions;
 import labels.labels.SelectedLabel;
 import lang.Maybe;
 
 import org.apache.commons.lang.UnhandledException;
 import org.reactive.Receiver;
 
 import tasks.ActiveTask;
 import tasks.Task;
 import tasks.TaskView;
 import ui.swing.mainScreen.dragAndDrop.TaskTransferable;
 import ui.swing.mainScreen.tasks.TaskSelectionListener;
 import ui.swing.tasks.SelectedTaskSource;
 import ui.swing.utils.SimpleInternalFrame;
 import ui.swing.utils.SwingUtils;
 import ui.swing.utils.UIEventsExecutor;
 import wheel.io.files.Directory;
 import basic.Alert;
 import basic.AlertImpl;
 import basic.DeferredExecutor;
 
 @SuppressWarnings("serial")
 public class TaskList extends JPanel {
 
 	public class ActiveTaskChanged implements Receiver<Maybe<Task>> {
 
 		@Override
 		public void receive(final Maybe<Task> pulse) {
 			final Maybe<Task> maybeActiveTask = pulse;
 
 			if (maybeActiveTask == null) {
 				return;
 			}
 
 			setSelectedTask(maybeActiveTask.unbox());
 
 		}
 	}
 
 	class FireChangeListeners implements Runnable {
 
 		public void run() {
 			final TaskView selectedValueInSwingThread = (TaskView) SelectedValueGetter
 					.getSelectedValueInSwingThread(tasksList);
 			selectedTask.supply(selectedValueInSwingThread);
 
 			SwingUtilities.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					for (final TaskSelectionListener listener : listeners) {
 						listener.selectionChangedTo(selectedValueInSwingThread);
 					}
 				}
 			});
 		}
 
 	}
 
 	private final TaskListListModel tasksListModel;
 	private final JList tasksList;
 	private final List<TaskSelectionListener> listeners;
 	private final LabelsList labelsList;
 	private final AlertImpl selectedLabelChanged;
 	private final AlertImpl movePeriodAlert;
 
 	private TaskView dropTargetTask;
 	private final TaskContextMenu taskContextMenu;
 
 	private final TasksListData screenData;
 	private final TaskListMemory memory;
 
 	private final DeferredExecutor executor;
 	private final SelectedTaskSource selectedTask;
 	private final ActiveTask activeTaskSignal;
 	private final UIEventsExecutor uiEventsExecutor;
 	private final JiraOptions jiraOptions;
 	private final SelectedLabel selectedLabel;
 	protected String movePerioData;
 
 	public TaskList(final TaskListModel model,
 			final UIEventsExecutor uiEventsExecutor,
 			final LabelsList labelsList, final Directory directory,
 			final TaskContextMenu taskContextMenu,
 			final SelectedTaskSource selectedTask, final ActiveTask activeTask,
 			final SelectedLabel selectedLabel, final JiraOptions jiraOptions) {
 
 		this.selectedLabel = selectedLabel;
 		this.executor = new DeferredExecutor(200, new FireChangeListeners());
 
 		this.uiEventsExecutor = uiEventsExecutor;
 		this.selectedTask = selectedTask;
 		this.activeTaskSignal = activeTask;
 		this.jiraOptions = jiraOptions;
 		this.memory = new DeferredTaskListMemory(directory);
 		this.screenData = memory.retrieve();
 		this.taskContextMenu = taskContextMenu;
 
 		this.labelsList = labelsList;
 
 		this.selectedLabelChanged = new AlertImpl();
 		movePeriodAlert = new AlertImpl();
 		this.listeners = new ArrayList<TaskSelectionListener>();
 		this.tasksListModel = new TaskListListModel();
 
 		this.tasksList = newJList(this.tasksListModel, model.getTooltips());
 
 		final JScrollPane scrolledLabelsList = new JScrollPane(labelsList);
 		final JScrollPane scrolledTasksList = new JScrollPane(this.tasksList);
 		scrolledLabelsList.setMinimumSize(new Dimension(0, 110));
 
 		final SimpleInternalFrame labels = new SimpleInternalFrame("Labels",
 				null, scrolledLabelsList);
 		final SimpleInternalFrame tasks = new SimpleInternalFrame("Tasks",
 				null, scrolledTasksList);
 
 		final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
 				labels, tasks);
 
 		hideLabelsIfPropertySet(labels, split);
 
 		split.setContinuousLayout(true);
 
 		this.setLayout(new BorderLayout());
 
 		final JButton createTaskButton = new JButton("Start task");
 		createTaskButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				uiEventsExecutor.execute(new Runnable() {
 
 					@Override
 					public void run() {
 						model.startTask();
 					}
 
 				});
 			}
 
 		});
 		createTaskButton.setFocusable(false);
 		final JPanel createTaskPannel = new JPanel();
 		createTaskPannel.setLayout(new BorderLayout());
 		createTaskPannel.add(createTaskButton, BorderLayout.CENTER);
 		createTaskPannel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
 				10));
 
 		this.add(createTaskPannel, BorderLayout.NORTH);
 		this.add(split, BorderLayout.CENTER);
 		this.setMinimumSize(new Dimension(180, 0));
 
 		bindToLabelsList();
 		bindToActiveTaskSignal();
 	}
 
 	private void hideLabelsIfPropertySet(final SimpleInternalFrame labels,
 			final JSplitPane split) {
 		if (System.getProperty("HIDE_LABELS") != null) {
 			split.remove(labels);
 		}
 	}
 
 	private void bindToActiveTaskSignal() {
 		activeTaskSignal.addReceiver(new ActiveTaskChanged());
 	}
 
 	private void bindToLabelsList() {
 		this.labelsList.getSelectionModel().addListSelectionListener(
 				new ListSelectionListener() {
 
 					public void valueChanged(final ListSelectionEvent e) {
 
 						try {
 							TaskList.this.labelsList.setCursor(new Cursor(
 									Cursor.WAIT_CURSOR));
 							TaskList.this.selectedLabelChanged.fire();
 							final int selectedIndex = labelsList
 									.getSelectedIndex();
 							screenData.setSelectedLabel(selectedIndex);
 							labelsList.setPreferredIndex(selectedIndex);
 							memory.mind(screenData);
 
 							if (labelsList.getSelectedValue() != null) {
 								selectedLabel.update((String) labelsList
 										.getSelectedValue());
 							}
 						} finally {
 							TaskList.this.labelsList.setCursor(new Cursor(
 									Cursor.DEFAULT_CURSOR));
 						}
 
 					}
 				});
 
 		labelsList.setPreferredIndex(screenData.getSelectedLabel());
 
 	}
 
 	public Alert selectedLabelChanged() {
 		return this.selectedLabelChanged;
 	}
 
 	public String selectedLabel() {
 		return (String) this.labelsList.getSelectedValue();
 	}
 
 	class SelectedTaskChangedListSelectionListener implements
 			ListSelectionListener {
 
 		@Override
 		public void valueChanged(final ListSelectionEvent e) {
 			uiEventsExecutor.execute(new Runnable() {
 
 				@Override
 				public void run() {
 					selectedTaskChanged(tasksList.getSelectedIndex());
 				}
 
 			});
 		}
 
 		void selectedTaskChanged(final int selectedIndex) {
 			fireTaskChangeListeners();
 
 			if (selectedIndex > -1) {
 				screenData.setSelectedTask(tasksListModel.getElementAt(
 						selectedIndex).name());
 				memory.mind(screenData);
 			}
 
 		}
 	}
 
 	private JList newJList(final TaskListListModel listModel,
 			final TooltipForTask tooltip) {
 		final JList tasksList = new TasksJList(listModel, tooltip);
 
 		final ListSelectionModel selectionModel = tasksList.getSelectionModel();
 
 		selectionModel
 				.addListSelectionListener(new SelectedTaskChangedListSelectionListener());
 
 		tasksList.setCellRenderer(new TaskListCellRenderer());
 
 		tasksList.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseClicked(final MouseEvent e) {
 
 				if (e.getButton() == MouseEvent.BUTTON1
 						&& e.getClickCount() == 2) {
 
 					final Maybe<JiraIssue> jiraIssue = ((TaskView) tasksList
 							.getSelectedValue()).getJiraIssue();
 
 					final Maybe<String> jiraUrl = jiraOptions.getURL();
 
 					if (jiraUrl != null && jiraIssue != null) {
 						final String url = "/browse/"
 								+ jiraIssue.unbox().getKey();
 						try {
 							final URI uri = new URI(jiraUrl.unbox() + url);
 							Desktop.getDesktop().browse(uri);
 						} catch (final IOException e1) {
 							throw new RuntimeException(e1);
 						} catch (final URISyntaxException e1) {
 							JOptionPane.showMessageDialog(TaskList.this,
 									"Invalid jira url: " + jiraUrl);
 						}
 					}
 				}
 
 				if (e.getButton() == MouseEvent.BUTTON3) {
 					final int index = tasksList.locationToIndex(e.getPoint());
 					selectionModel.setSelectionInterval(index, index);
 					final TaskView currentSelected = (TaskView) tasksList
 							.getSelectedValue();
 					TaskList.this.taskContextMenu.show(tasksList, e.getX(), e
 							.getY(), currentSelected);
 				}
 			}
 
 		});
 
 		tasksList.setTransferHandler(new TransferHandler() {
 
 			@Override
 			public int getSourceActions(final JComponent c) {
 				return COPY;
 			}
 
 			@Override
 			protected Transferable createTransferable(final JComponent c) {
 				return new TaskTransferable("a");
 			}
 
 			@Override
 			public boolean canImport(final TransferSupport info) {
 				try {
 					final String data = (String) getTransferableStringOrCry(info);
 					return data.startsWith("task");
 				} catch (final Exception e) {
 				}
 
 				return false;
 			}
 
 			@Override
 			public boolean importData(final TransferSupport info) {
 				final Point point = info.getDropLocation().getDropPoint();
 				final int index = tasksList.locationToIndex(point);
 				dropTargetTask = tasksListModel.getElementAt(index);
 				movePerioData = getTransferableStringOrCry(info);
 				movePeriodAlert.fire();
 				return true;
 			}
 
 			private String getTransferableStringOrCry(final TransferSupport info) {
 				try {
 					return (String) info.getTransferable().getTransferData(
 							DataFlavor.stringFlavor);
 				} catch (UnsupportedFlavorException e) {
 					throw new UnhandledException(e);
 				} catch (IOException e) {
 					throw new UnhandledException(e);
 				}
 			}
 		});
 
 		tasksList.setDragEnabled(true);
 		tasksList.setDropMode(DropMode.ON);
 
 		return tasksList;
 	}
 
 	public void setTasks(final List<TaskView> tasks) {
 
 		if (EventQueue.isDispatchThread()) {
 			internalSetTasks(tasks);
 		} else {
 			SwingUtils.invokeAndWaitOrCry(new Runnable() {
 				@Override
 				public void run() {
 					internalSetTasks(tasks);
 				}
 			});
 		}
 	}
 
 	private void internalSetTasks(final List<TaskView> tasks) {
 		this.tasksListModel.setTasks(tasks);
 
 		final boolean hasElements = this.tasksListModel.getSize() > 0;
 		if (!hasElements) {
 			return;
 		}
 
 		final Maybe<TaskView> taskByName = tasksListModel
 				.getTaskByName(screenData.getSelectedTask());
 
 		if (taskByName != null) {
 			setSelectedTask(taskByName.unbox());
 		} else {
 			setSelectedTask(tasksListModel.getElementAt(0));
 		}
 
 		fireTaskChangeListeners();
 	}
 
 	public void addTaskSelectionListener(final TaskSelectionListener listener) {
 		this.listeners.add(listener);
 		fireTaskChangeListeners();
 	}
 
 	private void fireTaskChangeListeners() {
 		executor.execute();
 	}
 
 	public void setSelectedTask(final TaskView task) {
 		class SetSelectedValue implements Runnable {
 
 			@Override
 			public void run() {
 				tasksList.setSelectedValue(task, true);
 			}
 
 		}
 		SwingUtilities.invokeLater(new SetSelectedValue());
 	}
 
 	public final Alert movePeriodAlert() {
 		return movePeriodAlert;
 	}
 
 	public TaskView dropTargetTask() {
 		return dropTargetTask;
 	}
 
 	public void setDropTargetTaskTesting(final TaskView dropTargetTask) {
 		this.dropTargetTask = dropTargetTask;
 	}
 
 	public String getMovePerioData() {
 		return movePerioData;
 	}
 
 }

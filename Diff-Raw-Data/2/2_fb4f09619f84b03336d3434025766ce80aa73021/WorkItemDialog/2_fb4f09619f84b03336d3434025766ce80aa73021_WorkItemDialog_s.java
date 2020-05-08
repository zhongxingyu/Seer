 package de.objectcode.time4u.client.ui.dialogs;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import de.objectcode.time4u.client.store.api.IProjectRepository;
 import de.objectcode.time4u.client.store.api.IRepository;
 import de.objectcode.time4u.client.store.api.ITaskRepository;
 import de.objectcode.time4u.client.store.api.ITodoRepository;
 import de.objectcode.time4u.client.store.api.IWorkItemRepository;
 import de.objectcode.time4u.client.ui.UIPlugin;
 import de.objectcode.time4u.client.ui.controls.ComboTreeViewer;
 import de.objectcode.time4u.client.ui.controls.DateCombo;
 import de.objectcode.time4u.client.ui.controls.TimeCombo;
 import de.objectcode.time4u.client.ui.provider.ProjectContentProvider;
 import de.objectcode.time4u.client.ui.provider.ProjectLabelProvider;
 import de.objectcode.time4u.client.ui.provider.TaskContentProvider;
 import de.objectcode.time4u.client.ui.provider.TaskLabelProvider;
 import de.objectcode.time4u.client.ui.provider.TodoLabelProvider;
 import de.objectcode.time4u.client.ui.provider.TodoListContentProvider;
 import de.objectcode.time4u.server.api.data.CalendarDay;
 import de.objectcode.time4u.server.api.data.DayInfo;
 import de.objectcode.time4u.server.api.data.ProjectSummary;
 import de.objectcode.time4u.server.api.data.TaskSummary;
 import de.objectcode.time4u.server.api.data.TodoSummary;
 import de.objectcode.time4u.server.api.data.WorkItem;
 
 public class WorkItemDialog extends Dialog
 {
   private final IProjectRepository m_projectRepository;
   private final ITaskRepository m_taskRepository;
   private final IWorkItemRepository m_workItemRepository;
   private final ITodoRepository m_todoRepository;
 
   private DateCombo m_dateText;
   private TimeCombo m_beginText;
   private TimeCombo m_endText;
   private Text m_commentText;
   private final WorkItem m_workItem;
   private ComboTreeViewer m_projectTreeViewer;
   private ComboViewer m_taskViewer;
   private ComboViewer m_todoViewer;
   private final boolean m_create;
 
   public WorkItemDialog(final IShellProvider shellProvider, final IRepository repository, final ProjectSummary project,
       final TaskSummary task, final CalendarDay calendarDay)
   {
     super(shellProvider);
 
     setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | getDefaultOrientation());
 
     m_projectRepository = repository.getProjectRepository();
     m_taskRepository = repository.getTaskRepository();
     m_workItemRepository = repository.getWorkItemRepository();
     m_todoRepository = repository.getTodoRepository();
 
     m_workItem = new WorkItem();
     m_workItem.setDay(calendarDay);
     m_workItem.setComment("");
     m_workItem.setTaskId(task != null ? task.getId() : null);
     m_workItem.setProjectId(project != null ? project.getId() : null);
     m_create = true;
     m_workItem.setBegin(0);
 
     try {
       final DayInfo dayInfo = m_workItemRepository.getDayInfo(m_workItem.getDay());
 
       if (dayInfo != null && !dayInfo.getWorkItems().isEmpty()) {
         m_workItem.setBegin(0);
         for (final WorkItem workItem : dayInfo.getWorkItems()) {
           if (workItem.getEnd() > workItem.getBegin()) {
             m_workItem.setBegin(workItem.getEnd());
           }
         }
       }
     } catch (final Exception e) {
     }
     m_workItem.setEnd(m_workItem.getEnd());
   }
 
   public WorkItemDialog(final IShellProvider shellProvider, final IRepository repository, final WorkItem workItem)
   {
     super(shellProvider);
 
     setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | getDefaultOrientation());
 
     m_projectRepository = repository.getProjectRepository();
     m_taskRepository = repository.getTaskRepository();
     m_workItemRepository = repository.getWorkItemRepository();
     m_todoRepository = repository.getTodoRepository();
 
     m_workItem = workItem;
     if (m_workItem.getComment() == null) {
       m_workItem.setComment("");
     }
     m_create = false;
   }
 
   public WorkItem getWorkItem()
   {
     return m_workItem;
   }
 
   @Override
   protected void configureShell(final Shell newShell)
   {
     super.configureShell(newShell);
 
     if (m_create) {
       newShell.setText(UIPlugin.getDefault().getString("dialog.workItem.new.title"));
     } else {
       newShell.setText(UIPlugin.getDefault().getString("dialog.workItem.edit.title"));
     }
   }
 
   @Override
   protected Control createDialogArea(final Composite parent)
   {
     final Composite composite = (Composite) super.createDialogArea(parent);
     final Composite root = new Composite(composite, SWT.NONE);
     root.setLayout(new GridLayout(4, false));
     root.setLayoutData(new GridData(GridData.FILL_BOTH));
 
     final Label dateLabel = new Label(root, SWT.NONE);
     dateLabel.setText(UIPlugin.getDefault().getString("workItem.day.label"));
     m_dateText = new DateCombo(root, m_create ? SWT.BORDER : SWT.BORDER | SWT.READ_ONLY);
     GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
     gridData.horizontalSpan = 3;
     m_dateText.setLayoutData(gridData);
     m_dateText.select(m_workItem.getDay().getCalendar());
 
     final Label beginLabel = new Label(root, SWT.NONE);
     beginLabel.setText(UIPlugin.getDefault().getString("workItem.begin.label"));
     m_beginText = new TimeCombo(root, SWT.BORDER);
     m_beginText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
     m_beginText.select(m_workItem.getBegin());
     m_beginText.addSelectionListener(new SelectionAdapter() {
       @Override
       public void widgetSelected(final SelectionEvent e)
       {
         enableOkButton();
       }
     });
     m_beginText.setFocus();
 
     final Label endLabel = new Label(root, SWT.NONE);
     endLabel.setText(UIPlugin.getDefault().getString("workItem.end.label"));
     m_endText = new TimeCombo(root, SWT.BORDER);
     m_endText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
     m_endText.select(m_workItem.getEnd());
     m_endText.addSelectionListener(new SelectionAdapter() {
       @Override
       public void widgetSelected(final SelectionEvent e)
       {
         enableOkButton();
       }
     });
 
     final Label projectTreeLabel = new Label(root, SWT.LEFT);
     projectTreeLabel.setText(UIPlugin.getDefault().getString("project.label"));
 
     m_projectTreeViewer = new ComboTreeViewer(root, SWT.BORDER | SWT.DROP_DOWN);
     gridData = new GridData(GridData.FILL_HORIZONTAL);
     gridData.horizontalSpan = 3;
     m_projectTreeViewer.setLayoutData(gridData);
    m_projectTreeViewer.setContentProvider(new ProjectContentProvider(m_projectRepository, false));
     m_projectTreeViewer.setLabelProvider(new ProjectLabelProvider());
     m_projectTreeViewer.setInput(new Object());
 
     final Label taskLabel = new Label(root, SWT.LEFT);
     taskLabel.setText(UIPlugin.getDefault().getString("task.label"));
 
     m_taskViewer = new ComboViewer(root, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
     gridData = new GridData(GridData.FILL_HORIZONTAL);
     gridData.horizontalSpan = 3;
     m_taskViewer.getCombo().setLayoutData(gridData);
     m_taskViewer.setContentProvider(new TaskContentProvider(m_taskRepository, m_create));
     m_taskViewer.setLabelProvider(new TaskLabelProvider());
     m_taskViewer.addSelectionChangedListener(new ISelectionChangedListener() {
       public void selectionChanged(final SelectionChangedEvent event)
       {
         enableOkButton();
       }
     });
 
     final Label commentLabel = new Label(root, SWT.LEFT);
     commentLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
     commentLabel.setText(UIPlugin.getDefault().getString("workItem.comment.label"));
     m_commentText = new Text(root, SWT.BORDER | SWT.MULTI);
     gridData = new GridData(GridData.FILL_BOTH);
     gridData.horizontalSpan = 3;
     gridData.widthHint = convertWidthInCharsToPixels(60);
     gridData.heightHint = convertHeightInCharsToPixels(4);
     m_commentText.setLayoutData(gridData);
     m_commentText.setText(m_workItem.getComment());
     m_commentText.setTextLimit(1000);
     m_commentText.addTraverseListener(new TraverseListener() {
       public void keyTraversed(final TraverseEvent e)
       {
         if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
           e.doit = true;
         } else if (e.detail == SWT.TRAVERSE_RETURN && e.stateMask != 0) {
           e.doit = true;
         }
       }
     });
 
     final Label todoLabel = new Label(root, SWT.LEFT);
     todoLabel.setText(UIPlugin.getDefault().getString("workItem.todo.label"));
     m_todoViewer = new ComboViewer(root, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
     gridData = new GridData(GridData.FILL_HORIZONTAL);
     gridData.horizontalSpan = 3;
     m_todoViewer.getCombo().setLayoutData(gridData);
     m_todoViewer.setContentProvider(new TodoListContentProvider(m_todoRepository));
     m_todoViewer.setLabelProvider(new TodoLabelProvider());
 
     m_projectTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
       public void selectionChanged(final SelectionChangedEvent event)
       {
         final ISelection selection = event.getSelection();
 
         if (selection != null && selection instanceof IStructuredSelection) {
           final Object sel = ((IStructuredSelection) selection).getFirstElement();
 
           m_taskViewer.setInput(sel);
           enableOkButton();
         }
       }
     });
     m_taskViewer.addSelectionChangedListener(new ISelectionChangedListener() {
       public void selectionChanged(final SelectionChangedEvent event)
       {
         final ISelection selection = event.getSelection();
 
         if (selection != null && selection instanceof IStructuredSelection) {
           final Object sel = ((IStructuredSelection) selection).getFirstElement();
 
           m_todoViewer.setInput(sel);
           enableOkButton();
         }
       }
     });
     try {
       if (m_workItem.getProjectId() != null) {
         m_projectTreeViewer.setSelection(new StructuredSelection(m_projectRepository.getProjectSummary(m_workItem
             .getProjectId())));
       }
       if (m_workItem.getTaskId() != null) {
         m_taskViewer.setSelection(new StructuredSelection(m_taskRepository.getTaskSummary(m_workItem.getTaskId())));
       }
       if (m_workItem.getTodoId() != null) {
         m_todoViewer.setSelection(new StructuredSelection(m_todoRepository.getTodoSummary(m_workItem.getTodoId())));
       } else {
         m_todoViewer.setSelection(new StructuredSelection(TodoListContentProvider.EMPTY));
       }
     } catch (final Exception e) {
       UIPlugin.getDefault().log(e);
     }
 
     return composite;
   }
 
   @Override
   protected Control createButtonBar(final Composite parent)
   {
     final Control control = super.createButtonBar(parent);
 
     enableOkButton();
 
     return control;
   }
 
   @Override
   protected void okPressed()
   {
     m_workItem.setBegin(m_beginText.getSelection());
     m_workItem.setEnd(m_endText.getSelection());
     m_workItem.setComment(m_commentText.getText());
     if (m_create) {
       m_workItem.setDay(new CalendarDay(m_dateText.getSelection()));
     }
     final ISelection projectSelection = m_projectTreeViewer.getSelection();
     if (projectSelection instanceof IStructuredSelection) {
       final Object obj = ((IStructuredSelection) projectSelection).getFirstElement();
 
       if (obj != null && obj instanceof ProjectSummary) {
         m_workItem.setProjectId(((ProjectSummary) obj).getId());
       }
     }
     final ISelection taskSelection = m_taskViewer.getSelection();
     if (taskSelection instanceof IStructuredSelection) {
       final Object obj = ((IStructuredSelection) taskSelection).getFirstElement();
 
       if (obj != null && obj instanceof TaskSummary) {
         m_workItem.setTaskId(((TaskSummary) obj).getId());
       }
     }
     final ISelection todoSelection = m_todoViewer.getSelection();
     if (todoSelection instanceof IStructuredSelection) {
       final Object obj = ((IStructuredSelection) todoSelection).getFirstElement();
 
       if (obj != null && obj instanceof TodoSummary) {
         m_workItem.setTodoId(((TodoSummary) obj).getId());
       } else {
         m_workItem.setTodoId(null);
       }
     }
 
     super.okPressed();
   }
 
   private void enableOkButton()
   {
     if (m_beginText == null || m_endText == null || m_taskViewer == null) {
       return;
     }
 
     final int beginSelection = m_beginText.getSelection();
     final int endSelection = m_endText.getSelection();
 
     if (endSelection < beginSelection) {
       m_endText.select(beginSelection);
     }
 
     final ISelection taskSelection = m_taskViewer.getSelection();
 
     final Button button = getButton(IDialogConstants.OK_ID);
 
     if (button != null) {
       button.setEnabled(beginSelection >= 0 && beginSelection <= 24 * 3600 && endSelection >= 0
           && endSelection <= 24 * 3600 && beginSelection <= endSelection && !taskSelection.isEmpty());
     }
   }
 }

 package net.todd.biblestudy.rcp.views;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.swing.event.EventListenerList;
 
 import net.todd.biblestudy.db.Note;
 import net.todd.biblestudy.rcp.presenters.IExportNotesListener;
 import net.todd.biblestudy.rcp.presenters.ViewEvent;
 
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.PlatformUI;
 
 public class ExportNotesView extends Dialog implements IExportNotesView
 {
 	private static final String NOTE_NAME_COLUMN_HEADER = "Note";
 	private static final String LAST_MODIFIED_COLUMN_HEADER = "Last modified";
 	private static final String CREATED_COLUMN_HEADER = "Created on";
 
 	private static final int NOTE_NAME_COLUMN_WIDTH = 200;
 	private static final int LAST_MODIFIED_COLUMN_WIDTH = 100;
 	private static final int CREATED_COLUMN_WIDTH = 100;
 
 	private EventListenerList eventListeners = new EventListenerList();
 	private TableViewer notesTableViewer;
 	private Table notesTable;
 	private TableColumn noteNameColumn;
 	private TableColumn lastModifiedColumn;
 	private TableColumn createdColumn;
 	private List<Note> selectedNotes = new ArrayList<Note>();
 
 	public ExportNotesView(Shell shell)
 	{
 		super(shell);
 		setShellStyle(getShellStyle() | SWT.RESIZE);
 	}
 
 	@Override
 	protected void configureShell(Shell newShell)
 	{
 		super.configureShell(newShell);
 		newShell.setText("Export Notes");
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent)
 	{
 		GridLayout gridLayout = new GridLayout(1, false);
 		gridLayout.marginTop = 2;
 		gridLayout.marginBottom = 2;
 		gridLayout.marginLeft = 2;
 		gridLayout.marginRight = 2;
 
 		GridData compositeLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		compositeLayoutData.widthHint = 450;
 		compositeLayoutData.heightHint = 200;
 
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(gridLayout);
 		composite.setLayoutData(compositeLayoutData);
 
 		notesTableViewer = new TableViewer(composite, SWT.CHECK | SWT.V_SCROLL | SWT.BORDER
				| SWT.SHADOW_ETCHED_IN | SWT.FULL_SELECTION);
 		notesTableViewer.setContentProvider(new ArrayContentProvider());
 		notesTableViewer.setLabelProvider(new ExportNoteLabelProvider());
 
 		notesTable = notesTableViewer.getTable();
 		notesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		notesTable.setHeaderVisible(true);
 		notesTable.setLinesVisible(true);
 
 		addCheckListener();
 
 		noteNameColumn = new TableColumn(notesTable, SWT.LEFT);
 		noteNameColumn.setText(NOTE_NAME_COLUMN_HEADER);
 		noteNameColumn.setWidth(NOTE_NAME_COLUMN_WIDTH);
 		noteNameColumn.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				notesTable.setSortColumn(noteNameColumn);
 				((NoteViewerSorter) notesTableViewer.getSorter()).doSort(1);
 				notesTableViewer.refresh();
 			}
 		});
 
 		lastModifiedColumn = new TableColumn(notesTable, SWT.LEFT);
 		lastModifiedColumn.setText(LAST_MODIFIED_COLUMN_HEADER);
 		lastModifiedColumn.setWidth(LAST_MODIFIED_COLUMN_WIDTH);
 		lastModifiedColumn.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				notesTable.setSortColumn(noteNameColumn);
 				((NoteViewerSorter) notesTableViewer.getSorter()).doSort(2);
 				notesTableViewer.refresh();
 			}
 		});
 
 		createdColumn = new TableColumn(notesTable, SWT.LEFT);
 		createdColumn.setText(CREATED_COLUMN_HEADER);
 		createdColumn.setWidth(CREATED_COLUMN_WIDTH);
 		createdColumn.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				notesTable.setSortColumn(noteNameColumn);
 				((NoteViewerSorter) notesTableViewer.getSorter()).doSort(3);
 				notesTableViewer.refresh();
 			}
 		});
 
 		fireEvent(new ViewEvent(ViewEvent.EXPORT_NOTES_DIALOG_OPENED));
 
 		return parent;
 	}
 
 	private void addCheckListener()
 	{
 		notesTable.addListener(SWT.Selection, new Listener()
 		{
 			public void handleEvent(Event event)
 			{
 				if (event.detail == SWT.CHECK)
 				{
 					TableItem tableItem = (TableItem) event.item;
 					Note note = (Note) tableItem.getData();
 					if (tableItem.getChecked())
 					{
 						selectedNotes.add(note);
 					}
 					else
 					{
 						selectedNotes.remove(note);
 					}
 				}
 			}
 
 		});
 	}
 
 	@Override
 	protected void okPressed()
 	{
 		fireEvent(new ViewEvent(ViewEvent.EXPORT_NOTES_EXPORT));
 	}
 
 	@Override
 	public boolean close()
 	{
 		fireEvent(new ViewEvent(ViewEvent.EXPORT_NOTES_DIALOG_CLOSED));
 		return super.close();
 	}
 
 	public void openExportDialog()
 	{
 		Display.getDefault().asyncExec(new Runnable()
 		{
 			public void run()
 			{
 				open();
 			}
 		});
 	}
 
 	public void populateAllNotes(List<Note> notes)
 	{
 		Note[] input = new Note[notes.size()];
 		notes.toArray(input);
 		notesTableViewer.setInput(input);
 		notesTableViewer.refresh();
 	}
 
 	public void addListener(IExportNotesListener listener)
 	{
 		eventListeners.add(IExportNotesListener.class, listener);
 	}
 
 	public void removeListener(IExportNotesListener listener)
 	{
 		eventListeners.remove(IExportNotesListener.class, listener);
 	}
 
 	private void fireEvent(ViewEvent event)
 	{
 		IExportNotesListener[] listeners = eventListeners.getListeners(IExportNotesListener.class);
 
 		for (IExportNotesListener listener : listeners)
 		{
 			listener.handleEvent(event);
 		}
 	}
 
 	private class ExportNoteLabelProvider extends LabelProvider implements ITableLabelProvider
 	{
 		public Image getColumnImage(Object element, int columnIndex)
 		{
 			return null;
 		}
 
 		public String getColumnText(Object element, int columnIndex)
 		{
 			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
 
 			String columnText = "";
 
 			Note note = (Note) element;
 
 			if (columnIndex == 0)
 			{
 				columnText = note.getName();
 			}
 			else if (columnIndex == 1)
 			{
 				columnText = formatter.format(note.getLastModified());
 			}
 			else if (columnIndex == 2)
 			{
 				columnText = formatter.format(note.getCreatedTimestamp());
 			}
 
 			if (formatter.format(new Date()).equals(columnText))
 			{
 				columnText = "Today";
 			}
 
 			return columnText;
 		}
 	}
 
 	public List<Note> getSelectedNotes()
 	{
 		return selectedNotes;
 	}
 
 	public String openFileDialog()
 	{
 		FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
 		dialog.setFilterExtensions(new String[] { "*.zip" });
 
 		return dialog.open();
 	}
 
 	public void closeExportDialog()
 	{
 		fireEvent(new ViewEvent(ViewEvent.EXPORT_NOTES_DIALOG_CLOSED));
 		close();
 	}
 
 	public void startExportJob(Job job)
 	{
 		PlatformUI.getWorkbench().getProgressService().showInDialog(
 				Display.getCurrent().getActiveShell(), job);
 
 		job.setUser(true);
 		job.setPriority(Job.INTERACTIVE);
 		job.schedule();
 	}
 }

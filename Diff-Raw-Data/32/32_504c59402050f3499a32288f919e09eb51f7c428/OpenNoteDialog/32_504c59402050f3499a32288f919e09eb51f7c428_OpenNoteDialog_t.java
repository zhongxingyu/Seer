 package net.todd.biblestudy.rcp.views;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.event.EventListenerList;
 
 import net.todd.biblestudy.common.ViewHelper;
 import net.todd.biblestudy.db.Note;
 import net.todd.biblestudy.rcp.presenters.IOpenNoteEventListener;
 import net.todd.biblestudy.rcp.presenters.ViewEvent;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.TrayDialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 public class OpenNoteDialog extends TrayDialog implements IOpenNoteDialog
 {
 	public static final String NOTE_NAME_COLUMN_HEADER = "Note";
 	private static final int NOTE_NAME_COLUMN_WIDTH = 200;
 	public static final String LAST_MODIFIED_COLUMN_HEADER = "Last Modified";
 	private static final int LAST_MODIFIED_COLUMN_WIDTH = 100;
 	public static final String CREATED_COLUMN_HEADER = "Created On";
 	private static final int CREATED_COLUMN_WIDTH = 100;
 
 	private Text filterText;
 
 	public OpenNoteDialog(Shell shell)
 	{
 		super(shell);
 
 		setShellStyle(getShellStyle() | SWT.RESIZE);
 	}
 
 	@Override
 	protected void configureShell(Shell newShell)
 	{
 		super.configureShell(newShell);
 
 		newShell.setText("Open Note");
 	}
 
 	EventListenerList eventListeners = new EventListenerList();
 	private TableViewer notesTableViewer;
 
 	private TableColumn noteNameColumn;
 	private TableColumn lastModifiedColumn;
 	private TableColumn createdColumn;
 	private Table notesTable;
 	private Button renameButton;
 	private Button deleteButton;
 	private Note selectedNote;
 	private TableEditor notesTableEditor;
 	private String renamedNoteName;
 
 	public void addOpenNoteEventListener(IOpenNoteEventListener listener)
 	{
 		eventListeners.add(IOpenNoteEventListener.class, listener);
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
 
 		filterText = new Text(composite, SWT.BORDER);
 		filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		filterText.addKeyListener(new KeyAdapter()
 		{
 			@Override
 			public void keyReleased(KeyEvent e)
 			{
 				notesTableViewer.resetFilters();
 
 				String filterString = filterText.getText();
 
 				if (StringUtils.isEmpty(filterString) == false)
 				{
 					notesTableViewer.addFilter(new NoteFilter(filterString));
 					notesTableViewer.getTable().select(0);
 				}
 
 				handleSelection();
 			}
 		});
 
 		filterText.setFocus();
 
 		notesTableViewer = new TableViewer(composite, SWT.V_SCROLL | SWT.BORDER
				| SWT.SHADOW_ETCHED_IN | SWT.FULL_SELECTION);
 		notesTableViewer.setContentProvider(new ArrayContentProvider());
 		notesTableViewer.setLabelProvider(new NoteLabelProvider());
 
 		notesTableViewer.setSorter(new NoteViewerSorter());
 
 		notesTable = notesTableViewer.getTable();
 		notesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		notesTable.setHeaderVisible(true);
 		notesTable.setLinesVisible(true);
 
 		notesTableEditor = new TableEditor(notesTable);
 		notesTableEditor.horizontalAlignment = SWT.LEFT;
 		notesTableEditor.grabHorizontal = true;
 
 		notesTableViewer.addDoubleClickListener(new IDoubleClickListener()
 		{
 			public void doubleClick(DoubleClickEvent event)
 			{
 				handleSelection();
 
 				fireEvent(new ViewEvent(ViewEvent.OPEN_NOTE_OK_PRESSED));
 			}
 		});
 		notesTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
 		{
 			public void selectionChanged(SelectionChangedEvent event)
 			{
 				handleSelection();
 			}
 		});
 
 		noteNameColumn = new TableColumn(notesTable, SWT.LEFT);
 		noteNameColumn.setText(NOTE_NAME_COLUMN_HEADER);
 		noteNameColumn.setWidth(NOTE_NAME_COLUMN_WIDTH);
		noteNameColumn.setResizable(true);
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
		lastModifiedColumn.setResizable(true);
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
		createdColumn.setResizable(true);
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
 
 		GridLayout extraButtonsCompositeLayout = new GridLayout(2, false);
 
 		Composite extraButtonsComposite = new Composite(composite, SWT.NONE);
 		extraButtonsComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
 		extraButtonsComposite.setLayout(extraButtonsCompositeLayout);
 
 		renameButton = new Button(extraButtonsComposite, SWT.PUSH);
 		renameButton.setText("Rename");
 		renameButton.setEnabled(false);
 		renameButton.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				fireEvent(new ViewEvent(ViewEvent.OPEN_NOTE_RENAME_BUTTON_PRESSED));
 			}
 		});
 
 		deleteButton = new Button(extraButtonsComposite, SWT.PUSH);
 		deleteButton.setText("Delete");
 		deleteButton.setEnabled(false);
 		deleteButton.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				fireEvent(new ViewEvent(ViewEvent.OPEN_NOTE_DELETE));
 			}
 		});
 
 		return parent;
 	}
 
 	private void handleSelection()
 	{
 		StructuredSelection structuredSelection = (StructuredSelection) notesTableViewer
 				.getSelection();
 		selectedNote = (Note) structuredSelection.getFirstElement();
 
 		updateButtonStatus();
 	}
 
 	private void updateButtonStatus()
 	{
 		if (selectedNote == null)
 		{
 			getButton(OK).setEnabled(false);
 			renameButton.setEnabled(false);
 			deleteButton.setEnabled(false);
 		}
 		else
 		{
 			getButton(OK).setEnabled(true);
 			renameButton.setEnabled(true);
 			deleteButton.setEnabled(true);
 		}
 	}
 
 	@Override
 	protected void createButtonsForButtonBar(Composite parent)
 	{
 		super.createButtonsForButtonBar(parent);
 
 		updateButtonStatus();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
 	 */
 	@Override
 	protected void okPressed()
 	{
 		fireEvent(new ViewEvent(ViewEvent.OPEN_NOTE_OK_PRESSED));
 	}
 
 	@Override
 	protected void cancelPressed()
 	{
 		fireEvent(new ViewEvent(ViewEvent.OPEN_NOTE_CANCEL_PRESSED));
 	}
 
 	private void fireEvent(ViewEvent e)
 	{
 		IOpenNoteEventListener[] listeners = eventListeners
 				.getListeners(IOpenNoteEventListener.class);
 
 		for (IOpenNoteEventListener listener : listeners)
 		{
 			listener.handleEvent(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.todd.biblestudy.rcp.views.INewNoteDialog#closeDialog()
 	 */
 	public void closeDialog()
 	{
 		close();
 
 		removeAllListeners();
 	}
 
 	public void removeAllListeners()
 	{
 		IOpenNoteEventListener[] listeners = eventListeners
 				.getListeners(IOpenNoteEventListener.class);
 
 		List<IOpenNoteEventListener> clonedListeners = new ArrayList<IOpenNoteEventListener>();
 
 		for (IOpenNoteEventListener listener : listeners)
 		{
 			clonedListeners.add(listener);
 		}
 
 		for (IOpenNoteEventListener listener : clonedListeners)
 		{
 			eventListeners.remove(IOpenNoteEventListener.class, listener);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.todd.biblestudy.rcp.views.INewNoteDialog#getNewNoteName()
 	 */
 	public Note getSelectedNote()
 	{
 		return selectedNote;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.todd.biblestudy.rcp.views.INewNoteDialog#openDialog()
 	 */
 	public void openDialog()
 	{
 		ViewHelper.runWithoutBusyIndicator(new Runnable()
 		{
 			public void run()
 			{
 				open();
 			}
 		});
 
 		fireEvent(new ViewEvent(ViewEvent.OPEN_NOTE_OPENED));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.todd.biblestudy.rcp.views.INewNoteDialog#populateDropDown(net.todd.biblestudy.db.Note[])
 	 */
 	public void populateDropDown(final Note[] notes)
 	{
 		ViewHelper.runWithBusyIndicator(new Runnable()
 		{
 			public void run()
 			{
 				notesTableViewer.setInput(notes);
 				notesTableViewer.refresh();
 			}
 		});
 	}
 
 	public int openDeleteConfirmationWindow()
 	{
 		MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(),
 				"Are you sure?", null, "Are you sure you want to delete this note?",
 				MessageDialog.QUESTION, new String[] { "No", "Yes" }, 0);
 		return dialog.open();
 	}
 
 	public void makeSelectedNoteNameEditable()
 	{
 		TableItem[] tableItems = notesTable.getSelection();
 
 		if (tableItems != null && tableItems.length > 0)
 		{
 			final TableItem selection = tableItems[0];
			
 			String noteName = selection.getText(0);
 
			final Text text = new Text(notesTable, SWT.NONE);
			
 			Listener textListener = new Listener()
 			{
 				public void handleEvent(Event e)
 				{
 					if (e.type == SWT.FocusOut)
 					{
 						makeNoteNameEditable(selection, text);
 					}
 					else if (e.type == SWT.Traverse)
 					{
 						if (e.detail == SWT.TRAVERSE_RETURN)
 						{
 							changeNoteName(selection, text);
 							removeEditabilityOfNoteName(text);
 						}
 						else if (e.detail == SWT.TRAVERSE_ESCAPE)
 						{
 							removeEditabilityOfNoteName(text);
 							e.doit = false;
 						}
 					}
 				}
 			};
 
 			text.addListener(SWT.FocusOut, textListener);
 			text.addListener(SWT.Traverse, textListener);
 			text.setText(noteName);
 			text.setFocus();
 			text.selectAll();
 
 			notesTableEditor.setEditor(text, selection, 0);
 
 			notesTable.getShell().setDefaultButton(null);
 		}
 	}
 
 	private void removeEditabilityOfNoteName(final Text text)
 	{
 		text.dispose();
 		notesTable.getShell().setDefaultButton(getButton(OK));
 	}
 
 	private void makeNoteNameEditable(final TableItem selection, final Text text)
 	{
 		String newNoteName = text.getText();
 		text.dispose();
 
 		notesTable.getShell().setDefaultButton(getButton(OK));
 
 		selection.setText(0, newNoteName);
 	}
 
 	private void changeNoteName(final TableItem selection, final Text text)
 	{
 		String newNoteName = text.getText();
 		selection.setText(0, newNoteName);
 
 		renamedNoteName = newNoteName;
 
 		fireEvent(new ViewEvent(ViewEvent.OPEN_NOTE_RENAME));
 	}
 
 	public String getRenamedNoteName()
 	{
 		return renamedNoteName;
 	}
 }

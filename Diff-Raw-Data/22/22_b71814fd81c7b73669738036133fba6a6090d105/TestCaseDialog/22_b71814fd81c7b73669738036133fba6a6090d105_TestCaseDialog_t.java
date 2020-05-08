 package sg.edu.nus.iss.yunakti.ui.dialog;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 
 import sg.edu.nus.iss.yunakti.engine.EngineCore;
 import sg.edu.nus.iss.yunakti.model.YClass;
 import sg.edu.nus.iss.yunakti.model.YModel;
 import sg.edu.nus.iss.yunakti.ui.dialog.filter.TestCaseFilter;
 import sg.edu.nus.iss.yunakti.ui.dialog.helper.TestCaseLabelProvider;
 import sg.edu.nus.iss.yunakti.ui.view.YunaktiGridView;
 
 /**
  * Dialog used to display all the testcases for a CUT. Also used to add or
  * delete an existing testcases for a CUT
  * 
  * @author subu
  * 
  */
 public class TestCaseDialog extends TitleAreaDialog {
 
 	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
 	private TableViewer tableViewer;
 	private Text searchText;
 	private TestCaseFilter filter;
 	private YModel model;
 	private TestCaseDialog dialog;
 	private List<YClass> testClassForCUT;
 	private List<YClass> allClasses;
 	private YunaktiGridView gridView;
 
 	public TestCaseDialog(Shell parentShell) {
 		super(parentShell);
 		dialog = this;
 	}
 
 	public TestCaseDialog(Shell parentShell, YModel model,
 			List<YClass> allClasses, YunaktiGridView gridView) {
 		super(parentShell);
 		dialog = this;
 		this.model = model;
 		this.allClasses = allClasses;
 		this.gridView = gridView;
 
 	}
 
 	@Override
 	public void create() {
 		super.create();
 		// Set the title
 		setTitle("Select Test Classes");
 		// Set the message
 		setMessage("Select Test Classes", IMessageProvider.INFORMATION);
 
 		// This is done to refresh the tableviewer everytime the dialog gets
 		// focus.
 		this.getShell().addFocusListener(new FocusListener() {
 
 			public void focusLost(FocusEvent e) {
 
 			}
 
 			@Override
 			public void focusGained(FocusEvent e) {
 				logger.fine("R2 focus gained");
 
 			}
 		});
 
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		GridLayout layout = new GridLayout(2, false);
 		parent.setLayout(layout);
 		Label searchLabel = new Label(parent, SWT.NONE);
 		searchLabel.setText("Search: ");
 		searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
 		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
 				| GridData.HORIZONTAL_ALIGN_FILL));
 
 		// Instantiate TableViewer
 		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
 		Table table = tableViewer.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
 		String[] tableColumns = new String[] { "ClassName", "Type" };
 
 		for (int i = 0; i < 2; i++) {
 			TableColumn tableColumn = new TableColumn(table,
 					columnAlignments[i]);
 			tableColumn.setText(tableColumns[i]);
			if (i == 1) {
				tableColumn.setWidth(150);
			}else{
				tableColumn.setWidth(100);
			}
 
 		}
 
 		tableViewer.setLabelProvider(new TestCaseLabelProvider());
 
 		tableViewer.setContentProvider(new ArrayContentProvider());
 
 		if (model != null) {
 			testClassForCUT = model.getTestCases();
 			setTableData(model);
 		}
 
 		// Layout the viewer
 		GridData gridData = new GridData();
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 2;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		tableViewer.getControl().setLayoutData(gridData);
 
 		searchText.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				filter.setSearchText(searchText.getText());
 				tableViewer.refresh();
 			}
 		});
 		filter = new TestCaseFilter();
 		tableViewer.addFilter(filter);
 
 		return parent;
 
 	}
 
 	public void setTableData(YModel model) {
 		this.model = model;
 		this.testClassForCUT = model.getTestCases();
 		logger.fine(model.getTestCases().toString());
 		if (model != null) {
 
 			tableViewer.setInput(model.getTestCases());
 			this.refresh();
 		}
 	}
 
 	/**
 	 * Create button handlers for Cancel & Delete button
 	 */
 	protected void createButtonsForButtonBar(Composite parent) {
 		GridData gridData = new GridData();
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 3;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = SWT.CENTER;
 
 		parent.setLayoutData(gridData);
 		// Create Add button
 		// Own method as we need to overview the SelectionAdapter
 		createOkButton(parent, OK, "Add", true);
 
 		// Create Cancel button
 		Button cancelButton = createButton(parent, CANCEL, "Cancel", false);
 		// Add a SelectionListener
 		cancelButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				setReturnCode(CANCEL);
 				close();
 			}
 		});
 
 		// Create Delete button
 		Button deleteButton = createButton(parent, SWT.PUSH, "Delete", false);
 		// Add a SelectionListener
 		deleteButton.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent e) {
 
 				logger.fine("Adding Selection Listener for Delete button");
 
 				IStructuredSelection sel = (IStructuredSelection) tableViewer
 						.getSelection();
 				YClass selectedYClass = (YClass) sel.getFirstElement();
 
 				if (selectedYClass != null) {
 					for (YClass yClass : model.getTestCases()) {
 						if (selectedYClass.getFullyQualifiedName().equals(
 								yClass.getFullyQualifiedName())) {
 							try {
 								// Before writing annotation, set the delete
 								// flag to true and call the removeTestCase in
 								// Model
 								yClass.setDeleteFlag(true);
 								model.removeTestCase(yClass);
 								EngineCore engineCore = new EngineCore();
 								engineCore.writeAnnotation(model);
 
 								// After writing annotation, remove the
 								// testclass actually from the model. Set the
 								// delete flag to false.
 								yClass.setDeleteFlag(false);
 								model.removeTestCase(yClass);
 								// Refreshing will happen only after clicking
 								TestCaseDialog.this.setTableData(model);
 								TestCaseDialog.this.tableViewer.refresh();
								TestCaseDialog.this.gridView
										.updateGridView(model);
 								break;
 
 							} catch (Exception ex) {
 								ex.printStackTrace();
 							}
 						}
 					}
 
 				}
 			}
 		});
 
 		this.setTableData(model);
 	}
 
 	/**
 	 * Button Handler for Ok Button. Adding Click Event Handler for OK Button.
 	 * Calls the pop-up window with all the classes in the project Explorer.
 	 * 
 	 * @param Compsosite
 	 *            parent
 	 * @param int id
 	 * @param String
 	 *            label
 	 * @param boolean defaultButton
 	 * @return
 	 */
 	protected Button createOkButton(Composite parent, int id, String label,
 			boolean defaultButton) {
 		// increment the number of columns in the button bar
 		((GridLayout) parent.getLayout()).numColumns++;
 		Button button = new Button(parent, SWT.PUSH);
 		button.setText(label);
 		button.setFont(JFaceResources.getDialogFont());
 		button.setData(new Integer(id));
 
 		button.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (allClasses != null) {
 					EngineCore engineCore = new EngineCore();
 					List<YClass> allClasses = engineCore
 							.getAllClassesInWorkspace();
 
 					logger.fine("Model ::" + model.toString());
 					FilteredTCSelectionDialog dialog = new FilteredTCSelectionDialog(
 							getShell(), allClasses, model, gridView,
 							TestCaseDialog.this);
 					dialog.setInitialPattern("?");
 					dialog.open();
 				} else {
 					MessageDialog.openError(getShell(), "Error",
 							"There are no classes in the workspace");
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 
 			}
 		});
 
 		if (defaultButton) {
 			Shell shell = parent.getShell();
 			if (shell != null) {
 				shell.setDefaultButton(button);
 			}
 		}
 		setButtonLayoutData(button);
 		return button;
 	}
 
 	public void refresh() {
 		tableViewer.refresh();
 	}
 
 }

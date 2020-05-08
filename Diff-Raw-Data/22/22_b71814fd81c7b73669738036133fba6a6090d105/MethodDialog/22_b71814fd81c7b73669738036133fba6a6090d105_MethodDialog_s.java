 package sg.edu.nus.iss.yunakti.ui.dialog;
 
 import java.util.ArrayList;
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
 import sg.edu.nus.iss.yunakti.model.YMethod;
 import sg.edu.nus.iss.yunakti.model.YModel;
 import sg.edu.nus.iss.yunakti.ui.dialog.filter.TestCaseFilter;
 import sg.edu.nus.iss.yunakti.ui.dialog.helper.MethodLabelProvider;
 import sg.edu.nus.iss.yunakti.ui.view.YunaktiGridView;
 
 /**
  * Dialog used to display all the testcases for a CUT. Also used to add or
  * delete an existing testcases for a CUT
  * 
  * @author subu
  * 
  */
 public class MethodDialog extends TitleAreaDialog {
 
 	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
 	private TableViewer tableViewer;
 	private Text searchText;
 	private TestCaseFilter filter;
 	private YModel model;
 	private MethodDialog dialog;
 	private List<YMethod> testMethodsForCUTMethod;
 	private List<YMethod> allTestMethodsInTC;
 	private YunaktiGridView gridView;
 
 	public MethodDialog(Shell parentShell) {
 		super(parentShell);
 		dialog = this;
 	}
 
 	public List<YMethod> getCurrentTestMethods() {
 		return this.testMethodsForCUTMethod;
 	}
 
 	private void populateTestMethodsInTC() {
 		this.allTestMethodsInTC = new ArrayList<YMethod>();
 
 		YMethod method1 = new YMethod();
 		method1.setMethodName("method1");
 		this.allTestMethodsInTC.add(method1);
 
 		YMethod method2 = new YMethod();
 		method2.setMethodName("method2");
 		this.allTestMethodsInTC.add(method2);
 
 		YMethod method3 = new YMethod();
 		method3.setMethodName("method3");
 		this.allTestMethodsInTC.add(method3);
 
 		YMethod method4 = new YMethod();
 		method4.setMethodName("method4");
 
 		YMethod method5 = new YMethod();
 		method5.setMethodName("xmethod2");
 		this.allTestMethodsInTC.add(method5);
 
 		YMethod method6 = new YMethod();
 		method6.setMethodName("xmethod3");
 		this.allTestMethodsInTC.add(method6);
 
 		YMethod method7 = new YMethod();
 		method7.setMethodName("xmethod4");
 		this.allTestMethodsInTC.add(method7);
 
 	}
 
 	public MethodDialog(Shell parentShell, List<YMethod> testMethodsForCUTMethod) {
 		super(parentShell);
 		dialog = this;
 		this.testMethodsForCUTMethod = testMethodsForCUTMethod;
 		System.out.println(testMethodsForCUTMethod);
 		populateTestMethodsInTC();
 		// this.gridView = gridView;o
 	}
 
 	@Override
 	public void create() {
 		super.create();
 		// Set the title
 		setTitle("Select Test Methods");
 		// Set the message
 		setMessage("Select Test Methods", IMessageProvider.INFORMATION);
 
 		// This is done to refresh the tableviewer everytime the dialog gets
 		// focus.
 		this.getShell().addFocusListener(new FocusListener() {
 
 			public void focusLost(FocusEvent e) {
 			}
 
 			@Override
 			public void focusGained(FocusEvent e) {
 				logger.fine("R4 focus gained");
 
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
 
 		int[] columnAlignments = new int[] { SWT.RIGHT_TO_LEFT };
 		String[] tableColumns = new String[] { "MethodName" };
 
 		for (int i = 0; i < 1; i++) {
 			TableColumn tableColumn = new TableColumn(table,
 					columnAlignments[i]);
 			new TableColumn(table, columnAlignments[i]);
 			tableColumn.setText(tableColumns[i]);
 			tableColumn.setWidth(100);
 
 		}
 
 		tableViewer.setLabelProvider(new MethodLabelProvider());
 
 		tableViewer.setContentProvider(new ArrayContentProvider());
 
 		if (testMethodsForCUTMethod != null) {
 			setTableData(testMethodsForCUTMethod);
 		}
 
 		// Layout the viewer
 		GridData gridData = new GridData();
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 2;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
		tableViewer.getControl().setLayoutData(gridData);

		
 
 		return parent;
 
 	}
 
 	public void setTableData(List<YMethod> testMethods) {
 		if (testMethods != null && testMethods.size() != 0) {
			tableViewer.setInput(testMethods);
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
 				IStructuredSelection sel = (IStructuredSelection) tableViewer
 						.getSelection();
 				YMethod selectedYMethod = (YMethod) sel.getFirstElement();
 
 				if (selectedYMethod != null) {
 					for(YMethod method : testMethodsForCUTMethod){
 						if(method.getMethodName().equals(selectedYMethod.getMethodName())){
 							testMethodsForCUTMethod.remove(selectedYMethod);
 							// Refreshing will happen only after clicking
 							MethodDialog.this.setTableData(testMethodsForCUTMethod);
 							MethodDialog.this.tableViewer.refresh();
 							break;
 						}
 					}
 				}
 			}
 		});
 
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
 				System.out.println("allMethods " + allTestMethodsInTC);
 				if (allTestMethodsInTC != null) {
 
 					FilteredMethodSelectionDialog dialog = new FilteredMethodSelectionDialog(
 							getShell(), allTestMethodsInTC, MethodDialog.this);
 					dialog.setInitialPattern("?");
 					dialog.open();
 				} else {
 					MessageDialog.openError(getShell(), "Error",
 							"There are no methods in the selected Test Class");
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

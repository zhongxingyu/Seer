 /* test assignments
  * html-IDE.openeditor
  */
 
 package navigatorView.views;
 
 import navigatorView.model.Assignment;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.eclipse.swt.SWT;
 
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 
 public class AssignmentChooser extends TitleAreaDialog {
 
 	private ArrayList<Assignment> assignments =  new ArrayList<Assignment>();
 	private Assignment showAssignment;
 	Composite assignmentArea;
 
 	public AssignmentChooser(Shell parentShell) {
 		super(parentShell);
 		// Get all the assignments currently loaded in student's Eclipse
 		findAssignmentsInWorkspace();
 	}
 
 	private void findAssignmentsInWorkspace() {
 		assignments.clear();
 		try {
 			ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceVisitor() {			
 				@Override
 				public boolean visit(IResource resource) throws CoreException {
 					if (!(resource.getType() == IResource.FILE)) return true;
 					String extension = resource.getFileExtension();
 					if (extension != null) {
 						if (extension.equalsIgnoreCase("isa")) {
 							parseISA((IFile)resource);
 						}
 					}
 					return true;
 				}
 			});
 		} catch (CoreException e1) {
 			System.err.println("Core Exception!!!");
 			e1.printStackTrace();
 		}
 		// sort assignments wrt sortOrder
 		Collections.sort(assignments);
 	}
 
 	
 	
 	@Override
 	public void create() {
 		super.create();
 		setTitle("Choose assignment (from your workspace):");
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 4;
 		layout.marginBottom = 20;
 		layout.marginRight = 20;
 		layout.marginTop = 20;
 		layout.marginLeft = 20;
 		parent.setLayout(layout);
 		
 		ScrolledComposite assignmentScrolledArea = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER_DASH );
 		assignmentArea = new Composite(assignmentScrolledArea, SWT.NONE);
 		assignmentScrolledArea.setContent(assignmentArea);
 		GridLayout assignmentLayout = new GridLayout();
 		layout.numColumns = 1;
 		assignmentArea.setLayout(assignmentLayout);
 		setAssignmentArea();
 		
 		return parent;
 	}
 
 	
 	private void setAssignmentArea() {
 		for (int i = 0; i < assignments.size(); i++) {
 
 			Button radio = new Button(assignmentArea, SWT.RADIO);
 			radio.setText(assignments.get(i).getName());
 			radio.setToolTipText(assignments.get(i).getIntro());
 			radio.setEnabled(true);
 
 			final int z = i;
 			radio.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					// show the Assignment
 					showAssignment = assignments.get(z);
 				}
 			});
 		}
 		assignmentArea.setSize(assignmentArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 	}
 	
 	private void refreshAssignmentArea() {
 		// note, the arraylist<Assignment> doesn't get updated here
 		Control[] assControls = assignmentArea.getChildren();
 		for (Control assControl : assControls) {
 			assControl.dispose();
 		}
 		setAssignmentArea();
 		assignmentArea.layout();
 	}
 	
 	
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		
 		// Create Okay button
 		Button okButton = createButton(parent, OK, "Okay", false);
 		okButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				setReturnCode(OK);
 				close();
 			}
 		});
 		
 		// Create Cancel button
 		Button cancelButton = createButton(parent, CANCEL, "Cancel", true);
 		cancelButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				setReturnCode(CANCEL);
 				close();
 			}
 		});
 		
 		Button importButton = new Button(parent, SWT.PUSH);
 		importButton.setText("Import...");
 		importButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				Shell shell = new Shell();
 				ImportAssignment dialog = new ImportAssignment(shell);
 				dialog.create();
 				if (dialog.open() == org.eclipse.jface.window.Window.OK) {
 					findAssignmentsInWorkspace();
 					refreshAssignmentArea();
 				}
 			}
 		});
 		
 		Button refreshButton = new Button(parent, SWT.PUSH);
 		refreshButton.setText("Refresh");
 		refreshButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				findAssignmentsInWorkspace();
 				refreshAssignmentArea();
 			}
 		});
 	}
 
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
 
 	public void parseISA(IFile file) {
 		Assignment s = Assignment.parseISA(file);
 		if (s == null) System.err.println("Failed to parse file: " + file.getName());
 		s.getIntro();
 		assignments.add(s);
 	}
 
 	public Assignment getSegment() {
 		return showAssignment;
 	}
 } 
 

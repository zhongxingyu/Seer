 package wyclipse.ui.pages;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.dialogs.ContainerSelectionDialog;
 
 import wybs.util.Trie;
 import wyclipse.core.builder.WhileyPath;
 import wyclipse.ui.dialogs.NewWhileyPathBuildRuleDialog;
 import wyclipse.ui.util.WhileyPathViewer;
 import wyclipse.ui.util.WyclipseUI;
 
 /**
  * Provides an abstract class which constructs the standard page for configuring
  * the whileypath. This is used in at least two places:
  * <code>NewWhileyProjectWizard</code> and <code>WhileyPathPropertyPage</code>.
  * 
  * @author David J. Pearce
  * 
  */
 public class WhileyPathConfigurationControl {
 	private Shell shell;
 	private IContainer container;
 	private WhileyPath whileypath;
 	
 	// WhileyPath view + controls
 	private WhileyPathViewer whileyPathViewer;
 	
 	// Default Output Folder Controls
 	private Label defaultOutputFolderLabel;
 	private Text defaultOutputFolderText; 
 	private Button defaultOutputFolderBrowseButton;
 	
 	public WhileyPathConfigurationControl(Shell shell) {
 		this.shell = shell;
 		this.whileypath = new WhileyPath();
 	}
 	
 	public WhileyPathConfigurationControl(Shell shell, IContainer container,
 			WhileyPath whileypath) {
 		this.shell = shell;
 		this.whileypath = whileypath;
 		this.container = container;
 	}
 	
 	public WhileyPath getWhileyPath() {
 		return whileypath;
 	}
 
 	public void setWhileyPath(WhileyPath whileypath) {
 		this.whileypath = whileypath;
 		this.whileyPathViewer.setInput(whileypath);
 		IPath defaultOutputFolder = whileypath.getDefaultOutputFolder();
 		if(defaultOutputFolder != null) {
 			defaultOutputFolderText.setText(defaultOutputFolder.toString());
 			defaultOutputFolderLabel.setEnabled(true);
 			defaultOutputFolderText.setEnabled(true);
 			defaultOutputFolderBrowseButton.setEnabled(true);
 		} else {
 			defaultOutputFolderLabel.setEnabled(false);
 			defaultOutputFolderText.setEnabled(false);
 			defaultOutputFolderBrowseButton.setEnabled(false);
 			defaultOutputFolderText.setText("");
 		}
 	}
 	
 	public void setContainer(IContainer container) {
 		this.container = container;
 	}
 	
 	public Composite create(Composite parent) {				
 		Composite container = new Composite(parent, SWT.NONE);
 		
 		// =====================================================================
 		// Configure Grid
 		// =====================================================================
 		
 		GridLayout layout = new GridLayout();		
 		layout.numColumns = 3;
 		layout.verticalSpacing = 9;	
 		layout.marginWidth = 20;
 		container.setLayout(layout);
 		
 		// =====================================================================
 		// Middle Section
 		// =====================================================================		
 				
 		// Create viewer which is 2 columns wide and 3 rows deep.
 		whileyPathViewer = createWhileyPathViewer(container, whileypath, 2, 4);						
 		Button addBuildButton = WyclipseUI.createButton(container, "Add Rule...", 120);
 		Button addExternalLibraryButton = WyclipseUI.createButton(container, "Add Library...", 120);
 		Button editButton = WyclipseUI.createButton(container, "Edit", 120);
 		Button removeButton = WyclipseUI.createButton(container, "Remove", 120);		
 		
 		addBuildButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleAddRule();
 			}
 		});
 		
 		addExternalLibraryButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleAddExternalLibrary();
 			}
 		});
 		
 		editButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleEditRule();
 			}
 		});
 		
 		removeButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleRemoveRule();
 			}
 		});
 		
 		// =====================================================================
 		// Bottom Section
 		// =====================================================================
 		defaultOutputFolderLabel = WyclipseUI.createLabel(container, "Default Output Folder:", 3);		
 		defaultOutputFolderText = WyclipseUI.createText(container, "", 2);
 		defaultOutputFolderBrowseButton = WyclipseUI.createButton(container, "Browse...", 120);
 
 		defaultOutputFolderBrowseButton
 				.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						handleBrowseDefaultOutputFolder();
 					}
 				});	
 		
 		IPath defaultOutputFolder = whileypath.getDefaultOutputFolder();
 		if(defaultOutputFolder == null) {
 			// No default output folder
 			defaultOutputFolderLabel.setEnabled(false);
 			defaultOutputFolderText.setEnabled(false);
 			defaultOutputFolderBrowseButton.setEnabled(false);
 		} else {
 			defaultOutputFolderText.setText(defaultOutputFolder.toString());
 		}
 		
		defaultOutputFolderText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleModifyDefaultOutputFolder();
			}
		});	
		
 		container.pack();
 		
 		return container;
 	}			
 	
 	// ======================================================================
 	// Call Backs
 	// ======================================================================
 
 	/**
 	 * This function is called when the add rule button is pressed.
 	 */
 	protected void handleAddRule() {
 		WhileyPath.BuildRule buildRule = new WhileyPath.BuildRule(new Path(""),
 				"**/*.whiley", null);
 		NewWhileyPathBuildRuleDialog dialog = new NewWhileyPathBuildRuleDialog(
 				shell, buildRule);
 		
 		if (dialog.open() == Window.OK) {
 			whileypath.getEntries().add(buildRule);
 			whileyPathViewer.refresh();
 		}
 	}
 	
 	/**
 	 * This function is called when the add library button is pressed.
 	 */
 	protected void handleAddExternalLibrary() {
 		FileDialog dialog = new FileDialog(shell);
 		String result = dialog.open();
 		if(result != null) {
 			IPath location = new Path(result);
 			whileypath.getEntries().add(new WhileyPath.ExternalLibrary(location, "**/*.wyil"));
 			whileyPathViewer.refresh();
 		}
 	}
 	
 	/**
 	 * This function is called when the edit button is pressed.
 	 */
 	protected void handleEditRule() {
 		WhileyPath.BuildRule buildRule = null;
 		
 		// First, extract the select item (if any)
 		TreeItem[] items = whileyPathViewer.getTree().getSelection();
 		for (TreeItem item : items) {
 			Object data = item.getData();
 			if (data instanceof WhileyPathViewer.PathNode) {			
 				WhileyPathViewer.PathNode pn = (WhileyPathViewer.PathNode) data;
 				if(pn.data instanceof WhileyPath.BuildRule) {
 					buildRule = (WhileyPath.BuildRule) pn.data;
 					break;					
 				}
 
 			}
 		}
 		
 		// Second, open the build rule dialog
 		if (buildRule != null) {
 			NewWhileyPathBuildRuleDialog dialog = new NewWhileyPathBuildRuleDialog(
 					shell, buildRule);
 			if (dialog.open() == Window.OK) {
 				whileyPathViewer.refresh();
 			}
 		}
 	}
 	
 	/**
 	 * This function is called when the remove button is pressed.
 	 */
 	protected void handleRemoveRule() {
 		TreeItem[] items = whileyPathViewer.getTree().getSelection();
 		for (TreeItem item : items) {
 			Object data = item.getData();
 			if (data instanceof WhileyPathViewer.PathNode) {
 				WhileyPathViewer.PathNode pn = (WhileyPathViewer.PathNode) data;
 				// Here, data refers to the WhileyPath.Entry object associated
 				// with this path node.
 				whileypath.getEntries().remove(pn.data);
 			}
 		}
 		whileyPathViewer.refresh();
 	}
 	
 	/**
 	 * This function is called when the browse button for the default output
 	 * folder is called.
 	 */
 	protected void handleBrowseDefaultOutputFolder() {
 		
 		// At this point, we need to create a special selection (tree?) dialog
 		// based on what we can see in the current project's location. One of
 		// the key problems here is that the current location may not actually
 		// exist! Therefore, we want to show onyl what does exist, and give the
 		// option to create something new (again observing that it's not
 		// actually created yet).
 		
 		// FIXME: as a very temporary solution, I'm using a
 		// ContainerSelectionDialog. This is roughly speaking the kind of dialog
 		// we'll want, although it will require the ability to add new folders.
 		
 		ContainerSelectionDialog dialog = new ContainerSelectionDialog(shell,
 				container, true, "Choose default output folder");
 		if (dialog.open() == ContainerSelectionDialog.OK) {
 			Object[] items = dialog.getResult();
 			if (items.length == 1) {
				String defaultOutputFolder = items[0].toString();
				defaultOutputFolderText.setText(defaultOutputFolder);
				whileypath.setDefaultOutputFolder(new Path(defaultOutputFolder));				
 			}
 		}
 	}
 	
	/**
	 * This function is called when the text for the default output
	 * folder is modified.
	 */
	protected void handleModifyDefaultOutputFolder() {
		whileypath.setDefaultOutputFolder(new Path(defaultOutputFolderText
				.getText()));
	}
	
 	// ======================================================================
 	// Helpers
 	// ======================================================================
 	
 	protected WhileyPathViewer createWhileyPathViewer(Composite container,
 			Object input, int horizontalSpan, int verticalSpan) {
 		WhileyPathViewer viewer = new WhileyPathViewer(container, SWT.VIRTUAL
 				| SWT.BORDER);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);		
 		gd.horizontalSpan = horizontalSpan;
 		gd.verticalSpan = verticalSpan;
 		viewer.getTree().setLayoutData(gd);
 		viewer.setInput(input);
 		return viewer;
 	}
 }

 package fiuba.mda.ui.main;
 
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import fiuba.mda.model.Application;
 import fiuba.mda.ui.main.tree.ProjectTreeBuilder;
 import fiuba.mda.ui.main.workspace.ControlBuilder;
 import fiuba.mda.ui.main.workspace.TabsFolder;
 import fiuba.mda.ui.utilities.ImageLoader;
 
 /**
  * Main application window
  */
 @Singleton
 public class MainWindow extends ApplicationWindow {
 	private final ProjectTreeBuilder projectTreeBuilder;
 
 	private final ToolBarActionProvider toolBarActions;
 
 	private final ImageLoader imageLoader;
 
 	private TabsFolder toolTabs;
 
 	private TabsFolder editorTabs;
 
 	/**
 	 * Creates a new {@link MainWindow} instance
 	 * 
 	 * @param shell
 	 *            the shell which will contain this window
 	 * @param documentModel
 	 *            the model on which this application window will operate
 	 * @param projectTreeBuilder
 	 *            the builder which will provide the project tree
 	 * @param toolBarActions
 	 *            the provider of all toolbar actions
 	 * @param imageLoader
 	 *            the provider of images
 	 */
 	@Inject
 	public MainWindow(final Shell shell, final Application documentModel,
 			final ProjectTreeBuilder projectTreeBuilder,
 			final ToolBarActionProvider toolBarActions,
 			final ImageLoader imageLoader) {
 		super(shell);
 		this.projectTreeBuilder = projectTreeBuilder;
 		this.toolBarActions = toolBarActions;
 		this.imageLoader = imageLoader;
 
 		this.addToolBar(SWT.FLAT | SWT.WRAP);
 	}
 
 	@Override
 	protected void configureShell(final Shell shell) {
 		super.configureShell(shell);
 		shell.setText("MDA IDE");
 		shell.setSize(800, 600);
 	}
 
 	@Override
 	protected ToolBarManager createToolBarManager(int style) {
 		ToolBarManager result = new ToolBarManager(style);
 		toolBarActions.provideActions(result);
 		return result;
 	}
 
 	@Override
 	protected Control createContents(final Composite parent) {
 		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
 		sash.setSashWidth(5);
 
 		toolTabs = new TabsFolder(sash, SWT.None);
 		toolTabs.ensureTab("Explorador de proyecto",
				imageLoader.of("folder_explore"), projectTreeBuilder,SWT.NONE);
 
 		editorTabs = new TabsFolder(sash, SWT.None);
 
 		sash.setWeights(new int[] { 3, 7 });
 
 		return sash;
 	}
 
 	/**
 	 * Adds a new editor window inside the editor tabs
 	 * 
 	 * @param name
 	 *            the name of the editor window
 	 * @param image
 	 *            the image to show for the editor
 	 * @param builder
 	 *            the builder in charge of creating the control used inside the
 	 *            window
 	 */
 	public void ensureEditor(final String name, final Image image,
 			final ControlBuilder builder) {
 		editorTabs.ensureTab(name, image, builder);
 	}
 
     public void renameEditor(final String oldName,final String newName) {
         editorTabs.renameTab(oldName,newName);
     }
 
 }

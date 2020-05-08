 package org.amanzi.awe.script.jirb;
 
 import java.io.IOException;
 import java.net.URL;
 
 import org.amanzi.scripting.jirb.IRBConfigData;
 import org.amanzi.scripting.jirb.SWTIRBConsole;
 import org.amanzi.scripting.jirb.SwingIRBConsole;
 import org.amanzi.splash.console.SpreadsheetManager;
 import org.amanzi.splash.ui.SplashPlugin;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 
 /**
  * This class makes a view with an embedded JIRB Console provided by the
  * org.amanzi.scripting.jirb plugin. In addition we provide toolbar actions for restarting the IRB
  * if the user types 'exit', as well as for opening the console in a pure Swing JFrame and an SWT
  * Shell. The customization options for the console are exposed from the org.amanzi.scripting.jirb
  * plugin in the form of the IRBConfigData class, and this is used to customize the console to some
  * extent.
  * <p>This class was original based on the template provided by the eclipse view plugin generator.</p>
  * @see org.amanzi.scripting.jirb
  * @see org.amanzi.scripting.jirb.SWTIRBConsole
  * @see org.amanzi.scripting.jirb.IRBConfigData
  */
 public class RubyConsole extends ViewPart {
 	private SWTIRBConsole ex;
 	private Action action0;
 	private Action action1;
 	private Action action2;
 
 	/**
 	 * The constructor.
 	 */
 	public RubyConsole() {
 	}
 
     /**
      * This is a callback that will allow us to create the embedded SWTIRBConsole and initialize it.
      */
 	public void createPartControl(Composite parent) {
         ex = new SWTIRBConsole(parent, new IRBConfigData(){{
             setTitle(" Welcome to the AWEScript Console \n\n");
             addExtraGlobal("view", RubyConsole.this);
             addExtraGlobal("catalog", net.refractions.udig.catalog.CatalogPlugin.getDefault());
             addExtraGlobal("catalogs", net.refractions.udig.catalog.CatalogPlugin.getDefault().getCatalogs());
             addExtraGlobal("projects", net.refractions.udig.project.ui.ApplicationGIS.getProjects());
             addExtraGlobal("active_project", net.refractions.udig.project.ui.ApplicationGIS.getActiveProject());
             try{
             	// TODO: Check if 'buddy class loading' is required for this, since the plugins are not explicitly specified as dependencies
             	addExtraGlobal("json_reader_class", Class.forName("org.amanzi.awe.catalog.json.JSONReader"));
            	addExtraGlobal("neo_reader_class", Class.forName("org.amanzi.awe.catalog.neo.actions.NeoReader"));
             }catch(ClassNotFoundException e){
             	System.err.println("Cannot find possible FeatureSource class: "+e.getMessage());
             	//e.printStackTrace(System.err);
             }
             addExtraGlobal("feature_source_class", org.geotools.data.FeatureSource.class);
             
             //manager of spreadsheets
             addExtraGlobal("spreadsheet_manager", SpreadsheetManager.getInstance());          
             
             String userDir = System.getProperty("user.home");
             setExtraLoadPath(new String[]{userDir+"/.awe/script",userDir+"/.awe/lib"});
             try{
                 // Add the code from the internal plugin awescript.rb to the startup
             	addExtraScript(FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry("gisCommands.rb")));
                 addExtraScript(FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry("awescript.rb")));
                 addExtraScript(FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry("spreadsheet.rb")));                
                 
             }catch(Exception e){
                 System.err.println("Failed to add internal awescript startup: "+e);
                 e.printStackTrace(System.err);
                 setExtraRequire(new String[]{"awescript"});   // try find the script from Ruby instead
             }
 
         }});
 		// Create the help context id for the viewer's control
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(ex, "org.amanzi.awe.script.jirb");
 		makeActions();
 		hookContextMenu();
 		contributeToActionBars();
 	}
 
 	private void hookContextMenu() {
 		MenuManager menuMgr = new MenuManager("#PopupMenu");
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager manager) {
 				RubyConsole.this.fillContextMenu(manager);
 			}
 		});
 		Menu menu = menuMgr.createContextMenu(ex);
 		ex.setMenu(menu);
 	}
 
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown(bars.getMenuManager());
 		fillLocalToolBar(bars.getToolBarManager());
 	}
 
 	private void fillLocalPullDown(IMenuManager manager) {
 		manager.add(action0);
 		manager.add(new Separator());
 		manager.add(action1);
 		manager.add(new Separator());
 		manager.add(action2);
 	}
 
 	private void fillContextMenu(IMenuManager manager) {
 		manager.add(action0);
 		manager.add(action1);
 		manager.add(action2);
 		// Other plug-ins can contribute their actions here
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 	}
 	
 	private void fillLocalToolBar(IToolBarManager manager) {
 		manager.add(action0);
 		manager.add(action1);
 		manager.add(action2);
 	}
 
 	private void makeActions() {
 		action0 = new Action() {
 			public void run() {
 				try {
 				    // restart the embedded console
 					ex.restart();
 				} catch(Throwable t){
 					System.err.println("Failed to re-start IRBConsole: "+t.getMessage());
 					t.printStackTrace(System.err);
 				}
 			}
 		};
 		action0.setText("Restart IRB Session");
 		action0.setToolTipText("Restart IRB Session");
 		action0.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
 			getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
 		
 		final Display display = this.getSite().getShell().getDisplay();
 		action1 = new Action() {
 			public void run() {
 				try {
 				    // Create a new standalone swing console (based on org.jruby.demo.IRBConsole)
 					SwingIRBConsole.start(null);
 				} catch(Throwable t){
 					System.err.println("Failed to start swing-based IRBConsole: "+t.getMessage());
 					t.printStackTrace(System.err);
 				}
 			}
 		};
 		action1.setText("Swing-based IRBConsole");
 		action1.setToolTipText("Start a Swing-based IRBConsole");
 		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
 			getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
 		
 		action2 = new Action() {
 			public void run() {
 				try {
                     // Create a new standalone SWT console console (based on org.jruby.demo.IRBConsole embedded in SWT)
 					SWTIRBConsole.start(display);
 				} catch(Throwable t){
 					System.err.println("Failed to start SWT-based IRBConsole: "+t.getMessage());
 					t.printStackTrace(System.err);
 				}
 			}
 		};
 		action2.setText("SWT/AWT based IRBConsole");
 		action2.setToolTipText("Start an SWT/AWT based IRBConsole");
 		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
 				getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
 	}
 
 	/**
 	 * Passing the focus request to the embedded composite.
 	 */
 	public void setFocus() {
 		ex.setFocus();
 	}
 }

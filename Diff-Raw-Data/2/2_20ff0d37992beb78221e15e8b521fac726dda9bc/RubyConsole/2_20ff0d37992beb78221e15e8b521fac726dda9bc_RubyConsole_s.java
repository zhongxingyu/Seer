 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.awe.script.jirb;
 
 import org.amanzi.scripting.jirb.IRBConfigData;
 import org.amanzi.scripting.jirb.SWTIRBConsole;
 import org.amanzi.scripting.jirb.SwingIRBConsole;
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
     
     /*
      * Postfix for Lib extra load path
      */
 	private static final String AWE_LIB_EXTRA_LOAD_PATH = "/.awe/lib";
 	
 	/*
 	 * Postfix for script extra load path
 	 */
     private static final String AWE_SCRIPT_EXTRA_LOAD_PATH = "/.awe/script";
     
     /*
      * Name of user.home system property
      */
     private static final String USER_HOME_PROPERTY = "user.home";
     
     private SWTIRBConsole ex;
 	private Action action0;
 	private Action action1;
 	private Action action2;
 
 	/*
 	 * Array of Extra Scripts for Ruby Console 
 	 */
 	//Lagutko, 20.08.2009, additional extran script 'neoSetup.rb'
 	public static final String[] EXTRA_SCRIPTS = {"gisGlobals.rb", "gisCommands.rb", "neoSpreadsheet.rb", "neoSetup.rb", "neoCommands.rb", "awescript.rb"};
 	
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
             setTitle(AweScriptConsoleMessages.Welcome);
             addExtraGlobal("view", RubyConsole.this);         
             
             //Lagutko, 21.08,2009, put BundleClassloader of this plugin
             setLoader(this.getClass().getClassLoader());
             
             String userDir = System.getProperty(USER_HOME_PROPERTY);
             setExtraLoadPath(new String[]{userDir+AWE_SCRIPT_EXTRA_LOAD_PATH,userDir+AWE_LIB_EXTRA_LOAD_PATH});
             try{
                 // Add the code from the internal plugin awescript.rb to the startup
                 //Lagutko, 29.07.2009, putting all extra scripts to array
                 for (String extraScript : EXTRA_SCRIPTS) {
                    addExtraScript(FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry(extraScript)));
                 }
                 
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
 		action0.setText(AweScriptConsoleMessages.Restart_IRB);
 		action0.setToolTipText(AweScriptConsoleMessages.Restart_IRB);
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
 		action1.setText(AweScriptConsoleMessages.Swing_based_Console_Text);
 		action1.setToolTipText(AweScriptConsoleMessages.Swing_based_Console_Tooltip);
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
 		action2.setText(AweScriptConsoleMessages.SWT_based_Console_Text);
 		action2.setToolTipText(AweScriptConsoleMessages.SWT_based_Console_Tooltip);
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

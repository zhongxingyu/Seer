 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.List;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.StatusLineManager;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import weito.Backend;
 import weito.RunPapersParameter;
 import debug.Debug;
 import debug.Debug.DebugMode;
 import debug.Printer;
 import debug.TextViewerPrinter;
 import drools.AlgorithmContents;
 import drools.AlgorithmContentsFactory;
 
 
 public class myWindow extends ApplicationWindow {
 	private Action newRun;
 	private StyledText styledText;
 
 	/**
 	 * Create the application window.
 	 */
 	public myWindow() {
 		super(null);
 		createActions();
 		addToolBar(SWT.FLAT | SWT.WRAP);
 		addMenuBar();
 		addStatusLine();
 	}
 
 	/**
 	 * Create contents of the application window.
 	 * @param parent
 	 */
 	@Override
 	protected Control createContents(Composite parent) {
 		Composite container = new Composite(parent, SWT.NONE);
 		container.setLayout(new FillLayout(SWT.HORIZONTAL));
 		{
 			TextViewer textViewer = new TextViewer(container, SWT.V_SCROLL);
 			textViewer.setEditable(false);
 			styledText = textViewer.getTextWidget();
 			styledText.setLeftMargin(10);
 		}
 
 		return container;
 	}
 
 	/**
 	 * Create the actions.
 	 */
 	private void createActions() {
 		// Create the actions
 		{
 			newRun = new Action("Create new Run") {
 				@Override
 				public void run() {
					styledText.setText(""); //Clear Text
 					Printer.setInstance(new TextViewerPrinter(styledText));
 					
 					Debug.setMode(EnumSet.of(DebugMode.FEATURE));
 					
 					List<String> paperLocs = new ArrayList<String>();
 					paperLocs.add("C:/rootfiles/agilestudygeneral/agileconference05/agilestyle-ex2.pdf");
 					//FileAccess fa = new FileAccess("pdf", true);
 					//paperLocs.addAll( fa.getFilesFromDir("C:/rootfiles/agilestudygeneral/agileconference05") );
 					//System.out.println("No of files: "+result.size());
 					
 					List<AlgorithmContents> drlLocs = new ArrayList<AlgorithmContents>();
 					AlgorithmContentsFactory f = new AlgorithmContentsFactory();
 					drlLocs.add( f.forDRLfile("basicformatalgorithm.drl") );
 					drlLocs.add( f.forRFfile( "formatalgorithm.rf" ) );
 					drlLocs.add( f.forDRLfile("conferencestyle.drl") );
 					
 					drlLocs.add( f.forDRLfile("basiccatalgorithm.drl") );
 					drlLocs.add( f.forRFfile( "categoryalgorithm.rf" ) );
 					
 					if( Debug.getMode().contains(DebugMode.DROOLSSTAGEENTER) ) drlLocs.add( f.forDRLfile("debug.drl") );
 					
 					ArrayList<String> keywords = new ArrayList<String>(Arrays.asList( new String[]{"agile","extreme programming","xp","scrum",
 							"dsdm","fdd","software","feature","driven","development","lean","empirical","study","result"} ));
 					
 					try {
 					Backend.runPapers(new RunPapersParameter(paperLocs, drlLocs, keywords));
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					super.run();
 				}
 			};
 		}
 	}
 
 	/**
 	 * Create the menu manager.
 	 * @return the menu manager
 	 */
 	@Override
 	protected MenuManager createMenuManager() {
 		MenuManager menuManager = new MenuManager("menu");
 		{
 			MenuManager menuManager_1 = new MenuManager("File");
 			menuManager.add(menuManager_1);
 			menuManager_1.add(newRun);
 		}
 		return menuManager;
 	}
 
 	/**
 	 * Create the toolbar manager.
 	 * @return the toolbar manager
 	 */
 	@Override
 	protected ToolBarManager createToolBarManager(int style) {
 		ToolBarManager toolBarManager = new ToolBarManager(style);
 		toolBarManager.add(newRun);
 		return toolBarManager;
 	}
 
 	/**
 	 * Create the status line manager.
 	 * @return the status line manager
 	 */
 	@Override
 	protected StatusLineManager createStatusLineManager() {
 		StatusLineManager statusLineManager = new StatusLineManager();
 		return statusLineManager;
 	}
 
 	/**
 	 * Launch the application.
 	 * @param args
 	 */
 	public static void main(String args[]) {
 		try {
 			myWindow window = new myWindow();
 			window.setBlockOnOpen(true);
 			window.open();
 			Display.getCurrent().dispose();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Configure the shell.
 	 * @param newShell
 	 */
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setText("WeitoGUI");
 	}
 
 	/**
 	 * Return the initial size of the window.
 	 */
 	@Override
 	protected Point getInitialSize() {
 		return new Point(450, 300);
 	}
 
 }

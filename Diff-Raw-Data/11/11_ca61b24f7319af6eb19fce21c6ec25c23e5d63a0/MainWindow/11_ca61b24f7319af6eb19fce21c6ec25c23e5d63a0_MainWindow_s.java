 package aeroport.sgbag.gui;
 
 import java.io.File;
 
 import lombok.Getter;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.StatusLineManager;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.wb.swt.SWTResourceManager;
 
 import aeroport.sgbag.controler.Simulation;
 import aeroport.sgbag.views.VueHall;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.jface.viewers.TreeViewer;
 
 /**
  * SGBag GUI root window.
  * 
  * @author Stanislas Signoud <signez@stanisoft.net>
  *
  */
 public class MainWindow extends ApplicationWindow {
 	private Action actionQuitter;
 	private Action actionDemarrer;
 	private Action actionPauser;
 	private Action actionArreter;
 	private Action actionOuvrir;
 	private Action actionSetAuto;
 	private Action actionSetManuel;
 
 	private VueHall vueHall;
 	
 	// TODO: Remove this (used only in unit tests)
 	@Getter
 	private Simulation simulation;
 	
 	private PropertiesWidget propertiesWidget;
 	private Button btnManuel;
 	private Button btnAutomatique;
 	
 	/**
 	 * Create the application window.
 	 */
 	public MainWindow() {
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
 		parent.setSize(new Point(400, 400));
 		Composite container = new Composite(parent, SWT.NONE);
 		container.setLayout(new GridLayout(3, false));
 		
 		vueHall = new VueHall(container, SWT.NONE);
 		vueHall.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 3));
 		vueHall.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		simulation = new Simulation(vueHall);
 		
		vueHall.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent arg0) {
				vueHall.draw();
			}
		});
		
 		Composite composite = new Composite(container, SWT.NONE);
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
 		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
 		
 		btnManuel = new Button(composite, SWT.TOGGLE);
 		btnManuel.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				actionSetManuel.run();
 			}
 		});
 		btnManuel.setText("Manuel");
 		
 		btnAutomatique = new Button(composite, SWT.TOGGLE);
 		btnAutomatique.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				actionSetAuto.run();
 			}
 		});
 		btnAutomatique.setText("Automatique");
 		
 		Group grpProperties = new Group(container, SWT.NONE);
 		GridData gd_grpProperties = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2);
 		gd_grpProperties.minimumWidth = 150;
 		gd_grpProperties.widthHint = 200;
 		grpProperties.setLayoutData(gd_grpProperties);
 		grpProperties.setLayout(new FillLayout(SWT.HORIZONTAL));
 		grpProperties.setText("Propriétés");
 		
 		propertiesWidget = new PropertiesWidget(grpProperties, SWT.NONE, null);
 		simulation.setPropertiesWidget(propertiesWidget);
 		
 		CLabel lblVitesse = new CLabel(container, SWT.NONE);
 		lblVitesse.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
 		lblVitesse.setAlignment(SWT.RIGHT);
 		lblVitesse.setText("Vitesse de la simulation");
 		
 		Scale sclVitesse = new Scale(container, SWT.NONE);
 		GridData gd_sclVitesse = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
 		gd_sclVitesse.widthHint = 100;
 		gd_sclVitesse.minimumWidth = 100;
 		sclVitesse.setLayoutData(gd_sclVitesse);
 		
 		return container;
 	}
 
 	/**
 	 * Create the actions.
 	 */
 	private void createActions() {
 		// Create the actions
 		{
 			actionQuitter = new Action("Quitter") {
 				@Override
 				public void run() {
 					super.run();
 					close();
 				}
 			};
 		}
 		{
 			actionDemarrer = new Action("Démarrer", ImageDescriptor.createFromFile(getClass(), "icons/play.png") ) {
 				@Override
 				public void run() {
 
 					simulation.play();
 
 				}
 			};
 		}
 		{
 			actionPauser = new Action("Pauser", ImageDescriptor.createFromFile(getClass(), "icons/pause.png") ) {
 				@Override
 				public void run() {
 					simulation.pause();
 				}
 			};
 		}
 		{
 			actionArreter = new Action("Arrêter", ImageDescriptor.createFromFile(getClass(), "icons/stop.png") ) {
 				@Override
 				public void run() {
 					simulation.stop();
 				}
 			};
 		}
 		{
 			actionOuvrir = new Action("Ouvrir", ImageDescriptor.createFromFile(getClass(), "icons/open.png") ) {
 				@Override
 				public void run() {
 					super.run();
 					FileDialog fd = new FileDialog(getShell());
 					fd.setFilterNames(new String[] { "Description XML" });
 				    fd.setFilterExtensions(new String[] { "*.xml" }); 
 				    fd.setFilterPath(System.getProperty("user.dir"));
 				    fd.setFileName("");
 				    
 				    String fileName = fd.open();
 				    
 				    if(fileName != null){
 
 				    	simulation.setXmlFile(new File(fileName));
 				    	simulation.init();
 
 				    	propertiesWidget.setSimulation(simulation);
 
 				    	propertiesWidget.refresh();
 				    }
 				}
 			};
 		}
 		{
 			actionSetAuto = new Action("Mode automatique") {
 				@Override
 				public void run() {
 					super.run();
 					
 					simulation.setMode(Simulation.Mode.AUTO);
 					setChecked(true);
 					actionSetManuel.setChecked(false);
 					btnManuel.setSelection(false);
 					btnAutomatique.setSelection(true);
 				}
 			};
 		}
 		{
 			actionSetManuel = new Action("Mode manuel") {
 				@Override
 				public void run() {
 					super.run();
 					
 					simulation.setMode(Simulation.Mode.MANUEL);
 					setChecked(true);
 					actionSetAuto.setChecked(false);
 					btnManuel.setSelection(true);
 					btnAutomatique.setSelection(false);
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
 		MenuManager menuBar = new MenuManager("menu");
 		{
 			MenuManager menuFichier = new MenuManager("Fichier");
 			menuBar.add(menuFichier);
 			menuFichier.add(actionDemarrer);
 			menuFichier.add(actionPauser);
 			menuFichier.add(actionArreter);
 			menuFichier.add(new Separator());
 			menuFichier.add(actionQuitter);
 		}
 		return menuBar;
 	}
 
 	/**
 	 * Create the toolbar manager.
 	 * @return the toolbar manager
 	 */
 	@Override
 	protected ToolBarManager createToolBarManager(int style) {
 		ToolBarManager toolBarManager = new ToolBarManager(style);
 		toolBarManager.add(actionOuvrir);
 		toolBarManager.add(new Separator());
 		toolBarManager.add(actionDemarrer);
 		toolBarManager.add(actionPauser);
 		toolBarManager.add(actionArreter);
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
 			MainWindow window = new MainWindow();
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
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 		shell.setText("SGBag - Interface de simulation");
 		shell.setMinimumSize(600,500);
 	}
 }

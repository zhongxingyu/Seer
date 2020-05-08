 /**
  * 
  */
 package cl.miempresa.accesos.principal;
 
 import java.io.FileInputStream;
 import java.util.List;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import swing2swt.layout.BorderLayout;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.wb.swt.SWTResourceManager;
 
 import cl.altair.acceso.dao.ComunaDAO;
 import cl.altair.acceso.dao.EdificioDAO;
 import cl.altair.acceso.dao.MotivoDAO;
 import cl.altair.acceso.dao.ProvinciaDAO;
 import cl.altair.acceso.dao.RegionDAO;
 import cl.altair.acceso.dao.TipoDependenciaDAO;
 import cl.altair.acceso.modelo.Edificio;
 import cl.altair.accesos.principal.formularios.ConfiguraEdificio;
 import cl.altair.accesos.principal.formularios.NewDependencia;
 import cl.altair.accesos.wizard.UsuarioInmueble.WizardUsuarioInmueble;
 import cl.altair.accesos.wizard.edificio.WizardEdificio;
 
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 
 /**
  * @author lpino
  *
  */
 public class Main {
 
 	protected static Shell shlGestion;
 	private static Edificio edificio;
 	private ListaAccesos listado;
 	private final static Logger LOGGER = Logger.getLogger(Main.class.getName());
 	private ArbolEdificio elArbolEdificio;
 	private ArbolDirectorio elArbol;
 
 	/**
 	 * Launch the application.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			Main window = new Main();
 			window.open();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Open the window.
 	 */
 	public void open() {
 		Display.setAppName("iConserje");
 		Display display = Display.getDefault();
 		createContents();
 		shlGestion.open();
 		shlGestion.layout();
 		while (!shlGestion.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected void createContents() {
 		shlGestion = new Shell();
 		shlGestion.setSize(1200, 800);
 		shlGestion.setText("Gestion de Accesos");
 		shlGestion.setLayout(new FillLayout(SWT.HORIZONTAL));
    
 		init();
 		
 		Composite composite = new Composite(shlGestion, SWT.BORDER);
 		composite.setLayout(new BorderLayout(0, 0));
 		
 		ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);
 		toolBar.setLayoutData(BorderLayout.NORTH);
 		
 		Label lblNewLabel = new Label(composite, SWT.NONE);
 		lblNewLabel.setLayoutData(BorderLayout.WEST);
 		
 		SashForm sashForm = new SashForm(composite, SWT.SMOOTH);
 		sashForm.setLayoutData(BorderLayout.CENTER);
 		TabFolder tabFolder = new TabFolder(sashForm, SWT.NONE);
 		
 		TabItem tbtmEdificio = new TabItem(tabFolder, SWT.NONE);
 		tbtmEdificio.setText("Edificio");
 		
 		Group grpEdificio = new Group(tabFolder, SWT.NONE);
 		grpEdificio.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.NORMAL));
 		tbtmEdificio.setControl(grpEdificio);
 		grpEdificio.setLayout(new GridLayout(1, false));
 		
 		ToolBar toolBar_3 = new ToolBar(grpEdificio, SWT.FLAT | SWT.RIGHT);
 		
 		//Boton para activar el dialogo de la nueva dependencia
 		ToolItem tltmNewItem = new ToolItem(toolBar_3, SWT.NONE);
 		tltmNewItem.setImage(SWTResourceManager.getImage(Main.class, "/com/altair/accesos/recursos/Button-Add-icon.png"));
 		tltmNewItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				NewDependencia formNuevaDependencia = new NewDependencia(shlGestion,0);
 				formNuevaDependencia.setElArbolEdificio(elArbolEdificio);
 				formNuevaDependencia.open();
 			}
 		});
 		tltmNewItem.setText("Nueva Dependencia");
 		
 		//Muestra el arbol del edificio
 		elArbolEdificio = new ArbolEdificio();
 		elArbolEdificio.setElEdificio(edificio);
 		elArbolEdificio.createPartControl(grpEdificio);
 
 		
 		TabItem tbtmDirectorio = new TabItem(tabFolder, SWT.NONE);
 		tbtmDirectorio.setText("Directorio");
 		
 		
 		Group grpGrupo = new Group(tabFolder, SWT.NONE);
 		tbtmDirectorio.setControl(grpGrupo);
 		grpGrupo.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.NORMAL));
 		grpGrupo.setLayout(new GridLayout(1, false));
 
 		ToolBar toolBar_2 = new ToolBar(grpGrupo, SWT.FLAT | SWT.RIGHT);
 		toolBar_2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));		
 
 		//Item del Toolbar del edificio
 /*		ToolItem tltmBuscar = new ToolItem(toolBar_2, SWT.DROP_DOWN);
 		tltmBuscar.setText("Buscar por...");
 
 		DropdownSelectionListener listenerBuscar = new DropdownSelectionListener(tltmBuscar);
 	    listenerBuscar.add("Usuario");
 	    listenerBuscar.add("Empresa");
 	    listenerBuscar.add("Dependencia");
 	    tltmBuscar.addSelectionListener(listenerBuscar);
 */	    
 		ToolItem tltmAgregarEmpresa = new ToolItem(toolBar_2, SWT.NONE);
 		tltmAgregarEmpresa.setImage(SWTResourceManager.getImage(Main.class, "/com/altair/accesos/recursos/Button-Add-icon.png"));
 		tltmAgregarEmpresa.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				WizardDialog wizardDialog = new WizardDialog(shlGestion, new WizardUsuarioInmueble(elArbol));
 				if (wizardDialog.open() == Window.OK) {
 				  System.out.println("Ok pressed");
 				} else {
 				  System.out.println("Cancel pressed");
 				}
 			}
 		});
 		tltmAgregarEmpresa.setText("Agregar Empresa");
 		
 		
 		elArbol = new ArbolDirectorio();
 		elArbol.setElEdificio(edificio);
 		elArbol.createPartControl(grpGrupo);
 		
 
 		Group grpGrupo_1 = new Group(sashForm, SWT.NONE);
 		grpGrupo_1.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.NORMAL));
 		grpGrupo_1.setLayout(new GridLayout(1, false));
 		
 		ToolBar toolBar_1 = new ToolBar(grpGrupo_1, SWT.FLAT | SWT.RIGHT);
 		toolBar_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
 		
 		//Item del filtro en el menu superior
 		ToolItem tltmFiltro = new ToolItem(toolBar_1, SWT.DROP_DOWN);
 		tltmFiltro.setText("Filtros");
 	    DropdownSelectionListener listenerOne = new DropdownSelectionListener(tltmFiltro);
 	    listenerOne.add("Fecha");
 	    listenerOne.add("Empresa");
 	    listenerOne.add("Dependencia");
 	    tltmFiltro.addSelectionListener(listenerOne);
 
 		
 		ToolItem tltmCrear = new ToolItem(toolBar_1, SWT.NONE);
 		tltmCrear.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				RegistroVisita dialogoRegistro = new RegistroVisita(shlGestion, SWT.BORDER | SWT.APPLICATION_MODAL | SWT.TITLE );
 				dialogoRegistro.setEdificio(edificio);
 				dialogoRegistro.setListado(listado);
 				dialogoRegistro.open();
 			}
 		});
 		tltmCrear.setText("Crear");
 
 		listado = new ListaAccesos();
 		listado.createPartControl(grpGrupo_1);
 		
 		sashForm.setWeights(new int[] {1, 2});
 		
 		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
 		lblNewLabel_1.setLayoutData(BorderLayout.EAST);
 		
 		Composite composite_1 = new Composite(composite, SWT.NONE);
 		composite_1.setLayoutData(BorderLayout.SOUTH);
 		composite_1.setLayout(new FormLayout());
 		
 		Menu menu = new Menu(shlGestion, SWT.BAR);
 		shlGestion.setMenuBar(menu);
 		
 		MenuItem mntmArchivo = new MenuItem(menu, SWT.CASCADE);
 		mntmArchivo.setText("Archivo");
 		
 		Menu menu_1 = new Menu(mntmArchivo);
 		mntmArchivo.setMenu(menu_1);
 		
 		MenuItem mntmCargaDatosEdificio = new MenuItem(menu_1, SWT.NONE);
 		mntmCargaDatosEdificio.setText("Carga Datos Edificio...");
 		
 		MenuItem mntmExportaDatosEdificio = new MenuItem(menu_1, SWT.NONE);
 		mntmExportaDatosEdificio.setText("Exporta Datos Edificio...");
 		
 		MenuItem mntmConfiguracin = new MenuItem(menu, SWT.CASCADE);
 		mntmConfiguracin.setText("Configuraci\u00F3n");
 		
 		Menu menu_2 = new Menu(mntmConfiguracin);
 		mntmConfiguracin.setMenu(menu_2);
 		
 		MenuItem mntmConfiguraEdificio = new MenuItem(menu_2, SWT.NONE);
 		mntmConfiguraEdificio.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ConfiguraEdificio dialogoEdificio = new ConfiguraEdificio(shlGestion, SWT.BORDER | SWT.APPLICATION_MODAL | SWT.TITLE );
 				dialogoEdificio.open();
 			}
 		});
 		mntmConfiguraEdificio.setText("Configura Edificio...");
 
 	}
 
 	public Edificio getEdificio() {
 		return edificio;
 	}
 	
 	private void init(){
 		EdificioDAO edao = new EdificioDAO();
 		List<Edificio> lista = edao.findAll();		
 		if(lista.size() == 0){
 			LOGGER.info("No existe edificio");
 			//Creacion de los datos basicos para la operacion de la aplicacion
 			MotivoDAO.creaMotivos();
 			TipoDependenciaDAO.creaTipoDependencias();
 			//Crea las regiones/provicias/comunas del pais. Necesarias para las direcciones
 			RegionDAO.creaRegiones();
 			ProvinciaDAO.creaProvicias();
 			ComunaDAO.creaComunas();
 			//Muestra wizard para cargar la informacion del edificio
 			LOGGER.fine("Abriendo wizard para crear los datos del edificio");
 			WizardDialog wizardDialog = new WizardDialog(shlGestion, new WizardEdificio());
 			if (wizardDialog.open() == Window.OK) {
 			  System.out.println("Ok pressed");
 			} else {
 			  System.out.println("Cancel pressed");
			  shlGestion.dispose();
			  System.exit(0);
 			}
 		} else {
 			LOGGER.info("Existe edificio");
 			edificio = lista.get(0);
 		}
 	}
 	/*
 	private static void initDerby(){
 	    // Decide on the db system directory: <userhome>/.addressbook/
 	    String userHomeDir = System.getProperty("user.home", ".");
 	    System.out.println("PATH USER.HOME = " + userHomeDir);
 	    String systemDir = userHomeDir + "/";
 	    // Set the db system directory.
 	    System.setProperty("derby.system.home", systemDir);
 	}
 */
 	static {
 		try{
 			FileInputStream fis = new FileInputStream("log.properties");
 			LogManager.getLogManager().readConfiguration(fis);
 			fis.close();
 		} catch(Exception e){
 			System.out.println("Error al cargar archivo de propiedades de logs " + e.getMessage());
 		}
 	}
 }

 /* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
  *
  * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *  Generalitat Valenciana
  *   Conselleria d'Infraestructures i Transport
  *   Av. Blasco Ib��ez, 50
  *   46010 VALENCIA
  *   SPAIN
  *
  *      +34 963862235
  *   gvsig@gva.es
  *      www.gvsig.gva.es
  *
  *    or
  *
  *   IVER T.I. S.A
  *   Salamanca 50
  *   46005 Valencia
  *   Spain
  *
  *   +34 963163400
  *   dac@iver.es
  */
 package com.iver.andami.ui.mdiFrame;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.ContainerEvent;
 import java.awt.event.ContainerListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 import javax.swing.MenuElement;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 import org.apache.log4j.Logger;
 import org.gvsig.gui.beans.controls.IControl;
 
 import com.iver.andami.Launcher;
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.Messages;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.andami.plugins.ExtensionDecorator;
 import com.iver.andami.plugins.PluginClassLoader;
 import com.iver.andami.plugins.config.generate.ActionTool;
 import com.iver.andami.plugins.config.generate.Label;
 import com.iver.andami.plugins.config.generate.Menu;
 import com.iver.andami.plugins.config.generate.PopupMenu;
 import com.iver.andami.plugins.config.generate.SelectableTool;
 import com.iver.andami.plugins.config.generate.SkinExtensionType;
 import com.iver.andami.plugins.config.generate.ToolBar;
 import com.iver.andami.ui.mdiManager.MDIManager;
 import com.iver.andami.ui.mdiManager.MDIManagerFactory;
 
 
 /**
  * Main application window.
  *
  * @version $Revision: 35160 $
  */
 public class MDIFrame extends JFrame implements ComponentListener,
 ContainerListener, ActionListener, MainFrame {
 	/** DOCUMENT ME! */
 	private static Logger logger = Logger.getLogger(MDIFrame.class.getName());
 	private MDIManager mdiManager = MDIManagerFactory.createManager();
 
 	/** Elementos de la aplicaci�n */
 	private JMenuBar menuBar = new JMenuBar();
 
 	/** Panel which contains the toolbars */
 	private JPanel toolBars = new JPanel();
 
 	/** Status bar */
 	private NewStatusBar bEstado = null;
 
 	/** Asocia los nombres con las barras de herramientas */
 	private HashMap toolBarMap = new HashMap();
 
 	/** Almacena los grupos de selectableTools */
 	private HashMap buttonGroupMap = new HashMap();
 	/**
 	 * Stores the initially selected tools.
 	 * It contains pairs (String groupName, JToolBarToggleButton button)
 	 */
 	private HashMap initialSelectedTools = new HashMap();
 
 	/**
 	 * Stores the actionCommand of the selected tool, for each group.
 	 * It contains pairs (String groupName, JToolBarToggleButton button)
 	 */
 	private HashMap selectedTool = null;
 	// this should be the same value defined at plugin-config.xsd
 	private String defaultGroup = "unico";
 
 	/** Asocia los nombres con los popupMenus */
 	private HashMap popupMap = new HashMap();
 
 	/** Asocia controles con la clase de la extension asociada */
 	private HashMap controlClass = new HashMap();
 
 	/**
 	 * Asocia la informaci�n sobre las etiquetas que van en la status bar con
 	 * cada extension
 	 */
 	private HashMap classLabels = new HashMap();
 
 	//private HashMap classControls = new HashMap();
 
 	/** ProgressListeners (ver interfaz com.iver.mdiApp.ui.ProgressListener) */
 	private ArrayList progressListeners = new ArrayList();
 
 	/** Timer para invocar los enventos de la interfaz anterior */
 	private Timer progressTimer = null;
 
 	/** Tabla hash que asocia las clases con las extensiones */
 	private HashMap classesExtensions = new HashMap();
 
 	/** �ltima clase que activ� etiquetas */
 	private Class lastLabelClass;
 
 	/** Instancia que pone los tooltip en la barra de estado */
 	private TooltipListener tooltipListener = new TooltipListener();
 	private HashMap infoCodedMenus = new HashMap();
 
 	private String titlePrefix;
 
 	private static final String noIcon = "no-icon";
 
 	/**
 	 * Makes some initialization tasks.
 	 *
 	 * @throws RuntimeException DOCUMENT ME!
 	 */
 	public void init() {
 		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
 		if (!SwingUtilities.isEventDispatchThread()) {
 			throw new RuntimeException("Not Event Dispatch Thread");
 		}
 
 		//Se a�aden los listeners del JFrame
 		this.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				Launcher.closeApplication();
 			}
 		});
 		this.addComponentListener(this);
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 		//Se configura la barra de menu
 		setJMenuBar(menuBar);
 
 		//Se configura el layout del JFrame principal
 		this.getContentPane().setLayout(new BorderLayout());
 
 		/*
 		 * Se configura y se a�ade el JPanel de las barras de
 		 * herramientas
 		 */
 		FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
 		layout.setHgap(0);
 		layout.setVgap(0);
 		toolBars.setLayout(layout);
 		getContentPane().add(toolBars, BorderLayout.PAGE_START);
 
 		// Se a�ade la barra de estado a la aplicaci�n
 		bEstado = new NewStatusBar();
 		bEstado.setInfoText(Messages.getString("StatusBar.Aplicacion_iniciada"));
 		getContentPane().add(bEstado, BorderLayout.SOUTH);
 
 		this.toolBars.addContainerListener(this);
 
 		pack();
 
 		// TODO LWS Aqui deber�a cargar los valores salvados de la �ltima ejecuci�n.
 		setSize(700, 580);
 		setLocation(10,10);
 		setExtendedState(MAXIMIZED_BOTH);
 
 		mdiManager.init(this);
 	}
 
 	/*
 	 * (non-javadoc)
 	 * @see java.awt.Frame.setTitle(String title)
 	 */
 	public void setTitle(String title) {
		super.setTitle(title + " - " + titlePrefix);
 	}
 
 	/**
 	 * A�ade un modo de operaci�n a la caja de herramientas
 	 *
 	 * @param ext Texto del boton, si es null no aparece texto
 	 * @param ext Icono del boton, si es null no aparece icono
 	 * @param ext Extensi�n asociada al control
 	 * @param selectableTool Enable text del control
 	 *
 	 * @throws ClassNotFoundException
 	 * @throws RuntimeException DOCUMENT ME!
 	 */
 	public void addTool(PluginClassLoader loader, SkinExtensionType ext,
 			ToolBar toolBar, SelectableTool selectableTool)
 	throws ClassNotFoundException {
 		if (!SwingUtilities.isEventDispatchThread()) {
 			throw new RuntimeException("No Event Dispatch Thread");
 		}
 
 		// Para traducir
 		PluginServices ps = PluginServices.getPluginServices(loader.getPluginName());
 
 		JToolBarToggleButton btn;
 		ImageIcon image = PluginServices.getIconTheme().get(selectableTool.getIcon());
 
 		if (image != null) {
 			btn = new JToolBarToggleButton(selectableTool.getText(), image);
 		} else {
 			logger.error(PluginServices.getText(this, "Unable_to_find_icon") +": "+selectableTool.getIcon());
 			btn = new JToolBarToggleButton(selectableTool.getText(),
 					PluginServices.getIconTheme().get(noIcon));
 		}
 
 		com.iver.andami.ui.mdiFrame.ToggleButtonModel buttonModel = new com.iver.andami.ui.mdiFrame.ToggleButtonModel();
 		btn.setModel(buttonModel);
 		btn.setMargin(new Insets(0, 0, 0, 0));
 		btn.addMouseListener(tooltipListener);
 		btn.addActionListener(this);
 		btn.setFocusable(false);
 		btn.setActionCommand(selectableTool.getActionCommand());
 		btn.setToolTipText(selectableTool.getTooltip());
 		btn.setEnabled(false);
 		btn.setVisible(false);
 		String name = toolBar.getName();
 
 		SelectableToolBar jtb = (SelectableToolBar) toolBarMap.get(name);
 
 		if (jtb == null) {
 			jtb = new SelectableToolBar(name);
 			jtb.setRollover(true);
 			jtb.setAndamiVisibility(toolBar.getIsVisible());
 			toolBarMap.put(name, jtb);
 			toolBars.add(jtb);
 		}
 
 		ButtonGroup group;
 		if (buttonGroupMap.containsKey(selectableTool.getGroup())) {
 			group = (ButtonGroup) buttonGroupMap.get(selectableTool.getGroup());
 		}
 		else {
 			group = new ButtonGroup();
 			buttonGroupMap.put(selectableTool.getGroup(), group);
 
 		}
 		jtb.addButton(group, btn);
 		buttonModel.setGroupName(selectableTool.getGroup());
 
 		if (selectableTool.getIsDefault()) {
 			btn.setSelected(true);
 			initialSelectedTools.put(selectableTool.getGroup(), btn.getActionCommand());
 		}
 
 		controlClass.put(btn, loader.loadClass(ext.getClassName()));
 
 		if (selectableTool.getName() != null) {
 			btn.setName(selectableTool.getName());
 		}
 
 		if (selectableTool.getTooltip() != null) {
 			btn.setToolTip(ps.getText(selectableTool.getTooltip()));
 		}
 
 		if (selectableTool.getEnableText() != null) {
 			btn.setEnableText(ps.getText(selectableTool.getEnableText()));
 		}
 
 		if (selectableTool.getLast() == true) {
 			jtb.addSeparator();
 		}
 	}
 
 	/**
 	 * A�ade un bot�n a la barra de herramientas
 	 *
 	 * @param ext Texto del boton, si es null no aparece texto
 	 * @param ext Extensi�n asociada al control
 	 * @param toolBar Icono del boton, si es null no aparece texto
 	 * @param actionTool Tooltip de la barra de herramientas
 	 *
 	 * @throws ClassNotFoundException
 	 * @throws RuntimeException DOCUMENT ME!
 	 */
 	public void addTool(PluginClassLoader loader, SkinExtensionType ext,
 			ToolBar toolBar, ActionTool actionTool) throws ClassNotFoundException {
 		if (!SwingUtilities.isEventDispatchThread()) {
 			throw new RuntimeException("No Event Dispatch Thread");
 		}
 
 		// Para traducir los textos que vengan
 		PluginServices ps = PluginServices.getPluginServices(loader.getPluginName());
 
 		JToolBarButton btn;
 		ImageIcon image = PluginServices.getIconTheme().get(actionTool.getIcon(),ps.getClassLoader());
 
 		if (image != null) {
 			btn = new JToolBarButton(actionTool.getText(), image);
 		} else {
 			logger.error(PluginServices.getText(this, "Unable_to_find_icon") +": "+actionTool.getIcon());
 			btn = new JToolBarButton(actionTool.getText(),
 					PluginServices.getIconTheme().get(noIcon));
 		}
 
 		btn.setMargin(new Insets(0, 0, 0, 0));
 		btn.addMouseListener(tooltipListener);
 		btn.addActionListener(this);
 		btn.setFocusable(false);
 		btn.setActionCommand(actionTool.getActionCommand());
 		btn.setEnabled(false);
 		btn.setVisible(false);
 
 		String name = toolBar.getName();
 
 		SelectableToolBar jtb = (SelectableToolBar) toolBarMap.get(name);
 
 		if (jtb == null) {
 			jtb = new SelectableToolBar(name);
 			jtb.setRollover(true);
 			jtb.setAndamiVisibility(toolBar.getIsVisible());
 			toolBarMap.put(name, jtb);
 			toolBars.add(jtb);
 		}
 
 		jtb.add(btn);
 
 		controlClass.put(btn, loader.loadClass(ext.getClassName()));
 
 		if (actionTool.getName() != null) {
 			btn.setName(actionTool.getName());
 		}
 
 		if (actionTool.getTooltip() != null) {
 			btn.setToolTip(ps.getText(actionTool.getTooltip()));
 		}
 
 		if (actionTool.getEnableText() != null) {
 			btn.setEnableText(ps.getText(actionTool.getEnableText()));
 		}
 
 		if (actionTool.getLast() == true) {
 			jtb.addSeparator();
 		}
 	}
 
 	/**
 	 * Creates the needed menu structure to add the menu to the bar.
 	 * Returns the father which must hold the menu which was
 	 * provided as parameter.
 	 *
 	 * Crea la estructura de men�s necesaria para a�adir el menu a la barra.
 	 * Devuelve el padre del cual debe colgar el menu que se pasa como
 	 * par�metro.
 	 *
 	 * @param menu The Menu whose support is going to be added
 	 * @param loader The plugin's class loader
 	 *
 	 * @return The proper father for the menu which was provided as parameter
 	 */
 	private JMenu createMenuAncestors(Menu menu, PluginClassLoader loader) {
 		MenuElement menuPadre = null;
 
 		PluginServices ps = PluginServices.getPluginServices(loader.getPluginName());
 
 		String[] menues = menu.getText().split("/");
 		ArrayList menuList = new ArrayList();
 		menuList.add(menues[0]);
 		menuPadre = getMenu(menuList, menuBar);
 
 		JMenu padre = null;
 
 		if (menuPadre==null) {
 			padre = new JMenu(ps.getText(menues[0]));
 			padre.setName(menues[0]);
 			menuBar.add(padre);
 		}
 		else if (menuPadre instanceof JMenu) {
 			padre = (JMenu) menuPadre;
 		}
 		else {
 			logger.error(ps.getText("error_creating_menu_Ancestor_does_not_exist"));
 			return null;
 		}
 
 		//Se crea el resto de menus
 		ArrayList temp = new ArrayList();
 
 		for (int i = 1; i < (menues.length - 1); i++) {
 			temp.add(menues[i]);
 		}
 
 		menuPadre = createMenus(temp, padre);
 
 		return (JMenu) menuPadre;
 	}
 
 	/**
 	 * A�ade la informaci�n del menu al framework. Debido a que los men�es se
 	 * pueden introducir en un orden determinado por el usuario, pero los
 	 * plugins se instalan en un orden arbitrario, primero se almacena la
 	 * informaci�n de todos los menus para luego ordenarlos y posteriormente
 	 * a�adirlos al interfaz
 	 *
 	 * @param loader Posicion del menu. Se ordena por este campo
 	 * @param ext Array con los nombres de los padres del menu
 	 * @param menu Texto del menu
 	 *
 	 * @throws ClassNotFoundException
 	 * @throws RuntimeException DOCUMENT ME!
 	 */
 	public void addMenu(PluginClassLoader loader, SkinExtensionType ext,
 			Menu menu) throws ClassNotFoundException {
 		if (!SwingUtilities.isEventDispatchThread()) {
 			throw new RuntimeException("No Event Dispatch Thread");
 		}
 
 		JMenu menuPadre = createMenuAncestors(menu, loader);
 
 		//Se registra y a�ade el menu
 		/* String[] aux = menu.getText().split("/");
 
         if (aux.length == 2)
             if (aux[1].equals("----"))
             {
                 menuPadre.addSeparator();
                 return;
             } */
 		if (menu.getIs_separator())
 		{
 			menuPadre.addSeparator();
 			return;
 		}
 
 		JMenuItem nuevoMenu = createJMenuItem(loader, menu);
 		nuevoMenu.addMouseListener(tooltipListener);
 		nuevoMenu.addActionListener(this);
 		menuPadre.add(nuevoMenu);
 		controlClass.put(nuevoMenu, loader.loadClass(ext.getClassName()));
 	}
 
 
 	/**
 	 * Dado un array de nombres de menu, encuentra el  men�
 	 *
 	 * @param nombres DOCUMENT ME!
 	 * @param padre DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	private javax.swing.JMenuItem getMenu(ArrayList nombres, MenuElement parent) {
 		if (parent instanceof javax.swing.JMenu) {
 			javax.swing.JMenu parentItem = (javax.swing.JMenu) parent;
 
 			for (int i=0; i<parentItem.getMenuComponentCount(); i++) {
 				if (parentItem.getMenuComponent(i).getName()!=null // not a JToolBar.Separator
 						&& parentItem.getMenuComponent(i).getName().compareTo((String)nombres.get(0)) == 0) {
 					nombres.remove(0);
 					if (nombres.isEmpty()) {
 						if (parentItem.getMenuComponent(i) instanceof javax.swing.JMenuItem) {
 							return (javax.swing.JMenuItem) parentItem.getMenuComponent(i);
 						}
 						else {
 							logger.error(PluginServices.getText(this, "Menu_type_not_supported_")+" "+parentItem.getMenuComponent(i).getClass().getName());
 							return null;
 						}
 					}
 					else {
 						return getMenu(nombres, (MenuElement) parentItem.getMenuComponent(i));
 					}
 				}
 			}
 		}
 		else if (parent instanceof JMenuBar) {
 			javax.swing.JMenuBar parentItem = (javax.swing.JMenuBar) parent;
 
 			for (int i=0; i<parentItem.getMenuCount(); i++) {
 				if (parentItem.getMenu(i).getName()!=null // not a JToolBar.Separator
 						&& parentItem.getMenu(i).getName().compareTo((String)nombres.get(0)) == 0) {
 					nombres.remove(0);
 					if (nombres.isEmpty()) {
 						if (parentItem.getMenu(i) instanceof javax.swing.JMenuItem) {
 							return (javax.swing.JMenuItem) parentItem.getMenu(i);
 						}
 						else {
 							logger.error(PluginServices.getText(this, "Menu_type_not_supported_")+" "+parentItem.getMenu(i).getClass().getName());
 							return null;
 						}
 					}
 					else {
 						return getMenu(nombres, (MenuElement) parentItem.getMenu(i));
 					}
 				}
 			}
 		}
 		else {
 			logger.error(PluginServices.getText(this, "Menu_type_not_supported_")+" "+parent.getClass().getName()+" "+parent.toString());
 		}
 		return null;
 	}
 
 	/**
 	 * Crea la estructura de menus recursivamente. Por ejemplo, si se le pasa
 	 * en el par�metro nombres el array {"Search", "References", "Workspace"}
 	 * crear� un men� Search, un submen� del anterior que se llamar�
 	 * References y debajo de �ste �ltimo otro menu llamado Workspace
 	 *
 	 * @param nombres Array con los nombres de los men�s que se quieren crear
 	 * @param padre Menu padre de los men�s creados. Es �til porque es un
 	 * 		  algoritmo recursivo
 	 *
 	 * @return Devuelve el men� creado. Al final de toda la recursividad,
 	 * 		   devolver� el men� de m�s abajo en la jerarqu�a
 	 *
 	 * @throws RuntimeException DOCUMENT ME!
 	 */
 	private JMenu createMenus(ArrayList nombres, JMenu padre) {
 		if (!SwingUtilities.isEventDispatchThread()) {
 			throw new RuntimeException("No Event Dispatch Thread");
 		}
 
 		//si no quedan nombres de menu por crear se vuelve: caso base
 		if (nombres.size() == 0) {
 			return padre;
 		}
 
 		//Se busca el menu por si ya existiera para no crearlo otra vez
 		JMenu buscado = null;
 
 		for (int i = 0; i < padre.getMenuComponentCount(); i++) {
 			try {
 				JMenu hijo = (JMenu) padre.getMenuComponent(i);
 
 				if (hijo.getName().compareTo((String) nombres.get(0)) == 0) {
 					buscado = hijo;
 				}
 			} catch (ClassCastException e) {
 				/*
 				 * Se ha encontrado un elemento hoja del arbol de men�es
 				 */
 			}
 		}
 
 		if (buscado != null) {
 			//Si lo hemos encontrado creamos el resto
 			nombres.remove(0);
 
 			return createMenus(nombres, buscado);
 		} else {
 			//Si no lo hemos encontrado se crea el menu, se a�ade al padre
 			//y se crea el resto
 			String nombre = (String) nombres.get(0);
 			JMenu menuPadre = new JMenu((String) PluginServices.getText(this, nombre));
 			menuPadre.setName(nombre);
 			padre.add(menuPadre);
 
 			nombres.remove(0);
 
 			return createMenus(nombres, menuPadre);
 		}
 	}
 
 	/**
 	 * M�todo invocado en respuesta a ciertos eventos de la interfaz que pueden
 	 * ocultar botones de las barras de herramientas y que redimensiona �sta
 	 * de manera conveniente para que no se oculte ninguno
 	 */
 	private void ajustarToolBar() {
 		int margen = 8;
 		int numFilas = 1;
 		double acum = margen;
 
 		int toolHeight = 0;
 
 		for (int i = 0; i < toolBars.getComponentCount(); i++) {
 			Component c = toolBars.getComponent(i);
 
 			if (!c.isVisible()) {
 				continue;
 			}
 
 			double width = c.getPreferredSize().getWidth();
 			acum = acum + width;
 
 			if (acum > this.getWidth()) {
 				numFilas++;
 				acum = width + margen;
 			}
 
 			if (c.getPreferredSize().getHeight() > toolHeight) {
 				toolHeight = c.getPreferredSize().height;
 			}
 		}
 
 		toolBars.setPreferredSize(new Dimension(this.getWidth(),
 				(int) (numFilas * toolHeight)));
 
 		toolBars.updateUI();
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param classesExtensions
 	 */
 	public void setClassesExtensions(HashMap classesExtensions) {
 		this.classesExtensions = classesExtensions;
 	}
 
 	/**
 	 * M�todo de callback invocado cuando se selecciona un men� o un bot�n de
 	 * la barra de herramientas. Se busca la extensi�n asociada y se ejecuta
 	 *
 	 * @param e Evento producido
 	 */
 	public void actionPerformed(ActionEvent e) {
 		Object control = e.getSource();
 		com.iver.andami.plugins.IExtension ext = (com.iver.andami.plugins.IExtension) classesExtensions.get((Class) controlClass.get(
 				control));
 		String actionCommand = e.getActionCommand();
 		try {
 			logger.debug(Messages.getString("Ejecutando comando: ") + actionCommand);
 			ext.execute(actionCommand);
 
 			try {
 				JToolBarToggleButton toggle = (JToolBarToggleButton) control;
 				ToggleButtonModel model = (ToggleButtonModel)toggle.getModel();
 				selectedTool.put(model.getGroupName(), actionCommand);
 			}
 			catch (ClassCastException ex) {}
 			catch (NullPointerException ex) {}
 
 		} catch (RuntimeException t) {
 			if (ext==null) {
 				logger.error(Messages.getString("No_extension_associated_with_this_event_")+ e.getActionCommand());
 			}
 			NotificationManager.addError(
 					Messages.getString("PluginServices.Bug_en_el_codigo") + ": " + t.getMessage(),
 					t);
 		}
 
 		enableControls();
 		showMemory();
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param name DOCUMENT ME!
 	 * @param loader DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	private String getName(String name, PluginClassLoader loader) {
 		if (name.indexOf('.') == -1) {
 			return loader.getPluginName() + "." + name;
 		} else {
 			return name;
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param loader DOCUMENT ME!
 	 * @param menu DOCUMENT ME!
 	 *
 	 * @throws RuntimeException DOCUMENT ME!
 	 */
 	public void addPopupMenu(PluginClassLoader loader, PopupMenu menu) {
 		if (!SwingUtilities.isEventDispatchThread()) {
 			throw new RuntimeException("No Event Dispatch Thread");
 		}
 
 		String name = getName(menu.getName(), loader);
 
 		//Se crea el control popupmenu
 		JPopUpMenu popupMenu = (JPopUpMenu) popupMap.get(name);
 
 		if (popupMenu == null) {
 			popupMenu = new JPopUpMenu(menu.getName());
 			popupMap.put(name, popupMenu);
 		}
 
 		// Se a�aden las entradas
 		Menu[] menues = menu.getMenu();
 
 		for (int i = 0; i < menues.length; i++) {
 			//Se registra y a�ade el menu
 			JMenuItem nuevoMenu = createJMenuItem(loader, menues[i]);
 
 			popupMenu.add(nuevoMenu);
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param loader
 	 * @param menu
 	 *
 	 * @return
 	 *
 	 * @throws RuntimeException DOCUMENT ME!
 	 */
 	private JMenuItem createJMenuItem(PluginClassLoader loader, Menu menu) {
 		JMenuItem nuevoMenu = null;
 
 		PluginServices ps = PluginServices.getPluginServices(loader.getPluginName());
 		String text = menu.getText();
 		text = text.substring(text.lastIndexOf('/') + 1);
 		String translatedText = ps.getText(text);
 
 		if (menu.getIcon() != null) {
 			ImageIcon image = PluginServices.getIconTheme().get(menu.getIcon(),ps.getClassLoader());
 			if (image != null) {
 				nuevoMenu = new JMenuItem(translatedText, image);
 			} else {
 				nuevoMenu = new JMenuItem(translatedText, PluginServices.getIconTheme().get(noIcon));
 				logger.error(PluginServices.getText(this, "Unable_to_find_icon") +": "+menu.getIcon());
 			}
 		} else {
 			nuevoMenu = new JMenuItem(translatedText);
 		}
 		nuevoMenu.setName(text);
 		if (menu.getMnemonic() != null) {
 			if (menu.getMnemonic().length() != 1) {
 				throw new RuntimeException(
 				"Mnemonic must be 1 character length");
 			}
 
 			nuevoMenu.setMnemonic(KeyMapping.getKey(menu.getMnemonic().charAt(0)));
 		}
 
 		if (menu.getKey() != null) {
 			String osName = (String) System.getProperty("os.name");
 			boolean MAC_OS_X = osName.toLowerCase().startsWith("mac os x");
 			if (MAC_OS_X) {
 				//en OS X, en vez de hardwiring la ShortcutKey, usamos el default sugerido por el OS
 				nuevoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyMapping.getKey(
 						menu.getKey().charAt(0)), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 			} else {
 				nuevoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyMapping.getKey(
 						menu.getKey().charAt(0)), ActionEvent.ALT_MASK));
 			}
 		}
 
 		nuevoMenu.setActionCommand(menu.getActionCommand());
 
 		if (menu.getTooltip() != null) {
 			nuevoMenu.setToolTip(ps.getText(menu.getTooltip()));
 		}
 
 		if (menu.getEnableText() != null) {
 			nuevoMenu.setEnableText(ps.getText(menu.getEnableText()));
 		}
 
 		nuevoMenu.setEnabled(true);
 		nuevoMenu.setVisible(true);
 
 		return nuevoMenu;
 	}
 
 	/**
 	 * Muestra u oculta el menu de nombre 'name'
 	 *
 	 * @param name Nombre del menu que se quiere mostrar
 	 * @param x Evento de raton
 	 * @param y DOCUMENT ME!
 	 * @param c DOCUMENT ME!
 	 */
 	private void showPopupMenu(String name, int x, int y, Component c) {
 		JPopupMenu menu = (JPopupMenu) popupMap.get(name);
 
 		if (menu != null) {
 			menu.show(c, x, y);
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param name DOCUMENT ME!
 	 * @param listener DOCUMENT ME!
 	 */
 	public void removePopupMenuListener(String name, ActionListener listener) {
 		JPopupMenu menu = (JPopupMenu) popupMap.get(name);
 
 		if (menu != null) {
 			Component[] jmenuitems = menu.getComponents();
 
 			for (int i = 0; i < jmenuitems.length; i++) {
 				if (jmenuitems[i] instanceof JMenuItem) {
 					((JMenuItem) jmenuitems[i]).removeActionListener(listener);
 				}
 			}
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param popupName
 	 * @param c DOCUMENT ME!
 	 * @param listener
 	 * @param loader
 	 */
 	public void addPopupMenuListener(String popupName, Component c,
 			ActionListener listener, PluginClassLoader loader) {
 		final String name = getName(popupName, loader);
 
 		JPopupMenu menu = (JPopupMenu) popupMap.get(name);
 
 		if (menu != null) {
 			Component[] jmenuitems = menu.getComponents();
 
 			for (int i = 0; i < jmenuitems.length; i++) {
 				if (jmenuitems[i] instanceof JMenuItem) {
 					((JMenuItem) jmenuitems[i]).addActionListener(listener);
 				}
 			}
 		}
 
 		c.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					showPopupMenu(name, e.getX(), e.getY(), e.getComponent());
 				}
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					showPopupMenu(name, e.getX(), e.getY(), e.getComponent());
 				}
 			}
 		});
 	}
 
 	/**
 	 * Loop on the controls to enable/disable and show/hide them, according to
 	 * its associated extension
 	 *
 	 * @throws RuntimeException DOCUMENT ME!
 	 */
 	public void enableControls() {
 		if (!SwingUtilities.isEventDispatchThread()) {
 			throw new RuntimeException("No Event Dispatch Thread");
 		}
 
 		Iterator e = classesExtensions.values().iterator();
 		HashMap estadoExtensiones = new HashMap();
 		HashMap visibilidadExtensiones = new HashMap();
 
 		while (e.hasNext()) {
 			ExtensionDecorator ext = (ExtensionDecorator) e.next();
 
 			try {
 				if (estadoExtensiones.get(ext) == null) {
 					boolean b;
 					if (ext.getVisibility() == ExtensionDecorator.ALWAYS_VISIBLE)
 						b = true;
 					else if (ext.getVisibility() == ExtensionDecorator.ALWAYS_INVISIBLE)
 						b = false;
 					else {
 						if (PluginServices.getExclusiveUIExtension() == null) {
 							b = ext.isVisible();
 						} else {
 							b = PluginServices.getExclusiveUIExtension().isVisible(ext.getExtension());
 						}
 					}
 					Boolean visible = new Boolean(b);
 					Boolean enabled = new Boolean(false);
 
 					if (visible.booleanValue()) {
 						if (PluginServices.getExclusiveUIExtension() == null) {
 							enabled = new Boolean(ext.isEnabled());
 						}else {
 							enabled = new Boolean(PluginServices.getExclusiveUIExtension().isEnabled(ext.getExtension()));
 						}
 
 					}
 
 					estadoExtensiones.put(ext, enabled);
 					visibilidadExtensiones.put(ext, visible);
 				}
 			} catch (Throwable e1) {
 				NotificationManager.addError(
 						Messages.getString("PluginServices.Bug_en_el_codigo") + ": " + e1.getMessage(),
 						e1);
 				estadoExtensiones.put(ext, Boolean.FALSE);
 			}
 		}
 
 		// Enable or disable controls, according to its associated extensions
 		e = controlClass.keySet().iterator();
 
 		while (e.hasNext()) {
 			JComponent control = (JComponent) e.next();
 
 			try {
 				com.iver.andami.plugins.IExtension ext = (com.iver.andami.plugins.IExtension) classesExtensions.get((Class) controlClass.get(
 						control));
 				boolean enabled = ((Boolean) estadoExtensiones.get(ext)).booleanValue();
 				boolean visible = ((Boolean) visibilidadExtensiones.get(ext)).booleanValue();
 				control.setEnabled(enabled);
 				control.setVisible(visible);
 			} catch (Exception ex) {
 				control.setEnabled(false);
 				control.setVisible(false);
 			}
 		}
 
 		// Loop in the menus to hide the menus that don't have visible children
 		for (int i = 0; i < menuBar.getMenuCount(); i++) {
 			MenuElement menu = menuBar.getMenu(i);
 			hideMenus(menu);
 			if (menu instanceof JMenu) {
 				//hide (ugly) redundant separators and assign keyboard mnemonics
 				Component[] comps = ((JMenu)menu).getMenuComponents();				
 				// mnemonics have to be unique for each top-level menu
 				char mnemonics[] = new char[comps.length];
 				if ( comps.length > 0 ) {
 					// Set keyboard mnemonic for this top-level entry
 					String text = ((JMenu)menu).getText();
 					char mnemonic = getMnemonic(text, mnemonics);		        
 					if (' ' != mnemonic)
 					{
 						((JMenu)menu).setMnemonic(mnemonic);
 						mnemonics[0] = mnemonic;                
 					}
 				}
 				// now go through all entries in this menu, hid
 				// separators if necessary and assing remaining mnemonics
 				hideSeparatorsAndMakeMnemonics(menu, mnemonics);
 			}
 		}
 
 		// hide the toolbars that don't contain any visible tool
 		Iterator it = toolBarMap.values().iterator();
 
 		while (it.hasNext()) {
 			SelectableToolBar t = (SelectableToolBar) it.next();
 			boolean todosOcultos = true;
 
 			for (int i = 0; i < t.getComponentCount(); i++) {
 				if (!(t.getComponent(i) instanceof JSeparator) // separators don't matter
 						&& t.getComponent(i).isVisible()) {
 					todosOcultos = false;
 				}
 			}
 
 			if (todosOcultos) {
 				t.setVisible(false);
 			}
 			else {
 				t.setVisible(t.getAndamiVisibility());
 			}
 		}
 
 		if (mdiManager != null) {
 			JPanel f = (JPanel) mdiManager.getActiveWindow();
 
 			if (f != null) {
 				if (lastLabelClass != f.getClass()) {
 					lastLabelClass = f.getClass();
 
 					Label[] lbls = (Label[]) classLabels.get(lastLabelClass);
 
 					if (lbls != null) {
 						bEstado.setLabelSet(lbls);
 					}
 				}
 			}
 		}
 
 		ajustarToolBar();
 
 		showMemory();
 	}
 
 	/**
 	 * Establece la visibilidad de un menu y todos sus descendientes en la
 	 * jerarquia teniendo en cuenta la visibilidad de todos los submenus.
 	 *
 	 * @param menu Menu que se quiere visualizar
 	 *
 	 * @return Devuelve true si el menu es visible y false en caso contrario
 	 */
 	private boolean hideMenus(MenuElement menu) {
 		MenuElement[] submenus = menu.getSubElements();
 
 		//Si no tiene hijos se devuelve su visibilidad
 		if (submenus.length == 0) {
 			return menu.getComponent().isVisible();
 		}
 
 		/*
 		 * Si tiene hijos se devuelve true si alg�no de ellos es visible,
 		 * pero se itera por todos ellos
 		 */
 		boolean visible = false;
 
 		for (int i = 0; i < submenus.length; i++) {
 			if (hideMenus(submenus[i])) {
 				if (!(menu instanceof JPopupMenu)) {
 					menu.getComponent().setVisible(true);
 				}
 
 				visible = true;
 			}
 		}
 
 		if (visible) {
 			return true;
 		}
 
 		menu.getComponent().setVisible(false);
 
 		return false;
 	}	
 
 	/**
 	 * 
 	 * Recurse through all menu elements and make sure there are no
 	 * redundant separators.
 	 * This method will make sure that a separator only becomes visible
 	 * if at least one visible non-separator menu entry preceeded it.
 	 *	 
 	 **/
 	private void hideSeparatorsAndMakeMnemonics(MenuElement menu, char[] mnemonics ) {
 		// flag that indicates whether a separator is to be displayed or not
 		boolean allowSeparator;
 
 		allowSeparator = false; // separator not allowed as very first menu item
 		Component[] comps = ((JMenu)menu).getMenuComponents();
 		if ( comps.length < 1 ) {
 			//zero-length menu: skip
 			return;
 		}
 
 		for ( int i=0; i < comps.length; i++ ) {				
 			if ( comps[i] instanceof JSeparator ) {
 				// got a separator: display only if allowed at this position
 				if ( allowSeparator == true ) {
 					// look at all successive menu entries to make sure that at least one
 					// is visible and not a separator (otherwise, this separator would
 					// be the last visible item in this menu) -- we don't want that
 					comps[i].setVisible( false );
 					for ( int j = i; j < comps.length; j ++ ) {
 						if ( !(comps[j] instanceof JSeparator)) {
 							if ( comps[j].isVisible() ) {
 								comps[i].setVisible( true ); // display separator!
 								break;
 							}
 						}
 					}						
 				} else {
 					comps[i].setVisible( false );
 				}
 				allowSeparator = false; // separator is not allowed right after another separator
 			} else {					
 				if (comps[i] instanceof JMenu) { // got a submenu: recurse through it
 					// get number of submenu components
 					Component[] scomps = ((JMenu)comps[i]).getMenuComponents();					
 					// make a new, fresh array to hold unique mnemonics for this submenu
 					char[] smnemonics = new char[scomps.length];
 					hideSeparatorsAndMakeMnemonics ( ((MenuElement)comps[i]), smnemonics );
 					if ( comps[i].isVisible() ) {
 						allowSeparator = true; // separators are OK after visible submenus
 						// Set keyboard mnemonic for this submenu
 						String text = ((JMenu)comps[i]).getText();
 						char mnemonic = getMnemonic(text, mnemonics);
 						if (' ' != mnemonic)
 						{
 							((JMenu)comps[i]).setMnemonic(mnemonic);
 							mnemonics[i] = mnemonic;                
 						}							
 					}
 				} else {
 					if ( comps[i].isVisible() ) {							
 						if ( comps[i] instanceof JMenuItem) {
 							// Set keyboard mnemonic for this menu item
 							String text = ((JMenuItem)comps[i]).getText();
 							char mnemonic = getMnemonic(text, mnemonics);
 							if (' ' != mnemonic)
 							{
 								((JMenuItem)comps[i]).setMnemonic(mnemonic);
 
 //								if (((JMenuItem)comps[i]).getAccelerator() == null){								
 //									char[] mnemonicsLower = new char[]{mnemonic};							
 //									((JMenuItem)comps[i]).setAccelerator(KeyStroke.getKeyStroke(KeyMapping.getKey(new String(mnemonicsLower).toLowerCase().charAt(0)), ActionEvent.ALT_MASK));
 //								}
 								mnemonics[i] = mnemonic;                
 							}
 						}
 						allowSeparator = true; // separators are OK after regular, visible entries
 					}
 				}
 			}
 		} 
 	}
 
 
 	/**
 	 * Helper functios for assigning a unique mnemomic char from
 	 * a pool of unassigned onces, stored in the array "mnemomnics"
 	 */
 	private char getMnemonic(String text, char[] mnemonics)
 	{
 		Vector words = new Vector();
 		StringTokenizer t = new StringTokenizer(text);
 		int maxsize = 0;
 
 		while (t.hasMoreTokens())
 		{
 			String word = (String) t.nextToken();
 			if (word.length() > maxsize) maxsize = word.length();
 			words.addElement(word);
 		}
 		words.trimToSize();
 
 		for (int i = 0; i < maxsize; ++i)
 		{
 			char mnemonic = getMnemonic(words, mnemonics, i);
 			if (' ' != mnemonic)
 				return mnemonic;
 		}
 
 		return ' ';
 	}
 
 	private char getMnemonic(Vector words, char[] mnemonics, int index)
 	{
 		int numwords = words.size();
 
 		for (int i = 0; i < numwords; ++i)
 		{
 			String word = (String) words.elementAt(i);
 			if (index >= word.length()) continue;
 
 			char c = word.charAt(index);
 			if (!isMnemonicExists(c, mnemonics)) {
 				/* pick only valid chars */
 				if ( c!=':' && c!='.' && c!=',' && 
 						c!=';' && c!='-' && c!='+' && 
 						c!='/' && c!='\\' && c!='\'' &&
 						c!='\"' && c!=' ' && c!='=' &&
 						c!='(' && c!=')' && c!='[' &&
 						c!=']' && c!='{' && c!='}' &&
 						c!='$' && c!='*' && c!='&' &&
 						c!='%' && c!='!' && c!='?' &&
 						c!='#' && c!='~' && c!='_' ) 
 				{
 					return c;
 				}
 			}
 		}
 		return ' ';
 	}
 
 	private boolean isMnemonicExists(char c, char[] mnemonics)
 	{
 		int num = mnemonics.length;
 		for (int i = 0; i < num; ++i)
 			if (mnemonics[i] == c) return true;
 		return false;
 	} 	
 
 	/**
 	 * Muestra la memoria consumida por el programa
 	 */
 	private void showMemory() {
 		Runtime r = Runtime.getRuntime();
 		long mem = r.totalMemory() - r.freeMemory();
 		logger.debug(PluginServices.getText(this, "memory_usage") + " " + mem/1024 +" KB");
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return
 	 */
 	public MDIManager getMDIManager() {
 		return mdiManager;
 	}
 
 	/**
 	 * Establece el mensaje en la barra de estado asociado a una etiqueta
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public NewStatusBar getStatusBar() {
 		return bEstado;
 	}
 	/**
 	 * You can use this function to select the appropiate
 	 * tool inside the toolbars
 	 */
 	public void setSelectedTool(String actionCommand)
 	{
 		setSelectedTool(defaultGroup, actionCommand);
 	}
 
 	/**
 	 * You can use this function to select the appropiate
 	 * tool inside the toolbars
 	 */
 	public void setSelectedTool(String groupName, String actionCommand)
 	{
 		ButtonGroup group = (ButtonGroup) buttonGroupMap.get(groupName);
 		if (group==null)
 			return;
 
 		Enumeration enumeration = group.getElements();
 		while (enumeration.hasMoreElements()) {
 			AbstractButton button = (AbstractButton) enumeration.nextElement();
 			if (button.getActionCommand().equals(actionCommand)) {
 				button.setSelected(true);
 			}
 		}
 
 		selectedTool.put(groupName, actionCommand);
 	}
 
 	/**
 	 * You can use this function to select the appropiate
 	 * tool inside the toolbars
 	 */
 	public void setSelectedTools(HashMap selectedTools)
 	{
 		selectedTool = selectedTools;
 		if (selectedTools==null) return;
 		Iterator groupNames = selectedTools.keySet().iterator();
 		while (groupNames.hasNext()) {
 			try {
 				String groupName = (String) groupNames.next();
 				ButtonGroup group = (ButtonGroup) buttonGroupMap.get(groupName);
 				Enumeration enumeration = group.getElements();
 				String actionCommand = (String) selectedTools.get(groupName);
 				if (actionCommand==null) continue;
 				while (enumeration.hasMoreElements()) {
 					AbstractButton button = (AbstractButton) enumeration.nextElement();
 					if (button.getActionCommand().equals(actionCommand)) {
 						button.setSelected(true);
 					}
 				}
 			}
 			catch (ClassCastException ex) {
 				logger.error("selectedTool should only contain pairs (String groupName, JToolBarToggleButton button)");
 			}
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param clase
 	 * @param label
 	 */
 	public void setStatusBarLabels(Class clase, Label[] label) {
 		classLabels.put(clase, label);
 	}
 
 	public void removeStatusBarLabels(Class clase) {
 		classLabels.remove(clase);
 	}
 
 	public void addStatusBarControl(Class extensionClass, IControl control) {
 		control.addActionListener(this);
 		bEstado.addControl(control.getName(), (Component)control);
 		controlClass.put(control, extensionClass);
 	}
 
 	public void removeStatusBarControl(String name) {
 		Component c = bEstado.removeControl(name);
 		if (c!=null)
 			controlClass.remove(c);
 	}
 
 	/**
 	 * @see com.iver.andami.ui.mdiFrame.MainFrame#removeMenu(com.iver.andami.plugins.config.generate.Menu)
 	 */
 	public void removeMenu(Menu menu) {
 		JMenuItem delete = (JMenuItem) infoCodedMenus.get(menu);
 
 		if (delete == null) {
 			throw new NoSuchElementException(menu.getText());
 		}
 
 		delete.getParent().remove(delete);
 		infoCodedMenus.remove(menu);
 	}
 
 	/**
 	 * @see com.iver.andami.ui.mdiFrame.MainFrame#addMenu(com.iver.andami.plugins.config.generate.Menu,
 	 * 		java.awt.event.ActionListener, PluginClassLoader)
 	 */
 	public void addMenu(Menu menu, ActionListener listener,
 			PluginClassLoader loader) {
 		JMenu menuPadre = createMenuAncestors(menu, loader);
 
 		//Se registra y a�ade el menu
 		JMenuItem nuevoMenu = createJMenuItem(loader, menu);
 		nuevoMenu.addMouseListener(tooltipListener);
 		nuevoMenu.addActionListener(listener);
 		menuPadre.add(nuevoMenu);
 
 		infoCodedMenus.put(menu, nuevoMenu);
 	}
 
 	/**
 	 * @see com.iver.andami.ui.mdiFrame.MainFrame#changeMenuName(java.lang.String[],
 	 * 		String, com.iver.andami.plugins.PluginClassLoader)
 	 */
 	public void changeMenuName(String[] menu, String newName,
 			PluginClassLoader loader) {
 
 		ArrayList menuList = new ArrayList();
 		for (int i = 0; i < menu.length; i++) {
 			menuList.add(menu[i]);
 		}
 
 		javax.swing.JMenuItem newMenu = getMenu(menuList, menuBar);
 		if (newMenu==null) {
 			throw new NoSuchMenuException(menu[0]);
 		}
 		else {
 			newMenu.setText(PluginServices.getText(this, newName));
 		}
 	}
 
 	/**
 	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
 	 */
 	public void componentHidden(ComponentEvent arg0) {
 	}
 
 	/**
 	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
 	 */
 	public void componentMoved(ComponentEvent arg0) {
 	}
 
 	/**
 	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
 	 */
 	public void componentResized(ComponentEvent arg0) {
 		ajustarToolBar();
 	}
 
 	/**
 	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
 	 */
 	public void componentShown(ComponentEvent arg0) {
 	}
 
 	/**
 	 * @see java.awt.event.ContainerListener#componentAdded(java.awt.event.ContainerEvent)
 	 */
 	public void componentAdded(ContainerEvent arg0) {
 		ajustarToolBar();
 	}
 
 	/**
 	 * @see java.awt.event.ContainerListener#componentRemoved(java.awt.event.ContainerEvent)
 	 */
 	public void componentRemoved(ContainerEvent arg0) {
 		ajustarToolBar();
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @author $author$
 	 * @version $Revision: 35160 $
 	 */
 	public class TooltipListener extends MouseAdapter {
 		/**
 		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
 		 */
 		public void mouseEntered(MouseEvent e) {
 			JComponent control = (JComponent) e.getSource();
 			EnableTextSupport ets = (EnableTextSupport) e.getSource();
 
 			String texto = null;
 			texto = control.getToolTipText();
 
 			if (texto != null) {
 				bEstado.setInfoTextTemporal(texto);
 			}
 		}
 
 		/**
 		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
 		 */
 		public void mouseExited(MouseEvent arg0) {
 			bEstado.restaurarTexto();
 		}
 
 		/**
 		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
 		 */
 		public void mousePressed(MouseEvent e) {
 			bEstado.restaurarTexto();
 		}
 	}
 	public String getTitlePrefix() {
 		return titlePrefix;
 	}
 	public void setTitlePrefix(String titlePrefix) {
 		this.titlePrefix = titlePrefix;
 	}
 
 	public HashMap getSelectedTools() {
 		return selectedTool;
 	}
 
 	public HashMap getInitialSelectedTools() {
 		return initialSelectedTools;
 	}
 
 
 	/**
 	 * Get a previously added JComponent by name. For example
 	 * you can use it if you need to obtain a JToolBar to
 	 * add some customized component.
 	 * @param name
 	 * @return the JComponent or null if none has been found
 	 */
 	public JComponent getComponentByName(String name)
 	{
 		Iterator e = controlClass.keySet().iterator();
 
 		while (e.hasNext()) {
 			JComponent control = (JComponent) e.next();
 			String nameCtrl = control.getName();
 			if (nameCtrl != null)
 			{
 				if (nameCtrl.compareTo(name) == 0)
 					return control;
 			}
 		}
 		Iterator it = toolBarMap.values().iterator();
 		while (it.hasNext()) {
 			SelectableToolBar t = (SelectableToolBar) it.next();
 			String nameCtrl = t.getName();
 			if (nameCtrl != null)
 				if (nameCtrl.compareTo(name) == 0)
 					return t;
 
 		}
 
 		return null;
 	}
 
 	public SelectableToolBar[] getToolbars() {
 		return (SelectableToolBar[]) toolBarMap.values().toArray(new SelectableToolBar[0]);
 	}
 
 	public boolean getToolbarVisibility(String name) {
 		JComponent component = PluginServices.getMainFrame().getComponentByName(name);
 		if (component!=null && component instanceof SelectableToolBar) {
 			SelectableToolBar toolBar = (SelectableToolBar) component;
 			return toolBar.getAndamiVisibility();
 		}
 		return false;
 	}
 
 	public boolean setToolbarVisibility(String name, boolean visibility) {
 		JComponent component = PluginServices.getMainFrame().getComponentByName(name);
 		if (component!=null && component instanceof SelectableToolBar) {
 			SelectableToolBar toolBar = (SelectableToolBar) component;
 			boolean oldVisibility = toolBar.getAndamiVisibility();
 			toolBar.setAndamiVisibility(visibility);
 			enableControls();
 			return oldVisibility;
 		}
 		return false;
 	}
 
 	public javax.swing.JMenuItem getMenuEntry(String[] menuPath) {
 		ArrayList menu = new ArrayList();
 		for (int i=0; i<menuPath.length; i++)
 			menu.add(menuPath[i]);
 		return getMenu(menu, menuBar);
 	}
 }

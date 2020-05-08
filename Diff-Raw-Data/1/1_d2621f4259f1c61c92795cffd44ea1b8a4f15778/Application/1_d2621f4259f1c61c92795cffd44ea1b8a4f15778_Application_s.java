 /*
  * Project: xdccBee
  * Copyright (C) 2009 snert@snert-lab.de,
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.snertlab.xdccBee.ui;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.StatusLineManager;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tray;
 import org.eclipse.swt.widgets.TrayItem;
 
 import de.snertlab.xdccBee.irc.IrcServer;
 import de.snertlab.xdccBee.irc.ServerList;
 import de.snertlab.xdccBee.messages.XdccBeeMessages;
 import de.snertlab.xdccBee.settings.ServerSettings;
 import de.snertlab.xdccBee.settings.Settings;
 import de.snertlab.xdccBee.tools.CocoaUIEnhancer;
 import de.snertlab.xdccBee.ui.actions.ActionAbout;
 import de.snertlab.xdccBee.ui.actions.ActionPreferences;
 import de.snertlab.xdccBee.ui.actions.ActionQuit;
 
 /**
  * @author snert
  *
  */
 public class Application extends ApplicationWindow {
 	
 	public static final String VERSION_STRING = readVersionNrFromProperties();
 	private static Application window;
 	private ViewMain viewMain;
 	public static void main(String args[]) {
 		try {
 			//INFO: Unter MacOsx muss jar wie folgt gestartet werden: java -XstartOnFirstThread -jar
 			window = new Application();
 			window.setBlockOnOpen(true);
 			window.open();
 			if(Display.getCurrent() != null && ! Display.getCurrent().isDisposed()){
 				Display.getCurrent().dispose();
 			}
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	public static Application getWindow() {
 		return window;
 	}
 	public static boolean isMac() {
 		if ( System.getProperty( "os.name" ).equals( "Mac OS X" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
             return true;
         }
         return false;
     }
 
 	public Application() {
 		super(null);
 		addToolBar(SWT.FLAT | SWT.WRAP);
 		addMenuBar();
 		addStatusLine();
 	}
 
 	@Override
 	protected Control createContents(Composite parent) {
 		if(isMac()) macMenu(); //Geht leider nicht in addMenuBar da shell noch nich da ist
 		Composite container = new Composite(parent, SWT.NONE);
 		container.setLayout(new FormLayout());
 		viewMain = new ViewMain();
 		viewMain.createContents(container);
 		if( ! isMac() ){
 			makeTray(); //TODO: Tray for mac?!	
 		}		
 		return container;
 	}
 	
 	private void macMenu(){
 		try {
 			Listener listener = new Listener() {
 				@Override
 				public void handleEvent(Event event) {
 					new ActionQuit(false).run();
 				}
 			};
 			CocoaUIEnhancer enhancer = new CocoaUIEnhancer( XdccBeeMessages.getString("Application_TITLE") ); //$NON-NLS-1$
 	        enhancer.hookApplicationMenu( getShell().getDisplay(), listener, new ActionAbout(getShell()), new ActionPreferences(getShell()));
 
 		} catch (Throwable e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private void winMenu(MenuManager menuManager) {
 		MenuManager fileMenu = new MenuManager(XdccBeeMessages.getString("Application_MENU_FILE")); //$NON-NLS-1$
 		MenuManager helpMenu = new MenuManager(XdccBeeMessages.getString("Application_MENU_HELP")); //$NON-NLS-1$
 		menuManager.add( fileMenu );
 		menuManager.add( helpMenu );
 		fileMenu.add( new ActionPreferences(getShell()) );
 		fileMenu.add( new ActionQuit(true) );
 		
 		helpMenu.add( new ActionAbout(getShell()) );
 	}
 
 	@Override
 	protected MenuManager createMenuManager() {
 		MenuManager menuManager = new MenuManager("menu"); //$NON-NLS-1$
 		if( ! isMac() ){
 			winMenu(menuManager);
 		}
 		return menuManager;
 	}
 
 	@Override
 	protected ToolBarManager createToolBarManager(int style) {
 		ToolBarManager toolBarManager = new ToolBarManager(style);
 		return toolBarManager;
 	}
 
 	@Override
 	protected StatusLineManager createStatusLineManager() {
 		StatusLineManager statusLineManager = new StatusLineManager();
 		return statusLineManager;
 	}
 
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		/*
 		 * Provide different resolutions for icons to get high quality rendering
 		 * wherever the OS needs large icons. For example, the ALT+TAB window on
 		 * certain systems uses a larger icon.
 		 */
 		newShell.setImages(new Image[] { Images.WINDOW_ICON_SMALL, Images.WINDOW_ICON_LARGE, Images.WINDOW_ICON_XLARGE, Images.WINDOW_ICON_XXLARGE});
 
 		newShell.setText(XdccBeeMessages.getString("Application_TITLE")); //$NON-NLS-1$
 		newShell.setSize(getSettings().getMainWindowSize());
 		if(getSettings().getMainWindowPosition().x !=0 || getSettings().getMainWindowPosition().y !=0 ){
 			newShell.setLocation(getSettings().getMainWindowPosition());
 		}else{
 			centerShell(newShell);
 		}
 		newShell.addDisposeListener( new DisposeListener() {
 			@Override
 			public void widgetDisposed(DisposeEvent e) {
 				new ActionQuit(false).run();
 			}
 		});
 		if(newShell.getDisplay().getSystemTray()!=null && ! isMac()){ //TODO: Tray for mac?! shellIconified doesnt work under mac
 			newShell.addShellListener(new ShellAdapter() {
 			    public void shellIconified(ShellEvent e) {
 			    	window.getShell().setVisible(false);
 			    }
 			  });			
 		}
 	}
 	
 	private void centerShell(Shell newShell) {
 		Rectangle pDisplayBounds = newShell.getDisplay().getBounds();
 		int nLeft = (pDisplayBounds.width - getInitialSize().x) / 2;
 		int nTop = (pDisplayBounds.height - getInitialSize().y) / 2;	                
 		newShell.setBounds(nLeft, nTop, getInitialSize().x, getInitialSize().y);
 	}
 	
 	public static void placeDialogInCenter(Shell parent, Shell shell){
 		Rectangle parentSize = parent.getBounds();
 		Rectangle mySize = shell.getBounds();
 		int locationX, locationY;
 		locationX = (parentSize.width - mySize.width)/2+parentSize.x;
 		locationY = (parentSize.height - mySize.height)/2+parentSize.y;
 		shell.setLocation(new Point(locationX, locationY));
 	}
 
 	@Override
 	protected Point getInitialSize() {
 		return getSettings().getMainWindowSize();
 	}
 
 	
 	public static Settings getSettings(){
 		return Settings.getInstance();
 	}
 	
 	public static ServerSettings getServerSettings(){
 		return ServerSettings.getInstance();
 	}
 	public ViewMain getViewMain() {
 		return viewMain;
 	}
 	private static String readVersionNrFromProperties(){
 		Properties prop = loadVersionProperties();
 		String version = ""; //$NON-NLS-1$
 		String major = (String)prop.get("build.major.number"); //$NON-NLS-1$
 		String minor = (String)prop.get("build.minor.number"); //$NON-NLS-1$
 		String patch = (String)prop.get("build.patch.number"); //$NON-NLS-1$
 		String revision = (String)prop.get("build.revision.number"); //$NON-NLS-1$
 		
 		version = "Version " + major + "." + minor + "." + patch + " Build(" + revision + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		return version;
 	}
 	
 	private static Properties loadVersionProperties(){
 	    try {
 	    	Properties properties = new Properties();
 	        properties.load(Application.class.getResourceAsStream("version.properties")); //$NON-NLS-1$
 	        return properties;
 	    } catch (IOException e) {
 	    	throw new RuntimeException(e);
 	    }
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.window.ApplicationWindow#close()
 	 */
 	@Override
 	public boolean close() {
 		List<IrcServer> listIrcServer = ServerList.getListConnectedServer();
 		for (IrcServer ircServer : listIrcServer) {
 			ircServer.disconnect();
 		}
 		return super.close();
 	}
 	
 	private void makeTray(){
 		Display display = window.getShell().getDisplay();
 		final Tray tray = display.getSystemTray();
 		if (tray == null) {
 			//no tray
 		} else {
 			final TrayItem item = new TrayItem (tray, SWT.NONE);
 			item.setToolTipText(XdccBeeMessages.getString("Application_TITLE"));
 			final Menu menu = new Menu (window.getShell(), SWT.POP_UP);
 			MenuItem mi = new MenuItem (menu, SWT.PUSH);
 			mi.setText (XdccBeeMessages.getString("Application_TRAY_OPEN"));
 			mi.addListener (SWT.Selection, new Listener () {
 				public void handleEvent (Event event) {
 					window.getShell().setVisible(true);
 					window.getShell().setMinimized(false);
 				}
 			});
 			menu.setDefaultItem(mi);
 			item.addListener (SWT.MenuDetect, new Listener () {
 				public void handleEvent (Event event) {
 					menu.setVisible (true);
 				}
 			});
 			item.addListener (SWT.Selection, new Listener () {
 				public void handleEvent (Event event) {
 					menu.setVisible (true);
 				}
 			});
 			item.setImage (Images.WINDOW_ICON_LARGE);
 		}
 	}
 	
 
 }

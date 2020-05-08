 /*
     Pairs, a concentration game with modular card packages.
     Copyright © 2012  Alexander Klauer
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package pairs.ui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 
 import java.util.prefs.Preferences;
 
 import javax.swing.*;
 
 import org.apache.log4j.Logger;
 
 import pairs.util.SVGResource;
 
 import static pairs.util.Message._;
 
 /**
  * Main window.
  */
 public class MainWindow extends JFrame implements ComponentListener, WindowStateListener {
 	/**
 	 * Logger for this class.
 	 */
 	private static final Logger logger = Logger.getLogger( MainWindow.class );
 
 	/**
 	 * Minimum window width.
 	 */
 	private static final int MIN_WIDTH = 600;
 
 	/**
 	 * Minimum window height.
 	 */
 	private static final int MIN_HEIGHT = 400;
 
 	/**
 	 * Preferences for this class.
 	 */
 	private static final Preferences preferences = Preferences.userNodeForPackage( MainWindow.class );
 
 	/**
 	 * Window width preference key.
 	 */
 	private static final String PREF_WIDTH = "mainWindowWidth";
 
 	/**
 	 * Window height preference key.
 	 */
 	private static final String PREF_HEIGHT = "mainWindowHeight";
 
 	/**
 	 * Window state preference key.
 	 */
 	private static final String PREF_STATE = "mainWindowState";
 
 	/**
 	 * Displays main window.
 	 */
 	public MainWindow() {
 		super( _( "label-concentration" ) );
 
 		/* Program icon */
 		try {
 			SVGResource resource = new SVGResource( "images/Crystal_icons.svg" );
 			setIconImage( resource.createImage( 64, 64 ) );
 		} catch ( Exception e ) {
			logger.error( _( "error-icon" ), e );
 		}
 
 		/* Default close operation */
 		setDefaultCloseOperation( EXIT_ON_CLOSE );
 
 		/* Menu */
 		JMenu fileMenu = new JMenu( new MenuAction( "menu-file" ) );
 		fileMenu.add( new MenuItemAction( "menu-exit" ) {
 			public void actionPerformed( ActionEvent e ) {
 				MainWindow.this.dispose();
 			}
 		} );
 		JMenu helpMenu = new JMenu( new MenuAction( "menu-help" ) );
 		helpMenu.add( new MenuItemAction( "menu-about" ) {
 			public void actionPerformed( ActionEvent e ) {
 				new AboutBox( MainWindow.this );
 			}
 		} );
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add( fileMenu );
 		menuBar.add( helpMenu );
 		setJMenuBar( menuBar );
 
 		/* Window sizing */
 		addComponentListener( this );
 		addWindowStateListener( this );
 		setSize( preferences.getInt( PREF_WIDTH, MIN_WIDTH ), preferences.getInt( PREF_HEIGHT, MIN_HEIGHT ) );
 		setExtendedState( preferences.getInt( PREF_STATE, NORMAL ) );
 		validate();
 		setLocationRelativeTo( null );
 		setVisible( true );
 	}
 
 	public void componentHidden( ComponentEvent e ) {
 	}
 
 	public void componentMoved( ComponentEvent e ) {
 	}
 
 	public void componentResized( ComponentEvent e ) {
 		int width = getWidth();
 		int height = getHeight();
 		if ( ( width < MIN_WIDTH ) || ( height < MIN_HEIGHT ) ) {
 			width = Math.max( width, MIN_WIDTH );
 			height = Math.max( height, MIN_HEIGHT );
 			setSize( width, height );
 		}
 		preferences.putInt( PREF_WIDTH, width );
 		preferences.putInt( PREF_HEIGHT, height );
 	}
 
 	public void componentShown( ComponentEvent e ) {
 	}
 
 	public void windowStateChanged( WindowEvent e ) {
 		preferences.putInt( PREF_STATE, e.getNewState() );
 	}
 }

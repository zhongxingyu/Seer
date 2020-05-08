 /*
  * ManagerListener.java		0.1		20/06/2006
  * 
  * Copyright (C) 2006 Thomas Chemineau
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
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 
 package net.aepik.casl.ui;
 
 import net.aepik.casl.core.Manager;
 import net.aepik.casl.ui.ldap.SchemaManagerListener;
 import net.aepik.casl.ui.ldap.SchemaManagerPanel;
 
 import org.jdesktop.jdic.desktop.Desktop;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.net.URL;
 import javax.swing.JMenuItem;
 
 /**
  * Cette classe écoute tous les événements qui interviennent sur
  * le modèle et sur la vue. C'est lui qui gère l'intéraction entre
  * la vue du manager des schémas et le manager lui-même.
 **/
 
 public class ManagerListener
 		implements
 			ActionListener,
 			MouseListener,
 			WindowListener {
 
 ////////////////////////////////
 // Attributs
 ////////////////////////////////
 
 	/** La fenêtre **/
 	private ManagerFrame managerFrame ;
 
 ////////////////////////////////
 // Constructeurs
 ////////////////////////////////
 
 	public ManagerListener( ManagerFrame mf ) {
 		managerFrame = mf ;
 	}
 
 ////////////////////////////////
 // Méthodes publiques
 ////////////////////////////////
 
 	/**
 	 * Gère les actions basique de la fenêtre.
 	 * @param e Un événement.
 	**/
 	public void actionPerformed( ActionEvent e ) {
 
 		Object o = e.getSource();
 
 		// On ferme la fenêtre.
 		if( o==managerFrame.item_quit ) {
 			windowClosing( null );
 
 		// On souhaite afficher la liste des plugins
 		} else if( o==managerFrame.item_plugins ) {
 
 			PluginManagerFrame f = new PluginManagerFrame(
 					managerFrame,
 					managerFrame.getManager().getPluginManager() );
 			f.setVisible( true );
 
 		// On demande l'aide du logiciel.
 		// On va ouvrir un naviguateur web avec une URL définie.
 		} else if( o==managerFrame.item_help ) {
 
 			try {
 				String currentDir = System.getProperty( "user.dir" );
				Desktop.browse( new URL( "file://" + currentDir + "/doc/index.html" ) );
 			} catch( Exception ex ) {
 				System.out.println( ex );
 			}
 
 		// On affiche les crédits.
 		} else if( o==managerFrame.item_authors ) {
 			new CreditsFrameLauncher( managerFrame, managerFrame.getManager() );
 		}
 	}
 
 	/**
 	 * Gère la description dans la barre de status.
 	 * @param e L'événement de la souris.
 	**/
 	public void mouseDescription( MouseEvent e ) {
 
 		Object o = e.getSource();
 
 		if( managerFrame!=null ) {
 
 			if( o instanceof JMenuItem && e.getComponent().isEnabled() ) {
 				managerFrame.setStatusDescription( ((JMenuItem) o).getText() );
 			} else {
 				managerFrame.setStatusDescription( null );
 			}
 		}
 	}
 
 	// Les méthodes liées à la souris.
 	public void mouseClicked( MouseEvent e ) {}
 	public void mouseEntered( MouseEvent e ) { mouseDescription( e ); }
 	public void mouseExited( MouseEvent e ) { mouseDescription( e ); }
 	public void mousePressed( MouseEvent e ) {}
 	public void mouseReleased( MouseEvent e ) {}
 
 	// Les méthode liées à la fenêtre.
 	public void windowActivated( WindowEvent e ) {}
 	public void windowClosed( WindowEvent e ) {}
  	public void windowClosing( WindowEvent e ) {
  		managerFrame.setVisible( false );
  		System.exit(0);
  	}
 	public void windowDeactivated( WindowEvent e ) {}
 	public void windowDeiconified( WindowEvent e ) {}
 	public void windowIconified( WindowEvent e ) {}
 	public void windowOpened( WindowEvent e ) {}
 
}

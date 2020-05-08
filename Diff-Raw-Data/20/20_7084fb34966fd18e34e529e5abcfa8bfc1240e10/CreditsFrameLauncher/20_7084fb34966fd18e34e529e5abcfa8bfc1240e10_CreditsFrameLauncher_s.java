 /*
  * CreditsFrameLauncher.java		0.2		10/07/2006
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

 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 
 public class CreditsFrameLauncher extends JDialog implements ActionListener {
 
 	private JButton closeButton;
 	private CreditsFrame creditsFrame ;
 
 	public CreditsFrameLauncher( ManagerFrame f, Manager m ) {
 
 		closeButton = new JButton( "Fermer" );
 		closeButton.addActionListener( this );
 
 		JPanel p = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
 		p.add( closeButton );
 
 		creditsFrame = new CreditsFrame( f, m, p, true );
 		creditsFrame.setModal( true );
 		creditsFrame.setVisible( true );
 	}
 
 	public void actionPerformed( ActionEvent e ) {
 
 		Object o = e.getSource();
 
 		if( o==closeButton )
 			creditsFrame.dispose();
 	}
 }

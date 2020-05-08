 /*
 * Copyright (C) 2006-2010 Thomas Chemineau
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
 import net.aepik.casl.core.util.Config;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Toolkit;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 public class CreditsFrame extends JDialog {
 
 	public CreditsFrame( JFrame p, Manager m, JComponent footerComponent, boolean licence ) {
 
 		super();
 		setSize( 400, 350 );
 		setLocationRelativeTo( p );
 		setTitle( "A propos..." );
 
 		footerComponent.setOpaque( false );
 
 		// - Le Logo du header -
 
 		Image imageTmp = Toolkit.getDefaultToolkit().getImage(Config.getResourcesPath() +  "/casl.png" );
 		ImageIcon image = new ImageIcon( imageTmp );
 		image = new ImageIcon( imageTmp.getScaledInstance(
 				image.getIconWidth()*3/4, image.getIconHeight()*3/4, Image.SCALE_SMOOTH ) );
 		JLabel imageLabel = new JLabel();
 		imageLabel.setPreferredSize(
 				new Dimension( image.getIconWidth()+10, image.getIconHeight() ) );
 		imageLabel.setIcon( image );
 		imageLabel.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 0 ) );
 
 		// - Les textes du header -
 
 		Font texteGras = new Font( (new JLabel()).getFont().getName(), Font.BOLD, 14 );
 		JLabel titreLabel = new JLabel( "CASL" );
 		titreLabel.setFont( texteGras );
		//titreLabel.setForeground( Color.red );
 
 		JTextArea descriptionLabel = new JTextArea(
 				"Version : " + m.getProperty( "Version" ) + "\n"
 				+ "Auteur : Thomas Chemineau\n" );
 		descriptionLabel.setBorder( BorderFactory.createEmptyBorder( 10, 0, 0, 0 ) );
 		descriptionLabel.setEditable( false );
 		descriptionLabel.setFont( (new JLabel()).getFont() );
 		descriptionLabel.setOpaque( false );
 		descriptionLabel.setLineWrap( true );
 		descriptionLabel.setWrapStyleWord( true );
 
 		// - Panel du bas -
 
 		JPanel footerPanel = new JPanel( new BorderLayout() );
 		footerPanel.add( footerComponent );
 		footerPanel.setBorder( BorderFactory.createMatteBorder( 1, 0, 0, 0, Color.lightGray ) );
 		footerPanel.setBackground( new Color( 236, 232, 224 ) );
 
 		// - TextArea de la licence -
 
 		JScrollPane licenceScroller = null ;
 
 		if( licence ) {
 			JTextArea licenceLabel = new JTextArea(
 				      "CASL version " + m.getProperty( "Version" ) + "\n"
 					+ "Copyright (C) 2006-2010 Thomas Chemineau\n\n"
 					+ "This program is free software; you can redistribute it and/or "
 					+ "modify it under the terms of the GNU General Public License "
 					+ "as published by the Free Software Foundation; either version 2 "
 					+ "of the License, or any later version.\n\n"
 					+ "This program is distributed in the hope that it will be useful, "
 					+ "but WITHOUT ANY WARRANTY; without even the implied warranty of "
 					+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the "
 					+ "GNU General Public License for more details.\n\n"
 					+ "You should have received a copy of the GNU General Public License "
 					+ "along with this program; if not, write to the Free Software "
 					+ "Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA." );
 			licenceLabel.setBackground( Color.white );
 			licenceLabel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
 			licenceLabel.setFont( (new JLabel()).getFont() );
 			licenceLabel.setEditable( false );
 			licenceLabel.setOpaque( true );
 			licenceLabel.setLineWrap( true );
 			licenceLabel.setWrapStyleWord( true );
 	
 			licenceScroller = new JScrollPane( licenceLabel );
			licenceScroller.setBorder(BorderFactory.createEmptyBorder( 10, 6, 6, 6 ));
 			licenceScroller.setOpaque( false );
 		}
 
 		// - Organisation graphique -
 
 		JPanel textePanel = new JPanel( new BorderLayout() );
 		textePanel.add( titreLabel, BorderLayout.NORTH );
 		textePanel.add( descriptionLabel, BorderLayout.CENTER );
 		textePanel.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 0 ) );
 		textePanel.setOpaque( false );
 
 		JPanel textePanelContainer = new JPanel( new BorderLayout() );
 		textePanelContainer.add( textePanel, BorderLayout.SOUTH );
 		textePanelContainer.setOpaque( false );
 
 		JPanel headerPanel = new JPanel( new BorderLayout() );
 		headerPanel.add( imageLabel, BorderLayout.WEST );
 		headerPanel.add( textePanelContainer, BorderLayout.CENTER );
 		headerPanel.setBorder( BorderFactory.createEmptyBorder( 10, 0, 0, 0 ) );
 		headerPanel.setOpaque( false );
 
 		JPanel mainPanel = new JPanel( new BorderLayout() );
 		mainPanel.add( headerPanel, BorderLayout.NORTH );
 		mainPanel.add( footerPanel, BorderLayout.SOUTH );
 		mainPanel.setBackground( Color.white );
 
 		if( licence )
 			mainPanel.add( licenceScroller, BorderLayout.CENTER );
 
 		add( mainPanel );
 	}
 }

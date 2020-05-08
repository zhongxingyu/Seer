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
 
 import javax.swing.*;
 
 import org.apache.log4j.Logger;
 
 import pairs.util.Resources;
 
 import static pairs.util.Message._;
 
 /**
  * Box displaying text from a resource.
  */
 class TextDisplayBox extends JDialog {
 	/**
 	 * Logger for this class.
 	 */
 	private static final Logger logger = Logger.getLogger( TextDisplayBox.class );
 
 	/**
 	 * Creates a new text display box.
 	 *
 	 * @param owner Owner of the display box.
 	 * @param titleKey Title key for the dialog title.
 	 * @param resourceName Resource name for the text to be displayed.
 	 */
 	TextDisplayBox( Dialog owner, String titleKey, String resourceName ) {
 		super( owner, _( titleKey ) );
 
 		/* Dispose on close */
 		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
 
 		/* Load resource */
 		String text;
 		try {
 			text = Resources.loadResourceAsString( resourceName );
 		} catch ( Exception e ) {
 			String errorMsg = _( "error-loadingtext", resourceName );
 			logger.error( errorMsg, e );
 			JOptionPane.showMessageDialog( this, errorMsg, _( "error" ), JOptionPane.ERROR_MESSAGE );
 			dispose();
 			return;
 		}
 
 		/* Layout */
 		setLayout( new BorderLayout() );
 		JTextArea textArea = new JTextArea( text, 20, 0 );
 		textArea.setEditable( false );
 		JScrollPane scrollPane = new JScrollPane( textArea );
		add( scrollPane, BorderLayout.CENTER );
 		add( new JButton( new ButtonAction( "button-close" ) {
 			public void actionPerformed( ActionEvent e ) {
 				TextDisplayBox.this.dispose();
 			}
 		} ), BorderLayout.SOUTH );
 
 		/* Display */
 		pack();
 		setLocationRelativeTo( null );
 		setVisible( true );
 	}
 }	

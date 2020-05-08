 package es.eucm.eadventure.editor.control.controllers.imageedition;
 
 import java.awt.Cursor;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JSpinner;
 import javax.swing.JToggleButton;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.controllers.imageedition.filter.TransparentColorFilter;
 import es.eucm.eadventure.editor.gui.displaydialogs.GraphicDialog;
 import es.eucm.eadventure.editor.gui.displaydialogs.GraphicDialog.ImagePanel;
 
 public class ImageToolBar extends JPanel {
 
     private static final long serialVersionUID = -87717263659816496L;
 
     private TransparentColorFilter filter;
 
     private EditImageController controller;
 
     private ArrayList<JComponent> tools;
 
     private GraphicDialog.ImagePanel panel;
 
     public ImageToolBar( boolean admitTransparency, TransparentColorFilter filter, EditImageController cont, ImagePanel p ) {
 
         this.panel = p;
         this.filter = filter;
         this.controller = cont;
         this.setLayout( new GridBagLayout( ) );
 
         tools = new ArrayList<JComponent>( );
 
         GridBagConstraints c = new GridBagConstraints( );
         c.insets = new Insets( 5, 5, 5, 5 );
         c.fill = GridBagConstraints.BOTH;
         c.gridx = 0;
         c.gridy = 0;
 
         JToggleButton editButton = new JToggleButton( TC.get( "ImageEdition.SelectColorTransparent" ) );
         editButton.setEnabled( admitTransparency );
         this.add( editButton, c );
 
         if( admitTransparency ) {
             editButton.addActionListener( new ActionListener( ) {
 
                 public void actionPerformed( ActionEvent e ) {
 
                     boolean selected = ( (JToggleButton) ( e.getSource( ) ) ).isSelected( );
                    
                    ImageToolBar.this.filter.setActive( selected );
 
                     for( JComponent c : tools ) {
                         c.setEnabled( selected );
                     }
 
                     if( selected ) {
                         panel.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
                     }
                     else
                         panel.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
 
                 }
 
             } );
 
             c.gridx++;
             this.add( new JSeparator( JSeparator.VERTICAL ), c );
 
             JButton undoButton = new JButton( new ImageIcon( "img/icons/undo.png" ) );
             undoButton.setToolTipText( TC.get( "Tools.Undo" ) );
             undoButton.addActionListener( new ActionListener( ) {
 
                 public void actionPerformed( ActionEvent e ) {
 
                     controller.undo( );
 
                 }
 
             } );
             tools.add( undoButton );
 
             c.gridx++;
             this.add( undoButton, c );
 
             JButton redoButton = new JButton( new ImageIcon( "img/icons/redo.png" ) );
             redoButton.setToolTipText( TC.get( "Tools.Redo" ) );
             redoButton.addActionListener( new ActionListener( ) {
 
                 public void actionPerformed( ActionEvent e ) {
 
                     controller.redo( );
 
                 }
 
             } );
 
             c.gridx++;
             this.add( redoButton, c );
             tools.add( redoButton );
 
             c.gridx++;
             this.add( new JSeparator( JSeparator.VERTICAL ), c );
 
             JCheckBox contiguous = new JCheckBox( TC.get( "ImageEdition.Contiguous" ) );
             contiguous.setSelected( filter.isContiguous( ) );
             contiguous.addActionListener( new ActionListener( ) {
 
                 public void actionPerformed( ActionEvent arg ) {
 
                     ImageToolBar.this.filter.setContiguous( !ImageToolBar.this.filter.isContiguous( ) );
                 }
 
             } );
             c.gridx++;
             this.add( contiguous, c );
             tools.add( contiguous );
 
             c.gridx++;
             this.add( new JSeparator( JSeparator.VERTICAL ), c );
 
             c.gridx++;
             this.add( new JLabel( TC.get( "ImageEdition.Threshold" ) ), c );
 
             JSpinner s = new JSpinner( new SpinnerNumberModel( filter.getThreshold( ), 10, 255, 1 ) );
             s.addChangeListener( new ChangeListener( ) {
 
                 public void stateChanged( ChangeEvent arg0 ) {
 
                     ImageToolBar.this.filter.setThreshold( (Integer) ( (JSpinner) arg0.getSource( ) ).getValue( ) );
 
                 }
 
             } );
 
             c.gridx++;
             this.add( s, c );
             tools.add( s );
 
             editButton.setSelected( false );
 
             for( JComponent com : tools ) {
                 com.setEnabled( false );
             }
         }
         else {
             c.gridx++;
             this.add( new JLabel( TC.get( "ImageEdition.NoTransparencyAllowed" ) ), c );
         }
 
     }
 
 }

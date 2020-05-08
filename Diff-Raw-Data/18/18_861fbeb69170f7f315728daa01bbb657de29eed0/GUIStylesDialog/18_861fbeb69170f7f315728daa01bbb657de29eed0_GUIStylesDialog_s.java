 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package es.eucm.eadventure.editor.gui.editdialogs;
 
 import java.awt.BorderLayout;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextPane;
 
 import es.eucm.eadventure.common.data.adventure.DescriptorData;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 
 /**
  * Dialog to let the user choose between the different styles of GUIs for the
  * adventure.
  * 
  * @author Bruno Torijano Bueno
  */
 public class GUIStylesDialog extends JDialog {
 
     /**
      * Required.
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Stores the option selected for the style of the GUI;
      */
     private int optionSelected;
 
     /**
      * Radio button for the "Traditional" option.
      */
     private JRadioButton traditionalRadioButton;
 
     /**
      * Radio button for the "Contextual" option.
      */
     private JRadioButton contextualRadioButton;
 
     /**
      * Constructor.
      * 
      * @param optionSelected
      *            GUI style active
      */
     public GUIStylesDialog( int optionSelected ) {
 
         // Set the values
         super( Controller.getInstance( ).peekWindow( ), TC.get( "GUIStyles.Title" ), Dialog.ModalityType.APPLICATION_MODAL );
         this.optionSelected = optionSelected;
 
         // Panel with the report options
         JPanel guiStylesPanel = new JPanel( );
         guiStylesPanel.setLayout( new GridBagLayout( ) );
         guiStylesPanel.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "GUIStyles.Title" ) ) );
 
         // Traditional radio button
         GridBagConstraints c = new GridBagConstraints( );
         c.insets = new Insets( 10, 10, 3, 10 );
         c.gridx = 0;
         c.gridy = 0;
         c.anchor = GridBagConstraints.LINE_START;
         c.fill = GridBagConstraints.NONE;
         traditionalRadioButton = new JRadioButton( TC.get( "GUIStyles.Traditional" ) );
         guiStylesPanel.add( traditionalRadioButton, c );
 
         // Traditional description
         c.insets = new Insets( 0, 10, 10, 10 );
         c.gridy = 1;
         c.weightx = 1;
         c.fill = GridBagConstraints.BOTH;
         c.anchor = GridBagConstraints.CENTER;
         JTextPane xmlReportInfo = new JTextPane( );
         xmlReportInfo.setEditable( false );
         xmlReportInfo.setBackground( getContentPane( ).getBackground( ) );
         xmlReportInfo.setText( TC.get( "GUIStyles.TraditionalDescription" ) );
         guiStylesPanel.add( xmlReportInfo, c );
 
         // Contextual radio button
         c.insets = new Insets( 10, 10, 3, 10 );
         c.gridy = 2;
         c.fill = GridBagConstraints.NONE;
         c.anchor = GridBagConstraints.LINE_START;
         contextualRadioButton = new JRadioButton( TC.get( "GUIStyles.Contextual" ) );
         contextualRadioButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
             }
         } );
         guiStylesPanel.add( contextualRadioButton, c );
 
         // Contextual description
         c.insets = new Insets( 0, 10, 10, 10 );
         c.gridx = 0;
         c.gridy = 3;
         c.weightx = 1;
         c.fill = GridBagConstraints.BOTH;
         c.anchor = GridBagConstraints.CENTER;
         JTextPane htmlReportInfo = new JTextPane( );
         htmlReportInfo.setEditable( false );
         htmlReportInfo.setBackground( getContentPane( ).getBackground( ) );
         htmlReportInfo.setText( TC.get( "GUIStyles.ContextualDescription" ) );
         guiStylesPanel.add( htmlReportInfo, c );
 
         // Panel with the buttons
         JPanel buttonsPanel = new JPanel( );
         buttonsPanel.setLayout( new FlowLayout( ) );
         JButton btnLoad = new JButton( TC.get( "GeneralText.OK" ) );
         btnLoad.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 setVisible( false );
                 dispose( );
             }
         } );
         buttonsPanel.add( btnLoad );
         JButton btnCancel = new JButton( TC.get( "GeneralText.Cancel" ) );
         btnCancel.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 invalidateOptionSelected( );
                 setVisible( false );
                 dispose( );
             }
         } );
         buttonsPanel.add( btnCancel );
 
         // Add the principal and the buttons panel
         add( guiStylesPanel, BorderLayout.CENTER );
         add( buttonsPanel, BorderLayout.SOUTH );
 
         // Configure the radio buttons
         traditionalRadioButton.addActionListener( new OptionChangedListener( ) );
         contextualRadioButton.addActionListener( new OptionChangedListener( ) );
         ButtonGroup guiButtonGroup = new ButtonGroup( );
         guiButtonGroup.add( traditionalRadioButton );
         guiButtonGroup.add( contextualRadioButton );
         if( optionSelected == DescriptorData.GUI_TRADITIONAL )
             traditionalRadioButton.setSelected( true );
         else if( optionSelected == DescriptorData.GUI_CONTEXTUAL )
             contextualRadioButton.setSelected( true );
 
         // If the dialog is being closed, disable the selection option
         addWindowListener( new WindowAdapter( ) {
 
             @Override
             public void windowClosing( WindowEvent e ) {
 
                 invalidateOptionSelected( );
                 setVisible( false );
                 dispose( );
             }
         } );
 
         // Set size and position and show the dialog
         setSize( 400, 320 );
         Dimension screenSize = Toolkit.getDefaultToolkit( ).getScreenSize( );
         setLocation( ( screenSize.width - getWidth( ) ) / 2, ( screenSize.height - getHeight( ) ) / 2 );
         setResizable( false );
         setVisible( true );
     }
 
     /**
      * Returns the option that has been selected in the dialog.
      * 
      * @return Option selected: 0 for traditional, 1 for contextual, -1 if the
      *         dialog was closed or the "Cancel" button pressed
      */
     public int getOptionSelected( ) {
 
         return optionSelected;
     }
 
     /**
      * Invalidates the option selected (when the "Cancel" button is pressed or
      * the dialog is closed).
      */
     private void invalidateOptionSelected( ) {
 
         optionSelected = -1;
     }
 
     /**
      * Listener for the radio buttons.
      */
     private class OptionChangedListener implements ActionListener {
 
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
          */
         public void actionPerformed( ActionEvent e ) {
 
             if( e.getSource( ).equals( traditionalRadioButton ) )
                 optionSelected = DescriptorData.GUI_TRADITIONAL;
             else if( e.getSource( ).equals( contextualRadioButton ) )
                 optionSelected = DescriptorData.GUI_CONTEXTUAL;
             Controller.getInstance( ).changeToolGUIStyleDialog( optionSelected );
         }
     }
 }

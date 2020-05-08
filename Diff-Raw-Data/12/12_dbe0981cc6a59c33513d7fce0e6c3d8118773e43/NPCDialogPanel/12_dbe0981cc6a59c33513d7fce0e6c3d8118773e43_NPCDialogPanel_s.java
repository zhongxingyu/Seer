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
 package es.eucm.eadventure.editor.gui.elementpanels.character;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import com.sun.speech.freetts.Voice;
 import com.sun.speech.freetts.VoiceManager;
 
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.controllers.character.NPCDataControl;
 import es.eucm.eadventure.editor.gui.auxiliar.components.TextPreviewPanel;
 
 public class NPCDialogPanel extends JPanel {
 
     /**
      * 
      */
     private static final long serialVersionUID = 3457765490477715761L;
 
     private NPCDataControl dataControl;
 
     /**
      * Text color preview panel.
      */
     private TextPreviewPanel textPreviewPanel;
 
     /**
      * Combo box to select between the existent voices
      */
     private JComboBox voicesComboBox;
 
     /**
      * Text field to get the text to try the current voice in the synthesizer
      */
     private JTextField trySynthesizer;
 
     /**
      * Check box to set that the conversation lines of the player must be read
      * by synthesizer
      */
     private JCheckBox alwaysSynthesizer;
 
     private String[] checkVoices;
 
     private JButton bubbleBkgButton;
 
     private JButton bubbleBorderButton;
 
     private JCheckBox showsSpeechBubbles;
 
     private JButton playText;
 
     public NPCDialogPanel( NPCDataControl dataControl ) {
 
         this.dataControl = dataControl;
         setLayout( new GridBagLayout( ) );
         GridBagConstraints cDoc = new GridBagConstraints( );
 
         cDoc.insets = new Insets( 5, 5, 2, 2 );
 
         // Create the text area for the documentation
         cDoc.fill = GridBagConstraints.BOTH;
         cDoc.weightx = 1;
         cDoc.weighty = 1;
         cDoc.fill = GridBagConstraints.HORIZONTAL;
         cDoc.weighty = 0;
         cDoc.gridy = 0;
 
         JPanel textColorPanel = createTextColorPanel( );
         add( textColorPanel, cDoc );
 
         // Create the field for voice selection
         cDoc.gridy = 1;
         JPanel voiceSelection = new JPanel( );
         voiceSelection.setLayout( new GridLayout( 2, 2 ) );
         // Create ComboBox for select the voices
         String[] voices = availableVoices( );
         // alan voice doesn't work
      
             voicesComboBox = new JComboBox( voices );
             checkVoices = voices;
 
             voicesComboBox.addItemListener( new VoiceComboBoxListener( ) );
         if( dataControl.getVoice( ) != null ) {
             for( int i = 1; i < checkVoices.length; i++ )
                 if( checkVoices[i].equals( dataControl.getVoice( ) ) )
                     voicesComboBox.setSelectedIndex( i );
         }
         voiceSelection.add( voicesComboBox );
         // Create CheckBox for select if always synthesizer voices
         alwaysSynthesizer = new JCheckBox( TC.get( "Synthesizer.CheckAlways" ) );
         alwaysSynthesizer.addItemListener( new VoiceCheckVoxListener( ) );
         alwaysSynthesizer.setSelected( dataControl.isAlwaysSynthesizer( ) );
         voiceSelection.add( alwaysSynthesizer );
         // Create a TextField to introduce text to try it in the synthesizer
         trySynthesizer = new JTextField( );
         voiceSelection.add( trySynthesizer );
         // Create a Button to take the text and try it in the synthesizer
         playText = new JButton( TC.get( "Synthesizer.ButtonPlay" ) );
         playText.addActionListener( new VoiceButtonListener( ) );
         voiceSelection.add( playText );
         
         int selection = voicesComboBox.getSelectedIndex( );
         boolean selectedVoice =selection !=0;
         alwaysSynthesizer.setEnabled( selectedVoice ); 
         trySynthesizer.setEnabled( selectedVoice );
         playText.setEnabled( selectedVoice);
         
         TitledBorder border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ), TC.get( "Synthesizer.BorderVoices" ), TitledBorder.LEFT, TitledBorder.TOP );
         voiceSelection.setBorder( border );
         add( voiceSelection, cDoc );
     }
 
     /**
      * 
      * Called when select a field in combo box voiceComboBox
      * 
      */
     private class VoiceComboBoxListener implements ItemListener {
 
         public void itemStateChanged( ItemEvent arg0 ) {
 
             int selection = voicesComboBox.getSelectedIndex( );
             boolean enableCheckBox = selection !=0; 
            boolean availableVoice ;
            if (dataControl.getVoice( )==null){
                availableVoice = false;
            }else{
                availableVoice = (dataControl.getVoice().equals( new String("")));
            }
                
                if (availableVoice != enableCheckBox){
                     alwaysSynthesizer.setEnabled( enableCheckBox);
                     trySynthesizer.setText( "" );
                     trySynthesizer.setEnabled( enableCheckBox);
                     playText.setEnabled( enableCheckBox ); 
                     }
             if( selection != 0 ) {
                 dataControl.setVoice( (String) voicesComboBox.getSelectedItem( ) );
             }
             else{
                 dataControl.setVoice( new String( "" ) );
             }
         }
     }
 
     /**
      * Called when the demo synthesizer button has been pressed
      */
     private class VoiceButtonListener implements ActionListener {
 
         public void actionPerformed( ActionEvent arg0 ) {
 
             if( voicesComboBox.getSelectedIndex( ) != 0 && trySynthesizer.getText( ) != null ) {
                 VoiceManager voiceManager = VoiceManager.getInstance( );
                 Voice voice = voiceManager.getVoice( (String) voicesComboBox.getSelectedItem( ) );
                 voice.allocate( );
                 voice.speak( trySynthesizer.getText( ) );
             }
         }
 
     }
 
     private JPanel createTextColorPanel( ) {
 
         JPanel textColorPanel = new JPanel( );
         //textColorPanel.setLayout( new GridLayout( 6, 1, 4, 4 ) );
         textColorPanel.setLayout( new GridBagLayout( ) );
         GridBagConstraints c = new GridBagConstraints( );
         c.gridx = 0;
         c.gridy = 0;
         c.weightx = 1.0;
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weighty = 0.1;
 
         showsSpeechBubbles = new JCheckBox( TC.get( "Player.ShowsSpeechBubble" ) );
         textColorPanel.add( showsSpeechBubbles, c );
         showsSpeechBubbles.setSelected( dataControl.getShowsSpeechBubbles( ) );
         showsSpeechBubbles.addChangeListener( new ChangeListener( ) {
 
             public void stateChanged( ChangeEvent arg0 ) {
 
                 if( showsSpeechBubbles.isSelected( ) != dataControl.getShowsSpeechBubbles( ) ) {
                     dataControl.setShowsSpeechBubbles( showsSpeechBubbles.isSelected( ) );
                     if( bubbleBkgButton != null && bubbleBorderButton != null ) {
                         bubbleBkgButton.setEnabled( dataControl.getShowsSpeechBubbles( ) );
                         bubbleBorderButton.setEnabled( dataControl.getShowsSpeechBubbles( ) );
                     }
                     if( textPreviewPanel != null ) {
                         textPreviewPanel.setShowsSpeechBubbles( showsSpeechBubbles.isSelected( ) );
                     }
 
                 }
             }
         } );
         textPreviewPanel = new TextPreviewPanel( dataControl.getTextFrontColor( ), dataControl.getTextBorderColor( ), dataControl.getShowsSpeechBubbles( ), dataControl.getBubbleBkgColor( ), dataControl.getBubbleBorderColor( ) );
         c.gridy++;
         c.weighty = 1.0;
         c.ipady = 40;
         textColorPanel.add( textPreviewPanel, c );
         JButton frontColorButton = new JButton( TC.get( "Player.FrontColor" ) );
         frontColorButton.addActionListener( new ChangeTextColorListener( ChangeTextColorListener.FRONT_COLOR ) );
         c.gridy++;
         c.weighty = 0.1;
         c.ipady = 0;
         textColorPanel.add( frontColorButton, c );
         JButton borderColorButton = new JButton( TC.get( "Player.BorderColor" ) );
         borderColorButton.addActionListener( new ChangeTextColorListener( ChangeTextColorListener.BORDER_COLOR ) );
         c.gridy++;
         textColorPanel.add( borderColorButton, c );
         bubbleBkgButton = new JButton( TC.get( "Player.BubbleBkgColor" ) );
         bubbleBkgButton.addActionListener( new ChangeTextColorListener( ChangeTextColorListener.BUBBLEBKG_COLOR ) );
         c.gridy++;
         textColorPanel.add( bubbleBkgButton, c );
         bubbleBkgButton.setEnabled( dataControl.getShowsSpeechBubbles( ) );
         bubbleBorderButton = new JButton( TC.get( "Player.BubbleBorderColor" ) );
         bubbleBorderButton.addActionListener( new ChangeTextColorListener( ChangeTextColorListener.BUBBLEBORDER_COLOR ) );
         c.gridy++;
         textColorPanel.add( bubbleBorderButton, c );
         bubbleBorderButton.setEnabled( dataControl.getShowsSpeechBubbles( ) );
         textColorPanel.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "Player.TextColor" ) ) );
         return textColorPanel;
     }
 
     /**
      * Called when change the state of checkbox
      */
 
     private class VoiceCheckVoxListener implements ItemListener {
 
         public void itemStateChanged( ItemEvent arg0 ) {
 
             dataControl.setAlwaysSynthesizer( alwaysSynthesizer.isSelected( ) );
         }
     }
 
     /**
      * Return a string array with names of the available voices for synthesizer
      * text
      * 
      * @return string array with names of the available voices for synthesizer
      *         text
      */
     private String[] availableVoices( ) {
         System.setProperty( "freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory" );
         VoiceManager voiceManager = VoiceManager.getInstance( );
         Voice[] availableVoices = voiceManager.getVoices( );
         String[] voiceName = new String[ availableVoices.length + 1 ];
         voiceName[0] = TC.get( "Synthesizer.Empty" );
         for( int i = 0; i < availableVoices.length; i++ )
             voiceName[i + 1] = availableVoices[i].getName( );
         return voiceName;
     }
 
     /**
      * Listener for the change color buttons.
      */
     private class ChangeTextColorListener implements ActionListener {
 
         /**
          * Constant for front color.
          */
         public static final int FRONT_COLOR = 0;
 
         /**
          * Constant for border color.
          */
         public static final int BORDER_COLOR = 1;
 
         public static final int BUBBLEBKG_COLOR = 2;
 
         public static final int BUBBLEBORDER_COLOR = 3;
 
         private int color;
 
         /**
          * Color chooser.
          */
         private JColorChooser colorChooser;
 
         /**
          * Text preview panel.
          */
         private TextPreviewPanel colorPreviewPanel;
 
         /**
          * Constructor.
          * 
          * @param frontColor
          *            Whether the front or border color must be changed
          */
         public ChangeTextColorListener( int color ) {
 
             this.color = color;
 
             // Create the color chooser
             colorChooser = new JColorChooser( );
 
             // Create and add the preview panel, attaching it to the color chooser
             colorPreviewPanel = new TextPreviewPanel( dataControl.getTextFrontColor( ), dataControl.getTextBorderColor( ), dataControl.getShowsSpeechBubbles( ), dataControl.getBubbleBkgColor( ), dataControl.getBubbleBorderColor( ) );
             colorPreviewPanel.setPreferredSize( new Dimension( 400, 40 ) );
             colorPreviewPanel.setBorder( BorderFactory.createEmptyBorder( 1, 1, 1, 1 ) );
             colorChooser.setPreviewPanel( colorPreviewPanel );
             colorChooser.getSelectionModel( ).addChangeListener( new UpdatePreviewPanelListener( ) );
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
          */
         public void actionPerformed( ActionEvent e ) {
 
             // Update the color on the color chooser and the preview panel
             if( color == FRONT_COLOR )
                 colorChooser.setColor( dataControl.getTextFrontColor( ) );
             else if( color == BORDER_COLOR )
                 colorChooser.setColor( dataControl.getTextBorderColor( ) );
             else if( color == BUBBLEBKG_COLOR )
                 colorChooser.setColor( dataControl.getBubbleBkgColor( ) );
             else if( color == BUBBLEBORDER_COLOR )
                 colorChooser.setColor( dataControl.getBubbleBorderColor( ) );
 
             colorPreviewPanel.setTextFrontColor( dataControl.getTextFrontColor( ) );
             colorPreviewPanel.setTextBorderColor( dataControl.getTextBorderColor( ) );
 
             // Create and show the dialog
             JDialog colorDialog = null;
             if( color == FRONT_COLOR )
                 colorDialog = JColorChooser.createDialog( null, TC.get( "Player.FrontColor" ), true, colorChooser, new UpdateColorListener( ), null );
             else if( color == BORDER_COLOR )
                 colorDialog = JColorChooser.createDialog( null, TC.get( "Player.BorderColor" ), true, colorChooser, new UpdateColorListener( ), null );
             else if( color == BUBBLEBKG_COLOR )
                 colorDialog = JColorChooser.createDialog( null, TC.get( "Player.BubbleBkgColor" ), true, colorChooser, new UpdateColorListener( ), null );
             else if( color == BUBBLEBORDER_COLOR )
                 colorDialog = JColorChooser.createDialog( null, TC.get( "Player.BubbleBorderColor" ), true, colorChooser, new UpdateColorListener( ), null );
             colorDialog.setResizable( false );
             colorDialog.setVisible( true );
         }
 
         /**
          * Listener for the "Acept" button of the color chooser dialog.
          */
         private class UpdateColorListener implements ActionListener {
 
             /*
              * (non-Javadoc)
              * 
              * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
              */
             public void actionPerformed( ActionEvent e ) {
 
                 // Update the text color
                 if( color == FRONT_COLOR ) {
                     dataControl.setTextFrontColor( colorChooser.getColor( ) );
                     textPreviewPanel.setTextFrontColor( colorChooser.getColor( ) );
                 }
                 else if( color == BORDER_COLOR ) {
                     dataControl.setTextBorderColor( colorChooser.getColor( ) );
                     textPreviewPanel.setTextBorderColor( colorChooser.getColor( ) );
                 }
                 else if( color == BUBBLEBKG_COLOR ) {
                     dataControl.setBubbleBkgColor( colorChooser.getColor( ) );
                     textPreviewPanel.setBubbleBkgColor( colorChooser.getColor( ) );
                 }
                 else if( color == BUBBLEBORDER_COLOR ) {
                     dataControl.setBubbleBorderColor( colorChooser.getColor( ) );
                     textPreviewPanel.setBubbleBorderColor( colorChooser.getColor( ) );
                 }
             }
         }
 
         /**
          * Listener for the color preview panel.
          */
         private class UpdatePreviewPanelListener implements ChangeListener {
 
             /*
              * (non-Javadoc)
              * 
              * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
              */
             public void stateChanged( ChangeEvent e ) {
 
                 if( color == FRONT_COLOR )
                     colorPreviewPanel.setTextFrontColor( colorChooser.getColor( ) );
                 else if( color == BORDER_COLOR )
                     colorPreviewPanel.setTextBorderColor( colorChooser.getColor( ) );
                 else if( color == BUBBLEBKG_COLOR )
                     colorPreviewPanel.setBubbleBkgColor( colorChooser.getColor( ) );
                 else if( color == BUBBLEBORDER_COLOR )
                     colorPreviewPanel.setBubbleBorderColor( colorChooser.getColor( ) );
             }
         }
     }
 
     public boolean updateFields( ) {
 
         this.showsSpeechBubbles.setSelected( dataControl.getShowsSpeechBubbles( ) );
         this.bubbleBkgButton.setEnabled( dataControl.getShowsSpeechBubbles( ) );
         this.bubbleBorderButton.setEnabled( dataControl.getShowsSpeechBubbles( ) );
         this.textPreviewPanel.setBubbleBkgColor( dataControl.getBubbleBkgColor( ) );
         this.textPreviewPanel.setBubbleBorderColor( dataControl.getBubbleBorderColor( ) );
         this.textPreviewPanel.setTextBorderColor( dataControl.getTextBorderColor( ) );
         this.textPreviewPanel.setTextFrontColor( dataControl.getTextFrontColor( ) );
         this.textPreviewPanel.setShowsSpeechBubbles( dataControl.getShowsSpeechBubbles( ) );
         this.textPreviewPanel.updateUI( );
         if( dataControl.getVoice( ) != null ) {
             for( int i = 1; i < checkVoices.length; i++ )
                 if( checkVoices[i].equals( dataControl.getVoice( ) ) )
                     voicesComboBox.setSelectedIndex( i );
         }
         alwaysSynthesizer.setSelected( dataControl.isAlwaysSynthesizer( ) );
         this.repaint( );
         return true;
     }
 
 }

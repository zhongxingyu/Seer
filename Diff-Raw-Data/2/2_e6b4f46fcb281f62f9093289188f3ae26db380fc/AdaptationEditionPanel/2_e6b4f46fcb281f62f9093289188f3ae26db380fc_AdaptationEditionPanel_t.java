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
 package es.eucm.eadventure.editor.gui.elementpanels.adaptation;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.Searchable;
 import es.eucm.eadventure.editor.control.controllers.adaptation.AdaptationProfileDataControl;
 import es.eucm.eadventure.editor.control.controllers.adaptation.AdaptationRuleDataControl;
 import es.eucm.eadventure.editor.control.tools.adaptation.AddRuleTool;
 import es.eucm.eadventure.editor.control.tools.adaptation.DeleteRuleTool;
 import es.eucm.eadventure.editor.control.tools.adaptation.DuplicateRuleTool;
 import es.eucm.eadventure.editor.gui.DataControlsPanel;
 import es.eucm.eadventure.editor.gui.Updateable;
 import es.eucm.eadventure.editor.gui.auxiliar.components.JFiller;
 import es.eucm.eadventure.editor.gui.elementpanels.PanelTab;
 import es.eucm.eadventure.editor.gui.elementpanels.general.TableScrollPane;
 
 public class AdaptationEditionPanel extends JPanel implements Updateable, DataControlsPanel {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Panel which contains the profiles's type
      */
     private JPanel profileTypePanel;
 
     /**
      * Panel which contains the initial state
      */
     private InitialStatePanel initialStatePanel;
 
     /**
      * Panel which contains all the rules associated with current profile
      */
     private JPanel ruleListPanel;
 
     /**
      * Panel which contains the initial state and LMS state of selected rule
      */
     private JTabbedPane rulesInfoPanel;
 
     /**
      * Button to duplicate selected rule
      */
     private JButton duplicate;
 
     /**
      * Button to delete selected rule
      */
     private JButton delete;
 
     /**
      * Data control
      */
     private AdaptationProfileDataControl dataControl;
 
     /*
     private AdaptationRuleDataControl lastRule;*/
 
     /**
      * Table with all profile's rules
      */
     private JTable informationTable;
 
     /**
      * Combo box for adaptation profile type
      */
     private JComboBox comboProfile;
 
     /**
      * Move property up ( /\ ) button
      */
     private JButton movePropertyUpButton;
 
     /**
      * Move property down ( \/ ) button
      */
     private JButton movePropertyDownButton;
 
     /**
      * 
      */
     JLabel typeLabel;
 
     public AdaptationEditionPanel( AdaptationProfileDataControl dataControl ) {
 
         this.dataControl = dataControl;
         createProfileTypePanel( );
         createInitialState( );
         createRuleListPanel( );
         rulesInfoPanel = new JTabbedPane( );
 
         createRulesInfoPanel( );
 
         this.setLayout( new GridBagLayout( ) );
         GridBagConstraints c = new GridBagConstraints( );
         c.insets = new Insets( 5, 5, 5, 5 );
         c.fill = GridBagConstraints.VERTICAL;
         c.weighty = 1;
         c.gridx = 0;
         c.gridy = 0;
         this.add( profileTypePanel, c );
         c.fill = GridBagConstraints.BOTH;
         c.weightx = 2.0;
         c.gridx = 1;
         this.add( initialStatePanel, c );
         c.gridx = 0;
         c.gridwidth = 2;
         c.ipady = 100;
         c.gridy++;
         this.add( ruleListPanel, c );
         c.gridy++;
         this.add( rulesInfoPanel, c );
     }
 
     public void createProfileTypePanel( ) {
 
         profileTypePanel = new JPanel( );
         profileTypePanel.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "AssessmentRule.ProfileType" ) ) );
 
         String[] options = new String[] { TC.get( "AdaptationRulesList.Scorm2004" ), TC.get( "AdaptationRulesList.Scorm12" ), TC.get( "AdaptationRulesList.Normal" ) };
         comboProfile = new JComboBox( options );
         if( dataControl.isScorm12( ) )
             comboProfile.setSelectedIndex( 1 );
         else if( dataControl.isScorm2004( ) )
             comboProfile.setSelectedIndex( 0 );
         else
             comboProfile.setSelectedIndex( 2 );
 
         comboProfile.addActionListener( new ComboListener( comboProfile.getSelectedIndex( ) ) );
 
         profileTypePanel.add( comboProfile );
     }
 
     public void createInitialState( ) {
 
         initialStatePanel = new InitialStatePanel( dataControl, true );
     }
 
     private class ComboListener implements ActionListener {
 
         private int pastSelection;
 
         public ComboListener( int pastSelection ) {
 
             this.pastSelection = pastSelection;
         }
 
         //@Override
         public void actionPerformed( ActionEvent e ) {
 
             JComboBox combo = ( (JComboBox) e.getSource( ) );
             if( pastSelection != combo.getSelectedIndex( ) ) {
                 informationTable.clearSelection( );
                 if( ( combo.getSelectedIndex( ) == 0 )  && !dataControl.isScorm2004( )){
                     dataControl.changeToScorm2004Profile( );
                 }
                 else if( ( combo.getSelectedIndex( ) == 1)  && !dataControl.isScorm12( )) {
                     dataControl.changeToScorm12Profile( );
                 }
                 else if( ( combo.getSelectedIndex( ) == 2 ) && (dataControl.isScorm12( ) || dataControl.isScorm2004( )))  {
                     dataControl.changeToNormalProfile( );
                 }
                 pastSelection = combo.getSelectedIndex( );
             }
 
         }
 
     }
 
     private void createRuleListPanel( ) {
 
         informationTable = new AdaptationRulesTable( dataControl );
         informationTable.getSelectionModel( ).addListSelectionListener( new ListSelectionListener( ) {
 
             public void valueChanged( ListSelectionEvent e ) {
 
                 if( informationTable.getSelectedRow( ) > -1 ) {
                     delete.setEnabled( true );
                     duplicate.setEnabled( true );
                     movePropertyUpButton.setEnabled( true );
                     movePropertyDownButton.setEnabled( true );
              //       lastRule = dataControl.getAdaptationRules( ).get( informationTable.getSelectedRow( ) );
                 }
                 else {
                     delete.setEnabled( false );
                     duplicate.setEnabled( false );
                     movePropertyUpButton.setEnabled( false );
                     movePropertyDownButton.setEnabled( false );
                 }
                 // TODO it produces so calls to that method, analyze 
                 createRulesInfoPanel( );
             }
         } );
         //informationTable.removeEditor( );
         ruleListPanel = new JPanel( );
         ruleListPanel.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "AdaptationRulesList.ListTitle" ) ) );
         ruleListPanel.setLayout( new BorderLayout( ) );
         ruleListPanel.add( new TableScrollPane( informationTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ), BorderLayout.CENTER );
 
         JButton add = new JButton( new ImageIcon( "img/icons/addNode.png" ) );
         add.setContentAreaFilled( false );
         add.setMargin( new Insets( 0, 0, 0, 0 ) );
         add.setBorder( BorderFactory.createEmptyBorder( ) );
         add.setToolTipText( TC.get( "AdaptationProfile.AddRule" ) );
         add.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 if( Controller.getInstance( ).addTool( new AddRuleTool( dataControl, Controller.ADAPTATION_RULE ) ) ) {
                     ( (AdaptationRulesTable) informationTable ).fireTableDataChanged( );
                     informationTable.changeSelection( dataControl.getAdaptationRules( ).size( ) - 1, 0, false, false );
                 }
             }
         } );
 
         duplicate = new JButton( new ImageIcon( "img/icons/duplicateNode.png" ) );
         duplicate.setContentAreaFilled( false );
         duplicate.setMargin( new Insets( 0, 0, 0, 0 ) );
         duplicate.setBorder( BorderFactory.createEmptyBorder( ) );
         duplicate.setToolTipText( TC.get( "AdaptationProfile.Duplicate" ) );
         duplicate.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 if( Controller.getInstance( ).addTool( new DuplicateRuleTool( dataControl, Controller.ADAPTATION_RULE, informationTable.getSelectedRow( ) ) ) ) {
                     ( (AdaptationRulesTable) informationTable ).fireTableDataChanged( );
                     informationTable.changeSelection( dataControl.getAdaptationRules( ).size( ) - 1, 0, false, false );
                 }
             }
         } );
         duplicate.setEnabled( false );
 
         delete = new JButton( new ImageIcon( "img/icons/deleteNode.png" ) );
         delete.setContentAreaFilled( false );
         delete.setMargin( new Insets( 0, 0, 0, 0 ) );
         delete.setBorder( BorderFactory.createEmptyBorder( ) );
         delete.setToolTipText( TC.get( "AdaptationProfile.DeleteRule" ) );
         delete.setEnabled( false );
         delete.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 if( Controller.getInstance( ).addTool( new DeleteRuleTool( dataControl, Controller.ADAPTATION_RULE, informationTable.getSelectedRow( ) ) ) ) {
                     ( (AdaptationRulesTable) informationTable ).fireTableDataChanged( );
                     informationTable.clearSelection( );
                 }
             }
         } );
 
         // Up and down buttons
         movePropertyUpButton = new JButton( new ImageIcon( "img/icons/moveNodeUp.png" ) );
         movePropertyUpButton.setContentAreaFilled( false );
         movePropertyUpButton.setMargin( new Insets( 0, 0, 0, 0 ) );
         movePropertyUpButton.setBorder( BorderFactory.createEmptyBorder( ) );
         movePropertyUpButton.setToolTipText( TC.get( "UOLProperties.MoveUp" ) );
         movePropertyUpButton.addActionListener( new ListenerButtonMoveLineUp( ) );
         movePropertyUpButton.setEnabled( false );
 
         movePropertyDownButton = new JButton( new ImageIcon( "img/icons/moveNodeDown.png" ) );
         movePropertyDownButton.setContentAreaFilled( false );
         movePropertyDownButton.setMargin( new Insets( 0, 0, 0, 0 ) );
         movePropertyDownButton.setBorder( BorderFactory.createEmptyBorder( ) );
         movePropertyDownButton.setToolTipText( TC.get( "UOLProperties.MoveDown" ) );
         movePropertyDownButton.addActionListener( new ListenerButtonMoveLineDown( ) );
         movePropertyDownButton.setEnabled( false );
 
         JPanel buttonsPanel = new JPanel( new GridBagLayout( ) );
         GridBagConstraints c = new GridBagConstraints( );
         c.gridx = 0;
         c.gridy = 0;
         buttonsPanel.add( add, c );
         c.gridy = 1;
         buttonsPanel.add( duplicate, c );
         c.gridy = 3;
         buttonsPanel.add( movePropertyUpButton, c );
         c.gridy = 4;
         buttonsPanel.add( movePropertyDownButton, c );
         c.gridy = 5;
         buttonsPanel.add( delete, c );
         c.gridy = 2;
         c.weighty = 1.0;
         c.fill = GridBagConstraints.VERTICAL;
         buttonsPanel.add( new JFiller( ), c );
         ruleListPanel.add( buttonsPanel, BorderLayout.EAST );
         ruleListPanel.setMinimumSize( new Dimension( 0, 30 ) );
         ruleListPanel.setMaximumSize( new Dimension( 0, 30 ) );
     }
 
     private void createRulesInfoPanel( ) {
 
         if( informationTable.getSelectedRow( ) < 0 || informationTable.getSelectedRow( ) >= dataControl.getAdaptationRules( ).size( ) ) {
             rulesInfoPanel.removeAll( );
             JPanel empty = new JPanel( );
             JLabel label = new JLabel( TC.get( "AdaptationProfile.Empty" ) );
             empty.add( label );
             rulesInfoPanel.add( empty );
             rulesInfoPanel.setMinimumSize( new Dimension( 0, 100 ) );
             rulesInfoPanel.updateUI( );
         }
         else {
             rulesInfoPanel.removeAll( );
             
             JPanel Uol = new UOLPropertiesPanel( dataControl.getAdaptationRules( ).get( informationTable.getSelectedRow( ) ), dataControl.isScorm12( ), dataControl.isScorm2004( ) );
             rulesInfoPanel.addTab( TC.get( "AdaptationProfile.TabbedLMSState" ), Uol );
 
             JPanel gameStatePanel = new GameStatePanel( dataControl.getAdaptationRules( ).get( informationTable.getSelectedRow( ) ) );
             rulesInfoPanel.addTab( TC.get( "AdaptationProfile.TabbedInitialState" ), gameStatePanel );
 
             // Create the game-state panel
             rulesInfoPanel.setPreferredSize( new Dimension( 0, 250 ) );
             rulesInfoPanel.updateUI( );
 
         }
     }
 
     private class GameStatePanelTab extends PanelTab {
 
         private AdaptationRuleDataControl dataControl;
 
         public GameStatePanelTab( AdaptationRuleDataControl adpRuleDataControl ) {
 
             super( TC.get( "AdaptationProfile.TabbedInitialState" ), adpRuleDataControl );
             //this.setHelpPath("scenes/Scene_Barriers.html");
             this.dataControl = adpRuleDataControl;
         }
 
         @Override
         protected JComponent getTabComponent( ) {
 
             return new GameStatePanel( dataControl );
         }
     }
 
     private class UOLPropertiesPanelTab extends PanelTab {
 
         private AdaptationRuleDataControl dataControl;
 
         private boolean isScorm12;
 
         private boolean isScorm2004;
 
         public UOLPropertiesPanelTab( AdaptationRuleDataControl dataControl, boolean isScorm12, boolean isScorm2004 ) {
 
             super( TC.get( "AdaptationProfile.TabbedLMSState" ), dataControl );
             //this.setHelpPath("scenes/Scene_Barriers.html");
             this.dataControl = dataControl;
             this.isScorm12 = isScorm12;
             this.isScorm2004 = isScorm2004;
         }
 
         @Override
         protected JComponent getTabComponent( ) {
 
             return new UOLPropertiesPanel( dataControl, isScorm12, isScorm2004 );
         }
     }
 
     public boolean updateFields( ) {
 
         int selected = informationTable.getSelectedRow( );
        // int selectedTab = rulesInfoPanel.getSelectedIndex( );
         
         // the call is not spread 
         //if( rulesInfoPanel != null && rulesInfoPanel instanceof Updateable )
           //  ( (Updateable) rulesInfoPanel ).updateFields( );
        
        initialStatePanel.updateFields( );
         if( informationTable.getCellEditor( ) != null ) {
             informationTable.getCellEditor( ).cancelCellEditing( );
         }
         ( (AbstractTableModel) informationTable.getModel( ) ).fireTableDataChanged( );
         // update combo box
         if( dataControl.isScorm12( ) )
             comboProfile.setSelectedIndex( 1 );
         else if( dataControl.isScorm2004( ) )
             comboProfile.setSelectedIndex( 0 );
         else
             comboProfile.setSelectedIndex( 2 );
 
             
            
         informationTable.getSelectionModel( ).setSelectionInterval( selected, selected );
 
        /* if( selectedTab > -1 && selectedTab<rulesInfoPanel.getTabCount()){
     		rulesInfoPanel.setSelectedIndex( selectedTab );
         }
         
         if( initialStatePanel instanceof Updateable )
             ( (Updateable) initialStatePanel ).updateFields( );
 */
         return true;
     }
 
     /**
      * Listener for the move line up ( /\ ) button
      */
     private class ListenerButtonMoveLineUp implements ActionListener {
 
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
          */
         public void actionPerformed( ActionEvent e ) {
 
             // Take the selected row, and the selected node
             int selectedRow = informationTable.getSelectedRow( );
 
             // If the line was moved
             if( dataControl.moveElementUp( dataControl.getAdaptationRules( ).get( selectedRow ) ) ) {
 
                 // Move the selection along with the line
                 informationTable.setRowSelectionInterval( selectedRow - 1, selectedRow - 1 );
                 informationTable.scrollRectToVisible( informationTable.getCellRect( selectedRow - 1, 0, true ) );
 
             }
         }
     }
 
     /**
      * Listener for the move line down ( \/ ) button
      */
     private class ListenerButtonMoveLineDown implements ActionListener {
 
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
          */
         public void actionPerformed( ActionEvent e ) {
 
             // Take the selected row, and the selected node
             int selectedRow = informationTable.getSelectedRow( );
 
             // If the line was moved
             if( dataControl.moveElementDown( dataControl.getAdaptationRules( ).get( selectedRow ) ) ) {
 
                 // Move the selection along with the line
                 informationTable.setRowSelectionInterval( selectedRow + 1, selectedRow + 1 );
                 informationTable.scrollRectToVisible( informationTable.getCellRect( selectedRow + 1, 0, true ) );
 
             }
         }
     }
 
     public void setSelectedItem( List<Searchable> path ) {
 
         if( path.size( ) > 0 ) {
             for( int i = 0; i < dataControl.getDataControls( ).size( ); i++ ) {
                 if( dataControl.getDataControls( ).get( i ) == path.get( path.size( ) - 1 ) )
                     informationTable.changeSelection( i, i, false, false );
             }
         }
     }
 
 }

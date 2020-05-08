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
 package es.eucm.eadventure.editor.gui.elementpanels.condition;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.ConditionsController;
 import es.eucm.eadventure.editor.control.controllers.ConditionsController.ConditionContextProperty;
 import es.eucm.eadventure.editor.control.controllers.ConditionsController.ConditionCustomMessage;
 import es.eucm.eadventure.editor.control.controllers.ConditionsController.ConditionOwner;
 import es.eucm.eadventure.editor.gui.Updateable;
 import es.eucm.eadventure.editor.gui.editdialogs.ConditionDialog;
 import es.eucm.eadventure.editor.gui.editdialogs.ConditionsDialog;
 
 public class ConditionsPanel extends JPanel implements Updateable, ConditionsPanelController {
 
     /**
      * Required
      */
     private static final long serialVersionUID = -3452049823030669523L;
 
     public static final Color FLAG_COLOR = new Color( 220, 5, 5, 50 );
 
     public static final Color VAR_COLOR = new Color( 5, 5, 220, 50 );
 
     /*
      * Colors
      */
     private static Color topPanelLineColor = Color.black;
 
     private static Color centralPanelLineColor = Color.black;
 
     private static Color buttonsPanelLineColor = Color.black;
 
     private ConditionsController conditionsController;
 
     private List<EditablePanel> panels;
 
     private EditablePanel selectedPanel = null;
 
     private JPanel topPanel;
 
     private JEditorPane textPane;
 
     private JPanel centralPanel;
 
     private JPanel buttonsPanel;
 
     private JButton addConditionButton;
 
     private JButton okButton;
 
     /**
      * Constructor.
      * 
      * @param conditionController
      *            Controller for the conditions
      * @param keyListener
      */
     public ConditionsPanel( ConditionsController conditionsController ) {
 
         this.setLayout( new BorderLayout( ) );
         this.conditionsController = conditionsController;
 
         topPanel = createTopPanel( );
         this.add( topPanel, BorderLayout.NORTH );
 
         createButtonsPanel( );
         this.add( buttonsPanel, BorderLayout.SOUTH );
 
         createCentralPanel( );
 
     }
 
     public void update( ) {
 
         textPane.setContentType( "text/html" );
         textPane.setText( getHTMLTopText( ) );
         updateCentralPanel( );
     }
 
     private JPanel createTopPanel( ) {
 
         JPanel topPanel = new JPanel( );
         topPanel.setLayout( new BorderLayout( ) );
         topPanel.setBorder( BorderFactory.createLineBorder( topPanelLineColor ) );
 
         textPane = new JEditorPane( "text/html", getHTMLTopText( ) );
         textPane.setEditable( false );
         textPane.setOpaque( false );
 
         topPanel.add( textPane, BorderLayout.CENTER );
         return topPanel;
     }
 
     private String getHTMLTopText( ) {
 
         String html = "<html>\n" + "\t<head>\n" + "\t</head>\n" + "\t<body>\n" + "\t\t<p align=center>";
         HashMap<String, ConditionContextProperty> context = conditionsController.getContext( );
         if( context.containsKey( ConditionsController.CONDITION_OWNER ) ) {
             ConditionOwner owner = (ConditionOwner) context.get( ConditionsController.CONDITION_OWNER );
 
             if( !context.containsKey( ConditionsController.CONDITION_CUSTOM_MESSAGE ) ) {
 
                 String ownerTypeString = TC.getElement( owner.getOwnerType( ) );
                 html += TC.get( "Conditions.Context.Sentence1" ) + "<i>" + ownerTypeString + "</i>" + " <b>\"" + owner.getOwnerName( ) + "\" </b>";
 
                 ConditionOwner parent = owner.getParent( );
                 if( parent != null )
                     html += " (";
                 while( parent != null ) {
                     html += TC.getElement( parent.getOwnerType( ) ) + " " + parent.getOwnerName( );
                     parent = parent.getParent( );
                     if( parent != null )
                         html += ", ";
                     else
                         html += ")";
                 }
 
                 if( !conditionsController.isEmpty( ) ) {
                     html += TC.get( "Conditions.Context.Sentence2a" );
                 }
                 else {
                     html += TC.get( "Conditions.Context.Sentence2b" );
                 }
             }
             else {
                
                context = ConditionsController.createContextFromOwner(owner.getOwnerType( ),owner.getOwnerName( ));
                 ConditionCustomMessage cMessage = (ConditionCustomMessage) context.get( ConditionsController.CONDITION_CUSTOM_MESSAGE );
                 if( !conditionsController.isEmpty( ) ) {
                     html += cMessage.getFormattedSentence( owner );
                 }
                 else {
                     html += cMessage.getNoConditionFormattedSentence( owner );
                 }
             }
 
         }
         else {
             html += TC.get( "Conditions.Context.NoOwner" );
         }
         html += "\n\t\t</p>\n";
         html += "\t</body>\n";
         html += "</html>";
         return html;
     }
 
     private void createButtonsPanel( ) {
 
         buttonsPanel = new JPanel( );
         addConditionButton = new JButton( new ImageIcon( "img/icons/addNode.png" ) );
         addConditionButton.setText( TC.get( "Conditions.AddCondition" ) );
         addConditionButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 ConditionsPanel.this.addCondition( );
             }
         } );
         buttonsPanel.add( addConditionButton );
 
         ConditionOwner owner = (ConditionOwner) conditionsController.getContext( ).get( ConditionsController.CONDITION_OWNER );
         if( owner != null && owner.getOwnerType( ) != Controller.GLOBAL_STATE ) {
             okButton = new JButton( TC.get( "GeneralText.OK" ) );
             okButton.addActionListener( new ActionListener( ) {
 
                 public void actionPerformed( ActionEvent e ) {
 
                     Container parent = getParent( );
                     int i = 0;
                     while( i < 10 && !( parent instanceof ConditionsDialog ) ) {
                         parent = parent.getParent( );
                         i++;
                     }
                     if( i < 10 ) {
                         ( (ConditionsDialog) parent ).setVisible( false );
                     }
                 }
 
             } );
             buttonsPanel.add( okButton );
         }
         buttonsPanel.setBorder( BorderFactory.createLineBorder( buttonsPanelLineColor ) );
     }
 
     private JPanel createCentralPanel( ) {
 
         centralPanel = new JPanel( );
         centralPanel.setBorder( BorderFactory.createLineBorder( centralPanelLineColor ) );
         centralPanel.setLayout( new BoxLayout( centralPanel, BoxLayout.PAGE_AXIS ) );
         JScrollPane scroll = new JScrollPane( centralPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
         this.add( scroll, BorderLayout.CENTER );
 
         updateCentralPanel( );
 
         return centralPanel;
     }
 
     private void updateCentralPanel( ) {
 
         centralPanel.removeAll( );
         panels = new ArrayList<EditablePanel>( );
         for( int i = 0; i < conditionsController.getBlocksCount( ); i++ ) {
             if( i > 0 ) {
                 EvalFunctionPanel labelPanel = new EvalFunctionPanel( this, i - 1, ConditionsController.INDEX_NOT_USED, EvalFunctionPanel.AND );
                 JPanel evalFunctionPanel = new JPanel( );
                 evalFunctionPanel.add( labelPanel );
                 panels.add( labelPanel );
                 centralPanel.add( evalFunctionPanel );
             }
 
             EditablePanel subPanel = null;
             subPanel = new CompositeConditionPanel( this, i );//subPanel = new ConditionPanel(1,null);//getConditionPanel ( wrapper.getEitherBlock(), false);
             //subPanel.setBorder(new LineBorder( Color.DARK_GRAY, 1, true ));
             //subPanel.setBorder( new CurvedBorder(50, Color.DARK_GRAY) );
             panels.add( subPanel );
             centralPanel.add( subPanel );
         }
 
         centralPanel.repaint( );
     }
 
     public boolean updateFields( ) {
 
         update( );
         return true;
     }
 
     public void evalEditablePanelSelectionEvent( EditablePanel source, int oldState, int newState ) {
 
         if( newState != EditablePanel.NO_SELECTED && selectedPanel != null && selectedPanel != source )
             selectedPanel.deselect( );
 
         if( newState != EditablePanel.NO_SELECTED )
             selectedPanel = source;
     }
 
     public void evalFunctionChanged( EvalFunctionPanel source, int index1, int index2, int oldValue, int newValue ) {
 
         if( this.conditionsController.setEvalFunction( index1, index2, newValue ) ) {
             update( );
         }
     }
 
     public HashMap<String, String> getCondition( int index1, int index2 ) {
 
         return conditionsController.getCondition( index1, index2 );
     }
 
     public int getConditionCount( int index1 ) {
 
         return conditionsController.getConditionCount( index1 );
     }
 
     public void addCondition( int index1, int index2 ) {
 
         // Display the dialog to add a new condition
         ConditionDialog conditionDialog = new ConditionDialog( TC.get( "Conditions.AddCondition" ) );
 
         // If the data was approved
         if( conditionDialog.wasPressedOKButton( ) ) {
             // Set the new values and update the table
             if( conditionsController.addCondition( index1, index2, conditionDialog.getSelectedType( ), conditionDialog.getSelectedId( ), conditionDialog.getSelectedState( ), conditionDialog.getSelectedValue( ) ) ) {
                 update( );
             }
         }
     }
 
     public void addCondition( ) {
 
         addCondition( conditionsController.getBlocksCount( ), ConditionsController.INDEX_NOT_USED );
     }
 
     public void deleteCondition( int index1, int index2 ) {
 
         if( conditionsController.deleteCondition( index1, index2 ) ) {
             update( );
         }
     }
 
     public void duplicateCondition( int index1, int index2 ) {
 
         if( conditionsController.duplicateCondition( index1, index2 ) ) {
             update( );
         }
 
     }
 
     public void editCondition( int index1, int index2 ) {
 
         // Take the actual values of the condition, and display the editing dialog
         //String stateValue = conditionsTable.getValueAt( selectedCondition, 0 ).toString( );
         //String flagValue = conditionsTable.getValueAt( selectedCondition, 1 ).toString( );
         HashMap<String, String> properties = getCondition( index1, index2 );
         String defaultId = properties.get( ConditionsPanelController.CONDITION_ID );
         String defaultFlag = properties.get( ConditionsPanelController.CONDITION_ID );
         String defaultVar = properties.get( ConditionsPanelController.CONDITION_ID );
         String defaultState = properties.get( ConditionsPanelController.CONDITION_STATE );
         String defaultValue = properties.get( ConditionsPanelController.CONDITION_VALUE );
         String defaultType = properties.get( ConditionsPanelController.CONDITION_TYPE );
 
         if( defaultType.equals( ConditionsPanelController.CONDITION_TYPE_FLAG ) ) {
             defaultVar = defaultId = null;
         }
         else if( defaultType.equals( ConditionsPanelController.CONDITION_TYPE_VAR ) ) {
             defaultFlag = defaultId = null;
         }
         else if( defaultType.equals( ConditionsPanelController.CONDITION_TYPE_GS ) ) {
             defaultFlag = defaultVar = null;
         }
         ConditionDialog conditionDialog = new ConditionDialog( defaultType, TC.get( "Conditions.EditCondition" ), defaultState, defaultFlag, defaultVar, defaultId, defaultValue, conditionsController.getContext( ) );
 
         // If the data was approved
         if( conditionDialog.wasPressedOKButton( ) ) {
 
             // Set the new values
             /*conditionsController.setConditionType( tableIndex, selectedCondition, conditionDialog.getSelectedType( ) );
             conditionsController.setConditionState( tableIndex, selectedCondition, conditionDialog.getSelectedState( ) );
             conditionsController.setConditionId( tableIndex, selectedCondition, conditionDialog.getSelectedId( ) );
             conditionsController.setConditionValue( tableIndex, selectedCondition, conditionDialog.getSelectedValue( ) );*/
             properties.clear( );
             properties.put( ConditionsPanelController.CONDITION_ID, conditionDialog.getSelectedId( ) );
             properties.put( ConditionsPanelController.CONDITION_STATE, conditionDialog.getSelectedState( ) );
             properties.put( ConditionsPanelController.CONDITION_VALUE, conditionDialog.getSelectedValue( ) );
             properties.put( ConditionsPanelController.CONDITION_TYPE, conditionDialog.getSelectedType( ) );
             if( conditionsController.setCondition( index1, index2, properties ) )
                 // Update the table
                 update( );
         }
     }
 }

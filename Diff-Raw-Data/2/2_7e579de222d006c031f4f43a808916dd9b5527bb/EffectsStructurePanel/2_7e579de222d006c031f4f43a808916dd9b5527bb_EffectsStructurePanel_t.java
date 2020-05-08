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
 package es.eucm.eadventure.editor.gui.structurepanel;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.border.Border;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.gui.editdialogs.SelectEffectsDialog;
 import es.eucm.eadventure.editor.gui.structurepanel.structureelements.Effects.ChangesInSceneStructureListElement;
 import es.eucm.eadventure.editor.gui.structurepanel.structureelements.Effects.EffectsStructureListElement;
 import es.eucm.eadventure.editor.gui.structurepanel.structureelements.Effects.FeedbackStructureListElement;
 import es.eucm.eadventure.editor.gui.structurepanel.structureelements.Effects.GameStateStructureListElement;
 import es.eucm.eadventure.editor.gui.structurepanel.structureelements.Effects.MainStructureListElement;
 import es.eucm.eadventure.editor.gui.structurepanel.structureelements.Effects.MiscelaneousStructureListElement;
 import es.eucm.eadventure.editor.gui.structurepanel.structureelements.Effects.MultimediaStructureListElement;
 import es.eucm.eadventure.editor.gui.structurepanel.structureelements.Effects.TriggerStructureListElement;
 
 /**
  * Extends structure panel to adapt to select effects dialog
  * 
  */
 public class EffectsStructurePanel extends StructurePanel {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     private static final String ACTIVATE_URL = "effects_short/Effects_Activate.html";
 
     private static final String DEACTIVATE_URL = "effects_short/Effects_Deactivate.html";
 
     private static final String INCR_URL = "effects_short/Effects_Increment.html";
 
     private static final String DECR_URL = "effects_short/Effects_Decrement.html";
 
     private static final String SET_URL = "effects_short/Effects_Setvar.html";
 
     private static final String MACRO_URL = "effects_short/Effects_Macro.html";
 
     private static final String CONSUME_URL = "effects_short/Effects_Consume.html";
 
     private static final String GENERATE_URL = "effects_short/Effects_Generate.html";
 
     private static final String CANCEL_URL = "effects_short/Effects_Cancel.html";
 
     private static final String SP_PLAYER_URL = "effects_short/Effects_SP_Player.html";
 
     private static final String SP_NPC_URL = "effects_short/Effects_SP_NPC.html";
 
     private static final String BOOK_URL = "effects_short/Effects_Book.html";
 
     private static final String SOUND_URL = "effects_short/Effects_Audio.html";
 
     private static final String ANIMATION_URL = "effects_short/Effects_Animation.html";
 
     private static final String MV_PLAYER_URL = "effects_short/Effects_MV_Player.html";
 
     private static final String MV_NPC_URL = "effects_short/Effects_MV_NPC.html";
 
     private static final String CONV_URL = "effects_short/Effects_Conversation.html";
 
     private static final String CUTSCENE_URL = "effects_short/Effects_Cutscene.html";
 
     private static final String SCENE_URL = "effects_short/Effects_Scene.html";
 
     private static final String LAST_SCENE_URL = "effects_short/Effects_LastScene.html";
 
     private static final String RAMDON_URL = "effects_short/Effects_Random.html";
 
     private static final String TEXT_URL = "effects_short/Effects_ShowText.html";
 
     private static final String TIME_URL = "effects_short/Effects_WaitTime.html";
     
     private static final String HIGHLIGHT_URL = "effects_short/Effects_HighlightItem.html";
     
     private static final String MOVE_OBJECT_URL = "effects_short/Effects_MoveObject.html";
 
     private EffectInfoPanel infoPanel;
 
     private boolean showAll;
 
     private SelectEffectsDialog dialog;
 
     /*
      * Constants for icon size in buttons. Three sizes are available: SMALL (16x16), MEDIUM (32x32) and LARGE (64x64)
      */
     public static final int ICON_SIZE_SMALL = 0;
 
     public static final int ICON_SIZE_MEDIUM = 1;
 
     public static final int ICON_SIZE_LARGE = 2;
 
     public static final int ICON_SIZE_LARGE_HOT = 3;
 
     private static String getIconBasePath( int size ) {
 
         if( size == ICON_SIZE_SMALL )
             return "img/icons/effects/16x16/";
         else if( size == ICON_SIZE_LARGE )
             return "img/icons/effects/64x64/";
         else if( size == ICON_SIZE_LARGE_HOT )
             return "img/icons/effects/64x64-hot/";
         else
             return "img/icons/effects/32x32/";
     }
 
     public static Icon getEffectIcon( String name, int size ) {
 
         Icon effectIcon = null;
 
         if( name.equals( TC.get( "Effect.Activate" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "activate.png" );
         }
         else if( name.equals( TC.get( "Effect.Deactivate" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "deactivate.png" );
         }
         else if( name.equals( TC.get( "Effect.SetValue" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "set-value.png" );
         }
         else if( name.equals( TC.get( "Effect.IncrementVar" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "increment.png" );
         }
         else if( name.equals( TC.get( "Effect.DecrementVar" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "decrement.png" );
         }
         else if( name.equals( TC.get( "Effect.MacroReference" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "macro.png" );
         }
         else if( name.equals( TC.get( "Effect.ConsumeObject" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "consume-object.png" );
         }
         else if( name.equals( TC.get( "Effect.GenerateObject" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "generate-object.png" );
         }
         else if( name.equals( TC.get( "Effect.CancelAction" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "cancel-action.png" );
         }
         else if( name.equals( TC.get( "Effect.SpeakPlayer" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "speak-player.png" );
         }
         else if( name.equals( TC.get( "Effect.SpeakCharacter" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "speak-npc.png" );
         }
         else if( name.equals( TC.get( "Effect.TriggerBook" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "trigger-book.png" );
         }
         else if( name.equals( TC.get( "Effect.PlaySound" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "play-sound.png" );
         }
         else if( name.equals( TC.get( "Effect.PlayAnimation" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "play-animation.png" );
         }
         else if( name.equals( TC.get( "Effect.MovePlayer" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "move-player.png" );
         }
         else if( name.equals( TC.get( "Effect.MoveCharacter" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "move-npc.png" );
         }
         else if( name.equals( TC.get( "Effect.TriggerConversation" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "trigger-conversation.png" );
         }
         else if( name.equals( TC.get( "Effect.TriggerCutscene" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "trigger-cutscene.png" );
         }
         else if( name.equals( TC.get( "Effect.TriggerScene" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "trigger-scene.png" );
         }
         else if( name.equals( TC.get( "Effect.TriggerLastScene" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "trigger-last-scene.png" );
         }
         else if( name.equals( TC.get( "Effect.RandomEffect" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "random-effect.png" );
         }
         else if( name.equals( TC.get( "Effect.ShowText" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "show-text.png" );
         }
         else if( name.equals( TC.get( "Effect.WaitTime" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "wait.png" );
         }
         else if (name.equals( TC.get( "Effect.HighlightItem" ) )) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "highlight-item.png");
         }
         else if (name.equals( TC.get( "Effect.MoveObject" ) )) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "move-object.png");
         }
         else if( name.equals( TC.get( "EffectsGroup.GameState" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "game-state.png" );
         }
         else if( name.equals( TC.get( "EffectsGroup.Multimedia" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "multimedia.png" );
         }
         else if( name.equals( TC.get( "EffectsGroup.Miscellaneous" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "miscellaneous.png" );
         }
         else if( name.equals( TC.get( "EffectsGroup.Trigger" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "trigger-events.png" );
         }
         else if( name.equals( TC.get( "EffectsGroup.Feedback" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "feedback.png" );
         }
         else if( name.equals( TC.get( "EffectsGroup.ChangeInScene" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "changes-in-scene.png" );
         }
        else if( name.equals( TC.get( "EffectsGroup.Main" ) ) ) {
             effectIcon = new ImageIcon( getIconBasePath( size ) + "effects" );
         }
         // when this method is called for structure list effects
         else
             effectIcon = new ImageIcon( getIconBasePath( size ) + "effects" );
 
         return effectIcon;
 
     }
 
     public EffectsStructurePanel( boolean showAll, SelectEffectsDialog dialog ) {
 
         super( null, 30, 55 );
         this.showAll = showAll;
         this.dialog = dialog;
         infoPanel = new EffectInfoPanel( );
         recreateElements( );
         //StructureControl.getInstance().setStructurePanel(this);
 
         changeEffectEditPanel( ( (EffectsStructureListElement) structureElements.get( 0 ) ).getPath( ) );
     }
 
     public String getSelectedEffect( ) {
 
         if( /*selectedElement==0 ||*/list.getSelectedRow( ) == -1 )
             return null;
         return structureElements.get( selectedElement ).getChild( list.getSelectedRow( ) ).getName( );
     }
 
     @Override
     public void recreateElements( ) {
 
         structureElements.clear( );
         if( showAll )
             structureElements.add( new MainStructureListElement( ) );
         else {
             structureElements.add( new GameStateStructureListElement( ) );
             structureElements.add( new MultimediaStructureListElement( ) );
             structureElements.add( new FeedbackStructureListElement( ) );
             structureElements.add( new TriggerStructureListElement( ) );
             structureElements.add( new ChangesInSceneStructureListElement( ) );
             structureElements.add( new MiscelaneousStructureListElement( ) );
         }
         update( );
     }
 
     @Override
     public void update( ) {
 
         super.update( );
         int i = 0;
         removeAll( );
 
         for( StructureListElement element : structureElements ) {
             if( i == selectedElement )
                 add( createSelectedElementPanel( element, i ), new Integer( element.getChildCount( ) != 0 ? -1 : 40 ) );
             else {
                 button = new JButton( element.getName( ), element.getIcon( ) );
                 button.setHorizontalAlignment( SwingConstants.LEFT );
                 Border b1 = BorderFactory.createRaisedBevelBorder( );
                 Border b2 = BorderFactory.createEmptyBorder( 3, 10, 3, 10 );
                 button.setBorder( BorderFactory.createCompoundBorder( b1, b2 ) );
                 button.setContentAreaFilled( false );
                 button.addActionListener( new ListElementButtonActionListener( i ) );
                 button.setFocusable( false );
                 if( i < selectedElement )
                     //add(button, new Integer(selectedElement == 0?15:35));
                     add( button, new Integer( 35 ) );
                 else if( i > selectedElement )
                     //add(button, new Integer(selectedElement == 0?15:35));
                     add( button, new Integer( 35 ) );
             }
             i++;
         }
         this.updateUI( );
     }
 
     @Override
     protected JPanel createSelectedElementPanel( final StructureListElement element, final int index ) {
 
         JPanel result = super.createSelectedElementPanel( element, index );
         button.addActionListener( new ListElementButtonActionListener( index ) );
         list.addMouseListener( new MouseAdapter( ) {
 
             @Override
             public void mouseClicked( MouseEvent e ) {
 
                 if( e.getClickCount( ) == 2 ) {
                     dialog.setOk( true );
                 }
             }
         } );
         list.getSelectionModel( ).addListSelectionListener( new ListSelectionListener( ) {
 
             public void valueChanged( ListSelectionEvent e ) {
 
                 if( list.getSelectedRow( ) >= 0 ) {
                     list.setRowHeight( 20 );
                     list.setRowHeight( list.getSelectedRow( ), 30 );
                     list.editCellAt( list.getSelectedRow( ), 0 );
                     changeEffectEditPanel( getSelectedEffect( ) );
                 }
                 else {
                     changeEffectEditPanel( ( (EffectsStructureListElement) structureElements.get( index ) ).getPath( ) );
                 }
 
             }
         } );
         return result;
     }
 
     /**
      * @return the infoPanel
      */
     public EffectInfoPanel getInfoPanel( ) {
 
         return infoPanel;
     }
 
     /**
      * @param infoPanel
      *            the infoPanel to set
      */
     public void setInfoPanel( EffectInfoPanel infoPanel ) {
 
         this.infoPanel = infoPanel;
     }
 
     private void changeEffectEditPanel( String name ) {
 
         String text = null;
         if( name.equals( TC.get( "Effect.Activate" ) ) ) {
             text = ACTIVATE_URL;
         }
         else if( name.equals( TC.get( "Effect.Deactivate" ) ) ) {
             text = DEACTIVATE_URL;
         }
         else if( name.equals( TC.get( "Effect.SetValue" ) ) ) {
             text = SET_URL;
         }
         else if( name.equals( TC.get( "Effect.IncrementVar" ) ) ) {
             text = INCR_URL;
         }
         else if( name.equals( TC.get( "Effect.DecrementVar" ) ) ) {
             text = DECR_URL;
         }
         else if( name.equals( TC.get( "Effect.MacroReference" ) ) ) {
             text = MACRO_URL;
         }
         else if( name.equals( TC.get( "Effect.ConsumeObject" ) ) ) {
             text = CONSUME_URL;
         }
         else if( name.equals( TC.get( "Effect.GenerateObject" ) ) ) {
             text = GENERATE_URL;
         }
         else if( name.equals( TC.get( "Effect.CancelAction" ) ) ) {
             text = CANCEL_URL;
         }
         else if( name.equals( TC.get( "Effect.SpeakPlayer" ) ) ) {
             text = SP_PLAYER_URL;
         }
         else if( name.equals( TC.get( "Effect.SpeakCharacter" ) ) ) {
             text = SP_NPC_URL;
         }
         else if( name.equals( TC.get( "Effect.TriggerBook" ) ) ) {
             text = BOOK_URL;
         }
         else if( name.equals( TC.get( "Effect.PlaySound" ) ) ) {
             text = SOUND_URL;
         }
         else if( name.equals( TC.get( "Effect.PlayAnimation" ) ) ) {
             text = ANIMATION_URL;
         }
         else if( name.equals( TC.get( "Effect.MovePlayer" ) ) ) {
             text = MV_PLAYER_URL;
         }
         else if( name.equals( TC.get( "Effect.MoveCharacter" ) ) ) {
             text = MV_NPC_URL;
         }
         else if( name.equals( TC.get( "Effect.TriggerConversation" ) ) ) {
             text = CONV_URL;
         }
         else if( name.equals( TC.get( "Effect.TriggerCutscene" ) ) ) {
             text = CUTSCENE_URL;
         }
         else if( name.equals( TC.get( "Effect.TriggerScene" ) ) ) {
             text = SCENE_URL;
         }
         else if( name.equals( TC.get( "Effect.TriggerLastScene" ) ) ) {
             text = LAST_SCENE_URL;
         }
         else if( name.equals( TC.get( "Effect.RandomEffect" ) ) ) {
             text = RAMDON_URL;
         }
         else if( name.equals( TC.get( "Effect.ShowText" ) ) ) {
             text = TEXT_URL;
         }
         else if( name.equals( TC.get( "Effect.WaitTime" ) ) ) {
             text = TIME_URL;
         }
         else if( name.equals( TC.get( "Effect.HighlightItem" ) )) {
             text = HIGHLIGHT_URL;
         }
         else if( name.equals( TC.get( "Effect.MoveObject" ))) {
             text = MOVE_OBJECT_URL;
         }
         // when this method is called for structure list effects
         else
             text = name;
 
         infoPanel.setHTMLText( text );
 
     }
 
     private class ListElementButtonActionListener implements ActionListener {
 
         private int index;
 
         public ListElementButtonActionListener( int index ) {
 
             this.index = index;
         }
 
         public void actionPerformed( ActionEvent arg0 ) {
 
             selectedElement = index;
             update( );
             changeEffectEditPanel( ( (EffectsStructureListElement) structureElements.get( selectedElement ) ).getPath( ) );
             list.requestFocusInWindow( );
         }
     }
 
 }

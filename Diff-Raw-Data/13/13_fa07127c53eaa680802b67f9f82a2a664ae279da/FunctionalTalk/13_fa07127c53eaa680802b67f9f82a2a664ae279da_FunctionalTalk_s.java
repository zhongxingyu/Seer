 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *         research group.
  *  
  *   Copyright 2005-2010 <e-UCM> research group.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *         http://e-adventure.e-ucm.es/contributors
  * 
  *   <e-UCM> is a research group of the Department of Software Engineering
  *         and Artificial Intelligence at the Complutense University of Madrid
  *         (School of Computer Science).
  * 
  *         C Profesor Jose Garcia Santesmases sn,
  *         28040 Madrid (Madrid), Spain.
  * 
  *         For more info please visit:  <http://e-adventure.e-ucm.es> or
  *         <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  *     <e-Adventure> is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     <e-Adventure> is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU Lesser General Public License for more details.
  * 
  *     You should have received a copy of the GNU Lesser General Public License
  *     along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.engine.core.control.functionaldata.functionalactions;
 
 import java.util.List;
 
 import es.eucm.eadventure.common.data.chapter.Action;
 import es.eucm.eadventure.engine.core.control.ActionManager;
 import es.eucm.eadventure.engine.core.control.DebugLog;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.animations.AnimationState;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalConditions;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalElement;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalNPC;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalPlayer;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionaleffects.FunctionalEffects;
 import es.eucm.eadventure.engine.core.data.GameText;
 
 /**
  * The action to talk with an npc
  * 
  * @author Eugenio Marchiori
  */
 public class FunctionalTalk extends FunctionalAction {
 
     /**
      * The default distance to keep between player and npc
      */
     public static final int DEFAULT_DISTANCE_TO_KEEP = 35;
 
     /**
      * The functional npc to speak with
      */
     FunctionalNPC npc;
 
     /**
      * True if there is a conversation
      */
     private boolean anyConversation;
 
     /**
      * Constructor using a default action and the npc with which to talk
      * 
      * @param action
      *            The original action
      * @param npc
      *            The npc with which to talk
      */
     public FunctionalTalk( Action action, FunctionalElement npc ) {
 
         super( action );
         this.npc = (FunctionalNPC) npc;
         this.type = ActionManager.ACTION_TALK;
         if( functionalPlayer != null )
             keepDistance = this.functionalPlayer.getWidth( ) / 2;
         else
             keepDistance = DEFAULT_DISTANCE_TO_KEEP;
         keepDistance += npc.getWidth( ) / 2;
 
         List<Action> actions = this.npc.getNPC( ).getActions( );
 
         anyConversation = false;
         for( int i = 0; i < actions.size( ) && !anyConversation; i++ )
            if( actions.get( i ).getType( ) == Action.TALK_TO && new FunctionalConditions( actions.get( i ).getConditions( ) ).allConditionsOk( ) )
                 anyConversation = true;
 
         if( anyConversation ) {
             needsGoTo = true;
         }
         else {
             needsGoTo = false;
         }
     }
 
     @Override
     public void drawAditionalElements( ) {
 
     }
 
     @Override
     public void setAnotherElement( FunctionalElement element ) {
 
     }
 
     @Override
     public void start( FunctionalPlayer functionalPlayer ) {
 
         this.functionalPlayer = functionalPlayer;
 
         if( anyConversation ) {
             if( npc != null ) {
                 if( npc.getX( ) > functionalPlayer.getX( ) )
                     functionalPlayer.setDirection( AnimationState.EAST );
                 else
                     functionalPlayer.setDirection( AnimationState.WEST );
             }
             finished = false;
         }
         else {
             if( functionalPlayer.isAlwaysSynthesizer( ) )
                 functionalPlayer.speakWithFreeTTS( GameText.getTextTalkCannot( ), functionalPlayer.getPlayerVoice( ) );
             else
                 functionalPlayer.speak( GameText.getTextTalkCannot( ), Game.getInstance().getGameDescriptor( ).isKeepShowing( ) );
             finished = true;
         }
 
         DebugLog.player( "Start talk : " + npc.getNPC( ).getId( ) );
     }
 
     @Override
     public void stop( ) {
 
         finished = true;
     }
 
     @Override
     public void update( long elapsedTime ) {
 
         List<Action> actions = npc.getNPC( ).getActions( );
         boolean triggeredConversation = false;
 
         for( int i = 0; i < actions.size( ) && !triggeredConversation; i++ ) {
             if( actions.get( i ).getType( ) == Action.TALK_TO && new FunctionalConditions( actions.get( i ).getConditions( ) ).allConditionsOk( ) ) {
                 Game.getInstance( ).setCurrentNPC( npc );
                 FunctionalEffects.storeAllEffects( actions.get( i ).getEffects( ) );
                 triggeredConversation = true;
             }
         }
 
         if( !triggeredConversation ) {
             if( functionalPlayer.isAlwaysSynthesizer( ) )
                 functionalPlayer.speakWithFreeTTS( GameText.getTextTalkCannot( ), functionalPlayer.getPlayerVoice( ) );
             else
                 functionalPlayer.speak( GameText.getTextTalkCannot( ), Game.getInstance().getGameDescriptor( ).isKeepShowing( ) );
         }
         finished = true;
     }
 }

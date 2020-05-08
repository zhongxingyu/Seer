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
 package es.eucm.eadventure.editor.control.tools.general.effects;
 
 import java.util.List;
 
 import es.eucm.eadventure.common.data.chapter.effects.AbstractEffect;
 import es.eucm.eadventure.common.data.chapter.effects.ActivateEffect;
 import es.eucm.eadventure.common.data.chapter.effects.ConsumeObjectEffect;
 import es.eucm.eadventure.common.data.chapter.effects.DeactivateEffect;
 import es.eucm.eadventure.common.data.chapter.effects.DecrementVarEffect;
 import es.eucm.eadventure.common.data.chapter.effects.Effect;
 import es.eucm.eadventure.common.data.chapter.effects.Effects;
 import es.eucm.eadventure.common.data.chapter.effects.GenerateObjectEffect;
 import es.eucm.eadventure.common.data.chapter.effects.HighlightItemEffect;
 import es.eucm.eadventure.common.data.chapter.effects.IncrementVarEffect;
 import es.eucm.eadventure.common.data.chapter.effects.MacroReferenceEffect;
 import es.eucm.eadventure.common.data.chapter.effects.MoveNPCEffect;
 import es.eucm.eadventure.common.data.chapter.effects.MoveObjectEffect;
 import es.eucm.eadventure.common.data.chapter.effects.MovePlayerEffect;
 import es.eucm.eadventure.common.data.chapter.effects.PlayAnimationEffect;
 import es.eucm.eadventure.common.data.chapter.effects.PlaySoundEffect;
 import es.eucm.eadventure.common.data.chapter.effects.RandomEffect;
 import es.eucm.eadventure.common.data.chapter.effects.SetValueEffect;
 import es.eucm.eadventure.common.data.chapter.effects.ShowTextEffect;
 import es.eucm.eadventure.common.data.chapter.effects.SpeakCharEffect;
 import es.eucm.eadventure.common.data.chapter.effects.SpeakPlayerEffect;
 import es.eucm.eadventure.common.data.chapter.effects.TriggerBookEffect;
 import es.eucm.eadventure.common.data.chapter.effects.TriggerConversationEffect;
 import es.eucm.eadventure.common.data.chapter.effects.TriggerCutsceneEffect;
 import es.eucm.eadventure.common.data.chapter.effects.TriggerSceneEffect;
 import es.eucm.eadventure.common.data.chapter.effects.WaitTimeEffect;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.ConditionsController;
 import es.eucm.eadventure.editor.control.tools.Tool;
 
 
 /**
  * Edition tool for adding an effect
  * 
  * @author Javier
  * 
  */
 public class AddEffectTool extends Tool {
 
     protected Effects effects;
 
     protected AbstractEffect effectToAdd;
 
     protected List<ConditionsController> conditions;
 
     protected ConditionsController condition;
 
     public AddEffectTool( Effects effects, AbstractEffect effectToAdd, List<ConditionsController> conditions ) {
 
         this.effects = effects;
         this.effectToAdd = effectToAdd;
         this.conditions = conditions;
     }
 
     @Override
     public boolean canRedo( ) {
 
         return true;
     }
 
     @Override
     public boolean canUndo( ) {
 
         return true;
     }
 
     @Override
     public boolean combine( Tool other ) {
 
         return false;
     }
 
     @Override
     public boolean doTool( ) {
 
         effects.add( effectToAdd );
         if( conditions != null ) {
            condition = new ConditionsController( effectToAdd.getConditions( ), Controller.EFFECT, getEffectInfo( effectToAdd ) );
             conditions.add( condition );
         }
 
         return true;
     }
 
     @Override
     public boolean redoTool( ) {
 
         boolean done = doTool( );
         if( done ) {
             Controller.getInstance( ).updateVarFlagSummary( );
             Controller.getInstance( ).updatePanel( );
         }
         return done;
     }
 
     @Override
     public boolean undoTool( ) {
 
         effects.getEffects( ).remove( effectToAdd );
         if( conditions != null ) {
             conditions.remove( condition );
         }
         Controller.getInstance( ).updateVarFlagSummary( );
         Controller.getInstance( ).updatePanel( );
         return true;
     }
 
    protected static String getEffectInfo( AbstractEffect effect ) {
 
         String effectInfo = null;
 
         switch( effect.getType( ) ) {
             case Effect.ACTIVATE:
                 ActivateEffect activateEffect = (ActivateEffect) effect;
                 effectInfo = TC.get( "Effect.ActivateInfo", activateEffect.getTargetId( ) );
                 break;
             case Effect.DEACTIVATE:
                 DeactivateEffect deactivateEffect = (DeactivateEffect) effect;
                 effectInfo = TC.get( "Effect.DeactivateInfo", deactivateEffect.getTargetId( ) );
                 break;
             case Effect.SET_VALUE:
                 SetValueEffect setValueEffect = (SetValueEffect) effect;
                 effectInfo = TC.get( "Effect.SetValueInfo", new String[] { setValueEffect.getTargetId( ), Integer.toString( setValueEffect.getValue( ) ) } );
                 break;
             case Effect.INCREMENT_VAR:
                 IncrementVarEffect incrementEffect = (IncrementVarEffect) effect;
                 effectInfo = TC.get( "Effect.IncrementVarInfo", new String[] { incrementEffect.getTargetId( ), Integer.toString( incrementEffect.getIncrement( ) ) } );
                 break;
             case Effect.DECREMENT_VAR:
                 DecrementVarEffect decrementEffect = (DecrementVarEffect) effect;
                 effectInfo = TC.get( "Effect.DecrementVarInfo", new String[] { decrementEffect.getTargetId( ), Integer.toString( decrementEffect.getDecrement( ) ) } );
                 break;
             case Effect.MACRO_REF:
                 MacroReferenceEffect macroReferenceEffect = (MacroReferenceEffect) effect;
                 effectInfo = TC.get( "Effect.MacroRefInfo", macroReferenceEffect.getTargetId( ) );
                 break;
             case Effect.CONSUME_OBJECT:
                 ConsumeObjectEffect consumeObjectEffect = (ConsumeObjectEffect) effect;
                 effectInfo = TC.get( "Effect.ConsumeObjectInfo", consumeObjectEffect.getTargetId( ) );
                 break;
             case Effect.GENERATE_OBJECT:
                 GenerateObjectEffect generateObjectEffect = (GenerateObjectEffect) effect;
                 effectInfo = TC.get( "Effect.GenerateObjectInfo", generateObjectEffect.getTargetId( ) );
                 break;
             case Effect.CANCEL_ACTION:
                 effectInfo = TC.get( "Effect.CancelActionInfo" );
                 break;
             case Effect.SPEAK_PLAYER:
                 SpeakPlayerEffect speakPlayerEffect = (SpeakPlayerEffect) effect;
                 effectInfo = TC.get( "Effect.SpeakPlayerInfo", speakPlayerEffect.getLine( ) );
                 break;
             case Effect.SPEAK_CHAR:
                 SpeakCharEffect speakCharEffect = (SpeakCharEffect) effect;
                 effectInfo = TC.get( "Effect.SpeakCharacterInfo", new String[] { speakCharEffect.getTargetId( ), speakCharEffect.getLine( ) } );
                 break;
             case Effect.TRIGGER_BOOK:
                 TriggerBookEffect triggerBookEffect = (TriggerBookEffect) effect;
                 effectInfo = TC.get( "Effect.TriggerBookInfo", triggerBookEffect.getTargetId( ) );
                 break;
             case Effect.PLAY_SOUND:
                 PlaySoundEffect playSoundEffect = (PlaySoundEffect) effect;
                 effectInfo = TC.get( "Effect.PlaySoundInfo", playSoundEffect.getPath( ) );
                 break;
             case Effect.PLAY_ANIMATION:
                 PlayAnimationEffect playAnimationEffect = (PlayAnimationEffect) effect;
                 effectInfo = TC.get( "Effect.PlayAnimationInfo", playAnimationEffect.getPath( ) );
                 break;
             case Effect.MOVE_PLAYER:
                 MovePlayerEffect movePlayerEffect = (MovePlayerEffect) effect;
                 effectInfo = TC.get( "Effect.MovePlayerInfo", new String[] { String.valueOf( movePlayerEffect.getX( ) ), String.valueOf( movePlayerEffect.getY( ) ) } );
                 break;
             case Effect.MOVE_NPC:
                 MoveNPCEffect moveNPCEffect = (MoveNPCEffect) effect;
                 effectInfo = TC.get( "Effect.MoveCharacterInfo", new String[] { moveNPCEffect.getTargetId( ), String.valueOf( moveNPCEffect.getX( ) ), String.valueOf( moveNPCEffect.getY( ) ) } );
                 break;
             case Effect.TRIGGER_CONVERSATION:
                 TriggerConversationEffect triggerConversationEffect = (TriggerConversationEffect) effect;
                 effectInfo = TC.get( "Effect.TriggerConversationInfo", triggerConversationEffect.getTargetId( ) );
                 break;
             case Effect.TRIGGER_CUTSCENE:
                 TriggerCutsceneEffect triggerCutsceneEffect = (TriggerCutsceneEffect) effect;
                 effectInfo = TC.get( "Effect.TriggerCutsceneInfo", triggerCutsceneEffect.getTargetId( ) );
                 break;
             case Effect.TRIGGER_SCENE:
                 TriggerSceneEffect triggerSceneEffect = (TriggerSceneEffect) effect;
                 effectInfo = TC.get( "Effect.TriggerSceneInfo", triggerSceneEffect.getTargetId( ) );
                 break;
             case Effect.TRIGGER_LAST_SCENE:
                 effectInfo = TC.get( "Effect.TriggerLastSceneInfo" );
                 break;
             case Effect.RANDOM_EFFECT:
                 RandomEffect randomEffect = (RandomEffect) effect;
                 String posInfo = "";
                 String negInfo = "";
                 if( randomEffect.getPositiveEffect( ) != null )
                     posInfo = getEffectInfo( randomEffect.getPositiveEffect( ) );
                 negInfo = getEffectInfo( randomEffect.getNegativeEffect( ) );
                 effectInfo = TC.get( "Effect.RandomInfo", new String[] { Integer.toString( randomEffect.getProbability( ) ), Integer.toString( 100 - randomEffect.getProbability( ) ), posInfo, negInfo } );
                 break;
             case Effect.WAIT_TIME:
                 WaitTimeEffect waitTimeEffect = (WaitTimeEffect) effect;
                 effectInfo = TC.get( "Effect.WaitTimeInfo", Integer.toString( waitTimeEffect.getTime( ) ) );
                 break;
             case Effect.SHOW_TEXT:
                 ShowTextEffect showTextInfo = (ShowTextEffect) effect;
                 effectInfo = TC.get( "Effect.ShowTextInfo", new String[] { showTextInfo.getText( ), Integer.toString( showTextInfo.getX( ) ), Integer.toString( showTextInfo.getY( ) ) } );
                 break;
             case Effect.HIGHLIGHT_ITEM:
                 HighlightItemEffect highlightItemEffect = (HighlightItemEffect) effect;
                 if (highlightItemEffect.getHighlightType( ) == HighlightItemEffect.NO_HIGHLIGHT)
                     effectInfo = TC.get( "Effect.NoHighlightItemInfo", new String[] { highlightItemEffect.getTargetId( ) } );
                 if (highlightItemEffect.getHighlightType( ) == HighlightItemEffect.HIGHLIGHT_BLUE)
                     effectInfo = TC.get( "Effect.BlueHighlightItemInfo", new String[] { highlightItemEffect.getTargetId( ) } );
                 if (highlightItemEffect.getHighlightType( ) == HighlightItemEffect.HIGHLIGHT_GREEN)
                     effectInfo = TC.get( "Effect.GreenHighlightItemInfo", new String[] { highlightItemEffect.getTargetId( ) } );
                 if (highlightItemEffect.getHighlightType( ) == HighlightItemEffect.HIGHLIGHT_RED)
                     effectInfo = TC.get( "Effect.RedHighlightItemInfo", new String[] { highlightItemEffect.getTargetId( ) } );
                 if (highlightItemEffect.getHighlightType( ) == HighlightItemEffect.HIGHLIGHT_BORDER)
                     effectInfo = TC.get( "Effect.BorderHighlightItemInfo", new String[] { highlightItemEffect.getTargetId( ) } );
                 break;
             case Effect.MOVE_OBJECT:
                 MoveObjectEffect moveObjectEffect = (MoveObjectEffect) effect;
                 effectInfo = TC.get( "Effect.MoveObjectInfo", moveObjectEffect.getTargetId( ) );
                 break;
         }
         
 
         return effectInfo;
     }
     
 }

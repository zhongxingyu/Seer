 /*******************************************************************************
  * eAdventure (formerly <e-Adventure> and <e-Game>) is a research project of the e-UCM
  *          research group.
  *   
  *    Copyright 2005-2012 e-UCM research group.
  *  
  *     e-UCM is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://e-adventure.e-ucm.es> or
  *          <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  * This file is part of eAdventure, version 1.5.
  * 
  *   You can access a list of all the contributors to eAdventure at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       eAdventure is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      eAdventure is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with Adventure.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.engine.core.control.functionaldata;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Transparency;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 import java.util.List;
 
 import es.eucm.eadventure.common.auxiliar.SpecialAssetPaths;
 import es.eucm.eadventure.common.data.chapter.Action;
 import es.eucm.eadventure.common.data.chapter.CustomAction;
 import es.eucm.eadventure.common.data.chapter.Exit;
 import es.eucm.eadventure.common.data.chapter.InfluenceArea;
 import es.eucm.eadventure.common.data.chapter.elements.Element;
 import es.eucm.eadventure.common.data.chapter.elements.NPC;
 import es.eucm.eadventure.common.data.chapter.elements.Player;
 import es.eucm.eadventure.common.data.chapter.resources.Asset;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 import es.eucm.eadventure.common.data.chapter.scenes.Scene;
 import es.eucm.eadventure.engine.core.control.ActionManager;
 import es.eucm.eadventure.engine.core.control.DebugLog;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.animations.Animation;
 import es.eucm.eadventure.engine.core.control.animations.AnimationState;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalAction;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalCustom;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalCustomInteract;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalDragTo;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalExamine;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalGive;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalGoTo;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalGrab;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalLook;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalNullAction;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalSpeak;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalTalk;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalUse;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionalactions.FunctionalUseWith;
 import es.eucm.eadventure.engine.core.data.GameText;
 import es.eucm.eadventure.engine.core.gui.GUI;
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 import es.eucm.eadventure.engine.resourcehandler.ResourceHandler;
 import es.eucm.eadventure.tracking.pub._HighLevelEvents;
 
 /**
  * The player
  */
 public class FunctionalPlayer extends FunctionalElement implements TalkingElement, _HighLevelEvents {
 
     /**
      * Default speed of the player.
      */
     public static final float DEFAULT_SPEED = 120.0f;
 
     public static final float SPEED_TRANSPARENT_MODE = 5000;
 
     /**
      * Speed in the X coordinate.
      */
     protected float speedX;
 
     /**
      * Speed in the Y coordinate.
      */
     protected float speedY;
 
     /**
      * Player containing the data
      */
     private Player player;
 
     /**
      * Front color of the player's text
      */
     private Color textFrontColor;
 
     /**
      * Border color of the player's text
      */
     private Color textBorderColor;
 
     private Color bubbleBkgColor;
 
     private Color bubbleBorderColor;
 
     /**
      * Resources being used by the character
      */
     private Resources resources;
 
     /**
      * The exit where the player is heading
      */
     private Exit targetExit;
 
     /**
      * Current player's direction
      */
     private int currentDirection = -1;
 
     /**
      * Last player's direction set
      */
     //private int lastDirection = -1;
     public List<FunctionalAction> actionPool;
 
     public List<Animation[]> animationPool;
 
     private boolean isTransparent = false;
 
     private float oldScale = -1;
 
     private Image oldImage = null;
 
     private Image oldOriginalImage = null;
     
     private boolean keepShowingGlobal;
 
     /**
      * @return the isTransparent
      */
     public boolean isTransparent( ) {
 
         return isTransparent;
     }
 
     /**
      * @param isTransparent
      *            the isTransparent to set
      */
     public void setTransparent( boolean isTransparent ) {
 
         this.isTransparent = isTransparent;
     }
 
     /**
      * Creates a new FunctionalPlayer
      * 
      * @param player
      *            the player's data
      */
     public FunctionalPlayer( Player player ) {
 
         super( 0, 0 );
         this.player = player;
         speedX = 0;
         speedY = 0;
         layer = -1;
         scale = 1;
         currentDirection = AnimationState.EAST;
         // Select the resources block
         resources = createResourcesBlock( );
 
         actionPool = new ArrayList<FunctionalAction>( );
         animationPool = new ArrayList<Animation[]>( );
 
         animationPool.add( loadPlayerAnimations() );
 
         textFrontColor = generateColor( player.getTextFrontColor( ) );
         textBorderColor = generateColor( player.getTextBorderColor( ) );
         bubbleBkgColor = generateColor( player.getBubbleBkgColor( ) );
         bubbleBorderColor = generateColor( player.getBubbleBorderColor( ) );
         keepShowingGlobal = Game.getInstance( ).getGameDescriptor( ).isKeepShowing( );
     }
 
     
     private Animation[] loadPlayerAnimations(){
         
         MultimediaManager multimedia = MultimediaManager.getInstance( );
         
         Animation[] animations = new Animation[ 4 ];
         
         if( resources.getAssetPath( NPC.RESOURCE_TYPE_STAND_RIGHT ) != null && !resources.getAssetPath( NPC.RESOURCE_TYPE_STAND_RIGHT ).equals( SpecialAssetPaths.ASSET_EMPTY_ANIMATION )
                 && !resources.getAssetPath( NPC.RESOURCE_TYPE_STAND_RIGHT ).equals( SpecialAssetPaths.ASSET_EMPTY_ANIMATION + ".eaa" ) )
             animations[AnimationState.EAST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_STAND_RIGHT ), false, MultimediaManager.IMAGE_PLAYER );
         else
             animations[AnimationState.EAST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_STAND_LEFT ), true, MultimediaManager.IMAGE_PLAYER );
       
         if( resources.getAssetPath( NPC.RESOURCE_TYPE_STAND_LEFT ) != null && !resources.getAssetPath( NPC.RESOURCE_TYPE_STAND_LEFT ).equals( SpecialAssetPaths.ASSET_EMPTY_ANIMATION )
                 && !resources.getAssetPath( NPC.RESOURCE_TYPE_STAND_LEFT ).equals( SpecialAssetPaths.ASSET_EMPTY_ANIMATION + ".eaa" ))
             animations[AnimationState.WEST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_STAND_LEFT ), false, MultimediaManager.IMAGE_PLAYER );
         else
             animations[AnimationState.WEST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_STAND_RIGHT ), true, MultimediaManager.IMAGE_PLAYER );
        
         animations[AnimationState.NORTH] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_STAND_UP ), false, MultimediaManager.IMAGE_PLAYER );
         animations[AnimationState.SOUTH] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_STAND_DOWN ), false, MultimediaManager.IMAGE_PLAYER );
         
         return animations;
     }
     
     public boolean isAlwaysSynthesizer( ) {
 
         return player.isAlwaysSynthesizer( );
     }
 
     public String getPlayerVoice( ) {
 
         return player.getVoice( );
     }
 
     /**
      * Updates the resources of the npc (if the current resources and the new
      * one are different)
      */
     public void updateResources( ) {
 
         // Get the new resources
         Resources newResources = createResourcesBlock( );
 
         // If the resources have changed, load the new one
         if( resources != newResources ) {
             resources = newResources;
 
             // Flush the past resources from the images cache
             MultimediaManager.getInstance( ).flushImagePool( MultimediaManager.IMAGE_PLAYER );
             this.animationPool.clear( );
             this.animationPool.add( loadPlayerAnimations() );
         }
     }
 
     @Override
     public Element getElement( ) {
 
         return player;
     }
 
     @Override
     public int getWidth( ) {
 
         return getCurrentAnimation( ).getImage( ).getWidth( null );
     }
 
     @Override
     public int getHeight( ) {
 
         return getCurrentAnimation( ).getImage( ).getHeight( null );
     }
 
     /**
      * Adds a new action to the pool. The walking action will be added as
      * needed.
      * 
      * @param action
      *            The action to add to the pool
      * @param element
      *            The element on with the action is performed
      */
     public void addAction( FunctionalAction action ) {
 
         actionPool.add( action );
     }
 
     /**
      * Cancel all actions currently being performed
      */
     public void cancelActions( ) {
 
         getCurrentAction( ).stop( );
         actionPool.clear( );
         cancelAnimations( );
     }
 
     public FunctionalAction getCurrentAction( ) {
 
         if( actionPool.size( ) > 0 )
             return actionPool.get( actionPool.size( ) - 1 );
         else
             return new FunctionalNullAction( );
     }
 
     public void popAction( ) {
 
         if( actionPool.size( ) > 0 )
             actionPool.remove( actionPool.size( ) - 1 );
     }
 
     /**
      * Adds a new animation to the animation pool, the exact result depends on
      * whether the animation is to be repeated indefinitely or a limited number
      * of times.
      * 
      * @param animation
      * @param repeat
      */
     public void setAnimation( Animation[] animations, int repeat ) {
 
         //TODO check behavior
         if( repeat != -1 ) {
             animationPool.add( animations );
         }
         else {
             if( animationPool.size( ) > 1 )
                 animationPool.remove( animationPool.size( ) - 1 );
             animationPool.add( animations );
         }
     }
 
     /**
      * When an action is completed, it is likely it would like to remove the
      * animation it set
      */
     public void popAnimation( ) {
 
         if( animationPool.size( ) > 1 ) {
             animationPool.remove( animationPool.size( ) - 1 );
         }
     }
 
     public Animation getCurrentAnimation( ) {
 
         if( currentDirection >= 0 && currentDirection < 4 )
             return animationPool.get( animationPool.size( ) - 1 )[currentDirection];
         else
             return animationPool.get( animationPool.size( ) - 1 )[0];
     }
 
     public void cancelAnimations( ) {
 
         if( animationPool.size( ) > 0 ) {
             Animation[] temp = animationPool.get( 0 );
             animationPool.clear( );
             animationPool.add( temp );
         }
     }
 
     /**
      * Performs the given action with the given element
      * 
      * @param element
      *            the element that will receive the action
      * @param actionSelected
      *            the action to be performed
      */
     public void performActionInElement( FunctionalElement element ) {
 
         Game game = Game.getInstance( );
         int actionSelected = Game.getInstance( ).getActionManager( ).getActionSelected( );
         if( actionSelected == ActionManager.ACTION_LOOK ) {
             game.getGameLog( ).highLevelEvent( LOOK, element.getElement( ).getId( ) );
             addAction( new FunctionalLook( element ) );
             return;
         } 
         
         FunctionalAction nextAction = new FunctionalNullAction( );
         switch( actionSelected ) {
             case ActionManager.ACTION_EXAMINE:
                 cancelActions( );
                 game.getGameLog( ).highLevelEvent( EXAMINE, element.getElement( ).getId( ) );
                 nextAction = new FunctionalExamine( null, element );
                 break;
             case ActionManager.ACTION_GIVE:
                 game.getGameLog( ).highLevelEvent( START_GIVE_TO, element.getElement( ).getId( ) );
                 if( element.canPerform( actionSelected ) ) {
                     if( element.isInInventory( ) ) {
                         cancelActions( );
                         nextAction = new FunctionalGive( null, element );
                         game.getActionManager( ).setActionSelected( ActionManager.ACTION_GIVE_TO );
                     }
                     else {
                         if( player.isAlwaysSynthesizer( ) )
                             speakWithFreeTTS( GameText.getTextGiveObjectNotInventory( ), player.getVoice( ) );
                         else
                             speak( GameText.getTextGiveObjectNotInventory( ), keepShowingGlobal );
                     }
                 }
                 else {
                     if( player.isAlwaysSynthesizer( ) )
                         speakWithFreeTTS( GameText.getTextGiveNPC( ), player.getVoice( ) );
                     else
                         speak( GameText.getTextGiveNPC( ), keepShowingGlobal );
                 }
                 break;
             case ActionManager.ACTION_GIVE_TO:
                 game.getGameLog( ).highLevelEvent( END_GIVE_TO, ( (FunctionalGive) getCurrentAction() ).getElement( ).getElement( ).getId( ),
                         element.getElement( ).getId( ) );
                 if( element.canPerform( actionSelected ) ) {
                     if( getCurrentAction( ).getType( ) == ActionManager.ACTION_GIVE ) {
                         nextAction = getCurrentAction( );
                         popAction( );
                         ( (FunctionalGive) nextAction ).setAnotherElement( element );
                     }
                 }
                 else {
                     popAction( );
                     speak( GameText.getTextGiveCannot( ), keepShowingGlobal );
                 }
                 break;
             case ActionManager.ACTION_GRAB:
                 cancelActions( );
                 game.getGameLog( ).highLevelEvent( GRAB, element.getElement( ).getId( ) );
                 if( element.canPerform( actionSelected ) ) {
                     if( !element.isInInventory( ) ) {
                         nextAction = new FunctionalGrab( null, element );
                     }
                     else
                         speak( GameText.getTextGrabObjectInventory( ), keepShowingGlobal );
                 }
                 else
                     speak( GameText.getTextGrabNPC( ), keepShowingGlobal );
                 break;
             case ActionManager.ACTION_TALK:
                 game.getGameLog( ).highLevelEvent( TALK, element.getElement( ).getId( ) );
                 cancelActions( );
                 if( element.canPerform( actionSelected ) ) {
                     nextAction = new FunctionalTalk( null, element );
                 }
                 else
                     speak( GameText.getTextTalkObject( ), keepShowingGlobal );
                 break;
             case ActionManager.ACTION_USE:
                 
                 if( element.canPerform( actionSelected ) ) {
                     if( element.canBeUsedAlone( ) ) {
                         game.getGameLog( ).highLevelEvent( USE, element.getElement( ).getId( ) );
                         cancelActions( );
                         nextAction = new FunctionalUse( element );
                     }
                     else {
                         game.getGameLog( ).highLevelEvent( START_USE_WITH, element.getElement( ).getId( ) );
                         cancelActions( );
                         nextAction = new FunctionalUseWith( null, element );
                         game.getActionManager( ).setActionSelected( ActionManager.ACTION_USE_WITH );
                     }
                 }
                 else {
                     game.getGameLog( ).highLevelEvent( USE, element.getElement( ).getId( ) );
                     popAction( );
                     speak( GameText.getTextUseNPC( ), keepShowingGlobal );
                 }
                 break;
             case ActionManager.ACTION_DRAG_TO:
                 if (getCurrentAction().getType( ) == Action.DRAG_TO) {
                     game.getGameLog( ).highLevelEvent( END_DRAG_TO, ( (FunctionalDragTo) getCurrentAction() ).getElement( ).getElement( ).getId( ),
                             element.getElement( ).getId( ) );
                     nextAction = getCurrentAction();
                     popAction();
                     if (nextAction != null && element != null && nextAction instanceof FunctionalDragTo) {
                         ((FunctionalDragTo) nextAction).setAnotherElement(element);
                     }
                 }
                 else {
                     game.getGameLog( ).highLevelEvent( START_DRAG_TO, element.getElement( ).getId( ) );
                     nextAction = new FunctionalDragTo( element );
                     game.getActionManager( ).setActionSelected( ActionManager.ACTION_DRAG_TO );
                 }
                 break;
             case ActionManager.ACTION_CUSTOM:
                 game.getGameLog( ).highLevelEvent( CUSTOM+": "+game.getActionManager( ).getCustomActionName( ), element.getElement( ).getId( ) );
               //      if (element)
                 nextAction = new FunctionalCustom( element, Game.getInstance( ).getActionManager( ).getCustomActionName( ) );
                 break;
             case ActionManager.ACTION_CUSTOM_INTERACT:
                 if( getCurrentAction( ).getType( ) == Action.CUSTOM_INTERACT ) {
                     game.getGameLog( ).highLevelEvent( END_CUSTOM_INTERACTION+": "+game.getActionManager( ).getCustomActionName( ),
                             ( (FunctionalCustomInteract) getCurrentAction() ).getElement( ).getElement( ).getId( ),
                             element.getElement( ).getId( ) );
                     nextAction = getCurrentAction( );
                     popAction( );
                     if( nextAction != null && element != null && nextAction instanceof FunctionalCustomInteract ) {
                         ( (FunctionalCustomInteract) nextAction ).setAnotherElement( element );
                     }
                 }
                 else {
                     game.getGameLog( ).highLevelEvent( START_CUSTOM_INTERACTION+": "+game.getActionManager( ).getCustomActionName( ), element.getElement( ).getId( ) );
                     nextAction = new FunctionalCustomInteract( element, Game.getInstance( ).getActionManager( ).getCustomActionName( ) );
                 }
                 break;
             case ActionManager.ACTION_USE_WITH:
                 if( element.canPerform( actionSelected ) ) {
                     game.getGameLog( ).highLevelEvent( END_USE_WITH+": "+game.getActionManager( ).getCustomActionName( ),
                             ( (FunctionalUseWith) getCurrentAction() ).getElement( ).getElement( ).getId( ),
                             element.getElement( ).getId( ) );
                     
                     if( getCurrentAction( ).getType( ) == ActionManager.ACTION_USE_WITH ) {
                         
                         nextAction = getCurrentAction( );
                         popAction( );
                         ( (FunctionalUseWith) nextAction ).setAnotherElement( element );
                     }
                }
                 else {
                     popAction( );
                     speak( GameText.getTextUseNPC( ), keepShowingGlobal );
                 }
                 break;
         }
 
      // this method has been added to solve inconsistencies between available actions and actions buttons being displayed
        if (nextAction.getType( ) == ActionManager.ACTION_TALK ||
                nextAction.hasOriginalAction( )){
         
         if( nextAction.isNeedsGoTo( ) && !this.isTransparent ) {
             FunctionalGoTo functionalGoTo = new FunctionalGoTo( null, (int) element.getX( ), (int) element.getY( ), this, element );
             if( functionalGoTo.canGetTo( ) ) {
                 addAction( nextAction );
                 functionalGoTo.setKeepDistance( nextAction.getKeepDistance( ) );
                 addAction( functionalGoTo );
             }
             else {
                 //addAction( functionalGoTo ); this avoid a lock of player
             }
             return;
         }
         else
             addAction( nextAction );
         return;
         } else {
             // ensure to reset action selected when original action is null
             game.getActionManager( ).setActionSelected( -1  );
         }
             
 
     }
 
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.control.functionaldata.Renderable#draw(java.awt.Graphics2D)
      */
     public void draw( ) {
 
         if( !isTransparent ) {
             Image image = getCurrentAnimation( ).getImage( );
 
             int realX = (int) ( x - ( image.getWidth( null ) * scale / 2 ) - Game.getInstance( ).getFunctionalScene( ).getOffsetX( ) );
             int realY = (int) ( y - ( image.getHeight( null ) * scale ) );
 
             if( image == oldOriginalImage && scale == oldScale )
                 image = oldImage;
             else if( scale != 1 ) {
                 oldOriginalImage = image;
                 image = GUI.getInstance( ).getGraphicsConfiguration( ).createCompatibleImage( Math.round( image.getWidth( null ) * scale ),  Math.round( image.getHeight( null ) * scale ), Transparency.TRANSLUCENT );
                 ((Graphics2D) image.getGraphics( )).drawImage( oldOriginalImage, AffineTransform.getScaleInstance( scale, scale ), null );
             }
             else
                 oldOriginalImage = image;
 
             oldScale = scale;
             oldImage = image;
 
             if( layer == Scene.PLAYER_WITHOUT_LAYER || layer == Scene.PLAYER_NO_ALLOWED )
                 GUI.getInstance( ).addPlayerToDraw( image, realX, realY, Math.round( y ), Math.round( y ) );
             else
                 GUI.getInstance( ).addElementToDraw( image, realX, realY, layer, Math.round( y ), null, null );
         }
         if( getCurrentAction( ).isStarted( ) && !getCurrentAction( ).isFinished( ) )
             getCurrentAction( ).drawAditionalElements( );
     }
 
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.control.functionaldata.Renderable#update(long)
      */
     public void update( long elapsedTime ) {
 
         while( getCurrentAction( ).isFinished( ) ) {
             this.popAction( );
         }
         if( !getCurrentAction( ).isStarted( ) ) {
             getCurrentAction( ).start( this );
             //TODO descomentar para activar los efectos en el primer click de interacciones
             //getCurrentAction( ).launchClickEffects( );
         }
         else {
             getCurrentAction( ).update( elapsedTime );
         }
         //TODO check if the animation must be pop because it repeated as indicated
         getCurrentAnimation( ).update( elapsedTime );
     }
 
     @Override
     public boolean isPointInside( float x, float y ) {
 
         return ( this.x - getWidth( ) / 2 < x ) && ( x < this.x + getWidth( ) / 2 ) && ( this.y - getHeight( ) < y ) && ( y < this.y );
     }
 
     @Override
     public boolean canPerform( int action ) {
 
         boolean canPerform = false;
 
         switch( action ) {
             case ActionManager.ACTION_LOOK:
             case ActionManager.ACTION_EXAMINE:
             case ActionManager.ACTION_CUSTOM:
                 canPerform = true;
                 break;
 
             case ActionManager.ACTION_GRAB:
             case ActionManager.ACTION_USE:
             case ActionManager.ACTION_GIVE:
             case ActionManager.ACTION_GOTO:
             case ActionManager.ACTION_USE_WITH:
             case ActionManager.ACTION_GIVE_TO:
             case ActionManager.ACTION_TALK:
             case ActionManager.ACTION_CUSTOM_INTERACT:
                 canPerform = false;
                 break;
         }
 
         return canPerform;
     }
 
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.control.functionaldata.TalkingElement#speak(java.lang.String)
      */
     public void speak( String text, boolean keepShowing ) {
         
         text = processName(text);
         if( text != null ) {
             DebugLog.player( "Player says " + text );
             FunctionalSpeak functionalSpeak = new FunctionalSpeak( null, text, keepShowing );
             addAction( functionalSpeak );
         }
     }
     
     public void speak( String text){
         speak( text, false);
     }
 
     public long speak( String text, String audioPath, boolean keepShowing ) {
         
         
         text = processName(text);
         DebugLog.player( "Player says " + text + " with audio" );
         FunctionalSpeak functionalSpeak = new FunctionalSpeak( null, text, audioPath, keepShowing );
         addAction( functionalSpeak );
         return functionalSpeak.getAudioId( );
     }
     
     public long speak( String text, String audioPath) {
         return speak( text, audioPath, false );
     }
 
     /**
      * Speak using the TTS
      */
     public void speakWithFreeTTS( String text, String voice, boolean keepShowing ) {
 
         text = processName(text);
         
         if( text != null ) {
             DebugLog.player( "Player speaks with text-to-speech" );
             FunctionalSpeak functionalSpeak = new FunctionalSpeak( null, text, keepShowing );
             if (voice != null && !voice.equals( "" ))
                 functionalSpeak.setSpeakFreeTTS( text, voice );
             
             addAction( functionalSpeak );
             
         }
     }
     
     /**
      * Speak with TTS without keep showing the line
      * @param text
      * @param voice
      */
     public void speakWithFreeTTS( String text, String voice ) {
         speakWithFreeTTS( text, voice, false);
     }
     
     /**
      * Look for [] tag to add the name of the speaker
      * 
      * @param text
      * @return
      */
     public String processName(String text){
         
         if (text!= null && text.startsWith( "[]" ))
            text = text.replaceFirst( "\\[\\]", "[ " + GameText.TEXT_PLAYER + " ]");
            
         return text;
     }
 
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.control.functionaldata.TalkingElement#stopTalking()
      */
     public void stopTalking( ) {
 
         if( getCurrentAction( ).getType( ) == ActionManager.ACTION_TALK ) {
             getCurrentAction( ).stop( );
             this.cancelActions( );
         }
     }
 
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.control.functionaldata.TalkingElement#isTalking()
      */
     public boolean isTalking( ) {
 
         return getCurrentAction( ).getType( ) == ActionManager.ACTION_TALK;
     }
 
     /**
      * Returns if the player is walking
      * 
      * @return true if the player is walking, false otherwise
      */
     public boolean isWalking( ) {
 
         return getCurrentAction( ).getType( ) == ActionManager.ACTION_GOTO;
     }
 
     /**
      * Changes the player's target exit
      * 
      * @param exit
      *            the new target exit
      */
     public void setTargetExit( Exit exit ) {
 
         targetExit = exit;
     }
 
     /**
      * Returns the player's target exit
      * 
      * @return the player's target exit
      */
     public Exit getTargetExit( ) {
 
         return targetExit;
     }
 
     /**
      * Changes the player's current direction
      * 
      * @param direction
      *            the new player's direction
      */
     public void setDirection( int direction ) {
 
         currentDirection = direction;
     }
 
     /**
      * Return the last direction set
      */
     public int getDirection( ) {
 
         return currentDirection;
     }
 
     /**
      * Returns the X coordinate speed.
      * 
      * @return X coordinate speed
      */
     public float getSpeedX( ) {
 
         return speedX;
     }
 
     /**
      * Sets the new X coordinate speed.
      * 
      * @param speedX
      *            New X coordinate speed
      */
     public void setSpeedX( float speedX ) {
 
         if( !isTransparent )
             this.speedX = speedX;
         else if( speedX < 0 ) {
             this.speedX = -SPEED_TRANSPARENT_MODE;
         }
         else if( speedX > 0 ) {
             this.speedX = SPEED_TRANSPARENT_MODE;
         }
         else {
             this.speedX = 0;
         }
 
     }
 
     /**
      * Returns the Y coordinate speed.
      * 
      * @return Y coordinate speed
      */
     public float getSpeedY( ) {
 
         return speedY;
     }
 
     /**
      * Sets the new Y coordinate speed.
      * 
      * @param speedY
      *            New Y coordinate speed
      */
     public void setSpeedY( float speedY ) {
 
         this.speedY = speedY;
     }
 
     /**
      * Returns the player's data
      * 
      * @return the player's data
      */
     public Player getPlayer( ) {
 
         return player;
     }
 
     /**
      * Returns the front color of the player's text
      * 
      * @return Front color of the text
      */
     public Color getTextFrontColor( ) {
 
         return textFrontColor;
     }
 
     /**
      * Returns the border color of the player's text
      * 
      * @return Border color of the text
      */
     public Color getTextBorderColor( ) {
 
         return textBorderColor;
     }
 
     /**
      * Returns the resources of the npc
      * 
      * @return Resources of the npc
      */
     public Resources getResources( ) {
 
         return resources;
     }
 
     /**
      * Creates the current resource block to be used
      */
     public Resources createResourcesBlock( ) {
 
         // Get the active resources block
         Resources newResources = null;
         for( int i = 0; i < player.getResources( ).size( ) && newResources == null; i++ )
             if( new FunctionalConditions( player.getResources( ).get( i ).getConditions( ) ).allConditionsOk( ) )
                 newResources = player.getResources( ).get( i );
 
         // If no resource block is available, create a default one 
         if( newResources == null ) {
             newResources = new Resources( );
             newResources.addAsset( new Asset( NPC.RESOURCE_TYPE_SPEAK_DOWN, ResourceHandler.DEFAULT_ANIMATION ) );
             newResources.addAsset( new Asset( NPC.RESOURCE_TYPE_SPEAK_UP, ResourceHandler.DEFAULT_ANIMATION ) );
             newResources.addAsset( new Asset( NPC.RESOURCE_TYPE_SPEAK_RIGHT, ResourceHandler.DEFAULT_ANIMATION ) );
             newResources.addAsset( new Asset( NPC.RESOURCE_TYPE_WALK_DOWN, ResourceHandler.DEFAULT_ANIMATION ) );
             newResources.addAsset( new Asset( NPC.RESOURCE_TYPE_WALK_UP, ResourceHandler.DEFAULT_ANIMATION ) );
             newResources.addAsset( new Asset( NPC.RESOURCE_TYPE_WALK_RIGHT, ResourceHandler.DEFAULT_ANIMATION ) );
             newResources.addAsset( new Asset( NPC.RESOURCE_TYPE_USE_RIGHT, ResourceHandler.DEFAULT_ANIMATION ) );
             newResources.addAsset( new Asset( NPC.RESOURCE_TYPE_STAND_UP, ResourceHandler.DEFAULT_ANIMATION ) );
             newResources.addAsset( new Asset( NPC.RESOURCE_TYPE_STAND_DOWN, ResourceHandler.DEFAULT_ANIMATION ) );
             newResources.addAsset( new Asset( NPC.RESOURCE_TYPE_STAND_RIGHT, ResourceHandler.DEFAULT_ANIMATION ) );
         }
         return newResources;
     }
 
     @Override
     public Action getFirstValidAction( int actionType ) {
 
         return null;
     }
 
     @Override
     public CustomAction getFirstValidCustomAction( String actionName ) {
 
         return null;
     }
 
     @Override
     public CustomAction getFirstValidCustomInteraction( String actionName ) {
 
         return null;
     }
 
     @Override
     public InfluenceArea getInfluenceArea( ) {
 
         return null;
     }
 
     public Color getBubbleBkgColor( ) {
 
         return bubbleBkgColor;
     }
 
     public Color getBubbleBorderColor( ) {
 
         return bubbleBorderColor;
     }
 
     public boolean getShowsSpeechBubbles( ) {
 
         return player.getShowsSpeechBubbles( );
     }
 
     /**
      * Returns the X coordinate of the left-top vertex of this item.
      * It takes into account scene offset, transparent parts, and scale.  
      * @return The Current Absolute X position of the let-top vertex of the item 
      */
     @Override
     public int getXImage(){
         return Math.round( getX( )-Game.getInstance( ).getFunctionalScene( ).getOffsetX( )-getWImage( )/2 );
     }
 
     /**
      * Returns the Y coordinate of the left-top vertex of this item.
      * It takes into account transparent parts, and scale.  
      * @return The Current Absolute Y position of the let-top vertex of the item 
      */
     @Override
     public int getYImage(){
         return  Math.round( getY( )-getHImage( ) );
     }
     
     /**
      * Returns the current height of this item.
      * It takes into account transparent parts, and scale.  
      * @return The Current height of the item 
      */
     @Override
     public int getHImage(){
         return Math.round( getHeight()*scale );
     }
     
     /**
      * Returns the current width of this item.
      * It takes into account scene offset, transparent parts, and scale.  
      * @return The Current width of the item 
      */
     @Override
     public int getWImage(){
         return Math.round(getWidth()*scale);
     }
     
 }

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
 import java.awt.image.BufferedImage;
 
 import es.eucm.eadventure.common.data.chapter.Action;
 import es.eucm.eadventure.common.data.chapter.CustomAction;
 import es.eucm.eadventure.common.data.chapter.ElementReference;
 import es.eucm.eadventure.common.data.chapter.InfluenceArea;
 import es.eucm.eadventure.common.data.chapter.elements.Element;
 import es.eucm.eadventure.common.data.chapter.elements.NPC;
 import es.eucm.eadventure.common.data.chapter.resources.Asset;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 import es.eucm.eadventure.engine.core.control.ActionManager;
 import es.eucm.eadventure.engine.core.control.DebugLog;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.animations.npc.NPCIdle;
 import es.eucm.eadventure.engine.core.control.animations.npc.NPCState;
 import es.eucm.eadventure.engine.core.control.animations.npc.NPCTalking;
 import es.eucm.eadventure.engine.core.control.animations.npc.NPCWalking;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionaleffects.FunctionalEffects;
 import es.eucm.eadventure.engine.resourcehandler.ResourceHandler;
 
 /**
  * A non playing character (npc) in the game
  */
 public class FunctionalNPC extends FunctionalElement implements TalkingElement {
 
     /**
      * npc's idle state
      */
 
     public static final int IDLE = 0;
 
     /**
      * npc's talking state
      */
     public static final int TALK = 1;
 
     /**
      * npc's walking state
      */
     public static final int WALK = 2;
 
     /**
      * Default velocity for the characters.
      */
     public static final float DEFAULT_SPEED = 120.0f;
 
     /**
      * Speed in the X coordinate.
      */
     protected float speedX;
 
     /**
      * Speed in the Y coordinate.
      */
     protected float speedY;
 
     /**
      * NPC containing the data
      */
     private NPC npc;
 
     /**
      * Front color of the character's text
      */
     private Color textFrontColor;
 
     /**
      * Border color of the character's text
      */
     private Color textBorderColor;
 
     private Color bubbleBkgColor;
 
     private Color bubbleBorderColor;
 
     /**
      * Resources being used by the character
      */
     private Resources resources;
 
     /**
      * Idle state
      */
     private NPCState idleAnimation;
 
     /**
      * Speaking state
      */
     private NPCTalking talkingAnimation;
 
     /**
      * Walking state
      */
     private NPCState walkingAnimation;
 
     /**
      * Current state
      */
     private NPCState currentState;
 
     /**
      * Destiny coordenates
      */
     private int destX, destY;
 
     /**
      * Current player's direction
      */
     private int currentDirection = -1;
 
     /**
      * Last player's direction set
      */
     //private int lastDirection = -1;
     private InfluenceArea influenceArea;
 
     private ElementReference reference;
 
     public FunctionalNPC( NPC npc, ElementReference reference ) {
 
         this( npc, reference.getInfluenceArea( ), reference.getX( ), reference.getY( ), reference.getLayer( ) );
         this.reference = reference;
         this.scale = reference.getScale( );
     }
 
     /**
      * Creates a new FunctionalNCP
      * 
      * @param npc
      *            the npc's data
      * @param x
      *            the npc's horizontal position
      * @param y
      *            the npc's vertical position
      * @param layer
      *            the npcs layer, it means, it will be painted in that position
      */
     private FunctionalNPC( NPC npc, InfluenceArea influenceArea, int x, int y, int layer ) {
 
         super( x, y );
         this.npc = npc;
         this.layer = layer;
         this.influenceArea = influenceArea;
 
         textFrontColor = generateColor( npc.getTextFrontColor( ) );
         textBorderColor = generateColor( npc.getTextBorderColor( ) );
         bubbleBkgColor = generateColor( npc.getBubbleBkgColor( ) );
         bubbleBorderColor = generateColor( npc.getBubbleBorderColor( ) );
 
         // Select the resources
         resources = createResourcesBlock( );
 
         // Create the states of the character
         idleAnimation = new NPCIdle( this );
         talkingAnimation = new NPCTalking( this );
         walkingAnimation = new NPCWalking( this );
 
         destX = 0;
         destY = 0;
 
         currentState = idleAnimation;
         currentState.initialize( );
         speedX = 0;
         speedY = 0;
     }
 
     /**
      * Returns this npc's data
      * 
      * @return this npc's data
      */
     public NPC getNPC( ) {
 
         return npc;
     }
 
     /**
      * Check if all character conversation lines must be read by synthesizer
      * 
      * @return true, if all player conversation lines must be read by
      *         synthesizer
      */
     public boolean isAlwaysSynthesizer( ) {
 
         return npc.isAlwaysSynthesizer( );
     }
 
     /**
      * Takes the character voice for synthesizer
      * 
      * @return A string representing associates voice
      */
     public String getPlayerVoice( ) {
 
         return npc.getVoice( );
     }
 
     /**
      * Returns the front color of the character's text
      * 
      * @return Front color of the text
      */
     public Color getTextFrontColor( ) {
 
         return textFrontColor;
     }
 
     /**
      * Returns the border color of the character's text
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
      * Updates the resources of the npc (if the current resources and the new
      * one are different)
      */
     public void updateResources( ) {
 
         // Get the new resources
         Resources newResources = createResourcesBlock( );
 
         // If the resources have changed, load the new one
         if( resources != newResources ) {
             resources = newResources;
 
             // Update the resources of the states
             idleAnimation.loadResources( );
             talkingAnimation.loadResources( );
             walkingAnimation.loadResources( );
         }
     }
 
     @Override
     public Element getElement( ) {
 
         return npc;
     }
 
     @Override
     public int getWidth( ) {
 
         return currentState.getImage( ).getWidth( null );
     }
 
     @Override
     public int getHeight( ) {
 
         return currentState.getImage( ).getHeight( null );
     }
 
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.control.functionaldata.Renderable#draw(java.awt.Graphics2D)
      */
     public void draw( ) {
         currentState.draw( Math.round( x ), Math.round( y ), scale, layer, this );
     }
 
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.control.functionaldata.Renderable#update(long)
      */
     public void update( long elapsedTime ) {
 
         currentState.update( elapsedTime );
         //currentState.updateAnimation( );
         currentState.getCurrentAnimation( ).update( elapsedTime );
     }
 
     @Override
     public boolean isPointInside( float x, float y ) {
 
         //        return ( this.x - getWidth( ) / 2 < x ) && ( x < this.x + getWidth( ) / 2 ) && ( this.y - getHeight( ) < y ) && ( y < this.y );
         boolean isInside = false;
 
         int mousex = (int) ( x - ( this.x - getWidth( ) * scale / 2 ) );
         int mousey = (int) ( y - ( this.y - getHeight( ) * scale ) );
 
         if( ( mousex >= 0 ) && ( mousex < getWidth( ) * scale ) && ( mousey >= 0 ) && ( mousey < getHeight( ) * scale ) ) {
             BufferedImage bufferedImage = (BufferedImage) currentState.getImage( );
             int alpha = bufferedImage.getRGB( (int) ( mousex / scale ), (int) ( mousey / scale ) ) >>> 24;
             isInside = alpha > 128;
         }
        
        if (isInside){
            FunctionalScene fscene = Game.getInstance( ).getFunctionalScene( );
            if (fscene!=null){
                fscene.resetAllItems( null );
            }
        }

 
         return isInside;
     }
     
     /**
      * Triggers the drag to action associated with the item
      * 
      * @param npc
      *            The second item necessary for the use with action
      * @return True if the items were used, false otherwise
      */
     public boolean dragTo( FunctionalNPC npc ) {
         boolean dragTo = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < this.npc.getActions( ).size( ) && !dragTo; i++ ) {
             Action action = this.npc.getAction( i );
             if( action.getType( ) == Action.DRAG_TO && action.getTargetId( ).equals( npc.getNPC( ).getId( ) ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     dragTo = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < this.npc.getActions( ).size( ) && !dragTo; i++ ) {
             Action action = this.npc.getAction( i );
             if( action.getType( ) == Action.DRAG_TO && action.getTargetId( ).equals( npc.getNPC( ).getId( ) ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     dragTo = true;
                 }
             }
         }
         return dragTo;
     }
     
     /**
      * Triggers the drag to action associated with the item
      * 
      * @param anotherItem
      *            The second item necessary for the use with action
      * @return True if the items were used, false otherwise
      */
     public boolean dragTo( FunctionalItem anotherItem ) {
         boolean dragTo = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < npc.getActions( ).size( ) && !dragTo; i++ ) {
             Action action = npc.getAction( i );
             if( action.getType( ) == Action.DRAG_TO && action.getTargetId( ).equals( anotherItem.getItem( ).getId( ) ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     dragTo = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < npc.getActions( ).size( ) && !dragTo; i++ ) {
             Action action = npc.getAction( i );
             if( action.getType( ) == Action.DRAG_TO && action.getTargetId( ).equals( anotherItem.getItem( ).getId( ) ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     dragTo = true;
                 }
             }
         }
         return dragTo;
     }
 
     @Override
     public boolean canPerform( int action ) {
 
         boolean canPerform = false;
 
         switch( action ) {
             case ActionManager.ACTION_LOOK:
             case ActionManager.ACTION_EXAMINE:
             case ActionManager.ACTION_GOTO:
             case ActionManager.ACTION_GIVE_TO:
             case ActionManager.ACTION_TALK:
             case ActionManager.ACTION_USE:
                 canPerform = true;
                 break;
             case ActionManager.ACTION_GRAB:
             case ActionManager.ACTION_GIVE:
             case ActionManager.ACTION_USE_WITH:
                 canPerform = false;
                 break;
         }
 
         return canPerform;
     }
 
     /**
      * Changes this npc's state
      * 
      * @param animationState
      *            the new npc's state
      */
     public void setState( int animationState ) {
 
         switch( animationState ) {
             case IDLE:
                 currentState = idleAnimation;
                 break;
             case TALK:
                 currentState = talkingAnimation;
                 break;
             case WALK:
                 currentState = walkingAnimation;
                 break;
         }
         currentState.setResetAnimation( true );
         currentState.initialize( );
         
         //if( currentDirection != -1 )
         //    currentState.setCurrentDirection( currentDirection );
         //currentDirection = -1;
     }
 
     /*
      * (non-Javadoc)
      * @see es.eucm.eadventure.engine.engine.control.functionaldata.TalkingElement#speak(java.lang.String)
      */
     public void speak( String text2, boolean keepShowing ) {
 
         text2 = processName(text2);
         String text = Game.getInstance( ).processText( text2 );
 
         DebugLog.player( "NPC " + npc.getId( ) + " says " + text );
         talkingAnimation.setText( text, keepShowing);
         setState( TALK );
     }
     
     public void speak( String text2){
         speak( text2, false);
     }
 
     public long speak( String text2, String audioPath, boolean keepShowing ) {
 
         text2 =  processName(text2);
         String text = Game.getInstance( ).processText( text2 );
 
         DebugLog.player( "NPC " + npc.getId( ) + " says " + text + " with audio" );
         talkingAnimation.setAudio( audioPath );
         talkingAnimation.setText( text, keepShowing );
         setState( TALK );
         
         return talkingAnimation.getAudioId( );
     }
     
     public long  speak( String text2, String audioPath){
         return speak( text2, audioPath, false);
     }
 
     public void speakWithFreeTTS( String text2, String voice, boolean keepShowing ) {
 
         
         text2 =  processName(text2);
         String text = Game.getInstance( ).processText( text2 );
 
         DebugLog.player( "NPC " + npc.getId( ) + " speaks with text-to-speech" );
         // Start the voice
         speak( text , keepShowing);
         draw( );
         if (voice!=null && !voice.equals( "" ))
             talkingAnimation.setSpeakFreeTTS( text, voice );
     }
     
     public void speakWithFreeTTS( String text2, String voice){
         speakWithFreeTTS( text2, voice, false);
     }
     
     /**
      * Look for [] tag to add the name of the speaker
      * 
      * @param text
      * @return
      */
     private String  processName(String text){
        
         
        if (text!= null && text.startsWith( "[]" ))
            text = text.replaceFirst( "\\[\\]",  "[ " + npc.getId( ) + " ]");
        return text;
     }
 
 
     public void stopTalking( ) {
 
         talkingAnimation.setAudio( null );
         talkingAnimation.stopTTSTalking( );
         setState( IDLE );
     }
 
     /*
      * (non-Javadoc)
      * @see es.eucm.eadventure.engine.engine.control.functionaldata.TalkingElement#isTalking()
      */
     public boolean isTalking( ) {
 
         return currentState == talkingAnimation;
     }
 
     @Override
     public boolean examine( ) {
 
         boolean examined = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < npc.getActions( ).size( ) && !examined; i++ ) {
             Action action = npc.getAction( i );
             if( action.getType( ) == Action.EXAMINE ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     examined = true;
                 }
             }
         }
 
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < npc.getActions( ).size( ) && !examined; i++ ) {
             Action action = npc.getAction( i );
             if( action.getType( ) == Action.EXAMINE ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     examined = true;
                 }
             }
         }
         return examined;
     }
 
     /**
      * Triggers the use action associated with the item
      * 
      * @return True if the item was used, false otherwise
      */
     @Override
     public boolean use( ) {
 
         boolean used = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < npc.getActions( ).size( ) && !used; i++ ) {
             Action action = npc.getAction( i );
             if( action.getType( ) == Action.USE ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     used = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < npc.getActions( ).size( ) && !used; i++ ) {
             Action action = npc.getAction( i );
             if( action.getType( ) == Action.USE ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     used = true;
                 }
             }
         }
         return used;
     }
 
     /**
      * Changes the destiny position of the npc
      * 
      * @param x
      *            horizontal position of the destiny position
      * @param y
      *            vertical position of the destiny position
      */
     public void setDestiny( int x, int y ) {
 
         destX = x;
         destY = y;
 
     }
 
     /**
      * Returns the horizontal position of the npc's destiny position
      * 
      * @return the horizontal position of the npc's destiny position
      */
     public int getDestX( ) {
 
         return destX;
     }
 
     /**
      * Returns the vertical position of the npc's destiny position
      * 
      * @return the vertical position of the npc's destiny position
      */
     public int getDestY( ) {
 
         return destY;
     }
 
     /**
      * Returns if the NPC is walking
      * 
      * @return true if the NPC is walking
      */
     public boolean isWalking( ) {
 
         return currentState == walkingAnimation;
     }
 
     /**
      * Changes the npc's current direction
      * 
      * @param direction
      *            the new npc's direction
      */
     public void setDirection( int direction ) {
 
         currentDirection = direction;
         //lastDirection = direction;
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
 
         this.speedX = speedX;
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
      * @param speedX
      *            New Y coordinate speed
      */
     public void setSpeedY( float speedY ) {
 
         this.speedY = speedY;
     }
 
     /**
      * Creates the current resource block to be used
      */
     public Resources createResourcesBlock( ) {
 
         // Get the active resources block
         Resources newResources = null;
         for( int i = 0; i < npc.getResources( ).size( ) && newResources == null; i++ )
             if( new FunctionalConditions( npc.getResources( ).get( i ).getConditions( ) ).allConditionsOk( ) )
                 newResources = npc.getResources( ).get( i );
 
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
 
         // Looks first in actions
         for( Action action : npc.getActions( ) ) {
             if( action.getType( ) == actionType ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     return action;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has not-effects
         for( Action action : npc.getActions( ) ) {
             if( action.getType( ) == actionType ) {
                 if( action.isActivatedNotEffects( ) ) {
                     return action;
                 }
             }
         }
         return null;
     }
 
     @Override
     public CustomAction getFirstValidCustomAction( String actionName ) {
 
         // Looks first in actions
         for( Action action : npc.getActions( ) ) {
             if( action.getType( ) == Action.CUSTOM && ( (CustomAction) action ).getName( ).equals( actionName ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     return (CustomAction) action;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has not-effects
         for( Action action : npc.getActions( ) ) {
             if( action.getType( ) == Action.CUSTOM && ( (CustomAction) action ).getName( ).equals( actionName ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     return (CustomAction) action;
                 }
             }
         }
         return null;
 
     }
 
     @Override
     public CustomAction getFirstValidCustomInteraction( String actionName ) {
 
         //Looks first in actions
         for( Action action : npc.getActions( ) ) {
             if( action.getType( ) == Action.CUSTOM_INTERACT && ( (CustomAction) action ).getName( ).equals( actionName ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     return (CustomAction) action;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has not-effects
         for( Action action : npc.getActions( ) ) {
             if( action.getType( ) == Action.CUSTOM_INTERACT && ( (CustomAction) action ).getName( ).equals( actionName ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     return (CustomAction) action;
                 }
             }
         }
         return null;
 
     }
 
     public boolean custom( String actionName ) {
 
         boolean custom = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < npc.getActions( ).size( ) && !custom; i++ ) {
             Action action = npc.getAction( i );
             if( action.getType( ) == Action.CUSTOM && ( (CustomAction) action ).getName( ).equals( actionName ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     custom = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has not-effects
         for( int i = 0; i < npc.getActions( ).size( ) && !custom; i++ ) {
             Action action = npc.getAction( i );
             if( action.getType( ) == Action.CUSTOM && ( (CustomAction) action ).getName( ).equals( actionName ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     custom = true;
                 }
             }
         }
 
         return custom;
     }
 
     @Override
     public InfluenceArea getInfluenceArea( ) {
 
         return influenceArea;
     }
 
     public Color getBubbleBkgColor( ) {
 
         return bubbleBkgColor;
     }
 
     public Color getBubbleBorderColor( ) {
 
         return bubbleBorderColor;
     }
 
     public boolean getShowsSpeechBubbles( ) {
 
         return npc.getShowsSpeechBubbles( );
     }
 
     public ElementReference getReference( ) {
 
         return reference;
     }
     
     @Override
     public boolean canBeDragged() {
         boolean canBeDragged = false;
         for (int i = 0; i < npc.getActions().size( ) && !canBeDragged; i++) {
             Action action = npc.getAction( i );
             if (action.getType( ) == Action.DRAG_TO) {
                 if ( new FunctionalConditions( action.getConditions( )).allConditionsOk( )){
                     canBeDragged = true;
                 } else if (action.isActivatedNotEffects( ))
                     canBeDragged = true;
             }
         }
         return canBeDragged;
     }
 
     @Override
     public boolean canBeUsedAlone( ) {
 
         return true;
     }
     
     /**
      * Returns the X coordinate of the left-top vertex of this item.
      * It takes into account scene offset, transparent parts, and scale.  
      * @return The Current Absolute X position of the let-top vertex of the item 
      */
     @Override
     public int getXImage(){
         return Math.round( getX( )/*-Game.getInstance( ).getFunctionalScene( ).getOffsetX( )*/-getWImage( )/2 );
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

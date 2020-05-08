 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  * research group.
  * 
  * Copyright 2005-2010 <e-UCM> research group.
  * 
  * You can access a list of all the contributors to <e-Adventure> at:
  * http://e-adventure.e-ucm.es/contributors
  * 
  * <e-UCM> is a research group of the Department of Software Engineering and
  * Artificial Intelligence at the Complutense University of Madrid (School of
  * Computer Science).
  * 
  * C Profesor Jose Garcia Santesmases sn, 28040 Madrid (Madrid), Spain.
  * 
  * For more info please visit: <http://e-adventure.e-ucm.es> or
  * <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  * <e-Adventure> is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with <e-Adventure>. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.engine.core.control.functionaldata.functionalactions;
 
 import es.eucm.eadventure.common.auxiliar.SpecialAssetPaths;
 import es.eucm.eadventure.common.data.chapter.Action;
 import es.eucm.eadventure.common.data.chapter.elements.NPC;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 import es.eucm.eadventure.engine.core.control.ActionManager;
 import es.eucm.eadventure.engine.core.control.DebugLog;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.animations.Animation;
 import es.eucm.eadventure.engine.core.control.animations.AnimationState;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalElement;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalPlayer;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalTrajectory;
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 
 /**
  * The action to go to a certain position or to the influence area of an element
  * if possible.
  * 
  * @author Eugenio Marchiori
  * 
  */
 public class FunctionalGoTo extends FunctionalAction {
 
     /**
      * The position along the x axis the player will reach
      */
     private int posX;
 
     /**
      * The position along the y axis the player will reach
      */
     private int posY;
 
     /**
      * The position along the x axis the player wanted to reach
      */
     private int originalPosX;
 
     /**
      * The position along the y axis the player wanted to reach
      */
     private int originalPosY;
 
     /**
      * The speed at which the player moves along the x axis
      */
     private float speedX;
 
     /**
      * The speed at which the player moves along the y axis
      */
     private float speedY;
 
     /**
      * True if the action has an animation
      */
     private boolean hasAnimation = false;
 
     /**
      * The functional trajectory of the scene (if it exists)
      */
     private FunctionalTrajectory trajectory;
 
     /**
      * The resources used in the action
      */
     private Resources resources;
 
     /**
      * The games multimedia manager
      */
     private MultimediaManager multimedia;
 
     /**
      * True if the trajectory was already updated for the current destination
      */
     private boolean trajectoryUpdated;
 
     /**
      * Default constructor with the original action and the position to reach.
      * 
      * @param action
      *            The original action
      * @param posX
      *            The position to reach along the x axis
      * @param posY
      *            The position to reach along the y axis
      */
     public FunctionalGoTo( Action action, int posX, int posY ) {
 
         super( action );
         this.originalPosX = posX;
         this.originalPosY = posY;
         this.trajectory = Game.getInstance( ).getFunctionalScene( ).getTrajectory( );
         int[] finalDest = Game.getInstance( ).getFunctionalScene( ).checkPlayerAgainstBarriers( posX, posY );
         if( trajectory.hasTrajectory( ) ) {
             this.trajectory.setDestinationElement( null );
             this.trajectory.updatePathToNearestPoint( Game.getInstance( ).getFunctionalPlayer( ).getX( ), Game.getInstance( ).getFunctionalPlayer( ).getY( ), posX, posY );
             trajectoryUpdated = true;
         }
         this.posX = finalDest[0];
         this.posY = finalDest[1];
 
         trajectoryUpdated = false;
         type = ActionManager.ACTION_GOTO;
         keepDistance = 0;
     }
 
     /**
      * Default constructor including the distance to keep between the player and
      * the position to reach.
      * 
      * @param action
      *            The original action
      * @param posX
      *            The position to reach along the x axis
      * @param posY
      *            The position to reach along the y axis
      * @param keepDistance
      *            The distance to keep
      */
     public FunctionalGoTo( Action action, int posX, int posY, int keepDistance ) {
 
         this( action, posX, posY );
         this.keepDistance = keepDistance;
     }
 
     /**
      * Constructor with an element, used mainly when the scene uses trajectories
      * and the influence area of the object must be used to decide where to go.
      * 
      * @param action
      *            The original action
      * @param x
      *            The position along the x axis
      * @param y
      *            The position along the y axis
      * @param functionalPlayer
      *            The functional player of the game
      * @param element
      *            The element to get to
      */
     public FunctionalGoTo( Action action, int x, int y, FunctionalPlayer functionalPlayer, FunctionalElement element ) {
 
         this( action, x, y );
         if( trajectory.hasTrajectory( ) ) {
             trajectory.setDestinationElement( element );
             trajectory.updatePathToNearestPoint( functionalPlayer.getX( ), functionalPlayer.getY( ), originalPosX, originalPosY );
             trajectoryUpdated = true;
         }
     }
 
     @Override
     public void start( FunctionalPlayer functionalPlayer ) {
 
         this.functionalPlayer = functionalPlayer;
         finished = false;
         this.needsGoTo = false;
 
         resources = functionalPlayer.getResources( );
         multimedia = MultimediaManager.getInstance( );
         if( !trajectory.hasTrajectory( ) ) {
             if( functionalPlayer.getX( ) < posX ) {
                 functionalPlayer.setDirection( AnimationState.EAST );
                 speedX = FunctionalPlayer.DEFAULT_SPEED;
             }
             else {
                 functionalPlayer.setDirection( AnimationState.WEST );
                 speedX = -FunctionalPlayer.DEFAULT_SPEED;
             }
             /*if( functionalPlayer.getY( ) < posY ) {
                 speedY = FunctionalPlayer.DEFAULT_SPEED;
             }
             else {
                 speedY = -FunctionalPlayer.DEFAULT_SPEED;
             }*/
             Animation[] animations = new Animation[ 4 ];
             if( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ) != null && !resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ).equals( SpecialAssetPaths.ASSET_EMPTY_ANIMATION ) )
                 animations[AnimationState.EAST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ), false, MultimediaManager.IMAGE_PLAYER );
             else
                 animations[AnimationState.EAST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ), true, MultimediaManager.IMAGE_PLAYER );
 
             if( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ) != null && !resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ).equals( SpecialAssetPaths.ASSET_EMPTY_ANIMATION ) )
                 animations[AnimationState.WEST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ), false, MultimediaManager.IMAGE_PLAYER );
             else
                 animations[AnimationState.WEST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ), true, MultimediaManager.IMAGE_PLAYER );
            animations[AnimationState.NORTH] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_UP ), false, MultimediaManager.IMAGE_PLAYER );
            animations[AnimationState.SOUTH] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_DOWN ), false, MultimediaManager.IMAGE_PLAYER );
             functionalPlayer.setAnimation( animations, -1 );
 
         }
         else if( !trajectoryUpdated ) {
             trajectory.updatePathToNearestPoint( functionalPlayer.getX( ), functionalPlayer.getY( ), posX, posY );
         }
 
         DebugLog.player( "Player moves: hasTrajectory=" + trajectory.hasTrajectory( ) + " destination=" + posX + "," + posY );
     }
 
     @Override
     public void update( long elapsedTime ) {
 
         if( !trajectory.hasTrajectory( ) && !finished ) {
             boolean endX = false;
             //boolean endY = false;
             boolean endY = true;
             if( ( speedX > 0 && functionalPlayer.getX( ) < posX - keepDistance ) || ( speedX <= 0 && functionalPlayer.getX( ) >= posX + keepDistance ) ) {
                 float oldX = functionalPlayer.getX( );
                 float newX = oldX + speedX * elapsedTime / 1000;
                 functionalPlayer.setX( newX );
             }
             else {
                 endX = true;
             }
            /* if( ( speedY > 0 && functionalPlayer.getY( ) < posY - keepDistance ) || ( speedY <= 0 && functionalPlayer.getY( ) >= posY + keepDistance ) ) {
                 float oldY = functionalPlayer.getY( );
                 float newY = oldY + speedY * elapsedTime / 1000;
                 
                 if (endX && (newY < posY)){
                     functionalPlayer.setDirection( AnimationState.SOUTH );
                 } 
                 else if (endX && (newY >= posY) ) {
                     functionalPlayer.setDirection( AnimationState.NORTH );
                 }
                 functionalPlayer.setY( newY );
             }
             else {
                 endY = true;
             }*/
             if( endX && endY ) {
                 finished = true;
                 functionalPlayer.popAnimation( );
             }
         }
         else if( !finished ) {
             float oldSpeedX = trajectory.getSpeedX( );
             float oldSpeedY = trajectory.getSpeedY( );
             trajectory.updateSpeeds( elapsedTime, functionalPlayer.getX( ), functionalPlayer.getY( ), FunctionalPlayer.DEFAULT_SPEED );
             if( trajectory.getSpeedX( ) != 0 || trajectory.getSpeedY( ) != 0 ) {
                 setAnimation( oldSpeedX, oldSpeedY, trajectory.getSpeedX( ), trajectory.getSpeedY( ) );
                 functionalPlayer.setScale( trajectory.getScale( ) );
                 float oldX = functionalPlayer.getX( );
                 float newX = oldX + trajectory.getSpeedX( ) * elapsedTime / 1000;
                 functionalPlayer.setX( newX );
 
               /*  float oldY = functionalPlayer.getY( );
                 float newY = oldY + trajectory.getSpeedY( ) * elapsedTime / 1000;
                 functionalPlayer.setY( newY );*/
             }
             else {
                 finished = true;
                 functionalPlayer.popAnimation( );
             }
         }
     }
 
     /**
      * Set the animation necessary in each moment given the old and new speeds
      * of the player along each axis
      * 
      * @param oldSpeedX
      *            The previous speed along the x axis
      * @param oldSpeedY
      *            The previous speed along the y axis
      * @param newSpeedX
      *            The current speed algon the x axis
      * @param newSpeedY
      *            The current speed algon the y axis
      */
     private void setAnimation( float oldSpeedX, float oldSpeedY, float newSpeedX, float newSpeedY ) {
 
         Animation[] animations = new Animation[ 4 ];
         animations[AnimationState.EAST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ), false, MultimediaManager.IMAGE_PLAYER );
         if( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ) != null && !resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ).equals( SpecialAssetPaths.ASSET_EMPTY_ANIMATION ) )
             animations[AnimationState.WEST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ), false, MultimediaManager.IMAGE_PLAYER );
         else
             animations[AnimationState.WEST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ), true, MultimediaManager.IMAGE_PLAYER );
        animations[AnimationState.NORTH] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_UP ), false, MultimediaManager.IMAGE_PLAYER );
        animations[AnimationState.SOUTH] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_DOWN ), false, MultimediaManager.IMAGE_PLAYER );
 
         int nextDir = AnimationState.EAST;
        if( Math.abs( newSpeedY ) > Math.abs( newSpeedX ) ) {
             if( newSpeedY > 0 )
                 nextDir = AnimationState.SOUTH;
             else
                 nextDir = AnimationState.NORTH;
         }
        else {
             if( newSpeedX > 0 )
                 nextDir = AnimationState.EAST;
             else
                 nextDir = AnimationState.WEST;
        }
 
         if( !hasAnimation ) {
             hasAnimation = true;
             functionalPlayer.setDirection( nextDir );
             functionalPlayer.setAnimation( animations, -1 );
         }
         else if( nextDir != functionalPlayer.getDirection( ) ) {
             functionalPlayer.popAnimation( );
             functionalPlayer.setDirection( nextDir );
             functionalPlayer.setAnimation( animations, -1 );
         }
     }
 
     /**
      * Returns true if the player can get to his destination and false in any
      * other case
      * 
      * @return True if the player can get to his destination
      */
     public boolean canGetTo( ) {
 
         if( !trajectory.hasTrajectory( ) )
             return posX == originalPosX && posY == originalPosY;
         else {
             return trajectory.canGetTo( );
         }
     }
 
     @Override
     public void drawAditionalElements( ) {
 
     }
 
     @Override
     public void stop( ) {
 
         finished = true;
     }
 
     @Override
     public void setAnotherElement( FunctionalElement element ) {
 
     }
 
     /**
      * @return the posX
      */
     public int getPosX( ) {
 
         return posX;
     }
 
     /**
      * @return the posY
      */
     public int getPosY( ) {
 
         return posY;
     }
 }

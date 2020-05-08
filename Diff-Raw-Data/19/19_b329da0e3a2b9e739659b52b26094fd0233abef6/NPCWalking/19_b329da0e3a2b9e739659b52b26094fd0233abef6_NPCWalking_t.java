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
 package es.eucm.eadventure.engine.core.control.animations.npc;
 
 import es.eucm.eadventure.common.auxiliar.SpecialAssetPaths;
 import es.eucm.eadventure.common.data.chapter.elements.NPC;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 import es.eucm.eadventure.engine.core.control.animations.AnimationState;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalNPC;
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 
 public class NPCWalking extends NPCState {
 
     /**
      * Creates a new NPCIdle
      * 
      * @param npc
      *            the reference to the npc
      */
     public NPCWalking( FunctionalNPC npc ) {
 
         super( npc );
     }
 
     @Override
     public void update( long elapsedTime ) {
 
         boolean endX = false;
         boolean endY = false;
         if( ( npc.getSpeedX( ) > 0 && npc.getX( ) < npc.getDestX( ) ) || ( npc.getSpeedX( ) <= 0 && npc.getX( ) >= npc.getDestX( ) ) ) {
             npc.setX( npc.getX( ) + npc.getSpeedX( ) * elapsedTime / 1000 );
         }
         else {
             endX = true;
         }
         if( ( npc.getSpeedY( ) > 0 && npc.getY( ) < npc.getDestY( ) ) || ( npc.getSpeedY( ) <= 0 && npc.getY( ) >= npc.getDestY( ) ) ) {
             npc.setY( npc.getY( ) + npc.getSpeedY( ) * elapsedTime / 1000 );
            
             if (endX && (npc.getY( ) < npc.getDestY( ))){
                 npc.setDirection( AnimationState.SOUTH );
             } 
             else if (endX && (npc.getY( ) >= npc.getDestY( )) ) {
                 npc.setDirection( AnimationState.NORTH );
             }
         }
         else {
             endY = true;
         }
         if( endX && endY ) {
             npc.setState( FunctionalNPC.IDLE );
             if( npc.getDirection( ) == -1 )
                 npc.setDirection( AnimationState.SOUTH );
         }
     }
 
     @Override
     public void initialize( ) {
 
         if( npc.getX( ) < npc.getDestX( ) ) {
             setCurrentDirection( EAST );
             npc.setSpeedX( FunctionalNPC.DEFAULT_SPEED );
         }
         else {
             setCurrentDirection( WEST );
             npc.setSpeedX( -FunctionalNPC.DEFAULT_SPEED );
         }
         if( npc.getY( ) < npc.getDestY( ) ) {
             npc.setSpeedY( FunctionalNPC.DEFAULT_SPEED );
         }
         else {
             npc.setSpeedY( -FunctionalNPC.DEFAULT_SPEED );
         }
     }
 
     @Override
     public void loadResources( ) {
 
         Resources resources = npc.getResources( );
 
         MultimediaManager multimedia = MultimediaManager.getInstance( );
 
         // added make the mirror when only is defined left animation
        if( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ) != null && !resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ).equals( SpecialAssetPaths.ASSET_EMPTY_ANIMATION ) 
                && !resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ).equals( SpecialAssetPaths.ASSET_EMPTY_ANIMATION + ".eaa" ))
             animations[EAST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ), false, MultimediaManager.IMAGE_SCENE );
         else
             animations[EAST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ), true, MultimediaManager.IMAGE_SCENE );
 
        if( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ) != null && !resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ).equals( SpecialAssetPaths.ASSET_EMPTY_ANIMATION )
                && !resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ).equals( SpecialAssetPaths.ASSET_EMPTY_ANIMATION + ".eaa"))
             animations[WEST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ), false, MultimediaManager.IMAGE_SCENE );
         else
             animations[WEST] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ), true, MultimediaManager.IMAGE_SCENE );
         animations[NORTH] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_UP ), false, MultimediaManager.IMAGE_SCENE );
         animations[SOUTH] = multimedia.loadAnimation( resources.getAssetPath( NPC.RESOURCE_TYPE_WALK_DOWN ), false, MultimediaManager.IMAGE_SCENE );
     }
 
 }

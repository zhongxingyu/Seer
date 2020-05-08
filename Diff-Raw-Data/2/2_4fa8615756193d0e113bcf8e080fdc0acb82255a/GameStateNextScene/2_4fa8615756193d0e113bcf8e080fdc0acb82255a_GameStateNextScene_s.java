 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *          research group.
  *   
  *    Copyright 2005-2012 <e-UCM> research group.
  *  
  *     <e-UCM> is a research group of the Department of Software Engineering
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
  * This file is part of <e-Adventure>, version 1.4.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       <e-Adventure> is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      <e-Adventure> is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.engine.core.control.gamestate;
 
 import es.eucm.eadventure.common.data.chapter.Exit;
 import es.eucm.eadventure.common.data.chapter.scenes.GeneralScene;
 import es.eucm.eadventure.common.data.chapter.scenes.Scene;
 import es.eucm.eadventure.engine.core.control.ActionManager;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalConditions;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalScene;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionaleffects.FunctionalEffects;
 import es.eucm.eadventure.engine.core.gui.GUI;
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 import es.eucm.eadventure.tracking.pub._HighLevelEvents;
 
 /**
  * A game main loop while a next scene is being processed
  */
 public class GameStateNextScene extends GameState implements _HighLevelEvents {
 
     /*
      * (non-Javadoc)
      * @see es.eucm.eadventure.engine.engine.control.gamestate.GameState#EvilLoop(long, int)
      */
     @Override
     public void mainLoop( long elapsedTime, int fps ) {
 
         // Flush the image pool and the garbage colector
         MultimediaManager.getInstance( ).flushImagePool( MultimediaManager.IMAGE_SCENE );
         System.gc( );
 
         // Pick the next scene, and the scene related to it
         Exit nextScene = game.getNextScene( );
         GeneralScene generalScene = game.getCurrentChapterData( ).getGeneralScene( nextScene.getNextSceneId( ) );
 
         //Log
         gameLog.highLevelEvent( NEW_SCENE, generalScene.getId( ) );
         
         // Depending on the type of the scene
         switch( generalScene.getType( ) ) {
             case GeneralScene.SCENE:
                 
                 
                 // If next scene is the same as current scene, do not change the scene, only execute the effects
                 if (game.getFunctionalScene( )==null || !game.getFunctionalScene( ).getScene( ).getId( ).equals( generalScene.getId( ) )){
                     
                 GUI.getInstance( ).setTransition( nextScene.getTransitionTime( ), nextScene.getTransitionType( ), elapsedTime );
 
                 if( game.getFunctionalScene( ) != null && !GUI.getInstance( ).hasTransition( ) ) {
                     game.getFunctionalScene( ).draw( );
                 }
                 if( GUI.getInstance( ).hasTransition( ) )
                     GUI.getInstance( ).drawScene( null, elapsedTime );
                 GUI.getInstance( ).clearBackground( );
 
                 // If it is a scene
                 Scene scene = (Scene) generalScene;
 
                 // Set the loading state
                 game.setState( Game.STATE_LOADING );
 
                 // Create a background music identifier to not replay the music from the start
                 long backgroundMusicId = -1;
                 String newMusicPath = this.findMusicPath( scene );
                 String oldMusicPath = null;
                 // If there is a funcional scene
                 if( game.getFunctionalScene( ) != null ) {
                     // Take the old and the new music path
                     
                     for( int i = 0; i < game.getFunctionalScene( ).getScene( ).getResources( ).size( ) && oldMusicPath == null; i++ )
                         if( new FunctionalConditions( game.getFunctionalScene( ).getScene( ).getResources( ).get( i ).getConditions( ) ).allConditionsOk( ) )
                             oldMusicPath = game.getFunctionalScene( ).getScene( ).getResources( ).get( i ).getAssetPath( Scene.RESOURCE_TYPE_MUSIC );
                 }else if (game.getMusicInSlides( )!=null){
                     oldMusicPath =game.getMusicInSlides( ); 
                 }
                 // If the music paths are the same, take the music identifier
                 if( oldMusicPath != null && newMusicPath != null && oldMusicPath.equals( newMusicPath ) && game.getFunctionalScene( ) != null)
                     backgroundMusicId = game.getFunctionalScene( ).getBackgroundMusicId( );
                 else if( oldMusicPath != null && newMusicPath != null && oldMusicPath.equals( newMusicPath ) && game.getMusicInSlides( ) != null){
                     backgroundMusicId = game.getMusicInSlidesId( );
                     game.setMusicInSlides( null );
                     game.setMusicInSlidesId( -1 );
                     
                }else
                     game.getFunctionalScene( ).stopBackgroundMusic( );
                 // set the player layer for this scene
                 game.setPlayerLayer( scene.getPlayerLayer( ) );
                 // Create the new functional scene
                 FunctionalScene newScene = new FunctionalScene( scene, game.getFunctionalPlayer( ), backgroundMusicId );
                 // restart the position of the elements in the previous scene
                 
                 game.setFunctionalScene( newScene );
 
                 // Set the player position
 
                 if( nextScene.hasPlayerPosition( ) ) {
                     if( scene.getTrajectory( ) == null ) {
                         game.getFunctionalPlayer( ).setX( nextScene.getDestinyX( ) );
                         game.getFunctionalPlayer( ).setY( nextScene.getDestinyY( ) );
                         game.getFunctionalPlayer( ).setScale( scene.getPlayerScale( ) );
                     }
                     else {
                         game.getFunctionalScene( ).getTrajectory( ).changeInitialNode( nextScene.getDestinyX( ), nextScene.getDestinyY( ) );
                     }
                 }
                 else if( scene.getTrajectory( ) != null ) {
                     game.getFunctionalScene( ).getTrajectory( ).changeInitialNode( scene.getTrajectory( ).getInitial( ).getX( ), scene.getTrajectory( ).getInitial( ).getY( ));
                 }
                 else if( scene.hasDefaultPosition( ) ) {
                     // If no next scene position was defined, use the scene default
                     game.getFunctionalPlayer( ).setX( scene.getPositionX( ) );
                     game.getFunctionalPlayer( ).setY( scene.getPositionY( ) );
                     game.getFunctionalPlayer( ).setScale( scene.getPlayerScale( ) );
                 }
                 else {
                     // If no position was defined at all, place the player in the middle
                     game.getFunctionalPlayer( ).setX( GUI.getInstance( ).getGameAreaWidth( ) / 2 );
                     game.getFunctionalPlayer( ).setY( GUI.getInstance( ).getGameAreaHeight( ) / 2 );
                     game.getFunctionalPlayer( ).setScale( scene.getPlayerScale( ) );
                 }
 
                 // Set the state of the player and the action manager
                 game.getFunctionalPlayer( ).cancelActions( );
                 game.getFunctionalPlayer( ).cancelAnimations( );
                 game.getActionManager( ).setActionSelected( ActionManager.ACTION_GOTO );
 
                 }
                 // Effects are always executed even the next scene will be the same as current scene
                 // Play the post effects only if we arrive to a playable scene
                 // this method also call to  game.setState( Game.STATE_RUN_EFFECTS );
                 FunctionalEffects.storeAllEffects( nextScene.getPostEffects( ) );
 
                 // Switch to run effects node
                 //game.setState( Game.STATE_RUN_EFFECTS );
 
                 break;
 
             case GeneralScene.SLIDESCENE:
 
                 GUI.getInstance( ).setTransition( nextScene.getTransitionTime( ), nextScene.getTransitionType( ), elapsedTime );
                 if( game.getFunctionalScene( ) != null && !GUI.getInstance( ).hasTransition( ) ) {
                     game.getFunctionalScene( ).draw( );
                 }
                 if( GUI.getInstance( ).hasTransition( ) )
                     GUI.getInstance( ).drawScene( null, elapsedTime );
                 GUI.getInstance( ).clearBackground( );
 
                 
                 if (game.getFunctionalScene( )!= null)
                     game.getFunctionalScene( ).restartElementReferencesPositions( );
                 // If it is a slidescene, load the slidescene
                 game.setState( Game.STATE_SLIDE_SCENE );
                 break;
 
             case GeneralScene.VIDEOSCENE:
                 // Stop the music
                 if( game.getFunctionalScene( ) != null ){
                     game.getFunctionalScene( ).stopBackgroundMusic( );
                     game.getFunctionalScene( ).restartElementReferencesPositions( );
                 }
                 // If it is a videoscene, load the videoscene
                 game.setState( Game.STATE_VIDEO_SCENE );
                 break;
         }
     }
     
     private String findMusicPath(Scene scene){
         String newMusicPath = null;
         for( int i = 0; i < scene.getResources( ).size( ) && newMusicPath == null; i++ ){
             if( new FunctionalConditions( scene.getResources( ).get( i ).getConditions( ) ).allConditionsOk( ) ){
                 newMusicPath = scene.getResources( ).get( i ).getAssetPath( Scene.RESOURCE_TYPE_MUSIC );
             }
         }
         return newMusicPath;
     }
 }

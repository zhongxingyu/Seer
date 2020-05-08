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
 package es.eucm.eadventure.engine.core.control.gamestate;
 
 import java.awt.event.MouseEvent;
 
 import javax.swing.JOptionPane;
 
 import es.eucm.eadventure.common.data.chapter.Exit;
 import es.eucm.eadventure.common.data.chapter.effects.Effects;
 import es.eucm.eadventure.common.data.chapter.resources.Asset;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 import es.eucm.eadventure.common.data.chapter.scenes.Cutscene;
 import es.eucm.eadventure.common.data.chapter.scenes.Scene;
 import es.eucm.eadventure.common.data.chapter.scenes.Slidescene;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.animations.Animation;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalConditions;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionaleffects.FunctionalEffects;
 import es.eucm.eadventure.engine.core.gui.GUI;
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 import es.eucm.eadventure.engine.resourcehandler.ResourceHandler;
 
 /**
  * A game main loop while a "slidescene" is being displayed
  */
 public class GameStateSlidescene extends GameState {
 
     /**
      * Animation of the slidescene
      */
     private Animation slides;
 
     /**
      * Slidescene being played
      */
     private Slidescene slidescene;
 
     /**
      * Flag to control that we jump to the next chapter at most once
      */
     private boolean yetSkipped = false;
     
     /**
      * Flag to stop updating the slides when they finished
      */
     private boolean finish = false;
 
    // private BufferedImage bkg = new BufferedImage( GUI.WINDOW_WIDTH, GUI.WINDOW_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR );
 
     /**
      * Creates a new GameStateSlidescene
      */
     public GameStateSlidescene( ) {
 
         super( );
 
         slidescene = (Slidescene) game.getCurrentChapterData( ).getGeneralScene( game.getNextScene( ).getNextSceneId( ) );
 
         // Select the resources
         Resources resources = createResourcesBlock( );
 
         // Create a background music identifier to not replay the music from the start
         long backgroundMusicId = -1;
 
         // If there is a funcional scene
         if( game.getFunctionalScene( ) != null ) {
             // Take the old and the new music path
             String oldMusicPath = null;
             for( int i = 0; i < game.getFunctionalScene( ).getScene( ).getResources( ).size( ) && oldMusicPath == null; i++ )
                 if( new FunctionalConditions( game.getFunctionalScene( ).getScene( ).getResources( ).get( i ).getConditions( ) ).allConditionsOk( ) )
                     oldMusicPath = game.getFunctionalScene( ).getScene( ).getResources( ).get( i ).getAssetPath( Scene.RESOURCE_TYPE_MUSIC );
             String newMusicPath = null;
             for( int i = 0; i < slidescene.getResources( ).size( ) && newMusicPath == null; i++ )
                 if( new FunctionalConditions( slidescene.getResources( ).get( i ).getConditions( ) ).allConditionsOk( ) )
                     newMusicPath = slidescene.getResources( ).get( i ).getAssetPath( Scene.RESOURCE_TYPE_MUSIC );
 
             // If the music paths are the same, take the music identifier
             if( oldMusicPath != null && newMusicPath != null && oldMusicPath.equals( newMusicPath ) )
                 backgroundMusicId = game.getFunctionalScene( ).getBackgroundMusicId( );
             else
                 game.getFunctionalScene( ).stopBackgroundMusic( );
         }
         if( Game.getInstance( ).getOptions( ).isMusicActive( ) ) {
             if( backgroundMusicId != -1 ) {
                 if( !MultimediaManager.getInstance( ).isPlaying( backgroundMusicId ) ) {
                     backgroundMusicId = MultimediaManager.getInstance( ).loadMusic( resources.getAssetPath( Scene.RESOURCE_TYPE_MUSIC ), true );
                     MultimediaManager.getInstance( ).startPlaying( backgroundMusicId );
                 }
             }
             else {
                 if( resources.existAsset( Scene.RESOURCE_TYPE_MUSIC ) ) {
                     backgroundMusicId = MultimediaManager.getInstance( ).loadMusic( resources.getAssetPath( Scene.RESOURCE_TYPE_MUSIC ), true );
                     MultimediaManager.getInstance( ).startPlaying( backgroundMusicId );
                 }
             }
         }
 
         // Create the set of slides and start it
         slides = MultimediaManager.getInstance( ).loadSlides( resources.getAssetPath( Slidescene.RESOURCE_TYPE_SLIDES ), MultimediaManager.IMAGE_SCENE );
         slides.start( );
     }
 
     /*
      * (non-Javadoc)
      * @see es.eucm.eadventure.engine.engine.control.gamestate.GameState#EvilLoop(long, int)
      */
     @Override
     public void mainLoop( long elapsedTime, int fps ) {
         // Paint the current slide
         //Graphics2D g = (Graphics2D) bkg.getGraphics( );
         //g.clearRect( 0, 0, GUI.WINDOW_WIDTH, GUI.WINDOW_HEIGHT );
         slides.update( elapsedTime );
         if( !slides.isPlayingForFirstTime( ) && !finish )
             finishedSlides( );
         //g.drawImage( slides.getImage( ), 0, 0, null );
         //g.dispose( );
         if (!finish){
             GUI.getInstance( ).addBackgroundToDraw( slides.getImage( ), 0 );
             GUI.getInstance( ).endDraw( );
             GUI.getInstance( ).drawScene( GUI.getInstance( ).getGraphics( ), elapsedTime );
         }
     }
 
     @Override
     public void mouseClicked( MouseEvent e ) {
 
         // Display the next slide 
         boolean endSlides = slides.nextImage( );
 
         // If the slides have ended
         if( endSlides ) {
             finish = true;
             finishedSlides( );
         }
     }
 
     private void finishedSlides( ) {
 
         // If it is a endscene, go to the next chapter
         if( !yetSkipped && slidescene.getNext( ) == Cutscene.ENDCHAPTER ) {
             
             yetSkipped = true;
             game.goToNextChapter( );
         }
         else if( slidescene.getNext( ) == Cutscene.NEWSCENE ) {
             
             Exit exit = new Exit( slidescene.getTargetId( ) );
             exit.setDestinyX( slidescene.getPositionX( ) );
             exit.setDestinyY( slidescene.getPositionY( ) );
             exit.setPostEffects( slidescene.getEffects( ) );
             exit.setTransitionTime( slidescene.getTransitionTime( ) );
             exit.setTransitionType( slidescene.getTransitionType( ) );
             game.setNextScene( exit );
             game.setState( Game.STATE_NEXT_SCENE );
         }
         else {
             if( game.getFunctionalScene( ) == null ) {
                 JOptionPane.showMessageDialog( null, TC.get( "DesignError.Message" ), TC.get( "DesignError.Title" ), JOptionPane.ERROR_MESSAGE );
                 yetSkipped = true;
                 game.goToNextChapter( );
             }
             slides.stopMusic();
             FunctionalEffects.storeAllEffects( new Effects( ) );
         }
     }
 
     /**
      * Creates the current resource block to be used
      */
     private Resources createResourcesBlock( ) {
 
         // Get the active resources block
         Resources newResources = null;
         for( int i = 0; i < slidescene.getResources( ).size( ) && newResources == null; i++ )
             if( new FunctionalConditions( slidescene.getResources( ).get( i ).getConditions( ) ).allConditionsOk( ) )
                 newResources = slidescene.getResources( ).get( i );
 
         // If no resource block is available, create a default one 
         if( newResources == null ) {
             newResources = new Resources( );
             newResources.addAsset( new Asset( Slidescene.RESOURCE_TYPE_SLIDES, ResourceHandler.DEFAULT_SLIDES ) );
         }
         return newResources;
     }
 }

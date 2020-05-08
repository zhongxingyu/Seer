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
 package es.eucm.eadventure.engine.core.control.animations;
 
 import java.awt.Image;
 
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 
 /**
  * Class that represents an animation made up of frames in the engine. It uses
  * the logic in {@link es.eucm.eadventure.common.data.animation.Animation}.
  * 
  * 
  * @author Eugenio Marchiori
  */
 public class FrameAnimation implements Animation {
 
     /**
      * The animation with the frames and the logic
      */
     private es.eucm.eadventure.common.data.animation.Animation animation;
 
     /**
      * The time accumulated in the playing of the animation
      */
     private long accumulatedTime;
 
     /**
      * Create a new instance using an animation.
      * 
      * @param animation
      *            The animation with the frames and the logic
      */
     public FrameAnimation( es.eucm.eadventure.common.data.animation.Animation animation ) {
 
         this.animation = animation;
         accumulatedTime = 0;
     }
 
     public Image getImage( ) {
 
         Image temp = animation.getImage( accumulatedTime, es.eucm.eadventure.common.data.animation.Animation.ENGINE );
         String sound = animation.getNewSound( );
         if( sound != null && sound != "" ) {
            long soundID = MultimediaManager.getInstance( ).loadSound( sound, false );
            MultimediaManager.getInstance( ).startPlaying( soundID );
        }
         return temp;
     }
 
     public boolean isPlayingForFirstTime( ) {
 
         //return playingForFirstTime;
         return !animation.finishedFirstTime( accumulatedTime );
     }
 
     public void start( ) {
 
         accumulatedTime = 0;
     }
 
     public void update( long elapsedTime ) {
 
         accumulatedTime += elapsedTime;
     }
 
     public boolean nextImage( ) {
 
         boolean noMoreFrames = false;
 
         accumulatedTime = animation.skipFrame( accumulatedTime );
 
         if( accumulatedTime >= animation.getTotalTime( ) ) {
             accumulatedTime %= animation.getTotalTime( );
             noMoreFrames = true;
         }
 
         return noMoreFrames;
     }
 
     public void setAnimation( es.eucm.eadventure.common.data.animation.Animation animation ) {
 
         this.animation = animation;
     }
 
     public void setMirror( boolean mirror ) {
 
         animation.setMirror( mirror );
     }
 
     public void setFullscreen( boolean b ) {
 
         animation.setFullscreen( b );
     }
 }

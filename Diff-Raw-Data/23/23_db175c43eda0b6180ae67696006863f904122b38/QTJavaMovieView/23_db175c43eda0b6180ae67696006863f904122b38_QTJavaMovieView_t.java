 //
 //  QTJavaMovieView.java
 //  QTCubed
 //
 //  Created by Chappell Charles on 10/02/19.
 //  Copyright (c) 2010 MC Cubed, Inc. All rights reserved.
 //
 //  This program is free software; you can redistribute it and/or modify
 //  it under the terms of the GNU General Public License as published by
 //  the Free Software Foundation; either version 2 of the License, or
 //  (at your option) any later version.
 //  
 //  This program is distributed in the hope that it will be useful,
 //  but WITHOUT ANY WARRANTY; without even the implied warranty of
 //  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //  GNU General Public License for more details.
 //  
 //  You should have received a copy of the GNU General Public License along
 //  with this program; if not, write to the Free Software Foundation, Inc.,
 //  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 //  
 //  
 //  This is also dual licensed as proprietary software, and may be used in
 //  commercial/proprietary software products by obtaining a license from:
 //  MC Cubed, Inc
 //  1-3-4 Kamikizaki, Urawa-ku
 //  Saitama, Saitama, 330-0071
 //  Japan
 //
 //  Email: info@mc-cubed.net
 //  Website: http://www.mc-cubed.net/
 package net.mc_cubed.qtcubed;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import quicktime.QTException;
 import quicktime.app.view.MoviePlayer;
 import quicktime.app.view.QTFactory;
 import quicktime.app.view.QTJComponent;
 import quicktime.std.StdQTException;
 import quicktime.std.movies.Movie;
 import java.awt.Component;
 import java.security.PrivilegedAction;
 import java.util.logging.Logger;
 import quicktime.qd.QDColor;
 import quicktime.std.StdQTConstants;
 import quicktime.std.movies.MovieController;
 
 class QTJavaMovieView implements QTMovieView {
 
     QTJComponent component;
     MoviePlayer player;
     boolean controllerVisible = false;
     boolean preservesAspect = true;
 
     public QTJavaMovieView() throws InstantiationException {
         {
             try {
                 // Create a blank movie from the file Blank.mp4
                 quicktime.app.players.QTPlayer qtplayer =
                         (quicktime.app.players.QTPlayer) quicktime.app.QTFactory.makeDrawable(
                         getClass().getResource("/Blank.mp4").openStream(),
                         StdQTConstants.kDataRefFileExtensionTag, ".mp4");
                 MovieController mc = qtplayer.getMovieController();
                 Movie newMovie = mc.getMovie();
                 player = new MoviePlayer(newMovie);
                 component = java.security.AccessController.doPrivileged(new PrivilegedAction<QTJComponent>() {
                     public QTJComponent run() {
                         try {
                             return QTFactory.makeQTJComponent(player);
                         } catch (QTException qte) {
                             throw new RuntimeException(qte);
                         }
                     }
                 });
             } catch (IOException ex) {
                 Logger.getLogger(QTJavaMovieView.class.getName()).log(Level.SEVERE, null, ex);
             } catch (QTException ex) {
                 Logger.getAnonymousLogger().severe(ex.getLocalizedMessage());
                 throw new RuntimeException(ex);
             }
         }
 
     }
 
     public Component getComponent() {
         return component.asJComponent();
     }
 
     public void setMovie(QTMovie movie) {
         QTJavaMovie javaMovie = (QTJavaMovie) movie;
         try {
             MoviePlayer mp = new MoviePlayer(javaMovie.movie);
             component.setMoviePlayer(mp);
            player = mp;
         } catch (QTException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     public void play() {
         try {
             player.setRate(1);
             player.getMovie().start();
            player.getMovie().setRate(1);
         } catch (StdQTException qte) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,"Couldn't play media: {0}",qte);
         }
     }
 
     public void pause() {
         try {
             player.setRate(0);
             player.getMovie().stop();
            player.getMovie().setRate(0);
         } catch (StdQTException qte) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,"Couldn't stop media: {0}",qte);
         }
     }
 
     public boolean isControllerVisible() {
         return controllerVisible;
     }
 
     public void setControllerVisisble(boolean visible) {
         this.controllerVisible = visible;
         // TODO: implement controller visibility switching
     }
 
     public boolean preservesAspectRatio() {
         throw new java.lang.UnsupportedOperationException("Not supported yet");
     }
 
     public void setPreservesAspectRatio(boolean preserves) {
         throw new java.lang.UnsupportedOperationException("Not supported yet");
     }
 
     public java.awt.Color getFillColor() {
         try {
             return new java.awt.Color(player.getGWorld().getBackColor().getARGB());
         } catch (StdQTException ex) {
             Logger.getLogger(QTJavaMovieView.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     public void setFillColor(java.awt.Color fillColor) {
         try {
             player.getGWorld().setBackColor(QDColor.fromARGBColor(fillColor.getRGB()));
         } catch (StdQTException ex) {
             Logger.getLogger(QTJavaMovieView.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void gotoBeginning() {
         try {
             player.getMovie().goToBeginning();
         } catch (StdQTException ex) {
             Logger.getLogger(QTJavaMovieView.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void gotoEnd() {
         try {
             player.getMovie().goToEnd();
         } catch (StdQTException ex) {
             Logger.getLogger(QTJavaMovieView.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void gotoNextSelectionPoint() {
         throw new java.lang.UnsupportedOperationException("Not supported yet");
     }
 
     public void gotoPreviousSelectionPoint() {
         throw new java.lang.UnsupportedOperationException("Not supported yet");
     }
 
     public void gotoPosterFrame() {
         try {
             int posterTime = player.getMovie().getPosterTime();
             player.getMovie().setTimeValue(posterTime);
         } catch (StdQTException ex) {
             Logger.getLogger(QTJavaMovieView.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void stepForward() {
         throw new java.lang.UnsupportedOperationException("Not supported yet");
     }
 
     public void stepBackward() {
         throw new java.lang.UnsupportedOperationException("Not supported yet");
     }
 }

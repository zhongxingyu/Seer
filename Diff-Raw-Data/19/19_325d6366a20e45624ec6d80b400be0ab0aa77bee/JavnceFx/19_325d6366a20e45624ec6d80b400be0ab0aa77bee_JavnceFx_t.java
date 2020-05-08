 /*
  * Copyright (C) 2013 Pauli Kauppinen
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.javnce.ui;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.application.Application;
 import javafx.application.Platform;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.scene.Scene;
 import javafx.scene.image.Image;
 import javafx.stage.Stage;
 import org.javnce.eventing.EventLoop;
 import org.javnce.eventing.EventLoopErrorHandler;
 
 /**
  * The Class JavnceFx is the Javnce Application.
  */
 public class JavnceFx extends Application {
 
     /**
      * The view controller.
      */
     private Controller controller;
 
     /**
      * Bind full screen property with stage property.
      *
      * @param stage the stage
      */
     private void bindFullScreen(final Stage stage) {
         //fullScreenProperty is read only -> binding with listeners
         stage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
             @Override
             public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                 controller.getProperties().getFullScreen().set(newValue);
             }
         });
         controller.getProperties().getFullScreen().addListener(new ChangeListener<Boolean>() {
             @Override
             public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                 if (newValue) {
                     stage.setFullScreen(newValue);
                 }
             }
         });
     }
 
     /* (non-Javadoc)
      * @see javafx.application.Application#start(javafx.stage.Stage)
      */
     @Override
     public void start(final Stage stage) throws Exception {
         initErrorHandler();
 
         controller = new ViewController();
 
         stage.setScene(new Scene(controller.getParent()));
 
         stage.setTitle("Javnce - Java VNC");
         stage.getIcons().add(new Image(getClass().getResourceAsStream("logo.png")));
 
         bindFullScreen(stage);
 
         stage.show();
     }
 
     /* (non-Javadoc)
      * @see javafx.application.Application#stop()
      */
     @Override
     public void stop() throws Exception {
        EventLoop.shutdownAll();
         if (null != controller) {
             controller.exit();
         }
     }
 
     /**
      * Add own error handler.
      */
     private void initErrorHandler() {
         EventLoop.setErrorHandler(new EventLoopErrorHandler() {
             @Override
             public void fatalError(final Object object, final Throwable throwable) {
                 Logger.getLogger(object.getClass().getName()).log(Level.SEVERE, null, throwable);
                 Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                         Platform.exit();
                     }
                 });
             }
         });
     }
 
     /**
      * The main method.
      *
      * @param args the arguments
      */
     public static void main(String[] args) {
         launch(args);
     }
 }

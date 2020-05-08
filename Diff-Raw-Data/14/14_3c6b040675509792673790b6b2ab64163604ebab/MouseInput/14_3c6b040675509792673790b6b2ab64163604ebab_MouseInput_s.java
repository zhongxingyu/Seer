 /* 
 *	Date: Oct 13, 2013
 *	Company: Spectre Games
 *	Web Address: http://www.spectregames.com
 * 
 *	Copyright 2013 Phantom Coder(Richard Tester)
 * 	Based on code from the following YouTube Channels:
 *			BossLetsPlays(Matthew Rogers)
 *			RealTutsGML(Zack Berenger)
 *
 *  	Licensed under the Apache License, Version 2.0 (the "License");
 *  	you may not use this file except in compliance with the License.
 *  	You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  	Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  	See the License for the specific language governing permissions and
 *  	limitations under the License.
 */
 package com.spectregames.concentration.input;
 
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import com.spectregames.concentration.Game;
 import com.spectregames.concentration.enums.GameState;
 import com.spectregames.concentration.libs.Reference;
 import com.spectregames.concentration.screens.MainMenu;
 
 /**
  *	Project: Concentration
  * 	Package: com.spectregames.concentration.input
  *	Class: MouseInput
  *
  * @author Phantom Coder
  * @license Apache Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
  * 
  *	
  */
 
 public class MouseInput extends MouseAdapter {
     
     /**
      * The x and y coords of the mouse
      */
     public static int MOUSE_X, MOUSE_Y;
     
     /**
      * Used to check for intersection in other classes <br> set to a 1x1 at location (1,1) by default, to avoid a NullPointerException
      */
     public static Rectangle MOUSE = new Rectangle(1,1,1,1);
 
     
     /**
      * This method is called whenever a mouse button is clicked
      */
     public void mouseClicked(MouseEvent e) {
 
     }
     
     @Override
     /**
      * This is called whenever the mouse is moved
      */
     public void mouseMoved(MouseEvent e) {
         MOUSE_X = e.getX();
         MOUSE_Y = e.getY();
         MOUSE = new Rectangle(MOUSE_X, MOUSE_Y, 1, 1);
     }
     
     public void mousePressed(MouseEvent e){
     	Reference.isPressed = true;
  
     }
     
     public void mouseReleased(MouseEvent e){
         int mouse = e.getButton();  //used to check what button was clicked
         Rectangle rect = new Rectangle(e.getX(), e.getY(), 1, 1);  //creates a 1x1 rectangle around the cursor to be used to check where it was clicked
 
         if (mouse == MouseEvent.BUTTON1) {  //only do this stuff if the left mouse button was clicked
 
             switch (Game.state) {  //depending on what state the game is in, check for the following cases
                 case GAME:
                     break;
                 case MENU:
                 	
                 	// If we released our mouse button while in the bounds main menu's play button, 
                 	// change the games GameState to GAME and set isPressed to false.
                     if (rect.intersects(MainMenu.play)){
                         Game.state = GameState.GAME;
                         Reference.isPressed = false;
                    }
                     
                     // If we released our mouse button while in the bounds main menu's options button, 
                 	// change the games GameState to OPTIONS and set isPressed to false.
                     if (rect.intersects(MainMenu.options)){ 
                     	Game.state = GameState.OPTIONS;
                     	Reference.isPressed = false;
                    }
                     
                     // If we released our mouse button while in the bounds main menu's instructions button, 
                 	// change the games GameState to INSTRUCTIONS and set isPressed to false.
                     if (rect.intersects(MainMenu.instructions)){
                         Game.state = GameState.INSTRUCTIONS;
                         Reference.isPressed = false;
                    }
                     
                     // If we released our mouse button while in the bounds main menu's high scores button, 
                 	// change the games GameState to SCORES and set isPressed to false.
                     if (rect.intersects(MainMenu.scores)){
                         Game.state = GameState.SCORES;
                     	Reference.isPressed = false;
            		}
                     
                     // If we released our mouse button while in the bounds main menu's quit button, 
                 	// change the games GameState to QUITTING and set isPressed to false.
                     if (rect.intersects(MainMenu.quit)){
                         Game.state = GameState.QUITTING;
                     	Reference.isPressed = false;
            		}
                     
                     break;
                     
                 case OPTIONS:
                     break;
                     
                 case PAUSE:
                     break;
                     
                 default:
                     break;
 
             }
         }
     }
 
 }

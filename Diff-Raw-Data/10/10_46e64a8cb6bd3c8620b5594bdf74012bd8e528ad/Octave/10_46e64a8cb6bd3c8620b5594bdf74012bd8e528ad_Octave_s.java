 /*
 Copyright (C) 2012 Arvind Kumar
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>
 
 Please note that in the event that any source file or other resource in this project does not include the above header, it should be assumed to be under the same license.
 */
 
 /**
  * @version 1.1.0 alpha
  * @since 2012-09-10
  *
  * This is the entry point for Octave.
 **/
  
 package source;
 
 import java.awt.event.*;
 import java.io.File;
import java.applet.*;
 import javax.swing.*;
import java.lang.reflect.*;
 import java.net.*;
 
 
 /**
  * Main class for Octave
  *
  * It contains the entry point, {@link #main(String[])}
 **/
 public class Octave extends JApplet implements ActionListener{
 	static final long serialVersionUID=1000;
 	
 	/**
 	 * The game instance.
 	 */
 	public static Octave game;
 	
 	/**
 	 * The root folder of the icon resources
 	**/
 	public static URL codeBase=null;
 
 	/**
 	 * How much to scale certain objects
 	 */
 	public static final int SCALE=2;
 
 	/**
 	 * The Frames Per Second of the game.
 	**/
     public static final int FPS=30;
 
 	/**
 	 * The calculated delay in milliseconds of the game.
 	**/
     public static final int DELAY=1000/FPS;
 	/**
 	 * The root folder of all resources
 	**/
 	public static final String RCROOT="resources"+File.separator;
 
 	/**
 	 * The root folder of the image resources
 	**/
 	public static final String IMGROOT=RCROOT+"images"+File.separator;
 
 	/**
 	 * The root folder of the icon resources
 	**/
 	public static final String ICONROOT=RCROOT+"icons"+File.separator;
 	
 	/**
 	 * The game's map controller instance.
 	 *
 	 * @see Map
 	**/
 	Map map;
     
     /**
      * Handles the game step timing.
     **/
     Timer step;
 
 	public Octave(){
 		JFrame frame=new JFrame("Octave - v1.1.0 Alpha");
 		game=this;
 		frame.setSize(500,500);
 		map=new Map();
 		frame.setContentPane(map);
 		frame.setVisible(true);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         step=new Timer(DELAY,this);
 		game.step.start();
 	}
 
 	public void init(){
 		game=this;
 		codeBase=getCodeBase();
 		map=new Map();
 		setContentPane(map);
 	}
 	
 	public void stop(){ }
 	
 	public static void main(String args[]){
 		game=new Octave();
 	}
    
     public void actionPerformed(ActionEvent e){
 		map.step();
 	}
 }

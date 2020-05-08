 /*******************************************************************************
  * Copyright (c) 2013 DHBW.
  * This source is subject to the DHBW Permissive License.
  * Please see the License.txt file for more information.
  * All other rights reserved.
  * 
  * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY 
  * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
  * PARTICULAR PURPOSE.
  * 
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  *
  *Project: Zombiz
  *Package: com.dhbw.zombiz
  *
  * 
  *Contributors:
  * -Christoph Schabert
 
  ********************************************************************************/
 
 package com.dhbw.Zombiz;
 
 import com.dhbw.Zombiz.gameEngine.logic.Actor;
 import com.dhbw.Zombiz.output.audio.Sound;
 import com.dhbw.Zombiz.gameEngine.parser.*;
 
 
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.io.ObjectInputStream.GetField;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextPane;
 import javax.swing.WindowConstants;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 
 import com.dhbw.Zombiz.gameEngine.logic.BuildRoom;
 import com.dhbw.Zombiz.gameEngine.logic.Conversation;
 import com.dhbw.Zombiz.gameEngine.logic.DialogEntry;
 import com.dhbw.Zombiz.gameEngine.logic.Item;
 import com.dhbw.Zombiz.gameEngine.logic.Actor;
 import com.dhbw.Zombiz.gameEngine.logic.Runtime;
 import com.dhbw.Zombiz.gameEngine.logic.Room;
 import com.dhbw.Zombiz.gameEngine.parser.XmlParser;
 import com.dhbw.Zombiz.output.audio.*;
 import com.dhbw.Zombiz.output.display.Menu;
 
 /**
  * Starts the Main Game
  * 
  * at this State only for test purpose
  *
  */
 @SuppressWarnings("unused")
 public class App 
 {
 	public final static JFrame frame = new JFrame("Nightmare On Coblitzallee");	
     public static void main( String[] args )
     {
     	
     	
     	frame.setSize(800,600);
 		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setResizable(false);
 		
 		
 		Runtime r = new Runtime(true,frame);
 		
 
 		frame.setVisible(true);
     	
     	Menu menu = new Menu();
 		menu.mainMenu(frame);
     	
     
 
 		
 		
 		
     	
     	} 
         
 
     	
 
 
 }

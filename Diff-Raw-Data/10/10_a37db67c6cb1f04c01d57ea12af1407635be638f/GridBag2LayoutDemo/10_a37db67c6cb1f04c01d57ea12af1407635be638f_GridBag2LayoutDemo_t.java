 /*
  * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *   - Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *   - Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  *
  *   - Neither the name of Sun Microsystems nor the names of its
  *     contributors may be used to endorse or promote products derived
  *     from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */ 
 
 /*
  * GridBag2LayoutDemo.java requires no other files.
  */
 
 import java.awt.*;
 
 import myjava.awt.*;
 import myjavax.swing.*;
 
 import polyglot.ext.esj.primitives.*;
 import polyglot.ext.esj.tologic.LogMap;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 public class GridBag2LayoutDemo {
 
     //HS
     static boolean fallbackOn;
    final static int SPEC_GRID = 9; //9; 
     static { ESJButton.setSpecGrid(SPEC_GRID); }
     //HS END
     
     final static boolean shouldFill = true;
     final static boolean shouldWeightX = true;
     final static boolean RIGHT_TO_LEFT = false;
     final static int GRIDCOLS = 3; 
     final static int GRIDROWS = 3; 
 
 
     public static void addComponentsToPane(Container pane) {
         if (RIGHT_TO_LEFT) {
             pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
         }
 
         ESJButton button;
 	pane.setLayout(new GridBag2Layout(fallbackOn, SPEC_GRID, GRIDCOLS, GRIDROWS)); //HS
 	GridBag2Constraints c = new GridBag2Constraints();
 	if (shouldFill) {
 	//natural height, maximum width
 	c.fill = GridBag2Constraints.HORIZONTAL;
 	}
 
 	button = new ESJButton("Button 1");
 	if (shouldWeightX) {
 	c.weightx = 0.5;
 	}
 	c.fill = GridBag2Constraints.HORIZONTAL;
 	c.gridx = 0;
 	c.gridy = 0;
 	pane.add(button, c);
 
 	c = new GridBag2Constraints(); //HS
 	button = new ESJButton("Button 2");
 	c.fill = GridBag2Constraints.HORIZONTAL;
 	c.weightx = 0.5;
 	c.gridx = 1;
 	c.gridy = 0;
 	pane.add(button, c);
 
 	c = new GridBag2Constraints(); //HS
 	button = new ESJButton("Button 3");
 	c.fill = GridBag2Constraints.HORIZONTAL;
 	c.weightx = 0.5;
 	c.gridx = 2;
 	c.gridy = 0;
 	pane.add(button, c);
 
 	c = new GridBag2Constraints(); //HS
 	button = new ESJButton("Long-Named Button 4");
 	c.fill = GridBag2Constraints.HORIZONTAL;
 	c.ipady = 40;      //make this component tall
 	c.weightx = 0.0;
 	c.gridwidth = 3;
 	c.gridx = 0;
 	c.gridy = 1;
 	pane.add(button, c);
 
 	c = new GridBag2Constraints(); //HS
 	button = new ESJButton("5");
 	c.fill = GridBag2Constraints.HORIZONTAL;
 	c.ipady = 0;       //reset to default
 	c.weighty = 1.0;   //request any extra vertical space
 	c.anchor = GridBag2Constraints.PAGE_END; //bottom of space
 	c.insets = new Insets(10,0,0,0);  //top padding
 	c.gridx = 1;       //aligned with button 2
 	c.gridwidth = 2;   //2 columns wide
 	c.gridy = 2;       //third row
 	pane.add(button, c);
 
     }
 
     /**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from the
      * event-dispatching thread.
      */
     private static void createAndShowGUI() {
         //Create and set up the window.
         JFrame frame = new JFrame("GridBagLayoutDemo");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         //Set up the content pane.
         addComponentsToPane(frame.getContentPane());
 
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
 
     public static void main(String[] args) {
 	fallbackOn = args.length > 0;
 	LogMap.SolverOpt_debugLevel(1);
 	//ESJInteger.setBounds(0,Math.max(100,600/SPEC_GRID));
         //Schedule a job for the event-dispatching thread:
         //creating and showing this application's GUI.
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGUI();
             }
         });
     }
 }

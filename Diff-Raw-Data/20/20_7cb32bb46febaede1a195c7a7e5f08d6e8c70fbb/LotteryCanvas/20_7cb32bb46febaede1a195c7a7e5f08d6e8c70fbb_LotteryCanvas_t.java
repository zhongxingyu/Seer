 /**
  *    Copyright 2013 Michael Fitzmaurice
  * 
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.michaelfitzmaurice.lottery;
 
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Button;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Panel;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 /**
  * This animated applet allows the user to generate 6 unique 'lottery' numbers
  * (i.e. integers within the range 1 - 49) at the push of a button. The user can
  * then press another button to sort these numbers into ascending order. An
  * unlimited number of sets of numbers can be generated and sorted. The applet
  * avoids using classes and methods only available in versions of the Java
  * language from 1.2 upwards, in order to maintain compatibility with as wide a
  * range of commercial browsers and Java plugins as possible.
  * 
  * @author Michael Fitzmaurice, September 2001
  */
 public class LotteryCanvas extends Applet implements ActionListener {
     
     private static final long serialVersionUID = -9116095178574976507L;
     
     // hardcode for now
     private static final int NUMBER_OF_BALLS = 49;
     private static final int NUMBER_TO_PICK = 6;
     
     // size of the canvas
     private int height, width;
     
     private Color backgroundColor;
     private Image[] ballImages;
     private LotteryBall[] currentBalls;
     private String pathToImageDir;
     
     // the vertical point on screen where balls will rest
     private int ballFloor;
     // positions for each of the 6 balls in horizontal plane;
     private int[] horizontalLocations;
     
     // flag for sort operation
     private boolean sorted;
     
     private static final int BALL_SPEED = 3;
     
     // GUI buttons
     private Button playButton, sortButton;
 
     /**
      * Initialises all this LotteryCanvas' member variables and sets its
      * on-screen appearance.
      */
     public void init() {
         
         backgroundColor = new Color(0, 0, 110);
         setBackground(backgroundColor);
         height = getBounds().height;
         width = getBounds().width;
         // want balls to stop just below middle of screen
         ballFloor = height / 2;
 
         // setup the GUI
         setLayout(new BorderLayout());
         Panel buttonPanel = new Panel();
         buttonPanel.setBackground(Color.gray);
 
         playButton = new Button("Pick Numbers");
         sortButton = new Button("Sort Numbers");
         playButton.addActionListener(this);
         sortButton.addActionListener(this);
 
         buttonPanel.add(playButton);
         buttonPanel.add(sortButton);
         add(buttonPanel, BorderLayout.SOUTH);
 
         // load & save ball images
         pathToImageDir = getParameter("image-path");
         ballImages = new Image[NUMBER_OF_BALLS];
 
         for (int i = 0; i < NUMBER_OF_BALLS; i++) {
             ballImages[i] = 
                 getImage(getDocumentBase(), 
                         pathToImageDir + "num" + (i + 1) + ".gif");
         }
 
         // work out absolute positions for each ball
         horizontalLocations = new int[NUMBER_TO_PICK];
         // ensure the balls are centred on screen - work out space allowed at
         // edges. 300 pixels are reserved in the centre of the picture for
         // the balls (6 * 50 pixels)
         int edgeBuffer = ( width - (50 * NUMBER_TO_PICK) ) / 2;
 
         // size of each ball image is 50 pixels. place 1st ball (edgeBuffer) 
         // pixels in from the left, then each ball side by side
         horizontalLocations[0] = edgeBuffer;
         for (int i = 1; i < NUMBER_TO_PICK; i++) {
             horizontalLocations[i] = edgeBuffer + (i * 50);
         }
     }
 
     /**
      * Paints the current scene onto the screen.
      * 
      * @param g
      *            The specified Graphics context
      */
     public void paint(Graphics g) {
         // double buffering to reduce flicker - draw 
         // entire picture in stages offscreen
         Image offScreenImage = createImage(width, height);
         Graphics offScreenGraphics = offScreenImage.getGraphics();
         offScreenGraphics.setColor(backgroundColor);
         offScreenGraphics.fillRect(0, 0, width, height);
 
         if (currentBalls != null) {
             // let all 6 balls draw themselves off-screen first
             for (int i = 0; i < currentBalls.length; i++) {
                 currentBalls[i].drawSelf(offScreenGraphics);
             }
         }
 
         // copy the finished off-screen image onto the canvas in one go
         g.drawImage(offScreenImage, 0, 0, this);
     }
 
     public void update(Graphics g) {
         // override update() to reduce flicker - no need to bother
         // redrawing the background on the on-screen Graphics object,
         // since the whole thing is copied from the off-screen Graphics
         // object once it is complete. To clear the on-screen background
         // first is the default behaviour - do not want this duplication
         paint(g);
     }
 
     /**
      * Calling this method prompts this LotteryCanvas to update the current
      * graphical display.
      */
     public void redrawScene() {
         // repaint calls update() automatically
         repaint();
     }
 
     /**
      * Randomly generates the <i>n</i> numbers and then calls for the creation
      * and animation of corresponding LotteryBall objects
      */
     private void selectNumbers() {
         int[] luckyNumbers = new int[NUMBER_TO_PICK];
 
         // pick random numbers between 1 - 49
         for (int i = 0; i < NUMBER_TO_PICK; i++) {
             boolean alreadyIn = true;
             int newNumber = 0;
 
             // keep generating random numbers until we get a new one
             while (alreadyIn) {
                 alreadyIn = false;
                 newNumber = (int) (Math.random() * 49) + 1;
 
                 // check if it has already been picked
                 for (int j = 0; j < i; j++) {
                     if (luckyNumbers[j] == newNumber) {
                         alreadyIn = true;
                         break;
                     }
                 }
             }
 
             luckyNumbers[i] = newNumber;
         }
 
         sorted = false;
         dropBalls(luckyNumbers);
     }
 
     /**
      * Sorts the current six numbers into ascending order and reorders their
      * appearance on the screen.
      */
     private void sortNumbers() {
         if (currentBalls == null || sorted) {
             return;
         }
 
         // store values represented by the current 6 numbers, ready to sort
         int[] sortedValues = new int[currentBalls.length];
         for (int i = 0; i < currentBalls.length; i++) {
             sortedValues[i] = currentBalls[i].getValue();
         }
 
         // okay to use bubble sort here, since there are only 6 elements
         int maxPasses = sortedValues.length - 1;
 
         for (int i = 0; i < maxPasses; i++) {
             for (int j = maxPasses; j > i; j--) {
                 if (sortedValues[j] < sortedValues[j - 1]) {
                     int temp = sortedValues[j];
                     sortedValues[j] = sortedValues[j - 1];
                     sortedValues[j - 1] = temp;
                 }
             }
         }
 
         // this is much faster than a bubble sort, but is
         // not supported in many browsers (new in JDK 1.2)
         // Arrays.sort(sortedValues);
 
         sorted = true;
         dropBalls(sortedValues);
     }
 
     /**
      * Creates an instance of LotteryBall for each of the numbers 
      * within the <code> values</code> argument, and informs each 
      * of these to begin their own animation.
      * 
      * @arg values An array of 6 ints, each must be within the range 1 - 49
      */
     private void dropBalls(int[] values) {
         if (currentBalls == null) {
             currentBalls = new LotteryBall[NUMBER_TO_PICK];
         }
 
         // starting position will be the same for all 6 balls - hence we
         // can set this position once, outside the sfor loop
         int startYPos;
 
         if (! sorted) {
             // when the balls are first selected, drop from beyond top of screen
             startYPos = -(BALL_SPEED * 17);
         } else {
             // if we are re-drawing after a sort, we do not want the balls to
             // drop from off-screen. In order to provide a clear graphical
             // distinction between the 2 operations, drop from a very low height
             startYPos = ballFloor - (5 * BALL_SPEED);
         }
 
         for (int i = 0; i < values.length; i++) {
             int value = values[i];
             Image img = ballImages[value - 1];
             int xPos = horizontalLocations[i];
 
             currentBalls[i] = 
                 new LotteryBall(img, 
                                 value, 
                                 xPos, 
                                 startYPos,
                                 ballFloor, 
                                 BALL_SPEED, 
                                 this);
         }
 
         // once started, the selected balls will control their own movement
         for (int i = 0; i < NUMBER_TO_PICK; i++) {
             currentBalls[i].drop();
         }
     }
 
     public void actionPerformed(ActionEvent e) {
         
         Button clicked = (Button) e.getSource();
 
         if (clicked == playButton) {
             selectNumbers();
         } else if (clicked == sortButton) {
             sortNumbers();
         }
     }
 }

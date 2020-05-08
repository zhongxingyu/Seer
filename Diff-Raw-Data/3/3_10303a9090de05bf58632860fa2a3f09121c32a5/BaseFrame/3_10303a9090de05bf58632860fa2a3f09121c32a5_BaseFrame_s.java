 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GUIComponents;
 
 import Controls.Controls;
 import java.awt.Dimension;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 /**
  * Seems like somewhere in here we need to have the keylisteners and
  * mouselisteners, since the frame is what is focused when you're clicking and
  * pressing keys. This means that the mousecontrol and keyboardcontrol classes
  * should be instances of keylistener and mouselistener so that we can just
  * pass those to the addlistener methods. Of couse, this is only guesswork, since
  * i've not done it before. Many problems here. I suggest for the time being we do
  * it the simple way and just make listeners in here to listen for things.
  * But there's another problem! If we're using this frame as the main thing, and
  * we're swapping panels, then listeners have to do different things based on game
  * state - might be solvable if we reference the panel and get the game state.
  * 
  * @author michal
  */
 
 /**
  * Constructor sets up the frame by adding the listeners required and creates a new controls object
  * @author Dave
  */
 public class BaseFrame extends JFrame{
 
    // JFrame bFrame;
     Dimension windowSize;
     Controls c;
 
     public BaseFrame(Dimension windowSize) {
      this.windowSize = windowSize;
      add("Center", new GamePanel());
      setSize(windowSize);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      addKeyListener(c);
      addMouseListener(c);
      addMouseMotionListener(c);
     c = new Controls();
      setEnabled(true);
      setVisible(true);
      //   initFrame();     
     }
 
     /*
      * Initialises the frame. Sets the default close operation to exit.
      * Sets the width and height of the frame to the value passed in to
      * the constructor. Sets the frame to be enabled and sets it to be visible.
      */
     /**
      * Redundant for now as moved the initialisation to the constructor
      */
     private void initFrame() {
 //        bFrame = new JFrame("ShootyThing");
 //        Controls c = new Controls();
 //        bFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 //        bFrame.setSize(windowSize);
 //
 //        /*
 //         * Adds a gamepanel to the frame. Better way of doing this needed.
 //         */
 ////        bFrame.addKeyListener(c);
 ////        bFrame.addMouseListener(c);
 ////        bFrame.addMouseMotionListener(c);
 //
 //        bFrame.setEnabled(true);
 //        bFrame.setVisible(true);
     }
 
     /**
      * Adds a panel to the frame.
      * @param addPanel Panel to add to the frame.
      */
     public void addPanel(JPanel addPanel) {
         //bFrame.add(addPanel);
     }
     /**
      * Replaces the current panel in the frame with a new panel.
      * @param repPanel Panel to put into the frame.
      */
     public void replacePanel(JPanel repPanel) {
 //        bFrame.removeAll();
 //        bFrame.add(repPanel);
     }
 
 }

 package combat;
 /** 
  * @author       DevB
  * @version      $Id: CombatMenu.java,v 1.6 2012/04/08 03:50:18 DevA Exp $
  *
  * Revision History:
  *   $Log: CombatMenu.java,v $
  *   Revision 1.6  2012/04/08 03:50:18  DevA
  *   Cleaned up the code to run with Java 1.6: removed unused imports,
  *   fixed some UI focus issues (introduced by new focus "features" in Java since
  *   our original implementation), and made the CommandInterpreter not a Singleton
  *
  *   Revision 1.5  2003/05/30 17:56:10  DevB
  *   Reformatted, also removed score displays as those are
  *   now handled by the scoreboard.
  *
  *   Revision 1.4  2000/05/12 00:01:36  DevC
  *   comments & cleanup
  *
  *   Revision 1.3  2000/05/10 20:45:09  DevC
  *   added method to allow update of score
  *
  *   Revision 1.2  2000/05/09 17:20:22  DevA
  *   Threading support for scoring in place in Game and
  *   PlayerManager.
  *
  *   Revision 1.1  2000/05/09 04:57:18  DevB
  *   Initial revision
  *
  */
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import combat.KeyDialog;
 
 /**
  * This class start up the game.  It puts together the GUI and instantiates
  * the main classes and links them together.
  */
 
 public class CombatMenu extends JPanel {
 
 	private static final long serialVersionUID = -1;
 	
     Game theGame;               //the game object
     JFrame levSelectWindow;     //Window for selecting levels
 
     /**
      * Constructor
      * Simply constructs the Combat menu, which substitutes for a menu bar
      * due to funny drawing issues.
      * @param theGame   The game running in this Combat window.
      */
     public CombatMenu( Game theGame )
     {
         //assign the incoming game object
         this.theGame = theGame;
         
         //set up this menu
         setLayout( new GridLayout( 1, 6 ) );
         setSize( 700, 50 );
         setFocusable(false);
         
         //create the level select window, buttons and score
         levSelectWindow = makeSetLevelWindow();
         buildButtons();
     }
 
     /**
      * Builds all the buttons on the CombatMenu.
      */
     private void buildButtons()
     {
         // Build the button that will start a new game.
         JButton newGame = new JButton( "New Game" );
         newGame.setFocusable(false);
         newGame.addActionListener(
             new ActionListener()
             {
                 public void actionPerformed( ActionEvent e )
                 {
                     theGame.newGame();
                 }
             }
         );
         
         // Build the button that will set a new level.
         JButton setLevel = new JButton( "Set Level" );
         setLevel.setFocusable(false);
         setLevel.addActionListener(
             new ActionListener()
             {
                 public void actionPerformed( ActionEvent e )
                 {
                     levSelectWindow.setVisible(true);
                 }
             }
         );
         
         // Build the button that will set the key commands.
         JButton setKeys = new JButton( "Set Keys" );
         setKeys.setFocusable(false);
         
         // Add an ActionListener here when we get that ready.
        final KeyDialog k = new KeyDialog(new JFrame(), this.theGame.ci);
         k.setVisible(false);
         setKeys.addActionListener(
             new ActionListener()
             {
                 public void actionPerformed( ActionEvent e )
                 {
                     k.setVisible(true);
                 }
             }
         );
         
         // Build the buttons that will control pausing and unpausing the game.
         JButton pause = new JButton( "Pause Game" );
         pause.setFocusable(false);
         JButton resume = new JButton( "Resume Game" );
         resume.setFocusable(false);
         pause.addActionListener(
             new ActionListener()
             {
                 public void actionPerformed( ActionEvent e )
                 {
                     theGame.pause();
                 }
             }
         );
         
         resume.addActionListener(
             new ActionListener()
             {
                 public void actionPerformed( ActionEvent e )
                 {
                     theGame.resume();
                 }
             }
         );
         
         // Build the button that will quit the application.
         JButton quit = new JButton( "Quit" );
         quit.setFocusable(false);
         quit.addActionListener(
             new ActionListener()
             {
                 public void actionPerformed( ActionEvent e )
                 {
                     theGame.quit();
                     System.exit(1);
                 }
             }
         );
         
         // Add all the buttons to this panel.
         add( newGame, 0 );
         add( setLevel, 1 );
         add( setKeys, 2 );
         add( pause, 3 );
         add( resume, 4 );
         add( quit, 5 );
     }
 
     /**
      *  There is currently a window with 2 buttons for pre-made levels.
      *  Ideally, this would be dynamic creating as many buttons as there
      *  are level files, or a menu or something.  For now, however, this 
      *  window allos for 2 level files to be loaded, demonstrating 
      *  how the game can be dynamically loaded from level files
      *
      * @return	The window used for setting the level.
      */
     private JFrame makeSetLevelWindow()
     {
         //create the window and set the attributes
         JFrame levelSelectorWindow = new JFrame( "Level Selector" );
         levelSelectorWindow.setSize( 300, 100 );
         levelSelectorWindow.setResizable( false );
         
         //grab the content pane and set that up with a layout
         Container theContents = levelSelectorWindow.getContentPane();
         theContents.setLayout( new GridLayout( 2, 1 ) );
         
         JLabel pickNum = new JLabel( "    Pick a built in level:" );
         
         // May need to add more buttons later.
         JPanel buttonPanel = new JPanel(new GridLayout(1,2));
         JButton lev1Button = new JButton( "Level 1" );
         lev1Button.addActionListener(
             // The level 1 button's action listener... may need to
             // change file name.
             new ActionListener()
             {
                 public void actionPerformed( ActionEvent e )
                 {
                     theGame.setLevel( "level1.lvl" );
                     levSelectWindow.setVisible(false);
                 }
             }
         );
         
 	    JButton lev2Button = new JButton( "Level 2" );
 	    lev2Button.addActionListener(
             // The level 2 button's action listener... may need to
             // change file name.
             new ActionListener()
             {
                 public void actionPerformed( ActionEvent e )
                 {
                     theGame.setLevel( "level2.lvl" );
                     levSelectWindow.setVisible(false);
                 }
             }
         );
         
         //add the buttons to the button panel
         buttonPanel.add( lev1Button, 0 );
         buttonPanel.add( lev2Button, 1 );
         
         //add the panels to the content pane of the frame
         theContents.add( pickNum, 0 );
         theContents.add( buttonPanel, 1 );
         
         return levelSelectorWindow;
     }
 }

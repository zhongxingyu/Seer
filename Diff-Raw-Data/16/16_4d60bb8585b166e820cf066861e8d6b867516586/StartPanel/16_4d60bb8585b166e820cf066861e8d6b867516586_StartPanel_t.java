 /*
  * A ISGCI specific implementation for the startpage of the tabbedpane that is
  * displayed once a new window is opened. It displays logo, most common
  * functions and news.
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.Robot;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.net.URI;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.border.BevelBorder;
 
 /**
  * A ISGCI specific implementation for the startpage of the tabbedpane that is
  * displayed once a new window is opened. It displays logo, most common
  * functions and news.
  */
 class StartPanel extends JPanel {
 
     /**
      * Should be changed each time this class changes.
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * The website containing the news.
      */
     private static final String NEWSPAGE 
         = "http://92.51.130.117/news.html";
     
     /**
      * The ISGCI Homepage.
      */
     private static final String ISGCIHOME = "www.graphclasses.org";
     
     /**
      * How much time should pass between animationframes.
      */
     private static final int DELAY = 4;
 
     /**
      * How much time should pass between clicks.
      */
     private static final int CLICKDELAY = 600;
     
     /**
      * The parent which contains the startpanel.
      */
     private ISGCIMainFrame mainframe;
 
     /**
      * Creates a new StartPanel for ISGCI.
      * 
      * @param parent
      *            The reference to the window containing this panel.
      */
     public StartPanel(final ISGCIMainFrame parent) {
         
         mainframe = parent;
         
         // create logo
         JLabel logo = new JLabel(new ImageIcon(
                 getClass().getResource("/logo.png")));
         
         JPanel logoPanel = new JPanel();
         logoPanel.setBackground(Color.white);
         logoPanel.setBorder(
                 BorderFactory.createBevelBorder(BevelBorder.RAISED));
         logoPanel.add(logo);
         
         // Logo panel throws nullpointer exceptions if clicked, so
         // consume the events before they reach the panel
         logoPanel.addMouseListener(new MouseEater());
         
         // Buttons
         JButton drawButton = createDrawingTaskButton();
         JButton boundaryButton = createBoundaryTaskButton();
         JButton classButton = createCommonClassesTaskButton();
         JButton inclusionButton = createInclusionRelationTaskButton();
         
         // align icons and text in buttons
         drawButton.setHorizontalAlignment(SwingConstants.LEFT);
         boundaryButton.setHorizontalAlignment(SwingConstants.LEFT);
         classButton.setHorizontalAlignment(SwingConstants.LEFT);
         inclusionButton.setHorizontalAlignment(SwingConstants.LEFT);
         
         // organize buttonlayout
         JPanel buttonPanel = new JPanel();
         BoxLayout buttonlayout 
             = new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS);
         buttonPanel.setLayout(buttonlayout);
         
         JLabel howtoLabel = new JLabel("<html><h1>HOW TO:</h1></html>");
         howtoLabel.setHorizontalAlignment(SwingConstants.CENTER);
         howtoLabel.addMouseListener(new MouseEater());
         
         buttonPanel.add(howtoLabel);
         buttonPanel.add(drawButton);
         buttonPanel.add(boundaryButton);
         buttonPanel.add(classButton);
         buttonPanel.add(inclusionButton);
         
         // organize left layout
         final int gap = 5;
         JPanel leftPanel = new JPanel(new BorderLayout(gap, gap));
         
         leftPanel.add(logoPanel, BorderLayout.PAGE_START);
         leftPanel.add(buttonPanel, BorderLayout.CENTER);
 
         // set news-pane
         final Dimension newsSize = new Dimension(400, 100); 
         
         JComponent newsPane = loadNews();
         newsPane.setPreferredSize(newsSize);
         
         // join everything together
         setLayout(new BorderLayout(gap, gap));
         
         add(leftPanel, BorderLayout.LINE_START);
         add(newsPane, BorderLayout.CENTER);
        
     }
     
     /**
      * Creates a robot on the currently active screen.
      *  
      * @return
      *          A new robot on the currently active screen.
      */
     private Robot getRobot() {
         Robot robot = null;
         
         try {
             // first, try to create a robot
             robot = new Robot();
             
             // then, try to get a robot on the current screen, which is
             // more likely to throw an error
             robot = new Robot(mainframe.getGraphicsConfiguration()
                                 .getDevice());
         } catch (Exception e) {
             System.err.println("Failed to initialize robot for moving mouse!");
         }
         
         return robot;
     }
     
     /**
      * Moves to mouse from the current position to a point by using a slightly
      * tweaked Bresenham's line algorithm.
      * 
      * @param robot
      *          The robot which is used to move the mouse.
      * 
      * @param toComponent
      *          Where the mousepointer should move to.
      */
     private void moveMouseTo(Robot robot, JComponent toComponent) {
         Point to = toComponent.getLocationOnScreen();
         
         // add size/2 so mousepointer isn't in left top corner of component
         to.x += toComponent.getWidth() / 2;
         to.y += toComponent.getHeight() / 2; 
         
         moveMouseTo(robot, to);
     }
     
     /**
      * Moves to mouse from the current position to a point by using a slightly
      * tweaked Bresenham's line algorithm.
      * 
      * @param robot
      *          The robot which is used to move the mouse.
      * 
      * @param to
      *          Where the mousepointer should move to.
      */
     private void moveMouseTo(Robot robot, Point to) {
         Point start = MouseInfo.getPointerInfo().getLocation();
         
         boolean steep = Math.abs(to.y - start.y) > Math.abs(to.x - start.x);
         if (steep) {
             int t;
             
             // swap(start.x, start.y);
             t = start.x;
             start.x = start.y;
             start.y = t;
             
             // swap(to.x, to.y);
             t = to.x;
             to.x = to.y;
             to.y = t;
         }
 
         int deltax = Math.abs(to.x - start.x);
         int deltay = Math.abs(to.y - start.y);
         int error = deltax / 2;
         int ystep;
         int y = start.y;
 
         if (start.y < to.y) {
             ystep = 1;
         } else {
             ystep = -1;
         }
 
         // do "animation"
         for (int x = start.x; x != to.x;
                 x += Math.signum(to.x - start.x) * 1) {
 
             if (steep) {
                 robot.mouseMove(y, x);
             } else {
                 robot.mouseMove(x, y);
             }
 
             error = error - deltay;
 
             if (error < 0) {
                 y = y + ystep;
                 error = error + deltax;
             }
 
             // delay animation so it's visible
             robot.delay(DELAY);
         }
     }
     
     /**
      * Creates a button, that - if clicked - will take over the mouse
      * and guide the user how to do a specific task, in this case:
      * How to draw a hierarchy.
      * 
      * @return
      *          A jbutton that will guide the mouse upon click.
      */
     private JButton createDrawingTaskButton() {
         JButton button = IconButtonFactory.createImageButton(
                 IconButtonFactory.TIP_ICON, 
                 "<html> <br/> Draw a hierarchy of super-  <br/> "
                 + "and/or subclasses <br/> <br/> </html>",
                 "Guides you through the process of drawing a hierarchy of "
                + "super- and/or subclasses of some class(es). Will take over"
                + "your mouse for a short time!"); 
         
         button.addActionListener(new ActionListener() {    
             @Override
             public void actionPerformed(ActionEvent e) {
                 // create robot on the screen where the parent is active
                 Robot mouseRobot = getRobot();
                 
                 if (mouseRobot == null) {
                     return;
                 }
                 
                 // Click draw button
                 JComponent endButton = mainframe.getToolbar().getDrawButton();
                 moveMouseTo(mouseRobot, endButton);
                 
                 // click
                 mouseRobot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                 mouseRobot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                 
             }
         });
         
         return button;
     }
     
     /**
      * Creates a button, that - if clicked - will take over the mouse
      * and guide the user how to do a specific task, in this case:
      * How to look at the boundary of a problem.
      * 
      * @return
      *          A jbutton that will guide the mouse upon click.
      */
     private JButton createBoundaryTaskButton() {
         JButton button = IconButtonFactory.createImageButton(
                 IconButtonFactory.TIP_ICON, 
                 "<html> <br/> Look at the boundary of a   <br/>"
                 + "problem <br/>  <br/> </html>",
                 "Guides you through the process of looking at the "
                + "boundary of a problem. Will take over"
                + "your mouse for a short time!");
         
         button.addActionListener(new ActionListener() {
             
             @Override
             public void actionPerformed(ActionEvent e) {
                 // create robot on the screen where the parent is active
                 final Robot mouseRobot = getRobot();
                 
                 if (mouseRobot == null) {
                     return;
                 }
                 
                 // Open the problemMenu
                 JMenu problemMenu = mainframe.getProblemsMenu();
                 moveMouseTo(mouseRobot, problemMenu);
                 problemMenu.doClick(1);
                 mouseRobot.delay(CLICKDELAY);
                 
                 // let the ui build the dialog
                 SwingUtilities.invokeLater(new Runnable() {
                     
                     @Override
                     public void run() {
 
                         // Hover over the open-problem menuitem
                         JMenuItem problemItem 
                             = mainframe.getOpenProblemMenuItem();
                         
                         moveMouseTo(mouseRobot, problemItem);
                     }
                 });
             }
         });
         
         return button;
     }
     
     /**
      * Creates a button, that - if clicked - will take over the mouse
      * and guide the user how to do a specific task, in this case:
      * How to determine common super- and subclasses of two classes.
      * 
      * @return
      *          A jbutton that will guide the mouse upon click.
      */
     private JButton createCommonClassesTaskButton() {
         JButton button = IconButtonFactory.createImageButton(
                 IconButtonFactory.TIP_ICON, 
                 "<html> <br/> Determine super- and        <br/> "
                 + "subclasses of two classes <br/> <br/> </html>",
                "Guides you through the process of determining " 
                + "super- and subclasses of two classes. Will take over"
                + "your mouse for a short time!");
         
         button.addActionListener(new ActionListener() {
             
             @Override
             public void actionPerformed(ActionEvent e) {
                 // create robot on the screen where the parent is active
                 final Robot mouseRobot = getRobot();
                 
                 if (mouseRobot == null) {
                     return;
                 }
                 
                 // Open the problemMenu
                 final JMenu graphMenu = mainframe.getGraphMenu();
                 moveMouseTo(mouseRobot, graphMenu);
                 graphMenu.doClick(1);
                 mouseRobot.delay(CLICKDELAY);
                 
                 // let the ui build the dialog
                 SwingUtilities.invokeLater(new Runnable() {
                     
                     @Override
                     public void run() {
 
                         // Hover over the open-problem menuitem
                         JMenuItem inclusionProblem 
                             = mainframe.getInclusionMenuItem();
                         
                         moveMouseTo(mouseRobot, inclusionProblem);
                         
                         mouseRobot.delay(CLICKDELAY);
                         inclusionProblem.doClick(1);
                     }
                 });
 
             }
         });
         
         return button;
     }
     
     /**
      * Creates a button, that - if clicked - will take over the mouse
      * and guide the user how to do a specific task, in this case:
      * How to determine the inclusion relation between two classes.
      * 
      * @return
      *          A jbutton that will guide the mouse upon click.
      */
     private JButton createInclusionRelationTaskButton() {
         JButton button = IconButtonFactory.createImageButton(
                 IconButtonFactory.TIP_ICON, 
                 "<html> <br/> Find the inclusion relation <br/> "
                 + "between two classes <br/>  <br/> </html>",
                 "Guides you through the process of determining the "
                + "inclusion relation between two classes. Will take over"
                + "your mouse for a short time!");
 
         button.addActionListener(new ActionListener() {
             
             @Override
             public void actionPerformed(ActionEvent e) {
                 // create robot on the screen where the parent is active
                 final Robot mouseRobot = getRobot();
                 
                 if (mouseRobot == null) {
                     return;
                 }
                 
                 // Open the problemMenu
                 final JMenu graphMenu = mainframe.getGraphMenu();
                 moveMouseTo(mouseRobot, graphMenu);
                 graphMenu.doClick(1);
                 mouseRobot.delay(CLICKDELAY);
                 
                 // let the ui build the dialog
                 SwingUtilities.invokeLater(new Runnable() {
                     
                     @Override
                     public void run() {
 
                         // Hover over the open-problem menuitem
                         JMenuItem inclusionProblem 
                             = mainframe.getInclusionMenuItem();
                         
                         moveMouseTo(mouseRobot, inclusionProblem);
                         
                         mouseRobot.delay(CLICKDELAY);
                         inclusionProblem.doClick(1);
                     }
                 });
 
             }
         });
         
         return button;
     }
     
     /**
      * Loads news into a EditorPane.
      * @return
      *          The component containing the editorpane.
      */
     private JComponent loadNews() {
         JEditorPane jep = new JEditorPane();
         jep.setEditable(false);
 
         try {
           jep.setPage(NEWSPAGE);
         } catch (IOException e) {
           jep.setContentType("text/html");
           jep.setText("<html>Could not load webpage</html>");
         } 
 
         JScrollPane scrollPane = new JScrollPane(jep);
         return scrollPane;
     }
     
     /**
      * Eats up all mouseevents, so nullpointerexceptions will not be thrown
      * if a label or image is clicked within the startpanel.
      *
      */
     static class MouseEater implements MouseListener {
        
         @Override
         public void mouseReleased(MouseEvent e) {
             e.consume();
         }
         
         @Override
         public void mousePressed(MouseEvent e) {
             e.consume();
         }
         
         @Override
         public void mouseExited(MouseEvent e) {
             e.consume();
         }
         
         @Override
         public void mouseEntered(MouseEvent e) {
             e.consume();
         }
         
         @Override
         public void mouseClicked(MouseEvent e) {
             try {
                 if (e.getSource() instanceof JPanel) {
                     java.awt.Desktop.getDesktop().browse(new URI(ISGCIHOME));
                 }
             } catch (Exception err) {
                 System.err.println("Error opening weppage!");
             }
             
             
             e.consume();
         }
     }
 }
 
 /* EOF */

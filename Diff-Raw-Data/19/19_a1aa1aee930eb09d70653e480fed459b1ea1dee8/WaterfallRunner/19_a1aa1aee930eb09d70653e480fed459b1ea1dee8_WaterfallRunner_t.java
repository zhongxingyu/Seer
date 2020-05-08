 package GUI;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Scanner;
 
 import javax.swing.*;        
 
 import commands.FullSetUp;
 
 import backend.Invoker;
 
 /**
  * @author Jessica!
  */
 public class WaterfallRunner
 {
     //defines the window size
     private final static int WINDOW_HEIGHT = 750;
     private final static int WINDOW_WIDTH = 900;
     
     //defines the size of the grid the user can draw on
     //width should stay 16, but height can be adjusted to whatever
    //private final static int MAX_WIDTH = 16;
    
    //update: Armstrong 10/15/13
    //expand to 32 valves
    private final static int MAX_WIDTH = 32;
     private final static int MAX_HEIGHT = 1024;
     
     //create a button group to control all the letters/numbers at once
     private final static ButtonGroup letters = new ButtonGroup();
     
     //the slider to adjust speed of waterfall
     private static JSlider speedSlider = new JSlider(0,2);
     
     //defines the buttons that comprise the grid
     private static JButton[][] grid = new JButton[MAX_HEIGHT][MAX_WIDTH];
     
     //2D array that will tell the pic whether a cell on the grid is to be 
     //drawn or not
     private static boolean[][] buttonStates = new boolean[MAX_HEIGHT][MAX_WIDTH];
     
     //the current letter selected in the text panel -- "" meaning no letter selected
     private static String letterSelected = "";
     
     //file that holds the text drawing instructions
     private static File file = new File("letters.txt");
     
     /**
      * Runs magic waterfall gui
      * @param args
      */
     public static void main(String[] args) 
     {
         //Schedule a job for the event-dispatching thread:
         //creating and showing this application's GUI.
         javax.swing.SwingUtilities.invokeLater(new Runnable() 
         {
             public void run() 
             {
                 createAndShowGUI();
             }
         });
     }
     
     /**
      * Creates the components of the GUI and displays them
      */
     private static void createAndShowGUI() 
     {
         //Create and set up the window
         JFrame frame = createApplicationWindow(); 
         
         //add the export button
         frame.add(createExport());
         
         //add the clear button
         frame.add(createClear());
             
         //add the grid to the window
         frame.add(createGridPanel());
         
         //add the speed adjustment to the window
         frame.add(createSpeedPanel());
         
         //add the letters panel to the window
         frame.add(createTextPanel());
 
         //Display the window.
         frame.setVisible(true);
     }
     
     private static JFrame createApplicationWindow()
     {
         //title of window
         JFrame frame = new JFrame("Waterfall Control Panel");
         
         //set size and window location and resize properties
         frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
         frame.setResizable(false);
         frame.setLocation(10, 10);
         
         //remove all layouts from this frame
         frame.setLayout(null);
         
         //set close operation
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         return frame;
     }
    
     /**
      * Defines what to do when a grid cell is clicked
      * @param i row in grid
      * @param k column in grid
      */
     private static void clickButton(int i, int k)
     {
         //set the color
         grid[i][k].setBackground(Color.BLACK);
         grid[i][k].setOpaque(true);
         grid[i][k].setActionCommand("");
         
         //set the boolean state array
         buttonStates[i][k] = true;
     }
 
     /**
      * Defines what to do when a grid cell that was clicked
      * is clicked again
      * @param i row in grid
      * @param k cloumn in grid
      */
     private static void unclickButton(int i, int k)
     {
         grid[i][k].setBackground(Color.WHITE);
         grid[i][k].setActionCommand("click");
 
         buttonStates[i][k] = false;
     }
     
     /**
      * Creates and defines the behavior of the export picture button
      * @return the button
      */
     private static JButton createExport()
     {
         //create button and set position and visual appearance
         JButton export = new JButton("Export Picture");
         export.setFocusable(false);
         export.setBounds(10,10,100,30);
         export.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), ""));
         
         //add a click event to this button
         export.addActionListener(new ActionListener()
         {
             /**
              * For now, send the information to the console. Will eventually need to send to
              * Wellington's code
              */
             public void actionPerformed(ActionEvent e)
             {
                 //get the current placement of the slider
                 System.out.println("Speed: " + speedSlider.getValue());
                 
                 //print the buttonState array
                 for(int i = 0; i < MAX_HEIGHT; i++)
                 {
                     for(int k = 0; k < MAX_WIDTH; k++)
                     {
                         System.out.print(buttonStates[i][k] + " ");
                     }
                     
                     System.out.println();
                 }
                 
                 FullSetUp command= new FullSetUp(buttonStates,speedSlider.getValue());
                 Invoker.getSingleton().execute(command);
             }  
         });
         
         return export;
     }
 
     /**
      * Creates and defines the functionality of the clear button
      * @return the button
      */
     private static Component createClear()
     {
         //create button and set position and visual appearance
         JButton clear = new JButton("Clear Grid");
         clear.setFocusable(false);
         clear.setBounds(150,10,100,30);
         clear.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), ""));
         
         //add a click event to this button
         clear.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 //travel through the entire grid and deselect every cell
                 for(int i = 0; i < MAX_HEIGHT; i++)
                 {
                     for(int k = 0; k < MAX_WIDTH; k++)
                     {
                         unclickButton(i, k);
                     }
                 }
             }  
         });
         
         return clear;
     }
 
 
     /**
      * @return the group of letter button objects
      */
     public static ButtonGroup getLetters()
     {
         return letters;
     }
 
     /**
      * Creates the panel in the window that allows the user to select a 
      * letter/number to draw on the grid
      * @return the panel
      */
     private static Component createTextPanel()
     {
         //declare grid area for letters and numbers and its appearance
         final JPanel textArea = new JPanel(new GridLayout(6,6));
         textArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Text"));
         textArea.setBounds(5, 50, 250, 550);
         
         //the current char to put on the letter grid
         char c = '0'; 
         
         //fill the grid
         for(int i = 0; i < 6; i++)
         {
             for(int k = 0; k < 8; k++)
             {
                 //only put 0-9 and A-Z on the grid
                 if(c <= '9' || (c >= 'A' && c <= 'Z'))
                 {
                     //create a new button for the char and define its appearance
                     final JButton temp = new JButton("" + c);
                     temp.setActionCommand("");
                     temp.setBackground(Color.WHITE);
                     temp.setBorder(BorderFactory.createLineBorder(Color.black));
                     
                     //add the button to the grid and to the button group
                     textArea.add(temp);
                     letters.add(temp);
                     temp.setActionCommand("click");
                     
                     //define behavior for the button when clicked
                     temp.addActionListener(new ActionListener()
                     {
                         public void actionPerformed(ActionEvent e)
                         {
                             if(temp.getActionCommand().equals("click"))
                             {
                                 //on click, get the entire group of buttons
                                 Enumeration<AbstractButton> elements = letters.getElements();
 
                                 //cycle through all the buttons, deselecting every button to ensure
                                 //only one char can be selected at a time
                                 while(elements.hasMoreElements())
                                 {
                                     JButton next = (JButton) elements.nextElement();
                                     next.setActionCommand("click");
                                     next.setBackground(Color.WHITE);
                                     temp.setSelected(false);
                                 }
 
                                 //select the button the user just clicked on
                                 temp.setSelected(true);
                                 temp.setBackground(Color.LIGHT_GRAY);
                                 temp.setOpaque(true);
                                 temp.setActionCommand("");
                                 letterSelected = temp.getText();
                             }
                             else
                             {
                                 //deselect a currently selected button
                                 temp.setSelected(false);
                                 temp.setBackground(Color.WHITE);
                                 temp.setActionCommand("click");
                                 letterSelected = "";
                             }     
                         }   
                     });
                 }
                 //increment character
                 c++;
             }
         }
         
         return textArea;
     }
 
     /**
      * Creates the panel in the window that allows the user to select the speed of the water
      * @return the panel
      */
     private static JPanel createSpeedPanel()
     {
         //create a new panel for the slider with a title and border
         JPanel speedPanel = new JPanel();        
         speedPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Speed Adjustment"));
        
         //create the speed slider and add it to the panel
         speedPanel.add(speedSlider);
         
         //make the ticks show up and the cursor snap to them
         speedSlider.setPaintTicks(true);
         speedSlider.setSnapToTicks(true);
         speedSlider.setMajorTickSpacing(1);
         speedSlider.setPaintLabels(true);
         
         //Create the table that holds the labels for the slider
         //Table indexed by tick # and stores associated label
         Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
         labelTable.put( new Integer(0), new JLabel("Slow") );
         labelTable.put( new Integer(1), new JLabel("Medium") );
         labelTable.put( new Integer(2), new JLabel("Fast") );
         speedSlider.setLabelTable(labelTable);
        
         //set the position of the speed panel
         speedPanel.setBounds(5, 605, 250, 100);
         
         return speedPanel;
     }
 
     /**
      * Creates a panel that contains the grid the user can draw in
      * @return the panel
      */
     private static JScrollPane createGridPanel()
     {
         //the base for the grid of buttons to select
         JPanel canvas = new JPanel(new GridLayout(MAX_HEIGHT,MAX_WIDTH));
         
         //a scrolling pane the canvas will sit in so it can be long and thin
         JScrollPane scrollPane = new JScrollPane(canvas);
         
         //define positioning of the panel and appearance
         scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Drawing Area"));
         scrollPane.setBounds(260,5,625,700);
 
         //the height of the grid squares
         Dimension d = new Dimension(0,30);
        
         //cycle through the grid space and create buttons to represent each cell
         for(int i = 0; i < MAX_HEIGHT; i++)
         {
             for(int k = 0; k < MAX_WIDTH; k++)
             {
                 //create a new button and define its appearance
                 grid[i][k] = new JButton("");
                 grid[i][k].setBackground(Color.WHITE);
                 grid[i][k].setBorder(BorderFactory.createLineBorder(Color.black));
                 grid[i][k].setPreferredSize(d);
                 
                 grid[i][k].setActionCommand("click");
                 
                 //temporary variables to get around addActionListener's quirks
                 final int temp1 = i;
                 final int temp2 = k;
                 
                 //add clicking behavior
                 grid[i][k].addActionListener(new ActionListener()
                 {
                     public void actionPerformed(ActionEvent e)
                     {
                         if(e.getActionCommand().equals("click"))
                         {
                             //if there is a letter selected in the letter panel, draw that letter
                             //on click
                             if(!letterSelected.equals(""))
                             {
                                 //code to click letter buttons
                                 try
                                 {
                                     //setup for file reading
                                     Scanner sc = new Scanner(file);
                                     
                                     //look for the selected char in the file
                                     while(!sc.next().equals(letterSelected));
                                     
                                     //read the next lines of the file for drawing instructions
                                     for(int x = 0; x < 10; x++)
                                     {
                                         //make sure letter fits in grid vertically
                                         if(temp1+x < MAX_HEIGHT)
                                         {
                                             String line = sc.next();
                                             for(int y = 0; y < 9; y++)
                                             {
                                                 //make sure letter fits in grid horizontally, and use the
                                                 //clicked cell as the top center point of the letter
                                                 if(y <= 4 && temp2-(4-y) >= 0 && line.charAt(y) == 'X')
                                                 {
                                                     clickButton(temp1+x, temp2-(4-y));
                                                 }
                                                else if(y > 4 && temp2+(y-4) < MAX_WIDTH && line.charAt(y) == 'X')
                                                 {
                                                     clickButton(temp1+x, temp2+(y-4));
                                                 }
 
                                             }      
                                         }
                                         
                                     }
                                 }
                                 catch (FileNotFoundException e1)
                                 {
                                     // TODO Auto-generated catch block
                                     e1.printStackTrace();
                                 }
                             }
                             else
                             {
                                 //no letter was selected, so just click one cell
                                 clickButton(temp1, temp2);
                             }
                         }
                         else
                         {
                             //deselect the clicked cell
                             unclickButton(temp1, temp2);
                         }
                     }
                 });
                 canvas.add(grid[i][k]);
             }
         }
 
         return scrollPane;
     }
 
     
 
     
 }

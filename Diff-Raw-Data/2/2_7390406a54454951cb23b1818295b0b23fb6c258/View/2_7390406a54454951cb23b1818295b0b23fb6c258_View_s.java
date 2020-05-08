 
 /**
  * Write a description of class View here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JLabel;
 import javax.swing.border.EmptyBorder;
 
 import java.awt.Dimension;
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.GridLayout;
 import java.awt.FlowLayout;
 import java.awt.Image;
 import java.awt.event.*;
 import java.awt.Color;
 import java.awt.Font;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.Timer;
 
 public class View extends JFrame
 {
     // This panel is used to hold the current page title and remaining points
     JPanel pageStartPanel;
     // This panel is renewed when new things are shown so the reference is needed. 
     JPanel cards;
     // This label is used to hold the remainging points
     JLabel points;
     JLabel currentPaneTitle;
     Controller controller;
     // constructor
     public View(Controller controller)
     {
         this.controller = controller;
         // initialize the page start panel and add a border
         pageStartPanel = new JPanel(new BorderLayout());
         pageStartPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 
         // Initial points to 100
         points = new JLabel("Calculating Points...");
         // intialize to begin screen title
         currentPaneTitle = new JLabel("Starting up...");
 
         // add the points and title label to the pageStartPanel
         pageStartPanel.add(points, BorderLayout.LINE_END);
         pageStartPanel.add(currentPaneTitle, BorderLayout.LINE_START);
 
         // initialize the centerPanel
         cards = new JPanel(new CardLayout());
 
         this.getContentPane().add(cards, BorderLayout.CENTER);
         this.getContentPane().add(pageStartPanel, BorderLayout.PAGE_START);
         this.setTitle("Delayed Consequence");
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setSize(800, 600);
         this.setMinimumSize(new Dimension(600, 600));
         // frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
         this.setLocationRelativeTo(null);
         // frame.setUndecorated(true);
         this.setVisible(true);
     }
     
     public void setCurrentTitle(String title)
     {
         currentPaneTitle.setText(title);
     }
     
     public void setPoints(int points)
     {
         this.points.setText("Remaining Points: " + points);
     }
     
     public void showInstructionSheet()
     {
         JPanel newPanel = new JPanel(new BorderLayout());
         newPanel.setBackground(new Color(156,181,228)); 
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         JTextArea introTextArea = new JTextArea();
         ArrayList<String> introduction = Setup.getITS();
         introTextArea.setLineWrap(true);
         introTextArea.setWrapStyleWord(true);
         introTextArea.setEditable(false);
         introTextArea.setBackground(new Color(0,0,0,0));
         for(int i = 0; i < introduction.size(); i++)
         {
             introTextArea.setWrapStyleWord(true);
             introTextArea.append(introduction.get(i));
             introTextArea.append("\n");
         }     
         JPanel beginButtonPanel = new JPanel(new BorderLayout());
         beginButtonPanel.setBackground(new Color(156,181,228));
         pageStartPanel.setBackground(new Color(156,181,228));
         JButton beginButton = new JButton("Begin");
         beginButton.addActionListener(new BeginButtonAction());        
         newPanel.add(introTextArea, BorderLayout.CENTER);
         beginButtonPanel.add(beginButton, BorderLayout.LINE_END);
         newPanel.add(beginButtonPanel, BorderLayout.PAGE_END);
         cards.add(newPanel, "Instruction Sheet");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     } 
     
     public void establishPreference(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(1,2,10,10));
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(0,153,153));
         pageStartPanel.setBackground(new Color(0,153,153));        
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         left.setFont(new Font("Dialog", Font.BOLD, 200));
         left.addActionListener(new ButtonAction(group, leftIndex));
         right.addActionListener(new ButtonAction(group, rightIndex));
         right.setFont(new Font("Dialog", Font.BOLD, 200));
         newPanel.add(left);
         newPanel.add(right);
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }    
     
     public void presentCondition(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(1,2,40,40));
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(0,153,153));
         pageStartPanel.setBackground(new Color(0,153,153));        
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         left.setFont(new Font("Dialog", Font.BOLD, 200));
         left.addActionListener(new ButtonAction(group, leftIndex));
         right.addActionListener(new ButtonAction(group, rightIndex));
         right.setFont(new Font("Dialog", Font.BOLD, 200));
         newPanel.add(left);
         newPanel.add(right);
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }
     
     public void dvrc(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(1,2,10,10));
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(142,180,227));
         pageStartPanel.setBackground(new Color(142,180,227));        
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         left.setFont(new Font("Dialog", Font.BOLD, 200));
         left.addActionListener(new dvrcButtonOne(group, leftIndex));
         right.addActionListener(new dvrcButtonOne(group, rightIndex));
         right.setFont(new Font("Dialog", Font.BOLD, 200));
         newPanel.add(left);
         newPanel.add(right);
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }
     
     public void dvrc2()
     {
         JPanel newPanel = new JPanel();
        newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(240,240,240));
         pageStartPanel.setBackground(new Color(240,240,240));        
         JTextArea reading = new JTextArea();
         JScrollPane sp = new JScrollPane(reading);
         ArrayList<String> introduction = Setup.getRI();
         for(int i = 0; i < introduction.size(); i++)
             {
                 reading.append(introduction.get(i));
                 reading.append("\n");
             }
         reading.setLineWrap(true);
         reading.setWrapStyleWord(true);
         reading.setEditable(false);
         reading.setBackground(new Color(0,0,0,0));        
         newPanel.add(sp, BorderLayout.CENTER);
         cards.add(newPanel, "Read now!");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);  
         
         ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 //...Perform a task...
 
                 dvrc3('D', '3', 0, 0, 1);
             }
             };
         Timer timer = new Timer(Setup.getRD(), taskPerformer);
         timer.setRepeats(false);
         timer.start();       
     }
     
     public void dvrc3(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(1,2,10,10));
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(217,150,148));
         pageStartPanel.setBackground(new Color(217,150,148));        
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         left.setFont(new Font("Dialog", Font.BOLD, 200));
         left.addActionListener(new dvrcButtonTwo(group, leftIndex));
         right.addActionListener(new dvrcButtonTwo(group, rightIndex));
         right.setFont(new Font("Dialog", Font.BOLD, 200));
         newPanel.add(left);
         newPanel.add(right);
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }
     
     public void dvrc4()
     {
         JPanel newPanel = new JPanel();
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(179,162,199));
         pageStartPanel.setBackground(new Color(179,162,199));   
         JTextArea reading = new JTextArea("Man You fucked up. Your shit sucks. You picked the wrong one. I fucked your mother. mIpsumLorumIpsumLorumIpsumLorumIpsumLorumIpsumLorumIpsum");
         reading.setLineWrap(true);
         reading.setWrapStyleWord(true);
         reading.setEditable(false);
         reading.setBackground(new Color(0,0,0,0));
         newPanel.add(reading, BorderLayout.CENTER);
         cards.add(newPanel, "Read now!");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards); 
         
         
          ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 //...Perform a task...
 
                 controller.printConditionStats();
             }
             };
         Timer timer = new Timer(Setup.getRD(), taskPerformer);
         timer.setRepeats(false);
         timer.start();       
     }
     
     public class dvrcButtonTwo implements ActionListener
     {
         int group;
         int index;
         // constructor
         public dvrcButtonTwo(int group, int index)
         {
             this.group = group;
             this.index = index;
         }
         public void actionPerformed(ActionEvent e)
         {
             System.out.println("DEBUG - dvrcButtonAction.actionPerformed() - Symbol clicked incrementConditionCount(" + group + "," + index + ")");
             controller.incrementConditionCount(Model.DVRC_ENUM,index);
             System.out.println("DEBUG - dvrcButtonAction.actionPerformed() - invokeContinueBaselineCondition");
             dvrc4();
             
         }
     }
     
      public class dvrcButtonOne implements ActionListener
     {
         int group;
         int index;
         // constructor
         public dvrcButtonOne(int group, int index)
         {
             this.group = group;
             this.index = index;
         }
         public void actionPerformed(ActionEvent e)
         {
             System.out.println("DEBUG - dvrcButtonAction.actionPerformed() - Symbol clicked incrementConditionCount(" + group + "," + index + ")");
             controller.incrementConditionCount(Model.DVRC_ENUM, index);
             System.out.println("DEBUG - dvrcButtonAction.actionPerformed() - invoke dvrc2()");
             dvrc2();
             
         }
     }
     
     public void timerScreen(int a)
     {
         Timer timer = new Timer( 1000, new ActionListener(){
                     @Override
                     public void actionPerformed( ActionEvent e ){
                         CardLayout cl = (CardLayout) cards.getLayout();
                         cl.next(cards);
                     }
                 } );
         timer.start();
         timer.setRepeats(false);
     } 
 
     
     // nested class
     public class BeginButtonAction implements ActionListener
     {
 
         public void actionPerformed(ActionEvent e)
         {
             System.out.println("DEBUG: BeginButtonAction.actionPerformed() - begin button clicked, invoke getBaselineCondition().");
             controller.getBaselineCondition();
         }
     }
     public class ButtonAction implements ActionListener
     {
         int group;
         int index;
         // constructor
         public ButtonAction(int group, int index)
         {
             this.group = group;
             this.index = index;
         }
         public void actionPerformed(ActionEvent e)
         {
             System.out.println("DEBUG - ButtonAction.actionPerformed() - Symbol clicked incrementHitCount(" + group + "," + index + ")");
             controller.incrementHitCount(group, index);
             System.out.println("DEBUG - ButtonAction.actionPerformed() - invokeContinueBaselineCondition");
             controller.continueBaselineCondition();
         }
     }
 }

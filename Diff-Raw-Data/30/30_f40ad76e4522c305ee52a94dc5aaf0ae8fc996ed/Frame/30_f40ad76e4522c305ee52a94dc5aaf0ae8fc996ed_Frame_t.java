 package pomodoro.frame;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.BevelBorder;
 import pomodoro.listener.Button;
 
 /**
  * Frame class handles JFrame & components
  * @author 200261705
  */
 public class Frame extends JFrame implements Runnable
 {
     private final Dimension SIZE = new Dimension(600, 500);
     
     public Thread loop;
     public boolean run, timer, working;
     
     public ArrayList<String> taskList;
     
     private Button button;
     
     public int minutes, seconds, taskID;
     public String taskInfo;
     
     public JLabel time, currentTask;
     private JLabel enterTask, tasks;
     
     public JTextField taskInput, taskOne, taskTwo, taskThree;
     
     private JButton start, stop, work, rest, add;
     public JButton setTaskOne, setTaskTwo, setTaskThree,
                     completeTaskOne, completeTaskTwo,
                     completeTaskThree, removeTaskOne,
                     removeTaskTwo, removeTaskThree;
     
     public JPanel panel, topPanel, middlePanel, bottomPanel,
                   topLeftPanel, topTopPanel, topRightPanel, topBottomPanel,
                   middleTopPanel, middleBottomPanel, middleCenterPanel;
     
     /**
      * Frame constructor
      */
     public Frame()
     {
         this.setSize(SIZE);
         this.setTitle("Pomodoro Task || by Michael");
         this.setLocationRelativeTo(null);
         this.setResizable(false);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         init();
         
         this.setVisible(true);
     } //end of Frame constructor
     
     /**
      * init method
      */
     private void init()
     {
         initVariables();
         addComponents();
         
         loop.start();
         run = true;
     } //end of init method
     
     /**
      * initVariables method
      */
     private void initVariables()
     {
         loop = new Thread(this);
         run = false;
         timer = false;
         working = true;
         
         button = new Button(this);
         
         minutes = 25;
         seconds = 0;
         taskID = 0;
         taskInfo = "None";
         
         taskList = new ArrayList<>();
     } //end of initVariables method
     
     /**
      * addComponents method
      */
     private void addComponents()
     {
         panel = new JPanel();
         panel.setPreferredSize(new Dimension(600, 500));
         topPanel = new JPanel();
         topPanel.setPreferredSize(new Dimension(600, 150));
         middlePanel = new JPanel();
         middlePanel.setPreferredSize(new Dimension(600, 250));
         middleTopPanel = new JPanel();
         middleTopPanel.setPreferredSize(new Dimension(600, 50));
         middleCenterPanel = new JPanel();
         middleCenterPanel.setPreferredSize(new Dimension(600, 50));
         middleBottomPanel = new JPanel(new GridLayout(3, 4));
         bottomPanel = new JPanel();
         bottomPanel.setPreferredSize(new Dimension(600, 100));
         topLeftPanel = new JPanel();
         topTopPanel = new JPanel();
         topTopPanel.setPreferredSize(new Dimension(400, 100));
         topBottomPanel = new JPanel();
         topBottomPanel.setPreferredSize(new Dimension(400, 100));
         topRightPanel = new JPanel();
         
         time = new JLabel(minutes + " : " + seconds);
         time.setSize(new Dimension(25, 100));
         time.setFont(new Font("Arial", Font.BOLD, 48));
         currentTask = new JLabel("Current Task: " + "Set a task.");
         enterTask = new JLabel("Enter Task: ");
         tasks = new JLabel("Tasks");
         tasks.setFont(new Font("Arial", Font.BOLD, 36));
         currentTask = new JLabel("Status:  " + "  |  Task:  " + taskInfo);
         currentTask.setPreferredSize(new Dimension(250, 25));
         currentTask.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.GRAY, Color.GRAY));
         
         taskInput = new JTextField();
         taskInput.setPreferredSize(new Dimension(400, 25));
         taskOne = new JTextField();
        taskOne.setEditable(false);
         taskTwo = new JTextField();
        taskTwo.setEditable(false);
         taskThree = new JTextField();
        taskThree.setEditable(false);
         
         start = new JButton("Start");
         start.addActionListener(button);
         stop = new JButton("Stop");
         stop.addActionListener(button);
         work = new JButton("Work");
         work.addActionListener(button);
         rest = new JButton("Rest");
         rest.addActionListener(button);
         add = new JButton("Add");
         add.addActionListener(button);
         setTaskOne = new JButton("Set");
         setTaskOne.addActionListener(button);
         setTaskTwo = new JButton("Set");
         setTaskTwo.addActionListener(button);
         setTaskThree = new JButton("Set");
         setTaskThree.addActionListener(button);
         completeTaskOne = new JButton("Complete");
         completeTaskOne.addActionListener(button);
         completeTaskTwo = new JButton("Complete");
         completeTaskTwo.addActionListener(button);
         completeTaskThree = new JButton("Complete");
         completeTaskThree.addActionListener(button);
         removeTaskOne = new JButton("Remove");
         removeTaskOne.addActionListener(button);
         removeTaskTwo = new JButton("Remove");
         removeTaskTwo.addActionListener(button);
         removeTaskThree = new JButton("Remove");
         removeTaskThree.addActionListener(button);
         
         topTopPanel.add(start);
         topTopPanel.add(time);
         topTopPanel.add(stop);
         topBottomPanel.add(work);
         topBottomPanel.add(rest);
         
         topPanel.add(topLeftPanel, BorderLayout.WEST);
         topPanel.add(topTopPanel, BorderLayout.NORTH);
         topPanel.add(topBottomPanel, BorderLayout.SOUTH);
         topPanel.add(topRightPanel, BorderLayout.EAST);
         
         middleTopPanel.add(enterTask);
         middleTopPanel.add(taskInput);
         middleTopPanel.add(add);
         
         middleCenterPanel.add(tasks);
         
         middleBottomPanel.add(taskOne);
         middleBottomPanel.add(completeTaskOne);
         middleBottomPanel.add(setTaskOne);
         middleBottomPanel.add(removeTaskOne);
         middleBottomPanel.add(taskTwo);
         middleBottomPanel.add(completeTaskTwo);
         middleBottomPanel.add(setTaskTwo);
         middleBottomPanel.add(removeTaskTwo);
         middleBottomPanel.add(taskThree);
         middleBottomPanel.add(completeTaskThree);
         middleBottomPanel.add(setTaskThree);
         middleBottomPanel.add(removeTaskThree);
         
         middlePanel.add(middleTopPanel, BorderLayout.NORTH);
         middlePanel.add(middleCenterPanel, BorderLayout.CENTER);
         middlePanel.add(middleBottomPanel, BorderLayout.SOUTH);
         
         bottomPanel.add(currentTask, BorderLayout.NORTH);
         
         panel.add(topPanel, BorderLayout.NORTH);
         panel.add(middlePanel, BorderLayout.CENTER);
         panel.add(bottomPanel, BorderLayout.SOUTH);
         
         add(panel);
     } //end of addComponents method
     
     
     /**
      * run method
      */
     @Override
     public void run()
     {
         while(run)
         {
             if(timer)
             {   
                 if(!(minutes == 0 && seconds == 0))
                 {
                     if(seconds == 0)
                     {
                         minutes--;
                         seconds = 60;
                     }
 
                     seconds--;
                     time.setText(minutes + " : " + seconds);
                     try
                     {
                         Thread.sleep(960);
                     }
                     catch(Exception e)
                     {
                         e.printStackTrace();
                     }
                 }
                 else
                 {
                     if(working)
                     {
                         Toolkit.getDefaultToolkit().beep();
                         minutes = 25;
                         seconds = 0;
                         JOptionPane.showMessageDialog(null, "Times up! Task completed!");
                     }
                     else if(!working)
                     {
                         Toolkit.getDefaultToolkit().beep();
                        minutes = 5;
                        seconds = 0;
                        JOptionPane.showMessageDialog(null, "Times up! Rest completed!"); 
                     }
                 }
             }
             
             time.setText(minutes + " : " + seconds);
             currentTask.setText("Status:  " + (working ? "Work" : "Rest") + "  |  Task:  " + taskInfo);
             
             try
             {
                 Thread.sleep(20);
             }
             catch(Exception e)
             {
                 e.printStackTrace();
             }
         }
     } //end of run method
     
 } //end of Frame class

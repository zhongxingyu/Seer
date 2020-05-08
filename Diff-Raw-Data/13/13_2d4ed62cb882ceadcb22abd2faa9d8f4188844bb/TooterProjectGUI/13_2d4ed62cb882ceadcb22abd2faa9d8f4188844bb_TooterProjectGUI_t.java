 package GUI;
 
import util.LibraryUtils;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class TooterProjectGUI {
 
     static {
         LibraryUtils.setLibraryPath();
      }
 
 
     private JFrame frame = new JFrame();
     private JPanel panelCont = new JPanel();
     private JPanel panelMain = new JPanel();
     private JPanel panelSplash = new JPanel();
     private JPanel panelPiano = new JPanel();
     private JPanel panelChoose = new JPanel();
     CardLayout cl = new CardLayout();
 
     public TooterProjectGUI() {
         panelCont.setLayout(cl);
         panelCont.setBackground(Color.WHITE);
 
         // choice panel
         JPanel main = new JPanel();
         main.setLayout(new GridLayout(2, 1, 0, 0));
         main.setBackground(Color.WHITE);
         main.setSize(new Dimension(600, 700));
 
         // Create the top panel
         JPanel topPanel = new JPanel();
         topPanel.setBackground(Color.WHITE);
 
         // Add image to top panel
         JLabel label = new JLabel();
         label.setIcon(new ImageIcon("Resources/images/tooterlogo.png"));// your image here
         topPanel.add(label);
 
         main.add(topPanel);
 
         // Create the bottom panel
         JPanel botPanel = new JPanel();
         botPanel.setBackground(Color.WHITE);
         // Join a song
         JButton join = new JButton("Join a Song!");
         join.addActionListener(new ActionListener(){
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 cl.show(panelCont, "splash");
             }
         });
         botPanel.add(join);
         // Start a song
         JButton start = new JButton("Start a song!");
         start.addActionListener(new ActionListener(){
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 cl.show(panelCont, "choice");
             }
         });
         botPanel.add(start);
         main.add(botPanel);
         panelMain.add(main);
 
         // piano panel
         Piano piano = new Piano(cl, panelCont, true);
         panelPiano.add(piano);
 
         // choosePanel
         ChooseInstrument choose = new ChooseInstrument(cl, panelCont);
         panelChoose.add(choose);
 
         // splash panel
         JoinHost splash =  new JoinHost (cl, panelCont);
         panelSplash.add(splash);
 
         panelCont.add(panelChoose, "choice");
         panelCont.add(panelPiano, "Piano");
         panelCont.add(panelSplash, "splash");
         panelCont.add(panelMain, "main");
         cl.show(panelCont, "main");
 
         frame.add(panelCont);
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         frame.pack();
         frame.setSize(600, 700);
         frame.getContentPane().setBackground(Color.WHITE);
         frame.setVisible(true);
     }
     /**
      * @param args
      */
     public static void main(String[] args) {
         SwingUtilities.invokeLater(new Runnable(){
             @Override
             public void run(){
                 new TooterProjectGUI();
             }
         });
 
     }
 
 }

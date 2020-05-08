 // Importiert alle Klassen aus den wichtigsten Paketen zur GUI-Entwicklung
 import java.io.File;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 // Definiert eine neue Klasse als Unterklasse von JFrame
 public class MeineGUI extends JFrame implements ActionListener, MusicPlayerListener
 {
     private JLabel percent;
     private JButton setButton;
     private JButton stopButton;
     private JButton playButton;
     private MusicPlayer player;
     private JFileChooser fileC;
     public MeineGUI()
     {
         player = new MusicPlayer();
         setLayout(new FlowLayout());
 
         setButton = new JButton("set");
         playButton = new JButton("play");
         stopButton = new JButton("stop");
        percent = new JLabel("0%");
         fileC = new JFileChooser();
 
         setButton.addActionListener(this);
         playButton.addActionListener(this);
         stopButton.addActionListener(this);
         player.addListener(this);
         
         add(setButton);
         add(playButton);
         add(stopButton);
         add(percent);
 
         setSystemlook();
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         pack();
         setVisible(true);
     }
 
     public static void main(String[] args)
     {
         new MeineGUI();
     }
 
     public void actionPerformed(ActionEvent e)
     {
         if(e.getSource() == setButton)
         {
             int returnVal = fileC.showOpenDialog(MeineGUI.this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File datei = fileC.getSelectedFile();
                 player.open(datei.getAbsolutePath());
             }
         }
         else if(e.getSource() == playButton)
             player.play();
         else if(e.getSource() == stopButton)
             player.stop();
     }
 
     public void positionChanged(int newPosition)
     {
         percent.setText(newPosition+" %");
     }
 
     public void setSystemlook(){
         try{
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch(Exception e){
             System.out.println("Error setting Look and feel: " + e);
         }
     }
 }

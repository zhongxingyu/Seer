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
     private JButton pauseButton;
     private JButton playButton;
     private MusicPlayer player;
     private JFileChooser fileC;
    private boolean p;
    private boolean f;
     public MeineGUI()
     {
        f = false;
        p = false;
         player = new MusicPlayer();
         setLayout(new FlowLayout());
 
         pauseButton = new JButton("Pause");
         setButton = new JButton("set");
         playButton = new JButton("play");
         stopButton = new JButton("stop");
         percent = new JLabel("000%");
         fileC = new JFileChooser();
 
         pauseButton.addActionListener(this);
         setButton.addActionListener(this);
         playButton.addActionListener(this);
         stopButton.addActionListener(this);
         player.addListener(this);
 
         add(setButton);
         add(playButton);
         add(pauseButton);
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
                f = true;
             }
         }
        else if(e.getSource() == playButton && f)
         {  
            if(!p)
             {
                 player.play();
                p = true;
             }
             else
                 player.resume();
         }
         else if(e.getSource() == stopButton)
         {
             player.stop();
            p = false;
         }
         else if(e.getSource() == pauseButton)
             player.pause();
 
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

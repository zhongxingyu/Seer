 package pure_mp3;
 
 /**
  * Write a description of class PlayerMenu here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 public class PlayerMenu extends JPanel
 {
 	private static final long serialVersionUID = 2385007980763532219L;
     private JButton prev;
     private JButton play;
     private JButton next;
     private JSlider volume;
     
     public PlayerMenu()
     {
         super();
         setLayout(null);
         
         prev = new JButton("<|");
         prev.addActionListener(new ActionListener()
         {               
             public void actionPerformed(ActionEvent e)
             {
                 playPrev();
             }
         });
         prev.setBounds(0,20,50,40);
         add(prev);
         
         play = new JButton(">");
         play.setBounds(50,20,80,40);
         play.addActionListener(new ActionListener()
         {               
             public void actionPerformed(ActionEvent e)
             {
                 play();
             }
         });
         add(play);
         
         //TODO: PAUSE-BUTTON
         
         next = new JButton("|>");
         next.setBounds(130,20,50,40);
         next.addActionListener(new ActionListener()
         {               
             public void actionPerformed(ActionEvent e)
             {
                 playNext();
             }
         });
         add(next);
         
         volume = new JSlider();
         volume.setOrientation(JSlider.VERTICAL);
         volume.setMinimum(0);
         volume.setMaximum(100);
         volume.setValue(100);
         volume.addChangeListener(new ChangeListener()
         {
             public void stateChanged(ChangeEvent e)
             {
                 JSlider source = (JSlider)e.getSource();
                 source.setValueIsAdjusting(false);
                 changeVolume((int)source.getValue());
             }
         });
         volume.setBounds(196,0,20,80);
         add(volume);
     }
     
     public void playPrev()
     {
     	Global.player.playPrev();
     }
     
     public void play()
     {
     	new Thread()
     	{
     		public void run()
     		{
    			Global.player.playpause();
     		}
     	}.start();        
     }
     
     public void playNext()
     {
     	Global.player.playNext();        
     }
     
     public void changeVolume(int xVolume)
     {
     	Global.player.changeVolume(xVolume);
     }
 }

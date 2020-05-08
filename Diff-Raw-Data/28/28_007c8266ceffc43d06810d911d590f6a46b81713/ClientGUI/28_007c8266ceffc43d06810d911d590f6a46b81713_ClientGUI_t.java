 package ucbang.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferStrategy;
 
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
import javax.swing.JOptionPane;
 
 import ucbang.core.Card;
 import ucbang.core.Player;
 
 public class ClientGUI extends JFrame{
 	Keyboard kb;
 	BufferStrategy strategy;
     int player;
     
     public ClientGUI() {
     }
     public ClientGUI(int p) {
         player = p;
         //set window sizes
         setPreferredSize(new Dimension(800,600));
         setSize(new Dimension(800,600));
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         kb=new Keyboard();
         addKeyListener(kb);
      	this.setIgnoreRepaint(true);
      	this.setVisible(true);
     	this.createBufferStrategy(2);
     	strategy=this.getBufferStrategy();
     }
        
     public void paint(Graphics g){
 		Graphics2D graphics;
 		try {
 			graphics = (Graphics2D) strategy.getDrawGraphics();
 		} catch (Exception e) {
 			return;
 		}
 		//fill background w/ dark green
 		graphics.setColor(Color.GREEN);
 		graphics.fillRect(0, 0, 800, 400);
 		graphics.setColor(new Color(100,0,0));
 		graphics.fillRect(0, 400, 800, 600);
 
 		graphics.dispose();
 		//paint backbuffer to window
 		strategy.show();
 	}
     public void update(){
 		paint(this.getGraphics());
 
     }
     /**
      * Asks the player to choose a card
      * @param al
      * @return
      */
     public int promptChooseCard(ArrayList<Card> al){
    	Object[] options = al.toArray();
		int n = JOptionPane.showOptionDialog(this,
		"Who do you want to be?",
		"Choose your character!",
		JOptionPane.YES_NO_CANCEL_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null,
		options,
		options[0]);

        return n;
     }
 }

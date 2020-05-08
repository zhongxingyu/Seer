 package game.gui;
 
 import game.ui.GameEvent;
 
 import javax.swing.JPanel;
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.LinkedList;
 
 public class MazePanel extends JPanel implements KeyListener {
 	
 	private LinkedList<Character> keys = new LinkedList<Character>();
 
 	public MazePanel() {
 		addKeyListener(this);
 		setBackground(Color.WHITE);
 		setFocusable( true );
 	}
 	
 	public char getNextKey() {
 		if (keys.isEmpty() ) 
 			return '\n';
 		else 
 			return keys.removeFirst();
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
         if (e.getKeyChar() == 'w') {
         	keys.add('w');
         }
         if (e.getKeyChar() == 'a') {
         	keys.add('a');
         }
         if (e.getKeyChar() == 's') {
         	keys.add('s');
         }
         if (e.getKeyChar() == 'd') {
         	keys.add('d');
         }
        if (e.getKeyChar() == 'e') {
        	keys.add('e');
        }
        if (e.getKeyChar() == 'z') {
        	keys.add('z');
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        	keys.add(' ');
         }
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 	}
 
 }

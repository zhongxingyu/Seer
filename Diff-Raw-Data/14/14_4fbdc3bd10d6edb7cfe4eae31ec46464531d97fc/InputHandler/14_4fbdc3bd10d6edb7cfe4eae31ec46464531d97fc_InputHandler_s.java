 package fi.dy.esav.GameEngine;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 public class InputHandler implements MouseListener, MouseMotionListener, KeyListener {
 
 	private InputState inputstate;
 	
 	/**
 	 * Disabled default constructor
 	 */
 	@SuppressWarnings("unused")
 	private InputHandler() {}
 	
 	/**
 	 * Constructor
 	 * @param state The InputState to hold the inputs states
 	 */
 	public InputHandler(InputState state) {
 		this.inputstate = state;
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
 		inputstate.keysDown.add(e.getKeyCode());
		System.out.println(e.getKeyCode());
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
		inputstate.keysDown.remove(e.getKeyCode());
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		inputstate.mousePos = e.getPoint();
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		inputstate.mouseButtons.add(e.getButton());
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
		inputstate.mouseButtons.remove(e.getButton());
		
 	}
 	
 }

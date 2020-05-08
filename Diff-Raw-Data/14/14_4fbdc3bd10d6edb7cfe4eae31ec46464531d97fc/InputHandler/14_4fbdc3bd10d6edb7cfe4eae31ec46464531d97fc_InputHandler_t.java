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
		if(inputstate.keysDown.contains(e.getKeyCode())) return;
 		inputstate.keysDown.add(e.getKeyCode());
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
		if(!inputstate.keysDown.contains(e.getKeyCode())) return;
		inputstate.keysDown.remove((Object)e.getKeyCode());
 		
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
		if(inputstate.mouseButtons.contains(e.getButton())) return;
 		inputstate.mouseButtons.add(e.getButton());
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
		if(!inputstate.mouseButtons.contains((Object)e.getButton())) return;
		inputstate.mouseButtons.remove((Object)e.getButton());		
 	}
 	
 }

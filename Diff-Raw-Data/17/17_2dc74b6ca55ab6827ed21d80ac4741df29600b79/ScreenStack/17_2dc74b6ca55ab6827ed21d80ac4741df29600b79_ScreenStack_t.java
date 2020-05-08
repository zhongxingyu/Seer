 package edu.rit.se.sse.rapdevx.gui;
 
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 public class ScreenStack extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
 	
 	private static final long serialVersionUID = 1L;
 	private ArrayList<Screen> screenList;
 	
 	public ScreenStack() {
 		screenList = new ArrayList<Screen>();
 	}
 	
 	public void addScreen(Screen screen) {
 		screenList.add(screen);
 	}
 	
 	public void removeScreen(Screen screen) {
 		screenList.remove(screen);
 	}
 	
 	public void update() {
 		boolean otherScreenHasFocus = false;
 		boolean coveredByOtherScreen = false;
 		
		for (int i = screenList.size() - 1; i > 0; i--) {
			Screen screen = screenList.get(i);
			
			screen.update(otherScreenHasFocus, !coveredByOtherScreen);
 			
 			if ((screen.getState() == Screen.State.ACTIVE) ||
 					(screen.getState() == Screen.State.TRANSITION_ON))
 			{
 				if (!otherScreenHasFocus) {
 					otherScreenHasFocus = true;
 					
 					if (screen.isPopup()) {
 						coveredByOtherScreen = true;
 					}
 				}
 			}
 		}
 	}
 	
 	public void draw(Graphics2D gPen) {
 		for (Screen screen : screenList) {
 			screen.draw(gPen);
 		}
 	}
 	
 	/**** User Input ****/
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
		for (Screen screen : screenList) {
			screen.keyPressed(e);
		}
 		
 	}
 	
 	@Override
 	public void keyReleased(KeyEvent e) {
		for (Screen screen : screenList) {
			screen.keyReleased(e);
		}
 	}
 	
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
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
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		//TODO should we only send this to the screen with focus?
 		
 		//tell all the screen about this mouse event
 		for(Screen screen: screenList) {
 			if(e.isConsumed()) {
 				System.out.println("Ate it");
 				return;	//we're done if someone handled the event
 			}
 			
 			screen.mouseMoved(e);
 		}
 	}
 	
 }

 package renderer;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JTextField;
 
 import engine.Base;
 
 /**
  * This class display a base.
  * @author Yuki
  *
  */
 @SuppressWarnings("serial")
 public class BaseSprite extends Sprite {
 	/**
 	 * nbAgents is the JTextField which is used to display the nbAgents of the correpsonding base.
 	 */
 	private JTextField nbAgents;
 
 	public BaseSprite(Base newBase) {
 		super();
 		
 		this.nbAgents = new JTextField(String.valueOf(newBase.getNbAgents()));
		this.nbAgents.setPreferredSize(new Dimension(40, 20));
 		this.nbAgents.setDisabledTextColor(new Color(255, 255, 255));
 		this.nbAgents.setEnabled(false);
 		this.nbAgents.setBorder(null);
 		this.nbAgents.setOpaque(false);
 		this.add(this.nbAgents, BorderLayout.CENTER);
 		
 		this.addMouseListener(new MouseListener() {
             @Override
             public void mouseReleased(MouseEvent e) {
                 System.out.println(":MOUSE_RELEASED_EVENT:");
             }
             @Override
             public void mousePressed(MouseEvent e) {
                 System.out.println(":MOUSE_PRESSED_EVENT:");
             }
             @Override
             public void mouseExited(MouseEvent e) {
                 //System.out.println(":MOUSE_EXITED_EVENT:");
             }
             @Override
             public void mouseEntered(MouseEvent e) {
                 //System.out.println(":MOUSE_ENTER_EVENT:");
             }
             @Override
             public void mouseClicked(MouseEvent e) {
                 //System.out.println(":MOUSE_CLICK_EVENT:");
             }
         });
 	}
 	
 	public JTextField getNbAgents() {
 		return this.nbAgents;
 	}
 }

 
 import javax.swing.JButton;
 import javax.swing.JInternalFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.*;
 import java.beans.PropertyVetoException;
 
 
 /**
  * HCI Project Phase 1
  * 
  * @author Sam Shelley, Khurram Aslam
  */
 
 public class LabelFrame extends JInternalFrame implements ActionListener, KeyListener, InternalFrameListener{
 	private static final long serialVersionUID = 1L;
 	private JTextField text;
 	ImageDesktop parent;
     public LabelFrame(ImageDesktop parent,int x, int y) {
         super("Choose a Label", false, true, false, false); 
         this.parent = parent;
         setSize(300,120);
         text = new JTextField();
         text.setPreferredSize(new Dimension(250,30));
         text.setMaximumSize(new Dimension(250,30));
         text.setMinimumSize(new Dimension(250,30));
         text.addKeyListener(this);
         addInternalFrameListener(this);
         
         JButton discard = new JButton();
         discard.setText("Discard");
         discard.setActionCommand("discard");
         discard.addActionListener(this);
         
       
         JButton submit = new JButton();
         submit.setText("Submit");
         submit.addActionListener(this);
         submit.setActionCommand("submit");
         
         JPanel main = new JPanel();
         setContentPane(main);
         main.add(text);
         main.add(discard);
         main.add(submit);
         main.setLayout(new FlowLayout(FlowLayout.CENTER));
         setLocation(x, y);
     }
 
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent key) {
 		if(key.getKeyCode() == KeyEvent.VK_ENTER) {
 			addLabel(text.getText());
 			setVisible(false);
 			dispose();
 		} else if(key.getKeyCode() == KeyEvent.VK_ESCAPE) {
 			try {
 				setClosed(true);
 			} catch (PropertyVetoException e) {
 				e.printStackTrace();
 			}
 			dispose();
 		} else if(key.getKeyCode() == KeyEvent.VK_Z && key.isControlDown()) {
 			setVisible(false);
 			dispose();
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent key) {
 		
 	}
 
 	@Override
 	public void internalFrameActivated(InternalFrameEvent arg0) {
 
 		
 	}
 
 	@Override
 	public void internalFrameClosed(InternalFrameEvent arg0) {
 		
 	}
 
 	@Override
 	public void internalFrameClosing(InternalFrameEvent arg0) {
 		//removes current polygon
		parent.deleteCurrentPolygon();
 		
 	}
 
 	@Override
 	public void internalFrameDeactivated(InternalFrameEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public void internalFrameDeiconified(InternalFrameEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public void internalFrameIconified(InternalFrameEvent arg0) {
 		
 		
 	}
 
 	@Override
 	public void internalFrameOpened(InternalFrameEvent arg0) {
 		
 		
 	}
 	private void addLabel(String label) {
 		parent.addLabel(label);
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if("submit".equals(e.getActionCommand())) {
 			addLabel(text.getText());
 			setVisible(false);
 			dispose();
 		} else if("discard".equals(e.getActionCommand())) {
 			try {
 				setClosed(true);
 			} catch (PropertyVetoException e1) {
 				System.out.println("Fatal error");
 			}
 			dispose();
 		}
 		
 	}
 }

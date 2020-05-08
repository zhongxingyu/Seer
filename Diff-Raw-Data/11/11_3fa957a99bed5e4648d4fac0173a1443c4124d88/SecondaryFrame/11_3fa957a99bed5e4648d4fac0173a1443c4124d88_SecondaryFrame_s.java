 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vue.terminal.secondaryFramed;
 
 import controller.terminal.controller.TerminalController;
 import java.awt.GraphicsConfiguration;
 import java.awt.HeadlessException;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import javax.swing.JFrame;
 
 /**
  *
  * @author Valentin SEITZ
  */
 public class SecondaryFrame extends JFrame {
 
 	public SecondaryFrame() throws HeadlessException {
 		this.initialize();
 	}
 
 	public SecondaryFrame(GraphicsConfiguration gc) {
 		super(gc);
 		this.initialize();
 	}
 
 	public SecondaryFrame(String string) throws HeadlessException {
 		super(string);
 		this.initialize();
 	}
 
 	public SecondaryFrame(String string, GraphicsConfiguration gc) {
 		super(string, gc);
 		this.initialize();
 	}
 
 	private void initialize() {
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		this.addWindowListener(new WindowListener() {
 			@Override
 			public void windowOpened(WindowEvent we) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 			}
 
 			@Override
 			public void windowClosing(WindowEvent we) {
 				TerminalController.doCancel();
 			}
 
 			@Override
 			public void windowClosed(WindowEvent we) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 			}
 
 			@Override
 			public void windowIconified(WindowEvent we) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 			}
 
 			@Override
 			public void windowDeiconified(WindowEvent we) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 			}
 
 			@Override
 			public void windowActivated(WindowEvent we) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 			}
 
 			@Override
 			public void windowDeactivated(WindowEvent we) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 			}
 		});
 	}
 }

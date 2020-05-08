 package org.wwscc.actions;
 
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 
 public class QuitAction extends AbstractAction
 {
 	public QuitAction()
 	{
 		super("Quit");
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		System.exit(0);
 	}
 }

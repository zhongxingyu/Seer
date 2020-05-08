 package org.computer.knauss.reqtDiscussion.ui.ctrl;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.JOptionPane;
 
 import org.computer.knauss.reqtDiscussion.io.DAORegistry;
 
 public class ChooseDAOManager extends AbstractCommand {
 
 	private static final long serialVersionUID = 1L;
 
 	public ChooseDAOManager() {
 		super("Select data source");
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		String[] sources = DAORegistry.getInstance().availableDAOManagers();
 		String choice = (String) JOptionPane.showInputDialog(null,
 				"Available datasources:", "Choose data source",
				JOptionPane.QUESTION_MESSAGE, null, sources, sources[0]);
 		DAORegistry.getInstance().selectDAOManager(choice);
 	}
 
 }

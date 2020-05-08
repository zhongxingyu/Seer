 package org.computer.knauss.reqtDiscussion.ui.ctrl;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.JOptionPane;
 
 import org.computer.knauss.reqtDiscussion.io.DAOException;
 import org.computer.knauss.reqtDiscussion.io.IDiscussionDAO;
 import org.computer.knauss.reqtDiscussion.model.Discussion;
 import org.computer.knauss.reqtDiscussion.ui.uiModel.DiscussionTableModel;
 
 public class LoadDiscussionByID extends AbstractCommand {
 
 	private static final long serialVersionUID = 1L;
 
 	public LoadDiscussionByID() {
 		super("Load discussion by ID...");
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
		DiscussionTableModel wtm = getWorkitemTableModel();
 
 		String input = JOptionPane
 				.showInputDialog(
 						null,
 						"Please specify the IDs of discussions to load.\n Multiple discussions should be seperated by ',' or '-'.",
 						"Load workitem by ID", JOptionPane.QUESTION_MESSAGE);
 
 		String[] inputs = null;
 		if (input.indexOf('-') > 0)
 			inputs = input.split("-");
 		else
 			inputs = input.split(",");
 
 		// int[] classifiedWorkitems = new int[inputs.length];
 		Discussion[] data = new Discussion[inputs.length];
 
 		try {
 			IDiscussionDAO dao = getWorkitemDAO();
 			for (int i = 0; i < inputs.length; i++) {
 				data[i] = dao.getDiscussion(Integer.valueOf(inputs[i].trim()));
 			}
 		} catch (NumberFormatException e) {
 			JOptionPane.showMessageDialog(null,
 					"Invalid ID in [" + input + "].", "Invalid ID",
 					JOptionPane.ERROR_MESSAGE);
 		} catch (DAOException e) {
 			JOptionPane.showMessageDialog(null,
 					"Could not load Discussions.", "Data Access Exception",
 					JOptionPane.ERROR_MESSAGE);
 		}
 		wtm.setDiscussions(data);
 	}
 
 }

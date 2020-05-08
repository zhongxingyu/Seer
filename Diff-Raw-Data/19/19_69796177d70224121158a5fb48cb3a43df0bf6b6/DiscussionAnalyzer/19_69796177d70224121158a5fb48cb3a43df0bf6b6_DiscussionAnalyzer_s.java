 package org.computer.knauss.reqtDiscussion;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 
 import org.computer.knauss.reqtDiscussion.io.DAORegistry;
 import org.computer.knauss.reqtDiscussion.io.jazz.JazzDAOManager;
 import org.computer.knauss.reqtDiscussion.io.sql.SQLDAOManager;
 import org.computer.knauss.reqtDiscussion.ui.DiscussionAnalyzerFrame;
 import org.computer.knauss.reqtDiscussion.ui.ctrl.AbstractCommand;
 import org.computer.knauss.reqtDiscussion.ui.ctrl.ChooseDAOManager;
 import org.computer.knauss.reqtDiscussion.ui.ctrl.InsertOrUpdateDiscussionEventClassification;
 import org.computer.knauss.reqtDiscussion.ui.ctrl.LoadDiscussions;
 import org.computer.knauss.reqtDiscussion.ui.ctrl.LoadDiscussionByID;
 import org.computer.knauss.reqtDiscussion.ui.ctrl.SetReferenceClassifierName;
 import org.computer.knauss.reqtDiscussion.ui.ctrl.ShowStatistics;
 import org.computer.knauss.reqtDiscussion.ui.uiModel.DiscussionTableModel;
 
 public class DiscussionAnalyzer {
 
 	private static DAORegistry daoRegistry;
 	private static DiscussionTableModel tableModel;
 
 	public static void main(String[] args) {
 		DiscussionAnalyzerFrame daFrame = new DiscussionAnalyzerFrame();
 
 		daFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		daFrame.pack();
 
 		tableModel = new DiscussionTableModel();
 		daFrame.setTableModel(tableModel);
 		try {
 			daoRegistry = DAORegistry.getInstance();
 			// add the data sources
 			daoRegistry.register("PSQL (default)", new SQLDAOManager(
 					"local-postgres-properties.txt",
 					"psql-default-schema-queries.txt"));
 			daoRegistry.register("jazz.net", new JazzDAOManager());
 
 			// add the commands
 			daFrame.addAction(DiscussionAnalyzerFrame.DATA_MENU,
 					configureCommand(new LoadDiscussions()));
 
 			daFrame.addAction(DiscussionAnalyzerFrame.DATA_MENU,
 					configureCommand(new LoadDiscussionByID()));
 
 			daFrame.addAction(DiscussionAnalyzerFrame.EDIT_MENU,
 					configureCommand(new SetReferenceClassifierName()));
 			daFrame.addAction(DiscussionAnalyzerFrame.EDIT_MENU,
 					configureCommand(new ChooseDAOManager()));
 
 			daFrame.getEditClassificationFrame()
 					.setInsertOrUpdateCommand(
 							configureCommand(new InsertOrUpdateDiscussionEventClassification()));
 
 			daFrame.addAction(DiscussionAnalyzerFrame.STATISTICS_MENU,
 					configureCommand(new ShowStatistics()));
 
 		} catch (NullPointerException e) {
 			System.err
 					.println("Failed initialization. Perhaps the database could not be reached? Try to (re-)connect to: ssh -L 5432:localhost:5432 ballroom.segal.uvic.ca");
 			e.printStackTrace();
 		}
 		// Clean up after work:
 		daFrame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent we) {
 				try {
 					daoRegistry.closeAllConnections();
 				} catch (Throwable t) {
 					t.printStackTrace();
 				}
 				System.exit(0);
 			}
 		});
 		daFrame.setVisible(true);
 	}
 
 	private static AbstractCommand configureCommand(AbstractCommand cmd) {
 		cmd.setDAORegistry(daoRegistry);
 		cmd.setWorkitemTableModel(tableModel);
 		return cmd;
 	}
 }

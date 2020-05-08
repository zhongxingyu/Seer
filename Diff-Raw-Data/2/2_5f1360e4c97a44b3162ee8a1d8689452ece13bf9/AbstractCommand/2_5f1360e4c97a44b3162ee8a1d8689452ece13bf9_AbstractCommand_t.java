 package org.computer.knauss.reqtDiscussion.ui.ctrl;
 
 import javax.swing.AbstractAction;
 
 import org.computer.knauss.reqtDiscussion.io.DAOException;
 import org.computer.knauss.reqtDiscussion.io.DAORegistry;
 import org.computer.knauss.reqtDiscussion.io.IDiscussionDAO;
 import org.computer.knauss.reqtDiscussion.io.IDiscussionEventClassificationDAO;
 import org.computer.knauss.reqtDiscussion.io.IDiscussionEventDAO;
 import org.computer.knauss.reqtDiscussion.ui.uiModel.DiscussionTableModel;
 
 public abstract class AbstractCommand extends AbstractAction {
 
 	private static final long serialVersionUID = 1L;
 	private DiscussionTableModel workitemTableModel;
 	private DAORegistry daoRegistry;
 
 	public AbstractCommand(String name) {
 		super(name);
 	}
 
 	public IDiscussionEventDAO getWorkitemCommentDAO() throws DAOException {
 		return this.daoRegistry.getSelectedDAOManager().getDiscussionEventDAO();
 	}
 
 	public IDiscussionDAO getWorkitemDAO() throws DAOException {
 		return this.daoRegistry.getSelectedDAOManager().getDiscussionDAO();
 	}
 
 	public void setWorkitemTableModel(DiscussionTableModel wtm) {
 		this.workitemTableModel = wtm;
 	}
 
	public DiscussionTableModel getDiscussionTableModel() {
 		return this.workitemTableModel;
 	}
 
 	public IDiscussionEventClassificationDAO getWorkitemCommentClassificationDAO() throws DAOException {
 		return this.daoRegistry.getSelectedDAOManager().getDiscussionEventClassificationDAO();
 	}
 	
 	public void setDAORegistry(DAORegistry dr){
 		this.daoRegistry = dr;
 	}
 }

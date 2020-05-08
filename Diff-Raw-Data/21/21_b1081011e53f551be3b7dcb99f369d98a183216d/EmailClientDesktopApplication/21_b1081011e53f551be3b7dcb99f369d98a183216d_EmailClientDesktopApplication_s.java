 /**
  * 
  */
 package org.promasi.desktop_swing.application.email;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.io.IOException;
 import java.util.List;
 import java.util.Vector;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableCellRenderer;
 
 import org.jdesktop.swingx.JXPanel;
 import org.joda.time.DateTime;
 import org.promasi.desktop_swing.IDesktop;
 import org.promasi.desktop_swing.application.ADesktopApplication;
 import org.promasi.desktop_swing.application.TaskBarIcon;
 import org.promasi.game.IGame;
 import org.promasi.game.company.DepartmentMemento;
 import org.promasi.game.company.EmployeeMemento;
 import org.promasi.game.company.ICompanyListener;
 import org.promasi.game.company.CompanyMemento;
 import org.promasi.game.company.IDepartmentListener;
 import org.promasi.game.project.ProjectMemento;
 
 import org.promasi.utilities.file.RootDirectory;
 import org.promasi.utils_swing.Colors;
 import org.promasi.utils_swing.GuiException;
 import org.promasi.utils_swing.Painters;
 
 /**
  * @author alekstheod
  *
  */
 public class EmailClientDesktopApplication extends ADesktopApplication implements ICompanyListener, IDepartmentListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * 
 	 */
 	public static final String CONST_APPNAME = "Email";
 	
 	/**
 	 * 
 	 */
 	public static final String CONST_APP_ICON = "gmail.png";
 	
 	/**
 	 * 
 	 */
 	private TaskBarIcon _quickStartButton;
 	
 	/**
 	 * A list with received messages \see="Message"
 	 */
 	private JTable _messageTable;
 	
 	/**
 	 * 
 	 */
 	private MessageTableModel _msgTableModel;
 	
 	/**|
 	 * 
 	 */
 	private EmailJPanel _emailPanel;
 	
 	/**
 	 * Lock object.
 	 */
 	private Lock _lockObject;
 	
 	/**
 	 * 
 	 */
 	private IGame _game;
 	
 	/**
 	 * 
 	 * @throws GuiException
 	 * @throws IOException 
 	 */
 	public EmailClientDesktopApplication( IGame game, IDesktop desktop ) throws GuiException, IOException {
 		super(CONST_APPNAME, RootDirectory.getInstance().getImagesDirectory() + CONST_APP_ICON);
 		
 		if( game == null ){
 			throw new GuiException("Wrong argument game == null");
 		}
 		
 		_lockObject = new ReentrantLock();
 		_game = game;
 		_quickStartButton = new TaskBarIcon(this, desktop);
 		desktop.addQuickStartButton(_quickStartButton);
 		setLayout(new BorderLayout());
 		
 		JXPanel bgPanel = new JXPanel();
 		bgPanel.setBackgroundPainter(Painters.Background);
 		bgPanel.setLayout(new BorderLayout());
 		
 		_messageTable = new JTable();
 		_messageTable.setShowHorizontalLines(false);
 		_messageTable.setShowVerticalLines(true);
 		_messageTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
 		JLabel label = (JLabel)_messageTable.getTableHeader().getDefaultRenderer();
 		label.setHorizontalAlignment(JLabel.LEFT);
 		
 		JSplitPane splitPane = new JSplitPane();
 		splitPane.setOneTouchExpandable(true);
 		splitPane.setDividerLocation(300);
 		splitPane.setOpaque(false);
 		splitPane.setBackground(Colors.White.alpha(0f));
 		
 		Dimension minSize = new Dimension(100, 50);
 		splitPane.setMinimumSize(minSize);
 		splitPane.setMinimumSize(minSize);
 		
 		JXPanel messagesPanel = new JXPanel();
 		messagesPanel.setOpaque(false);
 		messagesPanel.setBorder(new EmptyBorder(10,10,10,10));
 		_msgTableModel =  new MessageTableModel( new Vector<Message>( ) );
 		_messageTable.setModel( _msgTableModel );
 		messagesPanel.setLayout(new BorderLayout());
 		JScrollPane scrollpane = new JScrollPane(_messageTable);
 		messagesPanel.add(scrollpane, BorderLayout.CENTER);
 		
 		splitPane.setLeftComponent(messagesPanel);
 		
 		_emailPanel = new EmailJPanel();
 		splitPane.setRightComponent(_emailPanel);
 		
 		bgPanel.add(splitPane, BorderLayout.CENTER);
 		add(bgPanel, BorderLayout.CENTER);
 		
 		_messageTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			
 			@Override
 			public void valueChanged(ListSelectionEvent arg0) {
 				try {
 					Message msg =_msgTableModel.getMessage( _messageTable.getSelectedRow() );
 					_emailPanel.showMessage(msg);
 				} catch (GuiException e) {
 					// TODO Log error
 				}
 			}
 		});
 		
 		_messageTable.setDefaultRenderer(Object.class, new MessageTableCellRenderer());
 		_game.addCompanyListener(this);
 		_game.addDepartmentListener(this);
 	}
 
 	@Override
 	public void projectAssigned(final String owner, CompanyMemento company,
 			final ProjectMemento project, final DateTime dateTime) {
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 					_lockObject.lock();
 					Message msg = new Message("Customer", "Project assigned", dateTime, project.getDescription());
 					List< Message > messages = _msgTableModel.getMessages();
 					messages.add(msg);
 					_msgTableModel = new MessageTableModel(messages);
 					_messageTable.setModel(_msgTableModel);
 					_quickStartButton.showPopupNotifier("You have " + getUnreadMessages() + " unread messages");
 				} catch (GuiException e) {
 					e.printStackTrace();
 				}finally{
 					_lockObject.unlock();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void projectFinished(String owner, CompanyMemento company,
 			ProjectMemento project, DateTime dateTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void companyIsInsolvent(String owner, CompanyMemento company,
 			ProjectMemento assignedProject, DateTime dateTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onExecuteWorkingStep(String owner, CompanyMemento company,
 			ProjectMemento assignedProject, DateTime dateTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void companyAssigned(String owner, CompanyMemento company) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void employeeDischarged(String director, DepartmentMemento department, final EmployeeMemento employee, final DateTime dateTime) {
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 					_lockObject.lock();
					Message msg = new Message("IT Department", "Employee discharged", dateTime, employee.getCurriculumVitae());
					List< Message > messages = _msgTableModel.getMessages();
					messages.add(0, msg);
					_msgTableModel = new MessageTableModel(messages);
					_messageTable.setModel(_msgTableModel);
					_quickStartButton.showPopupNotifier("You have " + getUnreadMessages() + " unread messages");
 				} catch (GuiException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}finally{
 					_lockObject.unlock();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void employeeHired(String director, DepartmentMemento department, final EmployeeMemento employee, final DateTime dateTime) {
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 					_lockObject.lock();
 					Message msg = new Message("IT Department", "Employee hired", dateTime, employee.getCurriculumVitae());
 					List< Message > messages = _msgTableModel.getMessages();
 					messages.add(0, msg);
 					_msgTableModel = new MessageTableModel(messages);
 					_messageTable.setModel(_msgTableModel);
 					_quickStartButton.showPopupNotifier( "You have " + getUnreadMessages() + " unread messages");
 				} catch (GuiException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}finally{
 					_lockObject.unlock();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void tasksAssigned(String director, DepartmentMemento department, final DateTime dateTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void tasksAssignFailed(String director, DepartmentMemento department, final DateTime dateTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void departmentAssigned(String director, DepartmentMemento department, final DateTime dateTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	private int getUnreadMessages(){
 		int result = 0;
 		List< Message > messages = _msgTableModel.getMessages();
 		for( Message msg : messages){
 			if( !msg.itWasOpened()){
 				result++;
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @author alekstheod
 	 *
 	 */
 	class MessageTableCellRenderer extends DefaultTableCellRenderer {  
 		 /**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 			
 		/**
 		 * 
 		 */
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {  
 			JLabel parent = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 
 			
 			try {
 				 
 				Message msg = _msgTableModel.getMessage( row );
 				if(!msg.itWasOpened()){ 
 					parent.setFont(parent.getFont().deriveFont(Font.BOLD)); 
 				}else{
 					parent.setFont(parent.getFont().deriveFont(Font.PLAIN)); 
 				}
 			     
 			} catch (Exception e) {
 				//TODO log
 			}
 			
 			return parent;  
 		}      
 	}  
 }

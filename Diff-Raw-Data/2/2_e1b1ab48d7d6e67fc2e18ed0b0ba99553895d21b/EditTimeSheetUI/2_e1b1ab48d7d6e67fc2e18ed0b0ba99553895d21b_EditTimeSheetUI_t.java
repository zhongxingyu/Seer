 package views.timesheet;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import controllers.ClientCtrl;
 import controllers.TimeSheetCtrl;
 import models.Client;
 import models.TimeSheet;
 import controllers.UserCtrl;
 import models.User;
 import controllers.UserPermissionCtrl;
 import models.UserPermission;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import javax.swing.border.LineBorder;
 import views.SystemUI;
 import utils.Helper;
 import utils.Logging;
 import utils.UserSession;
 
 public class EditTimeSheetUI
 {
     private static JFrame _frame;
     private static EditTimeSheetUI _instance;
 	private JPanel _contentPane;
 	private TimeSheet _timeSheet;
 		
 	private JComboBox<String> drpClients;
 	private DefaultComboBoxModel<String> _model;
 	
 	private JPanel pnlTimeSheet;
 	private JPanel pnlPermission;
 	private JTextPane txtNotes;
     private JTextField txtCaseId;
 	private JList<String> lstGroup;
 	private JList<String> lstUser;
     
 	// Controllers
 	private ClientCtrl _clientCtrl;
 	private UserCtrl _userCtrl;
 	private UserPermissionCtrl _userPermissionCtrl;
 	private TimeSheetCtrl _tsCtrl;
 	
     public static JFrame createWindow(TimeSheet ts)
     {
         if(_instance == null)
             _instance = new EditTimeSheetUI(ts);
 
         return _frame;
     }
 	/**
 	 * Create the frame.
 	 */
 	private EditTimeSheetUI(TimeSheet ts) 
 	{
         _timeSheet = ts;
 		createElements();
 	}
 	
 	public void createElements()
 	{
         _clientCtrl = new ClientCtrl();
         _userCtrl = new UserCtrl();
         _userPermissionCtrl = new UserPermissionCtrl();
         _tsCtrl = new TimeSheetCtrl();
         
 		_frame = new JFrame();
 		_frame.setIconImage(Toolkit.getDefaultToolkit().getImage(SystemUI.class.getResource("/new_timesheet.png")));
 		_frame.setTitle("Ny Registrering");
 		_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         _frame.addWindowListener(new WindowAdapter()
         {
             public void windowClosing(WindowEvent e)
             {
                 _instance = null;
                 _frame.dispose();
             }
         });
		_frame.setSize(new Dimension(415, 397));
 		Helper.centerOnScreen(_frame);
         _frame.setVisible(true);
         _frame.setResizable(false);
 		_contentPane = new JPanel();
 		_contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		
 		_frame.setContentPane(_contentPane);
 		_contentPane.setLayout(null);
 
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBounds(10, 58, 392, 262);
 		_contentPane.add(tabbedPane);
 		
 		JButton btnCancel = new JButton("Annuller");
 		btnCancel.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 _instance = null;
                 _frame.dispose();
             }
         });
 		btnCancel.setBounds(302, 331, 100, 23);
 		_contentPane.add(btnCancel);
 		
 		JButton btnNext = new JButton("Opdater");
 		btnNext.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 updateTimeSheet();
             }
         });
 		btnNext.setBounds(195, 331, 100, 23);
 		_contentPane.add(btnNext);
 		
 		JSeparator separator = new JSeparator();
 		separator.setBounds(10, 45, 392, 2);
 		_contentPane.add(separator);
 		
 		JLabel lblClient = new JLabel("Klient");
 		lblClient.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblClient.setBounds(15, 11, 53, 23);
 		_contentPane.add(lblClient);
 		
 		drpClients = new JComboBox<String>();
 		drpClients.setBounds(55, 11, 347, 22);
 		drpClients.setEnabled(false);
 		_contentPane.add(drpClients);
 		_model = new DefaultComboBoxModel<String>(addClients());
         drpClients.setModel(_model);
 
 		// pane1 start
 		pnlTimeSheet = new JPanel();
 		pnlTimeSheet.setLayout(null);
 		
 		JLabel lblID = new JLabel("ID");
 		lblID.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblID.setBounds(5, 5, 60, 23);
 		pnlTimeSheet.add(lblID);
 		
 		JLabel lblNote = new JLabel("Note");
 		lblNote.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblNote.setBounds(5, 30, 60, 23);
 		pnlTimeSheet.add(lblNote);
 		
 		// for display ClientId
 		txtCaseId = new JTextField();
 		txtCaseId.setBounds(45, 5, 332, 20);
 		pnlTimeSheet.add(txtCaseId);
 		
 		// for notes
 		txtNotes = new JTextPane();
 		txtNotes.setBounds(45, 31, 332, 195);
 		pnlTimeSheet.add(txtNotes);
         txtNotes.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		
 		tabbedPane.add("Time-Sag", pnlTimeSheet);
 		
 		pnlPermission = new JPanel();
 		pnlPermission.setLayout(null);
 				
 		tabbedPane.add("Rettigheder", pnlPermission);
 		
 		JPanel groupPanel = new JPanel();
 		groupPanel.setBounds(10, 11, 179, 212);
 		groupPanel.setBorder(BorderFactory.createTitledBorder("Gruppe"));
 		pnlPermission.add(groupPanel);
 		groupPanel.setLayout(null);
 		
 		lstGroup = new JList<String>();
 		lstGroup.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lstGroup.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		lstGroup.setBounds(13, 17, 153, 182);
         lstGroup.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         populateGroupList();
 		groupPanel.add(lstGroup);
 		
 		JPanel pnlUser = new JPanel();
 		pnlUser.setBorder(BorderFactory.createTitledBorder("Bruger"));
 		pnlUser.setBounds(197, 11, 179, 212);
 		pnlPermission.add(pnlUser);
 		pnlUser.setLayout(null);
 		
 		lstUser = new JList<String>();
 		lstUser.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lstUser.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		lstUser.setBounds(13, 17, 153, 182);
         lstUser.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         populateUserList();
 		JScrollPane userListScroll = new JScrollPane(lstUser);
 		groupPanel.add(userListScroll);
 		pnlUser.add(lstUser);
 		
 		addData();
 	}
 	
 	
 	public String[] addClients()
 	{
 		try
 		{
             ArrayList<Client> clients = _clientCtrl.getAllClients();
 			String[] clientNames = new String[clients.size()];
 			for (int index = 0; index < clients.size(); index++)
 				clientNames[index] = clients.get(index).getName() + " (" + clients.get(index).getPhoneNo() +")";
 
 			return clientNames;
 		}
 		catch (Exception e)
 		{
 			JOptionPane.showMessageDialog(null, Logging.handleException(e, 0), "Fejl", JOptionPane.WARNING_MESSAGE);
             e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	public void updateTimeSheet()
 	{
         try
         {	
         	//updating TimeSheet
         	_timeSheet.setCaseId(txtCaseId.getText());
             _timeSheet.setUser(UserSession.getLoggedInUser());
             
             long clientPhone = Long.parseLong(drpClients.getSelectedItem().toString().substring(drpClients.getSelectedItem().toString().indexOf("(") + 1,
                     drpClients.getSelectedItem().toString().indexOf(")")));
 
             _timeSheet.setClient(_clientCtrl.getClientByPhone(clientPhone));
             _timeSheet.setNote(txtNotes.getText());
             Calendar cal = Calendar.getInstance();
             _timeSheet.setEditedDate(cal.getTime());
           
             _tsCtrl.updateTimeSheet(_timeSheet);
             
             _tsCtrl.deleteAllPermissions(_timeSheet);
             for(String roleName : lstGroup.getSelectedValuesList())
             {
                 _tsCtrl.insertPermission(_timeSheet, _userPermissionCtrl.getPermissionByTitle(roleName));
             }
             for(String userName : lstUser.getSelectedValuesList())
             {
                 _tsCtrl.insertPermission(_timeSheet, _userCtrl.getUserByName(userName));
             }
             
             
     		JOptionPane.showMessageDialog(null, "Time-sagen er nu opdateret", "INFORMATION!", JOptionPane.INFORMATION_MESSAGE);
     		_instance = null;
     		_frame.dispose();
         }
         catch (Exception e)
         {
             JOptionPane.showMessageDialog(null, Logging.handleException(e, 99), "Fejl!", JOptionPane.ERROR_MESSAGE);
             e.printStackTrace();
         }
 	}
 	
 	public String[] populateGroupList()
 	{
         try
         {
             new PopulateGroupList().execute();
         }
         catch(Exception ex)
         {
         	JOptionPane.showMessageDialog(null, Logging.handleException(ex, 99), "Fejl!", JOptionPane.ERROR_MESSAGE);
         }
 
         return null;
 	}
 	
 	public String[] populateUserList()
 	{
         try
         {
             new PopulateUserList().execute();
         }
         catch(Exception ex)
         {
         	JOptionPane.showMessageDialog(null, Logging.handleException(ex, 99), "Fejl!", JOptionPane.ERROR_MESSAGE);
         }
 
         return null;
 	}
 	
 	public void addData()
 	{
     	try
     	{
             drpClients.setSelectedItem(_timeSheet.getClient().getName());
    			txtCaseId.setText(_timeSheet.getCaseId());
    			txtNotes.setText(_timeSheet.getNote());
     	}
     	catch (Exception ex)
         {
             JOptionPane.showMessageDialog(null, Logging.handleException(ex, 1), "Fejl", JOptionPane.ERROR_MESSAGE);
         }
 	}
 	
 	public void selectGroups()
 	{
         for(int i = 0; i < lstGroup.getModel().getSize(); i++)
         {
             try
             {
                 String roleName = lstGroup.getModel().getElementAt(i);
                 if(_userPermissionCtrl.isRoleAllowed(_timeSheet, roleName))
                 {
                     lstGroup.getSelectionModel().addSelectionInterval(i, i);
                 }
             }
             catch (Exception e)
             {
                 JOptionPane.showMessageDialog(null, Logging.handleException(e, 0), "Fejl!", JOptionPane.ERROR_MESSAGE);
             }
         }
 	}
 	
 	public void selectUsers()
 	{
         for(int i = 0; i < lstUser.getModel().getSize(); i++)
         {
             try
             {
                 String userName = lstUser.getModel().getElementAt(i);
                 if(_userCtrl.isUserAllowed(_timeSheet, userName))
                 {
                     lstUser.getSelectionModel().addSelectionInterval(i, i);
                 }
             }
             catch (Exception e)
             {
                 JOptionPane.showMessageDialog(null, Logging.handleException(e, 0), "Fejl!", JOptionPane.ERROR_MESSAGE);
             }
         }
 	}
 	
 	class PopulateUserList extends SwingWorker<Integer, Integer>
     {
     	@Override
         protected Integer doInBackground() throws Exception
         {
             ArrayList<User> userList;
             try
             {
                 userList = _userCtrl.getAllUsers();
                 DefaultListModel<String> userNames = new DefaultListModel<String>();
                 for(int i = 0; i < userList.size(); i++)
                     userNames.add(i, userList.get(i).getUserName());
 
                 lstUser.setModel(userNames);
 
                 return 1;
             }
             catch(Exception ex)
             {
                 JOptionPane.showMessageDialog(null, Logging.handleException(ex, 0), "Fejl!", JOptionPane.ERROR_MESSAGE);
             }
 
             return null;
         }
 
         @Override
         protected void done()
         {
             selectUsers();
         }
     }
 	
 	class PopulateGroupList extends SwingWorker<Integer, Integer>
     {
     	@Override
         protected Integer doInBackground() throws Exception
         {
             ArrayList<UserPermission> groupList;
             try
             {
                 groupList = _userPermissionCtrl.getAllRoles();
                 DefaultListModel<String> groupNames = new DefaultListModel<String>();
                 for(int i = 0; i < groupList.size(); i++)
                     groupNames.add(i, groupList.get(i).getUserRole());
 
                 lstGroup.setModel(groupNames);
 
                 return 1;
             }
             catch(Exception ex)
             {
                 JOptionPane.showMessageDialog(null, Logging.handleException(ex, 0), "Fejl!", JOptionPane.ERROR_MESSAGE);
             }
 
             return null;
         }
 
         @Override
         protected void done()
         {
             selectGroups();
         }
     }
 }

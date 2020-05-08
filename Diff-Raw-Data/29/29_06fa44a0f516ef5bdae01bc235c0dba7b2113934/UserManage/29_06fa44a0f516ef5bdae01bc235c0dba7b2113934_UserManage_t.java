 package jacs.gui;
 
 import java.awt.List;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import jacs.database.exceed.dao.DaoFactory;
 import jacs.database.exceed.dao.UserDAO;
 import jacs.database.exceed.model.User;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 import javax.swing.table.DefaultTableModel;
 /**
  * This class Gui_page_User for eXceedVoteServer
  * @author Nookskill
  *
  */
 public class UserManage extends JPanel{
 	
 	private static UserDAO user_dao = DaoFactory.getInstance().getUserDAO();
 	 JLabel name = new JLabel("Username ");
      JLabel pass = new JLabel("Password ");
      JTextField username = new JTextField(15);
      JPasswordField password = new JPasswordField(15);
      JButton add = new JButton("Add");
      JButton delete = new JButton("Delete");
      JLabel message = new JLabel();
      DefaultTableModel model = new DefaultTableModel();
      JTable table ;
      JScrollPane scrollPane;
 	public UserManage() {
 
 		table = new JTable(model);
 		table.setEnabled(false);
 		scrollPane = new JScrollPane(table);
         SpringLayout layout = new SpringLayout();
         model.addColumn("Username"); 
 		java.util.List<User> list = user_dao.findAllUser();
 		Iterator<User> it = list.iterator();
 		while(it.hasNext())	{
 			User user = it.next();
 			//table.setValueAt(user.getUsername(), i, 0);
 			model.addRow(new Object[]{user.getUsername()}); 
 			
 		}
         this.setLayout(layout);
         
         this.add(name);
         this.add(username);
         this.add(pass);
         this.add(password);
         this.add(add);
         this.add(delete);
         this.add(message);
         this.add(scrollPane);
         ActionListener listener = new ButtonListener();
         add.addActionListener(listener);
         delete.addActionListener(listener);
         
         layout.putConstraint(SpringLayout.WEST, name,
                 100,
                 SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.WEST, username,
                 5,
                 SpringLayout.EAST, name);
         layout.putConstraint(SpringLayout.NORTH, name,
                 30,
                 SpringLayout.NORTH, this);
         layout.putConstraint(SpringLayout.NORTH, username,
                 30,
                 SpringLayout.NORTH, this);
         /***********************/
         layout.putConstraint(SpringLayout.WEST, pass,
                 100,
                 SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.WEST, password,
                 5,
                 SpringLayout.EAST, name);
         layout.putConstraint(SpringLayout.NORTH, pass,
                 60	,
                 SpringLayout.NORTH, this);
         layout.putConstraint(SpringLayout.NORTH, password,
                 60,
                 SpringLayout.NORTH, this);
         /************************/
         layout.putConstraint(SpringLayout.WEST, add,
                 100,
                 SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.WEST, delete,
                 5,
                 SpringLayout.EAST, name);
         layout.putConstraint(SpringLayout.NORTH, add,
                 90	,
                 SpringLayout.NORTH, this);
         layout.putConstraint(SpringLayout.NORTH, delete,
                 90,
                 SpringLayout.NORTH, this);        
         /**********************/
         layout.putConstraint(SpringLayout.WEST, message,
                 100,
                 SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, message,
                 120	,
                 SpringLayout.NORTH, this);
         /**********************/
         layout.putConstraint(SpringLayout.WEST, scrollPane,
                 400,
                 SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, scrollPane,
                 30	,
                 SpringLayout.NORTH, this);
         
 	}
 
 	class ButtonListener implements ActionListener{
 		
 		
 		@Override
 		public void actionPerformed(ActionEvent event) {
 			if(event.getActionCommand().equals("Add")){
 				String userkey = username.getText().toString();
 				String passkey = password.getText().toString();
 				String mess = user_dao.regisUser(userkey, passkey);
				if(!userkey.equals(""))	{
					message.setText(mess);
					username.setText("");
					password.setText("");
					if(mess.contains("REGIS_SUCCESS"))
						model.addRow(new Object[]{userkey}); 
				}
 				
 			}
 			else if(event.getActionCommand().equals("Delete")){
 				String userkey = username.getText().toString();
 				String passkey = password.getText().toString();
 				String mess = user_dao.deleteUser(userkey, passkey);
 				message.setText(mess);
 				username.setText("");
 				password.setText("");
 				int length = model.getRowCount();
 				for(int i=0;i<length;i++){
 					if(model.getValueAt(i, 0).toString().equals(userkey)){
 						model.removeRow(i);
 						break;
 					}
 				}
 			}
 			
 		}
 		
 	}
 }

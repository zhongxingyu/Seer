 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.util.ArrayList;
 
 
 public class LoginFrame extends JFrame
 {
 	public LoginFrame(final Connection cn)
 	{
 		JPanel listpane = new JPanel();
 		listpane.setLayout(new BoxLayout(listpane, BoxLayout.PAGE_AXIS));
 		setTitle("Login to Dishaster!");
 		setVisible(true);
 		Dimension stddim = new Dimension(300, 25);
 		final ArrayList<String> accounttypelist = new ArrayList<String>();
 		
 		JLabel name = new JLabel("Select your username to login!");
 		final JComboBox loginbox = new JComboBox();
 		loginbox.setMaximumSize(stddim);
 		
 		JLabel or = new JLabel("Or, create new account here:");
		JLabel newname = new JLabel("   New username:");
 		final JTextField createnamebox = new JTextField();
 		createnamebox.setMaximumSize(stddim);
 		JLabel newtype = new JLabel("Account type:");
 		String types[] = {"User", "Admin"};
 		final JComboBox createtypebox = new JComboBox(types);
 		createtypebox.setMaximumSize(stddim);
 		JLabel newfavorite = new JLabel("Favorite Food Type:");
 		final JTextField createfavoritebox = new JTextField();
 		createfavoritebox.setMaximumSize(stddim);
 		final JButton createbutton = new JButton("Create New Account");
 		
 		String statement = "select id, name, type from user";
  	    System.out.println(statement);
  	    
  	    loginbox.addItem("Select username");
  	    try
  	    {
 	 	    PreparedStatement preparedStatement = cn.prepareStatement(statement);
 	 	    ResultSet resultSet = preparedStatement.executeQuery();
 	        ResultSetMetaData rsmd = resultSet.getMetaData();
 	        int columnNumber = rsmd.getColumnCount();
 	        
            while (resultSet.next()) //moves through rows, first is blank
            {
          	    String entry = "";
 	            for (int k = 1; k <= columnNumber; k++)
 	            {
 	               //moves through columns
 	         	   if (k != columnNumber)
 	         		   entry += resultSet.getString(k) + "   ";
 	         	   else
 	         		   entry += resultSet.getString(k);
 	            }
 	            loginbox.addItem(entry);
            }
  	    }
  	    catch(Exception e) {}
 	   
         loginbox.addActionListener(new ActionListener() 
         {
             @Override
             public void actionPerformed(ActionEvent event) 
             {
             	try 
             	{
             		if (loginbox.getSelectedIndex() != -1 && loginbox.getSelectedIndex() != 0)
             		{
             			String cut[] = loginbox.getSelectedItem().toString().split("   ");
             			System.out.println(cut.length);
             			LoginFrame.this.loggedin(cn, Integer.parseInt(cut[0]), cut[2]);
             		}
             	}
             	catch(Exception e) {}
             }
         });
         
         createbutton.addActionListener(new ActionListener() 
         {
             @Override
             public void actionPerformed(ActionEvent event) 
             {
             	try 
             	{
            		if (createnamebox.getText().length()  > 2 && createnamebox.getText().length() < 20 
            				&& createfavoritebox.getText().length() < 20)
             		{
 	            		String statement = "insert into user(name, likes, type) values(" +
	            				"?,?,'" + createtypebox.getSelectedItem() + "')";
 	            		
 	            		PreparedStatement preparedStatement = cn.prepareStatement(statement);
 	            		preparedStatement.setString(1, createnamebox.getText());
 	            		preparedStatement.setString(2, createfavoritebox.getText());
 	            		System.out.println(preparedStatement.toString());
 	            		preparedStatement.executeUpdate();
 	            		
 	            		//put new username in list
 	        	 	    preparedStatement = cn.prepareStatement("select id, name, type from user order by id DESC limit 1");
 	        	 	    ResultSet resultSet = preparedStatement.executeQuery();
 	        	        ResultSetMetaData rsmd = resultSet.getMetaData();
 	        	        int columnNumber = rsmd.getColumnCount();
 	        	        
 	                   while (resultSet.next()) //moves through rows, first is blank
 	                   {
 	                 	    String entry = "";
 	        	            for (int k = 1; k <= columnNumber; k++)
 	        	            {
 	        	               //moves through columns
 	        	         	   if (k != columnNumber)
 	        	         		   entry += resultSet.getString(k) + "   ";
 	        	         	   else
 	        	         		   entry += resultSet.getString(k);
 	        	            }
 	        	            loginbox.addItem(entry);
 	                   }
 	                   JOptionPane.showMessageDialog(createbutton,"New account created successfully!");
             		}
             		else
             		{
                 		JOptionPane.showMessageDialog(createbutton,"Creating new account failed.");
                 		System.out.println("Insertion failed");
             		}
             	}
             	catch(Exception e) 
             	{
             		JOptionPane.showMessageDialog(createbutton,"Error!");
             		System.out.println("Insertion failed");
             	}
             }
         });
 		
 		listpane.add(name);
 		listpane.add(loginbox);
 		listpane.add(new JSeparator(SwingConstants.VERTICAL));
 		listpane.add(or);
 		listpane.add(newname);
 		listpane.add(createnamebox);
 		listpane.add(newtype);
 		listpane.add(createtypebox);
 		listpane.add(newfavorite);
 		listpane.add(createfavoritebox);
 		listpane.add(createbutton);
 		
		setSize(300, 300);
 		add(listpane);
 		this.repaint();
 		this.revalidate();
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 	
 	public void loggedin(Connection cn, int userid, String accounttype)
 	{
 		GUIFrame view = new GUIFrame(cn, userid, accounttype);
 		this.dispose();
 	}
 }

 import java.awt.*;
 import java.awt.event.*;
 import java.sql.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import javax.swing.*;
 
 /*
  * A review frame where a user can insert a review.
  */
 public class InsertReviewFrame extends JFrame
 {
 	private static Connection connection;
     private static PreparedStatement preparedStatement = null;
     
     JLabel label1, label2, label3, label4; 
     JComboBox input1, input2, input3, input4;
     JButton saveButton, resetButton;
     
     String[] ratingChoices = {"1 - Dishaster!", "2 - Poor", "3 - Meh", "4 - Good", "5 - Great" };
     String restid = "";
     
 	public InsertReviewFrame(Connection cn, final int userid)
 	{
 		connection = cn;
         setTitle("Dishaster! - New Review");
         setLayout(new GridLayout(5,2));
         setSize(new Dimension(400, 250));
         
         label1 = new JLabel(" Restaurant:");
         label2 = new JLabel(" Address:");
         label3 = new JLabel(" Dish:");
         label4 = new JLabel(" Rating:");
         
 	  	ArrayList<String> names = new ArrayList<String>();
 		try {
 			names = GUIFrame.readDataBase("distinct name", 1, 1, "");
 		} catch (Exception e2) {
 			e2.printStackTrace();
 		}
 
         input1 = new JComboBox(names.toArray());
         input1.setSelectedItem(null);
         input2 = new JComboBox();
         input3 = new JComboBox();
         input4 = new JComboBox();
         
         input1.addActionListener(new ActionListener() 
         {
             @Override
             public void actionPerformed(ActionEvent event) 
             {
             	try 
             	{
             		input2.removeAllItems();
             		input3.removeAllItems();
 
             		String name = (String)input1.getSelectedItem();
             		name = name.replace("'", "\\'");
             		String where = "name = '" + name + "'";
             		
             		ArrayList<String> addresses = GUIFrame.readDataBase("address", 1, 1, where);
             		for (String address : addresses)
             			input2.addItem(address);
             		input2.setSelectedItem(null); 		
             		ArrayList<String> foods = GUIFrame.readDataBase("distinct food", 1, 3, where);
             		for (String food : foods)
             			input3.addItem(food);
             		input3.setSelectedItem(null);
             	}
             	catch(Exception e) {}
             }
         });
         
         input2.addActionListener(new ActionListener() 
         {
             @Override
             public void actionPerformed(ActionEvent event) 
             {
             	try 
             	{
             		String where = "address = '" + input2.getSelectedItem() + "'";
             		restid = GUIFrame.readDataBase("restaurant_id", 1, 1, where).get(0);
             	}
             	catch(Exception e) {}
             }
         });
         
         input4 = new JComboBox(ratingChoices);
         input4.setSelectedItem(null);
         
         saveButton = new JButton("Add");
         resetButton = new JButton("Reset");
         
         
         add(label1);
         add(input1);
         add(label2);
         add(input2);
         add(label3);
         add(input3);
         add(label4);
         add(input4);
         add(saveButton);
         add(resetButton);
         
         // resets all the fields
         resetButton.addActionListener(new ActionListener() 
         {
         	public void actionPerformed(ActionEvent ae)
         	{
         		input1.setSelectedItem(null);
         		input2.setSelectedItem(null);
         		input3.setSelectedItem(null);
         		input4.setSelectedItem(null);
         	}
         });
         
         // insert new review
         saveButton.addActionListener(new ActionListener() 
         {
         	public void actionPerformed(ActionEvent ae)
         	{
 	        	String value1 = (String) input1.getSelectedItem();
 	        	String value2 = (String) input2.getSelectedItem();
 	        	String value3 = (String) input3.getSelectedItem();
 	        	String value4 = Integer.toString(input4.getSelectedIndex() + 1);
 	        	System.out.println(value1 + value2 + value3 + value4);
 	        	
 	        	try
 	        	{
 	        		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 	        		Calendar cal = Calendar.getInstance();
 	        		String statement = "insert into Review(reviewer_id, restaurant_id, food, rating, created) " + 
 	        				"values(" + userid + ", " + restid + ", '" + input3.getSelectedItem() + "', " + (input4.getSelectedIndex()+1) + ", '" + dateFormat.format(cal.getTime()) + "')";
 	        		System.out.println(statement);
 	        		preparedStatement = connection.prepareStatement(statement);
 		        	
 	        		preparedStatement.executeUpdate();
 		        	JOptionPane.showMessageDialog(saveButton, "Successfully added.");
 		        	
 		        	// update the review frame table
 		        	ReviewFrame.updateTable("SELECT name as restaurant, address, food, rating, created as date " +
 									  	    "FROM Review NATURAL JOIN Restaurant " +
 									  	    "WHERE reviewer_id = " + userid + " " +
 					        			    "ORDER BY created DESC;");
 		        	
 		        	dispose();
 	        	}
 	        	
 	        	catch(Exception e)
 	        	{
	        		JOptionPane.showMessageDialog(saveButton,"Error!");
 	        	}
         	}
         });
     
         setVisible(true);
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 	}
 }

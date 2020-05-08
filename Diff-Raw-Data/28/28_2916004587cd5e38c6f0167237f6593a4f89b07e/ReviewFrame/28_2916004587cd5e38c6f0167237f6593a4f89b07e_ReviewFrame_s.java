 /**
  * A frame that allows the user to view the current reviews
  * that he/she has made. Also in this frame the user has the
  * ability to insert, update, and delete reviews.
  * 
  * Fall 2013, CS 157A, Group Project
  * @author Kevin Tan
  * @version 11/20/2013
  */
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.util.Vector;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 
 public class ReviewFrame extends JFrame
 {
 	private static Connection connection;
 	private static PreparedStatement preparedStatement = null;
 	
 	// title of the frame
 	private static final String FRAME_TITLE = "Dishaster! - Reviews";
 	
 	// panel that will contain the table of reviews
 	final static JPanel reviewList = new JPanel();
 	static JTable table;
 	static JButton deleteButton;
 	
 	/**
 	 * Constructs the review frame.
 	 * @param connection the connection to database
 	 * @param userid the user id used to log in
 	 * @throws SQLException
 	 */
 	public ReviewFrame(final Connection connection, final int userid) throws SQLException
 	{
 		ReviewFrame.connection = connection;
 		
 		// set the attributes of the frame
         setTitle(FRAME_TITLE);
         setLayout(new FlowLayout(FlowLayout.CENTER));
         setSize(new Dimension(500, 500)); // (width, height)
         
         // button to INSERT a new review
         JButton insertButton = new JButton("New Review");
         insertButton.setPreferredSize(new Dimension(150, 30));
         insertButton.addActionListener(new ActionListener() 
         {
             @Override
             public void actionPerformed(ActionEvent event) 
             {
             	try { new InsertReviewFrame(connection, userid); } // opens new frame
             	catch(Exception e) {}
             	deleteButton.setEnabled(false);
             }
         });
         add(insertButton); // add button to the frame
         
         
         // button to UPDATE a review
         JButton updateButton = new JButton("Update Review");
         updateButton.setPreferredSize(new Dimension(150, 30));
         updateButton.addActionListener(new ActionListener() 
         {
             @Override
             public void actionPerformed(ActionEvent event) 
             {
             	try { new UpdateReviewFrame(connection, userid); } // opens new frame
             	catch(Exception e) {}
             	deleteButton.setEnabled(false);
             }
         });
         add(updateButton); // add button to the frame
         
         
         // button to refresh the list of reviews
         deleteButton = new JButton("Delete Review");
         deleteButton.setEnabled(false);
         deleteButton.setPreferredSize(new Dimension(150, 30));
         deleteButton.addActionListener(new ActionListener() 
         {
             @Override
             public void actionPerformed(ActionEvent event) 
             {
 	        	try 
 	        	{
 	        		int row = table.getSelectedRow();
 	        		if (row == -1)
 	        			return;
         		
 	        		String restaddr = table.getModel().getValueAt(row, 1).toString();
 	        		restaddr = restaddr.replace("'", "\\'");
 	        		String statement = "select restaurant_id from Review join Restaurant using(restaurant_id) where" + 
 	        				" reviewer_id = " + userid + " AND address = '" + restaddr + "'";
 	        		System.out.println(statement);
 	        		preparedStatement = connection.prepareStatement(statement);
 	        		ResultSet resultSet = preparedStatement.executeQuery();
 	        		resultSet.next();
 	        		String restid = resultSet.getString(1);
 	        		String food = table.getModel().getValueAt(row, 2).toString();
 	        		statement = "delete from Review where" + 
 	        				" reviewer_id = " + userid + " AND restaurant_id = " + restid + 
 	        				" AND food = '" + food + "'";
 	        		System.out.println(statement);
 	        		preparedStatement = connection.prepareStatement(statement);
 	        		preparedStatement.executeUpdate();
 	        		
 		        	updateTable("SELECT food, name " +
 						  	"FROM Wishlist NATURAL JOIN Restaurant " +
 						  	"WHERE id = " + userid + " " +
 			    			"");
 		        	deleteButton.setEnabled(false);
 
 	        	}
 	        	catch(Exception e) {}
 	        }
         });
         add(deleteButton); // add button to the frame
         
         // add list of reviews
         add(reviewList);
         
         // get the list of reviews that match the user id	    
         updateTable("SELECT name as restaurant, address, food, rating, created as date " +
 				  	"FROM Review NATURAL JOIN Restaurant " +
 				  	"WHERE reviewer_id = " + userid + " " +
         			"ORDER BY created DESC;");
         
         
         // other frame option statements
         setVisible(true);
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         
 	} // end ReviewFrame constructor
 	
 	
 	/**
 	 * Builds a table model
 	 * @author Paul Vargas on http://stackoverflow.com/questions/10620448/most-simple-code-to-populate-jtable-from-resultset
 	 * @param rs the result set
 	 * @return a table model with the data and column names
 	 * @throws SQLException
 	 */
 	public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException 
 	{
         ResultSetMetaData metaData = rs.getMetaData();
 
         // names of columns
         Vector<String> columnNames = new Vector<String>();
         int columnCount = metaData.getColumnCount();
         for (int column = 1; column <= columnCount; column++) {
             columnNames.add(metaData.getColumnName(column));
         }
 
         // data of the table
         Vector<Vector<Object>> data = new Vector<Vector<Object>>();
         while (rs.next()) {
             Vector<Object> vector = new Vector<Object>();
             for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                 vector.add(rs.getObject(columnIndex));
             }
             data.add(vector);
         }
 
         return new DefaultTableModel(data, columnNames);
     }
 	
 	
 	/**
 	 * Updates the table with a new review category
 	 * @param statement the SQL query to be executed
 	 * @throws SQLException
 	 */
 	public static void updateTable(String statement) throws SQLException
 	{
 		reviewList.removeAll(); // first clear the existing table
 		
 		preparedStatement = connection.prepareStatement(statement);
         ResultSet resultSet = preparedStatement.executeQuery();
 	    table = new JTable(buildTableModel(resultSet));
 	    table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
 	        public void valueChanged(ListSelectionEvent event) {
 	        	deleteButton.setEnabled(true);
 	        }
 	    });
 	    reviewList.add(new JScrollPane(table)); // add the new table to the panel
 	    
 	    reviewList.repaint();
 	    reviewList.revalidate();
 	}
 }

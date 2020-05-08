 package mgtsys;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 import java.sql.*;
 import java.util.ArrayList;
 
 /**
  * @author Yun Wang
  *
  */
 public class start extends JFrame {
 	
 	static final String jdbcURL 
 	= "jdbc:oracle:thin:@ora.csc.ncsu.edu:1521:orcl";
 	
 	private JButton log_in = new JButton("Login");
 	private JButton create = new JButton("Create New Account");
 	private JButton exit = new JButton("Exit");
 	private JPanel panel = new JPanel();
 	private int length = 500;
 	private int height = 500;	
 	
 	public start(boolean first){
 		try{
 			 Class.forName("oracle.jdbc.driver.OracleDriver");
 
			 String user = "apsherid";	
			 String passwd = "000948698";	
 			    
 			 Connection conn = null;
 		     Statement stmt = null;
 		     ResultSet rs = null;
 		     
 		     try{
 		    	// Get a connection from the first driver in the
 		 		// DriverManager list that recognizes the URL jdbcURL
 		 		conn = DriverManager.getConnection(jdbcURL, user, passwd);
 		 		
 		 		// Create a statement object that will be sending your
 				// SQL statements to the DBMS
 				stmt = conn.createStatement();
 				
			//	rs = stmt.executeQuery("SELECT S_ID, S_PASS FROM STUDENTS");
				//printResultSet(rs);
 				
 				if(first){
 					// Create tables
 					ArrayList<String> c_table = initTables();
 					
 					for(int i = 0; i < c_table.size(); i++){
 						//System.out.println(c_table.get(i));
 						stmt.executeUpdate(c_table.get(i));
 					}
 					
 					// Populate the tables
 					ArrayList<String> i_table = initContents();
 					
 					for(int i = 0; i < i_table.size(); i++){
 						//System.out.println(i_table.get(i));
 						stmt.executeUpdate(i_table.get(i));
 					}
 					
 					// for test only
 					// Get data from the Students table
 					rs = stmt.executeQuery("SELECT S_ID, S_PASS FROM STUDENTS");
 					while (rs.next()) {
 					    String id = rs.getString("S_ID");
 					    String pass = rs.getString("S_PASS");
 					    System.out.println(id + "   " + pass);
 					}
 				}
 				
 		     } finally {
 	                close(rs);
 	                close(stmt);
 	                close(conn);
 	         }
 		} catch(Throwable oops) {
             oops.printStackTrace();
         }
 		
 		initComponents();
 	}
 	
 	private void printResultSet(ResultSet rs) {
 		try{
 	      ResultSetMetaData rsmd = rs.getMetaData();
 	      
 	      System.out.println("");
 	  
 	      int numberOfColumns = rsmd.getColumnCount();
 	  
 	      for (int i = 1; i <= numberOfColumns; i++) {
 	        if (i > 1) System.out.print(",  ");
 	        String columnName = rsmd.getColumnName(i);
 	        System.out.print(columnName);
 	      }
 	      System.out.println("");
 	  
 	      while (rs.next()) {
 	        for (int i = 1; i <= numberOfColumns; i++) {
 	          if (i > 1) System.out.print(",  ");
 	          String columnValue = rs.getString(i);
 	          System.out.print(columnValue);
 	        }
 	        System.out.println("");  
 	      }
 		} catch(Exception E){}
 	}
 	
 	private ArrayList<String> initTables(){
 		String s = null;
 		StringBuffer sb = new StringBuffer();
 		ArrayList<String> cre_table = new ArrayList<String>();
 		
 		try{
 			FileReader fr = new FileReader(new File("classes_setup.sql"));
 			BufferedReader br = new BufferedReader(fr);
 			
 			while((s = br.readLine()) != null){
 				if((!(s.startsWith("--"))) && (!s.equals(""))){
 					s = s.trim();
 					if(s.endsWith(";"))
 						sb.append(s);
 					else
 						sb.append(s+" ");
 				}
 				
 			}
 			br.close();
 			
 			String[] inst = sb.toString().split(";");
 			
 			for(int i = 0; i < inst.length; i++){
 				cre_table.add(inst[i]);
 				//stmt.executeUpdate(inst[i]);
 				//System.out.println(inst[i]);
 			}
 		}
 		catch(Throwable oops) {
             oops.printStackTrace();
         }	
 		
 		return cre_table;
 	}
 	
 	private ArrayList<String> initContents(){
 		String s = null;
 		StringBuffer sb = new StringBuffer();
 		ArrayList<String> insert = new ArrayList<String>();
 		
 		try{
 			FileReader fr = new FileReader(new File("insert_statements.sql"));
 			BufferedReader br = new BufferedReader(fr);
 			
 			while((s = br.readLine()) != null){
 				if((!(s.startsWith("--"))) && (!s.equals(""))){
 					s = s.trim();
 					if(s.endsWith(";"))
 						sb.append(s);
 					else
 						sb.append(s+" ");
 				}
 			}
 			br.close();
 			
 			String[] inst = sb.toString().split(";");
 			
 			for(int i = 0; i < inst.length; i++){
 				insert.add(inst[i]);
 				//stmt.executeUpdate(inst[i]);
 				//System.out.println(inst[i]);
 			}
 		}
 		catch(Throwable oops) {
             oops.printStackTrace();
         }
 		
 		return insert;
 	}
 	
 	private void initComponents(){
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setPreferredSize(new Dimension(length, height));
         panel.setBackground(new Color(193, 210, 240));
         setContentPane(panel);
         
         log_in.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
         log_in.addActionListener(new LoginListener());
         create.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
         create.addActionListener(new CreateListener());
         exit.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
         exit.addActionListener(new ExitListener());
         
         GroupLayout WelcomeLayout = new GroupLayout(getContentPane());
         getContentPane().setLayout(WelcomeLayout);
         
         WelcomeLayout.setHorizontalGroup(
         		WelcomeLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
         		.addGroup(WelcomeLayout.createSequentialGroup()
         				.addGap(140)
         				.addGroup(WelcomeLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
         					.addComponent(log_in, GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
         	        		.addComponent(create)
         	        		.addComponent(exit))
         		)
         );
         
         WelcomeLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {log_in, create, exit});
         
         WelcomeLayout.setVerticalGroup(
         		WelcomeLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
         		.addGroup(GroupLayout.Alignment.CENTER, WelcomeLayout.createSequentialGroup()
         				.addGap(90, 90, 90)
         				.addComponent(log_in, GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
         				.addGap(25, 25, 25)
         				.addComponent(create)
         				.addGap(25, 25, 25)
         				.addComponent(exit))
         );
         
         WelcomeLayout.linkSize(SwingConstants.VERTICAL, new Component[] {log_in, create, exit});
         
         pack();
 	}
 	
 	static void close(Connection conn) {
         if(conn != null) {
             try { conn.close(); } catch(Throwable whatever) {}
         }
     }
 
     static void close(Statement st) {
         if(st != null) {
             try { st.close(); } catch(Throwable whatever) {}
         }
     }
 
     static void close(ResultSet rs) {
         if(rs != null) {
             try { rs.close(); } catch(Throwable whatever) {}
         }
     }
 	
 	public class LoginListener implements ActionListener{
 		public void actionPerformed(ActionEvent InstrEvent){
 			new login().setVisible(true);
 			dispose();
 		}
 	}
 	
 	public class CreateListener implements ActionListener{
 		public void actionPerformed(ActionEvent StdntEvent){
 			new createUser().setVisible(true);
 			dispose();
 		}
 	}
 	
 	public class ExitListener implements ActionListener{
 		public void actionPerformed(ActionEvent AsstEvent){
 			dispose();
 		}
 	}
 }

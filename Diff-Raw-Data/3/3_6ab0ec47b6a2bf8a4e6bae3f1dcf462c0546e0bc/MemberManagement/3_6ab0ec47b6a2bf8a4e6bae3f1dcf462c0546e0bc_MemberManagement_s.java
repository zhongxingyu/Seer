 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 
 public class MemberManagement extends JPanel{
 	JLabel details;
 	JPanel center,smo,addrm,modify;
	JTextBox FirstText,LastText,yearText;
 	JButton backButton,displaylist,backButton2,addrmButton,filterButton;
 	
 	JTable table;
 	JScrollPane tablepane;
 	
 	GridBagConstraints constraints;
 	
 	private static final MemberManagement singleton=new MemberManagement();
 	public static MemberManagement getSingleton(){
 		return singleton;
 		
 	}
 	
 	MemberManagement(){
 		
 		setLayout(new BorderLayout());
 		constraints=new GridBagConstraints();
 		
 		
 		
 		backButton=new JButton("Back to mainpage");
 		add(backButton,BorderLayout.NORTH);
 		backButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"mainPage");
 			}
 		});
 		
 		
 		int i=0;
 		Object [][] display=new Object[10][10];
 		String[] colHeads={"ID","FirstName","LastName","JoinDate"}; 
 		ResultSet testResult;
 		Konnection test= Konnection.getSingleton();
 		String testQuery="Select * from members;";
 		testResult=test.query(testQuery);
 		try{
 			
 			while(testResult.next()){
 		
 				display[i][0]=testResult.getString("ID");
 				display[i][1]=testResult.getString("FirstName");
 				display[i][2]=testResult.getString("LastName");
 				display[i][3]=testResult.getString("JoinDate");
 				display[i][4]=testResult.getString("Position");
 				
 				
 				System.out.println(display[i][0]+" "+display[i][1]+" "+display[i][2]+" "+display[i][3]+" ");
 				i++;
 			}
 		}
 		catch(SQLException e){
 			e.printStackTrace();
 		}
 		
 		table=new JTable(display,colHeads);
 		tablepane=new JScrollPane(table);
 		
 		smo=new JPanel();
 		
 		smo.setLayout(new BorderLayout());
 		smo.add(tablepane,BorderLayout.NORTH);
 		MainFrame.getSingleton().mainPanel.add(smo,"tablepane");
 		
 		smo.add(backButton2=new JButton("Back"),BorderLayout.SOUTH);
 		
 		center=new JPanel();
 		center.setLayout(new GridLayout(0,3));
 		
 		center.add(displaylist=new JButton("display the member list"));
 		center.add(addrmButton=new JButton("Add/Remove Member"));
 		center.add(filterButton=new JButton("Modify Member"));
 	    add(center,BorderLayout.CENTER);
 		
 		addrm=new JPanel();
 		addrm.setLayout(new GridLayout(5,2));
 		
 	    
 	    displaylist.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"tablepane");
 			}
 		});
 		
 		backButton2.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"mainPage");
 			}
 			
 		});
 		
 		addrmButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"mainPage");
 			}
 			
 		});
 		
 		filterButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"mainPage");
 			}
 			
 		});
 		
 	
 	}
 }

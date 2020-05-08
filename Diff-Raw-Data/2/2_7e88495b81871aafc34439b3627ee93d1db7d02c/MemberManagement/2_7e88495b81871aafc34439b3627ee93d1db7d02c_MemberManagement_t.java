 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 public class MemberManagement extends JPanel{
 	
 	JLabel details;
 	JPanel center,smo,addrm,modify,tableresult;
 	
 	JLabel FirstLabel,LastLabel,yearLabel,joinLabel,positionLabel,filterLabel,filter2Label,InvalidMessage,addressLabel,ValidMessage;
 	JTextField FirstText,LastText,yearText,joinText,positionText,addressText;
 	TextField filterText,filter2Text;
 	JButton backButton,displaylist,backButton2,backButton3,backButton5,addrmButton,filterButton,commitButton;
 	JButton backButton4,deleteMember;
 	JLabel delMessage,baddelMessage;
 	JPanel smo_below;
 	JPanel addrm_above,modifyPanel,modifyAbove,modifyBelow;
 	JTable table;
 	JScrollPane tablepane,resultpane;
 	String filter=new String();
 		
 	TableModel tableModel;
 	
 	AbstractTableModel change;
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
 		Object [][] display=new Object[130][10];
 		String[] colHeads={"ID","FirstName","LastName","JoinDate","Position","Address"}; 
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
 				display[i][5]=testResult.getString("Address");
 				
 				System.out.println(display[i][0]+" "+display[i][1]+" "+display[i][2]+" "+display[i][3]+" ");
 				i++;
 			}
 		}
 		catch(SQLException e){
 			e.printStackTrace();
 		}
 		
 		table=new JTable(display,colHeads);
 		tablepane=new JScrollPane(table,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		
 		smo=new JPanel();
 		
 		smo.setLayout(new BorderLayout());
 		smo.add(tablepane,BorderLayout.NORTH);
 		
 		smo_below=new JPanel();
 		
 		smo_below.add(filterButton=new JButton("Filter Name: "));
 		smo_below.add(filterText=new TextField(20));
 		
 		
 		tableModel =table.getModel();
 	   
 		filterText.addTextListener(new TextListener(){
 			public void textValueChanged(TextEvent te){
 				filter=filterText.getText();
 				((AbstractTableModel)tableModel).fireTableDataChanged();
 				
 			}
 		});
 		
 		
 		tableModel.addTableModelListener(new TableModelListener(){
 			
 			public void tableChanged(TableModelEvent a){
 				
 					int i=0;
 					JTable result;
 					
 					//DefaultTableModel model=new DefaultTableModel();
 					
 					Object [][] display1=new Object[30][10];
 					String[] colHeads1={"ID","FirstName","LastName","JoinDate","Position","Address"}; 
 					ResultSet testResult;
 					Konnection test= Konnection.getSingleton();
 					String testQuery="Select * from members where FirstName like '"+filter+"%';";
 					testResult=test.query(testQuery);
 					try{
 					
 						while(testResult.next()){
 				
 							display1[i][0]=testResult.getString("ID");
 							display1[i][1]=testResult.getString("FirstName");
 							display1[i][2]=testResult.getString("LastName");
 							display1[i][3]=testResult.getString("JoinDate");
 							display1[i][4]=testResult.getString("Position");
 							display1[i][5]=testResult.getString("Address");
 							
 							System.out.println(display1[i][0]+" "+display1[i][1]+" "+display1[i][2]+" "+display1[i][3]+" ");
 						
 							//smo_below.add(new JLabel(display1[i][0]+" "+display1[i][1]+" "+display1[i][2]+" "+display1[i][3]+" "),BorderLayout.CENTER);	
 							
 							i++;
 						}
 						
 						smo.remove(tablepane);
 						table=new JTable(display1,colHeads1);
 						tablepane=new JScrollPane(table,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 						smo.add(tablepane,BorderLayout.NORTH);
 						smo.validate();
 					}
 					catch(SQLException e){
 						e.printStackTrace();
 					}
 					
 					
 					
 				}
 		
 		});
 		
 		smo.add(smo_below,BorderLayout.CENTER);
 		
 		MainFrame.getSingleton().mainPanel.add(smo,"tablepane");
 		
 		smo.add(backButton2=new JButton("Back"),BorderLayout.SOUTH);
 		
 		center=new JPanel();
 		center.setLayout(new GridLayout(0,3));
 		
 		center.add(displaylist=new JButton("display the member list"));
 		center.add(addrmButton=new JButton("Add a Member"));
 		center.add(filterButton=new JButton("Remove a Member"));
 	    add(center,BorderLayout.CENTER);
 	    
 	    modifyPanel=new JPanel();
 	    modifyPanel.setLayout(new BorderLayout());
 	    MainFrame.getSingleton().mainPanel.add(modifyPanel,"delete");
 	    GridBagConstraints constraints2=new GridBagConstraints();
 	    modifyPanel.add(backButton5=new JButton("Back"),BorderLayout.SOUTH);
 		modifyAbove=new JPanel();
 		modifyAbove.setLayout(new GridBagLayout());
 		constraints2.gridy=0;
 		modifyAbove.add(filter2Label=new JLabel("Enter ID: "),constraints2);
 		modifyAbove.add(filter2Text=new TextField(20),constraints2);
 		constraints2.gridy=1;
 		constraints2.gridwidth=2;
 		modifyAbove.add(deleteMember=new JButton("Delete Member"),constraints2);
 		constraints2.gridy=2;
 		modifyAbove.add(delMessage=new JLabel("Delete Successful !"),constraints2);
 		delMessage.setVisible(false);
 		constraints2.gridy=3;
 		modifyAbove.add(baddelMessage=new JLabel("Delete Unsuccessful !"),constraints2);
 		baddelMessage.setVisible(false);
 		modifyPanel.add(modifyAbove,BorderLayout.CENTER);
 		
 		deleteMember.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				String val=new String();
 				Konnection test= Konnection.getSingleton();
 				int r1=test.update("Delete from members where ID='"+filter2Text.getText()+"';");
 				if(r1>0){
 					baddelMessage.setVisible(false);
 					delMessage.setVisible(true);
 					((AbstractTableModel)tableModel).fireTableDataChanged();
 				}else{
 					delMessage.setVisible(false);
 					baddelMessage.setVisible(true);
 				}
 				modifyAbove.validate();
 				
 			}
 		});
 		
 		
 		addrm=new JPanel();
 		addrm.setLayout(new BorderLayout());
 		addrm_above=new JPanel();
 		
 		addrm_above.setLayout(new GridBagLayout());
 		constraints.gridx=0;
 		constraints.gridy=0;
 		addrm_above.add(FirstLabel=new JLabel("First Name:"),constraints);
 		constraints.gridx=1;
 		constraints.gridy=0;
 		addrm_above.add(FirstText=new JTextField(20),constraints);
 		constraints.gridx=0;
 		constraints.gridy=1;
 	    addrm_above.add(LastLabel=new JLabel("Last Name:"),constraints);
 	    constraints.gridx=1;
 		constraints.gridy=1;
 	    addrm_above.add(LastText=new JTextField(20),constraints);
 	    constraints.gridx=0;
 		constraints.gridy=2;
 	    addrm_above.add(yearLabel=new JLabel("Year:"),constraints);
 	    constraints.gridx=1;
 		constraints.gridy=2;
 	    addrm_above.add(yearText=new JTextField(20),constraints);
 	    constraints.gridx=0;
 		constraints.gridy=3;
 	    addrm_above.add(joinLabel=new JLabel("Join date:"),constraints);
 	    constraints.gridx=1;
 		constraints.gridy=3;
 	    addrm_above.add(joinText=new JTextField(20),constraints);
 	    constraints.gridx=0;
 		constraints.gridy=4;
 	    addrm_above.add(positionLabel=new JLabel("Position:"),constraints);
 	    constraints.gridx=1;
 		constraints.gridy=4;
 	    addrm_above.add(positionText=new JTextField(20),constraints);
 	    constraints.gridx=0;
 	    constraints.gridy=5;
 	    addrm_above.add(addressLabel=new JLabel("Address: "),constraints);
 	    constraints.gridx=1;
 	    constraints.gridy=5;
 	    addrm_above.add(addressText=new JTextField(20),constraints);
 	    constraints.gridx=0;
 	    constraints.gridy=6;
 	    
 	    constraints.gridheight=2;
 	    constraints.gridwidth=2;
 	    addrm_above.add(commitButton=new JButton("commit to database"),constraints);
 	    addrm.add(backButton3=new JButton("Back"),BorderLayout.SOUTH);
 	    constraints.gridx=0;
 		constraints.gridy=8;
 		
 		constraints.gridheight=1;
 		constraints.gridwidth=2;
 		addrm_above.add(InvalidMessage=new JLabel("Invalid Entries"),constraints);
 		InvalidMessage.setVisible(false);
 		constraints.gridy=9;
 		String val=new String();
 		ResultSet rs=test.query("Select max(ID) as ID from members;");
 		try{
 			rs.next();
 		val=rs.getString("ID");
 		System.out.println(val);
 		}catch(Exception q){
 			q.printStackTrace();
 		}
 		
 		ValidMessage=new JLabel("Query Successful !! Remember your member ID please "+(Integer.parseInt(val)+1));
 		addrm_above.add(ValidMessage,constraints);
 	    addrm.add(addrm_above,BorderLayout.CENTER);
 	    ValidMessage.setVisible(false);
 	    
 	    MainFrame.getSingleton().mainPanel.add(addrm,"add");
 	    
 	    
 	    commitButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 								if(FirstText.getText().isEmpty()||LastText.getText().isEmpty()||yearText.getText().isEmpty()||positionText.getText().isEmpty()||joinText.getText().isEmpty()){
 									InvalidMessage.setVisible(true);
 									System.out.println("Invalid Entries !!");
 									addrm_above.validate();
 								}
 								else{
 									InvalidMessage.setVisible(false);
 					
 									int auth=0;
 									Konnection test= Konnection.getSingleton();
 									if(positionText.getText().compareToIgnoreCase("Board Member")==0){
 										auth=2;
 									}
 									else if(positionText.getText().compareToIgnoreCase("Treasurer")==0){
 										auth=2;
 									}
 									else if(positionText.getText().contains("Head")){
 										auth=2;
 									}
									else if(positionText.getText().compareToIgnoreCase("Management Commitee")==0){
 										auth=3;
 									}
 									else if(positionText.getText().compareToIgnoreCase("Member")==0){
 										auth=4;
 									}
 									String testQuery="Insert into members values(null"+",'"+FirstText.getText()+"','"+LastText.getText()+"','"+joinText.getText()+"','"+positionText.getText()+"','"+(FirstText.getText()).charAt(0)+(LastText.getText()).charAt(0)+"','"+addressText.getText()+"','"+auth+"');";
 									int r=test.update(testQuery);
 									if(r>0){
 										
 										ValidMessage.setVisible(true);
 										addrm_above.validate();
 										((AbstractTableModel)tableModel).fireTableDataChanged();
 										
 									}
 									
 									
 									
 								}
 			}
 		});
 			
 	    displaylist.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"tablepane");
 			}
 		});
 			
 		backButton2.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				table.setVisible(true);
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"membermanagement");
 			
 			
 			}
 			
 		});
 		backButton5.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				table.setVisible(true);
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"membermanagement");
 			
 			
 			}
 			
 		});
 	
 		backButton3.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"membermanagement");
 			}
 			
 		});
 		
 		addrmButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"add");
 			}
 			
 		});
 		
 		filterButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"delete");
 			}
 			
 		});
 		
 	
 	}
 }

 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 
 
 public class mainPage extends JPanel{
 	
 	JPanel mainabove,maincenter;
 	JPanel maincenterTop;
 	JButton notifyButton,memberButton,calendarButton,logoutButton;
 	GridBagLayout centerLayout,leftLayout,rightLayout;
 	GridLayout mainLayout;
 	GridBagConstraints constraints;
 	Member currentMember;
 	private static final mainPage singleton= new mainPage();  
 	public static mainPage getSingleton(){
 	    	return singleton;
 	}
 	mainPage(){
 		currentMember=login.currentMember;
 		mainLayout=new GridLayout();
 		constraints=new GridBagConstraints();
 		currentMember.ConsoleOutput();
 		
 		mainabove=new JPanel();
 		mainabove.setLayout(mainLayout);
 		
 		//setting layout
 		setLayout(new BorderLayout());
 		
 		mainabove.add(notifyButton=new JButton("Notifications"));
 		mainabove.add(memberButton=new JButton("Member Management"));
 		mainabove.add(calendarButton=new JButton("     Calendar     "));
 		
 		add(mainabove,BorderLayout.NORTH);
 		add(logoutButton=new JButton("Logout"),BorderLayout.SOUTH);
 		
 		memberButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"membermanagement");
 			}
 		});
 		notifyButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"notifications");
 			}
 		});
 		calendarButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"calendar");
 			}
 		});
 		
 		logoutButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"login");
 			}
 		});
 	}
 	
 	public void displayMemberInfo(){
		if(maincenter!=null){
			remove(maincenter);
		}
 		maincenter=new JPanel();
 		maincenter.setLayout(new GridLayout(3,1));
 		
 		maincenterTop=new JPanel();
 		maincenter.add(maincenterTop);
 		
 		maincenterTop.add(new JLabel("<html><h1><font face=nowdance>Hey </font></h1></html>"));
 		maincenterTop.add(new JLabel("<html><h1><font face=nowdance>"+ currentMember.FirstName+" "+currentMember.LastName+"</font></h1></html>"));
 		maincenterTop.add(new JLabel("<html><h1><font color=blue face=nowdance>("+currentMember.Position+")</font></h1></html>"));
 		
 		add(maincenter,BorderLayout.CENTER);
 	}
 
 }

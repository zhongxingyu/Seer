 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 
 
 public class mainPage extends JPanel{
 	
 	JPanel mainabove,maincenter;
 	JPanel maincenterTop,errorPanel;
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
 		
 		mainabove.add(notifyButton=new JButton("Notifications",new ImageIcon("MailBox-icon.png")));
 		notifyButton.setHorizontalTextPosition(SwingConstants.CENTER);
 		notifyButton.setVerticalTextPosition(SwingConstants.BOTTOM);
 		mainabove.add(memberButton=new JButton("Member Management",new ImageIcon("Folder-icon.png")));
 		memberButton.setHorizontalTextPosition(SwingConstants.CENTER);
 		memberButton.setVerticalTextPosition(SwingConstants.BOTTOM);
 		mainabove.add(calendarButton=new JButton("     Calendar     ",new ImageIcon("Doc-icon.png")));
 		calendarButton.setHorizontalTextPosition(SwingConstants.CENTER);
 		calendarButton.setVerticalTextPosition(SwingConstants.BOTTOM);
 		
 		add(mainabove,BorderLayout.CENTER);
 		add(logoutButton=new JButton("Logout"),BorderLayout.SOUTH);
 		
 		errorPanel=new JPanel();
 		errorPanel.setLayout(new BorderLayout());
 		JLabel errorIcon=new JLabel(new ImageIcon("Important-Icon.png"));
 		
 		JLabel errorMsg=new JLabel("<html><h1><b>You do not have the authority to view this Page</b></h1></html>");
 		errorMsg.setHorizontalAlignment(SwingConstants.CENTER);
 		JButton errorButton=new JButton("Back to MainPage");
 		
 		errorPanel.add(errorIcon,BorderLayout.CENTER);
 		errorPanel.add(errorMsg,BorderLayout.NORTH);
 		errorPanel.add(errorButton,BorderLayout.SOUTH);
 		
 		MainFrame.getSingleton().mainPanel.add(errorPanel,"ERROR");
 		
 		
 		errorButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"mainPage");
 			}
 		});
 		
 		memberButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"membermanagement");
 			}
 		});
 		notifyButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				Notifications.getSingleton().refreshNotifications();
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
 		maincenter.setLayout(new GridLayout(1,3));
 		
 		maincenterTop=new JPanel();
 		maincenter.add(maincenterTop);
 		
 		maincenterTop.add(new JLabel("<html><h1><font face=nowdance>Hey </font></h1></html>"));
 		maincenterTop.add(new JLabel("<html><h1><font face=nowdance>"+ currentMember.FirstName+" "+currentMember.LastName+"</font></h1></html>"));
 		maincenterTop.add(new JLabel("<html><h1><font color=blue face=nowdance>("+currentMember.Position+")</font></h1></html>"));
 		
 		add(maincenter,BorderLayout.NORTH);
 	}
 
 }

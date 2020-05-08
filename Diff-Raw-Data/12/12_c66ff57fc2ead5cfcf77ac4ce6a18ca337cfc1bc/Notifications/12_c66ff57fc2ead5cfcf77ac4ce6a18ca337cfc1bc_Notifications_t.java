 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
import java.sql.SQLException;
 
 public class Notifications extends JPanel {
 	JLabel details;
 	JButton back;
 	JPanel center;
 	GridBagConstraints constraints=new GridBagConstraints();
 	
 	private static final Notifications singleton=new Notifications();
 	public static Notifications getSingleton(){
 		return singleton;
 		
 	}
 	
 	Notifications(){
 		
 		
 		setLayout(new BorderLayout());
 		center=new JPanel();
 		
 		center.setLayout(new GridBagLayout());
 		center.add(details=new JLabel("Here are your recent Notifications !"),constraints);
 		add(center,BorderLayout.CENTER);
 		
 		
 		back=new JButton("Back to mainpage");
 		add(back,BorderLayout.NORTH);
 		back.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"mainPage");
 			}
 		});
 	}
 	
	void sendPublicNotification(Note publicNote){
		String updateStatement;
		try{
			updateStatement="insert into notifications values(default,'"+publicNote;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
 	}
 	
 }

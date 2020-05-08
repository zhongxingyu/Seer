 package edu.cmu.cs214.hw9.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
import edu.cmu.cs214.hw9.facelook.FriendController;
 import edu.cmu.cs214.hw9.facelook.NewsfeedController;
 import edu.cmu.cs214.hw9.facelook.SubscriptionController;
 
 public class SubscriptionListPanel extends JPanel {
 
 	/**
 	 * Create the panel.
 	 */
 	private FacelookAppGUI container;
 	public SubscriptionListPanel(final String email, FacelookAppGUI a) {
 		super();
 		container = a;
 		this.setBackground(Color.decode("#3b5998"));
 		this.setPreferredSize(new Dimension(770,539));
 		setLayout(null);
 		
 		JLabel lblFacelook = new JLabel("Facelook");
 		lblFacelook.setFont(new Font("Lucida Fax", Font.PLAIN, 32));
 		lblFacelook.setForeground(Color.WHITE);
 		lblFacelook.setBounds(12, 13, 199, 32);
 		add(lblFacelook);
 		
 		JLabel lblSubscriptions = new JLabel("Subscriptions");
 		lblSubscriptions.setForeground(Color.WHITE);
 		lblSubscriptions.setFont(new Font("Lucida Fax", Font.PLAIN, 18));
 		lblSubscriptions.setBounds(12, 58, 199, 32);
 		add(lblSubscriptions);
 		
 		
 		JScrollPane scrollPane1 = new JScrollPane();
 		scrollPane1.setBounds(22, 103, 380, 396);
 		add(scrollPane1);
 		JPanel panel = new JPanel();
 		scrollPane1.setViewportView(panel);
 		panel.setLayout(new GridLayout(0, 1, 0, 5));
 		
 		
 		/*
 		 * Fill this grid with buttons that link to profile pages of people who you are subscribed to i.e.
 		 * JButton link = new JButton("Patrick Woody");
 		 * link.addActionListener(new ActionListener(){ ... });
 		 * panel.add(link);
 		 */
 		ArrayList<String> subs = SubscriptionController.listSubscriptions(email);
 		for(String sub: subs){
 			
 			final String subName = sub; //so I can pass this along to the ProfilePanel
 			String userName = NewsfeedController.getUserName(sub);
 			JButton friendButton = new JButton(userName);
 			friendButton.addActionListener(new ActionListener(){   
 				
 				public void actionPerformed(ActionEvent e){
 					
 					container.replace(new ProfilePanel(subName, email, container));
 					
 					
 				}//go to user's profile
 				
 			});
 			panel.add(friendButton);
 		
 		}
 		
 		
 		
 		JButton btnNewsFeed = new JButton("News Feed");
 		btnNewsFeed.setBounds(661, 49, 97, 25);
 		add(btnNewsFeed);
 		btnNewsFeed.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				container.replace(new NewsFeedPanel(email, container));
 			}
 
 			
 		});
 	}
 
 }

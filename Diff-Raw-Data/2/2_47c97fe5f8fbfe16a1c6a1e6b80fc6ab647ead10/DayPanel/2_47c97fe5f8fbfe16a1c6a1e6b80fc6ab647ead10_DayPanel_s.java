 package gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import data.User;
 
 public class DayPanel extends JPanel{
 	
 	private JPanel mainPanel = new JPanel();
 	private GridBagConstraints mainC;
 	private User user;
 	
 
 	public DayPanel(User user) {

 		setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		mainPanel = new JPanel();
 		mainPanel.setPreferredSize(new Dimension(75,600));
 		mainPanel.setLayout(new GridLayout(14,0));
 		setPreferredSize(new Dimension(75,650));
 		mainPanel.setBackground(Color.WHITE);
 		mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		c.gridx = 0;
 		c.gridy = 0;
 		add(new JLabel(user.getUserName()), c);
 		c.gridx = 0;
 		c.gridy = 1;
 		add(mainPanel,c);
 		validate();
 		repaint();
 		
 	}
 	public DayPanel(String string){
 		setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		mainPanel = new JPanel();
 		mainPanel.setPreferredSize(new Dimension(75,600));
 		mainPanel.setLayout(new GridLayout(14,0));
 		setPreferredSize(new Dimension(75,650));
 		mainPanel.setBackground(Color.WHITE);
 		mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		c.gridx = 0;
 		c.gridy = 0;
 		add(new JLabel(string),c);
 		c.gridx = 0;
 		c.gridy = 1;
 		add(mainPanel,c);
 		validate();
 		repaint();
 	}
 	
 	public JPanel getMainPanel(){
 		return mainPanel;
 	}
 	
 	public GridBagConstraints getConstraints(){
 		return mainC;
 	}
 	
 	public void addPanel(JPanel panel){
 		mainPanel.add(panel);
 	}
 	
 	public User getUser(){
 		return this.user;
 	}
 	
 
 }

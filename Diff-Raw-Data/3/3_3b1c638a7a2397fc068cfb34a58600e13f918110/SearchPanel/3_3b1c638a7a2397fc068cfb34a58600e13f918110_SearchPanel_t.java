 import gui.ImagePanel;
 
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 
 import javax.swing.SwingConstants;
 import java.awt.Color;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Line2D;
 import java.util.Random;
 
 import javax.swing.JProgressBar;
 
 import classes.User;
 
 /**
  * TODO Put here a description of what this class does.
  * 
  * @author sternetj. Created Feb 5, 2013.
  */
 public class SearchPanel extends JPanel {
 	private JLabel lblMainInfo;
 	private User friend;
 	private JLabel lblAdditionalInfo;
 	private JLabel lblFriendDistance;
 
 	/**
 	 * Create the panel.
 	 */
 	public SearchPanel() {
 		initialize();
 	}
 
 	private void initialize() {
 		setLayout(null);
 
 		// JPanel picPanel = new JPanel();
 		// picPanel.setBackground(Color.LIGHT_GRAY);
 		//
 		// picPanel.setBounds(10, 10, 60, 60);
 
 
 
 		// add(picPanel);
 
 		friend = null;
 
 		lblMainInfo = new JLabel("Main Info");
 		lblMainInfo.setFont(new Font("Tahoma", Font.BOLD, 18));
 		lblMainInfo.setBounds(77, 11, 565, 22);
 		add(lblMainInfo);
 
 		lblAdditionalInfo = new JLabel("Additional Info");
 		lblAdditionalInfo.setForeground(Color.GRAY);
 		lblAdditionalInfo.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		lblAdditionalInfo.setVerticalAlignment(SwingConstants.TOP);
 		lblAdditionalInfo.setBounds(77, 34, 565, 34);
 		add(lblAdditionalInfo);
 		
 		lblFriendDistance = new JLabel("");
 		lblFriendDistance.setFont(new Font("Tahoma", Font.BOLD, 28));
 		lblFriendDistance.setHorizontalAlignment(SwingConstants.CENTER);
 		lblFriendDistance.setBounds(652, 10, 60, 60);
 		add(lblFriendDistance);
 		
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (e.getID() == e.BUTTON2) {
 					System.out.println("right?");
 				}
 				else System.out.println("left?");
 			}
 		});
 	}
 
 	public void setUsername(String text) {
 		lblMainInfo.setText(text);
 	}
 
 	public void setUser(User u) {
 		this.friend = u;
 		setUsername(u.getName());
 		ImagePanel image;
		String check = u.getImageURL();
		if (u.getImageURL() != null && u.getImageURL() != "")
 			image = new ImagePanel(u.getImageURL(),60,60);
 		else
 			image = new ImagePanel(0);
 		image.setBounds(10, 10, 60, 60);
 		add(image);
 	}
 	
 	public User getUser() {
 		return this.friend;
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		g.setColor(Color.LIGHT_GRAY);
 		g.drawLine(10, 80, 700, 80);
 	}
 	
 	public String getAdditionalInfo(){
 		return this.lblAdditionalInfo.getText();
 	}
 	
 	public void setAdditionalInfo(String info){
 		this.lblAdditionalInfo.setText(info);
 	}
 	
 	public String getMainInfo(){
 		return this.lblMainInfo.getText();		
 	}
 	
 	public void setMainInfo(String info){
 		this.lblMainInfo.setText(info);
 	}
 	
 	public String getdistanceInfo(){
 		return this.lblFriendDistance.getText();
 	}
 	
 	public void setDistance(int dist){
 		
 		if(dist>=1){	
 			this.lblFriendDistance.setText(dist + "\u00b0");
 		}
 	}
 }

 package gui;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 public class Sidebar extends JPanel {
 	
 	private JLabel recieved, sent, calenders;
 	private JList rList, sList;
 	private JScrollPane sScroll,rScroll;
 	private String[] list = {"t1","t2","t3"};
 	
 	GridBagConstraints gbc = new GridBagConstraints();
 	
 	public Sidebar(){
 		recieved = new JLabel();
		recieved.setText("Mottatte Mteinnkallelser");
 		rList = new JList();
 		//rList.setCellRenderer(new CellRenderer());
 		rScroll = new JScrollPane(rList);
 		rScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		
 		sent = new JLabel();
		sent.setText("Sendte Mteinnkallelser");
 		sList = new JList();
 		sScroll = new JScrollPane(sList);
 		sScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		
 		calenders = new JLabel();
 		calenders.setText("Kalendre");
 		
 		setLayout(new GridBagLayout());
 		gbc.gridy = 0;
 		add(recieved,gbc);
 		gbc.gridy = 1;
 		add(rScroll,gbc);
 		gbc.gridy = 2;
 		add(sent,gbc);
 		gbc.gridy = 3;
 		add(sScroll,gbc);
 		gbc.gridy = 4;
 		add(calenders,gbc);
 		
 		//setPreferredSize(new Dimension(250,600));
 	}
 }

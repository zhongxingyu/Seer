 package com.test9.irc.display;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
public class UserListPanel extends JPanel implements Comparator<String>{
 
 	private static final long serialVersionUID = 3331343604631033360L;
 	private static Rectangle boundsRect;
 	private static Font font = new Font("Lucida Grande", Font.PLAIN, 12);
	private static DefaultListModel<String> listModel = new DefaultListModel<String>();
 	private String channel, server;
 	private JScrollPane scrollPane;
 	private JList<String> jList;
 	private ArrayList<String> nicks = new ArrayList<String>();
 
 
 	public UserListPanel(String server, String channel, int width, int height)
 	{
 		this.server = server;
 		this.channel = channel;
 		setLayout(new BorderLayout());
 		boundsRect = new Rectangle(0,0,width,height);
 		setBounds(boundsRect);
 		setBackground(Color.BLACK);
 		jList = new JList<String>(listModel);
 		jList.setFont(font);
 		scrollPane = new JScrollPane(jList);
 		add(scrollPane, BorderLayout.CENTER);
 
 	}
 	
 	
 	/**
 	 * This is used to append a new string to a channels text area.
 	 * @param message
 	 */
 	public void newUser(String user)
 	{
 		nicks.add(user+"\r\n");
 		nicks.trimToSize();
 		Collections.sort(nicks);
 		
 		listModel.clear();
 		for(String s : nicks)
 			listModel.addElement(s);
 	}
 	
 	public void deleteUser(String user)
 	{
 		boolean found = false;
 		int index = 0;
 		
 		while(!found)
 		{
 			if(nicks.get(index).equals(user))
 				nicks.remove(index);
 		}
 		
 		nicks.trimToSize();
 		Collections.sort(nicks);
 		
 		listModel.clear();
 		for(String s : nicks)
 			listModel.addElement(s);
 	}
 	
 	public static void setNewBounds(int width, int height)
 	{
 		boundsRect.setBounds(0, 0, width, height);
 	}
 
 
 	/**
 	 * @return the channel
 	 */
 	public String getChannel() {
 		return channel;
 	}
 
 
 	/**
 	 * @param channel the channel to set
 	 */
 	public void setChannel(String channel) {
 		this.channel = channel;
 	}
 
 
 	/**
 	 * @return the scrollPane
 	 */
 	public JScrollPane getScrollPane() {
 		return scrollPane;
 	}
 
 
 	/**
 	 * @param scrollPane the scrollPane to set
 	 */
 	public void setScrollPane(JScrollPane scrollPane) {
 		this.scrollPane = scrollPane;
 	}
 	
 
 	/**
 	 * @return the serialversionuid
 	 */
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 
 
 	/**
 	 * @return the bounds
 	 */
 	public static Rectangle getBoundsRec() {
 		return boundsRect;
 	}
 
 
 	/**
 	 * @param bounds the bounds to set
 	 */
 	public static void setBoundsRec(Rectangle bounds) {
 		UserListPanel.boundsRect = bounds;
 	}
 
 
 	@Override
 	public int compare(String o1, String o2) {
 		return(o1.toLowerCase().compareTo(o2.toLowerCase()));
 	}
 
 
 	/**
 	 * @return the server
 	 */
 	public String getServer() {
 		return server;
 	}
 
 
 	/**
 	 * @param server the server to set
 	 */
 	public void setServer(String server) {
 		this.server = server;
 	}
 }

 package com.test9.irc.display;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Rectangle;
 
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 public class UserListPanel extends JPanel implements ListSelectionListener{
 
 	private static final long serialVersionUID = 3331343604631033360L;
 	/**
 	 * Holds the bounds for the panel.
 	 */
 	private static Rectangle boundsRect;
 
 	/**
 	 * Holds the font for the panel.
 	 */
 	private static Font font = new Font("Lucida Grande", Font.BOLD, 12);
 
 	/**
 	 * Holds the model of the list that will be used.
 	 */
 	private SortedListModel<String> listModel = new SortedListModel<String>();
 
 	/**
 	 * Holds the name of the channel that the list is for.
 	 */
 	private String channel;
 
 	/**
 	 * Holds the name of the server the list is on.
 	 */
 	private String server;
 
 	/**
 	 * The scroll pane for hte list.
 	 */
 	private JScrollPane scrollPane;
 
 	/**
 	 * The list that contains all the information.
 	 */
	private JList<String> jList = new JList<String>();
 
 
 	/**
 	 * Constructs a new UserListPanel for a channel.
 	 * @param server Name of the server the list is on.
 	 * @param channel Name of the channel the list is on.
 	 * @param width Width.
 	 * @param height Height.
 	 */
	@SuppressWarnings("unchecked")
 	UserListPanel(String server, String channel, int width, int height)
 	{
 		System.out.println(server + " " + channel);
 		jList.setBackground(Color.BLACK);
 		jList.setForeground(Color.WHITE);
 		this.server = server;
 		this.channel = channel;
 		setLayout(new BorderLayout());
 		boundsRect = new Rectangle(0,0,width,height);
 		setBounds(boundsRect);
 		setBackground(Color.BLACK);
 		jList.setModel(listModel);
 		jList.setFont(font);
 		scrollPane = new JScrollPane(jList);
 		scrollPane.setBackground(Color.BLACK);
 		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(2,0));
 		scrollPane.setBorder(null);
 		add(scrollPane, BorderLayout.CENTER);
 
 	}
 
 	/**
 	 * Adds a new user.
 	 * @param user User's nick.
 	 */
 	void newUser(String user)
 	{
 		listModel.add(user);
 	}
 
 	/**
 	 * Removes a parted user.
 	 * @param user User's nick.
 	 */
 	void userPart(String user)
 	{
 		listModel.removeElement(user);
 	}
 	
 	void userAway(String nick) {
 		
 	}
 
 	/**
 	 * Changes a nick if someone changes their nick on the server.
 	 * @param oldNick The original nick.
 	 * @param newNick The user's new nick.
 	 */
 	void nickChange(String oldNick, String newNick)
 	{
 		listModel.removeElement(oldNick);
 		newUser(newNick);
 	}
 
 	/**
 	 * Used for resizing the UserListPanel.
 	 * @param width Width.
 	 * @param height Height.
 	 */
 	static void setNewBounds(int width, int height)
 	{
 		boundsRect.setBounds(0, 0, width, height);
 	}
 
 
 	public void valueChanged(ListSelectionEvent e) {
 
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
 	void setChannel(String channel) {
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
 	void setScrollPane(JScrollPane scrollPane) {
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
 	static void setBoundsRec(Rectangle bounds) {
 		UserListPanel.boundsRect = bounds;
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
 	void setServer(String server) {
 		this.server = server;
 	}
 
 	/**
 	 * @return the listModel
 	 */
 	public SortedListModel<String> getListModel() {
 		return listModel;
 	}
 
 	/**
 	 * @param listModel the listModel to set
 	 */
 	public void setListModel(SortedListModel<String> listModel) {
 		this.listModel = listModel;
 	}
 }

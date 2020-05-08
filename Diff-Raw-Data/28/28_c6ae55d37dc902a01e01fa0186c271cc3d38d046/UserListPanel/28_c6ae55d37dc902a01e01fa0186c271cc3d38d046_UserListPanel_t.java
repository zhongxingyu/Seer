 package com.test9.irc.display;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 public class UserListPanel extends JPanel implements ListSelectionListener, FocusListener{
 
 	private static final long serialVersionUID = 3331343604631033360L;
 	/**
 	 * Holds the bounds for the panel.
 	 */
 	private static Rectangle boundsRect;
 
 	/**
 	 * Holds the font for the panel.
 	 */
	private static Font font = new Font("Lucida Grande", Font.BOLD, 10);
 
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
 	private JList jList = new JList();
 
 
 	/**
 	 * Constructs a new UserListPanel for a channel.
 	 * @param server Name of the server the list is on.
 	 * @param channel Name of the channel the list is on.
 	 * @param width Width.
 	 * @param height Height.
 	 */
 	UserListPanel(String server, String channel, int width, int height)
 	{
 		//System.out.println(server + " " + channel);
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
 		scrollPane.getVerticalScrollBar().setPreferredSize(ChatWindow.getScrollBarDim());
 		scrollPane.getHorizontalScrollBar().setPreferredSize(ChatWindow.getScrollBarDim());
 		scrollPane.setBorder(null);
 		add(scrollPane, BorderLayout.CENTER);
 		jList.addFocusListener(this);
 
 
 	}
 
 	/**
 	 * Adds a new user.
 	 * @param user User's nick.
 	 */
 	void newUser(String user)
 	{
 		listModel.add(user);
 		invalidate();
 	}
 
 	/**
 	 * Removes a parted user.
 	 * @param user User's nick.
 	 */
 	void userPart(String user)
 	{
 		listModel.removeElement(user);
 		invalidate();
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
 		if(listModel.removeElement(oldNick)) {
 			newUser(newNick);
 			invalidate();
 		}
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
 		invalidate();
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
 		invalidate();
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
 		invalidate();
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
 		invalidate();
 	}
 
 	@Override
 	public void focusGained(FocusEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void focusLost(FocusEvent e) {
 		jList.getSelectionModel().clearSelection();
 	}
 }

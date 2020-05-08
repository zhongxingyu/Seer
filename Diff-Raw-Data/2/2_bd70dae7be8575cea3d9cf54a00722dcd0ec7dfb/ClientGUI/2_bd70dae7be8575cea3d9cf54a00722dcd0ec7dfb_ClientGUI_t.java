 package ucbang.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import ucbang.core.Card;
 import ucbang.core.Player;
 import ucbang.network.Client;
 
 public class ClientGUI extends JFrame implements KeyListener, ComponentListener,
 	ActionListener{
 
 	private static final long serialVersionUID = 4377855794895936467L;
 	int p;
 	StringBuilder chat;
 	boolean chatting = false;
 	public int width = 800;
 	public int height = 600;
 	ArrayList<String> text = new ArrayList<String>();
 	ArrayList<Color> textColor = new ArrayList<Color>();
 	int textIndex = -1; // the bottom line of the text
 	public Client client;
 	JMenuBar menubar;
 	JMenu menu, chatmenu, helpmenu;
 	JMenuItem quit, chatlog, about;
 	JPanel panel;
 	JDialog logviewer;
 	JScrollPane logscroll;
 	JTextArea logs;
 	public ClientGUI(int p, Client client) {
 		this.client = client;
 		this.p = p;
 		chat = new StringBuilder();
 		createAndShowGui();
 	}
 
 
 	/**
 	 * 
 	 */
 	private void createAndShowGui() {
 		// set window sizes
 		//setPreferredSize(new Dimension(width, height));
 		//setSize(new Dimension(width, height));
 		addKeyListener(this);
 		this.setIgnoreRepaint(true);
 		GamePanel panel = new GamePanel();
 		panel.setSize(width, height);
 		panel.setPreferredSize(new Dimension(width, height));
 		this.add(panel);
 		this.pack();
 		createMenu();
 		this.setJMenuBar(menubar);
 		this.setVisible(true);
 		this.requestFocus(true);
 		this.setTitle("UCBang");
 		addWindowListener(new WindowAdapter() {
 			public void windowActivated(WindowEvent e) {
 				paint(getGraphics());
 			}
 			public void windowClosing(WindowEvent e){
 				((ClientGUI)(e.getWindow())).client.running=false;
 			}
 		});
 		panel.addComponentListener(this);
 
 		createLogViewer();
 	}
 
 
 	/**
 	 * 
 	 */
 	private void createLogViewer() {
 		logviewer = new JDialog(this, "Chat Log", false);
 		logviewer.setSize(400, 200);
 		logviewer.setLocation(800, 0);
 		logs = new JTextArea(9,34);
 		logs.setEditable(false);
 		logscroll = new JScrollPane(logs, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		logviewer.add(logscroll);
 		logviewer.pack();
 	}
 
 
 	/**
 	 * 
 	 */
 	private void createMenu() {
 		menubar = new JMenuBar();
 		menu = new JMenu("Game");
 		menu.setMnemonic(KeyEvent.VK_G);
 		menubar.add(menu);
 		quit = new JMenuItem("Quit");
 		quit.setMnemonic(KeyEvent.VK_Q);
 		quit.addActionListener(this);
 		menu.add(quit);
 		
 		chatmenu = new JMenu("Chat");
 		chatmenu.setMnemonic(KeyEvent.VK_C);
 		menubar.add(chatmenu);
 		chatlog = new JMenuItem("View Chat Log");
 		chatlog.setMnemonic(KeyEvent.VK_L);
 		chatlog.addActionListener(this);
 		chatmenu.add(chatlog);
 		
 		helpmenu = new JMenu("Help");
 		helpmenu.setMnemonic(KeyEvent.VK_H);
 		menubar.add(helpmenu);
 		about = new JMenuItem("About");
 		about.setMnemonic(KeyEvent.VK_A);
 		about.addActionListener(this);
 		helpmenu.add(about);
 	}
 
 	/**
 	 * appendText with default color of white
 	 * 
 	 * @param str
 	 * @param c
 	 */
 	public void appendText(String str) {
 		appendText(str, Color.WHITE);
 	}
 
 	/**
 	 * adds text to the bottom of the text area
 	 * 
 	 * @param str
 	 * @param c
 	 */
 	public void appendText(String str, Color c) {
 		// TODO: actually do something with color
 		textIndex++;
 		text.add(str);
 		textColor.add(c);
		//paint(getGraphics()); HJKLM
 		logs.append(str+"\n");
 		logscroll.getVerticalScrollBar().setValue(logscroll.getVerticalScrollBar().getMaximum());
 		logviewer.repaint();
 	}
 
 	public void update() {
 		paint(this.getGraphics());
 	}
 
 	/**
 	 * Prompts the player to choose a name
 	 * @return the name the player chose
 	 */
 	public static String promptChooseName() {
 		String s = "";
 		while (s == null || s.length() == 0) {
 			s = (String) JOptionPane
 			.showInputDialog(null, "What is your name?");
 		}
 		return s;
 	}
 
 	/**
 	 * Creates a yes or no prompt with the desired question and title
 	 * @param message The message to ask
 	 * @param title The title of the message prompt
 	 * @return whether the client agrees to start; 0 is yes and 1 is no
 	 */
 	public int promptYesNo(String message, String title) {
 		// I think it's visually more intuitive to have Yes on the left, but keep in
 		// mind that this means 0 is yes and 1 is no!
 		int r = -1;
 		while (r == -1) {
 			r = JOptionPane.showOptionDialog(this, message, title,
 					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
 					null, new String[] { "Yes", "No" }, "Yes");
 		}
 		return r;
 	}
 
 
 	/**
 	 * Asks the player to choose a card. This is used for many instances. TODO:
 	 * replace al with ID of the player.
 	 * 
 	 * @param al
 	 * @return
 	 */
 	public void promptChooseCard(ArrayList<Card> al, String str1, String str2, boolean force) {
 		client.field.pick = al;
 		client.prompting = true;
 		client.forceDecision = force;
 	}
 	/**
 	 * Adds one bool, then does promptChooseCard
 	 * @param str1
 	 * @param str2
 	 * @param force
 	 */
 	public void promptTargetCard(String str1, String str2, boolean force) {
 		promptChooseCard(null, str1, str2, force);
 	}
 
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void keyTyped(KeyEvent e) {
 		if((int)e.getKeyChar()==27){
 			if(chatting){
 				chatting = false;
 				if (chat.length() > 0) {
 					chat.delete(0, chat.length());
 				}
 			}
 			else{
 				if(client.prompting&&!client.forceDecision){
 					client.outMsgs.add("Prompt:-1");
                                         client.nextPrompt = -2;
 					client.prompting = false;
 				}
 				return;
 			}
 		}
 		else if (e.getKeyChar() == '\n') {
 			chatting = !chatting;
 			if (!chatting && chat.length() > 0) {
 				client.addChat(chat.toString());
 				chat.delete(0, chat.length());
 			}
 		} else if (chatting) {
 			if ((e.getKeyChar()) == 8 && chat.length() > 0)
 				chat.deleteCharAt(chat.length() - 1);
 			else
 				chat.append(e.getKeyChar());
 		} else{
 			/*if(Character.isDigit(e.getKeyChar())){
 				int f = ((Character)e.getKeyChar())%48;
 				appendText(client.players.get(f).name);
 			}
 			if((char)e.getKeyChar()=='a'){
 				appendText(String.valueOf(client.numPlayers));
 			}
 			if((char)e.getKeyChar()=='s'){
 				appendText(String.valueOf(client.players.size()));
 			}
 			if((char)e.getKeyChar()=='d'){
 				appendText(String.valueOf(client.id));
 			}
 			if((char)e.getKeyChar()=='f'){
 				appendText(String.valueOf(client.player.id));
 			}
 			if((char)e.getKeyChar()=='g'){
 				appendText(client.players.get(client.id)+" "+client.player+" "+client.id);
 			}
 			if((char)e.getKeyChar()=='h'){
 				appendText(client.players.get(client.id).hand.size()+""+client.player.hand.size());
 			}
 			if((char)e.getKeyChar()=='j'){
 				String s = "";
 				for(Card c:client.player.hand){
 					s+=c.name+" ";
 				}
 				appendText(s);
 			}
                         */
 		}
 	}
 
 
 	public void componentHidden(ComponentEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	public void componentMoved(ComponentEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void componentResized(ComponentEvent e){
 		int oldw = width;
 		int oldh = height;
 
 		height = e.getComponent().getHeight();
 		width = e.getComponent().getWidth();
 		if(height<600)
 			height=600;
 		if(width<800)
 			width=800;
 		e.getComponent().setSize(width, height);
 		
 		if(client.field!=null)client.field.resize(oldw, oldh, width, height);
 	}
 
 
 	public void componentShown(ComponentEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	//@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource().equals(quit)){
 			client.running = false;
 		}else if(e.getSource().equals(chatlog)){
 			logviewer.setVisible(true);
 		}else if(e.getSource().equals(about)){
 			
 		}
 	}
 	private class GamePanel extends JPanel{
 		public void paint(Graphics g) {
 			Graphics2D graphics = (Graphics2D)g;
 			graphics.setColor(new Color(175, 150, 50));
 			graphics.fillRect(0, 0, width, height);
 			//the ugly proxy skip turn button: this is coded for in Field.java
 			graphics.setColor(new Color(255, 255, 255));
 			graphics.fillRect(760, 560, 40, 40);
 			graphics.setColor(new Color(0, 0, 0));
 			graphics.drawString("Skip", 770, 580);
 
 			if (chatting) {
 				graphics.setColor(new Color(185, 160, 60));
 				graphics.draw3DRect(17, 407, 760, 18, true);
 				graphics.setColor(Color.WHITE);
 				graphics.drawString("Chatting: " + chat.toString(), 20, 420);
 			}
 			if (textIndex >= 0) { // there is text to display, must draw it
 				for (int n = textIndex; n >= (textIndex < 9 ? 0 : textIndex - 9); n--) {
 					graphics.setColor(textColor.get(n));
 					graphics.drawString(text.get(n), 20, 580 - 15 * (textIndex - n));
 					graphics.setColor(Color.WHITE);
 				}
 			}
 			if(client.field!=null)
 	                    client.field.paint(graphics);
 			graphics.setColor(Color.DARK_GRAY);
 			graphics.drawString("Players", 10, 12);
 			Iterator<Player> iter = client.players.iterator();
 			int n = 0;
 			while (iter.hasNext()) {
 				Player temp = iter.next();
 	                        graphics.drawString(temp.name, 25, 25 + 15 * n++);
 			}
 		}
 	}
 }

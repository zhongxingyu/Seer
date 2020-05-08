 package ucbang.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferStrategy;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import ucbang.core.Card;
 import ucbang.core.Player;
 import ucbang.core.Deck;
 import ucbang.network.Client;
 
 public class ClientGUI extends JFrame implements KeyListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4377855794895936467L;
 	BufferStrategy strategy;
 	public Player player;
 	int p;
 	StringBuilder chat;
 	boolean chatting = false;
 
 	ArrayList<String> text = new ArrayList<String>();
 	int textIndex = -1; // the bottom line of the text
 	Client client;
 	CardDisplayer cd;//TODO: Temporary
 	public ClientGUI(int p, Client client) {
 		this.p = p;
 		chat = new StringBuilder();
 		// set window sizes
 		setPreferredSize(new Dimension(800, 600));
 		setSize(new Dimension(800, 600));
 		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		addKeyListener(this);
 		this.setIgnoreRepaint(true);
 		this.setVisible(true);
 		this.requestFocus(true);
 		this.createBufferStrategy(2);
 		strategy = this.getBufferStrategy();
 		this.client = client;
 		this.setTitle("UCBang");
 		addWindowListener(new WindowAdapter() {
 			public void windowActivated(WindowEvent e) {
 				paint(getGraphics());
 			}
 		});
 		//TODO: Remove this later, just for testing drawing.
		cd = new CardDisplayer(new Card(Deck.CardName.BANG), 200, 50);
 		paint(getGraphics());
 	}
 
 	public void paint(Graphics g) {
 		Graphics2D graphics;
 		try {
 			graphics = (Graphics2D) strategy.getDrawGraphics();
 		} catch (Exception e) {
 			return;
 		}
 		// fill background w/ dark green
 		graphics.setColor(Color.GREEN);
 		graphics.fillRect(0, 0, 800, 400);
 		graphics.setColor(new Color(100, 0, 0));
 		graphics.fillRect(0, 400, 800, 600);
 		if (chatting) {
 			graphics.setColor(Color.WHITE);
 			graphics.drawString("Chatting: " + chat.toString(), 20, 420);
 		}
 		if (textIndex >= 0) { // there is text to display, must draw it
 			graphics.setColor(Color.WHITE);
 			for (int n = textIndex; n >= (textIndex < 9 ? 0 : textIndex - 9); n--) {
 				graphics
 						.drawString(text.get(n), 20, 580 - 15 * (textIndex - n));
 			}
 		}
 		graphics.setColor(Color.DARK_GRAY);
 		graphics.drawString("Players", 25, 40);
 		Iterator<String> iter = client.players.iterator();
 		int n = 0;
 		while (iter.hasNext()) {
 			graphics.drawString(iter.next(), 30, 60 + 15 * n++);
 		}
 		cd.paint(graphics);
 		graphics.dispose();
 		// paint backbuffer to window
 		strategy.show();
 	}
 
 	/**
 	 * appendText with default color of black
 	 * 
 	 * @param str
 	 * @param c
 	 */
 	public void appendText(String str) {
 		appendText(str, Color.BLACK);
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
 		paint(getGraphics());
 	}
 
 	public void update() {
 		paint(this.getGraphics());
 	}
 
 	public String promptChooseName() {
 		String s = "";
 		while (s == null || s.length() == 0) {
 			s = (String) JOptionPane
 					.showInputDialog(this, "What is your name?");
 		}
 		return s;
 	}
 
 	// I think it's visually more intuitive to have Yes on the left, but keep in
 	// mind that this means 01 is yes and 1 is no!
 	public int promptYesNo(String str1, String str2) {
 		int r = -1;
 		while (r == -1) {
 			r = JOptionPane.showOptionDialog(this, str1, str2,
 					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
 					null, new String[] { "Yes", "No" }, "Yes");
 		}
 		return r;
 	}
 
 	public int promptChooseTargetPlayer() {
 		return 1 - p; // temporary fix for not being able to target
 	}
 
 	public int promptChooseCharacter(ArrayList<Card> al) {
 		return promptChooseCard(al, "Who do you want to be? You are a(n) " // TODO
 																			// :
 																			// check
 																			// for
 																			// a
 																			// /
 																			// an
 																			// instead
 																			// of
 																			// being
 																			// lazy
 				+ player.role, "Choose your character!", true);
 	}
 
 	/**
 	 * Asks the player to choose a card. This is used for many instances. TODO:
 	 * replace al with ID of the player.
 	 * 
 	 * @param al
 	 * @return
 	 */
 	public int promptChooseCard(ArrayList<Card> al, String str1, String str2,
 			boolean forceDecision) {
 		Card[] temp = new Card[al.size()];
 		temp = al.toArray(temp);
 		String[] options = new String[temp.length];
 		for (int i = 0; i < temp.length; i++) {
 			options[i] = ((Card) temp[i]).name;
 		}
 		int n = -1;
 		if (forceDecision) {
 			while (n == -1)
 				n = JOptionPane
 						.showOptionDialog(this, str1, str2,
 								JOptionPane.YES_NO_OPTION,
 								JOptionPane.QUESTION_MESSAGE, null, options,
 								options[0]);
 			return n;
 		} else
 			return JOptionPane.showOptionDialog(this, str1, str2,
 					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
 					null, options, options[0]);
 	}
 
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void keyTyped(KeyEvent e) {
 		if (e.getKeyChar() == '\n') {
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
 		}
 		paint(getGraphics());
 	}
 }

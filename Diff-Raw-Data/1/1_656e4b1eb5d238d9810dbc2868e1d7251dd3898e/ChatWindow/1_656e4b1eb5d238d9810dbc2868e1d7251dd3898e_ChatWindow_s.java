 package org.publicmain.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 
 import org.publicmain.chatengine.ChatEngine;
 import org.publicmain.common.LogEngine;
 import org.publicmain.common.MSG;
 import org.publicmain.common.MSGCode;
 import org.publicmain.common.Node;
 
 /**
  * @author ATRM
  * 
  */
 
 public class ChatWindow extends JPanel implements ActionListener, Observer {
 
 	// Deklarationen:
 	private String name;
 	private JButton sendenBtn;
 	private JTextPane msgTextPane;
 	private HTMLEditorKit htmlKit;
 	private HTMLDocument htmlDoc;
 	private JScrollPane msgTextScroller;
 	private JTextField eingabeFeld;
 	private String gruppe;
 	private Long userID;
 	private boolean isPrivCW;
 	private GUI gui;
 	private History keyHistory;
 	private ChatWindowTab myTab;
 	private JPanel panel;
 	private String helptext="<br><table color='#05405E'>" +
 			"<tr><td colspan='3'><b>Command</b></td><td><b>Description</b></td></tr>" +
 			"<tr><td colspan='3'>/clear</td><td>clear screen</td></tr>" +
 			"<tr><td colspan='3'>/exit</td><td>exit program</td></tr>" +
 			"<tr><td colspan='3'>/help</td><td>display this help</td></tr>" +
 			"<tr><td>/ignore</td><td colspan='2'>&lt;username&gt;</td><td>ignore this user</td></tr>" +
 			"<tr><td>/unignore</td><td colspan='2'>&lt;username&gt;</td><td>unignore this user</td></tr>" +
 			"<tr><td>/info</td><td colspan='2'>&lt;username&gt;</td><td>display information about user</td></tr>" +
 			"<tr><td>/alias</td><td colspan='2'>&lt;username&gt;</td><td>change username</td></tr>" +
 			"<tr><td>/g</td><td>&lt;groupname&gt;</td><td>&lt;message&gt;</td>message to group</tr>" +
 			"<tr><td>/w</td><td>&lt;username&gt;</td><td>&lt;message&gt;</td>whisper to user</tr>" +
 			"<tr><td>/s</td><td  colspan='2'>&lt;message&gt;</td>shout</tr>" +
 			"</table><br>";
 	private boolean onlineState;
 	private Thread onlineStateSetter;
 
 	public ChatWindow( long uid) {
 		this.userID = uid;
 		this.isPrivCW = true;
 		Node nodeForUID = GUI.getGUI().getNodeForUID(userID);
 		if(nodeForUID!=null)this.name = nodeForUID.getAlias();
 		doWindowbuildingstuff();
 	}
 
 
 	public ChatWindow(String gruppenname) {
 		gruppe = gruppenname;
 		this.name = gruppenname;
 		this.isPrivCW = false;
 		onlineState = true;
 		doWindowbuildingstuff();
 	}
 
 	public void updateName() {
 		if(isPrivCW) {
 			Node nodeForUID = GUI.getGUI().getNodeForUID(userID);
 			if(nodeForUID!=null)this.name = nodeForUID.getAlias();
 			myTab.updateAlias();
 		}
 	}
 	/**
 	 * Erstellt Content und macht Layout fr das Chatpanel
 	 */
 	private void doWindowbuildingstuff() {
 		this.myTab =  new ChatWindowTab(name, GUI.getGUI().getTabbedPane(), this); 
 		// Layout fr ChatWindow (JPanel) festlegen auf BorderLayout:
 		this.setLayout(new BorderLayout());
 
 		// Initialisierungen:
 		this.gui = GUI.getGUI();
 		this.sendenBtn = new JButton("send");
 		this.msgTextPane = new JTextPane();
 		this.htmlKit = new HTMLEditorKit();
 		this.htmlDoc = new HTMLDocument();
 		this.msgTextScroller = new JScrollPane(msgTextPane,	JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		this.eingabeFeld = new JTextField();
 		this.panel = new JPanel(new BorderLayout());
 
 		this.msgTextPane.setEditable(false);
 		this.msgTextPane.setPreferredSize(new Dimension(400, 300));
 		this.msgTextPane.setEditorKit(htmlKit);
 		this.msgTextPane.setDocument(htmlDoc);
 
 		this.eingabeFeld.setDocument(new SetMaxText(200)); // spter ber Configure-Datei
 
 		// KeyListener fr Nachrichtenhistorie hinzufgen
 		this.eingabeFeld.addKeyListener(new History(eingabeFeld));
 
 		this.sendenBtn.addActionListener(this);
 		this.sendenBtn.addMouseListener(new MouseListenerImplementation());
 
 		this.eingabeFeld.addActionListener(this);
 		this.keyHistory=new History(eingabeFeld);
 
 		this.panel.add(eingabeFeld, BorderLayout.CENTER);
 		this.panel.add(sendenBtn, BorderLayout.EAST);
 		
 		this.add(msgTextScroller, BorderLayout.CENTER);
 		this.add(panel, BorderLayout.SOUTH);
 
 		
 		this.onlineStateSetter = new Thread(new RunnableImplementation());
 		if(isPrivCW){
 			this.onlineStateSetter.start();
 		}
 		new DropTarget(msgTextPane, new DropTargetListenerImplementation());
 		
 		this.setVisible(true);
 	}
 	
 	/**
 	 * @return String fr Tab..
 	 */
 	public String getChatWindowName() {
 		return this.name;
 	}
 	
 	void setChatWindowName(String name){
 		this.name = name;
 	}
 	
 	/**
 	 * @return
 	 */
 	public JPanel getWindowTab(){
 		return this.myTab;
 	}
 	
 	/**
 	 * @return
 	 */
 	boolean getOnlineState(){
 		return this.onlineState;
 	}
 	
 	void focusEingabefeld(){
 		this.eingabeFeld.requestFocusInWindow();
 		this.myTab.stopBlink();
 	}
 	
 	/**
 	 * @return ture wenn privates ChatWindow
 	 */
 	public boolean isPrivate(){
 		return this.isPrivCW;
 	}
 	
 	/**
 	 * @return true wenn Gruppen ChatWindow 
 	 */
 	public boolean isGroup(){
 		return !this.isPrivCW;
 	}
 	
 	/**
 	 * @param x
 	 */
 	public void info(String x){
 		putMSG(new MSG(x,MSGCode.CW_INFO_TEXT));
 	}
 	
 	/**
 	 * @param x
 	 */
 	public void warn(String x){
 		putMSG(new MSG(x,MSGCode.CW_WARNING_TEXT));
 	}
 	
 	/**
 	 * @param x
 	 */
 	public void error(String x){
 		putMSG(new MSG(x,MSGCode.CW_ERROR_TEXT));
 	}
 	
 	/**
 	 * In dieser Methode werden die Texteingaben aus dem eingabeFeld verarbeitet
 	 */
 	public void actionPerformed(ActionEvent e) {
 		// Eingabe aus dem Textfeld in String eingabe speichern
 		String eingabe = eingabeFeld.getText();
 
 		// HTML-Elemente verhindern
 		eingabe = eingabe.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
 
 		// erlaubte HTML-Elemente mit anderem Syntax einfgen
 		eingabe = eingabe.replaceAll("(\\[)(?=/?(b|u|i|strike)\\])", "<");
 		eingabe = eingabe.replaceAll("(?<=((</?)(b|u|i|strike)))(\\])", ">");
 		
 		// Prfen ob etwas eingegeben wurde, wenn nicht dann auch nichts machen
 		if (!eingabe.equals("")) {
 
 			// Prfen ob die Eingabe ein Befehl ist
 			if (eingabe.startsWith("/")) {
 				String[] tmp;
 				
 				// Prfen ob die Eingabe ein einfacher Befehl ist
 				if (eingabe.equals("/clear")) {
 					this.msgTextPane.setText("");
 				}
 				else if (eingabe.equals("/help")) {
 					info(helptext);
 				}
 				else if (eingabe.equals("/exit")) {
 					this.gui.shutdown();
 				}
 
 				// Prfen ob es ein Befehl mit Parametern ist und ob diese vorhanden sind
 				else if (eingabe.startsWith("/ignore ")	&& (tmp = eingabe.split(" ", 2)).length == 2) {
 					Node tmp_node = ChatEngine.getCE().getNodeforAlias(tmp[1]);
 					if((tmp_node!=null) && this.gui.ignoreUser(tmp_node.getUserID())){
 						info(tmp_node + " wird ignoriert!");
 						LogEngine.log(tmp_node + " wird ignoriert!", LogEngine.INFO);
 					} else {
 						warn("Ignorieren von " + tmp[1] + " nicht mglich!");
 						LogEngine.log("Ignorieren von " + tmp[1] + " nicht mglich!", LogEngine.INFO);
 					}
 				}
 				else if (eingabe.startsWith("/unignore ") && (tmp = eingabe.split(" ", 2)).length == 2){
 					Node tmp_node = ChatEngine.getCE().getNodeforAlias(tmp[1]);
 					if((tmp_node!=null) && this.gui.unignoreUser(tmp_node.getUserID())){
 						info(tmp_node + " wird nicht weiter ignoriert!");
 					} else {
 						warn(tmp[1] + "wurde nicht gefunden!");
 					}
 				}
 				else if (eingabe.startsWith("/alias ") && (tmp = eingabe.split(" ", 2)).length == 2) {
 					GUI.getGUI().changeAlias(tmp[1]);
 				}
 				else if (eingabe.startsWith("/info ") && (tmp = eingabe.split(" ", 2)).length == 2) {
 					Node nodeforalias=ChatEngine.getCE().getNodeforAlias(tmp[1]);
 					printInfo(nodeforalias);
 				}
 				else if (eingabe.startsWith("/debug ") && (tmp = eingabe.split(" ", 3)).length >= 2) {
 					ChatEngine.getCE().debug(tmp[1],(tmp.length>2)?tmp[2]:"");
 				}
 				else if (eingabe.startsWith("/w ") && (tmp = eingabe.split(" ", 3)).length == 3) {
 					Node tmp_node=ChatEngine.getCE().getNodeforAlias(tmp[1]);
 					if(tmp_node!=null)this.gui.privSend(tmp_node.getUserID(), tmp[2]);
 					else{
 						warn(tmp[1] + " wurde nicht gefunden!");
 					}
 				}
 				else if (eingabe.startsWith("/g ")	&& (tmp = eingabe.split(" ", 3)).length == 3) {
 					this.gui.groupSend(tmp[1], tmp[2]);
 				}
 				else {
 					error("Befehl nicht gltig oder vollstndig...");
 				}
 			}
 
 			// Wenn es kein Befehl ist muss es wohl eine Nachricht sein
 			else if (isPrivate()) {
 				// ggf. eingabe durch Methode filtern
 				this.gui.privSend(userID, eingabe);
 			}
 			else {
 				// ggf. eingabe durch Methode filtern
 				this.gui.groupSend(gruppe, eingabe);
 						}
 		// In jedem Fall wird das Eingabefeld gelscht
 		this.eingabeFeld.setText("");
 		}
 	}
 
 
 	public void printInfo(Node nodeforalias) {
 		if (nodeforalias!=null) {
 			Map<String, String> tmp_data = nodeforalias.getData();
 			GUI.getGUI().info("Infos for User : " + nodeforalias.getAlias(), null,0);
 			for (String x : tmp_data.keySet()) {
 				GUI.getGUI().info(x.toUpperCase() + "\t: " + tmp_data.get(x), null,1);
 			}
 			GUI.getGUI().info("----------------------------------------------------", null,0);
 		}
 	}
 
 	public void update(Observable sourceChannel, Object msg) {
 		if(GUI.getGUI().getTabbedPane().indexOfComponent(this)!=GUI.getGUI().getTabbedPane().getSelectedIndex()){
 			this.myTab.startBlink();
 		}
 		MSG tmpMSG = (MSG) msg;
 		gui.msgToTray(tmpMSG);
 		this.putMSG(tmpMSG);
 		LogEngine.log(this,"ausgabe",tmpMSG);
 	}
 	
 	/**
 	 * @param msg
 	 */
 	public void putMSG(MSG msg){
 		this.printMSG(msg);
 	}
 	/**
 	 * Methode zur Benachrichtigung des Benutzers ber das Textausgabefeld
 	 * (msgTextArea), gleichzeitig wird die LogEngine informiert.
 	 * 
 	 * @param reason
 	 */
 	private void printMSG(MSG msg) {
 		String color = "black";
 		Node sender = ChatEngine.getCE().getNodeForNID(msg.getSender());
 		String senderalias = (sender!=null)? sender.getAlias():"unknown";
 		
 		switch(msg.getTyp()){
 		
 		case SYSTEM:
 			if(msg.getCode() == MSGCode.CW_INFO_TEXT){
 				color = "#05405E";
 			} else {
 				color = "red";
 			}
 			try {
 				htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(),"<font color='" + color + "'>System: " + (String) msg.getData() + "</font>", 0, 0, null);
 			} catch (BadLocationException | IOException e) {
 				LogEngine.log(e);
 			}
 			break;
 		case GROUP:
 			if(msg.getSender() == ChatEngine.getCE().getMyNodeID()){
 				color = "#FF8512";
 			} else {
 				color = "#0970A4";
 			}
 			try {
 				htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "<font color='" + color + "'>" + senderalias +": </font><font color='black'>" + (String) msg.getData() + "</font>", 0, 0, null);
 			} catch (BadLocationException | IOException e) {
 				LogEngine.log(e);
 			}
 			break;
 		case PRIVATE:
 			if(msg.getSender() == ChatEngine.getCE().getMyNodeID()){
 				color = "#FF8512";
 			} else {
 				color = "#19A6F1";
 			}
 			try {
 				htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "<font color='" + color + "'>" + senderalias + ": </font><font color='black'>" + (String) msg.getData() + "</font>", 0, 0, null);
 			} catch (BadLocationException | IOException e) {
 				LogEngine.log(e);
 			}
 			break;
 			default:
 		
 		}
 		msgTextPane.setCaretPosition(htmlDoc.getLength());
 		//LogEngine.log(this,"printing",msg);
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj!=null) {
 			if(gruppe!=null&& gruppe.equals(obj))return true;
 			if(userID!=null&&userID.equals(obj))return true;
 			if(obj instanceof ChatWindow) {
 				ChatWindow other = (ChatWindow) obj;
 				if (other.isPrivCW != isPrivCW)return false;
 				if (other.gruppe != gruppe)return false;
 				if (other.userID != userID)return false;
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private final class DropTargetListenerImplementation implements DropTargetListener {
 		public void dropActionChanged(DropTargetDragEvent dtde) {
 		}
 		public void drop(DropTargetDropEvent event) {
 				 event.acceptDrop(DnDConstants.ACTION_COPY);
 			            try {
 			            	if(isPrivCW){
 						List<File> files = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
 						if(files.size()==1) {
 							gui.sendFile(files.get(0), userID);
 						}
 						else warn("Only single files can be transfered");
 						}
 			            	else warn("You dropped some files into a GroupChat.... Don't do that!");
 			            } catch (Exception e) {
 			                LogEngine.log(this,"You can only drop one File",LogEngine.ERROR);
 			            }
 			        event.dropComplete(true);
 		}
 		
 		public void dragOver(DropTargetDragEvent dtde) {
 		}
 		public void dragExit(DropTargetEvent dte) {
 		}
 		public void dragEnter(DropTargetDragEvent dtde) {
 		}
 	}
 
 
 	private final class RunnableImplementation implements Runnable {
 		@Override
 		public void run() {
 			while(true){
 				if (isPrivCW) {
 					if (gui.getNodeForUID(userID) == null) {
 						onlineState = false;
 						myTab.setOffline();
 						eingabeFeld.setEnabled(false);
 						sendenBtn.setForeground(Color.GRAY);
 						sendenBtn.setEnabled(false);
 					} else {
 						onlineState = true;
 						myTab.setOnline();
 						eingabeFeld.setEnabled(true);
 						sendenBtn.setForeground(Color.BLACK);
 						sendenBtn.setEnabled(true);
 					}
 					synchronized (ChatEngine.getCE().getUsers()) {
 						try {
 							ChatEngine.getCE().getUsers().wait();
 						} catch (InterruptedException e) {
 							LogEngine.log(e);
 						}
 					}
 				}
 			}
 		}
 	}
 
 
 	private final class MouseListenerImplementation implements MouseListener {
 		public void mouseReleased(MouseEvent e) {
 		}
 
 		public void mousePressed(MouseEvent e) {
 		}
 
 		public void mouseExited(MouseEvent e) {
 			JButton source = (JButton) e.getSource();
 			if(onlineState){
 				source.setForeground(Color.BLACK);
 			}
 		}
 
 		public void mouseEntered(MouseEvent e) {
 			JButton source = (JButton) e.getSource();
 			if(onlineState){
 				source.setForeground(new Color(255, 130, 13));
 			}
 		}
 
 		public void mouseClicked(MouseEvent e) {
 		}
 	}
 
 
 	/**
 	 * KeyListener fr Nachrichtenhistorie ggf. fr andere Dinge verwendbar
 	 */
 	private class History implements KeyListener{
 
 			private ArrayList<String> eingabeHistorie;
 			private int eingabeAktuell;
 			
 			public History(JTextField target) {
 				eingabeHistorie=new ArrayList<String>();
 				eingabeAktuell=0;
 				target.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 					add(((JTextField)e.getSource()).getText());	
 					}
 				});
 				target.addKeyListener(this);
 			}
 
 			public void keyTyped(KeyEvent arg0) {
 			}
 
 			public void add(String eingabe) {
 				eingabeHistorie.add(eingabe);
 				eingabeAktuell = eingabeHistorie.size();
 			}
 
 			public void keyReleased(KeyEvent arg0) {
 			}
 
 			public void keyPressed(KeyEvent arg0) {
 				JTextField tmp = (JTextField) arg0.getSource();
 				if (arg0.getKeyCode() == 38 && eingabeAktuell > 0) {
 					eingabeAktuell--;
 					tmp.setText(eingabeHistorie.get(eingabeAktuell));
 				} else if (arg0.getKeyCode() == 40 && eingabeAktuell < eingabeHistorie.size()) {
 					eingabeAktuell++;
 					if (eingabeAktuell < eingabeHistorie.size()) {
 						tmp.setText(eingabeHistorie.get(eingabeAktuell));
 					} else
 						tmp.setText("");
 				} else {
 
 				}
 		}
 	}
 }

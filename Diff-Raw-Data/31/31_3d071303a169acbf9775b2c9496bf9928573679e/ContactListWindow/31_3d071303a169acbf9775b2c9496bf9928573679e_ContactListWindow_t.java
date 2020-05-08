 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 
 import client.Client;
 import controller.ContactListController;
 import controller.Controller;
 
 /**
  * Displays a list of all connected contacts. The user may choose another user to discuss with,
  * start a group discussion or disconnect itself from the server.
  * @author etudiant
  * @see ChatPanel
  */
 public class ContactListWindow extends JPanel implements ActionListener, MouseListener, View {
 	
 	public static final int DEFAULT_WIDTH = 300;
 	public static final int DEFAULT_HEIGHT = 400;
 	public static final int DEFAULT_LOCATION_X = 600;
 	public static final int DEFAULT_LOCATION_Y = 200;
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1654591401132479730L;
 	
 	private JButton disconnectButton = new JButton("Disconnect");
 	private JButton multiChatButton = new JButton("Multi-Chat");
 	private JScrollPane scrollPane = new JScrollPane();
 	private JList loginList = new JList();
 	private List<String> logins = new Vector<String>();
 	private JFrame cadre;
 	private Map<String, ChatPanel> discMap = new HashMap<String, ChatPanel>();
 
 	private ContactListController controller;
 	private Map<Vector<String>, ChatPanel>  mapListChat = new HashMap<Vector<String>, ChatPanel>();
 	
 	public void setLogins(List<String> logins) {
 		this.logins = logins;
 	}
 	
 	/**
 	 * Refreshes the list of connected clients.
 	 * @param listeClient
 	 * @param client
 	 * @throws IOException
 	 */
 	public void refresh(List<String> listeClient, Client client) throws IOException {
 		logins.clear();
 		logins = listeClient;
 		listeClient.remove(client.getLogin());
 		controller.getClient().reconnectedClients();
 		loginList.removeAll();
 		loginList.setListData(listeClient.toArray());
 		cadre.validate();
 	}
 	
 	/**
 	 * Refreshes the list of connected clients (alternate method).
 	 * @param clientLogins
 	 */
	public void refreshDisconnected(List<String> clientLogins, String toRemove) {
		clientLogins.remove(toRemove);
 		loginList.removeAll();
 		loginList.setListData(clientLogins.toArray());
 		cadre.validate();
 	}
 	
 	@Override
 	public void lancerAffichage() throws IOException
 	{
 		String login = controller.getClient().getLogin();
 		cadre = new javax.swing.JFrame(login + " - List of connected friends: ");
 		disconnectButton.addActionListener(this);
 		multiChatButton.addActionListener(this);
 		loginList.setListData(logins.toArray());
 		
 		loginList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		
 		loginList.addMouseListener(this);
 		
 		JPanel panneauListeContact = new JPanel();
 		panneauListeContact.setLayout(new BorderLayout());
 		panneauListeContact.add(loginList, BorderLayout.NORTH);
 		
 		scrollPane.getViewport().setView(loginList);
 		
 		panneauListeContact.add(scrollPane, BorderLayout.CENTER);
 		panneauListeContact.setBorder(new EmptyBorder(5,5,10,15));
 		
 		JPanel panneauAction = new JPanel();
 		panneauAction.setLayout(new BorderLayout());
 		panneauAction.add(multiChatButton);
 		panneauAction.setBorder(new EmptyBorder(0,30,10,30));
 		
 		JPanel panneauPrincipal = new JPanel();
 		panneauPrincipal.setLayout(new BorderLayout());
 		panneauPrincipal.add(panneauListeContact, BorderLayout.CENTER);
 		panneauPrincipal.add(panneauAction, BorderLayout.SOUTH);
 		panneauListeContact.setBorder(new EmptyBorder(10,10,10,10));
 		
 		cadre.setContentPane(panneauPrincipal);
 		cadre.setLocation(DEFAULT_LOCATION_X, DEFAULT_LOCATION_Y);
 		cadre.pack();
 		cadre.setResizable(false);
 		cadre.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
 		cadre.setVisible(true);
 		cadre.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 		cadre.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				disconnect();
 				System.exit(0);
 			}
 		});
 		
 		panneauPrincipal.setBorder(new LineBorder(Color.darkGray, 5));
 		panneauAction.setBorder(new LineBorder(Color.darkGray, 5));
 		panneauAction.setBackground(Color.darkGray);
 		panneauListeContact.setBorder(new LineBorder(Color.darkGray, 5));
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == disconnectButton)
 		{
 			disconnect();
 		} else if (e.getSource() == multiChatButton) {
 			if(logins.size()>1) {
 				SelectMultiContactWindow smlw = new SelectMultiContactWindow(logins, controller, mapListChat);
 				try {
 					smlw.lancerAffichage();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 			} else {
 				JOptionPane.showMessageDialog(null, "There must be two or more connected users", "Action  impossible", JOptionPane.INFORMATION_MESSAGE);
 			}
 		}
 	}
 
 	public Map<Vector<String>, ChatPanel> getMapListChat() {
 		return mapListChat;
 	}
 
 	public void setMapListChat(Map<Vector<String>, ChatPanel> mapListChat) {
 		this.mapListChat = mapListChat;
 	}
 
 	/**
 	 * Go back to the login window.
 	 */
 	public void disconnect() {
 		for(String login : discMap.keySet()){
 			discMap.get(login).getFrame().dispose();	
 		}
 		discMap.clear();
 		for(Vector<String> listLogin : mapListChat.keySet()) {
 			mapListChat.get(listLogin).getFrame().dispose();
 		}
 		mapListChat.clear();
 		cadre.setVisible(false);
 		
 		controller.disconnect();
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		String login = (String) loginList.getSelectedValue();
 		if (login != null) {
 			if (!discMap.containsKey(login)) {
 				if (controller.getClient().isServerUp()) {
 					ChatPanel p1 = new ChatPanel(login, discMap, controller);
 					discMap.put(login, p1);
 					try {
 						p1.lancerAffichage();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				} else {
 					if(controller.getClient().getClientIps().containsKey(login)) {
 						ChatPanel p1 = new ChatPanel(login, discMap, controller);
 						discMap.put(login, p1);
 						try {
 							p1.lancerAffichage();
 						} catch (IOException e1) {
 							e1.printStackTrace();
 						}
 					} else {
 						fireErrorMessage("Server is down; user IP can't be retrieved");
 					}
 				}
 			} else {
 				ChatPanel p = discMap.get(login);
 				p.getFrame().toFront();
 			}
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {}
 
 	@Override
 	public void mouseExited(MouseEvent e) {}
 
 	@Override
 	public void mousePressed(MouseEvent e) {}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {}
 
 	public void setController(Controller clc) {
 		this.controller = (ContactListController) clc;
 	}
 	
 	@Override
 	public Controller getController() {
 		return controller;
 	}
 	
 	public Map<String, ChatPanel> getDiscMap() {
 		return discMap;
 	}
 
 	public void setDiscMap(Map<String, ChatPanel> discMap) {
 		this.discMap = discMap;
 	}
 
 	public void fireErrorMessage(String message) {
 		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.INFORMATION_MESSAGE);
 	}
 
 
 }

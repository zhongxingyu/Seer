 package userInterface;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import clientServer.Client;
 import javax.swing.Box;
 import java.awt.Component;
 
 public class ClientServerUI
 {
 
 	private static JFrame frame;
 	public static Client client;
 	public static HashMap<String, String> clientList;
 	private static Set<String> keyClientList;
 	private static Vector<String> SimpleClientList;
 	private static JPanel list;
 	private static JList<String> listTest;
 
 	private static ActionListener listenerMenu;
 	private static JMenuBar menuBar;
 	private static JPanel connectionPanel;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args)
 	{
 		EventQueue.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
 					ClientServerUI window = new ClientServerUI();
 					window.frame.setVisible(true);
 				} catch (Exception e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public ClientServerUI()
 	{
		client = new Client("client3", 3003, "localhost");
 		clientList = client.getClientList();
 		keyClientList = clientList.keySet();
 		listenerMenu = new ClientServerListener();
 		initialize();
 	}
 
 	public static void refreshClient()
 	{
 		connectionPanel.removeAll();
 		keyClientList = clientList.keySet();
 		SimpleClientList = new Vector<String>();
 		for (String key : keyClientList)
 		{
 			SimpleClientList.add(clientList.get(key));
 		}
 		System.out.println("nouvelle list" + SimpleClientList);
 		listTest = new JList<String>(SimpleClientList);
 		list.add(listTest, BorderLayout.CENTER);
 		list.repaint();
 		listTest.repaint();
 		frame.validate();
 		frame.repaint();
 		//System.out.println(list);
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize()
 	{
 		frame = new JFrame("Projet msn");
 		list = new JPanel();
 		list.repaint();
 		list.setLayout(new BorderLayout());
 
 		constructMenu();
 		constructConnectionPanel();
 
 		frame.getContentPane().add(menuBar, BorderLayout.NORTH);
 		frame.getContentPane().add(connectionPanel, BorderLayout.CENTER);
 		frame.getContentPane().add(list, BorderLayout.SOUTH);
 
 		frame.setLocation(400, 300);
 		frame.setMinimumSize(new Dimension(200, 300));
 		frame.setResizable(false);
 		frame.setVisible(true);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	private void constructMenu()
 	{
 		menuBar = new JMenuBar();
 		JMenu menuPrincipal = new JMenu("Fichier");
 		JMenu menuConfiguration = new JMenu("Configuration");
 		JMenuItem profil = new JMenuItem("Profil");
 		profil.addActionListener(listenerMenu);
 		JMenuItem refresh = new JMenuItem("Rafraichir");
 		refresh.addActionListener(listenerMenu);
 		JMenuItem connection = new JMenuItem("Se connecter");
 		connection.addActionListener(listenerMenu);
 		JMenuItem unConnection = new JMenuItem("Se déconnecter");
 		unConnection.addActionListener(listenerMenu);
 		menuPrincipal.add(profil);
 		menuPrincipal.add(refresh);
 		menuPrincipal.add(connection);
 		menuPrincipal.add(unConnection);
 
 		JMenuItem adressServer = new JMenuItem("Adresse serveur");
 		adressServer.addActionListener(listenerMenu);
 		JMenuItem serverPort = new JMenuItem("Port serveur");
 		serverPort.addActionListener(listenerMenu);
 		JMenuItem UDPPort = new JMenuItem("Port UDP");
 		UDPPort.addActionListener(listenerMenu);
 		JMenuItem TCPPort = new JMenuItem("Port TCP");
 		TCPPort.addActionListener(listenerMenu);
 		menuConfiguration.add(adressServer);
 		menuConfiguration.add(serverPort);
 		menuConfiguration.add(UDPPort);
 		menuConfiguration.add(TCPPort);
 
 		menuBar.add(menuPrincipal);
 		menuBar.add(menuConfiguration);
 	}
 
 	private void constructConnectionPanel()
 	{
 		connectionPanel = new JPanel();
 
 		connectionPanel.setLayout(new BorderLayout(0, 0));
 
 		Box verticalBox = Box.createVerticalBox();
 		connectionPanel.add(verticalBox, BorderLayout.CENTER);
 
 		Component verticalStrut_1 = Box.createVerticalStrut(20);
 		verticalStrut_1.setMaximumSize(new Dimension(32767, 100));
 		verticalBox.add(verticalStrut_1);
 
 		JTextField pseudoField = new JTextField("Raphael");
 		pseudoField.setMinimumSize(new Dimension(95, 20));
 		pseudoField.setMaximumSize(new Dimension(110, 25));
 		verticalBox.add(pseudoField);
 
 		Component verticalStrut = Box.createVerticalStrut(5);
 		verticalBox.add(verticalStrut);
 		JButton connectionButton = new JButton("Se connecter");
 		connectionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
 		verticalBox.add(connectionButton);
 		connectionButton.addActionListener(listenerMenu);
 	}
 }

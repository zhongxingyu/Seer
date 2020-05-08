 import java.net.*;
 import java.io.*;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JRadioButton;
 import javax.swing.JTabbedPane;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.JFrame;
 import javax.swing.JComponent;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.JOptionPane;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.KeyEvent;
 
 import javax.media.ControllerListener;
 import javax.media.MediaLocator;
 import javax.media.Player;
 import javax.media.rtp.RTPManager;
 import javax.media.rtp.ReceiveStream;
 
 import java.util.ArrayList;
 
 public class ConferenceClient extends JFrame implements ActionListener
 {
 	ClientConnection serverConnection = null;
 	
 	ClientBroadcaster stream = null;
 	ClientReceiver receiver = null;
 	
 	private JTabbedPane tabs = null;
 	
 	private JComponent clientsPanel;
 	private JTable clientsTable;
 	public JComponent basePanel;
 
 	//table data for other clients
 	String[] columns = {"Name", "Multicast IP", "Port"};
 	String[][] rowData = {{"foo", "test", "test"}};
 
 	private String outputIP = null;
 	private int outputPort = -1;
 	
 	private String serverIP = null;
 	private int serverPort = -1;
 	
 	private boolean sending;
 
 	/* END OF TEMP TESTING CODE (LOLOLOL AGILE SCRUM PROTOTYPING ETC) */
 	
 	private ArrayList<ClientReceiver> receiverList = null;
 	
 	private ArrayList<ClientInfo> infoList = null;
 
 	public ConferenceClient (boolean isSending)
 	{
 		super("Hermes ::: " + System.getProperty("user.name"));
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		receiverList = new ArrayList<ClientReceiver>();
 		infoList = new ArrayList<ClientInfo>();
 
 		infoList.add(new ClientInfo("224.0.0.100", "finn", 9000));
 		infoList.add(new ClientInfo("224.0.0.200", "jake", 10000));
 
 		sending = isSending;
 		
 		//prompt for streaming data from user
 		{
 			JTextField ipAddressField = new JTextField();
 			JTextField portField = new JTextField();
 			final JComponent[] inputs = new JComponent[] { new JLabel("Mulicast IP"), ipAddressField, new JLabel("Multicast Port"), portField};
 			JOptionPane.showMessageDialog(null, inputs, "Please enter your multicast information.", JOptionPane.PLAIN_MESSAGE);
 			outputIP = ipAddressField.getText();
 			outputPort = Integer.parseInt(portField.getText());
 		}
 		
 		//prompt for server information
 		{
 			JTextField ipAddressField = new JTextField();
 			JTextField portField = new JTextField();
 			final JComponent[] inputs = new JComponent[] { new JLabel("Server IP"), ipAddressField, new JLabel("Server Port"), portField};
 			JOptionPane.showMessageDialog(null, inputs, "Please enter the server data", JOptionPane.PLAIN_MESSAGE);
 			serverIP = ipAddressField.getText();
 			serverPort = Integer.parseInt(portField.getText());
 		}
 		
 		try
 		{
			serverConnection = new ClientConnection(InetAddress.getByName(outputIP), InetAddress.getByName(serverIP), 8000, System.getProperty("user.name"));
 			Thread t = new Thread(serverConnection);
 			t.start();
 		}
 		catch (Exception e)
 		{
 			System.out.println("Error creating server connection");
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		this.setSize(300, 300);
 		initalize();
 	}
 	
 	private void fillRefreshTable (ArrayList<ClientInfo> clients)
 	{
 		if (clients == null)
 		{
 			return;
 		}
 
 		ArrayList<String[]> formattedRows = new ArrayList<String[]>();
 		
 		for (ClientInfo c : clients)
 		{
 			String[] row = {c.name, c.ip, Integer.toString(c.port)};
 			formattedRows.add(row);
 		}
 		
 		String[][] finalRows = {{"test", "test", "test"}};
 		finalRows = formattedRows.toArray(finalRows);
 		
 		rowData = finalRows;
 		
 		DefaultTableModel newModel = new DefaultTableModel(rowData, columns);
 		clientsTable.setModel(newModel);
 	}
 
 	public void initalize ()
 	{
 		tabs = new JTabbedPane();
 
 		basePanel = new JPanel(false);
 		basePanel.setLayout(new BorderLayout());
 		tabs.addTab("Your stream", null, basePanel, "A preview window of your multicast video feed.");
 		
 		clientsPanel = new JPanel(false);
 		clientsPanel.setLayout(new BorderLayout());
 		clientsTable = new JTable(rowData, columns)
 		{
 			public boolean isCellEditable(int row, int column)
 			{
 				return false;
 			}
 		};
 		clientsTable.addMouseListener(new MouseAdapter()
 		{
 			public void mouseClicked(MouseEvent e)
 			{
 				if (e.getClickCount() == 2)
 				{
 					JTable target = (JTable)e.getSource();
 					int row = target.getSelectedRow();
 					int column = target.getSelectedColumn();
 					
 					//System.out.println("ROW: " + clientsTable.getValueAt(row, 0));
 					
 					String[] sessions = new String[2];
 					sessions[0] = (String)clientsTable.getValueAt(row, 1) + '/' + Integer.parseInt((String)(clientsTable.getValueAt(row, 2)));
 					sessions[1] = (String)clientsTable.getValueAt(row, 1) + '/' + (Integer.parseInt((String)(clientsTable.getValueAt(row, 2))) + 2);
 					
 					int bufferSize = 350;
 			
 					receiver = new ClientReceiver(sessions, bufferSize, (String)(clientsTable.getValueAt(row, 0)));
 					if (!(receiver.initalize()))
 					{
 						System.out.println("FAILED to initialize a session");
 						System.exit(-1);
 					}
 					
 					receiverList.add(receiver);
 				}
 			}
 		});
 		clientsPanel.add(clientsTable, BorderLayout.CENTER);
 		JButton button = new JButton("refresh");
 		button.addActionListener(this);
 		button.setActionCommand("refillTable");
 		clientsPanel.add(button, BorderLayout.LINE_START);
 		tabs.addTab("Clients", null, clientsPanel, "Listing of other available clients on the network");
 
 		add(tabs);
 
 		this.setVisible(true);
 
 		stream = new ClientBroadcaster(new MediaLocator("file:samples/test-mpeg.mpg"), outputIP, outputPort, 0.5f, this);
 		stream.start();
 
 	}
 	
 	public void actionPerformed (ActionEvent e)
 	{
 		if ("refillTable".equals(e.getActionCommand()))
 		{
 			infoList = serverConnection.getParticpants();
 			fillRefreshTable(infoList);
 		}
 	}
 
 	public static void main (String[] args)
 	{
 		if ("send".equals(args[0]))
 		{
 			new ConferenceClient(true);
 		}
 		else if ("recv".equals(args[0]))
 		{
 			new ConferenceClient(false);
 		}
 	}
 }

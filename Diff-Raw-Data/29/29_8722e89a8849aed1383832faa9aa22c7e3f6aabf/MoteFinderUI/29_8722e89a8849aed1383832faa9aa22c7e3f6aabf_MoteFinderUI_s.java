 package wiitracker.ui;
 
 import info.clearthought.layout.TableLayout;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import motej.Mote;
 import motej.MoteFinder;
 import motej.MoteFinderListener;
 
 import org.apache.log4j.Logger;
 
 import wiitracker.driver.Driver;
 
 public class MoteFinderUI extends JFrame implements MoteFinderListener, ListSelectionListener {
 
 	private static final long serialVersionUID = -5102936730448673343L;
 
 	private static final String SAVED_MOTES_FILENAME = "motes.list";
 
 	private static final String START_SEARCHING = "Start Searching";
 	private static final String STOP_SEARCHING = "Stop Searching";
 
 	private final Logger log = Logger.getLogger(this.getClass());
 
 	private DefaultListModel listModel = new DefaultListModel();
 	private JList moteList;
 	private JButton addButton;
 	private JButton removeButton;
 	private JButton connectButton;
 	private JButton searchButton;
 
 	private JScrollPane moteListScrollPane;
 
 	private MoteFinder moteFinder;
 	private boolean searching = false;
 
 	public boolean validateAddress(String address) {
 		if (address.length() != 12) {
 			log.warn("Address length incorrect: " + address.length());
 			return false;
 		}
 		for (char c : address.toCharArray()) {
 			c = Character.toUpperCase(c);
 			if (!Character.isDigit(c) && c != 'A' && c != 'B' && c != 'C' && c != 'D' && c != 'E'
 					&& c != 'F') {
 				log.warn("Address contains invalid character: " + c);
 				return false;
 			}
 		}
 		return true;
 	}
 
 	protected final transient ActionListener addActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			String result = (String) JOptionPane.showInputDialog(MoteFinderUI.this,
 					"Enter the Bluetooth address of the Wiimote you wish to use.", "Add Wiimote",
 					JOptionPane.QUESTION_MESSAGE, new ImageIcon("resources/mote.gif"), null, null);
 			if (result != null) {
 				if (validateAddress(result)) {
 					listModel.addElement(result);
 					log.info("Address added manually: " + result);
 					writeSavedAddresses();
 				} else {
 					JOptionPane.showMessageDialog(MoteFinderUI.this,
 							"The address entered is not a valid Bluetooth address.",
 							"Invalid Address", JOptionPane.WARNING_MESSAGE);
 				}
 			}
 		}
 	};
 
 	protected final transient ActionListener removeActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			Object removed = moteList.getSelectedValue();
 			listModel.removeElementAt(moteList.getSelectedIndex());
 			log.info("Address removed: " + removed);
 			writeSavedAddresses();
 		}
 	};
 
 	protected final transient ActionListener searchActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			if (searching) {
 				searchButton.setText(START_SEARCHING);
 				moteFinder.stopDiscovery();
 				searching = false;
 			} else {
 				searchButton.setText(STOP_SEARCHING);
 				moteFinder.startDiscovery();
 				searching = true;
 			}
 		}
 	};
 
 	protected final transient ActionListener connectActionListener = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			final String address = moteList.getSelectedValue().toString();
 			final Mote mote;
 			try {
 				mote = new Mote(moteList.getSelectedValue().toString());
 				Driver.enableMote(mote);
 				MoteFinderUI.this.setVisible(false);
 			} catch (RuntimeException ex) {
 				JOptionPane.showMessageDialog(MoteFinderUI.this,
 						"An error occurred while connecting to the Wiimote at address " + address,
 						"Unable to Connect", JOptionPane.WARNING_MESSAGE);
 				ex.printStackTrace();
 			}
 		}
 	};
 
 	public MoteFinderUI() {
 		super();
 
 		moteList = new JList(listModel);
 		moteListScrollPane = new JScrollPane(moteList);
 
 		moteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		moteList.addListSelectionListener(this);
 
 		readSavedAddresses();
 
 		addButton = new JButton("Add by address");
 		addButton.addActionListener(addActionListener);
 		removeButton = new JButton("Remove address");
 		removeButton.addActionListener(removeActionListener);
 		connectButton = new JButton("Connect");
 		connectButton.addActionListener(connectActionListener);
 
 		searchButton = new JButton(START_SEARCHING);
 		searchButton.addActionListener(searchActionListener);
 
 		removeButton.setEnabled(false);
 		connectButton.setEnabled(false);
 
 		final Dimension d = new Dimension(640, 480);
 		this.setSize(d);
 		this.setPreferredSize(d);
 		this.setMinimumSize(d);
 		this.setMaximumSize(d);
 
 		final double[] colSize = { 10, TableLayout.FILL, 0.25, 10 };
 		final double[] rowSize = { 10, 30, 30, 30, TableLayout.FILL, 30, 10 };
 		final TableLayout layout = new TableLayout(colSize, rowSize);
 		layout.setVGap(2);
 		layout.setHGap(5);
 		this.getContentPane().setLayout(layout);
 
 		this.getContentPane().add(moteListScrollPane, "1,1,1,5");
 		this.getContentPane().add(addButton, "2,1");
 		this.getContentPane().add(removeButton, "2,2");
 		this.getContentPane().add(searchButton, "2,3");
 		this.getContentPane().add(connectButton, "2,5");
 
		moteFinder = MoteFinder.getMoteFinder();
		moteFinder.addMoteFinderListener(this);
 
 	}
 
 	protected void writeSavedAddresses() {
 		log.debug("Writing saved addresses...");
 		try {
 			FileOutputStream fout = new FileOutputStream(SAVED_MOTES_FILENAME);
 			ObjectOutputStream oos = new ObjectOutputStream(fout);
 			oos.writeObject(listModel);
 			oos.close();
 			log.debug("Addresses written");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected void readSavedAddresses() {
 		log.debug("Reading saved addresses...");
 		FileInputStream fin;
 		try {
 			fin = new FileInputStream(SAVED_MOTES_FILENAME);
 			ObjectInputStream ois = new ObjectInputStream(fin);
 			listModel = (DefaultListModel) ois.readObject();
 			ois.close();
 			moteList.setModel(listModel);
 			log.debug("Addresses read: " + listModel.toString());
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void moteFound(Mote mote) {
 		log.info("Found a Wiimote: " + mote.getBluetoothAddress());
 		if (!listModel.contains(mote.getBluetoothAddress())) {
 			listModel.addElement(mote.getBluetoothAddress());
 			writeSavedAddresses();
 		}
 		mote.disconnect();
 	}
 
 	public void valueChanged(ListSelectionEvent e) {
 		if (!e.getValueIsAdjusting()) {
 			final int index = ((JList) e.getSource()).getSelectedIndex();
 			if (index > -1) {
 				removeButton.setEnabled(true);
 				connectButton.setEnabled(true);
 			} else {
 				removeButton.setEnabled(false);
 				connectButton.setEnabled(false);
 			}
 		}
 	}
 
 }

 package frame;
 
 
 import java.awt.EventQueue;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JLabel;
 import javax.swing.JComboBox;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 
 import java.awt.Font;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Vector;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.Rectangle;
 
 public class HotspotFrame extends JFrame implements ActionListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JPanel contentPane;
 	private Vector<String> interfaces = new Vector<String>(1,1);
 	private JComboBox comboBoxIface;
 	private JButton btnActivate, btnDeactivate;
 	private String[] psCommand = {"ps", "-e"};
 	private String[] apctlStart = {"ap_ctl", "--start"};
 	private String[] apctlStop = {"ap_ctl", "--stop"};
 	List<NetworkInterface> ifaces;
 
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					HotspotFrame frame = new HotspotFrame();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public HotspotFrame() {
 		String username = System.getProperty("user.name");
 		if (!username.equals("root")) {
 			String error = "Error: Current user is not root";
 			JOptionPane.showMessageDialog(this, error);
 		}
 		setBounds(new Rectangle(0, 0, 200, 200));
 		getInterfaces();
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		
 		comboBoxIface = new JComboBox(interfaces);
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JLabel lblHotspotSettings = new JLabel("Hotspot Settings");
 		lblHotspotSettings.setFont(new Font("Dialog", Font.BOLD, 16));
 		lblHotspotSettings.setBounds(12, 12, 195, 15);
 		contentPane.add(lblHotspotSettings);
 		comboBoxIface.setFont(new Font("Dialog", Font.PLAIN, 12));
 		comboBoxIface.setBounds(12, 69, 218, 24);
 		contentPane.add(comboBoxIface);
 		
 		btnActivate = new JButton("Activate");
 		btnActivate.setBounds(12, 115, 117, 25);
 		btnActivate.setActionCommand("activate");
 		contentPane.add(btnActivate);
 		
 		btnDeactivate = new JButton("Deactivate");
 		btnDeactivate.setBounds(153, 115, 117, 25);
 		btnDeactivate.setActionCommand("deactivate");
 		btnDeactivate.setEnabled(false);
 		contentPane.add(btnDeactivate);
 
 		if (getHotspotState()) {
 			btnDeactivate.setEnabled(true);
 			btnActivate.setEnabled(false);
 		} else {
 			btnDeactivate.setEnabled(false);
 			btnActivate.setEnabled(true);
 		}
 
 		
 		btnActivate.addActionListener(this);
 		btnDeactivate.addActionListener(this);
 		
 		JLabel lblConnectionToShare = new JLabel("Connection to share:");
 		lblConnectionToShare.setBounds(12, 49, 195, 15);
 		contentPane.add(lblConnectionToShare);
 	}
 	
 	public void actionPerformed(ActionEvent e) {
         if ("deactivate".equals(e.getActionCommand())) {
         	deactivateHotspot();
         	if(getHotspotState() == true) {
         		System.out.println("error deactivating hotspot");
         		JOptionPane.showMessageDialog(this, "Error deactivating hotspot");
         	}
             btnActivate.setEnabled(true);
             btnDeactivate.setEnabled(false);
         } else {
         	if(getHotspotState() == true) {
         		System.out.println("error hotspot active");
         		JOptionPane.showMessageDialog(this, "Error hotspot already active");
         	} else {
         		String iface;
         		iface = (ifaces.get(comboBoxIface.getSelectedIndex())).getDisplayName();
         		activateHotspot(iface);
         	}
             btnActivate.setEnabled(false);
             btnDeactivate.setEnabled(true);
         }
     }
 	
 	/*
 	 * Deactivate hotspot
 	 */
 	private void deactivateHotspot() {
 		ProcessBuilder pb = new ProcessBuilder(apctlStop);
 		try {
 			Process proc = pb.start();
 			if(proc.waitFor() != 0)
 				System.out.println("Error activating hotspot");
 		} catch ( Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * Activate hotspot
 	 */
 	private void activateHotspot(String iface) {
 		List<String> startCmd = new ArrayList<String>();
 		for (String i : apctlStart) {
 			startCmd.add(i);
 		}
 		startCmd.add(iface);
 		try {
 			ProcessBuilder pb  = new ProcessBuilder(startCmd);
 			Process proc = pb.start();
 			if(proc.waitFor() != 0)
 				System.out.println("Error activating hotspot");
 		} catch ( Exception e ) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * Check to see if hostapd or dnsmasq are running
 	 * return true if hotspot is active
 	 */
 	private boolean getHotspotState() {
 		boolean hotspotState = false;
 		try {			
 			ProcessBuilder pb = new ProcessBuilder(psCommand);
 			Process proc = pb.start();
 			BufferedReader br = new BufferedReader(new InputStreamReader (proc.getInputStream()));
 			String line;
 			while ((line = br.readLine()) != null) {
 				if((line.indexOf("hostapd") != -1) || line.indexOf("dnsmasq") != -1) {
 					hotspotState = true;
 					System.out.println("hotspot active");
 					break;
 				}
 	        }
 			br.close();
 			if(proc.waitFor() != 0)
 				System.out.println("Error activating hotspot");
 		} catch ( Exception e) {
 			e.printStackTrace();
 		}
 		
 		return hotspotState;
 	}
 	
 
 	/*
 	 * Populate interfaces with all active network interfaces except wlan0 and lo
 	 */
 	private void getInterfaces() {
 
 		try {
 			Enumeration<NetworkInterface> ifacesEnum = NetworkInterface.getNetworkInterfaces();
 			ifaces = Collections.list(ifacesEnum);
			for(int i=0; i<ifaces.size(); i++) {
				NetworkInterface iface = ifaces.get(i);
 				String ifaceName = iface.getDisplayName();
				if ((ifaceName.equals("lo")) || (ifaceName.equals("wlan0"))) {
					ifaces.remove(iface);
					i--;
 					continue;
				}
 				String textAddrs = " ";
 				Enumeration<InetAddress> addrs= iface.getInetAddresses();
 				for(InetAddress addr : Collections.list(addrs)) {
 					textAddrs = textAddrs + " " + addr.getHostAddress();
 				}
 				interfaces.addElement(ifaceName + textAddrs);
 			}
 
 		} catch (SocketException e) {
 			e.printStackTrace();
 		}
 		
 	}
 }

 package AttributePanel;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumn;
 
 import All.Channel;
 import All.Constants;
 
 
 public class NodeAttributesPanel extends JPanel{
 	public JLabel nameLabel, ETH_IPLabel, ETH_HWLabel, WLS_IPLabel, WLS_HWLabel, channelsLabel, mobilityOptionLabel, topologyOptionLabel;
 	public JTextArea name, ETH_IP, ETH_HW, WLS_IP, channels;
 	public JComboBox mobilityOption, topologyOption;
 	public JButton setData;
 	JTable WLS_HW_Table;
 	JScrollPane ps;
 	
 	
 	public NodeAttributesPanel(){
 		setLayout(new FlowLayout());
 		
 		nameLabel = new JLabel("Name:");
 		name = new JTextArea("");
		name.setEnabled(false);
 		name.addKeyListener(new KeyListener() {
 		    public void keyPressed(KeyEvent e) {
 		    	if(e.getKeyCode() == KeyEvent.VK_TAB) {
 		    		if(e.getModifiers() > 0) channels.transferFocusBackward();
 		    		else ETH_IP.transferFocus();	
 		    		e.consume();
 		    	}
 		    }
 
 			@Override
 			public void keyReleased(KeyEvent e) {}
 
 			@Override
 			public void keyTyped(KeyEvent e) {}
 		});
 		name.setLineWrap(true);
 		name.setBorder(BorderFactory.createLoweredBevelBorder());
 		
 		ETH_IPLabel = new JLabel("Ethernet IP:");
 		ETH_IP = new JTextArea("");
 		ETH_IP.addKeyListener(new KeyListener() {
 		    public void keyPressed(KeyEvent e) {
 		    	if(e.getKeyCode() == KeyEvent.VK_TAB) {
 		    		if(e.getModifiers() > 0) name.transferFocusBackward();
 		    		else ETH_HW.transferFocus();	
 		    		e.consume();
 		    	}
 		    }
 
 			@Override
 			public void keyReleased(KeyEvent e) {}
 
 			@Override
 			public void keyTyped(KeyEvent e) {}
 		});
 		ETH_IP.setLineWrap(true);
 		ETH_IP.setBorder(BorderFactory.createLoweredBevelBorder());
 		
 		ETH_HWLabel = new JLabel("Ethernet hardware:");
 		ETH_HW = new JTextArea("");
 		ETH_HW.setEnabled(false);
 		ETH_HW.addKeyListener(new KeyListener() {
 		    public void keyPressed(KeyEvent e) {
 		    	if(e.getKeyCode() == KeyEvent.VK_TAB) {
 		    		if(e.getModifiers() > 0) ETH_IP.transferFocusBackward();
 		    		else WLS_HW_Table.transferFocus();	
 		    		e.consume();
 		    	}
 		    }
 
 			@Override
 			public void keyReleased(KeyEvent e) {}
 
 			@Override
 			public void keyTyped(KeyEvent e) {}
 		});
 		ETH_HW.setLineWrap(true);
 		ETH_HW.setBorder(BorderFactory.createLoweredBevelBorder());
 		
 		WLS_IPLabel = new JLabel("Wireless IPs:");
 		
 		WLS_IP = new JTextArea("", Constants.TEXT_HEIGHT, Constants.TEXT_WIDTH);
 		WLS_IP.setEnabled(false);
 		WLS_IP.addKeyListener(new KeyListener() {
 		    public void keyPressed(KeyEvent e) {
 		    	if(e.getKeyCode() == KeyEvent.VK_TAB) {
 		    		System.out.println(e.getModifiers());
 		    		if(e.getModifiers() > 0) transferFocusBackward();
 		    		else transferFocus();	
 		    		e.consume();
 		    	}
 		    }
 
 			@Override
 			public void keyReleased(KeyEvent e) {}
 
 			@Override
 			public void keyTyped(KeyEvent e) {}
 		});
 		WLS_IP.setLineWrap(true);
 		WLS_IP.setBorder(BorderFactory.createLoweredBevelBorder());
 		
 		WLS_HWLabel = new JLabel("Wireless hardware:");
 		WLS_HW_Table = new JTable();
 		WirelessInterfacesTable t = new WirelessInterfacesTable();
 		WLS_HW_Table.setModel(t);
 		WLS_HW_Table.setBorder(BorderFactory.createLoweredBevelBorder());
 		TableColumn tcol = WLS_HW_Table.getColumnModel().getColumn(0);
 		tcol.setPreferredWidth(20);
 
 		JTableHeader header = WLS_HW_Table.getTableHeader();
 		header.setUpdateTableInRealTime(true);
 		header.setReorderingAllowed(true);
 		
 		ps = new JScrollPane(WLS_HW_Table);
 		ps.setPreferredSize(new Dimension(250,100));
 		channelsLabel = new JLabel("Channels/Probabilities:");
 		channels = new JTextArea("");
 		channels.addKeyListener(new KeyListener() {
 		    public void keyPressed(KeyEvent e) {
 		    	if(e.getKeyCode() == KeyEvent.VK_TAB) {
 		    		System.out.println(e.getModifiers());
 		    		if(e.getModifiers() > 0) transferFocusBackward();
 		    		else transferFocus();	
 		    		e.consume();
 		    	}
 		    }
 
 			@Override
 			public void keyReleased(KeyEvent e) {}
 
 			@Override
 			public void keyTyped(KeyEvent e) {}
 		});
 		channels.setLineWrap(true);
 		channels.setBorder(BorderFactory.createLoweredBevelBorder());
 
 		mobilityOptionLabel = new JLabel("Mobility Option");
 		mobilityOption = new JComboBox(new String[]{"GPS", "Static"});
 		
 		topologyOptionLabel = new JLabel("Topology Option");
 		topologyOption = new JComboBox(new String[]{"Location-based", "Static"});
 	}
 	
 	protected ArrayList<String> parseSeparatedString(String input) {
 		String[] bf = input.trim().toUpperCase().split("[, ]+");
 		
 		ArrayList<String> ret = new ArrayList<String>();
 		
 		for(int i = 0; i < bf.length; i++)
 			ret.add(bf[i]);
 		
 		return ret;
 	}
 	
 	protected ArrayList<Channel> parseChannels(String input) {
 		String[] bf = input.trim().toUpperCase().split("[, ]+");
 		
 		ArrayList<Channel> ret = new ArrayList<Channel>();
 		
 		for(int i = 0; i < bf.length; i++){
 			String[] part = bf[i].split("[:]");
 			
 			int channel = Integer.parseInt(part[0]);
 			double probability = Double.parseDouble(part[1]);
 			
 			ret.add(new Channel(channel, probability));
 		}
 		
 		return ret;
 	}
 }
 

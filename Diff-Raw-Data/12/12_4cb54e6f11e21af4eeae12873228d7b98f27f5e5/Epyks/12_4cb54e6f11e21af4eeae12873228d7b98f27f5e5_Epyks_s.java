 package epyks;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import comm.Comm;
 import comm.Connection;
 import comm.Contact;
 import comm.ContactControl;
 import comm.Event;
 
 
 
 public class Epyks extends JFrame implements ContactControl {
 	
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		javax.swing.SwingUtilities.invokeLater(
 			new Runnable() {
 				public void run() {
 					new Epyks();	            
 				}
 			}
 		);
 	}
 	
 	private JPanel peersPanel;
 	private JPanel pendingPeersPanel;
 	private final Map<Connection, Peer> peers;
 	private final Map<Connection, PeerPanel> pendingPeers;
 	
 	private Comm comm;
 	
 	private Settings settings;
 	private PeerPanel user;
 	
 	
 	public Epyks() {
 		super("Epyks");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         Properties props = new Properties();
         try {
         	props.load(new FileReader("data/config"));
         } catch (FileNotFoundException fe) {
         	System.err.println("No config file!");
         } catch (IOException e) {
 			System.err.println("IOException reading config!");
 		}
         
         settings = new Settings();
         
         
         Map<Byte,Plugin> plugins = new HashMap<Byte,Plugin>();
         JTabbedPane jtp = new JTabbedPane();
         
         try {
 			Scanner plug = new Scanner(new File("plugins/plugins"));
 			ClassLoader source = this.getClass().getClassLoader();
 			
 			while (plug.hasNext()) {
 				String nextName = plug.next();
 				Plugin next;
 				
 				try {
 					next = (Plugin)source.loadClass(nextName).newInstance();
 				} catch (Exception e) {
 					System.err.println("Could not find plugin " + nextName);
 					continue;
 				}
 				
 				plugins.put(next.usage(), next);
 				jtp.add(next);
 			}
 		} catch (FileNotFoundException e) {
 			// TODO try to recreate file and show user
 			e.printStackTrace();
 		} finally {
 			plugins.put(settings.usage(), settings);
 			jtp.add(settings);
 		}
         
         
 
         jtp.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
         
         
         JTextField jaddfield = new JTextField(10);
         JButton jaddplus = new JButton("+");
         jaddplus.setMargin(new Insets(0,3,0,3));
         ActionListener add = new Adder(jaddfield);
         jaddplus.addActionListener(add);
         jaddfield.addActionListener(add);
         
         JPanel jadd = new JPanel(new BorderLayout(0,2));
         jadd.add(jaddfield,BorderLayout.CENTER);
         jadd.add(jaddplus,BorderLayout.EAST);
         jadd.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
         
         peers = new HashMap<Connection,Peer>();
         pendingPeers = new HashMap<Connection,PeerPanel>();
         
         pendingPeersPanel = new JPanel();
         pendingPeersPanel.setLayout(new BoxLayout(pendingPeersPanel,BoxLayout.Y_AXIS));
         peersPanel = new JPanel();
         peersPanel.setLayout(new BoxLayout(peersPanel,BoxLayout.Y_AXIS));
         
         JPanel peersHolder = new JPanel(new BorderLayout());
         peersHolder.add(pendingPeersPanel,BorderLayout.NORTH);
         peersHolder.add(peersPanel,BorderLayout.CENTER);        
         
         JScrollPane jscp = new JScrollPane(peersHolder,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         
        user.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4,4,4,4), jscp.getBorder()));
         
         jscp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,4,0,4), jscp.getBorder()));
         jscp.setPreferredSize(new Dimension(130,200));
         
         JPanel jleft = new JPanel(new BorderLayout());
         jleft.add(jadd,BorderLayout.NORTH);
         jleft.add(jscp,BorderLayout.CENTER);   
         jleft.add(user,BorderLayout.SOUTH);
         
         add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,jleft,jtp));
         pack();
         setVisible(true);
         
         try {
 			comm = new Comm(this, plugins, props);
 		} catch (SocketException e) {
 			System.err.println("Failed to greate Sockets");
 		}
         comm.start();
         
         for (Plugin p:plugins.values()) {
         	p.setComm(comm);
         }
 	}
 	
 	private class Settings extends Plugin {		
 		Properties props;
 		
 		String name;
 		ImageIcon pic;
 		Connection connection;
 		
 		public Settings() {
 			setName("Settings");
 			
 			props = new Properties();
 			File settings = new File("data/settings");
 			
 			try { //really java?
 				props.load(new FileReader(settings));
 			} catch (FileNotFoundException e) {
 				try {
 					settings.createNewFile();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			name = props.getProperty("name");
 			try {
 				String picname = props.getProperty("pic");
 				if (picname != null)
 					pic = new ImageIcon(ImageIO.read(new File("data/" + picname)));
 			} catch (IOException e) {
 				pic = null;
 			}
 			
 			user = new PeerPanel(Epyks.this);
 			user.makeUserPanel(pic, name);
 		}
 		
 		@Override
 		public synchronized void doEvent(Contact s, Event e) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public byte usage() {
 			return (byte)0xff;
 		}
 		
 		public void setUserName(String n) {
 			synchronized (this) {
 				name = n;
 			}
 			
 			user.setName(n);
 		}
 		
 		public void setPic(ImageIcon i) {
 			synchronized (this) {
 				pic = i;
 			}
 			
 			user.setPic(i);
 		}
 
 		public void setConnection(Connection c) {
 			synchronized (this) {
 				connection = c;
 			}
 			
 			user.message(connection.toString(), Color.BLACK);
 		}
 		
 		public synchronized void save() {
 			try {
 				props.store(new FileWriter("data/settings"), null);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void setComm(Comm c) {}
 	}
 
 	@Override
 	public Contact makeContact(Connection c, ByteBuffer b) {
 		synchronized (peers) {
 			Contact ret = peers.get(c);
 			if (ret != null)
 				return ret;
 			
 			if (!pendingPeers.containsKey(c)) {
 				final PeerPanel pending = new PeerPanel(this);
 				pending.makePendingPanel(c);
 				pendingPeers.put(c,pending);
 				
 				SwingUtilities.invokeLater(new Runnable() {
 						public void run() {
 							pendingPeersPanel.add(pending);
 							pendingPeersPanel.revalidate();
 							pendingPeersPanel.repaint();
 						}
 					});
 			}
 			return null;
 		}
 	}
 
 	@Override
 	public void status(String s) {
 		user.message(s, Color.BLACK);
 	}
 
 	@Override
 	public void error(String s) {
 		user.message(s, Color.RED);
 	}
 
 	@Override
 	public void setOwnerConnection(Connection c) {
 		settings.setConnection(c);
 	}
 	
 	public class Remover implements ActionListener {
 		Connection owner;
 		
 		public Remover(Connection c) {
 			owner = c;
 		}
 		
 		public void actionPerformed(ActionEvent arg0) {
 			Peer peer;
 			synchronized (peers) {
 				peer = peers.remove(owner);
 				if (peer != null) {
 					peersPanel.remove(peer.panel);
 					peersPanel.revalidate();
 					peersPanel.repaint();
 				} else {
 					PeerPanel pending = pendingPeers.remove(owner);
 					if (pending != null) {
 						pendingPeersPanel.remove(pending);
 						pendingPeersPanel.revalidate();
 						pendingPeersPanel.repaint();
 					}
 					return;
 				}
 			}
 			
 			comm.remove(peer);
 		}
 	}
 	
 	public class Adder implements ActionListener {
 		JTextField source;
 		
 		public Adder(JTextField s) {
 			source = s;
 		}
 		
 		public void actionPerformed(ActionEvent arg0) {
 			String ip = source.getText();
 			source.setText("");
 			
 			Peer p;
 			try {
 				Connection to = new Connection(ip,comm.DEFAULT_PORT);
 				p = new Peer(to,Epyks.this);
 			} catch (UnknownHostException e) {
 				System.err.println("Bad Address");
 				return;
 			}
 			
 			synchronized (peers) {
 				if (peers.containsKey(p.connection)) {
 					System.err.println("Duplicate Contact");
 					return;
 				} else {
 					peers.put(p.connection, p);
 					peersPanel.add(p.panel);
 					peersPanel.revalidate();
 					peersPanel.repaint();
 				}
 			}
 			
 			comm.add(p,null);
 		}
 	}
 	
 	public class Retry implements ActionListener {
 		Connection owner;
 		
 		public Retry(Connection c) {
 			owner = c;
 		}
 		
 		public void actionPerformed(ActionEvent arg0) {
 			Peer p;
 			synchronized (peers) {
 				p = peers.get(owner);
 			}
 			
 			if (p != null)
 				comm.join(p,null);
 		}
 	}
 	
 	public class Accept implements ActionListener {
 		Connection owner;
 		
 		public Accept(Connection c) {
 			owner = c;
 		}
 		
 		public void actionPerformed(ActionEvent arg0) {
 			Peer p = new Peer(owner,Epyks.this);
 			
 			synchronized (peers) {
 				PeerPanel temp = pendingPeers.remove(owner);
 				pendingPeersPanel.remove(temp);
 				pendingPeersPanel.revalidate();
 				pendingPeersPanel.repaint();
 				
 				peers.put(owner, p);
 				peersPanel.add(p.panel);
 				peersPanel.revalidate();
 				peersPanel.repaint();
 			}
 			
 			if (p != null) {
 				comm.add(p,null);
 			}
 		}
 	}
 }

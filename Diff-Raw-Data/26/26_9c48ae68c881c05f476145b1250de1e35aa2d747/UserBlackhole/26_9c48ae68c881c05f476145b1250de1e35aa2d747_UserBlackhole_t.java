 package com.bengreenier.blackhole.core;
 
 import java.awt.Color;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 
 import com.bengreenier.blackhole.util.ByteArray;
 import com.bengreenier.blackhole.util.FileIO;
 import com.bengreenier.blackhole.util.Lock;
 import com.bengreenier.blackhole.util.Marker;
 import com.bengreenier.blackhole.util.Port;
 
 /**
  * The default state
  * that all users get
  * where both a server (if exits exist)
  * and a client (to send files) run.
  * 
  * TODO This code will support os specific
  * integrations, like right click -> blackhole
  * and other usercentric stylings.
  * 
  * @author B3N
  *
  */
 public class UserBlackhole {
 
 	public static void main(String[] args) {
 		new UserBlackhole(args).start();
 	}
 
 	private String[] args;
 	private Properties prop;
 	private Lock lock;
 	private JFrame frame;
 	private DropTarget dropTarget;
 	
 	int mouse_click_X = 0;
 	int mouse_click_Y = 0;
 	
	private String ipAddress = "127.0.0.1";
 
 	public UserBlackhole(String[] args) {
 		this.args = args;
 		this.prop = new Properties();
 		this.frame = new JFrame();
 
 		//just cause
 		Thread.currentThread().setName("UserBlackhole");
 		
 		
 		//this.lock = new Lock(Port.DEFAULT+1);
 
 				//try to load properties from ./blackhole.config	
 				try {
 					FileInputStream is = new FileInputStream("blackhole.config");
 					prop.loadFromXML(is);
 					is.close();
 				} catch (FileNotFoundException e) {
 					writePropDefaults();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		
 		//if (lock.isLocked())
 		//askWindowExit();
 	}
 
 	public UserBlackhole start() {
 		frame.setIconImage(new ImageIcon("res/drawable/blackhole.png").getImage());
 		frame.setTitle("Blackhole");
 		frame.setUndecorated(true);
 		frame.setBackground(new Color(0,0,0,0));
 		frame.setBounds(Integer.parseInt(prop.getProperty("location-x")), Integer.parseInt(prop.getProperty("location-y")), 128, 128);
 		frame.setVisible(true);
		ipAddress = prop.getProperty("ip_address");
 		
 		dropTarget = new DropTarget(frame,new DropTargetListener(){
 			@Override
 			public void dragEnter(DropTargetDragEvent arg0) {
 				// TODO Auto-generated method stub
 			}
 			@Override
 			public void dragExit(DropTargetEvent arg0) {
 				// TODO Auto-generated method stub
 			}
 			@Override
 			public void dragOver(DropTargetDragEvent arg0) {
 				// TODO Auto-generated method stub
 			}
 			@SuppressWarnings("unchecked")
 			@Override
 			public void drop(DropTargetDropEvent dtde) {
 				try {
 					// Ok, get the dropped object and try to figure out what it is
 					Transferable tr = dtde.getTransferable();
 					DataFlavor[] flavors = tr.getTransferDataFlavors();
 					for (int i = 0; i < flavors.length; i++) {
 						System.out.println("Possible flavor: " + flavors[i].getMimeType());
 						// Check for file lists specifically
 						if (flavors[i].isFlavorJavaFileListType()) {
 							dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
 							System.out.println("Good Drop!");
 							// And add the list of file names to our text area
 							java.util.List<Object> list = (java.util.List<Object>)tr.getTransferData(flavors[i]);
 							filesDropped(list);
 						}
 					}
 				}catch (Exception e) {
 				}
 			}
 
 			@Override
 			public void dropActionChanged(DropTargetDragEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 		});
 		
 		
 		frame.addWindowListener(new WindowAdapter(){
 			public void windowClosing(WindowEvent e) {
 				exit();
 
 			}
 		});
 		
 		frame.addMouseMotionListener(new MouseMotionListener(){
 			
 			@Override
 			public void mouseDragged(MouseEvent arg0) {
 				if (arg0.getButton() == 0) {
 					int oX = (int)(arg0.getXOnScreen()-mouse_click_X);
 					int oY = (int)(arg0.getYOnScreen()-mouse_click_Y);
 					
 					frame.setLocation(oX, oY);
 				}
 			}
 			
 			@Override
 			public void mouseMoved(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}});
 
 		JLabel label = new JLabel(new ImageIcon("res/drawable/blackhole.png"));
 		label.setBounds(0, 0,128,128);
 		
 		final JPopupMenu rightClickMenu = new JPopupMenu();
 		JMenuItem rightClickExit = new JMenuItem("Exit");
 		rightClickExit.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				askWindowExit();
 			}});
 		JMenuItem rightClickIP = new JMenuItem("Set IP");
 		rightClickIP.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				askSetIP();
 			}});
 		rightClickMenu.add(rightClickIP);
 		rightClickMenu.add(rightClickExit);
 
 		frame.addMouseListener(new MouseListener(){
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				mouse_click_X = arg0.getX();
 				mouse_click_Y = arg0.getY();
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				if (arg0.getButton() == MouseEvent.BUTTON3)
 					rightClickMenu.show(frame, arg0.getX(),arg0.getY());
 					
 			}});
 
 		frame.add(label);
 		
 		frame.repaint();
 		return this;
 	}
 
 	public UserBlackhole exit() {
 		writePropExit();
 
 		//try to save properties to ./blackhole.config
 		try{
 			FileOutputStream os = new FileOutputStream("blackhole.config");
 			prop.storeToXML(os, null);
 
 			os.close();
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		//i don't really like this line...
 		frame.dispose();
 
 		//release the lock
 		//lock.release();
 
 		return this;
 	}
 
 	private void askWindowExit() {
 		//this results in the window calling exit()
 		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
 	}
 	
 	private void askSetIP() {
 		//this results is the window popping up a new input dialog to change the IP address
		ipAddress = (String)JOptionPane.showInputDialog(
 		                    frame,
 		                    "Enter in new IP address",
 		                    "Set IP",
 		                    JOptionPane.PLAIN_MESSAGE,
 		                    null,
 		                    null,
		                    ipAddress);
 	}
 
 	private void writePropDefaults() {
 		prop.setProperty("save-location", "true");
 		prop.setProperty("location-x", "100");
 		prop.setProperty("location-y", "100");
		prop.setProperty("ip-address", "127.0.0.1");
 	}
 
 	private void writePropExit() {
 		if (frame != null) {
 			prop.setProperty("location-x", ""+(int)frame.getLocation().getX());
 			prop.setProperty("location-y", ""+(int)frame.getLocation().getY());
 		}
		if(ipAddress != prop.getProperty("ip-address")) {
			prop.setProperty("ip-address", ipAddress);
 		}
 	}
 
 	private void filesDropped(final java.util.List<Object> list) {
 		//TODO prompt for server information  
 		final int port = Port.DEFAULT;
 
 		//open a thread to send the files.
 		new Thread(){
 			@Override
 			public void run(){
 
 				Socket socket = new Socket();
 				try {
					socket.connect(new InetSocketAddress(ipAddress,port));
 				} catch (NumberFormatException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 				try {
 					ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream());
 
 					for (Object o : list) {
 						if (o instanceof File)
 							if (((File)o).exists()){
 								System.out.println("o.tostring = "+o.toString());
 								oout.writeObject(new Marker.FileHeaderMarker(((File)o).getPath(),((File)o).getPath().replace("\\","_").replace("/", "-").replace(":", "_")));
 								oout.writeObject(new ByteArray(FileIO.getByteArray(((File)o).getPath())));
 								oout.writeObject(new Marker.GenericFooterMarker());
 							}else{
 								Logger.getLogger("com.bengreenier.blackhole").log(Level.INFO,"Dropped File doesn't exist");
 							}
 
 					}
 
 					oout.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 				try {
 					socket.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 
 }

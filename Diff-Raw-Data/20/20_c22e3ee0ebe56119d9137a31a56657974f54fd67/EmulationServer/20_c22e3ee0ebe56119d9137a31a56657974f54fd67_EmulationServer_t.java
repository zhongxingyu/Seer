 package marino39.agamepad.server;
 
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.awt.AWTException;
 import java.awt.Dimension;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import java.util.List;
 
 import marino39.agamepad.protocol.KeyDownPacket;
 import marino39.agamepad.protocol.KeyUpPacket;
 import marino39.agamepad.protocol.MouseMovePacket;
 import marino39.agamepad.protocol.Packet;
 
 public class EmulationServer {
 	
 	private List<Socket> clients = new ArrayList<Socket>();
 	private Robot diabloIIIControler = null;
 	private int displayX = 1920;
 	private int displayY = 1080;
 	private int ratio = 2;
 	private float lastX = 0;
 	private float lastY = 0;
 	private String localAddress;
 	private ServerConfiguration serverConfig;
 	
 	public EmulationServer(ServerConfiguration sc) {
 		this.serverConfig = sc;
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		Dimension scrnsize = toolkit.getScreenSize();
 		displayX = scrnsize.width; displayY = scrnsize.height;
 		
 	}
 	
 	public void start() throws IOException, AWTException {
 		diabloIIIControler = new Robot();
 		diabloIIIControler.setAutoDelay(0);
 		System.out.println(":: Creating Server Socket");
 		final ServerSocket server = new ServerSocket(serverConfig.getGamePadServerPort());
 		System.out.println(":: Waiting for Clients");
 		
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				while (true) {
 					try {
 						addClient(server.accept());
 						System.out.println(":: New Client");
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}				
 				}
 			}
 		}).start();
 		
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				while (true) {
 					synchronized (clients) {
 						for (int i = 0; i < clients.size(); i++) {
 							Socket sc = clients.get(i);
 							try {
 								InputStream is = sc.getInputStream();
 								int len = 0;
 								if ((len = is.available()) > 0) {							
 									byte[] data = new byte[len];
 									data[0] = (byte) is.read();
 									data[1] = (byte) is.read();
 									is.read(data, 2, data[1]);
 									
 									
 									switch (data[0]) {
 									
 										case Packet.OPERATION_KEY_DOWN:
 											KeyDownPacket kdp = new KeyDownPacket(data);
 											System.out.println(":: " + kdp.toString());
 											int s = InputEvent.ALT_DOWN_MASK;
 											if (kdp.getKey() == InputEvent.BUTTON1_MASK || kdp.getKey() == InputEvent.BUTTON3_MASK || kdp.getKey() == 0)
 												diabloIIIControler.mousePress(kdp.getKey());
 											else
 												diabloIIIControler.keyPress(kdp.getKey());
 										break;
 										
 										case Packet.OPERATION_KEY_UP:
 											KeyUpPacket kup = new KeyUpPacket(data);
 											System.out.println(":: " + kup.toString());						
 											if (kup.getKey() == InputEvent.BUTTON1_MASK || kup.getKey() == InputEvent.BUTTON3_MASK || kup.getKey() == 0)
 												diabloIIIControler.mouseRelease(kup.getKey());
 											else
 												diabloIIIControler.keyRelease(kup.getKey());
 										break;
 
 										case Packet.OPERATION_MOUSE_MOVE:
 											MouseMovePacket mmp = new MouseMovePacket(data);
											float destX = displayX/2 - displayX/2*mmp.getX();
											float destY = displayY/2 - displayY/2*mmp.getY();
											System.out.println("x: " + mmp.getX() + " y: " + mmp.getY());
											
											diabloIIIControler.mouseMove((int) destX, (int) destY);			
 											break;
 										
 									}
 								}
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 					
 					try {
 						Thread.sleep(1);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			
 		}).start();
 
 	}
 	
 	private void addClient(Socket s) {
 		synchronized (clients) {
 			clients.add(s);
 		}
 	}
 	
 	private void removeClient(Socket s) {
 		synchronized (clients) {
 			clients.remove(s);
 		}
 	}
 
 	public String getLocalAddress() {
 		return localAddress;
 	}
 	
 }

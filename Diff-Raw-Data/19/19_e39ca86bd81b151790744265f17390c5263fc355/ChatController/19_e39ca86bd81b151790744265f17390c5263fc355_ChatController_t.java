 /**
  * 
  * 	Copyright 2012 Vince. All rights reserved.
  * 	
  * 	Redistribution and use in source and binary forms, with or without modification, are
  * 	permitted provided that the following conditions are met:
  * 	
  * 	   1. Redistributions of source code must retain the above copyright notice, this list of
  * 	      conditions and the following disclaimer.
  * 	
  * 	   2. Redistributions in binary form must reproduce the above copyright notice, this list
  * 	      of conditions and the following disclaimer in the documentation and/or other materials
  * 	      provided with the distribution.
  * 	
  * 	THIS SOFTWARE IS PROVIDED BY Vince ``AS IS'' AND ANY EXPRESS OR IMPLIED
  * 	WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * 	FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Vince OR
  * 	CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * 	CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * 	SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * 	ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * 	NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * 	ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 	
  * 	The views and conclusions contained in the software and documentation are those of the
  * 	authors and should not be interpreted as representing official policies, either expressed
  * 	or implied, of Vince.
  */
 package de.vistahr.lanchat.controller;
 
 import java.awt.AWTException;
 import java.awt.SystemTray;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 
 import de.vistahr.lanchat.model.ChatViewData;
 import de.vistahr.lanchat.model.ChatMessage;
 import de.vistahr.lanchat.model.ChatResponse;
 import de.vistahr.lanchat.view.ChatView;
 import de.vistahr.network.Multicast;
 import de.vistahr.network.Receivable;
 import de.vistahr.network.SLCP;
 
 /**
  * Main Chatcontroller
  * @author vistahr
  */
 public class ChatController {
 	
 	private ChatViewData model;
 	private ChatView view;
 	private Multicast mcast;
 
 	// Simple LanChat Protocol verion
 	public static String SLCP_VERSION = "1";
 	
 	
 	// Multicast settings
 	public static String MULTICAST_URL = "230.0.0.1";
 	public static int MULTICAST_GROUP = 4447;
 
 	
 	public ChatController(ChatViewData m, ChatView v) {
 		model = m;
 		view  = v;
 		
 		// add Listeners
 		addListeners();
 		
 		// Set default Username
 		try {
 			model.setChatname(InetAddress.getLocalHost().getHostName());
 		} catch (UnknownHostException e) {
 			model.setChatname("none");
 		}
 		
 		// init multicast instance
 		try {
 			mcast = new Multicast(MULTICAST_URL, MULTICAST_GROUP);
 		} catch (IOException e) {
 			view.showMessageDialog(e.getMessage());
 		}
 		
 		// Receivertaskloop
 		ExecutorService exec = Executors.newSingleThreadExecutor();
 		Runnable receiverTask = new Runnable() {
 			@Override
 			public void run() {
 				try {
 					mcast.receive(new Receivable() {
 						@Override
 						public void onReceive(String data) {
 							try {
 								// Parse incoming data
 								SLCP receiver = new SLCP(SLCP_VERSION);
 								try {
 									ChatResponse resp = receiver.parse(data);
 									if(resp instanceof ChatMessage)
 										model.addEntry((ChatMessage)resp);
 									// when muted, hide tray messages
 									if(!model.isMute()) {
 										view.showTrayMessageDialog("incoming message", model.getLastEntry().getChatMessage());
 										playSound(getClass().getResource("/res/ding.wav"));
 									}
 									
 								} catch(ParseException e) {
 									// continue - invalid input
 								}
 								
 							} catch (NullPointerException e) {
 								// continue empty messaga data
 							} catch (Exception e) {
 								view.showMessageDialog(e.getMessage());
 							}
 						}
 					});
 				} catch (IOException e) {
 					view.showMessageDialog(e.getMessage());
 				}
 			}
 		};
 		exec.submit(receiverTask);
 		
 	}
 	
 
 	/**
 	 * Add viewlisteners
 	 */
 	private void addListeners() {
 		// send 
 		view.getBtnSendMsg().addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				sendMessagePressed(e);
 			}
 		});
 		// send 
 		view.getJTextfieldSendMsg().addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				sendMessagePressed(e);
 			}
 		});
 		// quit
 		view.getBtnQuit().addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				quitChatPressed(e);
 			}
 		});
 		// mute
 		view.getBtnMute().addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				mutePressed(e);
 			}
 		});
 		// chatname
		view.getJTextfieldChatname().addFocusListener(new FocusListener() {
 			@Override
			public void focusLost(FocusEvent e) {
 				changeChatname(e);
 			}
 			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
 			}
 		});
 		// window listeners
 		view.getFrame().addWindowListener(new WindowListener() {
 			@Override
 			public void windowOpened(WindowEvent e) {}
 			@Override
 			public void windowIconified(WindowEvent e) {
 				// hide
 				view.getFrame().setState(JFrame.ICONIFIED);
 				view.getFrame().setVisible(false);
 				SystemTray tray = SystemTray.getSystemTray();
 	        	try {
 					tray.add(view.getTrayIcon());
 				} catch (AWTException ex) {
 					view.showMessageDialog(ex.getMessage());
 				}
 			}
 			@Override
 			public void windowDeiconified(WindowEvent e) {}
 			@Override
 			public void windowDeactivated(WindowEvent e) {}
 			@Override
 			public void windowClosing(WindowEvent e) {
 				quitChatPressed(null);
 			}
 			@Override
 			public void windowClosed(WindowEvent e) {}
 			@Override
 			public void windowActivated(WindowEvent e) {}
 		});
 		// autoscroll
 		view.getEditorScrollPane().getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
 			@Override
 			public void adjustmentValueChanged(AdjustmentEvent e) {
 				e.getAdjustable().setValue(e.getAdjustable().getMaximum());
 			}
 		});
 	}
 	
 	/**
 	 * Key event, when chatname changed
 	 * @param e
 	 */	
	private void changeChatname(FocusEvent e) {
 		try {
 			model.setChatname(view.getTxtChatname());
 		} catch(IllegalArgumentException ex) {
 			model.setChatname("default");
 		}	
 	}
 	
 	
 	/**
 	 * Toggle action, event when mute button pressed
 	 * @param e
 	 */
 	private void mutePressed(ActionEvent e) {
 		
 			if(!model.isMute()) {
 				try {
 					model.setMute(true);
 					view.getBtnMute().setIcon(new ImageIcon(getClass().getResource(ChatView.RES_PATH + ChatView.RES_ICON_MUTE)));
 				} catch(NullPointerException ex) {
 					view.showMessageDialog("Cannot load resource " + ChatView.RES_PATH + ChatView.RES_ICON_MUTE);
 				}
 				
 			} else {
 				try {
 					model.setMute(false);
 					view.getBtnMute().setIcon(new ImageIcon(getClass().getResource(ChatView.RES_PATH + ChatView.RES_ICON_UNMUTE)));
 				} catch(NullPointerException ex) {
 					view.showMessageDialog("Cannot load resource " + ChatView.RES_PATH + ChatView.RES_ICON_UNMUTE);
 				}
 			}
 		
 	}
 	
 	
 	/**
 	 * Action event, when quit button pressed
 	 * @param e
 	 */
 	private void quitChatPressed(ActionEvent e) {
 		try {
 			// send quit message
 			ChatMessage msg = new ChatMessage(model.getChatname(), "leaved", new Date());
 			SLCP sender = new SLCP(SLCP_VERSION);
 			mcast.send(sender.generateMessage(msg));
 			mcast.closeSocket();
 			
 		} catch (IOException ex) {
 			view.showMessageDialog(ex.getMessage());
 		}
 		System.exit(1);
 	}
 
 	
 	/**
 	 * Action event, when send message pressed
 	 * @param e
 	 */
 	private void sendMessagePressed(ActionEvent e) {
 		try {
 			// fill model
 			model.setChatMessage(view.getTxtSendMsg());
 		
 			// send
 			ChatMessage msg = new ChatMessage(view.getTxtChatname(), view.getTxtSendMsg(), new Date());
 			SLCP sender = new SLCP(SLCP_VERSION);
 			mcast.send(sender.generateMessage(msg));
 			model.setChatMessage("");
 		
 		} catch(IOException ex) {
 			view.showMessageDialog(ex.getMessage());
 			
 		} catch (NullPointerException ex) {
 			view.showMessageDialog("error: modelvar not initialized");
 			
 		} catch (IllegalArgumentException ex) {
 			// no msg output for chatname & message
 		}
 	}
 	
 	
 	// TODO
 	private void playSound(URL res) {
 	}
 	
 }

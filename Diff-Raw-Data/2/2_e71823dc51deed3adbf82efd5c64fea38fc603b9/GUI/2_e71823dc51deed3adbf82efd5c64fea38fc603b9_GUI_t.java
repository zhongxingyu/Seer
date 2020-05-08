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
 
 package de.vistahr.lanchat;
 
 import java.awt.AWTException;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.SystemTray;
 import java.awt.Toolkit;
 import java.awt.TrayIcon;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 
 /**
  * GUI class handle the swing gui with all components,
  * the windows listeners and the tray support.
  * 
  * @author vistahr
  *
  */
 public class GUI {
 	
 	
 	{ // Systemlook
 		try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             e.printStackTrace();
         }
 	}
 	
 
 	// Mainframe & basics
 	private JFrame frame;
 	private TrayIcon trayIcon;
 	public static String APP_NAME = "LanChat - blabla your life";
 	
 
 	/**
 	 * Return the initialized Frame
 	 * @return JFrame object
 	 */
 	public JFrame getFrame() {
 		return this.frame;
 	}
 	
 	
 	
 	// Components
 	public JButton btnQuit 			= new JButton("leave it");
 	public JLabel lblChatname 		= new JLabel("Chatname:");
 	public JTextField txtChatname	= new JTextField(12);
 	public JEditorPane paneChatbox 	= new JEditorPane();
 	public JLabel lblSendMsg 		= new JLabel("Message:");
 	public JTextField txtSendMsg 	= new JTextField(25);
 	public JButton btnSendMsg		= new JButton("send");
 	public JButton btnMute 			= new JButton(new ImageIcon(getClass().getResource("/res/unmute.png")));
 	
 	
 	/**
 	 * Constructor set up the UI and listeners
 	 */
 	public GUI() {
 		// Frame
 		this.frame = new JFrame(APP_NAME);
 		
 		// Components settings
 		paneChatbox.setEditable(false);
 		JScrollPane editorScrollPane = new JScrollPane(paneChatbox);
 		paneChatbox.setPreferredSize(new Dimension(295, 130));
 		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 
 		// Panels
 		JPanel panelTopL = new JPanel();
 		panelTopL.add(btnQuit);
 		
 		JPanel panelTopR = new JPanel();
 		panelTopR.add(lblChatname);
 		panelTopR.add(txtChatname);
 		panelTopR.add(btnMute);
 		
 		JPanel panelTop = new JPanel(new BorderLayout());
 		panelTop.add(panelTopL, BorderLayout.LINE_START);
 		panelTop.add(panelTopR, BorderLayout.LINE_END);
 		
 		
 		JPanel panelCenter = new JPanel();
 		panelCenter.add(editorScrollPane);
 		
 		JPanel panelBottomL = new JPanel();
 		panelBottomL.add(lblSendMsg);
 		panelBottomL.add(txtSendMsg);
 		panelBottomL.add(btnSendMsg);
 		
 		JPanel panelBottomR = new JPanel();
 		btnMute.setPreferredSize(new Dimension(20,20));
 		
 		
 		
 		JPanel panelBottom = new JPanel(new BorderLayout());
 		panelBottom.add(panelBottomL, BorderLayout.LINE_START);
 		panelBottom.add(panelBottomR, BorderLayout.LINE_END);
 		
 		
 		// add Panels
 		this.frame.getContentPane().add(panelTop, BorderLayout.PAGE_START);
 		this.frame.getContentPane().add(panelCenter, BorderLayout.CENTER);
 		this.frame.getContentPane().add(panelBottom, BorderLayout.PAGE_END);
 		
 		// Frame settings
 		this.frame.setSize(335,250);
 		this.frame.setResizable(false);
 		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.pack();
 		
 		// Windows listener
 		this.frame.addWindowListener(new WindowListener() {
 			@Override
 			public void windowOpened(WindowEvent e) {}
 			@Override
 			public void windowIconified(WindowEvent e) {
 				// hide
 				getFrame().setState(JFrame.ICONIFIED);
 				getFrame().setVisible(false);
 				SystemTray tray = SystemTray.getSystemTray();
 	        	try {
 					tray.add(getTrayIcon());
 					showTrayMessageDialog(APP_NAME, "minimized to tray");
 				} catch (AWTException ex) {
 					showMessageDialog(ex.getMessage());
 				}
 			}
 			@Override
 			public void windowDeiconified(WindowEvent e) {}
 			@Override
 			public void windowDeactivated(WindowEvent e) {}
 			@Override
 			public void windowClosing(WindowEvent e) {}
 			@Override
 			public void windowClosed(WindowEvent e) {}
 			@Override
 			public void windowActivated(WindowEvent e) {}
 		});
 		
 	}
 	
 	/**
 	 * Creates an warning dialog box
 	 * @param message
 	 * 			Message that will shown in the Dialogbox
 	 */
 	public void showMessageDialog(String message) {
 		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.WARNING_MESSAGE);
 	}
 	
 	
 	/**
 	 * Creates an trayicon dialog
 	 * @param header
 	 * 			message will shown in tray header
 	 * @param message
 	 * 			tray message
 	 */
 	public void showTrayMessageDialog(String header, String message) {
 		getTrayIcon().displayMessage(header, message, TrayIcon.MessageType.INFO);
 	}	
 	
 	
 	/**
 	 * Initialize and set up the trayicon
 	 * @return TrayIcon object
 	 */
 	private TrayIcon getTrayIcon() {
 		
 		if (SystemTray.isSupported()) {
 			
 			if(this.trayIcon == null) {
 			    Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/res/chat.png"));
 			    this.trayIcon = new TrayIcon(icon, APP_NAME);
 			    trayIcon.setImageAutoSize(true);
 			    trayIcon.addMouseListener(new MouseListener() {
 			        public void mouseClicked(MouseEvent e) {
 			        	// Get back
 			        	getFrame().setVisible(true);
 			        	getFrame().setState(JFrame.NORMAL);
 			        	SystemTray.getSystemTray().remove(getTrayIcon());
 			        }
 					@Override
 					public void mousePressed(MouseEvent e) {}
 					@Override
 					public void mouseReleased(MouseEvent e) {}
 					@Override
 					public void mouseEntered(MouseEvent e) {}
 					@Override
 					public void mouseExited(MouseEvent e) {}
 			    });
 			}
 			
 		    return trayIcon;
 		}
 		
 		return null;
 	}
 
 }

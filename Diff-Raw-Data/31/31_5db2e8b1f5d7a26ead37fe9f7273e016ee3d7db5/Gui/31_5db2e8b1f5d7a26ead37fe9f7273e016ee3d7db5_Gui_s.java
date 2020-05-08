 package com.example.FileSharing;
 
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.StringTokenizer;
 
 class Gui extends JFrame implements Runnable {
 	public DefaultListModel model1;
 
 	public Gui() {
 		super();
 
 		/* Bara de meniu BEGIN */
 
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		JMenu fileMenu = new JMenu("File");
 		JMenu ViewMenu = new JMenu("View");
 		JMenu helpMenu = new JMenu("Help");
 		JMenu optiuniMenu = new JMenu("Optiuni");
 		menuBar.add(fileMenu);
 		menuBar.add(ViewMenu);
 		menuBar.add(optiuniMenu);
 		menuBar.add(helpMenu);
 		JMenuItem connectAction = new JMenuItem("Connect");
 		JMenuItem exitAction = new JMenuItem("Exit");
 		fileMenu.add(exitAction);
 		fileMenu.add(connectAction);
 
 		/* Listener for the current window */
 		addWindowListener(new WindowListener() {
 
 			@Override
 			public void windowOpened(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void windowIconified(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void windowDeiconified(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void windowDeactivated(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void windowClosing(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 				dispose();
 				System.exit(0);
 			}
 
 			@Override
 			public void windowClosed(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 				dispose();
 				System.exit(0);
 			}
 
 			@Override
 			public void windowActivated(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		final JCheckBoxMenuItem FinishedUploads = new JCheckBoxMenuItem(
 				"FinishedUploads");
 		final JCheckBoxMenuItem FinishedDownloads = new JCheckBoxMenuItem(
 				"FinishedDownloads");
 
 		ViewMenu.add(FinishedUploads);
 		ViewMenu.addSeparator();
 		ViewMenu.add(FinishedDownloads);
 		FinishedUploads.setSelected(false);
 		FinishedDownloads.setSelected(false);
 
 		JMenuItem infoAction = new JMenuItem("Despre...");
 		helpMenu.add(infoAction);
 
 		exitAction.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);// iesire la apasarea butonului exit din meniu
 			}
 		});
 
		final JFrame frameGetConnectIp = new JFrame("Conenct to server");

 		connectAction.addActionListener(new ActionListener() {

 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				JPanel getIpPanel = new JPanel();
 				JLabel labelIp = new JLabel("Introduceti IP-ul serverului:");
 				JLabel labelPort = new JLabel("Introduceti port-ul serverului:");
 				JButton connectButton = new JButton("Connect");
 
 				final JTextField textIp = new JTextField(30);
 				textIp.getText();
 
 				final JTextField textPort = new JTextField(10);
 				textPort.getText();
 
 				connectButton.addMouseListener(new MouseListener() {
 
 					@Override
 					public void mouseClicked(MouseEvent arg0) {
 						// TODO Auto-generated method stub
 						connectServerActivity(frameGetConnectIp, textIp,
 								textPort);
 					}
 
 					@Override
 					public void mouseEntered(MouseEvent arg0) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void mouseExited(MouseEvent arg0) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void mousePressed(MouseEvent arg0) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void mouseReleased(MouseEvent arg0) {
 						// TODO Auto-generated method stub
 
 					}
 
 				});
 
 				connectButton.addKeyListener(new KeyListener() {
 
 					@Override
 					public void keyTyped(KeyEvent arg0) {
 						// TODO Auto-generated method stub
 					}
 
 					@Override
 					public void keyReleased(KeyEvent arg0) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void keyPressed(KeyEvent arg0) {
 						// TODO Auto-generated method stub
 						if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
 							System.err.println("enter pressed on button");
 							connectServerActivity(frameGetConnectIp, textIp,
 									textPort);
 						}
 					}
 				});
 				
 				textPort.addKeyListener(connectButton.getKeyListeners()[0]);
 
 				getIpPanel.add(labelIp);
 				getIpPanel.add(textIp);
 				getIpPanel.add(labelPort);
 				getIpPanel.add(textPort);
 				getIpPanel.add(connectButton);
 
 				frameGetConnectIp.add(getIpPanel);
 				frameGetConnectIp.setLayout(new FlowLayout());
 				frameGetConnectIp.pack();
 				frameGetConnectIp.setVisible(true);
 
 			}
 		});
 		infoAction.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(new JFrame(),
 						"TheGunn3rs Project\n"
 								+ "Created by:\n\nStudentii de la 333CA\n"
 								+ "Constantin Serban-Radoi\n"
 								+ "Laurentiu Tuca\n"
 								+ "\nSi studentii de la 333CC\n"
 								+ "Mihaela Culcus\n" + "Rares Petrescu\n"
 								+ "\n�2013 TheGunn3rs", "Despre program",
 						JOptionPane.PLAIN_MESSAGE);// informatii
 													// despre
 													// program
 			}
 		});
 		FinishedUploads.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (FinishedUploads.isSelected()) {
 					// afisare fisiere up
 				}
 				if (!FinishedUploads.isSelected()) {
 					// oprire afisare fisiere up
 				}
 			}
 		});
 		FinishedDownloads.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (FinishedDownloads.isSelected()) {
 					// afisare fisiere down
 
 				}
 				if (!FinishedDownloads.isSelected()) {
 					// oprire afisare fisiere down
 				}
 			}
 		});
 		/* Bara de meniu END */
 
 		JPanel Mesaj = new JPanel();
 		JPanel ToConnect = new JPanel();
 		JPanel Stare = new JPanel();
 		JTextField tf = new JTextField(30);
 
 		/* PANOU CAUTARE */
 		JPanel JSearch = new JPanel();
 		JLabel LabelSearch = new JLabel("Cautare:");
 		JTextField SearchText = new JTextField(15);
 		JButton butSearch = new JButton("Search");
 
 		// butSearch.setSize(5, 3);
 
 		JSearch.add(LabelSearch);
 		JSearch.add(SearchText);
 		JSearch.add(butSearch);
 		JSearch.setLayout(new FlowLayout());
 
 		/* PANOU USER CONECT */
 		JPanel JMessageSend = new JPanel();
 		JLabel labelmes = new JLabel("Scrie mesajul:");
 		JButton SendMessage = new JButton("Send");
 		SendMessage.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				// TODO Auto-generated method stub
 				System.exit(0);
 			}
 		});
 		JTextField Message = new JTextField(15);
 		Message.getText();
 		JMessageSend.add(labelmes);
 		JMessageSend.add(Message);
 		JMessageSend.add(SendMessage);
 
 		/* PANOU REZULTATE */
 		JPanel Results = new JPanel();
 		JList lst = new JList();
 		model1 = new DefaultListModel();
 		lst.setModel(model1);
 		lst.setSize(100, 100);
 		model1.addElement("PETRESCU  Rares");
 		Results.add(lst);
 
 		/* PANOU Userlist */
 		JPanel JUser = new JPanel();
 		JList lstuser = new JList();
 		JButton refresh = new JButton("Refresh");
 		model1 = new DefaultListModel();
 		lstuser.setModel(model1);
 		lstuser.setSize(100, 100);
 		model1.addElement("PETRESCU  Rares");
 		lstuser.addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mousePressed(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				// TODO Auto-generated method stub
 				System.out.println(Main.MAXSLOTS);
 			}
 		});
 		JUser.add(lstuser);
 		JUser.add(refresh);
 
 		/* PANOU Mesaje */
 		JPanel JMesReceived = new JPanel();
 		JList lstmsg = new JList();
 		model1 = new DefaultListModel();
 		lstmsg.setModel(model1);
 		lstmsg.setSize(100, 100);
 
 		/* PANOU Jos */
 		JPanel JDown = new JPanel();
 		JButton jos = new JButton("sgfsg");
 		JDown.add(jos);
 
 		JSplitPane JUpLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT, JSearch,
 				JMessageSend);
 		JSplitPane JUpRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT, JUser,
 				JMesReceived);
 		JSplitPane JUpCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				JUpLeft, Results);
 		JSplitPane JUpAll = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				JUpCenter, JUpRight);
 		JSplitPane JFinalPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
 				JUpAll, JDown);
 
 		add(JFinalPane);
 
 		// add(JUpCenter,BorderLayout.CENTER);
 		// add(JMessageSend);
 
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 		}
 		pack();
 		setVisible(true);
 
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void connectToServer(String ip, int port) {
 		try {
 			Main.servSock = new ServerSocket(10001);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		Main.alias = "laur";
 		try {
 			Main.connectionSock = new Socket(ip, port);
 		} catch (UnknownHostException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		} catch (IOException e3) {
 			// TODO Auto-generated catch block
 			e3.printStackTrace();
 		}
 		synchronized (Main.notifier) {
 			Main.notifier.notify();
 		}
 	}
 
 	private boolean checkIpAndPort(String ip, int port) {
 		if (port < 1024 || port > 65000)
 			return false;
 
 		StringTokenizer st = new StringTokenizer(ip, ".");
 		int step = 0;
 		while (st.hasMoreTokens()) {
 			int tok = Integer.parseInt(st.nextToken());
 			if (tok < 0 || tok > 255 || step > 4)
 				return false;
 			++step;
 		}
 
 		return true;
 	}
 
 	/**
 	 * @param frameGetConnectIp
 	 * @param textIp
 	 * @param textPort
 	 */
 	private void connectServerActivity(final JFrame frameGetConnectIp,
 			final JTextField textIp, final JTextField textPort) {
 		String ip = textIp.getText();
 		int port = Integer.parseInt(textPort.getText());
 		if (checkIpAndPort(ip, port) == true) {
 			connectToServer(ip, port);
 			frameGetConnectIp.transferFocus();
 			frameGetConnectIp.dispose();
 		}
 	}
 
 }

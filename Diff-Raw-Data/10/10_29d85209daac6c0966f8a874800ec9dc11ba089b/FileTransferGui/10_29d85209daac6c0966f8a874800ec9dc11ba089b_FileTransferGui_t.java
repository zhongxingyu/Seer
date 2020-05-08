 package org.fnppl.opensdx.file_transfer.gui;
 
 /*
  * Copyright (C) 2010-2011 
  * 							fine people e.V. <opensdx@fnppl.org> 
  * 							Henning Thieß <ht@fnppl.org>
  * 
  * 							http://fnppl.org
  */
 
 /*
  * Software license
  *
  * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
  *  
  * This file is part of openSDX
  * openSDX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * openSDX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * and GNU General Public License along with openSDX.
  * If not, see <http://www.gnu.org/licenses/>.
  *      
  */
 
 /*
  * Documentation license
  * 
  * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
  * 
  * This file is part of openSDX.
  * Permission is granted to copy, distribute and/or modify this document 
  * under the terms of the GNU Free Documentation License, Version 1.3 
  * or any later version published by the Free Software Foundation; 
  * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
  * A copy of the license is included in the section entitled "GNU 
  * Free Documentation License" resp. in the file called "FDL.txt".
  * 
  */
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.TableCellRenderer;
 
 import org.fnppl.opensdx.common.Util;
 import org.fnppl.opensdx.file_transfer.CommandResponseListener;
 import org.fnppl.opensdx.file_transfer.OSDXFileTransferClient;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferDeleteCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferDownloadCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferLoginCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferMkDirCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferRenameCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferUploadCommand;
 import org.fnppl.opensdx.file_transfer.model.FileTransferAccount;
 import org.fnppl.opensdx.file_transfer.model.RemoteFile;
 import org.fnppl.opensdx.file_transfer.model.Transfer;
 import org.fnppl.opensdx.gui.DefaultMessageHandler;
 import org.fnppl.opensdx.gui.Dialogs;
 import org.fnppl.opensdx.gui.Helper;
 import org.fnppl.opensdx.gui.MessageHandler;
 import org.fnppl.opensdx.gui.helper.MyObservable;
 import org.fnppl.opensdx.gui.helper.MyObserver;
 import org.fnppl.opensdx.helper.Logger;
 import org.fnppl.opensdx.http.HTTPClient;
 import org.fnppl.opensdx.http.HTTPClientPutRequest;
 import org.fnppl.opensdx.http.HTTPClientRequest;
 import org.fnppl.opensdx.http.HTTPClientResponse;
 import org.fnppl.opensdx.security.KeyApprovingStore;
 import org.fnppl.opensdx.security.OSDXKey;
 import org.fnppl.opensdx.security.SecurityHelper;
 import org.fnppl.opensdx.xml.Document;
 import org.fnppl.opensdx.xml.Element;
 
 public class FileTransferGui extends JFrame implements MyObserver, CommandResponseListener {
 
	public static final String version = "v. 2011-10-25";
 	private Vector<FileTransferAccount> accounts = new Vector<FileTransferAccount>();
 	private Vector<FileTransferAccount> supportedAccounts = new Vector<FileTransferAccount>();
 
 	private JPanel panelNorth;
 	private JComboBox selectAccount;
 	private JButton buConnect;
 	private JButton buEdit;
 	private JButton buRemove;
 	private JButton buTest;
 	
 	private JPopupMenu popup;
 	private JMenuItem menuSendLogFile;
 	private JMenuItem menuTestConnection;
 	private JMenuItem menuCancel;
 	
 
 	private JPanel panelStatus;
 	private JLabel txtStatus;
 	private JButton buCancelAll;
 	private JProgressBar progressBar;
 	private long progressCompleteFiles = 0;
 
 	private HashMap<Long,Transfer> transfersInProgress = new HashMap<Long, Transfer>();
 
 	private DefaultComboBoxModel selectAccount_model;
 
 	private OSDXFileTransferClient client = null;
 	private JPanel panelRemote;
 	private TreeAndTablePanelOSDXClient ttpanelRemote;
 
 	private TreeAndTablePanelLocal panelLocal;
 
 
 	private TableCellRenderer leftRenderer;
 	private TableCellRenderer centerRenderer;
 	private TableCellRenderer rightRenderer;
 
 	private JPanel panelSouth;
 	private JList log;
 	private DefaultListModel log_model;
 	private File userHome = null;
 
 	public FileTransferGui() {
 		initUserHome();
 		initSettings();
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				exit();
 			}
 		});
 		buildUi();
 	}
 
 	private void initUserHome() {
 		//init user home
 		userHome =  new File(System.getProperty("user.home"));
 		File f = new File(userHome,"openSDX");
 		if (f.exists() && f.isDirectory()) userHome = f;
 		System.out.println("home directory: "+userHome.getAbsolutePath());
 	}
 
 	private void initSettings() {
 		if (userHome == null) return;
 		File f = new File(userHome,"file_transfer_settings.xml");
 		if (!f.exists()) {
 			System.out.println("Could not load settings from: "+f.getAbsolutePath());
 			return;
 		}
 		try {
 			Element root =  Document.fromFile(f).getRootElement();
 			Vector<Element> eAccounts = root.getChildren("account");
 			for (Element e : eAccounts) {
 				try {
 					FileTransferAccount a = new FileTransferAccount();
 					a.type = e.getChildText("type");
 					if (a.type.equals(a.TYPE_OSDXFILESERVER)) {
 						a.username = e.getChildText("username");
 						a.host = e.getChildText("host");
 						a.port = Integer.parseInt(e.getChildTextNN("port"));
 						a.prepath = e.getChildTextNN("prepath");
 						a.keyid = e.getChildText("keyid");
 						a.keystore_filename = e.getChildTextNN("keystore");
 						accounts.add(a);
 						supportedAccounts.add(a);
 					}
 					else if (a.type.equals(a.TYPE_FTP)) {
 						a.username = e.getChildText("username");
 						a.host = e.getChildText("host");
 						accounts.add(a);
 					}
 					else {
 						a.type += " [NOT SUPPORTED]";
 						accounts.add(a);
 					}
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	private void exit() {
 		//TODO close open connections
 
 		this.dispose();
 	}
 
 	private void updateAccounts() {
 		selectAccount_model.removeAllElements();
 		selectAccount_model.addElement("Create new account ...");
 		selectAccount_model.addElement("[separator]");
 		for (FileTransferAccount a : supportedAccounts) {
 			if (a.type.equals(a.TYPE_OSDXFILESERVER)) {
 				String keyidShort;
 				try {
 					keyidShort = a.keyid.substring(0,8)+" ... "+a.keyid.substring(51);
 				} catch (Exception e) {
 					keyidShort = a.keyid;
 				}
 				String name = a.type+" :: "+a.username+", "+keyidShort+", "+a.host;
 				selectAccount_model.addElement(name);	
 			}
 			//			else if (a.type.equals(a.TYPE_FTP)) {
 			//				selectAccount_model.addElement(a.type+" :: "+a.username+"@"+a.host);
 			//			}
 			else {
 				System.out.println("accout type not supported: "+a.type);
 			}
 		}
 		selectAccount.setModel(selectAccount_model);
 	}
 
 	private void initComponents() {
 		panelNorth = new JPanel();
 		panelNorth.setLayout(new FlowLayout(FlowLayout.LEFT));
 
 		//popup
 		popup = new JPopupMenu("Debugging");
 		popup.setBorder(new TitledBorder("Debugging"));
 		
 		menuSendLogFile = new JMenuItem("send log");
 		menuSendLogFile.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				popup.setVisible(false);
 				int answ = Dialogs.showYES_NO_Dialog("Send logfile to server", "Really send your logfile?");
 				if (answ==Dialogs.YES) {
 					sendLogFile();
 				}
 			}
 		});
 		popup.add(menuSendLogFile);
 		
 		menuTestConnection = new JMenuItem("test connection");
 		menuTestConnection.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				popup.setVisible(false);
 				int answ = Dialogs.showYES_NO_Dialog("Test connection to server", "Really test connection to simfy.finetunes.net?");
 				if (answ==Dialogs.YES) {
 					testConnection();
 				}
 			}
 		});
 		popup.add(menuTestConnection);
 		popup.addSeparator();
 		
 		menuCancel = new JMenuItem("cancel");
 		menuCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				popup.setVisible(false);
 			}
 		});
 		popup.add(menuCancel);
 		
 		panelNorth.addMouseListener(new MouseListener() {
 			public void mousePressed(MouseEvent e) {
 				if (e.getButton() == MouseEvent.BUTTON3) {
 					if (popup.isVisible()) {
 						popup.setVisible(false);
 					} else {
 						popup.setLocation(e.getXOnScreen(), e.getYOnScreen());
 						File log = Logger.getFileTransferLogger().getLogFile(); 
 						if (log==null) {
 							menuSendLogFile.setVisible(false);
 						} else {
 							menuSendLogFile.setVisible(true);
 						}
 						popup.setVisible(true);
 					}
 				}
 			}
 			public void mouseReleased(MouseEvent e) {}
 			public void mouseExited(MouseEvent e) {}
 			public void mouseEntered(MouseEvent e) {}
 			public void mouseClicked(MouseEvent e) {}
 		});
 		
 		selectAccount_model = new DefaultComboBoxModel();
 		selectAccount = new JComboBox();
 		updateAccounts();
 		selectAccount.setRenderer(new ListCellRenderer() {
 			JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
 			JLabel l = null;
 
 			public Component getListCellRendererComponent(JList list,
 					Object value, int index, boolean isSelected,
 					boolean cellHasFocus) {
 				String str = (value == null) ? "" : value.toString();
 				if (str.equals("[separator]")) {
 					return separator;
 				}
 				if (l == null) {
 					l = new JLabel();
 					l.setOpaque(true);
 					l.setBorder(new EmptyBorder(1, 1, 1, 1));
 					l.setFont(list.getFont());
 				}
 				if (isSelected) {
 					l.setBackground(list.getSelectionBackground());
 					l.setForeground(list.getSelectionForeground());
 				} else {
 					l.setBackground(list.getBackground());
 					l.setForeground(list.getForeground());
 				}
 				l.setText(str);
 				return l;
 			}
 		});
 		panelNorth.add(selectAccount);
 		buConnect = new JButton("connect");
 		buConnect.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				button_connect_clicked();
 			}
 		});
 		buEdit = new JButton("edit");
 		buEdit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				button_edit_clicked();
 			}
 		});
 		buRemove = new JButton("remove");
 		buRemove.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				button_remove_clicked();
 			}
 		});
 
 		buTest = new JButton("test");
 		buTest.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				button_test_clicked();
 			}
 		});
 		buTest.setVisible(false);
 		panelNorth.add(buConnect);
 		panelNorth.add(buEdit);
 		panelNorth.add(buRemove);
 		panelNorth.add(buTest);
 
 
 		panelLocal = new TreeAndTablePanelLocal();
 		panelLocal.setPreferredColumnWidth(1, 20);
 		panelLocal.setPreferredColumnWidth(2, 30);
 
 		leftRenderer = new TableCellRenderer() {
 			private JLabel label;
 
 			public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus,	int row, int column) {
 				if (label==null) {
 					label = new JLabel();
 					label.setOpaque(true);
 					label.setBorder(new EmptyBorder(1, 1, 1, 1));
 					label.setFont(table.getFont());
 					label.setHorizontalAlignment(SwingConstants.LEFT);
 				}
 				String str = (value == null) ? "" : value.toString();
 				if (isSelected) {
 					label.setBackground(table.getSelectionBackground());
 					label.setForeground(table.getSelectionForeground());
 				} else {
 					label.setBackground(table.getBackground());
 					label.setForeground(table.getForeground());
 				}
 				label.setText(str);
 				return label;
 			}
 		};
 		centerRenderer = new TableCellRenderer() {
 			private JLabel label;
 
 			public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus,	int row, int column) {
 				if (label==null) {
 					label = new JLabel();
 					label.setOpaque(true);
 					label.setBorder(new EmptyBorder(1, 1, 1, 1));
 					label.setFont(table.getFont());
 					label.setHorizontalAlignment(SwingConstants.CENTER);
 				}
 				String str = (value == null) ? "" : value.toString();
 				if (isSelected) {
 					label.setBackground(table.getSelectionBackground());
 					label.setForeground(table.getSelectionForeground());
 				} else {
 					label.setBackground(table.getBackground());
 					label.setForeground(table.getForeground());
 				}
 				label.setText(str);
 				return label;
 			}
 		};
 		rightRenderer = new TableCellRenderer() {
 			private JLabel label;
 
 			public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus,	int row, int column) {
 				if (label==null) {
 					label = new JLabel();
 					label.setOpaque(true);
 					label.setBorder(new EmptyBorder(1, 1, 1, 1));
 					label.setFont(table.getFont());
 					label.setHorizontalAlignment(SwingConstants.RIGHT);
 				}
 				String str = (value == null) ? "" : value.toString();
 				if (isSelected) {
 					label.setBackground(table.getSelectionBackground());
 					label.setForeground(table.getSelectionForeground());
 				} else {
 					label.setBackground(table.getBackground());
 					label.setForeground(table.getForeground());
 				}
 				label.setText(str);
 				return label;
 			}
 		};
 		panelLocal.setColumnRenderer(0, leftRenderer);
 		panelLocal.setColumnRenderer(1, centerRenderer);		
 		panelLocal.setColumnRenderer(2, rightRenderer);
 		panelLocal.addObserver(this);
 
 		panelRemote = new JPanel();
 		panelRemote.setLayout(new BorderLayout());
 
 		panelSouth = new JPanel();
 		log = new JList();
 
 		log.setCellRenderer(new ListCellRenderer() {
 			DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
 
 			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 				if (value instanceof String[] && ((String[])value).length==5) {
 					String[] values = (String[]) value;
 					JPanel p = new JPanel();
 					p.setBackground(Color.WHITE);
 					GridBagLayout gbl = new GridBagLayout();
 					p.setLayout(gbl);
 
 					GridBagConstraints gbc = new GridBagConstraints();
 
 					// upload / download
 					JLabel l = new JLabel(values[0]);
 					gbc.gridx = 0;
 					gbc.gridy = 0;
 					gbc.gridwidth = 1;
 					gbc.gridheight = 1;
 					gbc.weightx = 0.0;
 					gbc.weighty = 0.0;
 					gbc.anchor = GridBagConstraints.WEST;
 					gbc.fill = GridBagConstraints.BOTH;
 					gbc.ipadx = 0;
 					gbc.ipady = 0;
 					gbc.insets = new Insets(2,2,2,2);
 					gbl.setConstraints(l, gbc);
 					p.add(l);
 
 					JLabel from = new JLabel(values[1]);
 					gbc.gridx = 1;
 					gbc.weightx = 50.0;
 					gbl.setConstraints(from, gbc);
 					p.add(from);
 
 					JLabel to = new JLabel(values[2]);
 					gbc.gridx = 2;
 					gbc.weightx = 50.0;
 					gbl.setConstraints(to, gbc);
 					p.add(to);
 
 					JLabel size = new JLabel(values[3]);
 					size.setPreferredSize(new Dimension(90,18));
 					gbc.gridx = 3;
 					gbc.weightx = 0.0;
 					gbl.setConstraints(size, gbc);
 					p.add(size);
 
 					JLabel state = new JLabel(values[4]);
 					state.setPreferredSize(new Dimension(140,18));
 					gbc.gridx = 4;
 					gbc.weightx = 0.0;
 					gbl.setConstraints(state, gbc);
 					p.add(state);
 					if (isSelected) {
 						p.setBackground(Color.lightGray.brighter());
 					}
 					return p;
 				} else {
 					return defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 				}
 			}
 		});
 
 		log_model = new DefaultListModel();
 		log.setModel(log_model);
 
 		panelStatus = new JPanel();
 		txtStatus = new JLabel();
 		buCancelAll = new JButton("Cancel all");
 		buCancelAll.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				button_cancelAll_clicked();
 			}
 		});
 		progressBar = new JProgressBar();
 
 	}
 	
 	private void testConnection() {
 		testConnectionToFinetunes();
 	}
 
 	
 	private void sendLogFile() {
 		final Logger logger = Logger.getFileTransferLogger();
 		final File log = Logger.getFileTransferLogger().getLogFile();
 		if (log!=null && log.exists()) {	
 			Thread t = new Thread() {
 				public void run() {
 					HTTPClient httpclient = new HTTPClient(logger.getLogfileUploadHost(), logger.getLogfileUploadPort());
 					try {
 						HTTPClientResponse resp = httpclient.sendPut(new HTTPClientPutRequest(log, logger.getLogfileUploadCommand()));
 						Dialogs.showMessage("Send logging :: "+resp.status);
 						
 					} catch (Exception e) {
 						e.printStackTrace();
 						Dialogs.showMessage("Error sending logfile.\nThe logfile has been saved to\n"+log.getAbsolutePath()+"\nYou can send it by email.");
 					}		
 				}
 			};
 			t.start();
 			
 		} else {
 			Dialogs.showMessage("Logfile not found.");
 		}
 	}
 
 	private void initLayout() {
 		//panelNorth
 		//		GridBagLayout gbl = new GridBagLayout();
 		//		panelNorth.setLayout(gbl);
 		//		GridBagConstraints gbc = new GridBagConstraints();
 		//
 		//		// selectAccount
 		//		gbc.gridx = 0;
 		//		gbc.gridy = 0;
 		//		gbc.gridwidth = 1;
 		//		gbc.gridheight = 1;
 		//		gbc.weightx = 100.0;
 		//		gbc.weighty = 0.0;
 		//		gbc.anchor = GridBagConstraints.CENTER;
 		//		gbc.fill = GridBagConstraints.BOTH;
 		//		gbc.ipadx = 0;
 		//		gbc.ipady = 0;
 		//		gbc.insets = new Insets(2,2,2,2);
 		//		gbl.setConstraints(selectAccount, gbc);
 		//		panelNorth.add(selectAccount);
 		//		
 		//		//buttons
 		//		gbc.gridx = 1;
 		//		gbc.weightx = 0.0;
 		//		gbl.setConstraints(buConnect, gbc);
 		//		panelNorth.add(buConnect);
 		//		
 		//		gbc.gridx = 2;
 		//		gbl.setConstraints(buEdit, gbc);
 		//		panelNorth.add(buEdit);
 		//		
 		//		gbc.gridx = 3;
 		//		gbl.setConstraints(buRemove, gbc);
 		//		panelNorth.add(buRemove);
 
 		//status panel
 		panelStatus.setVisible(false);
 		GridBagLayout gbl = new GridBagLayout();
 		panelStatus.setLayout(gbl);
 		GridBagConstraints gbc = new GridBagConstraints();
 
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.weightx = 60.0;
 		gbc.weighty = 0.0;
 		gbc.anchor = GridBagConstraints.CENTER;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.ipadx = 0;
 		gbc.ipady = 0;
 		gbc.insets = new Insets(2,2,2,2);
 		gbl.setConstraints(txtStatus, gbc);
 		panelStatus.add(txtStatus);
 
 		//progress
 		gbc.gridx = 1;
 		gbc.weightx = 40.0;
 		gbl.setConstraints(progressBar, gbc);
 		panelStatus.add(progressBar);
 
 		gbc.gridx = 2;
 		gbc.weightx = 0.0;
 		gbl.setConstraints(buCancelAll, gbc);
 		panelStatus.add(buCancelAll);
 
 		getContentPane().setLayout(new BorderLayout());
 		getContentPane().add(panelNorth, BorderLayout.NORTH);
 		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				panelLocal, panelRemote);
 		split.setDividerLocation(500);
 		getContentPane().add(split, BorderLayout.CENTER);
 
 		panelSouth.setLayout(new BorderLayout());
 		JScrollPane scrollLog = new JScrollPane(log);
 		panelSouth.add(scrollLog, BorderLayout.CENTER);
 		scrollLog.setPreferredSize(new Dimension(200, 150));
 		panelSouth.add(panelStatus, BorderLayout.SOUTH);
 
 		getContentPane().add(panelSouth, BorderLayout.SOUTH);
 
 	}
 
 	private void buildUi() {
 		setTitle("openSDX :: FileTransfer GUI    ("+version+")");
 		setSize(1024, 768);
 		initComponents();
 		initLayout();
 		Helper.centerMe(this, null);
 	}
 
 	public static void main(String[] args) {
 		try {
 			UIManager
 			.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
 		} catch (Exception ex) {
 			System.out.println("Nimbus look & feel not available");
 		}
 		FileTransferGui gui = new FileTransferGui();
 		gui.setVisible(true);
 	}
 
 	private void button_connect_clicked() {
 		if (buConnect.getText().equals("connect")) {
 			System.out.println("connect");
 			int sel = selectAccount.getSelectedIndex()-2;
 			if (sel<0) {
 				button_edit_clicked();
 			}
 			else if (sel>=0 && sel < supportedAccounts.size()) {
 				FileTransferAccount a = supportedAccounts.get(sel);
 				System.out.println("account: "+a.type+" :: "+a.username);
 				if (a.type.equals(a.TYPE_OSDXFILESERVER)) {
 
 					//check pre-conditions:
 					if (a.username==null || a.username.length()==0) {
 						Dialogs.showMessage("Sorry, missing username in account settings");
 						return;
 					}
 					if (a.host==null || a.host.length()==0) {
 						Dialogs.showMessage("Sorry, missing host in account settings");
 						return;
 					}
 					if (a.keystore_filename==null || a.keystore_filename.length()==0) {
 						Dialogs.showMessage("Sorry, missing keystore filename in account settings");
 						return;
 					}
 					if (a.keyid==null || a.keyid.length()==0) {
 						Dialogs.showMessage("Sorry, missing key id in account settings");
 						return;
 					}
 
 					//get osdx key out of keystoreOSDXKey key = null;
 					MessageHandler mh = new DefaultMessageHandler();
 					if (a.key==null || !a.key.getKeyID().equals(a.keyid)) {
 						File fKeyStore = new File(a.keystore_filename);
 						if (fKeyStore==null || !fKeyStore.exists()) {
 							Dialogs.showMessage("Could not open KeyStore:\n"+a.keystore_filename);
 							return;
 						}
 						try {
 							KeyApprovingStore keystore = KeyApprovingStore.fromFile(fKeyStore, mh);
 							a.key = keystore.getKey(a.keyid);
 							if (!a.key.hasPrivateKey()) {
 								Dialogs.showMessage("Sorry, no private key information for key id:\n"+a.keyid+" found in keystore");
 								return;
 							}
 							if (!a.key.isPrivateKeyUnlocked()) {
 								a.key.unlockPrivateKey(mh);
 								if (!a.key.isPrivateKeyUnlocked()) {
 									Dialogs.showMessage("Connection failed: private key is locked!");
 									return;
 								}
 							}
 						} catch (Exception ex) {
 							ex.printStackTrace();
 							Dialogs.showMessage("Error while opening KeyStore");
 							return;
 						}
 						if (a.key==null) {
 							Dialogs.showMessage("Given keyid not found.");
 							return;
 						}
 					} else {
 						if (!a.key.isPrivateKeyUnlocked()) {
 							a.key.unlockPrivateKey(mh);
 							if (!a.key.isPrivateKeyUnlocked()) {
 								Dialogs.showMessage("Connection failed: private key is locked!");
 								return;
 							}
 						}
 					}
 					client = new OSDXFileTransferClient();
 					try {
 						client.addResponseListener(this);
 						client.connect(a.host, a.port, a.prepath, a.key, a.username);
 
 					} catch (Exception e) {
 						e.printStackTrace();
 						addStatus("ERROR, could not connect to "+a.username+"@"+a.host+".");
 						Dialogs.showMessage("Sorry, could not connect to given account.\nServer \""+a.host+"\" does not respond.");
 						return;
 					}
 				} 
 			}
 		} else {
 			//System.out.println("disconnect");
 			addStatus("Disconnecting remote filesystem ...");
 			ttpanelRemote.closeConnection();
 			panelRemote.removeAll();
 			this.validate();
 			this.repaint();
 			buConnect.setText("connect");
 		}
 	}
 
 	private void button_edit_clicked() {
 		int sel = selectAccount.getSelectedIndex()-2;
 		if (sel>=0 && sel < supportedAccounts.size()) {
 			FileTransferAccount a = supportedAccounts.get(sel);
 			PanelAccount pAcc = new PanelAccount();
 			pAcc.update(a);
 			int ans = JOptionPane.showConfirmDialog(null,pAcc,"Edit Account",JOptionPane.OK_CANCEL_OPTION);
 			if (ans == JOptionPane.OK_OPTION) {
				FileTransferAccount editedAccount = pAcc.getAccount();
				int indA = accounts.indexOf(a);
				accounts.set(indA, editedAccount);
				supportedAccounts.set(sel, editedAccount);
 				updateAccounts();
 				selectAccount.setSelectedIndex(sel+2);
 			}
 		} else {
 			PanelAccount pAcc = new PanelAccount();
 			FileTransferAccount a_new = new FileTransferAccount();
 			a_new.type = FileTransferAccount.TYPE_OSDXFILESERVER;
 			a_new.host = "simfy.finetunes.net";
 			a_new.port = 4221;
 			a_new.prepath = "/";
 			a_new.username = "";
 			a_new.keystore_filename = System.getProperty("user.home")+File.separator+"openSDX"+File.separator+"defaultKeyStore.xml";
 			if (!new File(a_new.keystore_filename).exists()) {
 				a_new.keystore_filename = "";
 			}
 			a_new.keyid = "";
 			pAcc.update(a_new);
 			int ans = JOptionPane.showConfirmDialog(null,pAcc,"New Account",JOptionPane.OK_CANCEL_OPTION);
 			if (ans == JOptionPane.OK_OPTION) {
 				FileTransferAccount addA = pAcc.getAccount(); 
 				accounts.add(addA);
 				supportedAccounts.add(addA);
 				updateAccounts();
 				selectAccount.setSelectedIndex(sel+2);
 			}
 		}
 		saveAccounts();
 	}
 
 	private void saveAccounts() {
		System.out.println("save accounts");
 		File f = new File(userHome,"file_transfer_settings.xml");
 		boolean save = false;
 		if (!f.exists()) {
 			int ans = Dialogs.showYES_NO_Dialog("Save Account Settings", "Do you want to save your account settings to\n"+f.getAbsolutePath()+"?");
 			if (ans == Dialogs.YES) {
 				save = true;
 			}
 		} else {
 			save = true;
 		}
 		if (save) {
 			Element e = new Element("file_transfer_settings");
 			for (FileTransferAccount a : accounts) {
 				Element ea = new Element("account");
 				if (a.type.equals(FileTransferAccount.TYPE_OSDXFILESERVER)) {
 					ea.addContent("type", a.type);
 					ea.addContent("host", a.host);
 					ea.addContent("port", ""+a.port);
 					if (a.prepath!=null && a.prepath.length()>0) {
 						ea.addContent("prepath", a.prepath);
 					}
 					ea.addContent("username", a.username);
 					ea.addContent("keystore", a.keystore_filename);
 					ea.addContent("keyid", a.keyid);
 					e.addContent(ea);
 				}
 				else if (a.type.equals(FileTransferAccount.TYPE_FTP)) {
 					ea.addContent("type", a.type);
 					ea.addContent("host", a.host);
 					ea.addContent("username", a.username);
 					e.addContent(ea);
 				}
 				else {
 					Dialogs.showMessage("unsupported account type: "+a.type);
 				}
 			}
 			try {
 				Document.buildDocument(e).writeToFile(f);
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				Dialogs.showMessage("Error writing settings to file:\n"+f.getAbsolutePath());
 			}
 		}
 	}
 
 	private void button_remove_clicked() {
 		int sel = selectAccount.getSelectedIndex()-2;
 		if (sel>=0 && sel < accounts.size()) {
 			int ans = Dialogs.showYES_NO_Dialog("Remove Account", "Are you sure you want to remove the selected account?");
 			if (ans==Dialogs.YES) {
 				accounts.remove(sel);
 				updateAccounts();
 			}
 		}
 	}
 
 	private void testConnectionToFinetunes() {
 		try {
 			final Logger log = Logger.getFileTransferLogger();
 			//log.setSysoutLogging(true);
 			
 			System.out.println("Test connection");
 			
 			//init test account
 			FileTransferAccount a = new FileTransferAccount();
 			a.host = "simfy.finetunes.net";
 			//	a.host = "localhost";
 			a.port = 4221;
 			a.prepath = "/";
 			a.username = "test";
 					
 			StringBuffer b = new StringBuffer();
 			b.append("    <keypair>\n");
 			b.append("      <identities>\n");
 			b.append("        <identity>\n");
 			b.append("          <identnum>0001</identnum>\n");
 			b.append("          <email>test@fnppl.org</email>\n");
 			b.append("          <mnemonic restricted=\"false\">testkey</mnemonic>\n");
 			b.append("          <sha256>BF:13:53:A4:42:F1:12:09:7F:B6:BE:11:20:69:7D:C3:3F:AC:73:0F:F3:1B:E2:93:21:46:EA:1D:45:E7:10:3F</sha256>\n");
 			b.append("        </identity>\n");
 			b.append("      </identities>\n");
 			b.append("      <sha1fingerprint>F0:BB:9F:32:FF:EB:92:A4:D4:85:94:CD:BB:5E:7A:D7:4C:DE:D3:78</sha1fingerprint>\n");
 			b.append("      <authoritativekeyserver>keyserver.fnppl.org</authoritativekeyserver>\n");
 			b.append("      <datapath>\n");
 			b.append("        <step1>\n");
 			b.append("          <datasource>LOCAL</datasource>\n");
 			b.append("          <datainsertdatetime>2011-09-12 08:45:25 GMT+00:00</datainsertdatetime>\n");
 			b.append("        </step1>\n");
 			b.append("      </datapath>\n");
 			b.append("      <valid_from>2011-09-12 08:45:25 GMT+00:00</valid_from>\n");
 			b.append("      <valid_until>2036-09-11 14:45:25 GMT+00:00</valid_until>\n");
 			b.append("      <usage>ONLYSIGN</usage>\n");
 			b.append("      <level>MASTER</level>\n");
 			b.append("      <parentkeyid />\n");
 			b.append("      <algo>RSA</algo>\n");
 			b.append("      <bits>3072</bits>\n");
 			b.append("      <modulus>00:A6:77:B2:1E:CD:A5:9B:2C:F1:AF:C1:C0:F3:C8:A1:7B:24:76:49:F9:59:2E:33:00:95:5D:86:1A:F0:AE:67:35:1D:64:E3:DE:CC:06:B6:74:CE:18:56:E3:D9:74:DA:83:4E:EE:1F:AA:E3:63:14:51:49:DB:24:3B:FF:55:04:38:F4:D1:F8:0A:CF:3A:84:E9:33:D5:E9:23:18:84:3A:E9:06:7E:10:07:14:8E:BC:1D:0E:8D:74:39:CA:06:F2:9D:E8:F9:22:BF:F0:ED:3F:9E:57:A6:DE:CA:46:97:09:E1:F7:B4:30:BD:15:A6:0F:DE:32:6F:D0:B0:A6:D4:F9:38:98:72:A0:5F:A6:CE:B1:34:87:AB:7C:BC:94:F0:A8:54:C3:AC:05:BF:4A:AC:D9:E8:B8:E3:DE:76:DD:70:68:89:F1:50:62:44:B7:94:1A:C4:04:DD:82:B4:D6:C9:E0:B1:98:68:49:D5:DB:F0:86:1E:CA:58:95:13:17:42:99:8C:F3:A2:4D:5D:07:2C:39:01:5D:04:C0:D9:AE:23:97:58:77:4B:32:E5:3D:D2:E5:C5:D2:48:21:58:6A:A6:D6:CF:FE:BB:A0:AD:17:87:56:7F:F1:F1:DF:BA:95:53:11:5E:2D:07:AA:90:59:A3:C4:BB:77:C1:D8:F1:47:0A:F8:9E:71:AC:C6:97:62:24:C9:BF:C1:1F:B7:E9:F8:8A:8F:28:1C:D8:3D:8F:B0:B5:7D:61:8A:30:B3:4B:5E:CA:DA:47:BB:CB:67:F9:B2:B5:AB:67:96:A8:10:36:60:07:49:11:3F:A1:D3:7A:DA:83:C7:6C:C3:A7:0B:B4:BA:0E:7B:E9:0B:EC:23:C8:FF:8C:9E:8D:7C:D8:FC:20:88:8D:5E:48:D5:A0:5E:20:0E:90:9E:3D:6D:93:29:24:C1:DA:5F:1A:16:02:5A:B8:30:11:C9:C6:8C:88:05:18:4F:58:E5:F4:46:0C:67:1E:0C:19:F5:CB:94:5B:E8:EF:F9:51:3E:C9</modulus>\n");
 			b.append("      <pubkey>\n");
 			b.append("        <exponent>01:00:01</exponent>\n");
 			b.append("      </pubkey>\n");
 			b.append("      <privkey>\n");
 			b.append("        <exponent>\n");
 			b.append("          <locked>\n");
 			b.append("            <mantraname>password: test</mantraname>\n");
 			b.append("            <algo>AES@256</algo>\n");
 			b.append("            <initvector>94:98:85:DA:CF:3A:01:92:72:02:8B:19:FD:C5:E4:D4</initvector>\n");
 			b.append("            <padding>CBC/PKCS#5</padding>\n");
 			b.append("            <bytes>B8:71:7A:AC:E0:30:2E:C3:36:BE:F3:BF:27:A7:72:4C:E9:43:D1:D9:CD:1F:8C:9E:46:D7:71:C4:F0:94:23:07:44:79:8D:CE:8F:15:08:9E:8B:07:E4:85:8E:72:3F:32:FD:26:1F:52:A7:9D:C1:0B:2F:15:C0:BD:F0:29:25:AB:F7:E3:BD:49:40:40:D7:61:56:66:AD:79:91:88:B9:3D:72:4F:B6:4B:F6:CD:99:59:19:69:B9:1F:77:CE:02:98:CF:EF:ED:B9:7D:3F:65:1C:35:A1:26:F9:0D:BE:C1:45:41:CF:6D:66:70:0D:DD:37:F7:F1:2D:E6:34:97:58:E2:D5:BB:BE:8E:60:8B:82:EF:B0:B4:3F:F6:F3:FB:A3:5B:EF:E1:DA:03:C6:5B:8D:93:19:8F:85:89:18:7E:E9:C4:34:D8:E3:57:8E:4B:4F:AF:5C:1A:15:77:04:F2:77:52:CE:07:B8:99:A6:88:0B:C8:6F:03:BC:2E:C5:A7:97:A5:79:F7:BA:C8:94:6E:1A:AB:C1:9A:E4:2D:98:38:6D:B1:9A:4B:FA:31:C3:1C:40:F3:E8:DB:34:38:36:19:28:0B:2C:54:59:09:35:A5:E0:60:49:50:B3:65:2B:A5:8C:51:A4:E6:FB:1C:6D:F5:0F:1E:74:3D:70:AB:92:95:5A:9C:84:13:AB:0D:6E:E1:3D:60:E1:D0:84:FC:03:D1:6B:DF:A7:5E:4B:69:08:CF:7B:2F:C3:08:F0:98:E9:25:02:C0:52:FA:ED:F7:36:08:08:FA:39:25:32:2D:B7:23:29:35:E0:BF:84:F2:6A:82:F1:63:CB:BE:EE:F6:E5:7F:F8:AF:A1:40:70:28:C3:C8:81:CA:B0:C8:5D:49:30:7D:DD:8D:E4:2D:5E:B9:F8:14:EA:BD:B8:09:7C:61:7F:17:6E:BC:5B:30:08:28:4D:AD:C2:9C:A9:39:CB:CD:34:0E:34:48:C5:85:A8:20:89:D2:49:2D:85:80:A9:54:FF:0F:0A:26:E2:FD:F7:5A:72:B6:AE:30:48:4A:7B:EE:45:BB:5E:89</bytes>\n");
 			b.append("          </locked>\n");
 			b.append("        </exponent>\n");
 			b.append("      </privkey>\n");
 			b.append("      <gpgkeyserverid />\n");
 			b.append("    </keypair>\n");
 
 			a.key = OSDXKey.fromElement(Document.fromString(b.toString()).getRootElement());
 			a.key.unlockPrivateKey("test");
 
 			log.logMsg("Testing connection to "+a.host+", port "+a.port+", username "+a.username+", keyid "+a.key.getKeyID());
 			
 			final JFrame status = new JFrame("Testing connection to finetunes");
 			status.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 			status.setAlwaysOnTop(true);
 			status.setResizable(false);
 			status.setSize(350, 150);
 			status.getContentPane().setLayout(new BorderLayout());
 //			JPanel south = new JPanel();
 //			JButton buClose = new JButton("close");
 //			buClose.addActionListener(new ActionListener() {
 //				public void actionPerformed(ActionEvent e) {
 //					status.dispose();
 //				}
 //			});
 //			south.add(buClose);
 //			status.getContentPane().add(south,BorderLayout.SOUTH);
 			final JLabel statusTxt = new JLabel("Trying to Login ...");
 			statusTxt.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
 			statusTxt.setHorizontalTextPosition(SwingConstants.CENTER);
 			statusTxt.setVerticalTextPosition(SwingConstants.CENTER);
 			status.getContentPane().add(statusTxt, BorderLayout.CENTER);
 			
 			status.setVisible(true);
 			Helper.centerMe(status, this);
 			
 			try {
 				client = new OSDXFileTransferClient();
 				//client.addResponseListener(this);
 				client.addResponseListener(new CommandResponseListener() {
 					private String testdateFilename = "/test_"+System.currentTimeMillis()+".data";
 					private File testFile = File.createTempFile("osdxft_testdownload", ".tmp");
 					private long startUpload = 0L;
 					private long endUpload = 0L;
 					
 					private long startDownload = 0L;
 					private long endDownload = 0L;
 					
 					public void onSuccess(OSDXFileTransferCommand command) {
 						if (command instanceof OSDXFileTransferLoginCommand) {
 							log.logMsg("Test Login successful");
 							
 							//upload 1 MB of testdata
 							byte[] data = new byte[1024*1024];
 							Arrays.fill(data, (byte)66);
 							log.logMsg("Test Upload start");
 							statusTxt.setText("<html><body>Login successful<br>Testing upload... please wait</body></html>");
 							startUpload = System.currentTimeMillis();
 							client.uploadFile(data, testdateFilename, null);
 						}
 						else if (command instanceof OSDXFileTransferUploadCommand) {
 							endUpload = System.currentTimeMillis();
 							log.logMsg("Test Upload successful, transfered 1 MB in "+(endUpload-startUpload)+" ms -> "+(1024000.0/(endUpload-startUpload)+" Kb/s"));
 							statusTxt.setText("<html><body>Upload successful<br>Testing download... please wait</body></html>");
 							//download of testdata
 							log.logMsg("Test Download start");
 							startDownload = System.currentTimeMillis();
 							if (testFile.exists()) testFile.delete();
 							client.download(testdateFilename, testFile);
 						}
 						else if (command instanceof OSDXFileTransferDownloadCommand) {
 							endDownload = System.currentTimeMillis();
 							log.logMsg("Test Download successful, transfered 1 MB in "+(endDownload-startDownload)+" ms -> "+(1024000.0/(endDownload-startDownload)+" Kb/s"));
 							
 							//delete remote test file
 							client.delete(testdateFilename);
 							
 							//close connection
 							closeConnection();
 							String msg = "Test connection to simfy.finetunes.net successful.\nUpload rate: "+Math.round(1024000.0/(endUpload-startUpload))+" Kb/s\nDownload rate: "+Math.round(1024000.0/(endDownload-startDownload))+" Kb/s\n\nDo you want to send a report?";
 							
 							int ans = Dialogs.showYES_NO_Dialog("Test connection successful.",msg);
 							if (ans==Dialogs.YES) {
 								sendLogFile();
 							}
 						}
 					}
 					public void onStatusUpdate(OSDXFileTransferCommand command, long progress, long maxProgress, String msg) {
 	
 					}
 					public void onError(OSDXFileTransferCommand command, String msg) {
 						if (command instanceof OSDXFileTransferLoginCommand) {
 							log.logMsg("Test Login failed");
 							closeConnection();
 							int ans = Dialogs.showYES_NO_Dialog("Test connection failed.","Test connection to simfy.finetunes.net could not be established.\nDo you want to send a report?");
 							if (ans==Dialogs.YES) {
 								sendLogFile();
 							}
 						}
 						else if (command instanceof OSDXFileTransferUploadCommand) {
 							log.logMsg("Test Upload failed");
 							closeConnection();
 							int ans = Dialogs.showYES_NO_Dialog("Test connection failed.","Test upload to simfy.finetunes.net failed.\nDo you want to send a report?");
 							if (ans==Dialogs.YES) {
 								sendLogFile();
 							}
 						}
 						else if (command instanceof OSDXFileTransferDownloadCommand) {
 							log.logMsg("Test Download failed");
 							closeConnection();
 							int ans = Dialogs.showYES_NO_Dialog("Test connection failed.","Test download to simfy.finetunes.net failed.\nDo you want to send a report?");
 							if (ans==Dialogs.YES) {
 								sendLogFile();
 							}
 						}
 					}
 					
 					private void closeConnection() {
 						status.dispose();
 						if (testFile.exists()) {
 							testFile.deleteOnExit();
 							testFile.delete();
 						}
 						client.closeConnection();
 						client = null;
 //						addStatus("Disconnecting remote filesystem ...");
 //						ttpanelRemote.closeConnection();
 //						panelRemote.removeAll();
 //						FileTransferGui.this.validate();
 //						FileTransferGui.this.repaint();
 //						buConnect.setText("connect");
 					}
 				});
 				client.connect(a.host, a.port, a.prepath, a.key, a.username);
 				
 				log.logMsg("Test connection established.");
 			} catch (Exception ex) {
 				addStatus("ERROR, could not connect to "+a.username+"@"+a.host+".");
 				log.logError("Test connection failed with exception");
 				log.logException(ex);
 				Dialogs.showMessage("Sorry, could not connect to "+a.username+"@"+a.host+".");
 				return;
 			}
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	private void button_test_clicked() {
 		testConnectionToFinetunes();
 	}
 
 
 	private void button_upload_clicked() {
 		System.out.println("upload");
 		Vector<File> localFiles = panelLocal.getSelectedFiles();
 		String baseDir = null;
 		if (localFiles==null || localFiles.size()==0) {
 			File dir = panelLocal.getSelectedDir();
 			if (dir!=null) {
 				localFiles = new Vector<File>();
 				try {
 					baseDir = dir.getParentFile().getCanonicalPath();
 					if (!baseDir.endsWith(File.separator)) {
 						baseDir += File.separator;
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 					baseDir = null;
 				}
 				Util.listFiles(dir, localFiles);
 			}
 			if (localFiles==null || localFiles.size()==0) {
 				Dialogs.showMessage("Please select files to upload on local side.");
 				return;
 			}
 		}
 
 
 		RemoteFile targetDirectory = ttpanelRemote.getSelectedDir();
 		if (targetDirectory==null) {
 			Dialogs.showMessage("Please select a target directory on remote side.");
 			return;
 		}
 
 		String targetDir = targetDirectory.getFilnameWithPath();
 		if (!targetDir.endsWith("/")) {
 			targetDir += "/";
 		}
 
 		//show status Bar
 		long completeProgress = 0;
 		for (File from : localFiles) {
 			completeProgress += from.length();
 		}
 		int cp = (int)completeProgress;
 		//System.out.println("complete Progress :: "+completeProgress+"  as int "+cp);
 		if (panelStatus.isVisible()) {
 			progressBar.setMaximum(progressBar.getMaximum()+cp);
 			if (txtStatus.getText().equals("Downloading files")) {
 				txtStatus.setText("Uploading / Downloading files");
 			}
 		} else {
 			txtStatus.setText("Uploading files");
 			progressCompleteFiles = 0;
 			progressBar.setMinimum(0);
 			progressBar.setMaximum(cp);
 			progressBar.setValue(0);
 			panelStatus.setVisible(true);
 		}
 
 		for (File from : localFiles) {
 
 			String filenameTo = ""+targetDir;
 			if (baseDir==null) {
 				filenameTo += from.getName();
 			} else {
 				try {
 					System.out.println("from path :: "+from.getCanonicalPath());
 					filenameTo += from.getCanonicalPath().substring(baseDir.length());
 				} catch (IOException e) {
 					filenameTo += from.getName();
 					e.printStackTrace();
 				}
 			}
 
 			//RemoteFile to = new RemoteFile(tragetDirectory.getFilnameWithPath(), from.getName(), from.length(), from.lastModified(), false);
 
 			long id = client.upload(from, filenameTo);
 			Transfer t = new Transfer();
 
 			//t.msg = "uploading "+from.getAbsolutePath()+" -> "+filenameTo+ " ("+String.format("%8dkB",(localFile.length()/1000))+")";
 			//t.msg = String.format("uploading %-30s -> %-50s   (%8dkB)", from.getName(), filenameTo, (localFile.length()/1000));
 			t.msg = new String[] {"upload",from.getName(), filenameTo,String.format("%10dkB",(from.length()/1000)), "waiting"};
 			t.pos = addStatus(t.msg);
 			t.type = "upload";
 			t.startTime = -1L;
 			transfersInProgress.put(id,t);
 		}
 	}
 
 	private void button_download_clicked() {
 		System.out.println("download");
 		final File local = panelLocal.getSelectedDir();
 		if (local==null) {
 			Dialogs.showMessage("Please select a local directory.");
 			return;
 		}
 		final Vector<RemoteFile> remote = ttpanelRemote.getSelectedFiles();
 		if (remote==null || remote.size()==0) {
 			Dialogs.showMessage("Please select files to download on remote side.");
 			return;
 		}
 
 		//show status Bar
 		long completeProgress = 0;
 		for (RemoteFile remoteFile : remote) {
 			completeProgress += remoteFile.getLength();
 		}
 		int cp = (int)completeProgress;
 		//System.out.println("complete Progress :: "+completeProgress+"  as int "+cp);
 		if (panelStatus.isVisible()) {
 			progressBar.setMaximum(progressBar.getMaximum()+cp);
 			if (txtStatus.getText().equals("Uploading files")) {
 				txtStatus.setText("Uploading / Downloading files");
 			}
 		} else {
 			txtStatus.setText("Downloading files");
 			progressCompleteFiles = 0;
 			progressBar.setMinimum(0);
 			progressBar.setMaximum(cp);
 			progressBar.setValue(0);
 			panelStatus.setVisible(true);
 		}
 
 		for (final RemoteFile remoteFile : remote) {
 			final File target = new File(local,remoteFile.getName());
 
 			//String pre = "downloading "+remoteFile.getFilnameWithPath()+" -> "+target.getAbsolutePath(); 
 
 			long id = client.download(remoteFile.getFilnameWithPath(), target);
 
 			Transfer t = new Transfer();
 			t.msg = new String[] {"download",remoteFile.getFilnameWithPath(), target.getAbsolutePath(), String.format("%10dkB",(remoteFile.getLength()/1000)), "waiting"};
 			t.pos = addStatus(t.msg);
 			t.type = "download";
 			t.startTime = -1L;
 			transfersInProgress.put(id,t);
 		}
 	}
 
 
 	private void button_cancelAll_clicked() {
 		client.cancelCommands();
 		panelStatus.setVisible(false);
 		for (int i=0;i<log_model.getSize();i++) {
 			if (log_model.get(i) instanceof String[]) {
 				String[] s = (String[])log_model.get(i);
 				if(s.length==5 && s[4].equals("waiting")) {
 					s[4] = "canceld";
 				}
 			}
 		}
 		log.repaint();
 	}
 
 	Object sync_object = new Object();
 	public int addStatus(String message) {
 		synchronized (sync_object) {
 			log_model.addElement(message);
 			int pos = log_model.size()-1;
 			log.setSelectedIndex(pos);
 			log.repaint();	
 			return pos;
 		}
 	}
 
 	public int addStatus(String[] message) {
 		synchronized (sync_object) {
 			log_model.addElement(message);
 			int pos = log_model.size()-1;
 			log.setSelectedIndex(pos);
 			log.repaint();	
 			return pos;
 		}
 	}
 
 	public void setStatus(int pos, String message) {
 		synchronized (sync_object) {
 			log_model.set(pos, message);
 			log.setSelectedIndex(pos);
 			log.ensureIndexIsVisible(pos);
 			log.repaint();
 		}
 	}
 
 	public void setStatus(int pos, String[] message) {
 		synchronized (sync_object) {
 			try {
 				log_model.set(pos, message);
 				log.setSelectedIndex(pos);
 				log.ensureIndexIsVisible(pos);
 				log.repaint();
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	public void setLastStatus(String message) {
 		synchronized (sync_object) {
 			int pos = log_model.getSize()-1;
 			log_model.set(pos, message);
 			log.setSelectedIndex(pos);
 			log.ensureIndexIsVisible(pos);
 			log.repaint();
 		}
 	}
 
 	public String getLastLogEntry() {
 		return (String)log_model.lastElement();
 	}
 
 	public void notifyChange(MyObservable changedIn) {
 		if (changedIn == ttpanelRemote) {
 			button_download_clicked();
 		} else if (changedIn == panelLocal) {
 			button_upload_clicked();
 		}
 	}
 
 
 	public void onError(OSDXFileTransferCommand command, String msg) {
 		//handle errors without command
 		if (command == null) {
 			if (msg==null) {
 				Dialogs.showMessage("Unknown error in command");
 			} else {
 				Dialogs.showMessage(msg);
 				if (msg.equals("Connection to server terminated.")) {
 					//disconnect
 					button_connect_clicked();
 				}
 			}
 			return;
 		}
 
 		//handle login error
 		if (command instanceof OSDXFileTransferLoginCommand) {
 			addStatus("ERROR, could not connect to given account");
 			if (msg==null) {
 				Dialogs.showMessage("Sorry, could not connect to given account.");
 			} else {
 				Dialogs.showMessage("Sorry, could not connect to given account.\n"+msg);
 			}
 			return;
 		}
 
 		//show Error Message
 		if (msg==null) {
 			Dialogs.showMessage("Unknown error in command : "+command.getClass().getSimpleName());
 		} else {
 			Dialogs.showMessage(msg);
 		}
 		Transfer t  = transfersInProgress.get(command.getID());
 		if (t!=null) {
 			setStatus(t.pos, t.msg+ "    ERROR "+msg);
 			transfersInProgress.remove(command.getID());
 			if (t.type.equals("download")) {
 				panelLocal.refreshView();
 			}
 			if (t.type.equals("upload")) {
 				ttpanelRemote.refreshView(true);
 			}
 		}
 	}
 
 	public void onStatusUpdate(OSDXFileTransferCommand command, long progress, long maxProgress, String msg) {
 		//System.out.println("ON STATUS UPDATE "+command.getID());
 		Transfer t  = transfersInProgress.get(command.getID());
 		if (t!=null) {
 			//calc transfer Rate 
 			String transferRate = "";
 			if (t.startTime == -1L) {
 				t.startTime = System.currentTimeMillis();
 				t.dataAtTime = progress;
 			} else {
 				long now = System.currentTimeMillis();
 				long tr = ((progress-t.dataAtTime)*1000)/((now-t.startTime)*1024); //in kB / s
 				transferRate = String.format("  (%d kB/s)", tr);
 			}
 			String proz = "";
 			if (maxProgress>0) {
 				proz = String.format("  (%d", progress*100L/maxProgress)+" %)";
 			}
 			//setStatus(t.pos, t.msg+proz+transferRate);
 			t.msg[4] = proz+transferRate;
 			setStatus(t.pos, t.msg);
 
 			if (panelStatus.isVisible()) {
 				int value = (int)(progressCompleteFiles+progress);
 				progressBar.setValue(value);
 				if (value>=progressBar.getMaximum()) {
 					panelStatus.setVisible(false);
 					progressBar.setValue(0);
 				}
 				if (progress>=maxProgress) {
 					progressCompleteFiles += maxProgress;
 				}
 			}
 		}
 
 	}
 
 	public void onSuccess(OSDXFileTransferCommand command) {
 		System.out.println("Command successful: "+command.getClass().getSimpleName());
 
 		if (command instanceof OSDXFileTransferLoginCommand) {
 			addStatus("Connection to Server established.");
 			final FileTransferGui me = this;
 			Thread t = new Thread() {
 				public void run() {
 					ttpanelRemote = new TreeAndTablePanelOSDXClient(client);
 					ttpanelRemote.addObserver(me);
 					ttpanelRemote.setPreferredColumnWidth(1, 20);
 					ttpanelRemote.setPreferredColumnWidth(2, 30);
 					ttpanelRemote.setColumnRenderer(0, leftRenderer);
 					ttpanelRemote.setColumnRenderer(1, centerRenderer);		
 					ttpanelRemote.setColumnRenderer(2, rightRenderer);
 
 					panelRemote.removeAll();
 					panelRemote.add(ttpanelRemote, BorderLayout.CENTER);
 					buConnect.setText("disconnect");
 					me.validate();
 					me.repaint();
 				}
 			};
 			t.start();
 		}
 		else if (command instanceof OSDXFileTransferMkDirCommand) {
 			addStatus("mkdir \""+((OSDXFileTransferMkDirCommand)command).absolutePathname+"\" successful.");
 			ttpanelRemote.refreshView(false);
 			//			Thread t = new Thread() {
 			//				public void run() {
 			//					ttpanelRemote.refreshView();
 			//				}
 			//			};
 			//			t.start();
 		}
 		else if (command instanceof OSDXFileTransferDeleteCommand) {
 			addStatus("delete \""+((OSDXFileTransferDeleteCommand)command).absolutePathname+"\" successful.");
 			ttpanelRemote.refreshView(true);
 		}
 		else if (command instanceof OSDXFileTransferRenameCommand) {
 			addStatus("rename \""+((OSDXFileTransferRenameCommand)command).absolutePathname+"\" to \""+((OSDXFileTransferRenameCommand)command).newfilename+"\" successful.");
 			ttpanelRemote.refreshView(true);
 		} else {
 			Transfer t  = transfersInProgress.get(command.getID());
 			if (t!=null) {
 				transfersInProgress.remove(command.getID());
 			}
 			if (command instanceof OSDXFileTransferUploadCommand) {
 				//ttpanelRemote.updateTable();
 				if (!client.hasNextCommand()) {
 					ttpanelRemote.refreshView(true);
 				}
 			}
 		}
 	}
 
 }

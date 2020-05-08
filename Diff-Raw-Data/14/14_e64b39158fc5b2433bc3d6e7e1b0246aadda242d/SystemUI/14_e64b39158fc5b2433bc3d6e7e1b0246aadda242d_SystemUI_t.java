 /**
  * filename    : SystemUI.java
  * created     : Dec 13, 2012 (1:05:58 PM)
  * description :
  * -------------------------------------------------------
  * @version    : 0.1
  * @changes    :
  */
 
 package views;
 
 import controllers.ClientCtrl;
 import controllers.SearchCtrl;
 import controllers.TimeSheetCtrl;
 import models.Client;
 import models.DataEntry;
 import models.TimeSheet;
 import utils.*;
 import views.client.CreateClientUI;
 import views.dataentry.CreateDataEntryUI;
 import views.timesheet.CreateTimeSheetUI;
 import views.timesheet.EditTimeSheetUI;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.table.DefaultTableModel;
 import java.awt.*;
 import java.awt.event.*;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 @SuppressWarnings("serial")
 public class SystemUI extends JFrame implements ChangeListener
 {
 	// GUI elements
 	private boolean primaryTabActive;
     private TimeSheet _sheet;
 	private JPanel pnlSystemLayout;
 	private JPanel pnlClients;
 	private JPanel pnlTimeSheet;
 	private JTextField txtSearchOverview;
 	private JCheckBox chkUsersSheetsOnly;
 	private JList<String> lstTimeSheets;
 	private JList<String> lstClients;
 	private JLabel lblClientName_ts;
 	private JLabel lblCaseId_ts;
 	private JLabel lblClientAddress_ts;
 	private JLabel lblClientPhoneNo_ts;
 	private JLabel lblClientEmail_ts;
 	private JLabel lblTimeSheetOwner_ts;
 	private JTextArea txtNoteField;
 	private JLabel lblClientName_cl;
 	private JLabel lblClientAddress_cl;
 	private JLabel lblClientPhoneNo_cl;
 	private JLabel lblClientEmail_cl;
 	
 	// Data notification dialog
 	DataNotificationUI dbInfo = new views.DataNotificationUI();
 	
 	// Controllers
 	private TimeSheetCtrl _timeSheetCtrl;
 	private ClientCtrl _clientCtrl;
     private SearchCtrl _searchCtrl;
     private static SystemUI _instance;
 
 	// Sheet and Client grid view
 	private DefaultTableModel sheetModel;
 	private JTable sheetTable;
 	private String[] sheetColumn;
     private Date sortStartDate;
     private Date sortEndDate;
 	private DefaultTableModel clientModel;
 	private JTable clientTable;
 	private String[] clientColumn;
 
     public static SystemUI getInstance()
     { return _instance; }
 
 	public SystemUI()
 	{
         _instance = this;
 		_timeSheetCtrl = new TimeSheetCtrl();
 		_clientCtrl = new ClientCtrl();
         _searchCtrl = new SearchCtrl();
 
 		setIconImage(Toolkit.getDefaultToolkit().getImage(SystemUI.class.getResource("/icons/48x48/app.png")));
 		setTitle(SystemInformation.systemInformation(1) + " (" + SystemInformation.systemInformation(2) +
 				" - build " + SystemInformation.systemInformation(3) + ")");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		WindowListener exitListener = new WindowAdapter()
        {
 			public void windowClosing(WindowEvent e)
 			{
 				applicationExit();
 			}
 		};
 		addWindowListener(exitListener);
 		setBounds(0,0,1024,768);
 		setLocationRelativeTo(null);
 		setMinimumSize(new Dimension(1024,768));
 		setResizable(false);
 		
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		JMenu mnFiles = new JMenu("Filer");
 		mnFiles.setFont(new Font("Dialog", Font.PLAIN, 12));
 		menuBar.add(mnFiles);
 
 		JMenuItem mntmPrint = new JMenuItem("Udskriv");
 		mntmPrint.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
                 printSheet();
             }
 		});
 		mnFiles.add(mntmPrint);
 		
 		JSeparator separator = new JSeparator();
 		mnFiles.add(separator);
 		
 		JMenuItem mntmLogout = new JMenuItem("Logud");
 		mntmLogout.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				applicationLogout();
 			}
 		});
 		mnFiles.add(mntmLogout);
 		
 		JMenuItem mntmExit = new JMenuItem("Afslut");
 		mntmExit.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				applicationExit();
 			}
 		});
 		mnFiles.add(mntmExit);
 		
 		JMenu mnTimeSheet = new JMenu("Time-sag");
 		mnTimeSheet.setFont(new Font("Dialog", Font.PLAIN, 12));
 		menuBar.add(mnTimeSheet);
 		
 		JMenuItem mntmNewTimeSheet = new JMenuItem("Ny time-sag");
 		mntmNewTimeSheet.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				CreateTimeSheetUI.createWindow();
 			}
 		});
 		mnTimeSheet.add(mntmNewTimeSheet);
 		
 		JMenu mnClient = new JMenu("Klient");
 		mnClient.setFont(new Font("Dialog", Font.PLAIN, 12));
 		menuBar.add(mnClient);
 		
 		JMenuItem mntmNewClient = new JMenuItem("Ny klient");
 		mntmNewClient.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				CreateClientUI.createWindow();
 			}
 		});
 		mnClient.add(mntmNewClient);
 		
 		JMenuItem mntmEditClient = new JMenuItem("Rediger klient");
 		mntmEditClient.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 
 			}
 		});
 		mnClient.add(mntmEditClient);
 
 		JMenu mnSettings = new JMenu("Indstillinger");
 		mnSettings.setFont(new Font("Dialog", Font.PLAIN, 12));
 		menuBar.add(mnSettings);
 
         JMenuItem mntmPrintSettings = new JMenuItem("Udskrift");
         mntmPrintSettings.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 PrintSettingsUI.createWindow();
             }
         });
         mnSettings.add(mntmPrintSettings);
 		
 		JMenu mnAbout = new JMenu("Om");
 		mnAbout.setFont(new Font("Dialog", Font.PLAIN, 12));
 		menuBar.add(mnAbout);
 		
 		JMenuItem mntmAboutThis = new JMenuItem("Applikationen");
 		mntmAboutThis.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				AboutUI.createWindow();
 			}
 		});
 		mnAbout.add(mntmAboutThis);
 		
 		pnlSystemLayout = new JPanel();
 		pnlSystemLayout.setBorder(new EmptyBorder(5,5,5,5));
 		setContentPane(pnlSystemLayout);
 		pnlSystemLayout.setLayout(null);
 		
 		JPanel pnlQuickAccess = new JPanel();
 		pnlQuickAccess.setBounds(5,0,1014,37);
 		pnlSystemLayout.add(pnlQuickAccess);
 		pnlQuickAccess.setLayout(null);
 		
 		JLabel lblNewTimeSheet = new JLabel("Ny time-sag");
 		lblNewTimeSheet.addMouseListener(new MouseAdapter()
 		{
 			@Override
 			public void mouseClicked(MouseEvent e)
 			{
 				CreateTimeSheetUI.createWindow();
 			}
 		});
 		lblNewTimeSheet.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblNewTimeSheet.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		lblNewTimeSheet.setIcon(new ImageIcon(SystemUI.class.getResource("/icons/16x16/new_timesheet.png")));
 		lblNewTimeSheet.setBounds(5,12,95,16);
 		pnlQuickAccess.add(lblNewTimeSheet);
 		
 		JLabel lblNewDataEntry = new JLabel("Ny registrering");
 		lblNewDataEntry.addMouseListener(new MouseAdapter()
 		{
 			@Override
 			public void mouseClicked(MouseEvent e)
 			{
 				newDataEntry();
 			}
 		});
 		lblNewDataEntry.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblNewDataEntry.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		lblNewDataEntry.setIcon(new ImageIcon(SystemUI.class.getResource("/icons/16x16/new_dataentry.png")));
 		lblNewDataEntry.setBounds(115,12,114,16);
 		pnlQuickAccess.add(lblNewDataEntry);
 		
 		JLabel lblNewClient = new JLabel("Ny klient");
 		lblNewClient.addMouseListener(new MouseAdapter()
 		{
 			@Override
 			public void mouseClicked(MouseEvent e)
 			{
 				CreateClientUI.createWindow();
 			}
 		});
 		lblNewClient.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblNewClient.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		lblNewClient.setIcon(new ImageIcon(SystemUI.class.getResource("/icons/16x16/new_client.png")));
 		lblNewClient.setBounds(244,12,75,16);
 		pnlQuickAccess.add(lblNewClient);
 		
 		JLabel lblPrintOverview = new JLabel("Udskriv");
         lblPrintOverview.addMouseListener(new MouseAdapter()
         {
             @Override
             public void mouseClicked(MouseEvent e)
             {
                 printSheet();
             }
         });
 		lblPrintOverview.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblPrintOverview.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		lblPrintOverview.setIcon(new ImageIcon(SystemUI.class.getResource("/icons/16x16/print_overview.png")));
 		lblPrintOverview.setBounds(333,12,68,16);
 		pnlQuickAccess.add(lblPrintOverview);
 		
 		JLabel lblSortOverview = new JLabel("Sort" + "\u00e9" + "r");
         lblSortOverview.addMouseListener(new MouseAdapter()
         {
             @Override
             public void mouseClicked(MouseEvent e)
             {
                 SortUI.createWindow();
             }
         });
 		lblSortOverview.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblSortOverview.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		lblSortOverview.setIcon(new ImageIcon(SystemUI.class.getResource("/icons/16x16/sort_overview.png")));
 		lblSortOverview.setBounds(416,12,60,16);
 		pnlQuickAccess.add(lblSortOverview);
 		
 		JLabel lblPermission = new JLabel("Rettigheder");
 		lblPermission.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblPermission.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		lblPermission.setIcon(new ImageIcon(SystemUI.class.getResource("/icons/16x16/permission_timesheet.png")));
 		lblPermission.setBounds(493,12,114,16);
 		pnlQuickAccess.add(lblPermission);
 		
 		JLabel lblSearchOverview = new JLabel();
 		lblSearchOverview.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		lblSearchOverview.setIcon(new ImageIcon(SystemUI.class.getResource("/icons/16x16/search_overview.png")));
 		lblSearchOverview.setBounds(833,12,16,16);
 		pnlQuickAccess.add(lblSearchOverview);
 		
 		txtSearchOverview = new JTextField();
 		txtSearchOverview.addFocusListener(new FocusAdapter()
 		{
 			@Override
 			public void focusGained(FocusEvent e)
 			{
 				txtSearchOverview.setText("");
 			}
 			@Override
 			public void focusLost(FocusEvent e)
 			{
 				txtSearchOverview.setText("S\u00F8g");
 			}
 		});
         txtSearchOverview.addKeyListener(new KeyAdapter()
         {
             public void keyPressed(KeyEvent e)
             {
                 if (e.getKeyCode() == KeyEvent.VK_ENTER)
                 {
                     try
                     {
                         SearchUI.createWindow(_searchCtrl.search(txtSearchOverview.getText()));
                     }
                     catch (Exception ex)
                     {
                         ex.printStackTrace();
                     }
                 }
             }
         });
 		txtSearchOverview.setText("S\u00F8g");
 		txtSearchOverview.setBounds(854,9,155,22);
 		pnlQuickAccess.add(txtSearchOverview);
 		txtSearchOverview.setColumns(10);
 		
 		JPanel pnlOverviewSelection = new JPanel();
 		pnlOverviewSelection.setBounds(5,36,215,675);
 		pnlSystemLayout.add(pnlOverviewSelection);
 		pnlOverviewSelection.setLayout(null);
 
 		// START OF TIMESHEETS PANEL
 		pnlTimeSheet = new JPanel();
 		pnlTimeSheet.setBounds(220,36,799,675);
 		pnlSystemLayout.add(pnlTimeSheet);
 		pnlTimeSheet.setLayout(null);
 		
 		// START OF TS_INFO PANEL
 		JPanel pnlTimeSheetInfo = new JPanel();
 		pnlTimeSheetInfo.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		pnlTimeSheetInfo.setBackground(Color.WHITE);
 		pnlTimeSheetInfo.setBounds(3,2,790,86);
 		pnlTimeSheet.add(pnlTimeSheetInfo);
 		pnlTimeSheetInfo.setLayout(null);
 		
 		lblClientName_ts = new JLabel();
 		lblClientName_ts.setForeground(UIManager.getColor("CheckBoxMenuItem.acceleratorForeground"));
 		lblClientName_ts.setFont(new Font("Dialog", Font.PLAIN, 18));
 		lblClientName_ts.setBounds(5,5,500,20);
 		pnlTimeSheetInfo.add(lblClientName_ts);
 		
 		lblCaseId_ts = new JLabel();
 		lblCaseId_ts.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblCaseId_ts.setForeground(UIManager.getColor("CheckBoxMenuItem.acceleratorForeground"));
 		lblCaseId_ts.setFont(new Font("Dialog", Font.PLAIN, 18));
 		lblCaseId_ts.setBounds(535,5,250,20);
 		pnlTimeSheetInfo.add(lblCaseId_ts);
 		
 		lblClientAddress_ts = new JLabel();
 		lblClientAddress_ts.setForeground(Color.GRAY);
 		lblClientAddress_ts.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblClientAddress_ts.setBounds(5,25,500,15);
 		pnlTimeSheetInfo.add(lblClientAddress_ts);
 		
 		lblClientPhoneNo_ts = new JLabel();
 		lblClientPhoneNo_ts.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblClientPhoneNo_ts.setBounds(5,52,450,15);
 		pnlTimeSheetInfo.add(lblClientPhoneNo_ts);
 		
 		lblClientEmail_ts = new JLabel();
 		lblClientEmail_ts.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblClientEmail_ts.setBounds(5,66,450,15);
 		pnlTimeSheetInfo.add(lblClientEmail_ts);
 		
 		lblTimeSheetOwner_ts = new JLabel();
 		lblTimeSheetOwner_ts.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblTimeSheetOwner_ts.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblTimeSheetOwner_ts.setBounds(485,66,300,15);
 		pnlTimeSheetInfo.add(lblTimeSheetOwner_ts);
 		// END OF TS_INFO PANEL
 		
 		// START OF NOTE PANEL
 		JPanel pnlTimeSheetNote = new JPanel();
 		pnlTimeSheetNote.setBackground(Color.WHITE);
 		pnlTimeSheetNote.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		pnlTimeSheetNote.setBounds(3,99,790,65);
 		pnlTimeSheet.add(pnlTimeSheetNote);
 		pnlTimeSheetNote.setLayout(null);
 		
 		txtNoteField = new JTextArea();
 		txtNoteField.setBounds(5,5,780,55);
         txtNoteField.setEditable(false);
 		pnlTimeSheetNote.add(txtNoteField);	
 		// END OF NOTE PANEL
 		
 		// START OF SHEETGRID
 		JPanel pnlTimeSheetOverview = new JPanel();
 		pnlTimeSheetOverview.setBounds(3,170,790,498);
 		pnlTimeSheet.add(pnlTimeSheetOverview);
 
 		sheetColumn = new String[]{"P" + "\u00e5" + "begyndt", "Afsluttet", "Opgave", "Registrator", "Bem" + "\u00e6" + "rkning", " "};
 		
 		sheetTable = new JTable()
 		{
 			public boolean isCellEditable(int data, int columns)
 			{
 				if(columns == 5 || columns == 6)
 					return true;
 				return false;
 			}
 		};
 		
 		sheetModel = new DefaultTableModel();
 		
 		sheetTable.setModel(sheetModel);
 		sheetTable.setFillsViewportHeight(true);
 		
 		JScrollPane sheetDataScroll = new JScrollPane(sheetTable);
 		sheetTable.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		sheetTable.setPreferredScrollableViewportSize(new Dimension(790,493));
 		sheetDataScroll.setPreferredSize(new Dimension(790,493));
 		sheetDataScroll.setBorder(BorderFactory.createEmptyBorder());
 		pnlTimeSheetOverview.add(sheetDataScroll);
 		// END OF SHEETGRID
 		// END OF TIMESHEETS PANEL
 		
 		// START OF CLIENTS PANEL
 		pnlClients = new JPanel();
 		pnlClients.setBounds(220,36,799,675);
 		pnlSystemLayout.add(pnlClients);
 		pnlClients.setLayout(null);
 		
 		// START OF CL_INFO PANEL
 		JPanel pnlClientInfo = new JPanel();
 		pnlClientInfo.setLayout(null);
 		pnlClientInfo.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		pnlClientInfo.setBackground(Color.WHITE);
 		pnlClientInfo.setBounds(3,2,790,86);
 		pnlClients.add(pnlClientInfo);
 		
 		lblClientName_cl = new JLabel();
 		lblClientName_cl.setForeground(UIManager.getColor("CheckBoxMenuItem.acceleratorForeground"));
 		lblClientName_cl.setFont(new Font("Dialog", Font.PLAIN, 18));
 		lblClientName_cl.setBounds(5,5,500,20);
 		pnlClientInfo.add(lblClientName_cl);
 		
 		lblClientAddress_cl = new JLabel();
 		lblClientAddress_cl.setForeground(Color.GRAY);
 		lblClientAddress_cl.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblClientAddress_cl.setBounds(5,25,500,15);
 		pnlClientInfo.add(lblClientAddress_cl);
 		
 		lblClientPhoneNo_cl = new JLabel();
 		lblClientPhoneNo_cl.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblClientPhoneNo_cl.setBounds(5,52,450,15);
 		pnlClientInfo.add(lblClientPhoneNo_cl);
 		
 		lblClientEmail_cl = new JLabel();
 		lblClientEmail_cl.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblClientEmail_cl.setBounds(5,66,450,15);
 		pnlClientInfo.add(lblClientEmail_cl);
 		// END OF CL_INFO PANEL
 		
 		// START OF CLIENTGRID
 		JPanel pnlClientOverview = new JPanel();
 		pnlClientOverview.setBounds(3,94,790,574);
 		pnlClients.add(pnlClientOverview);
 		
 		clientColumn = new String[]{"Sags nr", "Oprettet", "Ansvarlig", "Note", " "};
 		
 		clientTable = new JTable()
 		{
 			public boolean isCellEditable(int data, int columns)
             {
                 if(columns == 4 || columns == 5)
                     return true;
                 return false;
             }
 		};
 		
 		clientModel = new DefaultTableModel();
 		
 		clientTable.setModel(clientModel);
 		clientTable.setFillsViewportHeight(true);
 		
 		JScrollPane clientDataScroll = new JScrollPane(clientTable);
 		clientTable.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		clientTable.setPreferredScrollableViewportSize(new Dimension(790,569));
 		clientDataScroll.setPreferredSize(new Dimension(790,569));
 		clientDataScroll.setBorder(BorderFactory.createEmptyBorder());
 		pnlClientOverview.add(clientDataScroll);
 		// END OF CLIENTGRID
 		// END OF CLIENTS PANEL
 
 		// START OF TAB SECTION
 		JTabbedPane tabSelection = new JTabbedPane(JTabbedPane.TOP);
 		tabSelection.setFont(new Font("Dialog", Font.PLAIN, 12));
 		tabSelection.setBounds(5, 0, 202, 668);
 		tabSelection.addChangeListener(this);
 		pnlOverviewSelection.add(tabSelection);
 		
 		// START OF CASE TAB
 		JPanel pnlTimeSheetTab = new JPanel();
 		tabSelection.addTab("Time-sager", null, pnlTimeSheetTab, null);
 		pnlTimeSheetTab.setLayout(null);
 		
 		JPanel pnlSheetList = new JPanel();
 		pnlSheetList.setBounds(5,5,187,605);
 		pnlSheetList.setLayout(null);
 		pnlTimeSheetTab.add(pnlSheetList);
 		
 		lstTimeSheets = new JList<String>();
 		lstTimeSheets.addMouseListener(new MouseAdapter()
 		{
 			@Override
 			public void mouseClicked(MouseEvent e)
 			{
 				dbInfo.setVisible(true);
 				addSheetData();
 			}
 		});
 		lstTimeSheets.setListData(populateSheetList());
 		lstTimeSheets.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lstTimeSheets.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		lstTimeSheets.setBounds(0,0,187,605);
 		JScrollPane sheetListScroll = new JScrollPane(lstTimeSheets);
 		pnlSheetList.add(sheetListScroll);
 		pnlSheetList.add(lstTimeSheets);
 		
 		chkUsersSheetsOnly = new JCheckBox("Vis kun mine sager");
 		chkUsersSheetsOnly.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				checkUserSheetsOnly();
 			}
 		});
 		chkUsersSheetsOnly.setFont(new Font("Dialog", Font.PLAIN, 12));
 		chkUsersSheetsOnly.setBounds(5,614,181,23);
 		pnlTimeSheetTab.add(chkUsersSheetsOnly);
 		// END OF CASE TAB
 		
 		// START OF CLIENT TAB
 		JPanel pnlClientTab = new JPanel();
 		tabSelection.addTab("Klienter", null, pnlClientTab, null);
 		pnlClientTab.setLayout(null);
 		
 		JPanel pnlClientList = new JPanel();
 		pnlClientList.setBounds(5,5,187,631);
 		pnlClientList.setLayout(null);
 		pnlClientTab.add(pnlClientList);
 				
 		lstClients = new JList<String>();
 		lstClients.addMouseListener(new MouseAdapter()
 		{
 			@Override
 			public void mouseClicked(MouseEvent e)
 			{
 				dbInfo.setVisible(true);
 				addClientData();
 			}
 		});
 		lstClients.setListData(populateClientList());
 		lstClients.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lstClients.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		lstClients.setBounds(0,0,187,631);
 		JScrollPane clientListScroll = new JScrollPane(lstClients);
 		pnlClientList.add(clientListScroll);
 		pnlClientList.add(lstClients);
 		// END OF CLIENT TAB
 		// END OF TAB SECTION
 	}
 
     private void printSheet()
     {
         if(primaryTabActive)
         {
             if(_sheet != null)
             {
                 GenerateReport genReport = new GenerateReport();
                 try
                 {
                     genReport.fillReport(_sheet.getSheetId(), UserSession.getExportToPdf(), UserSession.getOutputPath() + _sheet.getCaseId() + " (" + new SimpleDateFormat("dd-MM-yyyy HH.mm)").format(new Date()) + ".pdf");
                     JOptionPane.showMessageDialog(null, "Time-sagen er nu udskrevet", "Information!", JOptionPane.INFORMATION_MESSAGE);
                 }
                 catch (Exception ex)
                 {
                     JOptionPane.showMessageDialog(null, Logging.handleException(ex, 0), "Fejl!", JOptionPane.ERROR_MESSAGE);
                 }
             }
             else
                 JOptionPane.showMessageDialog(null, "V" + "\u00e6" + "lg den time-sag du " + "\u00f8" + "nsker at udskrive", "Information!", JOptionPane.INFORMATION_MESSAGE);
         }
         else
         {
             JOptionPane.showMessageDialog(null, "Udskrift funktionen er kun tilg" + "\u00e6" + "ngelig under \"Time-Sager\"", "Information!", JOptionPane.INFORMATION_MESSAGE);
         }
     }
 
     private void applicationExit()
 	{
 		int request = JOptionPane.showConfirmDialog(null, "Er du sikker p" + "\u00e5" + " at du vil afslutte programmet?", "Afslut", JOptionPane.YES_NO_OPTION);
 		if(request == JOptionPane.YES_OPTION)
 			System.exit(0);
 	}
 	
 	private void applicationLogout()
 	{
 		int request = JOptionPane.showConfirmDialog(null, "Er du sikker p" + "\u00e5" + " at du vil logge ud af programmet?", "Logud", JOptionPane.YES_NO_OPTION);
 		if(request == JOptionPane.YES_OPTION)
 			return; // logout and go to login dialog
 	}
 	
 	private void newDataEntry()
 	{
 		if(lstTimeSheets.getSelectedIndex() != -1)
 		{
 			String caseId = lstTimeSheets.getSelectedValue().substring(0, lstTimeSheets.getSelectedValue().indexOf("(")-1);
 			try {
 				TimeSheet timeSheet = _timeSheetCtrl.getTimeSheetByCaseId(caseId);
 				CreateDataEntryUI.createWindowForExistingCase(timeSheet);
 			}
 			catch(Exception ex)
 			{
 				JOptionPane.showMessageDialog(null, Logging.handleException(ex, 99), "Fejl", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		else
 		{
 			JOptionPane.showMessageDialog(null, "Der er ikke valgt en time-sag.", "Fejl", JOptionPane.WARNING_MESSAGE);
 		}
 	}
 	
 	private void checkUserSheetsOnly()
 	{
 		if(chkUsersSheetsOnly.isSelected())
 		{
 			lstTimeSheets.setListData(populateSheetByUser());
 		}
 		else
 		{
 			lstTimeSheets.setListData(populateSheetList());
 		}
 	}
 	
 	private void addButtonsToSheets(final int columnIndex)
 	{
 		Action show = new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				JTable sheetTable = (JTable) e.getSource();
 				int row = Integer.valueOf(e.getActionCommand());
 				
 				if(columnIndex == 5)
 				{
 
 				}
 			}
 		};
 		ButtonColumn buttonColumn = new ButtonColumn(sheetTable, show, columnIndex);
 		buttonColumn.setMnemonic(KeyEvent.VK_D);
         sheetTable.repaint();
 	}
 	
 	private void addButtonToClient(final int columnIndex)
 	{
 		Action show = new AbstractAction()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				JTable clientTable = (JTable) e.getSource();
 				int row = Integer.valueOf(e.getActionCommand());
 				String caseId = clientTable.getValueAt(row, 0).toString();
 				if(columnIndex == 4)
 				{
 					try
 					{
 						TimeSheet sheet = _timeSheetCtrl.getTimeSheetByCaseId(caseId);
 						EditTimeSheetUI.createWindow(sheet);
 					} 
 					catch (Exception ex)
 					{
 						JOptionPane.showMessageDialog(null, Logging.handleException(ex, 0), "Fejl!", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		};
 		ButtonColumn buttonColumn = new ButtonColumn(clientTable, show, columnIndex);
 		buttonColumn.setMnemonic(KeyEvent.VK_D);
         clientTable.repaint();
 	}
 	
 	private String[] populateSheetList()
 	{
         try
         {
             return new PopulateSheetList().doInBackground();
         }
         catch(Exception ex)
         {
         	JOptionPane.showMessageDialog(null, Logging.handleException(ex, 99), "Fejl!", JOptionPane.ERROR_MESSAGE);
         }
 
         return null;
     }
 	
 	private String[] populateSheetByUser()
 	{
 		try
 		{
 			return new PopulateSheetByUser().doInBackground();
 		}
 		catch(Exception ex)
 		{
 			JOptionPane.showMessageDialog(null, Logging.handleException(ex, 99), "Fejl!", JOptionPane.ERROR_MESSAGE);
 		}
 		
 		return null;
 	}
 	
 	private String[] populateClientList()
 	{
 		try
 		{
 			return new PopulateClientList().doInBackground();
 		}
 		catch(Exception ex)
 		{
 			JOptionPane.showMessageDialog(null, Logging.handleException(ex, 99), "Fejl!", JOptionPane.ERROR_MESSAGE);
         }
 
         return null;
 	}
 	
 	private void addSheetData()
 	{
         new AddSheetData().execute();
 	}
 	
 	private void addClientData()
 	{
 		new AddClientData().execute();
 	}
 
     class PopulateSheetList extends SwingWorker<String[], Integer>
     {
     	@Override
         protected String[] doInBackground() throws Exception
         {
             ArrayList<TimeSheet> timeSheetList;
             try
             {
                 timeSheetList = _timeSheetCtrl.getAllTimeSheets();
                 String[] sheetNames = new String[timeSheetList.size()];
                 for(int i = 0; i < timeSheetList.size(); i++)
                     sheetNames[i] = timeSheetList.get(i).getCaseId() +
                             " (" + timeSheetList.get(i).getClient().getName() + ")";
 
                 return sheetNames;
             }
             catch(Exception ex)
             {
                 JOptionPane.showMessageDialog(null, Logging.handleException(ex, 0), "Fejl!", JOptionPane.ERROR_MESSAGE);
             }
 
             return null;
         }
     }
     
     class PopulateSheetByUser extends SwingWorker<String[], Integer>
     {
     	@Override
     	protected String[] doInBackground() throws Exception
     	{
     		ArrayList<TimeSheet> timeSheetList;
     		try
     		{
                 timeSheetList = _timeSheetCtrl.getAllTimeSheetsByUser(UserSession.getLoggedInUser());
     			String[] sheetNames = new String[timeSheetList.size()];
     			for(int i = 0; i < timeSheetList.size(); i++)
     				sheetNames[i] = timeSheetList.get(i).getCaseId() +
     						" (" + timeSheetList.get(i).getClient().getName() + ")";
     			
     			return sheetNames;
     		}
     		catch(Exception ex)
     		{
     			JOptionPane.showMessageDialog(null, Logging.handleException(ex, 0), "Fejl!", JOptionPane.ERROR_MESSAGE);
     		}
     		
     		return null;
     	}
     }
     
     class PopulateClientList extends SwingWorker<String[], Integer>
     {
     	@Override
     	protected String[] doInBackground() throws Exception
     	{
     		ArrayList<Client> clientsList;
     		try
     		{
     			clientsList = _clientCtrl.getAllClients();
     			String[] clientNames = new String[clientsList.size()];
     			for(int i = 0; i < clientsList.size(); i++)
     				clientNames[i] = clientsList.get(i).getName() + " (" + clientsList.get(i).getPhoneNo() + ")";
     			
     			return clientNames;
     		}
     		catch(Exception ex)
     		{
     			JOptionPane.showMessageDialog(null, Logging.handleException(ex, 0), "Fejl!", JOptionPane.ERROR_MESSAGE);
     		}
     		
     		return null;
     	}
     }
 
     class AddSheetData extends SwingWorker<Integer, Integer>
     {
     	@Override
         protected Integer doInBackground() throws Exception
         {
             try
             {
             	String caseId = lstTimeSheets.getSelectedValue().substring(0, lstTimeSheets.getSelectedValue().indexOf("(")-1);
             	
             	TimeSheet sheet;
                 if(sortStartDate != null && sortEndDate != null)
                     sheet = _timeSheetCtrl.getTimeSheetByCaseId(caseId, sortStartDate, sortEndDate);
                 else
                     sheet = _timeSheetCtrl.getTimeSheetByCaseId(caseId);
 
                 if(sheet != null)
                 {	
                     ArrayList<DataEntry> dataEntries = sheet.getDataEntries();
 
                     Object[][] data = {};
                     sheetModel.setDataVector(data, sheetColumn);
 
                     for(int i = 0; i < dataEntries.size(); i++)
                     {
 
                         DataEntry dataEntry = dataEntries.get(i);
                         Object[] row = new Object[]{ dataEntry.getStartDate(), dataEntry.getEndDate(), dataEntry.getTask().getTitle(),
                                 dataEntry.getUser().getFirstName() + " " + dataEntry.getUser().getLastName(), dataEntry.getEntryRemark(), "Rediger/Slet"};
                         sheetModel.addRow(row);
                     }
                     
                     addButtonsToSheets(5);
 
                     _sheet = sheet;
                     lblClientName_ts.setText(sheet.getClient().getName());
         			lblClientAddress_ts.setText(sheet.getClient().getAddress() + ", " + sheet.getClient().getCity().getZipCode() + " " + sheet.getClient().getCity().getCityName());
         			lblClientPhoneNo_ts.setText("Telefon: " + String.valueOf(sheet.getClient().getPhoneNo()));
         			lblClientEmail_ts.setText("E-Mail: " + sheet.getClient().getEmail());
         			lblCaseId_ts.setText(sheet.getCaseId());
         			lblTimeSheetOwner_ts.setText("Ansvarlig: " + sheet.getUser().getFirstName() + " " + sheet.getUser().getLastName());
         			txtNoteField.setText("Note: " + sheet.getNote());
                 }
             }
             catch(Exception ex)
             {
                 JOptionPane.showMessageDialog(null, Logging.handleException(ex, 0), "Fejl!", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             }
 
             return null;
         }
     	
     	@Override
         protected void done()
         {
         	dbInfo.dispose();
         }
     }
     
     class AddClientData extends SwingWorker<Integer, Integer>
     {
     	@Override
     	protected Integer doInBackground() throws Exception
         {
 			try
 			{
 				long clientPhone = Long.parseLong(lstClients.getSelectedValue().substring(lstClients.getSelectedValue().indexOf("(")+1, 
 													lstClients.getSelectedValue().indexOf(")")));
 				
 				Client client = _clientCtrl.getClientByPhone(clientPhone);
 				
 				if(client != null)
 				{	
 					ArrayList<TimeSheet> timeSheets;
                     if(sortStartDate != null && sortEndDate != null)
                         timeSheets = _timeSheetCtrl.getAllTimeSheetsByClient(client, sortStartDate, sortEndDate);
                     else
                         timeSheets = _timeSheetCtrl.getAllTimeSheetsByClient(client);
 					
 					Object[][] data = {};
 					clientModel.setDataVector(data, clientColumn);
 					
 					for(int i = 0; i < timeSheets.size(); i++)
 					{
 						TimeSheet timesheet = timeSheets.get(i);
 						Object[] row = new Object[]{ timesheet.getCaseId(), timesheet.getCreationDate(), timesheet.getUser().getFirstName() + " " +
 								timesheet.getUser().getLastName(), timesheet.getNote(), "Redigere/Slet" };
 						
 						clientModel.addRow(row);
 					}
 					
 					addButtonToClient(4);
 
 					lblClientName_cl.setText(client.getName());
 					lblClientAddress_cl.setText(client.getAddress() + ", " + client.getCity().getZipCode() + " " + client.getCity().getCityName());
 					lblClientPhoneNo_cl.setText("Telefon: " + String.valueOf(client.getPhoneNo()));
 					lblClientEmail_cl.setText("E-Mail: " + client.getEmail());
 				}
 			}
 			catch(Exception ex)
 			{
 				ex.printStackTrace();
 			}
 			
 			return null;
         }
     	
     	@Override
     	protected void done()
         {
         	dbInfo.dispose();
         }
     }
     
     @Override
 	public void stateChanged(ChangeEvent e)
 	{	
 		try
 		{
 			if(!primaryTabActive)
 			{
 				pnlClients.setVisible(false);
 				pnlTimeSheet.setVisible(true);
 				primaryTabActive = true;
                 setSortDates(null, null);
 			}
 			else
 			{
 				pnlTimeSheet.setVisible(false);
 				pnlClients.setVisible(true);
 				primaryTabActive = false;
                 setSortDates(null, null);
 			}
 		}
 		catch(Exception ex)
 		{
 			JOptionPane.showMessageDialog(null, Logging.handleException(ex, 5), "Fejl!", JOptionPane.ERROR_MESSAGE);
 			System.exit(0);
 		}
 	}
 
     public void sortData()
     {
         if(!primaryTabActive)
         {
             if(!lstClients.isSelectionEmpty())
             {
                 new AddClientData().execute();
             }
             else
                 JOptionPane.showMessageDialog(null, "V" + "\u00e6" + "lg en klient, f" + "\u00f8" + "r du kan oprette et filter", "Information!", JOptionPane.INFORMATION_MESSAGE);
         }
         else
         {
             if(!lstTimeSheets.isSelectionEmpty())
             {
                 new AddSheetData().execute();
             }
             else
                 JOptionPane.showMessageDialog(null, "V" + "\u00e6" + "lg en time-sag, f" + "\u00f8" + "r du kan oprette et filter", "Information!", JOptionPane.INFORMATION_MESSAGE);
         }
     }
 
     public void setSortDates(Date startDate, Date endDate)
     {
         sortStartDate = startDate;
         sortEndDate = endDate;
     }
 }

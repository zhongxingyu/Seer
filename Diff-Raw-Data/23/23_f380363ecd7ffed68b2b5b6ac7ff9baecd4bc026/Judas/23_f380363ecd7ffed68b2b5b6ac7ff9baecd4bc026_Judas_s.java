 package org.iugonet.www;
 
 import java.awt.*;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.undo.AbstractUndoableEdit;
 import javax.swing.undo.UndoManager;
 import javax.swing.undo.UndoableEdit;
 import javax.swing.undo.UndoableEditSupport;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.print.PrinterJob;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
import java.util.Properties;
 
 public class Judas extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private JPanel contentPane;
 	private PlotPane[] plotPane;
 	private FileFilter[] filterData;
 	private FileFilter[] filterMetadata;
 	private FileFilter[] filterThemisData; // tgd
 	private FileFilter[] filterThemisTemplate; // tgt
 	private JTextField textField;
 	private static JPanel panel;
 	// private UndoManager undoManager;
 	static private UndoManager undoManager;
 
 	static public UndoManager getUndoManager() {
 		return undoManager;
 	}
 
 	private UndoableEditSupport undoableEditSupport;
 
 	private JCheckBox chckbxShowHorizontalTracking;
 
 	private JCheckBox chckbxPanelTracking;
 
 	private JCheckBox chckbxStatusBar;
 
 	private JCheckBox chckbxLegend;
 
 	private JCheckBox chckbxQueryForMarker;
 
 	private JCheckBox chckbxRubberBandFor;
 
 	private JCheckBox chckbxTrackAdditionalVariables;
 
 	static private JMenuItem mntmUndo;
 
 	static public JMenuItem getMntmUndo() {
 		return mntmUndo;
 	}
 
 	static private JMenuItem mntmRedo;
 
 	private JCheckBox chckbxShowVerticalTracking;
 
 	private String userHome;
 
 	private String themisRemotedataDir;
 
 	private String themisDataDir;
 
 	private String judsDataDir;
 
 	public String getUserHome() {
 		return userHome;
 	}
 
 	public void setUserHome(String userHome) {
 		this.userHome = userHome;
 	}
 
 	static public JMenuItem getMntmRedo() {
 		return mntmRedo;
 	}
 
 	public static JPanel getPanel() {
 		return panel;
 	}
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Judas frame = new Judas();
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
 	public Judas() {
 		super();
 		//
 		try {
 			String userHome = System.getProperty("user.home");
 			System.out.println("userHome="+userHome);
 			File file = new File(
 					userHome
 							+ "/Judas/workspace/JnlpGenerator/config/application.properties");
 			InputStream inputStream;
 			inputStream = new FileInputStream(file);
 //			properties.load(inputStream);
 //			getProperties("config/application.properties");
 
 //			themisDataDir = properties.getProperty("THEMIS_DATA_DIR");
 //			themisRemotedataDir = properties
 //					.getProperty("THEMIS_REMOTEDATA_DIR");
 //			judsDataDir = properties.getProperty("JUDAS_DATA_DIR");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		//
 		undoManager = new UndoManager();
 		undoableEditSupport = new UndoableEditSupport();
 		undoableEditSupport.addUndoableEditListener(undoManager);
 		System.out.println(undoManager.getLimit());
 		//
 		setTitle("Judas");
 		Icons icons = new Icons();
 		setIconImage(icons.getImage("icons/favicon.ico"));
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 900, 900);
 		contentPane = new JPanel();
 		contentPane.setName("Judas");
 		contentPane.setPreferredSize(new Dimension(900, 900));
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 
 		setLocationRelativeTo(null);
 		System.out.println(getContentPane().getName());
 
 		//
 		plotPane = new PlotPane[1];
 		plotPane[0] = new PlotPane();
 		plotPane[0].setBackground(Color.WHITE);
 		plotPane[0].setPreferredSize(new Dimension(900, 100));
 
 		filterData = new FileFilter[4];
 		filterData[0] = new FileNameExtensionFilter("CDF file", "cdf");
 		filterData[1] = new FileNameExtensionFilter("HDF5 file", "hdf");
 		filterData[2] = new FileNameExtensionFilter("HDF4 file", "hdf");
 		filterData[3] = new FileNameExtensionFilter("FITS file", "fits");
 
 		filterMetadata = new FileFilter[1];
 		filterMetadata[0] = new FileNameExtensionFilter("XML file", "xml");
 
 		filterThemisData = new FileFilter[1];
 		filterThemisData[0] = new FileNameExtensionFilter("TGD file", "tgd");
 
 		filterThemisTemplate = new FileFilter[1];
 		filterThemisTemplate[0] = new FileNameExtensionFilter("TGT file", "tgt");
 		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 
 		JMenuBar menuBar = new JMenuBar();
 		contentPane.add(menuBar);
 		menuBar.setPreferredSize(new Dimension(900, 30));
 		menuBar.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
 
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 		//
 
 		JMenuItem mntmOpenThemisDocument = new JMenuItem(
 				"Open THEMIS Document...");
 		mntmOpenThemisDocument.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Open THEMIS Document...");
 				JFileChooser filechooser = new JFileChooser();
 				filechooser.setDialogTitle("Open THEMIS Document...");
 				for (int i = 0; i < filterData.length; i++) {
 					filechooser.addChoosableFileFilter(filterData[i]);
 				}
 				for (int i = 0; i < filterMetadata.length; i++) {
 					filechooser.addChoosableFileFilter(filterMetadata[i]);
 				}
 				filechooser.setFileFilter(filterData[0]);
 				System.out.println(filechooser.getFileFilter());
 				int selected = filechooser.showOpenDialog(rootPane);
 				if (selected == JFileChooser.APPROVE_OPTION) {
 					File file = filechooser.getSelectedFile();
 				}
 			}
 		});
 		mnFile.add(mntmOpenThemisDocument);
 
 		JMenuItem mntmSaveThemisDocument = new JMenuItem(
 				"Save THEMIS Document...");
 		mntmSaveThemisDocument.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Save THEMIS Document...");
 				JFileChooser filechooser = new JFileChooser();
 				filechooser.setDialogTitle("Save THEMIS Document...");
 				for (int i = 0; i < filterData.length; i++) {
 					filechooser.addChoosableFileFilter(filterData[i]);
 				}
 				for (int i = 0; i < filterMetadata.length; i++) {
 					filechooser.addChoosableFileFilter(filterMetadata[i]);
 				}
 				filechooser.setFileFilter(filterData[0]);
 				int selected = filechooser.showOpenDialog(rootPane);
 
 				if (selected == JFileChooser.APPROVE_OPTION) {
 					File file = filechooser.getSelectedFile();
 				}
 			}
 		});
 		mnFile.add(mntmSaveThemisDocument);
 
 		JMenuItem mntmSaveThemisDocument_1 = new JMenuItem(
 				"Save THEMIS Document As...");
 		mntmSaveThemisDocument_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Save THEMIS Document As...");
 				JFileChooser filechooser = new JFileChooser();
 				filechooser.setDialogTitle("Save THEMIS Document As...");
 				for (int i = 0; i < filterData.length; i++) {
 					filechooser.addChoosableFileFilter(filterData[i]);
 				}
 				for (int i = 0; i < filterMetadata.length; i++) {
 					filechooser.addChoosableFileFilter(filterMetadata[i]);
 				}
 				filechooser.setFileFilter(filterData[0]);
 				int selected = filechooser.showOpenDialog(rootPane);
 
 				if (selected == JFileChooser.APPROVE_OPTION) {
 					File file = filechooser.getSelectedFile();
 				}
 			}
 		});
 		mnFile.add(mntmSaveThemisDocument_1);
 
 		JMenuItem mntmSaveWithData = new JMenuItem("Save With Data...");
 		mntmSaveWithData.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Save With Data...");
 				JFileChooser filechooser = new JFileChooser();
 				filechooser.setDialogTitle("Save With Data...");
 				for (int i = 0; i < filterData.length; i++) {
 					filechooser.addChoosableFileFilter(filterData[i]);
 				}
 				for (int i = 0; i < filterMetadata.length; i++) {
 					filechooser.addChoosableFileFilter(filterMetadata[i]);
 				}
 				filechooser.setAcceptAllFileFilterUsed(false);
 				filechooser.setFileFilter(filterData[0]);
 				int selected = filechooser.showOpenDialog(rootPane);
 
 				if (selected == JFileChooser.APPROVE_OPTION) {
 					File file = filechooser.getSelectedFile();
 				}
 			}
 		});
 		mnFile.add(mntmSaveWithData);
 
 		JMenu mnTemplate = new JMenu("Template");
 		mnFile.add(mnTemplate);
 
 		JMenuItem mntmOpenTemplate = new JMenuItem("Open Template...");
 		mntmOpenTemplate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Open Template...");
 				JFileChooser filechooser = new JFileChooser();
 				filechooser.setDialogTitle("Open Template...");
 				for (int i = 0; i < filterThemisTemplate.length; i++) {
 					filechooser.addChoosableFileFilter(filterThemisTemplate[i]);
 				}
 				filechooser.setFileFilter(filterThemisTemplate[0]);
 				System.out.println(filechooser.getFileFilter());
 				int selected = filechooser.showOpenDialog(rootPane);
 
 				if (selected == JFileChooser.APPROVE_OPTION) {
 					File file = filechooser.getSelectedFile();
 
 				}
 			}
 		});
 		mnTemplate.add(mntmOpenTemplate);
 
 		JMenuItem mntmSaveTemplate = new JMenuItem("Save Template...");
 		/*
 		mntmSaveTemplate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Save Template...");
 				JFileChooser filechooser = new JFileChooser();
 				filechooser.setDialogTitle("Save Template...");
 				for (int i = 0; i < filterThemisTemplate.length; i++) {
 					filechooser.addChoosableFileFilter(filterThemisTemplate[i]);
 				}
 				filechooser.setFileFilter(filterThemisTemplate[0]);
 				System.out.println(filechooser.getFileFilter());
 				int selected = filechooser.showOpenDialog(rootPane);
 
 				if (selected == JFileChooser.APPROVE_OPTION) {
 					File file = filechooser.getSelectedFile();
 
 				}
 			}
 		});*/
 		mnTemplate.add(mntmSaveTemplate);
 
 		JMenuItem mntmSaveTemplateAs = new JMenuItem("Save Template As...");
 		/*
 		mntmSaveTemplateAs.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Save Template As...");
 				JFileChooser filechooser = new JFileChooser();
 				filechooser.setDialogTitle("Save Template As...");
 				for (int i = 0; i < filterThemisTemplate.length; i++) {
 					filechooser.addChoosableFileFilter(filterThemisTemplate[i]);
 				}
 				filechooser.setFileFilter(filterThemisTemplate[0]);
 				System.out.println(filechooser.getFileFilter());
 				int selected = filechooser.showOpenDialog(rootPane);
 
 				if (selected == JFileChooser.APPROVE_OPTION) {
 					File file = filechooser.getSelectedFile();
 				}
 			}
 		});
 		*/
 		mnTemplate.add(mntmSaveTemplateAs);
 
 		JMenu mnResetTemplate = new JMenu("Reset Template");
 		mnTemplate.add(mnResetTemplate);
 
 		JMenuItem mntmResetPageTemplate = new JMenuItem("Reset Page Template");
 		mntmResetPageTemplate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Reset Page Template");
 			}
 		});
 		mnResetTemplate.add(mntmResetPageTemplate);
 
 		JMenuItem mntmResetPanelTemplate = new JMenuItem("Reset Panel Template");
 		mntmResetPanelTemplate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Reset Panel Template");
 			}
 		});
 		mnResetTemplate.add(mntmResetPanelTemplate);
 
 		JMenuItem mntmResetXaxisTemplate = new JMenuItem(
 				"Reset X-Axis Template");
 		mntmResetXaxisTemplate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Reset X-Axis Template");
 			}
 		});
 		mnResetTemplate.add(mntmResetXaxisTemplate);
 
 		JMenuItem mntmResetYaxisTemplate = new JMenuItem(
 				"Reset Y-Axis Template");
 		mntmResetYaxisTemplate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Reset Y-Axis Template");
 			}
 		});
 		mnResetTemplate.add(mntmResetYaxisTemplate);
 
 		JMenuItem mntmResetZaxisTemplate = new JMenuItem(
 				"Reset Z-Axis Template");
 		mntmResetZaxisTemplate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Reset Z-Axis Template");
 			}
 		});
 		mnResetTemplate.add(mntmResetZaxisTemplate);
 
 		JMenuItem mntmResetLineTemplate = new JMenuItem("Reset Line Template");
 		mntmResetLineTemplate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Reset Line Template");
 			}
 		});
 		mnResetTemplate.add(mntmResetLineTemplate);
 
 		JMenuItem mntmResetVariableTemplate = new JMenuItem(
 				"Reset Variable Template");
 		mntmResetVariableTemplate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Reset Variable Template");
 			}
 		});
 		mnResetTemplate.add(mntmResetVariableTemplate);
 
 		JSeparator separator = new JSeparator();
 		mnTemplate.add(separator);
 
 		JSeparator separator_2 = new JSeparator();
 		mnFile.add(separator_2);
 
 		JMenuItem mntmCloseWindow = new JMenuItem("Close Window");
 		mntmCloseWindow.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Close Window");
 			}
 		});
 		mnFile.add(mntmCloseWindow);
 
 		JSeparator separator_1 = new JSeparator();
 		mnFile.add(separator_1);
 
 		JMenuItem mntmLoadData = new JMenuItem("Load Data");
 		mntmLoadData.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Load Data");
 
 				LoadData loadData = new LoadData();
 				loadData.setLocationRelativeTo(null);
 				loadData.setVisible(true);
 			}
 		});
 		mnFile.add(mntmLoadData);
 
 		JMenuItem mntmGenerateThemisOverview = new JMenuItem(
 				"Generate THEMIS Overview Plot...");
 		mntmGenerateThemisOverview.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Generate THEMIS Overview Plot...");
 
 				GenerateThemisOverviewPlot generateThemisOverviewPlot = new GenerateThemisOverviewPlot();
 				generateThemisOverviewPlot.setLocationRelativeTo(null);
 				generateThemisOverviewPlot.setVisible(true);
 			}
 		});
 		mnFile.add(mntmGenerateThemisOverview);
 
 		JMenuItem mntmSaveDataAs = new JMenuItem("Save Data As...");
 		mntmSaveDataAs.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Save Data As...");
 				JFileChooser filechooser = new JFileChooser();
 				filechooser.setDialogTitle("Save Template As...");
 				for (int i = 0; i < filterThemisData.length; i++) {
 					filechooser.addChoosableFileFilter(filterThemisData[i]);
 				}
 				filechooser.setFileFilter(filterThemisData[0]);
 				System.out.println(filechooser.getFileFilter());
 				int selected = filechooser.showOpenDialog(rootPane);
 
 				if (selected == JFileChooser.APPROVE_OPTION) {
 					File file = filechooser.getSelectedFile();
 				}
 
 			}
 		});
 		mnFile.add(mntmSaveDataAs);
 
 		JMenuItem mntmManageDataAnd = new JMenuItem(
 				"Manage Data and Import/Export Tplot...");
 		mntmManageDataAnd.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Manage Data and Import/Export Tplot...");
 				ManageDataAndImportExportTplot manageDataAndImportExportTplot = new ManageDataAndImportExportTplot();
 				manageDataAndImportExportTplot.setLocationRelativeTo(null);
 				manageDataAndImportExportTplot.setVisible(true);
 			}
 		});
 		mnFile.add(mntmManageDataAnd);
 
 		JSeparator separator_3 = new JSeparator();
 		mnFile.add(separator_3);
 
 		JMenuItem mntmPrint = new JMenuItem("Print...");
 		
 		mntmPrint.setIcon(icons.getImageIcon("icons/print20.png"));
 		mntmPrint.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Print...");
 			}
 		});
 		mnFile.add(mntmPrint);
 
 		JMenuItem mntmPrintPreview = new JMenuItem("Print Preview");
 		mntmPrintPreview.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Print Preview");
 			}
 		});
 		mnFile.add(mntmPrintPreview);
 
 		JMenuItem mntmPrintMultipleFiles = new JMenuItem(
 				"Print Multiple Files...");
 		mntmPrintMultipleFiles.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Print Multiple Files...");
 			}
 		});
 		mnFile.add(mntmPrintMultipleFiles);
 
 		JMenuItem mntmPrintSetup = new JMenuItem("Print Setup");
 		mntmPrintSetup.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Print Setup");
 			}
 		});
 		mnFile.add(mntmPrintSetup);
 
 		JSeparator separator_4 = new JSeparator();
 		mnFile.add(separator_4);
 
 		JMenuItem mntmConfigurationSettings = new JMenuItem(
 				"Configuration Settings...");
 		mntmConfigurationSettings.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Configuration Settings...");
 				ConfigurationSettings configurationSettings = new ConfigurationSettings();
 				configurationSettings.setLocationRelativeTo(null);
 				configurationSettings.setVisible(true);
 			}
 		});
 		mnFile.add(mntmConfigurationSettings);
 
 		JSeparator separator_5 = new JSeparator();
 		mnFile.add(separator_5);
 
 		JMenuItem mntmExit = new JMenuItem("Exit");
 		mntmExit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Exit");
 				dispose();
 			}
 		});
 		mnFile.add(mntmExit);
 
 		JMenu mnEdit = new JMenu("Edit");
 		menuBar.add(mnEdit);
 
 		mntmUndo = new JMenuItem("Undo");
 		mntmUndo.setEnabled(false);
 	
 		mntmUndo.setIcon(icons.getImageIcon("icons/arrow-1-left20.png"));	
 		mntmUndo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Undo");
 				System.out.println("HOGE" + undoManager.toString());
 				if (undoManager.canUndo() == true) {
 					System.out.println("Can Undo");
 					undoManager.undo();
 				}
 				System.out.println("Can't Undo");
 			}
 		});
 		mnEdit.add(mntmUndo);
 
 		mntmRedo = new JMenuItem("Redo");
 		mntmRedo.setEnabled(false);
 		
 		mntmRedo.setIcon(icons.getImageIcon("icons/arrow-1-right20.png"));
 		mntmRedo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Redo");
 				if (undoManager.canRedo() == true) {
 					undoManager.redo();
 				}
 			}
 		});
 		mnEdit.add(mntmRedo);
 
 		JSeparator separator_6 = new JSeparator();
 		mnEdit.add(separator_6);
 
 		JMenuItem mntmCopy = new JMenuItem("Copy");
 
 		mntmCopy.setIcon(icons.getImageIcon("icons/copy20.png"));
 		mntmCopy.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Copy");
 			}
 		});
 		mnEdit.add(mntmCopy);
 
 		JSeparator separator_7 = new JSeparator();
 		mnEdit.add(separator_7);
 
 		JMenuItem mntmDeleteMarker = new JMenuItem("Delete Marker");
 		mntmDeleteMarker.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Delete Marker");
 			}
 		});
 		mnEdit.add(mntmDeleteMarker);
 
 		JSeparator separator_8 = new JSeparator();
 		mnEdit.add(separator_8);
 
 		JMenu mnSubset = new JMenu("Subset");
 		mnEdit.add(mnSubset);
 
 		JMenuItem mntmPage = new JMenuItem("Page");
 		mntmPage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Page");
 			}
 		});
 		mnSubset.add(mntmPage);
 
 		JMenuItem mntmMarkersinglePanel = new JMenuItem("Marker (Single Panel)");
 		mntmMarkersinglePanel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Marker (Single Panel)");
 			}
 		});
 		mnSubset.add(mntmMarkersinglePanel);
 
 		JMenuItem mntmMarkerallPanels = new JMenuItem("Marker (All Panels)");
 		mntmMarkerallPanels.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Marker (All Panels)");
 			}
 		});
 		mnSubset.add(mntmMarkerallPanels);
 
 		JMenu mnView = new JMenu("View");
 		menuBar.add(mnView);
 
 		JMenuItem mntmRefresh = new JMenuItem("Refresh");
 		mntmRefresh.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Refresh");
 			}
 		});
 		mnView.add(mntmRefresh);
 
 		JMenuItem mntmScrollForwardright = new JMenuItem(
 				"Scroll Forward (Right)");
 		mntmScrollForwardright.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Scroll Forward (Right)");
 			}
 		});
 		mnView.add(mntmScrollForwardright);
 
 		JMenuItem mntmScrollBackwardleft = new JMenuItem(
 				"Scroll Backward (Left)");
 		mntmScrollBackwardleft.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Scroll Backward (Left)");
 			}
 		});
 		mnView.add(mntmScrollBackwardleft);
 
 		JSeparator separator_9 = new JSeparator();
 		mnView.add(separator_9);
 
 		JMenuItem mntmExpandtab = new JMenuItem("Expand (Tab)");
 		mntmExpandtab.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Expand (Tab)");
 			}
 		});
 		mnView.add(mntmExpandtab);
 
 		JMenuItem mntmReducebackspace = new JMenuItem("Reduce (Backspace)");
 		mntmReducebackspace.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Reduce (Backspace)");
 			}
 		});
 		mnView.add(mntmReducebackspace);
 
 		JSeparator separator_10 = new JSeparator();
 		mnView.add(separator_10);
 
 		JMenuItem mntmHistoryWindow = new JMenuItem("History Window");
 		mntmHistoryWindow.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("History Window");
 				HistoryWindow historyWindow = new HistoryWindow();
 				historyWindow.setLocationRelativeTo(null);
 				historyWindow.setVisible(true);
 			}
 		});
 		mnView.add(mntmHistoryWindow);
 
 		chckbxLegend = new JCheckBox("Legend");
 		chckbxLegend.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// Create a new undo action
 				// Save our current state in a final boolean so it is accessible
 				// in the inner class below
 				final boolean curState = chckbxLegend.isSelected();
 				UndoableEdit undoableEdit = new AbstractUndoableEdit() {
 
 					// Method that is called when we must redo the undone action
 					public void redo()
 							throws javax.swing.undo.CannotRedoException {
 						super.redo();
 
 						// Set the checkbox to its pre-undone state
 						chckbxLegend.setSelected(curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 
 					public void undo()
 							throws javax.swing.undo.CannotUndoException {
 						super.undo();
 
 						// Set the checkbox to its previous state
 						chckbxLegend.setSelected(!curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 				};
 
 				// Add this undoable edit to the undo manager
 				Judas.getUndoManager().addEdit(undoableEdit);
 
 				// Update the state of the menu items
 				Judas.getMntmUndo()
 						.setEnabled(Judas.getUndoManager().canUndo());
 				Judas.getMntmRedo()
 						.setEnabled(Judas.getUndoManager().canRedo());
 			}
 		});
 		mnView.add(chckbxLegend);
 
 		chckbxStatusBar = new JCheckBox("Status Bar");
 		chckbxStatusBar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// Create a new undo action
 				// Save our current state in a final boolean so it is accessible
 				// in the inner class below
 				final boolean curState = chckbxStatusBar.isSelected();
 				UndoableEdit undoableEdit = new AbstractUndoableEdit() {
 
 					// Method that is called when we must redo the undone action
 					public void redo()
 							throws javax.swing.undo.CannotRedoException {
 						super.redo();
 
 						// Set the checkbox to its pre-undone state
 						chckbxStatusBar.setSelected(curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 
 					public void undo()
 							throws javax.swing.undo.CannotUndoException {
 						super.undo();
 
 						// Set the checkbox to its previous state
 						chckbxStatusBar.setSelected(!curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 				};
 
 				// Add this undoable edit to the undo manager
 				Judas.getUndoManager().addEdit(undoableEdit);
 
 				// Update the state of the menu items
 				Judas.getMntmUndo()
 						.setEnabled(Judas.getUndoManager().canUndo());
 				Judas.getMntmRedo()
 						.setEnabled(Judas.getUndoManager().canRedo());
 			}
 		});
 		mnView.add(chckbxStatusBar);
 
 		JMenu mnGraph = new JMenu("Graph");
 		menuBar.add(mnGraph);
 
 		chckbxPanelTracking = new JCheckBox("Panel Tracking");
 		chckbxPanelTracking.setSelected(true);
 		chckbxPanelTracking.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 
 				// Create a new undo action
 				// Save our current state in a final boolean so it is accessible
 				// in the inner class below
 				final boolean curState = chckbxPanelTracking.isSelected();
 				UndoableEdit undoableEdit = new AbstractUndoableEdit() {
 
 					// Method that is called when we must redo the undone action
 					public void redo()
 							throws javax.swing.undo.CannotRedoException {
 						super.redo();
 
 						// Set the checkbox to its pre-undone state
 						chckbxPanelTracking.setSelected(curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 
 					public void undo()
 							throws javax.swing.undo.CannotUndoException {
 						super.undo();
 
 						// Set the checkbox to its previous state
 						chckbxPanelTracking.setSelected(!curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 				};
 
 				// Add this undoable edit to the undo manager
 				Judas.getUndoManager().addEdit(undoableEdit);
 
 				// Update the state of the menu items
 				Judas.getMntmUndo()
 						.setEnabled(Judas.getUndoManager().canUndo());
 				Judas.getMntmRedo()
 						.setEnabled(Judas.getUndoManager().canRedo());
 			}
 		});
 		mnGraph.add(chckbxPanelTracking);
 
 		ButtonGroup buttonGroup = new ButtonGroup();
 
 		JRadioButton rdbtnTrackOnePanel = new JRadioButton("Track One Panel");
 		rdbtnTrackOnePanel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Track One Panel");
 			}
 		});
 		mnGraph.add(rdbtnTrackOnePanel);
 
 		buttonGroup.add(rdbtnTrackOnePanel);
 
 		JRadioButton rdbtnTrackAll = new JRadioButton("Track All");
 		rdbtnTrackAll.setSelected(true);
 		rdbtnTrackAll.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Track All");
 			}
 		});
 		mnGraph.add(rdbtnTrackAll);
 
 		buttonGroup.add(rdbtnTrackAll);
 
 		JSeparator separator_11 = new JSeparator();
 		mnGraph.add(separator_11);
 
 		chckbxShowHorizontalTracking = new JCheckBox("Show Horizontal Tracking");
 		chckbxShowHorizontalTracking.setSelected(true);
 		chckbxShowHorizontalTracking.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// Create a new undo action
 				// Save our current state in a final boolean so it is accessible
 				// in the inner class below
 				final boolean curState = chckbxShowHorizontalTracking
 						.isSelected();
 				UndoableEdit undoableEdit = new AbstractUndoableEdit() {
 
 					// Method that is called when we must redo the undone action
 					public void redo()
 							throws javax.swing.undo.CannotRedoException {
 						super.redo();
 
 						// Set the checkbox to its pre-undone state
 						chckbxShowHorizontalTracking.setSelected(curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 
 					public void undo()
 							throws javax.swing.undo.CannotUndoException {
 						super.undo();
 
 						// Set the checkbox to its previous state
 						chckbxShowHorizontalTracking.setSelected(!curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 				};
 
 				// Add this undoable edit to the undo manager
 				Judas.getUndoManager().addEdit(undoableEdit);
 
 				// Update the state of the menu items
 				Judas.getMntmUndo()
 						.setEnabled(Judas.getUndoManager().canUndo());
 				Judas.getMntmRedo()
 						.setEnabled(Judas.getUndoManager().canRedo());
 			}
 
 		});
 		mnGraph.add(chckbxShowHorizontalTracking);
 
 		chckbxShowVerticalTracking = new JCheckBox("Show Vertical Tracking");
 		chckbxShowVerticalTracking.setSelected(true);
 		chckbxShowVerticalTracking.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// Create a new undo action
 				// Save our current state in a final boolean so it is accessible
 				// in the inner class below
 				final boolean curState = chckbxShowVerticalTracking
 						.isSelected();
 				UndoableEdit undoableEdit = new AbstractUndoableEdit() {
 
 					// Method that is called when we must redo the undone action
 					public void redo()
 							throws javax.swing.undo.CannotRedoException {
 						super.redo();
 
 						// Set the checkbox to its pre-undone state
 						chckbxShowVerticalTracking.setSelected(curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 
 					public void undo()
 							throws javax.swing.undo.CannotUndoException {
 						super.undo();
 
 						// Set the checkbox to its previous state
 						chckbxShowVerticalTracking.setSelected(!curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 				};
 
 				// Add this undoable edit to the undo manager
 				Judas.getUndoManager().addEdit(undoableEdit);
 
 				// Update the state of the menu items
 				Judas.getMntmUndo()
 						.setEnabled(Judas.getUndoManager().canUndo());
 				Judas.getMntmRedo()
 						.setEnabled(Judas.getUndoManager().canRedo());
 			}
 		});
 		mnGraph.add(chckbxShowVerticalTracking);
 
 		chckbxTrackAdditionalVariables = new JCheckBox(
 				"Track Additional Variables");
 		chckbxTrackAdditionalVariables.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// Create a new undo action
 				// Save our current state in a final boolean so it is accessible
 				// in the inner class below
 				final boolean curState = chckbxTrackAdditionalVariables
 						.isSelected();
 				UndoableEdit undoableEdit = new AbstractUndoableEdit() {
 
 					// Method that is called when we must redo the undone action
 					public void redo()
 							throws javax.swing.undo.CannotRedoException {
 						super.redo();
 
 						// Set the checkbox to its pre-undone state
 						chckbxTrackAdditionalVariables.setSelected(curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 
 					public void undo()
 							throws javax.swing.undo.CannotUndoException {
 						super.undo();
 
 						// Set the checkbox to its previous state
 						chckbxTrackAdditionalVariables.setSelected(!curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 				};
 
 				// Add this undoable edit to the undo manager
 				Judas.getUndoManager().addEdit(undoableEdit);
 
 				// Update the state of the menu items
 				Judas.getMntmUndo()
 						.setEnabled(Judas.getUndoManager().canUndo());
 				Judas.getMntmRedo()
 						.setEnabled(Judas.getUndoManager().canRedo());
 			}
 		});
 		mnGraph.add(chckbxTrackAdditionalVariables);
 
 		chckbxRubberBandFor = new JCheckBox("Rubber Band for X-Only");
 		chckbxRubberBandFor.setSelected(true);
 		chckbxRubberBandFor.addActionListener(new ActionListener() {
 			/*
 			 * public void actionPerformed(ActionEvent e) {
 			 * System.out.println("Rubber Band for X-Only");
 			 * chckbxRubberBandFor.
 			 * setSelected(chckbxRubberBandFor.isSelected()); }
 			 */
 			public void actionPerformed(ActionEvent arg0) {
 				// Create a new undo action
 				// Save our current state in a final boolean so it is accessible
 				// in the inner class below
 				final boolean curState = chckbxRubberBandFor.isSelected();
 				UndoableEdit undoableEdit = new AbstractUndoableEdit() {
 
 					// Method that is called when we must redo the undone action
 					public void redo()
 							throws javax.swing.undo.CannotRedoException {
 						super.redo();
 
 						// Set the checkbox to its pre-undone state
 						chckbxRubberBandFor.setSelected(curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 
 					public void undo()
 							throws javax.swing.undo.CannotUndoException {
 						super.undo();
 
 						// Set the checkbox to its previous state
 						chckbxRubberBandFor.setSelected(!curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 				};
 
 				// Add this undoable edit to the undo manager
 				Judas.getUndoManager().addEdit(undoableEdit);
 
 				// Update the state of the menu items
 				Judas.getMntmUndo()
 						.setEnabled(Judas.getUndoManager().canUndo());
 				Judas.getMntmRedo()
 						.setEnabled(Judas.getUndoManager().canRedo());
 			}
 		});
 		mnGraph.add(chckbxRubberBandFor);
 
 		JSeparator separator_12 = new JSeparator();
 		mnGraph.add(separator_12);
 
 		chckbxQueryForMarker = new JCheckBox("Query for Marker Title");
 		chckbxQueryForMarker.setSelected(true);
 		chckbxQueryForMarker.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// Create a new undo action
 				// Save our current state in a final boolean so it is accessible
 				// in the inner class below
 				final boolean curState = chckbxQueryForMarker.isSelected();
 				UndoableEdit undoableEdit = new AbstractUndoableEdit() {
 
 					// Method that is called when we must redo the undone action
 					public void redo()
 							throws javax.swing.undo.CannotRedoException {
 						super.redo();
 
 						// Set the checkbox to its pre-undone state
 						chckbxQueryForMarker.setSelected(curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 
 					public void undo()
 							throws javax.swing.undo.CannotUndoException {
 						super.undo();
 
 						// Set the checkbox to its previous state
 						chckbxQueryForMarker.setSelected(!curState);
 
 						// Update our menu items
 						Judas.getMntmUndo().setEnabled(
 								Judas.getUndoManager().canUndo());
 						Judas.getMntmRedo().setEnabled(
 								Judas.getUndoManager().canRedo());
 					}
 				};
 
 				// Add this undoable edit to the undo manager
 				Judas.getUndoManager().addEdit(undoableEdit);
 
 				// Update the state of the menu items
 				Judas.getMntmUndo()
 						.setEnabled(Judas.getUndoManager().canUndo());
 				Judas.getMntmRedo()
 						.setEnabled(Judas.getUndoManager().canRedo());
 			}
 		});
 		mnGraph.add(chckbxQueryForMarker);
 
 		JSeparator separator_13 = new JSeparator();
 		mnGraph.add(separator_13);
 
 		JMenuItem mntmPlotlayoutOptions = new JMenuItem(
 				"Plot/Layout Options...");
 		mntmPlotlayoutOptions.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Plot/Layout Options...");
 				PlotLayoutOptions plotLayoutOptions = new PlotLayoutOptions();
 				plotLayoutOptions.setLocationRelativeTo(null);
 				plotLayoutOptions.setVisible(true);
 			}
 		});
 		mnGraph.add(mntmPlotlayoutOptions);
 
 		JMenuItem mntmPageOptions = new JMenuItem("Page Options...");
 		mntmPageOptions.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Page Options...");
 				PageOptions pageOptions = new PageOptions();
 				pageOptions.setLocationRelativeTo(null);
 				pageOptions.setVisible(true);
 			}
 		});
 		mnGraph.add(mntmPageOptions);
 
 		JMenuItem mntmPanelOptions = new JMenuItem("Panel Options...");
 		mntmPanelOptions.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Panel Options...");
 				PanelOptions panelOptions = new PanelOptions();
 				panelOptions.setLocationRelativeTo(null);
 				panelOptions.setVisible(true);
 			}
 		});
 		mnGraph.add(mntmPanelOptions);
 
 		JMenuItem mntmLineOptions = new JMenuItem("Line Options...");
 		mntmLineOptions.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Line Options...");
 				LineOptions lineOptions = new LineOptions();
 				lineOptions.setLocationRelativeTo(null);
 				lineOptions.setVisible(true);
 			}
 		});
 		mnGraph.add(mntmLineOptions);
 
 		JMenuItem mntmXAxisOptions = new JMenuItem("X Axis Options...");
 		mntmXAxisOptions.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("X Axis Options...");
 				XAxisOptions xAxisOptions = new XAxisOptions();
 				xAxisOptions.setLocationRelativeTo(null);
 				xAxisOptions.setVisible(true);
 			}
 		});
 		mnGraph.add(mntmXAxisOptions);
 
 		JMenuItem mntmYAxisOptions = new JMenuItem("Y Axis Options...");
 		mntmYAxisOptions.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Y Axis Options...");
 				YAxisOptions yAxisOptions = new YAxisOptions();
 				yAxisOptions.setLocationRelativeTo(null);
 				yAxisOptions.setVisible(true);
 			}
 		});
 		mnGraph.add(mntmYAxisOptions);
 
 		JMenuItem mntmZAxisOptions = new JMenuItem("Z Axis Options");
 		mntmZAxisOptions.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Z Axis Options...");
 				ZAxisOptions zAxisOptions = new ZAxisOptions();
 				zAxisOptions.setLocationRelativeTo(null);
 				zAxisOptions.setVisible(true);
 			}
 		});
 		mnGraph.add(mntmZAxisOptions);
 
 		// JMenuItem mntmVariableOptions = new JMenuItem("Variable Options...");
 		JMenuItem mntmVariableOptions = new JMenuItem("Variable Options...");
 		mntmVariableOptions.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Variable Options...");
 				VariableOptions variableOptions = new VariableOptions();
 				variableOptions.setLocationRelativeTo(null);
 				variableOptions.setVisible(true);
 
 				System.out.println("hoge");
 			}
 		});
 		mnGraph.add(mntmVariableOptions);
 
 		JMenu mnAnalysis = new JMenu("Analysis");
 		menuBar.add(mnAnalysis);
 
 		JMenuItem mntmCalculate = new JMenuItem("Calculate...");
 
 		mntmCalculate.setIcon(icons.getImageIcon("icons/copy20.png"));
 		mntmCalculate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Calculate...");
 				Calculate calculate = new Calculate();
 				calculate.setLocationRelativeTo(null);
 				calculate.setVisible(true);
 			}
 		});
 		mnAnalysis.add(mntmCalculate);
 
 		JMenuItem mntmNudgeTraces = new JMenuItem("Nudge Traces");
 		mntmNudgeTraces.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Nudge Traces");
 				NudgeTraces nudgeTraces = new NudgeTraces();
 				nudgeTraces.setLocationRelativeTo(null);
 				nudgeTraces.setVisible(true);
 			}
 		});
 		mnAnalysis.add(mntmNudgeTraces);
 
 		JMenuItem mntmDataProcessing = new JMenuItem("Data Processing...");
 
 		mntmDataProcessing.setIcon(icons.getImageIcon("icons/arrow-1-left20.png"));
 		mntmDataProcessing.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Data Processing...");
 				DataProcessing dataProcessing = new DataProcessing();
 				dataProcessing.setLocationRelativeTo(null);
 				dataProcessing.setVisible(true);
 			}
 		});
 		mnAnalysis.add(mntmDataProcessing);
 
 		JMenuItem mntmVelocitySlices = new JMenuItem("Velocity Slices...");
 		mntmVelocitySlices.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Velocity Slices...");
 				VelocitySlices velocitySlices = new VelocitySlices();
 				velocitySlices.setLocationRelativeTo(null);
 				velocitySlices.setVisible(true);
 			}
 		});
 		mnAnalysis.add(mntmVelocitySlices);
 
 		JMenu mnPages = new JMenu("Pages");
 		menuBar.add(mnPages);
 
 		JMenuItem mntmNew = new JMenuItem("New");
 		mntmNew.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("New");
 			}
 		});
 		mnPages.add(mntmNew);
 
 		JMenuItem mntmClose = new JMenuItem("Close");
 		mntmClose.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Close");
 			}
 		});
 		mnPages.add(mntmClose);
 
 		JSeparator separator_14 = new JSeparator();
 		mnPages.add(separator_14);
 
 		JRadioButton rdbtnPage = new JRadioButton("Page: 1");
 		rdbtnPage.setSelected(true);
 		mnPages.add(rdbtnPage);
 
 		JMenu mnHelp = new JMenu("Help");
 		menuBar.add(mnHelp);
 
 		JMenuItem mntmHelpWindow = new JMenuItem("Help Window...");
 		mntmHelpWindow.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Help Window...");
 				HelpWindow helpWindow = new HelpWindow();
 				helpWindow.setLocationRelativeTo(null);
 				helpWindow.setVisible(true);
 			}
 		});
 		mnHelp.add(mntmHelpWindow);
 
 		JMenuItem mntmHelpRequestForm = new JMenuItem("Help Request Form...");
 
 		mntmHelpRequestForm.setIcon(icons.getImageIcon("icons/info20.png"));
 		mntmHelpRequestForm.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Help Request Form...");
 				HelpRequestForm helpRequestForm = new HelpRequestForm();
 				helpRequestForm.setLocationRelativeTo(null);
 				helpRequestForm.setVisible(true);
 			}
 		});
 		mnHelp.add(mntmHelpRequestForm);
 
 		JMenuItem mntmOverviewPlotKey = new JMenuItem("Overview Plot Key...");
 		mntmOverviewPlotKey.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Overview Plot Key...");
 				OverviewPlotKey overviewPlotKey = new OverviewPlotKey();
 				overviewPlotKey.setLocationRelativeTo(null);
 				overviewPlotKey.setVisible(true);
 			}
 		});
 		mnHelp.add(mntmOverviewPlotKey);
 
 		JPanel panel_5 = new JPanel();
 		contentPane.add(panel_5);
 		panel_5.setPreferredSize(new Dimension(900, 40));
 		panel_5.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
 
 		mntmOpenThemisDocument.setIcon(icons.getImageIcon("icons/folder20.png"));
 		
 		JButton btnOpenThemisDocument = new JButton(icons.getImageIcon("icons/folder20.png"));
 		btnOpenThemisDocument.setForeground(Color.BLACK);
 		btnOpenThemisDocument.setToolTipText("Open File");
 		btnOpenThemisDocument.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("(Button)Open THEMIS Document...");
 				JFileChooser filechooser = new JFileChooser();
 				filechooser.setDialogTitle("Open THEMIS Document...");
 				for (int i = 0; i < filterData.length; i++) {
 					filechooser.addChoosableFileFilter(filterData[i]);
 				}
 				for (int i = 0; i < filterMetadata.length; i++) {
 					filechooser.addChoosableFileFilter(filterMetadata[i]);
 				}
 				filechooser.setFileFilter(filterData[0]);
 
 				System.out.println(filechooser.getFileFilter());
 				int selected = filechooser.showOpenDialog(rootPane);
 
 				if (selected == JFileChooser.APPROVE_OPTION) {
 					File file = filechooser.getSelectedFile();
 
 				}
 			}
 		});
 		btnOpenThemisDocument.setPreferredSize(new Dimension(54, 30));
 		panel_5.add(btnOpenThemisDocument);
 
 		mntmSaveThemisDocument.setIcon(icons.getImageIcon("icons/save20.png"));
 		
 		JButton btnSaveThemisDocument = new JButton(icons.getImageIcon("icons/save20.png"));
 		btnSaveThemisDocument.setToolTipText("Save File");
 		btnSaveThemisDocument.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("(Button)Save THEMIS Document...");
 				JFileChooser filechooser = new JFileChooser();
 				filechooser.setDialogTitle("Save THEMIS Document...");
 				for (int i = 0; i < filterData.length; i++) {
 					filechooser.addChoosableFileFilter(filterData[i]);
 				}
 				for (int i = 0; i < filterMetadata.length; i++) {
 					filechooser.addChoosableFileFilter(filterMetadata[i]);
 				}
 				filechooser.setFileFilter(filterData[0]);
 
 				int selected = filechooser.showOpenDialog(rootPane);
 
 				if (selected == JFileChooser.APPROVE_OPTION) {
 					File file = filechooser.getSelectedFile();
 				}
 			}
 		});
 		btnSaveThemisDocument.setPreferredSize(new Dimension(54, 30));
 		panel_5.add(btnSaveThemisDocument);
 
 		JButton btnPrint = new JButton(icons.getImageIcon("icons/print20.png"));
 		btnPrint.setToolTipText("Print");
 		btnPrint.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("(Button)Print...");
 				PrinterJob printerJob = PrinterJob.getPrinterJob();
 				printerJob.printDialog();
 			}
 		});
 		btnPrint.setPreferredSize(new Dimension(54, 30));
 		panel_5.add(btnPrint);
 		PrinterJob printerJob = PrinterJob.getPrinterJob();
 		
 		JButton btnCopyToClipboard = new JButton(icons.getImageIcon("icons/copy20.png"));
 		btnCopyToClipboard.setToolTipText("Copy to Clipboard");
 		btnCopyToClipboard.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("(Button)2 Panels");
 			}
 		});
 		btnCopyToClipboard.setPreferredSize(new Dimension(54, 30));
 		panel_5.add(btnCopyToClipboard);
 
 		JButton btnLoadData = new JButton(icons.getImageIcon("icons/unfold-multiple20.png"));
 		btnLoadData.setToolTipText("Load Data");
 		btnLoadData.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Load Data");
 
 				LoadData loadData = new LoadData();
 				loadData.setLocationRelativeTo(null);
 				loadData.setVisible(true);
 			}
 		});
 		btnLoadData.setPreferredSize(new Dimension(54, 30));
 		panel_5.add(btnLoadData);
 
 		JButton btnManageGuiDataAndImportAndExportTplot = new JButton(icons.getImageIcon("icons/settings20.png"));
 		btnManageGuiDataAndImportAndExportTplot
 				.setToolTipText("Manage GUI Data and Import and Export Tplot data");
 		btnManageGuiDataAndImportAndExportTplot
 				.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						System.out
 								.println("Manage Data and Import/Export Tplot...");
 						ManageDataAndImportExportTplot manageDataAndImportExportTplot = new ManageDataAndImportExportTplot();
 						manageDataAndImportExportTplot
 								.setLocationRelativeTo(null);
 						manageDataAndImportExportTplot.setVisible(true);
 					}
 				});
 		btnManageGuiDataAndImportAndExportTplot.setPreferredSize(new Dimension(
 				54, 30));
 		panel_5.add(btnManageGuiDataAndImportAndExportTplot);
 
 		JButton btnPlotData = new JButton(icons.getImageIcon("icons/activity20.png"));
 		btnPlotData.setToolTipText("Plot Data");
 		btnPlotData.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("(Button)6");
 			}
 		});
 		btnPlotData.setPreferredSize(new Dimension(54, 30));
 		panel_5.add(btnPlotData);
 
 		JButton btnCreateNewPage = new JButton(icons.getImageIcon("icons/window-stack20.png"));
 		btnCreateNewPage.setToolTipText("Create New Page");
 		btnCreateNewPage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Create New Page");
 			}
 		});
 		btnCreateNewPage.setPreferredSize(new Dimension(54, 30));
 		panel_5.add(btnCreateNewPage);
 
 		JButton btnHelp = new JButton(icons.getImageIcon("icons/info20.png"));
 		btnHelp.setToolTipText("Help");
 		btnHelp.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Help Window");
 				HelpWindow helpWindow = new HelpWindow();
 				helpWindow.setLocationRelativeTo(null);
 				helpWindow.setVisible(true);
 			}
 		});
 		btnHelp.setPreferredSize(new Dimension(54, 30));
 		panel_5.add(btnHelp);
 
 		textField = new JTextField();
 		textField.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("TextField");
 				String str = textField.getText();
 				for (int i = 0; i < plotPane.length; i++) {
 					plotPane[0].setScaleSize(Integer.parseInt(str));
 				}
 			}
 		});
 		textField.setText("100");
 		panel_5.add(textField);
 		textField.setColumns(3);
 
 		JLabel label = new JLabel("%");
 		panel_5.add(label);
 
 		JPanel panel_3 = new JPanel();
 		panel_3.setPreferredSize(new Dimension(54, 30));
 		panel_5.add(panel_3);
 		panel_3.setLayout(new GridLayout(2, 0, 0, 0));
 
 		JButton btnUp = new JButton(icons.getImageIcon("icons/arrow-3-up8.png"));
 		btnUp.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Up");
 				for (int i = 0; i < plotPane.length; i++) {
 					plotPane[0].addScaleSize();
 				}
 			}
 		});
 		btnUp.setPreferredSize(new Dimension(54, 15));
 		panel_3.add(btnUp);
 
 		JButton btnDown = new JButton(icons.getImageIcon("icons/arrow-3-down8.png"));
 		btnDown.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Down");
 				for (int i = 0; i < plotPane.length; i++) {
 					plotPane[i].subtractScaleSize();
 				}
 			}
 		});
 		btnDown.setPreferredSize(new Dimension(54, 15));
 		panel_3.add(btnDown);
 
 		JButton button = new JButton(icons.getImageIcon("icons/database20.png"));
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				openBrowser();
 			}
 
 			private void openBrowser() {
 				// TODO Auto-generated method stub
 				Desktop desktop = Desktop.getDesktop();
 				try {
 					desktop.browse(new URI("http://search.iugonet.org/iugonet"));
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (URISyntaxException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 
 		JButton btnJythonCommandLine = new JButton(icons.getImageIcon("icons/keyboard20.png"));
 		btnJythonCommandLine.setToolTipText("Jython Command Line");
 		btnJythonCommandLine.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("JyConsole");
 				Console console = new Console();
 				console.setLocationRelativeTo(null);
 				console.setVisible(true);
 			}
 		});
 		btnJythonCommandLine.setIcon(icons.getImageIcon("icons/keyboard20.png"));
 		panel_5.add(btnJythonCommandLine);
 		button.setToolTipText("IUGONET Metadata Database");
 		button.setIcon(icons.getImageIcon("icons/database20.png"));
 		panel_5.add(button);
 		//
 		panel = new JPanel();
 		panel.setPreferredSize(new Dimension(860, 770));
 		CardLayout layout = new CardLayout(0, 0);
 		panel.setLayout(layout);
 
 		JScrollPane scrollPane = new JScrollPane(panel);
 		scrollPane
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		contentPane.add(scrollPane);
 	}
 
 	private void checkConfig() {
 		System.out.println("checkConfig");
 		userHome = System.getProperty("user.home");
 		File file_1 = new File(userHome + "/.judas");
 		File file_2 = new File(userHome + "/.judas/judas.properties");
 
 		if (file_1.exists() == true && file_2.exists() == true) {
 			System.out.println("yomikomi");
 		} else if (file_1.exists() == true && file_2.exists() == false) {
 			createJudasProperties();
 			System.out.println("yomikomi");
 		} else if (file_1.exists() == false && file_2.exists() == false) {
 			createJudasFolder();
 			createJudasProperties();
 			System.out.println("yomikomi");
 		}
 	}
 
 	private void createJudasProperties() {
 		System.out.println("createJudasProperties");
 	}
 
 	private void createJudasFolder() {
 		File dir = new File(System.getProperty("user.home") + "/.judas");
 		dir.mkdir();
 	}
 
 	public void setScaleSizeTextField(int value) {
 		textField.setText(String.valueOf(value));
 	}
 	
 	/*
 	private Properties getProperties(String filename){
 		
 	}*/
 	
 }

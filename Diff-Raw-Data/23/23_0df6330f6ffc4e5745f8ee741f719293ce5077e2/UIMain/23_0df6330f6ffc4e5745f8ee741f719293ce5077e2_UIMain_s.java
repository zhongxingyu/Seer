 /*
  * UIMain.java (JavaVis)
  * Copyright 2010 Luke Plaster. All rights reserved.
  */
 package org.lukep.javavis.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 
 public class UIMain implements IProgramStatusReporter {
 
 	protected JFrame frmJavavis;
 	protected JTabbedPane mainTabbedPane;
 	protected JLabel mainStatusBar;
 	protected int workspaces = 0;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 			        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 					
 					UIMain window = new UIMain();
 					window.frmJavavis.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 * @throws Exception 
 	 */
 	public UIMain() throws Exception {
 		initialize();
 		addChildFrame();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 * @throws Exception 
 	 */
 	private void initialize() throws Exception {
 		frmJavavis = new JFrame();
 		frmJavavis.setTitle("JavaVis Software Quality Visualiser");
 		frmJavavis.setBounds(100, 100, 1024, 768);
 		frmJavavis.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		frmJavavis.setVisible(true);
 		frmJavavis.getContentPane().setLayout(new BorderLayout(0, 0));
 		
 		mainTabbedPane = new JTabbedPane();
 		frmJavavis.getContentPane().add(mainTabbedPane);
 		mainTabbedPane.setBackground(Color.LIGHT_GRAY);
 		
 		mainStatusBar = new JLabel("Ready");
 		frmJavavis.getContentPane().add(mainStatusBar, BorderLayout.SOUTH);
 		
 		JMenuBar menuBar = new JMenuBar();
 		frmJavavis.setJMenuBar(menuBar);
 		
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 		
		JMenuItem mntmNewCanvas = new JMenuItem("New canvas");
 		mntmNewCanvas.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				addChildFrame();
 			}
 		});
 		mnFile.add(mntmNewCanvas);
 		
		JMenuItem mntmOpenCodeDirectory = new JMenuItem("Open code directory...");
 		mntmOpenCodeDirectory.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser fc = new JFileChooser();
 				fc.setDialogTitle("Select a root directory containing source code");
 				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				fc.setMultiSelectionEnabled(false);
 				int returnVal = fc.showOpenDialog(null); // TODO: modify parent
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					Component selectedWorkspace = mainTabbedPane.getSelectedComponent();
 					VisualisationDesktopPane selectedVdp = (VisualisationDesktopPane)selectedWorkspace;
 					selectedVdp.loadCodeBase(fc.getSelectedFile());
 				}
 			}
 		});
 		mnFile.add(mntmOpenCodeDirectory);
 		
 		JMenuItem mntmExit = new JMenuItem("Exit");
 		mntmExit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 		mnFile.add(mntmExit);
 		
 		JMenu mnHelp = new JMenu("Help");
 		menuBar.add(mnHelp);
 		
 		JMenuItem mntmAbout = new JMenuItem("About");
 		mnHelp.add(mntmAbout);
 	}
 	
 	private void addChildFrame()
 	{
 		VisualisationDesktopPane visDesktop = null;
 		try {
 			visDesktop = new VisualisationDesktopPane(this);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		mainTabbedPane.addTab( "Workspace " + (++workspaces), visDesktop );
		mainTabbedPane.setSelectedIndex(workspaces-1);
 	}
 
 	@Override
 	public synchronized void setProgramStatus(String status) {
 		mainStatusBar.setText(status);
 		mainStatusBar.repaint();
 	}
 }

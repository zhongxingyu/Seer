 /*  Copyright (C) 2011  Nicholas Wright
 	
 	part of 'Aid', an imageboard downloader.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package gui;
 
 
 import java.awt.Button;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DecimalFormat;
 
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import board.Board;
 
 /**
  * Main GUI for the Application.<br/>
  * This Class is used to display all relevant data to the user.
  */
 public class Aid extends JFrame implements ActionListener, StatListener{
 	private final int STARTUP_DELAY = 7; // Delay in minutes between boards, when "Start all" is used
 	
 	/**
 	 * This Class is used to display all relevant data to the user.
 	 */
 	class StatusDisplay extends JPanel{
 		private static final long serialVersionUID = 1L;
 		
 		@Override
 		public void paintComponent(Graphics g){
 			for (int i = 0; i<boardList.getSize(); i++){
 				g.drawString(boardList.getStatus(i), 5, (i*15)+10);
 			}
 			g.drawString(Stats.getPageQueueState(), 5, 95);
 			g.drawString(Stats.getFileQueueState(), 5, 105);
 
 			//			g.drawString("Queued Pages: "+Integer.toString(Stats.getPageQueueSize())+" - "+Integer.toString(Stats.getPageQueueActive())+"/"+Integer.toString(pageQueue.getPoolSize()), 5, 95);
 			//			g.drawString("Queued Files: "+Integer.toString(imageQueue.getQueueSize())+" - "+Integer.toString(imageQueue.getActiveTasks())+"/"+Integer.toString(imageQueue.getPoolSize()), 5, 105);
 
 			DecimalFormat df = new DecimalFormat( "###,##0.000" ); 
 
 			if (Stats.getBytesSaved() < 1024*1048576)
 				g.drawString("Data saved: "+df.format(Stats.getBytesSaved()/1048576.0)+" mb", 5, 125);
 			else
 				g.drawString("Data saved: "+df.format(Stats.getBytesSaved()/1048576.0/1024.0)+" gb", 5, 125);
 
 			if (Stats.getBytesDiscarded() < 1024*1048576)
 				g.drawString("Data discarded: "+df.format(Stats.getBytesDiscarded()/1048576.0)+" mb", 5, 135);
 			else
 				g.drawString("Data discarded: "+df.format(Stats.getBytesDiscarded()/1048576.0/1024.0)+" gb", 5, 135);
 
 			//	g.drawString("pending : "+fileWriter.getPendingWrites(), 160, 125);
 		}
 	}
 
 	private Button btnFilter = new Button("Filter (0)");
 	private Button btnRun = new Button("Run");
 	private Button btnStartAll = new Button("Start All");
 	private Button btnStop = new Button("Stop");
 	private Button btnStopAll = new Button("Stop All");
 
 	private StatusDisplay statusDisplay = new StatusDisplay();	// Program status is displayed here in Text
 
 	private static final long serialVersionUID = 1L;
 
 	private JMenuBar jMenuBar = new JMenuBar();
 
 	private JMenuItem jMenuItemClearImage = new JMenuItem("Clear ImageQueue");
 	private JMenuItem jMenuItemClearLog = new JMenuItem("Clear log");
 	private JMenuItem jMenuItemClearPage = new JMenuItem("Clear PageQueue");
 	private JMenuItem jMenuItemClearStats = new JMenuItem("Clear stats");
 	private JMenuItem jMenuItemFilterlist = new JMenuItem("Filterlist");
 	private JMenuItem jMenuItemPruneCache = new JMenuItem("Prune cache");
 	private JMenuItem jMenuItemSkipOutput = new JMenuItem("Log skipped files");
 
 	private JMenu jSystemMenu = new JMenu("System");
 	private JMenu jDebugMenu = new JMenu("Debug");
 
	private JTextArea logArea = new JTextArea();
	private JScrollPane logScroll;
 
 	private JList<Board> lstBoards;	// GUI List for Boards
 	private BoardListDataModel boardList;
 
 	private JPanel mainPanel = new JPanel();
 	private JPanel panButton = new JPanel();   // group Buttons
 	private JPanel panControl = new JPanel();
 	private DataGraph dg = new DataGraph(100,100,50,2,5);
 
 	private JTextField txtKeys = new JTextField(20);
 
 	public Aid(BoardListDataModel listModel, ActionListener parent){
 		boardList = listModel;
		
 		Log.setLogArea(logArea);
 		// to prevent the action command from changing when the label is updated
 		btnFilter.setActionCommand("Filter"); 
 
 		// Set up menu Bars
 		this.setJMenuBar(jMenuBar);
 		jMenuBar.setVisible(true);
 
 		jMenuBar.add(jSystemMenu);
 		jMenuBar.add(jDebugMenu);
 
 		jSystemMenu.add(jMenuItemClearPage);
 		jSystemMenu.add(jMenuItemClearImage);
 		jSystemMenu.add(jMenuItemClearStats);
 		jSystemMenu.add(jMenuItemClearLog);
 		jSystemMenu.add(jMenuItemFilterlist);
 		jSystemMenu.add(jMenuItemPruneCache);
 
 		jDebugMenu.add(jMenuItemSkipOutput);
 
 		jMenuItemClearPage.addActionListener(parent);
 		jMenuItemClearImage.addActionListener(parent);
 		jMenuItemClearStats.addActionListener(parent);
 		jMenuItemFilterlist.addActionListener(parent);
 		jMenuItemPruneCache.addActionListener(parent);
 		jMenuItemClearLog.addActionListener(this);
 		jMenuBar.validate();
 		jMenuBar.repaint();
 
 		mainPanel.setSize(500, 300);	// the Main content Pane, to avoid pesky unfilled areas
 		mainPanel.setLayout(null);
 		add(mainPanel);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setSize(mainPanel.getSize());
 		this.setTitle("Another Imageboard Downloader");
 
 		this.setLayout(null);
 
 		statusDisplay.setForeground(Color.DARK_GRAY);
 		statusDisplay.setForeground(Color.BLACK);
 		statusDisplay.setBounds(0, 0, 230, 140);
 		mainPanel.add(statusDisplay);
 
 		lstBoards = new JList<>(listModel);
 
 		lstBoards.setBounds(230, 0, 50, 100);
 		mainPanel.add(lstBoards);
 
 		btnRun.addActionListener(this);
 		btnStop.addActionListener(this);
 		btnFilter.addActionListener(parent);
 		btnStartAll.addActionListener(this);
 		btnStopAll.addActionListener(this);
 
 		// Hash Key Display
 		txtKeys.setText("");
 		txtKeys.setEditable(false);
 
 		//Control Button Group
 		panButton.setLayout(new GridLayout(2,2));
 		panButton.add(btnRun);
 		panButton.add(btnStop);
 		panButton.add(btnFilter);
 		panButton.add(btnStopAll);
 		/////////////////////////////
 
 		//Control Panel Group
 		panControl.setLayout(new GridLayout(3,1));
 		panControl.add(txtKeys);
 		panControl.add(panButton);
 		panControl.add(btnStartAll);
 		panControl.setBounds(280, 0, 110, 100);
 		mainPanel.add(panControl);
 		////////////////////////////
 
		logArea.setBounds(0,150,mainPanel.getWidth()-15,90);
		logScroll = new JScrollPane(logArea);
 		logScroll.setBounds(0,150,mainPanel.getWidth()-15,90);
 		logArea.setText("");
 		logArea.setEditable(false);
 		mainPanel.add(logScroll);
 		
 
 		dg.setLocation(this.getWidth()-dg.getWidth()-5, 0);
 		mainPanel.add(dg);
 		dg.start();
 		mainPanel.validate();
 
 		validate();
 		repaint();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if("Start All".equals(e.getActionCommand())){
 			int startDelay = 0;
 			for (Object o : boardList.toArray()){
 				((Board)o).start(startDelay);
 				startDelay += STARTUP_DELAY;
 			}
 			this.paintComponents(getGraphics());
 		}
 		
 		if("Stop All".equals(e.getActionCommand())){
 			for (Object o : boardList.toArray()){
 				((Board)o).stop();			}
 			this.paintComponents(getGraphics());
 		}
 
 		if("Stop".equals(e.getActionCommand())){
 			if(lstBoards.getSelectedIndex() != -1)
 				boardList.getElementAt(lstBoards.getSelectedIndex()).stop();
 			this.paintComponents(getGraphics());
 		}
 
 		if("Run".equals(e.getActionCommand())){
 			if(lstBoards.getSelectedIndex() != -1)
 				boardList.getElementAt(lstBoards.getSelectedIndex()).start();
 			this.paintComponents(getGraphics());
 		}
 		
 		if("Clear log".equals(e.getActionCommand())){
 			Log.clear();
 		}
 		
 		
 	}
 
 	@Override
 	public void statChanged(String stat) {
 		if("resetLog".equals(stat)){
 			Log.clear();
 		}else if("filterSize".equals(stat)){
 			btnFilter.setLabel("Filter ("+Stats.getFilterSize()+")");
 		}else if("cacheSize".equals(stat)){
 			txtKeys.setText("URLs: "+Stats.getCacheSize());
 		}else if("timeGraphValue".equals(stat)){
 			dg.add(Stats.getTimeGraphValue());
 		}
 		repaint();
 	}	
 }

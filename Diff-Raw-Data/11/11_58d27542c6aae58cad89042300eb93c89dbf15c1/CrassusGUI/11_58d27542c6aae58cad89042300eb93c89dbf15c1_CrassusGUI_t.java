 /**
  * 
  */
 package edu.brown.cs32.atian.crassus.gui.mainwindow;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import javax.swing.border.EtchedBorder;
 
 import edu.brown.cs32.atian.crassus.backend.Stock;
 import edu.brown.cs32.atian.crassus.backend.StockFreqType;
 import edu.brown.cs32.atian.crassus.backend.StockList;
 import edu.brown.cs32.atian.crassus.gui.TimeFrame;
 import edu.brown.cs32.atian.crassus.gui.dialogs.DotCrassusFileGui;
 import edu.brown.cs32.atian.crassus.gui.mainwindow.plot.CrassusPlotPane;
 import edu.brown.cs32.atian.crassus.gui.mainwindow.table.indicator.CrassusIndicatorTablePane;
 import edu.brown.cs32.atian.crassus.gui.mainwindow.table.indicator.TestIndicator;
 import edu.brown.cs32.atian.crassus.gui.mainwindow.table.stock.CrassusStockTablePane;
 import edu.brown.cs32.atian.crassus.gui.undoable.UndoableStack;
 import edu.brown.cs32.atian.crassus.gui.undoable.UndoableStackListener;
 
 /**
  * @author Matthew
  *
  */
 public class CrassusGUI implements GUI {
 	
 	private class RefreshPlotListener implements CrassusStockWasAlteredListener {
 		
 		@Override
 		public void plotChanged() {
 			plotPane.refresh();
 		}
 
 		@Override
 		public void stockBoxChanged() {
 			stockBox.refresh();
 		}
 	}
 
 	private class CompoundChangeStockListener implements CrassusChangeStockListener {
 		
 		@Override 
 		public void changeToStock(Stock stock) {
 			plotPane.changeToStock(stock);
 			indicatorBox.changeToStock(stock);
 		}
 	}
 
 	private JFrame frame;
 	
 	private CrassusPlotPane plotPane;
 	
 	private CrassusStockTablePane stockBox;
 	private CrassusIndicatorTablePane indicatorBox;
 	
 	private JPanel stockInfo;
 	
 	private DotCrassusFileGui fileGui;
 	
 	private UndoableStack undoables;
 	private JMenuItem mUndo;
 	private JMenuItem mRedo;
 	
 	private class UndoStateListener implements UndoableStackListener{
 		@Override
 		public void changeUndo(String string) {
 			if(string==null){ 
 				mUndo.setText("Undo");
 				mUndo.setEnabled(false);  
 			}
 			else{ 
 				mUndo.setText("Undo "+string);
 				mUndo.setEnabled(true);  
 			}
 		}
 		@Override
 		public void changeRedo(String string) {
 			if(string==null){  
 				mRedo.setText("Redo");  
 				mRedo.setEnabled(false);  }
 			else{  
 				mRedo.setText("Redo "+string); 
 				mRedo.setEnabled(true);  
 			}
 		}
 	};
 
 	public CrassusGUI() {
 		frame = new JFrame("Crassus");
 		
 		undoables = new UndoableStack(
 				32,new UndoStateListener());
 
 		fileGui = new DotCrassusFileGui(frame,this);
 		
 		
 		try {
 			BufferedImage img = ImageIO.read(new File("img/crassus.png"));
 			frame.setIconImage(img);
 		} catch (IOException e) {}//not a disaster, can be ignored....
 		
 		frame.getContentPane().setBackground(Color.WHITE);
 		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 		stockBox = new CrassusStockTablePane(frame,undoables);
 		stockBox.setChangeStockListener(new CompoundChangeStockListener());
 
 		indicatorBox = new CrassusIndicatorTablePane(frame,undoables, new RefreshPlotListener());
 		
 		//make plot pane
 		plotPane = new CrassusPlotPane(undoables);
 		
 		
 		stockInfo = new JPanel();
 		stockInfo.setBackground(Color.WHITE);
 		stockInfo.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createEmptyBorder(20,20,20,20),
 				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
 		stockInfo.setLayout(new BorderLayout());
 		stockInfo.add(indicatorBox, BorderLayout.EAST);
 		stockInfo.add(plotPane,BorderLayout.CENTER);
 		
 		frame.add(stockBox, BorderLayout.WEST);
 		frame.add(stockInfo,BorderLayout.CENTER);
 		frame.setMinimumSize(new Dimension(900,500));
 		
 		frame.addWindowListener(
 				new WindowListener(){
 					@Override public void windowActivated(WindowEvent arg0) {}
 					@Override public void windowClosed(WindowEvent arg0) {}
 					
 					@Override 
 					public void windowClosing(WindowEvent arg0) {
 						possiblyExit();
 					}
 					
 					@Override public void windowDeactivated(WindowEvent arg0) {}
 					@Override public void windowDeiconified(WindowEvent arg0) {}
 					@Override public void windowIconified(WindowEvent arg0) {}
 					@Override public void windowOpened(WindowEvent arg0) {}
 				});
 		
 		
 
 		//MAKE THE FREAKING MENU
 		
 		JMenuBar menuBar = new JMenuBar();
 		
 		//ADDING FILE MENU
 		
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.setMnemonic(KeyEvent.VK_F1);
 		menuBar.add(fileMenu);
 		
 		JMenuItem mNew = new JMenuItem("New");
 		mNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.CTRL_MASK));
 		mNew.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent arg0) {changeStockListTo(fileGui.fileNew());}
 				});
 		fileMenu.add(mNew);
 
 		JMenuItem mOpen = new JMenuItem("Open");
 		mOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK));
 		mOpen.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent arg0) {changeStockListTo(fileGui.fileOpen());}
 				});
 		fileMenu.add(mOpen);
 		
 		JMenuItem mSave = new JMenuItem("Save");
 		mSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
 		mSave.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent arg0) {fileGui.fileSave();undoables.clearChangeState();}
 				});
 		fileMenu.add(mSave);
 		
 		JMenuItem mSaveAs = new JMenuItem("Save As");
 		mSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK|ActionEvent.ALT_MASK));
 		mSaveAs.addActionListener(
 				new ActionListener(){ @Override
 					public void actionPerformed(ActionEvent arg0) {fileGui.fileSaveAs();undoables.clearChangeState();}
 				});
 		fileMenu.add(mSaveAs);
 		
 		fileMenu.addSeparator();
 		
 		JMenuItem mExit = new JMenuItem("Exit");
 		mExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0));
 		mExit.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent arg0) {possiblyExit();}
 				});
 		fileMenu.add(mExit);
 		
 		//ADDING EDIT MENU
 		
 		JMenu editMenu = new JMenu("Edit");
 		editMenu.setMnemonic(KeyEvent.VK_F2);
 		menuBar.add(editMenu);
 		
 		mUndo = new JMenuItem("Undo");
 		mUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,InputEvent.CTRL_DOWN_MASK));
 		mUndo.addActionListener(
 				new ActionListener(){@Override 
 					public void actionPerformed(ActionEvent e) {undoables.undo();}
 				});
 		editMenu.add(mUndo);
 		mUndo.setEnabled(false);
 		
 		mRedo = new JMenuItem("Redo");
 		mRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK));
 		mRedo.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {undoables.redo();}
 				});
 		editMenu.add(mRedo);
 		mRedo.setEnabled(false);
 		
 		//ADDING TICKER MENU
 		
 		JMenu tickerMenu = new JMenu("Tickers");
 		tickerMenu.setMnemonic(KeyEvent.VK_F3);
 		menuBar.add(tickerMenu);
 		
 		JMenuItem mAddTicker = new JMenuItem("Add New Ticker");
 		mAddTicker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,InputEvent.CTRL_DOWN_MASK));
 		mAddTicker.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {stockBox.showNewTickerDialog();}
 				});
 		tickerMenu.add(mAddTicker);
 		
 		JMenuItem mRemoveTicker = new JMenuItem("Remove Selected Ticker");
 		mRemoveTicker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK));
 		//mRemoveTicker.addActionListener(new TickerRemoveTickerListener());
 		mRemoveTicker.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {stockBox.removeSelectedTicker();}
 				});
 		tickerMenu.add(mRemoveTicker);
 		
 		//TODO sorting stuff, possibly selection stuff (not sure)
 		
 		//ADDING PLOT MENU
 		
 		JMenu plotMenu = new JMenu("Plot");
 		plotMenu.setMnemonic(KeyEvent.VK_F4);
 		menuBar.add(plotMenu);
 		
 		JMenuItem mRefresh = new JMenuItem("Refresh");
 		mRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_DOWN_MASK));
 		mRefresh.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {plotPane.refresh();}
 				});
 		plotMenu.add(mRefresh);
 		
 		plotMenu.addSeparator();
 		
 		JMenuItem mSetTimeScaleOneHour = new JMenuItem("Plot One Hour");
 		mSetTimeScaleOneHour.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {plotPane.changeTimeFrame(TimeFrame.HOURLY);}
 				});
 		
 		JMenuItem mSetTimeScaleOneDay = new JMenuItem("Plot One Day");
 		mSetTimeScaleOneDay.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {plotPane.changeTimeFrame(TimeFrame.DAILY);}
 				});
 		plotMenu.add(mSetTimeScaleOneDay);
 		
 //		JMenuItem mSetTimeScaleOneWeek = new JMenuItem("Plot One Week");
 //		mSetTimeScaleOneWeek.addActionListener(
 //				new ActionListener(){@Override
 //					public void actionPerformed(ActionEvent e) {plotPane.changeTimeFrame(TimeFrame.WEEKLY);}
 //				});
 //		plotMenu.add(mSetTimeScaleOneWeek);
 		
 		JMenuItem mSetTimeScaleOneMonth = new JMenuItem("Plot One Month");
 		mSetTimeScaleOneMonth.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {plotPane.changeTimeFrame(TimeFrame.MONTHLY);}
 				});
 		plotMenu.add(mSetTimeScaleOneMonth);
 		
 		JMenuItem mSetTimeScaleOneYear = new JMenuItem("Plot One Year");
 		mSetTimeScaleOneYear.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {plotPane.changeTimeFrame(TimeFrame.YEARLY);}
 				});
 		plotMenu.add(mSetTimeScaleOneYear);
 		
 		JMenuItem mSetTimeScaleFiveYears = new JMenuItem("Plot Five Years");
 		mSetTimeScaleFiveYears.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {plotPane.changeTimeFrame(TimeFrame.FIVE_YEAR);}
 				});
 		plotMenu.add(mSetTimeScaleFiveYears);
 		
 		plotMenu.addSeparator();
 		
 		JMenuItem mSetTimeFreqMinutely = new JMenuItem("Use Intervals Of One Minute");
 		mSetTimeFreqMinutely.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {plotPane.changeTimeFreq(StockFreqType.MINUTELY);}
 				});
 		plotMenu.add(mSetTimeFreqMinutely);
 		
 		JMenuItem mSetTimeFreqDaily = new JMenuItem("Use Intervals Of One Day");
 		mSetTimeFreqDaily.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {plotPane.changeTimeFreq(StockFreqType.DAILY);}
 				});
 		plotMenu.add(mSetTimeFreqDaily);
 		
 		JMenuItem mSetTimeFreqWeekly = new JMenuItem("Use Intervals Of One Week");
 		mSetTimeFreqWeekly.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {plotPane.changeTimeFreq(StockFreqType.WEEKLY);}
 				});
 		plotMenu.add(mSetTimeFreqWeekly);
 		
 		JMenuItem mSetTimeFreqMonthly = new JMenuItem("Use Intervals Of One Month");
 		mSetTimeFreqMonthly.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {plotPane.changeTimeFreq(StockFreqType.MONTHLY);}
 				});
 		plotMenu.add(mSetTimeFreqMonthly);
 		
 		plotPane.setMenuItems(mSetTimeFreqMinutely,mSetTimeFreqDaily,mSetTimeFreqWeekly,mSetTimeFreqMonthly);
 		
 		//ADDING INDICATOR MENU
 		
 		JMenu indicatorMenu = new JMenu("Indicators");
 		indicatorMenu.setMnemonic(KeyEvent.VK_5);
 		menuBar.add(indicatorMenu);
 		
 		JMenuItem mAddIndicator = new JMenuItem("Add New Indicator");
 		mAddIndicator.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,InputEvent.CTRL_DOWN_MASK));
 		mAddIndicator.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent arg0) {indicatorBox.showNewIndicatorDialog();}
 				});
 		indicatorMenu.add(mAddIndicator);
 		
 		JMenuItem mRemoveIndicator = new JMenuItem("Remove Selected Indicator");
 		mRemoveIndicator.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK));
 		mRemoveIndicator.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {indicatorBox.removeSelectedIndicator();}
 				});
 		indicatorMenu.add(mRemoveIndicator);
 		
 		JMenuItem mEditIndicator = new JMenuItem("Edit Selected Indicator");
 		mEditIndicator.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,InputEvent.CTRL_DOWN_MASK|InputEvent.ALT_DOWN_MASK));
 		mEditIndicator.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent arg0) {indicatorBox.showChangeIndicatorDialog();}
 				});
 		indicatorMenu.add(mEditIndicator);
 		
 		//TODO sorting for indicators
 		
 		JMenu developerMenu = new JMenu("Developer Tools");
 		menuBar.add(developerMenu);
 		
 		JMenuItem mAddTestIndicator = new JMenuItem("Add A Test Indicator");
 		mAddTestIndicator.addActionListener(
 				new ActionListener(){@Override
 					public void actionPerformed(ActionEvent e) {indicatorBox.addIndicator(new TestIndicator());}
 				});
 		developerMenu.add(mAddTestIndicator);
 		
 		frame.setJMenuBar(menuBar);
 		
 		//DONE SETTING UP THE MENU BAR!
 		
 	}
 	
 	public void changeStockListTo(StockList stocks) {
 
 		stockBox.changeStockListTo(stocks);
 
 		if(stocks.getStockList().isEmpty()){
 			plotPane.changeToStock(null);
 			indicatorBox.changeToStock(null);
 		}
 		else{
 			plotPane.changeToStock(stocks.getStockList().get(0));
 			indicatorBox.changeToStock(stocks.getStockList().get(0));
 		}
 		undoables.clear();
 	}
 
 
 	@Override
 	public void launch() {
 		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
 		frame.setVisible(true);
 		
 		changeStockListTo(fileGui.fileNew());
 	}
 	
 	private void notifyUser(){
 //		String[] options = {"Switch To Crassus","Keep Doing What I Was Doing"};
 //		int value = JOptionPane.showOptionDialog(frame, "One of your stocks has been triggered by an indicator. What would you like to do?", 
 //				"Crassus",JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, 
 //				null, options, options[0]);
 //		if(value == 0){
 //			frame.toFront();
 //			frame.repaint();
 //		}
 		if(!frame.isActive()){
 			frame.toFront();
			frame.setState(JFrame.NORMAL);
			JOptionPane.showMessageDialog(frame,"One of your stocks has been triggered by an indicator.");
 		}
 	}
 
 	@Override
 	public void update() {
 		stockBox.refreshActiveStock();
 		indicatorBox.refresh();
 		plotPane.refresh();
 		boolean shouldNotifyUser = stockBox.refresh();
 		if(shouldNotifyUser){
 			notifyUser();
 		}
 	}
 	
 	@Override
 	public void updateTables() {
 		indicatorBox.refresh();
 		boolean shouldNotifyUser = stockBox.refresh();
 		if(shouldNotifyUser){
 			notifyUser();
 		}
 	}
 	
 	public void possiblyExit() {
 		if(undoables.hasNoMajorChanges())
 			System.exit(0);
 		if(fileGui.fileExit())
 			System.exit(0);
 	}
 
 }

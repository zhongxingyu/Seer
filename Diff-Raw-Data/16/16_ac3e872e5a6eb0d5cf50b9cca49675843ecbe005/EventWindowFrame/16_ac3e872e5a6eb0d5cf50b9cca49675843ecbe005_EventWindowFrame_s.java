 package edu.brown.cs32.atian.crassus.gui.indicatorwindows;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import edu.brown.cs32.atian.crassus.backend.Stock;
 import edu.brown.cs32.atian.crassus.backend.StockImpl;
 import edu.brown.cs32.atian.crassus.gui.WindowCloseListener;
 import edu.brown.cs32.atian.crassus.indicators.BollingerBands;
 import edu.brown.cs32.atian.crassus.indicators.Indicator;
 import edu.brown.cs32.atian.crassus.indicators.MACD;
 import edu.brown.cs32.atian.crassus.indicators.PivotPoints;
 import edu.brown.cs32.atian.crassus.indicators.PriceChannel;
 import edu.brown.cs32.atian.crassus.indicators.RSI;
 import edu.brown.cs32.atian.crassus.indicators.StochasticOscillator;
 
 public class EventWindowFrame implements EventWindow {
 
 	private JPanel currentPanel;
 	private JDialog frame;
 	
 	public static void main(String[] args) 
 	{
 		JFrame p = new JFrame("this");
 		p.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		EventWindowFrame w = new EventWindowFrame(p, new WindowCloseListenerStub(), new StockImpl("GOOG"));
 		w.display();
 	}
 	
 	public void display() 
 	{
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public EventWindowFrame(JFrame parent, WindowCloseListener closeListener, Stock stock)
 	{
 		frame = new JDialog(parent,"Bolinger Band Event");
 		frame.setModal(true);
 		frame.setResizable(false);
 		
 		
 		currentPanel = new BolingerBandPanel(closeListener, frame, stock);
 		
 		//add dropdown to main Frame
 		JPanel[] eventList = {new BolingerBandPanel(closeListener, frame, stock), 
 							  new MACDPanel(closeListener, frame, stock), 
 							  new PivotPanel(closeListener, frame, stock), 
 							  new PriceChannelPanel(closeListener, frame, stock), 
 							  new RSIPanel(closeListener, frame, stock), 
 							  new StochOscillPanel(closeListener, frame, stock)};
 		
 		frame.setSize(350, eventList[0].getHeight() + 70);
 		frame.setMinimumSize(new Dimension(350, eventList[0].getHeight() + 70));
 		frame.getContentPane().setBackground(Color.WHITE);
 		frame.setLayout(new BorderLayout());
 		
 		
 		JComboBox<JPanel> selectEvent = new JComboBox<JPanel>(eventList);
 		selectEvent.addActionListener(new WindowChanger());
 		JPanel dropDownPanel = new JPanel();
 		dropDownPanel.setMinimumSize(new Dimension(250,20));
 		dropDownPanel.setMaximumSize(new Dimension(250,20));
 		dropDownPanel.add(selectEvent);
 		dropDownPanel.setBorder(new EmptyBorder(20,20,20,20));
 
 		
 		
 		frame.add(dropDownPanel, BorderLayout.NORTH);
 		frame.add(currentPanel, BorderLayout.CENTER);
 		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 	}
 	
 	public EventWindowFrame(JFrame parent, WindowCloseListener closeListener, Stock stock, Indicator prevIndicator)
 	{
 		frame = new JDialog(parent,"Bolinger Band Event");
 		frame.setModal(true);
 		frame.setResizable(false);
 		
 		BolingerBandPanel bb = new BolingerBandPanel(closeListener, frame, stock);
 		MACDPanel mp = new MACDPanel(closeListener, frame, stock);
 		PivotPanel pp = new PivotPanel(closeListener, frame, stock);
 		PriceChannelPanel pc = new PriceChannelPanel(closeListener, frame, stock);
 		RSIPanel rsp = new RSIPanel(closeListener, frame, stock);
 		StochOscillPanel sop = new StochOscillPanel(closeListener, frame, stock);
 		
 		if(prevIndicator instanceof BollingerBands)
 		{
 			BollingerBands ind = (BollingerBands)prevIndicator;
 			currentPanel = bb;
 			bb.setPeriods(Integer.toString(ind.getPeriod()));
 			bb.setBandWidth(Integer.toString(ind.getBandWidth()));
 		}else if(prevIndicator instanceof PriceChannel)
 		{
 			PriceChannel ind = (PriceChannel)prevIndicator;
 			currentPanel = pc;
 			pc.setLookBack(Integer.toString(ind.getLookBackPeriod())); 
 		}else if(prevIndicator instanceof MACD)
 		{
 			MACD ind = (MACD)prevIndicator;
 			currentPanel = mp;
 			mp.setSignal(Integer.toString(ind.getSignalPeriod()));
 			mp.setShort(Integer.toString(ind.getShortPeriod()));
 			mp.setLong(Integer.toString(ind.getLongPeriod()));
 		}else if(prevIndicator instanceof RSI)
 		{
 			RSI ind = (RSI)prevIndicator;
 			currentPanel = rsp;
 			rsp.setPeriod(Integer.toString(ind.getPeriod()));
 		}else if(prevIndicator instanceof PivotPoints)
 		{
 			PivotPoints ind = (PivotPoints)prevIndicator;
 			currentPanel = pp;
 			pp.setSelected(ind.getPivotOption());
 		}else if(prevIndicator instanceof StochasticOscillator)
 		{
 			StochasticOscillator ind = (StochasticOscillator)prevIndicator;
 			currentPanel = sop;
 			sop.setPeriod(Integer.toString(ind.getPeriod()));
 		}
 		
 		//add dropdown to main Frame
 		JPanel[] eventList = {bb,pc,mp,rsp,pp,sop};
 		
 		frame.setSize(350, eventList[0].getHeight() + 70);
 		frame.setMinimumSize(new Dimension(350, eventList[0].getHeight() + 70));
 		frame.getContentPane().setBackground(Color.WHITE);
 		frame.setLayout(new BorderLayout());
 		
 		
 		JComboBox<JPanel> selectEvent = new JComboBox<JPanel>(eventList);
 		selectEvent.addActionListener(new WindowChanger());
 		JPanel dropDownPanel = new JPanel();
 		dropDownPanel.setMinimumSize(new Dimension(250,20));
 		dropDownPanel.setMaximumSize(new Dimension(250,20));
 		dropDownPanel.add(selectEvent);
 		dropDownPanel.setBorder(new EmptyBorder(20,20,20,20));
 
 		
 		
 		frame.add(dropDownPanel, BorderLayout.NORTH);
 		frame.add(currentPanel, BorderLayout.CENTER);
 		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 	}
 	
 	
 	
 	
 	class WindowChanger implements ActionListener
 	{
 		public WindowChanger(){}
 		
 		@Override
 		public void actionPerformed(ActionEvent e) 
 		{
 			@SuppressWarnings("unchecked")
 			JComboBox<JPanel> selectEvent = (JComboBox<JPanel>)e.getSource();
 			JPanel newPanel = (JPanel)selectEvent.getSelectedItem();
 			Dimension panelDim = newPanel.getSize();
 			frame.remove(currentPanel);
 			currentPanel = newPanel;
 			frame.setTitle(newPanel.toString());
 			frame.add(currentPanel, BorderLayout.CENTER);
 			frame.setSize(350, 70 + panelDim.height);
 			frame.setMinimumSize(new Dimension(350, 70 + panelDim.height));
 			frame.setMaximumSize(new Dimension(350, 70 + panelDim.height));
 			display();
 		}
 		
 	}
 	
 	@Override
 	public void setWindowCloseListener(WindowCloseListener listener){}
 }

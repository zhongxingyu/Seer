 package edu.brown.cs32.atian.crassus.gui.indicatorwindows;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.undo.UndoManager;
 
 import edu.brown.cs32.atian.crassus.backend.Stock;
 import edu.brown.cs32.atian.crassus.backend.StockFreqType;
 import edu.brown.cs32.atian.crassus.gui.WindowCloseListener;
 import edu.brown.cs32.atian.crassus.indicators.BollingerBands;
 import edu.brown.cs32.atian.crassus.indicators.Indicator;
 
 public class BolingerBandPanel extends JPanel
 {
 
 	/**
 	 * This is the panel that is displayed for Bolinger Bands Events
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private WindowCloseListener closeListener;
 	private Stock stock;
 	private JDialog parent;
 	private JTextField periods;
 	private JTextField bandWidth;
 	private String periodstt = "<html>The number of days when calculating the standard deviation and simple moving average.</html>";
 	private String bandWidthtt = "<html>The number of standard deviations for the outer bands.</html>";
 	
 	
 	public BolingerBandPanel(WindowCloseListener closeListener, JDialog parent, Stock stock)
 	{
 		this.closeListener = closeListener;
 		this.parent = parent;
 		this.stock = stock;
 		
 		
 		final UndoManager undoM = new UndoManager();
 		
 		NumberVerifier inputValidator = new NumberVerifier(this);
 		//top panel
 		JLabel periodsLabel = new JLabel("Number of Periods:");
 		periodsLabel.setToolTipText(periodstt);
 		JLabel bandWidthLabel = new JLabel("Bandwidth:");
 		bandWidthLabel.setToolTipText(bandWidthtt);
 		
 		periods = new CrassusTextField("20", periodstt, inputValidator, undoM);
 		
 		bandWidth = new CrassusTextField("2", bandWidthtt, inputValidator, undoM);
 		
 		
 		JPanel periodsInput = new JPanel();
 		periodsInput.setLayout(new FlowLayout());
 		periodsInput.add(periodsLabel);
 		periodsInput.add(periods);
 		
 		JPanel bandWidthInput = new JPanel();
 		bandWidthInput.setLayout(new FlowLayout());
 		bandWidthInput.add(bandWidthLabel);
 		bandWidthInput.add(bandWidth);
 		
 		JPanel parameters = new JPanel();
 		parameters.setLayout(new BoxLayout(parameters, BoxLayout.Y_AXIS));
 		parameters.add(periodsInput);
 		parameters.add(bandWidthInput);
 		
 		//middle panel
 		JPanel buttons = new JPanel();
 		buttons.setLayout(new FlowLayout());
 		JButton ok = new JButton("Ok");
 		ok.addActionListener(new OkListener());
 		JButton test = new JButton("Test");
 		test.addActionListener(new TestListener());
 		JButton cancel = new JButton("Cancel");
 		cancel.addActionListener(new CancelListener(parent));
 		buttons.add(ok);
 		buttons.add(test);
 		buttons.add(cancel);
 		
 		
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		this.add(parameters);
 		this.add(buttons);
 
 		
 	}
 	
 	public void setPeriods(String periods)
 	{
 		this.periods.setText(periods);
	}j
 	
 	public void setBandWidth(String bandwidth)
 	{
 		this.bandWidth.setText(bandwidth);
 	}
 	
 	
 	class OkListener extends AbstractOkListener
 	{
 
 		public OkListener() 
 		{
 			super(parent);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) 
 		{
 			String periodsArg = periods.getText();
 			String bandWidthArg = bandWidth.getText();
 			
 			if(periodsArg == null || bandWidthArg == null)
 			{
 				showErrorDialog("You must enter values.");
 			}
 			else
 			{
 				try
 				{
 					Indicator ind = new BollingerBands(stock.getStockPriceData(stock.getCurrFreq()), Integer.parseInt(periodsArg), 
 							Integer.parseInt(bandWidthArg));
 					
 					parent.dispose();
 					closeListener.windowClosedWithEvent(ind);
 				}
 				catch(NumberFormatException nfe)
 				{
 					showErrorDialog();
 				}
 				catch(IllegalArgumentException iae)
 				{
 					showErrorDialog(iae.getMessage());
 				}
 			}
 			
 		}
 		
 		
 	}
 	
 	class TestListener implements ActionListener
 	{
 
 		@Override
 		public void actionPerformed(ActionEvent e) 
 		{
 			
 		}
 		
 	}
 	
 	public String toString()
 	{
 		return "Bollinger Band Event";
 	}
 }

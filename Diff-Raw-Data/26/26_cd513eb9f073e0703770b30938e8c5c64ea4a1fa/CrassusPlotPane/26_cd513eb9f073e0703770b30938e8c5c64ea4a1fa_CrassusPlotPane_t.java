 package edu.brown.cs32.atian.crassus.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.BorderFactory;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 
 import edu.brown.cs32.atian.crassus.backend.Stock;
 import edu.brown.cs32.atian.crassus.backend.StockFreqType;
 
 @SuppressWarnings("serial")
 public class CrassusPlotPane extends JPanel {
 
 	public class TimeFreqChangeListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent e) {
 			if(stock==null)
 				return;
 			stock.setCurrFreq(timeFreqFromIndex(timeFreq.getSelectedIndex()));
 			refresh();
 		}
 	}
 
 	public class TimeScaleChangeListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent arg0) {
 			timeFreq.removeActionListener(timeFreqListener);//must be done to keep timeFreq from going bannanas; added back at the end of function
 			timeFreq.removeAllItems();
 			switch(timeframe.getSelectedIndex()){
 			case 4:
 				timeFreq.addItem("Daily");
 				timeFreq.addItem("Weekly");
 				timeFreq.addItem("Monthly");
 				timeFreq.addItem("Yearly");
 				break;
 			case 3:
 				timeFreq.addItem("Daily");
 				timeFreq.addItem("Weekly");
 				timeFreq.addItem("Monthly");
 				break;
 			case 2:
 				timeFreq.addItem("Daily");
 				timeFreq.addItem("Weekly");
 				break;
 			case 1:
 				timeFreq.addItem("Minutely");
 				timeFreq.addItem("Daily");
 				break;
 			case 0:
 				timeFreq.addItem("Minutely");
 			}
 			
 			timeFreq.showPopup();
 			timeFreq.hidePopup();
 			
			stock.setTimeFrame(timeframeFromIndex(timeframe.getSelectedIndex()));
 			
 			timeFreq.addActionListener(timeFreqListener);
 			timeFreq.setSelectedIndex(0);//force timeFreq to update. This will make 'refresh' unnecessary
			refresh();
 		}
 	}
 
 	public class ResizeListener implements ComponentListener {
 		@Override public void componentHidden(ComponentEvent arg0) {}
 		@Override public void componentMoved(ComponentEvent arg0) {}
 		@Override public void componentResized(ComponentEvent arg0) {refresh();}
 		@Override public void componentShown(ComponentEvent arg0) {}
 	}
 
 	private CrassusImageDisplayer imageDisplayer;
 	private JComboBox<String> timeframe;
 	private JComboBox<String> timeFreq;
 	private TimeFreqChangeListener timeFreqListener;
 	private Stock stock;
 	
 	public CrassusPlotPane(){
 		this.setBackground(Color.WHITE);
 		this.setPreferredSize(new Dimension(300,300));
 		this.setLayout(new BorderLayout());
 		this.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createCompoundBorder(
 						BorderFactory.createEmptyBorder(20,20,20,20),
 						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)),
 				BorderFactory.createEmptyBorder(5,5,5,5)));
 		
 		imageDisplayer = new CrassusImageDisplayer();
 		this.add(imageDisplayer, BorderLayout.CENTER);
 		
 		timeframe = new JComboBox<>();
 		timeframe.addItem("Today");
		timeframe.addItem("One Week");
		timeframe.addItem("One Month");
		timeframe.addItem("One Year");
 		timeframe.addItem("Five Years");
 		timeframe.addActionListener(new TimeScaleChangeListener());
 		
 		timeFreq = new JComboBox<>();
 		timeFreq.setModel(new DefaultComboBoxModel<String>());
		timeFreq.addItem("Minutely");
//		timeFreq.addItem("Daily");
//		timeFreq.addItem("Weekly");
//		timeFreq.addItem("Monthly");
//		timeFreq.addItem("Yearly");
 		
 		timeFreqListener = new TimeFreqChangeListener();
 		timeFreq.addActionListener(timeFreqListener);
 		
 		
 		JPanel timePanel = new JPanel();
 		timePanel.setBackground(Color.WHITE);
 		timePanel.setLayout(new FlowLayout());
 		timePanel.add(new JLabel("Time-scale: "));
 		timePanel.add(timeframe);
 		timePanel.add(new JLabel("  Time-frequency: "));
 		timePanel.add(timeFreq);
 		
 		this.add(timePanel, BorderLayout.SOUTH);
 		
 		this.addComponentListener(new ResizeListener());
 	}
 	
 	public void changeToStock(Stock stock){
 		this.stock = stock;
		this.stock.setTimeFrame(timeframeFromIndex(timeframe.getSelectedIndex()));
 		this.stock.setCurrFreq(timeFreqFromIndex(timeFreq.getSelectedIndex()));
 		refresh();
 	}
 	
 	private TimeFrame timeframeFromIndex(int index){
 		switch(index){
 		case 0:
 			return TimeFrame.DAILY;
 		case 1:
 			return TimeFrame.WEEKLY;
 		case 2:
 			return TimeFrame.MONTHLY;
 		case 3:
 			return TimeFrame.YEARLY;
 		case 4:
 		default:
 			return TimeFrame.FIVE_YEAR;
 		}
 	}
 	
 	@SuppressWarnings("incomplete-switch")
 	private StockFreqType timeFreqFromIndex(int index){
 		
 		switch(timeframeFromIndex(timeframe.getSelectedIndex())){
 		case FIVE_YEAR:
 		case YEARLY:
 		case MONTHLY:
 			index++;//first three don't allow minutely data
 		}
 		
 		switch(index){
 		case 0:
 			return StockFreqType.MINUTELY;
 		case 1:
 			return StockFreqType.DAILY;
 		case 2:
 			return StockFreqType.WEEKLY;
 		case 3:
 		default:
 			return StockFreqType.MONTHLY;
 		}
 	}
 	
 	public void refresh(){
 		//check width of imageDisplayer because when pane is swapped out it will be zero, plot object flips out
 		if(stock!=null && imageDisplayer.getWidth()!=0){
 			PlotWrapper plot = new PlotWrapper(stock.getCompanyName(), timeframeFromIndex(timeframe.getSelectedIndex()));
 			plot.setAxesTitles("Time", "Price");
 			stock.addToPlot(plot);
 			BufferedImage image = plot.getPrimaryBufferedImage(imageDisplayer.getWidth(), imageDisplayer.getHeight());
 			imageDisplayer.setImage(image);
 		}
 		else{
 			imageDisplayer.setImage(null);
 		}
 		imageDisplayer.revalidate();
 		imageDisplayer.repaint();
 	}
 	
 }

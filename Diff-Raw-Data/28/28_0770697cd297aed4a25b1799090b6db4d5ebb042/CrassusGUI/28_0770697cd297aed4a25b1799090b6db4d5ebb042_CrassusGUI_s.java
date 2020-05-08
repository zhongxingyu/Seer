 /**
  * 
  */
 package edu.brown.cs32.atian.crassus.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 
 import edu.brown.cs32.atian.crassus.backend.StockList;
 
 /**
  * @author Matthew
  *
  */
 public class CrassusGUI implements GUI {
 	
 	private JFrame frame;
 	
 	private CrassusPlotPane plotPane;
 	
 	CrassusStockTablePane stockBox;
 	CrassusEventTablePane eventBox;
 
 	public CrassusGUI(StockList stocks) {
 		
 		frame = new JFrame("Crassus");
 		frame.getContentPane().setBackground(Color.WHITE);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		stockBox = new CrassusStockTablePane(frame,stocks);
 
		eventBox = new CrassusEventTablePane();
 		
 		//make plot pane
 		plotPane = new CrassusPlotPane();
 		
 		
 		JPanel stockInfo = new JPanel();
 		stockInfo.setBackground(Color.WHITE);
 		stockInfo.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createEmptyBorder(20,20,20,20),
 				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
 		stockInfo.setLayout(new BorderLayout());
 		stockInfo.add(eventBox, BorderLayout.EAST);
 		stockInfo.add(plotPane,BorderLayout.CENTER);
 
 		frame.add(stockBox, BorderLayout.WEST);
 		frame.add(stockInfo,BorderLayout.CENTER);
 		frame.setMinimumSize(new Dimension(700,400));
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.brown.cs32.atian.crassus.gui.GUI#launch()
 	 */
 	@Override
 	public void launch() {
 		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
 		frame.setVisible(true);
 	}
 
 	@Override
 	public void update() {
 		stockBox.refresh();
 	}
 
 }

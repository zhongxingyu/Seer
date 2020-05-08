 package visual;
 
 import java.awt.BorderLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PiePlot3D;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.general.PieDataset;
 import org.jfree.util.Rotation;
 
 import base.Eshop;
 import base.EshopV2;
 import base.Product;
 
 @SuppressWarnings("serial")
 public class DesignV2 extends Design {
 
 	public DesignV2(Eshop eshop) {
 		super(eshop);
 		
 		this.tabs.addTab("Charts", this.statsPanel());
 		this.tabs.setEnabledAt(2, false);
 		
 		if(eshop instanceof EshopV2) {
 			this.tabs.setEnabledAt(2, true);
 			
 			this.tabs.addChangeListener(new ChangeListener() {
 			      public void stateChanged(ChangeEvent e) {
 			    	if(tabs.getSelectedIndex() == 2) {
 			    		tabs.setComponentAt(2, statsPanel());
 			    	}
 			      }
 			});
 			
 		}
 	}
 	
 	private ChartPanel availableQuantityPie() {
 		if(eshop instanceof EshopV2) {
 			PieDataset dataset = ((EshopV2) this.eshop).availableQuantityDataset();
 			JFreeChart chart = ChartFactory.createPieChart3D("Available quantity", dataset, true, true, false);
 	
 			//chart.setBackgroundPaint(p1.getBackground());
 			
 		    PiePlot3D plot = (PiePlot3D) chart.getPlot();
 		    plot.setStartAngle(290);
 		    plot.setDirection(Rotation.CLOCKWISE);
 		    
 			ChartPanel chartPanel = new ChartPanel(chart);
 			chartPanel.setPreferredSize(new java.awt.Dimension(500, 200));
 			return chartPanel;
 		}else{
 			return null;
 		}
 	}
 	
 	private ChartPanel weekSalesChart() {
 		if(eshop instanceof EshopV2) {
 		JFreeChart chart = ChartFactory.createXYBarChart(
	            "Last 7 days sales",
	            "Day of month", 
 	            false,
 	            Product.getCurrency(), 
 	            ((EshopV2) this.eshop).weekSalesDataset(),
 	            PlotOrientation.VERTICAL,
 	            true,
 	            true,
 	            false
 	        );
 
 	        ChartPanel chartPanel = new ChartPanel(chart);
 	        return chartPanel;
 		}else{
 			return null;
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public JPanel statsPanel() {
 		JPanel p1 = new JPanel();
 		p1.setLayout(new BorderLayout());
 		p1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		
 		if(eshop instanceof EshopV2) {
 			// Pie
 			p1.add(this.availableQuantityPie(), BorderLayout.LINE_END);
 			
 			// Chart
 			p1.add(this.weekSalesChart(), BorderLayout.CENTER);
 			
 		}
 		return p1;
 	}
 	
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				Design ex = new DesignV2(new EshopV2());
 				ex.setVisible(true);
 			}
 		});
 	}
 
 }

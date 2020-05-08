 package cz.fjfi.pvs.kosapi.chart;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 
 import org.apache.log4j.Logger;
 
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.ui.Size2D;
 
 public abstract class Chart extends JFrame{
 	protected JFreeChart chart;
 	protected String chartTitle;
 	
 	protected float foregroundAlpha = 0.7f;
 	protected float backgroundAlpha = 0.2f;
 	protected Color backgroundPaint = Color.white;
 	protected boolean antiAlias = true;
 	protected boolean textAntiAlias = true;
 	protected boolean borderVisible = false;
         
     protected Logger logger = Logger.getLogger(Chart.class);
 	
     
 	protected abstract void setChartProperties();
 	
 	public void saveAsPNG(String outputFile)
 	{
 		saveAsPNG(outputFile, new Size2D(800, 600));
 	}
 	
 	public void saveAsPNG(String outputFileName, Size2D size)
 	{
 		String outputFile = outputFileName + ".png";
 		try
 		{
 			ChartUtilities.saveChartAsPNG(new File(outputFile), chart, (int)size.width, (int)size.height);
 			logger.info("Chart saved as: " + outputFile);
 		}
 		catch(IOException saveException)
 		{
 			logger.error(saveException.getMessage(), saveException);
 		}
 	}
 }

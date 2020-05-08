 /**
  * 
  */
 package com.yogocodes.httpmonitor.gui.listeners;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartFrame;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
import com.yogocodes.httpm*onitor.core.MonitorResult;
 import com.yogocodes.httpmonitor.core.SystemPropertyContainer;
 import com.yogocodes.httpmonitor.core.SystemPropertyContainerFactory;
 
 /**
  * @author joukojo
  * 
  */
 public class ViewResultGraphActionListenerImpl implements ActionListener {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(final ActionEvent e) {
 
 		//
 		final SystemPropertyContainer propertyContainer = SystemPropertyContainerFactory.getContainerInstance();
 
 		final String logFileName = propertyContainer.getLogFileName();
 
 		Map<String, XYSeries> dataSeries = null;
 		try {
 			dataSeries = readCSVFile(logFileName);
 
 			final XYSeriesCollection xyDataset = new XYSeriesCollection();
 
 			final Collection<XYSeries> values = dataSeries.values();
 
 			for (final XYSeries series : values) {
 				xyDataset.addSeries(series);
 			}
 
 			final JFreeChart chart = ChartFactory.createXYLineChart("Response times", "Time", "Response time(ms)", xyDataset, PlotOrientation.VERTICAL, true, true, false);
 
 			final ChartFrame frame1 = new ChartFrame("Performance charts", chart);
 			frame1.setResizable(true);
 
 			frame1.setSize(700, 500);
 			frame1.setLocationRelativeTo(null);
 			frame1.setVisible(true);
 
 		} catch (final FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (final IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (final ParseException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 	}
 
 	protected Map<String, XYSeries> readCSVFile(final String logFileName) throws FileNotFoundException, IOException, ParseException {
 		final FileReader fileReader = new FileReader(logFileName);
 
 		final BufferedReader reader = new BufferedReader(fileReader);
 		String line = reader.readLine();
 		final Map<String, XYSeries> dataSeries = new HashMap<String, XYSeries>();
 		while (line != null) {
 			final MonitorResult result = MonitorResult.fromCSVLine(line);
 			final String url = result.getUrl();
 			if (!dataSeries.containsKey(url)) {
 
 				dataSeries.put(url, new XYSeries(url));
 			}
 			dataSeries.get(url).add(result.getExecuteTime(), result.getTime());
 
 			line = reader.readLine();
 		}
 		return dataSeries;
 	}
 }

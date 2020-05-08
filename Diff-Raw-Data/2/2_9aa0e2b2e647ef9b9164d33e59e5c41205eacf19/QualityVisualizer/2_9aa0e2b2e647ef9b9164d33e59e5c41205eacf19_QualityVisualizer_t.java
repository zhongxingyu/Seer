 package tu.modgeh.intfiresim;
 
 
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import javax.swing.JFrame;
 import javax.swing.border.EmptyBorder;
 import org.jfree.chart.*;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 public class QualityVisualizer {
 
 	public QualityVisualizer() {
 	}
 
 	private static XYItemRenderer createRenderer() {
 
 		DefaultXYItemRenderer renderer = null;
 
 		renderer = new DefaultXYItemRenderer();
 
 		renderer.setDrawSeriesLineAsPath(true);
 		renderer.setBaseItemLabelsVisible(true);
 		renderer.setBaseLinesVisible(true);
 		renderer.setBaseShapesVisible(false);
 		renderer.setBaseShapesFilled(false);
 
 		return renderer;
 	}
 
 	private static ValueAxis createDomainAxis() {
 
 		NumberAxis axis = null;
 
 		axis = new NumberAxis();
 //		axis.setAutoRange(true);
 		axis.setLowerBound(0.0);
 		axis.setUpperBound(1.0);
 
 		return axis;
 	}
 
 	private static ValueAxis createRangeAxis() {
 
 		NumberAxis axis = null;
 
 		axis = new NumberAxis();
 		axis.setAutoRange(true);
 
 		return axis;
 	}
 
 	public static ChartPanel createCanvas() {
 
 		ChartPanel panel = null;
 
 		final XYSeriesCollection currentFreqsSeries = new XYSeriesCollection();
 
 		final double simulationTime = 10.0;
 		final int sigmas = 30;
 		final double sigmaMin = 0.5;
 		final double sigmaMax = 1.5;
 		final double current = 0.4E-9;
 		final double receiveSpikeInterval = 50.0E-3;
 		final double pulsePotential = 7E-3;
 		final int numBestQualities = 3;
 
 		SortedSet<FreqencyQualitySeriesCreator> messurements = new TreeSet<FreqencyQualitySeriesCreator>();
 
 		final double deltaSigma = (sigmaMax - sigmaMin) / sigmas;
 System.out.println("6.c) simulating ...");
 		for (double sigma = sigmaMin; sigma <= sigmaMax; sigma += deltaSigma) {
 			final double curSigma = (double)Math.round(sigma * sigmas) / sigmas;
 			sigma = curSigma;
 			final Integrator integrator = new Integrator();
 			integrator.setCurrent(current);
 			integrator.setNoiseStandardDeviation(sigma);
 			integrator.setReceivePulsePotential(pulsePotential);
 
 			FreqencyQualitySeriesCreator serGen = new FreqencyQualitySeriesCreator(integrator, receiveSpikeInterval, simulationTime);
 
 			try {
 System.out.println("\tsigma: " + curSigma + " ...");
 				serGen.call();
 				messurements.add(serGen);
 //				XYSeries currentFreqs = serGen.call();
 //				double quality = Math.round(serGen.getQuality() * 1000.0) / 1000.0;
 //				currentFreqs.setKey(/*spikes.getKey() + "   " + */"sigma: " + curSigma + " Q: " + quality);
 //				currentFreqsSeries.addSeries(currentFreqs);
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 
 		final Integrator integrator = new Integrator();
 		integrator.setRestingPotential(-54.001E-3); // nearly spiking already
 		integrator.setResetPotential(-54.001E-3); // nearly spiking already
 		integrator.setCurrent(0.0);
 		integrator.setNoiseStandardDeviation(0.0);
 		integrator.setReceivePulsePotential(pulsePotential);
 		FreqencyQualitySeriesCreator serGen = new FreqencyQualitySeriesCreator(integrator, receiveSpikeInterval, simulationTime);
 		try {
 System.out.println("\tpulse-indicator ...");
 			serGen.call();
 			messurements.add(serGen);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 
 System.out.println(numBestQualities + " best qualities ...");
 		int i = 0;
 		for (FreqencyQualitySeriesCreator freqencyQualitySeriesCreator : messurements) {
 			double sigma = freqencyQualitySeriesCreator.getIntegrator().getNoiseStandardDeviation();
 			sigma = Math.round(sigma * 10000.0) / 10000.0;
 			double quality = freqencyQualitySeriesCreator.getQuality();
 			quality = Math.round(quality * 10000.0) / 10000.0;
 			XYSeries membranePotential = freqencyQualitySeriesCreator.getMembranePotential();
 			membranePotential.setKey("sigma: " + sigma + " Q: " + quality);
 			currentFreqsSeries.addSeries(freqencyQualitySeriesCreator.getMembranePotential());
 			i++;
 			if (i > numBestQualities) {
 				break;
 			}
 		}
 
 		ValueAxis rangeAxis = createRangeAxis();
 		ValueAxis domainAxis = createDomainAxis();
 
 		XYPlot plot = new XYPlot(currentFreqsSeries, domainAxis, rangeAxis, createRenderer());
 
 		plot.setDomainCrosshairVisible(true);
 
 		plot.setDomainPannable(true);
 		plot.setRangePannable(true);
 
 		JFreeChart chart = new JFreeChart(plot);
 		panel = new ChartPanel(chart);
 
 		return panel;
 	}
 
     public void run() {
 
		final JFrame mainFrame = new JFrame("6.c) Spike detection of small pulses combined with noise (and base current)");
 		mainFrame.setSize(800, 500);
 
 		ChartPanel canvas = createCanvas();
 		canvas.setBorder(new EmptyBorder(10, 10, 10, 10));
 
 		mainFrame.getContentPane().add(canvas);
 
 		java.awt.EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				mainFrame.setVisible(true);
 			}
 		});
 	}
 }

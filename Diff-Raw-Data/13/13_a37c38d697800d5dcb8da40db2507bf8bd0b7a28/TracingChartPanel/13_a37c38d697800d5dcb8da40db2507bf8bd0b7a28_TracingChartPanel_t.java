 package ee.ttu.kinect.view.chart;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.data.time.FixedMillisecond;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.data.time.TimeSeriesDataItem;
 
 import ee.ttu.kinect.model.Body;
 import ee.ttu.kinect.model.JointType;
 import ee.ttu.kinect.model.ModelProcessor;
 import ee.ttu.kinect.view.ChartType;
 
 public class TracingChartPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	private ModelProcessor processor;
 	
 	private JointType selectedJoint;
 	
 	private JComboBox<JointType> jointCombo;
 
 	private JCheckBox packedModeChekbox;
 	
 	private JCheckBox velocityXCheckbox;
 
 	private JCheckBox velocityYCheckbox;
 
 	private JCheckBox velocityZCheckbox;
 
 	private JCheckBox accelerationXCheckbox;
 
 	private JCheckBox accelerationYCheckbox;
 
 	private JCheckBox accelerationZCheckbox;
 	
 	private JButton valuesAnalysisButton;
 	
 	private JButton segmentationAnalysisButton;
 	
 	private JButton motionAnalysisButton;
 
 	private JFreeChart chart;
 
 	private JPanel chartControlPanel;
 
 	private ChartPanel chartPanel;
 
 	private TimeSeriesCollection dataset;
 
 	private TimeSeries seriesVelocityX;
 	private TimeSeries seriesVelocityY;
 	private TimeSeries seriesVelocityZ;
 
 	private TimeSeries seriesAccelerationX;
 	private TimeSeries seriesAccelerationY;
 	private TimeSeries seriesAccelerationZ;
 
 	private ChartSelector chartSelector;
 
 	public TracingChartPanel() {
 		Border border = BorderFactory.createEtchedBorder();
 		setBorder(BorderFactory.createTitledBorder(border, "Chart"));
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
 		jointCombo = new JComboBox<JointType>(JointType.values());
 		selectedJoint = (JointType) jointCombo.getSelectedItem();
 		processor = new ModelProcessor(selectedJoint);
 		
 		packedModeChekbox = new JCheckBox("Packed mode");
 		velocityXCheckbox = new JCheckBox("Velocity X");
 		velocityYCheckbox = new JCheckBox("Velocity Y");
 		velocityZCheckbox = new JCheckBox("Velocity Z");
 		accelerationXCheckbox = new JCheckBox("Acceleration X");
 		accelerationYCheckbox = new JCheckBox("Acceleration Y");
 		accelerationZCheckbox = new JCheckBox("Acceleration Z");
 		
 		valuesAnalysisButton = new JButton("Values analysis");
 		
 		segmentationAnalysisButton = new JButton("Segmentation analysis");
 		
 		motionAnalysisButton = new JButton("Motion analysis");
 
 		chartSelector = new ChartSelector();		
 
 		// listeners for controls
 		ChartControlChangeListener chartControlListener = new ChartControlChangeListener();
 		jointCombo.addActionListener(chartControlListener);
 		velocityXCheckbox.addActionListener(chartControlListener);
 		velocityYCheckbox.addActionListener(chartControlListener);
 		velocityZCheckbox.addActionListener(chartControlListener);
 		accelerationXCheckbox.addActionListener(chartControlListener);
 		accelerationYCheckbox.addActionListener(chartControlListener);
 		accelerationZCheckbox.addActionListener(chartControlListener);
 		packedModeChekbox.addActionListener(chartControlListener);
 
 		packedModeChekbox.setSelected(true);
 		
 		seriesVelocityX = new TimeSeries("Velocity X");
 		seriesVelocityY = new TimeSeries("Velocity Y");
 		seriesVelocityZ = new TimeSeries("Velocity Z");
 		seriesAccelerationX = new TimeSeries("Acceleration X");
 		seriesAccelerationY = new TimeSeries("Acceleration Y");
 		seriesAccelerationZ = new TimeSeries("Acceleration Z");
 
 		dataset = new TimeSeriesCollection();
 		chart = ChartFactory.createTimeSeriesChart(
 				"Velocity/Acceleration chart", "Time", "Velocity/Acceleration",
 				dataset, true, true, false);
 		XYPlot plot = (XYPlot) chart.getPlot();
 		DateAxis axis = (DateAxis) plot.getDomainAxis();
 		axis.setDateFormatOverride(new SimpleDateFormat("mm:ss.SSS"));
 
 		chartPanel = new ChartPanel(chart);
 		chartPanel.setPreferredSize(new Dimension(200, 330));
 
 		chartControlPanel = new JPanel();
 
 		chartControlPanel.setLayout(new BoxLayout(chartControlPanel,
 				BoxLayout.X_AXIS));
 		chartControlPanel.add(packedModeChekbox);
 		chartControlPanel.add(jointCombo);
 		chartControlPanel.add(velocityXCheckbox);
 		chartControlPanel.add(velocityYCheckbox);
 		chartControlPanel.add(velocityZCheckbox);
 		chartControlPanel.add(accelerationXCheckbox);
 		chartControlPanel.add(accelerationYCheckbox);
 		chartControlPanel.add(accelerationZCheckbox);
 		chartControlPanel.add(valuesAnalysisButton);
 		chartControlPanel.add(segmentationAnalysisButton);
 		chartControlPanel.add(motionAnalysisButton);
 
 		add(chartControlPanel);
 		add(chartPanel);
 	}
 	
 	// XYPlot plot = (XYPlot) (chart.getPlot());
 	// plot.getRenderer().setSeriesPaint(0, Color.RED);
 	// plot.getRenderer().setSeriesPaint(1, Color.GREEN);
 	// plot.getRenderer().setSeriesPaint(2, Color.BLUE);
 	// plot.getRenderer().setSeriesPaint(3, Color.YELLOW);
 	// plot.getRenderer().setSeriesPaint(4, Color.MAGENTA);
 	// plot.getRenderer().setSeriesPaint(5, Color.CYAN);
 
 	private class ChartControlChangeListener implements ActionListener {
 		@Override
 		public synchronized void actionPerformed(ActionEvent e) {
 			if (!jointCombo.getSelectedItem().equals(selectedJoint)) {
 				selectedJoint = (JointType) jointCombo.getSelectedItem();
 				clearChart();
 			}
 
 			// packed mode checkbox
 			togglePackedMode(packedModeChekbox.isSelected());
 			
 			// velocity checkboxes
 			if (velocityXCheckbox.isSelected()) {
 				if (!dataset.getSeries().contains(seriesVelocityX)) {
 					dataset.addSeries(seriesVelocityX);
 				}
 			} else {
 				if (dataset.getSeries().contains(seriesVelocityX)) {
 					// velocitySeriesX.clear();
 					dataset.removeSeries(seriesVelocityX);
 				}
 			}
 			if (velocityYCheckbox.isSelected()) {
 				if (!dataset.getSeries().contains(seriesVelocityY)) {
 					dataset.addSeries(seriesVelocityY);
 				}
 			} else {
 				if (dataset.getSeries().contains(seriesVelocityY)) {
 					// velocitySeriesY.clear();
 					dataset.removeSeries(seriesVelocityY);
 				}
 			}
 			if (velocityZCheckbox.isSelected()) {
 				if (!dataset.getSeries().contains(seriesVelocityZ)) {
 					dataset.addSeries(seriesVelocityZ);
 				}
 			} else {
 				if (dataset.getSeries().contains(seriesVelocityZ)) {
 					// velocitySeriesZ.clear();
 					dataset.removeSeries(seriesVelocityZ);
 				}
 			}
 
 			// acceleration checkboxes
 			if (accelerationXCheckbox.isSelected()) {
 				if (!dataset.getSeries().contains(seriesAccelerationX)) {
 					dataset.addSeries(seriesAccelerationX);
 				}
 			} else {
 				if (dataset.getSeries().contains(seriesAccelerationX)) {
 					dataset.removeSeries(seriesAccelerationX);
 				}
 			}
 			if (accelerationYCheckbox.isSelected()) {
 				if (!dataset.getSeries().contains(seriesAccelerationY)) {
 					dataset.addSeries(seriesAccelerationY);
 				}
 			} else {
 				if (dataset.getSeries().contains(seriesAccelerationY)) {
 					dataset.removeSeries(seriesAccelerationY);
 				}
 			}
 			if (accelerationZCheckbox.isSelected()) {
 				if (!dataset.getSeries().contains(seriesAccelerationZ)) {
 					dataset.addSeries(seriesAccelerationZ);
 				}
 			} else {
 				if (dataset.getSeries().contains(seriesAccelerationZ)) {
 					dataset.removeSeries(seriesAccelerationZ);
 				}
 			}
 		}
 
 		private void togglePackedMode(boolean selected) {
 			long period = selected ? Long.MAX_VALUE : 10000;
 			seriesVelocityX.setMaximumItemAge(period);
 			seriesVelocityY.setMaximumItemAge(period);
 			seriesVelocityZ.setMaximumItemAge(period);
 			seriesAccelerationX.setMaximumItemAge(period);
 			seriesAccelerationY.setMaximumItemAge(period);
 			seriesAccelerationZ.setMaximumItemAge(period);
 		}
 	}
 	
 	public void addListenerForValuesAnalysis(ActionListener listener) {
 		valuesAnalysisButton.addActionListener(listener);
 	}
 	
 	public void addListenerForSegmentationAnalysis(ActionListener listener) {
 		segmentationAnalysisButton.addActionListener(listener);
 	}
 	
 	public void addListenerForMotionAnalysis(ActionListener listener) {
 		motionAnalysisButton.addActionListener(listener);
 	}
 	
 	public void openChartSelector(List<Body> data, ChartType type) {
 		chartSelector.open(data, type);
 	}
 
 	public void clearChart() {		
 		seriesVelocityX.clear();
 		seriesVelocityY.clear();
 		seriesVelocityZ.clear();
 		seriesAccelerationX.clear();
 		seriesAccelerationY.clear();
 		seriesAccelerationZ.clear();
 		
 		processor.reset();
 	}
 	
 	public void updateChart(Body body, boolean seatedMode) {		
 		if (!velocityXCheckbox.isSelected() 
 				&& !velocityYCheckbox.isSelected()
 				&& !velocityZCheckbox.isSelected()
 				&& !accelerationXCheckbox.isSelected()
 				&& !accelerationYCheckbox.isSelected()
 				&& !accelerationZCheckbox.isSelected()) {
 			return;
 		}
 		if (body == null || !body.isBodyReady()) {
 			return;
 		}
 //		Body oldBody = body.getOldBody();
 //		Body oldOldBody = (oldBody == null) ? null : oldBody.getOldBody();
 //		if (body == null || !body.isBodyReady() 
 //				|| oldBody == null || !oldBody.isBodyReady() 
 //				|| oldOldBody == null || !oldOldBody.isBodyReady()) {
 //			return;
 //		}
 		
 		JointType type = (JointType) jointCombo.getSelectedItem();
 		if (seatedMode
 				&& (type == JointType.ANKLE_LEFT
 						|| type == JointType.ANKLE_RIGHT
 						|| type == JointType.FOOT_LEFT
 						|| type == JointType.FOOT_RIGHT
 						|| type == JointType.HIP_CENTER
 						|| type == JointType.HIP_LEFT
 						|| type == JointType.HIP_RIGHT
 						|| type == JointType.KNEE_LEFT
 						|| type == JointType.KNEE_RIGHT 
 						|| type == JointType.SPINE)) {
 			return;
 		}
 		
 		processor.setType(type);
 		
		//System.out.print("frame nr. " + body.getFrameNumber() + " ");
 		if (processor.process(body)) {
 			updateVelocity(body.getTimestamp(), processor.getVelocityX(), processor.getVelocityY(), processor.getVelocityZ());
 			updateAcceleration(body.getTimestamp(), processor.getAccelerationX(), processor.getAccelerationY(), processor.getAccelerationZ());
 		}
 		
 		
 //		updateVelocity(body.getTimestamp(),
 //				Calculator.calculateVelocity(oldBody.getJoint(type).getPositionX(), body.getJoint(type).getPositionX(), oldBody.getTimestamp(), body.getTimestamp()),
 //				Calculator.calculateVelocity(oldBody.getJoint(type).getPositionY(), body.getJoint(type).getPositionY(), oldBody.getTimestamp(), body.getTimestamp()),
 //				Calculator.calculateVelocity(oldBody.getJoint(type).getPositionZ(), body.getJoint(type).getPositionZ(), oldBody.getTimestamp(), body.getTimestamp()));
 //		updateAcceleration(body.getTimestamp(),
 //				Calculator.calculateAcceleration(oldOldBody.getJoint(type).getPositionX(), oldBody.getJoint(type).getPositionX(), body.getJoint(type).getPositionX(), oldOldBody.getTimestamp(), oldBody.getTimestamp(), body.getTimestamp()),
 //				Calculator.calculateAcceleration(oldOldBody.getJoint(type).getPositionY(), oldBody.getJoint(type).getPositionY(), body.getJoint(type).getPositionY(), oldOldBody.getTimestamp(), oldBody.getTimestamp(), body.getTimestamp()),
 //				Calculator.calculateAcceleration(oldOldBody.getJoint(type).getPositionZ(), oldBody.getJoint(type).getPositionZ(), body.getJoint(type).getPositionZ(), oldOldBody.getTimestamp(), oldBody.getTimestamp(), body.getTimestamp()));
 	}
 
 	private void updateVelocity(long timestamp, double velocityX,
 			double velocityY, double velocityZ) {
 //		System.out.printf("hoj2 %f %f %f \n", velocityX, velocityY, velocityZ);
 		seriesVelocityX.addOrUpdate(new TimeSeriesDataItem(
 				new FixedMillisecond(timestamp), velocityX));
 		seriesVelocityY.addOrUpdate(new TimeSeriesDataItem(
 				new FixedMillisecond(timestamp), velocityY));
 		seriesVelocityZ.addOrUpdate(new TimeSeriesDataItem(
 				new FixedMillisecond(timestamp), velocityZ));
 	}
 
 	private void updateAcceleration(long timestamp, double accelerationX,
 			double accelerationY, double accelerationZ) {
//		System.out.printf("hoj2 %f %f %f \n", accelerationX, accelerationY, accelerationZ);
 		seriesAccelerationX.addOrUpdate(new TimeSeriesDataItem(
 				new FixedMillisecond(timestamp), accelerationX));
 		seriesAccelerationY.addOrUpdate(new TimeSeriesDataItem(
 				new FixedMillisecond(timestamp), accelerationY));
 		seriesAccelerationZ.addOrUpdate(new TimeSeriesDataItem(
 				new FixedMillisecond(timestamp), accelerationZ));
 	}
 	
 }

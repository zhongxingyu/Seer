 package ee.ttu.kinect.view;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.data.time.FixedMillisecond;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.data.time.TimeSeriesDataItem;
 
 import ee.ttu.kinect.model.Body;
 import ee.ttu.kinect.model.JointType;
 
 public class JointChartPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	private JComboBox<JointType> jointCombo;
 	
 	private JCheckBox xCheckbox;
 
 	private JCheckBox yCheckbox;
 
 	private JCheckBox zCheckbox;
 
 	private JCheckBox velocityCheckbox;
 
 	private JCheckBox accelerationCheckbox;
 
 	private JFreeChart chart;
 
 	private JPanel chartControlPanel;
 
 	private ChartPanel chartPanel;
 	
 	private TimeSeriesCollection dataset;
 	
 	private TimeSeries seriesVelocityX = new TimeSeries("Velocity X");
 	private TimeSeries seriesVelocityY = new TimeSeries("Velocity Y");
 	private TimeSeries seriesVelocityZ = new TimeSeries("Velocity Z");
 	
 	private JointType selectedJoint; 
 
 	public JointChartPanel() {
 		Border border = BorderFactory.createEtchedBorder();
 		setBorder(BorderFactory.createTitledBorder(border, "Chart"));
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		
 		jointCombo = new JComboBox<JointType>();
 		for (JointType jt : JointType.values()) {
 			jointCombo.addItem(jt);
 		}
 		xCheckbox = new JCheckBox("X");
 		yCheckbox = new JCheckBox("Y");
 		zCheckbox = new JCheckBox("Z");
 		velocityCheckbox = new JCheckBox("Velocity");
 		accelerationCheckbox = new JCheckBox("Acceleration");
 
 		selectedJoint = (JointType) jointCombo.getSelectedItem();
 		
 		// listeners for controls
 		ChartControlChangeListener chartControlListener = new ChartControlChangeListener();
 		jointCombo.addActionListener(chartControlListener);
 		xCheckbox.addActionListener(chartControlListener);
 		yCheckbox.addActionListener(chartControlListener);
 		zCheckbox.addActionListener(chartControlListener);
 		velocityCheckbox.addActionListener(chartControlListener);
 		accelerationCheckbox.addActionListener(chartControlListener);
 
 		dataset = new TimeSeriesCollection();
 		chart = ChartFactory.createTimeSeriesChart("Velocity/Acceleration chart",
 				"Time", "Velocity/Acceleration", dataset,
 				true, true, false);
 		
 		chartPanel = new ChartPanel(chart);
 		chartPanel.setPreferredSize(new Dimension(200, 300));
 
 		chartControlPanel = new JPanel();
 
 		chartControlPanel.setLayout(new BoxLayout(chartControlPanel,
 				BoxLayout.X_AXIS));
 		chartControlPanel.add(jointCombo);
 		chartControlPanel.add(xCheckbox);
 		chartControlPanel.add(yCheckbox);
 		chartControlPanel.add(zCheckbox);
 		chartControlPanel.add(velocityCheckbox);
 		chartControlPanel.add(accelerationCheckbox);
 
 		add(chartControlPanel);
 		add(chartPanel);
 	}
 	
 	private class ChartControlChangeListener implements ActionListener {
 		@Override
 		public synchronized void actionPerformed(ActionEvent e) {
 			if (!jointCombo.getSelectedItem().equals(selectedJoint)) {
 				selectedJoint = (JointType) jointCombo.getSelectedItem();
 				clearChart();
 			}
 			
 			if (velocityCheckbox.isSelected()) {
 				if (xCheckbox.isSelected()) {
 					if (!dataset.getSeries().contains(seriesVelocityX)) {
 						dataset.addSeries(seriesVelocityX);
 					}
 				} else {
 					if (dataset.getSeries().contains(seriesVelocityX)) {
 						//velocitySeriesX.clear();
 						dataset.removeSeries(seriesVelocityX);
 					}
 				}
 				if (yCheckbox.isSelected()) {
 					if (!dataset.getSeries().contains(seriesVelocityY)) {
 						dataset.addSeries(seriesVelocityY);
 					}
 				} else {
 					if (dataset.getSeries().contains(seriesVelocityY)) {
 						//velocitySeriesY.clear();
 						dataset.removeSeries(seriesVelocityY);
 					}
 				}
 				if (zCheckbox.isSelected()) {
 					if (!dataset.getSeries().contains(seriesVelocityZ)) {
 						dataset.addSeries(seriesVelocityZ);
 					}
 				} else {
 					if (dataset.getSeries().contains(seriesVelocityZ)) {
 						//velocitySeriesZ.clear();
 						dataset.removeSeries(seriesVelocityZ);
 					}
 				}
 			} else {
 				dataset.removeSeries(seriesVelocityX);
 				dataset.removeSeries(seriesVelocityY);
 				dataset.removeSeries(seriesVelocityZ);
 			}
 		}	
 	}
 	
 	public void updateChart(Body body) {
		System.out.println("update: " + body);
 		JointType selectedType = (JointType) jointCombo.getSelectedItem();
 		switch (selectedType) {
 		case ANKLE_LEFT:
 			updateVelocity(body.getTimestamp(), body.getAnkleLeftXVelocity(), body.getAnkleLeftYVelocity(), body.getAnkleLeftZVelocity());
 			break;
 		case ANKLE_RIGHT:
 			updateVelocity(body.getTimestamp(), body.getAnkleRightXVelocity(), body.getAnkleRightYVelocity(), body.getAnkleRightZVelocity());
 			break;
 		case ELBOW_LEFT:
 			updateVelocity(body.getTimestamp(), body.getElbowLeftXVelocity(), body.getElbowLeftYVelocity(), body.getElbowLeftZVelocity());
 			break;
 		case ELBOW_RIGHT:
 			updateVelocity(body.getTimestamp(), body.getElbowRightXVelocity(), body.getElbowRightYVelocity(), body.getElbowRightZVelocity());
 			break;
 		case FOOT_LEFT:
 			updateVelocity(body.getTimestamp(), body.getFootLeftXVelocity(), body.getFootLeftYVelocity(), body.getFootLeftZVelocity());
 			break;
 		case FOOT_RIGHT:
 			updateVelocity(body.getTimestamp(), body.getFootRightXVelocity(), body.getFootRightYVelocity(), body.getFootRightZVelocity());
 			break;
 		case HAND_LEFT:
 			updateVelocity(body.getTimestamp(), body.getHandLeftXVelocity(), body.getHandLeftXVelocity(), body.getHandLeftXVelocity());
 			break;
 		case HAND_RIGHT:
 			updateVelocity(body.getTimestamp(), body.getHandRightXVelocity(), body.getHandRightYVelocity(), body.getHandRightZVelocity());
 			break;
 		case HEAD:
 			updateVelocity(body.getTimestamp(), body.getHeadXVelocity(), body.getHeadXVelocity(), body.getHeadXVelocity());
 			break;
 		case HIP_CENTER:
 			updateVelocity(body.getTimestamp(), body.getHipCenterXVelocity(), body.getHipCenterXVelocity(), body.getHipCenterZVelocity());
 			break;
 		case HIP_LEFT:
 			updateVelocity(body.getTimestamp(), body.getHipLeftXVelocity(), body.getHipLeftYVelocity(), body.getHipLeftZVelocity());
 			break;
 		case HIP_RIGHT:
 			updateVelocity(body.getTimestamp(), body.getHipRightXVelocity(), body.getHipRightYVelocity(), body.getHipRightZVelocity());
 			break;
 		case KNEE_LEFT:
 			updateVelocity(body.getTimestamp(), body.getKneeLeftXVelocity(), body.getKneeLeftYVelocity(), body.getKneeLeftZVelocity());
 			break;
 		case KNEE_RIGHT:
 			updateVelocity(body.getTimestamp(), body.getKneeRightXVelocity(), body.getKneeRightYVelocity(), body.getKneeRightZVelocity());
 			break;
 		case SHOULDER_CENTER:
 			updateVelocity(body.getTimestamp(), body.getShoulderCenterXVelocity(), body.getShoulderCenterYVelocity(), body.getShoulderCenterZVelocity());
 			break;
 		case SHOULDER_LEFT:
 			updateVelocity(body.getTimestamp(), body.getShoulderLeftXVelocity(), body.getShoulderLeftYVelocity(), body.getShoulderLeftZVelocity());
 			break;
 		case SHOULDER_RIGHT:
 			updateVelocity(body.getTimestamp(), body.getShoulderRightXVelocity(), body.getShoulderRightYVelocity(), body.getShoulderRightZVelocity());
 			break;
 		case SPINE:
 			updateVelocity(body.getTimestamp(), body.getSpineXVelocity(), body.getSpineYVelocity(), body.getSpineZVelocity());
 			break;
 		case WRIST_LEFT:
 			updateVelocity(body.getTimestamp(), body.getWristLeftXVelocity(), body.getWristLeftYVelocity(), body.getWristLeftZVelocity());
 			break;
 		case WRIST_RIGHT:
 			updateVelocity(body.getTimestamp(), body.getWristRightXVelocity(), body.getWristRightYVelocity(), body.getWristRightZVelocity());
 			break;
 		}
 	}
 
 	private void updateVelocity(long timestamp, double velocityX, double velocityY, double velocityZ) {
 		seriesVelocityX.add(new TimeSeriesDataItem(new FixedMillisecond(timestamp), velocityX));
 		seriesVelocityY.add(new TimeSeriesDataItem(new FixedMillisecond(timestamp), velocityY));
 		seriesVelocityZ.add(new TimeSeriesDataItem(new FixedMillisecond(timestamp), velocityZ));
 	}
 	
 	public void clearChart() {
 		seriesVelocityX.clear();
 		seriesVelocityY.clear();
 		seriesVelocityZ.clear();
 		dataset.removeAllSeries();
 	}
 
 }

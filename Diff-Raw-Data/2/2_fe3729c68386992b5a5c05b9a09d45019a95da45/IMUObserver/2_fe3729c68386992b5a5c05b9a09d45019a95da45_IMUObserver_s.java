 package rocketview;
 
 import cansocket.*;
 import widgets.*;
 
 import java.text.*;
 import java.awt.*;
 import javax.swing.*;
 
 /*----------------------------------------------------------------
  * Handles message ids IMUAccel & IMUGyro
  * Updates the 6 stripcharts
  *
  * for IMUAccel expect 3 short ints (6 bytes total): x, y, z
  * for IMUGyro expect 3 short ints (6 bytes total): pitch, yaw, roll
  *
  * chart vertical axis expects values 0-4096 opaque units
  */
 class IMUObserver extends JPanel implements CanObserver
 {
 	protected final DecimalFormat fmt = new DecimalFormat("0.0");
 	protected final JLabel tempLabel = new JLabel("Temp: -");
 
 	// first subscript in arrays is one of these
 	protected final int IMU_ACCEL = 0;
 	protected final int IMU_GYRO = 1;
 
 	protected final StripChart data[][];
 	protected final String title[][] = {
 		{ "X", "Y", "Z", },
 		{ "Pitch", "Yaw", "Roll", }
 	};
 	protected final String unit[] = { "g", "deg/s" };
 	protected final int id[] = {
 		CanBusIDs.IMU_ACCEL_DATA,
 		CanBusIDs.IMU_GYRO_DATA
 	};
 
 /*
 	// low limit, graph vertical axis
 	protected final double vLow[][] = {
 		{ -2, -2, -16 },
 		{ -90, -90, -90 }
 	};
 
 	// high limit, graph vertical axis
 	protected final double vHigh[][] = {
 		{ 2, 2, 16 },
 		{ 90, 90, 90 }
 	};
 */
 
 
 	public IMUObserver(CanDispatch dispatch)
 	{
 		dispatch.add(this);
 		
 		GridBoxLayout mainLayout = new GridBoxLayout();
 		setLayout(mainLayout);
 
 		add(tempLabel);
 		
 		JPanel subSys = new JPanel();
 		subSys.setLayout(new GridLayout(0, 1));
 		data = new StripChart[title.length][];
 		for(int i = 0; i < data.length; ++i)
 		{
 			data[i] = new StripChart[title[i].length];
 			for(int j = 0; j < data[i].length; ++j)
 				subSys.add(createChart(dispatch, i, j));
 		}
 
 		GridBagConstraints gbc = (GridBagConstraints)mainLayout.getConstraints(tempLabel).clone();
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.weighty = 1.0;
 		gbc.gridheight = GridBagConstraints.REMAINDER;
 		add(subSys, gbc);
 	}
 
 	protected JComponent createChart(CanDispatch dispatch, int type, int num)
 	{
 		StripChart chart = new StripChart();
 		data[type][num] = chart;
 
 		// data.setYRange(vLow[type][num], vHigh[type][num]);
 		chart.setYRange(0, 4095);
 
 		chart.setBorder(new IMUBorder(dispatch, title[type][num], unit[type], id[type], num));
 		return chart;
 	}
 
 	public void message(CanMessage msg)
 	{
 		int type;
 		switch (msg.getId())
 		{
 			case CanBusIDs.IMU_ACCEL_DATA:
 				type = IMU_ACCEL;
 				break;
 			case CanBusIDs.IMU_GYRO_DATA:
 				type = IMU_GYRO;
 				break;
 			case CanBusIDs.TEMP_REPORT_DATA:
				double v = 3487.972309658033 / Math.log(3.116381893600779E8 / msg.getData16(0) - 252811.23882451496) + 273.15;
 				tempLabel.setText("Temp: " + fmt.format(v) + " C");
 				return;
 			default:
 				return;
 		}
 
 		for(int i = 0; i < data[type].length; ++i)
 			data[type][i].addPoint(msg.getTimestamp() / 100f, msg.getData16(i));
 	}
 }

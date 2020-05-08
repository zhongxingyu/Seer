 /*
  * Remote.java
  *
  * Copyright 2012 Thomas Buck <xythobuz@me.com>
  *
  * This file is part of xyRobot.
  *
  * xyRobot is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * xyRobot is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with xyRobot.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.imageio.*;
 import java.io.*;
 
 public class Remote extends JFrame implements ActionListener, ChangeListener,
 												MouseListener, KeyListener,
 												ComponentListener {
 
	private final String version = "0.9.1";
 	public int width = 485 + DistanceWindow.width;
 	public int height = 420;
 	public final int xOff = 0;
 	public final int yOff = 0;
 
 	private PaintCanvas canvas = null;
 
 	private JPanel bigPanel = null;
 
 	private JPanel serialStuff = null;
 	private JComboBox portSelector = null;
 	private JButton openPort = null;
 	private JButton closePort = null;
 
 	private JLabel status = null;
 
 	private JPanel cameraStuff = null;
 	private JButton trigger = null;
 	private JSlider depth = null;
 	private JButton save = null;
 	private JSlider camMoveX = null;
 	private JSlider camMoveY = null;
 	private JButton camSettings = null;
 
 	private JPanel driveStuff = null;
 	private JTextField dist = null;
 	private JLabel distCM = null;
 	private JTextField speed = null;
 	private JCheckBox reverse = null;
 	private JButton drive = null;
 	private JTextField degree = null;
 	private JLabel degreeDeg = null;
 	private JButton turnRight = null;
 	private JButton turnLeft = null;
 
 	public CanvasWindow canvasWin = null;
 	public DistanceWindow distanceWin = null;
 
 	public SerialCommunicator serial = null;
 
 	private final int UP    = 0;
 	private final int DOWN  = 1;
 	private final int LEFT  = 2;
 	private final int RIGHT = 3;
 
 	private int registers[] = {
 		0x00, // Z & Offset
 		0x03, // N & VH & Gain
 		0x00, // C1
 		0x01, // C0
 		0x01, // P
 		0x00, // M
 		0x01, // X
 		0x04  // E & I & V
 	};
 
 	public Remote() {
 		super("xyRobotRemote");
 
 		serial = new SerialCommunicator(this);
 
 		setBounds(xOff, yOff, width, height);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(null);
 		addComponentListener(this);
 		Container c = getContentPane();
 
 		distanceWin = new DistanceWindow(this, 80);
 		distanceWin.setBounds(0, 0, distanceWin.width, height);
 		distanceWin.setVal(80);
 		c.add(distanceWin);
 
 		bigPanel = new JPanel();
 		bigPanel.setBounds(distanceWin.width, 0, width - distanceWin.width, height);
 		bigPanel.addKeyListener(this);
 		bigPanel.setLayout(new FlowLayout());
 		c.add(bigPanel);
 
 		canvas = new PaintCanvas(128, 128, 2);
 		// canvas.setBounds(10, 10, 256, 256);
 		canvas.setPreferredSize(new Dimension(256, 256));
 		canvas.setBorder(BorderFactory.createLoweredBevelBorder());
 		canvas.addMouseListener(this);
 		canvas.addKeyListener(this);
 		bigPanel.add(canvas);
 
 		cameraStuff = new JPanel();
 		cameraStuff.setBorder(BorderFactory.createTitledBorder("Camera"));
 		// cameraStuff.setBounds(275, 105, 215, 225);
 		cameraStuff.setPreferredSize(new Dimension(215, 225));
 		cameraStuff.setLayout(null);
 		cameraStuff.addKeyListener(this);
 		bigPanel.add(cameraStuff);
 
 		serialStuff = new JPanel();
 		serialStuff.setBorder(BorderFactory.createTitledBorder("Serial"));
 		// serialStuff.setBounds(275, 5, 215, 95);
 		serialStuff.setPreferredSize(new Dimension(215, 95));
 		serialStuff.setLayout(null);
 		serialStuff.addKeyListener(this);
 		bigPanel.add(serialStuff);
 
 		driveStuff = new JPanel();
 		driveStuff.setBorder(BorderFactory.createTitledBorder("Drive"));
 		// driveStuff.setBounds(275, 330, 215, 100);
 		driveStuff.setPreferredSize(new Dimension(215, 100));
 		driveStuff.setLayout(null);
 		driveStuff.addKeyListener(this);
 		bigPanel.add(driveStuff);
 
		status = new JLabel("<html>Initializing xyRobotRemote " + version);
 		// status.setBounds(10, 275, 256, 50);
 		// status.setSize(new Dimension(256, 50));
 		status.addKeyListener(this);
 		status.setBorder(BorderFactory.createLoweredBevelBorder());
 		bigPanel.add(status);
 
 		// --------------------------------
 
 		String[] ports = HelperUtility.getPorts();
 		if ((ports == null) || (ports.length == 0)) {
 			showError("No serial ports found!");
 			System.exit(1);
 		}
 		java.util.List<String> list = java.util.Arrays.asList(ports);
 		java.util.Collections.reverse(list);
 		ports = (String[])list.toArray();
 		for (int i = 0; i < ports.length; i++) {
 			if (ports[i].equals("/dev/tty.xyRobot-DevB") && (i != 0)) {
 				String tmp = ports[0];
 				ports[0] = ports[i];
 				ports[i] = tmp;
 			}
 		}
 		portSelector = new JComboBox(ports);
 		portSelector.setBounds(5, 20, 205, 30);
 		portSelector.addKeyListener(this);
 		serialStuff.add(portSelector);
 
 		openPort = new JButton();
 		openPort.setText("Open");
 		openPort.setBounds(5, 55, 100, 30);
 		openPort.addActionListener(this);
 		openPort.addKeyListener(this);
 		serialStuff.add(openPort);
 
 		closePort = new JButton();
 		closePort.setText("Close");
 		closePort.setBounds(105, 55, 100, 30);
 		closePort.addActionListener(this);
 		closePort.addKeyListener(this);
 		serialStuff.add(closePort);
 
 		trigger = new JButton();
 		trigger.setText("Shoot Pic");
 		trigger.setBounds(60, 15, 140, 30);
 		trigger.addActionListener(this);
 		trigger.addKeyListener(this);
 		cameraStuff.add(trigger);
 
 		camSettings = new JButton();
 		camSettings.setText("Cam Registers");
 		camSettings.setBounds(60, 95, 140, 30);
 		camSettings.addActionListener(this);
 		camSettings.addKeyListener(this);
 		cameraStuff.add(camSettings);
 
 		save = new JButton();
 		save.setText("Save image");
 		save.setBounds(60, 130, 140, 30);
 		save.addActionListener(this);
 		save.addKeyListener(this);
 		cameraStuff.add(save);
 
 		depth = new JSlider(0, 3);
 		depth.setValue(3);
 		depth.addChangeListener(this);
 		depth.setBounds(60, 50, 140, 40);
 		depth.setMajorTickSpacing(1);
 		depth.setMajorTickSpacing(1);
 		depth.setPaintTicks(true);
 		java.util.Hashtable<Integer, JLabel> table = new java.util.Hashtable<Integer, JLabel>();
 		table.put(0, new JLabel("1"));
 		table.put(1, new JLabel("2"));
 		table.put(2, new JLabel("4"));
 		table.put(3, new JLabel("8"));
 		depth.setLabelTable(table);
 		depth.setPaintLabels(true);
 		depth.addKeyListener(this);
 		cameraStuff.add(depth);
 
 		camMoveY = new JSlider(JSlider.VERTICAL, 0, 180, 40); // vertical cam movement slider
 		camMoveY.setBounds(5, 15, 60, 205);
 		camMoveY.addChangeListener(this);
 		camMoveY.setMajorTickSpacing(45);
 		camMoveY.setMinorTickSpacing(45);
 		camMoveY.setPaintTicks(true);
 		camMoveY.addKeyListener(this);
 		cameraStuff.add(camMoveY);
 
 		camMoveX = new JSlider(0, 180);
 		camMoveX.setValue(95);
 		camMoveX.setBounds(60, 165, 150, 60);
 		camMoveX.addChangeListener(this);
 		camMoveX.setMajorTickSpacing(45);
 		camMoveX.setMinorTickSpacing(45);
 		camMoveX.setPaintTicks(true);
 		camMoveX.addKeyListener(this);
 		cameraStuff.add(camMoveX);
 
 		dist = new JTextField();
 		dist.setText("100");
 		dist.setBounds(5, 20, 40, 30);
 		dist.addKeyListener(this);
 		driveStuff.add(dist);
 
 		distCM = new JLabel();
 		distCM.setText("cm");
 		distCM.setBounds(45, 20, 20, 30);
 		distCM.addKeyListener(this);
 		driveStuff.add(distCM);
 
 		speed = new JTextField();
 		speed.setText("200");
 		speed.setBounds(70, 20, 40, 30);
 		speed.addKeyListener(this);
 		driveStuff.add(speed);
 
 		reverse = new JCheckBox();
 		reverse.setBounds(115, 20, 30, 30);
 		reverse.addKeyListener(this);
 		driveStuff.add(reverse);
 
 		drive = new JButton();
 		drive.setText("Drive");
 		drive.setBounds(150, 20, 60, 30);
 		drive.addActionListener(this);
 		drive.addKeyListener(this);
 		driveStuff.add(drive);
 
 		degree = new JTextField();
 		degree.setText("180");
 		degree.setBounds(15, 55, 40, 30);
 		degree.addKeyListener(this);
 		driveStuff.add(degree);
 
 		degreeDeg = new JLabel();
 		degreeDeg.setText("\u00b0");
 		degreeDeg.setBounds(55, 55, 30, 30);
 		degreeDeg.addKeyListener(this);
 		driveStuff.add(degreeDeg);
 
 		turnLeft = new JButton();
 		turnLeft.setText("L");
 		turnLeft.setBounds(80, 55, 50, 30);
 		turnLeft.addActionListener(this);
 		turnLeft.addKeyListener(this);
 		driveStuff.add(turnLeft);
 
 		turnRight = new JButton();
 		turnRight.setText("R");
 		turnRight.setBounds(135, 55, 50, 30);
 		turnRight.addActionListener(this);
 		turnRight.addKeyListener(this);
 		driveStuff.add(turnRight);
 
 		setControls(false); // Turn everything off
 		setVisible(true);
 
 		// Shutdown Hook to close an opened serial port
 		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownThread(this), "Serial Closer"));
 
		log("Rendering Image");
 		ImageTest t = new ImageTest("splash.png");
 		canvas.setData(t.getData());
 		fixImageColor(false); // Converting color-pics leads to dark images...
 	}
 
 	// ------------------------------
 	// |            Misc            |
 	// ------------------------------
 
 	public void setControls(boolean open) {
 		closePort.setEnabled(open);
 		openPort.setEnabled(!open);
 		trigger.setEnabled(open);
 		// save.setEnabled(open);
 		camMoveX.setEnabled(open);
 		camMoveY.setEnabled(open);
 		// camSettings.setEnabled(open);
 		drive.setEnabled(open);
 		reverse.setEnabled(open);
 		speed.setEnabled(open);
 		dist.setEnabled(open);
 		degree.setEnabled(open);
 		turnRight.setEnabled(open);
 		turnLeft.setEnabled(open);
 		depth.setEnabled(open);
 	}
 
 	private void setDistanceView() {
 		serial.writeChar(0x86);
 		short[] dist = serial.readData(1);
 		distanceWin.setVal(dist[0]);
 	}
 
 	public void showError(String error) {
 		JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
 		log("ERROR: " + error);
 	}
 
 	public void showInfo(String info) {
 		JOptionPane.showMessageDialog(this, info, "Info", JOptionPane.INFORMATION_MESSAGE);
 		log(info);
 	}
 
 	public void log(String log) {
		String s = status.getText();
		if (s.length() > 50) {
			status.setText("<html>" + log);
		} else {
			status.setText(s + " -- " + log);
		}
 	}
 
 	private void readFile(String f) {
 		try {
 			FileReader fr = new FileReader(f);
 			readFile(fr);
 		} catch (Exception e) {
 			System.out.println("Error: " + e.getMessage());
 		}
 	}
 
 	private void readFile(InputStream is) {
 		InputStreamReader isr = new InputStreamReader(is);
 		readFile(isr);
 	}
 
 	private void readFile(Reader r) {
 		try {
 			BufferedReader br = new BufferedReader(r);
 			java.util.ArrayList<String> l = new java.util.ArrayList<String>();
 			String str, delimiter = ",\n";
 			while ((str = br.readLine()) != null) {
 				java.util.StringTokenizer t = new java.util.StringTokenizer(str, delimiter);
 				while (t.hasMoreTokens()) {
 					String element = t.nextToken();
 					l.add(element);
 				}
 			}
 			canvas.setData(l.toArray());
 		} catch (Exception e) {
 			System.out.println("Error: " + e.getMessage());
 		}
 	}
 
 	private	void printImageStats() {
 		int average = 0, max = 0, min = 256;
 		for (int i = 0; i < 128; i++) {
 			for (int j = 0; j < 128; j++) {
 				if (canvas.data[i][j] > max) {
 					max = canvas.data[i][j];
 				}
 				if (canvas.data[i][j] < min) {
 					min = canvas.data[i][j];
 				}
 
 				average += canvas.data[i][j];
 			}
 		}
 		average /= (128 * 128);
 
 		System.out.println("Current Image Statistics:");
 		System.out.println("Average: " + average);
 		System.out.println("Maximum: " + max);
 		System.out.println("Minimum: " + min);
 	}
 
 	private void fixImageColor(boolean print) {
 		int max = 0;
 		for (int i = 0; i < 128; i++) {
 			for (int j = 0; j < 128; j++) {
 				if (canvas.data[i][j] > max) {
 					max = canvas.data[i][j];
 				}
 			}
 		}
 		if (max < 255) {
 			double f = (double)255 / (double)max;
 			for (int i = 0; i < 128; i++) {
 				for (int j = 0; j < 128; j++) {
 					canvas.data[i][j] *= f;
 					if (canvas.data[i][j] > 255) {
 						canvas.data[i][j] = 255;
 					}
 				}
 			}
 			canvas.repaint();
 			if (print) System.out.printf("Image fixed. Factor: %.2f\n", f);
 		}
 	}
 
 	// ------------------------------
 	// | Calls for external windows |
 	// ------------------------------
 
 	public void distanceWindowKilled() {
 		distanceWin = new DistanceWindow(this);
 		if (serial.isOpen()) {
 			setDistanceView();
 		}
 	}
 
 	public void cameraWindowKilled() {
 		canvasWin = null;
 		canvas.setToRefresh(null);
 	}
 
 	public void registersUpdated(int[] regs) {
 		registers = regs;
 	}
 
 	// ------------------------------
 	// |         Listeners          |
 	// ------------------------------
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(turnRight)) {
 			turnButton(true);
 		} else if (e.getSource().equals(turnLeft)) {
 			turnButton(false);
 		} else if (e.getSource().equals(drive)) {
 			driveButton();
 		} else if (e.getSource().equals(camSettings)) {
 			camSettingsButton();
 		} else if (e.getSource().equals(trigger)) {
 			triggerButton();
 		} else if (e.getSource().equals(openPort)) {
 			openButton();
 		} else if (e.getSource().equals(closePort)) {
 			closeButton();
 		} else if (e.getSource().equals(save)) {
 			saveButton();
 		}
 	}
 
 	public void keyTyped(KeyEvent e) {
 		switch (e.getKeyChar()) {
 			case 'r':
 				canvas.randomize();
 				break;
 
 			case 's':
 				printImageStats();
 				break;
 
 			case 'd':
 				fixImageColor(true);
 				break;
 
 			case 'w':
 				System.out.println(getWidth() + "x" + getHeight());
 				break;
 		}
 	}
 	public void keyPressed(KeyEvent e) {}
 	public void keyReleased(KeyEvent e) {}
 
 	public void mouseClicked(MouseEvent e) {
 		if (e.getSource().equals(canvas)) {
 			if (e.getClickCount() == 2) {
 				if (canvasWin == null) {
 					canvasWin = new CanvasWindow(this);
 					canvas.setToRefresh(canvasWin.getCanvas());
 					canvasWin.getCanvas().setData(canvas.data);
 				}
 			}
 		}
 	}
 	public void mousePressed(MouseEvent e) {}
 	public void mouseReleased(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 
 	public void componentResized(ComponentEvent e) {
 		width = getWidth();
 		height = getHeight();
 
 		DistanceWindow dW = new DistanceWindow(this, 80);
 		dW.setVal(distanceWin.value);
 		getContentPane().remove(distanceWin);
 		distanceWin = dW;
 		distanceWin.setBounds(0, 0, distanceWin.width, height);
 		getContentPane().add(distanceWin);
 
 		bigPanel.setBounds(distanceWin.width, 0, width - distanceWin.width, height);
 
 		repaint();
 	}
 	public void componentHidden(ComponentEvent e) {}
 	public void componentMoved(ComponentEvent e) {}
 	public void componentShown(ComponentEvent e) {}
 
 	public void stateChanged(ChangeEvent e) {
 		// For the sliders
 		if (e.getSource().equals(camMoveY)) {
 			if (!((JSlider)e.getSource()).getValueIsAdjusting()) {
 				// New Y-Axis position
 				serial.writeChar(0x80);
 				serial.writeChar(camMoveY.getValue());
 			}
 		} else if (e.getSource().equals(camMoveX)) {
 			if (!((JSlider)e.getSource()).getValueIsAdjusting()) {
 				// New X-Axis position
 				serial.writeChar(0x81);
 				serial.writeChar(camMoveX.getValue());
 			}
 		}
 	}
 
 	// ------------------------------
 	// |      Button Handler        |
 	// ------------------------------
 
 	private void triggerButton() {
 		int length = 0;
 		switch(depth.getValue()) {
 			case 0:
 				serial.writeChar(0x88);
 				length = 64 * 32;
 				break;
 			case 1:
 				serial.writeChar(0x87);
 				length = 64 * 64;
 				break;
 			case 2:
 				serial.writeChar(0x85);
 				length = 128 * 64;
 				break;
 			case 3:
 				serial.writeChar(0x82);
 				length = 128 * 128;
 				break;
 		}
 		log("Writing registers.");
 		for (int i = 0; i < 8; i++) {
 			serial.writeChar(registers[i]);
 		}
 		log("Getting picture data");
 		short[] d = serial.readData(length);
 		if (d == null) {
 			if (serial.lastError == serial.TIMEOUT) {
 				showError("Timed out!");
 			} else {
 				showError("Error while reading!");
 			}
 		} else {
 			canvas.setData(d, 8);
 			log("Getting distance");
 			setDistanceView();
 			log("Done!");
 		}
 	}
 
 	private void turnButton(boolean turnRight) {
 		serial.writeChar(0x84); // command, degree, dir
 		serial.writeChar((char)Integer.valueOf(degree.getText(), 10).intValue());
 		String temp = "";
 		if (turnRight) {
 			serial.writeChar(1);
 			temp = "Right";
 		} else {
 			serial.writeChar(0);
 			temp = "Left";
 		}
 		log("Turning " + degree.getText() + "\u00b0 " + temp);
 	}
 
 	private void driveButton() {
 		serial.writeChar(0x83); // command, dist, speed, dir
 		serial.writeChar((char)Integer.valueOf(dist.getText(), 10).intValue());
 		serial.writeChar((char)Integer.valueOf(speed.getText(), 10).intValue());
 		String temp = "";
 		if (reverse.isSelected()) {
 			temp = "Reverse";
 			serial.writeChar(0);
 		} else {
 			serial.writeChar(1);
 			temp = "Forward";
 		}
 		log("Driving " + dist.getText() + "cm with " + speed.getText() + " " + temp);
 	}
 
 	private void camSettingsButton() {
 		Registers reg = new Registers(this, registers);
 		reg.setVisible(true);
 	}
 
 	private void openButton() {
 		// Open Port, enable controls
 		if (serial.openPort((String)portSelector.getSelectedItem())) {
 			setControls(true);
 			log("Connected!");
 		} else {
 			showError("Could not open port " + (String)portSelector.getSelectedItem());
 		}
 	}
 
 	private void closeButton() {
 		if (serial.isOpen())
 			serial.closePort();
 		setControls(false);
 		log("Connection closed.");
 	}
 
 	private void saveButton() {
 		// Render canvas into image
 		JFileChooser chooser = new JFileChooser();
 		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
 			String path = chooser.getSelectedFile().getAbsolutePath();
 			if (!path.endsWith(".png")) {
 				path += ".png";
 			}
 			File f = new File(path);
 			try {
 				ImageIO.write(canvas.paintIntoImage(), "png", f);
 			} catch (Exception ex) {
 				showError("Could not write!" + ex.getMessage());
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		Remote r = new Remote();
 	}
 }
 
 // Close serial connection on shutdown
 class ShutdownThread implements Runnable {
 	private Remote remote = null;
 
 	public ShutdownThread(Remote r) {
 		remote = r;
 	}
 
 	public void run() {
 		if (remote.serial != null) {
 			if (remote.serial.isOpen()) {
 				remote.serial.closePort();
 			}
 		}
 	}
 }

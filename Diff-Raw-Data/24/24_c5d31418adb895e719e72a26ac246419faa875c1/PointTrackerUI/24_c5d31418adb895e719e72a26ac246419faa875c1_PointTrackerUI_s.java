 package wiitracker.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileNotFoundException;
 import java.util.Date;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 

import wiitracker.fingertracking.FingerLabeler;
import wiitracker.output.XMLWriter;

 import motej.IrCameraMode;
 import motej.IrCameraSensitivity;
 import motej.Mote;
 
 /**
  * A window which displays the four points seen by the Wiimote IR camera.
  * 
  */
public class PointTrackerUI extends JFrame {
 
 	private static final long serialVersionUID = 9219999171449199862L;
 
 	/**
 	 * The panel which draws the points.
 	 */
 	private SwingPointTracker tracker;
 
 	/**
 	 * Writes XML for the points the Wiimote detects.
 	 */
 	private static XMLWriter writer;
 
 	/**
 	 * True if we are currently writing to XML; false otherwise.
 	 */
 	private static boolean writing;
 
 	/**
 	 * Panel which provides buttons for setting sensitivity of the IR camera and
 	 * for starting and stopping writing to XML.
 	 * 
 	 */
 	protected static class MoteSettingsPanel extends JPanel {
 
 		private static final long serialVersionUID = -1893034921967850816L;
 
 		/**
 		 * The Wiimote we are getting IR data from.
 		 */
 		private Mote mote;
 		private JButton XMLstart;
 
 		/**
 		 * Create a new settings panel which controls the sensitivity of a
 		 * Wiimote.
 		 * 
 		 * @param m
 		 *            The Wiimote this panel controls.
 		 */
 		public MoteSettingsPanel(Mote m) {
 			super();
 			this.setLayout(new GridBagLayout());
 
 			Dimension d = new Dimension(1024, 100);
 			this.setSize(d);
 			this.setPreferredSize(d);
 			this.setMinimumSize(d);
 			this.setMaximumSize(d);
 
 			GridBagConstraints c = new GridBagConstraints();
 
 			this.mote = m;
 			writing = false;
 
 			JLabel sensitivityLabel = new JLabel("Sensitivity: ");
 			JButton b1 = new JButton("1");
 			JButton b2 = new JButton("2");
 			JButton b3 = new JButton("3");
 			JButton b4 = new JButton("4");
 			JButton b5 = new JButton("5");
 			JButton b6 = new JButton("Marcan");
 			JButton b7 = new JButton("Inio");
 			XMLstart = new JButton("XML start");
 
 			ActionListener senstivityListener = new ActionListener() {
 
 				/**
 				 * Set the Wiimote sensitivity.
 				 */
 				public void actionPerformed(ActionEvent e) {
 					String bText = ((JButton) e.getSource()).getText();
 					System.out.println("Setting sensitivity to " + bText);
 					mote.disableIrCamera();
 					if (bText.equals("1")) {
 						mote.enableIrCamera(IrCameraMode.FULL, IrCameraSensitivity.WII_LEVEL_1);
 					} else if (bText.equals("2")) {
 						mote.enableIrCamera(IrCameraMode.FULL, IrCameraSensitivity.WII_LEVEL_2);
 					} else if (bText.equals("3")) {
 						mote.enableIrCamera(IrCameraMode.FULL, IrCameraSensitivity.WII_LEVEL_3);
 					} else if (bText.equals("4")) {
 						mote.enableIrCamera(IrCameraMode.FULL, IrCameraSensitivity.WII_LEVEL_4);
 					} else if (bText.equals("5")) {
 						mote.enableIrCamera(IrCameraMode.FULL, IrCameraSensitivity.WII_LEVEL_5);
 					} else if (bText.equals("Marcan")) {
 						mote.enableIrCamera(IrCameraMode.FULL, IrCameraSensitivity.MARCAN);
 					} else if (bText.equals("Inio")) {
 						mote.enableIrCamera(IrCameraMode.FULL, IrCameraSensitivity.INIO);
 					}
 				}
 
 			};
 
 			ActionListener xmlListener = new ActionListener() {
 				/**
 				 * Start and stop XML writing
 				 */
 				public void actionPerformed(ActionEvent e) {
 					if (!writing) {
 						try {
 							writer = new XMLWriter(new Date().toString(), "./data.xml");
 							mote.addIrCameraListener(writer);
 							mote.addMoteDisconnectedListener(writer);
 							writing = true;
 							((JButton) e.getSource()).setText("XML stop");
 						} catch (FileNotFoundException ex) {
 							ex.printStackTrace();
 						}
 					} else {
 						writer.stopWrite();
 						writing = false;
 						((JButton) e.getSource()).setText("XML start");
 					}
 				}
 			};
 
 			b1.addActionListener(senstivityListener);
 			b2.addActionListener(senstivityListener);
 			b3.addActionListener(senstivityListener);
 			b4.addActionListener(senstivityListener);
 			b5.addActionListener(senstivityListener);
 			XMLstart.addActionListener(xmlListener);
 
 			c.gridx = 0;
 			c.gridy = 0;
 			c.gridwidth = 1;
 			c.gridheight = 1;
 			c.weighty = 1;
 
 			this.add(sensitivityLabel, c);
 
 			c.gridx = 1;
 			c.gridy = 0;
 			c.gridwidth = 1;
 			c.gridheight = 1;
 			c.weighty = 1;
 
 			this.add(b1, c);
 
 			c.gridx = 2;
 			c.gridy = 0;
 			c.gridwidth = 1;
 			c.gridheight = 1;
 			c.weighty = 1;
 
 			this.add(b2, c);
 
 			c.gridx = 3;
 			c.gridy = 0;
 			c.gridwidth = 1;
 			c.gridheight = 1;
 			c.weighty = 1;
 
 			this.add(b3, c);
 
 			c.gridx = 4;
 			c.gridy = 0;
 			c.gridwidth = 1;
 			c.gridheight = 1;
 			c.weighty = 1;
 
 			this.add(b4, c);
 
 			c.gridx = 5;
 			c.gridy = 0;
 			c.gridwidth = 1;
 			c.gridheight = 1;
 			c.weighty = 1;
 
 			this.add(b5, c);
 
 			c.gridx = 6;
 			c.gridy = 0;
 			c.gridwidth = 1;
 			c.gridheight = 1;
 			c.weighty = 1;
 
 			this.add(b6, c);
 
 			c.gridx = 7;
 			c.gridy = 0;
 			c.gridwidth = 1;
 			c.gridheight = 1;
 			c.weighty = 1;
 
 			this.add(b7, c);
 
 			c.gridx = 10;
 			c.gridy = 0;
 			c.gridwidth = 1;
 			c.gridheight = 1;
 			c.weighty = 1;
 
 			this.add(XMLstart, c);
 
 		}
 	}
 
 	/**
 	 * Create a window which draws points representing the points seen by the
 	 * Wiimote and which controls the Wiimote's sensitivity.
 	 * 
 	 * @param m
 	 *            The Wiimote to get data from and control.
 	 */
 	public PointTrackerUI(Mote m) {
 		super("WiiMote Point Tracker");
 		this.getContentPane().setLayout(new BorderLayout());
 
 		// The settings panel lets us change sensitivity and start/stop XML
 		// writing.
 		MoteSettingsPanel settingsPanel = new MoteSettingsPanel(m);
 		this.getContentPane().add(settingsPanel, BorderLayout.SOUTH);
 		tracker = new SwingPointTracker();
 		Dimension size = new Dimension(1024, 824);
 		this.setSize(size);
 		this.setPreferredSize(size);
 		this.setMinimumSize(size);
 		this.getContentPane().add(tracker, BorderLayout.CENTER);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		FingerLabeler fingerLabel = new FingerLabeler();
 		fingerLabel.addIrCameraListener(tracker);
 		// The Point Tracker should be updated whenever an IR point moves.
 		m.addIrCameraListener(fingerLabel);
 	}
 
 	public SwingPointTracker getSwingPointTracker() {
 		return tracker;
 	}
 
 }

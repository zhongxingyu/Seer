 package com.aerodynelabs.habtk.tracking;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 import com.aerodynelabs.habtk.connectors.APRSIS;
 import com.aerodynelabs.habtk.connectors.APRSSource;
 import com.aerodynelabs.habtk.connectors.parsers.APRSPacket;
 import com.aerodynelabs.map.MapPoint;
 
 public class APRSTracker extends Tracker {
 	
 	private static APRSIS aprsis = null;
 //	private static AGWPE agwpe = null;
 	
 	private String callsign;
 	private APRSSource source;
 	
 	@SuppressWarnings("serial")
 	class SetupDialog extends JDialog {
 		
 		private final String options[] = {
 			"APRS-IS"
 		};
 		
 		boolean accepted = false;
 		
 		JTextField fCallsign;
 		JComboBox fConnector;
 		JTextField fName;
 		
 		public SetupDialog() {
 			setTitle("Setup APRS Tracker");
 			setModal(true);
 			SpringLayout layout = new SpringLayout();
 			setLayout(layout);
 			Container pane = getContentPane();
 			
 			JLabel lCallsign = new JLabel("Callsign:");
 			fCallsign = new JTextField(10);
 			layout.putConstraint(SpringLayout.NORTH, fCallsign, 6, SpringLayout.NORTH, pane);
 			layout.putConstraint(SpringLayout.BASELINE, lCallsign, 0, SpringLayout.BASELINE, fCallsign);
 			layout.putConstraint(SpringLayout.WEST, lCallsign, 6, SpringLayout.WEST, pane);
 			layout.putConstraint(SpringLayout.WEST, fCallsign, 6, SpringLayout.EAST, lCallsign);
 			layout.putConstraint(SpringLayout.EAST, fCallsign, -6, SpringLayout.EAST, pane);
 			add(lCallsign);
 			add(fCallsign);
 			
 			JLabel lConnector = new JLabel("Connector:");
 			fConnector = new JComboBox(options);
 			fName = new JTextField(30);
 			fName.setEditable(false);
 			JButton bConnector = new JButton("Configure");
 			bConnector.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					callsign = fCallsign.getText();
 					if(fConnector.getSelectedItem().equals("APRS-IS")) {
 						boolean reconfig = false;
 						if(aprsis != null) {	// Connection already exists
							// TODO
 							int n = JOptionPane.showConfirmDialog(null,
 									"APRS-IS is already configured.\n" +
 									"Would you like to reconfigure?",
 									"APRS-IS Configuration",
 									JOptionPane.YES_NO_OPTION);
 							if(n == JOptionPane.YES_OPTION) {
 								reconfig = true;
 							} else {
 								source = aprsis;
 								aprsis.addFilter("f/" + callsign + "/100");
 								aprsis.addFilter("b/" + callsign);
 							}
 						}
 						if(aprsis == null || reconfig == true) { // Connection has not been created
 							String serverCall = (String)JOptionPane.showInputDialog("APRS-IS Callsign:");
 							String serverPass = null;
 							if(serverCall != null) serverPass = (String)JOptionPane.showInputDialog("APRS-IS Passcode:");
 							if(serverPass == null) {
 								accepted = false;
 								return;
 							} else {
 								APRSIS server = new APRSIS("midwest.aprs2.net", 14580, serverCall, serverPass);
 								if(server.isValid()) {
 									server.addFilter("f/" + callsign + "/100");
 									server.addFilter("b/" + callsign);
 									aprsis = server;
 									source = aprsis;
 //									fName.setText(callsign + ": " + server.toString());
 								} else {
 									accepted = false;
 									return;
 								}
 							}
 						}
 					}
 					// Add more connector types here
 					fName.setText(callsign + ": " + source.toString());
 				}
 			});
 			layout.putConstraint(SpringLayout.NORTH, fConnector, 6, SpringLayout.SOUTH, fCallsign);
 			layout.putConstraint(SpringLayout.BASELINE, lConnector, 0, SpringLayout.BASELINE, fConnector);
 			layout.putConstraint(SpringLayout.WEST, lConnector, 6, SpringLayout.WEST, pane);
 			layout.putConstraint(SpringLayout.WEST, fConnector, 6, SpringLayout.EAST, lConnector);
 			layout.putConstraint(SpringLayout.EAST, fConnector, -6, SpringLayout.EAST, pane);
 			layout.putConstraint(SpringLayout.NORTH, fName, 6, SpringLayout.SOUTH, fConnector);
 			layout.putConstraint(SpringLayout.NORTH, bConnector, 0, SpringLayout.NORTH, fName);
 			layout.putConstraint(SpringLayout.SOUTH, bConnector, 0, SpringLayout.SOUTH, fName);
 			layout.putConstraint(SpringLayout.WEST, fName, 6, SpringLayout.WEST, pane);
 			layout.putConstraint(SpringLayout.WEST, bConnector, 6, SpringLayout.EAST, fName);
 			layout.putConstraint(SpringLayout.EAST, pane, 6, SpringLayout.EAST, bConnector);
 			add(lConnector);
 			add(fConnector);
 			add(fName);
 			add(bConnector);
 			
 			JButton cancel = new JButton("Cancel");
 			cancel.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					accepted = false;
 					dispose();
 				}
 			});
 			JButton ok = new JButton("Accept");
 			ok.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					try {
 						// XXX validate callsign
 						boolean validCall = true;
 						accepted = (validCall || source.isValid());
 					} catch(Exception e1) {
 						e1.printStackTrace();
 						accepted = false;
 					}
 					dispose();
 				}
 			});
 			layout.putConstraint(SpringLayout.NORTH, ok, 6, SpringLayout.SOUTH, bConnector);
 			layout.putConstraint(SpringLayout.SOUTH, pane, 6, SpringLayout.SOUTH, ok);
 			layout.putConstraint(SpringLayout.BASELINE, cancel, 0, SpringLayout.BASELINE, ok);
 			layout.putConstraint(SpringLayout.EAST, ok, -6, SpringLayout.EAST, pane);
 			layout.putConstraint(SpringLayout.EAST, cancel, -6, SpringLayout.WEST, ok);
 			add(cancel);
 			add(ok);
 			
 			pack();
 			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 			setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
 			setVisible(true);
 		}
 		
 		public boolean wasAccepted() {
 			return accepted;
 		}
 		
 	}
 	
 	public String getCallsign() {
 		return callsign;
 	}
 	
 	public APRSSource getSource() {
 		return source;
 	}
 	
 	public APRSTracker() {
 	}
 
 	@Override
 	public boolean setup() {
 		SetupDialog dialog = new SetupDialog();
 		return dialog.wasAccepted();
 	}
 	
 	@Override
 	public String toString() {
 		return callsign + ": " + source.toString();
 	}
 	
 	@Override
 	public boolean isReady() {
 		return source.isReady();
 	}
 	
 	@Override
 	public MapPoint getPacket() {
 		MapPoint position = null;
 		if(source.isReady() == true) {
 			String line = source.readLine();
 			APRSPacket pkt = new APRSPacket(line);
 			if(pkt.isPosition() == true) {
 				if(pkt.getFrom().equalsIgnoreCase(callsign))
 					position = new MapPoint(pkt.getLatitude(), pkt.getLongitude(), pkt.getAltitude(), pkt.getTimestamp() / 1000, pkt.getFrom());
 			}
 		}
 		return position;
 	}
 
 }

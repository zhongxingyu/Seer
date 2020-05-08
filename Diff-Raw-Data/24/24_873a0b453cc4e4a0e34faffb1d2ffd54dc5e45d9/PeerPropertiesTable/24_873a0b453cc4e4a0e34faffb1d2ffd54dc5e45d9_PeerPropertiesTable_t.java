 package kimononet.simulation;
 
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 import kimononet.geo.GeoLocation;
 import kimononet.geo.GeoLocationException;
 import kimononet.geo.GeoVelocity;
 import kimononet.peer.PeerAddress;
 import kimononet.peer.PeerAddressException;
 import kimononet.peer.PeerAgent;
 
 public class PeerPropertiesTable extends JTable {
 
 	private final String[] properties = {	"Name",					// Row 0
 											"Address",				// Row 1
 											"Longitude (Degrees)",	// Row 2
 											"Latitude (Degrees)",	// Row 3
											"Speed (m/s)",			// Row 4
											"Bearing (Degrees)" };	// Row 5
 
 	private PropertyTableModel model;
 	private Simulation sim;
 
 	public void refresh() {
 		model.getDataVector().clear();
 
 		PeerAgent agent = sim.getCurrentPeerAgent();
 
 		if (agent == null)
 			return;
 
 		String name = agent.getPeer().getName();
 		PeerAddress address = agent.getPeer().getAddress();
 		GeoLocation location = agent.getPeer().getLocation();
 		GeoVelocity velocity = agent.getPeer().getVelocity();
 
 		String longitude = new String();
 		String latitude = new String();
 
 		longitude = Double.toString(location.getLongitude());
 		latitude = Double.toString(location.getLatitude());
 
 		model.addRow(new Object[] {properties[0], name});
 		model.addRow(new Object[] {properties[1], address.toString()});
 		model.addRow(new Object[] {properties[2], longitude});
 		model.addRow(new Object[] {properties[3], latitude});
		model.addRow(new Object[] {properties[4], Float.toString(velocity.getSpeed())});
		model.addRow(new Object[] {properties[5], Double.toString(Math.toDegrees(velocity.getBearing()))});
 	}
 
 	public PeerPropertiesTable(Simulation s) {
 		super();
 
 		sim = s;
 
 		model = new PropertyTableModel();
 
 		model.addTableModelListener(new TableModelListener() {
 			public void tableChanged(TableModelEvent tme) {
 				if (tme.getType() == TableModelEvent.UPDATE) {
 					// Automatically apply user-entered values upon table value change.
 
 					PeerAgent agent = sim.getCurrentPeerAgent();
 
 					if (agent == null)
 						return;
 
 					try {
 						String name = (String)model.getValueAt(0, 1);
 						PeerAddress address = new PeerAddress((String)model.getValueAt(1, 1));
 						double longitude = Double.parseDouble((String)model.getValueAt(2, 1));
 						double latitude = Double.parseDouble((String)model.getValueAt(3, 1));
						float speed = Float.parseFloat((String)model.getValueAt(4, 1));
						float bearing = (float)Math.toRadians(Double.parseDouble((String)model.getValueAt(5, 1)));
 
						GeoLocation location = new GeoLocation(longitude, latitude, agent.getPeer().getLocation().getAccuracy());
 						GeoVelocity velocity = new GeoVelocity(speed, bearing); 
 
 						agent.getPeer().setName(name);
 						agent.getPeer().setAddress(address);
 						sim.updateCurrentPeerAgent(location, velocity);
 					} catch (NumberFormatException nfe) {
 						JOptionPane.showMessageDialog(sim.getFrame(), "Please a valid number.");
 					} catch (GeoLocationException gle) {
 						JOptionPane.showMessageDialog(sim.getFrame(), gle.getMessage());
 					} catch (PeerAddressException pae) {
 						JOptionPane.showMessageDialog(sim.getFrame(), "Please a valid address.");
 					}
 
 					sim.refresh();
 				}
 			}
 		});
 
 		setModel(model);
 	}
 
 }

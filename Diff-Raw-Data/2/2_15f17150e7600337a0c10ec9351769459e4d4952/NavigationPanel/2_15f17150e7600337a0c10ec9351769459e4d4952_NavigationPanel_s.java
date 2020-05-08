 package im.jeanfrancois.etsmaps.ui;
 
 import ca.odell.glazedlists.BasicEventList;
 import ca.odell.glazedlists.EventList;
 import ca.odell.glazedlists.gui.TableFormat;
 import ca.odell.glazedlists.swing.EventComboBoxModel;
 import ca.odell.glazedlists.swing.EventTableModel;
 import ca.odell.glazedlists.util.concurrent.ReadWriteLock;
 import com.google.inject.Inject;
 import im.jeanfrancois.etsmaps.model.Landmark;
 import im.jeanfrancois.etsmaps.model.Leg;
 import im.jeanfrancois.etsmaps.model.NavigableMap;
 import im.jeanfrancois.etsmaps.model.Route;
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 
 /**
  * Navigation panel that contains all the UI controls to search for
  * landmarks and navigate between them.
  *
  * @author jfim
  */
 public class NavigationPanel extends JPanel {
 	private EventList<Leg> routeLegs = new BasicEventList<Leg>();
 	private NavigableMap map;
 
 	@Inject
 	public NavigationPanel(final NavigableMap map) {
 		this.map = map;
		setLayout(new MigLayout("wrap 2", "", "[][][][grow,fill]"));
 
 		EventList<Landmark> landmarks = new BasicEventList<Landmark>();
 		landmarks.addAll(map.getLandmarks());
 
 		add(new JLabel("From"));
 
 		final JComboBox originComboBox = new JComboBox(new EventComboBoxModel<Landmark>(landmarks));
 		add(originComboBox);
 		add(new JLabel("To"));
 
 		final JComboBox destinationComboBox = new JComboBox(new EventComboBoxModel<Landmark>(landmarks));
 		add(destinationComboBox);
 
 		final JButton button = new JButton("Navigate");
 		add(button, "span 2");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Route route = map.getRouteBetweenLandmarks((Landmark) originComboBox.getSelectedItem(),
 						(Landmark) destinationComboBox.getSelectedItem());
 
 				ArrayList<Leg> legs = new ArrayList<Leg>();
 
 				for (int i = 0; i < route.getLegCount(); ++i) {
 					legs.add(route.getLeg(i));
 				}
 
 				ReadWriteLock lock = routeLegs.getReadWriteLock();
 				lock.writeLock().lock();
 				routeLegs.clear();
 				routeLegs.addAll(legs);
 				lock.writeLock().unlock();
 			}
 		});
 
 		add(new JLabel("Directions"), "span 2");
 
 		final JTable table = new JTable(new EventTableModel<Leg>(routeLegs,
 				new TableFormat<Leg>() {
 					public int getColumnCount() {
 						return 2;
 					}
 
 					public String getColumnName(int i) {
 						return "";
 					}
 
 					public Object getColumnValue(Leg leg, int i) {
 						if (i == 0) {
 							return leg.getDescription();
 						}
 
 						return leg.getLengthInMetres() + " m";
 					}
 				}));
 		add(new JScrollPane(table), "width 100%, span 2");
 	}
 }

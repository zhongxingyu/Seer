 package grisu.frontend.view.swing.jobmonitoring.single;
 
 import grisu.control.ServiceInterface;
 import grisu.model.GrisuRegistryManager;
 import grisu.model.UserEnvironmentManager;
 
 import java.awt.CardLayout;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedSet;
 
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 public class MultiSingleJobMonitoringGrid extends JPanel {
 
 	private final ServiceInterface si;
 	private final UserEnvironmentManager em;
 
 	private final Map<String, SingleJobTabbedPane> grids = new HashMap<String, SingleJobTabbedPane>();
 
 	/**
 	 * Create the panel.
 	 */
 	public MultiSingleJobMonitoringGrid(ServiceInterface si) {
 		this.si = si;
 		this.em = GrisuRegistryManager.getDefault(si)
 				.getUserEnvironmentManager();
 
 		setLayout(new CardLayout(0, 0));
 	}
 
 	public void displayGridForApplication(final String application) {
 
 		if (grids.get(application) == null) {
 
 			final SingleJobTabbedPane temp = new SingleJobTabbedPane(si,
 					application);
 			grids.put(application, temp);
			SwingUtilities.invokeLater(new Thread() {
				public void run() {
					add(temp, application);
				}
			});
 		}
 
 		SwingUtilities.invokeLater(new Thread() {
 			@Override
 			public void run() {
 				final CardLayout cl = (CardLayout) (getLayout());
 				cl.show(MultiSingleJobMonitoringGrid.this, application);
 			}
 		});
 
 	}
 
 	public SortedSet<String> getAvailableApplications(boolean refreshOnBackend) {
 		return em.getCurrentApplicationsBatch(refreshOnBackend);
 	}
 
 }

 package grisu.frontend.view.swing.jobmonitoring.single;
 
 import grisu.control.ServiceInterface;
 import grisu.frontend.control.jobMonitoring.RunningJobManager;
 import grisu.frontend.control.utils.ApplicationsManager;
 import grisu.frontend.model.events.JobCleanedEvent;
 import grisu.frontend.model.job.GrisuJob;
 
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;

import com.jidesoft.swing.JideTabbedPane;

 public class SingleJobTabbedPane extends JPanel implements
 SingleJobSelectionListener, EventSubscriber<JobCleanedEvent> {
 
 	private JideTabbedPane jideTabbedPane;
 	private SimpleSingleJobsGrid grid;
 
 	private final Map<String, JobDetailPanel> panels = Collections
 			.synchronizedMap(new HashMap<String, JobDetailPanel>());
 
 	private final ServiceInterface si;
 	private final String application;
 	private JButton updateButton;
 
 	/**
 	 * Create the panel.
 	 */
 	public SingleJobTabbedPane(ServiceInterface si, String application) {
 		this.si = si;
 		this.application = application;
 		setLayout(new BorderLayout(0, 0));
 		add(getJideTabbedPane(), BorderLayout.CENTER);
 		addSingleJobSelectionListener(this);
 		EventBus.subscribe(JobCleanedEvent.class, this);
 	}
 
 	// register a listener
 	public void addSingleJobSelectionListener(SingleJobSelectionListener l) {
 		getGrid().addJobSelectionListener(l);
 	}
 
 	private SimpleSingleJobsGrid getGrid() {
 
 		if (grid == null) {
 			grid = new SimpleSingleJobsGrid(si, application);
 		}
 		return grid;
 	}
 
 	private JideTabbedPane getJideTabbedPane() {
 		if (jideTabbedPane == null) {
 			jideTabbedPane = new JideTabbedPane();
 			jideTabbedPane.setHideOneTab(false);
 			jideTabbedPane.setShowCloseButtonOnTab(true);
 			jideTabbedPane.setCloseTabOnMouseMiddleButton(true);
			jideTabbedPane.setTabClosableAt(0, false);
 			String title = null;
 			if (StringUtils.isBlank(application)) {
 				title = "All jobs";
 			} else {
 				title = ApplicationsManager.getPrettyName(application)
 						+ " jobs";
 			}
 			jideTabbedPane.addTab(title, getGrid());
 
 		}
 		return jideTabbedPane;
 	}
 
 	private JButton getUpdateButton() {
 		if (updateButton == null) {
 			updateButton = new JButton("Update");
 			updateButton.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent e) {
 
 					RunningJobManager.getDefault(si).updateJobnameList(null, true);
 
 				}
 			});
 		}
 		return updateButton;
 	}
 
 	public void jobSelected(final GrisuJob bj) {
 
 		new Thread() {
 			@Override
 			public void run() {
 				final Component c = getJideTabbedPane().getSelectedComponent();
 				SwingUtilities.invokeLater(new Thread() {
 					@Override
 					public void run() {
 						c.setCursor(Cursor
 								.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
 					}
 				});
 
 				JobDetailPanel temp = panels.get(bj.getJobname());
 				if (panels.get(bj.getJobname()) == null) {
 					temp = new JobDetailPanelDefault(si, bj);
 					panels.put(bj.getJobname(), temp);
 				}
 
 				try {
 					getJideTabbedPane().setSelectedComponent(temp.getPanel());
 				} catch (final IllegalArgumentException e) {
 					getJideTabbedPane()
 					.addTab(bj.getJobname(), temp.getPanel());
 					getJideTabbedPane().setSelectedComponent(temp.getPanel());
 				} finally {
 					c.setCursor(Cursor
 							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 				}
 			}
 		}.start();
 
 	}
 
 	public void onEvent(JobCleanedEvent arg0) {
 
 		// System.out.println("Removing panel...");
 		final GrisuJob bj = arg0.getJob();
 		final JobDetailPanel temp = panels.get(bj.getJobname());
 		if (panels.get(bj.getJobname()) != null) {
 			getJideTabbedPane().setSelectedIndex(0);
 			panels.remove(bj.getJobname());
 			getJideTabbedPane().remove(temp.getPanel());
 		}
 
 	}
 
 	// remove a listener
 	synchronized public void removeSingleJobSelectionListener(
 			SingleJobSelectionListener l) {
 		getGrid().removeJobSelectionListener(l);
 	}
 }

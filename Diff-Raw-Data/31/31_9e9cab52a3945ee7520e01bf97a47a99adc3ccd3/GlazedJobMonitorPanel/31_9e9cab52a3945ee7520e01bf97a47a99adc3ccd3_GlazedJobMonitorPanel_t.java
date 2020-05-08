 package org.vpac.grisu.client.view.swing.jobs;
 
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Map;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.client.control.EnvironmentManager;
 import org.vpac.grisu.client.control.jobs.JobComparator;
 import org.vpac.grisu.client.control.jobs.JobFilterator;
 import org.vpac.grisu.client.control.jobs.JobnameComparator;
 import org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObject;
 import org.vpac.grisu.client.model.jobs.GrisuJobMonitoringObjectImpl;
 import org.vpac.grisu.client.model.template.JsdlTemplateListener;
 import org.vpac.grisu.client.view.swing.utils.Utils;
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.grisu.control.exceptions.NoSuchJobException;
 
 import ca.odell.glazedlists.FilterList;
 import ca.odell.glazedlists.GlazedLists;
 import ca.odell.glazedlists.SortedList;
 import ca.odell.glazedlists.gui.TableFormat;
 import ca.odell.glazedlists.swing.EventSelectionModel;
 import ca.odell.glazedlists.swing.EventTableModel;
 import ca.odell.glazedlists.swing.TableComparatorChooser;
 import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class GlazedJobMonitorPanel extends JPanel {
 
 	private JCheckBox updateCompleteListCheckBox;
 	private JMenuItem viewJobDetailsMenuItem;
 	private JMenuItem killAndCleanMenuItem;
 	private JMenuItem killJobMenuItem;
 	private JMenuItem refreshMenuItem;
 	private JPopupMenu popupMenu;
 	static final Logger myLogger = Logger.getLogger(GlazedJobMonitorPanel.class
 			.getName());
 
 	private JLabel filterLabel;
 	private JTextField filterField;
 	private JButton refreshButton;
 	private JButton deleteButton;
 	private JTable table;
 	private JScrollPane scrollPane;
 	private EventTableModel<GrisuJobMonitoringObject> jobModel = null;
 	private EventSelectionModel<GrisuJobMonitoringObject> selectionModel = null;
 
 	private EnvironmentManager em = null;
 	
 	private boolean firstRunFinished = false;
 
 	// /**
 	// * Create the panel
 	// */
 	// public GlazedJobMonitorPanel() {
 	// super();
 	// init(null);
 	// //
 	// }
 
 	public GlazedJobMonitorPanel(final EnvironmentManager em) {
 		super();
 		init(em);
 		add(getUpdateCompleteListCheckBox(), new CellConstraints(4, 6,
 				CellConstraints.RIGHT, CellConstraints.CENTER));
 
 		updateWholeJobTable();
 	}
 
 	public void updateSelectedJobs() {
 
 		new Thread() {
 			public void run() {
 				setPanelBusy(true);
 				try {
 					for (int row : getTable().getSelectedRows()) {
 						final GrisuJobMonitoringObject job = jobModel
 						.getElementAt(row);
 						updateJob(job);
 						jobModel.fireTableRowsUpdated(row, row);
 					}
 
 				} finally {
 					setPanelBusy(false);
 				}
 			}
 		}.start();
 
 	}
 
 	private void updateJob(final GrisuJobMonitoringObject job) {
 		if (job.getStatusAsInt() < JobConstants.FINISHED_EITHER_WAY) {
 
 			final Map<String, String> tempMap;
 
 			try {
 				tempMap = em.getServiceInterface().getAllJobProperties(
 						job.getName()).propertiesAsMap();
 			} catch (NoSuchJobException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return;
 			}
 
 //			Runnable doFillDetails = new Runnable() {
 //				public void run() {
 					job.fillJobDetails(tempMap);
 //				}
 //			};
 
 //			SwingUtilities.invokeLater(doFillDetails);
 		}
 	}
 
 	public void updateWholeJobTable() {
 
 		new Thread() {
 			public void run() {
 
 				setPanelBusy(true);
 				try {
 					// for (final GrisuJobMonitoringObject job :
 					// em.getGlazedJobManagement()
 					// .getAllJobs(false)) {
 					for (int i = 0; i < jobModel.getRowCount(); i++) {
 
 						final GrisuJobMonitoringObject job = jobModel
 								.getElementAt(i);
 
 						updateJob(job);
 
 						jobModel.fireTableRowsUpdated(i, i);
 					}
 
 					// jobModel.fireTableDataChanged();
 					if ( ! firstRunFinished ) {
 						// sort table according to submissionTime
 						
 					}
 					
 					firstRunFinished = true;
 				} finally {
 					setPanelBusy(false);
 				}
 			}
 
 		}.start();
 
 	}
 
 	public void init(EnvironmentManager em) {
 		setLayout(new FormLayout(
 			new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow(1.0)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.PREF_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow(1.0)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.PREF_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC}));
 		this.em = em;
 
 		add(getScrollPane(), new CellConstraints(2, 4, 5, 1, CellConstraints.FILL, CellConstraints.FILL));
 		add(getDeleteButton(), new CellConstraints(2, 6, CellConstraints.LEFT,
 				CellConstraints.CENTER));
 		add(getRefreshButton(), new CellConstraints(6, 6,
 				CellConstraints.RIGHT, CellConstraints.DEFAULT));
 		add(getFilterField(), new CellConstraints(4, 2, 3, 1,
 				CellConstraints.FILL, CellConstraints.CENTER));
 		add(getFilterLabel(), new CellConstraints(2, 2, CellConstraints.RIGHT,
 				CellConstraints.CENTER));
 
 		String[] propertyNames = new String[] { "name", "status",
 				"submissionTime", "submissionHost", "fqan" };
 		String[] columnLabels = new String[] { "Jobname", "Status",
 				"Submission Time", "Submission Host", "VO" };
 
 		TableFormat<GrisuJobMonitoringObject> tf = GlazedLists.tableFormat(
 				GrisuJobMonitoringObject.class, propertyNames, columnLabels);
 		SortedList<GrisuJobMonitoringObject> sortedJobList = new SortedList<GrisuJobMonitoringObject>(
 				em.getJobManager().getAllJobs(false),
 //				new JobComparator());
 				new JobnameComparator());
 		// jobModel = new
 		// EventTableModel<GrisuJobMonitoringObject>(em.getGlazedJobManagement().getAllJobs(),
 		// tf);
 		// add filtering to the table
 		FilterList<GrisuJobMonitoringObject> textFilteredIssues = new FilterList<GrisuJobMonitoringObject>(
 				sortedJobList, new TextComponentMatcherEditor(getFilterField(),
 						new JobFilterator()));
 
 		jobModel = new EventTableModel<GrisuJobMonitoringObject>(
 				textFilteredIssues, tf);
 		selectionModel = new EventSelectionModel<GrisuJobMonitoringObject>(
 				textFilteredIssues);
 
 		getTable().setModel(jobModel);
 		getTable().setSelectionModel(selectionModel);
 
 		// add sorting to the table
 		TableComparatorChooser tableSorter = new TableComparatorChooser(
 				getTable(), sortedJobList, false);
 		tableSorter.getComparatorsForColumn(2).clear();
 		tableSorter.getComparatorsForColumn(2).add(new JobComparator());
 
 	}
 
 	/**
 	 * @return
 	 */
 	protected JScrollPane getScrollPane() {
 		if (scrollPane == null) {
 			scrollPane = new JScrollPane();
 			scrollPane.setViewportView(getTable());
 		}
 		return scrollPane;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JTable getTable() {
 		if (table == null) {
 			table = new JTable();
 			table.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(final MouseEvent e) {
 
 					if (e.getClickCount() == 2) {
 						showJobDetails();
 					}
 				}
 			});
 			addPopup(table, getPopupMenu());
 		}
 		return table;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JButton getDeleteButton() {
 		if (deleteButton == null) {
 			deleteButton = new JButton();
 			deleteButton.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					myLogger.debug("Delete button pressed.");
 
 					cleanJobs();
 				}
 			});
 			deleteButton.setText("Kill & clean");
 		}
 		return deleteButton;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JButton getRefreshButton() {
 		if (refreshButton == null) {
 			refreshButton = new JButton();
 
 			refreshButton.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					myLogger.debug("Refresh button pressed.");
 
 					refreshJobs(getUpdateCompleteListCheckBox().isSelected());
 
 				}
 			});
 			refreshButton.setText("Refresh all");
 		}
 		return refreshButton;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JTextField getFilterField() {
 		if (filterField == null) {
 			filterField = new JTextField();
 		}
 		return filterField;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JLabel getFilterLabel() {
 		if (filterLabel == null) {
 			filterLabel = new JLabel();
 			filterLabel.setText("Filter:");
 		}
 		return filterLabel;
 	}
 
 	private void showJobDetails() {
 
 		new Thread() {
 			public void run() {
 				setPanelBusy(true);
 				try {
 					for (GrisuJobMonitoringObject job : selectionModel
 							.getSelected()) {
 						
						if ( job.getStatusAsInt(true) > JobConstants.READY_TO_SUBMIT ) {
 						DefaultJobDetailDialog jdp = new DefaultJobDetailDialog(
 								em);
 
 						jdp.setJob(job);
 
 						jdp.setVisible(true);
 						} else {
 							Utils.showErrorMessage(em, GlazedJobMonitorPanel.this,
 									"jobNotSubmittedYet", null);
 							System.out.println("Error Message closed.");
 							setPanelBusy(false);
 						}
 					}
 				} catch (Exception e) {
 					myLogger.error(e);
 					Utils.showErrorMessage(em, GlazedJobMonitorPanel.this,
 							"couldNotFindJobDirecotory", e);
 					setPanelBusy(false);
 					System.out.println("Error Message closed.");
 				} finally {
 					setPanelBusy(false);
 				}
 			}
 
 		}.start();
 	}
 
 	private void cleanJobs() {
 
 		StringBuffer message = new StringBuffer();
 		message.append("You are about to kill and clean these jobs:\n\n");
 		for (GrisuJobMonitoringObject job : selectionModel.getSelected()) {
 			message.append(job.getName() + "\n");
 		}
 		message
 				.append("\n\nAll data that was produced by these jobs will\nbe deleted. Do you really want to do that?");
 		int answer = JOptionPane.showConfirmDialog(this, message.toString(),
 				"Kill Jobs", JOptionPane.YES_NO_OPTION);
 
 		if (answer != JOptionPane.YES_OPTION) {
 			return;
 		}
 
 		new Thread() {
 			public void run() {
 
 				setPanelBusy(true);
 
 				try {
 					em.getJobManager().cleanJobs(
 							selectionModel.getSelected());
 				} catch (Throwable e) {
 					e.printStackTrace();
 					setPanelBusy(false);
 					Utils.showErrorMessage(em, GlazedJobMonitorPanel.this, "unspecifiedJobCleanError", null);
 				} finally {
 					setPanelBusy(false);
 				}
 
 			}
 		}.start();
 	}
 
 	private void killJobs() {
 
 		StringBuffer message = new StringBuffer();
 		message.append("You are about to kill these jobs:\n\n");
 		for (GrisuJobMonitoringObject job : selectionModel.getSelected()) {
 			message.append(job.getName() + "\n");
 		}
 		message.append("\n\nDo you really want to do that?");
 		int answer = JOptionPane.showConfirmDialog(this, message.toString(),
 				"Kill Jobs", JOptionPane.YES_NO_OPTION);
 
 		if (answer != JOptionPane.YES_OPTION) {
 			return;
 		}
 
 		new Thread() {
 			public void run() {
 
 				setPanelBusy(true);
 
 				try {
 					em.getJobManager().killJobs(
 							selectionModel.getSelected());
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} finally {
 					setPanelBusy(false);
 				}
 
 			}
 		}.start();
 
 	}
 
 	private void setPanelBusy(boolean busy) {
 
 		if (busy) {
 			GlazedJobMonitorPanel.this.setCursor(Cursor
 					.getPredefinedCursor(Cursor.WAIT_CURSOR));
 			getRefreshButton().setEnabled(false);
 			getDeleteButton().setEnabled(false);
 			getTable().setEnabled(false);
 			getViewJobDetailsMenuItem().setEnabled(false);
 			getKillAndCleanMenuItem().setEnabled(false);
 			getKillJobMenuItem().setEnabled(false);
 			getRefreshMenuItem().setEnabled(false);
 			getFilterField().setEnabled(false);
 			getUpdateCompleteListCheckBox().setEnabled(false);
 		} else {
 			GlazedJobMonitorPanel.this.setCursor(Cursor
 					.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			getRefreshButton().setEnabled(true);
 			getDeleteButton().setEnabled(true);
 			getTable().setEnabled(true);
 			getViewJobDetailsMenuItem().setEnabled(true);
 			getKillAndCleanMenuItem().setEnabled(true);
 			getKillJobMenuItem().setEnabled(true);
 			getRefreshMenuItem().setEnabled(true);
 			getFilterField().setEnabled(true);
 			getUpdateCompleteListCheckBox().setEnabled(true);
 		}
 
 	}
 
 	public void refreshJobs(final boolean forceServerRefresh) {
 
 		new Thread() {
 			public void run() {
 
 //				setPanelBusy(true);
 //				try {
 
 					if (forceServerRefresh) {
 						// refresh all jobs
 						em.getJobManager().refreshAllJobs(true);
 						updateWholeJobTable();
 					} else {
 
 						updateWholeJobTable();
 						// if (selectionModel.getSelected().size() == 0
 						// || refreshAll) {
 						// em.getGlazedJobManagement().refreshAllJobs(false);
 						// } else {
 						// em.getGlazedJobManagement().refreshJobs(
 						// selectionModel.getSelected());
 						// }
 					}
 
 //				} finally {
 //					setPanelBusy(false);
 //				}
 
 			}
 		}.start();
 
 	}
 
 	/**
 	 * @return
 	 */
 	protected JPopupMenu getPopupMenu() {
 		if (popupMenu == null) {
 			popupMenu = new JPopupMenu();
 			popupMenu.add(getViewJobDetailsMenuItem());
 			popupMenu.add(getRefreshMenuItem());
 			popupMenu.add(getKillJobMenuItem());
 			popupMenu.add(getKillAndCleanMenuItem());
 		}
 		return popupMenu;
 	}
 
 	/**
 	 * WindowBuilder generated method.<br>
 	 * Please don't remove this method or its invocations.<br>
 	 * It used by WindowBuilder to associate the {@link javax.swing.JPopupMenu}
 	 * with parent.
 	 */
 	private static void addPopup(Component component, final JPopupMenu popup) {
 		component.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				if (e.isPopupTrigger())
 					showMenu(e);
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				if (e.isPopupTrigger())
 					showMenu(e);
 			}
 
 			private void showMenu(MouseEvent e) {
 				popup.show(e.getComponent(), e.getX(), e.getY());
 			}
 		});
 	}
 
 	/**
 	 * @return
 	 */
 	protected JMenuItem getRefreshMenuItem() {
 		if (refreshMenuItem == null) {
 			refreshMenuItem = new JMenuItem();
 			refreshMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					updateSelectedJobs();
 				}
 			});
 			refreshMenuItem.setText("Refresh");
 		}
 		return refreshMenuItem;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JMenuItem getKillJobMenuItem() {
 		if (killJobMenuItem == null) {
 			killJobMenuItem = new JMenuItem();
 			killJobMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					killJobs();
 				}
 			});
 			killJobMenuItem.setText("Kill job(s)");
 		}
 		return killJobMenuItem;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JMenuItem getKillAndCleanMenuItem() {
 		if (killAndCleanMenuItem == null) {
 			killAndCleanMenuItem = new JMenuItem();
 			killAndCleanMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					cleanJobs();
 				}
 			});
 			killAndCleanMenuItem.setText("Kill & clean job(s)");
 		}
 		return killAndCleanMenuItem;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JMenuItem getViewJobDetailsMenuItem() {
 		if (viewJobDetailsMenuItem == null) {
 			viewJobDetailsMenuItem = new JMenuItem();
 			viewJobDetailsMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					showJobDetails();
 				}
 			});
 			viewJobDetailsMenuItem.setText("View job details");
 		}
 		return viewJobDetailsMenuItem;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JCheckBox getUpdateCompleteListCheckBox() {
 		if (updateCompleteListCheckBox == null) {
 			updateCompleteListCheckBox = new JCheckBox();
 			updateCompleteListCheckBox
 					.setHorizontalTextPosition(SwingConstants.LEADING);
 			updateCompleteListCheckBox
 					.setText("Rebuild Joblist (takes longer)");
 			updateCompleteListCheckBox
 					.setToolTipText("<html>If this is checked, Grisu deletes and rebuilds the whole <br>"
 							+ "internal list of jobs. This might be useful if you use another client to submit jobs.<br>"
 							+ "It makes sure that every one of your jobs will be displayed here,<br>"
 							+ "but in most cases it takes considerably longer.</html>");
 		}
 		return updateCompleteListCheckBox;
 	}
 
 }

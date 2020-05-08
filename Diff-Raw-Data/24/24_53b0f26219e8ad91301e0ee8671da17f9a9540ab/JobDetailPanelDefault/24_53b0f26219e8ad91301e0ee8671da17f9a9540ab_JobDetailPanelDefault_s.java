 package org.vpac.grisu.frontend.view.swing.jobmonitoring.single;
 
 import java.awt.Cursor;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Map;
 
 import javax.swing.ImageIcon;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.frontend.model.job.JobObject;
 import org.vpac.grisu.frontend.view.swing.files.preview.FileListWithPreviewPanel;
 import org.vpac.grisu.frontend.view.swing.jobmonitoring.single.appSpecific.AppSpecificViewerPanel;
 
 import au.org.arcs.jcommons.constants.Constants;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import com.jidesoft.swing.JideTabbedPane;
 
 public class JobDetailPanelDefault extends JPanel implements
 		PropertyChangeListener, JobDetailPanel {
 
 	static final Logger myLogger = Logger.getLogger(JobDetailPanelDefault.class
 			.getName());
 
 	private static String generateHtml(Map<String, String> jobProperties) {
 
 		final StringBuffer html = new StringBuffer(
 				"<html><table width=\"100%\">");
 
 		boolean alternate = true;
 		for (final String key : jobProperties.keySet()) {
 			if (alternate) {
 				html.append("<tr bgcolor=\"#FFFFFF\"><td>");
 			} else {
 				html.append("<tr><td>");
 			}
 			html.append(key);
 			html.append("</td><td>");
 			html.append(jobProperties.get(key));
 			html.append("</td></tr>");
 			alternate = !alternate;
 		}
 		html.append("</table></html>");
 
 		return html.toString();
 	}
 
 	private static String generateLogHtml(Map<Date, String> jobProperties) {
 
 		final StringBuffer html = new StringBuffer(
 				"<html><table width=\"100%\">");
 
 		boolean alternate = true;
 		for (final Date key : jobProperties.keySet()) {
 			if (alternate) {
 				html.append("<tr bgcolor=\"#FFFFFF\"><td>");
 			} else {
 				html.append("<tr><td>");
 			}
 			html.append(key.toString());
 			html.append("</td><td>");
 			html.append(jobProperties.get(key));
 			html.append("</td></tr>");
 			alternate = !alternate;
 		}
 		html.append("</table></html>");
 
 		return html.toString();
 	}
 
 	private final ImageIcon REFRESH_ICON = new ImageIcon(
 			JobDetailPanelDefault.class.getClassLoader().getResource(
 					"refresh.png"));
 
 	private JTextField txtNa;
 	private JobObject job;
 	private JideTabbedPane jideTabbedPane;
 	private JScrollPane scrollPane;
 	// private JTextArea propertiesTextArea;
 	private JScrollPane scrollPane_1;
 	private JEditorPane logTextArea;
 
 	private FileListWithPreviewPanel fileListWithPreviewPanel;
 	private final ServiceInterface si;
 	private JLabel lblApplication;
 	private JLabel lblJobname;
 	private JTextField jobnameTextField;
 	private JTextField applicationTextField;
 	private JLabel statusRefreshButton;
 	private JLabel lblSubmitted;
 
 	private JTextField submittedTextField;
 	private AppSpecificViewerPanel asvp = null;
 
 	// public static SimpleDateFormat format = new SimpleDateFormat(
 	// "dd.MM.yyyy - HH.mm.SS");
 
 	private JEditorPane propertiesPane;
 
 	/**
 	 * Create the panel.
 	 * 
 	 * @wbp.parser.constructor
 	 */
 	public JobDetailPanelDefault(ServiceInterface si) {
 
 		this.si = si;
 
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(30dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(42dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_ROWSPEC, }));
 		add(getLblJobname(), "2, 2, right, default");
 		add(getJobnameTextField(), "4, 2, fill, default");
 		add(getLblSubmitted(), "2, 4, right, default");
 		add(getSubmittedTextField(), "4, 4, fill, default");
 		add(getStatusRefreshButton(), "2, 6, right, default");
 		add(getTxtNa(), "4, 6, fill, default");
 		add(getLblApplication(), "2, 8, right, default");
 		add(getApplicationTextField(), "4, 8, fill, default");
 		add(getJideTabbedPane(), "2, 10, 5, 1, fill, fill");
 
 	}
 
 	public JobDetailPanelDefault(ServiceInterface si, JobObject job) {
 		this(si);
 		setJob(job);
 	}
 
 	private JTextField getApplicationTextField() {
 		if (applicationTextField == null) {
 			applicationTextField = new JTextField();
 			applicationTextField.setText("n/a");
 			applicationTextField.setHorizontalAlignment(SwingConstants.CENTER);
 			applicationTextField.setEditable(false);
 			applicationTextField.setColumns(10);
 		}
 		return applicationTextField;
 	}
 
 	private FileListWithPreviewPanel getFileListWithPreviewPanel() {
 		if (fileListWithPreviewPanel == null) {
 			fileListWithPreviewPanel = new FileListWithPreviewPanel(si, null,
 					null, false, true, false, true, false);
 		}
 		return fileListWithPreviewPanel;
 	}
 
 	private JideTabbedPane getJideTabbedPane() {
 		if (jideTabbedPane == null) {
 			jideTabbedPane = new JideTabbedPane();
 			jideTabbedPane.setTabPlacement(SwingConstants.TOP);
 
 			jideTabbedPane.addTab("Job directory", null,
 					getFileListWithPreviewPanel(), null);
 			jideTabbedPane.addTab("Properties", getScrollPane_1());
 			jideTabbedPane.addTab("Log", getScrollPane_1_1());
 		}
 		return jideTabbedPane;
 	}
 
 	private JTextField getJobnameTextField() {
 		if (jobnameTextField == null) {
 			jobnameTextField = new JTextField();
 			jobnameTextField.setText("n/a");
 			jobnameTextField.setHorizontalAlignment(SwingConstants.CENTER);
 			jobnameTextField.setEditable(false);
 			jobnameTextField.setColumns(10);
 		}
 		return jobnameTextField;
 	}
 
 	private JLabel getLblApplication() {
 		if (lblApplication == null) {
 			lblApplication = new JLabel("Application");
 		}
 		return lblApplication;
 	}
 
 	private JLabel getLblJobname() {
 		if (lblJobname == null) {
 			lblJobname = new JLabel("Jobname");
 		}
 		return lblJobname;
 	}
 
 	private JLabel getLblSubmitted() {
 		if (lblSubmitted == null) {
 			lblSubmitted = new JLabel("Submitted");
 		}
 		return lblSubmitted;
 	}
 
 	private JEditorPane getLogTextArea() {
 		if (logTextArea == null) {
 			logTextArea = new JEditorPane();
 			logTextArea.setContentType("text/html");
 			logTextArea.setEditable(false);
 		}
 		return logTextArea;
 	}
 
 	// private JTextArea getPropertiesTextArea() {
 	// if (propertiesTextArea == null) {
 	// propertiesTextArea = new JTextArea();
 	// }
 	// return propertiesTextArea;
 	// }
 
 	public JPanel getPanel() {
 		return this;
 	}
 
 	private JEditorPane getPropertiesPane() {
 		if (propertiesPane == null) {
 			propertiesPane = new JEditorPane();
 			propertiesPane.setContentType("text/html");
 			propertiesPane.setEditable(false);
 		}
 		return propertiesPane;
 	}
 
 	private JScrollPane getScrollPane_1() {
 		if (scrollPane == null) {
 			scrollPane = new JScrollPane();
 			scrollPane.setViewportView(getPropertiesPane());
 		}
 		return scrollPane;
 	}
 
 	private JScrollPane getScrollPane_1_1() {
 		if (scrollPane_1 == null) {
 			scrollPane_1 = new JScrollPane();
 			scrollPane_1.setViewportView(getLogTextArea());
 		}
 		return scrollPane_1;
 	}
 
 	private JLabel getStatusRefreshButton() {
 		if (statusRefreshButton == null) {
 			statusRefreshButton = new JLabel("Status");
 			statusRefreshButton.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mouseClicked(MouseEvent e) {
 
 					if (job != null) {
 						final Cursor old = statusRefreshButton.getCursor();
 						statusRefreshButton.setCursor(Cursor
 								.getPredefinedCursor(Cursor.WAIT_CURSOR));
 						job.getStatus(true);
 						statusRefreshButton.setCursor(old);
 					}
 
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent e) {
 
 					statusRefreshButton.setCursor(Cursor
 							.getPredefinedCursor(Cursor.HAND_CURSOR));
 
 				}
 
 				@Override
 				public void mouseExited(MouseEvent e) {
 					statusRefreshButton.setCursor(Cursor.getDefaultCursor());
 				}
 
 			});
 			statusRefreshButton.setBorder(null);
 			statusRefreshButton.setIcon(REFRESH_ICON);
 		}
 		return statusRefreshButton;
 	}
 
 	private JTextField getSubmittedTextField() {
 		if (submittedTextField == null) {
 			submittedTextField = new JTextField();
 			submittedTextField.setHorizontalAlignment(SwingConstants.CENTER);
 			submittedTextField.setText("n/a");
 			submittedTextField.setEditable(false);
 			submittedTextField.setColumns(10);
 		}
 		return submittedTextField;
 	}
 
 	private JTextField getTxtNa() {
 		if (txtNa == null) {
 			txtNa = new JTextField();
 			txtNa.setText("n/a");
 			txtNa.setHorizontalAlignment(SwingConstants.CENTER);
 			txtNa.setEditable(false);
 			txtNa.setColumns(10);
 		}
 		return txtNa;
 	}
 
 	public void propertyChange(PropertyChangeEvent evt) {
 
 		if (evt.getPropertyName().equals("status")) {
 
			System.out.println("Prop: new " + evt.getNewValue());
 			getTxtNa()
 					.setText(
 							JobConstants.translateStatus((Integer) (evt
 									.getNewValue())));
 			setLog();
 			setProperties();
 
			getFileListWithPreviewPanel().refresh();
 		}
 	}
 
 	public void setJob(JobObject job) {
 
 		this.job = job;
 
 		this.job.addPropertyChangeListener(this);
 
 		// try to create app specific panel
 		asvp = AppSpecificViewerPanel.create(si, job);
 		if (asvp != null) {
 			try {
 				asvp.setJob(job);
 				getJideTabbedPane().insertTab(
 						job.getApplication() + " details", null, asvp, null, 0);
 			} catch (Exception e) {
 				myLogger.error(e);
 			}
 		}
 
 		getJideTabbedPane().setSelectedIndex(0);
 		getStatusRefreshButton().setEnabled(true);
 		getJobnameTextField().setText(job.getJobname());
 
 		getFileListWithPreviewPanel().setRootUrl(job.getJobDirectoryUrl());
 		getFileListWithPreviewPanel().setCurrentUrl(job.getJobDirectoryUrl());
 		getTxtNa().setText(JobConstants.translateStatus(job.getStatus(false)));
 
 		setProperties();
 		setLog();
 
 	}
 
 	private void setLog() {
 
 		String html = generateLogHtml(job.getLogMessages(true));
 
 		getLogTextArea().setText(html);
 	}
 
 	private void setProperties() {
 		String propText = generateHtml(job.getAllJobProperties(true));
 		getPropertiesPane().setText(propText);
 
 		String subTime = null;
 		try {
 			final String subTimeString = job.getJobProperty(
 					Constants.SUBMISSION_TIME_KEY, false);
 			subTime = DateFormat.getInstance().format(
 					new Date(Long.parseLong(subTimeString)));
 		} catch (final Exception e) {
 			subTime = "n/a";
 		}
 		getSubmittedTextField().setText(subTime);
 
 		getApplicationTextField().setText(
 				job.getJobProperty(Constants.APPLICATIONNAME_KEY, false));
 
 	}
 }

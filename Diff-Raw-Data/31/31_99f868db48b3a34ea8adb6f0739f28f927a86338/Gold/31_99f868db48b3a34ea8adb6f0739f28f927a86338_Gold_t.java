 package grisu.frontend.view.swing.jobmonitoring.single.appSpecific;
 
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.frontend.control.clientexceptions.FileTransactionException;
 import grisu.frontend.view.swing.files.preview.fileViewers.utils.JobStatusFileDialog;
 import grisu.model.FileManager;
 import grisu.model.GrisuRegistryManager;
 
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.TimerTask;
 import java.util.logging.Level;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JSeparator;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.error.ErrorInfo;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class Gold extends AppSpecificViewerPanel {
 
 	static class JmolPanel extends JPanel {
 
 		private static final long serialVersionUID = -3661941083797644242L;
 
 		final Dimension currentSize = new Dimension();
 
 		final Rectangle rectClip = new Rectangle();
 
 		JmolPanel() {
 
 		}
 
 	}
 
 	class UpdateProgressTask extends TimerTask {
 
 		@Override
 		public void run() {
 
 			calculateCurrentLigandNoAndCpusAndLicenses();
 		}
 
 	}
 
 	private static final int GOLD_LICENSES = 12;
 
 	private JLabel label;
 	private JProgressBar progressBar;
 
 	private JLabel lblLigandsFinished;
 	// private String goldFilePath = null;
 	private String currentStatusPath = null;
 	private String statusPath = null;
 	private JSeparator separator;
 	private JButton btnHistory;
 	private JSeparator separator_1;
 	private int noCpus = 0;
 	private int noLigands = 0;
 
 	private final FileManager fm;
 	private JLabel lblStatus;
 
 	private String job_status_url;
 	private JLabel lblWalltime;
 
 	private int walltime = -1;
 	private Long startTimestamp = -1L;
 	private Long endTimestamp = -1L;
 	private JProgressBar walltimeProgressbar;
 	private JProgressBar licensesJobProgressBar;
 	private JProgressBar licensesAllProgressbar;
 	private JLabel lblLicensesUsedfor;
 	private JLabel lblLicensesUsedoverall;
 	private JProgressBar cpusProgressBar;
 	private JLabel lblCpusUsedfor;
 
 	public Gold(ServiceInterface si) {
 		super(si);
 		fm = GrisuRegistryManager.getDefault(si).getFileManager();
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(12dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(46dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(32dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(62dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(32dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(16dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(16dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(16dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_ROWSPEC, }));
 		add(getLabel(), "2, 4, 3, 1, default, top");
 		add(getProgressBar(), "4, 6, 3, 1, default, top");
 		add(getWalltimeProgressbar(), "8, 6, 3, 1, fill, top");
 		add(getLblLigandsFinished(), "4, 8, 3, 1, left, default");
 		add(getLblWalltime(), "8, 8, 3, 1, left, default");
 		add(getSeparator(), "2, 10, 9, 1, fill, fill");
		add(getLblStatus(), "2, 12, 3, 1, default, top");
		add(getBtnHistory(), "10, 12, right, top");
		add(getCpusProgressBar(), "4, 14, 3, 1, default, bottom");
		add(getLicensesJobProgressBar(), "8, 14, 3, 1");
		add(getLblCpusUsedfor(), "4, 16");
		add(getLblLicensesUsedfor(), "8, 16, 3, 1");
		add(getLicensesAllProgressbar(), "8, 18, 3, 1");
		add(getLblLicensesUsedoverall(), "8, 20, 3, 1");
 		add(getSeparator_1(), "2, 22, 9, 1");
 	}
 
 	protected void calculateCurrentLigandNoAndCpusAndLicenses() {
 
 		if (StringUtils.isBlank(currentStatusPath)) {
 			return;
 		}
 
 		getCpusProgressBar().setString("Updating...");
 		getLicensesAllProgressbar().setString("Updating...");
 		getLicensesJobProgressBar().setString("Updating...");
 		getWalltimeProgressbar().setString("Updating...");
 		getProgressBar().setString("Updating...");
 
 		List<String> lines = null;
 		Long timestampTemp = -1L;
 		try {
 			File currentStatusFile = fm.downloadFile(currentStatusPath);
 			lines = FileUtils.readLines(currentStatusFile);
 			timestampTemp = fm.getLastModified(currentStatusPath);
 		} catch (Exception e) {
 			myLogger.error(e);
 			getCpusProgressBar().setString("n/a");
 			getLicensesAllProgressbar().setString("n/a");
 			getLicensesJobProgressBar().setString("n/a");
 			getWalltimeProgressbar().setString("n/a");
 			getProgressBar().setString("n/a");
 			return;
 		}
 
 		Long deltaInSeconds = (timestampTemp - startTimestamp)/1000L;
 		getWalltimeProgressbar().setValue(deltaInSeconds.intValue());
 
 		Long hours = deltaInSeconds / 3600L;
 		getWalltimeProgressbar().setString(
 				hours + "  (of " + walltime / 3600 + ")");
 
 		if (lines.size() != 1) {
 			getCpusProgressBar().setString("Error...");
 			getLicensesAllProgressbar().setString("Error...");
 			getLicensesJobProgressBar().setString("Error...");
 			return;
 		}
 
 		String[] tokens = lines.get(0).split(",");
 
 		int cpusTemp = -1;
 		try {
 			cpusTemp = Integer.parseInt(tokens[1]);
 		} catch (Exception e) {
 			myLogger.error(e);
 		}
 
 		int licensesUserTemp = -1;
 		try {
 			licensesUserTemp = Integer.parseInt(tokens[2]);
 		} catch (Exception e) {
 			myLogger.error(e);
 		}
 
 		int licensesAllTemp = -1;
 		try {
 			licensesAllTemp = Integer.parseInt(tokens[3]);
 		} catch (Exception e) {
 			myLogger.error(e);
 		}
 
 		if (cpusTemp <= 0) {
 			getCpusProgressBar().setString("n/a");
			getCpusProgressBar().setValue(0);
 		} else {
 			getCpusProgressBar().setString(cpusTemp + "   (of " + noCpus + ")");
			getCpusProgressBar().setValue(cpusTemp);
 		}
 		if (licensesUserTemp <= 0) {
 			getLicensesJobProgressBar().setString("n/a");
			getLicensesJobProgressBar().setValue(0);
 		} else {
 			getLicensesJobProgressBar().setString(
 					licensesUserTemp + "   (of " + noCpus + ")");
			getLicensesJobProgressBar().setValue(licensesUserTemp);
 		}
 		if (licensesAllTemp <= 0) {
 			getLicensesAllProgressbar().setString("n/a");
			getLicensesAllProgressbar().setValue(0);
 		} else {
 			getLicensesAllProgressbar().setString(
					licensesAllTemp + "   (of " + GOLD_LICENSES + ")");
			getLicensesAllProgressbar().setValue(licensesAllTemp);
 		}
 
 
 		Integer ligands = -1;
 		try {
 			ligands = Integer.parseInt(tokens[4]);
 		} catch (Exception e) {
 			myLogger.error(e);
 			getProgressBar().setString("n/a");
 			getProgressBar().setValue(0);
 			return;
 		}
 
 		getProgressBar().setString(
 				ligands.toString() + "  (of " + noLigands + ")");
 		getProgressBar().setValue(ligands);
 	}
 
 	private File downloadJobStatusFile() throws FileTransactionException {
 		File temp = fm.downloadFile(job_status_url);
 		return temp;
 	}
 
 	private JButton getBtnHistory() {
 		if (btnHistory == null) {
 			btnHistory = new JButton("History");
 			btnHistory.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 
 					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
 					File temp;
 					try {
 						temp = downloadJobStatusFile();
 					} catch (FileTransactionException e1) {
 						ErrorInfo ei = new ErrorInfo(
 								"Download error",
 								"Error while trying to download job status file.",
 								e1.getLocalizedMessage(), (String) null, e1,
 								Level.SEVERE, (Map) null);
 						JXErrorPane.showDialog(Gold.this.getPanel(), ei);
 						return;
 					}
 
 					JobStatusFileDialog dialog = new JobStatusFileDialog();
 					dialog.setFileManagerAndUrl(fm, job_status_url);
 					dialog.setFile(null, temp);
 					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 					dialog.setVisible(true);
 
 				}
 			});
 		}
 		return btnHistory;
 	}
 
 	private JProgressBar getCpusProgressBar() {
 		if (cpusProgressBar == null) {
 			cpusProgressBar = new JProgressBar();
 			cpusProgressBar.setEnabled(false);
 			cpusProgressBar.setStringPainted(true);
 			cpusProgressBar.setString("n/a");
 			cpusProgressBar.setMinimum(0);
 		}
 		return cpusProgressBar;
 	}
 
 	private JLabel getLabel() {
 		if (label == null) {
 			label = new JLabel("Progress");
 		}
 		return label;
 	}
 
 	private JLabel getLblCpusUsedfor() {
 		if (lblCpusUsedfor == null) {
 			lblCpusUsedfor = new JLabel("Cpus used (for job)");
 		}
 		return lblCpusUsedfor;
 	}
 
 	private JLabel getLblLicensesUsedfor() {
 		if (lblLicensesUsedfor == null) {
 			lblLicensesUsedfor = new JLabel("Licenses used (for job)");
 		}
 		return lblLicensesUsedfor;
 	}
 
 	private JLabel getLblLicensesUsedoverall() {
 		if (lblLicensesUsedoverall == null) {
 			lblLicensesUsedoverall = new JLabel("Licenses used (overall)");
 		}
 		return lblLicensesUsedoverall;
 	}
 
 	private JLabel getLblLigandsFinished() {
 		if (lblLigandsFinished == null) {
 			lblLigandsFinished = new JLabel("Ligands finished");
 		}
 		return lblLigandsFinished;
 	}
 
 	private JLabel getLblStatus() {
 		if (lblStatus == null) {
 			lblStatus = new JLabel("Status");
 		}
 		return lblStatus;
 	}
 
 	private JLabel getLblWalltime() {
 		if (lblWalltime == null) {
 			lblWalltime = new JLabel("Walltime (in h)");
 		}
 		return lblWalltime;
 	}
 
 	private JProgressBar getLicensesAllProgressbar() {
 		if (licensesAllProgressbar == null) {
 			licensesAllProgressbar = new JProgressBar();
 			licensesAllProgressbar.setEnabled(false);
 			licensesAllProgressbar.setStringPainted(true);
 			licensesAllProgressbar.setString("n/a");
 			licensesAllProgressbar.setMinimum(0);
 		}
 		return licensesAllProgressbar;
 	}
 
 	private JProgressBar getLicensesJobProgressBar() {
 		if (licensesJobProgressBar == null) {
 			licensesJobProgressBar = new JProgressBar();
 			licensesJobProgressBar.setEnabled(false);
 			licensesJobProgressBar.setStringPainted(true);
 			licensesJobProgressBar.setString("n/a");
 			licensesJobProgressBar.setMinimum(0);
 		}
 		return licensesJobProgressBar;
 	}
 
 	private JProgressBar getProgressBar() {
 		if (progressBar == null) {
 			progressBar = new JProgressBar();
 			progressBar.setEnabled(false);
 			progressBar.setStringPainted(true);
 			progressBar.setString("n/a");
 			progressBar.setMinimum(0);
 			// progressBar.setMaximum(20000);
 			// progressBar.setMaximum(20);
 		}
 		return progressBar;
 	}
 
 	private JSeparator getSeparator() {
 		if (separator == null) {
 			separator = new JSeparator();
 		}
 		return separator;
 	}
 
 	private JSeparator getSeparator_1() {
 		if (separator_1 == null) {
 			separator_1 = new JSeparator();
 		}
 		return separator_1;
 	}
 
 	private JProgressBar getWalltimeProgressbar() {
 		if (walltimeProgressbar == null) {
 			walltimeProgressbar = new JProgressBar();
 			walltimeProgressbar.setEnabled(false);
 			walltimeProgressbar.setStringPainted(true);
 			walltimeProgressbar.setString("n/a");
 			walltimeProgressbar.setMinimum(0);
 		}
 		return walltimeProgressbar;
 	}
 
 	@Override
 	public void initialize() {
 
 		currentStatusPath = getJob().getJobDirectoryUrl() + "/"
 		+ "job_status_latest";
 		statusPath = getJob().getJobDirectoryUrl() + "/" + "job_status";
 
 		noCpus = getJob().getCpus();
 
 		walltime = getJob().getWalltimeInSeconds();
 
 		job_status_url = getJob().getJobDirectoryUrl() + "/" + "job_status";
 
 	}
 
 	@Override
 	void jobFinished() {
 		updateProgress();
 		// getBtnArchive().setEnabled(true);
 	}
 
 	@Override
 	public void jobStarted() {
 
 		File temp = getJob().downloadAndCacheOutputFile("ligands_total");
 		List<String> l = null;
 		try {
 			l = FileUtils.readLines(temp);
 		} catch (IOException e) {
 			myLogger.error(e);
 			return;
 		}
 
 		String ligandsTotalUrl = getJob().getJobDirectoryUrl()
 		+ "/ligands_total";
 		try {
 			startTimestamp = fm.getLastModified(ligandsTotalUrl);
 			endTimestamp = startTimestamp + (walltime * 1000);
 			getWalltimeProgressbar().setEnabled(true);
 			getWalltimeProgressbar().setMinimum(0);
 			getWalltimeProgressbar().setMaximum(walltime);
 		} catch (RemoteFileSystemException e1) {
 			myLogger.debug(e1);
 		}
 
 		if (l.size() != 1) {
 			myLogger.error("Can't get total number of ligands...");
 			return;
 		}
 		try {
 			noLigands = Integer.parseInt(l.get(0));
 		} catch (Exception e) {
 			e.printStackTrace();
 			myLogger.equals(e);
 			return;
 		}
 		getProgressBar().setEnabled(true);
 		getProgressBar().setMaximum(noLigands);
 
 		getLicensesAllProgressbar().setMinimum(0);
 		getLicensesAllProgressbar().setMaximum(GOLD_LICENSES);
 		getLicensesAllProgressbar().setEnabled(true);
 
 		getLicensesJobProgressBar().setMinimum(0);
 		getLicensesJobProgressBar().setMaximum(noCpus);
 		getLicensesJobProgressBar().setEnabled(true);
 
 		getCpusProgressBar().setMinimum(0);
 		getCpusProgressBar().setMaximum(noCpus);
 		getCpusProgressBar().setEnabled(true);
 
 		updateProgress();
 	}
 
 	@Override
 	public void jobUpdated(PropertyChangeEvent evt) {
 
 	}
 
 	@Override
 	void progressUpdate() {
 		updateProgress();
 	}
 
 	private void updateProgress() {
 
 		new Thread() {
 			@Override
 			public void run() {
 				calculateCurrentLigandNoAndCpusAndLicenses();
 			}
 		}.start();
 
 	}
 }

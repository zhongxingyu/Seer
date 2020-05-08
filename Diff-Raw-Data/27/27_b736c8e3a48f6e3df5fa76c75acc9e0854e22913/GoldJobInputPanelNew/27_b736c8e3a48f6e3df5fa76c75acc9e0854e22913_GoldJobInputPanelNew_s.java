 package org.bestgrid.virtscreen.view.gold;
 
 import grisu.control.ServiceInterface;
 import grisu.frontend.control.clientexceptions.FileTransactionException;
 import grisu.frontend.view.swing.jobcreation.JobCreationPanel;
 import grisu.frontend.view.swing.jobcreation.widgets.AbstractWidget;
 import grisu.frontend.view.swing.jobcreation.widgets.Cpus;
 import grisu.frontend.view.swing.jobcreation.widgets.Email;
 import grisu.frontend.view.swing.jobcreation.widgets.SingleInputFile;
 import grisu.frontend.view.swing.jobcreation.widgets.SubmissionLogPanel;
 import grisu.frontend.view.swing.jobcreation.widgets.Walltime;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import org.apache.commons.lang.StringUtils;
 import org.bestgrid.virtscreen.model.gold.GoldConfFile;
 import org.bestgrid.virtscreen.model.gold.GoldJob;
 import org.netbeans.validation.api.ui.ValidationGroup;
 import org.netbeans.validation.api.ui.ValidationPanel;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class GoldJobInputPanelNew extends JPanel implements JobCreationPanel,
		PropertyChangeListener {
 
 	private ServiceInterface si;
 	private ConfFileInputFile confFileInput;
 	private Cpus cpus;
 	private Walltime walltime;
 	private SubmissionLogPanel submissionLogPanel;
 	private JButton btnSubmit;
 
 	private final String HISTORY_KEY = "virtScreenGoldJob";
 
 	private final List<AbstractWidget> widgets = new LinkedList<AbstractWidget>();
 	private SingleInputFile singleInputFile;
 	private final ValidationPanel validationPanel = new ValidationPanel();
 	private final ValidationGroup validationGroup;
 	private JButton btnRefresh;
 	private JLabel errorLabel;
 	private AdvancedLibrarySelectPanel goldLibrarySelectPanel;
 	private DockingAmoungCombo dockingAmoungCombo;
 
 	// private GoldConfFileNew currentConfFile = null;
 	private Email email;
 	private Email email_1;
 
 	private GoldConfFile goldConfFile;
 
 	/**
 	 * Create the panel.
 	 */
 	public GoldJobInputPanelNew() {
 		super();
 
 		validationPanel.setInnerComponent(this);
 		validationGroup = validationPanel.getValidationGroup();
 
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(56dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(64dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(28dlu;min)"),
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(31dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_ROWSPEC, }));
 		add(getConfFileInput(), "2, 2, 5, 1, fill, fill");
 		add(getBtnRefresh(), "8, 2, default, top");
 		add(getLigandFileSelectPanel(), "2, 4, 7, 1, fill, center");
 		add(getDockingAmoungCombo(), "2, 6, left, top");
 		add(getCpus(), "4, 6, fill, top");
 		add(getWalltime(), "6, 6, 3, 1, fill, top");
 		add(getEmail_1(), "2, 8, 7, 1, fill, fill");
 		add(getErrorLabel(), "2, 10, 5, 1");
 		add(getBtnSubmit(), "8, 10, right, default");
 		add(getSubmissionLogPanel(), "2, 12, 7, 1, fill, fill");
 
 	}
 
 	private void addWidget(AbstractWidget widget) {
 		widgets.add(widget);
 		widget.setValidationGroup(validationGroup);
 	}
 
 	public boolean createsBatchJob() {
 		return false;
 	}
 
 	public boolean createsSingleJob() {
 		return true;
 	}
 
 	private JButton getBtnRefresh() {
 		if (btnRefresh == null) {
 			btnRefresh = new JButton("Reload");
 			btnRefresh.setEnabled(false);
 			btnRefresh.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					parseConfig();
 				}
 			});
 		}
 		return btnRefresh;
 	}
 
 	private JButton getBtnSubmit() {
 		if (btnSubmit == null) {
 			btnSubmit = new JButton("Submit");
 			btnSubmit.setEnabled(false);
 			btnSubmit.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					new Thread() {
 						@Override
 						public void run() {
 							submit();
 						}
 					}.start();
 				}
 			});
 		}
 		return btnSubmit;
 	}
 
 	private ConfFileInputFile getConfFileInput() {
 		if (confFileInput == null) {
 			confFileInput = new ConfFileInputFile(this);
 			confFileInput.setTitle(".conf file");
 			confFileInput.setHistoryKey(HISTORY_KEY + "_conf_file");
 			addWidget(confFileInput);
 		}
 		return confFileInput;
 	}
 
 	private Cpus getCpus() {
 		if (cpus == null) {
 			cpus = new Cpus();
 			cpus.setHistoryKey(HISTORY_KEY + "_cpus");
 			addWidget(cpus);
 		}
 		return cpus;
 	}
 
 	private DockingAmoungCombo getDockingAmoungCombo() {
 		if (dockingAmoungCombo == null) {
 			dockingAmoungCombo = new DockingAmoungCombo();
 			widgets.add(dockingAmoungCombo);
 
 		}
 		return dockingAmoungCombo;
 	}
 
 	private Email getEmail_1() {
 		if (email_1 == null) {
 			email_1 = new Email();
 			widgets.add(email_1);
 		}
 		return email_1;
 	}
 
 	private JLabel getErrorLabel() {
 		if (errorLabel == null) {
 			errorLabel = new JLabel("");
 			errorLabel.setForeground(Color.RED);
 		}
 		return errorLabel;
 	}
 
 	// private GoldLibrarySelectPanel getGoldLibrarySelectPanel() {
 	// if (goldLibrarySelectPanel == null) {
 	// goldLibrarySelectPanel = new GoldLibrarySelectPanel();
 	// addWidget(goldLibrarySelectPanel);
 	// }
 	// return goldLibrarySelectPanel;
 	// }
 
 	private AdvancedLibrarySelectPanel getLigandFileSelectPanel() {
 		if (goldLibrarySelectPanel == null) {
 			goldLibrarySelectPanel = new AdvancedLibrarySelectPanel();
 			addWidget(goldLibrarySelectPanel);
 		}
 		return goldLibrarySelectPanel;
 	}
 
 	public JPanel getPanel() {
 		return this;
 	}
 
 	public String getPanelName() {
 		return "Gold";
 	}
 
 	private SubmissionLogPanel getSubmissionLogPanel() {
 		if (submissionLogPanel == null) {
 			submissionLogPanel = new SubmissionLogPanel();
 		}
 		return submissionLogPanel;
 	}
 
 	public String getSupportedApplication() {
 		return "Gold";
 	}
 
 	private Walltime getWalltime() {
 		if (walltime == null) {
 			walltime = new Walltime();
 			walltime.setHistoryKey(HISTORY_KEY + "_walltime");
 			addWidget(walltime);
 		}
 		return walltime;
 	}
 
 	private void lockUI(final boolean lock) {
 
 		SwingUtilities.invokeLater(new Thread() {
 			@Override
 			public void run() {
 				if (goldConfFile.isValid()) {
 					getBtnSubmit().setEnabled(!lock);
 				} else {
 					getBtnSubmit().setEnabled(false);
 				}
 				getBtnRefresh().setEnabled(!lock);
 				for (AbstractWidget w : widgets) {
 					w.lockUI(lock);
 				}
 			}
 		});
 
 	}
 
 	public void parseConfig() {
 
 		final String confUrl = getConfFileInput().getInputFileUrl();
 		getErrorLabel().setText("");
 
 		if (StringUtils.isBlank(confUrl)) {
			setParseResult(false, "No .conf file specified.",
					"Select .conf file.");
 			getBtnRefresh().setEnabled(false);
 			getBtnSubmit().setEnabled(false);
 		} else {
 			final StringBuffer logMessage = new StringBuffer(
					"Parse log:\n\nLoading conf file...\n");
 			final StringBuffer fixes = new StringBuffer();
 
 			lockUI(true);
 
 			getSubmissionLogPanel().setText("Parsing file " + confUrl + "...");
 
 			new Thread() {
 				@Override
 				public void run() {
 					try {
 						// boolean checkValid = false;
 						goldConfFile.setConfFile(confUrl);
 
 						getLigandFileSelectPanel()
								.setGoldConfFile(goldConfFile);
 						getDockingAmoungCombo().setGoldConfFile(goldConfFile);
 						// checkValid = true;
 						lockUI(false);
 
 						getLigandFileSelectPanel().reset();
 
 					} catch (FileTransactionException e) {
 						logMessage.append("Can't access .conf file: "
 								+ getConfFileInput().getInputFileUrl());
 						fixes.append("Select an existing file.");
 						setParseResult(false, logMessage.toString(),
 								fixes.toString());
 						lockUI(false);
 						getBtnSubmit().setEnabled(false);
 						return;
 					} catch (Exception e) {
 						e.printStackTrace();
 						logMessage.append("Error opening .conf file: "
 								+ e.getLocalizedMessage());
 						fixes.append("Please check syntax of .conf file "
 								+ confUrl);
 						setParseResult(false, logMessage.toString(),
 								fixes.toString());
 						lockUI(false);
 						getBtnSubmit().setEnabled(false);
 						return;
 					}
 
 				}
 			}.start();
 		}
 
 	}
 
 	public void propertyChange(PropertyChangeEvent evt) {
 		// // TODO Auto-generated method stub
 		// setParseResult((Boolean) evt.getNewValue(),
 		// currentConfFile.getLogMessage(), currentConfFile.getFixes());
 		boolean success = goldConfFile.isValid();
 
 		String msg = goldConfFile.getParseMessages();
 
 		String fixes = goldConfFile.getFixes();
 
 		setParseResult(success, msg, fixes);
 		getBtnSubmit().setEnabled(success);
 		// X.p("Property change: " + success);
 
 	}
 
 	private void setParseResult(boolean success, String logMessage, String fixes) {
 
 		getBtnSubmit().setEnabled(success);
 		if (success) {
 			getErrorLabel().setText("");
 			logMessage = logMessage
					+ "Config file parsed successful. Job ready for submission.\n";
 		} else {
 			getErrorLabel()
					.setText(
							"Error when parsing .conf file. Please check log below for details.");
 			logMessage = logMessage
					+ "=====================================================================================\n\nError when parsing config file. Please check errors below, fix and update the file and click \"Reload\":\n";
 		}
 		getSubmissionLogPanel().setText(logMessage + "\n" + fixes);
 
 	}
 
 	public void setServiceInterface(ServiceInterface si) {
 		this.si = si;
 		this.goldConfFile = new GoldConfFile(si);
 		this.goldConfFile.addListener(this);
 		for (AbstractWidget w : widgets) {
 			w.setServiceInterface(si);
 		}
 
 	}
 
 	private void submit() {
 
 		final GoldJob job;
 
 		try {
 			job = new GoldJob(si, goldConfFile);
 
 		} catch (FileTransactionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
 
 		job.setCpus(getCpus().getCpus());
 		job.setWalltime(getWalltime().getWalltimeInSeconds());
 
 		String email = getEmail_1().getEmailAddress();
 		if (StringUtils.isNotBlank(email)
 				&& (getEmail_1().sendEmailWhenJobFinished() || getEmail_1()
 						.sendEmailWhenJobIsStarted())) {
 			job.setEmail(email);
 			job.sendEmailOnJobFinish(getEmail_1().sendEmailWhenJobFinished());
 			job.sendEmailOnJobStart(getEmail_1().sendEmailWhenJobIsStarted());
 		}
 
 		new Thread() {
 			@Override
 			public void run() {
 
 				try {
 					lockUI(true);
 
 					getSubmissionLogPanel().setJobObject(job.getJobObject());
 					job.createAndSubmitJob();
 
 					for (AbstractWidget w : widgets) {
 						w.saveItemToHistory();
 					}
 					getConfFileInput().setInputFile(null);
 					getLigandFileSelectPanel().setGoldConfFile(null);
 				} catch (Exception e) {
 					String message = "\nJob creation / submission failed: "
							+ e.getLocalizedMessage() + "\n";
 					e.printStackTrace();
 					getErrorLabel().setText(message);
 					getSubmissionLogPanel().appendMessage(message);
 				} finally {
 					lockUI(false);
 					SwingUtilities.invokeLater(new Thread() {
 						@Override
 						public void run() {
 							getBtnSubmit().setEnabled(false);
 							getBtnRefresh().setEnabled(false);
 							getLigandFileSelectPanel().lockUI(true);
 							getDockingAmoungCombo().lockUI(true);
 						}
 					});
 
 				}
 			}
 		}.start();
 
 	}
 }

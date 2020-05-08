 package org.vpac.grisu.frontend.view.swing.jobcreation;
 
 import java.awt.CardLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import org.jdesktop.swingx.JXErrorPane;
 import org.netbeans.validation.api.ui.ValidationPanel;
 import org.vpac.grisu.control.exceptions.JobPropertiesException;
 import org.vpac.grisu.control.exceptions.TemplateException;
 import org.vpac.grisu.frontend.model.job.JobObject;
 import org.vpac.grisu.frontend.view.swing.jobcreation.templates.TemplateObject;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import javax.swing.JLabel;
 import org.vpac.grisu.frontend.view.swing.DefaultFqanChangePanel;
 
 public class TemplateWrapperPanel extends JPanel {
 
 	public static final String JOB_CREATE_PANEL = "jobCreatePanel";
 	public static final String SUBMISSION_LOG_PANEL = "logPanel";
 
 	private final SubmissionMonitorPanel monitorPanel = new SubmissionMonitorPanel();
 	private final JPanel creationPanel;
 
 	private final CardLayout cardLayout = new CardLayout();
 
 	private final TemplateObject template;
 	private JButton submitButton;
 	private ValidationPanel validationPanel;
 	private JLabel label;
 	private JLabel label_1;
 	private final DefaultFqanChangePanel defaultFqanChangePanel = new DefaultFqanChangePanel();
 
 	/**
 	 * Create the panel.
 	 */
 	public TemplateWrapperPanel(TemplateObject template) {
 
 		this.template = template;
 		setLayout(cardLayout);
 
 		creationPanel = new JPanel();
 
 		add(creationPanel, JOB_CREATE_PANEL);
 		creationPanel.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(29dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("bottom:default"),
 				FormFactory.RELATED_GAP_ROWSPEC,}));
 
 		creationPanel.add(template.getTemplatePanel(), "2, 2, 3, 1, fill, fill");
 		if (template.getValidationPanel() != null) {
 			creationPanel
 					.add(template.getValidationPanel(), "2, 4, 2, 1, fill, fill");
 		}
 		
		creationPanel.add(defaultFqanChangePanel, "2, 6, left, center");
 
		creationPanel.add(getSubmitButton(), "4, 6, right, center");
 		add(monitorPanel, SUBMISSION_LOG_PANEL);
 
 		cardLayout.show(this, JOB_CREATE_PANEL);
 		monitorPanel.setTemplateWrapperPanel(this);
 		try {
 			defaultFqanChangePanel.setServiceInterface(template.getServiceInterface());
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private JButton getSubmitButton() {
 		if (submitButton == null) {
 			submitButton = new JButton("Submit");
 			submitButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 
 					try {
 						JobObject job = JobObject.createJobObject(template
 								.getServiceInterface(), template
 								.getJobSubmissionObject());
 						monitorPanel.startJobSubmission(job);
 					} catch (JobPropertiesException e) {
 
 						JXErrorPane.showDialog(e);
 
 						return;
 					}
 
 				}
 			});
 		}
 		return submitButton;
 	}
 
 	private ValidationPanel getValidationPanel() {
 		if (validationPanel == null) {
 			validationPanel = new ValidationPanel();
 		}
 		return validationPanel;
 	}
 
 	public void resetTemplate() throws TemplateException {
 
 		template.reset();
 	}
 
 	public void switchToJobCreationPanel() {
 		cardLayout.show(this, JOB_CREATE_PANEL);
 	}
 
 	public void switchToLogPanel() {
 		cardLayout.show(this, SUBMISSION_LOG_PANEL);
 	}
 }

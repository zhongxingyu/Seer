 
 
 package org.vpac.grisu.client.view.swing.template;
 
 import java.awt.CardLayout;
 
 import javax.swing.JPanel;
 
 import org.vpac.grisu.client.control.exceptions.JobSubmissionException;
 import org.vpac.grisu.client.control.exceptions.TemplateException;
 import org.vpac.grisu.client.control.template.ModuleException;
 import org.vpac.grisu.client.model.template.JsdlTemplate;
 import org.vpac.grisu.client.model.template.JsdlTemplateEvent;
 import org.vpac.grisu.client.model.template.JsdlTemplateListener;
 import org.vpac.grisu.client.view.swing.utils.WaitingPanel;
 
 public class JobPanel extends JPanel implements JsdlTemplateListener {
 	
 	private WaitingPanel waitingPanel;
 	public static final String STATUS_PANEL = "Status";
 	public static final String TEMPLATE_PANEL = "Template";
 	public static final String WAITING_PANEL = "Waiting";
 	public static final String TEMPLATE_ERROR_PANEL = "Template Error";
 
 	private JobStatusPanel jobStatusPanel;
 	private JobTemplatePanel jobTemplatePanel;
 	private TemplateErrorPanel templateErrorPanel;
 	
 	private JsdlTemplate template = null;
 	
 	private boolean templatePanelVisisble = true;
 	
 	private boolean panelInitializationFinished = false;
 	
 	/**
 	 * Create the panel
 	 */
 	public JobPanel() {
 		super();
 		setLayout(new CardLayout());
 		add(getWaitingPanel(), getWaitingPanel().getName());
 		showPanel(WAITING_PANEL);
 		add(getJobStatusPanel(), getJobStatusPanel().getName());
 		add(getJobTemplatePanel(), getJobTemplatePanel().getName());
 		add(getTemplateErrorPanel(), getTemplateErrorPanel().getName());
 		//
 	}
 	
 	private void showPanel(String panelName) {
 		CardLayout cl = (CardLayout)this.getLayout();
 	    cl.show(this,panelName);
 
 	    if ( TEMPLATE_PANEL.equals(panelName) ) {
 	    	templatePanelVisisble = true; 
 	    } else {
 	    	templatePanelVisisble = false;
 	    }
 	    
 	}
 	
 	public void showWaiting() {
 		showPanel(WAITING_PANEL);
 	}
 	
 	public void showStatus() {
 		showPanel(STATUS_PANEL);
 	}
 	
 	public void showTemplate() {
 		showPanel(TEMPLATE_PANEL);
 	}
 	
 	public void showTemplateError() {
 		showPanel(TEMPLATE_ERROR_PANEL);
 	}
 	
 //	public void toggleView() {
 //		if (templatePanelVisisble) {
 //			showStatus();
 //		} else {
 //			showTemplate();
 //		}
 //	}
 	
 	public void setTemplate(JsdlTemplate template) {
 		this.template = template;
 		template.addJsdlTemplateListener(this);
 		try {
 			getJobTemplatePanel().setTemplate(template);
 			getJobStatusPanel().setTemplate(template);
 		} catch (Exception e) {
 			getTemplateErrorPanel().setErrorMessage("Can't create this template.", e);
 			remove(getJobStatusPanel());
 			remove(getJobTemplatePanel());
 			showTemplateError();
 //			throw new TemplateException("Can't render template panel.", e);
 		}
 		
 		panelInitializationFinished = true;
 		showTemplate();
 	}
 	
 	public JsdlTemplate getTemplate() {
 		return this.template;
 	}
 	
 	/**
 	 * @return
 	 */
 	protected JobTemplatePanel getJobTemplatePanel() {
 		if (jobTemplatePanel == null) {
 			jobTemplatePanel = new JobTemplatePanel();
 			jobTemplatePanel.setName(TEMPLATE_PANEL);
 		}
 		return jobTemplatePanel;
 	}
 	/**
 	 * @return
 	 */
 	protected JobStatusPanel getJobStatusPanel() {
 		if (jobStatusPanel == null) {
 			jobStatusPanel = new JobStatusPanel();
 			jobStatusPanel.setName(STATUS_PANEL);
 		}
 		return jobStatusPanel;
 	}
 	
 	protected TemplateErrorPanel getTemplateErrorPanel() {
 		if ( templateErrorPanel == null ) {
 			templateErrorPanel = new TemplateErrorPanel();
 			templateErrorPanel.setName(TEMPLATE_ERROR_PANEL);
 		}
 		return templateErrorPanel;
 	}
 
 	public void submissionExceptionOccured(JsdlTemplateEvent event,
 			JobSubmissionException exception) {
 		
 //		showPanel(STATUS_PANEL);
 		
 	}
 
 	public void templateStatusChanged(JsdlTemplateEvent event) {
 		
 		if ( panelInitializationFinished ) {
 		
 		if ( template.getStatus() >= JsdlTemplate.STATUS_SUBMISSION_STARTED ) {
 			
 			showPanel(STATUS_PANEL);
 		} else { 
 			showPanel(TEMPLATE_PANEL);
 		}
 		}
 	}
	
 	/**
 	 * @return
 	 */
 	protected WaitingPanel getWaitingPanel() {
 		if (waitingPanel == null) {
 			waitingPanel = new WaitingPanel();
 			waitingPanel.setName(WAITING_PANEL);
 		}
 		return waitingPanel;
 	}
 
 }

 package org.vpac.grisu.frontend.view.swing;
 
 import java.awt.EventQueue;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.TemplateManager;
 import org.vpac.grisu.control.exceptions.NoSuchTemplateException;
 import org.vpac.grisu.frontend.view.swing.jobcreation.JobCreationPanel;
 import org.vpac.grisu.frontend.view.swing.jobcreation.TemplateJobCreationPanel;
 import org.vpac.grisu.model.GrisuRegistryManager;
 
 public class GrisuTemplateApp extends GrisuApplicationWindow implements
 		PropertyChangeListener {
 
 	static final Logger myLogger = Logger.getLogger(GrisuTemplateApp.class
 			.getName());
 
 	public static void main(String[] args) {
 
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 
 					GrisuApplicationWindow appWindow = new GrisuTemplateApp();
 					appWindow.setVisible(true);
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 	}
 
 	private final GrisuMenu menu = new GrisuMenu();
 	private TemplateManager tm;
 
 	public GrisuTemplateApp() {
 		super();
 		getFrame().setJMenuBar(menu);
 	}
 
 	private JobCreationPanel createFixedPanel(String panelClassName) {
 
 		try {
 
 			Class panelClass = null;
 
 			if (panelClassName.contains(".")) {
 				panelClass = Class.forName(panelClassName);
 			} else {
 				panelClass = Class
 						.forName("org.vpac.grisu.frontend.view.swing.jobcreation.createJobPanels."
 								+ panelClassName);
 			}
 
 			JobCreationPanel panel = (JobCreationPanel) panelClass
 					.newInstance();
 
 			return panel;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public boolean displayAppSpecificMonitoringItems() {
 		return true;
 	}
 
 	@Override
 	public boolean displayBatchJobsCreationPane() {
 		return true;
 	}
 
 	@Override
 	public boolean displaySingleJobsCreationPane() {
 		return true;
 	}
 
 	@Override
 	public JobCreationPanel[] getJobCreationPanels() {
 
 		if (getServiceInterface() == null) {
 			return new JobCreationPanel[] {};
 		}
 
 		List<JobCreationPanel> panels = new LinkedList<JobCreationPanel>();
 
 		String fixedPanels = System.getProperty("grisu.createJobPanels");
 		if (StringUtils.isNotBlank(fixedPanels)) {
 
 			for (String panel : fixedPanels.split(",")) {
 
 				JobCreationPanel creationPanel = createFixedPanel(panel);
 				if (creationPanel != null) {
 					panels.add(creationPanel);
 				}
 
 			}
 
 		}
 
 		SortedSet<String> allTemplates = null;
 		String fixedTemplates = System.getProperty("grisu.defaultApplications");
		if (StringUtils.isNotBlank(fixedTemplates)) {
 			myLogger.debug("Found defaultApplications: " + fixedTemplates);
 			String[] temp = fixedTemplates.split(",");
 			allTemplates = new TreeSet<String>(Arrays.asList(temp));
 		} else {
 			myLogger.debug("Didn't find defaultApplications,");
 			allTemplates = tm.getAllTemplateNames();
 		}
 
 		for (String name : allTemplates) {
 			try {
 				JobCreationPanel panel = new TemplateJobCreationPanel(name, tm
 						.getTemplate(name));
 				if (panel == null) {
 					myLogger.warn("Can't find template " + name);
 					continue;
 				}
 				panel.setServiceInterface(getServiceInterface());
 				panels.add(panel);
 			} catch (NoSuchTemplateException e) {
 				myLogger.warn("Can't find template " + name);
 				continue;
 			}
 		}
 
 		return panels.toArray(new JobCreationPanel[] {});
 	}
 
 	@Override
 	public String getName() {
 		return "Default Grisu client";
 	}
 
 	@Override
 	public void initOptionalStuff(ServiceInterface si) {
 
 		menu.setServiceInterface(si);
 		tm = GrisuRegistryManager.getDefault(si).getTemplateManager();
 		tm.addTemplateManagerListener(this);
 	}
 
 	public void propertyChange(PropertyChangeEvent evt) {
 
 		if (getServiceInterface() == null) {
 			myLogger.info("No serviceInterface. Not updateing template list.");
 			return;
 		}
 
 		refreshJobCreationPanels();
 
 	}
 
 }

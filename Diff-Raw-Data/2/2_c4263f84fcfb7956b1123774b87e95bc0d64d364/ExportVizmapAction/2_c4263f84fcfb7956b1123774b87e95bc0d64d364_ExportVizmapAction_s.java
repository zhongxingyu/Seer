 /*
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package cytoscape.actions;
 
 import cytoscape.Cytoscape;
 
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 
 import cytoscape.task.ui.JTaskConfig;
 
 import cytoscape.task.util.TaskManager;
 
 import cytoscape.util.CyFileFilter;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.util.FileUtil;
 
 import java.awt.event.ActionEvent;
 
 
 /**
  * Export visual styles as a vizmap.props file<br>
  *
  * @version 0.8
  * @since 2.3
  * @author kono
  *
  */
 public class ExportVizmapAction extends CytoscapeAction {
 	/**
 	 * Creates a new ExportVizmapAction object.
 	 */
 	public ExportVizmapAction() {
 		super("Vizmap Property File");
 		setPreferredMenu("File.Export");
 	}
 
 	/**
 	 * Get file name and execute the saving task<br>
 	 */
 	public void actionPerformed(ActionEvent e) {
 		String name;
 
 		try {
			name = FileUtil.getFile("Export Vizmaper as property file", FileUtil.SAVE,
 			                        new CyFileFilter[] {  }).toString();
 		} catch (Exception exp) {
 			// this is because the selection was canceled
 			return;
 		}
 
 		if (!name.endsWith(".props"))
 			name = name + ".props";
 
 		// Create Task
 		ExportVizmapTask task = new ExportVizmapTask(name);
 
 		// Configure JTask Dialog Pop-Up Box
 		JTaskConfig jTaskConfig = new JTaskConfig();
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		jTaskConfig.displayCloseButton(true);
 		jTaskConfig.displayStatus(true);
 		jTaskConfig.displayCancelButton(false);
 		jTaskConfig.setAutoDispose(false);
 
 		// Execute Task in New Thread; pop open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
 	}
 }
 
 
 /**
  * Task to Save Graph Data to GML Format.
  */
 class ExportVizmapTask implements Task {
 	private String fileName;
 	private TaskMonitor taskMonitor;
 
 	/**
 	 * Constructor.
 	 */
 	ExportVizmapTask(String fileName) {
 		this.fileName = fileName;
 	}
 
 	/**
 	 * Executes Task
 	 */
 	public void run() {
 		taskMonitor.setStatus("Saving Visual Styles...");
 		taskMonitor.setPercentCompleted(-1);
 
 		Cytoscape.firePropertyChange(Cytoscape.SAVE_VIZMAP_PROPS, null, fileName);
 
 		taskMonitor.setPercentCompleted(100);
 		taskMonitor.setStatus("Vizmaps successfully saved to:  " + fileName);
 	}
 
 	/**
 	 * Halts the Task: Not Currently Implemented.
 	 */
 	public void halt() {
 		// Task can not currently be halted.
 	}
 
 	/**
 	 * Sets the Task Monitor.
 	 *
 	 * @param taskMonitor
 	 *            TaskMonitor Object.
 	 */
 	public void setTaskMonitor(TaskMonitor taskMonitor) throws IllegalThreadStateException {
 		this.taskMonitor = taskMonitor;
 	}
 
 	/**
 	 * Gets the Task Title.
 	 *
 	 * @return Task Title.
 	 */
 	public String getTitle() {
 		return new String("Saving Vizmap");
 	}
 }

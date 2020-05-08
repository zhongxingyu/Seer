 package org.geworkbench.builtin.projects;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.APSerializable;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.engine.config.rules.GeawConfigObject;
 import org.geworkbench.engine.properties.PropertiesManager;
 import org.geworkbench.util.ProgressBar;
 import org.geworkbench.util.threading.SwingWorker;
 
 /**
  * This class handles workspace's saving and opening: showing progress bar
  * 
  * the main purpose to build this class is to take some tasks out of the super
  * large ProjectPanel
  * 
  * @author zji
  * @version $Id$
  * 
  */
 public class WorkspaceHandler {
 	static Log log = LogFactory.getLog(WorkspaceHandler.class);
 
 	private ProjectPanel enclosingProjectPanel = null;
 
 	WorkspaceHandler(ProjectPanel enclosingProjectPanel) {
 		this.enclosingProjectPanel = enclosingProjectPanel;
 	}
 
 	ProgressBar pb = null;
 	
 	private String wsFilePath = "";
 
 	/**
 	 * 
 	 * @param wsp_dir
 	 * @param terminating
 	 */
 	public void save(String wsp_dir, boolean terminating) {
 		PropertiesManager properties = PropertiesManager.getInstance();
 		String workspaceDir = ".";
 		try {
 			workspaceDir = properties.getProperty(ProjectPanel.class,
 					wsp_dir, workspaceDir);
 		} catch (IOException exception) {
 			exception.printStackTrace(); // To change body of catch statement
 			// use File | Settings | File
 			// Templates.
 		}
 
 		JFileChooser fc = new JFileChooser(workspaceDir);
 		String wsFilename = null;
 		WorkspaceFileFilter filter = new WorkspaceFileFilter();
 		fc.setFileFilter(filter);
 		fc.setDialogTitle("Save Current Workspace");
 		String extension = filter.getExtension();
 		int choice = fc.showSaveDialog(enclosingProjectPanel.jProjectPanel);
 		if (choice == JFileChooser.APPROVE_OPTION) {
 			File selectedFile = fc.getSelectedFile();
 			wsFilename = selectedFile.getAbsolutePath();
 
 			if (!selectedFile.getName().endsWith(".wsp")) {
 				selectedFile = new File(selectedFile.getAbsolutePath() + ".wsp");
 			}
 			
 			if (selectedFile.exists()) {
 				if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(
 						null,
 						"Are you sure you want to overwrite this workspace?",
 						"Overwrite?", JOptionPane.YES_NO_OPTION)) {
 					JOptionPane.showMessageDialog(null, "Save cancelled.");
 					return;
 				}
 			}
 
 			// This will happen for the case x:\ where x: is not a valid directory
 			String parent = fc.getSelectedFile().getParent();
 			if (parent == null){
 				JOptionPane.showMessageDialog(null,
 						"Could not create workspace file. \nSave cancelled.", 
 						"Error", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			// Store directory that we opened this from
 			try {
 				properties.setProperty(ProjectPanel.class, wsp_dir, parent);
 			} catch (IOException e1) {
 				JOptionPane.showMessageDialog(null,
 						"Could not create workspace file. \nSave cancelled.", 
 						"Error", JOptionPane.ERROR_MESSAGE);
 				log.error("Error: " + e1);
 			}
 
 			if (!wsFilename.endsWith(extension)) {
 				wsFilename += extension;
 			}
 
 		    PanelFocusedListener wfl = new PanelFocusedListener();
 			SaveTask task = new SaveTask(wsFilename, terminating, wfl);
 			task.execute();
 			pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
 			pb.setTitle("Workspace is being saved.");
			pb.setModal(true);
 			pb.start();
 			
 			wsFilePath = wsFilename;
 
 		}
 	}
 
 	/**
 	 * Prompt the user to open a saved workspace.
 	 * @param wsp_dir
 	 */
 	public void open(String wsp_dir) {
 		PropertiesManager properties = PropertiesManager.getInstance();
 		String workspaceDir = ".";
 		try {
 			workspaceDir = properties.getProperty(ProjectPanel.class,
 					wsp_dir, workspaceDir);
 		} catch (IOException exception) {
 			exception.printStackTrace(); // To change body of catch statement
 			// use File | Settings | File
 			// Templates.
 		}
 
 		// Prompt user for designating the file containing the workspace to be
 		// opened.
 		JFileChooser fc = new JFileChooser(workspaceDir);
 		String wsFilename = null;
 		WorkspaceFileFilter filter = new WorkspaceFileFilter();
 		fc.setFileFilter(filter);
 		fc.setDialogTitle("Open Workspace");
 		String extension = filter.getExtension();
 		int choice = fc.showOpenDialog(enclosingProjectPanel.jProjectPanel);
 		if (choice == JFileChooser.APPROVE_OPTION) {
 			wsFilename = fc.getSelectedFile().getAbsolutePath();
 			if (!wsFilename.endsWith(extension)) {
 				wsFilename += extension;
 			}
			//current workspace is not empty
			if (enclosingProjectPanel.projectTree.getRowCount() > 1)
			{
				//inform user this will overwrite the current workspace
				//give user a chance to continue or cancel workspace loading
				Object[] options = {"Continue loading & overwrite current workspace", "Save current workspace before continuing", "Cancel"};
				int n = JOptionPane
						.showOptionDialog(
								null,
								"Opening this workspace will overwrite your current workspace and your current data and results will be lost. "+
								"Are you sure you want to continue?",
								"Confirm Loading", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n == JOptionPane.CANCEL_OPTION || n == JOptionPane.CLOSED_OPTION)
					return;
				if (n == JOptionPane.NO_OPTION) {
					this.save(wsp_dir, false);
					if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(
							null, "Are you sure you want to load workspace "
									+ fc.getSelectedFile().getName() + "?",
							"Confirm Loading", JOptionPane.YES_NO_OPTION))
						return;
					
				}
			}
 
 			// Store directory that we opened this from
 			try {
 				properties.setProperty(ProjectPanel.class, wsp_dir, fc
 						.getSelectedFile().getParent());
 			} catch (IOException e) {
 				log.info("current directory was not successfuly stored.");
 				e.printStackTrace();
 			}
 
 			OpenTask openTask = new OpenTask(wsFilename);
 			openTask.execute();
 			pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
 			pb.setTitle("Workspace is being loaded.");
 			pb.setAlwaysOnTop(true);
 			pb.start();
 			
 			wsFilePath = wsFilename;
 
 		}
 
 	}
 		
 	/**
 	 * 
 	 * @return
 	 */
 	public String getWorkspacePath(){
 		return this.wsFilePath;
 	}
 	
 	/**
 	 * 
 	 * @author zji
 	 *
 	 */
 	private class SaveTask extends SwingWorker<Void, Void> {
 		private String filename;
 		private boolean terminating;
 		private PanelFocusedListener wfl = null;
 
 		SaveTask(String filename, boolean terminating, PanelFocusedListener wfl ) {
 			super();
 			this.filename = filename;
 			this.terminating = terminating;
 			this.wfl = wfl;
 		}
 
 		@Override
 		protected void done() {
 			GeawConfigObject.getGuiWindow().removeWindowFocusListener(wfl); 			 
 			pb.dispose();
 			
 			try{
 				// TODO Put check loop here with status bar
 				this.get();
 	
 				JOptionPane.getRootFrame().setAlwaysOnTop(true);
 				JOptionPane.showMessageDialog(null, "Workspace saved.");
 	
 				if(terminating) {
 					GeawConfigObject.getGuiWindow().dispose();
 					System.exit(0);
 				}
 			}catch(Exception e){
 				JOptionPane.showMessageDialog(null,
 						"Could not create workspace file. \nSave cancelled.", 
 						"Error", JOptionPane.ERROR_MESSAGE);
 				log.error("Error: " + e);
 			}
 		}
 
 		@Override
 		protected Void doInBackground() throws Exception {
 			GeawConfigObject.getGuiWindow().addWindowFocusListener(wfl);
 			enclosingProjectPanel.serialize(filename);
 			return null;
 		}
 
 	}
 	
 	/**
 	 * 
 	 * @author zji
 	 *
 	 */
 	private class OpenTask extends SwingWorker<Void, Void> {
 		private String filename;
 
 		OpenTask(String filename) {
 			super();
 			this.filename = filename;
 		}
 
 		@Override
 		protected void done() {
 			try {
 				get();
 			} catch (ExecutionException e) {
 				// printStackTrace what is from doInBackground
 				e.getCause().printStackTrace();
 				JOptionPane.showMessageDialog(null,
 						"Check that the file contains a valid workspace.",
 						"Open Workspace Error", JOptionPane.ERROR_MESSAGE);
 			} catch (InterruptedException e) {
 				// This should not happen. get() is called only to handle the
 				// exception from doInBackGound
 				e.printStackTrace();
 			}
 			pb.dispose();
 		}
 
 		@Override
 		protected Void doInBackground() throws Exception {
 			FileInputStream in = new FileInputStream(filename);
 			ObjectInputStream s = new ObjectInputStream(in);
 			SaveTree saveTree = (SaveTree) s.readObject();
 			enclosingProjectPanel.clear();
 			enclosingProjectPanel.populateFromSaveTree(saveTree);
 			APSerializable aps = (APSerializable) s.readObject();
 			AnnotationParser.setFromSerializable(aps);
 			// ProjectTreeNode tempNode = (ProjectTreeNode) s.readObject();
 			// // Clean up local structures and notify interested components
 			// to clean
 			// // themselves up.
 			// root = tempNode;
 			// projectRenderer.clearNodeSelections();
 			// projectTreeModel = new DefaultTreeModel(root);
 			// projectTree.setModel(projectTreeModel);
 
 			return null;
 		}
 
 	}
 	
 	/**
 	 * 
 	 * @author zji
 	 *
 	 */
 	private class PanelFocusedListener implements
 			java.awt.event.WindowFocusListener {
 
 		public void windowGainedFocus(java.awt.event.WindowEvent e) {
 
 			if (pb.isShowing()) {
 				pb.setFocusable(true);
 				pb.requestFocus();
 
 			}
 
 		}
 
 		public void windowLostFocus(java.awt.event.WindowEvent e) {
 			//do nothing; 
 
 		}
 
 	}
 
 	private static class WorkspaceFileFilter extends FileFilter {
 		private static final String fileExt = ".wsp";
 
 		public String getExtension() {
 			return fileExt;
 		}
 
 		public String getDescription() {
 			return "Workspace Files";
 		}
 
 		public boolean accept(File f) {
 			boolean returnVal = false;
 			if (f.isDirectory() || f.getName().endsWith(fileExt)) {
 				return true;
 			}
 
 			return returnVal;
 		}
 
 	}
 
 }

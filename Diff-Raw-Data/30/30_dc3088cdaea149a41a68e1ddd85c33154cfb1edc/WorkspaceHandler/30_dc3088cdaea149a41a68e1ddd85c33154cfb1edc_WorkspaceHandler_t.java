 package org.geworkbench.builtin.projects;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.APSerializable;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.engine.config.UILauncher;
 import org.geworkbench.engine.config.rules.GeawConfigObject;
 import org.geworkbench.engine.properties.PropertiesManager;
 import org.geworkbench.util.ProgressDialog;
 import org.geworkbench.util.ProgressItem;
 import org.geworkbench.util.ProgressTask;
 
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
 
 	private final ProjectPanel enclosingProjectPanel = ProjectPanel.getInstance();
 
 	private ProgressDialog pdmodal = ProgressDialog.create(ProgressDialog.MODAL_TYPE);
 	private ProgressDialog pdnonmodal = ProgressDialog.create(ProgressDialog.NONMODAL_TYPE);
 	
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
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.setFileFilter(filter);
 		fc.setDialogTitle("Save Current Workspace");
 		String extension = filter.getExtension();
 		int choice = fc.showSaveDialog(enclosingProjectPanel.getComponent());
 		if (choice == JFileChooser.APPROVE_OPTION) {
 			File selectedFile = fc.getSelectedFile();
 			wsFilename = selectedFile.getAbsolutePath();
 
 			if (!selectedFile.getName().endsWith(".wsp")) {
 				selectedFile = new File(selectedFile.getAbsolutePath() + ".wsp");
 			}
 			
 			if (selectedFile.exists()) {
 				int n = JOptionPane.showConfirmDialog(
 						null,
 						"Are you sure you want to overwrite this workspace?",
 						"Overwrite?", JOptionPane.YES_NO_OPTION);
 				if (n == JOptionPane.NO_OPTION || n == JOptionPane.CLOSED_OPTION) {
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
 
 			SaveTask task = new SaveTask(ProgressItem.INDETERMINATE_TYPE, "Workspace is being saved.", wsFilename, terminating);
 			pdmodal.executeTask(task);
 
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
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.setFileFilter(filter);
 		fc.setDialogTitle("Open Workspace");
 		String extension = filter.getExtension();
 		int choice = fc.showOpenDialog(enclosingProjectPanel.getComponent());
 		if (choice == JFileChooser.APPROVE_OPTION) {
 			wsFilename = fc.getSelectedFile().getAbsolutePath();
 			if (!wsFilename.endsWith(extension)) {
 				wsFilename += extension;
 			}
 			if (!confirmLoading(wsp_dir, fc.getSelectedFile().getName()))
 				return;
 
 			// Store directory that we opened this from
 			try {
 				properties.setProperty(ProjectPanel.class, wsp_dir, fc
 						.getSelectedFile().getParent());
 			} catch (IOException e) {
 				log.info("current directory was not successfuly stored.");
 				e.printStackTrace();
 			}
 
 			pdnonmodal.cancelAllTasks();
 			OpenTask openTask = new OpenTask(ProgressItem.INDETERMINATE_TYPE, "Workspace is being loaded.", wsFilename);
 			pdnonmodal.executeTask(openTask);
 
 			wsFilePath = wsFilename;
 
 		}
 
 	}
 		
 	protected boolean confirmLoading(String wspDir, String wspFname) {
 		// current workspace is not empty
 		if (enclosingProjectPanel.countProjectTree() > 1) {
 			// inform user this will overwrite the current workspace
 			// give user a chance to continue or cancel workspace loading
 			String info = "Opening this";
 			String title = "Loading";
 			if (wspFname == null)
 			{
 				info = "Creating new";
 				title = "Creation";
 			}
 
 			Object[] options = {"Proceed", "Save", "Cancel" };
 
 			int n = JOptionPane
 					.showOptionDialog(
 							null,
 							info + " workspace will overwrite your current workspace.\nYour current data and results will be lost.\n"
 									+ "Do you want to save your current workspace or \ndo you want to overwrite your current workspace?",
 							"Confirm Workspace "+title,
 							JOptionPane.YES_NO_CANCEL_OPTION,
 							JOptionPane.QUESTION_MESSAGE, null, options,
 							options[1]);
 			if (n == JOptionPane.CANCEL_OPTION
 					|| n == JOptionPane.CLOSED_OPTION)
 				return false;
 			if (n == JOptionPane.NO_OPTION) {
 				this.save(wspDir, false);
 				if (wspFname == null)
 					return true;
 				int a = JOptionPane.showConfirmDialog(
 						null, "Are you sure you want to load workspace "
 						+ wspFname + "?", "Confirm Workspace Loading",
 						JOptionPane.YES_NO_OPTION);
 				if (a == JOptionPane.NO_OPTION ||a == JOptionPane.CLOSED_OPTION)
 					return false;
 			}
 		}
 		return true;
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
 	protected class SaveTask extends ProgressTask<Void, Void> {
 		protected String filename;
 		protected boolean terminating;
 
 		SaveTask(int pbtype, String message, String filename, boolean terminating ) {
 			super(pbtype, message);
 			this.filename = filename;
 			this.terminating = terminating;
 		}
 
 		@Override
 		protected void done() {
 			pdmodal.removeTask(this);
 			if (isCancelled()) return;
 			
 			try{
 				this.get();
 	
 				//JOptionPane.getRootFrame().setAlwaysOnTop(true);
 				//JOptionPane.showMessageDialog(null, "Workspace saved.");
 	
 				if(terminating) {
 					GeawConfigObject.getGuiWindow().dispose();
					UILauncher.printTimeStamp("geWorkbench exited.");
 					System.exit(0);
 				}
 			} catch (ExecutionException e){
 				JOptionPane.showMessageDialog(null,
 						"Could not create workspace file for "+e.getMessage()+". \nSave cancelled.", 
 						"Error", JOptionPane.ERROR_MESSAGE);
 				log.error("Error: " + e.getCause());
 				File file = new File(filename);
 				file.delete(); 
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		protected Void doInBackground() throws FileNotFoundException, IOException {
 			ObjectOutput s = null;
 			FileOutputStream f = null;
 			try {
 				f = new FileOutputStream(filename);
 				s = new ObjectOutputStream(f);
 				SaveTree saveTree = new SaveTree(enclosingProjectPanel,
 						enclosingProjectPanel.getDataSet(), RWspHandler.wspId, RWspHandler.dirty, 
 						RWspHandler.checkoutstr, RWspHandler.lastchange);
 				s.writeObject(saveTree);
 				APSerializable aps = AnnotationParser.getSerializable();
 				s.writeObject(aps);
 				s.flush();
 			} finally {
 				try {
 					f.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 			return null;
 		}
 
 	}
 	
 	/**
 	 * 
 	 * @author zji
 	 *
 	 */
 	protected class OpenTask extends ProgressTask<Void, Void> {
 		protected String filename;
 
 		OpenTask(int pbtype, String message, String filename) {
 			super(pbtype, message);
 			this.filename = filename;
 		}
 
 		@Override
 		protected void done() {
 			if (isCancelled()){
 				pdnonmodal.removeTask(this);
 				return;
 			}
 			enclosingProjectPanel.clear();
 
 			try {
 				get();
 				enclosingProjectPanel.populateFromSaveTree(saveTree);
 				RWspHandler.wspId = saveTree.getWspId();
 				RWspHandler.dirty = saveTree.getDirty();
 				RWspHandler.checkoutstr = saveTree.getCheckout();
 				RWspHandler.lastchange = saveTree.getLastchange();
 			} catch (ExecutionException e) {
 				// printStackTrace what is from doInBackground
 				e.getCause().printStackTrace();
 				RWspHandler.wspId = 0;
 				JOptionPane.showMessageDialog(null,
 						"Check that the file contains a valid workspace.",
 						"Open Workspace Error", JOptionPane.ERROR_MESSAGE);
 			} catch (InterruptedException e) {
 				// This should not happen. get() is called only to handle the
 				// exception from doInBackGound
 				e.printStackTrace();
 				RWspHandler.wspId = 0;
 			} catch (Exception e) { // null pinter, no serializable
 				e.printStackTrace();
 				RWspHandler.wspId = 0;
 				JOptionPane.showMessageDialog(null,
 						"Check that the file contains a valid workspace.\n"+e,
 						"Open Workspace Error", JOptionPane.ERROR_MESSAGE);
 			} finally {
 				pdnonmodal.removeTask(this);
 			}
 		}
 
 		private SaveTree saveTree = null;
 		
 		@Override
 		protected Void doInBackground() throws Exception {
 			FileInputStream in = null;
 			try {
 				in = new FileInputStream(filename);
 				ObjectInputStream s = new ObjectInputStream(in);
 				saveTree = (SaveTree) s.readObject();
 				APSerializable aps = (APSerializable) s.readObject();
 				AnnotationParser.setFromSerializable(aps);
 			} catch (Exception e) {
 				// no-op here. user will see error from done();
 				e.printStackTrace();
 			} finally {
 				if(in!=null)
 					in.close();
 			}
 
 			return null;
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

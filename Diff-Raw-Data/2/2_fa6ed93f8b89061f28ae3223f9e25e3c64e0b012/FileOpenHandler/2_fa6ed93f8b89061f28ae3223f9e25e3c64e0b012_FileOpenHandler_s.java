 package org.geworkbench.builtin.projects;
 
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.InterruptedIOException;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
 import org.geworkbench.bison.util.colorcontext.ColorContext;
 import org.geworkbench.engine.config.rules.GeawConfigObject;
 import org.geworkbench.parsers.AdjacencyMatrixFileFormat;
 import org.geworkbench.parsers.DataSetFileFormat;
 import org.geworkbench.parsers.FileFormat;
 import org.geworkbench.parsers.InputFileFormatException;
 import org.geworkbench.parsers.PatternFileFormat;
 import org.geworkbench.util.AffyAnnotationUtil;
 
 /**
  * This class is refactored out of ProjectPanel to handle the file open action,
  * especially tackles the progress bar requirement for multiple files.
  *
  * @author zji
  * @version $Id$
  *
  */
 public class FileOpenHandler {
 	static Log log = LogFactory.getLog(FileOpenHandler.class);
 
 	private final File[] dataSetFiles;
 	private final FileFormat inputFormat;
 
 	private final ProjectPanel projectPanel;
 	private final JProgressBar projectPanelProgressBar;
 	
 	private static final String OUT_OF_MEMORY_MESSAGE = "In order to prevent data corruption,\n"
 			+ "it is strongly suggested that you\n"
 			+ "restart geWorkbench now.\n"
 			+ "To increase memory available to\n"
 			+ "geWorkbench, please refer to\n"
 			+ "geWorkbench documentation.\n\n"
 			+ "Exit geWorkbench?";
 	private static final String OUT_OF_MEMORY_MESSAGE_TITLE = "Java total heap memory exception";
 
 	FileOpenHandler(final File[] dataSetFiles, final FileFormat inputFormat)
 			throws InputFileFormatException {
 		this.dataSetFiles = dataSetFiles;
 		this.inputFormat = inputFormat;
 
 		projectPanel = ProjectPanel.getInstance();
 		projectPanelProgressBar = projectPanel.getProgressBar();
 		projectPanelProgressBar.setStringPainted(true);
 		projectPanelProgressBar.setString("Loading");
 		projectPanelProgressBar.setIndeterminate(true);
 		ProjectPanel.getInstance().getComponent().setCursor(Cursor
 				.getPredefinedCursor(Cursor.WAIT_CURSOR));
 	}
 
 	/**
 	 *
 	 */
 	public void openFiles() {
 		OpenMultipleFileTask task = new OpenMultipleFileTask();
 
 		ProgressBarDialog pb = new ProgressBarDialog(GeawConfigObject.getGuiWindow(),
 				"Files are being opened.", task);
 		pb.setMessageAndNote(String.format("Completed %d out of %d files.", 0,
 				dataSetFiles.length), String.format(
 				"Currently being processed is %s.", dataSetFiles[0].getName()));
 		
 		task.progressBarDialog = pb;
 		task.execute();
 
 		task.addPropertyChangeListener(pb);
 	}
 
 	private class ProgressBarDialog extends JDialog implements ActionListener,
 			PropertyChangeListener {
 		private static final long serialVersionUID = -3259066552401380723L;
 
 		private JLabel message = null;
 		private JLabel note = null;
 		private JButton cancelButton = null;
 
 		private final OpenMultipleFileTask task;
 		private void setMessageAndNote(String message, String note) {
 			this.message.setText(message);
 			this.note.setText(note);
 			this.message.invalidate();
 		}
 
 		ProgressBarDialog(JFrame ownerFrame, String title, final OpenMultipleFileTask task) {
 			// it is important to make it non-modal - for the same reason
 			// customizing dialog is necessary
 			// because this class FileOpenHandler is used within a file chooser
 			// event handler, so it would leave the file open dialog open
 			// otherwise
 			super(ownerFrame, title, false);
 			
 			this.task = task;
 
 			this.setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
 			JPanel leftPanel = new JPanel();
 			add(leftPanel);
 			leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);
 			JPanel rightPanel = new JPanel();
 			add(rightPanel);
 			rightPanel.setAlignmentY(Component.TOP_ALIGNMENT);
 			rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
 
 			JLabel icon = new JLabel(UIManager
 					.getIcon("OptionPane.informationIcon"));
 			icon.setAlignmentY(Component.TOP_ALIGNMENT);
 			leftPanel.add(icon);
 
 			// add two lines of messages
 			rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
 			message = new JLabel("");
 			message.setAlignmentX(Component.LEFT_ALIGNMENT);
 			rightPanel.add(message);
 			rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
 			note = new JLabel("");
 			note.setAlignmentX(Component.LEFT_ALIGNMENT);
 			rightPanel.add(note);
 
 			JProgressBar progress = new JProgressBar(0, 100);
 			progress.setIndeterminate(true);
 			progress.setMinimumSize(new Dimension(300, 22));
 			progress.setPreferredSize(new Dimension(300, 50));
 
 			progress.setAlignmentX(Component.LEFT_ALIGNMENT);
 			rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
 			rightPanel.add(progress);
 
 			cancelButton = new JButton("Cancel");
 			cancelButton.addActionListener(this);
 			cancelButton.setAlignmentX(Component.LEFT_ALIGNMENT);
 			rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
 			rightPanel.add(cancelButton);
 			rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
 
 			pack();
 			setLocationRelativeTo(ownerFrame);
 
 			// disable exit
 			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 			//setVisible(true);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			if (cancelButton == e.getSource()) {
 				task.cancel(true);
 
 				// task is really stopped when checking isCancel between reading
 				// files, but UI should show canceled
 				projectPanelProgressBar.setString("");
 				projectPanelProgressBar.setIndeterminate(false);
 				projectPanel.getComponent().setCursor(Cursor
 						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 
 				dispose();
 			}
 		}
 
 		/**
 		 * Invoked when task's progress property changes.
 		 */
 		public void propertyChange(PropertyChangeEvent evt) {
 			if ("progress" == evt.getPropertyName()) {
 				int progress = (Integer) evt.getNewValue();
 				String note = "";
 				if (progress >= 0 && progress < dataSetFiles.length)
 					note = String.format("Currently being processed is %s.",
 							dataSetFiles[progress].getName());
 				setMessageAndNote(String.format(
 						"Completed %d out %d files.", progress,
 						dataSetFiles.length), note);
 			}
 		}
 	} // end of class ProgressBarDialog
 	
 	private void clearProjectPanelProgressBar() {
 		projectPanelProgressBar.setString("");
 		projectPanelProgressBar.setIndeterminate(false);
 		projectPanel.getComponent().setCursor(Cursor
 				.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 	}
 
 	private class OpenMultipleFileTask extends SwingWorker<Void, Void> {
 		ProgressBarDialog progressBarDialog;
 
 		/*
 		 * (non-Javadoc)
 		 * @see org.geworkbench.util.threading.SwingWorker#done()
 		 */
 		@SuppressWarnings({ "rawtypes", "unchecked" })
 		@Override
 		protected void done() {
 			try {
 				get();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				return;
 			} catch (ExecutionException e) {
 				Throwable cause = e.getCause();
 				if(cause instanceof InputFileFormatException) {
 					// Let the user know that there was a problem
 					// parsing the file.
 					JOptionPane
 							.showMessageDialog(
 									null,
 									"The input file does not comply with the designated format: "+((InputFileFormatException)cause).getMessage(),
 									"Parsing Error",
 									JOptionPane.ERROR_MESSAGE);
 					clearProjectPanelProgressBar();
 				} else if (cause instanceof InterruptedIOException) {
 					clearProjectPanelProgressBar();
 					if ( !cause.getMessage().equals("progress"))
 			    	   cause.printStackTrace();
 				} else {
 					e.printStackTrace();
 				}
 				return;
 			}
 
 			if (dataSets.length>1 && dataSets[0] instanceof DSMicroarraySet) {
 				DSMicroarraySet[] maSets = new DSMicroarraySet[dataSets.length];
 
 				for (int i = 0; i < dataSets.length; i++) {
 					maSets[i] = (DSMicroarraySet) dataSets[i];
 				}
 				DSMicroarraySet mergedSet = doMergeSets(maSets);
 				if(mergedSet!=null) {
 					projectPanel.addDataSetNode(mergedSet);
 				}
 			} else {
 				for (int i = 0; i < dataSets.length; i++) {
 					DSDataSet set = dataSets[i];
 	
 					if (set == null) {
 						clearProjectPanelProgressBar();
 						log.info("null dataset encountered");
 						progressBarDialog.dispose();
 						projectPanelProgressBar.setString("");
 						projectPanelProgressBar.setIndeterminate(false);
 						projectPanel.getComponent().setCursor(Cursor
 								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 						return;
 					}
 	
 					// Do initial color context update if it is a microarray
 					if (set instanceof DSMicroarraySet) {
 						ProjectPanel
 								.addColorContext((DSMicroarraySet) set);
 					}
 	
 					if (set instanceof AdjacencyMatrixDataSet) {
 						// adjacency matrix as added as a sub node
 						AdjacencyMatrixDataSet adjMatrixDS = (AdjacencyMatrixDataSet) set;
 						projectPanel.addDataSetSubNode(adjMatrixDS);
 					} else {
 						if(set instanceof PatternResult) {
 							// Pattern Result added as a sub node
 							PatternResult patternResult = (PatternResult) set;
 							projectPanel.addDataSetSubNode(patternResult);
 						}else {
 							projectPanel.addDataSetNode(set);
 						}
 					}
 				}
 			}
 
 			progressBarDialog.dispose();
 			projectPanelProgressBar.setString("");
 			projectPanelProgressBar.setIndeterminate(false);
 			projectPanel.getComponent().setCursor(Cursor
 					.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 		}
 
 		@SuppressWarnings("rawtypes")
 		private DSDataSet[] dataSets = null;
 		/*
 		 * (non-Javadoc)
 		 * @see org.geworkbench.util.threading.SwingWorker#doInBackground()
 		 */
 		@SuppressWarnings({ "unchecked" })
 		@Override
 		protected Void doInBackground() throws Exception {
 			int n = dataSetFiles.length;
 			dataSets = new DSDataSet[n];
 
 			DataSetFileFormat dataSetFileFormat = (DataSetFileFormat) inputFormat;
 			if (dataSetFiles.length == 1) {
 				try {
 					// adjacency matrix need access to project node
 					if (dataSetFileFormat instanceof AdjacencyMatrixFileFormat) {
 						ProjectTreeNode selectedNode = ProjectPanel.getInstance()
 								.getSelection().getSelectedNode();
 						// it has to be a project node
 						if (selectedNode instanceof ProjectNode) {
 							ProjectNode projectNode = (ProjectNode) selectedNode;
 							((AdjacencyMatrixFileFormat) dataSetFileFormat)
 									.setProjectNode(projectNode);
 						}
 					}
 
 					// pattern file format need access to project node
 					if (dataSetFileFormat instanceof PatternFileFormat) {
 			
 						ProjectTreeNode selectedNode = ProjectPanel.getInstance()
 								.getSelection().getSelectedNode();
 						// it has to be a project node
 						if (selectedNode instanceof ProjectNode) {
 							ProjectNode projectNode = (ProjectNode) selectedNode;
 							((PatternFileFormat) dataSetFileFormat)
 									.setProjectNode(projectNode);
 						}
 					}
 					
 					dataSets[0] = dataSetFileFormat.getDataFile(dataSetFiles[0]);
 				} catch (OutOfMemoryError er) {
 					log.warn("Loading a single file memory error: " + er);
 					int response = JOptionPane.showConfirmDialog(null,
 							OUT_OF_MEMORY_MESSAGE, OUT_OF_MEMORY_MESSAGE_TITLE,
 							JOptionPane.YES_NO_OPTION,
 							JOptionPane.ERROR_MESSAGE);
 					if (response == JOptionPane.YES_OPTION) {
 						System.exit(1);
 					}
 				 }
 			} else {
 				// multiple file selection is not supported for adjacency matrix
 				if (dataSetFileFormat instanceof AdjacencyMatrixFileFormat) {
 					JOptionPane.showMessageDialog(null, "Multiple file selection is not supported for network files.");
 					return null;
 				}
 
 				// invoking AnnotationParser.matchChipType with null dataset is
 				// different from the previous algorithm.
 				// also notice that this will block
 				String chipType = null; // ignored by other format
 				if(dataSetFileFormat.isMergeSupported())
 					chipType = AffyAnnotationUtil.matchAffyAnnotationFile(null);
 				progressBarDialog.setVisible(true);
 
 				for (int i = 0; i < dataSetFiles.length; i++) {
 					if (isCancelled()) {
 						return null;
 					}
 					File dataSetFile = dataSetFiles[i];
 
 					try {
 						if(chipType==null) {
 							dataSets[i] = dataSetFileFormat.getDataFile(
 									dataSetFile);
 						} else {
 							dataSets[i] = dataSetFileFormat.getDataFile(
 									dataSetFile, chipType);
 							AnnotationParser.setChipType(dataSets[i], chipType);
 							if (dataSets[i] instanceof CSMicroarraySet) {
 								((CSMicroarraySet) dataSets[i])
 										.setAnnotationFileName(AnnotationParser
 												.getLastAnnotationFileName());
 							}
 						}
 					} catch (OutOfMemoryError er) {
 						log.warn("Loading multiple files memory error: " + er);
 						int response = JOptionPane.showConfirmDialog(null,
 								OUT_OF_MEMORY_MESSAGE,
 								OUT_OF_MEMORY_MESSAGE_TITLE,
 								JOptionPane.YES_NO_OPTION,
 								JOptionPane.ERROR_MESSAGE);
 						if (response == JOptionPane.YES_OPTION) {
 							System.exit(1);
 						}
 					} catch (UnsupportedOperationException e) {
 						log.warn("This data type doesn't support chip type overrides, will have to ask user again.");
 						dataSets[i] = ((DataSetFileFormat) inputFormat)
 								.getDataFile(dataSetFile);
 					}
 
 					setProgress(i + 1);
 				}
 			}
 
 			return null;
 		}
 
 	}
 
 	/**
 	 * Merger an array of MSMicroarraySets and create a new dataset node.
 	 * 
 	 * This method may return null.
 	 *
 	 * @param sets
 	 */
 	public static DSMicroarraySet doMergeSets(DSMicroarraySet[] sets) {
 		if (!isSameMarkerSets(sets)) {
 			JOptionPane
 					.showMessageDialog(
 							null,
 							"Can't merge datasets.  Only datasets with the same markers can be merged.",
 							"Operation failed while merging",
 							JOptionPane.INFORMATION_MESSAGE);
 			return null;
 		}
 		if (microarraySetsIntersect(sets)) {
 			JOptionPane
 					.showMessageDialog(
 							null,
							"Can't merge datasets.  Only datasets without microarray intersection can be merged.",
 							"Operation failed while merging",
 							JOptionPane.INFORMATION_MESSAGE);
 			return null;
 		}
 		if (sets == null)
 			return null;
 		
 		DSMicroarraySet mergedSet = null;
 
 		String desc = "";
 		if(sets.length>1)desc = "Merged DataSet: ";
 		
 		for (int i = 0; i < sets.length; i++) {
 			DSMicroarraySet set = (DSMicroarraySet) sets[i];
 			if (mergedSet == null) {
 				try {
 					mergedSet = set.getClass().newInstance();
 					mergedSet.addObject(ColorContext.class,
 							set.getObject(ColorContext.class));
 					// mergedSet.setMarkerNo(set.size());
 					// mergedSet.setMicroarrayNo(set.size());
 
 					((DSMicroarraySet) mergedSet)
 							.setCompatibilityLabel(set.getCompatibilityLabel());
 					((DSMicroarraySet) mergedSet).getMarkers()
 							.addAll(set.getMarkers());
 					DSItemList<DSGeneMarker> markerList = set.getMarkers();
 					for (int j = 0; j < markerList.size(); j++) {
 						DSGeneMarker dsGeneMarker = markerList.get(j);
 						((DSMicroarraySet) mergedSet)
 								.getMarkers().add(dsGeneMarker.deepCopy());
 					}
 					for (int k = 0; k < set.size(); k++) {
 						mergedSet.add(set.get(k).deepCopy());
 					}
 					desc += set.getLabel() + " ";
 					// XQ fix bug 1539, add annotation information to the
 					// merged dataset.
 					String chipType = AnnotationParser.getChipType(set);
 					AnnotationParser.setChipType(mergedSet, chipType);
 				} catch (InstantiationException ie) {
 					ie.printStackTrace();
 				} catch (IllegalAccessException iae) {
 					iae.printStackTrace();
 				}
 			} else {
 				desc += set.getLabel() + " ";
 				try {
 					mergedSet.mergeMicroarraySet(set);
 				} catch (Exception ex) {
 					ex.printStackTrace();
 					JOptionPane.showMessageDialog(null,
 							"Only microarray sets created"
 									+ " from the same chip set can be merged",
 							"Merge Error", JOptionPane.ERROR_MESSAGE);
 					return null;
 				}
 			}
 		}
 
 		if (mergedSet != null) {
 			mergedSet.setLabel("Merged array set");
 			mergedSet.setLabel(desc);
 			mergedSet.setDescription(mergedSet.getDescription());
 			((CSMicroarraySet) mergedSet)
 					.setAnnotationFileName(((CSMicroarraySet) sets[0])
 							.getAnnotationFileName());
 		}
 		// Add color context
 		ProjectPanel.addColorContext(mergedSet);
 
 		return mergedSet;
 	}
 
 	/**
 	 * Check for markers in DSMicroarraySets, if markers are all the same,
 	 * return true. This method assume there's no duplicate markers within each
 	 * set.
 	 *
 	 * @param sets
 	 * @return
 	 */
 	private static boolean isSameMarkerSets(DSMicroarraySet[] sets) {
 		if (sets == null || sets.length <= 1)
 			return true;
 
 		HashSet<DSGeneMarker> set1 = new HashSet<DSGeneMarker>();
 		set1.addAll(sets[0].getMarkers());
 
 		HashSet<DSGeneMarker> set2 = new HashSet<DSGeneMarker>();
 		for (int i = 1; i < sets.length; i++) {
 			set2.clear();
 			set2.addAll(sets[i].getMarkers());
 			if (!set1.equals(set2))
 				return false;
 		}
 		return true; // all marker sets are identical
 	}
 
 	private static boolean microarraySetsIntersect(DSMicroarraySet[] sets) {
 		Set<String> set = new HashSet<String>();
 		for (DSMicroarraySet s : sets) {
 			for (DSMicroarray array : s) {
 				String label = array.getLabel();
 				if (set.contains(label))
 					return true;
 				else
 					set.add(label);
 			}
 		}
 		return false;
 	}
 }

 package org.geworkbench.parsers;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.filechooser.FileFilter;
 
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
 import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
 import org.geworkbench.bison.parsers.resources.Resource;
 import org.geworkbench.builtin.projects.DataSetNode;
 import org.geworkbench.builtin.projects.ProjectNode;
 import org.geworkbench.builtin.projects.ProjectTreeNode;
 import org.geworkbench.parsers.sequences.SequenceResource;
 import org.geworkbench.util.Util;
 
 public class PatternFileFormat extends DataSetFileFormat {
 	
 	private static final String[] patExtensions = { "pat"};
 	private String fileName;
 	private PatternResult patternResult = null;
 	private ProjectNode projectNode;
 	private boolean isCancel = false;
 	private DataSetNode selectedDataSetNode;
 	
 	PatternFilter patFilter = null;
 	SequenceResource resource = new SequenceResource();
 	
 	public PatternFileFormat() {
 		formatName = "Pattern File"; // Setup the display name for the
 		patFilter = new PatternFilter();
 
 	}
 	 
 	
 	 public boolean isMergeSupported() {return false;}
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.components.parsers.FileFormat#getResource(java.io.File)
 	 */
 	public Resource getResource(File file) {
 		try {
 			resource.setReader(new BufferedReader(new FileReader(file)));
 			resource.setInputFileName(file.getName());
 		} catch (IOException ioe) {
 			ioe.printStackTrace(System.err);
 		}
 		return resource;
 	}
 	
 	public void setProjectNode(ProjectNode projectNode) {
 		this.projectNode = projectNode;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.components.parsers.FileFormat#getFileExtensions()
 	 */
 	public String[] getFileExtensions() {
 		return patExtensions;
 	}
 	
 	@Override
 	public DSDataSet<? extends DSBioObject> getDataFile(final File file)
 			throws InputFileFormatException, InterruptedIOException {
 		DSDataSet<?> ds = getPatternResultSet(file);
 		return ds;
 	}
 	
 	@Override
 	public DSDataSet<? extends DSBioObject> getDataFile(File[] file)
 			throws InputFileFormatException {
 		
 		return null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private DSDataSet<?> getPatternResultSet(File file) 
 		throws InputFileFormatException, InterruptedIOException {{
 			// TODO Auto-generated method stub
 
 			// get list of data sets that a selected adjacency matrix could be
 			// attached to
 			this.fileName = file.getName();
 			ArrayList<DataSetNode> dataSetstmp = new ArrayList<DataSetNode>();
 			for (Enumeration<?> en = projectNode.children(); en.hasMoreElements();) {
 				ProjectTreeNode node = (ProjectTreeNode) en.nextElement();
 				if (node instanceof DataSetNode) {
 					dataSetstmp.add((DataSetNode) node);
 				}
 			}
 
 			if (dataSetstmp.isEmpty()) {
 				JOptionPane.showMessageDialog(null,
 						"No Sequence Set is available");
 				return null;
 			} else {
 				DataSetNode[] dataSets = dataSetstmp.toArray(new DataSetNode[1]);
 				JDialog loadDialog = new JDialog();
 
 				loadDialog.addWindowListener(new WindowAdapter() {
 
 					public void windowClosing(WindowEvent e) {
 						isCancel = true;
 					}
 				});
 
 				isCancel = false;
				loadDialog.setTitle("Load Interaction Network");
 				SelectParentDataSet loadPanel = new SelectParentDataSet(
 						loadDialog, dataSets);
 
 				loadDialog.add(loadPanel);
 				loadDialog.setModal(true);
 				loadDialog.pack();
 				Util.centerWindow(loadDialog);
 				loadDialog.setVisible(true);
 
 				if (isCancel)
 					return null;
 
 			}
 			
 			if ((selectedDataSetNode != null)) {
 				DSDataSet<? extends DSBioObject> ds = selectedDataSetNode.getDataset();
 				if(ds instanceof DSSequence) {	
 					JOptionPane.showMessageDialog(null,
 							"Not a Sequence Set selected", "Unable to Load",
 							JOptionPane.ERROR_MESSAGE);
 				} else {
 					patternResult = new PatternResult(ds.getFile(), (DSDataSet<DSSequence>) ds);
 					if(!patternResult.read(file)) {
 						return null;
 					}
 				}
 			} else {
 				JOptionPane.showMessageDialog(null, "No Microarray Set selected",
 						"Unable to Load", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		return patternResult;
 	}
 
 	@Override
 	public boolean checkFormat(File file) throws InterruptedIOException {
 		return true;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geworkbench.components.parsers.FileFormat#getFileFilter()
 	 */
 	public FileFilter getFileFilter() {
 		return patFilter;
 	}
 	
 	/**
 	 * Defines a <code>FileFilter</code> to be used when the user is prompted
 	 * to select SOFT input files. The filter will only display files
 	 * whose extension belongs to the list of file extensions defined.
 	 * 
 	 * @author yc2480
 	 * @author nrp2119
 	 */
 	class PatternFilter extends FileFilter {
 
 		public String getDescription() {
 			return getFormatName();
 		}
 
 		public boolean accept(File f) {
 			boolean returnVal = false;
 			for (int i = 0; i < patExtensions.length; ++i)
 				if (f.isDirectory() || f.getName().toLowerCase().endsWith(patExtensions[i])) {
 					return true;
 				}
 			return returnVal;
 		}
 	}
 	
 	private class SelectParentDataSet extends JPanel {
 		
 		static final long serialVersionUID = -1855255412334333328L;
 
 		final JDialog parent;
 
 		private JComboBox sequenceSetJcb;
 		private DataSetNode[] dataSets;
 		
 		JLabel label3 = new JLabel("Sequence Dataset:   ");
 		JButton continueButton = new JButton("Continue");
 		JButton cancelButton = new JButton("Cancel");
 
 		public SelectParentDataSet(JDialog parent,
 				DataSetNode[] dataSets) {
 
 			setLayout(new BorderLayout());
 			this.parent = parent;
 			this.dataSets = dataSets;
 			init();
 
 		}
 
 		private void init() {
 
 			JPanel panel1 = new JPanel(new GridLayout(4, 3));
 			JPanel panel2 = new JPanel(new GridLayout(2, 1));
 			JPanel panel3 = new JPanel(new GridLayout(0, 3));
 			sequenceSetJcb = new JComboBox(dataSets);
 			panel1.add(label3);
 			panel1.add(sequenceSetJcb);
 			
 			continueButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					continueButtonActionPerformed();
 					parent.dispose();
 					isCancel = false;
 				}
 			});
 			cancelButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					parent.dispose();
 					isCancel = true;
 				}
 			});
 			panel3.add(continueButton);
 			panel3.add(new JLabel("  "));
 			panel3.add(cancelButton);
 
 			this.add(panel1, BorderLayout.NORTH);
 			this.add(panel2, BorderLayout.CENTER);
 			this.add(panel3, BorderLayout.SOUTH);
 
 		}
 		private void continueButtonActionPerformed() {
 
 			selectedDataSetNode = (DataSetNode) sequenceSetJcb
 					.getSelectedItem();
 
 		}
 		
 	}
 	
 }

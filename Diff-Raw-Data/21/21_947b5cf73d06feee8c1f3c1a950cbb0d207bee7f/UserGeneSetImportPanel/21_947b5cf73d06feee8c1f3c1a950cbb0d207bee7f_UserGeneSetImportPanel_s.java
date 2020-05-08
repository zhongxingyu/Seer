 /**
  * * Copyright (c) 2007 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
  * * Research, University of Toronto
  * *
  * * Code written by: Michael Matan
  * * Authors: Michael Matan, Gary D. Bader
  * *
  * * This library is free software; you can redistribute it and/or modify it
  * * under the terms of the GNU Lesser General Public License as published
  * * by the Free Software Foundation; either version 2.1 of the License, or
  * * any later version.
  * *
  * * This library is distributed in the hope that it will be useful, but
  * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  * * documentation provided hereunder is on an "as is" basis, and
  * * University of Toronto
  * * has no obligations to provide maintenance, support,
  * * updates, enhancements or modifications.  In no event shall the
  * * University of Toronto
  * * be liable to any party for direct, indirect, special,
  * * incidental or consequential damages, including lost profits, arising
  * * out of the use of this software and its documentation, even if
  * * University of Toronto
  * * has been advised of the possibility of such damage.  See
  * * the GNU Lesser General Public License for more details.
  * *
  * * You should have received a copy of the GNU Lesser General Public License
  * * along with this library; if not, write to the Free Software Foundation,
  * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  * *
  * * Description: GUI Widget component of the GOSlimmer panel for importing user gene sets   
  */
 package org.ccbr.bader.yeast.view.gui;
 
 import giny.model.Node;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextPane;
 import javax.swing.JViewport;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 import org.ccbr.bader.yeast.GOSlimmer;
 import org.ccbr.bader.yeast.GOSlimmerSession;
 import org.ccbr.bader.yeast.GOSlimmerUtil;
 import org.ccbr.bader.yeast.controller.GOSlimmerController;
 import org.ccbr.bader.yeast.view.gui.misc.JButtonMod;
 import org.ccbr.bader.yeast.view.gui.misc.JLabelMod;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.attr.MultiHashMapDefinition;
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 import cytoscape.task.util.TaskManager;
 
 
 /**GUI Widget component of the GOSlimmer panel for importing user gene sets.
  * @author mikematan
  *
  */
 public class UserGeneSetImportPanel extends JPanel implements ActionListener {
 
 	private static final String lsep = System.getProperty("line.separator");
 	
 	private GOSlimmerSession session;
 	
 	public UserGeneSetImportPanel(GOSlimmerSession session) {
 		this.session = session;
 	}
 	
 	
 	private void initComponents() {
 		this.setBorder(BorderFactory.createTitledBorder("Import User Gene Set"));
 		this.add(getImportGeneSetButton());
 		//initiallize these two, but do not add them until they have info to display, as otherwise they will take up empty space
 		getUnmatchedIdsLabel();
 		getMatchedIdsLabel();
 
 	}
 	
 	private JButton importGeneSetButton;
 	private static final String importGeneSetButtonText = "Import Gene Set";
 	private static final String importGeneSetButtonToolTip =
 			 "Import a text file of gene IDs and you'll be able to examine "+
 		lsep+"the degree to which selected GO terms cover your gene set.   "+
 		lsep+"Gene IDs are mapped to GO terms by way of the Gene Annotation"+ 
 		lsep+"file which you've applied, so the Gene IDs need to match the "+
 		lsep+"gene names used in the annotation file.  Gene IDs must be on " +
 		lsep+"separate lines of the file";
 	
 	private JButton getImportGeneSetButton() {
 		if (importGeneSetButton==null) {
 			importGeneSetButton = new JButtonMod(importGeneSetButtonText);
 			importGeneSetButton.addActionListener(this);
 			importGeneSetButton.setToolTipText(importGeneSetButtonToolTip);
 		}
 		return importGeneSetButton;
 	}
 	
 	{
 		initComponents();
 	}
 
 	public void actionPerformed(ActionEvent event) {
 		Object src = event.getSource();
 		if (src instanceof JButton) {
 			JButton bsrc = (JButton) src;
 			if(bsrc.getText().equals(importGeneSetButtonText)) {
 				//delete the previously imported gene set, if one exists
 				if (session.isUserGeneSetImported()) {
 					removePreviousGeneSet();
 					session.setUserGeneSetImported(false);
 				}
 				importGeneSet();
 				return;
 			}
 		}
 	}
 
 	/**Removes the data associated with a previously imported user gene set from the GOSlimmer session's data model.  Basically 
 	 * a clean up operation which is used before importing a new user gene set.
 	 * 
 	 */
 	private void removePreviousGeneSet() {
 		//for each namespace network, iterate through the nodes and remove the annotated user gene list attributes
 		for (GOSlimmerController controller: session.getNamespaceToController().values()) {
 			Iterator<Node> nodesI = controller.getNetwork().nodesIterator();
 			while(nodesI.hasNext()) {
 				Node node = nodesI.next();
 				if ((nodeAtt.getMultiHashMapDefinition().getAttributeValueType(GOSlimmer.directlyAnnotatedUserGenesAttributeName))!=-1) {
 					nodeAtt.deleteAttribute(node.getIdentifier(),GOSlimmer.directlyAnnotatedUserGenesAttributeName);
 				}
 				if ((nodeAtt.getMultiHashMapDefinition().getAttributeValueType(GOSlimmer.inferredAnnotatedUserGenesAttributeName))!=-1) {
 					nodeAtt.deleteAttribute(node.getIdentifier(),GOSlimmer.inferredAnnotatedUserGenesAttributeName);
 				}
 			}
 		}
 	}
 
 
 	private File lastImportDir;
 	
 	/**
 	 * Imports the user specified gene set into the GOSlimmer sessions data model, which involves mapping the user genes 
 	 * to the genes in the gene association annotation file which has been imported and recording the lists of covered user 
 	 * genes as attributes of the GO nodes in the GOSlimmer sub-graphs.
 	 */
 	private void importGeneSet() {
 		//set the start directory of the JFileChooser to the last import directory, if it has been determined
 		JFileChooser chooser = lastImportDir==null?new JFileChooser():new JFileChooser(lastImportDir);
 		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		if (session.getGaru()==null) {
 			JOptionPane.showMessageDialog(this, "No Gene Annotation file has been applied; A gene annotation must first be imported in order to map Gene IDs to GO Terms.","Error",JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		int retval = chooser.showOpenDialog(this);
 		if (retval==JFileChooser.APPROVE_OPTION) {
 			final File geneFile = chooser.getSelectedFile();
 			lastImportDir = geneFile.getParentFile();
 			//execute task in background for importing the user gene set.
 			TaskManager.executeTask(new Task() {
 
				public String getTitle() {
 					return "Importing Gene ID File and attaching matching Genes to GO Terms";
 				}
 
 				public void halt() {
 					
 				}
 
 				public void run() {
 					try {
 						Collection<String> geneIds = parseGeneIdFile(geneFile);
 						annotateDAGWithUserGeneSet(geneIds);
 						//update user gene count in the session, so that coverage can be properly calculated by the statbeans
 						session.setUserGeneSetImported(true);
 						session.setUserGeneSet(geneIds);
 					} catch (FileNotFoundException e) {
 						JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Failed to import Gene Set;  Could not find specified file","Error",JOptionPane.ERROR_MESSAGE);
 						return;
 					} catch (IOException e) {
 						JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Failed to import Gene Set;  Error while reading file","Error",JOptionPane.ERROR_MESSAGE);
 						e.printStackTrace();
 						return;
 					}
					
				}
 
				public void setTaskMonitor(TaskMonitor arg0) throws IllegalThreadStateException {
					// TODO Auto-generated method stub
 					
 				}
 				
 			}, null);
 //			try {
 //				Collection<String> geneIds = parseGeneIdFile(geneFile);
 //				annotateDAGWithUserGeneSet(geneIds);
 //				session.setUserGeneSetImported(true);
 //			} catch (FileNotFoundException e) {
 //				JOptionPane.showMessageDialog(this, "Failed to import Gene Set;  Could not find specified file","Error",JOptionPane.ERROR_MESSAGE);
 //				return;
 //			} catch (IOException e) {
 //				JOptionPane.showMessageDialog(this, "Failed to import Gene Set;  Error while reading file","Error",JOptionPane.ERROR_MESSAGE);
 //				e.printStackTrace();
 //				return;
 //			}
 			//TODO clean up if failed to import gene set
 			
 			
 		}
 		
 	}
 	
 	/**Parse the gene ID list file imported by the user
 	 * @param geneFile the file specified by the user which contains the user gene set
 	 * @return a collection of gene IDs parsed from the file
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private Collection<String> parseGeneIdFile(File geneFile) throws FileNotFoundException,IOException {
 		BufferedReader in = new BufferedReader(new FileReader(geneFile));
 		Collection<String> geneIds = new HashSet<String>();
 		String line = null;
 		while ((line=in.readLine())!=null) {
 			String geneId = parseGeneIdLine(line);
 			if (geneId!=null) geneIds.add(geneId);
 		}
 		return geneIds;
 	}
 
 
 	/**Parses a line of the user gene ID list file.  Line is expected to contain a single Gene ID, without any whitespace within it.  Trailing whitespace is allowed.
 	 * @param line
 	 * @return
 	 */
 	private String parseGeneIdLine(String line) {
 		if (line == null || line.matches("\\s*")) return null;
 		line = line.trim();
 		//TODO insert checks to ensure format is acceptable
 		if (line.matches(".*\\s.*")) {
 			String errorMessage = "Parse error while processing gene ID file:  cannot have whitespaces within a Gene ID.  Invalid line was: " + line;
			JOptionPane.showMessageDialog(null, errorMessage,"Error parsing user gene file",JOptionPane.ERROR_MESSAGE);
 			throw new RuntimeException(errorMessage);
 		}
 		return line; //trimmed
 	}
 
 
 	/**Annotates the GOSlimmer subgraphs (DAGs) with the user gene set information.  This sets list attributes of the nodes within the 
 	 * subgraphs which detail user gene IDs covered by that GO term, whether directly or indirectly.
 	 * @param geneIds the IDs of the genes which the DAG is to be annotated with
 	 */
 	private void annotateDAGWithUserGeneSet(Collection<String> geneIds) {
 		/*
 		 * two passes: 
 		 * 	first pass, create attributes for directly annotating terms
 		 * 	second pass,  create attributes for indirectly/inferred annotating terms
 		 */
 		
 		Collection<String> matchedIds = new HashSet<String>(); //this will collect all the user gene IDs which were successfully matched to GO Term(s) 
 //		for(GOSlimmerController controller:session.getNamespaceToController().values()) {
 //			CyNetwork network = controller.getNetwork();
 //			//remove the last set of user annotated genes, since they are no longer relevant
 //			if (GOSlimmerUtil.areUserGeneAttributesDefined()) GOSlimmerUtil.removeUserGeneAttributes(network);
 //			
 //			//first pass, determine direct annotation
 //			controller.attachDirectlyAnnotatedUserGenesToTerms(geneIds);
 //			//second pass, determined inferred annotation
 //			controller.attachInferredAnnotatedUserGenesToTerms(geneIds);
 //			
 //			
 //
 //			
 //			
 //			//add all IDs which were successfully matched 
 //			matchedIds.addAll(GOSlimmerUtil.getGenesCoveredByGoNode(GOSlimmerUtil.getRootNode(network), true,true));
 //			
 //			//TODO insert test to determine which of the user specified IDs were not matched to any GO term
 //			/*
 //			 * pseudocode:
 //			 * 	get root node of graph
 //			 * 	get the directly and inferred gene sets for the root
 //			 *  any genes in the original set which are not in the 
 //			 */
 //		}
 		//TODO eliminate some of this gratuitous code duplication.
 		for(GOSlimmerController controller:session.getNamespaceToController().values()) {
 			matchedIds.addAll(controller.applyUserGeneSet(geneIds));
 		}
 		/* now, determine the difference between the user gene set and those which were successfully matched, and notify the 
 		 * user somehow of which gene IDs failed to be matched  
 		 */
 		unmatchedIds = GOSlimmerUtil.difference(geneIds,matchedIds);
 		updateTotalUserIdsLabel(geneIds);
 		updateUnmatchedIdsLabel(unmatchedIds);
 		updateMatchedIdsLabel(matchedIds);
 		this.add(getTotalIdsLabel());
 		this.add(getMatchedIdsLabel());
 		this.add(getUnmatchedIdsLabel());
 		
 		for(GOSlimmerController controller:session.getNamespaceToController().values()) {
 			//recalculate the user gene statistics based on this newly imported user gene ID file
 			//TODO determine if we should be using the matched genes count, or the full number of genes which the user attempted to import
 			controller.setupUserGeneStatistics(matchedIds.size());
 		}
 	}
 	
 	/**Returns a mouselistener which pops up a JFrame inset JTable which lists the ids passed in the <code>ids</code> collection.  The JTable uses the <code>idType</code> parameter to 
 	 * label the JTable's single column, and the <code>popupTitle</code> is used as the name of the JFrame popup
 	 * @param idType the type of the ids, used to label the JTable column in the popup frame
 	 * @param popupTitle the name to be used for the popup frame
 	 * @param ids the ids which will be listed in the popup JFrame's JTable
 	 * @return a mouselistener which pops up the frame on mouseclick events
 	 */
 	MouseListener getIdListPopupMouseListener(final String idType, final String popupTitle, final Collection<String> ids) {
 		return new MouseListener() {
 
 			public void mouseClicked(MouseEvent arg0) {
 				if (ids==null || ids.size()==0) return;
 				JFrame frame = new JFrame(popupTitle);
 				JScrollPane spane;
 				JTable table = new JTable();
 				TableModel model = new DefaultTableModel(ids.size(),1){
 					@Override
 					public boolean isCellEditable(int arg0, int arg1) {
 						return false;
 					}
 				};
 				Iterator<String> unmatchedIdI = ids.iterator();
 				int i = 0;
 				while(unmatchedIdI.hasNext()) {
 					model.setValueAt(unmatchedIdI.next(), i, 0);
 					i++;
 				}
 				table.setShowGrid(true);
 				
 				table.setModel(model);
 				table.getColumnModel().getColumn(0).setHeaderValue(idType);
 				spane = new JScrollPane(table);
 				
 				JViewport port = new JViewport();
 				port.setName(idType);
 				spane.setColumnHeader(port);
 				frame.getContentPane().add(spane);
 				frame.pack();
 				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 				frame.setVisible(true);
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				
 			}
 
 			public void mousePressed(MouseEvent arg0) {
 				
 			}
 
 			public void mouseReleased(MouseEvent arg0) {
 				
 			}
 			
 		};
 	}
 	
 	/**
 	 * Notes the total number of Gene IDs which the user attempted to import
 	 */
 	JLabel totalIdsLabel;
 	
 	private JLabel getTotalIdsLabel() {
 		if (totalIdsLabel == null ) {
 			totalIdsLabel = new JLabel();
 		}
 		return totalIdsLabel;
 	}
 	
 	/**Updates the JLabel detailing the genes which the user attempted to import based on the collection of gene IDs passed as a parameter
 	 * @param totalIds String Collection of gene IDs which the user attempted to import
 	 */
 	public void updateTotalUserIdsLabel(final Collection<String> totalIds) {
 		//remove the old mouse listeners
 		for (MouseListener ml: getTotalIdsLabel().getMouseListeners()) getTotalIdsLabel().removeMouseListener(ml);
 		if (totalIds == null || totalIds.size()==0) {
 			getTotalIdsLabel().setText("0 User genes;");
 		}
 		else {
 			getTotalIdsLabel().setText(totalIds.size() + " User Genes");
 			getTotalIdsLabel().setToolTipText("Click for more info");
 			getTotalIdsLabel().addMouseListener(getIdListPopupMouseListener("User Gene IDs", "These are the User Specified Gene IDs which the user attempted to import", totalIds));
 		}
 	}
 	
 	
 
 	
 	JLabel matchedIdsLabel;
 	/**Updates the JLabel detailing the genes which the user successfully imported based on the collection of gene IDs passed as a parameter
 	 * @param matchedIds the String Collection of gene IDs which were successfully imported and matched to GO terms
 	 */
 	public void updateMatchedIdsLabel(final Collection<String> matchedIds) {
 //		remove the old mouse listeners
 		for (MouseListener ml: getMatchedIdsLabel().getMouseListeners()) getMatchedIdsLabel().removeMouseListener(ml);
 		
 		if (matchedIds==null || matchedIds.size() ==0) {
 			getMatchedIdsLabel().setText("No User Gene IDs could be mapped to GO Terms.");
 			matchedIdsLabel.setToolTipText("");
 
 		}
 		else {
 			getMatchedIdsLabel().setText(matchedIds.size() + " Gene IDs were mapped to GO Terms");
 			matchedIdsLabel.setToolTipText("Click for more info");
 			matchedIdsLabel.addMouseListener(getIdListPopupMouseListener("Matched Gene IDs", "These User Specified Gene IDs were matched to Gene IDs in the Gene Annnotation File", matchedIds));
 		}
 	}
 	
 	/**Updates the JLabel detailing the genes which the user unsuccessfully imported based on the collection of gene IDs passed as a parameter
 	 * @param unmatchedIds the String Collection of gene IDs which were imported but could not be matched to GO terms
 	 */
 	public void updateUnmatchedIdsLabel(final Collection<String> unmatchedIds) {
 //		remove the old mouse listeners
 		for (MouseListener ml: getUnmatchedIdsLabel().getMouseListeners()) getUnmatchedIdsLabel().removeMouseListener(ml);
 		if (unmatchedIds==null || unmatchedIds.size() ==0) {
 			getUnmatchedIdsLabel().setText("All User Gene IDs successfully mapped to GO Terms.");
 			unmatchedIdsLabel.setToolTipText("");
 		}
 		else {
 			getUnmatchedIdsLabel().setText(unmatchedIds.size() + " Gene IDs could not be mapped to GO Terms");
 			unmatchedIdsLabel.setToolTipText("Click for more info");
 			unmatchedIdsLabel.addMouseListener(getIdListPopupMouseListener("Unmatched Gene IDs", "These User Specified Gene IDs could not be matched to Gene IDs in the Gene Annnotation File", unmatchedIds));
 		}
 	}
 
 	private Collection<String> unmatchedIds;
 	
 	private JLabel unmatchedIdsLabel;
 	private JLabel getUnmatchedIdsLabel() {
 //		final Collection<String> unmatchedIds = this.unmatchedIds;
 		if (unmatchedIdsLabel==null) {
 			unmatchedIdsLabel = new JLabelMod("");
 		}
 		return unmatchedIdsLabel;
 	}
 	
 	private JLabel getMatchedIdsLabel() {
 		if (matchedIdsLabel==null) {
 			matchedIdsLabel = new JLabelMod("");
 
 //			updateUnmatchedIdsLabel();
 		}
 		return matchedIdsLabel;
 	}
 
 	private static final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
 
 	
 	
 }

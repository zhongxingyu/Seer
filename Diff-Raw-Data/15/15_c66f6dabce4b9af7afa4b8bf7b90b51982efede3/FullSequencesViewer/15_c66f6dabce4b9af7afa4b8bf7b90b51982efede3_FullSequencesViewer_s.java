 /*
  * BioJava development code
  *
  * This code may be freely distributed and modified under the
  * terms of the GNU Lesser General Public Licence. This should
  * be distributed with the code. If you do not have a copy,
  * see:
  *
  * http://www.gnu.org/copyleft/lesser.html
  *
  * Copyright for this code is held jointly by the individual
  * authors. These should be listed in @author doc comments.
  *
  * For more information on the BioJava project and its aims,
  * or to join the biojava-l mailing list, visit the home page
  * at:
  *
  * http://www.biojava.org/
  *
  * This code was contributed from the Molecular Biology Toolkit
  * (MBT) project at the University of California San Diego.
  *
  * Please reference J.L. Moreland, A.Gramada, O.V. Buzko, Qing
  * Zhang and P.E. Bourne 2005 The Molecular Biology Toolkit (MBT):
  * A Modular Platform for Developing Molecular Visualization
  * Applications. BMC Bioinformatics, 6:21.
  *
  * The MBT project was funded as part of the National Institutes
  * of Health PPG grant number 1-P01-GM63208 and its National
  * Institute of General Medical Sciences (NIGMS) division. Ongoing
  * development for the MBT project is managed by the RCSB
  * Protein Data Bank(http://www.pdb.org) and supported by funds
  * from the National Science Foundation (NSF), the National
  * Institute of General Medical Sciences (NIGMS), the Office of
  * Science, Department of Energy (DOE), the National Library of
  * Medicine (NLM), the National Cancer Institute (NCI), the
  * National Center for Research Resources (NCRR), the National
  * Institute of Biomedical Imaging and Bioengineering (NIBIB),
  * the National Institute of Neurological Disorders and Stroke
  * (NINDS), and the National Institute of Diabetes and Digestive
  * and Kidney Diseases (NIDDK).
  *
  * Created on 2008/12/22
  *
  */ 
 package org.rcsb.lx.ui;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Vector;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 
 import org.rcsb.lx.controllers.app.LigandExplorer;
 import org.rcsb.lx.controllers.update.LXUpdateEvent;
 import org.rcsb.mbt.model.ExternChain;
 import org.rcsb.mbt.model.Residue;
 import org.rcsb.mbt.model.Structure;
 import org.rcsb.mbt.model.StructureComponent;
 import org.rcsb.mbt.model.StructureMap;
 import org.rcsb.mbt.model.StructureModel;
 import org.rcsb.mbt.model.util.DebugState;
 import org.rcsb.uiApp.controllers.app.AppBase;
 import org.rcsb.uiApp.controllers.update.IUpdateListener;
 import org.rcsb.uiApp.controllers.update.UpdateEvent;
 import org.rcsb.vf.glscene.jogl.SequencePanelBase;
 
 
 /**
  * @author John Beaver
  */
 @SuppressWarnings("serial")
 public class FullSequencesViewer extends JScrollPane implements IUpdateListener
 {
 	JPanel contentPane = new JPanel(null,false);
     
     // definitions for painted component sizes
     
     public ArrayList<SequencePanelBase> sequencePanels = new ArrayList<SequencePanelBase>();
     
     private boolean isDirty;
     
     public FullSequencesViewer()
     {
         super();
         super.setViewportView(contentPane);
         super.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         super.setDoubleBuffered(false);
         LigandExplorer.sgetUpdateController().registerListener(this);
     }
 
     public void setComponents()
     {
         createViewersForCurrentEpitope();
     }
     
     public void createViewersForCurrentEpitope()
     {
        contentPane.removeAll();
         sequencePanels.clear();
         StructureModel model = AppBase.sgetModel();
         if (model.getStructures() == null)
         	return;
         
         for (Structure struc : model.getStructures())
         {
 	        final StructureMap sm = struc.getStructureMap();
 	        
 	        final int scrollbarWidth = (int)super.getVerticalScrollBar().getPreferredSize().getWidth();
 	        
 	        final Vector<StructureComponent> topChains = sm.getPdbTopLevelElements();
 	        Vector<StructureComponent> chains = new Vector<StructureComponent>();
 	        
 	        for (StructureComponent comp : topChains)
 	          if (!(comp instanceof ExternChain && ((ExternChain)comp).getResidue(0).getClassification() ==
 	        	  Residue.Classification.LIGAND)) chains.add(comp);
 	        						// leave out the ligands...
 	        	  
 	        if (chains != null)
 	        {            
 	            FullSequencePanel[] tempSequencePanels = new FullSequencePanel[chains.size()];
 	            
 	            int i = 0;
 	            for (StructureComponent c : chains)
 	            {
 	            	String description = null;
 	            	
 	            	if (DebugState.isDebug())
 	            		assert (c instanceof ExternChain);
 	            						// curious if we ever get anything else (and why)...
 	            	
 	                if (c instanceof ExternChain)
 	                {
 	                	ExternChain xc = (ExternChain)c;
 	                	switch (xc.getExternChainType())
 	                	{
 	                	case BASIC: description = "Chain " + xc.getChainId() + ":"; break;
 	                	case MISCELLANEOUS: description = "Non Protein:"; break;
 	                	default:
 	                		continue;
 	                						// no waters...
 	                	}
 	                }
 	                tempSequencePanels[i++] = new FullSequencePanel(description, c, scrollbarWidth);
 	            }
 	            
 	            Arrays.sort(tempSequencePanels,
 	            	new Comparator<FullSequencePanel>()
 	            	{
 						public int compare(final FullSequencePanel s1, final FullSequencePanel s2)
 						{
 							if (s1 == null)
 								return 1;
 							
 							else if (s2 == null)
 								return -1;
 								
 							ExternChain xc1 = (ExternChain)s1.chain;
 							ExternChain xc2 = (ExternChain)s2.chain;
 							
 							// non-protein chains always go to the bottom
 							if (xc1.isMiscellaneousChain())
 									return 1;
 							
 							// non-protein chains always go to the bottom
 							if (xc2.isMiscellaneousChain())
 								   return -1;
 							
 							return xc1.getChainId().compareTo(xc2.getChainId());
 						}           	
 		            });
 	            
 	            sequencePanels.add(new SequenceStructureTitlePanel(struc));
 	            for (FullSequencePanel panel : tempSequencePanels)
 	            	if (panel != null) sequencePanels.add(panel);
 	        }
         }
         
         for (SequencePanelBase panel : sequencePanels)
         	contentPane.add(panel);
         
         // force a resize of the components (see paintComponent())
         isDirty = true;
         
         super.repaint();
     }
     
     private final Dimension oldSize = new Dimension(-1,-1);
 
     @Override
 	public void paintComponent(final Graphics g)
     {
         final Dimension newSize = contentPane.getSize();
         
         if(!newSize.equals(oldSize) || isDirty) {
             final JScrollBar bar = super.getVerticalScrollBar();
             if(bar == null) {
                 return;
             }
             
             final int scrollWidth = bar.getWidth();
             final Insets insets = super.getInsets();
             
             int curY = 0;
             if (!sequencePanels.isEmpty())
             	for (SequencePanelBase panel : sequencePanels)
             	{
             		panel.heightForWidth(newSize.width);
 	                panel.setBounds(0,curY,newSize.width,panel.preferredHeight);
 	                curY += panel.preferredHeight + 3;
                 }
             
 			contentPane.setPreferredSize(new Dimension(newSize.width - scrollWidth - insets.left - insets.right,
 													   curY + insets.top + insets.bottom));
         }
     }
 
     /*
 	 * Called either from the SequencesTab or from the update manager.
 	 * 
 	 * 24-Oct-08 - rickb
      */
     public void updateSequences()
     {
 			isDirty = true;
 			for (SequencePanelBase panel : sequencePanels)
 				panel.isDirty = true;
 			
 			this.invalidate();
 	}
 
 	public void handleUpdateEvent(UpdateEvent evt)
 	{
 		if (evt.action == UpdateEvent.Action.EXTENDED &&
 			((LXUpdateEvent)evt).lxAction == LXUpdateEvent.LXAction.INTERACTIONS_CHANGED)
 				updateSequences();
 	}
 }

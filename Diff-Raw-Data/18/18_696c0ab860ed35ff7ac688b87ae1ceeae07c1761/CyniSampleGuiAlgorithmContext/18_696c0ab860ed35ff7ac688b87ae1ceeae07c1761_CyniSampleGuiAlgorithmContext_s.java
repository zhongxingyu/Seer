 /*
  * #%L
  * Cyni Sample Gui implementaion
  * $Id:$
  * $HeadURL:$
  * %%
  * Copyright (C) 2006 - 2013 The Cytoscape Consortium
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 
 package org.cytoscape.cyniSampleGuiAlgorithm.internal;
 
 import org.cytoscape.cyni.*;
import javax.swing.JPanel;
 import javax.swing.*;
 import java.awt.*;
 
 
 public class CyniSampleGuiAlgorithmContext extends CyniAlgorithmContext  {
 	
 	private JPanel mainPanel;
 	
 	CyniSampleGui param1Panel, param2Panel;
     Option[] param1Distances = new Option[3];
     Option[] param2Distances = new Option[3];
     final static boolean MULTICOLORED = false;
  
     LimitsRangeModel dataModel = new LimitsRangeModel();
     
     LimitsRangeModel dataModel2 = new LimitsRangeModel();
 
 	public CyniSampleGuiAlgorithmContext( ) {
 		super(true);
 		
 		
         param1Distances[0] = new Option("Only positives", 5.0);
         param1Distances[1] = new Option("Only Negatives", -5.0);
         param1Distances[2] = new Option("All", 0.0);
         param1Panel = new CyniSampleGui( "Parameter 1",param1Distances,
                                           dataModel);
  
         
         param2Distances[0] = new Option("Only positives", 5.0);
         param2Distances[1] = new Option("Only Negatives", -5.0);
         param2Distances[2] = new Option("All", 0.0);
 
         param2Panel = new CyniSampleGui("Parameter 2",param2Distances,
                                        dataModel2);
  
         mainPanel = new JPanel();
         mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
         if (MULTICOLORED) {
             mainPanel.setOpaque(true);
             mainPanel.setBackground(new Color(255, 0, 0));
         }
         mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
         mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
         mainPanel.add(param1Panel);
         mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
         mainPanel.add(param2Panel);
         mainPanel.add(Box.createGlue());
         dataModel.setDoubleValue(5.0);
         dataModel.setMinimum(0);
         dataModel.setMaximum(10);
         dataModel2.setDoubleValue(5.0);
         dataModel2.setMinimum(0);
         dataModel2.setMaximum(10);
 	}
	public boolean contextHasOwnSwingComponent()
 	{
 		return true;
 	}
 
 	public JPanel getContextSwingPanel()
 	{
 		return mainPanel;
 	}
 	
 	public boolean contextContentValid()
 	{
 		if(getParam2() > 0)
 			return true;
 		else
 			return false;
 	}
 	
 	public double getParam1()
 	{
 		return param1Panel.getLimit();
 	}
 
 	public double getParam2()
 	{
 		return param2Panel.getLimit();
 	}
 	
 	
 }

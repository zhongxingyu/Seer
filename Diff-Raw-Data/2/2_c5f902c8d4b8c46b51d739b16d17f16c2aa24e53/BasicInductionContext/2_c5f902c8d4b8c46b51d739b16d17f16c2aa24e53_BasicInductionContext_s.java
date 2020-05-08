 /*
  * #%L
  * Cyni Implementation (cyni-impl)
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
 package org.cytoscape.cyni.internal.inductionAlgorithms.BasicAlgorithm;
 
 import java.io.IOException;
 import java.util.List;
 import org.cytoscape.work.util.*;
 import org.cytoscape.model.CyTable;
 
 import org.cytoscape.cyni.*;
 import org.cytoscape.work.Tunable;
 import org.cytoscape.work.TunableValidator;
 
 public class BasicInductionContext extends AbstractCyniAlgorithmContext implements TunableValidator {
 	@Tunable(description="Threshold to add new edge")
 	public double thresholdAddEdge = 0.5;
 	
	@Tunable(description="Use only absolut values for correlation")
 	public boolean useAbsolut = true;
 
 	//@Tunable(description="Output Only Nodes with Edges")
 	//public boolean removeNodes = false;
 	
 	@Tunable(description="Use selected nodes only", groups="Parameters if a network associated to table data")
 	public boolean selectedOnly = false;
 	
 	@Tunable(description="Metric")
 	public ListSingleSelection<CyCyniMetric> measures;
 	
 	@Tunable(description="Data Attributes", groups="Sources for Network Inference")
 	public ListMultipleSelection<String> attributeList;
 
 	
 	private List<String> attributes;
 	
 	public BasicInductionContext(boolean supportsSelectedOnly, CyTable table,  List<CyCyniMetric> metrics) {
 		super(supportsSelectedOnly);
 		attributes = getAllAttributesNumbers(table);
 		if(attributes.size() > 0)
 		{
 			attributeList = new  ListMultipleSelection<String>(attributes);
 		}
 		else
 		{
 			attributeList = new  ListMultipleSelection<String>("No sources available");
 		}
 		if(metrics.size() > 0)
 		{
 			measures = new  ListSingleSelection<CyCyniMetric>(metrics);
 		}
 		else
 		{
 			measures = new  ListSingleSelection<CyCyniMetric>();//("No metrics available");
 		}
 	}
 	
 	@Override
 	public ValidationState getValidationState(final Appendable errMsg) {
 		setSelectedOnly(selectedOnly);
 		if (thresholdAddEdge < 0.0 && useAbsolut)
 		{
 			try {
 				errMsg.append("Threshold needs to be greater than 0.0!!!!");
 			} catch (IOException e) {
 				e.printStackTrace();
 				return ValidationState.INVALID;
 			}
 			return ValidationState.INVALID;
 		}
 			
 		if(measures.getPossibleValues().size()<=0) {
 			try {
 				errMsg.append("No metrics available to apply the algorithm!!!!");
 			} catch (IOException e) {
 				e.printStackTrace();
 				return ValidationState.INVALID;
 			}
 			return ValidationState.INVALID;
 			
 		}
 		
 		if(attributeList.getSelectedValues().get(0).matches("No sources available") || attributeList.getPossibleValues().size() == 0) {
 			try {
 				errMsg.append("No sources available to apply the algorithm!!!!");
 			} catch (IOException e) {
 				e.printStackTrace();
 				return ValidationState.INVALID;
 			}
 			return ValidationState.INVALID;
 			
 		}
 		return ValidationState.OK;
 	}
 }

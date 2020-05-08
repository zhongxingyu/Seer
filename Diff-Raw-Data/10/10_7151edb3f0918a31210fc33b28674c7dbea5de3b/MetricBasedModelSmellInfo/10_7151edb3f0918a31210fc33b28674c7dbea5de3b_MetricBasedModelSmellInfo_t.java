 package org.eclipse.emf.refactor.smells.generator.core;
 
 import org.eclipse.core.resources.IProject;
 
 /**
  * Subclass of <i>ModelSmellInfo</i> which holds specific information for the
  * <br>- generation of smell plugins based on EMF Metrics
  * 
  * @author Matthias Burhenne
  *
  */
 
 public class MetricBasedModelSmellInfo extends ModelSmellInfo {
 	
 	private String metricID;
 	private String comparator;
 
 	public MetricBasedModelSmellInfo(String id, String name,
 			String description, String metamodel, IProject project,
 			String metricID, String comparator) {
 		super(id, name, description, metamodel, project);
 		this.metricID = metricID;
 		this.comparator = comparator;
 	}
 	
 	public String getMetricID(){
 		return metricID;
 	}
 	
 	public String getComparator(){
 		if(comparator.equals("="))
 			return "==";
 		else
 			return comparator;
 	}
 
//	@Override
//	public String getDescription() {
//		String descriptionText = super.getDescription();
//		return descriptionText + " ('value " + comparator + " limit' indicates this smell)";
//	}
 }

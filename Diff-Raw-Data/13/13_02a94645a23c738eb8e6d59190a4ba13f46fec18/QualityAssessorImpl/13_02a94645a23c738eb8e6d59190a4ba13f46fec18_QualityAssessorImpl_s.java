 package cz.cuni.mff.odcleanstore.qualityassessment.impl;
 
 import cz.cuni.mff.odcleanstore.qualityassessment.*;
 import cz.cuni.mff.odcleanstore.transformer.TransformationContext;
 import cz.cuni.mff.odcleanstore.transformer.TransformedGraph;
 
 public class QualityAssessorImpl implements QualityAssessor {
 	
 	private CommonAssessment state;
 
 	@Override
 	public void transformNewGraph(TransformedGraph inputGraph,
 			TransformationContext context) {
 
 		state = new DirtyStoreAssessment();
 		
 		state.assessQuality(inputGraph, context);
 	}
 
 	@Override
 	public void transformExistingGraph(TransformedGraph inputGraph,
 			TransformationContext context) {
 		
 		state = new CleanStoreAssessment();
 		
 		state.assessQuality(inputGraph, context);
 	}
 }

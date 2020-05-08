 package edu.utah.cdmcc.glucose.tutorial.services;
 
 import glucose.GlucoseFactory;
 import glucose.IntensiveCareUnit;

 
 public class IntensiveCareUnitServiceImpl implements IntensiveCareUnitService {
 
 	//IntensiveCareUnit icu = GlucoseFactory.eINSTANCE.createExampleGlucoseModel();
 	IntensiveCareUnit icu = GlucoseFactory.eINSTANCE.createIntensiveCareUnit();
 	
 	@Override
 	public IntensiveCareUnit getRootGroup() {		
 		return icu;
 	}
 	
 
 
 }

 package it.unibg.robotics.resolutionmodel.ros.commands.resolution.expressions;
 
 import it.unibg.robotics.resolutionmodel.RMAbstractTransformation;
 import it.unibg.robotics.resolutionmodel.RMResolutionElement;
 import it.unibg.robotics.resolutionmodel.ResolutionModel;
 import it.unibg.robotics.resolutionmodel.rosresolutionmodel.ROSRequiredComponents;
 import it.unibg.robotics.resolutionmodel.rosresolutionmodel.ROSRequiredConnections;
 import it.unibg.robotics.resolutionmodel.rosresolutionmodel.ROSTransfConnection;
 import it.unibg.robotics.resolutionmodel.rosresolutionmodel.ROSTransfImplementation;
 import it.unibg.robotics.resolutionmodel.rosresolutionmodel.ROSTransfProperty;
 
 import org.eclipse.core.expressions.PropertyTester;
 
 public class ROSResolutionModelTypeTester extends PropertyTester{
 
 	@Override
 	public boolean test(Object receiver, String property, Object[] args,
 			Object expectedValue) {
 		
 		if (property.equals("is_a_ROS_resolution_model")) {
 			ResolutionModel model = (ResolutionModel)receiver;
 			
 			
 			for(RMResolutionElement resElem : model.getResolutionElements()){
 				
 				if(resElem.getRequiredComponents() instanceof ROSRequiredComponents){
 					return true;
 				}
 				if(resElem.getRequiredConnections() instanceof ROSRequiredConnections){
 					return true;
 				}
 				for(RMAbstractTransformation transf : resElem.getTransformations()){
 					
 					if(transf instanceof ROSTransfImplementation ||
 							transf instanceof ROSTransfProperty ||
 							transf instanceof ROSTransfConnection){
 						return true;
 					}
 					
 					
 				}
 				
 			}
 			
 		}
 		return false;
 	}
 
 }

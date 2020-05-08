 package edu.teco.dnd.deploy;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import edu.teco.dnd.blocks.FunctionBlock;
 import edu.teco.dnd.blocks.InvalidFunctionBlockException;
 import edu.teco.dnd.graphiti.model.FunctionBlockModel;
 import edu.teco.dnd.module.Module;
 import edu.teco.dnd.module.config.BlockTypeHolder;
 
 public class UserConstraints implements Constraint{
 
 	private Map<FunctionBlock, UUID> moduleConstraints = new HashMap<FunctionBlock, UUID>();
 	private Map<FunctionBlock, String> placeConstraints = new HashMap<FunctionBlock, String>();
 	
 	public UserConstraints(Map<FunctionBlockModel, UUID> modules, Map<FunctionBlockModel, String> place){
 		for (FunctionBlockModel model : modules.keySet()){
 			try {
 				moduleConstraints.put(model.createBlock(), modules.get(model));
 			} catch (InvalidFunctionBlockException e) {
 				e.printStackTrace();
 			}
 		}
 		for (FunctionBlockModel model: place.keySet()){
 			try {
 				placeConstraints.put(model.createBlock(), place.get(model));
 			} catch (InvalidFunctionBlockException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	@Override
 	public boolean isAllowed(Distribution distribution, FunctionBlock block,
 			Module module, BlockTypeHolder holder) {
 
 		if (moduleConstraints.containsKey(block)){
 			UUID id = module.getUUID();
 			if (!id.equals(moduleConstraints.get(block))){
 				return false;
 			}
 		}
 		
 		if (placeConstraints.containsKey(block)){
 			String place = module.getLocation();
			if(!placeConstraints.get(block).equals(place)){
 				return false;
 			}
 		}
 		
 		return true;
 	}
 
 }

 /**
  * 
  */
 package hu.modembed.pic;
 
 import hu.modembed.model.modembed.core.instructionset.Instruction;
 import hu.modembed.model.modembed.core.instructionset.InstructionSet;
 import hu.modembed.model.modembed.core.object.AssemblerObject;
 import hu.modembed.model.modembed.core.object.InstructionCall;
 import hu.modembed.model.modembed.core.object.InstructionCallParameter;
 import hu.modembed.model.modembed.core.object.ObjectFactory;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * @author balazs.grill
  *
  */
 public class BankSelectionOptimalization {
 
 	private Instruction movlb = null;
 	
 	/**
 	 * 
 	 */
 	public BankSelectionOptimalization(InstructionSet iset) {
 		for(Instruction i : iset.getInstructions()){
 			if ("MOVLB".equals(i.getName())){
 				this.movlb = i;
 			}
 		}
 	}
 	
 	public AssemblerObject optimize(AssemblerObject input){
 		if (movlb == null) return input;
 		
 		AssemblerObject output = ObjectFactory.eINSTANCE.createAssemblerObject();
 		output.setName(input.getName());
 		output.setDescription(input.getDescription());
 		output.setStartAddress(input.getStartAddress());
 		
 		Set<Long> labels = new TreeSet<Long>();
 		
 		for(InstructionCall ic : input.getInstructions()){
 			for(InstructionCallParameter icp : ic.getParameters()){
 				if (icp.isLabel()){
 					labels.add(icp.getValue());
 				}
 			}
 		}
 		
 		Map<Long, Long> labelValues = new HashMap<Long, Long>(labels.size());
 		List<InstructionCallParameter> labelParams = new LinkedList<InstructionCallParameter>();
 		long currentBank = -1;
 		long origin = 0;
 		long target = 0;
 		for(InstructionCall ic : input.getInstructions()){
 			boolean addi = false;
 			
 			if (labels.contains(origin)){
 				labelValues.put(origin, target);
 				addi = true; /* Always include MOVLB if control can jump here */
 				currentBank = -1;
 			}
 			
 
 			if (movlb.equals(ic.getInstruction())){
 				long newBank = -1;
 				for(InstructionCallParameter icp : ic.getParameters()){
 					newBank = icp.getValue();
 				}
 				if (currentBank == -1){
 					addi = true;
 				}else{
 					addi = newBank != currentBank;
 				}
 				currentBank = newBank;
 			}else{
 				addi = true;
 			}
 
 			
 			if (addi){
 				InstructionCall newic = ObjectFactory.eINSTANCE.createInstructionCall();
 				newic.setInstruction(ic.getInstruction());
 				newic.setDescription(ic.getDescription());
 				output.getInstructions().add(newic);
 				for(InstructionCallParameter icp : ic.getParameters()){
 					InstructionCallParameter newicp = ObjectFactory.eINSTANCE.createInstructionCallParameter();
 					newicp.setDefinition(icp.getDefinition());
 					newicp.setValue(icp.getValue());
 					newicp.setDescription(icp.getDescription());
 					newicp.setLabel(icp.isLabel());
 					newic.getParameters().add(newicp);
 					if (newicp.isLabel()){
 						labelParams.add(newicp);
 					}
 				}
 				target++;
 			}
 			origin++;
 		}
 		
 		for(InstructionCallParameter icp : labelParams){
			icp.setValue(labelValues.get(icp.getValue()));
 		}
 		
 		return output;
 	}
 
 }

 /**
  * 
  */
 package hu.modembed.pic.ant;
 
 import hu.modembed.MODembedCore;
 import hu.modembed.ant.TaskUtils;
 import hu.modembed.model.modembed.core.instructionset.Instruction;
 import hu.modembed.model.modembed.core.instructionset.InstructionSet;
 import hu.modembed.model.modembed.core.object.AssemblerObject;
 import hu.modembed.model.modembed.core.object.InstructionCall;
 import hu.modembed.pic.BankSelectionOptimalization;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 
 /**
  * @author balazs.grill
  *
  */
 public class OptimizeBankSelectTask extends Task {
 
 	private File input;
 	private File output;
 	
 	public void setInput(File input) {
 		this.input = input;
 	}
 	
 	public void setOutput(File output) {
 		this.output = output;
 	}
 	
 	private static InstructionSet detect(AssemblerObject asm) throws Exception{
 		Set<InstructionSet> isets = new HashSet<InstructionSet>();
 		for(InstructionCall ic : asm.getInstructions()){
 			Instruction i = ic.getInstruction();
 			EObject is = i.eContainer();
 			if (is instanceof InstructionSet){
 				isets.add((InstructionSet)is);
 			}
 		}
 		
 		for(InstructionSet iset : isets.toArray(new InstructionSet[isets.size()])){
 			InstructionSet ancestor = iset.getExtend();
 			if (ancestor != null){
 				isets.remove(ancestor);
 			}
 		}
 		
		if (isets.size() == 0) return isets.iterator().next();
 		
		if (isets.size() > 1) throw new Exception("Instruction set used in AssemblerObject is ambigous!");
 		
 		throw new Exception("Could not determine instruction set!");
 	}
 	
 	@Override
 	public void execute() throws BuildException {
 		if (input == null) throw new BuildException("input is a required attribute");
 		if (output == null) throw new BuildException("input is a required attribute");
 		
 		ResourceSet rs = MODembedCore.createResourceSet();
 		try {
 			AssemblerObject in = TaskUtils.loadInput(rs, input, AssemblerObject.class);
 			
 			if (TaskUtils.checkModificationTime("Bank-select optimization", rs, output)){
 				Resource out = TaskUtils.getOutput(rs, output);
 				BankSelectionOptimalization opt = new BankSelectionOptimalization(detect(in));
 				out.getContents().add(opt.optimize(in));
 				out.save(null);
 			}
 			
 		} catch (Exception e) {
 			throw new BuildException(e);
 		}
 	}
 
 }

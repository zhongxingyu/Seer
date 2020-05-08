 package com.vectorsf.jvoice.core.validation.operations;
 
 import java.io.File;
 
 import com.vectorsf.jvoice.model.operations.CallState;
 import com.vectorsf.jvoice.model.operations.ComponentBean;
 import com.vectorsf.jvoice.model.operations.Flow;
 
 public class CallStateValidator {
 
 	private final String PATH = "src/main/java";
 
 	private IOperationsValidator operationsValidator;
 
 	public CallStateValidator(IOperationsValidator operationsValidator) {
 		this.operationsValidator = operationsValidator;
 	}
 
 	public boolean validate_CallState_methodInstanceBeanExecutionState(CallState state) {
 
 		Flow flow = (Flow) state.eContainer();
 		boolean existbean = false;
 		for (ComponentBean bean : flow.getBeans()) {
 			if (bean == state.getBean()) {
 				existbean = true;
 			}
 		}
 		if (!existbean) {
 			operationsValidator.error(state, "Instance Bean  " + state.getBean() + " not found");
 		}
 
 		return true;
 	}
 
 	public boolean validate_CallState_exitsBeanInExecute(CallState state) {
 		Flow flow = (Flow) state.eContainer();
 
 		File rawFile = ValidatorUtils.getFile(state);
		System.out.println("****************** 1 rawFile " + rawFile);
 
 		File projectFile = ValidatorUtils.findProjectFile(rawFile);
		System.out.println("****************** 2 projectFile " + projectFile);
 
 		String classbean;
 		if (state.getBean() != null) {
 			classbean = state.getBean().getFqdn();
			System.out.println("****************** 3 classbean " + classbean);
 			File folder = new File(projectFile, PATH);
			System.out.println("****************** 4 folder " + folder);
 			File filepack = new File(folder, classbean.replace(".", "/").concat(".java"));
			System.out.println("****************** 5 filepack " + filepack);
			System.out.println("****************** 6 filepack.exists() " + filepack.exists());
 			if (!filepack.exists()) {
 				operationsValidator.error(state, "No exits bean in execute state \"" + state.getName()
 						+ "\" in flow \"" + flow.getName() + "\"");
 			}
 		}
 		return true;
 	}
 
 	public boolean validate_CallState_noEmptyParams(CallState state) {
 		for (String param : state.getParameters()) {
 			if (param.trim().isEmpty()) {
 				operationsValidator.error(state, "Parameters in Call State must not be empty.");
 				// Se utiliza un "break" para que solo aparezca un mensaje por
 				// estado, ya que no se puede poner el nombre del parametro vacio
 				// y por tanto, no se puede poner un mensaje por parametro.
 				break;
 			}
 		}
 		return true;
 	}
 }

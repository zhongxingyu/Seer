 package org.uncertweb.et.screening;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.uncertweb.et.design.DesignException;
 import org.uncertweb.et.design.MorrisDesign;
 import org.uncertweb.et.parameter.Input;
 import org.uncertweb.et.parameter.Output;
 import org.uncertweb.et.parameter.VariableInput;
 import org.uncertweb.et.process.ProcessEvaluationResult;
 import org.uncertweb.et.process.ProcessEvaluator;
 import org.uncertweb.et.process.ProcessEvaluatorException;
 
 public class Screening {
 
 	private String serviceURL;
 	private String processIdentifier;
 	private List<Input> inputs;
 	private List<VariableInput> variableInputs;
 	private List<Output> outputs;
 	private int numTrajectories = 5;
 	private int discretisationLevel = 10;
 	private int deltaP = 1; 
 	
 	public Screening(String serviceURL, String processIdentifier, List<Input> inputs, List<Output> outputs) {
 		this.serviceURL = serviceURL;
 		this.processIdentifier = processIdentifier;
 		this.inputs = inputs;
 		variableInputs = new ArrayList<VariableInput>();
 		for (Input input : inputs) {
 			if (input.isVariableInput()) {
 				variableInputs.add((VariableInput) input);
 			}
 		}
 		this.outputs = outputs;
 	}
 	
	public Screening(String serviceURL, String processIdentifier, List<Input> inputs, List<Output> outputs, int numTrajectories, int discretisationLevel, int deltaP) {
 		this(serviceURL, processIdentifier, inputs, outputs);
 		this.numTrajectories = numTrajectories;
 		this.discretisationLevel = discretisationLevel;
		this.deltaP = deltaP;
 	}
 
 	public List<MorrisOutputResult> run() throws ScreeningException, ProcessEvaluatorException, DesignException {
 		MorrisDesign design = runDesign();
 		ProcessEvaluationResult evalResults = runEval(design);
 		return runScreening(design, evalResults);
 	}
 
 	private MorrisDesign runDesign() throws DesignException {
 		return MorrisDesign.create(variableInputs, numTrajectories, discretisationLevel, deltaP);
 	}
 
 	private ProcessEvaluationResult runEval(MorrisDesign design) throws ProcessEvaluatorException {
 		return ProcessEvaluator.evaluate(serviceURL, processIdentifier, inputs, outputs, design);
 	}
 
 	private List<MorrisOutputResult> runScreening(MorrisDesign design, ProcessEvaluationResult evalResults) throws ScreeningException {
 		// do screening
 		Morris morris = new Morris(design, evalResults);
 		return morris.getResults();
 	}
 
 }

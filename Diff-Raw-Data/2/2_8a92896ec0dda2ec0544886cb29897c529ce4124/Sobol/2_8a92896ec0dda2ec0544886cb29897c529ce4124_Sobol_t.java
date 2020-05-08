 package org.uncertweb.et.sensitivity;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.rosuda.REngine.REXPMismatchException;
 import org.rosuda.REngine.REngineException;
 import org.uncertweb.et.ConfigException;
 import org.uncertweb.et.design.Design;
 import org.uncertweb.et.design.DesignException;
 import org.uncertweb.et.design.LHSDesign;
 import org.uncertweb.et.emulator.Emulator;
 import org.uncertweb.et.emulator.EmulatorEvaluationResult;
 import org.uncertweb.et.emulator.EmulatorEvaluator;
 import org.uncertweb.et.emulator.EmulatorEvaluatorException;
 import org.uncertweb.et.parameter.Input;
 import org.uncertweb.et.parameter.Output;
 import org.uncertweb.et.parameter.VariableInput;
 import org.uncertweb.et.process.ProcessEvaluationResult;
 import org.uncertweb.et.process.ProcessEvaluator;
 import org.uncertweb.et.process.ProcessEvaluatorException;
 
 public class Sobol {
 	
 	public static List<SobolOutputResult> run(List<Input> inputs, List<Output> outputs, String serviceURL, String processIdentifier, boolean plot, int designSize, int numBoot, double confidenceLevel) throws DesignException, ProcessEvaluatorException, SobolException {
 		// filter variable/fixed inputs
 		List<Input> variableInputs = new ArrayList<Input>();
 		for (Input input : inputs) {
 			if (input instanceof VariableInput) {
 				variableInputs.add(input);
 			}
 		}
 		
 		// need two designs
 		LHSDesign x1 = LHSDesign.create(variableInputs, designSize);
 		LHSDesign x2 = LHSDesign.create(variableInputs, designSize);
 
 		SobolRunner runner = null;
 		try {
 			// create a sobol runner
 			runner = new SobolRunner(x1, x2, numBoot, confidenceLevel);
 
 			// get design
 			Design design = runner.getX();
 
 			// run simulator using design
 			ProcessEvaluationResult processResult = ProcessEvaluator.evaluate(serviceURL, processIdentifier, inputs, outputs, design);
 
 			// generate results
 			List<SobolOutputResult> results = new ArrayList<SobolOutputResult>();
 			for (Output output : outputs) {
 				try {
 					// set y, triggers sa
 					runner.setY(processResult, output.getIdentifier());
 	
 					// get sa results
 					results.add(runner.getResult(plot));
 				}
 				catch (REngineException e) {
 					// FIXME: repeated below, plus useful for all R interaction
 					String message = (runner != null ? runner.getLastError() : null);
 					if (message == null) {
 						message = "Problem evaluating R expression.";
 					}
 					else {
 						message = "From R: " + message;
 					}
 					// very hacky
 			        BufferedImage img = new BufferedImage(500, 100, BufferedImage.TYPE_INT_ARGB);
 			        Graphics2D g2d = img.createGraphics();
 			        g2d.setPaint(Color.red);
 			        g2d.setFont(new Font("Serif", Font.BOLD, 20));
 			        FontMetrics fm = g2d.getFontMetrics();
 			        int x = 5;
 			        int y = fm.getHeight() + 5;
 			        g2d.drawString(message, x, y);
 			        g2d.dispose();
					results.add(new SobolOutputResult(output.getIdentifier(), new ArrayList<SobolInputResult>(), img));
 					
 					//throw new SobolException(message);
 				}
 			}
 			
 			return results;
 		}
 		catch (REngineException e) {
 			String message = (runner != null ? runner.getLastError() : null);
 			if (message == null) {
 				message = "Problem evaluating R expression.";
 			}
 			else {
 				message = "From R: " + message;
 			}
 			throw new SobolException(message);
 		}
 		catch (REXPMismatchException e) {
 			throw new SobolException("Error receiving data from R: " + e.getMessage());
 		}
 		catch (ConfigException e) {
 			throw new SobolException("Couldn't load Rserve config details.");
 		}
 		finally {
 			runner.done();
 		}
 	}
 
 	public static List<SobolOutputResult> run(Emulator emulator, boolean plot, int designSize, int numBoot, double confidenceLevel) throws DesignException, EmulatorEvaluatorException, SobolException {
 		// strip fixed inputs, even though they will be during the evaluation stage anyway
 		List<Input> variableInputs = new ArrayList<Input>();
 		for (Input input : emulator.getInputs()) {
 			if (input instanceof VariableInput) {
 				variableInputs.add(input);
 			}
 		}
 		
 		// need two designs
 		LHSDesign x1 = LHSDesign.create(variableInputs, designSize);
 		LHSDesign x2 = LHSDesign.create(variableInputs, designSize);
 
 		SobolRunner r = null;
 		try {
 			// create a sobol runner
 			r = new SobolRunner(x1, x2, numBoot, confidenceLevel);
 
 			// get design
 			Design design = r.getX();
 
 			// run emulator using design
 			EmulatorEvaluationResult emulatorResult = EmulatorEvaluator.run(emulator, design);
 
 			// tell sobol results
 			// emulators only have one output for now
 			r.setY(emulatorResult);
 
 			// get sa results
 			SobolOutputResult result = r.getResult(plot);
 			return Arrays.asList(new SobolOutputResult[] { result });
 		}
 		catch (REngineException e) {
 			String message = (r != null ? r.getLastError() : null);
 			if (message == null) {
 				message = "Problem evaluating R expression.";
 			}
 			else {
 				message = "From R: " + message;
 			}
 			throw new SobolException(message);
 		}
 		catch (REXPMismatchException e) {
 			throw new SobolException("Error receiving data from R: " + e.getMessage());
 		}
 		catch (ConfigException e) {
 			throw new SobolException("Couldn't load Rserve config details.");
 		}
 		finally {
 			r.done();
 		}
 	}
 
 }

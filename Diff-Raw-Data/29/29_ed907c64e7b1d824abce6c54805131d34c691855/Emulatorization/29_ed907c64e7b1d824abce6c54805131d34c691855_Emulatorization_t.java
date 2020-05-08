 package org.uncertweb.et;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.rosuda.REngine.Rserve.RConnection;
 import org.rosuda.REngine.Rserve.RserveException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.uncertweb.et.description.ProcessDescription;
 import org.uncertweb.et.description.ServiceDescription;
 import org.uncertweb.et.description.parser.ServiceDescriptionParser;
 import org.uncertweb.et.design.Design;
 import org.uncertweb.et.design.LHSDesign;
 import org.uncertweb.et.emulator.EmulatorEvaluationResult;
 import org.uncertweb.et.emulator.EmulatorEvaluator;
 import org.uncertweb.et.learning.Learning;
 import org.uncertweb.et.learning.LearningResult;
 import org.uncertweb.et.process.ProcessEvaluationResult;
 import org.uncertweb.et.process.ProcessEvaluator;
 import org.uncertweb.et.request.DesignRequest;
 import org.uncertweb.et.request.EvaluateEmulatorRequest;
 import org.uncertweb.et.request.EvaluateProcessRequest;
 import org.uncertweb.et.request.GetProcessDescriptionRequest;
 import org.uncertweb.et.request.GetProcessIdentifiersRequest;
 import org.uncertweb.et.request.LearningRequest;
 import org.uncertweb.et.request.Request;
 import org.uncertweb.et.request.ScreeningRequest;
 import org.uncertweb.et.request.SensitivityRequest;
 import org.uncertweb.et.request.StatusRequest;
 import org.uncertweb.et.request.ValidationRequest;
 import org.uncertweb.et.response.DesignResponse;
 import org.uncertweb.et.response.EvaluateEmulatorResponse;
 import org.uncertweb.et.response.EvaluateProcessResponse;
 import org.uncertweb.et.response.GetProcessDescriptionResponse;
 import org.uncertweb.et.response.GetProcessIdentifiersResponse;
 import org.uncertweb.et.response.LearningResponse;
 import org.uncertweb.et.response.Response;
 import org.uncertweb.et.response.ScreeningResponse;
 import org.uncertweb.et.response.SensitivityResponse;
 import org.uncertweb.et.response.StatusResponse;
 import org.uncertweb.et.response.ValidationResponse;
 import org.uncertweb.et.screening.Screening;
 import org.uncertweb.et.sensitivity.AnalysisOutputResult;
 import org.uncertweb.et.sensitivity.fast.Fast;
 import org.uncertweb.et.sensitivity.sobol.Sobol;
 import org.uncertweb.et.validation.Validator;
 import org.uncertweb.et.validation.ValidatorResult;
 import org.uncertweb.matlab.MLException;
 import org.uncertweb.matlab.MLRequest;
 import org.uncertweb.matlab.MLResult;
 
 public class Emulatorization {
 
 	private static final Logger logger = LoggerFactory.getLogger(Emulatorization.class);
 
 	private Emulatorization() {
 
 	}
 
 	// TODO: request object validation.
 	// TODO: if no outputs are passed to screening you get null pointer
 	// handler: pass request objects -> delegates to correct classes -> returns response
 	public static Response process(Request request) throws Exception {
 		try {
 			Response response;
 			if (request instanceof GetProcessIdentifiersRequest) {
 				GetProcessIdentifiersRequest gpiRequest = (GetProcessIdentifiersRequest) request;
 
 				ServiceDescription description = ServiceDescriptionParser.parse(gpiRequest.getServiceURL());
 				List<String> processIdentifiers = description.getProcessIdentifiers();
 
 				response = new GetProcessIdentifiersResponse(processIdentifiers);
 			}
 			else if (request instanceof GetProcessDescriptionRequest) {
 				GetProcessDescriptionRequest gpdRequest = (GetProcessDescriptionRequest) request;
 
 				ServiceDescription description = ServiceDescriptionParser.parse(gpdRequest.getServiceURL());
 				ProcessDescription processDescription = description.getProcessDescription(gpdRequest.getProcessIdentifier());
 
 				// FIXME: will return blank response if unsupported data type, should be an exception
 
 				response = new GetProcessDescriptionResponse(processDescription);
 			}
 			else if (request instanceof ScreeningRequest) {				
 				ScreeningRequest sRequest = (ScreeningRequest) request;
 
 				int numTraj = sRequest.getNumTrajectories();
 				int discretisationLevel = sRequest.getDiscretisationLevel();
 
 				Screening screening;
				if (numTraj == 0 && discretisationLevel == 0) {
 					screening = new Screening(sRequest.getServiceURL(), sRequest.getProcessIdentifier(), sRequest.getInputs(), sRequest.getOutputs());
 				}
 				else {
					screening = new Screening(sRequest.getServiceURL(), sRequest.getProcessIdentifier(), sRequest.getInputs(), sRequest.getOutputs(), numTraj, discretisationLevel);
 				}
 
 				response = new ScreeningResponse(screening.run());
 			}
 			else if (request instanceof DesignRequest) {
 				DesignRequest dRequest = (DesignRequest) request;
 
 				Design design = LHSDesign.create(dRequest.getInputs(), dRequest.getSize());
 
 				response = new DesignResponse(design);
 			}
 			else if (request instanceof EvaluateProcessRequest) {
 				EvaluateProcessRequest eRequest = (EvaluateProcessRequest) request;
 
 				ProcessEvaluationResult result = ProcessEvaluator.evaluate(eRequest.getServiceURL(), eRequest.getProcessIdentifier(), eRequest.getInputs(), eRequest.getOutputs(), eRequest.getDesign());
 
 				return new EvaluateProcessResponse(result);
 			}
 			else if (request instanceof LearningRequest) {
 				LearningRequest lRequest = (LearningRequest) request;
 
 				LearningResult result = Learning.learn(lRequest.getDesign(), lRequest.getEvaluationResult(), lRequest.getSelectedOutputIdentifier(), lRequest.getCovarianceFunction(), lRequest.getLengthScaleMultiplier(), lRequest.getNuggetVariance(), lRequest.getMeanFunction(), lRequest.isNormalisation());
 
 				return new LearningResponse(result);
 			}
 			else if (request instanceof ValidationRequest) {
 				ValidationRequest vRequest = (ValidationRequest) request;
 				
 				ValidatorResult result;
 				if (vRequest.getDesign() != null) {
 					result = Validator.validate(vRequest.getEmulator(), vRequest.getDesign(), vRequest.getEvaluationResult());
 				}
 				else {
 					result = Validator.validate(vRequest.getServiceURL(), vRequest.getProcessIdentifier(), vRequest.getInputs(), vRequest.getOutputs(), vRequest.getEmulator(), vRequest.getDesignSize());
 				}
 
 				return new ValidationResponse(result);
 			}
 			else if (request instanceof EvaluateEmulatorRequest) {
 				EvaluateEmulatorRequest eRequest = (EvaluateEmulatorRequest) request;
 
 				EmulatorEvaluationResult result = EmulatorEvaluator.run(eRequest.getEmulator(), eRequest.getDesign());
 
 				return new EvaluateEmulatorResponse(result);
 			}
 			else if (request instanceof SensitivityRequest) {
 				SensitivityRequest sRequest = (SensitivityRequest) request;
 
 				List<AnalysisOutputResult> results;
 				if (sRequest.getMethod().equals("sobol")) {
 					if (sRequest.getEmulator() != null) {
 						results = Sobol.run(sRequest.getEmulator(), sRequest.isPlot(), sRequest.getDesignSize(), sRequest.getNumBoot(), sRequest.getConfidenceLevel());
 					}
 					else {
 						results = Sobol.run(sRequest.getInputs(), sRequest.getOutputs(), sRequest.getServiceURL(), sRequest.getProcessIdentifier(), sRequest.isPlot(), sRequest.getDesignSize(), sRequest.getNumBoot(), sRequest.getConfidenceLevel());
 					}
 				}
 				else {
 					if (sRequest.getEmulator() != null) {
 						results = Fast.run(sRequest.getEmulator(), sRequest.isPlot(), sRequest.getDesignSize());
 					}
 					else {
 						results = Fast.run(sRequest.getInputs(), sRequest.getOutputs(), sRequest.getServiceURL(), sRequest.getProcessIdentifier(), sRequest.isPlot(), sRequest.getDesignSize());
 					}	
 				}
 
 				return new SensitivityResponse(results);
 			}
 			else if (request instanceof StatusRequest) {
 				boolean matlabOK = false;
 				String matlabMessage;
 				try {
 					MLRequest mlRequest = new MLRequest("matlab_version", 1);
 					MLResult result = MATLAB.sendRequest(mlRequest);
 					matlabOK = true;
 					//result.getResult(0).getAsString().getString()
 					matlabMessage =  "Ready";
 				}
 				catch (IOException e) {
 					matlabMessage = "Couldn't connect to MATLAB";
 				}
 				catch (MLException e) {
 					matlabMessage = "Couldn't get version information from MATLAB";
 				}
 
 				boolean rserveOK = false;
 				String rserveMessage;
 				RConnection c = null;
 				try {
 					c = R.getConnection();
 					rserveOK = true;
 					//c.eval("R.Version()$version.string").asString()
 					rserveMessage = "Ready";
 				}
 				catch (RserveException e) {
 					rserveMessage = "Couldn't connect to R";
 				}
 				finally {
 					if (c != null) {
 						c.close();
 					}
 				}
 
 				return new StatusResponse("Ready", matlabOK, matlabMessage, rserveOK, rserveMessage);
 			}
 			else {
 				// shouldn't ever happen
 				response = null;
 			}
 
 			return response;
 		}
 		catch (Exception e) {
 			// log it first
 			logger.error(e.getMessage(), e);
 
 			// then throw
 			throw e;
 		}
 	}
 
 }

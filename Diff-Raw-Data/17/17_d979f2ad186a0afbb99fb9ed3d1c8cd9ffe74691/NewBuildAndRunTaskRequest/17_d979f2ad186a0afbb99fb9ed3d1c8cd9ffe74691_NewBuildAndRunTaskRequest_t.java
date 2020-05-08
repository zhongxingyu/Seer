 package icloude.frontend_backend.requests;
 
 /**
  * @author DimaTWL This class describes 'Build project' request from protocol.
  * 
  */
 public class NewBuildAndRunTaskRequest extends BaseRequest {
 	private String projectID;
 	private String compileParameters;
	private String entryPointID;
 	private String inputData;
 	
 
 	private NewBuildAndRunTaskRequest() {
 	}
 
 
 	/**
 	 * @return the projectID
 	 */
 	public String getProjectID() {
 		return projectID;
 	}
 
 
 	/**
 	 * @return the compileParameters
 	 */
 	public String getCompileParameters() {
 		return compileParameters;
 	}
 
 
 	/**
 	 * @return the entryPoindID
 	 */
	public String getEntryPointID() {
		return entryPointID;
 	}
 
 
 	/**
 	 * @return the inputData
 	 */
 	public String getInputData() {
 		return inputData;
 	}
 
 
 	/**
 	 * @param protocolVersion
 	 * @param requestID
 	 * @param requestType
 	 * @param userID
 	 * @param projectID
 	 * @param compileParameters
 	 * @param entryPoindID
 	 * @param inputData
 	 */
 	public NewBuildAndRunTaskRequest(Integer protocolVersion, String requestID,
 			String requestType, String userID, String projectID,
 			String compileParameters, String entryPoindID, String inputData) {
 		super(protocolVersion, requestID, requestType, userID);
 		this.projectID = projectID;
 		this.compileParameters = compileParameters;
		this.entryPointID = entryPoindID;
 		this.inputData = inputData;
 	}
 
 
 
 
 }

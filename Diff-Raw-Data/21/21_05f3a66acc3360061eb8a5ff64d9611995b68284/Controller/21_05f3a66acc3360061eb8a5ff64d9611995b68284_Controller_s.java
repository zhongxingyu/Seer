 package servertester.controllers;
 
 import java.util.*;
 
 import client.server_communicator.HTTP;
 import client.server_communicator.ReqRes;
 
 import servertester.views.*;
 
 public class Controller implements IController {
 
 	private IView _view;
 	
 	public Controller() {
 		return;
 	}
 	
 	public IView getView() {
 		return _view;
 	}
 	
 	public void setView(IView value) {
 		_view = value;
 	}
 	
 	// IController methods
 	//
 	
 	@Override
 	public void initialize() {
 		getView().setHost("localhost");
 		getView().setPort("39640");
 		operationSelected();
 	}
 
 	@Override
 	public void operationSelected() {
 		ArrayList<String> paramNames = new ArrayList<String>();
 		paramNames.add("User");
 		paramNames.add("Password");
 		
 		switch (getView().getOperation()) {
 		case VALIDATE_USER:
 			break;
 		case GET_PROJECTS:
 			break;
 		case GET_SAMPLE_IMAGE:
 			paramNames.add("Project");
 			break;
 		case DOWNLOAD_BATCH:
 			paramNames.add("Project");
 			break;
 		case GET_FIELDS:
 			paramNames.add("Project");
 			break;
 		case SUBMIT_BATCH:
 			paramNames.add("Batch");
 			paramNames.add("Record Values");
 			break;
 		case SEARCH:
 			paramNames.add("Fields");
 			paramNames.add("Search Values");
 			break;
 		default:
 			assert false;
 			break;
 		}
 		
 		getView().setRequest("");
 		getView().setResponse("");
 		getView().setParameterNames(paramNames.toArray(new String[paramNames.size()]));
 	}
 
 	@Override
 	public void executeOperation() {
 		switch (getView().getOperation()) {
 		case VALIDATE_USER:
 			validateUser();
 			break;
 		case GET_PROJECTS:
 			getProjects();
 			break;
 		case GET_SAMPLE_IMAGE:
 			getSampleImage();
 			break;
 		case DOWNLOAD_BATCH:
 			downloadBatch();
 			break;
 		case GET_FIELDS:
 			getFields();
 			break;
 		case SUBMIT_BATCH:
 			submitBatch();
 			break;
 		case SEARCH:
 			search();
 			break;
 		default:
 			assert false;
 			break;
 		}
 	}
 	
 	private HTTP client;
 	private String[] guiParams;
 	private ReqRes reqres;
 	
 	private void getReady() {
 		guiParams = getView().getParameterValues();
 		client = new HTTP(getView().getHost(), Integer.parseInt(getView().getPort()));
 	}
 	
 	private void displayResults(ReqRes reqres) {
 		getView().setRequest(reqres.request);
 		getView().setResponse(reqres.response.getStatus() + " " + reqres.response.getClientResponseStatus() + "\n" + reqres.body);
 	}
 	
 	private void validateUser() {
 		getReady();
 		
 		Map<String, String> params = new HashMap<String, String>() {{
 			put("username", guiParams[0]);
 			put("password", guiParams[1]);
 		}};
 		
 		String endpoint = "user/validate";
 		ReqRes reqres = client.post(endpoint, params);
 		
 		displayResults(reqres);
 	}
 	
 	private void getProjects() {
 		getReady();
 		
 		Map<String, String> params = new HashMap<String, String>() {{
 			put("username", guiParams[0]);
 			put("password", guiParams[1]);
 		}};
 		
 		String endpoint = "project";
 		ReqRes reqres = client.post(endpoint, params);
 		
 		displayResults(reqres);
 	}
 	
 	private void getSampleImage() {
 		getReady();
 		
 		Map<String, String> params = new HashMap<String, String>() {{
 			put("username", guiParams[0]);
 			put("password", guiParams[1]);
 			put("projectid", guiParams[2]);
 		}};
 		
 		String endpoint = "batch/sample";
 		ReqRes reqres = client.post(endpoint, params);
 		
 		displayResults(reqres);
 	}
 	
 	private void downloadBatch() {
 		getReady();
 		
 		Map<String, String> params = new HashMap<String, String>() {{
 			put("username", guiParams[0]);
 			put("password", guiParams[1]);
 			put("projectid", guiParams[2]);
 		}};
 		
 		String endpoint = "batch/get";
 		ReqRes reqres = client.post(endpoint, params);
 		
 		displayResults(reqres);
 	}
 	
 	private void getFields() {
 		getReady();
 		
 		Map<String, String> params = new HashMap<String, String>() {{
 			put("username", guiParams[0]);
 			put("password", guiParams[1]);
 			put("projectid", guiParams[2]);
 		}};
 		
 		String endpoint = "fields";
 		ReqRes reqres = client.post(endpoint, params);
 		
 		displayResults(reqres);
 	}
 	
 	private void submitBatch() {
 		getReady();
 		
 		Map<String, String> params = new HashMap<String, String>() {{
 			put("username", guiParams[0]);
 			put("password", guiParams[1]);
 			put("batch", guiParams[2]);
 			put("record_values", guiParams[2]);
 		}};
 		
 		String endpoint = "batch/submit";
 		ReqRes reqres = client.post(endpoint, params);
 		
 		displayResults(reqres);
 	}
 	
 	private void search() {
 		getReady();
 		
 		Map<String, String> params = new HashMap<String, String>() {{
 			put("username", guiParams[0]);
 			put("password", guiParams[1]);
 			put("fields", guiParams[2]);
			put("search_values", guiParams[2]);
 		}};
 		
 		String endpoint = "batch/search";
 		ReqRes reqres = client.post(endpoint, params);
 		
 		displayResults(reqres);
 	}
 
 }
 

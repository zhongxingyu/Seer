 package org.innobuilt.wicket.rest.jsonrpc;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.model.Model;
 import org.innobuilt.wicket.rest.JsonWebServicePage;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import com.google.gson.Gson;
 
 //Must use post
 public class JsonRPCServicePage extends JsonWebServicePage {
 
 	private static final String VERSION = "2.0";
 	private static final String UNSUPPORTED_HTTP_METHOD = "Unsupported HTTP Method: ";
 	private static final Integer UNSUPPORTED_HTTP_METHOD_CODE = 1;
 	private static final String UNSUPPORTED_VERSION = "Unsupported Version: ";
 	private static final Integer UNSUPPORTED_VERSION_CODE = 2;
 	private static final String UNSUPPORTED_REQUEST = "Problem reading request (http://json-rpc.org/wiki/specification)";
 	private static final Integer UNSUPPORTED_REQUEST_CODE = 3;
 	private static final String UNSUPPORTED_METHOD = "Unsupported method: ";
 	private static final int UNSUPPORTED_METHOD_CODE = 4;
 	private static final String NULL_ID = "id cannot be null";
 	private static final Integer NULL_ID_CODE = 5;
 	private static final String PARSE_ERROR = "Error parsing RPC request";
 	private static final Integer PARSE_ERROR_CODE = 6;
 	private static final String METHOD_EXCEPTION = "The remote method threw an exception: ";
 	private static final Integer METHOD_EXCEPTION_CODE = 7;
	private transient List<Method> methods = new ArrayList<Method>();
 
 	public JsonRPCServicePage(PageParameters params) {
 		super(params);
 		for (Method method : this.getClass().getMethods()) {
 			if (method.getAnnotation(Expose.class) != null) {
 				methods.add(method);
 			}
 		}
 	}
 
 	private Error getUnsupportedMethodError(String method) {
 		return new Error(UNSUPPORTED_HTTP_METHOD_CODE, UNSUPPORTED_HTTP_METHOD + method);
 	}
 
 	@Override
 	protected final void setModelFromBody(String body) {
 		Gson gson = getBuilder().create();
 		Request request = gson.fromJson(body, Request.class);
 		setDefaultModel(new Model((Serializable) request));
 
 		// if body is no good throw an error
 		if (request == null) {
 			request = new Request();
 			setDefaultModel(new Model((Serializable) request));
 			request.setError(new Error(UNSUPPORTED_REQUEST_CODE, UNSUPPORTED_REQUEST));
 			return;
 		}
 		// if no id is given, throw an error
 		if (request.getId() == null) {
 			request.setError(new Error(NULL_ID_CODE, NULL_ID));
 			return;
 		}
 		// If version is no good throw error
 		if (request.getJsonrpc() == null || !request.getJsonrpc().equals(VERSION)) {
 			request.setError(new Error(UNSUPPORTED_VERSION_CODE, UNSUPPORTED_VERSION + request.getJsonrpc()));
 			return;
 		}
 		// If no method throw error
 		if (request.getMethod() == null) {
 			request.setError(new Error(UNSUPPORTED_METHOD_CODE, UNSUPPORTED_METHOD + null));
 			return;
 		}
 
 		// Get the method name and look it up
 		Method method = getMethod(request);
 		if (method == null) {
 			request.setError(new Error(UNSUPPORTED_METHOD_CODE, UNSUPPORTED_METHOD + request.getMethod()));
 			return;
 		} else {
 			request.setMethodObject(method);
 		}
 		
 		// put the params in their own array so we can deserialize individually
 		JSONParser parser = new JSONParser();
 		try {
 			JSONObject json = (JSONObject) parser.parse(body);
 
 			if(json.get("params") != null) {
 				String paramString = json.get("params").toString();
 				
 				JSONArray reqParms = (JSONArray) parser.parse(paramString);
 				Class[] pTypes = request.getMethodObject().getParameterTypes();
 				
 				if(reqParms.size() != pTypes.length) throw new Exception("Wrong number of arguments.");
 				
 				List parms = new ArrayList();
 				int i = 0;
 				for (Class clazz:pTypes) {
 					String parm = reqParms.get(i).toString();
 					System.out.println("parm=" + parm);
 					parms.add(gson.fromJson(parm, clazz));
 					i++;
 				}
 				
 				request.setParams(parms.toArray());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			request.setError(new Error(PARSE_ERROR_CODE, PARSE_ERROR + e.getMessage()));
 			return;
 		}
 		// TODO http://code.google.com/p/json-simple/wiki/DecodingExamples
 	}
 
 	private Request getJsonRpcRequest() {
 		return (Request) getDefaultModelObject();
 	}
 
 	private void errorResponse(Error error) {
 		Response response = new Response();
 		response.setError(error);
 		if (getJsonRpcRequest() != null)
 			response.setId(getJsonRpcRequest().getId());
 		response.setJsonrpc(VERSION);
 		setDefaultModel(new Model(response));
 	}
 
 	@Override
 	public void doDelete(PageParameters params) {
 		errorResponse(getUnsupportedMethodError(DELETE));
 	}
 
 	@Override
 	public void doGet(PageParameters params) {
 		errorResponse(getUnsupportedMethodError(GET));
 	}
 
 	@Override
 	public void doPut(PageParameters params) {
 		errorResponse(getUnsupportedMethodError(PUT));
 	}
 
 	@Override
 	public void doPost(PageParameters params) {
 		Request request = getJsonRpcRequest();
 		
 		if(request == null) {
 			errorResponse(new Error(UNSUPPORTED_REQUEST_CODE, UNSUPPORTED_REQUEST));
 			return;
 		}
 		//If we encountered errors during parsing, error will be set
 		if(request.getError() != null) {
 			errorResponse(request.getError());
 			return;
 		}
 
 		Method method = request.getMethodObject();
 
 		// Run the method
 			//create a response object and put the result object in it
 			try {
 				Response response = new Response();
 				Serializable result = (Serializable)method.invoke(this, request.getParams());
 				response.setResult(result);
 				response.setId(request.getId());
 				response.setJsonrpc(VERSION);
 				setDefaultModel(new Model(response));
 			} catch (IllegalArgumentException e) {
 				errorResponse(new Error(UNSUPPORTED_METHOD_CODE, UNSUPPORTED_METHOD + e.getMessage()));
 			} catch (IllegalAccessException e) {
 				errorResponse(new Error(UNSUPPORTED_METHOD_CODE, UNSUPPORTED_METHOD + e.getMessage()));
 			} catch (InvocationTargetException e) {
 				errorResponse(new Error(UNSUPPORTED_METHOD_CODE, UNSUPPORTED_METHOD + e.getMessage()));
 			} catch (Exception e) {
 				errorResponse(new Error(METHOD_EXCEPTION_CODE, METHOD_EXCEPTION + e.getMessage()));
 			}
  
 
 	}
 
 	// TODO unless we add param types to annotation, we can't have two methods
 	// with the same name
 	public Method getMethod(Request request) {
 		for (Method method : this.methods) {
 			if (request.getMethod().equals(method.getName())) {
 				return method;
 			}
 		}
 
 		return null;
 	}
 
 	@Expose
 	public Map<String, String[]> getMethods() {
 		Map map = new HashMap<String, String[]>();
 		for (Method method : methods) {
 			String[] params = new String[method.getParameterTypes().length];
 			int i = 0;
 			for (Class clazz : method.getParameterTypes()) {
 				params[i] = clazz.getName();
 				i++;
 			}
 			map.put(method.getName(), params);
 		}
 		return map;
 	}
 }

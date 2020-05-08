 package com.redhat.qe.reportengineforwarder;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import spark.Request;
 import spark.Response;
 import spark.Route;
 import spark.Spark;
 
 import com.redhat.reportengine.client.RemoteAPI;
 
 public class Server {
 	public static class ResponseWrapper {
 
 		private Response response;
 		private int code;
 		private String body;
 
 		/**
 		 * @param response
 		 */
 		public ResponseWrapper(Response response, int code, String body) {
 			super();
 			this.code = code;
 			this.body = body;
 			this.response = response;
 			this.response.status(code);
 			this.response.body("body");
 		}
 
 		/**
 		 * @return the response
 		 */
 		public Response getResponse() {
 			return response;
 		}
 
 		/**
 		 * @param response
 		 *            the response to set
 		 */
 		public void setResponse(Response response) {
 			this.response = response;
 		}
 
 		public String toString() {
 			return String.format("%s: %s",code, body);
 		}
 	}
 
 	public static abstract class ActiveReportRoute extends Route {
 
 		private RemoteAPI api;
 
 		protected ActiveReportRoute(String path, RemoteAPI api) {
 			super(path);
 			this.api = api;
 		}
 
 		public abstract Object handleApi(Request request, Response response) throws Exception;
 
 		@Override
 		public Object handle(Request request, Response response) {
 			if (api.isClientConfigurationSuccess()) {
 				return runApiHandle(request, response);
 			} else {
 				return new ResponseWrapper(response, 500, "api is not connected");
 			}
 		}
 
 		/**
 		 * @param request
 		 * @param response
 		 */
 		private Object runApiHandle(Request request, Response response) {
 			try {
 				return handleApi(request, response);
 			} catch (Exception e) {
 				StringWriter errors = new StringWriter();
 				e.printStackTrace(new PrintWriter(errors));
 				return new ResponseWrapper(response,500, errors.toString());
 			}
 		}
 
 	}
 
 	private static String getNameParam(Request request, Response response) {
 		String name = request.queryParams("name");
 		if (name == null || name.isEmpty()) {
 			throw new RuntimeException("name param not given");
 		}
 		return name;
 	}
 
 
 	public static void main(String[] args) {
 		
 		final RemoteAPI reportEngine = new RemoteAPI();
 		
 		if(args.length > 0){
 			Integer.parseInt(args[0]);
 		}else{			
 			Spark.setPort(27514);
 		}
 
 		Spark.get(new Route("/report/create") {
 			@Override
 			public Object handle(Request request, Response response) {
 
 				startReport(reportEngine, request, response);
 				if (reportEngine.isClientConfigurationSuccess()){
 					return new ResponseWrapper(response, 200, "report started");
 				}else {
 					return new ResponseWrapper(response, 500, "report could not be created");
 				}
 				
 				
 			}
 
 			private void startReport(final RemoteAPI reportEngine, Request request, Response response) {
 				try {
 					reportEngine.initClient(request.queryParams("name"));
 				} catch (Exception e) {
					e.printStackTrace();
 				}
 			}
 		});
 
 		Spark.get(new ActiveReportRoute("/testgroup/create", reportEngine) {
 
 			@Override
 			public Object handleApi(Request request, Response response) throws Exception {
 				return createTestGroup(reportEngine, response);
 			}
 			
 			private ResponseWrapper createTestGroup(final RemoteAPI reportEngine, Response response) {
 				try {
 					reportEngine.insertTestGroup("tests");
 				} catch (Exception e) {
 					return new ResponseWrapper(response, 500, "test group not be created");
 				}
 				return new ResponseWrapper(response, 500, "test group created");
 			}
 		});
 		Spark.get(new ActiveReportRoute("/report/finish", reportEngine) {
 
 			@Override
 			public Object handleApi(Request request, Response response) throws Exception {
 				reportEngine.updateTestSuite("Completed", "TODO buildverison empty");
 				return new ResponseWrapper(response, 200, "report completed");
 			}
 		});
 
 		Spark.get(new ActiveReportRoute("/testcase/start", reportEngine) {
 
 			@Override
 			public Object handleApi(Request request, Response response) throws Exception {
 				String name = getNameParam(request, response);
 				reportEngine.insertTestCase(name, "Running");
 				return new ResponseWrapper(response, 200, "reporting that test case as running");
 			}
 
 		});
 		
 		Spark.get(new ActiveReportRoute("/testcase/pass", reportEngine) {
 
 			@Override
 			public Object handleApi(Request request, Response response) throws Exception {
 				reportEngine.updateTestCase("Passed");
 				return new ResponseWrapper(response, 200, "OK");
 			}
 
 		});
 		Spark.get(new ActiveReportRoute("/testcase/fail", reportEngine) {
 
 			@Override
 			public Object handleApi(Request request, Response response) throws Exception {
 				reportEngine.updateTestCase("Failed",request.queryParams("message"));
 				return new ResponseWrapper(response, 200, "OK");
 			}
 
 		});
 
 	}
 }

 package org.ukiuni.lighthttpserver.request;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.ukiuni.lighthttpserver.response.Response;
 import org.ukiuni.lighthttpserver.response.Response404;
 import org.ukiuni.lighthttpserver.util.FileUtil;
 
 public class DefaultHandler extends Handler {
 	private PrintStream errorOut = System.err;
 	private List<Request> requestLog;
 
 	public boolean isLogging() {
 		return null != requestLog;
 	}
 
 	public DefaultHandler requestLoggingOn() {
 		if (null == requestLog) {
 			requestLog = new LinkedList<Request>();
 		}
 		return this;
 	}
 
 	public List<Request> getRequestLog() {
 		return requestLog;
 	}
 
 	public DefaultHandler clearLog() {
 		if (null != requestLog) {
 			requestLog.clear();
 		}
 		return this;
 	}
 
 	public DefaultHandler requestLoggingOff() {
 		if (null != requestLog) {
 			clearLog();
 			requestLog = null;
 		}
 		return this;
 	}
 
 	public PrintStream getErrorOut() {
 		return errorOut;
 	}
 
 	public void setErrorOut(PrintStream errorOut) {
 		this.errorOut = errorOut;
 	}
 
 	private Map<String, ReturnValue> returnMap = new HashMap<String, ReturnValue>();
 	{
 		returnMap.put("/", new ReturnValue(200, "<html><body><H1>Hello! This is LightHttpServer</H1></body><html>", "text/html; charset=UTF-8", "UTF-8"));
 	}
 
 	public DefaultHandler addResponse(String path, String value) {
 		return addResponse(path, 200, value);
 	}
 
 	public DefaultHandler addResponse(String path, int responseCode, String value) {
 		return addResponse(path, responseCode, value, "text/html; charset=UTF-8", "UTF-8");
 	}
 
 	public DefaultHandler addResponse(String path, String value, String contentType) {
 		return addResponse(path, value, contentType, "UTF-8");
 	}
 
 	public DefaultHandler addResponse(String path, String value, String contentType, String encode) {
 		returnMap.put(path, new ReturnValue(200, value, contentType, encode));
 		return this;
 	}
 
 	public DefaultHandler addResponse(String path, int responseCode, String value, String contentType, String encode) {
 		returnMap.put(path, new ReturnValue(responseCode, value, contentType, encode));
 		return this;
 	}
 
 	public DefaultHandler addResponse(String path, File file) {
 		return this.addResponse(path, 200, file);
 	}
 
 	public DefaultHandler addResponse(String path, int responseCode, File file) {
 		return this.addResponse(path, responseCode, file, FileUtil.getMimeType(file));
 	}
 
 	public DefaultHandler addResponse(String path, int responseCode, File file, String contentType) {
 		returnMap.put(path, new ReturnValue(responseCode, file, contentType));
 		return this;
 	}
 
 	public DefaultHandler addResponse(String path, Response response) {
 		returnMap.put(path, new ReturnValue(response));
 		return this;
 	}
 
 	@Override
 	public Response onRequest(Request request) {
 		if (null != this.requestLog) {
 			this.requestLog.add(request);
 		}
 		ReturnValue returnValue = returnMap.get(request.getPath());
 		if (null != returnValue) {
 			return new ReturnValueSettableResponse(returnValue, request);
 		}
 		return new Response404();
 	}
 
 	private class ReturnValueSettableResponse extends Response {
 		ReturnValue returnValue;
 		Request request;
 
 		public ReturnValueSettableResponse(ReturnValue returnValue, Request request) {
 			this.returnValue = returnValue;
 			this.request = request;
 		}
 
 		@Override
 		public void onResponse(OutputStream out) throws Throwable {
 			try {
 				if (null != returnValue.response) {
 					returnValue.response.setRequest(request);
 					returnValue.response.onResponse(out);
 				} else if (returnValue.file != null) {
 					write(out, returnValue.responseCode, returnValue.file, returnValue.contentType);
 				} else {
 					write(out, returnValue.responseCode, returnValue.value, returnValue.contentType, returnValue.encode);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private class ReturnValue {
 		public ReturnValue(int responseCode, String value, String contentType, String encode) {
 			this.responseCode = responseCode;
 			this.value = value;
 			this.contentType = contentType;
 			this.encode = encode;
 		}
 
 		public ReturnValue(int responseCode, File file, String contentType) {
 			this.responseCode = responseCode;
 			this.contentType = contentType;
 			this.file = file;
 		}
 
 		public ReturnValue(Response response) {
 			this.response = response;
 		}
 
 		public Response response;
 		public int responseCode;
 		public File file;
 		public String contentType;
 		public String value;
 		public String encode;
 	}
 
 	@Override
 	public void onException(Throwable e) {
 		e.printStackTrace(errorOut);
 	}
 }

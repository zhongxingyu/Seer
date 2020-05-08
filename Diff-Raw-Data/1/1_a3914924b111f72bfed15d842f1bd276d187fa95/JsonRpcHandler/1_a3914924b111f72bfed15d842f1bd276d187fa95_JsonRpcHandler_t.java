 /*
  * DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  *                    Version 2, December 2004
  * 
  * Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>
  * 
  * Everyone is permitted to copy and distribute verbatim or modified
  * copies of this license document, and changing it is allowed as long
  * as the name is changed.
  * 
  *            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  *   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
  * 
  * 0. You just DO WHAT THE FUCK YOU WANT TO.
  */
 
 package com.theisleoffavalon.mcmanager.network.handler;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import com.theisleoffavalon.mcmanager.network.handler.jsonrpc.RpcMethod;
 import com.theisleoffavalon.mcmanager.network.handler.jsonrpc.RpcRequest;
 import com.theisleoffavalon.mcmanager.network.handler.jsonrpc.RpcResponse;
 import com.theisleoffavalon.mcmanager.network.handler.jsonrpc.RpcResponse.Error;
 import com.theisleoffavalon.mcmanager.network.handler.jsonrpc.RpcResponse.ErrorCode;
 import com.theisleoffavalon.mcmanager.util.LogHelper;
 
 /**
  * Handles JSON type requests that come in and returns
  * JSON in turn.
  * 
  * @author Cadyyan
  *
  */
 public class JsonRpcHandler extends AbstractHandler
 {
 	/**
 	 * The MIME type for JSON.
 	 */
 	public static final String JSON_CONTENT_TYPE = "application/json";
 	
 	/**
 	 * An entry in the method handler map.
 	 * 
 	 * @author Cadyyan
 	 *
 	 */
 	private static class MethodHandlerEntry
 	{
 		/**
 		 * The handler instance.
 		 */
 		public final Object handler;
 		
 		/**
 		 * The method that will handle the RPC method.
 		 */
 		public final Method method;
 		
 		/**
 		 * Creates a new method handler entry.
 		 * 
 		 * @param handler - the handler instance
 		 * @param method - the RPC handler method
 		 */
 		public MethodHandlerEntry(Object handler, Method method)
 		{
 			this.handler = handler;
 			this.method = method;
 		}
 	}
 	
 	/**
 	 * The parser to use for JSON.
 	 */
 	private JSONParser parser;
 	
 	/**
 	 * The mapping of RPC methods to method handlers.
 	 */
 	private Map<String, MethodHandlerEntry> methodHandlers;
 	
 	/**
 	 * Creates a new JSON based web request handler.
 	 */
 	public JsonRpcHandler()
 	{
 		super();
 		
 		parser = new JSONParser();
 		methodHandlers = new HashMap<String, MethodHandlerEntry>();
 		
 		addHandler(this);
 	}
 
 	@Override
 	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		Object rawRpcRequest;
 		
 		try
 		{
 			InputStream stream = request.getInputStream();
 			byte buf[] = new byte[stream.available()];
 			stream.read(buf);
 			String json = new String(buf);
 			rawRpcRequest = parser.parse(json);
 		}
 		catch(ParseException e)
 		{
 			LogHelper.warning("Received RPC request that contained invalid JSON.");
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 			baseRequest.setHandled(true);
 			parser.reset();
 			return;
 		}
 		
 		if(rawRpcRequest instanceof JSONArray)
 		{
 			// This should be a bundle of RPC requests so try to get them out.
 			JSONArray batchRequest = (JSONArray)rawRpcRequest;
 			JSONArray batchResponse = new JSONArray();
 			
 			for(Object object : batchRequest)
 			{
 				RpcRequest rpcRequest = convertRequest((JSONObject)object);
 				if(rpcRequest == null)
 				{
 					LogHelper.warning("Received RPC request that contained an invalid RPC request.");
 					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 					baseRequest.setHandled(true);
 					return;
 				}
 				
 				RpcResponse rpcResponse = handleRpcRequest(rpcRequest);
 				batchResponse.add(rpcResponse);
 			}
 			
 			Writer writer = response.getWriter();
 			batchResponse.writeJSONString(writer);
 			response.setStatus(HttpServletResponse.SC_OK);
 			baseRequest.setHandled(true);
 		}
 		else if(rawRpcRequest instanceof JSONObject)
 		{
 			// This should be a single RPC request.
 			RpcRequest rpcRequest = convertRequest((JSONObject)rawRpcRequest);
 			if(rpcRequest == null)
 			{
 				LogHelper.warning("Received RPC request that contained an invalid RPC request.");
 				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 				baseRequest.setHandled(true);
 				return;
 			}
 			
 			RpcResponse rpcResponse = handleRpcRequest(rpcRequest);
 			Writer writer = response.getWriter();
 			rpcResponse.writeJSONString(writer);
 			response.setStatus(HttpServletResponse.SC_OK);
 			baseRequest.setHandled(true);
 		}
 		else
 		{
 			LogHelper.warning("Received RPC request that contained the wrong type of JSON object.");
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 			baseRequest.setHandled(true);
 			return;
 		}
 	}
 	
 	/**
 	 * Adds a new RPC method handler.
 	 * 
 	 * @param handler - a handler instance
 	 */
 	public void addHandler(Object handler)
 	{
 		if(handler == null)
 			throw new NullPointerException("Handler cannot be null.");
 		
 		Class klass = handler.getClass();
 		Method methods[] = klass.getMethods();
 		for(Method m : methods)
 		{
 			RpcMethod rpcMethod = m.getAnnotation(RpcMethod.class);
 			if(rpcMethod == null)
 				continue;
 			
 			LogHelper.debug("Found RPC handler for " + rpcMethod.method());
 			
 			if(methodHandlers.containsKey(rpcMethod.method()))
 			{
 				LogHelper.warning("Duplicate RPC handler for  " + rpcMethod.method());
 				continue;
 			}
 			
 			methodHandlers.put(rpcMethod.method(), new MethodHandlerEntry(handler, m));
 		}
 	}
 	
 	/**
 	 * Handles RPC requests for the methods method.
 	 * 
 	 * @param request - the request
 	 * @param response - the response
 	 */
 	@RpcMethod(method = "getAllMethods", description = "Get's all available methods implemented by the server. This returns an array of strings with each string being a method name.")
 	public void handleRPCGetAllMethods(RpcRequest request, RpcResponse response)
 	{
 		Set<String> methods = methodHandlers.keySet();
 		JSONArray array = new JSONArray();
 		
 		array.addAll(methods);
 		response.addResult("methods", array);
 	}
 	
 	/**
 	 * Converts a JSON object that was given by a request to an RPC request.
 	 * 
 	 * @param jsonObj - the JSON request object
 	 * @return an RPC request object or <code>null</code> for failure
 	 */
 	private RpcRequest convertRequest(JSONObject jsonObj)
 	{
 		String method = (String)jsonObj.get("method");
 		String id = (String)jsonObj.get("id");
 		
 		RpcRequest request = new RpcRequest(method, (Map<String, Object>)jsonObj.get("params"), id);
 		
 		return request;
 	}
 	
 	/**
 	 * Handles an RPC request.
 	 * 
 	 * @param rpcRequest - the RPC request
 	 * @return an RPC response
 	 */
 	private RpcResponse handleRpcRequest(RpcRequest rpcRequest)
 	{
 		RpcResponse rpcResponse = rpcRequest.isNotification() ? null : new RpcResponse(rpcRequest);
 		String methodName = rpcRequest.getMethod();
 		if(!methodHandlers.containsKey(methodName))
 		{
 			LogHelper.warning("Received RPC request but there was no method handler to handle it (" + methodName + ")");
 			
 			if(rpcResponse != null)
 				rpcResponse.setError(new Error(ErrorCode.METHOD_NOT_FOUND, "That method is not implemented by the server.", null));
 		}
 		else
 		{
 			MethodHandlerEntry entry = methodHandlers.get(methodName);
 			try
 			{
 				entry.method.invoke(entry.handler, rpcRequest, rpcResponse);
 			}
 			catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
 			{
 				LogHelper.warning("Failure when handling RPC request.\n" + e.getMessage());
 				
 				if(rpcResponse != null)
 					rpcResponse.setError(new Error(ErrorCode.INTERNAL_ERROR, "An error occured when processing the request.", null));
 			}
 		}
 		
 		return rpcResponse;
 	}
 }

 // Copyright 2011, Google Inc. All Rights Reserved.
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 package swarm.server.transaction;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Writer;
 import java.lang.reflect.Method;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONException;
 
 import swarm.server.account.SqlAccountDatabase;
 import swarm.server.app.A_ServerJsonFactory;
 import swarm.server.data.blob.BlobManagerFactory;
 import swarm.server.session.SessionManager;
 import swarm.server.telemetry.TelemetryDatabase;
 import swarm.shared.app.A_App;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.json.A_JsonFactory;
 import swarm.shared.json.E_JsonKey;
 import swarm.shared.json.I_ReadsJson;
 import swarm.shared.json.I_JsonObject;
 import swarm.shared.json.JsonQuery;
 import swarm.shared.json.JsonHelper;
 import swarm.shared.reflection.I_Class;
 import swarm.shared.structs.OptHashMap;
 import swarm.shared.transaction.A_TransactionObject;
 import swarm.shared.transaction.CachePolicy;
 import swarm.shared.transaction.E_ReservedRequestPath;
 import swarm.shared.transaction.E_ResponseError;
 import swarm.shared.transaction.E_RequestPath;
 import swarm.shared.transaction.I_RequestPath;
 import swarm.shared.transaction.RequestPathManager;
 import swarm.shared.transaction.TransactionRequest;
 import swarm.shared.transaction.TransactionResponse;
 import swarm.shared.transaction.U_RequestBatch;
 
 public class ServerTransactionManager
 {
 	private static final Logger s_logger = Logger.getLogger(ServerTransactionManager.class.getName());
 	
 	private final HashMap<Integer, I_RequestHandler> m_handlers = new HashMap<Integer, I_RequestHandler>();
 	private final HashSet<Integer> m_debugResponseErrorPaths = new HashSet<Integer>();
 	private final ArrayList<I_DeferredRequestHandler> m_deferredHandlers = new ArrayList<I_DeferredRequestHandler>();
 	private final ArrayList<I_TransactionScopeListener> m_scopeListeners = new ArrayList<I_TransactionScopeListener>();
 	private final A_ServerJsonFactory m_jsonFactory;
 	private final boolean m_verboseJson;
 	private final RequestPathManager m_requestPathMngr;
 	private final int m_libServerVersion;
 	private final int m_appServerVersion;
 	
 	public ServerTransactionManager(A_ServerJsonFactory jsonFactory, RequestPathManager requestPathMngr, boolean verboseTransactions, int libServerVersion, int appServerVersion)
 	{
 		m_jsonFactory = jsonFactory;
 		m_requestPathMngr = requestPathMngr;		
 		m_verboseJson = verboseTransactions;
 		m_libServerVersion = libServerVersion;
 		m_appServerVersion = appServerVersion;
 		
 		m_requestPathMngr.register(E_ReservedRequestPath.values());
 	}
 	
 	public I_RequestHandler getRequestHandler(I_RequestPath path)
 	{
 		return m_handlers.get(path.getId());
 	}
 	
 	public void setRequestHandler(I_RequestHandler handler, I_RequestPath path)
 	{
 		m_handlers.put(path.getId(), handler);
 	}
 	
 	public void setDebugResponseError(I_RequestPath path)
 	{
 		m_debugResponseErrorPaths.add(path.getId());
 	}
 	
 	public void addDeferredHandler(I_DeferredRequestHandler handler)
 	{
 		m_deferredHandlers.add(handler);
 	}
 	
 	public void addScopeListener(I_TransactionScopeListener listener)
 	{
 		m_scopeListeners.add(listener);
 	}
 	
 	private TransactionResponse createEarlyOutResponse(E_ResponseError responseError)
 	{
 		TransactionResponse responseToReturn = new TransactionResponse(m_jsonFactory);
 		responseToReturn.setError(responseError);
 		
 		return responseToReturn;
 	}
 	
 	public TransactionResponse handleRequestFromClient(final Object nativeRequest, final Object nativeResponse, Object nativeContext, I_JsonObject requestJson, I_JsonObject responseJson_out)
 	{
 		return this.handleRequestFromClient(nativeRequest, nativeResponse, nativeContext, requestJson, responseJson_out, m_verboseJson);
 	}
 	
 	public TransactionResponse handleRequestFromClient(final Object nativeRequest, final Object nativeResponse, Object nativeContext, I_JsonObject requestJson, I_JsonObject responseJson_out, boolean verboseJson)
 	{
 		m_jsonFactory.startScope(verboseJson);
 		
 		TransactionResponse responseToReturn = null;
 		
 		//--- DRK > Just being anal and putting everything within a try.
 		try
 		{
 			//--- DRK > Early out for problems with request json.
 			if( requestJson == null )
 			{
 				responseToReturn = this.createEarlyOutResponse(E_ResponseError.REQUEST_READ_ERROR);
 				
				return responseToReturn; // hits finally block
 			}
 			
 			//--- DRK > Create a wrapper around the native request and see if there's a server version mismatch.
 			TransactionRequest wrappedRequest = new TransactionRequest(m_jsonFactory, nativeRequest);
 			wrappedRequest.readJson(m_jsonFactory, m_requestPathMngr, requestJson);
 			Integer libServerVersionAsFarAsClientKnows = wrappedRequest.getLibServerVersion();
 			Integer appServerVersionAsFarAsClientKnows = wrappedRequest.getAppServerVersion();
 			boolean serverVersionMismatch =
 					libServerVersionAsFarAsClientKnows != null && libServerVersionAsFarAsClientKnows != m_libServerVersion ||
 					appServerVersionAsFarAsClientKnows != null && appServerVersionAsFarAsClientKnows != m_appServerVersion ;;
 			
 			//--- DRK > Early out for server version mismatches.
 			if( serverVersionMismatch )
 			{
 				responseToReturn = this.createEarlyOutResponse(E_ResponseError.VERSION_MISMATCH);
 				
				return responseToReturn; // hits finally block
 			}
 			
 			if( wrappedRequest.getPath() == null )
 			{
 				responseToReturn = this.createEarlyOutResponse(E_ResponseError.UNKNOWN_PATH);
 
				return responseToReturn; // hits finally block
 			}
 			
 			boolean isBatch = wrappedRequest.getPath() == E_ReservedRequestPath.batch;
 			
 			final TransactionContext context = new TransactionContext(isBatch, nativeContext);
 			
 			for( int i = 0; i < m_scopeListeners.size(); i++ )
 			{
 				m_scopeListeners.get(i).onEnterScope();
 			}
 	
 			if( isBatch )
 			{
 				final TransactionResponseBatch responseBatch = new TransactionResponseBatch(m_jsonFactory);
 				responseToReturn = responseBatch;
 				
 				U_RequestBatch.I_JsonReadDelegate readDelegate = new U_RequestBatch.I_JsonReadDelegate()
 				{
 					@Override
 					public void onRequestFound(I_JsonObject requestJson)
 					{
 						TransactionRequest batchedRequest = new TransactionRequest(m_jsonFactory, nativeRequest);
 						batchedRequest.readJson(ServerTransactionManager.this.m_jsonFactory, m_requestPathMngr, requestJson);
 						TransactionResponse batchedResponse = new TransactionResponse(m_jsonFactory, nativeResponse);
 						
 						context.addTransaction(batchedRequest, batchedResponse);
 					}
 				};
 				
 				U_RequestBatch.readRequestList(m_jsonFactory, requestJson, readDelegate);
 				
 				TransactionBatch transactionBatch = context.getBatch();
 				
 				int transactionCount = transactionBatch.getCount();
 				if( transactionCount > 1 )
 				{
 					for( int i = 0; i < m_scopeListeners.size(); i++ )
 					{
 						m_scopeListeners.get(i).onBatchStart();
 					}
 				}
 				
 				for( int i = 0; i < transactionBatch.getCount(); i++ )
 				{
 					TransactionRequest batchedRequest = transactionBatch.getRequest(i);
 					TransactionResponse batchedResponse = transactionBatch.getResponse(i);
 					
 					callRequestHandler(context, batchedRequest, batchedResponse);
 					
 					if( batchedResponse.getError() == E_ResponseError.DEFERRED )
 					{
 						context.queueDeferredTransaction(batchedRequest, batchedResponse);
 					}
 					
 					responseBatch.addResponse(batchedResponse);
 				}
 				
 				if( context.getDeferredCount() > 0 )
 				{
 					TransactionBatch deferredBatch = context.getDeferredBatch();
 					
 					for( int i = 0; i < m_deferredHandlers.size(); i++ )
 					{
 						try
 						{
 							m_deferredHandlers.get(i).handleDeferredRequests(context, deferredBatch);
 						}
 						catch(Exception e)
 						{
 							//--- DRK > Handlers should try their damnedest to not let exceptions get up to here, so this is bad...
 							s_logger.log(Level.SEVERE, "Exception occurred while handling deferred requests.", e);
 
 							deferredBatch.markUnhandledTransactions(E_ResponseError.HANDLER_EXCEPTION);
 						}
 						
 						deferredBatch.removeHandledTransactions();
 						
 						if( deferredBatch.getCount() == 0 )
 						{
 							break;
 						}
 					}
 					
 					deferredBatch.markUnhandledTransactions(E_ResponseError.DEFERRED_BUT_NEVER_HANDLED);
 				}
 				
 				if( transactionCount > 1 )
 				{
 					for( int i = 0; i < m_scopeListeners.size(); i++ )
 					{
 						m_scopeListeners.get(i).onBatchEnd();
 					}
 				}
 			}
 			else
 			{
 				responseToReturn = new TransactionResponse(m_jsonFactory, nativeResponse);
 				
 				context.addTransaction(wrappedRequest, responseToReturn);
 				
 				callRequestHandler(context, wrappedRequest, responseToReturn);
 				
 				if( responseToReturn.getError() == E_ResponseError.DEFERRED )
 				{
 					responseToReturn.setError(E_ResponseError.DEFERRED_BUT_NEVER_HANDLED);
 				}
 			}
 		}
		
 		//--- DRK > Most likely means some problem with this class...unlikely, but catching everything just to be safe.
 		catch(Throwable e)
 		{
 			s_logger.log(Level.SEVERE, "Transaction manager encountered problem while processing request for json: " + requestJson.writeString(), e);
 			
 			responseToReturn = null;
 		}
 		
 		if( responseToReturn == null )
 		{
 			responseToReturn = new TransactionResponse(m_jsonFactory);
 			responseToReturn.setError(E_ResponseError.SERVER_EXCEPTION);
 			
 			s_logger.log(Level.SEVERE, "Response should not have been null.");
 		}
 		
 		responseToReturn.writeJson(m_jsonFactory, responseJson_out);
 		
 		m_jsonFactory.endScope();
 		
 		for( int i = 0; i < m_scopeListeners.size(); i++ )
 		{
 			m_scopeListeners.get(i).onExitScope();
 		}
 		
 		return responseToReturn;
 	}
 	
 	/**
 	 * Probably mostly only for debug purposes, where you don't really care if the response succeeded or not.
 	 * 
 	 * @param request
 	 */
 	public void callRequestHandler(TransactionRequest request)
 	{
 		this.callRequestHandler(new TransactionContext(false, null), request, new TransactionResponse(m_jsonFactory));
 	}
 	
 	public void callRequestHandler(TransactionRequest request, TransactionResponse response)
 	{
 		this.callRequestHandler(new TransactionContext(false, null), request, response);
 	}
 	
 	public void callRequestHandler(TransactionContext context, TransactionRequest request, TransactionResponse response)
 	{
 		response.setError(E_ResponseError.NO_ERROR);
 		
 		I_RequestPath path = request.getPath();
 		
 		if( path == null )
 		{
 			s_logger.log(Level.WARNING, "Could not find a registered path.");
 			
 			response.setError(E_ResponseError.UNKNOWN_PATH);
 			
 			return;
 		}
 		
 		if( m_debugResponseErrorPaths.contains(path.getId()) )
 		{
 			response.setError(E_ResponseError.SERVER_EXCEPTION);
 			return;
 		}
 		
 		I_RequestHandler handler = m_handlers.get(path.getId());
 		
 		if( handler != null )
 		{
 			try
 			{
 				handler.handleRequest(context, request, response);
 			}
 			catch (Exception e)
 			{
 				//--- DRK > Handlers should try their damnedest to not let exceptions get up to here, so this is bad...
 				s_logger.log(Level.SEVERE, "Exception occurred while invoking " + path + ".", e);
 				
 				response.setError(E_ResponseError.HANDLER_EXCEPTION);
 			}
 		}
 		else
 		{
 			s_logger.log(Level.WARNING, "Could not find handler for path: " + path);
 			
 			response.setError(E_ResponseError.UNKNOWN_PATH);
 		}
 	}
 }

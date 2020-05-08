 package swarm.client.transaction;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import swarm.client.time.U_Time;
 import swarm.shared.utils.ListenerManager;
 import swarm.shared.app.A_App;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.json.A_JsonFactory;
 import swarm.shared.json.I_JsonArray;
 import swarm.shared.json.I_ReadsJson;
 import swarm.shared.json.I_JsonObject;
 import swarm.shared.json.I_WritesJson;
 import swarm.shared.json.JsonQuery;
 import swarm.shared.json.JsonHelper;
 import swarm.shared.transaction.A_TransactionObject;
 import swarm.shared.transaction.E_RequestPath;
 import swarm.shared.transaction.E_ReservedRequestPath;
 import swarm.shared.transaction.E_ResponseError;
 import swarm.shared.transaction.I_RequestPath;
 import swarm.shared.transaction.RequestPathManager;
 import swarm.shared.transaction.S_Transaction;
 import swarm.shared.json.E_JsonKey;
 import swarm.shared.transaction.TransactionRequest;
 import swarm.shared.transaction.TransactionResponse;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.user.client.rpc.RpcRequestBuilder;
 
 /**
 * TODO(DRK):	Need to have stricter rules for when you can cancel requests.
  *				For example, it's now possible to cancel a request inside success and error handlers.
  *				Should this be allowed in any way?  If so, how should it work?.
  */
 public class ClientTransactionManager
 {	
 	private static final Logger s_logger = Logger.getLogger(ClientTransactionManager.class.getName());
 		
 	private final ListenerManager<I_TransactionResponseHandler> m_handlers = new ListenerManager<I_TransactionResponseHandler>();
 	private final ListenerManager<I_ResponseBatchListener> m_batchListeners = new ListenerManager<I_ResponseBatchListener>();
 	
 	private TransactionRequestBatch m_transactionRequestBatch = null;
 	
 	private I_SyncRequestDispatcher m_syncDispatcher = null;
 	private I_AsyncRequestDispatcher m_asyncDispatcher = null;
 	
 	private final TransactionResponse m_reusedResponse;
 	
 	private boolean m_isInsideBatch = false;
 	
 	private TransactionRequestBatch m_currentRequestBatch = null;
 	private I_JsonArray m_currentResponseList = null;
 	private TransactionResponse m_currentErrorResponse = null;
 	private int m_currentBatchIndex = 0;
 	
 	private TransactionRequest m_currentlyHandledRequest;
 	
 	private final I_ResponseCallbacks m_callbacks = new I_ResponseCallbacks()
 	{
 		@Override
 		public void onResponseReceived(TransactionRequest request, TransactionResponse response)
 		{
 			ClientTransactionManager.this.onResponseReceived(request, response);
 		}
 		
 		@Override
 		public void onResponseReceived(TransactionRequestBatch requestBatch, I_JsonArray jsonResponseBatch)
 		{
 			ClientTransactionManager.this.onResponseReceived(requestBatch, jsonResponseBatch);
 		}
 		
 		@Override
 		public void onError(TransactionRequest request, TransactionResponse response)
 		{
 			ClientTransactionManager.this.onError(request, response);
 		}
 	};
 	
 	private RequestPathManager m_requestPathMngr;
 	private A_JsonFactory m_jsonFactory;
 	
 	public ClientTransactionManager(RequestPathManager requestPathMngr, A_JsonFactory jsonFactory) 
 	{
 		m_jsonFactory = jsonFactory;
 		m_requestPathMngr = requestPathMngr;
 		m_requestPathMngr.register(E_ReservedRequestPath.values());
 		m_reusedResponse = new TransactionResponse(m_jsonFactory);
 	}
 	
 	public void setSyncRequestDispatcher(I_SyncRequestDispatcher dispatcher)
 	{
 		m_syncDispatcher = dispatcher;
 		m_syncDispatcher.initialize(m_callbacks, S_Transaction.MAX_GET_URL_LENGTH);
 	}
 	
 	public void setAsyncRequestDispatcher(I_AsyncRequestDispatcher dispatcher)
 	{
 		m_asyncDispatcher = dispatcher;
 		m_asyncDispatcher.initialize(m_callbacks, S_Transaction.MAX_GET_URL_LENGTH);
 	}
 	
 	private void callSuccessHandlers(TransactionRequest request, TransactionResponse response)
 	{
 		U_Debug.ASSERT(request.isCancelled() == false, "callSuccessHandlers1");
 		
 		m_currentlyHandledRequest = request;
 		
 		ArrayList<I_TransactionResponseHandler> list = m_handlers.getListeners();
 		
 		for ( int i = list.size()-1; i >= 0; i-- )
 		{
 			E_ResponseSuccessControl controlStatement = list.get(i).onResponseSuccess(request, response);
 			
 			switch(controlStatement)
 			{
 				case BREAK:  return;
 			}
 		}
 		
 		m_currentlyHandledRequest = null;
 	}
 	
 	private void callErrorHandlers(TransactionRequest request, TransactionResponse response)
 	{
 		U_Debug.ASSERT(request.isCancelled() == false, "callErrorHandlers");
 		
 		m_currentlyHandledRequest = request;
 		
 		ArrayList<I_TransactionResponseHandler> list = m_handlers.getListeners();
 		
 		for ( int i = list.size()-1; i >= 0; i-- )
 		{
 			E_ResponseErrorControl controlStatement = list.get(i).onResponseError(request, response);
 			
 			switch(controlStatement)
 			{
 				case BREAK:  return;
 				case CONTINUE:
 					break;
 				default:
 					break;
 			}
 		}
 		
 		m_currentlyHandledRequest = null;
 	}
 	
 	public boolean isInBatch()
 	{
 		return m_isInsideBatch;
 	}
 	
 	public void queueRequest(E_RequestPath path, I_WritesJson ... writesJson)
 	{
 		TransactionRequest request = new TransactionRequest(m_jsonFactory, path);
 		
 		for( int i = 0; i < writesJson.length; i++ )
 		{
 			writesJson[i].writeJson(m_jsonFactory, request.getJsonArgs());
 		}
 
 		queueRequest_private(request);
 	}
 	
 	public void queueRequest(E_RequestPath requestPath)
 	{
 		TransactionRequest request = new TransactionRequest(m_jsonFactory, requestPath);
 		queueRequest_private(request);
 	}
 	
 	private void queueRequest_private(TransactionRequest request)
 	{
 		if( m_transactionRequestBatch == null )
 		{
 			m_transactionRequestBatch = new TransactionRequestBatch(m_jsonFactory);
 		}
 		
 		m_transactionRequestBatch.addRequest(request);
 	}
 	
 	public void flushRequestQueue()
 	{
 		if ( m_transactionRequestBatch == null )  return;		
 		
 		//trace("flushing " + m_requestQueue.length + " transactions");
 		
 		this.makeRequest_private(m_transactionRequestBatch);
 		
 		m_transactionRequestBatch = null;
 	}
 	
 	public void flushSyncResponses()
 	{
 		if( m_syncDispatcher != null )
 		{
 			m_syncDispatcher.flushResponses();
 		}
 	}
 	
 	public void makeRequest(I_RequestPath path, I_WritesJson ... writesJson)
 	{
 		TransactionRequest request = new TransactionRequest(m_jsonFactory, path);
 		for( int i = 0; i < writesJson.length; i++ )
 		{
 			writesJson[i].writeJson(m_jsonFactory, request.getJsonArgs());
 		}
 		
 		makeRequest_private(request);
 	}
 	
 	public void makeRequest(I_RequestPath path)
 	{
 		TransactionRequest request = new TransactionRequest(m_jsonFactory, path);
 		makeRequest_private(request);
 	}
 	
 	private void makeRequest_private(TransactionRequest request)
 	{
 		request.onDispatch(U_Time.getMilliseconds(), S_CommonApp.SERVER_VERSION);
 		//request.init(m_requestPathMngr);
 		
 		if( m_syncDispatcher.dispatch(request) ){}
 		else if( m_asyncDispatcher.dispatch(request) ){}
 		else
 		{
 			//TODO(DRK): Create a third "error" dispatcher that simply responds to requests with CLIENT_EXCEPTION (or a new error type)
 			//			 The error here I guess is that something was so fucked we couldn't even get the request out the door with the other two dispatchers.
 		}
 	}
 	
 	public void performAction(E_TransactionAction action, E_RequestPath requestPath)
 	{
 		TransactionRequest request = new TransactionRequest(m_jsonFactory, requestPath);
 		performAction_private(action, request);
 	}
 	
 	public void performAction(E_TransactionAction action, E_RequestPath requestPath, I_WritesJson writesJson)
 	{
 		TransactionRequest request = new TransactionRequest(m_jsonFactory, requestPath);
 		writesJson.writeJson(m_jsonFactory, request.getJsonArgs());
 		
 		performAction_private(action, request);
 	}
 	
 	/**
 	 * This allows other classes (mainly various managers) to delegate how
 	 * they send their transactions to the caller of the manager's methods.
 	 */
 	private void performAction_private(E_TransactionAction action, TransactionRequest request)
 	{
 		switch(action)
 		{
 			case MAKE_REQUEST:
 			{
 				this.makeRequest_private(request);
 				
 				break;
 			}
 			
 			case QUEUE_REQUEST:
 			{
 				this.queueRequest_private(request);
 				
 				break;
 			}
 			
 			case QUEUE_REQUEST_AND_FLUSH:
 			{
 				this.queueRequest_private(request);
 				this.flushRequestQueue();
 				
 				break;
 			}
 		}
 	}
 	
 	public TransactionResponse getPreviousBatchResponse(E_RequestPath path)
 	{
 		Object responseObject = getPreviousBatchResponseObject(path);
 		
 		if( responseObject != null )
 		{
 			if( m_currentErrorResponse != null )
 			{
 				return (TransactionResponse)responseObject;
 			}
 			else
 			{
 				I_JsonObject responseJson = (I_JsonObject) responseObject;
 				TransactionResponse response = new TransactionResponse(m_jsonFactory);
 				response.readJson(m_jsonFactory, responseJson);
 				
 				return response;
 			}
 		}
 		
 		return null;
 	}
 	
 	public boolean hasPreviousBatchResponse(E_RequestPath path)
 	{
 		return getPreviousBatchResponseObject(path) != null;
 	}
 	
 	private Object getPreviousBatchResponseObject(E_RequestPath path)
 	{
 		if( !m_isInsideBatch )  return null;
 
 		for( int i = m_currentBatchIndex-1; i >= 0; i-- )
 		{
 			TransactionRequest previousRequest = m_currentRequestBatch.getRequest(i);
 			
 			if( previousRequest.isCancelled() )  continue;
 			
 			if( previousRequest.getPath() == path )
 			{
 				if( m_currentErrorResponse != null )
 				{
 					return m_currentErrorResponse;
 				}
 				else
 				{
 					return m_currentResponseList.getObject(i);
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	private void onBatchResponseStart(TransactionRequestBatch batch, I_JsonArray responseList)
 	{
 		m_currentResponseList = responseList;
 		
 		onBatchStart_shared(batch);
 	}
 	
 	private void onBatchResponseStartWithError(TransactionRequestBatch batch, TransactionResponse errorResponse)
 	{
 		m_currentErrorResponse = errorResponse;
 		
 		onBatchStart_shared(batch);
 	}
 	
 	private void onBatchStart_shared(TransactionRequestBatch batch)
 	{
 		m_currentRequestBatch = batch;
 		m_isInsideBatch = true;
 		
 		ArrayList<I_ResponseBatchListener> list = m_batchListeners.getListeners();
 		
 		for ( int i = list.size()-1; i >= 0; i-- )
 		{
 			list.get(i).onResponseBatchStart();
 		}
 	}
 	
 	private void onBatchResponseEnd()
 	{
 		m_isInsideBatch = false;
 		m_currentRequestBatch = null;
 		m_currentResponseList = null;
 		m_currentErrorResponse = null;
 		
 		ArrayList<I_ResponseBatchListener> list = m_batchListeners.getListeners();
 		
 		for ( int i = list.size()-1; i >= 0; i-- )
 		{
 			list.get(i).onResponseBatchEnd();
 		}
 	}
 	
 	public void addBatchListener(I_ResponseBatchListener listener)
 	{
 		m_batchListeners.addListenerToBack(listener);
 	}
 	
 	public void removeBatchListener(I_ResponseBatchListener listener)
 	{
 		m_batchListeners.removeListener(listener);
 	}
 	
 	public void addHandler(I_TransactionResponseHandler handler)
 	{
 		m_handlers.addListenerToBack(handler);
 	}
 	
 	public void removeHandler(I_TransactionResponseHandler handler)
 	{
 		m_handlers.removeListener(handler);
 	}
 	
 	public TransactionRequest getDispatchedRequest(E_RequestPath path)
 	{
 		return this.getDispatchedRequest(path, null);
 	}
 	
 	public TransactionRequest getDispatchedRequest(I_RequestPath path, JsonQuery jsonQuery)
 	{
 		TransactionRequest dispatchedRequest = m_asyncDispatcher.getDispatchedRequest(path, jsonQuery, m_currentlyHandledRequest);
 		
 		return dispatchedRequest;
 	}
 	
 	public boolean containsDispatchedRequest(I_RequestPath path)
 	{
 		return this.containsDispatchedRequest(path, null);
 	}
 	
 	public boolean containsDispatchedRequest(I_RequestPath path, JsonQuery query)
 	{
 		return this.getDispatchedRequest(path, query) != null;
 	}
 	
 	public void cancelRequestsByPaths(I_RequestPath ... paths)
 	{
 		for( int i = 0; i < paths.length; i++ )
 		{
 			cancelRequestsByPath(paths[i]);
 		}
 	}
 	
 	public void cancelRequestsByPath(I_RequestPath path)
 	{
 		m_asyncDispatcher.cancelRequestsByPath(path, m_currentlyHandledRequest);
 	}
 	
 	void onResponseReceived(TransactionRequestBatch requestBatch, I_JsonArray jsonResponseBatch)
 	{
 		if( jsonResponseBatch.getSize() > 1 )
 		{
 			this.onBatchResponseStart(requestBatch, jsonResponseBatch);
 		}
 		
 		for( int i = 0; i < jsonResponseBatch.getSize(); i++ )
 		{
 			this.m_currentBatchIndex = i;
 			
 			TransactionRequest ithRequest = requestBatch.getRequest(i);
 			
 			if( ithRequest.isCancelled() )
 			{
 				continue;
 			}
 			
 			I_JsonObject jsonObject = jsonResponseBatch.getObject(i);
 			
 			m_reusedResponse.clear();
 			
 			m_reusedResponse.readJson(m_jsonFactory, jsonObject);
 			
 			if( m_reusedResponse.getError() == E_ResponseError.NO_ERROR )
 			{
 				this.callSuccessHandlers(ithRequest, m_reusedResponse);
 			}
 			else
 			{
 				this.callErrorHandlers(ithRequest, m_reusedResponse);
 			}
 		}
 
 		if( jsonResponseBatch.getSize() > 1 )
 		{
 			this.onBatchResponseEnd();
 		}
 	}
 	
 	void onResponseReceived(TransactionRequest request, TransactionResponse response)
 	{
 		if( response.getError() == E_ResponseError.NO_ERROR )
 		{
 			this.callSuccessHandlers(request, response);
 		}
 		else
 		{
 			this.callErrorHandlers(request, response);
 		}
 	}
 	
 	void onError(TransactionRequest request, TransactionResponse response)
 	{			
 		if( !(request instanceof TransactionRequestBatch) )
 		{
 			this.callErrorHandlers(request, response);
 		}
 		else
 		{
 			TransactionRequestBatch batch = (TransactionRequestBatch) request;
 			
 			if( batch.getSize() > 1 )
 			{
 				this.onBatchResponseStartWithError(batch, response);
 			}
 			
 			for( int i = 0; i < batch.getSize(); i++ )
 			{
 				this.m_currentBatchIndex = i;
 				
 				if( batch.getRequest(i).isCancelled() )
 				{
 					continue;
 				}
 				
 				this.callErrorHandlers(batch.getRequest(i), response);
 			}
 			
 			if( batch.getSize() > 1 )
 			{
 				this.onBatchResponseEnd();
 			}
 		}
 	}
 }

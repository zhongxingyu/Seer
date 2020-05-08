 package gwtmvp.client.dispatch;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.gwtplatform.dispatch.client.DefaultDispatchAsync;
 import com.gwtplatform.dispatch.client.ExceptionHandler;
 import com.gwtplatform.dispatch.client.actionhandler.ClientActionHandlerRegistry;
 import com.gwtplatform.dispatch.shared.Action;
 import com.gwtplatform.dispatch.shared.DispatchRequest;
 import com.gwtplatform.dispatch.shared.Result;
 import com.gwtplatform.dispatch.shared.SecurityCookieAccessor;
 
 /**
  * Dispatcher which supports in memory data caching
  */
 @Singleton
 public class CachingDispatchAsync extends DefaultDispatchAsync {
 
 	private static HashMap<Action<Result>, Result> cache = new HashMap<Action<Result>, Result>();
 	private static HashMap<Action<Result>, ArrayList<AsyncCallback<Result>>> pendingCallbacks =
 			new HashMap<Action<Result>, ArrayList<AsyncCallback<Result>>>();
 
	@Inject
 	public CachingDispatchAsync(ExceptionHandler exceptionHandler,
 			SecurityCookieAccessor securityCookieAccessor,
 			ClientActionHandlerRegistry registry) {
 		super(exceptionHandler, securityCookieAccessor, registry);
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * Executes the given action and with cache when action is cacheable.
 	 * @return
 	 */
 	@Override
 	public <A extends Action<R>, R extends Result> DispatchRequest execute(final A action, AsyncCallback<R> callback) {
 		Log.debug("Calling service for " +action);
		return super.execute(action, callback);
 	}
 
 
 	/**
 	 * Executes the give Action. If the Action was executed before it will get fetched from the cache.
 	 *
 	 * @param Action implementation
 	 * @param Result implementation
 	 * @param action the action
 	 * @param callback the callback
 	 */
 	@SuppressWarnings("unchecked")
 	public <A extends Action<R>, R extends Result> void executeWithCache(final A action, final AsyncCallback<R> callback) {
 
 		Log.debug("Executing with cache " + action.toString());
 
 		final ArrayList<AsyncCallback<Result>> pending = pendingCallbacks.get(action);
 		// TODO timeout
 		if (pending != null) {
 			Log.debug("Command pending for " + action);
 			pending.add((AsyncCallback<Result>) callback);
 			return;
 		}
 
 		final Result r = cache.get(action);
 
 		if (r != null) {
 			Log.debug("Cache hit for " + action);
 			callback.onSuccess((R) r);
 		} else {
 			Log.debug("Calling real sevice for " + action);
 			pendingCallbacks.put((Action<Result>) action, new ArrayList<AsyncCallback<Result>>());
			super.execute(action, new AsyncCallback<R>() {
 
 				@Override
 				public void onFailure(Throwable caught) {
 					Log.debug("Real service call failed " + action);
 					// Process all pending callbacks for this action
 					ArrayList<AsyncCallback<Result>> callbacks = pendingCallbacks.remove(action);
 					for (AsyncCallback<Result> pendingCallback : callbacks) {
 						pendingCallback.onFailure(caught);
 					}
 					callback.onFailure(caught);
 				}
 
 				@Override
 				public void onSuccess(R result) {
 					Log.debug("Real service returned successfully " + action);
 					ArrayList<AsyncCallback<Result>> callbacks = pendingCallbacks.remove(action);
 					for (AsyncCallback<Result> pendingCallback : callbacks) {
 						pendingCallback.onSuccess(result);
 					}
 					cache.put((Action<Result>) action, (Result) result);
 					callback.onSuccess(result);
 				}
 
 			});
 		}
 	}
 
 
 	/**
 	 * Clear the cache
 	 */
 	public void clear() {
 		cache.clear();
 	}
 
 
 	/**
 	 * Clear the cache for a specific Action
 	 * 
 	 * @param action
 	 */
 	@SuppressWarnings("unchecked")
 	public <A extends Action<R>, R extends Result> void clear(A action) {
 		cache.put((Action<Result>) action, null);
 	}
 
 }

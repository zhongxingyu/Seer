 package com.ononedb.nextweb.js.fn;
 
 import io.nextweb.Session;
 import io.nextweb.fn.AsyncResult;
 import io.nextweb.fn.Closure;
 import io.nextweb.fn.ExceptionListener;
 import io.nextweb.fn.ExceptionResult;
 import io.nextweb.fn.Fn;
 import io.nextweb.fn.Result;
 import io.nextweb.operations.callbacks.Callback;
 import io.nextweb.operations.callbacks.CallbackFactory;
 import io.nextweb.operations.exceptions.ExceptionManager;
 import io.nextweb.operations.exceptions.ImpossibleListener;
import io.nextweb.operations.exceptions.ImpossibleResult;
 import io.nextweb.operations.exceptions.UnauthorizedListener;
 import io.nextweb.operations.exceptions.UnauthorizedResult;
 import io.nextweb.operations.exceptions.UndefinedListener;
 import io.nextweb.operations.exceptions.UndefinedResult;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import one.core.domain.OneClient;
 import one.core.dsl.callbacks.WhenCommitted;
 import one.core.dsl.callbacks.results.WithCommittedResult;
 
 import com.ononedb.nextweb.OnedbSession;
 
 public class JsResultImplementation<ResultType> implements Result<ResultType> {
 
 	private final AsyncResult<ResultType> asyncResult;
 
 	private ResultType resultCache;
 
 	private final ExceptionManager exceptionManager;
 
 	private boolean requestingResult;
 
 	private final List<Callback<ResultType>> deferredCalls;
 
 	private final Session session;
 
 	private void requestResult(final Callback<ResultType> callback) {
 
 		if (resultCache != null) {
 			callback.onSuccess(resultCache);
 			return;
 		}
 
 		if (requestingResult) {
 			deferredCalls.add(callback);
 			return;
 		}
 
 		requestingResult = true;
 
 		asyncResult.get(CallbackFactory
 				.eagerCallback(session, exceptionManager,
 						new Closure<ResultType>() {
 
 							@Override
 							public void apply(final ResultType result) {
 								resultCache = result;
 								requestingResult = false;
 								callback.onSuccess(result);
 
 								for (final Callback<ResultType> deferredCallback : deferredCalls) {
 									deferredCallback.onSuccess(result);
 								}
 								deferredCalls.clear();
 							}
 
 						}).catchExceptions(new ExceptionListener() {
 
 					@Override
 					public void onFailure(final ExceptionResult r) {
 						requestingResult = false;
 						callback.onFailure(r);
 						for (final Callback<ResultType> deferredCallback : deferredCalls) {
 							deferredCallback.onFailure(r);
 						}
 						deferredCalls.clear();
 					}
 				}).catchUnauthorized(new UnauthorizedListener() {
 
 					@Override
 					public void onUnauthorized(final UnauthorizedResult r) {
 						requestingResult = false;
 						callback.onUnauthorized(r);
 						for (final Callback<ResultType> deferredCallback : deferredCalls) {
 							deferredCallback.onUnauthorized(r);
 						}
 						deferredCalls.clear();
 					}
 				}).catchUndefined(new UndefinedListener() {
 
 					@Override
 					public void onUndefined(final UndefinedResult r) {
 						requestingResult = false;
 						callback.onUndefined(r);
 						for (final Callback<ResultType> deferredCallback : deferredCalls) {
 							deferredCallback.onUndefined(r);
 						}
 						deferredCalls.clear();
 					}
				}).catchImpossible(new ImpossibleListener() {

					@Override
					public void onImpossible(final ImpossibleResult ir) {
						requestingResult = false;
						callback.onImpossible(ir);
						for (final Callback<ResultType> deferredCallback : deferredCalls) {
							deferredCallback.onImpossible(ir);
						}
						deferredCalls.clear();

					}
 				}));
 
 		if (session != null) {
 
 			final OneClient client = ((OnedbSession) session).getClient();
 
 			client.one().commit(client).and(new WhenCommitted() {
 
 				@Override
 				public void thenDo(final WithCommittedResult r) {
 
 				}
 
 				@Override
 				public void onFailure(final Throwable t) {
 					exceptionManager.onFailure(Fn.exception(this, t));
 				}
 
 			});
 		}
 
 	}
 
 	@Override
 	public ExceptionManager getExceptionManager() {
 		return exceptionManager;
 	}
 
 	@Override
 	public void get(final Callback<ResultType> callback) {
 		requestResult(callback);
 	}
 
 	/**
 	 * Will trigger a request
 	 */
 	@Override
 	public ResultType get() {
 
 		requestResult(CallbackFactory.lazyCallback(session, exceptionManager,
 				new Closure<ResultType>() {
 
 					@Override
 					public void apply(final ResultType o) {
 						// nothing
 					}
 				}));
 
 		return this.resultCache;
 	}
 
 	public JsResultImplementation(final Session session,
 			final ExceptionManager fallbackExceptionManager,
 			final AsyncResult<ResultType> asyncResult) {
 		super();
 		assert asyncResult != null;
 		this.session = session;
 		this.asyncResult = asyncResult;
 		this.resultCache = null;
 		this.exceptionManager = new ExceptionManager(fallbackExceptionManager);
 		this.requestingResult = false;
 		this.deferredCalls = new LinkedList<Callback<ResultType>>();
 	}
 
 	@Override
 	public void get(final Closure<ResultType> callback) {
 		get(CallbackFactory.lazyCallback(session, exceptionManager,
 				new Closure<ResultType>() {
 
 					@Override
 					public void apply(final ResultType o) {
 						callback.apply(o);
 					}
 				}));
 
 	}
 
 	@Override
 	public Result<ResultType> catchImpossible(final ImpossibleListener listener) {
 		this.exceptionManager.catchImpossible(listener);
 		return this;
 	}
 
 	@Override
 	public Result<ResultType> catchUndefined(final UndefinedListener listener) {
 		this.exceptionManager.catchUndefined(listener);
 		return this;
 	}
 
 	@Override
 	public Result<ResultType> catchUnauthorized(
 			final UnauthorizedListener listener) {
 		this.exceptionManager.catchUnauthorized(listener);
 		return this;
 	}
 
 	@Override
 	public Result<ResultType> catchExceptions(final ExceptionListener listener) {
 		this.exceptionManager.catchExceptions(listener);
 		return this;
 	}
 
 }

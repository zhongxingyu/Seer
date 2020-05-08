 package com.ononedb.nextweb.js;
 
 import io.nextweb.Session;
 import io.nextweb.common.LocalServer;
 import io.nextweb.engine.Capability;
 import io.nextweb.engine.StartServerCapability;
 import io.nextweb.fn.AsyncResult;
 import io.nextweb.fn.ExceptionListener;
 import io.nextweb.fn.ExceptionResult;
 import io.nextweb.fn.Result;
 import io.nextweb.js.NextwebJs;
 import io.nextweb.js.engine.JsFactory;
 import io.nextweb.js.engine.JsNextwebEngine;
 import io.nextweb.operations.exceptions.ExceptionManager;
 import nx.client.gwt.services.GwtRemoteService;
 import nx.client.gwt.services.GwtRemoteServiceAsync;
 import one.client.gwt.OneGwt;
 import one.core.domain.BackgroundListener;
 import one.core.dsl.CoreDsl;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.ononedb.nextweb.common.H;
 import com.ononedb.nextweb.internal.OnedbFactory;
 import com.ononedb.nextweb.js.fn.JsResultImplementation;
 import com.ononedb.nextweb.js.utils.OnedbWrapper;
 import com.ononedb.nextweb.plugins.DefaultPluginFactory;
 
 public class OnedbNextwebJsEngineImpl implements OnedbNextwebEngineJs {
 
 	private CoreDsl dsl;
 	private final ExceptionManager exceptionManager;
 	private final JsFactory jsFactory;
 	private StartServerCapability startServerCapability;
 
 	public static OnedbNextwebJsEngineImpl init() {
 		final OnedbNextwebJsEngineImpl engine = new OnedbNextwebJsEngineImpl();
 		NextwebJs.injectEngine(JsNextwebEngine.wrap(engine));
 		return engine;
 	}
 
 	public static OnedbNextwebJsEngineImpl assertInitialized() {
 		if (NextwebJs.getEngine() == null
 				|| (!(NextwebJs.getEngine() instanceof JsNextwebEngine))) {
 			return init();
 		}
 
 		return (OnedbNextwebJsEngineImpl) NextwebJs.getEngine().getEngine();
 	}
 
 	private CoreDsl assertDsl() {
 		if (dsl != null) {
 			return dsl;
 		}
 
 		final GwtRemoteServiceAsync gwtService = GWT
 				.create(GwtRemoteService.class);
 
 		((ServiceDefTarget) gwtService)
 				.setServiceEntryPoint("/servlets/v01/gwtrpc");
 
 		dsl = OneGwt.init(gwtService, "");
 
 		dsl.getDefaults().getSettings()
 				.setDefaultBackgroundListener(new BackgroundListener() {
 
 					@Override
 					public void onBackgroundException(final Object operation,
 							final Throwable t, final Throwable origin) {
 						throw new RuntimeException("Exception: "
 								+ t.getMessage() + " for operation: ["
 								+ operation + "] originating from: [" + origin
 								+ "].", t);
 					}
 				});
 
 		return dsl;
 	}
 
 	@Override
 	public Session createSession() {
 
 		final CoreDsl dsl = assertDsl();
 
 		return getFactory().createSession(this, dsl.createClient());
 	}
 
 	@Override
 	public <ResultType> Result<ResultType> createResult(
 			final ExceptionManager exceptionManager, final Session session,
 			final AsyncResult<ResultType> asyncResult) {
 		return new JsResultImplementation<ResultType>(session,
 				exceptionManager, asyncResult);
 	}
 
 	@Override
 	public ExceptionManager getExceptionManager() {
 
 		return exceptionManager;
 	}
 
 	@Override
 	public OnedbFactory getFactory() {
 		return new OnedbFactory();
 	}
 
 	public OnedbNextwebJsEngineImpl() {
 		super();
 		this.exceptionManager = getFactory().createExceptionManager(null);
 		this.exceptionManager.catchExceptions(new ExceptionListener() {
 
 			@Override
 			public void onFailure(final ExceptionResult r) {
 				throw new RuntimeException("Unhandled exception: "
 						+ r.exception().getMessage() + " from object "
 						+ r.origin() + " (" + r.origin().getClass() + ")");
 			}
 		});
 		this.jsFactory = new JsFactory();
 		jsFactory.getWrappers().addWrapper(OnedbWrapper.ONEJSON);
 	}
 
 	@Override
 	public DefaultPluginFactory plugin() {
 		return H.onedbDefaultPluginFactory();
 	}
 
 	@Override
 	public JsFactory jsFactory() {
 		return jsFactory;
 	}
 
 	@Override
 	public void runSafe(final Session forSession, final Runnable task) {
 		task.run(); // no multi-threading in JS assured.
 	}
 
 	@Override
 	public boolean hasStartServerCapability() {
 		return startServerCapability != null;
 	}
 
 	@Override
 	public void injectCapability(final Capability capability) {
 		if (capability instanceof StartServerCapability) {
 			startServerCapability = (StartServerCapability) capability;
 		}
 
 		throw new IllegalArgumentException(
 				"This engine cannot recognize the capability: ["
 						+ capability.getClass() + "]");
 	}
 
 	@Override
 	public LocalServer startServer(final int port) {
 		if (startServerCapability == null) {
 			throw new IllegalStateException(
 					"Please inject a StartServerCapability first.");
 		}
 
 		return startServerCapability.startServer(port);
 	}
 
 }

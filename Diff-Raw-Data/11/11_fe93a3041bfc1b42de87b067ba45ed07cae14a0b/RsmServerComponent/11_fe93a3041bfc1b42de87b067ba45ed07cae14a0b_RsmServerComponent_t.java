 package com.appjangle.rsm.server.internal;
 
 import io.nextweb.Link;
 import io.nextweb.Node;
 import io.nextweb.NodeList;
 import io.nextweb.Session;
 import io.nextweb.common.Interval;
 import io.nextweb.common.Monitor;
 import io.nextweb.common.MonitorContext;
 import io.nextweb.fn.Closure;
 import io.nextweb.fn.ExceptionListener;
 import io.nextweb.fn.ExceptionResult;
 import io.nextweb.fn.Result;
 import io.nextweb.fn.Success;
 import io.nextweb.jre.Nextweb;
 import io.nextweb.operations.exceptions.ImpossibleListener;
 import io.nextweb.operations.exceptions.ImpossibleResult;
 import io.nextweb.operations.exceptions.UndefinedListener;
 import io.nextweb.operations.exceptions.UndefinedResult;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import one.async.joiner.CallbackLatch;
 
 import com.appjangle.rsm.client.commands.ComponentCommand;
 import com.appjangle.rsm.client.commands.OperationCallback;
 import com.appjangle.rsm.client.commands.v01.FailureResponse;
 import com.appjangle.rsm.client.commands.v01.SuccessResponse;
 import com.appjangle.rsm.server.RsmServerConfiguration;
 
 import de.mxro.server.ComponentConfiguration;
 import de.mxro.server.ComponentContext;
 import de.mxro.server.ServerComponent;
 import de.mxro.server.StartCallback;
 
 public class RsmServerComponent implements ServerComponent {
 
 	private volatile boolean started = false;
 	private volatile boolean starting = false;
 
 	Session session;
 	RsmServerConfiguration conf;
 	Monitor monitor;
 	Link commands;
 	private ComponentContext context;
 
 	@Override
 	public void start(final StartCallback callback) {
 		if (started || starting) {
 			throw new IllegalStateException(
 					"Cannot start an already started component.");
 		}
 		starting = true;
 
 		session = Nextweb.createSession();
 
 		commands = session.node(conf.getCommandsNode(),
 				conf.getCommandsNodeSecret());
 
 		// System.out.println("start monitoring: " + conf.getCommandsNode());
 
 		final Result<Monitor> monitorResult = commands.monitor(Interval.FAST,
 				new Closure<MonitorContext>() {
 
 					@Override
 					public void apply(final MonitorContext ctx) {
 
 						processRequests(ctx.node());
 					}
 
 				});
 
 		monitorResult.catchExceptions(new ExceptionListener() {
 
 			@Override
 			public void onFailure(final ExceptionResult r) {
 				callback.onFailure(r.exception());
 			}
 		});
 
 		monitorResult.get(new Closure<Monitor>() {
 
 			@Override
 			public void apply(final Monitor o) {
 
 				monitor = o;
 				starting = false;
 				started = true;
 				callback.onStarted();
 			}
 		});
 
 	}
 
 	private static interface RequestsProcessedCallback {
 
 		public void onDone();
 
 	}
 
 	private final List<NodeList> scheduled = Collections
 			.synchronizedList(new LinkedList<NodeList>());
 	private Boolean processing = false;
 
 	private void processRequests(final Node node) {
 
 		node.selectAll().get(new Closure<NodeList>() {
 
 			@Override
 			public void apply(final NodeList o) {
 
 				synchronized (processing) {
 					scheduled.add(o);
 
 					if (!processing) {
 						processScheduled();
 					}
 				}
 
 			}
 		});
 
 	}
 
 	protected void processScheduled() {
 
 		synchronized (processing) {
 
 			processing = true;
 
 			if (scheduled.size() == 0) {
 				processing = false;
 				return;
 			}
 
 			final NodeList o = scheduled.get(0);
 
 			scheduled.remove(0);
 
			System.out.println("processing: " + o.size() + " " + o.asList());

 			processRequests(o, new RequestsProcessedCallback() {
 
 				@Override
 				public void onDone() {
					System.out.println("DONE");
 					processScheduled();
 				}
 			});
 		}
 	}
 
 	private void processRequests(final NodeList o,
 			final RequestsProcessedCallback requestsProcessedCallback) {
 
 		final CallbackLatch latch = new CallbackLatch(o.nodes().size()) {
 
 			@Override
 			public void onFailed(final Throwable t) {
 				requestsProcessedCallback.onDone();
 			}
 
 			@Override
 			public void onCompleted() {
 				requestsProcessedCallback.onDone();
 			}
 		};
 
 		for (final Node child : o.nodes()) {
 
 			final Object value = child.value();

			if (!(value instanceof ComponentCommand)) {
				latch.registerSuccess();
				continue;
			}

 			if (value instanceof ComponentCommand) {
 				final ComponentCommand command = (ComponentCommand) value;
 
 				final Result<Success> removeRequest = commands
 						.removeSafe(child);
 
 				removeRequest.catchImpossible(new ImpossibleListener() {
 
 					@Override
 					public void onImpossible(final ImpossibleResult ir) {
 						latch.registerSuccess();
 						// some other process might have processed this item
 					}
 				});
 
 				removeRequest.get(new Closure<Success>() {
 
 					@Override
 					public void apply(final Success o) {
 						final Link responseNode = session.node(command
 								.getResponsePort().getUri(), command
 								.getResponsePort().getSecret());
 
 						responseNode.catchUndefined(new UndefinedListener() {
 
 							@Override
 							public void onUndefined(final UndefinedResult r) {
 								latch.registerSuccess();
 								throw new RuntimeException(
 										"Response node has not been defined correctly.");
 							}
 						});
 
 						responseNode.get(new Closure<Node>() {
 
 							@Override
 							public void apply(final Node o) {
 								processCommand(command, o,
 										new RequestsProcessedCallback() {
 
 											@Override
 											public void onDone() {
 												latch.registerSuccess();
 											}
 										});
 							}
 						});
 
 					}
 				});
 
 			}
 
 		}
 
 	}
 
 	/**
 	 * Performing command for server and posting response to response node
 	 * specified by client.
 	 * 
 	 * @param command
 	 * @param requestsProcessedCallback
 	 */
 	private void processCommand(final ComponentCommand command,
 			final Node responseNode,
 			final RequestsProcessedCallback requestsProcessedCallback) {
 
 		conf.getExecutor(context).perform(command.getOperation(), context,
 				new OperationCallback() {
 
 					@Override
 					public void onSuccess() {
 						final SuccessResponse successResponse = new SuccessResponse();
 						responseNode.appendSafe(successResponse);
 
 						session.commit();
 						requestsProcessedCallback.onDone();
 					}
 
 					@Override
 					public void onFailure(final Throwable t) {
 						final FailureResponse failureResponse = new FailureResponse();
 						failureResponse.setException(t);
 
 						responseNode.appendSafe(failureResponse);
 
 						session.commit();
 						requestsProcessedCallback.onDone();
 
 					}
 				});
 
 	}
 
 	@Override
 	public void stop(final de.mxro.server.ShutdownCallback callback) {
 		if (!(started || starting)) {
 			throw new IllegalStateException(
 					"Cannot stop an already stopped component.");
 		}
 
 		if (starting) {
 			while (!started) {
 
 			}
 		}
 
 		monitor.stop().get(new Closure<Success>() {
 
 			@Override
 			public void apply(final Success o) {
 
 				final Result<Success> result = session.close();
 				result.catchExceptions(new ExceptionListener() {
 
 					@Override
 					public void onFailure(final ExceptionResult r) {
 						callback.onFailure(r.exception());
 					}
 				});
 				result.get(new Closure<Success>() {
 
 					@Override
 					public void apply(final Success o) {
 
 						started = false;
 						callback.onShutdownComplete();
 					}
 				});
 			}
 		});
 
 	}
 
 	@Override
 	public void injectConfiguration(final ComponentConfiguration conf) {
 		this.conf = (RsmServerConfiguration) conf;
 	}
 
 	@Override
 	public void injectContext(final ComponentContext context) {
 		this.context = context;
 	}
 
 	@Override
 	public ComponentConfiguration getConfiguration() {
 		return conf;
 	}
 
 }

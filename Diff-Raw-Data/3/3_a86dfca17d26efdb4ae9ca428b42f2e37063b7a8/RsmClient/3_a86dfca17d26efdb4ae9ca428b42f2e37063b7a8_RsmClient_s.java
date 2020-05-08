 package com.appjangle.rsm.client;
 
 import io.nextweb.Link;
 import io.nextweb.ListQuery;
 import io.nextweb.Node;
 import io.nextweb.NodeList;
 import io.nextweb.Query;
 import io.nextweb.Session;
 import io.nextweb.common.Interval;
 import io.nextweb.common.MonitorContext;
 import io.nextweb.fn.Closure;
 import io.nextweb.jre.Nextweb;
 
 import com.appjangle.rsm.client.commands.ComponentOperation;
 import com.appjangle.rsm.client.commands.OperationCallback;
 import com.appjangle.rsm.client.commands.v01.ComponentCommandData;
 import com.appjangle.rsm.client.commands.v01.FailureResponse;
 import com.appjangle.rsm.client.commands.v01.SuccessResponse;
 
 public class RsmClient {
 
 	/**
 	 * Run an operation on the server for a specific component.
 	 * 
 	 * @param operation
 	 * @param forId
 	 * @param conf
 	 * @param callback
 	 */
 	public static void performCommand(final ComponentOperation operation,
 			final String forId, final ClientConfiguration conf,
 			final OperationCallback callback) {
 		assert forId != null;
 		assert operation != null;
 
 		final Session session = Nextweb.createSession();
 
 		// prepare response node
 		final Link responsesLink = session.node(conf.getResponsesNode(),
 				conf.getResponseNodeSecret());
 		final Query responseQuery = responsesLink.appendSafe("resp");
 
 		final Node response = responseQuery.get();
 
 		// preparing command
 		final ComponentCommandData command = new ComponentCommandData();
 		command.setId(forId);
 		command.setOperation(operation);
 		command.setPort(Nextweb
 				.getEngine()
 				.getFactory()
 				.createPort(session, response.uri(),
 						conf.getResponseNodeSecret()));
 
 		// monitor node for response from server
 		response.monitor(Interval.EXTRA_FAST, new Closure<MonitorContext>() {
 
 			@Override
 			public void apply(final MonitorContext ctx) {
 
 				final ListQuery allQuery = ctx.node().selectAll();
 
 				allQuery.get(new Closure<NodeList>() {
 
 					@Override
 					public void apply(final NodeList o) {
 						if (o.values().contains(new SuccessResponse())) {
 
 							responsesLink.remove(response);
 							ctx.monitor().stop();
 							session.commit();
 							callback.onSuccess();
 							return;
 						}
 
 						for (final Object obj : o.values()) {
 							if (obj instanceof FailureResponse) {
 
 								responsesLink.remove(response);
 								ctx.monitor().stop();
 								session.commit();
 
 								final FailureResponse failureResponse = (FailureResponse) obj;
 								callback.onFailure(failureResponse
 										.getException());
 								return;
 							}
 
 						}
 					}
 				});
 
 			}
 		});
 
 		// add to commands node
 		session.post(command, conf.getCommandsNode(),
 				conf.getCommandsNodeSecret());
 
 		// synchronizing all changes with server
 		session.commit().get();
 
 	}
 }

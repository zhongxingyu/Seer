 package com.appjangle.rsm.client.internal;
 
 import io.nextweb.Link;
 import io.nextweb.LinkList;
 import io.nextweb.LinkListQuery;
 import io.nextweb.Node;
 import io.nextweb.fn.BasicResult;
 import io.nextweb.fn.Closure;
 import io.nextweb.fn.ExceptionListener;
 import io.nextweb.fn.ExceptionResult;
 import io.nextweb.fn.Result;
 import io.nextweb.fn.Success;
 import io.nextweb.fn.SuccessFail;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ClearResponseNodeProcess {
 
 	public static interface ResponseNodeCleared {
 		public void onSuccess();
 
 		public void onFailure(Throwable t);
 	}
 
 	public void clearResponse(final Link responsesLink,
 			final Node responseNode, final ResponseNodeCleared callback) {
 
 		final LinkListQuery allChildren = responseNode.selectAllLinks();
 
 		allChildren.catchExceptions(createExceptionListener("selectChildren",
 				callback));
 
 		allChildren.get(new Closure<LinkList>() {
 
 			@Override
 			public void apply(final LinkList o) {
 
 				final List<BasicResult<?>> res = new ArrayList<BasicResult<?>>(
 						o.size() + 1);
 
 				for (final Link l : o) {
 
 					final Result<Success> removeChildReq = responseNode
 							.removeSafe(l);
 
 					removeChildReq.catchExceptions(createExceptionListener(
 							"remove child " + l, callback));
 
 					res.add(removeChildReq);
 
 				}
 
 				final Result<Success> removeResponse = responsesLink
 						.removeSafe(responseNode);
 
 				removeResponse.catchExceptions(createExceptionListener(
 						"removing response " + responseNode, callback));
 
 				res.add(removeResponse);
 
 				responseNode.getSession()
						.getAll(true, (BasicResult<?>[]) res.toArray())
 						.get(new Closure<SuccessFail>() {
 
 							@Override
 							public void apply(final SuccessFail o) {
 								if (o.isFail()) {
 									callback.onFailure(o.getException());
 									return;
 								}
 
 								callback.onSuccess();
 							}
 						});
 
 			}
 		});
 
 	}
 
 	private ExceptionListener createExceptionListener(final String message,
 			final ResponseNodeCleared callback) {
 		return new ExceptionListener() {
 
 			@Override
 			public void onFailure(final ExceptionResult r) {
 				callback.onFailure(new Exception(message, r.exception()));
 			}
 		};
 	}
 }

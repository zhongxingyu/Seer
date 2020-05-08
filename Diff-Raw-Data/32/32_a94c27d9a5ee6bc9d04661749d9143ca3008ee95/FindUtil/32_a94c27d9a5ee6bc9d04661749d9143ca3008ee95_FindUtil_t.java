 package suite.lp.search;
 
 import java.util.List;
 import java.util.concurrent.SynchronousQueue;
 
 import suite.lp.doer.Cloner;
 import suite.lp.search.ProverBuilder.Finder;
 import suite.node.Node;
 import suite.node.Reference;
 import suite.util.FunUtil.Sink;
 import suite.util.FunUtil.Source;
 import suite.util.LogUtil;
 import suite.util.To;
 
 public class FindUtil {
 
	private static Node eoq = new Reference();

 	public static Node collectSingle(Finder finder, Node in) {
 		List<Node> list = To.list(collect(finder, in));
 		if (list.size() == 1)
 			return list.get(0).finalNode();
 		else
 			throw new RuntimeException("Single result expected");
 	}
 
 	public static List<Node> collectList(Finder finder, Node in) {
 		return To.list(collect(finder, in));
 	}
 
 	private static Source<Node> collect(Finder finder, Node in) {
 		return collect(finder, To.source(in));
 	}
 
 	/**
 	 * Does find in background.
 	 */
 	private static Source<Node> collect(final Finder finder, final Source<Node> in) {
 		final SynchronousQueue<Node> queue = new SynchronousQueue<>();
 
 		final Sink<Node> sink = new Sink<Node>() {
 			public void sink(Node node) {
				put(queue, new Cloner().clone(node));
 			}
 		};
 
 		new Thread() {
 			public void run() {
 				try {
 					finder.find(in, sink);
 				} catch (Exception ex) {
 					LogUtil.error(ex);
 				} finally {
					put(queue, eoq);
 				}
 			}
 		}.start();
 
 		return new Source<Node>() {
 			public Node source() {
 				try {
 					Node node = queue.take();
 					return node != eoq ? node : null;
 				} catch (InterruptedException ex) {
 					throw new RuntimeException(ex);
 				}
 			}
 		};
 	}
 
	private static void put(SynchronousQueue<Node> queue, Node node) {
		try {
			queue.put(node);
		} catch (InterruptedException ex) {
			LogUtil.error(ex);
		}
	}

 }

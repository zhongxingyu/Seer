 package suite.lp.search;
 
 import java.util.List;
 import java.util.concurrent.SynchronousQueue;
 
 import suite.lp.doer.Cloner;
 import suite.lp.search.ProverBuilder.Finder;
 import suite.node.Node;
 import suite.node.Reference;
 import suite.util.FunUtil.Sink;
 import suite.util.FunUtil.Source;
 import suite.util.To;
 
 public class FindUtil {
 
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
		final Node eos = new Reference();

 		final SynchronousQueue<Node> queue = new SynchronousQueue<>();
 
 		final Sink<Node> sink = new Sink<Node>() {
 			public void sink(Node node) {
				queue.offer(new Cloner().clone(node));
 			}
 		};
 
 		new Thread() {
 			public void run() {
				finder.find(in, sink);
				queue.offer(eos);
 			}
 		}.start();
 
 		return new Source<Node>() {
 			public Node source() {
 				try {
 					Node node = queue.take();
					return node != eos ? node : null;
 				} catch (InterruptedException ex) {
 					throw new RuntimeException(ex);
 				}
 			}
 		};
 	}
 
 }

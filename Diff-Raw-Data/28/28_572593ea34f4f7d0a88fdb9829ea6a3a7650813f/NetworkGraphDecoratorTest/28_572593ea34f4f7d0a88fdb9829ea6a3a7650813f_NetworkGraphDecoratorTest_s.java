 package be.uclouvain.jail.adapt;
 
 import junit.framework.TestCase;
 import net.chefbe.javautils.adapt.AdaptUtils;
 import net.chefbe.javautils.adapt.IAdapter;
 import be.uclouvain.jail.dialect.dot.JDotty;
 import be.uclouvain.jail.graph.IDirectedGraph;
 import be.uclouvain.jail.graph.adjacency.AdjacencyDirectedGraph;
import be.uclouvain.jail.uinfo.IUserInfo;
 import be.uclouvain.jail.vm.JailVM;
 
 /**
  * Tests the network graph.
  * 
  * @author blambeau
  */
 public class NetworkGraphDecoratorTest extends TestCase {
 
 	/** Tests show graph. */
 	public void testShowGraph() throws Exception {
 		AdaptUtils.findAdapter(AdjacencyDirectedGraph.class,IDirectedGraph.class);
 		JailVM vm = new JailVM();
 		JDotty jdotty = new JDotty();
 		IDirectedGraph g = vm.getCoreToolkit().adaptations("hello");
 		jdotty.present(g, null);
 		
		IAdapter adapter = AdaptUtils.findAdapter(AdjacencyDirectedGraph.class, IUserInfo.class);
 		System.out.println(adapter);
 	}
 	
 	/** Main method. */
 	public static void main(String[] args) throws Exception {
 		new NetworkGraphDecoratorTest().testShowGraph();
 	}
 	
 }

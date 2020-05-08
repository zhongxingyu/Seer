 // Released under GPLv2 or later. See http://www.gnu.org/ for details.
 package tags;
 
 import tags.io.AttrGraphMLReader;
 import tags.io.AttrGraphMLMetadata;
import edu.uci.ics.jung.io.GraphMLMetadata;
 
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.FactoryUtils;
 
import java.util.Map;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
 
 /**
 ** Entry point for the tag-routing suite.
 **
 ** @author infinity0
 */
 public class Tags {
 
 	public static void main(String[] args) throws Throwable {
 		System.out.println("Hello, world!");
 		String fn = args.length == 0? "../test.graphml": args[0];
 		readGraphML(fn);
		//for (String a: args) { System.out.println(a); }
 	}
 
 	public static class Vx {
 		@Override public String toString() { return "o"; }
 	}
 
 	public static class Ed {
 		@Override public String toString() { return "->"; }
 	}
 
	public static void readGraphML(String fn) throws IOException, ParserConfigurationException, SAXException {
 
 		Factory<Vx> fac_v = FactoryUtils.instantiateFactory(Vx.class);
 		Factory<Ed> fac_e = FactoryUtils.instantiateFactory(Ed.class);
 		AttrGraphMLReader<DirectedGraph<Vx, Ed>, Vx, Ed> reader = new
 		AttrGraphMLReader<DirectedGraph<Vx, Ed>, Vx, Ed>(fac_v, fac_e);
 
 		DirectedGraph<Vx, Ed> g = new DirectedSparseGraph<Vx, Ed>();
 		reader.load(fn, g);
 
 		for (Vx vx: g.getVertices()) {
 			StringBuilder s = new StringBuilder();
 			s.append(vx).append(" ( ");
 			for (Map.Entry<String, GraphMLMetadata<Vx>> en: reader.getVertexMetadata().entrySet()) {
 				s.append(en.getKey()).append(':');
 				s.append(en.getValue().transformer.transform(vx)).append(' ');
 			}
 			s.append(')');
 			System.out.println(s);
 		}
 
 		for (Ed ed: g.getEdges()) {
 			StringBuilder s = new StringBuilder();
 			s.append(ed).append(" ( ");
 			for (Map.Entry<String, AttrGraphMLMetadata<Ed>> en: reader.getEdgeAttrMetadata().entrySet()) {
 				s.append(en.getKey()).append(':');
 				s.append(en.getValue().transformer().transform(ed)).append(' ');
 			}
 			s.append(')');
 			System.out.println(s);
 		}
 
 	}
 
 }

 package au.net.netstorm.boost.edge.testdata;
 
 import java.io.InputStream;
 
 import au.net.netstorm.boost.edge.Edge;
 
//FIX 2328 swap over to just use edge classes from demo tree
 public interface AutoEdgeInputStream extends Edge<InputStream> {
     int read(byte[] out);
 }

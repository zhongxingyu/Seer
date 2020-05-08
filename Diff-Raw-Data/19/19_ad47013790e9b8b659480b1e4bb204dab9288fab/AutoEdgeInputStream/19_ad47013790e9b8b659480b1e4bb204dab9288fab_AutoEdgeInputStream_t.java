 package au.net.netstorm.boost.edge.testdata;
 
 import java.io.InputStream;
 
 import au.net.netstorm.boost.edge.Edge;
 
 public interface AutoEdgeInputStream extends Edge<InputStream> {
     int read(byte[] out);
 }

 package au.net.netstorm.boost.edge.testdata;
 
 import java.nio.channels.WritableByteChannel;
 
 import au.net.netstorm.boost.edge.Edge;
 
//FIX 2328 swap over to just use edge classes from demo tree
 public interface AutoEdgeWritableByteChannel extends Edge<WritableByteChannel> {
     int write(AutoEdgeByteBuffer src);
 }

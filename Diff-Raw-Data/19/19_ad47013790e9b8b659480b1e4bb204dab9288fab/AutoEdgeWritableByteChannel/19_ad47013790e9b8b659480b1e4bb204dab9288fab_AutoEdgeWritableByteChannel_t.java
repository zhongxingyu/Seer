 package au.net.netstorm.boost.edge.testdata;
 
 import java.nio.channels.WritableByteChannel;
 
 import au.net.netstorm.boost.edge.Edge;
 
 public interface AutoEdgeWritableByteChannel extends Edge<WritableByteChannel> {
     int write(AutoEdgeByteBuffer src);
 }

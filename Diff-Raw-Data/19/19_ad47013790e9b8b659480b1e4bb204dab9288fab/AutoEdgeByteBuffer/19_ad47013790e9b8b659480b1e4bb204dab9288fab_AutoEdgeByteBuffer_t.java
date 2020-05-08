 package au.net.netstorm.boost.edge.testdata;
 
 import java.nio.ByteBuffer;
 
 import au.net.netstorm.boost.edge.Edge;
 
 public interface AutoEdgeByteBuffer extends Edge<ByteBuffer> {
     AutoEdgeByteBuffer duplicate();
 }

 package au.net.netstorm.boost.edge.testdata;
 
 import java.nio.ByteBuffer;
 
 import au.net.netstorm.boost.edge.Edge;
 
// FIX 2328 swap over to just use edge classes from demo tree
 public interface AutoEdgeByteBuffer extends Edge<ByteBuffer> {
     AutoEdgeByteBuffer duplicate();
 }

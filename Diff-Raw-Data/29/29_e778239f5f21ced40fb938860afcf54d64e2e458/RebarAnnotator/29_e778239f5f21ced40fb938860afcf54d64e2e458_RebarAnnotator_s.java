 /**
  * 
  */
 package edu.jhu.hlt.rebar.accumulo;
 
 import org.apache.accumulo.core.client.Connector;
 import org.apache.accumulo.core.client.MutationsRejectedException;
 import org.apache.accumulo.core.data.Mutation;
 import org.apache.accumulo.core.data.Value;
 import org.apache.thrift.TException;
 import org.apache.thrift.TSerializer;
 import org.apache.thrift.protocol.TBinaryProtocol;
 
 import com.maxjthomas.dumpster.AnnotationException;
 import com.maxjthomas.dumpster.Annotator;
 import com.maxjthomas.dumpster.Document;
 import com.maxjthomas.dumpster.LangId;
 import com.maxjthomas.dumpster.Stage;
 
 import edu.jhu.hlt.rebar.RebarException;
 
 /**
  * @author max
  *
  */
 public class RebarAnnotator extends AbstractAccumuloClient implements AutoCloseable, Annotator.Iface {
 
   /**
    * @throws RebarException 
    * 
    */
   public RebarAnnotator() throws RebarException {
     this(AbstractAccumuloClient.getConnector());
   }
   
   public RebarAnnotator (Connector conn) throws RebarException {
     super(conn);
   }
 
 
   /* (non-Javadoc)
    * @see edu.jhu.hlt.rebar.accumulo.AbstractAccumuloClient#flush()
    */
   @Override
   public void flush() throws RebarException {
     try {
       this.bw.flush();
     } catch (MutationsRejectedException e) {
       throw new RebarException(e);
     }
   }
 
   /* (non-Javadoc)
    * @see java.lang.AutoCloseable#close()
    */
   @Override
   public void close() throws Exception {
     this.bw.close();
   }
 }

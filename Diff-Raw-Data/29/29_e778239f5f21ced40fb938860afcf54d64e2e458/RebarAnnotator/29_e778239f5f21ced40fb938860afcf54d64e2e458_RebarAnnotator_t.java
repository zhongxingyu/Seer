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
import edu.jhu.hlt.rebar.config.RebarConfiguration;
 
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

  @Override
  public void addLanguageId(Document document, Stage stage, LangId lid) throws AnnotationException, TException {
    // TODO Auto-generated method stub
    byte[] lidBytes = this.serializer.serialize(lid);
    final Mutation m = new Mutation(document.id);
    try {
      m.put(RebarConfiguration.DOCUMENT_ANNOTATION_COLF, stage.name, new Value(lidBytes));
      this.bw.addMutation(m);
    } catch (MutationsRejectedException e) {
      throw new TException(e.getMessage());
    } finally {
      
    }
  }
 }

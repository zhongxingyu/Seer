 package amp.gel.dao.impl.accumulo;
 
 import java.util.List;
 
 import org.apache.accumulo.core.data.Mutation;
 
 import cmf.bus.Envelope;
 
 /**
 * Generates Accumulo mutations from envelopes.
  * 
  */
 public interface MutationsGenerator {
 	List<Mutation> generate(Envelope envelope);
 }

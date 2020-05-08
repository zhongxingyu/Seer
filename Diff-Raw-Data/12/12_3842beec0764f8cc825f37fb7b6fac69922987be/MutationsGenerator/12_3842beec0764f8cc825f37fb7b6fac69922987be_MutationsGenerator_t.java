 package amp.gel.dao.impl.accumulo;
 
 import java.util.List;
 
 import org.apache.accumulo.core.data.Mutation;
 
 import cmf.bus.Envelope;
 
 /**
 * Generates Accumulo mutations from envelopes. In most cases, one envelope will
 * correspond to one mutation, however this interface shouldn't overly constrain
 * the generator, so allowing one envelope to correspond to many mutations.
  * 
  */
 public interface MutationsGenerator {
 	List<Mutation> generate(Envelope envelope);
 }

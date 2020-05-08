 package nl.surfnet.bod.nbi.client;
 
 import java.util.List;
 
 import nl.surfnet.bod.nbi.client.generated.TerminationPoint;
 
 public interface NbiClient {
 
   /**
    * 
    * @return A {@link List} of {@link TerminationPoint}'s or null if no ports
   *         were found.
    */
   public List<TerminationPoint> findAllPorts();
 
   /**
    * 
    * @param name
    *          The name of the port
    * @return A {@link TerminationPoint} or <code>null</code> if nothing was
    *         found.
    */
   public TerminationPoint findPortsByName(final String name);
 
 }

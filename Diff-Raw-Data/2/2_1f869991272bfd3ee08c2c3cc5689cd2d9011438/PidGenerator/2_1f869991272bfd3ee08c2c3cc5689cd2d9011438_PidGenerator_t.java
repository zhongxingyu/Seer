 package dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator;
 
 
 
 /**
  * Pidgenerator factory class, and interface. Use the static #getPidGenerator() to
  * get an implementation of this class, and then the #generateNextAvailablePID
  * method to generate pids
  */
 public interface PidGenerator {
 
 
 
 
     /**
      * Get the next available pid with the prefix s
     * @param infix the prefix
      * @return the next available pid
      * @throws PIDGeneratorException Catch-all exception for everything that
      * can go wrong.
      */
     public abstract String generateNextAvailablePID(String infix) throws
                                                               PIDGeneratorException;
 
 }

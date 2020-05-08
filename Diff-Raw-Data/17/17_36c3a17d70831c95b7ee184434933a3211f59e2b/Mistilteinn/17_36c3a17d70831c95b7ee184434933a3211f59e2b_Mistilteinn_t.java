 package org.codefirst.mistilteinn;
 
 import java.io.IOException;
 
 import org.codefirst.mistilteinn.vcs.GitAdapter;
 
 /**
  * Mistilteinn Main API
  */
 public class Mistilteinn {
     /** target repository. */
     private GitAdapter gitAdapter;
 
     /**
      * constructor.
      * @param gitPath path to repository (.git)
      * @throws IOException
      */
     public Mistilteinn(String gitPath) throws IOException {
         this.gitAdapter = new GitAdapter(gitPath);
     }
 
     /**
      * change branch to id/#{ticketId}.
      * @param ticketId ticket id
      * @return true if success
     * @throws MistilteinnException exceptions
      */
     public void ticket(int ticketId) throws MistilteinnException {
         this.gitAdapter.ticket(ticketId);
     }
 }

 package org.codefirst.mistilteinn;
 
 import java.io.IOException;
 
 import org.codefirst.mistilteinn.vcs.GitAdapter;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
 
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
     * @throws InvalidRefNameException
     * @throws RefNotFoundException
     * @throws RefAlreadyExistsException
     * @throws JGitInternalException
      */
     public void ticket(int ticketId) throws MistilteinnException {
         this.gitAdapter.ticket(ticketId);
     }
 }

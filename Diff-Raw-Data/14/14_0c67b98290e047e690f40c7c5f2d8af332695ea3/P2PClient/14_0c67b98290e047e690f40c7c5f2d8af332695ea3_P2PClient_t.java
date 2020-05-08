 package org.lastbamboo.common.p2p;
 
 import java.io.IOException;
 
import javax.security.auth.login.CredentialException;

 import org.lastbamboo.common.offer.answer.Offerer;
 
 /**
  * General interface for P2P clients.
  */
 public interface P2PClient extends SocketFactory, Offerer {
     
     /**
      * Logs in to the server.
      * 
      * @param user The user name.
      * @param password The password.
      * @return The JID of the logged in user.
      * @throws IOException If we could not log in.
     * @throws CredentialException If the credentials are wrong.
      */
    String login(String user, String password) throws IOException, 
        CredentialException;
     
     /**
      * Logs in to the server.
      * 
      * @param user The user name.
      * @param password The password.
      * @param id An ID to use to identify this logged-in instance.
      * @return The JID of the logged in user.
      * @throws IOException If we could not log in.
     * @throws CredentialException If the credentials are wrong.
      */
    String login(String user, String password, String id) throws IOException, 
        CredentialException;
     
     /**
      * Logs out the currently logged in user.
      */
     void logout();
     
     /**
      * Adds a listener for changes to the state of p2p connections.
      * 
      * @param listener The listener to add.
      */
     void addConnectionListener(P2PConnectionListener listener);
     
 }

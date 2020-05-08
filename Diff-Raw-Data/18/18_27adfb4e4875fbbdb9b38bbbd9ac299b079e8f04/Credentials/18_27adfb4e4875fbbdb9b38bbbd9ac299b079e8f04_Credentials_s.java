 package no.feide.moria.directory;
 
 /**
  * Represents a set of user credentials, that is, a username/password pair. Used
  * for Moria authentication methods, and may be expanded to support other types
  * of credentials in a future version <br>
  * <br>
  * Note that this is a subset of the functionality offered by the Moria 1
  * <code>Credentials</code> class.
  */
 public class Credentials {
 
     /** Internal representation of the username. */
     private final String username;
 
     /** Internal representation of the user's password. */
     private final String password;
 
 
     /**
      * Constructor. Creates a new set of credentials consisting of a
      * username/password pair.
      * @param username
      *            The username. May not be <code>null</code> or an empty
      *            string.
      * @param password
      *            The user's password. May not be <code>null</code> or an
      *            empty string.
      * @throws NullPointerException
      *             If either <code>username</code> or <code>password</code>
     *             is <code>null</code> or an empty string.
      */
     public Credentials(final String username, final String password) {
 
         // Sanity checks.
        if ((username == null) || (username.length() == 0))
            throw new NullPointerException("User name cannot be NULL or an empty string");
        if ((password == null || password.length() == 0))
            throw new NullPointerException("Password cannot be NULL or an empty string");
 
         this.username = username;
         this.password = password;
 
     }
 
 
     /**
      * Retrieve the username part of the credentials.
      * @return A newly allocated <code>String</code> containing the username.
      */
     public String getUsername() {
 
         return new String(username);
 
     }
 
 
     /**
      * Retrieve the password part of the credentials.
      * @return A newly allocated <code>String</code> containing the password.
      */
     public String getPassword() {
 
         return new String(password);
 
     }
 
 }

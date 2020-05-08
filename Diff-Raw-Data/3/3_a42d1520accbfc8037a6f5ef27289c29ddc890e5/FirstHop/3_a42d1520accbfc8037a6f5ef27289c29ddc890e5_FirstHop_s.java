 package wknyc;
 
 import javax.jcr.Repository;
 import javax.jcr.Session;
 
 /**
  * First hope example. Logs in to a content repository and prints a status message
  */
 public class FirstHop {
 	/**
 	 * The main entry point of the example application.
 	 *
 	 * @param args command line arguments (ignored)
 	 * @throws Exception if an error occurs
 	 */
 	@SuppressWarnings("unchecked")
 	public static void main (String[] args) throws Exception {
		Class<Repository> repositoryClass = (Class<Repository>) Class.forName(Config.Repository());
 		Repository repository = repositoryClass.getConstructor().newInstance();
 		Session session = repository.login();
 		try {
 			String user = session.getUserID();
 			String name = repository.getDescriptor(Repository.REP_NAME_DESC);
 			System.out.println("Logged in as " + user + " to a " + name + " repository");
 		} finally {
 			session.logout();
 		}
 	}
 }

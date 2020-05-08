 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.net.URL;
 
 import javax.jcr.Binary;
 import javax.jcr.Node;
 import javax.jcr.Repository;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.modeshape.common.collection.Problems;
 import org.modeshape.jcr.ModeShapeEngine;
 import org.modeshape.jcr.RepositoryConfiguration;
 
 public class Tryout {
 	public static void main(String[] args) {
 		ModeShapeEngine engine = initEngine();
 		Repository repository = initRepository(engine);
 		
 		Session session = login(repository);
 		
 		writeData(session);
 
 
 	}
 
 	private static Session login(Repository repository) {
 		
 		Session session = null;
 		try {
 			session = repository.login("default");
 		} catch (RepositoryException e) {
 			System.out.println("There was an error.");
 			e.printStackTrace();
 		}
 		return session;
 	}
 
 	private static void writeData(Session session) {
 		try {
 			// Create the '/files' node that is an 'nt:folder' ...
 			Node root = session.getRootNode();
 			Node filesNode = root.addNode("files", "nt:folder");
 			InputStream stream =
 			    new BufferedInputStream(Tryout.class.getResourceAsStream("firefox-logo.png"));
 			// Create an 'nt:file' node at the supplied path ...
 			Node fileNode = filesNode.addNode("firefox-logo.jpg","nt:file");
 	
 			// Upload the file to that node ...
 			Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
 			Binary binary = session.getValueFactory().createBinary(stream);
 			contentNode.setProperty("jcr:data", binary);
 			session.save();
 		} catch (RepositoryException e) {
 			System.out.println("There was an error.");
 			e.printStackTrace();
 		}
 	}
 
 	private static Repository initRepository(ModeShapeEngine engine) {
 		Repository repository = null;
 		String repositoryName = null;
 		try {
 			URL url = Tryout.class.getClassLoader().getResource(
					"my-repository-config.json");
 			RepositoryConfiguration config = RepositoryConfiguration.read(url);
 			// Verify the configuration for the repository ...
 			Problems problems = config.validate();
 			if (problems.hasErrors()) {
 				System.err.println("Problems starting the engine.");
 				System.err.println(problems);
 				System.exit(-1);
 			}
 			// Deploy the repository ...
 			repository = engine.deploy(config);
 			repositoryName = config.getName();
 		} catch (Throwable e) {
 			e.printStackTrace();
 			System.exit(-1);
 			return null;
 		}
 		return repository;
 	}
 
 	private static ModeShapeEngine initEngine() {
 		ModeShapeEngine engine = new ModeShapeEngine();
 		engine.start();
 		return engine;
 	}
 }

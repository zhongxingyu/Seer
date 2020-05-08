 package org.innobuilt.fincayra.persistence;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.concurrent.TimeUnit;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.Property;
 import javax.jcr.PropertyIterator;
 import javax.jcr.Repository;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.Value;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryResult;
 
 import org.modeshape.common.collection.Problem;
 import org.modeshape.jcr.JcrConfiguration;
 import org.modeshape.jcr.JcrEngine;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 
 public class PersistenceManager {
 	private Repository repository;
 	private JcrEngine engine;
 	private static Logger LOGGER = LoggerFactory.getLogger(PersistenceManager.class);
 	
 	public void init() {
        if (engine != null) return; // already started
 
         // Load the configuration from a file, as provided by the user interface ...
         JcrConfiguration configuration = new JcrConfiguration();
         try {
 			configuration.loadFrom(this.getClass().getClassLoader().getResourceAsStream("configRepository.xml"));
 		} catch (MalformedURLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (SAXException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
         // Now create the JCR engine ...
         engine = configuration.build();
         engine.start();

         if (engine.getProblems().hasProblems()) {
             for (Problem problem : engine.getProblems()) {
                 System.err.println(problem.getMessageString());
             }
             throw new RuntimeException("Could not start due to problems");
         }
 		
		try {
 			repository = engine.getRepository("fincayra-repository");
 		} catch (RepositoryException e1) {
			// TODO Auto-generated catch block
 			e1.printStackTrace();
		};
 		
 		//Create a session so we get this repository up and running
 		Session s = null;
 		try {
 			s = getSession();
 		} catch (RepositoryException e) {
 			LOGGER.error("PROBLEM CONNECTING TO JCR-REPOSITORY", e);
 		} finally {
 			s.logout();
 		}
 	}
 	
 	public QueryResult find(Session session, String qryString) throws RepositoryException {
 		LOGGER.debug("EXECUTING XPATH QUERY : {}", qryString);
 		return session.getWorkspace().getQueryManager().createQuery(qryString, Query.XPATH).execute();
 	}
 	
 	public QueryResult find(Session session, String qryString, long offset, long limit) throws RepositoryException {
 		LOGGER.debug("EXECUTING XPATH QUERY : {}", qryString);
 		Query qry = session.getWorkspace().getQueryManager().createQuery(qryString, Query.XPATH);
 		qry.setOffset(offset);
 		qry.setLimit(limit);
 		return qry.execute();
 	}
 	
 
 	public Session getSession() throws RepositoryException {
 		//return repository.login(new SimpleCredentials("username", "password".toCharArray()));
 		return repository.login();
 	}
 
 	public Repository getRepository() {
 		return repository;
 	}
 
 	public void destroy() {
         if (engine == null) return;
         try {
             // Tell the engine to shut down, and then wait up to 5 seconds for it to complete...
             engine.shutdown();
            engine.awaitTermination(5, TimeUnit.SECONDS);
         } catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
             engine = null;
         }
 	}
 	
     /** Recursively outputs the contents of the given node. */
     public void dump(Node node) throws RepositoryException {
         // First output the node path
         System.out.println(node.getPath());
         // Skip the virtual (and large!) jcr:system subtree
         if (node.getName().equals("jcr:system")) {
             return;
         }
 
         // Then output the properties
         PropertyIterator properties = node.getProperties();
         while (properties.hasNext()) {
             Property property = properties.nextProperty();
             if (property.getDefinition().isMultiple()) {
                 // A multi-valued property, print all values
                 Value[] values = property.getValues();
                 for (int i = 0; i < values.length; i++) {
                     System.out.println(
                         property.getPath() + " = " + values[i].getString());
                 }
             } else {
                 // A single-valued property
                 System.out.println(
                     property.getPath() + " = " + property.getString());
             }
         }
 
         // Finally output all the child nodes recursively
         NodeIterator nodes = node.getNodes();
         while (nodes.hasNext()) {
             dump(nodes.nextNode());
         }
     }
 	
 }

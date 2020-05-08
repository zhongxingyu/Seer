 package net.metadata.auselit.lorestore.triplestore;
 
 import java.io.File;
 
 import net.metadata.auselit.lorestore.exceptions.OREDBConnectionException;
 
 import org.apache.log4j.Logger;
 import org.ontoware.rdf2go.model.ModelSet;
 import org.openrdf.rdf2go.RepositoryModelSet;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.repository.sail.SailRepositoryConnection;
 import org.openrdf.sail.nativerdf.NativeStore;
 
 /**
  * 
  * @author uqdayers
  *
  */
 public class NativeTripleStoreConnectorFactory implements
 		TripleStoreConnectorFactory {
 	private static final Logger LOG = Logger.getLogger(NativeTripleStoreConnectorFactory.class);
 
 	private SailRepository repo;
 	private String dataDirPath;
	private String sesameIndexes = "spoc,posc,cspo,ocsp";
 	
 	public ModelSet retrieveConnection() {
 		if (repo == null) {
 			initRepo();
 		}
 		ModelSet container = new RepositoryModelSet(repo);
 		container.open();
 		return container;
 	}
 
 	private void initRepo() {
 		File dataDir = new File(dataDirPath);
		NativeStore nativeStore = new NativeStore(dataDir, sesameIndexes);
 		repo = new SailRepository(nativeStore);
 		try {
 			repo.initialize();
 		} catch (RepositoryException e) {
 			throw new OREDBConnectionException("Error creating persistent sesame store", e);
 		}
 	}
 	
 	public long size() throws RepositoryException {
 		SailRepositoryConnection connection = repo.getConnection();
 		long size = connection.size();
 		connection.close();
 		return size;
 	}
 
 	public void setDataDirPath(String dataDirPath) {
 		this.dataDirPath = dataDirPath;
 	}
 
 	public String getDataDirPath() {
 		return dataDirPath;
 	}
 
 	public void release(ModelSet connection) throws InterruptedException {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void destroy() {
 		LOG.info("Shutting down SAIL repository");
 		try {
			if (repo != null) {
				repo.shutDown();
			}
 		} catch (RepositoryException e) {
 			LOG.error("Error shutting down sail repository", e);
 		}
 	}

	public void setSesameIndexes(String sesameIndexes) {
		this.sesameIndexes = sesameIndexes;
	}

	public String getSesameIndexes() {
		return sesameIndexes;
	}
 }

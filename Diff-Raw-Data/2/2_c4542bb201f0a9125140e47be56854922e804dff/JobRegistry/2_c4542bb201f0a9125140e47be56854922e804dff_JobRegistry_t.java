 /**
  * 
  */
 package uk.ac.ebi.fgpt.sampletab.subs;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry;
 import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry.Operation;
 import uk.ac.ebi.fg.biosd.model.organizational.MSI;
 import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.JobRegisterDAO;
 import com.jolbox.bonecp.BoneCPDataSource;
 
 /**
  * @author drashtti This class connects the job-registry column of the
  *         relational database with the subs-tracking database
  * 
  */
 public class JobRegistry {
 
 	private TrackingManager tm;
 	private Logger log;
 
 	public JobRegistry() {
 		tm = new TrackingManager();
 		log = LoggerFactory.getLogger(getClass());
 	}
 
 	public void getJobRegistry() throws SQLException, IOException {
 		/**EntityManagerFactory emf = Resources.getInstance()
 				.getEntityManagerFactory();*/
 		
 		Properties hibernateProperties = new Properties ();
 		ClassLoader loader = Thread.currentThread().getContextClassLoader();
 		hibernateProperties.load ( loader.getResourceAsStream ( "hibernate_bioSD.properties"));
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory ( "defaultPersistenceUnit" , hibernateProperties);
 		//new DbSchemaEnhancerProcessor ( entityManagerFactory ).enhance ();
 		EntityManager em = emf.createEntityManager();
 
 		JobRegisterDAO jrDao = new JobRegisterDAO(em);
 
 		List<JobRegisterEntry> logs = jrDao.find(1, MSI.class);
 		if (logs.size() != 1){
		log.error("find with entityType failed!");
 		}
 		for (JobRegisterEntry l : logs) {
 			String accession = l.getAcc();
 			Operation operation = l.getOperation();
 			Date timestamp = l.getTimestamp();
 			String id = getExpermentId(accession);
 			writeToDatabase(id, operation.toString(), timestamp);
 		}
 
 	}
 
 	private void writeToDatabase(String id, String operation, Date timestamp) {
 
 		String query = "INSERT INTO events (experiment_id, event_type,start_time, end_time) VALUES ( '"
 				+ id
 				+ "','RelationalDatabase_"
 				+ operation
 				+ "','"
 				+ timestamp
 				+ "','" + timestamp + "')";
 		PreparedStatement stmt = null;
 		Connection con = null;
 		try {
 			BoneCPDataSource ds1 = tm.getDataSource();
 			con = ds1.getConnection();
 			con.setAutoCommit(false);
 			stmt = con.prepareStatement(query);
 			// stmt = con.createStatement();
 			int change = stmt.executeUpdate(query);
 			con.commit();
 			log.info("Number of rows updated = " + change);
 		} catch (SQLException e) {
 			log.error(e.getMessage());
 			if (con != null) {
 				try {
 					log.error("Transaction is being rolled back");
 					con.rollback();
 				} catch (SQLException excep) {
 					log.error(excep.getMessage());
 				}
 			}
 		} catch (ClassNotFoundException e) {
 			log.error("The BoneCPDatasouce class for connection to the database cannot be found :"
 					+ e.getMessage());
 
 		} finally {
 			if (stmt != null) {
 				try {
 					stmt.close();
 					con.close();
 				} catch (SQLException e) {
 					log.error("Problem in closing the connection: "
 							+ e.getMessage());
 
 				}
 			}
 		}
 
 	}
 
 	private String getExpermentId(String accession) {
 		String id = null;
 		Statement stmt = null;
 		Connection con = null;
 		ResultSet rs = null;
 		String query = "SELECT id from experiments WHERE accession='"
 				+ accession + "'";
 		try {
 			BoneCPDataSource ds1 = tm.getDataSource();
 			con = ds1.getConnection();			
 			stmt = con.createStatement();
 			rs = stmt.executeQuery(query);
 			if (rs.isFirst() && rs.isLast()) {
 				id = rs.getString("id");
 			} else if (rs.first() != rs.last()) {
 				log.error("The experiment " + accession
 						+ "has multiple ids in the SubsTracking database");
 			} else {
 				log.error("The experiment "
 						+ accession
 						+ "does not have a corresponding id in the SubsTracking database");
 			}
 		}
 
 		catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			log.error("The BoneCPDatasouce class for connection to the database cannot be found :"
 					+ e.getMessage());
 		} finally {
 			if (stmt != null) {
 				try {
 					stmt.close();
 					rs.close();
 					con.close();
 				} catch (SQLException e) {
 					log.error("Problem in closing the statement: "
 							+ e.getMessage());
 
 				}
 
 			}
 		}
 
 		return id;
 	}
 
 }

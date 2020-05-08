 package cz.cuni.mff.odcleanstore.qualityassessment.rules;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import cz.cuni.mff.odcleanstore.connection.VirtuosoConnectionWrapper;
 import cz.cuni.mff.odcleanstore.connection.WrappedResultSet;
 import cz.cuni.mff.odcleanstore.connection.exceptions.ConnectionException;
 import cz.cuni.mff.odcleanstore.connection.exceptions.DatabaseException;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.qualityassessment.exceptions.QualityAssessmentException;
 
 import java.sql.*;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Rules Model.
  *
  * Facilitates changes and queries for quality assessment rules.
  *
  * @author Jakub Daniel
  */
 public class RulesModel {
 	private static final Logger LOG = LoggerFactory.getLogger(RulesModel.class);
 
 	private JDBCConnectionCredentials endpoint;
 	
 	public RulesModel (JDBCConnectionCredentials endpoint) {
 		this.endpoint = endpoint;
 	}
 	
 	private Collection<Rule> queryRules (String query, Object... objects) throws QualityAssessmentException {
 		Collection<Rule> rules = new ArrayList<Rule>();
 		
 		VirtuosoConnectionWrapper connection = null;
 		WrappedResultSet results = null;
 		
 		try {
 			connection = VirtuosoConnectionWrapper.createConnection(endpoint);
 			results = connection.executeSelect(query, objects);
 			
 			/**
 			 * Fill the collection with rule instances for all records in database.
 			 */
 			while (results.next()) {
 				ResultSet result = results.getCurrentResultSet();
 				
 				Integer id = result.getInt("id");
 				
 				Blob filterBlob = result.getBlob("filter");
 				String filter = new String(filterBlob.getBytes(1, (int)filterBlob.length()));
 				
 				Float coefficient = result.getFloat("coefficient");
 				
 				Blob descriptionBlob = result.getBlob("description");
 				String description = new String(descriptionBlob.getBytes(1, (int)descriptionBlob.length()));
 				
 				rules.add(new Rule(id, filter, coefficient, description));
 			}
 		} catch (DatabaseException e) {
 			throw new QualityAssessmentException(e.getMessage());
 		} catch (SQLException e) {
 			throw new QualityAssessmentException(e.getMessage());
 		} finally {
 			if (results != null) {
 				results.closeQuietly();
 			}
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (ConnectionException e) {
 					LOG.error("Rules Model connection not closed: " + e.getMessage());
 				}
 			}
 		}
 		
 		return rules;
 	}
 	
 	/**
      * Get rules applicable to graphs coming from a particular publisher.
      * 
      * @param group ID of the rule group
 	 *
 	 * @return a collection of rules applicable to a graph coming from a particular publisher.
      */
 	public Collection<Rule> getRules (int group) throws QualityAssessmentException {
 		
 		Collection<Rule> publisherSpecific = queryRules("SELECT * FROM " +
 					"DB.ODCLEANSTORE.QA_RULES WHERE groupId = ?", group);
 		
 		return publisherSpecific;
 	}
 	
 	/**
      * Get rules applicable to graphs coming from a particular publisher.
      * 
      * @param groupLabel the label of the group from which the rules are selected
 	 *
 	 * @return a collection of rules applicable to a graph coming from a particular publisher.
      */
 	public Collection<Rule> getRules (String groupLabel) throws QualityAssessmentException {
 		
 		Collection<Rule> publisherSpecific = queryRules("SELECT * FROM " +
 					"DB.ODCLEANSTORE.QA_RULES AS rules JOIN" +
 					"DB.ODCLEANSTORE.QA_RULES_GROUPS AS groups ON rules.groupId = groups.id" +
 					"WHERE groups.label = ?", groupLabel);
 		
 		return publisherSpecific;
 	}
 }

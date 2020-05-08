 package cz.cuni.mff.odcleanstore.qualityassessment.rules;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import cz.cuni.mff.odcleanstore.connection.EnumLogLevel;
 import cz.cuni.mff.odcleanstore.connection.VirtuosoConnectionWrapper;
 import cz.cuni.mff.odcleanstore.connection.WrappedResultSet;
 import cz.cuni.mff.odcleanstore.connection.exceptions.ConnectionException;
 import cz.cuni.mff.odcleanstore.connection.exceptions.DatabaseException;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.qualityassessment.exceptions.QualityAssessmentException;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.sql.ResultSet;
 import java.sql.Blob;
 import java.sql.SQLException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hp.hpl.jena.query.QueryException;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.OWL;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 import de.fuberlin.wiwiss.ng4j.impl.GraphReaderService;
 
 /**
  * Rules Model.
  *
  * Facilitates changes and queries for quality assessment rules.
  *
  * @author Jakub Daniel
  */
 public class RulesModel {
 	public static void main(String[] args) {
 		try {
 			new RulesModel(new JDBCConnectionCredentials("jdbc:virtuoso://localhost:1111/UID=dba/PWD=dba", "dba", "dba")).compileOntologyToRules(new FileInputStream(System.getProperty("user.home") + "/odcleanstore/public-contracts.ttl"), 1);
 		} catch (QualityAssessmentException e) {
 			System.err.println(e.getMessage());
 		} catch (FileNotFoundException e) {
 			System.err.println(e.getMessage());
 		}
 	}
 
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
 				
 				Integer groupId = result.getInt("groupId");
 				
 				Blob filterBlob = result.getBlob("filter");
 				String filter = new String(filterBlob.getBytes(1, (int)filterBlob.length()));
 				
 				Double coefficient = result.getDouble("coefficient");
 				
 				Blob descriptionBlob = result.getBlob("description");
 				String description = new String(descriptionBlob.getBytes(1, (int)descriptionBlob.length()));
 				
 				rules.add(new Rule(id, groupId, filter, coefficient, description));
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
      * @param groupIds IDs of the rule groups from which the rules are selected
      */
 	public Collection<Rule> getRules (Integer... groupIds) throws QualityAssessmentException {
 		Set<Rule> rules = new HashSet<Rule>();
 		
 		for (int i = 0; i < groupIds.length; ++i) {
 			Collection<Rule> groupSpecific = queryRules("SELECT id, groupId, filter, coefficient, description FROM " +
 					"DB.ODCLEANSTORE.QA_RULES WHERE groupId = ?", groupIds[i]);
 			
 			rules.addAll(groupSpecific);
 		}
 
 		return rules;
 	}
 	
 	/**
      * @param groupLabels set of labels of groups from which the rules are selected
      */
 	public Collection<Rule> getRules (String... groupLabels) throws QualityAssessmentException {
 		Set<Rule> rules = new HashSet<Rule>();
 		
 		for (int i = 0; i < groupLabels.length; ++i) {
 			Collection<Rule> groupSpecific = queryRules("SELECT rules.id AS id," +
 					"rules.groupId AS groupId," +
 					"rules.filter AS filter," +
 					"rules.coefficient AS coefficient," +
 					"rules.description AS description FROM " +
 					"DB.ODCLEANSTORE.QA_RULES AS rules JOIN " +
 					"DB.ODCLEANSTORE.QA_RULES_GROUPS AS groups ON rules.groupId = groups.id " +
 					"WHERE groups.label = ?", groupLabels[i]);
 			
 			rules.addAll(groupSpecific);
 		}
 		
 		return rules;
 	}
 	
 	public void compileOntologyToRules(InputStream ontology, Integer groupId) throws QualityAssessmentException {
 		Model model = ModelFactory.createOntologyModel();
 		
 		GraphReaderService reader = new GraphReaderService();
 
 		reader.setSourceInputStream(ontology, "");
 		reader.setLanguage("TURTLE");
 		reader.readInto(model);
 		
 		QueryExecution query = QueryExecutionFactory.create("SELECT ?s WHERE {?s ?p ?o} GROUP BY ?s", model);
 		
 		com.hp.hpl.jena.query.ResultSet resultSet = query.execSelect();
 		
 		while (resultSet.hasNext()) {
 			QuerySolution solution = resultSet.next();
 			
 			processOntologyResource(solution.getResource("s"), model, groupId);
 		}
 	}
 	
 	private void processOntologyResource(Resource resource, Model model, Integer groupId) throws QualityAssessmentException {
 		if (model.contains(resource, RDF.type, OWL.FunctionalProperty)) {	
 			Rule rule = new Rule(null, groupId, "{?s <" + resource.getURI() + "> ?o} GROUP BY ?s HAVING COUNT(?o) > 1", 0.8, resource.getLocalName() + " is FunctionalProperty (can have only 1 unique value)");
 			
 			storeRule(rule);
 		}
 		if (model.contains(resource, RDF.type, OWL.InverseFunctionalProperty)) {
 			Rule rule = new Rule(null, groupId, "{?s <" + resource.getURI() + "> ?o} GROUP BY ?o HAVING COUNT(?s) > 1", 0.8, resource.getLocalName() + " is InverseFunctionalProperty (value cannot be shared by two distinct subjects)");
 			
 			storeRule(rule);
 		}
 	}
 	
 	private void storeRule (Rule rule) throws QualityAssessmentException {
 		VirtuosoConnectionWrapper connection = null;
 		
 		try {
 			connection = VirtuosoConnectionWrapper.createConnection(endpoint);
 			
 			connection.adjustTransactionLevel(EnumLogLevel.TRANSACTION_LEVEL, false);
 			
 			connection.execute(String.format("INSERT INTO DB.ODCLEANSTORE.QA_RULES (groupId, filter, coefficient, description) VALUES (%d, '%s', %f, '%s')",
 					rule.getGroupId(), rule.getFilter(), rule.getCoefficient(), rule.getDescription()));
			connection.execute(String.format("INSERT INTO DB.ODCLEANSTORE.QA_RULES_TO_ONTOLOGIES_MAP (ruleId, ontology) VALUES (identity_value(), %s)", "http://TODO")); //TODO pass ontology URI
 			
 			connection.commit();
 		} catch (DatabaseException e) {
 			throw new QualityAssessmentException(e.getMessage());
 		} catch (QueryException e) {
 			throw new QualityAssessmentException(e.getMessage());
 		} catch (SQLException e) {
 			throw new QualityAssessmentException(e.getMessage());
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (ConnectionException e) {
 					LOG.error("Rules Model connection not closed: " + e.getMessage());
 				}
 			}
 		}
 	}
 }

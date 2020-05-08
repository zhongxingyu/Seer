 package cz.cuni.mff.odcleanstore.qualityassessment.rules;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import cz.cuni.mff.odcleanstore.configuration.BackendConfig;
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.connection.EnumLogLevel;
 import cz.cuni.mff.odcleanstore.connection.VirtuosoConnectionWrapper;
 import cz.cuni.mff.odcleanstore.connection.WrappedResultSet;
 import cz.cuni.mff.odcleanstore.connection.exceptions.DatabaseException;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.qualityassessment.exceptions.QualityAssessmentException;
 
 import java.sql.ResultSet;
 import java.sql.Blob;
 import java.sql.SQLException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import virtuoso.jena.driver.VirtModel;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.vocabulary.OWL;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
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
 			ConfigLoader.loadConfig();
 			BackendConfig config = ConfigLoader.getConfig().getBackendGroup();
 
 			new RulesModel(config.getCleanDBJDBCConnectionCredentials()).compileOntologyToRules("http://purl.org/procurement/public-contracts", 1);
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 		}
 	}
 	
 	private static final String ruleByGroupIdQueryFormat = "SELECT id, groupId, filter, coefficient, description FROM " +
 			"DB.ODCLEANSTORE.QA_RULES WHERE groupId = ?";
 	private static final String ruleByGroupLabelQueryFormat = "SELECT rules.id AS id," +
 			"rules.groupId AS groupId," +
 			"rules.filter AS filter," +
 			"rules.coefficient AS coefficient," +
 			"rules.description AS description FROM " +
 			"DB.ODCLEANSTORE.QA_RULES AS rules JOIN " +
 			"DB.ODCLEANSTORE.QA_RULES_GROUPS AS groups ON rules.groupId = groups.id " +
 			"WHERE groups.label = ?";
 	private static final String ontologyResourceQueryFormat = "SELECT ?s WHERE {?s ?p ?o} GROUP BY ?s";
 	private static final String deleteRulesByOntologyFormat = "DELETE FROM DB.ODCLEANSTORE.QA_RULES WHERE id IN " +
 			"(SELECT ruleId AS id FROM DB.ODCLEANSTORE.QA_RULES_TO_ONTOLOGIES_MAP WHERE ontology = ?)";
 	private static final String insertRuleFormat = "INSERT INTO DB.ODCLEANSTORE.QA_RULES (groupId, filter, coefficient, description) VALUES (?, ?, ?, ?)";
 	private static final String mapRuleToOntologyFormat = "INSERT INTO DB.ODCLEANSTORE.QA_RULES_TO_ONTOLOGIES_MAP (ruleId, ontology) VALUES (identity_value(), ?)";
 
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
 			throw new QualityAssessmentException(e);
 		} catch (SQLException e) {
 			throw new QualityAssessmentException(e);
 		} finally {
 			if (results != null) {
 				results.closeQuietly();
 			}
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (DatabaseException e) {
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
 			Collection<Rule> groupSpecific = queryRules(ruleByGroupIdQueryFormat, groupIds[i]);
 			
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
 			Collection<Rule> groupSpecific = queryRules(ruleByGroupLabelQueryFormat, groupLabels[i]);
 			
 			rules.addAll(groupSpecific);
 		}
 		
 		return rules;
 	}
 	
 	public void compileOntologyToRules(String ontologyUri, Integer groupId) throws QualityAssessmentException {
 		VirtModel ontology = VirtModel.openDatabaseModel(ontologyUri,
 				endpoint.getConnectionString(),
 				endpoint.getUsername(),
 				endpoint.getPassword());
 		
 		QueryExecution query = QueryExecutionFactory.create(ontologyResourceQueryFormat, ontology);
 		
 		com.hp.hpl.jena.query.ResultSet resultSet = query.execSelect();
 		
 		dropRules(ontologyUri);
 		
 		while (resultSet.hasNext()) {
 			QuerySolution solution = resultSet.next();
 			
 			processOntologyResource(solution.getResource("s"), ontology, ontologyUri, groupId);
 		}
 	}
 	
 	private void dropRules(String ontology) throws QualityAssessmentException {
 		VirtuosoConnectionWrapper connection = null;
 		
 		try {
 			connection = VirtuosoConnectionWrapper.createConnection(endpoint);
 			
 			connection.execute(deleteRulesByOntologyFormat, ontology);
 			
 		} catch (DatabaseException e) {
 			throw new QualityAssessmentException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (DatabaseException e) {
 					LOG.error("Rules Model connection not closed: " + e.getMessage());
 				}
 			}
 		}
 	}
 	
 	private void processOntologyResource(Resource resource, Model model, String ontology, Integer groupId) throws QualityAssessmentException {
 		final String skosNS = "http://www.w3.org/2004/02/skos/core#";
 
 		/**
 		 * Functional Property can have only 1 value
 		 */
 		if (model.contains(resource, RDF.type, OWL.FunctionalProperty)) {	
 			Rule rule = new Rule(null, groupId, "{?s <" + resource.getURI() + "> ?o} GROUP BY ?s HAVING COUNT(?o) > 1", 0.8, resource.getLocalName() + " is FunctionalProperty (can have only 1 unique value)");
 			
 			storeRule(rule, ontology);
 		}
 
 		/**
 		 * Value of Inverse Functional Property cannot be shared by two or more subjects
 		 */
 		if (model.contains(resource, RDF.type, OWL.InverseFunctionalProperty)) {
 			Rule rule = new Rule(null, groupId, "{?s <" + resource.getURI() + "> ?o} GROUP BY ?o HAVING COUNT(?s) > 1", 0.8, resource.getLocalName() + " is InverseFunctionalProperty (value cannot be shared by two distinct subjects)");
 			
 			storeRule(rule, ontology);
 		}
 
 		/**
 		 * Enumeration check based on Concept Scheme
 		 */
 		if (model.contains(resource, RDF.type, model.createProperty(skosNS, "ConceptScheme"))) {
 			StmtIterator enumerations = model.listStatements(resource, model.createProperty(skosNS, "hasTopConcept"), model.getRDFNode(Node.ANY));
 
 			/**
 			 * Generate list of possible values
 			 */
 			StringBuilder valueList = new StringBuilder();
 			StringBuilder filter = new StringBuilder();
 			
 			while (enumerations.hasNext()) {	
 				Statement conceptStmt = enumerations.next();
 
 				if (model.contains(conceptStmt.getObject().asResource(), RDF.type, model.createProperty(skosNS, "Concept"))) {
 					valueList.append(conceptStmt.getObject().asNode().getURI());
 					filter.append("?o != <" + conceptStmt.getObject().asNode().getURI() + ">");
 					
 					if (enumerations.hasNext()) {
 						valueList.append(", ");
 						filter.append(" AND ");
 					}
 				} else {
 					throw new QualityAssessmentException("Missing definition of a Concept in ConceptScheme");
 				}
 			}
 
 			/**
 			 * Generate rules for all properties with enumerated range
 			 */
 			QueryExecution queryExecution = QueryExecutionFactory.create("SELECT ?s WHERE {?s <" + RDFS.range + "> [<" + OWL.hasValue + "> <" + resource.getURI() + ">].}", model);
 			
 			com.hp.hpl.jena.query.ResultSet resultSet = queryExecution.execSelect();
 			
 			while (resultSet.hasNext()) {
 				QuerySolution solution = resultSet.nextSolution();
 
 				Rule rule = new Rule(null, groupId, "{?s <" + solution.get("s") + "> ?o. FILTER (" + filter + ")}", 0.8, solution.getResource("s").getLocalName() + " can have only these values: " + valueList.toString());
 
 				storeRule(rule, ontology);
 			}
 		}
 	}
 	
 	private void storeRule (Rule rule, String ontology) throws QualityAssessmentException {
 		VirtuosoConnectionWrapper connection = null;
 		
 		try {
 			connection = VirtuosoConnectionWrapper.createConnection(endpoint);
 
 			connection.adjustTransactionLevel(EnumLogLevel.TRANSACTION_LEVEL, false);
 
 			connection.execute(insertRuleFormat, rule.getGroupId(), rule.getFilter(), rule.getCoefficient(), rule.getDescription());
 			connection.execute(mapRuleToOntologyFormat, ontology);
 			
 			connection.commit();
 			
 			LOG.info("Generated quality assessment rule from ontology " + ontology);
 		} catch (DatabaseException e) {
 			throw new QualityAssessmentException(e);
 		} catch (SQLException e) {
 			throw new QualityAssessmentException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (DatabaseException e) {
 					LOG.error("Rules Model connection not closed: " + e.getMessage());
 				}
 			}
 		}
 	}
 }

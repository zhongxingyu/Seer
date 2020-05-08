 package cz.cuni.mff.odcleanstore.webfrontend.dao.onto;
 
 import java.io.ByteArrayOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import org.apache.log4j.Logger;
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 
 import virtuoso.jena.driver.VirtGraph;
 
import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
import cz.cuni.mff.odcleanstore.configuration.exceptions.ConfigurationException;
 import cz.cuni.mff.odcleanstore.datanormalization.exceptions.DataNormalizationException;
 import cz.cuni.mff.odcleanstore.datanormalization.rules.DataNormalizationRulesModel;
 import cz.cuni.mff.odcleanstore.qualityassessment.exceptions.QualityAssessmentException;
 import cz.cuni.mff.odcleanstore.qualityassessment.rules.QualityAssessmentRulesModel;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.onto.Ontology;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoForEntityWithSurrogateKey;
 
 public class OntologyDao extends DaoForEntityWithSurrogateKey<Ontology> 
 {	
 	public static final String TABLE_NAME = TABLE_NAME_PREFIX + "ONTOLOGIES";
 	private static final String OUTPUT_LANGUAGE = "RDF/XML-ABBREV";
 	private static final String ENCODING = "UTF-8";
 	private static final String GRAPH_NAME_PREFIX = "http://opendata.cz/infrastructure/odcleanstore/ontologies/";
 	private static final String RULE_GROUP_PREFIX = "Generated from ontology: ";
 	private static final String QA_GROUPS_TABLE_NAME = "QA_RULES_GROUPS";
 	private static final String DN_GROUPS_TABLE_NAME = "DN_RULES_GROUPS";
 	private static final String QA_MAPPING_TABLE_NAME = "QA_RULES_GROUPS_TO_ONTOLOGIES_MAP";
 	private static final String DN_MAPPING_TABLE_NAME = "DN_RULES_GROUPS_TO_ONTOLOGIES_MAP";
 	
 
 	protected static Logger logger = Logger.getLogger(OntologyDao.class);
 	
 	private static final long serialVersionUID = 1L;
 	
 	private ParameterizedRowMapper<Ontology> rowMapper;
 	
 	public OntologyDao()
 	{
 		this.rowMapper = new OntologyRowMapper();
 	}
 
 	@Override
 	public String getTableName()
 	{
 		return TABLE_NAME;
 	}
 
 	@Override
 	protected ParameterizedRowMapper<Ontology> getRowMapper() 
 	{
 		return this.rowMapper;
 	}
 	
 	@Override
 	public Ontology loadRawBy(String columnName, Object value)
 	{
 		Ontology ontology = super.loadRawBy(columnName, value) ;
 			
 		ontology.setRdfData(loadRdfData(ontology.getGraphName()));
 		
 		return ontology;
 	}
 	
 	private String loadRdfData(String graphName) 
 	{	
 		logger.debug("Loading RDF graph: " + graphName);
 		
 		VirtGraph graph = new VirtGraph(graphName, this.lookupFactory.getDataSource());
 		Model model = ModelFactory.createModelForGraph(graph);
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		model.write(stream, OUTPUT_LANGUAGE);
 		String result = null;
 		try 
 		{
 			result = stream.toString(ENCODING);
 		} catch (UnsupportedEncodingException e) 
 		{
 			//TODO handle
 		}
 		return result;
 	}
 	
 	@Override
 	public void save(Ontology item) 
 	{
 		String query = "INSERT INTO " + TABLE_NAME + " (label, description, graphName) VALUES (?, ?, ?)";
 		
 		createGraphName(item);
 		Object[] params =
 		{
 			item.getLabel(),
 			item.getDescription(),
 			item.getGraphName()
 		};
 		
 		logger.debug("label: " + item.getLabel());
 		logger.debug("description: " + item.getDescription());
 		logger.debug("graphName" + item.getGraphName());
 		
 		getJdbcTemplate().update(query, params);
 		
 		// to be able to drop a graph in Virtuoso, it has to be explicitly created before
 		createGraph(item.getGraphName());
 		
 		storeRdfXml(item.getRdfData(), item.getGraphName());
 		
		try
		{
			ConfigLoader.loadConfig();
		
			generateRules(QA_GROUPS_TABLE_NAME, item.getLabel());
			generateRules(DN_GROUPS_TABLE_NAME, item.getLabel());
		} catch (ConfigurationException e) {
			// TODO:
		}
 	}
 	
 	private void createGraphName(Ontology item) 
 	{
 		try {
 			item.setGraphName(GRAPH_NAME_PREFIX + URLEncoder.encode(item.getLabel(), ENCODING));
 		} catch (UnsupportedEncodingException e) {
 			// TODO handle
 		}
 	}
 	
 	private void createGraph(String graphName) 
 	{
 		String query = "SPARQL CREATE SILENT GRAPH ??";
 		
 		Object[] params = { graphName };
 		
 		getJdbcTemplate().update(query, params);
 	}
 	
 	private void storeRdfXml(String rdfData, String graphName) 
 	{
 		String query = "CALL DB.DBA.RDF_LOAD_RDFXML_MT(?, '', ?)";
 		
 		Object[] params =
 		{
 			rdfData,
 			graphName
 		};
 		
 		getJdbcTemplate().update(query, params);
 	}
 	
 	@Override
 	public void update(Ontology item) throws Exception
 	{	
 		delete(item);
 		createGraphName(item);
 		save(item);
 	}
 	
 	private void deleteGraph(String graphName) 
 	{
 		String query = "SPARQL DROP SILENT GRAPH ??";
 		
 		Object[] params = { graphName };
 		
 		getJdbcTemplate().update(query, params);
 	}
 	
 	@Override
 	public void delete(Ontology item) throws Exception 
 	{
 		deleteRaw(item.getId());
 		deleteGraph(item.getGraphName());
 	}
 	
 	private void generateRules(String tableName, String ontologyLabel) 
 	{
 		String groupLabel = createGroupLabel(ontologyLabel);
 		createRulesGroup(tableName, groupLabel);
 		
 		Ontology ontologyWithId = super.loadRawBy("label", ontologyLabel);
 		Long ontologyId = ontologyWithId.getId();
 		Long groupId = getGroupId(tableName, groupLabel);
 		
 		createMapping(createMappingTableName(tableName), groupId, ontologyId);
	
 		if (QA_GROUPS_TABLE_NAME.equals(tableName)) {
 			QualityAssessmentRulesModel rulesModel = new QualityAssessmentRulesModel(this.lookupFactory.getDataSource());
 			
 			try {
 				rulesModel.compileOntologyToRules(ontologyId, groupId);
 			} catch (QualityAssessmentException e) {
 				// TODO: Handle it properly
 			}
 		} else {
 			DataNormalizationRulesModel rulesModel = new DataNormalizationRulesModel(this.lookupFactory.getDataSource());

 			try {
 				rulesModel.compileOntologyToRules(ontologyId, groupId);
 			} catch (DataNormalizationException e) {
 				// TODO: Handle it properly
 			}
 		}
 	}
 	
 	private String createGroupLabel(String ontologyLabel) 
 	{
 		return RULE_GROUP_PREFIX + ontologyLabel;
 	}
 	
 	private void createRulesGroup(String tableName, String groupLabel) 
 	{
 		String query = "INSERT INTO " + TABLE_NAME_PREFIX + tableName + " (label) VALUES (?)";
 		
 		Object[] params = { groupLabel };
 		
 		logger.debug("groupName" + groupLabel);
 		
 		getJdbcTemplate().update(query, params);
 	}
 	
 	private String createMappingTableName(String groupTableName) 
 	{
 		if (QA_GROUPS_TABLE_NAME.equals(groupTableName)) {
 			return QA_MAPPING_TABLE_NAME;
 		} else {
 			return DN_MAPPING_TABLE_NAME;
 		}
 	}
 	
 	private Long getGroupId(String tableName, String groupLabel) 
 	{
 		String query = "SELECT id FROM " + TABLE_NAME_PREFIX + tableName + " WHERE label = ?";
 		
 		Object[] params = { groupLabel };
 		
 		return getJdbcTemplate().queryForObject(query, params, Long.class);
 	}
 	
 	private void createMapping(String tableName, Long groupId, Long ontologyId)
 	{
 		String query = "INSERT INTO " + TABLE_NAME_PREFIX + tableName + " (groupId, ontologyId) VALUES (?, ?)";
 		
 		Object[] params = 
 		{ 
 			groupId,
 			ontologyId
 		};
 		
 		logger.debug("groupId" + groupId);
 		logger.debug("ontologyId" + ontologyId);
 		
 		getJdbcTemplate().update(query, params);
 	}
 }

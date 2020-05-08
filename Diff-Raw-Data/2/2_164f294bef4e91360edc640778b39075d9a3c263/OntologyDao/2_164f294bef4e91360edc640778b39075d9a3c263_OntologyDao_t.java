 package cz.cuni.mff.odcleanstore.webfrontend.dao.onto;
 
 import java.io.ByteArrayOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import org.apache.log4j.Logger;
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 
 import virtuoso.jena.driver.VirtGraph;
 
 import cz.cuni.mff.odcleanstore.webfrontend.bo.onto.Ontology;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.DaoForEntityWithSurrogateKey;
 
 public class OntologyDao extends DaoForEntityWithSurrogateKey<Ontology> 
 {	
 	public static final String TABLE_NAME = TABLE_NAME_PREFIX + "ONTOLOGIES";
 	private static final String OUTPUT_LANGUAGE = "RDF/XML-ABBREV";
 	private static final String ENCODING = "UTF-8";
 	public static final String GRAPH_NAME_PREFIX = "http://opendata.cz/infrastructure/odcleanstore/ontologies/";
 
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
 		
 		try {
 			item.setGraphName(GRAPH_NAME_PREFIX + URLEncoder.encode(item.getLabel(), ENCODING));
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
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
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
 	public void update(Ontology item)
 	{
 		String query =
 			"UPDATE " + TABLE_NAME + 
 			" SET label = ?, description = ?" +
 			" WHERE id = ?";
 		
 		Object[] params =
 		{
 			item.getLabel(),
 			item.getDescription(),
 			item.getId(),
 		};
 		
 		logger.debug("label: " + item.getLabel());
 		logger.debug("description: " + item.getDescription());
 		logger.debug("id: " + item.getId());
 		
 		getJdbcTemplate().update(query, params);
 		
 		deleteGraph(item.getGraphName());
 		
 		storeRdfXml(item.getRdfData(), item.getGraphName());
 	}
 	
 	private void deleteGraph(String graphName) 
 	{
 		String query = "SPARQL DROP SILENT GRAPH ??";
 		
 		Object[] params = { graphName };
 		
 		getJdbcTemplate().update(query, params);
 	}
 	
 	@Override
 	public void delete(Ontology item) throws Exception {
 		deleteRaw(item.getId());
 		deleteGraph(item.getGraphName());
 	}
 }

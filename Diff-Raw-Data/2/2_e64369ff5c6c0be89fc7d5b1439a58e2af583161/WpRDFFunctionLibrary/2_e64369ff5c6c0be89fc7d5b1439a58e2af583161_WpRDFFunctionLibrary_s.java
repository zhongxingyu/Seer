 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.rmi.RemoteException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.rpc.ServiceException;
 import javax.xml.stream.XMLEventFactory;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.XMLEvent;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.io.FilenameUtils;
 import org.bridgedb.DataSource;
 import org.bridgedb.IDMapper;
 import org.bridgedb.IDMapperException;
 import org.bridgedb.IDMapperStack;
 import org.bridgedb.Xref;
 import org.bridgedb.bio.BioDataSource;
 import org.pathvisio.model.ConverterException;
 import org.pathvisio.wikipathways.WikiPathwaysClient;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.DC;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import com.hp.hpl.jena.vocabulary.XSD;
 
 import de.fuberlin.wiwiss.ng4j.swp.vocabulary.FOAF;
 
 
 public class WpRDFFunctionLibrary {
 	public static WikiPathwaysClient startWpApiClient() throws MalformedURLException, ServiceException {
 		return new WikiPathwaysClient(new URL("http://www.wikipathways.org/wpi/webservice/webservice.php"));
 	}
 	public static IDMapperStack createBridgeDbMapper(Properties prop) throws ClassNotFoundException, IDMapperException{
 		BioDataSource.init();
 		Class.forName("org.bridgedb.rdb.IDMapperRdb");
 		File dir = new File(prop.getProperty("bridgefiles")); //TODO Get Refector to get them directly form bridgedb.org
 		FilenameFilter filter = new FilenameFilter() {
 		    public boolean accept(File dir, String name) {
 		        return name.toLowerCase().endsWith(".txt");
 		    }
 		};
 	
 		File[] bridgeDbFiles = dir.listFiles(filter);
 		IDMapperStack mapper = new IDMapperStack();
 		for (File bridgeDbFile : bridgeDbFiles) {
 			System.out.println(bridgeDbFile.getAbsolutePath());
 			mapper.addIDMapper("idmapper-pgdb:" + bridgeDbFile.getAbsolutePath());
 		}
 		return mapper;
 	}
 	
 	public static Model createBridgeDbModel() throws ClassNotFoundException, IDMapperException, FileNotFoundException{
 		Model bridgeDbmodel = ModelFactory.createDefaultModel();
 		InputStream in = new FileInputStream("/tmp/BioDataSource.ttl");
 		bridgeDbmodel.read(in, "", "TURTLE");
 		return bridgeDbmodel;
 	}
 	
 	public static void setModelPrefix(Model model){
 		model.setNsPrefix("biopax", Biopax_level3.getURI());
 		model.setNsPrefix("gpml", Gpml.getURI());
 		model.setNsPrefix("wp", Wp.getURI());
 		model.setNsPrefix("xsd", XSD.getURI());
 		model.setNsPrefix("rdf", RDF.getURI());
 		model.setNsPrefix("rdfs", RDFS.getURI());
 		model.setNsPrefix("dcterms", DCTerms.getURI());
 		model.setNsPrefix("wprdf", "http://rdf.wikipathways.org/");
 		model.setNsPrefix("pubmed", "http://www.ncbi.nlm.nih.gov/pubmed/");
 		model.setNsPrefix("foaf", FOAF.getURI());
 		model.setNsPrefix("ncbigene", "http://identifiers.org/ncbigene/");
 		model.setNsPrefix("cas", "http://identifiers.org/cas/");
 		model.setNsPrefix("dc", DC.getURI());
 		model.setNsPrefix("skos", Skos.getURI());
 		model.setNsPrefix("void", Void.getURI());
 		model.setNsPrefix("wprdf", "http://rdf.wikipathways.org/");
 		model.setNsPrefix("pav", Pav.getURI());
 		model.setNsPrefix("prov", Prov.getURI());
 		model.setNsPrefix("dcterms", DCTerms.getURI());
 		model.setNsPrefix("hmdb", "http://identifiers.org/hmdb/");
 		model.setNsPrefix("freq", Freq.getURI());
 	}
 	
 	public static void populateVoid(Model voidModel,HashMap<String, String> organismTaxonomy){
 		//Populate void.ttl
 		Calendar now = Calendar.getInstance();
 		Literal nowLiteral = voidModel.createTypedLiteral(now);
 		Literal titleLiteral = voidModel.createLiteral("WikiPathways-RDF VoID Description", "en");
 		Literal descriptionLiteral = voidModel.createLiteral("This is the VoID description for a WikiPathwyas-RDF dataset.", "en");
 		Resource voidBase = voidModel.createResource("http://rdf.wikipathways.org/");
 		Resource identifiersOrg = voidModel.createResource("http://identifiers.org");
 		Resource wpHomeBase = voidModel.createResource("http://www.wikipathways.org/");
 		Resource authorResource = voidModel.createResource("http://orcid.org/0000-0001-9773-4008");
 		Resource apiResource = voidModel.createResource("http://www.wikipathways.org/wpi/webservice/webservice.php");
 		Resource mainDatadump = voidModel.createResource("http://rdf.wikipathways.org/wpContent.ttl.gz");
 		Resource license = voidModel.createResource("http://creativecommons.org/licenses/by/3.0/");
 		Resource instituteResource = voidModel.createResource("http://dbpedia.org/page/Maastricht_University");
 		voidBase.addProperty(RDF.type, Void.Dataset);
 		voidBase.addProperty(DCTerms.title, titleLiteral);
 		voidBase.addProperty(DCTerms.description, descriptionLiteral);
 		voidBase.addProperty(FOAF.homepage, wpHomeBase);
 		voidBase.addProperty(DCTerms.license, license);
 		voidBase.addProperty(Void.uriSpace, voidBase);
 		voidBase.addProperty(Void.uriSpace, identifiersOrg);
 		voidBase.addProperty(Pav.importedBy, authorResource);
 		voidBase.addProperty(Pav.importedFrom, apiResource);
 		voidBase.addProperty(Pav.importedOn, nowLiteral);
 		voidBase.addProperty(Void.dataDump, mainDatadump);
 		voidBase.addProperty(Voag.frequencyOfChange, Freq.Irregular);
 		voidBase.addProperty(Pav.createdBy, authorResource);
 		voidBase.addProperty(Pav.createdAt, instituteResource);		 
 		voidBase.addLiteral(Pav.createdOn, nowLiteral);
 		voidBase.addProperty(DCTerms.subject, Biopax_level3.Pathway);
 		voidBase.addProperty(Void.exampleResource, voidModel.createResource("http://identifiers.org/ncbigene/2678"));
 		voidBase.addProperty(Void.exampleResource, voidModel.createResource("http://identifiers.org/pubmed/15215856"));
 		voidBase.addProperty(Void.exampleResource, voidModel.createResource("http://identifiers.org/hmdb/HMDB02005"));
 		voidBase.addProperty(Void.exampleResource, voidModel.createResource("http://rdf.wikipathways.org/WP15"));
 		voidBase.addProperty(Void.exampleResource, voidModel.createResource("http://identifiers.org/obo.chebi/17242"));
 
 		for (String organism : organismTaxonomy.values()) {
 			voidBase.addProperty(DCTerms.subject, voidModel.createResource("http://dbpedia.org/page/"+organism.replace(" ", "_")));
 		}
 		voidBase.addProperty(Void.vocabulary, Biopax_level3.NAMESPACE);
 		voidBase.addProperty(Void.vocabulary, voidModel.createResource(Wp.getURI()));
 		voidBase.addProperty(Void.vocabulary, voidModel.createResource(Gpml.getURI()));
 		voidBase.addProperty(Void.vocabulary, FOAF.NAMESPACE);
 		voidBase.addProperty(Void.vocabulary, Pav.NAMESPACE);
 	}
 	
 	public static Model createPathwayModel(){
 		Model pathwayModel = ModelFactory.createDefaultModel();
 		setModelPrefix(pathwayModel);
 		return pathwayModel;
 	}
 
 	public static void addBdbLinkSets(Model openPhactsLinkSets, IDMapper  mapper, Xref idXref, Resource internalWPDataNodeResource) throws IDMapperException {
 		Set<Xref> bdbLinkSetsIdXref = mapper.mapID(idXref);
 		Iterator<Xref> iter = bdbLinkSetsIdXref.iterator();
 		while (iter.hasNext()){
 			Xref linkedId = (Xref) iter.next();
 			String linkedIdString = linkedId.getId();
 			//System.out.println(linkedId.getURN());
 			if (linkedId.getURN().contains("urn")){
 				//System.out.println(linkedId.getURN().split(":").length);
 				String linkedIdSource = linkedId.getURN().split(":")[2];
 				//System.out.println("http://identifiers.org/"+linkedIdSource+"/"+linkedIdString);
 				Resource linkedIdResource = openPhactsLinkSets.createResource("http://identifiers.org/"+linkedIdSource+"/"+linkedIdString);
 				internalWPDataNodeResource.addProperty(Skos.related, linkedIdResource);
 			}
 		}
 	}
 
 	public static void getUnifiedIdentifiers(Model model, IDMapper  mapper, Xref idXref, Resource internalWPDataNodeResource) throws IDMapperException, UnsupportedEncodingException {
 		//ENSEMBL
 		Set<Xref> unifiedEnsemblIdXref = mapper.mapID(idXref, BioDataSource.ENSEMBL);
 		Iterator<Xref> iter = unifiedEnsemblIdXref.iterator();
 		while (iter.hasNext()){
 			Xref unifiedId = (Xref) iter.next();
 			String unifiedEnsemblDataNodeIdentifier = URLEncoder.encode(unifiedId.getId(), "UTF-8");
 			Resource unifiedEnsemblIdResource = model.createResource("http://identifiers.org/ensembl/"+unifiedEnsemblDataNodeIdentifier);
 			internalWPDataNodeResource.addProperty(Wp.bdbEnsembl, unifiedEnsemblIdResource);
 		}
 		//Uniprot
 		Set<Xref> unifiedUniprotIdXref = mapper.mapID(idXref, DataSource.getBySystemCode("S"));
 		Iterator<Xref> iterUniprot = unifiedUniprotIdXref.iterator();
 		while (iterUniprot.hasNext()){
 			Xref unifiedUniprotId = (Xref) iterUniprot.next();
 			String unifiedUniprotDataNodeIdentifier = URLEncoder.encode(unifiedUniprotId.getId(), "UTF-8");
 			Resource unifiedUniprotIdResource = model.createResource("http://identifiers.org/uniprot/"+unifiedUniprotDataNodeIdentifier);
 			internalWPDataNodeResource.addProperty(Wp.bdbUniprot, unifiedUniprotIdResource);
 		}
 		//Entrez Gene
 		Set<Xref> unifiedEntrezGeneIdXref = mapper.mapID(idXref, BioDataSource.ENTREZ_GENE);
 		Iterator<Xref> iterEntrezGene = unifiedEntrezGeneIdXref.iterator();
 		while (iterEntrezGene.hasNext()){
 			Xref unifiedEntrezGeneId = (Xref) iterEntrezGene.next();
 			String unifiedEntrezGeneDataNodeIdentifier = URLEncoder.encode(unifiedEntrezGeneId.getId(), "UTF-8");
 			Resource unifiedEntrezGeneIdResource = model.createResource("http://identifiers.org/entrez.gene/"+unifiedEntrezGeneDataNodeIdentifier);
 			internalWPDataNodeResource.addProperty(Wp.bdbEntrezGene, unifiedEntrezGeneIdResource);
 		}
 		//HMDB
 		Set<Xref> unifiedHmdbIdXref = mapper.mapID(idXref, BioDataSource.HMDB);
 		Iterator<Xref> iterHmdb = unifiedHmdbIdXref.iterator();
 		while (iterHmdb.hasNext()){
 			Xref unifiedHmdbId = (Xref) iterHmdb.next();
 			String unifiedHmdbDataNodeIdentifier = URLEncoder.encode(unifiedHmdbId.getId(),"UTF-8");
 			Resource unifiedHmdbIdResource = model.createResource("http://identifiers.org/hmdb/"+unifiedHmdbDataNodeIdentifier);
 			internalWPDataNodeResource.addProperty(Wp.bdbHmdb, unifiedHmdbIdResource);
 			//createCHEMINFBits(model,
 			//		internalWPDataNodeResource, CHEMINF.CHEMINF_000408, unifiedHmdbDataNodeIdentifier
 			//);
 		}
 		//CHEMSPIDER
 		Set<Xref> unifiedChemspiderIdXref = mapper.mapID(idXref, BioDataSource.CHEMSPIDER);
 		Iterator<Xref> iterChemspider = unifiedChemspiderIdXref.iterator();
 		while (iterChemspider.hasNext()){
 			Xref unifiedChemspiderId = (Xref) iterChemspider.next();
 			String unifiedChemspiderDataNodeIdentifier = URLEncoder.encode(unifiedChemspiderId.getId(), "UTF-8");
 			Resource unifiedChemspiderIdResource = model.createResource("http://identifiers.org/chemspider/"+unifiedChemspiderDataNodeIdentifier);
 			internalWPDataNodeResource.addProperty(Wp.bdbChemspider, unifiedChemspiderIdResource);
 			//createCHEMINFBits(model,
 			//		internalWPDataNodeResource, CHEMINF.CHEMINF_000405, unifiedChemspiderDataNodeIdentifier
 			//);
 		}
 
 	}
 
 	public static Document addWpProvenance(Document currentGPML, String wpIdentifier, String wpRevision) throws ConverterException, ParserConfigurationException, SAXException, IOException{	
 		Element pathwayElement = (Element) currentGPML.getElementsByTagName("Pathway").item(0);
 		pathwayElement.setAttribute("identifier", wpIdentifier);
 		pathwayElement.setAttribute("revision", wpRevision);
 		return currentGPML;
 	}
 
 	public static HashMap<String, String> getOrganismsTaxonomyMapping() throws ServiceException, ParserConfigurationException, SAXException, IOException{
 		HashMap<String, String> hm = new HashMap<String, String>();
 		WikiPathwaysClient wpClient = startWpApiClient();
 		String[] wpOrganisms = wpClient.listOrganisms();
 		for (String organism : wpOrganisms){
 			Document taxonomy = basicCalls.openXmlURL(constants.getEUtilsUrl("taxonomy", organism.replace(" ", "_")));
 			String ncbiTaxonomy = taxonomy.getElementsByTagName("Id").item(0).getTextContent().trim();
 			hm.put(organism, ncbiTaxonomy);
 		}
 		return hm;
 	}
 
 	public static Resource addVoidTriples(Model voidModel, Resource voidBase, Node pathwayNode, WikiPathwaysClient client) throws ParseException, RemoteException{
 		String wpIdentifier = pathwayNode.getAttributes().getNamedItem("identifier").getTextContent().trim();
 		String wpRevision = pathwayNode.getAttributes().getNamedItem("revision").getTextContent().trim();
 		String pathwayName = pathwayNode.getAttributes().getNamedItem("Name").getTextContent().trim();
 		String pathwayOrganism ="";
 		if (pathwayNode.getAttributes().getNamedItem("Organism") !=null) {
 			pathwayOrganism = pathwayNode.getAttributes().getNamedItem("Organism").getTextContent().trim();
 		} 
 		Resource voidPwResource = voidModel.createResource("http://rdf.wikipathways.org/"+wpIdentifier);
 		Resource pwResource = voidModel.createResource("http://www.wikipathways.org/index.php/Pathway:"+wpIdentifier);
 		voidPwResource.addProperty(FOAF.page, pwResource);
 		voidPwResource.addProperty(DCTerms.title, voidModel.createLiteral(pathwayName, "en"));
 		voidPwResource.addProperty(RDF.type, Void.Dataset);
 		voidBase.addProperty(Void.subset, voidPwResource);
 		voidPwResource.addLiteral(Pav.version, wpRevision);
 		voidPwResource.addLiteral(DCTerms.subject, pathwayOrganism);
 		Resource subDatadump = voidModel.createResource("http://rdf.wikipathways.org/"+wpIdentifier);
 		voidBase.addProperty(Void.dataDump, subDatadump);
 
 		//obtain history
 		/* DateFormat formatter;
 		Date date;
 		formatter = new SimpleDateFormat("yyyymmdd");
 		date = (Date) formatter.parse("20000101");
 		WSPathwayHistory pathwayHistory = client.getPathwayHistory(wpIdentifier, date);
 		for (int i = 0; i < pathwayHistory.getHistory().length; i++) {
 
 			String user = pathwayHistory.getHistory(i).getUser();
 			String version = pathwayHistory.getHistory(i).getRevision();
 			int year = Integer.parseInt(pathwayHistory.getHistory(i).getTimestamp().substring(0, 4));
 			int month = Integer.parseInt(pathwayHistory.getHistory(i).getTimestamp().substring(5, 6));
 			int day = Integer.parseInt(pathwayHistory.getHistory(i).getTimestamp().substring(7, 8));
 			//System.out.println("void year:"+year);
 			//System.out.println("void month:"+month);
 			//System.out.println("void day:"+day);
 
 			Calendar cal1 = new GregorianCalendar(year, month, day);	
 
 			voidPwResource.addProperty(Pav.authoredOn, voidModel.createTypedLiteral(cal1));
 			Resource wpUser = voidModel
 			.createResource("http://www.wikipathways.org/index.php/User:"
 					+ user.replace(" ", "_")); //TODO Needs to change to VIVO instance
 			voidPwResource.addProperty(Pav.contributedBy, wpUser);
 			if (version != wpRevision) {
 				voidPwResource.addProperty(Pav.previousVersion, version);
 			}
 		}
 		 */
 		return voidPwResource;
 	}
 
 	public static Resource addPathwayLevelTriple(Model model, Node pathwayNode, HashMap<String, String> organismMap) throws IOException{
 		String wpIdentifier = pathwayNode.getAttributes().getNamedItem("identifier").getTextContent().trim();
 		String wpRevision = pathwayNode.getAttributes().getNamedItem("revision").getTextContent().trim();
 		String pathwayName = pathwayNode.getAttributes().getNamedItem("Name").getTextContent().trim();
 		String pathwayOrganism = "";
 		if (pathwayNode.getAttributes().getNamedItem("Organism") != null)
 			pathwayOrganism = pathwayNode.getAttributes().getNamedItem("Organism").getTextContent().trim();
 
 		System.out.println(wpIdentifier);
 		Resource pathwayResource = model.createResource("http://rdf.wikipathways.org/Pathway/"+wpIdentifier+"_r"+wpRevision);
 		Resource pathwayIdentifier = model.createResource("http://identifiers.org/wikipathways/"+wpIdentifier);
 		pathwayResource.addProperty(FOAF.page, model.createResource(constants.getWikiPathwaysURL()+wpIdentifier+"_r"+wpRevision));
 		pathwayResource.addProperty(RDF.type, Wp.Pathway);
 		pathwayResource.addProperty(RDF.type, Skos.Collection);
 		pathwayResource.addProperty(DC.identifier, pathwayIdentifier);
 		pathwayResource.addLiteral(DCTerms.identifier, wpIdentifier);
 		pathwayResource.addLiteral(Pav.version, wpRevision);
 		pathwayResource.addLiteral(DC.title, model.createLiteral(pathwayName, "en"));
 		Resource organismResource = model.createResource("http://purl.obolibrary.org/obo/NCBITaxon_"+ organismMap.get(pathwayOrganism));
 		organismResource.addLiteral(RDFS.label, pathwayOrganism);
 		pathwayResource.addProperty(Wp.organism, organismResource);	
 		if (((Element) pathwayNode).getElementsByTagName("BiopaxRef").getLength()>0){
 			String biopaxRef = ((Element) pathwayNode).getElementsByTagName("BiopaxRef").item(0).getTextContent().trim();
 			pathwayResource.addProperty(Gpml.biopaxref, biopaxRef);	
 		}	
 		if (((Element) pathwayNode).getElementsByTagName("BiopaxRef").getLength()==0){
 			basicCalls.appendToFile("/tmp/noBiopaxRef.log", wpIdentifier+"\n");
 		}
 		if (((Element) pathwayNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("BoardHeight") != null)
 		{
 			Float height = Float.valueOf(((Element) pathwayNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("BoardHeight").getTextContent().trim().trim()).floatValue();
 			Float width = Float.valueOf(((Element) pathwayNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("BoardWidth").getTextContent().trim().trim()).floatValue();
 			pathwayResource.addLiteral(Gpml.width, width);
 			pathwayResource.addLiteral(Gpml.height, height);
 		}
 		if (((Element) pathwayNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("BoardHeight") == null)
 		{
 			basicCalls.appendToFile("/tmp/noBoardDimensions.log", wpIdentifier+"\n");
 		}
 
 		return pathwayResource;
 	}
 
 	public static void addDataNodeTriples(Model model, Resource pwResource, Node dataNode, String wpId, String revId, Model bridgeDbModel, IDMapper mapper, Model openPhactsLinkSets) throws IOException, IDMapperException{
 		String dataNodeLabel = dataNode.getAttributes().getNamedItem("TextLabel").getTextContent().trim();
 		String dataNodeType="";
 		if (dataNode.getAttributes().getNamedItem("Type") != null){ 
 			dataNodeType = dataNode.getAttributes().getNamedItem("Type").getTextContent().trim();
 		}
 		String dataNodeGroupRef = "";
 		if (dataNode.getAttributes().getNamedItem("GroupRef") != null) {
 			dataNodeGroupRef = dataNode.getAttributes().getNamedItem("GroupRef").getTextContent().trim();
 		}
 		String dataNodeDataSource = ((Element) dataNode).getElementsByTagName("Xref").item(0).getAttributes().getNamedItem("Database").getTextContent().trim();
 		String dataNodeIdentifier = ((Element) dataNode).getElementsByTagName("Xref").item(0).getAttributes().getNamedItem("ID").getTextContent().trim().replace(" ", "_");
 		Float dataNodeGraphicsCenterX = Float.valueOf(((Element) dataNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("CenterX").getTextContent().trim());
 		Float dataNodeGraphicsCenterY = Float.valueOf(((Element) dataNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("CenterY").getTextContent().trim());
 		Float dataNodeGraphicsHeight = Float.valueOf(((Element) dataNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("Height").getTextContent().trim());
 		Float dataNodeGraphicsWidth = Float.valueOf(((Element) dataNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("Width").getTextContent().trim());
 		String dataNodeDataColor = null;
 		if (((Element) dataNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("Color")!=null)
 			dataNodeDataColor = ((Element) dataNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("Color").getTextContent().trim();
 
 		String dataNodeZorder=null;
 		if (((Element) dataNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("ZOrder")!=null)
 			dataNodeZorder = ((Element) dataNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("ZOrder").getTextContent().trim();
 
 
 		String sparqlQueryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
 		"			PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
 		"			PREFIX dc: <http://purl.org/dc/terms/>\n" + 
 		"			PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
 		"			PREFIX schema: <http://schema.org/>\n" + 
 		"			PREFIX bridgeDb: <http://openphacts.cs.man.ac.uk:9090//ontology/DataSource.owl#>\n" + 
 		"			SELECT DISTINCT  ?identifiers_org_base ?urlPattern ?bio2rdf ?origrdf\n" + 
 		"			WHERE {\n" + 
 		"			?datasource bridgeDb:fullName \""+dataNodeDataSource+"\" .\n" + 
 		"			OPTIONAL {?datasource bridgeDb:urnBase ?urnBase .}\n" + 
 		"			OPTIONAL {?datasource bridgeDb:code ?code .}\n" + 
 		"			OPTIONAL {?datasource bridgeDb:mainUrl ?mainUrl .}\n" + 
 		"			OPTIONAL {?datasource bridgeDb:type ?bdtype .}\n" + 
 		"			?datasource bridgeDb:identifiers_org_base ?identifiers_org_base .\n" + 
 		"			OPTIONAL {?datasource bridgeDb:urlPattern ?urlPattern .}\n" + 
 		"			OPTIONAL {?datasource bridgeDb:shortName ?shortName .}\n" + 
 		"			OPTIONAL {?datasource bridgeDb:bio2RDF ?bio2rdf.}\n" + 
 		"			OPTIONAL {?datasource bridgeDb:sourceRDFURI ?origrdf.}\n" + 
 		"			}";
 		Query query = QueryFactory.create(sparqlQueryString);
 		QueryExecution queryExecution = QueryExecutionFactory.create(query, bridgeDbModel);
 
 		ResultSet resultSet = queryExecution.execSelect();
 		String sourceRDFURI=null;
 		String bio2RdfURI=null;
 		String nonRDFURIURI=null;
 		String identifiersorgURI=null;
 		while (resultSet.hasNext()) {
 			QuerySolution solution = resultSet.next();
 			if (solution.get("origrdf") != null) {
 				sourceRDFURI = solution.get("origrdf").toString();
 			}
 			if (solution.get("bio2rdf") != null){
 				bio2RdfURI = solution.get("bio2rdf").toString();
 			}
 			if (solution.get("urlPattern") != null){
 				nonRDFURIURI = solution.get("urlPattern").toString();
 			}
 			if (solution.get("identifiers_org_base") != null){
 				identifiersorgURI = solution.get("identifiers_org_base").toString();
 			}
 		}
 		String conceptUrl = "http://rdf.wikipathways.org/error/$id"; // The ConceptUrl is the main URI for a given skos:concept in WikiPathways
 		if (sourceRDFURI!= null) {
 			conceptUrl = sourceRDFURI;
 		} else if (bio2RdfURI != null){
 			conceptUrl = bio2RdfURI;
 		} else if (nonRDFURIURI != null){
 			conceptUrl = nonRDFURIURI;
 		}
 
 		String dataNodeGraphId = "";
 		if (dataNode.getAttributes().getNamedItem("GraphId")!=null){
 			dataNodeGraphId = dataNode.getAttributes().getNamedItem("GraphId").getTextContent().trim();
 		}
 
 		Resource internalWPDataNodeResource = model.createResource("http://rdf.wikipathways.org/Pathway/"+wpId+"_r"+revId+"/DataNode/"+dataNodeGraphId);
 		Resource dataNodeResource = model.createResource(conceptUrl.replace("$id", dataNodeIdentifier));
 		Resource identifiersOrgResource= model.createResource();
 		if (dataNodeDataSource == ""){
 			internalWPDataNodeResource = model.createResource("http://rdf.wikipathways.org/Pathway/"+wpId+"_r"+revId+"/noDatasource/"+ UUID.randomUUID());
 			internalWPDataNodeResource.addProperty(RDF.type, Gpml.requiresCurationAttention);
 			internalWPDataNodeResource.addLiteral(RDFS.comment, "This URI represents a DataNode in GPML where there is no DataSource set. ");
 			if (dataNodeIdentifier==""){
 				identifiersOrgResource = model.createResource("http://rdf.wikipathways.org/Pathway/"+wpId+"_r"+revId+"/DataNode/noIdentifier");
 				identifiersOrgResource.addProperty(RDF.type, Gpml.requiresCurationAttention);
 				identifiersOrgResource.addLiteral(RDFS.comment, "This URI represents a DataNode in GPML where there is no Identifier given set. ");
 			} else {
 			}
 		}
 		else {
 			//System.out.println(dataNodeIdentifier);
 			if ((dataNodeIdentifier=="") || (dataNodeIdentifier==null)){
 				internalWPDataNodeResource= model.createResource(conceptUrl.replace("$id", "noIdentifier"));
 				identifiersOrgResource = model.createResource("http://rdf.wikipathways.org/Pathway/"+wpId+"_r"+revId+"/DataNode/noIdentifier");
 				identifiersOrgResource.addLiteral(RDFS.comment, "This URI represents a DataNode in GPML where there is no Identifier given set. ");
 				internalWPDataNodeResource.addProperty(RDF.type, Gpml.requiresCurationAttention);
 			} else {
 				identifiersOrgResource = model.createResource(identifiersorgURI + dataNodeIdentifier);
 			}
 
 		}
 		Xref idXref = new Xref(dataNodeIdentifier, DataSource.getByFullName(dataNodeDataSource));
 		if (dataNodeType != ""){
 			if (dataNodeType.equals("GeneProduct")){
 				internalWPDataNodeResource.addProperty(RDF.type, Wp.GeneProduct);
 				getUnifiedIdentifiers(model, mapper, idXref, internalWPDataNodeResource);
 				Resource sourceURI = openPhactsLinkSets.createResource(identifiersOrgResource.getURI());
 				addBdbLinkSets(openPhactsLinkSets, mapper, idXref, sourceURI);
 
 
 			}
 			if (dataNodeType.equals("Metabolite")){
 				internalWPDataNodeResource.addProperty(RDF.type, Wp.Metabolite);
 				getUnifiedIdentifiers(model, mapper, idXref, internalWPDataNodeResource);
 				Resource sourceURI = openPhactsLinkSets.createResource(identifiersOrgResource.getURI());
 				addBdbLinkSets(openPhactsLinkSets, mapper, idXref, sourceURI);
 			}
 			if (dataNodeType.equals("Pathway")){
 				internalWPDataNodeResource.addProperty(RDF.type, Wp.Pathway);
 				getUnifiedIdentifiers(model, mapper, idXref, internalWPDataNodeResource);
 			}
 			if (dataNodeType.equals("Protein")){
 				internalWPDataNodeResource.addProperty(RDF.type, Wp.Protein);
 				getUnifiedIdentifiers(model, mapper, idXref, internalWPDataNodeResource);
 				Resource sourceURI = openPhactsLinkSets.createResource(identifiersOrgResource.getURI());
 				addBdbLinkSets(openPhactsLinkSets, mapper, idXref, sourceURI);
 			}
 			if (dataNodeType.equals("Complex")){
 				internalWPDataNodeResource.addProperty(RDF.type, Wp.Complex);
 				openPhactsLinkSets.createResource(identifiersOrgResource);
 				getUnifiedIdentifiers(model, mapper, idXref, internalWPDataNodeResource);
 			}
 		}
 
 
 		if ((dataNodeResource != null) && (dataNodeDataSource != "")){ 
 			internalWPDataNodeResource.addProperty(RDFS.subClassOf, dataNodeResource);
 		}
 		if (identifiersorgURI != null) internalWPDataNodeResource.addProperty(DC.identifier, identifiersOrgResource);
 		internalWPDataNodeResource.addLiteral(DCTerms.identifier, dataNodeIdentifier);
 		if (dataNodeGroupRef != ""){
 
 			Resource groupRefResource = model.createResource("http://rdf.wikipathways.org/Pathway/"+wpId+"_r"+revId+"/group/"+dataNodeGroupRef); 
 			groupRefResource.addProperty(DCTerms.isPartOf, pwResource);
 			internalWPDataNodeResource.addProperty(DCTerms.isPartOf, groupRefResource);
 		}
 		//Mapping to GPML
 		if ((dataNodeDataSource != "") && (dataNodeDataSource != null)){
 			internalWPDataNodeResource.addLiteral(DC.source, dataNodeDataSource);
 		}
 		internalWPDataNodeResource.addProperty(RDF.type, Gpml.DataNode);
 		internalWPDataNodeResource.addProperty(RDF.type, Skos.Concept);
 		internalWPDataNodeResource.addProperty(DCTerms.isPartOf, pwResource);
 
 		internalWPDataNodeResource.addLiteral(RDFS.label, model.createLiteral(dataNodeLabel, "en"));
 		internalWPDataNodeResource.addLiteral(Gpml.centerx, dataNodeGraphicsCenterX);
 		internalWPDataNodeResource.addLiteral(Gpml.centery, dataNodeGraphicsCenterY);
 		internalWPDataNodeResource.addLiteral(Gpml.height, dataNodeGraphicsHeight);
 		internalWPDataNodeResource.addLiteral(Gpml.width, dataNodeGraphicsWidth);
 		internalWPDataNodeResource.addLiteral(Gpml.graphid, dataNodeGraphId);
 		if (dataNodeDataColor != null)
 			internalWPDataNodeResource.addLiteral(Gpml.color, dataNodeDataColor);
 		if (dataNodeZorder!= null)
 			internalWPDataNodeResource.addLiteral(Gpml.zorder, dataNodeZorder);
 
 		internalWPDataNodeResource.addProperty(RDFS.isDefinedBy, Gpml.DataNode);
 
 	}
 
 	private static void createCHEMINFBits(Model model, Resource internalWPDataNodeResource,
 			Resource identifierResource, String unifiedHmdbDataNodeIdentifier) {
 		Resource cheminfEncodedIDResource = model.createResource(
 				internalWPDataNodeResource.getURI() + "/" + identifierResource.getLocalName()
 		);
 		cheminfEncodedIDResource.addProperty(RDF.type, identifierResource);
 		cheminfEncodedIDResource.addLiteral(SIO.SIO_000300, unifiedHmdbDataNodeIdentifier);
 		internalWPDataNodeResource.addProperty(CHEMINF.CHEMINF_000200, cheminfEncodedIDResource);
 	}
 
 	public static void addLineTriples(Model model, Resource pwResource, Node lineNode, String wpId, String revId){
 		// Make Line Resource
 		String graphId = String.valueOf(UUID.randomUUID()); //No graphRef set.
 		if (lineNode.getAttributes().getNamedItem("GraphId")!=null){
 			graphId = lineNode.getAttributes().getNamedItem("GraphId").getTextContent().trim();
 		}
 		Resource lineResource = model.createResource("http://rdf.wikipathways.org/Pathway/"+wpId+"_r"+revId+"/Line/"+graphId);
 		lineResource.addLiteral(Gpml.graphid, graphId);
 		if (lineNode.getAttributes().getNamedItem("GraphId")==null){
 			lineResource.addProperty(RDF.type, Gpml.requiresCurationAttention);
 		}
 		NodeList lineGraphics = ((Element) lineNode).getElementsByTagName("Graphics");
 		Float lineGraphicsLineThickness=null;
 		if (((Element) lineNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("LineThickness") != null) {
 			lineGraphicsLineThickness = Float.valueOf(((Element) lineNode).getElementsByTagName("Graphics").item(0).getAttributes().getNamedItem("LineThickness").getTextContent().trim());
 		}
 		int zOrder = 0;
 		if (lineGraphics.item(0).getAttributes().getNamedItem("ZOrder") != null){
 			zOrder = Integer.valueOf(lineGraphics.item(0).getAttributes().getNamedItem("ZOrder").getTextContent().trim());
 			lineResource.addLiteral(Gpml.zorder, zOrder);
 		}
 		lineResource.addProperty(DCTerms.isPartOf, pwResource);
 		lineResource.addProperty(RDF.type, Gpml.Interaction);
 		if (lineGraphicsLineThickness!=null){
 			lineResource.addLiteral(Gpml.linethickness, lineGraphicsLineThickness);
 		}
 		//Point Nodes and its attributes
 		NodeList points = ((Element) lineGraphics.item(0)).getElementsByTagName("Point");
 		List<String> graphRefs = new ArrayList<String>();
 		List<String> arrowHeads = new ArrayList<String>();
 		List<String> arrowTowards = new ArrayList<String>(); 	
 		for (int i=0; i<points.getLength(); i++){
 			String arrowHead = "";
 			if (points.item(i).getAttributes().getNamedItem("ArrowHead")!= null){ 
 				arrowHead = points.item(i).getAttributes().getNamedItem("ArrowHead").getTextContent().trim();
 
 			}
 			String graphRef = "";
 			if (points.item(i).getAttributes().getNamedItem("GraphRef") != null){
 				graphRef = points.item(i).getAttributes().getNamedItem("GraphRef").getTextContent().trim();
 				//TODO add graphref to model
 			}
 			Float relX =null;
 			Float relY = null;
 			if ((points.item(i).getAttributes().getNamedItem("RelX") != null) &&  (points.item(i).getAttributes().getNamedItem("RelY") != null)) {
 				relX =Float.valueOf(points.item(i).getAttributes().getNamedItem("RelX").getTextContent().trim());
 				relY =Float.valueOf(points.item(i).getAttributes().getNamedItem("RelY").getTextContent().trim());
 				lineResource.addLiteral(Gpml.relX, relX);
 				lineResource.addLiteral(Gpml.relY, relY);
 			}
 			Float x =null;
 			Float y = null;
 			if ((points.item(i).getAttributes().getNamedItem("RelX") != null) &&  (points.item(i).getAttributes().getNamedItem("RelY") != null)) {
 				x =Float.valueOf(points.item(i).getAttributes().getNamedItem("RelX").getTextContent().trim());
 				y =Float.valueOf(points.item(i).getAttributes().getNamedItem("RelY").getTextContent().trim());
 			}
 
 			if (arrowHead !=""){
 				lineResource.addProperty(Gpml.arrowTowards, graphRef);
 				lineResource.addLiteral(Gpml.arrowHead, arrowHead);
 				arrowHeads.add(arrowHead);
 				arrowTowards.add(graphRef);
 			}
 			if ((graphRef != null) && (graphRef != "" )){
 				lineResource.addLiteral(Gpml.graphref, graphRef);
 				graphRefs.add(graphRef);
 			}
 		}
 		/*if (graphRefs.size() == 2) {
 			Resource interactionResource = model.createResource("http://rdf.wikipathways.org/Pathway/\"+wpId+\"_r\"+revId+\"/Interaction/"+UUID.randomUUID());
 			interactionResource.addProperty(DCTerms.isPartOf, pwResource);
 			interactionResource.addProperty(RDF.type, Wp.Interaction);
 			interactionResource.addProperty(DCTerms.format, lineResource);
 			if (arrowHeads.size() == 1){
 				if (arrowHeads.get(0).equals("Arrow"))
 					interactionResource.addProperty(RDF.type, Wp.DirectedInteraction);
 				if (arrowHeads.get(0).equals("mim-catalysis"))
 					interactionResource.addProperty(RDF.type, Wp.Catalysis);
 				if (arrowHeads.get(0).equals("mim-stimulation"))
 					interactionResource.addProperty(RDF.type, Wp.Stimulation);
 				if (arrowHeads.get(0).equals("mim-conversion"))
 					interactionResource.addProperty(RDF.type, Wp.Conversion);
 				if (arrowHeads.get(0).equals("mim-transcription-translation"))
 					interactionResource.addProperty(RDF.type, Wp.TranscriptionTranslation);
 				if (arrowHeads.get(0).equals("mim-inhibition"))
 					interactionResource.addProperty(RDF.type, Wp.Inhibition);
 				if (arrowHeads.get(0).equals("mim-catalysis"))
 					interactionResource.addProperty(RDF.type, Wp.Catalysis);
 				if (arrowHeads.get(0).equals("mim-necesssary-stimulation"))
 					interactionResource.addProperty(RDF.type, Wp.NecessaryStimulation);
 				if (arrowHeads.get(0).equals("mim-binding"))
 					interactionResource.addProperty(RDF.type, Wp.Binding);
 				if (arrowHeads.get(0).equals("mim-cleavage"))
 					interactionResource.addProperty(RDF.type, Wp.Cleavage);
 				if (arrowHeads.get(0).equals("mim-modification"))
 					interactionResource.addProperty(RDF.type, Wp.Modification);
 				if (arrowHeads.get(0).equals("mim-gap"))
 					interactionResource.addProperty(RDF.type, Wp.Gap);
 
 			}
 
 			String queryString = 
 				"PREFIX gpml: <http://vocabularies.wikipathways.org/gpml#> " +
 				"PREFIX dc:      <http://purl.org/dc/elements/1.1/> " +
 				"SELECT * " +
 				"WHERE {" +
 				"      ?node gpml:graphid \""+graphRefs.get(0)+"\" . " +
 				"      ?node dc:identifier ?nodeIdentifier . " +
 				"      }";
 
 			Query query = QueryFactory.create(queryString);
 			QueryExecution qe = QueryExecutionFactory.create(query, model);
 			ResultSet resultSet = qe.execSelect();
 			while (resultSet.hasNext()) {
 				QuerySolution solution = resultSet.next();
 				if (solution.get("nodeIdentifier") != null) {
 					Resource nodeIdentifierResource = model.createResource(solution.get("nodeIdentifier").toString());
 					interactionResource.addProperty(Wp.hasParticipant, nodeIdentifierResource);
 				}
 			}
 			queryString = 
 				"PREFIX gpml: <http://vocabularies.wikipathways.org/gpml#> " +
 				"PREFIX dc:      <http://purl.org/dc/elements/1.1/> " +
 				"SELECT * " +
 				"WHERE {" +
 				"      ?node gpml:graphid \""+graphRefs.get(1)+"\" . " +
 				"      ?node dc:identifier ?nodeIdentifier . " +
 				"      }";
 			query = QueryFactory.create(queryString);
 			qe = QueryExecutionFactory.create(query, model);
 			resultSet = qe.execSelect();
 			while (resultSet.hasNext()) {
 				QuerySolution solution = resultSet.next();
 				if (solution.get("nodeIdentifier") != null) {
 					Resource nodeIdentifierResource = model.createResource(solution.get("nodeIdentifier").toString());
 					interactionResource.addProperty(Wp.hasParticipant, nodeIdentifierResource);
 				}
 			}
 			for (int j=0; j<arrowTowards.size(); j++){
 				queryString = 
 					"PREFIX gpml: <http://vocabularies.wikipathways.org/gpml#> " +
 					"PREFIX dc:      <http://purl.org/dc/elements/1.1/> " +
 					"SELECT * " +
 					"WHERE {" +
 					"      ?node gpml:graphid \""+arrowTowards.get(j)+"\" . " +
 					"      ?node dc:identifier ?nodeIdentifier . " +
 					"      }";
 
 				query = QueryFactory.create(queryString);
 				qe = QueryExecutionFactory.create(query, model);
 				resultSet = qe.execSelect();
 				while (resultSet.hasNext()) {
 					QuerySolution solution = resultSet.next();
 					if (solution.get("nodeIdentifier") != null) {
 						Resource nodeIdentifierResource = model.createResource(solution.get("nodeIdentifier").toString());
 						interactionResource.addProperty(Wp.interactionTarget, nodeIdentifierResource);						
 					}
 				}
 			}
 		} */
 
 		NodeList anchors = ((Element) lineGraphics.item(0)).getElementsByTagName("Anchor");
 		for (int i=0; i<anchors.getLength(); i++){
 			String anchorGraphId = "";
 			if ( anchors.item(i).getAttributes().getNamedItem("GraphId") != null){
 				anchorGraphId = anchors.item(i).getAttributes().getNamedItem("GraphId").getTextContent().trim();
 			}
 			String anchorPosition = "";
 			if (anchors.item(i).getAttributes().getNamedItem("Position")!=null){
 				anchorPosition = anchors.item(i).getAttributes().getNamedItem("Position").getTextContent().trim();
 			}
 
 			Boolean uuidset = false;
 			if (anchorGraphId == ""){
 				anchorGraphId = String.valueOf(UUID.randomUUID());
 				uuidset = true;
 			}
 			Resource anchorResource = model.createResource("http://rdf.wikipathways.org/Pathway/"+wpId+"_r"+revId+"/Line/"+graphId+"/anchor/"+anchorGraphId);
 			if (uuidset){
 				anchorResource.addProperty(RDF.type, Gpml.requiresCurationAttention);
 				anchorResource.addLiteral(RDFS.comment, "This anchor does not have a graphId set");
 			}
 			anchorResource.addProperty(RDF.type, Gpml.Anchor);
 			anchorResource.addLiteral(Gpml.graphid, anchorGraphId);
 			if (anchorPosition != ""){
 				anchorResource.addLiteral(Gpml.anchorPosition, anchorPosition);
 			}
 			String anchorShape;
 			if (anchors.item(i).getAttributes().getNamedItem("Shape")!=null)
 				anchorResource.addLiteral(Gpml.anchorShape,anchors.item(i).getAttributes().getNamedItem("Shape").getTextContent().trim());
 			lineResource.addLiteral(Gpml.hasAnchor, anchorResource);
 		}
 	}
 
 	public static void addLabelTriples(Model model, Resource pwResource, Node labelNode, String wpId, String revId){
 		String graphId = "";
 		if (labelNode.getAttributes().getNamedItem("GraphId")!=null){
 			graphId = labelNode.getAttributes().getNamedItem("GraphId").getTextContent().trim();
 		}
 		String textLabel = labelNode.getAttributes().getNamedItem("TextLabel").getTextContent().trim();
 		Resource labelResource = model.createResource("http://rdf.wikipathways.org/Pathway/"+wpId+"_r"+revId+"/GpmlLabel/"+UUID.randomUUID());
 		labelResource.addProperty(DCTerms.isPartOf, pwResource);
 		labelResource.addLiteral(Gpml.graphid, graphId);
 		labelResource.addLiteral(RDFS.label, textLabel);
 		labelResource.addProperty(RDF.type, Gpml.Label);
 
 		NodeList lineGraphics = ((Element) labelNode).getElementsByTagName("Graphics");
 		int zOrder = 0;
 		if (lineGraphics.item(0).getAttributes().getNamedItem("ZOrder") != null){
 			zOrder = Integer.valueOf(lineGraphics.item(0).getAttributes().getNamedItem("ZOrder").getTextContent().trim());
 			labelResource.addLiteral(Gpml.zorder, zOrder);
 		}
 
 		Float centerX = null;
 		Float centerY = null;
 		if (lineGraphics.item(0).getAttributes().getNamedItem("CenterX") != null){
 			centerX = Float.valueOf(lineGraphics.item(0).getAttributes().getNamedItem("CenterX").getTextContent().trim());
 		}
 		if (lineGraphics.item(0).getAttributes().getNamedItem("CenterY")!= null){
 			centerY = Float.valueOf(lineGraphics.item(0).getAttributes().getNamedItem("CenterY").getTextContent().trim());
 		}
 		String fillColor = "";
 		if (lineGraphics.item(0).getAttributes().getNamedItem("FillColor")!= null){
 			fillColor = lineGraphics.item(0).getAttributes().getNamedItem("FillColor").getTextContent().trim();
 		}
 		int fontSize =0;
 		if (lineGraphics.item(0).getAttributes().getNamedItem("FontSize")!=null){
 			fontSize = Integer.valueOf(lineGraphics.item(0).getAttributes().getNamedItem("FontSize").getTextContent().trim());
 		}
 		String fontWeight = "";
 		if (lineGraphics.item(0).getAttributes().getNamedItem("FontWeight")!=null){
 			fontWeight = lineGraphics.item(0).getAttributes().getNamedItem("FontWeight").getTextContent().trim();
 		}
 		Float height = Float.valueOf(lineGraphics.item(0).getAttributes().getNamedItem("Height").getTextContent().trim());
 		Float width = Float.valueOf(lineGraphics.item(0).getAttributes().getNamedItem("Width").getTextContent().trim());
 		String valign ="";
 		if (lineGraphics.item(0).getAttributes().getNamedItem("Valign")!=null){
 			valign = lineGraphics.item(0).getAttributes().getNamedItem("Valign").getTextContent().trim();
 		}
 
 		labelResource.addLiteral(Gpml.centerx, centerX);
 		labelResource.addLiteral(Gpml.centery, centerY);
 		labelResource.addLiteral(Gpml.fillcolor, fillColor);
 		labelResource.addLiteral(Gpml.fontsize, fontSize);
 		labelResource.addLiteral(Gpml.fontweight, fontWeight);
 		labelResource.addLiteral(Gpml.height, height);
 		labelResource.addLiteral(Gpml.width, width);
 		labelResource.addLiteral(Gpml.valign, valign);
 
 	}
 	public static void addReferenceTriples(Model model, Resource pwResource, Node referenceNode, String wpId, String revision){
 		String id = basicCalls.getStringNodeContent(referenceNode, "bp:ID").trim();
 		String db = basicCalls.getStringNodeContent(referenceNode, "bp:DB").trim();
 		String title = basicCalls.getStringNodeContent(referenceNode, "bp:TITLE");
 		String source = basicCalls.getStringNodeContent(referenceNode, "bp:SOURCE");
 		String year = basicCalls.getStringNodeContent(referenceNode, "bp:YEAR");
 		ArrayList<String> authors = new ArrayList<String>();
 		NodeList authorsNL = ((Element) referenceNode).getElementsByTagName("bp:AUTHORS");
 		for (int i=0; i<authorsNL.getLength(); i++){
 			authors.add(authorsNL.item(i).getTextContent().trim());
 		}
 		Resource referenceResource;
 		if (db.equals("PubMed") && (id !="") && id.trim().matches("^[0-9]+$")){
 			referenceResource = model.createResource("http://identifiers.org/pubmed/"+id);
 			referenceResource.addProperty(FOAF.page, model.createResource("http://www.ncbi.nlm.nih.gov/pubmed/"+id)); 
 
 		} else {
 			referenceResource = model.createResource();
 			referenceResource.addLiteral(DCTerms.identifier, id);
 			referenceResource.addLiteral(DC.source, db);
 			referenceResource.addLiteral(DCTerms.title, title);
 			referenceResource.addLiteral(DCTerms.source, source);
 			referenceResource.addLiteral(DCTerms.date, year);
 
 		}
 		referenceResource.addProperty(RDF.type, Wp.PublicationReference);
 		referenceResource.addProperty(DCTerms.isPartOf, pwResource);
 
 	}
 
 	public static void addGroupTriples(Model model, Resource pwResource, Node groupNode, String wpId, String revId){
 		String graphId = "";
 		if (((Element) groupNode).getAttributes().getNamedItem("GraphId") != null){
 			graphId = ((Element) groupNode).getAttributes().getNamedItem("GraphId").getTextContent().trim();
 		}
 		String groupId = ((Element) groupNode).getAttributes().getNamedItem("GroupId").getTextContent().trim();
 
 		Resource groupResource = model.createResource("http://rdf.wikipathways.org/Pathway/"+wpId+"_r"+revId+"/group/"+graphId);
 		groupResource.addProperty(RDF.type, Gpml.Group);
 		groupResource.addLiteral(Gpml.graphid, graphId);
 		groupResource.addProperty(DCTerms.isPartOf, pwResource);
 		//TODO understand what groupRef means
 	}
 
 	public static void addCommentTriples(Model model, Resource pwResource, NodeList commentNodeList, String wpId, String revId){
 		String wpCategory = "";
 		String wpDescription = "";
 		for ( int n=0; n<commentNodeList.getLength(); n++) {
 			String commentType = basicCalls.getStringAttributeContent(commentNodeList.item(n), "Source");
 			String commentContents = commentNodeList.item(n).getTextContent().trim(); 
 			if (commentType.equals("WikiPathways-category")){
 				if (commentContents.equals("Physiological Process")){
 					pwResource.addProperty(Wp.category, Wp.PhysiologicalProcess);
 					//System.out.println(commentType);
 				}
 				if (commentContents.equals("Metabolic Process")){
 					pwResource.addProperty(Wp.category, Wp.MetabolicProcess);
 					//System.out.println(commentType);
 				}
 				if (commentContents.equals("Cellular Process")){
 					pwResource.addProperty(Wp.category, Wp.CellularProcess);
 				}
 				if (commentContents.equals("Molecular Function")){
 					pwResource.addProperty(Wp.category, Wp.MolecularFunction);
 				}
 			}
 			if (commentType.equals("WikiPathways-description")){
 				pwResource.addLiteral(DCTerms.description, commentContents);		
 			}
 			if (commentType.equals("GenMAPP notes")){
 				pwResource.addLiteral(DCTerms.description, commentContents);
 				pwResource.addLiteral(Gpml.genmappNotes, commentContents);		
 			}
 
 		}
 	}
 
 	public static void mergeGpmltoSingleFile (String gpmlLocation) throws IOException, XMLStreamException, ParserConfigurationException, SAXException, TransformerException, ConverterException{
 		// Based on: http://stackoverflow.com/questions/10759775/how-to-merge-1000-xml-files-into-one-in-java
 		//for (int i = 1; i < 8 ; i++) {		
 		Writer outputWriter = new FileWriter("/tmp/WpGPML.xml");
         XMLOutputFactory xmlOutFactory = XMLOutputFactory.newFactory();
         XMLEventWriter xmlEventWriter = xmlOutFactory.createXMLEventWriter(outputWriter);
         XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();
 
         xmlEventWriter.add(xmlEventFactory.createStartDocument("ISO-8859-1", "1.0"));
         xmlEventWriter.add(xmlEventFactory.createStartElement("", null, "PathwaySet"));
         xmlEventWriter.add(xmlEventFactory.createAttribute("creationData", basicCalls.now()));
         XMLInputFactory xmlInFactory = XMLInputFactory.newFactory();
         
         	
         File dir = new File(gpmlLocation);
         
         File[] rootFiles = dir.listFiles();
         //the section below is only in case of analysis sets
         for (File rootFile : rootFiles) {
         	String fileName = FilenameUtils.removeExtension(rootFile.getName());
         	System.out.println(fileName);
         	String[] identifiers = fileName.split("_");
         	System.out.println(fileName);
         	String wpIdentifier = identifiers[identifiers.length-2];
         	String wpRevision = identifiers[identifiers.length-1];
         	//Pattern pattern = Pattern.compile("_(WP[0-9]+)_([0-9]+).gpml");
         	//Matcher matcher = pattern.matcher(fileName);
         	//System.out.println(matcher.find());
         	//String wpIdentifier = matcher.group(1);
         	File tempFile = new File(constants.localAllGPMLCacheDir()+wpIdentifier+"_"+wpRevision+".gpml");
         	//System.out.println(matcher.group(1));
         	//String wpRevision = matcher.group(2);
         	//System.out.println(matcher.group(2));
         	if (!(tempFile.exists())){					
 				System.out.println(tempFile.getName());
 				Document currentGPML = basicCalls.openXmlFile(rootFile.getPath());
 				basicCalls.saveDOMasXML(WpRDFFunctionLibrary.addWpProvenance(currentGPML, wpIdentifier, wpRevision), constants.localCurrentGPMLCache() + tempFile.getName());
 			}
         }
         
         
         dir = new File("/tmp/GPML");
         rootFiles = dir.listFiles();
         for (File rootFile : rootFiles) {
         	System.out.println(rootFile);
             XMLEventReader xmlEventReader = xmlInFactory.createXMLEventReader(new StreamSource(rootFile));
             XMLEvent event = xmlEventReader.nextEvent();
             // Skip ahead in the input to the opening document element
             try {
             while (event.getEventType() != XMLEvent.START_ELEMENT) {
                 event = xmlEventReader.nextEvent();
             } 
 
 
             do {
                 xmlEventWriter.add(event);
                 event = xmlEventReader.nextEvent();
             } while (event.getEventType() != XMLEvent.END_DOCUMENT);
             xmlEventReader.close();
             } catch (Exception e) {
             	System.out.println("Malformed gpml file");
             }
         }
 
         xmlEventWriter.add(xmlEventFactory.createEndElement("", null, "PathwaySet"));
         xmlEventWriter.add(xmlEventFactory.createEndDocument());
 
         xmlEventWriter.close();
         outputWriter.close();
 	}
 	public static void addPathwayOntologyTriples(Model model, Resource pwResource, Node ontologyNode){
 		String identifier = basicCalls.getStringNodeContent(ontologyNode, "bp:ID");
		pwResource.addProperty(Wp.pathwayOntology, model.createResource(constants.getOntologyURI(identifier).replace(":", "_").replace("http_", "http:"))); //TDOD discuss with Tina and Alex about what to do with this
 
 	}
 
 }

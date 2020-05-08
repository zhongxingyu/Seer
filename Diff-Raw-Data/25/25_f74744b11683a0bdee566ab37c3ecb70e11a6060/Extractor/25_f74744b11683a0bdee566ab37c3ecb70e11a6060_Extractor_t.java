 package cz.opendata.linked.geocoder.krovak;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntityBuilder;
 import org.apache.http.impl.client.HttpClientBuilder;
 import org.openrdf.model.Graph;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.query.QueryEvaluationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
 import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
 import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
 import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
 import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
 import cz.cuni.mff.xrg.odcs.rdf.exceptions.InvalidQueryException;
 import cz.cuni.mff.xrg.odcs.rdf.impl.MyTupleQueryResult;
 import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
 
 @AsExtractor
 public class Extractor 
 extends ConfigurableBase<ExtractorConfig> 
 implements DPU, ConfigDialogProvider<ExtractorConfig> {
 
 	/**
 	 * DPU's configuration.
 	 */
 
 	private Logger logger = LoggerFactory.getLogger(DPU.class);
 	
 	@InputDataUnit(name = "gmlPoint points")
 	public RDFDataUnit gmlPoints;
 
 	@OutputDataUnit(name = "Geocoordinates")
 	public RDFDataUnit outGeo;	
 	
 	public Extractor() {
 		super(ExtractorConfig.class);
 	}
 
 	@Override
 	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
 		return new ExtractorDialog();
 	}
 
 	public void execute(DPUContext ctx) throws DPUException
 	{
 		java.util.Date date = new java.util.Date();
 		long start = date.getTime();
 
 		URI gmlPoint = outGeo.createURI("http://www.opengis.net/ont/gml#Point");
 		URI gmlId = outGeo.createURI("http://www.opengis.net/ont/gml#id");
 		URI gmlPos = outGeo.createURI("http://www.opengis.net/ont/gml#pos");
 		URI gmlSRS = outGeo.createURI("http://www.opengis.net/ont/gml#srsName");
 		URI krovak = outGeo.createURI("urn:ogc:def:crs:EPSG::5514");
 
 		URI geoURI = outGeo.createURI("http://schema.org/geo");
 		URI geocoordsURI = outGeo.createURI("http://schema.org/GeoCoordinates");
 		//URI xsdDouble = outGeo.createURI("http://www.w3.org/2001/XMLSchema#double");
 		//URI xsdDecimal = outGeo.createURI("http://www.w3.org/2001/XMLSchema#decimal");
 		URI longURI = outGeo.createURI("http://schema.org/longitude");
 		URI latURI = outGeo.createURI("http://schema.org/latitude");
 		
 		String countQuery = 
 				  "PREFIX s: <http://schema.org/> "
 				+ "PREFIX gml: <http://www.opengis.net/ont/gml#> "
 				+ "SELECT (COUNT (*) as ?count) "
 				+ "WHERE "
 				+ "{"
 					+ "?point a gml:Point . "
 				+  "}"; 
 		String notGCcountQuery = 
 				  "PREFIX s: <http://schema.org/> "
 				+ "PREFIX gml: <http://www.opengis.net/ont/gml#> "
 				+ "SELECT (COUNT (*) as ?count) "
 				+ "WHERE "
 				+ "{"
 				+ "?point a gml:Point . "
 					+ "FILTER NOT EXISTS {?point s:geo ?geo}"
 				+  "}"; 
 		String sOrgConstructQuery = "PREFIX s: <http://schema.org/> "
 				+ "PREFIX gml: <http://www.opengis.net/ont/gml#> "
 				+ "CONSTRUCT {?point ?p ?o}"
 				+ "WHERE "
 				+ "{"
 				+ "?point a gml:Point ; "
 					+ "			?p ?o . "
 					+ "FILTER NOT EXISTS {?point s:geo ?geo}"
 				+  "}"; 
 
 		logger.debug("Init");
 		
 		int total = 0;
 		int ngc = 0;
 		int count = 0;
 		int failed = 0;
 		try {
 			MyTupleQueryResult countres = gmlPoints.executeSelectQueryAsTuples(countQuery);
 			MyTupleQueryResult countnotGC = gmlPoints.executeSelectQueryAsTuples(notGCcountQuery);
 			total = Integer.parseInt(countres.next().getValue("count").stringValue());
 			ngc = Integer.parseInt(countnotGC.next().getValue("count").stringValue());
 			ctx.sendMessage(MessageType.INFO, "Found " + total + " points, " + ngc + " not transformed yet.");
 		} catch (InvalidQueryException e1) {
 			logger.error(e1.getLocalizedMessage());
 		} catch (NumberFormatException e) {
 			logger.error(e.getLocalizedMessage());
 		} catch (QueryEvaluationException e) {
 			logger.error(e.getLocalizedMessage());
 		}
 
 		try {
 			
 			//Schema.org addresses
 			logger.debug("Getting point data via query: " + sOrgConstructQuery);
 			//MyTupleQueryResult res = sAddresses.executeSelectQueryAsTuples(sOrgQuery);
 			Graph resGraph = gmlPoints.executeConstructQuery(sOrgConstructQuery);
 			
 			int expectedNumOfBlocks = ngc/config.numofrecords + 1;
 			
 			logger.debug("Starting transformation, estimating " + expectedNumOfBlocks + "blocks. ");
 			
 			Iterator<Statement> it = resGraph.match(null, RDF.TYPE, gmlPoint);
 			
 			int blocksDone = 0;
 			while (it.hasNext() && !ctx.canceled())
 			{
 				int currentBlock = 0;
 				StringBuilder lines = new StringBuilder();
 				HashMap<String, String> uriMap = new HashMap<String, String>();
 
 				while (currentBlock < config.numofrecords && it.hasNext() && !ctx.canceled())
 				{
 					count++;
 					Resource currentPointURI = it.next().getSubject();
 					//logger.trace("Point " + count + "/" + ngc + ": " + currentPointURI.toString());
 					if (resGraph.match(currentPointURI, gmlSRS, krovak).hasNext())
 					{
 						currentBlock++;
 											
 						Iterator<Statement> it1 = resGraph.match(currentPointURI, gmlId, null); 
 						
 						Value id = it1.next().getObject();
 						
 						it1 = resGraph.match(currentPointURI, gmlPos, null); 
 		
 						Value pos = it1.next().getObject();
 						
 						String posString = pos.stringValue();
 						String Y = posString.substring(0, posString.indexOf(" "));
 						String X = posString.substring(posString.indexOf(" ") + 1);
 						String name = id.stringValue().replace(".", "_");
 						
 						uriMap.put(name, currentPointURI.toString());
 						
 						lines.append(name + "\t" + Y + "\t" + X + "\t0\t\r\n");
 	
 					}
 					else
 					{
 						logger.info("Point " + currentPointURI.toString() + " not Krovak");
 					}
 				}
 				
 				if (ctx.canceled()) break;
 				
 				blocksDone++;
 				
 				logger.info("Block " + blocksDone + "/" + expectedNumOfBlocks + " of " + currentBlock + " records prepared to send");
 				
 				String file = lines.toString();
 				
				String url = "http://geoportal.cuzk.cz/(S(" + config.sessionId + "))/WCTSHandlerhld.ashx";
 				
 				HttpClient httpclient = HttpClientBuilder.create().build();
 				HttpPost httppost = new HttpPost(url);
 
 				MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();        
 				multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
 				multipartEntity.setBoundary("----WebKitFormBoundaryCYQR5wAfAoAP7BrE");
 				
 				multipartEntity.addTextBody("source", "File");
 				multipartEntity.addTextBody("sourceSRS", "urn:ogc:def:crs,crs:EPSG::5514,crs:EPSG::5705");
 				multipartEntity.addTextBody("targetSRS", "urn:ogc:def:crs:EPSG::4937");
 				multipartEntity.addTextBody("sourceXYorder", "xy");
 				multipartEntity.addTextBody("targetXYorder", "xy");
 				multipartEntity.addTextBody("sourceSixtiethView", "false");
 				multipartEntity.addTextBody("targetSixtiethView", "true");
 				multipartEntity.addTextBody("wcts_fileType", "text");
 				multipartEntity.addBinaryBody("wcts_file1", new ByteArrayInputStream(file.getBytes()), ContentType.TEXT_PLAIN, "geo.txt");
 				    
 				httppost.setEntity(multipartEntity.build());
 
 				HttpResponse response = null;
 				try {
 					response = httpclient.execute(httppost);
 				} catch (ClientProtocolException e) {
 					logger.error(e.getLocalizedMessage());
 				} catch (IOException e) {
 					logger.error(e.getLocalizedMessage());
 				}
 				HttpEntity resEntity = response.getEntity();				
 				logger.debug("Got response");
 
 				String result = null;
 
 				if (resEntity != null) {
 				    InputStream inputStream = null;
 				    try {
 						inputStream = resEntity.getContent();
 					} catch (IllegalStateException e) {
 						logger.error(e.getLocalizedMessage());
 					} catch (IOException e) {
 						logger.error(e.getLocalizedMessage());
 					}
 				    
 				    StringWriter writer = new StringWriter();
 				    try {
 						IOUtils.copy(inputStream, writer, "UTF-8");
 					} catch (IOException e) {
 						logger.error(e.getLocalizedMessage());
 					}
 				    result = writer.toString();
 				}
 				else break;
 				
 				String[] resultLines = result.split("\\r\\n");
 				
 				for (String currentLine : resultLines)
 				{
 					String[] columns = currentLine.split("\\s");
 					
 					String currentPointUri = uriMap.get(columns[0]); 
 					BigDecimal latitude = new BigDecimal(Double.parseDouble(columns[1]) + (Double.parseDouble(columns[2])/60) + (Double.parseDouble(columns[3]) / 3600));
 					BigDecimal longitude = new BigDecimal(Double.parseDouble(columns[4]) + (Double.parseDouble(columns[5])/60) + (Double.parseDouble(columns[6]) / 3600));
 					
 					URI origPoinURI = outGeo.createURI(currentPointUri);
 					URI coordURI = outGeo.createURI(currentPointUri+"/wgs84");
 					
 					outGeo.addTriple(origPoinURI, geoURI , coordURI);
 					outGeo.addTriple(coordURI, RDF.TYPE, geocoordsURI);
 					outGeo.addTriple(coordURI, longURI, outGeo.createLiteral(longitude.toString()/*, xsdDecimal*/));
 					outGeo.addTriple(coordURI, latURI, outGeo.createLiteral(latitude.toString()/*, xsdDecimal*/));
 					
 				}
 				
 			}
 			if (ctx.canceled()) logger.info("Cancelled");
 			
 		} catch (InvalidQueryException e) {
 			logger.error(e.getLocalizedMessage());
 		}
 
        	logger.info("Transformation done.");
 
 		java.util.Date date2 = new java.util.Date();
 		long end = date2.getTime();
 
 		ctx.sendMessage(MessageType.INFO, "Transformed: " + count + " in " + (end-start) + "ms, failed attempts: " + failed);
 
 	}
 
 	@Override
 	public void cleanUp() {	}
 
 }

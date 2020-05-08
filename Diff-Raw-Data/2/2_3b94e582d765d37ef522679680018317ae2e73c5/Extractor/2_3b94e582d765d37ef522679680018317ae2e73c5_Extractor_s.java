 package cz.opendata.linked.geocoder.nominatim;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.lang.reflect.Type;
 import java.math.BigDecimal;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 
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
 
 import org.apache.commons.io.*;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.InstanceCreator;
 import com.google.gson.JsonParser;
 
 @AsExtractor
 public class Extractor 
 extends ConfigurableBase<ExtractorConfig> 
 implements DPU, ConfigDialogProvider<ExtractorConfig> {
 
 	/**
 	 * DPU's configuration.
 	 */
 
 	private Logger logger = LoggerFactory.getLogger(DPU.class);
 	private int geocodes = 0;
 	private int cacheHits = 0;
 	
 	@InputDataUnit(name = "Schema.org addresses")
 	public RDFDataUnit sAddresses;
 
 	@OutputDataUnit(name = "Geocoordinates")
 	public RDFDataUnit outGeo;	
 	
 	public Extractor() {
 		super(ExtractorConfig.class);
 	}
 
 	@Override
 	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
 		return new ExtractorDialog();
 	}
 
 	public class Response {
 	  public Map<String, Geo> descriptor;
 	  //getters&setters
 	  public Response () {  }
 	}
 	
 	public class Geo {
 	  public String lat;
 	  public String lon;
 	  //getters&setters
 	  public Geo () {  }
 	}
 	
 	public class GeoInstanceCreator implements InstanceCreator<Geo> {
 	   public Geo createInstance(Type type) {
 	     return new Geo();
 	   }
 	 }	
 	
 	private int countTodaysCacheFiles(DPUContext ctx)
 	{
 		int count = 0;
 
 		// Directory path here
 		File currentFile;
 		File folder = ctx.getGlobalDirectory();
 		if (!folder.isDirectory()) return 0;
 
 		File[] listOfFiles = folder.listFiles(); 
 		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 
 		for (int i = 0; i < listOfFiles.length; i++) 
 		{
 			if (listOfFiles[i].isFile()) 
 			{
 				currentFile = listOfFiles[i];
 
 				Date now = new Date();
 				Date modified = null;
 				try {
 					modified = sdf.parse(sdf.format(currentFile.lastModified()));
 				} catch (ParseException e) {
 					logger.error(e.getLocalizedMessage());
 				}
 				long diff = (now.getTime() - modified.getTime()) / 1000;
 				//System.out.println("Date modified: " + sdf.format(currentFile.lastModified()) + " which is " + diff + " seconds ago.");
 
 				if (diff < (config.hoursToCheck * 60 * 60)) count++;
 			}
 		}
 		logger.info("Total of " + count + " positions cached in last " + config.hoursToCheck + " hours. " + (config.limit - count) + " remaining.");
 		return count;
 	}
 	
 	private static String getURLContent(String p_sURL) throws IOException
 	{
 	    URL oURL;
 	    String sResponse = null;
 
         oURL = new URL(p_sURL);
         oURL.openConnection();
         try	{
         	sResponse = IOUtils.toString(oURL, "UTF-8");
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         }
 
 	    return sResponse;
 	}
 
 	private String getAddressPart(Resource currentAddressURI, URI currentPropertyURI, Graph resGraph) {
 		Iterator<Statement> it1 = resGraph.match(currentAddressURI, currentPropertyURI, null); 
 		String currentValueString = null;
 		if (it1.hasNext())
 		{
 			Value currentValue = it1.next().getObject();
 			if (currentValue != null)
 			{
 				currentValueString = currentValue.stringValue();
 			}
 		}
 		return currentValueString;
 	}
 	
 	public void execute(DPUContext ctx) throws DPUException
 	{
 		java.util.Date date = new java.util.Date();
 		long start = date.getTime();
 	    Gson gson = new GsonBuilder().registerTypeAdapter(Geo.class, new GeoInstanceCreator()).create();
 
 		URI dcsource = outGeo.createURI("http://purl.org/dc/terms/source");
 		URI nominatimURI = outGeo.createURI("http://nominatim.openstreetmap.org");
 		URI geoURI = outGeo.createURI("http://schema.org/geo");
 		URI geocoordsURI = outGeo.createURI("http://schema.org/GeoCoordinates");
 		URI postalAddressURI = sAddresses.createURI("http://schema.org/PostalAddress");
 		URI streetAddressURI = sAddresses.createURI("http://schema.org/streetAddress");
 		URI addressRegionURI = sAddresses.createURI("http://schema.org/addressRegion");
 		URI addressLocalityURI = sAddresses.createURI("http://schema.org/addressLocality");
 		URI addressCountryURI = sAddresses.createURI("http://schema.org/addressCountry");
 		URI postalCodeURI = sAddresses.createURI("http://schema.org/postalCode");
 		//URI xsdDouble = outGeo.createURI("http://www.w3.org/2001/XMLSchema#double");
 		//URI xsdDecimal = outGeo.createURI("http://www.w3.org/2001/XMLSchema#decimal");
 		URI longURI = outGeo.createURI("http://schema.org/longitude");
 		URI latURI = outGeo.createURI("http://schema.org/latitude");
 		String countQuery = "PREFIX s: <http://schema.org/> "
 				+ "SELECT (COUNT (*) as ?count) "
 				+ "WHERE "
 				+ "{"
 					+ "?address a s:PostalAddress . "
 				+  "}"; 
 		/*String notGCcountQuery = "PREFIX s: <http://schema.org/> "
 				+ "SELECT (COUNT (*) as ?count) "
 				+ "WHERE "
 				+ "{"
 					+ "?address a s:PostalAddress . "
 					+ "FILTER NOT EXISTS {?address s:geo ?geo}"
 				+  "}";*/ 
 		String sOrgConstructQuery = "PREFIX s: <http://schema.org/> "
 				+ "CONSTRUCT {?address ?p ?o}"
 				+ "WHERE "
 				+ "{"
 					+ "?address a s:PostalAddress ;"
 					+ "			?p ?o . "
 //					+ "FILTER NOT EXISTS {?address s:geo ?geo}"
 				+  "}"; 
 
 		logger.debug("Geocoder init");
 		
 		int total = 0;
 		int ngc = 0;
 		int count = 0;
 		int failed = 0;
 		try {
 			MyTupleQueryResult countres = sAddresses.executeSelectQueryAsTuples(countQuery);
 			//MyTupleQueryResult countnotGC = sAddresses.executeSelectQueryAsTuples(notGCcountQuery);
 			total = Integer.parseInt(countres.next().getValue("count").stringValue());
 			//ngc = Integer.parseInt(countnotGC.next().getValue("count").stringValue());
 			ctx.sendMessage(MessageType.INFO, "Found " + total + " addresses");
 		} catch (InvalidQueryException e1) {
 			logger.error(e1.getLocalizedMessage());
 		} catch (NumberFormatException e) {
 			logger.error(e.getLocalizedMessage());
 		} catch (QueryEvaluationException e) {
 			logger.error(e.getLocalizedMessage());
 		}
 
 		try {
 			
 			//Schema.org addresses
 			logger.debug("Executing Schema.org query: " + sOrgConstructQuery);
 			//MyTupleQueryResult res = sAddresses.executeSelectQueryAsTuples(sOrgQuery);
 			Graph resGraph = sAddresses.executeConstructQuery(sOrgConstructQuery);
 			
 			logger.debug("Starting geocoding.");
 			
 			Iterator<Statement> it = resGraph.match(null, RDF.TYPE, postalAddressURI);
 			
 			int cachedToday = countTodaysCacheFiles(ctx);
 			int toCache = (config.limit - cachedToday);
 
 			long lastDownload = 0;
 			while (it.hasNext() && !ctx.canceled() && geocodes <= toCache)
 			{
 				count++;
 				Resource currentAddressURI = it.next().getSubject();
 				
 				String streetAddress, addressLocality, addressRegion, postalCode, addressCountry;
 				
 				streetAddress = getAddressPart(currentAddressURI, streetAddressURI, resGraph);
 				addressLocality = getAddressPart(currentAddressURI, addressLocalityURI, resGraph);
 				addressRegion = getAddressPart(currentAddressURI, addressRegionURI, resGraph);
 				postalCode = getAddressPart(currentAddressURI, postalCodeURI, resGraph);
 				//TODO: address can be either 2letter country code or link to s:Country. so far it is manual.
 				//addressCountry = getAddressPart(currentAddressURI, addressCountryURI, resGraph);
 				
 				String address = (config.country.isEmpty() ? "" : config.country) + " " 
 				+ ((config.usePostalCode && postalCode != null) ? postalCode : "")  + " " 
 				+ ((config.useRegion && addressRegion != null) ? addressRegion : "") + " " 
 				+ ((config.useStreet && streetAddress != null) ? streetAddress : "") + " " 
				+ ((config.useLocality && addressLocality != null) ? addressLocality : "");
 				logger.debug("Address to geocode (" + count + "/" + total + "): " + address);
 								
 				String file;
 				if (config.structured)
 				{
 					file = "structured-" + address.replace(" ", "-").replace("?", "-").replace("/", "-").replace("\\", "-");	
 				}
 				else
 				{
 					file = address.replace(" ", "-").replace("?", "-").replace("/", "-").replace("\\", "-");	
 				}
 				//CACHE
 				
 				File hPath = ctx.getGlobalDirectory();
 				File hFile = new File(hPath, file);
 				
 				if (!hFile.exists())
 				{
 					long curTS = date.getTime();
 					if (lastDownload + config.interval > curTS)
 					{
 						logger.debug("Sleeping: " + (lastDownload + config.interval - curTS));
 						try {
 							Thread.sleep(lastDownload + config.interval - curTS);
 						} catch (InterruptedException e) {
 							logger.info("Interrupted while sleeping");
 						}
 					}
 					
 					String url;
 					if (config.structured) {
 						url = "http://nominatim.openstreetmap.org/search?format=json" 
 								+ (config.useStreet ? "&street=" + streetAddress : "") 
 								+ (config.useLocality ? "&city=" + (config.stripNumFromLocality ? addressLocality.replaceAll("[0-9]",  "").replace(" (I)+", "") : addressLocality) : "" ) 
 								+ (config.country.isEmpty() ? "" : "&state=" + config.country) 
 								+ (config.usePostalCode ? "&postalcode=" + postalCode : "")
 								+ (config.useRegion ? "&county=" + addressRegion : "");
 						//url = url.replace(" ", "+");
 					}
 					else {
 						url = "http://nominatim.openstreetmap.org/search?format=json&q=" + address.replace(" ", "+");
 					}
 					
 					String out = null;
 					try {
 						out = getURLContent(url.toString());
 					} catch (IOException e1) {
 						logger.error(e1.getLocalizedMessage() +" while geocoding " +  address);
 					}
 					lastDownload = date.getTime();
 					
 					geocodes++;
 					logger.debug("Queried Nominatim (" + geocodes + "): " + address);
 					
 					try {
 						BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
 						fw.append(out);
 						fw.close();
 					} catch (Exception e) {
 						logger.error(e.getLocalizedMessage());
 						e.printStackTrace();
 					}
 					//CACHED
 				}
 				else {
 					cacheHits++;
 					logger.debug("From cache (" + cacheHits + "): " + address);
 				}
 				
 				//READ FROM FILE - NOW IT EXISTS
 				String cachedFile = null;
 				try {
 					cachedFile = FileUtils.readFileToString(hFile);
 				} catch (IOException e) {
 					logger.error(e.getLocalizedMessage());
 				}
 				
 				try {
 					Geo[] gs = gson.fromJson(cachedFile, Geo[].class);
 					if (gs == null || gs.length == 0) {
 						failed++;
 						logger.debug("Failed to geolocate (" + failed + "): " + address);
 						continue;
 					}
 					
 					BigDecimal latitude = new BigDecimal(gs[0].lat);
 					BigDecimal longitude = new BigDecimal(gs[0].lon);
 					
 					logger.debug("Located: " + address + " Possibilities: " + gs.length + " Latitude: " + latitude + " Longitude: " + longitude);
 					
 					String uri = currentAddressURI.stringValue();
 					URI addressURI = outGeo.createURI(uri);
 					URI coordURI = outGeo.createURI(uri+"/geocoordinates/nominatim");
 					
 					outGeo.addTriple(addressURI, geoURI , coordURI);
 					outGeo.addTriple(coordURI, RDF.TYPE, geocoordsURI);
 					outGeo.addTriple(coordURI, longURI, outGeo.createLiteral(longitude.toString()/*, xsdDecimal*/));
 					outGeo.addTriple(coordURI, latURI, outGeo.createLiteral(latitude.toString()/*, xsdDecimal*/));
 					outGeo.addTriple(coordURI, dcsource, nominatimURI);
 				} catch (Exception e) {
 					logger.warn(e.getLocalizedMessage());
 					e.printStackTrace();
 				}
 				
 			}
 			if (ctx.canceled()) logger.info("Cancelled");
 			
 		} catch (InvalidQueryException e) {
 			logger.error(e.getLocalizedMessage());
 		}
 
        	logger.info("Geocoding done.");
 
 		java.util.Date date2 = new java.util.Date();
 		long end = date2.getTime();
 
 		ctx.sendMessage(MessageType.INFO, "Geocoded " + count + ": Nominatim: "+ geocodes +" From cache: " + cacheHits + " in " + (end-start) + "ms, failed attempts: " + failed);
 
 	}
 
 	@Override
 	public void cleanUp() {	}
 
 }

 package cz.opendata.linked.geocoder;
 
 import java.io.File;
 import java.util.Iterator;
 
 import org.openrdf.model.Graph;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.query.BindingSet;
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
 import cz.opendata.linked.geocoder.lib.Geocoder;
 import cz.opendata.linked.geocoder.lib.Geocoder.GeoProvider;
 import cz.opendata.linked.geocoder.lib.Geocoder.GeoProviderFactory;
 import cz.opendata.linked.geocoder.lib.Position;
 
 @AsExtractor
 public class Extractor 
 extends ConfigurableBase<ExtractorConfig> 
 implements DPU, ConfigDialogProvider<ExtractorConfig> {
 
 	/**
 	 * DPU's configuration.
 	 */
 
 	private Logger logger = LoggerFactory.getLogger(DPU.class);
 	
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
 
 	public void execute(DPUContext ctx) throws DPUException
 	{
 		java.util.Date date = new java.util.Date();
 		long start = date.getTime();
 
 		URI geoURI = outGeo.createURI("http://schema.org/geo");
 		URI geocoordsURI = outGeo.createURI("http://schema.org/GeoCoordinates");
 		URI postalAddressURI = sAddresses.createURI("http://schema.org/PostalAddress");
 		URI streetAddressURI = sAddresses.createURI("http://schema.org/streetAddress");
 		URI addressRegionURI = sAddresses.createURI("http://schema.org/addressRegion");
 		URI addressLocalityURI = sAddresses.createURI("http://schema.org/addressLocality");
 		URI addressCountryURI = sAddresses.createURI("http://schema.org/addressCountry");
 		URI postalCodeURI = sAddresses.createURI("http://schema.org/postalCode");
 		URI xsdDouble = outGeo.createURI("http://www.w3.org/2001/XMLSchema#double");
		URI xsdDecimal = outGeo.createURI("http://www.w3.org/2001/XMLSchema#decimal");
 		URI longURI = outGeo.createURI("http://schema.org/longitude");
 		URI latURI = outGeo.createURI("http://schema.org/latitude");
 		String geoCache = new File(ctx.getGlobalDirectory(), "cache/geocoder.cache").getAbsolutePath();
 		String countQuery = "PREFIX s: <http://schema.org/> "
 				+ "SELECT (COUNT (*) as ?count) "
 				+ "WHERE "
 				+ "{"
 					+ "?address a s:PostalAddress . "
 				+  "}"; 
 		String sOrgConstructQuery = "PREFIX s: <http://schema.org/> "
 				+ "CONSTRUCT {?address ?p ?o}"
 				+ "WHERE "
 				+ "{"
 					+ "?address a s:PostalAddress ;"
 					+ "			?p ?o . "
 				+  "}"; 
 		String sOrgQuery = "PREFIX s: <http://schema.org/> "
 				+ "SELECT DISTINCT * "
 				+ "WHERE "
 				+ "{"
 					+ "{?address a s:PostalAddress . } "
 					+ "UNION { ?address s:streetAddress ?street . } "
 					+ "UNION { ?address s:addressRegion ?region . } "
 					+ "UNION { ?address s:addressLocality ?locality . } "
 					+ "UNION { ?address s:postalCode ?postal . } "
 					+ "UNION { ?address s:addressCountry ?country . } "
 				+ " }";
 
 		logger.debug("Geocoder init");
 		Geocoder.loadCacheIfEmpty(geoCache);
 		GeoProvider gisgraphy = GeoProviderFactory.createXMLGeoProvider(
                 config.geocoderURI + "/fulltext/fulltextsearch?allwordsrequired=false&from=1&to=1&q=",
                 "/response/result/doc[1]/double[@name=\"lat\"]",
                 "/response/result/doc[1]/double[@name=\"lng\"]",
                 0);
 		logger.debug("Geocoder initialized");
 
 		int total = 0;
 		int count = 0;
 		int failed = 0;
 		try {
 			MyTupleQueryResult countres = sAddresses.executeSelectQueryAsTuples(countQuery);
 			total = Integer.parseInt(countres.next().getValue("count").stringValue());
 			logger.info("Found " + total + " addresses.");
 		} catch (InvalidQueryException e1) {
 			logger.error(e1.getLocalizedMessage());
 		} catch (NumberFormatException e) {
 			logger.error(e.getLocalizedMessage());
 		} catch (QueryEvaluationException e) {
 			logger.error(e.getLocalizedMessage());
 		}
 
 		try {
 			
 			//Schema.org addresses
 			logger.debug("Executing Schema.org query: " + sOrgQuery);
 			//MyTupleQueryResult res = sAddresses.executeSelectQueryAsTuples(sOrgQuery);
 			Graph resGraph = sAddresses.executeConstructQuery(sOrgConstructQuery);
 			
 			logger.debug("Starting geocoding.");
 			
 			URI[] propURIs = new URI [] {streetAddressURI, addressLocalityURI, addressRegionURI, postalCodeURI, addressCountryURI};
 			Iterator<Statement> it = resGraph.match(null, RDF.TYPE, postalAddressURI);
 			while (it.hasNext() && !ctx.canceled())
 			{
 				count++;
 				Resource currentAddressURI = it.next().getSubject();
 				StringBuilder addressToGeoCode = new StringBuilder();
 				
 				for (URI currentPropURI : propURIs)
 				{
 					Iterator<Statement> it1 = resGraph.match(currentAddressURI, currentPropURI, null); 
 					
 					if (it1.hasNext())
 					{
 						Value currentValue = it1.next().getObject();
 						if (currentValue != null)
 						{
 							//logger.trace("Currently " + currentBinding);
 							String currentValueString = currentValue.stringValue();
 							//logger.trace("Value " + currentValueString);
 							addressToGeoCode.append(currentValueString);
 							addressToGeoCode.append(" ");
 						}
 					}
 				}				
 /*			}
 			
 			String[] props = new String [] {"street", "locality", "region", "postal", "country"};
 			while (res.hasNext() && !ctx.canceled())
 			{
 				count++;
 				BindingSet s = res.next();
 				StringBuilder addressToGeoCode = new StringBuilder();
 				
 				for (String currentBinding : props)
 				{
 					Value currentValue = s.getValue(currentBinding);
 					if (s.hasBinding(currentBinding) && currentValue != null)
 					{
 						//logger.trace("Currently " + currentBinding);
 						String currentValueString = currentValue.stringValue();
 						//logger.trace("Value " + currentValueString);
 						addressToGeoCode.append(currentValueString);
 						addressToGeoCode.append(" ");
 					}
 				}*/
 				
 				String address = addressToGeoCode.toString();
 				logger.debug("Address to geocode (" + count + "/" + total + "): " + address);
 				
 				Position pos = Geocoder.locate(address, gisgraphy);
 				
 				Double latitude = null;
 				Double longitude = null;
 				
 				if (pos != null)
 				{
 					latitude = pos.getLatitude();
 					longitude = pos.getLongitude();
 					logger.debug("Located " + address + " Latitude: " + latitude + " Longitude: " + longitude);
 					
 					String uri = currentAddressURI.stringValue();
 //					String uri = s.getValue("address").stringValue();
 					URI addressURI = outGeo.createURI(uri);
 					URI coordURI = outGeo.createURI(uri+"/geocoordinates");
 					
 					outGeo.addTriple(addressURI, geoURI , coordURI);
 					outGeo.addTriple(coordURI, RDF.TYPE, geocoordsURI);
					outGeo.addTriple(coordURI, longURI, outGeo.createLiteral(longitude.toString()/*, xsdDecimal*/));
					outGeo.addTriple(coordURI, latURI, outGeo.createLiteral(latitude.toString()/*, xsdDecimal*/));
 				}
 				else {
 					failed++;
 					logger.warn("Failed to locate: " + address);
 				}
 			}
 			if (ctx.canceled()) logger.info("Cancelled");
 			
 		} catch (InvalidQueryException e) {
 			logger.error(e.getLocalizedMessage());
 		}/* catch (QueryEvaluationException e) {
 			logger.error(e.getLocalizedMessage());
 		}*/
 
        	logger.info("Geocoding done.");
 
        	if (config.rewriteCache) {
 		   	logger.debug("Saving geo cache");
 			Geocoder.saveCache(geoCache);
 		   	logger.debug("Geo cache saved.");
        	}
 			
 		java.util.Date date2 = new java.util.Date();
 		long end = date2.getTime();
 
 		ctx.sendMessage(MessageType.INFO, "Processed " + count + " in " + (end-start) + "ms, failed attempts: " + failed);
 
 	}
 
 	@Override
 	public void cleanUp() {	}
 
 }

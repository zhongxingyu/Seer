 package cz.opendata.linked.cz.gov.agendy;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
 import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
 import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
 import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
 import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException;
 import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
 import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;
 
 @AsExtractor
 public class Extractor 
 extends ConfigurableBase<ExtractorConfig> 
 implements DPU, ConfigDialogProvider<ExtractorConfig> {
 
 	/**
 	 * DPU's configuration.
 	 */
 
 	@OutputDataUnit
 	public RDFDataUnit outputDataUnit;
 
 	private Logger logger = LoggerFactory.getLogger(DPU.class);
 
 	public Extractor(){
 		super(ExtractorConfig.class);
 	}
 
 	@Override
 	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
 		return new ExtractorDialog();
 	}
 
 	public void execute(DPUContext ctx) throws DPUException
 	{
 		// vytvorime si parser
 		Cache.setInterval(config.interval);
 		Cache.setTimeout(config.timeout);
 		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
 		Cache.rewriteCache = config.rewriteCache;
 		Cache.logger = logger;
 
 		try {
 			Cache.setTrustAllCerts();
 		} catch (Exception e1) {
 			logger.error("Unexpected error when setting trust to all certificates: " + e1.getLocalizedMessage());
 		}
 		
 		String tempfilename = ctx.getWorkingDir() + "/" + config.outputFileName;
 		Parser s = new Parser();
 		s.logger = logger;
 		s.ctx = ctx;
 		try {
 			s.ps = new PrintStream(tempfilename, "UTF-8");
 		} catch (FileNotFoundException e) {
 			logger.error(e.getLocalizedMessage());
 		} catch (UnsupportedEncodingException e) {
 			logger.error(e.getLocalizedMessage());
 		}
 
 		s.ps.println(
 				"@prefix dcterms:  <http://purl.org/dc/terms/> .\n" +
 						//"@prefix rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
 						//"@prefix rdfs:     <http://www.w3.org/2000/01/rdf-schema#> .\n" +
 						"@prefix xsd:      <http://www.w3.org/2001/XMLSchema#> .\n" +
 						"@prefix skos:       <http://www.w3.org/2004/02/skos/core#> .\n" +
 						"@prefix gr:       <http://purl.org/goodrelations/v1#> .\n" +
 						"@prefix adms:       <http://www.w3.org/ns/adms#> .\n" +
 						"@prefix dcterms:       <http://purl.org/dc/terms/> .\n" +
 						
 						"\n" +
 						"@prefix ovm-a:      <http://linked.opendata.cz/ontology/domain/seznam.gov.cz/agendy/> .\n" +
 						"@prefix ovm-r:      <http://linked.opendata.cz/resource/domain/seznam.gov.cz/agendy/> .\n" +
 						"@prefix ovm-t:      <http://linked.opendata.cz/resource/domain/seznam.gov.cz/typyOVM/> .\n" +
 						"@prefix ovm-c:      <http://linked.opendata.cz/resource/domain/seznam.gov.cz/cinnosti/> .\n" +
 						"@prefix ovm-co:      <http://linked.opendata.cz/ontology/domain/seznam.gov.cz/cinnosti/> .\n" +
 
 						""
 				);
 		
 
 		// a spustim na vychozi stranku
 
 		logger.info("Starting extraction. Output: " + tempfilename);
 		
 		try {
 			try {
 				java.util.Date date = new java.util.Date();
 				long start = date.getTime();
 				
 				s.parse(new URL("https://rpp-ais.egon.gov.cz/gen/agendy-detail/"), "list");
 				
 				java.util.Date date2 = new java.util.Date();
 				long end = date2.getTime();
 				
 				logger.info("Processed in " + (end-start) + "ms");
 			}
 			catch (IOException e) {
				logger.error(e.getLocalizedMessage());
 			}
         	
 			logger.info("Parsing done. Passing RDF to ODCS");
 			//give ttl to odcs
 			try {
 				outputDataUnit.addFromTurtleFile(new File(tempfilename));
 			}
 			catch (RDFException e)
 			{
				logger.error("Cannot put TTL to repository.");
 				throw new DPUException("Cannot put TTL to repository.");
 			}
 		} catch (InterruptedException intex) {
 			logger.error("Interrupted");
 		}
 
 		s.ps.close();
 
 
 	}
 
 	@Override
 	public void cleanUp() {	}
 
 }

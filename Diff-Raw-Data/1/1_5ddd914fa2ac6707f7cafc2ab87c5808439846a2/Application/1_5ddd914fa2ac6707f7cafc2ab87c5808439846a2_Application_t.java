 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 import ch.epfl.codimsd.config.AppConfig;
 import ch.epfl.codimsd.exceptions.CodimsException;
 import ch.epfl.codimsd.helper.FileUtils;
 import ch.epfl.codimsd.qeef.Metadata;
 import ch.epfl.codimsd.qeef.QueryManager;
 import ch.epfl.codimsd.qeef.QueryManagerImpl;
 import ch.epfl.codimsd.qeef.QueryManagerRepository;
 import ch.epfl.codimsd.qep.QEPInfo;
 import ch.epfl.codimsd.query.RequestResult;
 import ch.epfl.codimsd.query.ResultSet;
 import ch.epfl.codimsd.query.sparql.JSONOutput;
 import ch.epfl.codimsd.query.sparql.ResultOutput;
 import ch.epfl.codimsd.query.sparql.XMLOutput;
 
 public class Application extends Controller {
 
 	static {
 		try {
 			String appPath = Play.applicationPath.getAbsolutePath() + "/";
 			Logger.info("App Path: %s", appPath);
 			AppConfig.setRootPath(appPath);
 			// Initializes the Service
 			QueryManagerImpl.getQueryManagerImpl();
 			Logger.info("QEF - Initialized successfully");
 		} catch (Exception e) {
 			Logger.error(e.getMessage(), e);
 		}
 	}
 	
     public static void index() {
     	try {
 			List<QEPInfo> qeps = QEPInfo.getQEPInfo();
             QueryManagerRepository qmr = QueryManagerRepository.getInstance();
 			render(qeps, qmr);
 		} catch (Exception e) {
 			flash.error(e.getMessage());
 			Logger.error(e.getMessage(), e);
 			index();
 		}
     }
 
     public static void executeQuery(Integer qep, String format) {
 		try {
 			long startTime = System.currentTimeMillis();
 			if (format == null)
 				format = "html";
 			
 			Logger.info("Execute QEP %s (%s)", qep, format);
 			if (qep == null)
 				throw new IllegalArgumentException("id parameter missing or not a number.");
 			
 			QueryManager queryManager = QueryManagerRepository.getInstance().get(qep);
 			long loadPlanTime = System.currentTimeMillis();
 
 			// If query has named parameters
 			String[] paramNames = null;
 			String[] paramValues = null;
 			if (queryManager.getNamedParams() != null && queryManager.getNamedParams().size() > 0) {
 				
 				Iterator<String> it = queryManager.getNamedParams().iterator();
 				paramNames = new String[queryManager.getNamedParams().size()];
 				paramValues = new String[queryManager.getNamedParams().size()];
 				
 				for (int i=0; it.hasNext(); i++) {
 					paramNames[i] = it.next();
 					paramValues[i] = params.get(paramNames[i].substring(2)); // Do not consider '?:'
 					if (paramValues[i] == null) {
 						formParams(qep, format, queryManager.getNamedParams());
 						// throw new IllegalArgumentException("Missing named parameter " + paramNames[i]);
 					}
 				}
 				Logger.info("paramNames: %s - paramValues: %s", 
 					Arrays.toString(paramNames), Arrays.toString(paramValues));
 			}
 			
 			queryManager.executeRequest(paramNames, paramValues);
 			RequestResult result = queryManager.getRequestResult();
 			
 			Metadata mt = result.getResultMetadata();
 			ResultSet rs = result.getResultSet();
 			long executionTime = System.currentTimeMillis();
 
 			response.setHeader("Cache-Control", "no-cache");
 			response.setHeader("Pragma", "no-cache");
 			
 			ResultOutput output = null;
 			if (format.equals("html")) {
 				response.setContentTypeIfNotSet("application/xml");
 				output = new XMLOutput("/public/xml-to-html.xsl");
 				
 			} else if (format.equals("xml")) {
 				response.setContentTypeIfNotSet("application/sparql-results+xml");
 				response.setHeader("Content-Disposition", "attachment;filename=sparql-results.xml");
 				output = new XMLOutput();
 				
 			} else if (format.equals("json")) {
 				response.setContentTypeIfNotSet("application/sparql-results+json; charset=utf-8");
 				response.setHeader("Content-Disposition", "attachment;filename=sparql-results.json");
 				output = new JSONOutput();
 				
 			} 
 			
 			if (output != null) {
 				output.format(response.out, rs, mt);
 				
 			} else {
 				throw new IllegalArgumentException("Invalid format: " + format);
 			}
 			long outputTime = System.currentTimeMillis();
 			
 			Logger.info("Plan(%s):%s Exec:%s Output(%s):%s Total:%s", 
 					qep, (loadPlanTime - startTime), (executionTime - loadPlanTime), 
 					format, (outputTime - executionTime), (outputTime - startTime));
 			
 		} catch (Exception e) {
			Logger.error(e.toString());
 			flash.error(e.getMessage());
 			Logger.error(e.getMessage(), e);
 			index();
 		}
     	
     }
 
     public static void formParams(Integer qep, String format, Set<String> namedParams) {
     	render(qep, format, namedParams);
     }
     
     public static void show(Integer id) {
     	try {
     		QEPInfo qepInfo = QEPInfo.getQEPInfo(id);
     		
     		if (qepInfo == null) {
     			throw new IllegalArgumentException("Could not find qep with id = " + id);
     		}
 
 			String fileName = qepInfo.getPath();
 			String description = qepInfo.getDescription();
     		String fileContents = FileUtils.readFile(AppConfig.CODIMS_HOME + fileName);
 			render(fileName, description, fileContents);
 			
 		} catch (Exception e) {
 			flash.error(e.getMessage());
 			Logger.error(e.getMessage(), e);
 			index();
 		}
     }
     
     // To test: http://localhost:9001/edit/11
     public static void edit(Integer id) {
     	try {
     		QEPInfo qepInfo = QEPInfo.getQEPInfo(id);
     		
     		if (qepInfo == null) {
     			throw new IllegalArgumentException("Could not find qep with id = " + id);
     		}
 
 			String fileContents = FileUtils.readFile(AppConfig.CODIMS_HOME + qepInfo.getPath());
 			render(fileContents);
 			
 		} catch (Exception e) {
 			flash.error(e.getMessage());
 			Logger.error(e.getMessage(), e);
 			index();
 		}
     }
 
     // To test: http://localhost:9001/save/teste.txt/oi 
     public static void save(String fileName, String fileContents) {
     	try {
 			FileUtils.writeFile(fileName, fileContents);
 			flash.success("File " + fileName + " saved successfully.");
 			index();
 		} catch (IOException e) {
 			flash.error(e.getMessage());
 			Logger.error(e.getMessage(), e);
 			index();
 		}
     }
 
     public static void reload(Integer id) {
     	try {
     		if (id == null) {
     			throw new IllegalArgumentException("Invalid plan id = " + id);
     		}
     		
 			QueryManager queryManager = QueryManagerRepository.getInstance().get(id, true);
 			flash.success("Plan successfully loaded in " + queryManager.getQepCreationTime() + "ms.");
 			index();
 			
 		} catch (Exception e) {
 			flash.error(e.getMessage());
 			Logger.error(e.getMessage(), e);
 			index();
 		}
     }
     
 }
 

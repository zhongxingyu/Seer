 package org.aksw.verilinks.server.tools;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.aksw.verilinks.server.msg.Task;
 import org.aksw.verilinks.server.msg.TemplateInstance;
 import org.aksw.verilinks.server.msg.TemplateLinkset;
 import org.aksw.verilinks.server.msg.TemplateProperty;
 import org.xml.sax.SAXException;
 
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.rdf.arp.JenaReader;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.util.FileManager;
 
 /**
  * Reads RDF resources Files and insert the resources into database. Create
  * database.
  */
 public class RDFHandler {
 
 	private String dbIniPath = "d://db_settings2.ini";
 
 	/** Location of the link file */
 	private String fileName;
 
 	private String templateFile;
 
 	public final static String formatNT = "N-TRIPLE";
 	public final static String formatXML = "XML";
 
 	/** Jena model **/
 	private Model model;
 	/** Jena linked data model **/
 	private Model modelLinkedData;
 
 	private Task task;
 
 	private String linkset;
 
 	// Get name of templates
 	private String ontologyNameSubject;
 	private String ontologyNameObject;
 
 	// Get endPoints
 	private String endPointSubject;
 	private String endPointObject;
 
 	// Subject, predicate and object taken from the RDF model, which the
 	// link-spec-file generated .
 	private String subject;
 	private String predicate;
 	private String object;
 	private double confidence;
 
 	private TemplateInstance subjectTemplate;
 	private TemplateInstance objectTemplate;
 
 	private List<String> subjectSparql = new ArrayList<String>();
 	private List<String> objectSparql = new ArrayList<String>();
 
 	private List<String> subjectText = new ArrayList<String>();
 	private List<String> objectText = new ArrayList<String>();
 
 	// SQL query to add results into database
 	private String sqlQuerySubject;
 	private String sqlQueryObject;
 	private String sqlQueryStatements;
 
 	// Status of instances: If SPARQLquery-execution returns an empty set
 	// (resource doesn't exist),
 	// then status = false. Else status = true.
 	private boolean statusSubject;
 	private boolean statusObject;
 
 	// Counter for queried Links
 	private int linkCounter = 0;
 	private int linkSuccess = 0;
 	private int linkFailure = 0;
 	private int linkAdded = 0;
 	private int linkConfidencePositive = 0;
 	private int linkConfidenceNegative = 0;
 
 	public RDFHandler(String fileName) {
 		this.fileName = fileName;
 	}
 
 	public RDFHandler(String filePath, String dbIniPath, String templateFile,
 			Task t) {
 		this.fileName = filePath;
 		this.dbIniPath = dbIniPath;
 		this.templateFile = templateFile;
 		this.task = t;
 	}
 
 	/**
 	 * Remove invalid characters and invalid/unnecessary sub-strings of String s
 	 * 
 	 * @param s
 	 * @return parsedEndString
 	 */
 	public String parseRDF(String s) {
 
 		String endString = "";
 		String dummy;
 		// Remove Quotations
 		int endIndex;
 		int beginIndex = s.indexOf('"');
 		if (beginIndex != -1) {
 			endIndex = s.lastIndexOf('"');
 			dummy = s.substring(beginIndex + 1, endIndex);
 		} else
 			dummy = s;
 		// Remove invalid characters e.g. '
 		beginIndex = dummy.indexOf("'");
 		if (beginIndex != -1) {
 			dummy = dummy.replace("'", "");
 		}
 		// Returns expression between "( ?x = " and " )"
 		beginIndex = dummy.indexOf("( ?x = ");
 		if (beginIndex != -1) {
 			beginIndex = s.indexOf("= ");
 			endIndex = s.lastIndexOf(')');
 			dummy = dummy.substring(beginIndex + 2, endIndex - 1);
 
 		}
 
 		// Reduce length
 		if (dummy.length() < 600)
 			endString = dummy;
 		else
 			endString = dummy.substring(0, 590).concat("(...)");
 
 		return endString;
 
 	}
 
 	public String parseURL(String s) {
 		// alles ausserhalb anfuehrungsstriche rauswerfen
 		echo("parseURL");
 		echo("raw: " + s);
 		int beginIndex = s.indexOf('<');
 		int endIndex = s.indexOf('>');
 		String dummy = s.substring(beginIndex + 1, endIndex);
 		echo("parsed: " + dummy);
 		return dummy;
 	}
 
 	public String parseEntity(String s) {
 
 		return parseURL(s);
 	}
 
 	// Read model from file
 	/**
 	 * Read N-Triple File into model
 	 */
 	public void readNT(Model model, String fileName) {
 		echo("Reading file..");
 		InputStream in = FileManager.get().open(fileName);
 		if (in == null) {
 			throw new IllegalArgumentException("File: " + fileName + " not found");
 		}
 		// read the RDF/XML file
 		model.read(in, null, formatNT);
 		model.write(System.out, "TURTLE");
 		echo("Done");
 	}
 
 	/**
 	 * Read XML File into model. Fix needed.
 	 */
 	public void readXML(Model model, String fileName) {
 		// TODO: Fix
 		echo("Reading xml file..");
 		InputStream in = FileManager.get().open(fileName);
 		if (in == null) {
 			throw new IllegalArgumentException("File: " + fileName + " not found");
 		}
 		// read the RDF/XML file
 		model.read(in, null);
 		// model.write(System.out);
 
 	}
 
 	/**
 	 * SPARQL Query Method with limit parameter, returns a Single Result as
 	 * String. If failure method returns ""
 	 * 
 	 * @param query
 	 *          String of the SPARQL query
 	 * @param endPoint
 	 *          String of the SPARQL endpoint
 	 * @param limit
 	 *          limit for amount of properties to be queried
 	 * @return endString
 	 */
 	public String sparqlQueryText(String uri, String query, String endPoint,
 			String queryLimit) {
 		if (query.isEmpty())
 			return "";
 		if (query.equals("verli:uri")) {
 			echo(uri);
 			return uri;
 		}
 		if (query.equals("verli:image"))
 			return query;
 
 		int limit = 3;
 		if (queryLimit != null)
 			limit = Integer.parseInt(queryLimit);
 		// System.out.println("BLABLA limit: "+limit);
 		String text = "";
 		String endText = "";
 		String parsedText = "";
 		QueryExecution qExec = QueryExecutionFactory.sparqlService(endPoint, query);
 
 		int limitCounter = 0;
 
 		QuerySolution binding = null;
 		// How many values of a property should be queried
 		try {
 			com.hp.hpl.jena.query.ResultSet rs = qExec.execSelect();
 
 			while (rs.hasNext()) { // if query is successful
 				if (limit <= limitCounter)
 					break;
 				binding = rs.nextSolution();
 				text = binding.toString();
 				// parse String
 				parsedText = parseRDF(text);
 				// parsedText = parseURL(parsedText);
 				// echo("parsedText: "+parsedText);
 				if (parsedText.contentEquals("owl:Thing")) {
 					// echo("@@@@@@@@@@@@@@@@@@@@@@@ owl:Thing!");
 					continue;
 				} else if (endText.isEmpty())
 					endText = parsedText;
 				else
 					endText = endText + PropertyConstants.SEPERATOR_PROPERTY_VALUE
 							+ parsedText;
 				// echo("SPARQLquery Ergebnis "+limitCounter +":  "
 				// +parsedText);
 				limitCounter++;
 			}
 			echo(endText);
 
 		} catch (Exception e) {
 			echo("Query Execution failure! " + e.getMessage());
 		} finally {
 			qExec.close();
 		}
 
 		return endText; // if not successful it returns ""
 
 	}
 
 	public String createSparqlQuery(String subject, TemplateProperty property) {
 		String query = "";
 		String filter = "";
 		if (property.getProperty().equals("verli:uri"))
 			return "verli:uri";
 		if (property.getProperty().equals("verli:image"))
 			return "verli:image";
 		if (property.getFilter() != null) {
 			filter = "FILTER ( lang(?x) = " + "'" + property.getFilter() + "'" + " )";
 		}
 		if (!property.getProperty().isEmpty()
 				&& !property.getProperty().equals(PropertyConstants.NO_DECLARATION))
 			query = "SELECT ?x " + "WHERE {<" + subject + "> "
 					+ property.getProperty() + " ?x . " + filter + "} limit 50";
 
 		return query;
 	}
 
 	/**
 	 * Start querying rdf data and insert into mysql database. Attention: 1.Set
 	 * needed templates manually. 2.Set database connection data manually. // Init
 	 * DBTool. Parameters: (server, database, user, pass) DBTool db = new
 	 * DBTool("localhost","veri_links", "root", "");
 	 */
 	public String start(TemplateLinkset template, String fileFormat) {
 		linkset = template.getSubject().getOntology() + "-"
 				+ template.getObject().getOntology();
 
 		echo("###RDFHandler: start for Template: " + linkset + ". Format: "
 				+ fileFormat);
 
 		String msg = null;
 
 		// Get name of templates
 		ontologyNameSubject = template.getSubject().getOntology();
 		ontologyNameObject = template.getObject().getOntology();
 
 		// Get endPoints
 		endPointSubject = template.getSubject().getEndpoint();
 		endPointObject = template.getObject().getEndpoint();
 
 		subjectText = new ArrayList<String>();
 		objectText = new ArrayList<String>();
 
 		// // SPARQL queries for every property
 		// querySubject = new String[numberOfProperties];
 		// queryObject = new String[numberOfProperties];
 		//
 		// // Results of SPARQL queries
 		// textSubject = new String[numberOfProperties];
 		// textObject = new String[numberOfProperties];
 
 		// Start
 		this.model = ModelFactory.createDefaultModel();
 
 		// Read model from XML or NT file
 		if (fileFormat == formatNT) {
 			this.readNT(model, this.fileName);
 			int conf = 0;
 			if (this.fileName.contains("positive"))
 				conf = 1;
 			else if (this.fileName.contains("negative"))
 				conf = -2;
 			// msg = this.dbInsertFromNT(ontoSubject, ontoObject, conf);
 		} else if (fileFormat == formatXML) {
 			echo("@@XML format");
 			this.readXML(model, this.fileName);
 			msg = this.dbInsertFromXML(template);
 		} else {
 			msg = "ERROR: Wrong parameter for file format!";
 			echo(msg);
 		}
 		return msg;
 	}
 
 	// private String dbInsertFromNT(Template ontoSubject, Template ontoObject,
 	// int conf) {
 	// echo("##Insert from nt-file");
 	// // Subject, predicate and object taken from the RDF model, which the
 	// // link-spec-file generated .
 	// String subject, predicate, object;
 	//
 	// // documentation of querying process
 	// linkSuccess = 0;
 	// linkFailure = 0;
 	// String msg = "";
 	// // Init DBTool. Parameters: (server, database, user, pass)
 	// // DBTool db = new DBTool("localhost","veri_links", "root", ""); // <---
 	// // Change this
 	// DBTool db = new DBTool(dbIniPath);
 	// Connection con = db.getConnection(); // Connect to db
 	// echo("Create XMLTool");
 	// XMLTool xml = new XMLTool(templateFile);
 	// try {
 	// xml.readTemplateFile();
 	// } catch (ParserConfigurationException e1) {
 	// // TODO Auto-generated catch block
 	// e1.printStackTrace();
 	// } catch (SAXException e1) {
 	// // TODO Auto-generated catch block
 	// e1.printStackTrace();
 	// } catch (IOException e1) {
 	// // TODO Auto-generated catch block
 	// e1.printStackTrace();
 	// }
 	// echo("Create XMLTool");
 	// // Info (property, important)
 	// subjectInfo
 	// importantSubject[0] = xml.getAttribute(ontoSubject.getName(),
 	// PropertyConstants.PROP0, "important");
 	// importantSubject[1] = xml.getAttribute(ontoSubject.getName(),
 	// PropertyConstants.PROP1, "important");
 	// importantSubject[2] = xml.getAttribute(ontoSubject.getName(),
 	// PropertyConstants.PROP2, "important");
 	// importantSubject[3] = xml.getAttribute(ontoSubject.getName(),
 	// PropertyConstants.PROP3, "important");
 	// importantSubject[4] = xml.getAttribute(ontoSubject.getName(),
 	// PropertyConstants.IMAGE, "important");
 	// if(ontoObject.getName().equals("bbcwildlife") ||
 	// ontoObject.getName().equals("linkedgeodata"))
 	// importantSubject[4] = "yes";
 	// String imp;
 	// if (ontoSubject.getType().equals(PropertyConstants.TEMPLATE_TYPE_MAP))
 	// imp = "yes";
 	// else
 	// imp = "no";
 	// importantSubject[5] = imp;
 	// importantSubject[6] = imp;
 	//
 	// echo("ImportantSubject:");
 	// for (int i = 0; i < importantSubject.length; i++)
 	// echo(importantSubject[i]);
 	//
 	// importantObject[0] = xml.getAttribute(ontoObject.getName(),
 	// PropertyConstants.PROP0, "important");
 	// importantObject[1] = xml.getAttribute(ontoObject.getName(),
 	// PropertyConstants.PROP1, "important");
 	// importantObject[2] = xml.getAttribute(ontoObject.getName(),
 	// PropertyConstants.PROP2, "important");
 	// importantObject[3] = xml.getAttribute(ontoObject.getName(),
 	// PropertyConstants.PROP3, "important");
 	// importantObject[4] = xml.getAttribute(ontoObject.getName(),
 	// PropertyConstants.IMAGE, "important");
 	// if (ontoObject.getType().equals(PropertyConstants.TEMPLATE_TYPE_MAP))
 	// imp = "yes";
 	// else
 	// imp = "no";
 	// importantObject[5] = imp;
 	// importantObject[6] = imp;
 	//
 	// echo("ImportantObject:");
 	// for (int i = 0; i < importantObject.length; i++)
 	// echo(importantObject[i]);
 	//
 	// // Iterate through all the statements of the model
 	// StmtIterator iter = model.listStatements();
 	// while (iter.hasNext()) {
 	//
 	// statusSubject = true;
 	// statusObject = true;
 	//
 	// Statement stmt = iter.nextStatement();
 	//
 	// subject = stmt.getSubject().toString();
 	// predicate = stmt.getPredicate().toString();
 	// object = stmt.getObject().toString();
 	//
 	// echo("\n<<<<<<<<<<<<<< Link # " + linkCounter
 	// + " >>>>>>>>>>>>>>");
 	// echo("s: " + subject);
 	// echo("p: " + predicate);
 	// echo("o: " + object);
 	// linkCounter++;
 	//
 	// // Standard SPARQL Query for a Property
 	// querySubject[0] = createSparqlQuery(subject, ontoSubject.getProp0(),"en");
 	// querySubject[1] = createSparqlQuery(subject, ontoSubject.getProp1());
 	// // querySubject[2]= createSparqlQuery(subject,
 	// // ontoSubject.getProp2(),"en");
 	// querySubject[2] = createSparqlQuery(subject, ontoSubject.getProp2(),
 	// xml.getAttribute(ontoSubject.getName(), PropertyConstants.PROP2,
 	// "filter"));
 	// querySubject[3] = createSparqlQuery(subject, ontoSubject.getProp3());
 	// querySubject[4] = createSparqlQuery(subject, ontoSubject.getImage());
 	// querySubject[5] = createSparqlQuery(subject, ontoSubject.getLat());
 	// querySubject[6] = createSparqlQuery(subject, ontoSubject.getLong());
 	//
 	// queryObject[0] = createSparqlQuery(object, ontoObject.getProp0());
 	// queryObject[1] = createSparqlQuery(object, ontoObject.getProp1());
 	// // queryObject[2]= createSparqlQuery(object, ontoObject.getProp2());
 	// queryObject[2] = createSparqlQuery(object, ontoObject.getProp2(),
 	// xml.getAttribute(ontoObject.getName(), PropertyConstants.PROP2,
 	// "filter"));
 	// queryObject[3] = createSparqlQuery(object, ontoObject.getProp3());
 	// queryObject[4] = createSparqlQuery(object, ontoObject.getImage());
 	// queryObject[5] = createSparqlQuery(object, ontoObject.getLat());
 	// queryObject[6] = createSparqlQuery(object, ontoObject.getLong());
 	//
 	// /**
 	// // Test
 	// for (int k = 0; k < numberOfProperties; k++) {
 	// echo(k + ".QUERY SUB : " + querySubject[k]
 	// + ". Length : " + querySubject[k].length());
 	// echo(k + ".   QUERY OB : " + queryObject[k]
 	// + ".Length : " + queryObject[k].length());
 	// }
 	// // */
 	//
 	// // Execute SPARQL query
 	// try {
 	// for (int i = 0; i < numberOfProperties; i++) { // No query for image
 	// echo("####" + i + ". Property####");
 	// System.out.print("(Value Subject)  =  ");
 	// if (querySubject[i].isEmpty()) {
 	// textSubject[i] = PropertyConstants.NO_DECLARATION;
 	// System.out.print("\n");
 	// } else {
 	// if (i == 4) { // Image -> limit to 1 image
 	// if (xml.getAttribute(ontoSubject.getName(),
 	// PropertyConstants.IMAGE, "linkedData").contains("yes")) {
 	// echo("Subject: Found image with special case! "
 	// + xml.getAttribute(ontoSubject.getName(),
 	// PropertyConstants.IMAGE, "linkedData"));
 	// textSubject[i] = linkedData(subject);
 	// if(checkUrl(textSubject[i])==false)
 	// textSubject[i]="";
 	// } else
 	// textSubject[i] = sparqlQueryText(querySubject[i],
 	// endPointSubject, 1);
 	// } else
 	// textSubject[i] = sparqlQueryText(querySubject[i], endPointSubject);
 	// }
 	// System.out.print("(Value Object)   =  ");
 	// if (queryObject[i].isEmpty()) {
 	// textObject[i] = PropertyConstants.NO_DECLARATION;
 	// System.out.print("\n");
 	// } else {
 	// if (i == 4) {
 	// if (xml.getAttribute(ontoObject.getName(),
 	// PropertyConstants.IMAGE, "linkedData").equals("yes")) {
 	// echo("Object: Found image with special case: "
 	// + xml.getAttribute(ontoObject.getName(),
 	// PropertyConstants.IMAGE, "linkedData"));
 	// textObject[i] = linkedData(object);
 	// if(checkUrl(textObject[i])==false)
 	// textObject[i]="";
 	// } else
 	// textObject[i] = sparqlQueryText(queryObject[i], endPointObject,
 	// 1);
 	// } else
 	// textObject[i] = sparqlQueryText(queryObject[i], endPointObject);
 	// }
 	// // If SPARQL query execution returns an empty set for subject query.
 	// // Set status false and stop querying each property of this subject
 	// // (not!!)
 	// if (textSubject[i].isEmpty() && importantSubject[i].equals("yes")) {
 	// statusSubject = false;
 	// echo(subject
 	// + "\nError: Null result for SPARQL query. Subject " + i + " "
 	// + textSubject[i]);
 	// // break;
 	// }
 	// // If SPARQL QueryExecution returns an empty set for object query ->
 	// // status = false and break
 	// if (textObject[i].isEmpty() && importantObject[i].equals("yes")) {
 	// statusObject = false;
 	// echo(object
 	// + "\nError: Null result for SPARQL query. Object " + i + " "
 	// + textObject[i]);
 	// // break;
 	// }
 	//
 	// }
 	// //
 	// // // Handle Image separate, because it's not crucial if an instance
 	// // doesn't have an image
 	// // // Won't set status to false, if SPARQL Query fails
 	// // echo("####"+(numberOfProperties-1)+". Property####");
 	// // System.out.print("(Value Subject)  =  ");
 	// // if (querySubject[numberOfProperties-1].isEmpty())
 	// // textSubject[numberOfProperties-1] = PropertyConstants.NO_DECLARATION;
 	// // else
 	// // textSubject[numberOfProperties-1] =
 	// // sparqlQueryText(querySubject[4],endPointSubject);
 	// // System.out.print("(Value Object)  =  ");
 	// // if (queryObject[numberOfProperties-1].isEmpty())
 	// // textObject[numberOfProperties-1] =
 	// // sparqlQueryText(queryObject[4],endPointObject);
 	// // else
 	// // textObject[numberOfProperties-1] = PropertyConstants.NO_DECLARATION;
 	// //
 	// //
 	//
 	// // Insert parsed SPARQL results into database
 	// if ((statusSubject && statusObject) == true) { // If subject and object
 	// // are valid
 	// linkSuccess++;
 	// echo("\n[[[[[[ SUCCESS ]]]]]]");
 	// // Resource
 	// sqlQuerySubject = "INSERT IGNORE INTO instances VALUES ('" + subject
 	// + "','" + textSubject[0] + "','" + textSubject[1] + "','"
 	// + textSubject[2] + "','" + textSubject[3] + "','"
 	// + ontologyNameSubject + "','" + textSubject[4] + "','"
 	// + ontoSubject.getType() + "','" + textSubject[5] + "','"
 	// + textSubject[6] + "' )";
 	// db.queryUpdate(sqlQuerySubject, con);
 	//
 	// sqlQueryObject = "INSERT IGNORE INTO instances VALUES ('" + object
 	// + "','" + textObject[0] + "','" + textObject[1] + "','"
 	// + textObject[2] + "','" + textObject[3] + "','"
 	// + ontologyNameObject + "','" + textObject[4] + "','"
 	// + ontoObject.getType() + "','" + textObject[5] + "','"
 	// + textObject[6] + "' )";
 	// db.queryUpdate(sqlQueryObject, con);
 	//
 	// // SQL query for inserting (subject, predicate, object, confidence)
 	// // into table links
 	// sqlQueryStatements = "INSERT IGNORE INTO links ("
 	// + PropertyConstants.DB_TABLE_LINKS_PROPERTY_SUBJECT + ", "
 	// + PropertyConstants.DB_TABLE_LINKS_PROPERTY_PREDICATE + ","
 	// + PropertyConstants.DB_TABLE_LINKS_PROPERTY_OBJECT + ","
 	// + PropertyConstants.DB_TABLE_LINKS_PROPERTY_LINKED_ONTOLOGIES
 	// + "," + PropertyConstants.DB_TABLE_LINKS_PROPERTY_CONFIDENCE
 	// + ") VALUES ('" + subject + "','" + predicate + "','" + object
 	// + "','" + linkedOntologies + "',"
 	// + conf + " )";
 	// // ??? PropertyConstants.CONFIDENCE_NOT_SPECIFIED
 	// db.queryUpdate(sqlQueryStatements, con);
 	//
 	// } else {// Invalid RDF Result. Method sparqlQueryText returns "".
 	// // Therefore, set confidence -1.
 	// linkFailure++;
 	// echo("\n[[[[[[ FAIL ]]]]]]");
 	// sqlQueryStatements = "INSERT IGNORE INTO links ("
 	// + PropertyConstants.DB_TABLE_LINKS_PROPERTY_SUBJECT + ", "
 	// + PropertyConstants.DB_TABLE_LINKS_PROPERTY_PREDICATE + ","
 	// + PropertyConstants.DB_TABLE_LINKS_PROPERTY_OBJECT + ","
 	// + PropertyConstants.DB_TABLE_LINKS_PROPERTY_LINKED_ONTOLOGIES
 	// + "," + PropertyConstants.DB_TABLE_LINKS_PROPERTY_CONFIDENCE
 	// + ") VALUES ('" + subject + "','" + predicate + "','" + object
 	// + "','" + linkedOntologies + "',"
 	// + PropertyConstants.CONFIDENCE_ERROR + " )";
 	//
 	// }
 	//
 	// // SQL query execution: Insert into table links
 	// // db.queryUpdate(sqlQueryStatements,con);
 	//
 	// } catch (Exception e) {
 	// echo("Execute SPARQL Error: " + e.getMessage());
 	// }
 	//
 	// // Ausgabe
 	// // System.out.print("ausgabe:     "+ subject + "   ");
 	// // System.out.print(predicate + "   ");
 	// // echo(object);
 	//
 	// }
 	// echo("###RDFHandler: Querying rdf statements done###");
 	//
 	// // Insert linkedOntologies into table linkedOntologies
 	// String sqlLinkedOntologies = "REPLACE INTO "
 	// + PropertyConstants.DB_TABLE_NAME_LINKEDONTOLOGIES + "("
 	// + PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_SUBJECT + ","
 	// + PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_PREDICATE + ","
 	// + PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_OBJECT + ","
 	// + PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_DESCRIPTION + ","
 	// + PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_DIFFICULTY + ","
 	// + PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_READY + ") VALUES ('"
 	// + ontologyNameSubject + "','" + this.task.getPredicate() + "','"
 	// + ontologyNameObject + "','" + this.task.getDescription() + "','"
 	// + this.task.getDifficulty() + "','" + "1" + "')";
 	// if (linkSuccess > 3) {
 	// db.queryUpdate(sqlLinkedOntologies, con);
 	// } else
 	// msg += "RDFHandler: "
 	// + ontologyNameSubject
 	// + " and "
 	// + ontologyNameObject
 	// +
 	// " have less than 5 succesful links. Won't be added tp linkedOntologies table!\n";
 	//
 	// // Close db Connection
 	// try {
 	// echo("Close DB Connection..");
 	// con.close();
 	// echo("Done.");
 	// } catch (SQLException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// }
 	//
 	// msg += "RDFHandler: Querying and inserting rdf-statements done. "
 	// + "Links queried: " + linkCounter + ". Successful: " + linkSuccess
 	// + ". Failure: " + linkFailure;
 	// echo("\n\n\n___________________" + msg + "\n\n\n");
 	// return msg;
 	// }
 
 	private void echo(String string) {
 		System.out.println("##RDFHandler: " + string);
 	}
 
 	private String dbInsertFromXML(TemplateLinkset template) {
 		echo("##Insert from xml-file");
 		// String subject, predicate, object;
 
 		// status of querying and adding links
 		linkCounter = 0;
 		linkSuccess = 0;
 		linkFailure = 0;
 		linkAdded = 0;
 		linkConfidencePositive = 0;
 		linkConfidenceNegative = 0;
 
 		TemplateProperty dummyProp = new TemplateProperty();
 		dummyProp.setImportant("no");
 		dummyProp.setProperty("verli:dummy");
 
 		String msg = null;
 
 		String rdfQuery = "PREFIX align: <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#.>"
 				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
 				+ "SELECT ?entity1 ?rel ?entity2 ?measure WHERE { "
 				+ "?cell rdf:type <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#Cell> ."
 				+ "?cell <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1> ?entity1 ."
 				+ "?cell <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2> ?entity2 ."
 				+ "?cell <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure> ?measure ."
 				+ "?cell <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#relation> ?rel ."
 				+ "}";
 
 		echo("rdfQuery: " + rdfQuery);
 		// Init DBTool. Parameters: (server, database, user, pass)
 		// DBTool db = new DBTool("localhost","veri_links", "root", ""); // <---
 		// Change this
 		DBTool db = new DBTool(dbIniPath);
 		Connection con = db.getConnection(); // Connect to db
 		echo("Create XMLTool");
 		XMLTool xml = new XMLTool(templateFile);
 		try {
 			xml.readTemplateFile();
 		} catch (ParserConfigurationException e1) {
 			echo("XMLTool Error: " + e1.getMessage());
 			e1.printStackTrace();
 		} catch (SAXException e1) {
 			echo("XMLTool Error: " + e1.getMessage());
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			echo("XMLTool Error: " + e1.getMessage());
 			e1.printStackTrace();
 		}
 		echo("Create XMLTool done.");
 
 		echo("Get Subject and Object");
 		subjectTemplate = template.getSubject();
 		objectTemplate = template.getObject();
 
 		echo("Query RDF model");
 		String rdfResult = "";
 
 		QueryExecution qExec = QueryExecutionFactory.create(rdfQuery, model);
 
 		com.hp.hpl.jena.query.ResultSet rs = qExec.execSelect();
 		// QuerySolution binding = rs.nextSolution();
 		// text = binding.toString();
 		while (rs.hasNext()) { // if query is successful
 			// When stop?
 			if (linkAdded > 50)
 				break;
 
 			echo("Result hasNext..");
 			statusSubject = true;
 			statusObject = true;
 
 			QuerySolution binding = rs.nextSolution();
 			rdfResult = binding.toString();
 			// processResult(rdfResult);
 			processResult(binding);
 			// echo(processResult(rdfResult));
 
 			linkCounter++;
 
 			// Should add?
 			if (confidence == 1 || confidence == -2) {
 				if ((linkConfidencePositive + linkConfidenceNegative) > 60)
 					continue;
 				else if (linkConfidencePositive > 30 && confidence == 1)
 					continue;
 				else if (linkConfidenceNegative > 30 && confidence == -2)
 					continue;
 			}
 
 			// Standard SPARQL Query for a Property
 			List<TemplateProperty> properties = subjectTemplate.getProperties();
 			for (int j = 0; j < properties.size(); j++) {
 				subjectSparql.add(createSparqlQuery(subject, properties.get(j)));
 			}
 			properties = objectTemplate.getProperties();
 			for (int j = 0; j < properties.size(); j++) {
 				objectSparql.add(createSparqlQuery(object, properties.get(j)));
 			}
 			//
 			// Test
 			echo("Print SPARQL queries:");
 			for (int k = 0; k < subjectSparql.size(); k++) {
 				echo(k + ".SPARQL Subject: " + subjectSparql.get(k));
 			}
 			for (int k = 0; k < objectSparql.size(); k++) {
 				echo(k + ".SPARQL Object: " + objectSparql.get(k));
 			}
 
 			// Execute SPARQL query
 			try {
 				echo("Execute sparql query");
 				// Find instance with higher numbers of properties
 				int length = 0;
 				String lData = "";
 				TemplateProperty subjectProp = null;
 				TemplateProperty objectProp = null;
 				if (subjectTemplate.getProperties().size() >= objectTemplate
 						.getProperties().size())
 					length = subjectTemplate.getProperties().size();
 				else
 					length = objectTemplate.getProperties().size();
 
 				for (int i = 0; i < length; i++) { // No query for image
 					echo("####" + i + ". Property####");
 					echo("(Value Subject)  =  ");
 					if (subjectSparql.size() <= i) {// if index is bigger than subject
 																					// size
 						subjectText.add(PropertyConstants.NO_DECLARATION);
 						subjectProp = dummyProp;
 						echo("Index bigger than subject propertyList size");
 					} else {
 						if (subjectSparql.get(i).isEmpty()) {
 							subjectText.add(PropertyConstants.NO_DECLARATION);
 							echo("Subject Empty\n");
 						} else {
 							subjectProp = subjectTemplate.getProperties().get(i);
 							// LinkedData
 							if (subjectProp.getLinkedData() != null) {
 								echo("Subject LinkedData found!");
 								lData = linkedData(subject);
 								if (checkUrl(lData) == false)
 									lData = "";
 								subjectText.add(lData);
 							} else { // Normal
 								subjectText.add(sparqlQueryText(subject, subjectSparql.get(i),
 										endPointSubject, subjectProp.getLimit()));
 							}
 						}
 					}
 
 					echo("(Value Object)  =  ");
 					if (objectSparql.size() <= i) {
 						objectText.add(PropertyConstants.NO_DECLARATION);
 						objectProp = dummyProp;
 						echo("Index bigger than object propertyList size");
 					} else {
 						if (objectSparql.get(i).isEmpty()) {
 							objectText.add(PropertyConstants.NO_DECLARATION);
 							echo("Object Empty\n");
 						} else {
 							objectProp = objectTemplate.getProperties().get(i);
 							// LinkedData
 							if (objectProp.getLinkedData() != null) {
 								echo("Object LinkedData found!");
 								lData = linkedData(object);
 								if (checkUrl(lData) == false)
 									lData = "";
 								objectText.add(lData);
 							} else { // Normal
 								objectText.add(sparqlQueryText(object, objectSparql.get(i),
 										endPointObject, objectProp.getLimit())); // limit 1
 							}
 						}
 					}
 					// If SPARQL query execution returns an empty set for subject query.
 					// Set status false and stop querying each property of this subject
 					// (not!!)
 					if (subjectText.get(i).isEmpty()
 							&& subjectProp.getImportant().equals("yes")) {
 						statusSubject = false;
 						echo(subject
 								+ "\nError: Null result for SPARQL query. Subject at index "
 								+ i + " " + subjectText.get(i));
 						// break;
 					}
 					// If SPARQL QueryExecution returns an empty set for object query ->
 					// status = false and break
 					if (objectText.get(i).isEmpty()
 							&& objectProp.getImportant().equals("yes")) {
 						statusObject = false;
 						echo(object
 								+ "\nError: Null result for SPARQL query. Object at index " + i
 								+ " " + objectText.get(i));
 						// break;
 					}
 
 				}
 				// Insert parsed SPARQL results into database
 				 if ((statusSubject && statusObject) == true) { // If subject and object are valid
 //				if (true) {
 					// Stats
 					linkSuccess++;
 					if (confidence == 1)
 						linkConfidencePositive++;
 					if (confidence == -2) { // transfrom to verli value
 						linkConfidenceNegative++;
 					}
 
 					linkAdded++;
 
 					System.out.println("\n[[[[[[ Success ]]]]]]");
 					System.out.println("linkAdded: " + linkAdded);
 
 					// SQL
 					// Instances
 					List<TemplateProperty> props = null;
 					props = subjectTemplate.getProperties(); // Subject
 					sqlQuerySubject = "";
 					for (int m = 1; m < props.size(); m++) { // Properties
 						sqlQuerySubject = "INSERT IGNORE INTO instances VALUES ( '"
 								+ subject + "' , '" + props.get(m).getProperty() + "' , '"
 								+ subjectText.get(m) + "'); ";
 						echo("Subject SQL Query: " + sqlQuerySubject);
 						db.queryUpdate(sqlQuerySubject, con);
 					}
 					// Endpoint
 					sqlQuerySubject = "INSERT IGNORE INTO instances VALUES ( '"
 						+ subject + "' , 'verli:ontology' , '"
 						+ subjectTemplate.getOntology() + "'); ";
 					echo("Subject SQL Query: " + sqlQuerySubject);
 					db.queryUpdate(sqlQuerySubject, con);
 					props = objectTemplate.getProperties(); // Object
 					sqlQueryObject = "";
 					for (int m = 1; m < props.size(); m++) { // Properties
 						sqlQueryObject = "INSERT IGNORE INTO instances VALUES ( '" + object
 								+ "' , '" + props.get(m).getProperty() + "' , '"
 								+ objectText.get(m) + "'); ";
 						echo("Object SQL Query: " + sqlQueryObject);
 						db.queryUpdate(sqlQueryObject, con);
 					}
 					// Endpoint
 					sqlQueryObject = "INSERT IGNORE INTO instances VALUES ( '" + object
 							+ "' , 'verli:ontology' , '"
 							+ objectTemplate.getOntology() + "'); ";
 					echo("Object SQL Query: " + sqlQueryObject);
 					db.queryUpdate(sqlQueryObject, con);
 					
 					// SQL query for inserting (subject, predicate, object, confidence)
 					// into table links
 					sqlQueryStatements = "INSERT IGNORE INTO links ("
 							+ PropertyConstants.DB_TABLE_LINKS_PROPERTY_SUBJECT + ", "
 							+ PropertyConstants.DB_TABLE_LINKS_PROPERTY_PREDICATE + ","
 							+ PropertyConstants.DB_TABLE_LINKS_PROPERTY_OBJECT + ","
 							+ PropertyConstants.DB_TABLE_LINKS_PROPERTY_LINKED_ONTOLOGIES
 							+ "," + PropertyConstants.DB_TABLE_LINKS_PROPERTY_CONFIDENCE
 							+ ") VALUES ('" + subject + "','" + predicate + "','" + object
 							+ "','" + linkset + "'," + confidence + " )";
 					echo("SQL QUERY statement: " + sqlQueryStatements);
 					// SQL query execution: Insert into table links
 					db.queryUpdate(sqlQueryStatements, con);
 
 				} else {// Invalid RDF Result. Method sparqlQueryText returns "".
 								// Therefore, set confidence -1.
 					linkFailure++;
 					System.out.println("\n[[[[[[ FAIL ]]]]]]");
 					System.out.println("linkFailure: " + linkFailure);
 				}
 
 			} catch (Exception e) {
 				System.out.println("Error: " + e.getMessage());
 			}
 
 			// Ausgabe
 			echo(linkCounter + ".link s:" + subject + " p: " + predicate + "o: "
 					+ object + "\n\n");
 
 			// Reset
 			subjectSparql = new ArrayList<String>();
 			objectSparql = new ArrayList<String>();
 			subjectText = new ArrayList<String>();
 			objectText = new ArrayList<String>();
 			
 		}
 		echo("Querying rdf statements done");
 
		String setTaskSuccessQuery = "UPDATE tasks set Done = true WHERE Linkset ='"+this.task.getLinkset()+"' and File ='"+this.task.getLinkFile()+"'";
		String setTemplatesSuccessQuery = "UPDATE templates set Ready = true where ID='"+this.task.getTemplate()+"' AND linkset ='"+this.task.getLinkset()+"'";
 		
 		
 		if (linkSuccess > 3) {
			echo("Task Query: "+setTaskSuccessQuery);
 			db.queryUpdate(setTaskSuccessQuery, con);
			echo("Template Query: "+setTemplatesSuccessQuery);
 			db.queryUpdate(setTemplatesSuccessQuery, con);
 		} else
 			msg += "RDFHandler: "
 					+ ontologyNameSubject
 					+ " and "
 					+ ontologyNameObject
 					+ " have less than 5 succesful links. Won't be added tp linkedOntologies table!\n";
 
 		// Close db Connection
 		try {
 			echo("Close DB Connection..");
 			con.close();
 			echo("Done.");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		msg += "RDFHandler: Querying and inserting rdf-statements done. "
 				+ "Links queried: " + linkCounter + ". Successful: " + linkSuccess
 				+ ". Failure: " + linkFailure + ", positiveConfidence: "
 				+ linkConfidencePositive + ", negativeConfidence: "
 				+ linkConfidenceNegative + " LinksAdded: " + linkAdded;
 		System.out.println("\n\n\n___________________" + msg + "\n\n\n");
 		return msg;
 
 	}
 
 	public void processResult(QuerySolution binding) {
 		echo("Process results: " + binding);
 
 		subject = binding.get("?entity1").toString();
 		object = binding.get("?entity2").toString();
 		String confidenceS = binding.get("?measure").toString();
 		predicate = binding.get("rel").toString();
 		//
 		// echo("--------------raw: s: "+subject);
 		// echo("raw: o: "+object);
 		// echo("raw: p: "+predicate);
 		// echo("raw: c: "+confidenceS);
 
 		subject = binding.get("?entity1").toString();
 		object = binding.get("?entity2").toString();
 		confidence = parseConfidence(binding.get("?measure").toString());
 		predicate = parseRelation(binding.get("rel").toString());
 
 		// Parse subject, predicate, obect, confidence
 		// String subject, predicate, object,confidence;
 		int pointer;
 
 		// pointer = binding.indexOf(')');
 		// subject = binding.substring(binding.indexOf('(') + 1, pointer++);
 		// binding = binding.substring(pointer++);
 		// echo("s: "+subject);
 		// pointer = binding.indexOf(')');
 		// object = binding.substring(binding.indexOf('(') + 1, pointer++);
 		// binding = binding.substring(pointer++);
 		// echo("o: "+object);
 		// pointer = binding.indexOf(')');
 		// String confidenceString = binding.substring(binding.indexOf('(') + 1,
 		// pointer++);
 		// binding = binding.substring(pointer++);
 		// echo("conf: "+confidenceString);
 		// pointer = binding.indexOf(')');
 		// predicate = binding.substring(binding.indexOf('(') + 1,
 		// pointer++);
 		// // result=result.substring(pointer+2);
 		// echo("predicate: "+predicate);
 		// //
 		// echo("\nVOR PRRIINNNTTTTZZ\n"+subject+"\n"+predicate+"\n"+object+"\n"+confidence);
 		//
 		// // Parsed string
 		// subject = parseURL(subject);
 		// object = parseURL(object);
 		// predicate = parseRelation(predicate);
 		// confidence = parseConfidence(confidenceString);
 
 		echo("Process results done.");
 
 		System.out.println("<<<<<<<<<<<<<<<<<<<<" + (linkCounter)
 				+ ".Link >>>>>>>>>>>>>>>>>>\n" + subject + "\n" + predicate + "\n"
 				+ object + "\n" + confidence);
 	}
 
 	private String parseRelation(String s) {
 		// echo("raw Relation: "+s);
 		String relation = s.trim();
 		// String newline = System.getProperty("line.separator");
 		// int index = s.lastIndexOf("n");
 		// String relation = s.substring(0, index - 1);
 		// relation = relation.substring(relation.lastIndexOf(' ') + 1);
 		if (relation.equals("="))
 			relation = "owl:sameAs";
 		// echo("indexx "+index); ----unten
 		echo("Parse relation: " + relation);
 		return relation;
 	}
 
 	private double parseConfidence(String s) {
 		// String conf = s.substring(0, s.lastIndexOf("n") - 1);
 		// conf = conf.substring(conf.lastIndexOf(' ') + 1);
 		// double round = Double.parseDouble(conf);
 		// round = Math.round(round * 10000d) / 10000d;
 		// // echo(round); ---unten
 		// if(round == 0.0)
 		// round=-2;
 		int start = s.indexOf('(') + 1;
 		int end = s.indexOf(')');
 		String conf = s.substring(start, end);
 		// conf = conf.substring(conf.lastIndexOf(' ') + 1);
 		double round = Double.parseDouble(conf);
 		round = Math.round(round * 10000d) / 10000d;
 		// echo(round); ---unten
 		if (round == 0.0)
 			round = -2;
 		echo("parse confidence: " + round);
 		return round;
 	}
 
 	/**
 	 * Method for creating veri_links database and tables. Chacnge connection data
 	 * according to your database.
 	 * 
 	 * @throws SQLException
 	 */
 	private void createDatabase() throws SQLException {
 
 		DBTool db = new DBTool(dbIniPath);
 		db.createDatabase();
 	}
 
 	public void setTask(Task t) {
 		this.task = t;
 	}
 
 	/**
 	 * Returns image uri
 	 * 
 	 * @param uri
 	 * @return null if not found
 	 */
 	public String linkedData(String uri) {
 		echo("LinkedData for " + uri);
 		modelLinkedData = ModelFactory.createDefaultModel();
 		JenaReader jr = new JenaReader();
 		jr.read(modelLinkedData, uri);
 		// modelLinkedData.write(System.out, "TURTLE");
 		return findImage(uri, modelLinkedData);
 	}
 
 	public String findImage(String uri, Model model) {
 		echo("Find image");
 		String subject, object, predicate;
 		StmtIterator iter = model.listStatements();
 		while (iter.hasNext()) {
 			Statement stmt = iter.nextStatement();
 
 			subject = stmt.getSubject().toString();
 			predicate = stmt.getPredicate().toString();
 			object = stmt.getObject().toString();
 
 			echo(subject + " , " + predicate + " , " + object);
 			if (object.equals("http://xmlns.com/foaf/0.1/Image")) {
 				echo("Found image: " + subject);
 				echo(subject + " , " + predicate + " , " + object);
 				return "<" + subject + ">";
 			}
 		}
 		echo("Image not found!");
 		return null;
 	}
 
 	private boolean checkUrl(String url) {
 		System.out.println("Check Url: " + url);
 		if (url.contains("<"))
 			url = url.replace("<", "");
 		if (url.contains(">"))
 			url = url.replace(">", "");
 		URL u;
 		int code = 0;
 		try {
 			// u = new URL (
 			// "http://static.bbci.co.uk/naturelibrary/3.1.1.0/images/ic/maps/366x217/species/European_Stonechat.gif");
 			u = new URL(url);
 			HttpURLConnection huc = (HttpURLConnection) u.openConnection();
 			huc.setRequestMethod("GET"); // OR huc.setRequestMethod ("HEAD");
 			huc.connect();
 			code = huc.getResponseCode();
 			System.out.println(code);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (code == 200) {
 			System.out.println("Check Url Succes");
 			return true;
 		} else {
 			System.out.println("Check Url Fail. Code: " + code);
 			return false;
 		}
 	}
 
 	public static void main(String args[]) {
 
 		// Test current dir
 		String dir = "user.dir"; // set to current directory
 		try {
 			dir = new File(System.getProperty(dir)).getCanonicalPath();
 		} catch (IOException e1) {
 		}
 		System.out.println("Current dir : " + dir);
 
 		Model model = ModelFactory.createDefaultModel();
 
 		// // Test Start
 		// method------------------------------------------------------------
 		// String path =
 		// "/home/q1/Desktop/linkFiles/factbooklang_links_verify131.xml";
 		String path = "/home/q1/Desktop/linkFiles/final/dbpedia_factbook/dbpedia_factbook_lang_links_verify.xml";
 		// path
 		// ="/home/q1/Desktop/linkFiles/spec/silk_2.5.3/spec/output/final/dbpedia-factbook-check-links.xml";
 		// path
 		// ="/home/q1/Desktop/linkFiles/final/dbpedia_factbook/1.dbpedia_factbook_lang_links_verify.xml";
 		// path
 		// ="/home/q1/Desktop/linkFiles/final/dbpedia_factbook/dbpedia-factbooklang_negative.nt";
 		// path
 		// ="/home/q1/Desktop/rdf linkFiles/final/dbpedia_factbook/dbpedia-factbooklang_positive.nt";
 
 		// path="/home/q1/Desktop/linkFiles/final/dbpedia_bbcwildlife/1.dbpedia_bbcwildlife_links_accepted.xml";
 		// path="/home/q1/Desktop/linkFiles/final/dbpedia_bbcwildlife/2.BBCwildlife_links_verify_final.xml";
 //		 path="/home/q1/Desktop/rdf linkFiles/final/dbpedia_bbcwildlife/3.BBCwildlife_links_accepted_new.xml";
 
 //		 path="/home/q1/Desktop/rdf linkFiles/final/dbpedia_lgd/dbpedia_lgd_countries_check.xml";
 		path = "/home/q1/Desktop/rdf linkFiles/final/dbpedia-factbook-check-links.xml";
 		String db = "src/main/resources/net/saim/game/resources/db_settings.ini";
 		String temp = "src/main/resources/net/saim/game/resources/LinkTemplates.xml";
 		System.out.println("Create XMLTool");
 		XMLTool xml = new XMLTool(temp);
 		try {
 			xml.readTemplateFile();
 		} catch (ParserConfigurationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (SAXException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		System.out.println("Create XMLTool done");
 		Task t = new Task();
 		t.setDescription("descr");
 		t.setDifficulty("hard");
 		t.setObject("factbook");
 		t.setSubject("dbpedia");
 		t.setPredicate("spoken in");
 		RDFHandler rdf = new RDFHandler(path, db, temp, t);
 		TemplateLinkset template = xml.getLinkset("dbpedia-factbook");
 		System.out.println("RDFHandler start");
 //		rdf.start(template, RDFHandler.formatXML);
 		// Test Start ------------------------------------------------------------
 
 		// Set path of the link file, which holds the semantic web links
 		// String path = "linkFiles/factbooklang_links_verify.nt"; // <--- Change
 		// this
 		// String path = "linkFiles/BBCwildlife_links_accepted.nt";
 		// String path = "linkFiles/dbpedia-linkedgeodata-countrylinks4.nt";
 		// String path = "linkFiles/factbooklang_links_verify.xml";
 		// String path =
 		// "C:/Informatik/jogre/games/ba/dbpedia_linkedmdb_films_verify.xml";
 		// String path = "C:/pbac_dbpedia_links.nt";
 		// RDFHandler rdf = new RDFHandler(path);
 		// rdf.linkedData("http://www.bbc.co.uk/nature/species/Mountain_Hare#species");
 
 		// Linked
 		// Data------------------------------------------------------------------
 		// String uri="http://www.bbc.co.uk/nature/species/Mountain_Hare#species";
 		// String
 		// ob="Mountain hares have evolved to change colour from dappled shades of brown to white, keeping them  against the winter snow. They are at their most vulnerable when they are still young leverets and preyed upon by foxes, stoats, birds of prey and cats.";
 		// JenaReader jr = new JenaReader();
 		// jr.read(model, uri);
 		//
 		// StmtIterator iter = model.listStatements();
 		// while (iter.hasNext()) {
 		// Statement stmt = iter.nextStatement();
 		//
 		// String subject = stmt.getSubject().toString();
 		// String predicate = stmt.getPredicate().toString();
 		// String object = stmt.getObject().toString();
 		//
 		// System.out.println(subject+" , "+predicate+ " , "+object);
 		//
 		// }
 
 		// Query Test
 		System.out.println("Query Test");
 		 String query
 		 ="Select * where { <http://www.bbc.co.uk/nature/species/Mountain_Hare#species> <http://purl.org/dc/terms/description> ?x}";
 		 query
 		 ="SELECT distinct ?x WHERE {<http://www4.wiwiss.fu-berlin.de/factbook/resource/Niger> <http://www.w3.org/2000/01/rdf-schema#label> ?x } limit 10";
 		 System.out.println("Query: "+query);
 		 String text = "";
 		 QueryExecution qExec =
 		 QueryExecutionFactory.sparqlService("http://www4.wiwiss.fu-berlin.de/factbook/sparql",
 		 query);
 		
 		 QuerySolution binding = null;
 		 // How many values of a property should be queried
 		 try {
 		 com.hp.hpl.jena.query.ResultSet rs = qExec.execSelect();
 		 System.out.println("echo: ");
 		 while (rs.hasNext()) { // if query is successful
 		
 			 binding = rs.nextSolution();
 			 text = binding.toString();
 			 System.out.println("results: "+text);
 		 }
 		 }catch(Exception e){
 			 System.out.println(e.getMessage());
 		 }
 		 System.out.println("done");
 		 
 //		 // QUERY test 2
 //		 String queryString ="SELECT distinct * WHERE {<http://www4.wiwiss.fu-berlin.de/factbook/resource/Niger> ?p ?x } limit 100";
 //      Query query = QueryFactory.create(queryString);
 //      QueryEngineHTTP qexec = (QueryEngineHTTP)QueryExecutionFactory.createServiceRequest("http://www4.wiwiss.fu-berlin.de/factbook/sparql", query);
 //      try {
 //          ResultSet results = qexec.execSelect();
 //          while ( results.hasNext() ) {
 //              QuerySolution soln = results.nextSolution();
 //              System.out.println("out: "+soln.toString());
 //          }
 //      } finally {
 //          qexec.close();
 //      }
 		 
 		 
 	}
 
 }

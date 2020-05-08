 package org.aksw.verilinks.server;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Connection;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.aksw.verilinks.server.msg.Task;
 import org.aksw.verilinks.server.msg.Template;
 import org.aksw.verilinks.server.tools.DBTool;
 import org.aksw.verilinks.server.tools.PropertyConstants;
 import org.aksw.verilinks.server.tools.XMLTool;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 import org.xml.sax.SAXException;
 
 /**
  * Servlet implementation class TemplateUploadServlet
  */
 public class TemplateUploadServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public TemplateUploadServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
     private static final String UPLOAD_DIRECTORY = "d:\\uploaded\\";
 	private static final String DEFAULT_TEMP_DIR = ".";
 	private static final String taskFile = "task.tk";
 
 	public final static String TEMPLATE = "templateElement";
 	public final static String JSRENDER = "jsrenderElement";
 	public final static String LINKSET = "linksetElement";
 	
 	public final static String NAME = "nameElement";
 	public final static String ENDPOINT = "endpointElement";
 	public final static String PROP = "propElement";
 
 	public final static String NAME_2 = "nameElement2";
 	public final static String ENDPOINT_2 = "endpointElement2";
 	public final static String PROP_2 = "propElement2";
 
 
 	public final static String FILE = "fileElement";
 	public final static String DIFFICULTY = "difficultyElement";
 
 	public final static String PREDICATE = "predicateElement";
 	public final static String DESCRIPTION = "descriptionElement";
 
 	private String name = "";
 	private String endpoint = "";
 	private String prop = "";
 	private Set<String> propList = new HashSet<String>();
 
 	private String name_2 = "";
 	private String endpoint_2 = "";
 	private String prop_2 = "";
 	private Set<String> propList_2 = new HashSet<String>();
 	
 	private String fileName;
 	private String filePath;
 	private String difficulty=null;
 	private String predicate;
 	private String description;
 	private String id;
 	private String jsrender = null;
 	private String template = null;
 	private String linkset = null;
 	
 	
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		super.doGet(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		System.out.println("####Upload Servlet: Receiving formular");
 		echo("Post: " + req.toString());
 		echo("ConenType: " + req.getContentType());
 		echo("Path: " + req.getContextPath());
 		echo("cntLength: "+req.getContentLength());
 		
 		// process only multipart requests
 		if (ServletFileUpload.isMultipartContent(req)) {
 
 			File tempDir = getTempDir();
 			if (!tempDir.exists()) {
 				tempDir.mkdirs();
 			}
 
 			// Create a factory for disk-based file items
 			FileItemFactory factory = new DiskFileItemFactory();
 
 			// Create a new file upload handler
 			ServletFileUpload upload = new ServletFileUpload(factory);
 
 			// Parse the request
 			try {
 				List<FileItem> items = upload.parseRequest(req);
 				System.out.println("Number of items: " + items.size());
 				fileName = null;
 				for (FileItem fileItem : items) {
 					// process only file upload
 					// System.out.println("FileItem name: "+fileItem.getFieldName());
 					String itemName = fileItem.getFieldName();
 					echo("itemName: "+itemName);
 					
 					// first
 					if (itemName.equals(NAME))
 						name = fileItem.getString();
 					if (itemName.equals(ENDPOINT))
 						endpoint = fileItem.getString();
 					if (itemName.equals(PROP)){
 						propList.add(fileItem.getString());
 					}
 					// second
 					if (itemName.equals(NAME_2))
 						name_2 = fileItem.getString();
 					if (itemName.equals(ENDPOINT_2))
 						endpoint_2 = fileItem.getString();
 					if (itemName.equals(PROP_2))
 						propList_2.add(fileItem.getString());
 
 					
 					if (itemName.equals(DIFFICULTY))
 						difficulty = fileItem.getString();
 					if (itemName.equals(PREDICATE))
 						predicate = fileItem.getString();
 					if (itemName.equals(DESCRIPTION))
 						description = fileItem.getString();
 					if (itemName.equals(TEMPLATE))
 						template = fileItem.getString();
 					if (itemName.equals(JSRENDER))
 						jsrender = fileItem.getString();
 					if (itemName.equals(LINKSET))
 						linkset = fileItem.getString();
 					if (itemName.equals(FILE)) {
 						fileName = fileItem.getName();
 
 						// Debug show received file
 						System.out.println("FieldName:    "
 								+ fileItem.getFieldName());
 						System.out.println("Name:    " + fileItem.getName());
 						System.out.println("Contenttype:    "
 								+ fileItem.getContentType());
 						System.out.println("Size:    " + fileItem.getSize());
 						// System.out
 						// .println("String:    " + fileItem.getString()); //
 
 						// get only the file name not whole path
 						if (fileName != null) {
 							fileName = FilenameUtils.getName(fileName);
 						}
 
 						File uploadedFile = new File(getResourceDir()
 								+ "linkFiles/", fileName);
 						filePath = uploadedFile.getAbsolutePath();
 						if (uploadedFile.createNewFile()) {
 							fileItem.write(uploadedFile);
 							resp.setStatus(HttpServletResponse.SC_CREATED);
 							resp.getWriter()
 									.println("The resource file was uploaded!");
 							resp.flushBuffer();
 						} else
 							throw new IOException(
 									"The file already exists in repository.");
 					}
 
 				}
 
 				// Add to Sparql template file
//				addToSparqlTemplate();
 
 				// Add linkedOntologies+difficulty into db
//				addTask();
 				
 				// Create JsRender Template file in folder templates/
 				createJsRenderTemplate(resp);
 				
 				// add task to task-file, for later processing
 				// updateDatabase(filePath);
 //				addTask(name, name_2, predicate, description, difficulty,
 //						fileName, id);
 
 				System.out.println("Print received template");
 				System.out.println("First");
 				System.out.println("Name: " + name);
 				System.out.println("EP: " + endpoint);
 				Iterator<String> itr = propList.iterator();
 			    while(itr.hasNext()){
 				      System.out.println("PROP: "+itr.next());
 				}
 				
 				System.out.println("\nSecond");
 				System.out.println("Name: " + name_2);
 				System.out.println("EP: " + endpoint_2);
 				itr = propList_2.iterator();
 			    while(itr.hasNext()){
 				      System.out.println("PROP_2: "+itr.next());
 				}
 			
 				echo("Predicate: " + predicate);
 				echo("Description: " + description);
				System.out.println("\nDifficulty: " + difficulty);
				System.out.println("\nFileName: " + fileName);
 				echo("Linkset: "+linkset);
 				echo("Template: "+template);
 				echo("JsRender: "+jsrender);
 				flush();
 
 				resp.setStatus(HttpServletResponse.SC_CREATED);
 				resp.getWriter()
 						.println("Adding Link Task done! Now you can choose to perform the Link Task!");
 				resp.flushBuffer();
 				echo("Receiving formular done");
 
 			} catch (Exception e) {
 				resp.sendError(
 						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 						"An error occurred while adding template : "
 								+ e.getMessage());
 			}
 
 		} else {
 			resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
 					"Request contents type is not supported by the servlet.");
 		}
 	}
 
 	private File getTempDir() {
 		return new File(DEFAULT_TEMP_DIR);
 	}
 
 	private String getResourceDir() {
 		String prefix = getServletContext().getRealPath("");
 		if (!prefix.endsWith("/")) {
 			prefix += '/';
 		}
 		// resourcePath = prefix+"WEB-INF/classes/";
 		String resourcePath = prefix + "WEB-INF/classes/";
 		System.out.println("prefix: " + prefix);
 		System.out.println("resourcePath: " + resourcePath);
 
 		return resourcePath;
 	}
 
 	private void createJsRenderTemplate(HttpServletResponse resp) throws IOException{
 		echo("Create JsRender Template");
 		if(template==null ){
 			echo("Template ID wasn't specified");
 			return;
 		}
 		if(jsrender==null ){
 			echo("No JsRender Template specified");
 			return;
 		}
 		String templateFile = template+".tmpl.html";
 		BufferedWriter outputStream = null;
 		try {
 				outputStream = new BufferedWriter(new FileWriter(
 						getResourceDir() + "templates/" + templateFile, true));
 				outputStream.write(jsrender);
 				outputStream.newLine();
 				System.out.println("Wrote to File: " + jsrender);
 				resp.setStatus(HttpServletResponse.SC_CREATED);
 				resp.getWriter()
 						.println("JsRender Template '"+templateFile+"' created! ");
 				echo("Create JsRender Template Done!");
 		} finally {
 			try {
 				if (outputStream != null)
 					outputStream.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void addToSparqlTemplate() {
 		System.out.println("ADDD Sparql Template---------------------- ");
 		XMLTool xml = new XMLTool(getResourceDir() + "Templates.xml");
 
 		// xml.addOntology(temp);
 		try {
 			xml.readTemplateFile();
 			System.out.println("##XML Tool: Reading Done");
 		} catch (ParserConfigurationException e) {
 			System.out.println("Error: " + e.getMessage());
 			e.printStackTrace();
 		} catch (SAXException e) {
 			System.out.println("Error: " + e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("Error: " + e.getMessage());
 			e.printStackTrace();
 		}
 		echo("blaaaaaaaa");
 		// Create templates
 		if (!endpoint.isEmpty()) {
 			Template temp = new Template(); // Template subject
 			temp.setName(name);
 			temp.setEndpoint(endpoint);
 //			Iterator<String> it = propList.iterator();
 //			while(it.hasNext()){
 //				temp.setProp(addBrackets(it.next()));
 //			}
 //			xml.addOntology(temp);
 			System.out.println("Add 1.Template DONE");
 		}
 		echo("1");
 		if (!endpoint_2.isEmpty()) { // Template object
 			Template temp2 = new Template();
 			temp2.setName(name_2);
 			temp2.setEndpoint(endpoint_2);
 //			Iterator<String> it = propList_2.iterator();
 //			while(it.hasNext()){
 //				temp2.setProp(addBrackets(it.next()));
 //			}
 //			xml.addOntology(temp2);
 			System.out.println("Add 2.Template DONE");
 		}
 		echo("2");
 
 		System.out.println("ADD Sparql Template DONE---------------------- ");
 	}
 	
 	
 	private void addTask(){
 		echo("Add Task into DB: ");
 		echo("Insert '" + name + "-" + name_2 + "' with difficulty '"
 				+ difficulty + "' into database");
 		
		if (!difficulty.isEmpty()){
 			echo("Won't add task! Difficulty is empty");
 			return;
 		}
 	
 		String taskQuery = "INSERT IGNORE INTO "
 			+ PropertyConstants.DB_TABLE_NAME_TASK + "("
 			+ PropertyConstants.DB_TABLE_TASK_LINKSET + ","
 			+ PropertyConstants.DB_TABLE_TASK_FILE + ","
 			+ PropertyConstants.DB_TABLE_TASK_TIME + ","
 			+ PropertyConstants.DB_TABLE_TASK_SUBJECT + ","
 			+ PropertyConstants.DB_TABLE_TASK_PREDICATE + ","
 			+ PropertyConstants.DB_TABLE_TASK_OBJECT +" , "
 			+ PropertyConstants.DB_TABLE_TASK_TEMPLATE +" , "
 			+ PropertyConstants.DB_TABLE_TASK_DESCRIPTION +" , "
 			+ PropertyConstants.DB_TABLE_TASK_DIFFICULTY +" , "
 			+ PropertyConstants.DB_TABLE_TASK_DONE 
 			+ ") VALUES ('" +linkset+"','"+fileName+"', now(),'"+ name + "','" + predicate + "','" + name_2+ "','"
 			+ template +"','"+ description+"','"+difficulty+"', 0 )";
 		System.out.println("taskQuery Query: " + taskQuery);
 		
 		String linksetQuery = "INSERT IGNORE INTO "
 				+ PropertyConstants.DB_TABLE_NAME_LINKEDONTOLOGIES + "("
 				+ PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_ID + ","
 				+ PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_SUBJECT + ","
 				+ PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_PREDICATE + ","
 				+ PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_OBJECT
 				+ ") VALUES ('" +linkset+"','"+ name + "','" + predicate + "','" + name_2+ "')";
 		System.out.println("Linkset Query: " + linksetQuery);
 		
 		String templateQuery = "INSERT IGNORE INTO "
 			+ PropertyConstants.DB_TABLE_NAME_TEMPLATES + "("
 			+ PropertyConstants.DB_TABLE_TEMPLATES_ID + ","
 			+ PropertyConstants.DB_TABLE_TEMPLATES_LINKSET + ","
 			+ PropertyConstants.DB_TABLE_TEMPLATES_DESCRIPTION + ","
 			+ PropertyConstants.DB_TABLE_TEMPLATES_DIFFICULTY + ","
 			+ PropertyConstants.DB_TABLE_TEMPLATES_READY
 			+ ") VALUES ('" + template + "','" + linkset + "','" + description+"','"+difficulty+"', 0 )";
 	System.out.println("Template Query: " + templateQuery);
 		
 		// db connect
 		try {
 			DBTool db = new DBTool(getResourceDir() + "db_settings.ini");
 			Connection con = db.getConnection(); // establish connection
 			db.queryUpdate(taskQuery, con);
 			db.queryUpdate(linksetQuery, con);
 			db.queryUpdate(templateQuery, con);
 
 		} catch (Exception e) {
 			echo("Server: ERROR inserting task: " + e.getMessage());
 		}
 		echo("Add Task Done!");
 	}
 
 	
 
 	private String addBrackets(String prop) {
 		String parsed = null;
 		if (!prop.isEmpty())
 			parsed = "<" + prop + ">";
 		echo("Add Brackets. Old: " + prop + " New: " + parsed);
 		return parsed;
 	}
 
 	private void flush() {
 		fileName =null;
 		jsrender = null;
 		template = null;
 		difficulty=null;
 		name = "";
 		endpoint = "";
 		propList.clear();
 
 		name_2 = "";
 		endpoint_2 = "";
 		propList_2.clear();
 	}
 
 	// private void updateDatabase(String filePath) {
 	// RDFHandler rdf = new RDFHandler(filePath,
 	// getResourceDir()+"db_settings.ini",getResourceDir()+"TemplatesOriginal.xml");
 	// System.out.println("Path: " +filePath +
 	// "\nxmlPath: "+getResourceDir()+"TemplatesOriginal.xml");
 	// XMLTool xml = new XMLTool(getResourceDir()+"TemplatesOriginal.xml");
 	// try {
 	// xml.readTemplateFile();
 	// } catch (ParserConfigurationException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// } catch (SAXException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// } catch (IOException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// }
 	// Template ontoSubject = xml.getTemplate("dbpedia");
 	// Template ontoObject = xml.getTemplate("factbook");
 	// System.out.println("Names: "+name+" , "+name_2);
 	// if (filePath.endsWith(".xml"))
 	// rdf.start(ontoSubject,ontoObject,RDFHandler.formatXML);
 	// else
 	// rdf.start(ontoSubject,ontoObject,RDFHandler.formatNT);
 	//
 	// }
 
 	/**
 	 * Add task to task-file. All the tasks in the task-file will be processed
 	 * later.
 	 * 
 	 * @param subjectName
 	 * @param objectName
 	 * @param fileName
 	 * @param description
 	 * @param fileName
 	 * @param linkFilePath
 	 *            path to link file
 	 */
 	private void addTask(String subjectName, String objectName,
 			String predicateName, String description, String difficulty,
 			String fileName, String linkset) {
 		System.out.println("##Upload Servlet: Add Task");
 		BufferedReader inputStream = null;
 		BufferedWriter outputStream = null;
 		try {
 			// Check if task already in task-file
 			inputStream = new BufferedReader(new FileReader(getResourceDir()
 					+ "linkFiles/" + this.taskFile));
 			echo("Check if task already in task-file: "+fileName);
 			String buffer;
 			boolean found = false;
 			while ((buffer = inputStream.readLine()) != null) {
 				if (buffer.contains(fileName)) {
 					echo("found");
 					found = true;
 					break;
 				}
 			}
 			inputStream.close();
 			if (found == false) {
 				echo("not found");
 				// Write to task file
 				outputStream = new BufferedWriter(new FileWriter(
 						getResourceDir() + "linkFiles/" + this.taskFile, true));
 				// buffer =
 				// linkFilePath.substring(linkFilePath.lastIndexOf('/')+1);
 				// Get Date
 				String date = now("yyyy/MM/dd-HH:mm:ss");
 				String line = subjectName + Task.SEPERATOR + objectName
 						+ Task.SEPERATOR + predicateName + Task.SEPERATOR
 						+ description + Task.SEPERATOR + difficulty
 						+ Task.SEPERATOR + fileName + Task.SEPERATOR + date
 						+ Task.SEPERATOR + linkset + Task.SEPERATOR + "0";
 				outputStream.write(line);
 				outputStream.newLine();
 				System.out.println("Wrote to File: " + line);
 			}
 			System.out.println("##Upload Servlet: Add Task Done\n");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			try {
 				if (inputStream != null)
 					inputStream.close();
 				if (outputStream != null)
 					outputStream.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public String now(String dateFormat) {
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
 		return sdf.format(cal.getTime());
 
 	}
 
 	private void echo(String s) {
 		System.out.println("##TemplateUploadServlet: " + s);
 	}
 
 }

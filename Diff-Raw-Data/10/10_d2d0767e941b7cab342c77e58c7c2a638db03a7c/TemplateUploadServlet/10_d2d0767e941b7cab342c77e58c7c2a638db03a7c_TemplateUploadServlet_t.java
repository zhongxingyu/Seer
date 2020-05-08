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
 import java.util.Calendar;
 import java.util.List;
 
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
 
 	public final static String LINKSET = "linksetElement";
 	public final static String NAME = "nameElement";
 	public final static String ENDPOINT = "endpointElement";
 	public final static String PROP0 = "prop0Element";
 	public final static String PROP1 = "prop1Element";
 	public final static String PROP2 = "prop2Element";
 	public final static String PROP3 = "prop3Element";
 	public final static String IMAGE = "imageElement";
 	public final static String TYPE = "typeElement";
 
 	public final static String NAME_2 = "nameElement2";
 	public final static String ENDPOINT_2 = "endpointElement2";
 	public final static String PROP0_2 = "prop0Element2";
 	public final static String PROP1_2 = "prop1Element2";
 	public final static String PROP2_2 = "prop2Element2";
 	public final static String PROP3_2 = "prop3Element2";
 	public final static String IMAGE_2 = "imageElement2";
 	public final static String TYPE_2 = "typeElement2";
 
 	public final static String FILE = "fileElement";
 	public final static String DIFFICULTY = "difficultyElement";
 
 	public final static String PREDICATE = "predicateElement";
 	public final static String DESCRIPTION = "descriptionElement";
 
 	private String name = "";
 	private String endpoint = "";
 	private String prop0 = "";
 	private String prop1 = "";
 	private String prop2 = "";
 	private String prop3 = "";
 	private String image = "";
 	private String type = "";
 
 	private String name_2 = "";
 	private String endpoint_2 = "";
 	private String prop0_2 = "";
 	private String prop1_2 = "";
 	private String prop2_2 = "";
 	private String prop3_2 = "";
 	private String image_2 = "";
 	private String type_2 = "";
 
 	private String filePath;
 	private String difficulty="";
 	private String predicate;
 	private String description;
 	private String linkset;
 
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
 				String fileName = null;
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
 					if (itemName.equals(PROP0))
 						prop0 = fileItem.getString();
 					if (itemName.equals(PROP1))
 						prop1 = fileItem.getString();
 					if (itemName.equals(PROP2))
 						prop2 = fileItem.getString();
 					if (itemName.equals(PROP3))
 						prop3 = fileItem.getString();
 					if (itemName.equals(IMAGE))
 						image = fileItem.getString();
 					if (itemName.equals(TYPE))
 						type = fileItem.getString();
 
 					// second
 					if (itemName.equals(NAME_2))
 						name_2 = fileItem.getString();
 					if (itemName.equals(ENDPOINT_2))
 						endpoint_2 = fileItem.getString();
 					if (itemName.equals(PROP0_2))
 						prop0_2 = fileItem.getString();
 					if (itemName.equals(PROP1_2))
 						prop1_2 = fileItem.getString();
 					if (itemName.equals(PROP2_2))
 						prop2_2 = fileItem.getString();
 					if (itemName.equals(PROP3_2))
 						prop3_2 = fileItem.getString();
 					if (itemName.equals(IMAGE_2))
 						image_2 = fileItem.getString();
 					if (itemName.equals(TYPE_2))
 						type_2 = fileItem.getString();
 
 					if (itemName.equals(DIFFICULTY))
 						difficulty = fileItem.getString();
 					if (itemName.equals(PREDICATE))
 						predicate = fileItem.getString();
 					if (itemName.equals(DESCRIPTION))
 						description = fileItem.getString();
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
 									.print("The file was created successfully. The task has been added. Now you can choose to perform this task.");
 							resp.flushBuffer();
 						} else
 							throw new IOException(
 									"The file already exists in repository.");
 					}
 
 				}
 
 				// Add to template
 				addTemplates();
 
 				// add task to task-file, for later processing
 				// updateDatabase(filePath);
 				addTask(name, name_2, predicate, description, difficulty,
 						fileName, linkset);
 
 				System.out.println("Print received template");
 				System.out.println("First");
 				System.out.println("Name: " + name);
 				System.out.println("EP: " + endpoint);
 				System.out.println("PROP0: " + prop0);
 				System.out.println("PROP1: " + prop1);
 				System.out.println("PROP2: " + prop2);
 				System.out.println("PROP3: " + prop3);
 				System.out.println("Image: " + image);
 				echo("Type: " + type);
 
 				System.out.println("\nSecond");
 				System.out.println("Name: " + name_2);
 				System.out.println("EP: " + endpoint_2);
 				System.out.println("PROP0: " + prop0_2);
 				System.out.println("PROP1: " + prop1_2);
 				System.out.println("PROP2: " + prop2_2);
 				System.out.println("PROP3: " + prop3_2);
 				System.out.println("Image: " + image_2);
 				echo("Type: " + type_2);
 				echo("Predicate: " + predicate);
 				echo("Description: " + description);
 				System.out.println("\nDifficulty: " + difficulty);
 				System.out.println("\nFileName: " + fileName);
 				System.out.println("\nLinkset: " + linkset);
 				flush();
 
 				System.out
 						.println("####Upload Servlet: Receiving formular done");
 
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
 
 	private void updateDatabease(File uploadedFile2) {
 		// TODO Auto-generated method stub
 
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
 
 	private void addTemplates() {
 		System.out.println("ADDD Template---------------------- ");
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
 			temp.setProp0(addBrackets(prop0));
 			temp.setProp1(addBrackets(prop1));
 			temp.setProp2(addBrackets(prop2));
 			temp.setProp3(addBrackets(prop3));
 			temp.setImage(addBrackets(image));
 			temp.setType(type);
 			xml.addOntology(temp);
 			System.out.println("Add 1.Template DONE");
 		}
 		echo("1");
 		if (!endpoint_2.isEmpty()) { // Template object
 			Template temp2 = new Template();
 			temp2.setName(name_2);
 			temp2.setEndpoint(endpoint_2);
 			temp2.setProp0(addBrackets(prop0_2));
 			temp2.setProp1(addBrackets(prop1_2));
 			temp2.setProp2(addBrackets(prop2_2));
 			temp2.setProp3(addBrackets(prop3_2));
 			temp2.setImage(addBrackets(image_2));
 			temp2.setType(type_2);
 			xml.addOntology(temp2);
 			System.out.println("Add 2.Template DONE");
 		}
 		echo("2");
 		// Add linkedOntologies+difficulty into db
 		if (!difficulty.isEmpty())
 			insertIntoDB();
 
 		System.out.println("ADD Template DONE---------------------- ");
 	}
 
 	private void insertIntoDB() {
 		echo("Insert '" + name + "-" + name_2 + "' with difficulty '"
 				+ difficulty + "' into database");
 		// Insert linkedOntologies into table linkedOntologies
 		// String query =
 		// "INSERT IGNORE INTO "+PropertyConstants.DB_TABLE_NAME_DIFFICULTY+
 		// "("+
 		// PropertyConstants.DB_TABLE_DIFFICULTY_LINKEDONTOLOGIES+","+PropertyConstants.DB_TABLE_DIFFICULTY_DIFFICULTY+") "
 		// +
 		// "VALUES ('"+ name+"-"+name_2+"','"+difficulty+"')";
 		// Insert linkedOntologies into table linkedOntologies
 		String query = "INSERT IGNORE INTO "
 				+ PropertyConstants.DB_TABLE_NAME_LINKEDONTOLOGIES + "("
 				+ PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_SUBJECT + ","
 				+ PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_PREDICATE + ","
 				+ PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_OBJECT + ","
 				+ PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_DESCRIPTION + ","
 				+ PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_DIFFICULTY + ","
 				+ PropertyConstants.DB_TABLE_LINKEDONTOLOGIES_READY
 				+ ") VALUES ('" + name + "','" + predicate + "','" + name_2
 				+ "','" + description + "','" + difficulty + "','" + "0" + "')";
 		System.out.println("blaaaaa: " + query);
 		// db connect
 		try {
 			DBTool db = new DBTool(getResourceDir() + "db_settings.ini");
 			Connection con = db.getConnection(); // establish connection
 			db.queryUpdate(query, con);
 
 		} catch (Exception e) {
 			echo("Server: ERROR getting linked Ontologies: " + e.getMessage());
 		}
 		echo("Insert Done!");
 
 	}
 
 	private String addBrackets(String prop) {
 		String parsed = null;
 		if (!prop.isEmpty())
 			parsed = "<" + prop + ">";
 		echo("Add Brackets. Old: " + prop + " New: " + parsed);
 		return parsed;
 	}
 
 	private void flush() {
 		name = "";
 		endpoint = "";
 		prop0 = "";
 		prop1 = "";
 		prop2 = "";
 		prop3 = "";
 		image = "";
 
 		name_2 = "";
 		endpoint_2 = "";
 		prop0_2 = "";
 		prop1_2 = "";
 		prop2_2 = "";
 		prop3_2 = "";
 		image_2 = "";
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

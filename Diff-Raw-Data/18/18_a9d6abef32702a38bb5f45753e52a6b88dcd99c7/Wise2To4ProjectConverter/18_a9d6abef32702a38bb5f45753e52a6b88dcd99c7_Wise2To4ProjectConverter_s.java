 package wise2.converter;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.Node;
 import org.dom4j.io.SAXReader;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import wise2.converter.converters.AlertsConverter;
 import wise2.converter.converters.BookmarksConverter;
 import wise2.converter.converters.ChallengeQuestionConverter;
 import wise2.converter.converters.ConcordModelSaveJarConverter;
 import wise2.converter.converters.Converter;
 import wise2.converter.converters.DiscussionConverter;
 import wise2.converter.converters.DisplayPageConverter;
 import wise2.converter.converters.EvidenceConverter;
 import wise2.converter.converters.JournalConverter;
 import wise2.converter.converters.NotesConverter;
 import wise2.converter.converters.OTrunkConverter;
 import wise2.converter.converters.OutsideUrlConverter;
 import wise2.converter.converters.SelfTestConverter;
 import wise2.converter.converters.SensemakerConverter;
 import wise2.converter.converters.StudentAssessmentConverter;
 import wise2.converter.converters.TableConverter;
 import wise2.converter.converters.Wisedraw2Converter;
 
 public class Wise2To4ProjectConverter {
 	//the wise 2 xml document
 	protected Document document;
 	
 	//the project id
 	private String projectId = "";
 	
 	//the project folder that the wise 4 project files will be created in
 	private File projectFolder;
 
 	//the folder that contains all the upload files for the wise 2 project
 	private File uploadFolder;
 	
 	//will contain the output log when we convert
 	private StringBuffer convertLogStringBuffer = new StringBuffer();
 	
 	//the file we will write the output log to
 	private File convertLogFile = null;
 	
 	//the name of the output log
 	private String convertLogFileName = "convert_log.txt";
 	
 	//a counter for the steps that will be used for the step file names e.g. node_0.ht
 	private int stepCounter = 0;
 	
 	//will contain all the step nodes
 	private JSONArray nodes = new JSONArray();
 	
 	//a counter that keeps count of the number of steps that we failed to convert
 	private int numberOfStepsFailedToConvert = 0;
 	
 	/**
 	 * Converts the wise 2 project zip file into a wise 4 project folder
 	 * @param selectedFile
 	 * @throws DocumentException
 	 * @throws IOException
 	 * @throws NullPointerException
 	 * @throws ZipException
 	 */
 	private Wise2To4ProjectConverter(File selectedFile) throws DocumentException, IOException, NullPointerException, ZipException {
 		//create a handle for the zip file
 		ZipFile projectArchiveFile = new ZipFile(selectedFile);
 		
 		//create the project folder that will contain the wise 4 project files
 		createProjectFolder(selectedFile);
 		
 		//create the upload folder that will contain the upload files from the wise 2 project
 		extractUploadFolder(selectedFile);
 		
 		//obtain the project xml file from the zip file
 		ZipEntry entry = projectArchiveFile.getEntry("wise-project.xml");
 		InputStream is = projectArchiveFile.getInputStream(entry);
 		InputStreamReader xmlInput = new InputStreamReader(is, "UTF-8");
 		SAXReader reader = new SAXReader();
 		document = reader.read(xmlInput);
 		
 		//obtain the root element of the xml file
 		Element rootElement = document.getRootElement();
 		
 		//obtain the project title
 		Node projectTitleNode = rootElement.selectSingleNode("title");
 		String projectTitle = projectTitleNode.getText();
 		
 		//the root project JSON object
 		JSONObject projectJSON = new JSONObject();
 		
 		//the project JSON array that contains all the sequences
 		JSONArray projectSequences = new JSONArray();
 		
 		//the root sequence
 		JSONObject masterSequence = new JSONObject();
 		
 		//put the root sequence into the array of sequences
 		projectSequences.put(masterSequence);
 		
 		/*
 		 * parse all the activities which will also parse and create all the 
 		 * steps within the activities.
 		 */
 		JSONArray projectActivitySequences = parseActivities(rootElement);
 		
 		//an array that will contain all the activity identifiers
 		JSONArray refs = new JSONArray();
 		
 		//loop through each activity
 		for(int x=0; x<projectActivitySequences.length(); x++) {
 			try {
 				//get the sequence object for the activity
 				JSONObject projectSequence = projectActivitySequences.getJSONObject(x);
 				
 				//get the id of the sequence
 				String projectSequenceIdentifier = projectSequence.getString("identifier");
 				
 				//put the id into the array of sequence ids in the root sequence
 				refs.put(projectSequenceIdentifier);
 				
 				//put the sequence object into the array of sequences
 				projectSequences.put(projectSequence);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		try {
 			//set the attributes of the root sequence
 			masterSequence.put("type", "sequence");
 			masterSequence.put("identifier", "master");
 			masterSequence.put("title", "master");
 			masterSequence.put("view", "");
 			masterSequence.put("refs", refs);
 		} catch (JSONException e1) {
 			e1.printStackTrace();
 		}
 		
 		try {
 			//set the attributes of the project
 			projectJSON.put("autoStep", true);
 			projectJSON.put("stepLevelNum", false);
 			projectJSON.put("stepTerm", "Step");
 			projectJSON.put("title", projectTitle);
 			projectJSON.put("constraints", new JSONArray());
 			projectJSON.put("nodes", nodes);
 			projectJSON.put("sequences", projectSequences);
 			projectJSON.put("startPoint", "master");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		//create the project.json file
 		File projectFile = new File(projectFolder, "wise4.project.json");
 		
 		try {
 			//write the .project.json contents to the actual file
 			BufferedWriter out = new BufferedWriter(new FileWriter(projectFile));
 
 			/*
 			 * toString(3) makes the toString() function output 3 spaces as indenting
 			 * to make the .json file easy to read 
 			 */
 			out.write(projectJSON.toString(3));
 			
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			//create and write the project file to disk
 			projectFile.createNewFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		
 		writeToConvertLog("====================\n");
 		
 		//output the confirmation that we are done converting the project
 		writeToConvertLog("Converted project " + projectId);
 		
 		if(numberOfStepsFailedToConvert > 0) {
 			//there were some steps we failed to convert
 			
 			String stepString = "steps";
 			if(numberOfStepsFailedToConvert == 1) {
 				stepString = "step";
 			}
 		
 			//output the number of steps we failed to convert
 			writeToConvertLog("Failed to convert " + numberOfStepsFailedToConvert + " " + stepString);
 		} else {
 			//we converted all the steps
 			writeToConvertLog("Successfully converted all steps");
 		}
 		
 		//create and write the output log
 		createConvertLog();
 	}
 	
 	/**
 	 * Parses all the activities and creates all the steps in the activities
 	 * @param rootElement the root xml element
 	 * @return a JSONArray containing all the activity sequence objects
 	 */
 	private JSONArray parseActivities(Node rootElement) {
 		//the JSON array that will contain all the activity sequence objects
 		JSONArray projectSequences = new JSONArray();
 		
 		//get all the activity nodes in the xml document
 		List<Node> activityNodes = rootElement.selectNodes("activity");
 		
 		//a counter for the activities
 		int sequenceCounter = 0;
 		
 		//loop through all the activity nodes
 		for(Node activityNode:activityNodes) {
 			//get the title of the activity
 			Node activityTitleNode = activityNode.selectSingleNode("title");
 			String activityTitle = activityTitleNode.getText();
 			
 			//output to the log to specify that we have started to convert this sequence
 			writeToConvertLog("[Activity " + (sequenceCounter + 1) + ": " + activityTitle + "]");
 			
 			//parses and creates all the steps in the activity
 			JSONArray projectStepNodesInActivity = parseSteps(activityNode);
 			
 			/*
 			 * an array that contains the refs of the steps within this activity in the
 			 * order that they appear in the project
 			 */
 			JSONArray sequenceRefs = new JSONArray();
 			
 			//loop through all the steps in the activity
 			for(int x=0; x<projectStepNodesInActivity.length(); x++) {
 				try {
 					//get the current step
 					JSONObject projectStepNode = projectStepNodesInActivity.getJSONObject(x);
 					
 					//get the ref of the step
 					String projectStepNodeRef = projectStepNode.getString("ref");
 					
 					//put the ref into the activity's array of refs
 					sequenceRefs.put(projectStepNodeRef);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			//create a new line in the output log
 			writeToConvertLog("");
 			
 			//create the activity sequence JSON object
 			JSONObject projectSequence = new JSONObject();
 			
 			try {
 				//set the attributes of the activity
 				projectSequence.put("type", "sequence");
 				projectSequence.put("identifier", "seq_" + sequenceCounter);
 				projectSequence.put("title", activityTitle);
 				projectSequence.put("view", "");
 				projectSequence.put("refs", sequenceRefs);
 				
 				//set the array of step refs into the activity
 				projectSequences.put(projectSequence);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			
 			//increment the activity counter
 			sequenceCounter++;
 		}
 		
 		//return the JSON array of activity objects 
 		return projectSequences;
 	}
 	
 	/**
 	 * Parse and create the steps in the given activity
 	 * @param activityNode an activity xml node
 	 * @return an array of steps that are in the activity
 	 */
 	private JSONArray parseSteps(Node activityNode) {
 		//the array to accumulate the step nodes
 		JSONArray projectStepNodes = new JSONArray();
 		
 		//get all the step xml nodes
 		List<Node> stepNodes = activityNode.selectNodes("step");
 		
 		//loop through all the step xml nodes
 		for(Node stepNode:stepNodes) {
 			//parse and create a step JSON node
 			JSONObject projectStepNode = parseStep(stepNode);
 			
 			if(projectStepNode != null) {
 				//put the step JSON node into the array
 				projectStepNodes.put(projectStepNode);				
 			}
 		}
 		
 		return projectStepNodes;
 	}
 	
 	/**
 	 * Parse and create the step content file and also the step JSON node that
 	 * will be placed in the .project.json file
 	 * @param stepNode a step xml node
 	 * @return a step JSON node
 	 */
 	private JSONObject parseStep(Node stepNode) {
 		//the step JSON node
 		JSONObject projectStepNode = null;
 		
 		//get the node type from the step xml node
 		Node stepNodeType = stepNode.selectSingleNode("type");
 		
 		//the default value for the step type
 		String stepType = "Unspecified";
 		
 		if(stepNodeType != null) {
 			//get the step type
 			stepType = stepNodeType.getText();
 		}
 		
 		//check if the step type was not specified
 		if(stepType.equals("Unspecified")) {
 			//step type node is not defined so we must figure it out by looking at the authoringURL
 			
 			//get the authoring url and url nodes
 			Node authorURL = stepNode.selectSingleNode("authoringURL");
 			Node url = stepNode.selectSingleNode("url");
 			
 			String authorURLString = "";
 			String urlString = "";
 			
 			if(authorURL != null) {
 				//obtain the authoring url text
 				authorURLString = authorURL.getText();
 			}
 			
 			if(url != null) {
 				//obtain the url text
 				urlString = url.getText();
 			}
 			
 			if(authorURLString.contains("otrunk-wise-step")) {
 				// we're working with a DIY type
 				String diyType = stepNode.selectSingleNode("otherData")
 						.getText();
 				if (diyType.equals("otrunk")) {
 					stepType = "OTrunk";
 				} else if (diyType.equals("model")) {
 					stepType = "OTrunkModel";
 				} else {
 					stepType = "OTrunkDIY";
 				}
 			} else if(authorURLString.contains("Discussion")) {
 				stepType = "DiscussionForum";
 			} else if(authorURLString.contains("allWork")) {
 				stepType = "ShowAllWork";
 			} else if(authorURLString.contains("SelfTest")) {
 				stepType = "SelfTest";
 			} else if(authorURLString.contains("Brainstorm")) {
 				stepType = "Brainstorm";
 			} else if(authorURLString.contains("GraphData")) {
 				stepType = "GraphData";
 			} else if(authorURLString.contains("Journal")) {
 				stepType = "Journal";
 			} else if(authorURLString.contains("psdDemoModeI")) {
 				stepType = "PrincipleMakerStep1";
 			} else if(authorURLString.contains("discussionModeI")) {
 				stepType = "PrincipleMakerStep2";
 			} else if(authorURLString.contains("exPsdDemoMode")) {
 				stepType = "PrincipleMakerStep3";
 			} else if(urlString.contains("SSStudent.php")) {
 				stepType = "Table";
 			}
 		}
 		
 		Node authorURL = stepNode.selectSingleNode("authoringURL");
 		
 		//a self test step will have type OutsideUrl and author url that contains "SelfTest"
 		if(stepType.equals("OutsideUrl") && authorURL.getText().contains("SelfTest")) {
 			stepType = "SelfTest";
 		}
 		
 		Converter converter = null;
 		
 		//obtain the correct converter for the current step type we need to convert
 		if(stepType.equals("DisplayPage")) {
 			converter = new DisplayPageConverter();
 		} else if(stepType.equals("Evidence")) {
 			converter = new EvidenceConverter();
 		} else if(stepType.equals("Notes")) {
 			converter = new NotesConverter();
 		} else if(stepType.equals("StudentAssessment")) {
 			converter = new StudentAssessmentConverter();
 		} else if(stepType.equals("SelfTest")) {
 			converter = new SelfTestConverter();
 		} else if(stepType.equals("Journal")) {
 			converter = new JournalConverter();
 		} else if(stepType.equals("Discussion") || stepType.equals("DiscussionForum")) {
 			converter = new DiscussionConverter();
 		} else if(stepType.equals("Wisedraw2")) {
 			converter = new Wisedraw2Converter();
 		} else if(stepType.equals("ChallengeQuestion")) {
 			converter = new ChallengeQuestionConverter();
 		} else if(stepType.equals("Bookmarks")) {
 			converter = new BookmarksConverter();
 		} else if(stepType.equals("Alerts")) {
 			converter = new AlertsConverter();
 		} else if(stepType.equals("Sensemaker")) {
 			converter = new SensemakerConverter();
 		} else if(stepType.equals("ConcordModelSaveJar")) {
 			converter = new ConcordModelSaveJarConverter();
 			((ConcordModelSaveJarConverter) converter).setProjectId(projectId);
 		} else if(stepType.equals("DataGrid")) {
 			converter = new TableConverter();
 		} else if(stepType.equals("OTrunk") || stepType.equals("Otrunk")) {
 			converter = new OTrunkConverter();
 		} else if(stepType.equals("OutsideUrl")) {
 			converter = new OutsideUrlConverter();
 		} else if(stepType.equals("Table")) {
 			converter = new TableConverter();
 		}
 		
 		if(converter != null) {
			projectStepNode = converter.createStep(stepNode, projectFolder, stepCounter);
			stepCounter++;
 		}
 		
 		if(projectStepNode != null) {
 			//add the step node to the array of nodes that will be in the project file
 			nodes.put(projectStepNode);			
 			
 			try {
 				//get the step id and title
 				String stepId = projectStepNode.getString("identifier");
 				String stepTitle = projectStepNode.getString("title");
 				
 				/*
 				 * output a line to the output log to show that this step was successfully converted
 				 * [x] means it was successfully converter
 				 */
 				writeToConvertLog("[x] " + stepType + " - " + stepId + " - " + stepTitle);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		} else {
 			/*
 			 * we do not have a converter for step type
 			 * [!] means we failed to convert this step
 			 */
 			writeToConvertLog("[!] Could not create " + stepType);
 			
 			//output the xml for the step to the output log so we can easily see what failed to convert
 			writeToConvertLog(stepNode.asXML());
 			
 			//increment the failed number counter
 			numberOfStepsFailedToConvert++;
 		}
 		
 		return projectStepNode;
 	}
 	
 	/**
 	 * Create the project folder that we will put all the wise 4 project files into
 	 * @param selectedFile the wise 2 export zip file
 	 */
 	private void createProjectFolder(File selectedFile) {
 		String filePath = selectedFile.getPath();
 		String parentFolder = selectedFile.getParent();
 		
 		/*
 		 * obtain the name of the zip file, this is assuming the zip file
 		 * was obtained from wise2 and is in a format like this example
 		 * wiseProject-31202-090911_122414-wpe.zip
 		 */
 		
 		//obtain everything starting at the wiseProject-
 		String archiveFileName = filePath.substring(filePath.indexOf("wiseProject-"));
 		
 		//find the first dash
 		int firstDash = archiveFileName.indexOf("-");
 		
 		//find the second dash
 		int secondDash = archiveFileName.indexOf("-", firstDash + 1);
 		
 		/*
 		 * obtain the numbers inbetween the first two dashes which should be
 		 * the project id. from the example above it would be 31202
 		 */
 		projectId = archiveFileName.substring(firstDash + 1, secondDash);
 		
 		/*
 		 * the folder name will be the project id and it will be placed in the
 		 * same folder the wise 2 export zip file is in
 		 */
 		String projectFolderPath = parentFolder + File.separator + projectId;
 
 		//create the folder
 		projectFolder = new File(projectFolderPath);
 		projectFolder.mkdir();
 	}
 	
 	/**
 	 * Extract all the upload folder files 
 	 * @param zipFile the wise 2 export zip file
 	 */
 	private void extractUploadFolder(File zipFile) {
         try {
     		byte[] buf = new byte[1024];
             ZipInputStream zipinputstream = null;
             ZipEntry zipentry;
 			zipinputstream = new ZipInputStream(new FileInputStream(zipFile));
 			
 			//get the wise 4 project folder
 			String projectFolderPath = projectFolder.getAbsolutePath();
 			
 			//create an assets folder in the wise 4 project folder
 			File uploadFolder = new File(projectFolderPath + File.separator + "assets");
 			uploadFolder.mkdir();
 			
 			zipentry = zipinputstream.getNextEntry();
 			
 			//loop through all the files in the wise 2 export zip file
             while (zipentry != null) {
             	//get the name of the file
             	String entryName = zipentry.getName();
             	
             	/*
             	 * get assets folder name e.g.
             	 * if the entryName in the zip file is upload/sunlight.jpg
             	 * the assetsEntryName will be assets/sunlight.jpg
             	 */
             	String assetsEntryName = entryName.replace("upload", "assets");
             	
             	//copy all files except wise-project.xml
             	if(!entryName.equals("wise-project.xml")) {
                     
                     FileOutputStream fileoutputstream;
                     
                     //create a new file handle for the file we are going to copy
                     File newFile = new File(projectFolderPath, assetsEntryName);
                     
                     //write to the output log that we are copying the file from the zip file
                     writeToConvertLog("copying: " + entryName + " to " + newFile.getAbsolutePath());
                     
                     //get the parent directory
                     String directory = newFile.getParent();
                     
                     //not sure what this is for
                     if(directory == null) {
                         if(newFile.isDirectory()) {
                             break;
                         }
                     }
                     
                     //obtain a file output handle that we will use to write the contents to the file
                     fileoutputstream = new FileOutputStream(projectFolderPath + File.separator + assetsEntryName);             
 
                     //write the contents to the file
                     int n;
                     while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                         fileoutputstream.write(buf, 0, n);
                     }
 
                     //close the file handle
                     fileoutputstream.close();
                     
                     //close the zip entry
                     zipinputstream.closeEntry();
             	}
             	
             	//move on to the next file in the zip file
             	zipentry = zipinputstream.getNextEntry(); 
             }
             
             //close the zip file handle
             zipinputstream.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		//create a new line in the console output
 		writeToConvertLog("");
 	}
 	
 	/**
 	 * Output convert log information to System.out and also to the
 	 * convert log string buffer that we will later write to a text
 	 * file.
 	 * @param string
 	 */
 	private void writeToConvertLog(String string) {
 		//output the string to the console
 		System.out.println(string);
 		
 		//output the string to the output log
 		convertLogStringBuffer.append(string + "\n");
 	}
 	
 	/**
 	 * Create the convert log text file and write the convert
 	 * log text into the file.
 	 */
 	private void createConvertLog() {
 		if(convertLogFile == null) {
 			//create the file if it does not already exist
 			convertLogFile = new File(projectFolder, convertLogFileName);
 		}
 		
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(convertLogFile));
 			
 			//write the convert log text that we have accumulated to the convert log
 			out.write(convertLogStringBuffer.toString());
 			
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Get the wise 4 project folder that we are creating the wise 4
 	 * projects file in.
 	 * @return the projectFolder
 	 */
 	public File getProjectFolder() {
 		return projectFolder;
 	}
 	
 	/**
 	 * Asks user for the exported Wise 2.0 project file i.e.
 	 * wiseProject-31015-080410_111406-wpe.zip
 	 * and converts the old Wise 2.0 project into a Wise 4.0 project
 	 * 
 	 * @param args args[0] is an optional argument that is the path to a wise 2 zip file
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		File selectedFile = null;
 		
 		//panel used to display the file chooser dialog
 		JPanel panel = new JPanel();
 		
 		if(args.length > 0) {
 			//an argument wise 2 zip file path has been passed in as an argument 
 			selectedFile = new File(args[0]);	
 		} else {
 			/*
 			 * no argument has been provided so we will display the file chooser
 			 * to let the user pick a wise 2 zip file from their hard drive
 			 */
 			JFileChooser fileChooser = new JFileChooser();
 			
 			//display the file chooser
 			fileChooser.showOpenDialog(panel);
 			
 			//get the file the user selected
 			selectedFile = fileChooser.getSelectedFile();			
 		}
 		
 		if(selectedFile != null) {
 			try{
 				//convert the wise 2 project into a wise 4 project
 				Wise2To4ProjectConverter projectConverter = new Wise2To4ProjectConverter(selectedFile);
 				
 				//project was converted successfully
 				JOptionPane.showMessageDialog(panel, "Project successfully converted to\n" + projectConverter.getProjectFolder().getAbsolutePath());	
 			} catch (ZipException e) {
 				//project was not converted, we were unable to open the zip file or the file chosen was not a zip file
 				JOptionPane.showMessageDialog(panel, "Unable to open selected file\n" + selectedFile.getAbsolutePath(), "Error converting project", JOptionPane.ERROR_MESSAGE);
 			}
 		} else {
 			//project was not converted, user did not select a file
 			JOptionPane.showMessageDialog(panel, "You must select a file to convert", "Error converting project", JOptionPane.ERROR_MESSAGE);
 		}
 
 		System.exit(0);
 	}
 }

 package models.question;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import javax.xml.XMLConstants;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 
 import models.data.DataDaemon;
 import models.data.Language;
 
 import org.codehaus.jackson.JsonNode;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import play.Play;
 import play.libs.Json;
 /**
  * A class to perform some IO with the questions within the question editor
  * @author Ruben Taelman
  *
  */
 
 public class QuestionIO {
 
     private static final String XML_NAMESPACE = "bebras:Question";
     private static final String XML_SCHEMA = "conf/questions.xsd";
     private static final Map<String, QuestionFactory<?>> QUESTION_TYPE_NAMES = new HashMap<String, QuestionFactory<?>>();
     static {
         QUESTION_TYPE_NAMES.put("multiple-choice-question", new MultipleChoiceQuestionFactory());
         QUESTION_TYPE_NAMES.put("regex-question", new RegexQuestionFactory());
     }
 
     /**
      * Validate a json formatted question
      * @param json json formatted question
      * @throws QuestionBuilderException any error that can occur
      * @return the question in the encoded json
      */
     public static Question validateJson(String json) throws QuestionBuilderException {
         JsonNode input = Json.parse(json);
         QuestionPack pack = jsonToQuestionPack(input);
         return getFromXml(pack.getXmlDocument());// The return is not catched because we only have to validate
     }
 
     /**
      * Hash a string and append a user id
      * @param toHash the string that has to be hashed
      * @param userID the user id to append
      * @return a hash
      * @throws NoSuchAlgorithmException
      */
     private static String makeHash(String toHash, String userID) throws NoSuchAlgorithmException {
         MessageDigest mdEnc = MessageDigest.getInstance("MD5");
         mdEnc.update(toHash.getBytes(), 0, toHash.length());
         String hash = new BigInteger(1, mdEnc.digest()).toString(16) + userID;
         return hash;
     }
 
     private static QuestionPack generateQuestionPack(String json, String userID, String userDownloadLocation) throws QuestionBuilderException {
         try {
             String hash = makeHash(json, userID);
 
             JsonNode input = Json.parse(json);
             String downloadLocation = Play.application().configuration().getString("questioneditor.download");
             File dir = new File(downloadLocation);
             if(!dir.exists()) dir.mkdir();
             QuestionPack pack = jsonToQuestionPack(input, downloadLocation, hash, userDownloadLocation);
             getFromXml(pack.getXmlDocument());// The return is not catched because we only have to validate
 
             return pack;
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException("Internal server error.", e);
         }
     }
 
     /**
      * Export the question encoded with json to a File
      * @param json json formatted question
      * @param userID the user id for the authenticated user
      * @param userDownloadLocation the location where the user can http-request his uploaded files
      * @return The compressed question file
      * @throws QuestionBuilderException any error that can occur
      */
     public static File export(String json, String userID, String userDownloadLocation) throws QuestionBuilderException {
         return generateQuestionPack(json, userID, userDownloadLocation).export(userID);
     }
 
     /**
      * Submit the question encoded with json to await approval
      * @param json json formatted question
      * @param userID the user id for the authenticated user
      * @param userDownloadLocation the location where the user can http-request his uploaded files
      * @throws QuestionBuilderException any error that can occur
      */
     public static void submit(String json, String userID, String userDownloadLocation) throws QuestionBuilderException {
         generateQuestionPack(json, userID, userDownloadLocation).submit(userID);
     }
 
     /**
      * Make a question from an uploaded zip file
      * @param zis   inputstream from the uploaded zip file
      * @param userID    id of the user
      * @param userDownloadLocation  the location where the user can http-request his uploaded files
      * @return a question based on the contents of the zip file
      * @throws QuestionBuilderException any error that can occur with questions
      * @throws IOException any IO error that can occur with the zipinputstream
      */
     public static Question importUpload(ZipInputStream zis, String userID, String userDownloadLocation) throws QuestionBuilderException, IOException {
         // Some strings we need
         String tempUploadLocation = Play.application().configuration().getString("questioneditor.tempUpload");
         String hash;
         try {
             hash = makeHash(zis.toString(), userID);
         } catch (NoSuchAlgorithmException e) {
             throw new QuestionBuilderException(e.getMessage());
         }
         String fileName = QuestionPack.QUESTIONXMLFILE+"~"+hash;
         String uploadLocation = QuestionIO.getUserUploadLocation(userID);
 
         // Loop over the entries
         ZipEntry entry = zis.getNextEntry();
         while(entry != null) {
             if(entry.getName().equals(QuestionPack.QUESTIONXMLFILE)) {
                 // Save the xml file to a temp folder to interpret later
                 copyStream(zis, new FileOutputStream(addTempFile(tempUploadLocation, fileName)));
             } else {
                 // Save all other files in the user's resource folder
                 File resource = addTempFile(uploadLocation, entry.getName());
                 copyStream(zis, new FileOutputStream(resource));
             }
 
             // Close everything to avoid leaks
             zis.closeEntry();
             entry = zis.getNextEntry();
         }
 
         // Make the question based on the temp xml file
         Question question = QuestionIO.getFromXml(tempUploadLocation+"/"+fileName);
 
         // Loop over the languages and add their correct index and feedback contents
         // These files were saved in the resources folder of the user so they need to be read
         // and deleted from there.
         for(Language language : question.getLanguages()) {
             question.setIndex(getResourceContentsAndRemove(userDownloadLocation
                     , uploadLocation, question.getIndex(language)), language);
             question.setFeedback(getResourceContentsAndRemove(userDownloadLocation
                     , uploadLocation, question.getFeedback(language)), language);
         }
 
         return question;
     }
 
     /**
      * Read contents from a fileName in an uploadLocation, delete the file and return
      * the content. All src attributes will be altered to include the userDownloadLocation.
      * @param userDownloadLocation  the location where the user can http-request his uploaded files
      * @param uploadLocation    the location of all the resource files of this user
      * @param fileName  the name of the file that needs to be read and deleted
      * @return  the contents of that file
      * @throws FileNotFoundException    if the file was not found
      * @throws IOException  if there was a read error
      */
     private static String getResourceContentsAndRemove(String userDownloadLocation
             , String uploadLocation, String fileName) throws FileNotFoundException, IOException {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
 
         // Open the file and copy it
         File file = new File(uploadLocation, fileName);
         copyStream(new FileInputStream(file), bos);
 
         // Replace the src attributes
         String content = bos.toString();
         content = content.replaceAll("src=\"", "src=\""+userDownloadLocation+"/");
 
         // Delete the file
         file.delete();
         return content;
     }
 
     /**
      * Copy from an inputstream to a certain outputstream
      * @param in an inputstream
      * @param out outputstream to where the content should be written to
      * @throws IOException when an error occurs with the I/O-streams
      */
     public static void copyStream(InputStream in, OutputStream out) throws IOException {
         byte[] buffer = new byte[1024 * 4];
         int n = 0;
         while (-1 != (n = in.read(buffer))) {
             out.write(buffer, 0, n);
         }
         out.close();
     }
     
     /**
      * Make a document from an url
      * @param xml absolute URL of an xml file
      * @return a document for the xml url
      * @throws QuestionBuilderException possible things that can go wrong
      */
     public static Document makeDocument(String xml) throws QuestionBuilderException {
         try {
             // Parse the given XML into a DOM tree
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             factory.setNamespaceAware(true);
     
             // Parse our file
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document doc = builder.parse(xml);
             return doc;
         } catch (ParserConfigurationException e) {
             throw new QuestionBuilderException("Incorrect XML, can't be parsed.");
         } catch (SAXException e) {
             throw new QuestionBuilderException("The XML is invalid."+e.getMessage());
         } catch (IOException e) {
            throw new QuestionBuilderException("Can't read the xml file.");
         }
     }
 
     /**
      * Creates a new question from a certain XML input
      * @param xml  absolute URL of an xml file
      * @return a new question
      * @throws QuestionBuilderException possible things that can go wrong
      */
     public static Question getFromXml(String xml) throws QuestionBuilderException {
         return getFromXml(makeDocument(xml));
     }
 
     /**
      * Creates a new question from a certain XML input
      * @param doc  document of an xml file
      * @return a new question
      * @throws QuestionBuilderException possible things that can go wrong
      */
     public static Question getFromXml(Document doc) throws QuestionBuilderException {
         Question question = null;
         try {
             // create a SchemaFactory capable of understanding our schemas
             SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
             // Load our schema
             Schema schema = sf.newSchema(new File(XML_SCHEMA));
             //factory.setSchema(schema); </-- DO NOT USE THIS, IT WILL ADD OPTIONAL ATTRS EVERYWHERE!
 
             // create a Validator instance to validate the give XML
             Validator validator = schema.newValidator();
 
             // Validate our document
             validator.validate(new DOMSource(doc));
 
             // Retrieve the root nodeList
             NodeList nodeList = doc.getChildNodes();
             nodeList = nodeList.item(0).getChildNodes();
             Node typeNode = nodeList.item(1);
             if(typeNode==null)
                 typeNode = nodeList.item(0);
             String type = typeNode.getNodeName();
 
             // Give the nodeList to the correct QuestionFactory to make our Question
             question = QUESTION_TYPE_NAMES.get(type).newQuestion(typeNode);
         } catch (SAXException e) {
             throw new QuestionBuilderException("The XML is invalid."+e.getMessage());
         } catch (IOException e) {
            throw new QuestionBuilderException("Can't read the XML file.");
         } catch (NullPointerException e) {
             throw new QuestionBuilderException("Unknown question type.");
         }
 
         return question;
     }
 
     /**
      * Convert a json node to a questionPack
      * @param json a root node from a json encoded question
      * @return  a new questionpack with the json contents
      * @throws QuestionBuilderException if the question was somehow invalid
      */
     public static QuestionPack jsonToQuestionPack(JsonNode json) throws QuestionBuilderException {
         return jsonToQuestionPack(json, null, null, null);
     }
 
     /**
      * Convert a json formatted question to an xml document
      * @param json json formatted question
      * @return a question in xml format
      * @throws QuestionBuilderException possible things that can go wrong
      */
     public static QuestionPack jsonToQuestionPack(JsonNode json, String location, String hash, String userDownloadLocation) throws QuestionBuilderException {
         try {
             // Make the required factories
             DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
             // Make an empty document
             Document doc = docBuilder.newDocument();
 
             // Make an empty questionPack with that document
             QuestionPack pack = new QuestionPack(doc);
 
             // Add attributes if they are present
             if(location != null)
                 pack.setTempDownloadLocation(location);
             if(hash != null)
                 pack.setHash(hash);
             if(userDownloadLocation != null)
                 pack.setUserDownloadLocation(userDownloadLocation);
 
             // Set the correct namespace
             Element root = doc.createElementNS(XML_NAMESPACE, "root");
 
             // Make the correct starting node based on the type of question
             QuestionType type = QuestionType.valueOf(json.get("type").asText());
             if(type == null)
                 throw new QuestionBuilderException("Invalid question type.");
             Element questionNode = doc.createElementNS(XML_NAMESPACE, type.getXmlElement());
 
             // Loop over the languages
             JsonNode languages = json.get("languages");
             for(int i=0;i<languages.size();i++) {
                 JsonNode language = languages.get(i);
 
                 // Make language node with the correct code
                 Element lang = doc.createElementNS(XML_NAMESPACE, "language");
                 String langCode = language.get("language").getTextValue();
                 lang.setAttribute("code", langCode);
 
                 // Add index node
                 Element index = doc.createElementNS(XML_NAMESPACE, "index");
                 if(location != null && hash != null)
                     index.setTextContent(pack.addIndex(langCode, language.get("index").getTextValue()));
                 else
                     index.setTextContent("validating");
                 lang.appendChild(index);
 
                 // Add feedback node
                 Element feedback = doc.createElementNS(XML_NAMESPACE, "feedback");
                 if(location != null && hash != null)
                     feedback.setTextContent(pack.addFeedback(langCode, language.get("feedback").getTextValue()));
                 else
                     feedback.setTextContent("validating");
                 lang.appendChild(feedback);
 
                 // Add title node
                 Element title = doc.createElementNS(XML_NAMESPACE, "title");
                 title.setTextContent(language.get("title").getTextValue());
                 lang.appendChild(title);
 
                 if(QuestionType.MULTIPLE_CHOICE.equals(type)) {
                     Element answers = doc.createElementNS(XML_NAMESPACE, "answers");
 
                     // Add answers node
                     JsonNode answerNodes = language.get("answers");
                     for(int j=0;j<answerNodes.size();j++) {
                         JsonNode answerNode = answerNodes.get(j);
 
                         // Add answers and mark as correct if needed
                         Element answer = doc.createElementNS(XML_NAMESPACE, "answer");
                         answer.setTextContent(answerNode.get("content").getTextValue());
                         if(answerNode.get("correct").asBoolean())
                             answer.setAttribute("correct", "true");
                         answers.appendChild(answer);
                     }
 
                     lang.appendChild(answers);
                 } else if(QuestionType.REGEX.equals(type)) {
                     // Add regex node
                     Element input = doc.createElementNS(XML_NAMESPACE, "input");
                     input.setAttribute("regex", language.get("regex").getTextValue());
                     lang.appendChild(input);
                 }
 
                 questionNode.appendChild(lang);
             }
 
             root.appendChild(questionNode);
             doc.appendChild(root);
 
             return pack;
 
         } catch (ParserConfigurationException e) {
             throw new QuestionBuilderException("An unexpected internal error occured while parsing.");
         } catch (IOException e) {
             throw new QuestionBuilderException("An unexpected internal error occured while saving.");
         }
     }
 
     /**
      * Get the location of a certain user folder. Folders will be created if they do not exist
      * @param locationToken play config token of the string
      * @param userID id of the user
      * @return location
      */
     private static String getUserFolderLocation(String locationToken, String userID) {
         String rootLocation = Play.application().configuration().getString(locationToken);
         File rootDirectory = new File(rootLocation);
         if(!rootDirectory.exists())
             rootDirectory.mkdir();
 
         String location = Play.application().configuration().getString(locationToken)+"/"+userID;
         File directory = new File(location);
         if(!directory.exists())
             directory.mkdir();
         return location;
     }
 
     /**
      * Return the location where this user can upload to
      * @param userID the id of the user
      * @return the location
      */
     public static String getUserUploadLocation(String userID) {
         return getUserFolderLocation("questioneditor.upload", userID);
     }
 
     /**
      * Return the location where this user can submit to
      * @param userID the id of the user
      * @return the location
      */
     public static String getUserSubmitLocation(String userID) {
         return getUserFolderLocation("questioneditor.submit", userID);
     }
 
     /**
      * Add a temporary file that will be automatically removed after 24 hours.
      * The file will also be removed if the server reboots within those 24 hours
      * @param location the location of the file
      * @param name the name of the file
      * @return the newly created file
      */
     public static File addTempFile(String location, String name) {
         // Make a new file on the given location with the given name
         final File file = new File(location, name);
 
         // Remove the file after 24 hours
         final Calendar calendar = Calendar.getInstance();
         calendar.add(Calendar.DATE, 1);
         DataDaemon.getInstance().runAt(new Runnable() {
 
             @Override
             public void run() {
                 file.delete();
             }
 
         }, calendar);
 
         // Remove the file if the server shuts down (this is needed for when the server
         // reboots within those 24 hours, otherwise we'll have file leaks)
         file.deleteOnExit();
 
         return file;
     }
 }

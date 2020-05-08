 package example.deploy.text;
 
 import static com.polopoly.cm.server.ServerNames.CONTENT_ATTRG_SYSTEM;
 import static com.polopoly.cm.server.ServerNames.CONTENT_ATTR_NAME;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.polopoly.cm.server.ServerNames;
 
 import example.deploy.hotdeploy.client.Major;
 
 public class TextContentParser {
     private static final Logger LOGGER = Logger
             .getLogger(TextContentParser.class.getName());
 
     public static final char SEPARATOR_CHAR = ':';
 
     public static final String TEXT_CONTENT_FILE_EXTENSION = "content";
 
     public static final String ID_PREFIX = "id";
 
     public static final String INPUT_TEMPLATE_PREFIX = "inputtemplate";
 
     public static final String NAME_PREFIX = "name";
 
     public static final String SECURITY_PARENT_PREFIX = "securityparent";
 
     public static final String COMPONENT_PREFIX = "component";
 
     public static final String REFERENCE_PREFIX = "ref";
 
     public static final String LIST_PREFIX = "list";
 
     public static final String TEMPLATE_PREFIX = "template";
 
     public static final String PUBLISH_PREFIX = "publish";
 
     public static final String MAJOR_PREFIX = "major";
 
     public static final String FILE_PREFIX = "file";
 
     private BufferedReader reader;
 
     private TextContentSet parsed = new TextContentSet();
 
     private TextContent currentContent;
 
     private String line;
 
     private int atLine;
 
     private URL contentUrl;
 
     private String fileName;
 
     public TextContentParser(InputStream inputStream, URL contentUrl,
             String fileName) throws IOException {
         reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
         this.contentUrl = contentUrl;
 
         int i = fileName.lastIndexOf('/');
 
         if (i != -1) {
             fileName = fileName.substring(i + 1);
         }
 
         if (fileName.endsWith('.' + TEXT_CONTENT_FILE_EXTENSION)) {
             fileName = fileName.substring(0, fileName.length()
                     - TEXT_CONTENT_FILE_EXTENSION.length() - 1);
         } else {
             LOGGER.log(Level.WARNING, "Expected file name " + fileName
                     + " to end with ." + TEXT_CONTENT_FILE_EXTENSION + ".");
         }
 
         this.fileName = fileName;
     }
 
     public TextContentSet parse() throws IOException, ParseException {
         while ((line = reader.readLine()) != null) {
             atLine++;
             parseLine(line);
         }
 
         return parsed;
     }
 
     private void parseLine(String line) throws ParseException {
         if (line.startsWith("#")) {
             return;
         }
 
         String[] fields = split(line);
 
         if (fields.length < 2) {
             if (!line.trim().equals("")) {
                 fail("Unrecognized line.");
             }
 
             return;
         }
 
         String prefix = fields[0];
 
         if (prefix.equals(ID_PREFIX)) {
             assertFields(2, fields);
             currentContent = new TextContent();
 
             currentContent.setId(expandId(fields[1]));
             parsed.add(currentContent);
 
             return;
         }
 
         if (currentContent == null) {
             fail("Add an \"" + ID_PREFIX + ":\" line first.");
             return;
         }
 
         if (prefix.equals(INPUT_TEMPLATE_PREFIX)) {
             assertFields(2, fields);
 
             currentContent.setInputTemplate(new ExternalIdReference(fields[1]));
         } else if (prefix.equals(NAME_PREFIX)) {
             assertFields(2, fields);
             // TODO: replace with constants.
             currentContent.setComponent(CONTENT_ATTRG_SYSTEM,
                     CONTENT_ATTR_NAME, fields[1]);
         } else if (prefix.equals(SECURITY_PARENT_PREFIX)) {
             assertFields(2, fields);
             currentContent.setSecurityParent(new ExternalIdReference(
                     expandId(fields[1])));
         } else if (prefix.equals(COMPONENT_PREFIX)) {
             assertFields(4, fields);
 
             currentContent.setComponent(fields[1], fields[2], fields[3]);
         } else if (prefix.equals(REFERENCE_PREFIX)) {
             assertFields(4, fields);
 
             currentContent.setReference(fields[1], fields[2],
                     new ExternalIdReference(expandId(fields[3])));
         } else if (prefix.equals(FILE_PREFIX)) {
             assertFields(3, fields);
 
             try {
                 URL fileUrl = new URL(contentUrl, fields[2]);
 
                 currentContent.addFile(fields[1], fileUrl.openStream());
             } catch (MalformedURLException e) {
                 fail("Could not read file " + fields[2] + " relative to "
                         + contentUrl + ".");
             } catch (IOException e) {
                 fail("Could not read file " + fields[2] + " relative to "
                         + contentUrl + ".");
             }
         } else if (prefix.equals(LIST_PREFIX)) {
             String group = null;
             String referredId = null;
             String metadata = null;
 
             if (fields.length == 2) {
                 group = ServerNames.DEPARTMENT_ATTRG_SYSTEM;
                 referredId = fields[1];
             } else if (fields.length == 3) {
                 group = fields[1];
                 referredId = fields[2];
             } else if (fields.length == 4) {
            	group = fields[1];
                 metadata = fields[3];
                 referredId = fields[2];
             } else {
                 fail("Expected one, two or three parameters for operation "
                         + fields[0]
                         + " (rather than the provided "
                         + (fields.length - 1)
                         + "). "
                         + "The parameters are: group (optionalunless reference metadata is provided), "
                         + "referred object, reference metadata (optional).");
             }
 
             currentContent.getList(group).add(
                     new ExternalIdReference(expandId(referredId),
                             expandId(metadata)));
         } else if (prefix.equals(TEMPLATE_PREFIX)) {
             assertFields(2, fields);
 
             currentContent.setTemplateId(fields[1]);
         } else if (prefix.equals(MAJOR_PREFIX)) {
             assertFields(2, fields);
 
             String majorString = fields[1].trim();
 
             try {
                 int intMajor = Integer.parseInt(majorString);
 
                 currentContent.setMajor(Major.getMajor(intMajor));
             } catch (NumberFormatException e) {
                 Major major = Major.getMajor(majorString);
 
                 if (major == Major.UNKNOWN) {
                     fail("Unknown major \"" + majorString + "\".");
                 }
 
                 currentContent.setMajor(major);
             }
         } else if (prefix.equals(PUBLISH_PREFIX)) {
             String group = null;
             String publishIn = null;
             String metadata = null;
 
             if (fields.length == 2) {
                 group = ServerNames.DEPARTMENT_ATTRG_SYSTEM;
                 publishIn = fields[1];
             } else if (fields.length == 3) {
                 group = fields[1];
                 publishIn = fields[2];
             } else if (fields.length == 4) {
                 group = fields[1];
                 publishIn = fields[2];
                 metadata = fields[3];
             } else {
                 fail("Expected one, two or three parameters for operation "
                         + fields[0]
                         + " (rather than the provided "
                         + (fields.length - 1)
                         + "). The parameters are: "
                         + "group (optional unless reference metadata is provided), object to publish in, "
                         + "reference metadata (optional).");
             }
 
             Publishing publishing = new Publishing(new ExternalIdReference(
                     expandId(publishIn), expandId(metadata)), group);
 
             currentContent.addPublishing(publishing);
         } else {
             fail("Line should start with " + ID_PREFIX + ", "
                     + INPUT_TEMPLATE_PREFIX + ", " + NAME_PREFIX + ", "
                     + SECURITY_PARENT_PREFIX + ", " + COMPONENT_PREFIX + ", "
                     + MAJOR_PREFIX + ", " + REFERENCE_PREFIX + " or "
                     + LIST_PREFIX + ".");
         }
     }
 
     private String expandId(String externalId) {
         // reference metadata may be null.
         if (externalId == null) {
             return null;
         }
 
         if (externalId.startsWith(".")) {
             return fileName + externalId;
         } else {
             return externalId;
         }
     }
 
     private String[] split(String line) {
         List<String> result = new ArrayList<String>();
 
         boolean quote = false;
 
         StringBuffer current = new StringBuffer(100);
 
         for (int i = 0; i < line.length(); i++) {
             char ch = line.charAt(i);
 
             if (ch == SEPARATOR_CHAR && !quote) {
                 result.add(current.toString());
                 current.setLength(0);
             } else if (ch == '\\' && !quote) {
                 quote = true;
             } else if (quote && ch == 'n') {
                 current.append('\n');
             } else {
                 current.append(ch);
                 quote = false;
             }
         }
 
         result.add(current.toString());
 
         return result.toArray(new String[result.size()]);
     }
 
     private void fail(String message) throws ParseException {
         throw new ParseException(message, line, atLine);
     }
 
     private void assertFields(int expectedFields, String[] fields)
             throws ParseException {
         if (fields.length != expectedFields) {
             fail("Expected " + (expectedFields - 1)
                     + " parameters for operation " + fields[0]
                     + " (rather than the provided " + (fields.length - 1)
                     + ").");
         }
 
     }
 
     public String getFileName() {
         return fileName;
     }
 
 }

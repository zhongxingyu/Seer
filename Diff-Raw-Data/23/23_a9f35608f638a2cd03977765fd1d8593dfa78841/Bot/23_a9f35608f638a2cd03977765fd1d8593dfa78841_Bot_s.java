 package aiml.bot;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import aiml.context.ContextInfo;
 import aiml.context.EnvironmentInputContext;
 import aiml.context.StringContext;
 import aiml.parser.AIMLParser;
 import aiml.parser.AimlParserException;
 
 public class Bot {
   private String name;
   private HashMap<String, String> properties = new HashMap<String, String>();
   private boolean standalone = false;
   private boolean enabled = false;
 
   private Logger logger = Logger.getLogger(this.getClass().getName());
 
   private XmlPullParser parser;
 
   public Bot() throws XmlPullParserException {
     super();
     parser = XmlPullParserFactory.newInstance().newPullParser();
   }
 
   public Bot(String name) throws XmlPullParserException {
     this();
     this.name = name;
   }
 
   public void setProperty(String name, String value) {
     properties.put(name, value);
   }
 
   public String getProperty(String name) throws InvalidPropertyException {
     if (properties.containsKey(name))
       return properties.get(name);
     else
       throw new InvalidPropertyException("Bot property '" + name +
           "' must be defined for bot '" + this.name + "'");
   }
 
   public boolean hasProperty(String name) {
     return properties.containsKey(name);
   }
 
   public String getName() {
     return name;
   }
 
   public void load(String file) throws XmlPullParserException, IOException,
       BotSyntaxException, AimlParserException {
     parser.setInput(new FileInputStream(file), "UTF-8");
     try {
       parser.setProperty("http://xmlpull.org/v1/doc/properties.html#location",
           file);
     } catch (XmlPullParserException e) {
     }
     require(XmlPullParser.START_DOCUMENT, null);
     parser.next();
     standalone = true;
     doBot();
     try {
       require(XmlPullParser.END_DOCUMENT, null);
     } catch (BotSyntaxException e) {
       throw new BotSyntaxException(
           "Syntax error: no markup allowed after end of root element " +
               parser.getPositionDescription());
     }
   }
 
   public void load(XmlPullParser parser) throws BotSyntaxException,
       XmlPullParserException, IOException, AimlParserException {
     XmlPullParser oldParser = this.parser;
     this.parser = parser;
     standalone = false;
     doBot();
     this.parser = oldParser;
   }
 
   private boolean isEvent(int eventType, String name)
       throws XmlPullParserException {
     return (parser.getEventType() == eventType && (name == null || parser.getName().equals(
         name)));
   }
 
   private void require(int eventType, String name, String failMessage)
       throws XmlPullParserException, BotSyntaxException {
     if ((parser.getEventType() == eventType && (name == null || parser.getName().equals(
         name))))
       return;
     else
       throw new BotSyntaxException("Syntax error: " + failMessage + " " +
           parser.getPositionDescription());
   }
 
   private void require(int eventType, String name) throws BotSyntaxException,
       XmlPullParserException {
     if (parser == null)
       throw new IllegalArgumentException();
     if ((parser.getEventType() == eventType && (name == null || parser.getName().equals(
         name))))
       return;
     else
       throw new BotSyntaxException("Syntax error: expected " +
           XmlPullParser.TYPES[eventType] + " '" + name + "' " +
           parser.getPositionDescription());
   }
 
   private String requireAttrib(String name) throws BotSyntaxException {
     if (parser.getAttributeValue(null, name) == null)
       throw new BotSyntaxException("Syntax error: mandatory attribute '" +
           name + "' missing from element '" + parser.getName() + "' " +
           parser.getPositionDescription());
     else
       return parser.getAttributeValue(null, name);
   }
 
   private void doBot() throws BotSyntaxException, XmlPullParserException,
       IOException, AimlParserException {
     require(XmlPullParser.START_TAG, "bot");
     String id = requireAttrib("id");
 
     if (!standalone && name != null && !id.equals(name)) {
       throw new BotSyntaxException("Syntax error: bot ID mismatch " +
           parser.getPositionDescription());
     } else
       name = id;
 
     String enabledAttr = requireAttrib("enabled");
     if (!standalone && enabledAttr.equals("false")) {
       enabled = false;
     } else {
       enabled = true;
     }
 
     parser.nextTag();
     if (isEvent(XmlPullParser.START_TAG, "properties")) {
       doProperties();
     }
     if (isEvent(XmlPullParser.START_TAG, "predicates")) {
       doPredicates();
     }
     if (isEvent(XmlPullParser.START_TAG, "substitutions")) {
       doSubstitutions();
     }
     if (isEvent(XmlPullParser.START_TAG, "sentence-splitters")) {
       doSentenceSplitters();
     }
     if (isEvent(XmlPullParser.START_TAG, "listeners")) {
       doListeners();
     }
     if (isEvent(XmlPullParser.START_TAG, "contexts")) {
       doContexts();
     } else {
       // TODO provide actual implementations of topics...
       ContextInfo.registerContext(new EnvironmentInputContext("input"));
       ContextInfo.registerContext(new StringContext("that", "dummy that"));
       ContextInfo.registerContext(new StringContext("topic", "dummy topic"));
     }
     doLearn();
 
     require(XmlPullParser.END_TAG, "bot");
     parser.next();
   }
 
   private void doLearn() throws XmlPullParserException, IOException,
       BotSyntaxException, AimlParserException {
     while (isEvent(XmlPullParser.START_TAG, "learn")) {
       File file = new File(parser.nextText());
       require(XmlPullParser.END_TAG, "learn");
       if (!file.exists() || !file.isFile()) {
         logger.warning("file " + file + " does not exist");
       } else {
         logger.info("Loading file " + file);
         new AIMLParser(this).load(file.getPath(), "UTF-8");
       }
       parser.nextTag();
     }
   }
 
   private void doContexts() {
     // TODO Auto-generated method stub
     throw new UnsupportedOperationException("doContexts()");
 
   }
 
   private void doListeners() {
     // TODO Auto-generated method stub
     throw new UnsupportedOperationException("doListeners()");
 
   }
 
   private void doSentenceSplitters() {
     // TODO Auto-generated method stub
     throw new UnsupportedOperationException("doSentenceSplitters()");
 
   }
 
   private void doSubstitutions() {
     // TODO Auto-generated method stub
     throw new UnsupportedOperationException("doSubstitutions()");
 
   }
 
   private void doPredicates() {
     // TODO Auto-generated method stub
     throw new UnsupportedOperationException("doPredicates()");
 
   }
 
   private void doProperties() throws BotSyntaxException,
       XmlPullParserException, IOException {
     require(XmlPullParser.START_TAG, "properties");
     parser.nextTag();
     while (isEvent(XmlPullParser.START_TAG, "property")) {
       doProperty();
     }
     require(XmlPullParser.END_TAG, "properties");
     parser.nextTag();
   }
 
   private void doProperty() throws BotSyntaxException, XmlPullParserException,
       IOException {
     require(XmlPullParser.START_TAG, "property");
     if (!parser.isEmptyElementTag()) {
       throw new BotSyntaxException(
           "Syntax error: bot property definition must be empty " +
               parser.getPositionDescription());
     }
     String propName = requireAttrib("name");
     String propValue = requireAttrib("value");
     setProperty(propName, propValue);
     parser.nextTag();
     parser.nextTag();
 
   }
 
 }

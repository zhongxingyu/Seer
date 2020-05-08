 /*
     jaiml - java AIML library
     Copyright (C) 2004, 2009  Kim Sullivan
 
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     (at your option) any later version.
 
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package aiml.parser;
 
 /**
  * <p>Title: AIML Pull Parser</p>
  * @author Kim Sullivan
  * @version 1.0
  */
 
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.logging.Logger;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import aiml.bot.Bot;
 import aiml.bot.InvalidPropertyException;
 import aiml.classifier.DuplicatePathException;
 import aiml.classifier.MultipleContextsException;
 import aiml.classifier.Path;
 import aiml.script.Block;
 import aiml.script.Formatter;
 import aiml.script.Script;
 
 public class AIMLParser {
 
   Logger log = Logger.getLogger(AIMLParser.class.getName());
   CheckingParser parser;
   Bot bot;
   Path currentPath;
 
   public AIMLParser(Bot bot) throws XmlPullParserException {
     parser = new CheckingParser(
         XmlPullParserFactory.newInstance().newPullParser(),
         AimlSyntaxException.class);
     this.bot = bot;
   }
 
   private void doAiml() throws IOException, XmlPullParserException,
       AimlParserException {
     parser.nextTag();
     parser.require("root element must be 'aiml'", XmlPullParser.START_TAG,
         "aiml");
     String version = parser.requireAttrib("version");
     if (!version.equals("1.0") && !version.equals("1.0.1"))
       throw new InvalidAimlVersionException(
           "Unsupported AIML version, refusing forward compatible processing mode" +
               parser.getPositionDescription());
 
     parser.nextTag();
     currentPath = new Path(bot.getClassifier().getContextInfo());
     doCategoryList();
     parser.require(XmlPullParser.END_TAG, "aiml");
     parser.next();
     assert (parser.isEvent(XmlPullParser.END_DOCUMENT, null)) : "Syntax error, no markup allowed after the root element";
   }
 
   private void doCategoryList() throws IOException, XmlPullParserException,
       AimlParserException {
     try {
       do {
         switch (parser.getEventType()) {
         case XmlPullParser.START_TAG:
           if (parser.getName().equals("category")) {
             doCategory();
           } else if (parser.getName().equals("contextgroup")) {
             doContextGroup();
           } else if (parser.getName().equals("topic")) {
             doTopic();
           } else
             throw new AimlSyntaxException(
                 "Syntax error: expected category, contextgroup or topic " +
                     parser.getPositionDescription());
           break;
         case XmlPullParser.END_TAG:
           if (parser.getName().equals("aiml") ||
               parser.getName().equals("contextgroup") ||
               parser.getName().equals("topic"))
             return;
           throw new AimlSyntaxException("Syntax error: end tag '" +
               parser.getName() + "' without opening tag " +
               parser.getPositionDescription());
         default:
           assert (false) : "Something very unexpected happened";
         }
         //parser.nextTag();
       } while (true);
     } catch (MultipleContextsException e) {
       throw new AimlSyntaxException(
          "Syntax error, paralell contexts and/or context overloading not allowed. Context " +
               e.getMessage() + " " + parser.getPositionDescription(), e);
     }
   }
 
   private void doTopic() throws IOException, XmlPullParserException,
       AimlParserException, MultipleContextsException {
     parser.require(XmlPullParser.START_TAG, "topic");
     currentPath.save();
     String pattern = parser.requireAttrib("name");
     currentPath.add("topic", pattern);
     parser.nextTag();
     doCategoryList();
     parser.require(XmlPullParser.END_TAG, "topic");
     parser.nextTag();
     currentPath.restore();
   }
 
   private void doContextGroup() throws IOException, XmlPullParserException,
       AimlParserException, MultipleContextsException {
     parser.require(XmlPullParser.START_TAG, "contextgroup");
     parser.nextTag();
     currentPath.save();
     doContextList();
     doCategoryList();
     parser.require(XmlPullParser.END_TAG, "contextgroup");
     parser.nextTag();
     currentPath.restore();
   }
 
   private void doContextList() throws XmlPullParserException, IOException,
       MultipleContextsException, AimlParserException {
     do {
       doContextDef();
     } while (parser.isEvent(XmlPullParser.START_TAG, "context"));
   }
 
   private void doCategory() throws IOException, XmlPullParserException,
       MultipleContextsException, AimlParserException {
     parser.require(XmlPullParser.START_TAG, "category");
     parser.nextTag();
     currentPath.save();
     if (parser.isEvent(XmlPullParser.START_TAG, "pattern")) {
       doPatternC();
     }
     if (parser.isEvent(XmlPullParser.START_TAG, "that")) {
       doThatC();
     }
     if (parser.isEvent(XmlPullParser.START_TAG, "context"))
       doContextList();
     parser.require("expected 'template' element in category",
         XmlPullParser.START_TAG, "template");
     Script s = doTemplate();
     parser.require(XmlPullParser.END_TAG, "category");
     try {
       bot.getClassifier().add(currentPath, s);
       log.info("added category " + currentPath + "{" + s + "}");
     } catch (DuplicatePathException e) {
       log.warning("Duplicate category " + currentPath + " " +
           parser.getPositionDescription());
     }
     parser.nextTag();
     currentPath.restore();
   }
 
   private Script doTemplate() throws IOException, XmlPullParserException,
       AimlParserException {
     parser.require(XmlPullParser.START_TAG, "template");
     Script s = new Block();
     s = s.parse(parser, bot.getClassifier());
     parser.require(XmlPullParser.END_TAG, "template");
     parser.nextTag();
     return s;
   }
 
   private void doContextDef() throws IOException, XmlPullParserException,
       MultipleContextsException, AimlParserException {
     parser.require(XmlPullParser.START_TAG, "context");
     String name = parser.requireAttrib("name");
     parser.next();
     String pattern = doPattern();
     currentPath.add(name, pattern);
     parser.require(XmlPullParser.END_TAG, "context");
     parser.nextTag();
   }
 
   private String doPattern() throws IOException, XmlPullParserException,
       AimlParserException {
     StringBuffer result = new StringBuffer();
     do {
       switch (parser.getEventType()) {
       case XmlPullParser.START_TAG:
         if (parser.getName().equals("bot"))
           result.append(doBotProperty());
         else
           throw new AimlSyntaxException("Unexpected start tag '" +
               parser.getName() +
               "' while parsing pattern, only 'bot' allowed " +
               parser.getPositionDescription());
         break;
       case XmlPullParser.END_TAG:
         String original = result.toString();
         String trimmed = Formatter.collapseWhitespace(original);
         if (!original.equals(trimmed)) {
           log.warning("Trimmed extra whitespace in the pattern [" + trimmed +
               "] " + parser.getPositionDescription());
         }
         return trimmed;
       case XmlPullParser.TEXT:
         result.append(parser.getText());
         parser.next();
         break;
       case XmlPullParser.END_DOCUMENT:
         throw new AimlSyntaxException(
             "Unexpected end of document while parsing pattern " +
                 parser.getPositionDescription());
       default:
         throw new IllegalStateException(
             "Something really weird happened while parsing pattern " +
                 parser.getPositionDescription());
       }
     } while (true);
   }
 
   private String doBotProperty() throws IOException, XmlPullParserException,
       AimlParserException {
     parser.require(XmlPullParser.START_TAG, "bot");
     if (!parser.isEmptyElementTag())
       throw new AimlSyntaxException(
           "Syntax error while parsing bot element in pattern: element must be empty " +
               parser.getPositionDescription());
     String name = parser.requireAttrib("name");
     String result;
     try {
       result = bot.getProperty(name);
     } catch (InvalidPropertyException e) {
       throw new AimlSyntaxException("Syntax error: " + e.getMessage() + " " +
           parser.getPositionDescription());
     }
     parser.nextTag();
     parser.next();
     return result;
   }
 
   private void doThatC() throws IOException, XmlPullParserException,
       MultipleContextsException, AimlParserException {
     parser.require(XmlPullParser.START_TAG, "that");
     parser.next();
     String pattern = doPattern();
     currentPath.add("that", pattern);
     parser.require(XmlPullParser.END_TAG, "that");
     parser.nextTag();
   }
 
   private void doPatternC() throws IOException, XmlPullParserException,
       MultipleContextsException, AimlParserException {
     parser.require(XmlPullParser.START_TAG, "pattern");
     parser.next();
     String pattern = doPattern();
     currentPath.add("input", pattern);
     parser.require(XmlPullParser.END_TAG, "pattern");
     parser.nextTag();
   }
 
   public void load(Reader in) throws IOException, XmlPullParserException,
       AimlParserException {
     parser.setInput(in);
     doAiml();
   }
 
   public void load(InputStream in, String encoding) throws IOException,
       XmlPullParserException, AimlParserException {
     parser.setInput(in, encoding);
     doAiml();
   }
 
   public void load(String file) throws XmlPullParserException, IOException,
       AimlParserException {
     FileReader in = null;
     try {
       in = new FileReader(file);
       parser.setInput(in);
       parser.setProperty("http://xmlpull.org/v1/doc/properties.html#location",
           file);
       doAiml();
     } finally {
       if (in != null) {
         in.close();
       }
     }
   }
 
   public void load(String file, String encoding) throws XmlPullParserException,
       IOException, AimlParserException {
     FileInputStream inputStream = null;
     try {
       inputStream = new FileInputStream(file);
       parser.setInput(inputStream, encoding);
       parser.setProperty("http://xmlpull.org/v1/doc/properties.html#location",
           file);
       doAiml();
     } finally {
       if (inputStream != null) {
         inputStream.close();
       }
     }
   }
 }

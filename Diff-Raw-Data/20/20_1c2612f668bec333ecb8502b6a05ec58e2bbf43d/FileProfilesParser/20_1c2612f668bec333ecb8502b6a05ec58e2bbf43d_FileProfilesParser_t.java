 package com.globalsight.tools;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Characters;
 import javax.xml.stream.events.EndElement;
 import javax.xml.stream.events.StartElement;
 
 import net.sundell.snax.*;
 
 /**
  * Parse the XML returned by a GlobalSight getFileProfilesEx 
  * webservices call.
  */
 public class FileProfilesParser {
 
     static final NodeModel<FP> MODEL = new NodeModelBuilder<FP>() {{
         elements("fileProfileInfo", "fileProfile")
             .attach(new FPHandler());
         elements("fileProfileInfo", "fileProfile", "fileExtensionInfo")
             .element("fileExtension").attach(new FileExtensionHandler());
         elements("fileProfileInfo", "fileProfile", "localeInfo")
             .child().attach(new LocaleHandler());
 
         // Put this rule last, since it's a wildcard
         elements("fileProfileInfo", "fileProfile").child()
             .attach(new AttrHandler());
     }}.build();
     
     private XMLInputFactory factory;
     
     FileProfilesParser(XMLInputFactory factory) {
         this.factory = factory;
     }
     
     public List<FileProfile> parse(String fileProfilesXml) 
                         throws SNAXUserException, XMLStreamException {
         SNAXParser<FP> parser = SNAXParser.createParser(factory, MODEL);
         FP fp = new FP();
         parser.parse(new StringReader(fileProfilesXml), fp);
         return fp.fileProfiles;
     }
     
     class FP {
         public List<FileProfile> fileProfiles = 
                     new ArrayList<FileProfile>();
        
         FileProfile current;
         
         void start() {
             current = new FileProfile();
         }
         void end() throws SNAXUserException {
             if (current.getId() == null) {
                 throw new SNAXUserException("No id");
             }
             if (current.getName() == null) {
                 throw new SNAXUserException("No name");
             }
             fileProfiles.add(current);
             current = null;
         }
     }
 
     static class FPHandler extends DefaultElementHandler<FP> {
         @Override
         public void startElement(StartElement element, FP data)
                 throws SNAXUserException {
             data.start();
         }
         @Override
         public void endElement(EndElement element, FP data)
                 throws SNAXUserException {
             data.end();
         }
     }
     static class AttrHandler extends DefaultElementHandler<FP> {
         @Override
         public void characters(StartElement parent, Characters contents, 
                 FP data) throws SNAXUserException {
             String el = parent.getName().getLocalPart();
             if (el.equals("id")) {
                 data.current.setId(contents.getData());
             }
             else if (el.equals("name")) {
                 data.current.setName(contents.getData());
             }
             else if (el.equals("l10nprofile")) {
                 data.current.setL10nProfile(contents.getData());
             }
             else if (el.equals("description")) {
                 data.current.setDescription(contents.getData());
             }
             else {
                 System.out.println("Ignored: " + el);
             }
         }
     }
     static class FileExtensionHandler extends DefaultElementHandler<FP> {
         @Override
         public void characters(StartElement parent, Characters contents, 
                 FP data) throws SNAXUserException {
             data.current.addFileExtension(contents.getData());
         }
     }
     static class LocaleHandler extends DefaultElementHandler<FP> {
         @Override
         public void characters(StartElement parent, Characters contents, 
                 FP data) throws SNAXUserException {
             String el = parent.getName().getLocalPart();
             if (el.equals("sourceLocale")) {
                 data.current.setSourceLocale(contents.getData());
             }
             else if (el.equals("targetLocale")) {
                 data.current.addTargetLocale(contents.getData());
             }
         }
     }
 }

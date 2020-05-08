 package com.seitenbau.micgwaf.parser;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import com.seitenbau.micgwaf.component.Component;
 
 public class HtmlParser
 {
   private static final String SAX_NAMESPACE_FEATURE_NAME 
       = "http://xml.org/sax/features/namespaces";
   
  public Map<String, Component> readComponents(File sourceDirectory)
   {
    if (sourceDirectory == null)
    {
      throw new NullPointerException("sourceDirectory must not be null");
    }
    if (!sourceDirectory.isDirectory())
     {
       throw new IllegalArgumentException(
          sourceDirectory.getAbsolutePath() + " is no Directory");
     }
     
     Map<String, Component> result = new HashMap<>();
    File[] files = sourceDirectory.listFiles();
     for (File file : files)
     {
       String fileName = file.getName();
       if (fileName.endsWith(".xhtml"))
       {
         try
         {
           FileInputStream inputStream = new FileInputStream(file);
           Component component = parse(inputStream);
           if (component.getId() == null)
           {
             component.setId(fileName.substring(0, fileName.length() - 6));
           }
           result.put(component.getId(), component);
         }
         catch (FileNotFoundException e)
         {
           throw new RuntimeException(e);
         } 
         catch (SAXException e)
         {
           throw new RuntimeException(e);
         }
         catch (IOException e)
         {
           throw new RuntimeException(e);
         } 
         catch (ParserConfigurationException e)
         {
           throw new RuntimeException(e);
         }
         
       }
     }
     return result;
   }  
   
   public Component parse(InputStream inputStream) 
       throws SAXException, IOException, ParserConfigurationException
   {
     SAXParserFactory saxParserFactory = SAXParserFactory.newInstance(); 
     SAXParser saxParser = saxParserFactory.newSAXParser(); 
     XMLReader xmlReader = saxParser.getXMLReader(); 
     DelegatingContentHandler handler 
         = new DelegatingContentHandler();
     xmlReader.setContentHandler(handler);
     xmlReader.setEntityResolver(handler);
     xmlReader.setDTDHandler(handler);
     xmlReader.setErrorHandler(handler);
     xmlReader.setFeature(SAX_NAMESPACE_FEATURE_NAME, true);
     xmlReader.parse(new InputSource(inputStream)); 
     return handler.currentResult;
   }
 }

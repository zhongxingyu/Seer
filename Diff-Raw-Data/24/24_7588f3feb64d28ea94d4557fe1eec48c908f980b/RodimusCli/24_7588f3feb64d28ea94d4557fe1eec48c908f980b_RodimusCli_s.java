 /**
  *    Copyright 2013 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.rodimus;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.Properties;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Source;
 import javax.xml.transform.Templates;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.sax.SAXResult;
 import javax.xml.transform.sax.SAXTransformerFactory;
 import javax.xml.transform.sax.TransformerHandler;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.output.StringBuilderWriter;
 import org.apache.poi.util.IOUtils;
 import org.apache.tika.Tika;
 import org.apache.tika.extractor.EmbeddedDocumentExtractor;
 import org.apache.tika.metadata.HttpHeaders;
 import org.apache.tika.metadata.Metadata;
 import org.apache.tika.metadata.TikaMetadataKeys;
 import org.apache.tika.parser.ParseContext;
 import org.apache.tika.parser.Parser;
 import org.apache.xml.serializer.Method;
 import org.apache.xml.serializer.OutputPropertiesFactory;
 import org.apache.xml.serializer.ToHTMLStream;
 import org.apache.xml.serializer.ToTextStream;
 import org.apache.xml.serializer.ToXMLStream;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 
 import net.sf.saxon.TransformerFactoryImpl;
 
 import com.lexicalscope.jewel.cli.ArgumentValidationException;
 import com.lexicalscope.jewel.cli.Cli;
 import com.lexicalscope.jewel.cli.CliFactory;
 
 public class RodimusCli {
   public static void main( String... args ) {
     try {
 	final Cli<RodimusInterface> cli = CliFactory.createCli(RodimusInterface.class);
 	final RodimusInterface options = cli.parseArguments(args);
 	
 	// if help was requested, then display the help message and exit.
 	if( options.isHelp() ) {
       System.out.println(cli.getHelpMessage());
       return;
 	}
 	
 	final boolean verbose = options.isVerbose();
 	
 	if( options.getFiles() == null || options.getFiles().size() < 1 ) {
       System.out.println(cli.getHelpMessage());
       return;	  
 	}
 	
 	// get the input file.
 	File inputFile = options.getFiles().get(0);
 	
 	// get the output file.
 	File outputDir = null;
 	if( options.getFiles().size() > 1 ) {
 	  outputDir = options.getFiles().get(1);
 	}
 	else {
 	  outputDir = new File(inputFile.getName().replaceFirst("\\.[^.]+\\Z", ""));
 	}
 	if( outputDir.exists() && !outputDir.isDirectory() ) {
 	  throw new Exception(outputDir+" is not a directory.");
 	}
 	outputDir.mkdirs();
 	
 	transformDocument(inputFile, outputDir, verbose);
     }
     catch( Exception e ) {
       e.printStackTrace(System.err);
     }
   }
   
   public static ParseContext createParseContext( final File assetDir, final boolean verbose ) {
     ParseContext context = new ParseContext();
     context.set(EmbeddedDocumentExtractor.class, new EmbeddedDocumentExtractor() {
         @Override
         public void parseEmbedded(InputStream in, ContentHandler handler, Metadata metadata, boolean outputHtml) throws SAXException, IOException {
             if( verbose ) {
                 System.out.println("Metadata:");
                 for( String name : metadata.names() ) {
                     System.out.println(name+":"+metadata.get(name));
                 }
                 System.out.println("Output Html:"+outputHtml);
             }
             
             // get the file out of the document and write it to disk.
             String name = metadata.get("resourceName");
             File imageFile = new File(assetDir, name);
             FileOutputStream out = new FileOutputStream(imageFile);
             IOUtils.copy(in, out);
         }
 
         /** false */
         @Override
         public boolean shouldParseEmbedded(Metadata arg0) {
             return true;
         }});
     return context;
     
   }
   
   public static void parseInput(InputStream in, ContentHandler out, ParseContext context, boolean verbose) {
     try {
       Tika tika = new Tika();
       Parser parser = tika.getParser();
       parser.parse(in, out, new Metadata(), context);
     } catch (Exception e) {
       System.out.println("Failed to parse file.");
       e.printStackTrace();
       return;
     }
   }
   
   public static StreamSource createStreamSource(URL url) throws IOException {
     StreamSource source = new StreamSource();
     source.setSystemId(url.toExternalForm());
     source.setInputStream(url.openStream());
     return source;
   }
   
   public static TransformerHandler getContentHandler(Source source)
       throws Exception {
     try {
       SAXTransformerFactory factory = (SAXTransformerFactory) new TransformerFactoryImpl();
       return factory.newTransformerHandler(source);
     } catch (Exception e) {
       throw new Exception("Could not load transform "
           + source.getSystemId(), e);
     }
   }
   
   public static InputStream createInputStream(File file) 
     throws Exception
     {
     try {
         return new FileInputStream(file);
     }
     catch( IOException ioe ) {
         throw new Exception("Could not load "+file.getAbsolutePath(), ioe);
     }
     }
 
   public static void transformDocument(File inputFile, File outputDir, boolean verbose) throws Exception {
     StreamSource xhtmlHandlerSource = createStreamSource(RodimusCli.class.getResource("/rodimus.xsl"));
 
     File indexFile = new File(outputDir, "index.html");
    File assetDir = new File(outputDir, "img");
     assetDir.mkdirs();
     
     // Set up the output buffer.
     StringBuilderWriter output = new StringBuilderWriter();
     
     // Set up the serializer.
     ToXMLStream serializer = new ToXMLStream();
     serializer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT,String.valueOf(2));
     serializer.setOutputProperty(OutputPropertiesFactory.S_KEY_LINE_SEPARATOR,"\n");
     serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
     serializer.setOutputProperty(OutputKeys.INDENT, "yes");
     serializer.setWriter(output);
     
     // Set up the xhtmlStructure handler.
     TransformerHandler xhtmlHandler = getContentHandler(xhtmlHandlerSource);
     xhtmlHandler.setResult(new SAXResult(serializer));
 
     // build the Tika handler.
     ParseContext context = createParseContext(assetDir, verbose);
    PostTikaHandler cleanUp = new PostTikaHandler();
     cleanUp.setContentHandler(xhtmlHandler);
     parseInput(createInputStream(inputFile), cleanUp, context, verbose);
     
     // Do some regular expression cleanup.
     String preOutput = output.toString();
     preOutput = preOutput.replaceAll("/>", " />");
     // TODO: img is in this list, but it is not a block level element.
     String blockLevel = "(?:address|article|aside|audio|blockquote|canvas|dd|div|dl|fieldset|figcaption|figure|footer|form|h[1-6]|header|hgroup|hr|noscript|ol|output|p|pre|sectop|table|tfoot|ul|video|img)";
     preOutput = preOutput.replaceAll("(</"+blockLevel+">)(\\s*)(<"+blockLevel+")", "$1$2$2$3");
     
     FileUtils.write(indexFile, preOutput);
   }
 }

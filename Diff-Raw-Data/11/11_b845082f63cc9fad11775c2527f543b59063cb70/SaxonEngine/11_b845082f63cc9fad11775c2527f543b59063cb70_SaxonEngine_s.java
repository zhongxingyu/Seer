 package uk.ac.ebi.arrayexpress.components;
 
 /*
  * Copyright 2009-2010 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import net.sf.saxon.Configuration;
 import net.sf.saxon.Controller;
 import net.sf.saxon.TransformerFactoryImpl;
 import net.sf.saxon.event.SequenceWriter;
 import net.sf.saxon.functions.FunctionLibraryList;
 import net.sf.saxon.instruct.TerminationException;
 import net.sf.saxon.om.DocumentInfo;
 import net.sf.saxon.om.Item;
 import net.sf.saxon.tinytree.TinyBuilder;
 import net.sf.saxon.trans.XPathException;
 import net.sf.saxon.xpath.XPathEvaluator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.app.Application;
 import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 import uk.ac.ebi.arrayexpress.utils.saxon.DocumentSource;
 import uk.ac.ebi.arrayexpress.utils.saxon.functions.UserFunctionLibrary;
 
 import javax.xml.transform.*;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import java.io.*;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 public class SaxonEngine extends ApplicationComponent implements URIResolver, ErrorListener
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
    // logging writer for the transformations
    private LoggerWriter loggerWriter;

     public TransformerFactoryImpl trFactory;
     private Map<String, Templates> templatesCache = new HashMap<String, Templates>();
     private Map<String, DocumentSource> documentSources = new HashMap<String, DocumentSource>();
 
     private final String XML_STRING_ENCODING = "ISO-8859-1";
 
     public SaxonEngine()
     {
         super("SaxonEngine");
     }
 
     public void initialize() throws Exception
     {
         // This is so we make sure we use Saxon and not anything else
         trFactory = (TransformerFactoryImpl)TransformerFactoryImpl.newInstance();
         trFactory.setErrorListener(this);
         trFactory.setURIResolver(this);
 
         // ok so we attempt to register some extension function now :)
         Configuration c = trFactory.getConfiguration();
         FunctionLibraryList extLibraries = new FunctionLibraryList();
         extLibraries.addFunctionLibrary(c.getExtensionBinder("java"));
         extLibraries.addFunctionLibrary(new UserFunctionLibrary());
         c.setExtensionBinder("java", extLibraries);
        
        loggerWriter = new LoggerWriter(logger);
     }
 
     public void terminate() throws Exception
     {
        loggerWriter = null;
     }
 
     public void registerDocumentSource(DocumentSource documentSource)
     {
         this.documentSources.put(documentSource.getDocumentURI(), documentSource);
     }
 
     // implements URIResolver.resolve
     public Source resolve( String href, String base ) throws TransformerException
     {
         Source src;
         try {
             // try document sources first
             if (documentSources.containsKey(href)) {
                 return documentSources.get(href).getDocument();    
             } else {
                 URL resource = Application.getInstance().getResource("/WEB-INF/server-assets/stylesheets/" + href);
                 if (null == resource) {
                     throw new TransformerException("Unable to locate stylesheet resource [" + href + "]");
                 }
                 InputStream input = resource.openStream();
                 if (null == input) {
                     throw new TransformerException("Unable to open stream for resource [" + resource + "]");
                 }
                 src = new StreamSource(input);
             }
         } catch ( TransformerException x ) {
             throw x;
         } catch ( Exception x ) {
             logger.error("Caught an exception:", x);
             throw new TransformerException(x.getMessage());
         }
 
         return src;
     }
 
     // implements ErrorListener.error
     public void error( TransformerException x ) throws TransformerException
     {
         throw x;
     }
 
     // implements ErrorListener.fatalError
     public void fatalError( TransformerException x ) throws TransformerException
     {
         throw x;
     }
 
     // implements ErrorListenet.warning
     public void	warning( TransformerException x )
     {
         logger.warn("There was a warning while transforming:", x);
     }
 
     public String serializeDocument( DocumentInfo document ) throws Exception
     {
         String string = null;
         Transformer transformer = trFactory.newTransformer();
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 
         transformer.setOutputProperty(OutputKeys.METHOD, "xml");
         transformer.setOutputProperty(OutputKeys.INDENT, "no");
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
 
         transformer.transform(document, new StreamResult(outStream));
         return outStream.toString(XML_STRING_ENCODING);
     }
 
     public DocumentInfo buildDocument( String xml ) throws XPathException
     {
         StringReader reader = new StringReader(xml);
         Configuration config = trFactory.getConfiguration();
         return config.buildDocument(new StreamSource(reader));
     }
 
     public String evaluateXPathSingle( DocumentInfo doc, String xpath ) throws XPathExpressionException
     {
         XPath xp = new XPathEvaluator(trFactory.getConfiguration());
         XPathExpression xpe = xp.compile(xpath);
         return xpe.evaluate(doc);
     }
 
     public boolean transformToWriter( DocumentInfo srcDocument, String stylesheet, Map<String,String[]> params, Writer dstWriter ) throws Exception
     {
         return transform(srcDocument, stylesheet, params, new StreamResult(dstWriter));
     }
 
     public boolean transformToFile( DocumentInfo srcDocument, String stylesheet, Map<String,String[]> params, File dstFile ) throws Exception
     {
         return transform(srcDocument, stylesheet, params, new StreamResult(dstFile));
     }
 
     public String transformToString( DocumentInfo srcDocument, String stylesheet, Map<String,String[]> params ) throws Exception
     {
         String str = null;
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 
         if (transform(srcDocument, stylesheet, params, new StreamResult(outStream))) {
             str = outStream.toString(XML_STRING_ENCODING);
             return str;
         } else {
             return null;
         }
     }
 
     public DocumentInfo transform( String srcXmlString, String stylesheet, Map<String,String[]> params ) throws Exception
     {
         Source src = new StreamSource(new StringReader(srcXmlString));
         TinyBuilder dstDocument = new TinyBuilder();
         if (transform(src, stylesheet, params, dstDocument)) {
             return (DocumentInfo)dstDocument.getCurrentRoot();
         }
         return null;
     }
 
     public DocumentInfo transform( DocumentInfo srcDocument, String stylesheet, Map<String,String[]> params ) throws Exception
     {
         TinyBuilder dstDocument = new TinyBuilder();
         if (transform(srcDocument, stylesheet, params, dstDocument)) {
             return (DocumentInfo)dstDocument.getCurrentRoot();
         }
         return null;
     }
 
     private boolean transform( Source src, String stylesheet, Map<String,String[]> params, Result dst ) throws Exception
     {
         boolean result = false;
         try {
             Templates templates;
             if (!templatesCache.containsKey(stylesheet)) {
                 logger.debug("Caching prepared stylesheet [{}]", stylesheet);
                 // Open the stylesheet
                 Source xslSource = resolve(stylesheet, null);
 
                 templates = trFactory.newTemplates(xslSource);
                 templatesCache.put(stylesheet, templates);
             } else {
                 logger.debug("Getting prepared stylesheet [{}] from cache", stylesheet);
                 templates = templatesCache.get(stylesheet);
             }
             Transformer xslt = templates.newTransformer();
 
             // redirect all messages to logger
            ((Controller)xslt).setMessageEmitter(loggerWriter);
 
             // assign the parameters (if not null)
             if (null != params) {
                 for ( Map.Entry<String, String[]> param : params.entrySet() ) {
                     xslt.setParameter(param.getKey(), StringTools.arrayToString(param.getValue(), " "));
                 }
             }
 
             // Perform the transformation, sending the output to the response.
             logger.debug("about to start transformer.transform() with stylesheet [{}]", stylesheet);
             xslt.transform(src, dst);
             logger.debug("transformer.transform() completed");
 
             result = true;
         } catch (TerminationException x ) {
             logger.error("Transformation has been terminated by xsl instruction, please inspect log for details");
         }
         return result;
     }
 
     class LoggerWriter extends SequenceWriter
     {
         private Logger logger;
 
         public LoggerWriter(Logger logger)
         {
             this.logger = logger;
         }
 
         public void write(Item item)
         {
             logger.debug("[xsl:message] {}", item.getStringValue());
         }
     }
 }

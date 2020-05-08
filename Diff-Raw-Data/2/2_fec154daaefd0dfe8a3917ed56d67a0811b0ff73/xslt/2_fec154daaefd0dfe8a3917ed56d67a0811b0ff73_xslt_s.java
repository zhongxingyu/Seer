 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ywc.core;
 
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.transform.Source;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import net.sf.saxon.Configuration;
 import net.sf.saxon.PreparedStylesheet;
 import net.sf.saxon.trans.CompilerInfo;
 
 /**
  *
  * @author topher
  */
 public class xslt {
 
     private Configuration config;
     private StreamSource styleSheet;
 
     public xslt(StreamSource streamSource) {
         this(streamSource, new Configuration());
     }
 
     /**
      * Constructs a new transformer with the given XSLT stylesheet
      * and the given <code>Configuration</code>.
      *
      * @param streamSource - The style sheet as a <code>StreamSource</code>
      * @param configuration - The configuration object
      */
     public xslt(StreamSource streamSource, Configuration configuration) {
         this.styleSheet = streamSource;
         this.config = configuration;
     }
 
     /**
      * Transforms the given XML file wrapped in a <code>StreamSource</code>
      * and returns an XML file in form of a <code>String</code>. This <code>String</code>
      * can be than wrapped into a <code>java.io.StringWriter</code> to get a
      * <code>java.io.Writer</code> or into a <code>java.io.StringReader</code> to get a
      * <code>java.io.Reader</code>.
      *
      * @param streamSourceInput
      * @return An XML document in form of a <code>String</code>
      * @throws Exception
      */
     public String transform(StreamSource streamSourceInput, HashMap xslParams) {
         String outStr = null;
         try {
             StringWriter out = new StringWriter();
 
             Transformer transformer = newTransformer(styleSheet);
 
             if (xslParams != null) {
                 Iterator it = xslParams.entrySet().iterator();
                 while (it.hasNext()) {
                     Map.Entry pairs = (Map.Entry) it.next();
                     transformer.setParameter((String) pairs.getKey(), pairs.getValue());
                 }
             }
 
             transformer.transform(streamSourceInput, new StreamResult(out));
 
             //      System.out.println(net.sf.saxon.Version.getProductVariantAndVersion(this.config));
 
             outStr = out.toString();
 
         } catch (Exception ex) {
             Logger.getLogger(xslt.class.getName()).log(Level.SEVERE, null, ex);
         }
         return outStr;
     }
 
     /**
      * Returns a new <code>Transformer</code> object for the given XSLT
      * file.
      *
      * @param source
      * @return A <code>Transformer</code>
      * @throws TransformerConfigurationException
      */
     protected Transformer newTransformer(Source source) throws TransformerConfigurationException {
         Templates templates = newTemplates(source);
         return templates.newTransformer();
     }
 
     /**
      * Creates a new XSLT stylesheet template from the given
      * XSLT source file
      *
      * @param source
      * @return A <code>Templates</code>
      * @throws TransformerConfigurationException
      */
     protected Templates newTemplates(Source source) throws TransformerConfigurationException {
         CompilerInfo info = new CompilerInfo();
         info.setURIResolver(config.getURIResolver());
         info.setErrorListener(config.getErrorListener());
 //        info.setCompileWithTracing(config.isCompileWithTracing());
 
 
         return PreparedStylesheet.compile(source, config, info);
     }
 
     public static String exec(String xmlFile, String xslFile, HashMap xslParams, String mcHash, Boolean flushCache) {
         
         long transformStart = System.currentTimeMillis();
         
         if (flushCache != null && flushCache) {
             mccon.mc.delete(mcHash);
         }
 
         Boolean isCachedEnabled = true;
         if ((mcHash == null) || (!settings.enableXslTransformCaching())) {
             isCachedEnabled = false;
         }
 
         String outMCStr = null;
         if (isCachedEnabled) {
             outMCStr = (String) mccon.mc.get(mcHash);
         }
 
 
         String outStr = "";
         if (outMCStr != null) {
             outStr = outMCStr;
         } else {
 
             xslt transformer = new xslt(new StreamSource(xslFile));
             outStr = transformer.transform(new StreamSource(xmlFile), xslParams);
 
            if (mcHash != null) {
                 mccon.mc.set(mcHash, outStr);
             }
 
         }
         
         if (settings.enableXslVerboseLogging()) {
             System.out.println("\txsl: " + (System.currentTimeMillis() - transformStart) + "ms\t" + xmlFile + " -> " + xslFile);
         }
         
         return outStr;
     }
 }

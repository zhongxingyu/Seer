 package org.synyx.minos.skillz.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.ConfigurationException;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 import org.apache.fop.apps.FOUserAgent;
 import org.apache.fop.apps.Fop;
 import org.apache.fop.apps.FopFactory;
 import org.apache.fop.apps.MimeConstants;
 import org.springframework.core.io.Resource;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 
 /**
  * Implementation of {@link FopXsltService}.
  * 
  * @author Markus Knittig - knittig@synyx.de
  */
 class FopXsltServiceImpl implements FopXsltService {
 
     private final TransformerFactory transformerFactory;
     private final Resource defaultXsltResource;
     private Resource configuration;
     private final URIResolver uriResolver;
 
 
     /**
      * Constructor for {@link FopXsltServiceImpl}. Uses the Saxon implementation of {@link TransformerFactory}.
      * 
      * @param defaultXsltResource
      */
     public FopXsltServiceImpl(Resource defaultXsltResource) {
 
         System.setProperty("javax.xml.transform.TransformerFactory", "com.icl.saxon.TransformerFactoryImpl");
         this.transformerFactory = TransformerFactory.newInstance();
         this.defaultXsltResource = defaultXsltResource;
         this.uriResolver = new ClassPathURIResolver();
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.minos.skillz.service.FopService#createFop(java.io.OutputStream)
      */
     @Override
     public Fop createFop(OutputStream outputStream) throws SAXException, IOException, ConfigurationException {
 
         FopFactory fopFactory = FopFactory.newInstance();
 
         if (configuration != null) {
             InputSource fopconfigInputSource = new InputSource(configuration.getInputStream());
             DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
             Configuration cfg = cfgBuilder.build(fopconfigInputSource);
 
             fopFactory.setUserConfig(cfg);
         }
 
         fopFactory.setURIResolver(uriResolver);
         FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
         foUserAgent.setURIResolver(uriResolver);
 
         return fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outputStream);
     }
 
 
     /**
      * Creates a {@link Transformer} instance with the given XSLT file or a default XSLT file if <code>null</code> was
      * given.
      * 
      * @param xsltFile
      * @return
      * @throws TransformerConfigurationException
      * @throws IOException
      */
     public Transformer createTransformer(File xsltFile) throws TransformerConfigurationException, IOException {
 
         transformerFactory.setURIResolver(uriResolver);
         Transformer transformer = null;
 
         if (xsltFile == null) {
            // using getInputStream() instead of getFile() because getFile() cannot handle xsl files in jars
            transformer = transformerFactory.newTransformer(new StreamSource(defaultXsltResource.getInputStream()));
         } else {
             transformer = transformerFactory.newTransformer(new StreamSource(xsltFile));
         }
 
         transformer.setParameter("versionParam", "2.0");
         transformer.setURIResolver(uriResolver);
 
         return transformer;
     }
 
 
     public void setConfiguration(Resource configuration) {
 
         this.configuration = configuration;
     }
 
     /**
      * This class is a URIResolver implementation that provides access to resources in the class path of a application
      * using class path URIs.
      * 
      * @author Markus Knittig - knittig@synyx.de
      */
     private class ClassPathURIResolver implements URIResolver {
 
         /** The protocol name for the class path URIs. */
         // XXX file protocol must be used, since FOP > 0.94 needs a valid
         // URL protocol
         public static final String CLASSPATH_CONTEXT_PROTOCOL = "file:";
 
 
         /*
          * (non-Javadoc)
          * 
          * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
          */
         @Override
         public Source resolve(String href, String base) throws TransformerException {
 
             if (href.startsWith(CLASSPATH_CONTEXT_PROTOCOL)) {
                 return resolveServletContextURI(href.substring(CLASSPATH_CONTEXT_PROTOCOL.length()));
             } else {
                 if (base != null && base.startsWith(CLASSPATH_CONTEXT_PROTOCOL) && (href.indexOf(':') < 0)) {
                     String abs = base + href;
                     return resolveServletContextURI(abs.substring(CLASSPATH_CONTEXT_PROTOCOL.length()));
                 } else {
                     return null;
                 }
             }
         }
 
 
         /**
          * Resolves the class path URI.
          * 
          * @param path
          * @return
          * @throws TransformerException
          */
         protected Source resolveServletContextURI(String path) throws TransformerException {
 
             while (path.startsWith("//")) {
                 path = path.substring(1);
             }
             URL url = ClassPathURIResolver.class.getResource(path);
             InputStream in = ClassPathURIResolver.class.getResourceAsStream(path);
             if (in != null) {
                 if (url != null) {
                     return new StreamSource(in, url.toExternalForm());
                 } else {
                     return new StreamSource(in);
                 }
             } else {
                 throw new TransformerException("Resource does not exist. \"" + path
                         + "\" is not accessible through the classpath.");
             }
         }
 
     }
 
 }

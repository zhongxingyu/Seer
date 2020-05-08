 package net.zeroinstall.pom2feed.service;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.io.BaseEncoding.base64;
 import java.io.IOException;
 import java.net.URL;
 import javax.xml.xpath.XPathExpressionException;
 import net.zeroinstall.model.InterfaceDocument;
 import net.zeroinstall.pom2feed.core.FeedBuilder;
 import net.zeroinstall.pom2feed.core.MavenMetadata;
 import static net.zeroinstall.pom2feed.core.MavenUtils.*;
 import static net.zeroinstall.pom2feed.core.UrlUtils.*;
 import org.apache.maven.model.*;
 import org.apache.maven.model.building.*;
 import org.apache.maven.model.resolution.InvalidRepositoryException;
 import org.apache.maven.model.resolution.ModelResolver;
 import org.apache.maven.model.resolution.UnresolvableModelException;
 import org.apache.xmlbeans.XmlCursor;
 import org.apache.xmlbeans.XmlOptions;
 import org.xml.sax.SAXException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Generates Zero Install feeds for Maven artifacts on demand.
  */
 public class FeedGenerator implements FeedProvider {
 
     private final static Logger LOGGER = LoggerFactory.getLogger(FeedGenerator.class);
     /**
      * The base URL of the Maven repository used to provide binaries.
      */
     private final URL mavenRepository;
     /**
      * The base URL of the pom2feed service used to provide dependencies. This
      * is usually the URL of this service itself.
      */
     private final URL pom2feedService;
     /**
      * The name of the key to use for GnuPG signing.
      */
     private final String gnuPGKey;
 
     /**
      * Creates a feed generator.
      *
      * @param mavenRepository The base URL of the Maven repository used to
      * provide binaries.
      * @param pom2feedService The base URL of the pom2feed service used to
      * provide dependencies. This is usually the URL of this service itself.
      * @param gnuPGKey The name of the key to use for GnuPG signing.
      */
     public FeedGenerator(URL mavenRepository, URL pom2feedService, String gnuPGKey) {
         this.mavenRepository = ensureSlashEnd(mavenRepository);
         this.pom2feedService = ensureSlashEnd(pom2feedService);
         this.gnuPGKey = gnuPGKey;
     }
 
     @Override
     public String getFeed(final String artifactPath) throws IOException, SAXException, XPathExpressionException, ModelBuildingException {
         MavenMetadata metadata = getMetadata(artifactPath);
         InterfaceDocument feed = buildFeed(metadata);
 
         addStylesheet(feed);
         return signFeed(feedToXmlText(feed));
     }
 
     private MavenMetadata getMetadata(String artifactPath) throws IOException, SAXException, XPathExpressionException {
         if (mavenRepository.getHost().equals("repo.maven.apache.org")) {
             return MavenMetadata.query(new URL("http://search.maven.org/solrsearch/"), artifactPath);
         } else {
             return MavenMetadata.load(new URL(mavenRepository, artifactPath + "maven-metadata.xml"));
         }
     }
 
     private InterfaceDocument buildFeed(MavenMetadata metadata) throws ModelBuildingException {
         FeedBuilder feedBuilder = new FeedBuilder(mavenRepository, pom2feedService);
         addMetadataToFeed(metadata, feedBuilder);
         addImplementationsToFeed(metadata, feedBuilder);
 
         InterfaceDocument feed = feedBuilder.getDocument();
         feed.getInterface().setUri(getServiceUrl(pom2feedService, metadata.getGroupId(), metadata.getArtifactId()));
         return feed;
     }
 
     private void addMetadataToFeed(MavenMetadata metadata, FeedBuilder feedBuilder) throws ModelBuildingException {
         Model model;
         try {
             model = getModel(metadata, metadata.getLatestVersion());
         } catch (ModelBuildingException ex) {
             // Fall back to first version if latest version does not work
             model = getModel(metadata, metadata.getVersions().get(0));
         }
 
         feedBuilder.addMetadata(model);
     }
 
     private void addImplementationsToFeed(MavenMetadata metadata, FeedBuilder feedBuilder) {
         for (String version : metadata.getVersions()) {
             try {
                 feedBuilder.addRemoteImplementation(getModel(metadata, version));
             } catch (ModelBuildingException ex) {
                 LOGGER.trace(null, ex);
             } catch (IOException ex) {
                 LOGGER.trace(null, ex);
             } catch (IllegalArgumentException ex) {
                 LOGGER.trace(null, ex);
             }
         }
     }
 
     private Model getModel(MavenMetadata metadata, String version) throws ModelBuildingException {
         UrlModelSource modelSource = new UrlModelSource(getArtifactFileUrl(mavenRepository,
                 metadata.getGroupId(), metadata.getArtifactId(), version, "pom"));
         ModelBuildingRequest request = new DefaultModelBuildingRequest()
                 .setModelSource(modelSource)
                 .setModelResolver(new RepositoryModelResolver())
                 // Special cases
                 .setInactiveProfileIds(newArrayList("java-1.5-detected")); // Apache Commons
 
         ModelBuilder builder = new DefaultModelBuilderFactory().newInstance();
         return builder.build(request).getEffectiveModel();
     }
 
     private void addStylesheet(InterfaceDocument feed) {
         XmlCursor cursor = feed.newCursor();
         cursor.toNextToken();
         cursor.insertProcInst("xml-stylesheet", "type='text/xsl' href='interface.xsl'");
     }
 
     private String feedToXmlText(InterfaceDocument feed) {
         return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                 + feed.xmlText(new XmlOptions().setUseDefaultNamespace().setSavePrettyPrint());
     }
 
     private String signFeed(String xmlText) throws IOException {
         xmlText += "\n";
         if (gnuPGKey == null) {
             return xmlText;
         }
 
        String signature = base64().lowerCase().encode(GnuPG.detachSign(xmlText, gnuPGKey));
         return xmlText + "<!-- Base64 Signature\n" + signature + "\n-->\n";
     }
 
     private class RepositoryModelResolver implements ModelResolver {
 
         @Override
         public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
             return new UrlModelSource(getArtifactFileUrl(mavenRepository,
                     groupId, artifactId, version, "pom"));
         }
 
         @Override
         public void addRepository(Repository repository) throws InvalidRepositoryException {
         }
 
         @Override
         public ModelResolver newCopy() {
             throw new UnsupportedOperationException("Not supported yet.");
         }
     }
 }

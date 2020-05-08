 package net.zeroinstall.pom2feed.core;
 
 import com.google.common.base.Joiner;
 import static com.google.common.base.Strings.isNullOrEmpty;
 import static com.google.common.base.Throwables.propagate;
 import com.google.common.collect.ImmutableList;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import static java.net.URLEncoder.encode;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import javax.xml.parsers.*;
 import javax.xml.xpath.*;
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 
 /**
  * Represents versioning metadata for a Maven artifact.
  */
 public class MavenMetadata {
 
     private static final DocumentBuilder docBuilder;
     private static final XPathExpression groupIdPath, artifactIdPath, versionsPath, latestVersionPath, versionsQueryPath;
 
     static {
         try {
             docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         } catch (ParserConfigurationException ex) {
             throw propagate(ex);
         }
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         try {
             groupIdPath = xpath.compile("//groupId/text()");
             artifactIdPath = xpath.compile("//artifactId/text()");
             versionsPath = xpath.compile("//versioning//versions//version/text()");
             latestVersionPath = xpath.compile("//versioning//latest/text()");
             versionsQueryPath = xpath.compile("//result[@name='response']//doc/str[@name='v']/text()");
         } catch (XPathExpressionException ex) {
             throw propagate(ex);
         }
     }
     private final String groupId;
     private final String artifactId;
     private final String latestVersion;
     private final List<String> versions;
 
     /**
      * Creates a new versioning metadata instance.
      *
      * @param groupId The group ID.
      * @param artifactId The artifact ID.
      * @param latestVersion The latest version of the artifact.
      * @param versions A list of all versions of the artifact.
      */
     public MavenMetadata(String groupId, String artifactId, String latestVersion, Iterable<String> versions) {
         this.groupId = groupId;
         this.artifactId = artifactId;
         this.latestVersion = latestVersion;
         this.versions = new ImmutableList.Builder<String>().addAll(versions).build();
     }
 
     /**
      * Loads versioning metadata from a remote XML file.
      *
      * @throws IOException Download of the source file failed.
      * @throws SAXException Parsing of the source file failed.
      */
     public static MavenMetadata load(URL url) throws IOException, SAXException, XPathExpressionException {
         return parse(url.openStream());
     }
 
     /**
      * Parses versioning metadata from an XML stream.
      *
      * @throws IOException Reading of the metadata file failed.
      * @throws SAXException Parsing of the metadata file failed.
      */
     public static MavenMetadata parse(InputStream stream) throws IOException, SAXException, XPathExpressionException {
         Document doc = docBuilder.parse(stream);
 
         NodeList groupIdNodes = (NodeList) groupIdPath.evaluate(doc, XPathConstants.NODESET);
         String groupId = groupIdNodes.item(0).getNodeValue();
         NodeList artifactIdNodes = (NodeList) artifactIdPath.evaluate(doc, XPathConstants.NODESET);
         String artifactId = artifactIdNodes.item(0).getNodeValue();
         NodeList versionNodes = (NodeList) versionsPath.evaluate(doc, XPathConstants.NODESET);
         List<String> versions = new LinkedList<String>();
         for (int i = 0; i < versionNodes.getLength(); i++) {
             versions.add(versionNodes.item(i).getNodeValue());
         }
         String latestVersion = (String) latestVersionPath.evaluate(doc, XPathConstants.STRING);
         if (isNullOrEmpty(latestVersion)) {
             latestVersion = versions.get(versions.size() - 1);
         }
 
         return new MavenMetadata(groupId, artifactId, latestVersion, versions);
     }
 
     /**
      * Retrieves versioning metadata from a search query.
      *
      * @throws IOException Download of the query data failed.
      * @throws SAXException Parsing of the query data failed.
      */
    public static MavenMetadata performQuery(URL queryService, String artifactPath) throws IOException, SAXException, XPathExpressionException {
         String[] parts = artifactPath.split("/");
         String groupId = Joiner.on(".").join(Arrays.copyOfRange(parts, 0, parts.length - 1));
         String artifactId = parts[parts.length - 1];
 
         URL url = new URL(queryService, "select?q="
                 + "g:%22" + encode(groupId, "UTF-8") + "%22+AND+"
                 + "a:%22" + encode(artifactId, "UTF-8") + "%22"
                 + "&core=gav&wt=xml");
         InputStream stream = url.openStream();
         Document doc = docBuilder.parse(stream);
 
         NodeList versionNodes = (NodeList) versionsQueryPath.evaluate(doc, XPathConstants.NODESET);
         List<String> versions = new LinkedList<String>();
         for (int i = 0; i < versionNodes.getLength(); i++) {
             versions.add(versionNodes.item(i).getNodeValue());
         }
        String latestVersion = versions.get(versions.size() - 1);
 
         return new MavenMetadata(groupId, artifactId, latestVersion, versions);
     }
 
     /**
      * Returns the group ID.
      */
     public String getGroupId() {
         return groupId;
     }
 
     /**
      * Returns the artifact ID.
      */
     public String getArtifactId() {
         return artifactId;
     }
 
     /**
      * Returns the latest version of the artifact.
      */
     public String getLatestVersion() {
         return latestVersion;
     }
 
     /**
      * Returns a list of all versions of the artifact.
      */
     public List<String> getVersions() {
         return versions;
     }
 }

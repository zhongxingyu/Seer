 package net.zeroinstall.pom2feed.core;
 
 import java.net.URL;
 import org.junit.*;
 import static org.junit.Assert.*;
 import static com.github.tomakehurst.wiremock.client.WireMock.*;
 import com.github.tomakehurst.wiremock.junit.WireMockRule;
 import static com.google.common.collect.Lists.newArrayList;
import java.io.IOException;
 
 public class MavenMetadataTest {
 
     @Rule
     public WireMockRule wireMockRule = new WireMockRule(8089);
 
     @Test
     public void testLoad() throws Exception {
         stubFor(get(urlEqualTo("/maven-metadata.xml")).willReturn(aResponse().withStatus(200).
                 withBody("<metadata>\n<groupId>group</groupId>\n<artifactId>artifact</artifactId>\n<versioning>\n<latest>1.2</latest>\n<versions>\n<version>1.0</version>\n<version>1.2</version>\n<version>1.1</version>\n</versions>\n</versioning>\n</metadata>")));
 
         MavenMetadata metadata = MavenMetadata.load(new URL("http://localhost:8089/maven-metadata.xml"));
 
         verify(getRequestedFor(urlEqualTo("/maven-metadata.xml")));
         assertEquals("group", metadata.getGroupId());
         assertEquals("artifact", metadata.getArtifactId());
         assertEquals("1.2", metadata.getLatestVersion());
         assertEquals(newArrayList("1.0", "1.2", "1.1"), metadata.getVersions());
     }
 
     @Test
     public void testLoadMissingLatest() throws Exception {
         stubFor(get(urlEqualTo("/maven-metadata.xml")).willReturn(aResponse().withStatus(200).
                 withBody("<metadata>\n<groupId>group</groupId>\n<artifactId>artifact</artifactId>\n<versioning>\n<versions>\n<version>1.0</version>\n<version>1.1</version>\n<version>1.2</version>\n</versions>\n</versioning>\n</metadata>")));
 
         MavenMetadata metadata = MavenMetadata.load(new URL("http://localhost:8089/maven-metadata.xml"));
 
         verify(getRequestedFor(urlEqualTo("/maven-metadata.xml")));
         assertEquals("group", metadata.getGroupId());
         assertEquals("artifact", metadata.getArtifactId());
         assertEquals("1.2", metadata.getLatestVersion());
         assertEquals(newArrayList("1.0", "1.1", "1.2"), metadata.getVersions());
     }
 
     @Test
     public void testQuery() throws Exception {
         stubFor(get(urlEqualTo("/select?q=g:%22group.subgroup%22+AND+a:%22artifact.subartifact%22&core=gav&wt=xml")).willReturn(aResponse().withStatus(200).
                 withBody("<response><lst name=\"responseHeader\"></lst><result name=\"response\">"
                 + "<doc><str name=\"a\">artifact.subartifact</str><str name=\"g\">group.subgroup</str><str name=\"id\">group.subgroup:artifact.subartifact:1.1</str><str name=\"v\">1.1</str></doc>\n"
                 + "<doc><str name=\"a\">artifact.subartifact</str><str name=\"g\">group.subgroup</str><str name=\"id\">group.subgroup:artifact.subartifact:1.0</str><str name=\"v\">1.0</str></doc>\n"
                 + "</result></response>")));
 
         MavenMetadata metadata = MavenMetadata.query(new URL("http://localhost:8089/"), "group/subgroup/artifact.subartifact");
 
         verify(getRequestedFor(urlEqualTo("/select?q=g:%22group.subgroup%22+AND+a:%22artifact.subartifact%22&core=gav&wt=xml")));
         assertEquals("group.subgroup", metadata.getGroupId());
         assertEquals("artifact.subartifact", metadata.getArtifactId());
         assertEquals("1.1", metadata.getLatestVersion());
         assertEquals(newArrayList("1.1", "1.0"), metadata.getVersions());
     }

    @Test(expected = IOException.class)
    public void testQueryEmpty() throws Exception {
        stubFor(get(urlEqualTo("/select?q=g:%22group.subgroup%22+AND+a:%22artifact.subartifact%22&core=gav&wt=xml")).willReturn(aResponse().withStatus(200).
                withBody("<response><lst name=\"responseHeader\"></lst><result name=\"response\">"
                + "</result></response>")));

        MavenMetadata.query(new URL("http://localhost:8089/"), "group/subgroup/artifact.subartifact");
    }
 }

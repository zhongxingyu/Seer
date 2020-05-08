 package com.zenika.dorm.core.test.dao.neo4j;
 
 import com.zenika.dorm.core.dao.neo4j.DormDaoNeo4j;
 import com.zenika.dorm.core.dao.neo4j.Neo4jDependency;
 import com.zenika.dorm.core.dao.neo4j.Neo4jIndex;
 import com.zenika.dorm.core.dao.neo4j.Neo4jMetadata;
 import com.zenika.dorm.core.dao.neo4j.Neo4jMetadataExtension;
 import com.zenika.dorm.core.dao.neo4j.Neo4jRelationship;
 import com.zenika.dorm.core.dao.neo4j.Neo4jResponse;
 import com.zenika.dorm.core.dao.neo4j.util.Neo4jRequestExecutor;
 import com.zenika.dorm.core.dao.neo4j.util.RequestExecutor;
 import com.zenika.dorm.core.graph.Dependency;
 import com.zenika.dorm.core.graph.DependencyNode;
 import com.zenika.dorm.core.model.impl.DefaultDormMetadataExtension;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 import static org.mockito.Mockito.*;
 import static org.junit.Assert.*;
 import static org.fest.assertions.Assertions.*;
 
 /**
  * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 public class Neo4jDaoTest {
 
     private static final Logger LOG = LoggerFactory.getLogger(Neo4jDaoTest.class);
 
     //    private Dependency dependency21Response;
 //    private DependencyNode dependencyNode;
 //    private DormMetadata metadata20Response;
 //    private DormMetadataExtension extension19Response;
 
     private Neo4jDaoTestProvider provider;
 
 
     private RequestExecutor executor;
 
     private DormDaoNeo4j dao;
 
     @Before
     public void setUp() {
         provider = new Neo4jDaoTestProvider();
         executor = mock(Neo4jRequestExecutor.class);
         setUpMethod();
//        dao = new DormDaoNeo4j(executor);
 //        dao.init();
 //        extension19Response = new DefaultDormMetadataExtension("habi-base");
 //        metadata20Response = DefaultDormMetadata.create("0.6", extension19Response);
 //        dependency21Response = DefaultDependency.create(metadata20Response, usage);
 //        dependencyNode = DefaultDependencyNode.create(dependency21Response);
 
     }
 //
 //    @Test
 //    public void push() throws Exception {
 //        dao.push(dependency21Response);
 //    }
 //
 //    @Test
 //    public void pushDependencyNode() {
 //        //dao.push(dependencyNode);
 //    }
 //
 //    @Test
 //    public void testPushWithRecursiveChildren() {
 //        DormMetadataExtension extension19Response;
 //        DormMetadata metadata20Response;
 //        Dependency dependency21Response;
 //        DependencyNode dependencyNode;
 //        List<DependencyNode> dependencies = new ArrayList<DependencyNode>();
 //        for (int i = 0; i < 100; i++) {
 //            extension19Response = new DefaultDormMetadataExtension("maven" + (i));
 //            metadata20Response = DefaultDormMetadata.create("1.0.0", extension19Response);
 //            dependency21Response = DefaultDependency.create(metadata20Response, Usage.create("DEFAULT"));
 //            dependencyNode = DefaultDependencyNode.create(dependency21Response);
 //            dependencies.add(dependencyNode);
 //            if (i != 0) {
 //                dependencies.get(i - 1).addChild(dependencies.get(i));
 //            }
 //        }
 //        long time = System.currentTimeMillis();
 //        dao.push(dependencies.get(0));
 //        System.out.println("Times to standard push : " + (System.currentTimeMillis() - time));
 //    }
 //
 //    @Test
 //    public void testPushWithManyChildren() {
 //        DormMetadataExtension extension19Response = new DefaultDormMetadataExtension("maven");
 //        DormMetadata metadata20Response = DefaultDormMetadata.create("1.0.0", extension19Response);
 //        Dependency dependency21Response = DefaultDependency.create(metadata20Response, Usage.create("DEFAULT"));
 //        DependencyNode dependencyNode = DefaultDependencyNode.create(dependency21Response);
 //        for (int i = 1; i < 100; i++) {
 //            DormMetadataExtension extensionBis = new DefaultDormMetadataExtension("maven" + (i * 10));
 //            DormMetadata metadataBis = DefaultDormMetadata.create("1.0.0", extensionBis);
 //            Dependency dependencyBis = DefaultDependency.create(metadataBis, Usage.create("DEFAULT"));
 //            DependencyNode dependencyNodeBis = DefaultDependencyNode.create(dependencyBis);
 //            dependencyNode.addChild(dependencyNodeBis);
 //        }
 //        long time = System.currentTimeMillis();
 //        dao.push(dependencyNode);
 //        System.out.println("Times to standard push : " + (System.currentTimeMillis() - time));
 //    }
 //
 //    @Test
 //    public void getByMetaData() {
 //        long time = System.currentTimeMillis();
 //        dao.getByMetadata(metadata20Response, usage);
 //        System.out.println("Times to standard push : " + (System.currentTimeMillis() - time));
 //    }
 //
 //    @Test
 //    public void getOtherByMetadata() {
 //        DormMetadataExtension extension19Response = new DefaultDormMetadataExtension("maven");
 //        DormMetadata metadata20Response = DefaultDormMetadata.create("1.0.0", extension19Response);
 //        long time = System.currentTimeMillis();
 //        dao.getByMetadata(metadata20Response, usage);
 //        System.out.println("Times to standard push : " + (System.currentTimeMillis() - time));
 //    }
 //
 //    @Test
 //    public void pushDependencyNodeWithChildren() {
 //        DependencyNode habi_base = createDependencyNode("habi-base", "0.6");
 //        DependencyNode commons_cli = createDependencyNode("commons-cli", "1.0");
 //        DependencyNode xercesImpl = createDependencyNode("xercesImpl", "2.4.0");
 //        DependencyNode commons_logging = createDependencyNode("commons-logging", "1.0.4");
 //        DependencyNode jdom = createDependencyNode("jdom", "1.1");
 //        DependencyNode xalan = createDependencyNode("xalan", "2.7.0");
 //        DependencyNode commons_lang = createDependencyNode("commons-lang", "1.0");
 //        DependencyNode junit = createDependencyNode("junit", "3.8.1");
 //        DependencyNode xml_apis = createDependencyNode("xml-apis", "1.0b2");
 //        habi_base.addChild(commons_cli);
 //        habi_base.addChild(xercesImpl);
 //        habi_base.addChild(commons_logging);
 //        habi_base.addChild(jdom);
 //        habi_base.addChild(junit);
 //        habi_base.addChild(xalan);
 //        commons_cli.addChild(commons_logging);
 //        commons_cli.addChild(commons_lang);
 //        commons_lang.addChild(junit);
 //        xalan.addChild(xml_apis);
 //        System.out.println(habi_base.getDependency().getMetadata().getFullQualifier());
 //        dao.push(habi_base);
 //    }
 //
 //
 //    private DependencyNode createDependencyNode(String name, String version) {
 //        DormMetadataExtension extension19Response = new DefaultDormMetadataExtension(name);
 //        DormMetadata metadata20Response = DefaultDormMetadata.create(version, extension19Response);
 //        Dependency dependency21Response = DefaultDependency.create(metadata20Response, usage);
 //        DependencyNode dependencyNode = DefaultDependencyNode.create(dependency21Response);
 //        return dependencyNode;
 //    }
 
     @Test
     public void testSearchNode() {
         LOG.trace("START testSearchNode");
         try {
             Neo4jDependency dependencyResult = dao.searchNode(provider.getIndexUri(), provider.getListDependencyResponseType().getType());
             verify(executor).get(provider.getIndexUri(), provider.getListDependencyResponseType().getType());
             assertThat(provider.getListDependencyResponse().get(0)).isSameAs(dependencyResult.getResponse());
             assertThat(provider.getListDependencyResponse().get(0).getData()).isSameAs(dependencyResult);
             assertEquals("The Responses doesn't same", provider.getListDependencyResponse().get(0), dependencyResult.getResponse());
         } catch (URISyntaxException e) {
             LOG.error("Bad URI", e);
         }
         LOG.trace("END testSearchNode");
     }
 
     @Test
     public void testFillNeo4jDependency() {
         LOG.trace("START testFillNeo4jDependency");
         try {
             Neo4jDependency dependencyResponse = dao.fillNeo4jDependency(provider.getDependency21Response().getData(), new DefaultDormMetadataExtension("test"));
             assertThat(dependencyResponse.getMetadata()).isSameAs(provider.getMetadata20Response().getData());
             assertThat(dependencyResponse.getMetadata().getNeo4jExtension()).isSameAs(provider.getExtension19Response().getData());
         } catch (URISyntaxException e) {
             LOG.error("Bad URI", e);
         }
         LOG.trace("END testFillNeo4jDependency");
     }
 
     @Test
     public void testGetDependency() {
         LOG.trace("START testGetDependency");
         try {
             Dependency dependency = dao.getDependency(provider.getDependency21Uri(), provider.getUsage(), new DefaultDormMetadataExtension("test"));
             assertThat(dependency).isInstanceOf(Neo4jDependency.class);
             assertThat(dependency.getMetadata()).isSameAs(provider.getMetadata20Response().getData());
             assertThat(dependency.getMetadata().getExtension()).isSameAs(provider.getExtension19Response().getData());
             assertThat(dependency.getUsage()).isSameAs(provider.getUsage());
         } catch (URISyntaxException e) {
             LOG.error("Bad URI", e);
         }
         LOG.trace("END testGetDependency");
     }
 
     @Test
     public void testPutChild() {
         LOG.trace("START testPutChild");
         try {
             Map<String, DependencyNode> map = new HashMap<String, DependencyNode>();
             dao.putChild(provider.getUsage(), map, provider.getRelationships(), new DefaultDormMetadataExtension("test"));
             assertThat(map.get(provider.getDependency21Uri().toString()).getDependency())
                     .isSameAs(provider.getDependency21Response().getData());
             assertThat(map.get(provider.getDependency21Uri().toString()).getDependency().getMetadata())
                     .isSameAs(provider.getMetadata20Response().getData());
             assertThat(map.get(provider.getDependency21Uri().toString()).getDependency().getMetadata().getExtension())
                     .isSameAs(provider.getExtension19Response().getData());
             assertThat(map.get(provider.getDependency3Uri().toString()).getDependency())
                     .isSameAs(provider.getDependency3Response().getData());
             assertThat(map.get(provider.getDependency3Uri().toString()).getDependency().getMetadata())
                     .isSameAs(provider.getMetadata2Response().getData());
             assertThat(map.get(provider.getDependency3Uri().toString()).getDependency().getMetadata().getExtension())
                     .isSameAs(provider.getExtension1Response().getData());
             assertThat(map.get(provider.getDependency21Uri().toString()).getChildren().iterator().next())
                     .isSameAs(map.get(provider.getDependency3Uri().toString()));
         } catch (URISyntaxException e) {
             LOG.error("Bad URI", e);
         }
         LOG.trace("END testPutChild");
     }
 
     @Test
     public void testPostNewDependency() {
         LOG.trace("START testPostDependency");
         try {
             Neo4jDependency dependency = dao.postDependency(provider.getDependency());
             assertThat(dependency.getResponse()).isSameAs(provider.getDependency21Response());
             verify(executor).post(provider.getDependency());
             verify(executor).post(provider.getMetadata());
             verify(executor).post(provider.getExtension());
             verify(executor).post(new Neo4jRelationship(provider.getMetadata20Response().getData(),
                     provider.getExtension19Response().getData(), Neo4jMetadataExtension.RELATIONSHIP_TYPE));
             verify(executor).post(new Neo4jRelationship(provider.getDependency21Response().getData(),
                     provider.getMetadata20Response().getData(), Neo4jMetadata.RELATIONSHIP_TYPE));
         } catch (URISyntaxException e) {
             LOG.error("Bad URI", e);
         }
         LOG.trace("END testPostNewDependency");
     }
 
     @Test
     public void testGetByMetadata(){
         DependencyNode node = dao.getByMetadata(provider.getMetadata(), provider.getUsage());
         assertThat(node.getDependency().getMetadata()).isEqualTo(provider.getMetadata());
         assertThat(node.getDependency().getMetadata().getExtension()).isEqualTo(provider.getExtension());
         assertThat(node.getChildren().iterator().next().getDependency()).isEqualTo(provider.getDependency3Response().getData());
     }
 
     private void setUpMethod() {
         when(executor.<List<Neo4jResponse<Neo4jDependency>>>get(provider.getIndexUri(), provider.getListDependencyResponseType().getType())).thenReturn(provider.getListDependencyResponse());
 
         when(executor.<Neo4jDependency>getNode(provider.getDependency21Uri(), provider.getDependencyResponseType().getType()))
                 .thenReturn(provider.getDependency21Response().getData());
 
         when(executor.<Neo4jDependency>getNode(provider.getDependency3Uri(), provider.getDependencyResponseType().getType()))
                 .thenReturn(provider.getDependency3Response().getData());
 
         when(executor.<Neo4jMetadata>getNode(provider.getMetadata20Uri(), provider.getMetadataResponseType().getType()))
                 .thenReturn(provider.getMetadata20Response().getData());
 
         when(executor.<Neo4jMetadata>getNode(provider.getMetadata2Uri(), provider.getMetadataResponseType().getType()))
                 .thenReturn(provider.getMetadata2Response().getData());
 
         when(executor.<Neo4jMetadataExtension>getNode(provider.getExtension19Uri(), provider.getExtensionResponseType().getType()))
                 .thenReturn(provider.getExtension19Response().getData());
 
         when(executor.<Neo4jMetadataExtension>getNode(provider.getExtension1Uri(), provider.getExtensionResponseType().getType()))
                 .thenReturn(provider.getExtension1Response().getData());
 
         when(executor.<List<Neo4jRelationship>>get(provider.getRelationshipDependency21Uri(), provider.getListRelationshipType().getType()))
                 .thenReturn(provider.getRelationshipsDependency21());
 
         when(executor.<List<Neo4jRelationship>>get(provider.getRelationshipMetadata20Uri(), provider.getListRelationshipType().getType()))
                 .thenReturn(provider.getRelationshipsMetadata20());
 
         when(executor.<List<Neo4jRelationship>>get(provider.getSearchRelationshipDependency21Uri(), provider.getListRelationshipType().getType()))
                 .thenReturn(provider.getRelationshipsDependency21());
 
         when(executor.<List<Neo4jRelationship>>get(provider.getSearchRelationshipMetadata20Uri(), provider.getListRelationshipType().getType()))
                 .thenReturn(provider.getRelationshipsMetadata20());
 
         when(executor.<List<Neo4jRelationship>>get(provider.getRelationshipDependency3Uri(), provider.getListRelationshipType().getType()))
                 .thenReturn(provider.getRelationshipsDependency3());
 
         when(executor.<List<Neo4jRelationship>>get(provider.getRelationshipMetadata2Uri(), provider.getListRelationshipType().getType()))
                 .thenReturn(provider.getRelationshipsMetadata2());
 
         when(executor.<List<Neo4jRelationship>>get(provider.getSearchRelationshipDependency3Uri(), provider.getListRelationshipType().getType()))
                 .thenReturn(provider.getRelationshipsDependency3());
 
         when(executor.<List<Neo4jRelationship>>get(provider.getSearchRelationshipMetadata2Uri(), provider.getListRelationshipType().getType()))
                 .thenReturn(provider.getRelationshipsMetadata2());
 
         when(executor.post(provider.getTraverseUri(), provider.getTraverse()))
                 .thenReturn(provider.getRelationships());
 
         when(executor.get(any(URI.class), eq(List.class))).thenReturn(provider.getEmptyList());
 
         when(executor.post(any(Neo4jIndex.class))).thenReturn(provider.getIndex());
 
         doAnswer(new Answer<Neo4jDependency>() {
             @Override
             public Neo4jDependency answer(InvocationOnMock invocation) throws Throwable {
                 Neo4jDependency dependency = (Neo4jDependency) invocation.getArguments()[0];
                 dependency.setResponse(provider.getDependency21Response());
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
         }).when(executor).post(provider.getDependency());
 
         doAnswer(new Answer<Neo4jMetadataExtension>() {
 
             @Override
             public Neo4jMetadataExtension answer(InvocationOnMock invocation) throws Throwable {
                 Neo4jMetadataExtension extension = (Neo4jMetadataExtension) invocation.getArguments()[0];
                 extension.setResponse(provider.getExtension19Response());
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
         }).when(executor).post(provider.getExtension());
 
         doAnswer(new Answer<Neo4jMetadata>() {
 
             @Override
             public Neo4jMetadata answer(InvocationOnMock invocation) throws Throwable {
                 Neo4jMetadata metadata = (Neo4jMetadata) invocation.getArguments()[0];
                 metadata.setResponse(provider.getMetadata20Response());
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
         }).when(executor).post(provider.getMetadata());
 
     }
 }

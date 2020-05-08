 package org.triple_brain.service.conf;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.assistedinject.FactoryModuleBuilder;
 import com.google.inject.matcher.Matchers;
 import com.google.inject.name.Names;
 import com.google.inject.servlet.GuiceServletContextListener;
 import com.sun.jersey.guice.JerseyServletModule;
 import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
 import org.apache.solr.core.CoreContainer;
import org.triple_brain.service.MessagesDistributorServlet;
import org.triple_brain.service.RestInterceptor;
import org.triple_brain.service.resources.*;
import org.triple_brain.service.resources.test.*;
 import org.triple_brain.module.model.graph.GraphComponentTest;
 import org.triple_brain.module.model.graph.neo4j.Neo4JGraphComponentTest;
 import org.triple_brain.module.neo4j_graph_manipulator.graph.Neo4JModule;
 import org.triple_brain.module.repository_sql.SQLModule;
 import org.triple_brain.module.search.GraphIndexer;
 import org.triple_brain.module.search.GraphSearch;
 import org.triple_brain.module.search.SearchUtils;
 import org.xml.sax.SAXException;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 import javax.ws.rs.Path;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.File;
 import java.io.IOException;
 
 import static com.google.inject.jndi.JndiIntegration.fromJndi;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public class GuiceConfig extends GuiceServletContextListener {
 
     @Override
     protected Injector getInjector() {
         return Guice.createInjector(new JerseyServletModule() {
             @Override
             protected void configureServlets() {
                 bind(Context.class).to(InitialContext.class);
                 RestInterceptor restInterceptor = new RestInterceptor();
                 requestInjection(restInterceptor);
 
                 bindInterceptor(Matchers.any(), Matchers.annotatedWith(Path.class),
                         restInterceptor);
 
                 install(new SQLModule());
 
                 bind(UserResource.class);
                 install(new FactoryModuleBuilder()
                         .build(DrawnGraphResourceFactory.class));
                 install(new FactoryModuleBuilder()
                         .build(GraphResourceFactory.class));
                 install(new FactoryModuleBuilder()
                         .build(VertexResourceFactory.class));
                 install(new FactoryModuleBuilder()
                         .build(EdgeResourceFactory.class));
                 install(new FactoryModuleBuilder()
                         .build(SearchResourceFactory.class));
 
                 serve("/MessageWebSocket").with(MessagesDistributorServlet.class);
 
                serve("/users/*").with(GuiceContainer.class);
 
                 bind(DataSource.class)
                         .annotatedWith(Names.named("nonRdfDb"))
                         .toProvider(fromJndi(DataSource.class, "jdbc/nonRdfTripleBrainDB"));
                 install(new Neo4JModule());
                 try{
                     final InitialContext jndiContext = new InitialContext();
                     String solrHomePath = (String) jndiContext.lookup("solr_home_path");
                     String solrXMLPath = (String) jndiContext.lookup("solr_xml_path_relative_to_home");
                     File solrConfigXml = new File(solrHomePath + solrXMLPath);
                     CoreContainer coreContainer = new CoreContainer(solrHomePath, solrConfigXml);
                     bind(GraphIndexer.class).toInstance(GraphIndexer.withCoreContainer(coreContainer));
                     bind(GraphSearch.class).toInstance(GraphSearch.withCoreContainer(coreContainer));
                     String isTestingStr = (String) jndiContext.lookup("is_testing");
                     Boolean isTesting = "yes".equals(isTestingStr);
                     if(isTesting){
                         bind(ResourceForTests.class);
                         bind(VertexResourceTestUtils.class);
                         bind(EdgeResourceTestUtils.class);
                         bind(GraphResourceTestUtils.class);
                         bind(UserResourceTestUtils.class);
                         bind(SearchUtils.class).toInstance(
                                 SearchUtils.usingCoreCoreContainer(coreContainer)
                         );
 
                         bind(GraphComponentTest.class).toInstance(
                                 new Neo4JGraphComponentTest()
                         );
                     }
                 }catch(NamingException | ParserConfigurationException | IOException | SAXException e){
                     throw new RuntimeException(e);
                 }
 
             }
         });
     }
 }

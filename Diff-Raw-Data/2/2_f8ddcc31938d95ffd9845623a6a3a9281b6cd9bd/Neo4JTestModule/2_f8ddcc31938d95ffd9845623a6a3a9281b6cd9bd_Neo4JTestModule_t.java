 package org.triple_brain.module.model.graph.neo4j;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.TypeLiteral;
 import com.google.inject.assistedinject.FactoryModuleBuilder;
 import org.neo4j.cypher.ExecutionEngine;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.factory.GraphDatabaseSetting;
 import org.neo4j.graphdb.factory.GraphDatabaseSettings;
 import org.neo4j.graphdb.index.ReadableIndex;
 import org.neo4j.test.TestGraphDatabaseFactory;
 import org.triple_brain.module.model.graph.GraphComponentTest;
 import org.triple_brain.module.model.graph.GraphFactory;
 import org.triple_brain.module.neo4j_graph_manipulator.graph.*;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class Neo4JTestModule extends AbstractModule {
 
     @Override
     protected void configure() {
 
         GraphDatabaseService graphDb = new TestGraphDatabaseFactory()
                 .newImpermanentDatabaseBuilder()
                 .setConfig(GraphDatabaseSettings.node_keys_indexable, Neo4JUserGraph.URI_PROPERTY_NAME)
                 .setConfig(GraphDatabaseSettings.node_auto_indexing, GraphDatabaseSetting.TRUE)
                 .setConfig( GraphDatabaseSettings.relationship_keys_indexable, Neo4JUserGraph.URI_PROPERTY_NAME )
                 .setConfig( GraphDatabaseSettings.relationship_auto_indexing, GraphDatabaseSetting.TRUE )
                 .newGraphDatabase();
 
         bind(GraphDatabaseService.class).toInstance(
                 graphDb
         );
         bind(ExecutionEngine.class).toInstance(
                 new ExecutionEngine(graphDb)
         );
 
         FactoryModuleBuilder factoryModuleBuilder = new FactoryModuleBuilder();
 
         install(factoryModuleBuilder
                 .build(Neo4JEdgeFactory.class));
 
         install(factoryModuleBuilder
                 .build(Neo4JUserGraphFactory.class));
 
         install(factoryModuleBuilder
                 .build(Neo4JVertexFactory.class));
 
         install(factoryModuleBuilder
                 .build(Neo4JSubGraphExtractorFactory.class));
 
         bind(GraphComponentTest.class).toInstance(
                 new Neo4JGraphComponentTest()
         );
 
         bind(new TypeLiteral<ReadableIndex<Node>>() {
         }).toInstance(
                 graphDb.index()
                         .getNodeAutoIndexer()
                         .getAutoIndex()
         );
 
         bind(new TypeLiteral<ReadableIndex<Relationship>>() {
         }).toInstance(
                 graphDb.index()
                         .getRelationshipAutoIndexer()
                         .getAutoIndex()
         );
 
         bind(GraphFactory.class).to(Neo4JGraphFactory.class);
 
         requireBinding(SuggestionNeo4JConverter.class);
 
         requireBinding(FriendlyResourceNeo4JUtils.class);
 
        requireBinding(Neo4JExternalResourceUtils.class);

         requireBinding(Neo4JUtils.class);
     }
 }

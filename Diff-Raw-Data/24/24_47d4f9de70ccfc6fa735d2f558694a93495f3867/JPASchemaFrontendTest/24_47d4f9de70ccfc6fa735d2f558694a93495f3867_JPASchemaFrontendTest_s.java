 package org.iti.sqlSchemaComparison.frontends.technologies;
 
 import junit.framework.Assert;
 
 import org.iti.sqlSchemaComparison.edge.ForeignKeyRelationEdge;
 import org.iti.sqlSchemaComparison.frontends.ISqlSchemaFrontend;
 import org.iti.sqlSchemaComparison.vertex.ISqlElement;
 import org.iti.sqlSchemaComparison.vertex.SqlElementFactory;
 import org.iti.sqlSchemaComparison.vertex.SqlElementType;
 import org.jgrapht.Graph;
 import org.jgrapht.graph.DefaultEdge;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 @RunWith(JUnit4.class)
 public class JPASchemaFrontendTest {
 
	private static final String JPA_FILE_PATH = "jpa\\Department.java";
 	private static final String JPA_FOLDER = "jpa";
 	
 	@Before
 	public void setUp() { }
 	
 	@Test
 	public void DatabaseConnectionEstablishedCorrectly() {
 		ISqlSchemaFrontend frontend = new JPASchemaFrontend(JPA_FILE_PATH);
 		Graph<ISqlElement, DefaultEdge> schema = frontend.createSqlSchema();
 		
 		Assert.assertNotNull(schema);
 		Assert.assertEquals(1, SqlElementFactory.getSqlElementsOfType(SqlElementType.Table, schema.vertexSet()).size());
 		Assert.assertEquals(2, SqlElementFactory.getSqlElementsOfType(SqlElementType.Column, schema.vertexSet()).size());
 	}
 	
 	@Test
 	public void DirectoryProcessing() {
 		ISqlSchemaFrontend frontend = new JPASchemaFrontend(JPA_FOLDER);
 		Graph<ISqlElement, DefaultEdge> schema = frontend.createSqlSchema();
 		
 		Assert.assertNotNull(schema);
 		Assert.assertEquals(3, SqlElementFactory.getSqlElementsOfType(SqlElementType.Table, schema.vertexSet()).size());
 		Assert.assertEquals(10, SqlElementFactory.getSqlElementsOfType(SqlElementType.Column, schema.vertexSet()).size());
 		Assert.assertEquals(2, getForeignKeyCount(schema));
 	}
 
 	private int getForeignKeyCount(Graph<ISqlElement, DefaultEdge> schema) {
 		int foreignKeyEdges = 0;
 		
 		for (DefaultEdge edge : schema.edgeSet())
 			if (edge instanceof ForeignKeyRelationEdge)
 				foreignKeyEdges++;
 		
 		return foreignKeyEdges;
 	}
 	
 	@After
 	public void tearDown() { }
 
 }

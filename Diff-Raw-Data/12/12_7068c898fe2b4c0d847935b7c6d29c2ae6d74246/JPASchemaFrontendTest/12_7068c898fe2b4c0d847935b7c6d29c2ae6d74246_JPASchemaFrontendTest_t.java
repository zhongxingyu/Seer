 package org.iti.sqlSchemaComparison.frontends.technologies;
 
 import static org.junit.Assert.*;
 
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
 
 	private static final String JPA_FILE_PATH = "jpa//Department.java";
 	private static final String JPA_FOLDER = "jpa";
 	
 	@Before
 	public void setUp() { }
 	
 	@Test
 	public void DatabaseConnectionEstablishedCorrectly() {
 		ISqlSchemaFrontend frontend = new JPASchemaFrontend(JPA_FILE_PATH);
 		Graph<ISqlElement, DefaultEdge> schema = frontend.createSqlSchema();
 		
 		assertNotNull(schema);
		ISqlElement[] tables = SqlElementFactory.getSqlElementsOfType(SqlElementType.Table, schema.vertexSet()).toArray(new ISqlElement[] {});
		
		assertEquals(1, tables.length);
		assertEquals("departments", tables[0].getSqlElementId());
 		assertEquals(2, SqlElementFactory.getSqlElementsOfType(SqlElementType.Column, schema.vertexSet()).size());
 	}
 	
 	@Test
 	public void DirectoryProcessing() {
 		ISqlSchemaFrontend frontend = new JPASchemaFrontend(JPA_FOLDER);
 		Graph<ISqlElement, DefaultEdge> schema = frontend.createSqlSchema();
 		
 		assertNotNull(schema);
 		assertEquals(3, SqlElementFactory.getSqlElementsOfType(SqlElementType.Table, schema.vertexSet()).size());
 		assertEquals(10, SqlElementFactory.getSqlElementsOfType(SqlElementType.Column, schema.vertexSet()).size());
 		assertEquals(2, getForeignKeyCount(schema));
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

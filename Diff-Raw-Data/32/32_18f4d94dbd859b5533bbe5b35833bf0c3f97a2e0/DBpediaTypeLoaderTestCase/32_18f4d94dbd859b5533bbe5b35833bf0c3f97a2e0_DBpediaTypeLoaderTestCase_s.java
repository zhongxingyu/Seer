 package dbpedia.utils;
 
 import static org.junit.Assert.*;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import db.TestDatabaseSameThatWikipediaDatabaseException;
 import db.WikipediaConnector;
 
 public class DBpediaTypeLoaderTestCase {
 	
 	private Connection testConnection;
 	private String typesTableName;
 	private String fakettl;
 	
 	@Before
 	public void setUp() throws ClassNotFoundException, SQLException, TestDatabaseSameThatWikipediaDatabaseException, FileNotFoundException, IOException{
 		this.testConnection=WikipediaConnector.getTestConnection();
 		this.typesTableName="dbTypesLocal";
 		this.fakettl="test/fakettl.ttl";
 		WikipediaConnector.restoreTestDatabase();
 		
 	}
 	
 
 	@Test
 	public void testLoad() throws ForbidenTableNameException, SQLException, IOException {
 		Set<String> expected = new HashSet<String>();
 		expected.add("<http://dbpedia.org/ontology/Disease>");
 		expected.add("<http://www.w3.org/2002/07/owl#Thing>");
 		
 		DBpediaTypeLoader.load(this.testConnection,this.typesTableName,this.fakettl);
 		
 		List<String> types = DBpediaTypeLoader.getTypes("Autism",this.testConnection,this.typesTableName);
 		Set<String> result = new HashSet<String>();
 		result.addAll(types);
 		
 		assertEquals(expected, result);
 	}
 	
 	@Test
 	public void testLoadForbidenNamePage() throws SQLException, IOException{	
 		try {
 			DBpediaTypeLoader.load(this.testConnection,"page","fakettl.ttl");
 			fail("Does not throw ForbidenTableNameException!");
 		} catch (ForbidenTableNameException e) {
 			// TODO Auto-generated catch block
 			
 		} 
 		
 	}
 	
 	@Test
 	public void testLoadForbidenNameCategory() throws SQLException, IOException{	
 		try {
 			DBpediaTypeLoader.load(this.testConnection,"category","fakettl.ttl");
 			fail("Does not throw ForbidenTableNameException!");
 		} catch (ForbidenTableNameException e) {
 			// TODO Auto-generated catch block
 			
 		} 
 		
 	}
 	
 	@Test
 	public void testLoadForbidenNamePagelinks() throws SQLException, IOException{	
 		try {
 			DBpediaTypeLoader.load(this.testConnection,"pagelinks","fakettl.ttl");
 			fail("Does not throw ForbidenTableNameException!");
 		} catch (ForbidenTableNameException e) {
 			// TODO Auto-generated catch block
 			
 		} 
 		
 	}
 	
 	@Test
 	public void testLoadForbidenNameCategoryLinks() throws SQLException, IOException{	
 		try {
 			DBpediaTypeLoader.load(this.testConnection,"categorylinks","fakettl.ttl");
 			fail("Does not throw ForbidenTableNameException!");
 		} catch (ForbidenTableNameException e) {
 			// TODO Auto-generated catch block
 		} 
 	}
 	
 
 }

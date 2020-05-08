 package megal.test;
 
 import static org.junit.Assert.*;
 
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 import java.util.Set;
 
 import megal.model.RDecl;
 import megal.model.RTypeDecl;
 import megal.relationships.Relationship;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.reflections.Reflections;
 
 public class ReflectionTest {
 	
 	List<megal.Runtime.Relationship> coreRels;
 	List<megal.Runtime.Relationship> customRels;
 	
 	@Before
 	public void setUp() throws Exception {
 		megal.Runtime runtime = new megal.Runtime();
 		
 		coreRels = runtime.getWeakRels();
 		customRels = runtime.getCustomRels();
 	}
 	
 	@Test
 	public void findAllRuntimeRelationships(){
 		assertSame(3, coreRels.size());
		assertSame(1, customRels.size());
 		
 		assertSame(false, customRels.get(0).isWeak());
 	}	
 	
 	@Test
 	public void getCoreForCustomRelationship(){
 		megal.Runtime.Relationship rel = customRels.get(0);
 		megal.Runtime.Relationship core = rel.getCore();
 		assertSame(true, core.isWeak());
 	}
 	
 	@Test
 	public void getRDeclForCustomRelationship(){
 		megal.Runtime.Relationship rel = customRels.get(0);
 		RTypeDecl rDecl = rel.toRTypeDecl();
 		
 		String name = rDecl.getName();
 		assertEquals("elementOf", name);
 		
 		String left = rDecl.getLeft();
 		assertEquals("File", left);
 		
 		String right = rDecl.getRight();
 		assertEquals("Language", right);
 		
 		assertEquals(false, rDecl.isCore());
 	}
 }

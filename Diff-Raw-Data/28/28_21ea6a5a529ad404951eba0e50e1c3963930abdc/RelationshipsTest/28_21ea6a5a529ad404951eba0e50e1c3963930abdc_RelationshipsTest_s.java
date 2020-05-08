 package megal.test;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 import java.util.Map.Entry;
 
 import megal.Context;
 import megal.Tool;
 import megal.model.Decl;
 import megal.model.RDecl;
 import megal.model.RTypeDecl;
 import megal.relationships.FileElementOfLanguage;
 import megal.relationships.Relationship;
 import megal.trivia.Pair;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class RelationshipsTest extends BaseTest {
 	
 	@Before
 	public void setUp() {
 		super.setUp();
 		String input = getResorucePath("/models/java.megal");
 		Tool.parse(input);
 	}
 	
 	@Test
 	public void customRelationshipsShouldBeAddedToTheGraph(){
 		List<RTypeDecl> customRels =  Context.runtime.getCustomRDecls();
		assertEquals(1, customRels.size());	
 		
 		RTypeDecl fileElementOfLanguage = customRels.get(0);
 		assertEquals("elementOf", fileElementOfLanguage.getName());
 		assertEquals("Language", fileElementOfLanguage.getRight());
 		assertEquals("File", fileElementOfLanguage.getLeft());	
 	}
 	
 	@Test
 	public void allRelationshipsFromPreludeShouldBeAddedToTheGraphBeforeBootstrap(){
 		Tool.analyze();
 		
 		assertEquals(7, Context.rTypeDecls.size());
 	}
 	
 	@Test
 	public void allCustomRelationshipsShouldBeAddedToTheGraphAfterBootstrap(){
 		Tool.analyze();
 		Tool.extend();
 		Tool.analyze();
 		
 		System.out.println("All relationship types:");
 		for (Entry<String, List<Pair<String, String>>> entry : Context.rTypeDecls.entrySet()) {
 			String key = entry.getKey();
 		    System.out.println("Key = " + key + ", Value = " + entry.getValue());
		    if (key.equals("elementOf")){
		    	// we have a custom relationships, so there should be 2 relationships for elementOf
 				assertEquals(2, entry.getValue().size());	
 		    }
 		    else{
		    	assertEquals(1, entry.getValue().size());	
 		    }
 		}
 	}
 	
 	@Test
 	public void existingCustomRelationsihpShouldBeProperlyResolved(){
 		Tool.analyze();
 		Tool.extend();
 		Tool.analyze();
 				
 		for(Decl d: Context.model.getDecls()){
 			if (d instanceof RDecl){
 				assertEquals("elementOf", ((RDecl)d).getRel().getName()); 
 				Relationship<?,?> rel = ((RDecl)d).getRelationship();
 				assertTrue(rel instanceof FileElementOfLanguage);
 			}
 		}
 	}
 }

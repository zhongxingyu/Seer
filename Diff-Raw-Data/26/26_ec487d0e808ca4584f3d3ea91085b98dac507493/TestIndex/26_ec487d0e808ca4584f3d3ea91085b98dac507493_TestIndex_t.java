 package net.ion.radon.repository.advance;
 
 import net.ion.radon.repository.Explain;
 import net.ion.radon.repository.Node;
import net.ion.radon.repository.NodeCursor;
 import net.ion.radon.repository.TestBaseRepository;
 
import com.mongodb.BasicDBObject;

 public class TestIndex extends TestBaseRepository{
 
 	public void testLazyConfirmPath() throws Exception {
 		session.newNode("bleujin").put("name", "bleujin") ;
 		session.commit() ;
 		
 		Node node = session.createQuery().findByPath("/bleujin") ;
 		Explain exp = session.getAttribute(Explain.class.getCanonicalName(), Explain.class) ;
 		assertEquals(true, exp.useIndex()) ;
 	}
 	
 	public void testConfirmIndex() throws Exception {
 		session.newNode("bleujin").put("nmae", "bleujin") ;
 		session.commit() ;
 	}
	
	public void testCreateCompoundIndex() throws Exception {
		session.newNode().inner("name").put("firstname", "bleu").put("lastname", "jin").getParent().put("age", 20) ;
		session.commit() ;

		session.getCurrentWorkspace().getCollection().ensureIndex(new BasicDBObject("name.firstname", -1)) ;
		
		NodeCursor nc = session.createQuery().eq("name.firstname", "bleu").find() ;
		assertEquals(true, nc.explain().useIndex()) ;
	}
 }

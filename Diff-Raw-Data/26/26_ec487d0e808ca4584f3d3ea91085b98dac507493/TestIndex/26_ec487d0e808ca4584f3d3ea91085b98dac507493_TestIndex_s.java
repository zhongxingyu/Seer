 package net.ion.radon.repository.advance;
 
import static net.ion.radon.repository.NodeConstants.PATH;
import net.ion.framework.util.Debug;
import net.ion.radon.core.PageBean;
 import net.ion.radon.repository.Explain;
 import net.ion.radon.repository.Node;
 import net.ion.radon.repository.TestBaseRepository;
 
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
 }

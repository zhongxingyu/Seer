 /**
  * 
  */
 package models.dbentities;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import javax.persistence.PersistenceException;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.avaje.ebean.Ebean;
 
 import test.ContextTest;
 
 /**
  * @author Jens N. Rammant
  *
  */
 public class ClassPupilTest extends ContextTest {
 	
 	@Before
 	public void clear(){	
 		List<ClassPupil> cp = Ebean.find(ClassPupil.class).findList();
 		for(ClassPupil c:cp)c.delete();
 	}
 
 	/**
 	 * Test method for {@link play.db.ebean.Model#save()}.
 	 */
 	@Test
 	public void testSave() {
 		ClassPupil cp = new ClassPupil();
 		cp.indid="a";
 		cp.classid="b";
 		
 		try{
 			cp.save();
 		}catch(PersistenceException p){
 			Assert.fail(p.toString());
 		}
 	}
 	
 	//TODO: make this test work
	//@Test(expected=PersistenceException.class)
 	public void testDuplicateSave() {
 		ClassPupil cp = new ClassPupil();
 		cp.indid="a";
 		cp.classid="b";
 		
 		ClassPupil cp2 = new ClassPupil();
 		cp2.indid="a";
 		cp2.classid="b";
 		
 		cp.save();
 		cp2.save();
 	}
 
 }

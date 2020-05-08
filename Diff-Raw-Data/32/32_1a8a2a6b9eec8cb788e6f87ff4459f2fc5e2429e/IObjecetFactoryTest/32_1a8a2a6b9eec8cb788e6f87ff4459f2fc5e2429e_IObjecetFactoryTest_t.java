 package se.chalmers.kangaroo.model.iobject;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import se.chalmers.kangaroo.constants.Constants;
 
 public class IObjecetFactoryTest {
 
 
 	@Test
 	public void iObjectTest(){
 		for(int i = 0; i < 200; i++){
			if( Constants.IOBJECTS_IDS.contains(" "+i+" ")){
 				assertTrue(IObjectFactory.createIObjects(i, 10, 10, null) != null);
 			}else{
 				assertTrue(IObjectFactory.createIObjects(i, 10, 10, null) == null);
 			}
 		}
 	}
 
 }

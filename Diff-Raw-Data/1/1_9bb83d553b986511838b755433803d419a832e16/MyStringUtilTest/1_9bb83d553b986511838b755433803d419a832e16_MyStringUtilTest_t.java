 package nico.util.string;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ngandriau
  * Date: 16/08/12
  * Time: 1:44 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MyStringUtilTest {
 
 
     @Test
     public void testWhenBlank(){
         assertTrue("should detect that string is blank", MyStringUtil.islank("   "));
         assertTrue("should detect that string is blank", MyStringUtil.islank(""));
        assertFalse("should detect that string is blank", MyStringUtil.islank("issue3"));
     }
 }

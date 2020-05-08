 import org.junit.Test;
 import src.Diamond;
 
 import static junit.framework.Assert.assertEquals;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class DiamondTest {
 
     @Test
     public void shouldMakeDiamondStringWithCenterCountOfOne(){
         assertThat(new Diamond(1).print(), is("*\n"));
     }
 
     @Test
     public void shouldMakeDiamondStringWithCenterCountOfThree(){
         assertEquals(new Diamond(3).print(), " * \n***\n * \n");
     }
 
     @Test
     public void shouldMakeDiamondStringWithMultipleNonCenterIncrements(){
        System.out.println(new Diamond(5).print());
         assertThat(new Diamond(5).print(), is("   *   \n *** \n*****\n *** \n   *   \n"));
     }
 }

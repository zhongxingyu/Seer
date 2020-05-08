 package test;
 
 import main.Plateau;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class PlateauTest {
    Plateau plateau=Plateau.getInstance(5,5);
     @Test
     public void shouldCheckYLimit(){
         assertThat(Plateau.checkYLimit(6), is(false));
         assertThat(Plateau.checkYLimit(-1), is(false));
         assertThat(Plateau.checkYLimit(4), is(true));
     }
 
     @Test
     public void shouldCheckXLimit(){
         assertThat(Plateau.checkXLimit(-1),is(false));
         assertThat(Plateau.checkXLimit(6),is(false));
         assertThat(Plateau.checkXLimit(3),is(true));
     }
 
 }

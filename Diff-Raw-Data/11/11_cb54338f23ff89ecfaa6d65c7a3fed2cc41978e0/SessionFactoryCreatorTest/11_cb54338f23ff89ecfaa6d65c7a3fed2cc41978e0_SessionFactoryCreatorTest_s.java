 package br.ime.usp.commendans.components;
 
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

 import org.junit.Test;
 
import br.com.caelum.vraptor.environment.Environment;

 public class SessionFactoryCreatorTest {
 
     @Test
     public void shouldGetSessionFactory() {
        Environment env = mock(Environment.class);
        when(env.getName()).thenReturn("heroku");
        SessionFactoryCreator sfc = new SessionFactoryCreator(env);
         sfc.create();
     }
 
 }

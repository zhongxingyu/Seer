 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import models.Context;
 
 import org.codehaus.jackson.JsonNode;
 import org.junit.*;
 
 import play.mvc.*;
 import play.test.*;
 import play.data.DynamicForm;
 import play.data.validation.ValidationError;
 import play.data.validation.Constraints.RequiredValidator;
 import play.i18n.Lang;
 import play.libs.F;
 import play.libs.F.*;
 import static play.test.Helpers.*;
 import static org.fest.assertions.Assertions.*;
 
 
 /**
 *
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
 public class ContextTest {
 
     @Test 
     public void getContextTest() {
         List<Context> c = Context.all();
         assertThat(c).isNotEmpty();
         Context x = Context.findbyPerson(new Long(5));
         assertThat(x).isNotNull();
     }
    
  
    
 }

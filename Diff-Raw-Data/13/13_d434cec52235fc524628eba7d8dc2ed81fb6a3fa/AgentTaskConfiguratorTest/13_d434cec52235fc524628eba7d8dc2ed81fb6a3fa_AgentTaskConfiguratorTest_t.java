package ut.org.whitesource.bamboo.plugins;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
import org.whitesource.bamboo.plugins.AgentTaskConfigurator;
 
 public class AgentTaskConfiguratorTest extends TestCase
 {
     private AgentTaskConfigurator configurator;
 
     @Override
     protected void setUp() throws Exception
     {
         super.setUp();
         configurator = new AgentTaskConfigurator();
     }
 
     @Test
     public void testProjectTokenIsNull()
     {
         final Map<String, Object> context = new HashMap<String, Object>();
         assertThat(context).isEmpty();
         configurator.populateContextForCreate(context);
         final String token = (String) context.get(AgentTaskConfigurator.PROJECT_TOKEN);
         assertThat(token).isNull();
     }
 }

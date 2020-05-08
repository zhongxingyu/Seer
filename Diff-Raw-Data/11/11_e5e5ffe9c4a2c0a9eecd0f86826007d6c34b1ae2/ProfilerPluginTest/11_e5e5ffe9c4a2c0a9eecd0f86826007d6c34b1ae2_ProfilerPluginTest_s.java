 package org.sonar.plugins.profiler;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class ProfilerPluginTest {
   private ProfilerPlugin plugin;
 
   @Before
   public void setUp() {
     plugin = new ProfilerPlugin();
   }
 
   @Test
   public void test() throws Exception {
     assertThat(plugin.getKey(), notNullValue());
     assertThat(plugin.getName(), notNullValue());
     assertThat(plugin.getExtensions().size(), is(7));
   }
 }

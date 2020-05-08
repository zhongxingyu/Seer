 package org.sonar.plugins.profiler;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.api.resources.Project;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class ProfilerDecoratorTest {
   private ProfilerDecorator decorator;
 
   @Before
   public void setUp() throws Exception {
     decorator = new ProfilerDecorator();
   }
 
   @Test
   public void testGeneratesMetrics() throws Exception {
     assertThat(decorator.generatesMetrics().size(), is(1));
   }
 
   @Test
   public void testShouldExecuteOnProject() throws Exception {
     Project project = mock(Project.class);
 
     assertThat(decorator.shouldExecuteOnProject(project), is(true));
   }
 
   @Test
   public void testToString() throws Exception {
     assertThat(decorator.toString(), is("ProfilerDecorator"));
   }
 }

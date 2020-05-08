 package org.suggs.test.sandbox.jbehave.support;
 
 import org.junit.Test;
 
 import static org.hamcrest.Matchers.greaterThan;
 import static org.junit.Assert.assertThat;
 import static org.suggs.test.sandbox.jbehave.support.ClasspathStoryFinder.findStories;
 
 public class ClasspathStoryFinderTest {
 
     @Test
     public void shouldFindTestJavaFileFromClasspath() {
        assertThat(findStories("**/*.xml").size(), greaterThan(1));
     }
 }

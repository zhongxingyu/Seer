 package org.suggs.test.sandbox.jbehave.support;
 
 import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
 
 import static org.hamcrest.Matchers.greaterThan;
 import static org.junit.Assert.assertThat;
 import static org.suggs.test.sandbox.jbehave.support.ClasspathStoryFinder.findStories;
 
 public class ClasspathStoryFinderTest {
 
    private static final Logger LOG = LoggerFactory.getLogger(ClasspathStoryFinderTest.class);

     @Test
     public void shouldFindTestJavaFileFromClasspath() {
        List<String> stories = findStories("**/*.xml");
        LOG.debug("Found the following {} stories {}", stories.size(), stories);
        assertThat(stories.size(), greaterThan(0));
     }
 }

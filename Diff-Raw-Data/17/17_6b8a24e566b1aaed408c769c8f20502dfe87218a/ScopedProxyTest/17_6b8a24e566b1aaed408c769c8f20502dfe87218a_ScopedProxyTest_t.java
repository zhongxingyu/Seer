 package org.rosenvold.spring.convention;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
import org.rosenvold.spring.convention.scope.MockSessionScopeContextLoader;
import org.rosenvold.spring.convention.testclasses.DefaultScopedProxy;
 import org.rosenvold.spring.convention.testclasses.ScopedProxy;
 import org.rosenvold.spring.convention.testclasses.TestCandidateEvaluator;
 import org.rosenvold.spring.convention.testclasses.TestnameToClassResolver;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertNotNull;
 
 /**
  * @author Kristian Rosenvold
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {
         "classpath:applicationContext-empty.xml"
}, loader = MockSessionScopeContextLoader.class)
 
 @ConventionConfiguration(candidateEvaluator = TestCandidateEvaluator.class, nameToClassResolver = TestnameToClassResolver.class)
 public class ScopedProxyTest {
 
     @SuppressWarnings({"SpringJavaAutowiringInspection"})
     @Autowired
     ScopedProxy scopedProxy;
 
     @Test
     public void testLoadContext()
             throws Exception {
         assertNotNull( scopedProxy);
        assertFalse(scopedProxy instanceof DefaultScopedProxy);
     }
 }

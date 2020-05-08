 package stories;
 
 import jcriteria.framework.Criteria;
 import jcriteria.framework.JCriteriaRunner;
 import org.junit.Assert;
 import org.junit.runner.RunWith;
 
 @RunWith(JCriteriaRunner.class)
 public class Example {
 
     @Criteria("Should run criteria")
     public void shouldRunCriteria() {
         Assert.assertTrue(true);
     }
 
     @Criteria("Should not run criteria")
     public void shouldNotRunCriteria() {
         Assert.assertTrue(true);
     }
 
     @Criteria("Run and fail")
     public void shouldRunAndFail() {
        //Assert.fail("Criteria failed");
     }
 }

 package org.tuml.tinker.constraint;
 
 import org.junit.Test;
import org.neo4j.graphdb.TransactionFailureException;
 import org.tuml.constraints.ConstraintRoot;
 import org.tuml.runtime.test.BaseLocalDbTest;
 
 /**
  * Date: 2013/03/10
  * Time: 2:05 PM
  */
 public class ConstrainedClassTest extends BaseLocalDbTest {
 
    @Test(expected = TransactionFailureException.class)
     public void testClassConstraintFail() {
        ConstraintRoot constraintRoot = new ConstraintRoot(true);
        constraintRoot.setName("constraintRootX");
        db.commit();
     }
 
     @Test
     public void testClassConstraintPass() {
         ConstraintRoot constraintRoot = new ConstraintRoot(true);
         constraintRoot.setName("constraintRoot");
         db.commit();
     }
 
 }

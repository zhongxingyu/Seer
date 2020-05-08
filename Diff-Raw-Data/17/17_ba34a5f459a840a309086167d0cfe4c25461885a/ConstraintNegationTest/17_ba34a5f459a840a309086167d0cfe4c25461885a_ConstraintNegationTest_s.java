 package pl.ais.commons.bean.validation;
 
import static org.hamcrest.MatcherAssert.assertThat;

 import org.junit.BeforeClass;
 import org.junit.Test;
 
import pl.ais.commons.bean.validation.constraints.BaseConstraint;
 import pl.ais.commons.domain.specification.Specifications;
 
 /**
  * Verifies the behavior of {@link Constraint} negation created using {@link Constraints#not(Constraint)} method.
  *
  * @author Warlock, AIS.PL
  * @since 1.0.1
  */
 public class ConstraintNegationTest {
 
     private static Constraint<Object> constraint;
 
     /**
      * Initializes the constraint instance.
      */
     @BeforeClass
     public static void beforeAll() {
        constraint = Constraints.not(new BaseConstraint<>("anyValue", Specifications.always()));
     }
 
     /**
      * Verifies if the specification is satisfied by null value.
      */
     @Test
     public void shouldntBeSatisfiedByAnyValue() {
        final ConstrainableValue<String> constrainable = new ConstrainableValue<>("");
        assertThat("Constraint shouldn't be satisfied by any value.", false == constraint.isSatisfiedBy(constrainable));
     }
 }

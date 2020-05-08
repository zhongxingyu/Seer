 package pl.ais.commons.bean.validation;
 
 import org.junit.BeforeClass;
import org.junit.Ignore;
 import org.junit.Test;
 
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
        constraint = Constraints.not(new Constraint<>("anyValue", Specifications.always()));
     }
 
     /**
      * Verifies if the specification is satisfied by null value.
      */
    @Ignore
     @Test
     public void shouldntBeSatisfiedByAnyValue() {
        try (final ValidationContext context = ValidationContext.validationOf("")) {
            // assertThat("Constraint shouldn't be satisfied by any value.", !constraint.isSatisfiedBy(Integer.valueOf(0)));
        }
     }
 }

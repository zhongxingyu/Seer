 package pl.softmil.validator.sumUpTo.impl;
 
 import java.math.BigDecimal;
 import java.util.Collection;
 
 import javax.validation.*;
 
 import pl.softmil.validator.sumUpTo.constraint.SumUpTo;
 
 public class SumUpToValidator implements
         ConstraintValidator<SumUpTo, Collection<? extends AsBigDecimal>> {
     private BigDecimal targetSum;
 
     @Override
     public void initialize(SumUpTo constraintAnnotation) {
         targetSum = new BigDecimal(constraintAnnotation.targetSum());
     }
 
     @Override
     public boolean isValid(Collection<? extends AsBigDecimal> value,
             ConstraintValidatorContext context) {
        BigDecimal sum = BigDecimal.ZERO;
         for (AsBigDecimal asBigDecimal : value) {
             sum = sum.add(asBigDecimal.asBigDecimal());
         }
         return sumEqualTarget(sum);
     }
 
     private boolean sumEqualTarget(BigDecimal sum) {
         return targetSum.compareTo(sum) == 0;
     }
 
 }

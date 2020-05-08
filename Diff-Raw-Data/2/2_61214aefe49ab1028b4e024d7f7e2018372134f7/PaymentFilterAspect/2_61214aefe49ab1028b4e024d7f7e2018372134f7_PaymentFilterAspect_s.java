 package fr.cg95.cvq.service.payment.aspect;
 
 import java.util.Set;
 
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.springframework.core.Ordered;
 
 import fr.cg95.cvq.business.payment.Payment;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.payment.annotation.PaymentFilter;
 import fr.cg95.cvq.util.Critere;
 
 /**
  * @author jsb@zenexity.fr
  */
 @Aspect
 public class PaymentFilterAspect implements Ordered {
 
     @SuppressWarnings("unchecked")
    @Before("fr.cg95.cvq.SystemArchitecture.businessService() && @annotation(paymentFilter) && within(fr.cg95.cvq.payment..*)")
     public void contextAnnotatedMethod(JoinPoint joinPoint, PaymentFilter paymentFilter) {
         Object[] arguments = joinPoint.getArgs();
         Set<Critere> criteriaSet = (Set<Critere>) arguments[0];
         if (SecurityContext.isFrontOfficeContext()) {
             criteriaSet.add(new Critere(Payment.SEARCH_BY_HOME_FOLDER_ID,
                 SecurityContext.getCurrentEcitizen().getHomeFolder().getId(),
                 Critere.EQUALS));
         }
     }
 
     public int getOrder() {
         return 2;
     }
 }

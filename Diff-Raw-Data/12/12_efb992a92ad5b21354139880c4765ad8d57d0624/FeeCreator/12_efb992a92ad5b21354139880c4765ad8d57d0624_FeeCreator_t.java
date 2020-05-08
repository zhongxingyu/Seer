 package de.hh.changeRing.transaction;
 
 import com.google.common.collect.Maps;
 import de.hh.changeRing.Context;
 import de.hh.changeRing.user.Member;
 import de.hh.changeRing.user.SystemAccount;
 import de.hh.changeRing.user.User;
 import de.hh.changeRing.user.UserUpdateEvent;
 import org.joda.time.DateMidnight;
 
import javax.ejb.Stateful;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.Map;
 
 import static java.math.BigDecimal.ZERO;
 import static java.math.RoundingMode.HALF_UP;
 
 @Named
 @SessionScoped
@Stateful
public class FeeCreator implements Serializable{
     public static final BigDecimal TAX_AMOUNT = new BigDecimal("2.00");
     public static final BigDecimal DEMURRAGE_PERCENT = new BigDecimal("0.02");
     @PersistenceContext
     private EntityManager entityManager;
 
     @Inject
     private Event<UserUpdateEvent> events;
 
     public FeeCalculationResult previewTax() {
         return calculateTax();
     }
 
     private FeeCalculationResult calculateTax() {
         FeeCalculationResult calculationResult = new FeeCalculationResult();
         List<Member> relevantMembers = entityManager.createNamedQuery("allMembers", Member.class).getResultList();
         for (Member member : relevantMembers) {
             calculationResult.userAmounts.put(member, TAX_AMOUNT);
         }
         return calculationResult;
     }
 
     public FeeCalculationResult previewDemurrage() {
         return calculateDemurage();
     }
 
     private FeeCalculationResult calculateDemurage() {
         FeeCalculationResult calculationResult = new FeeCalculationResult();
         List<Member> relevantMembers = entityManager.createNamedQuery("allMembers", Member.class).getResultList();
         for (Member member : relevantMembers) {
             if (member.getBalance().compareTo(ZERO) > 0) {
                 BigDecimal amount = member.getBalance().multiply(DEMURRAGE_PERCENT).setScale(2, HALF_UP);
                 calculationResult.userAmounts.put(member, amount);
             }
         }
         return calculationResult;
     }
 
     private SystemAccount getSystem() {
         return SystemAccount.getSystem(entityManager);
     }
 
     private void executeTax() {
         FeeCalculationResult tax = calculateTax();
         for (Map.Entry<User, BigDecimal> entry : tax.userAmounts.entrySet()) {
             Transaction.create(entry.getKey(), getSystem(), entry.getValue(), String.format("Fixe Gebühr von %s Motten für %s", entry.getValue(), Context.formatGermanDate(new DateMidnight())));
         }
         events.fire(new UserUpdateEvent(tax.userAmounts.keySet()));
     }
 
     private void executeDemurage() {
         FeeCalculationResult demurage = calculateDemurage();
         for (Map.Entry<User, BigDecimal> entry : demurage.userAmounts.entrySet()) {
             Transaction.create(entry.getKey(), getSystem(), entry.getValue(), String.format("Umlaufsicherung für %s, %s %% von %s ergibt: %s Motten",
                     Context.formatGermanDate(new DateMidnight()),
                     DEMURRAGE_PERCENT.multiply(new BigDecimal("100")).setScale(0, HALF_UP),
                     entry.getKey().getBalance(),
                     entry.getValue()));
         }
     }
 
     public void executeFees() {
         executeDemurage();
         executeTax();
     }
 
     public static class FeeCalculationResult {
         Map<User, BigDecimal> userAmounts = Maps.newHashMap();
 
         public BigDecimal getTotalAmount() {
             BigDecimal result = new BigDecimal("0.00");
             for (User user : userAmounts.keySet()) {
                 result = result.add(userAmounts.get(user));
             }
             return result;
         }
     }
 
 }

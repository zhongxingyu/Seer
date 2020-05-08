 package io.business.processes;
 
 import io.business.Product;
 import io.business.Reason;
 import io.business.conditions.*;
 import io.business.properties.Name;
 import io.business.properties.State;
 import io.business.properties.Type;
 import io.business.results.*;
 
 /**
  * This is just a helper class which creates predefined business processes.
  * @author zerodi
  */
 public class BusinessProcessHelper {
 
     /**
      * If the payment is for a physical product, generate a packing slip for shipping.
      */
     public static BusinessProcess firstProcess() {
         return new BusinessProcess()
                 .withConditions(new IsPhysical(true))
                 .withResults(new PackingSlip());
     }
 
     /**
      * If the payment is for a book, create a duplicate packing slip for the royalty department.
      */
     public static BusinessProcess secondProcess() {
         return new BusinessProcess()
                 .withConditions(new IsType("book"))
                .withResults(new PackingSlip("Royalty Department"));
     }
 
     /**
      * If the payment is for a membership, activate that membership.
      */
     public static BusinessProcess thirdProcess() {
         return new BusinessProcess()
                 .withConditions(
                         new IsType("Membership"),
                         new PaymentHasReason(Reason.PAYMENT))
                 .withResults(new ChangeState(new State("ACTIVE")));
     }
 
     /**
      * If the payment is an upgrade to a membership, apply the upgrade.
      */
     public static BusinessProcess fourthProcess() {
         return new BusinessProcess()
                 .withConditions(
                         new IsType("Membership"),
                         new HasState(new State("ACTIVE")),
                         new PaymentHasReason(Reason.UPGRADE))
                 .withResults(new ChangeState(new State("UPGRADED")));
     }
 
     /**
      * If the payment is for a membership, e-mail the owner and inform them of the activation
      */
     public static BusinessProcess fifthProcessA() {
         return new BusinessProcess()
                 .withConditions(
                         new IsType("Membership"),
                         new HasState(new State("INACTIVE")),
                         new PaymentHasReason(Reason.PAYMENT))
                 .withResults(
                         new ChangeState(new State("ACTIVE")),
                         new Email("Membership activated."));
     }
 
     /**
      * If the payment is for a membership upgrade, e-mail the owner and inform them of the upgrade.
      */
     public static BusinessProcess fifthProcessB() {
         return new BusinessProcess()
                 .withConditions(
                         new IsType("Membership"),
                         new HasState(new State("ACTIVE")),
                         new PaymentHasReason(Reason.UPGRADE))
                 .withResults(
                         new ChangeState(new State("UPGRADED")),
                         new Email("Membership upgraded."));
     }
 
 
     /**
      * If the payment is for the video "Learning to Ski,"
      * add a free "First Aid" video to the packing slip (the result of a court decision in 1997).
      */
     public static BusinessProcess sixthProcess() {
         return new BusinessProcess()
                 .withConditions(
                         new IsType("Video"),
                         new HasName("Learning To Ski"))
                 .withResults(
                         new AddProduct(
                                 new Product().withProperties(
                                         new Type("Video"),
                                         new Name("First Aid")
                                 )));
     }
 
     /**
      * If the payment is for a physical product, generate a commission payment to the agent.
      */
     public static BusinessProcess seventhProcessA() {
         return new BusinessProcess()
                 .withConditions(
                         new IsPhysical(true),
                         new PaymentHasReason(Reason.PAYMENT))
                 .withResults(
                         new GenerateExtraPayment("Agent", Reason.COMMISSION));
     }
 
     /**
      * If the payment is for a book, generate a commission payment to the agent.
      */
      public static BusinessProcess seventhProcessB() {
         return new BusinessProcess()
                 .withConditions(
                         new IsType("book"),
                         new PaymentHasReason(Reason.PAYMENT))
                 .withResults(
                         new GenerateExtraPayment("Agent", Reason.COMMISSION));
     }
 }

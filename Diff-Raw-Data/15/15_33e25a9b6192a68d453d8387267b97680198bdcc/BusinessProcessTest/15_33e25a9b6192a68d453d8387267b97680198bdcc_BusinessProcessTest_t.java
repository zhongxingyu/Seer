 package io.business;
 
 import com.beust.jcommander.internal.Lists;
 import io.business.conditions.*;
 import io.business.processes.BusinessProcessHelper;
 import io.business.properties.Name;
 import io.business.properties.Physical;
 import io.business.properties.State;
 import io.business.properties.Type;
 import io.business.results.*;
 import io.business.processes.BusinessProcess;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 
 /**
  * @author zerodi
  */
 public class BusinessProcessTest {
     private BusinessProcess businessProcess;
     private Product product;
 
 
     @BeforeMethod
     public void setUp() {
         businessProcess = new BusinessProcess();
         product = new Product();
     }
 
     @Test
     public void newBusinessProcessHasNoConditions() {
         // then
         assertThat(businessProcess.getConditions()).isEmpty();
     }
 
     @Test
     public void newBusinessProcessHasNoResults() {
         // then
         assertThat(businessProcess.getResults()).isEmpty();
     }
 
     @Test
     public void addingConditionAndARuleShouldBeReportedProperly() throws Exception {
         // given
         businessProcess = BusinessProcessHelper.firstProcess();
         List<Condition> businessProcessConditions = Lists.newArrayList(businessProcess.getConditions());
         List<Result> businessProcessResults = Lists.newArrayList(businessProcess.getResults());
 
         // then
         assertThat(businessProcessConditions).hasSize(1);
         assertThat(businessProcessConditions.get(0)).isInstanceOf(IsPhysical.class);
         assertThat(businessProcessResults).hasSize(1);
         assertThat(businessProcessResults.get(0)).isInstanceOf(PackingSlip.class);
     }
 
     @Test
     public void blankProcessDoesntProduceAnyResultsAfterProcessing() throws Exception {
         // then
         assertThat(businessProcess.process(new Payment(new Product()))).hasSize(0);
     }
 
 
     @Test
     public void processCheckingPhysicalPropertiesProducesAPackingSlip() {
         // given
         businessProcess = BusinessProcessHelper.firstProcess();
         product.addProperty(new Physical(true));
 
         // when
         Collection<Result> processingResult = businessProcess.process(new Payment(product));
 
         // then
         assertThat(processingResult).hasSize(1);
         assertThat(processingResult.iterator().next()).isInstanceOf(PackingSlip.class);
     }
 
     @Test
     public void paymentForABookShouldCreatePackingSlipForRoyaltyDepartment() throws Exception {
         // given
         businessProcess = BusinessProcessHelper.secondProcess();
         product.addProperty(new Type("book"));
 
         // when
         ArrayList<Result> results = new ArrayList<>();
         results.addAll(businessProcess.process(new Payment(product)));
 
         // then
        assertThat(results).hasSize(2);
         assertThat(results.get(0)).isInstanceOf(PackingSlip.class);
        assertThat(results.get(1)).isInstanceOf(PackingSlip.class);
        assertThat(((PackingSlip)results.get(1)).getDepartment()).isEqualToIgnoringCase("Royalty Department");
     }
 
     @Test
     public void paymentForAMembershipShouldActiveThatMembership() {
         // given
         businessProcess = BusinessProcessHelper.thirdProcess();
         product.addProperty(new Type("Membership"));
         Payment payment = new Payment(product, Reason.PAYMENT);
 
         // when
         businessProcess.process(payment);
 
         // then
         assertThat(product.getProperty(State.class)).isEqualTo(new State("ACTIVE"));
     }
 
 
     @Test
     public void paymentForActiveMembershipShouldUpgradeTheMembership() {
         // given
         businessProcess = BusinessProcessHelper.fourthProcess();
         Payment payment = new Payment(
                 product.withProperties(
                         new Type("Membership"),
                         new State("ACTIVE")), Reason.UPGRADE);
 
         // when
         businessProcess.process(payment);
 
         // then
         assertThat(product.getProperty(State.class)).isEqualTo(new State("UPGRADED"));
     }
 
 
     @Test
     public void paymentForAMembershipShouldGenerateAnEmailAboutTheActivation() {
         // given
         businessProcess = BusinessProcessHelper.fifthProcessA();
         product.addProperty(new Type("Membership"));
         product.addProperty(new State("INACTIVE"));
         Payment payment = new Payment(product, Reason.PAYMENT);
 
         // when
         ArrayList <Result> results = new ArrayList<>();
         results.addAll(businessProcess.process(payment));
 
         // then
         assertThat(results).hasSize(2);
         assertThat(results.get(0)).isInstanceOf(ChangeState.class);
         assertThat(((ChangeState)results.get(0)).getTargetState()).isEqualTo(new State("ACTIVE"));
         assertThat(results.get(1)).isInstanceOf(Email.class);
         assertThat(((Email)results.get(1)).getMessage()).containsIgnoringCase("Membership activated.");
     }
 
     @Test
     public void paymentForAMembershipUpgradeShouldGenerateAnEmail() {
         // given
         businessProcess = BusinessProcessHelper.fifthProcessB();
         product.addProperty(new Type("Membership"));
         product.addProperty(new State("ACTIVE"));
         Payment payment = new Payment(product, Reason.UPGRADE);
 
         // when
         ArrayList <Result> results = new ArrayList<>();
         results.addAll(businessProcess.process(payment));
 
         // then
         assertThat(results).hasSize(2);
         assertThat(results.get(0)).isInstanceOf(ChangeState.class);
         assertThat(((ChangeState)results.get(0)).getTargetState()).isEqualTo(new State("UPGRADED"));
         assertThat(results.get(1)).isInstanceOf(Email.class);
         assertThat(((Email)results.get(1)).getMessage()).containsIgnoringCase("Membership upgraded.");
     }
 
     @Test
     public void payingForLearningToSkiVideoShouldAddFirstAidVideoToPackingSlip() throws Exception {
         // given
         businessProcess = BusinessProcessHelper.sixthProcess();
         product.addProperty(new Type("Video"));
         product.addProperty(new Name("Learning To Ski"));
         product.addProperty(new Physical(true));
         Payment payment = new Payment(product, Reason.PAYMENT);
 
         // when
         ArrayList<Result> resultArrayList = new ArrayList<>();
         resultArrayList.addAll(businessProcess.process(payment));
 
         // then
         assertThat(resultArrayList.get(0)).isInstanceOf(AddProduct.class);
         assertThat(((AddProduct)resultArrayList.get(0)).getProductToAdd().getProperty(Type.class).getType())
                 .isEqualTo("Video");
         assertThat(((AddProduct)resultArrayList.get(0)).getProductToAdd().getProperty(Name.class).getName())
                 .isEqualTo("First Aid");
     }
 
     @Test
     public void paymentForAPhysicalProductShouldGenerateCommissionPayment() {
         // given
         businessProcess = BusinessProcessHelper.seventhProcessA();
         product.addProperty(new Physical(true));
         Payment payment = new Payment(product, Reason.PAYMENT);
 
         // when
         ArrayList<Result> resultArrayList = new ArrayList<>();
         resultArrayList.addAll(businessProcess.process(payment));
 
         // then
         assertThat(resultArrayList.get(0)).isInstanceOf(GenerateExtraPayment.class);
         assertThat(((GenerateExtraPayment)resultArrayList.get(0)).getPaymentReason()).isEqualTo(Reason.COMMISSION);
         assertThat(((GenerateExtraPayment)resultArrayList.get(0)).getReceiver()).isEqualTo("Agent");
     }
 
     @Test
     public void paymentForABookShouldGenerateCommissionToAnAgent() {
         // given
         businessProcess = BusinessProcessHelper.seventhProcessB();
         product.addProperty(new Type("book"));
         Payment payment = new Payment(product, Reason.PAYMENT);
 
         // when
         ArrayList<Result> resultArrayList = new ArrayList<>();
         resultArrayList.addAll(businessProcess.process(payment));
 
         // then
         assertThat(resultArrayList.get(0)).isInstanceOf(GenerateExtraPayment.class);
         assertThat(((GenerateExtraPayment)resultArrayList.get(0)).getPaymentReason()).isEqualTo(Reason.COMMISSION);
         assertThat(((GenerateExtraPayment)resultArrayList.get(0)).getReceiver()).isEqualTo("Agent");
     }
 
 
 
 }

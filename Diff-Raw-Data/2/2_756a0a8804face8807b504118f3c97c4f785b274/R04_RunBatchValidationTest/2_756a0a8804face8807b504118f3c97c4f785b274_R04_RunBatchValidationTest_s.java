 package mu.validation.run;
 
 import static mu.validation.utils.DateUtils.midnight;
 
 import mu.validation.domain.Contract;
 import mu.validation.domain.ContractBuilder;
 import mu.validation.service.ValidationService;
 import org.mockito.MockitoAnnotations;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 @ContextConfiguration(locations = "commonRun-context.xml")
 public class R04_RunBatchValidationTest extends AbstractTestNGSpringContextTests {
 
 	@Autowired
 	private ValidationService validationService;
 
 	private Contract contract;
 
 	@BeforeMethod
 	public void setUp() {
 		MockitoAnnotations.initMocks(this);
 		contract = ContractBuilder
 				.builderWithStartDate()
 				.withZenonPerson()
 				.withApproveDate(midnight(2013,6,6)).build();
 	}
 
 	@Test
 	public void shouldValidateForBatch() {
 		contract.sign();
 		contract.approve();
 
		validationService.validateContract(contract);
 
 	}
 
 
 }

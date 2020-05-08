 package org.mifos.loan.service;
 
 import org.apache.log4j.Logger;
 import org.mifos.client.service.StandardClientServiceTest;
 import org.mifos.loan.domain.LoanProductStatus;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.Errors;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.Validator;
 import org.springmodules.validation.bean.BeanValidator;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 @ContextConfiguration(locations={"classpath:unitTestContext.xml"})
 @Test(groups = { "unit" })
 public class LoanProductDtoValidationTest  extends AbstractTestNGSpringContextTests{
 	
     private static final Logger logger = Logger.getLogger(LoanProductDtoValidationTest.class);
 
 	private LoanProductDto loanProductDto;
 	
 	@Autowired
 	private Validator validator;
 	
 	@BeforeMethod
 	public void setup() {
 		loanProductDto = validLoanProductDto();
 	}
 	
 	public void testValidInputs() {
 		Assert.assertEquals(0, getErrors().getErrorCount());
 	}
 
 	public void testBlankLongName () {
 		loanProductDto.setLongName("");
 		assertFieldError ("longName", "not.blank");
 	}
 
 	public void testNullLongName () {
 		loanProductDto.setLongName(null);
 		assertFieldError ("longName", "not.null");
 	}
 
 	public void testNullMinInterestRate () {
 		loanProductDto.setMinInterestRate(null);
 		assertFieldError ("minInterestRate", "not.null");
 	}
 
 	public void testNegativeMinInterestRate () {
 		loanProductDto.setMinInterestRate(-1.1);
 		assertFieldError("minInterestRate", "min");
 	}
 	
 	public void testNullMaxInterestRate () {
 		loanProductDto.setMaxInterestRate(null);
 		assertFieldError ("maxInterestRate", "not.null");
 	}
 	
 	public void testNegativeMaxInterestRate () {
 		loanProductDto.setMaxInterestRate(-1.1);
 		assertFieldError("maxInterestRate", "min");
 	}
 
 	public void testNullStatus () {
 		loanProductDto.setStatus(null);
 		assertFieldError ("status", "not.null");
 	}
 	
	public void testMinInterestRate () {
		loanProductDto.setMinInterestRate(5.0);
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(loanProductDto, "loanProduct");
		validator.validate(loanProductDto, errors);
		logger.info(errors);
		Assert.assertTrue(errors.getErrorCount() > 0, "Expected errors but got none.");
	}
	
	
 	@Autowired
     @Test(enabled = false)
 	public void setValidator(Validator validator) {
 		this.validator = validator;
 	}
 
 	private LoanProductDto validLoanProductDto() {
 		loanProductDto = new LoanProductDto();
 		loanProductDto.setLongName("long");
 		loanProductDto.setShortName("short");
 		loanProductDto.setMinInterestRate(1.1);
 		loanProductDto.setMaxInterestRate(2.2);
 		loanProductDto.setStatus(LoanProductStatus.ACTIVE);
 		return loanProductDto;
 	}
 	
 	private void assertFieldError (String fieldName, String errorMessage) {
 		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(loanProductDto, "loanProduct");
 		validator.validate(loanProductDto, errors);
 		Assert.assertTrue(errors.getErrorCount() > 0, "Expected errors but got none.");
 		FieldError fieldError = errors.getFieldError(fieldName);
 		Assert.assertNotNull(fieldError, "Expected error on field " + fieldName + ", but got none");
 		Assert.assertEquals(fieldError.getDefaultMessage().toString(), errorMessage, "Incorrect validation error message.");
 	}
 
 	private Errors getErrors(){
 		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(loanProductDto, "loanProduct");
 		validator.validate(loanProductDto, errors);
 		return errors;
 
 	}
 }

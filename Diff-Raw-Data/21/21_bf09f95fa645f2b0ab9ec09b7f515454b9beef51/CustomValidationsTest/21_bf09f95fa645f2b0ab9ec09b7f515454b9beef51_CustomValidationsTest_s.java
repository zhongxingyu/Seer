 /**
  * @author Brad Leege <leege@doit.wisc.edu>
  * Created on 4/25/13 at 3:31 PM
  */
 package org.jasig.portlet.blackboardvcportlet.validations.test;
 
 import org.jasig.portlet.blackboardvcportlet.service.SessionForm;
 import org.joda.time.DateMidnight;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import java.util.Set;
 import static org.junit.Assert.assertEquals;
 
 public class CustomValidationsTest
 {
 	private static Logger logger = LoggerFactory.getLogger(CustomValidationsTest.class);
 	private static Validator validator;
 
 	@Before
 	public void setUp() throws Exception
 	{
 		validator = Validation.buildDefaultValidatorFactory().getValidator();
 	}
 
 	/**
 	 * Tests the QuarterHourCheck
 	 * The StartMinute should pass, while the EndMinute should trigger a violation.
 	 */
 	@Test
	@Ignore
 	public void testQuarterHourCheck()
 	{
 		logger.info("testQuarterHourCheck() started....");
 		SessionForm sessionForm = new SessionForm();
 		sessionForm.setSessionName("Test Session");
 		sessionForm.setBoundaryTime(15);
		sessionForm.setStartDate(DateMidnight.now());
		sessionForm.setStartHour(20);
 		sessionForm.setStartMinute(15);
		sessionForm.setEndDate(DateMidnight.now());
		sessionForm.setEndHour(21);
 		sessionForm.setEndMinute(35);
 
 		Set<ConstraintViolation<SessionForm>> constraintViolations = validator.validate(sessionForm);
 		assertEquals(1, constraintViolations.size());
 		assertEquals("endMinute", constraintViolations.iterator().next().getPropertyPath().toString());
 		logger.info("testQuarterHourCheck() finished.");
 	}
 
 	/**
 	 * Tests theq SessionEndTimeRangeCheck
 	 * The first attempt should pass all constraint checks, while the second attempt
 	 * should trigger an out of range violation.
 	 * @throws Exception
 	 */
 	@Test
	@Ignore
 	public void testSessionEndTimeRangeCheck() throws Exception
 	{
 		logger.info("testSessionEndTimeRangeCheck() started....");
 		SessionForm sessionForm = new SessionForm();
 		sessionForm.setSessionName("Test Session");
 		sessionForm.setBoundaryTime(15);
		sessionForm.setStartDate(DateMidnight.now());
 		sessionForm.setStartHour(20);
 		sessionForm.setStartMinute(15);
		sessionForm.setEndDate(DateMidnight.now());
 		sessionForm.setEndHour(21);
 		sessionForm.setEndMinute(45);
 
 		Set<ConstraintViolation<SessionForm>> constraintViolations = validator.validate(sessionForm);
 		assertEquals(0, constraintViolations.size());
 
 		sessionForm.setEndDate(DateMidnight.now().plusDays(2));
 		constraintViolations = validator.validate(sessionForm);
 		assertEquals(1, constraintViolations.size());
 
 		logger.info("testSessionEndTimeRangeCheck() finished.");
 	}
 }

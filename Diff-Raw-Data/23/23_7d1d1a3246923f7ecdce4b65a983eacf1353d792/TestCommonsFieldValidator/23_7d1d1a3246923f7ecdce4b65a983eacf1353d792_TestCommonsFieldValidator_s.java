 package org.megatome.frame2.validator;
 
 import java.util.Iterator;
 import java.util.Locale;
 
 import org.apache.commons.validator.Validator;
 import org.apache.commons.validator.ValidatorException;
 import org.apache.commons.validator.ValidatorResources;
 import org.apache.commons.validator.ValidatorResults;
 import org.megatome.frame2.Globals;
 import org.megatome.frame2.errors.Error;
 import org.megatome.frame2.errors.Errors;
 import org.megatome.frame2.errors.TestError;
 import org.megatome.frame2.util.ResourceLocator;
 
 import servletunit.frame2.MockFrame2TestCase;
 
 /**
  * @author hmilligan
  *
  * To change this generated comment edit the template variable "typecomment":
  * Window>Preferences>Java>Templates.
  * To enable and disable the creation of type comments go to
  * Window>Preferences>Java>Code Generation.
  */
 public class TestCommonsFieldValidator extends MockFrame2TestCase {
 
    ValidatorResources _validatorResources;
    static String DEF_FILE_PATH = "/org/megatome/frame2/validator/config";
    static String DEF_MAPPINGS_FILE = "test-all-mappings.xml";
    static String DEF_RULES_FILE = "test-all-rules.xml";
    static int DEF_ERRORS_SIZE = 1;
    // There is always one default error to verify error object passed in is not
    // corrupted by the validation methods.
    static String EMAIL_BEAN_NAME = "testEmailFieldBean";
    static String REQUIRED_BEAN_NAME = "testRequiredFieldBean";
    static String MASK_BEAN_NAME = "testMaskFieldBean";
    static String BYTE_BEAN_NAME = "testByteFieldBean";
    static String SHORT_BEAN_NAME = "testShortFieldBean";
    static String INT_BEAN_NAME = "testIntegerFieldBean";
    static String LONG_BEAN_NAME = "testLongFieldBean";
    static String FLOAT_BEAN_NAME = "testFloatFieldBean";
    static String DOUBLE_BEAN_NAME = "testDoubleFieldBean";
    static String DATE_BEAN_NAME = "testDateFieldBean";
    static String STRICT_DATE_BEAN_NAME = "testStrictDateFieldBean";
    static String INT_RANGE_BEAN_NAME = "testIntRangeFieldBean";
    static String FLOAT_RANGE_BEAN_NAME = "testFloatRangeFieldBean";
    static String DOUBLE_RANGE_BEAN_NAME = "testDoubleRangeFieldBean";
    static String CREDIT_CARD_BEAN_NAME = "testCreditCardFieldBean";
    static String MIN_LEN_BEAN_NAME = "testMinLengthFieldBean";
    static String MAX_LEN_BEAN_NAME = "testMaxLengthFieldBean";
    static final String TWO_FIELD_BEAN_NAME = "testTwoFieldBean";
    static final String REQUIRED_IF_NULL_BEAN_NAME = "testRequiredIfNullBean";
    static final String REQUIRED_IF_NOT_NULL_BEAN_NAME = "testRequiredIfNotNullBean";
    static final String REQUIRED_IF_EQUAL_BEAN_NAME = "testRequiredIfEqualBean";
 
    /**
     * Constructor for TestCommonsFieldValidator.
     * @param name
     */
    public TestCommonsFieldValidator(String name) {
       super(name);
    }
 
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
       super.setUp();
       _validatorResources = buildValidatorResources();
       ResourceLocator.setBasename("frame2-resource");
    }
 
    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
       super.tearDown();
       _validatorResources = null;
    }
 
    private ValidatorResources buildValidatorResources() {
       return buildValidatorResources(
          DEF_FILE_PATH,
          DEF_MAPPINGS_FILE,
          DEF_RULES_FILE);
    }
 
    private ValidatorResources buildValidatorResources(
       String filePath,
       String mappingsFile,
       String rulesFile) {
       ValidatorResources resources = null;
       try {
          CommonsValidatorWrapper.setFilePath(filePath);
          CommonsValidatorWrapper.setMappingsFile(mappingsFile);
          CommonsValidatorWrapper.setRulesFile(rulesFile);
          CommonsValidatorWrapper.load(getContext());
          resources = CommonsValidatorWrapper.getValidatorResources();
          assertNotNull(resources);
       } catch (CommonsValidatorException e) {
          fail();
       }
       return resources;
    }
 
    private boolean setupEmailTest(String emailAddr) {
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setEmail(emailAddr);
 
       testBean.setEmpty("dude");
 
       Errors errors = setupAndRunValidate(testBean, EMAIL_BEAN_NAME);
 
       return validateErrors(errors);
 
    }
 
 
    private boolean validateErrors(Errors errors) {
       return validateErrors(
          errors,
          TestError.PREPEND_MSG,
          TestError.FRAME2_MSG,
          null,       
          "tag.question");
    }
    
    private boolean validateErrors(Errors errors, String msg, String errorKey ) {
       return validateErrors(
          errors,
          TestError.PREPEND_MSG,
          msg,
          null,       
          errorKey);
    }
 
    private boolean validateErrors(
       Errors errors,
       String prependMsg,
       String msg,
       String appendMsg,
       String errorKey) {
       Iterator iter = errors.iterator();
 
       boolean foundValidationlError = false;
       boolean foundSeedError = false;
       while (iter.hasNext()) {
          Error error = (Error) iter.next();
          if (error.getKey().equals("seed")) {
             foundSeedError = true;
          }
          if (error.getKey().equals(errorKey)) {
             String errorMsg = error.getMessage(Locale.US);
             String expectedMsg = "";
             if (prependMsg != null) {
                expectedMsg += prependMsg;
             }
             if (msg != null) {
                expectedMsg += msg;
             }
             if (appendMsg != null) {
                appendMsg += appendMsg;
             }
 
             assertEquals(errorMsg, expectedMsg);
             foundValidationlError = true;
          }
       }
 
       assertTrue(foundSeedError);
 
       return foundValidationlError;
    }
 
    private Errors setupAndRunValidate(
       CommonsValidatorBean testBean,
       String fieldBeanName) {
       // Add an error to verify the error is passed in..
      Errors errors = new Errors();
       errors.add("seed", "dude");
 
       Validator validator = new Validator(_validatorResources, fieldBeanName);
       // add the name bean to the validator as a resource
       // for the validations to be performed on.
       validator.addResource(Validator.BEAN_KEY, testBean);
       validator.addResource(Globals.ERRORS_KEY, errors);
 
       // Get results of the validation.
       ValidatorResults results = null;
 
       try {
          results = validator.validate();
       } catch (ValidatorException e) {
          fail();
       }
       return errors;
    }
 
    public void testEmail() {
       assertFalse(setupEmailTest("Yabba@hotmail.com"));
    }
 
    public void testNegativeEmail() {
       assertTrue(setupEmailTest("Yahotmail.com"));
    }
 
    public void testNullEmailNotRequired() {
       _validatorResources = buildValidatorResources();
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setEmpty("dude");
 
       Errors errors = setupAndRunValidate(testBean, EMAIL_BEAN_NAME);
       assertEquals(DEF_ERRORS_SIZE, errors.size());
 
    }
 
    public void testRequired() {
 
       _validatorResources = buildValidatorResources();
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setRequired("required");
 
       Errors errors = setupAndRunValidate(testBean, REQUIRED_BEAN_NAME);
       assertEquals(DEF_ERRORS_SIZE, errors.size());
    }
 
    public void testNegativeRequired() {
 
       _validatorResources = buildValidatorResources();
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
 
       Errors errors = setupAndRunValidate(testBean, REQUIRED_BEAN_NAME);
 
       assertTrue(validateErrors(errors));
    }
 
    public void testMask() {
 
       _validatorResources = buildValidatorResources();
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setPhone("303-123-1234");
 
       Errors errors = setupAndRunValidate(testBean, MASK_BEAN_NAME);
       assertEquals(DEF_ERRORS_SIZE, errors.size());
    }
 
    public void testNegativeMask() {
 
       _validatorResources = buildValidatorResources();
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setPhone("303-123");
 
       Errors errors = setupAndRunValidate(testBean, MASK_BEAN_NAME);
 
       assertTrue(validateErrors(errors));
    }
 
    public void testNullPhone() {
 
       _validatorResources = buildValidatorResources();
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
 
       Errors errors = setupAndRunValidate(testBean, MASK_BEAN_NAME);
 
       assertEquals(DEF_ERRORS_SIZE, errors.size());
    }
 
    private void runTest(CommonsValidatorBean testBean, String beanName) {
       _validatorResources = buildValidatorResources();
 
       Errors errors = setupAndRunValidate(testBean, beanName);
       assertEquals(DEF_ERRORS_SIZE, errors.size());
    }
 
    private void runTestNegative(
       CommonsValidatorBean testBean,
       String beanName) {
       _validatorResources = buildValidatorResources();
       Errors errors = setupAndRunValidate(testBean, beanName);
       assertTrue(validateErrors(errors));
    }
    
    private void runTestNegative(
       CommonsValidatorBean testBean,
       String beanName, String overRideDefErrorMessage, String errorKey) {
       _validatorResources = buildValidatorResources();
       Errors errors = setupAndRunValidate(testBean, beanName);
       assertTrue(validateErrors(errors, overRideDefErrorMessage, errorKey));
    }
 
    public void testByte() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setByte("12");
 
       runTest(testBean, BYTE_BEAN_NAME);
    }
 
    public void testNegativeByte() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setByte("30323");
       runTestNegative(testBean, BYTE_BEAN_NAME);
    }
 
    public void testNullByte() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, BYTE_BEAN_NAME);
    }
 
    public void testShort() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setShort("12");
       runTest(testBean, SHORT_BEAN_NAME);
 
    }
 
    public void testNegativeShort() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setShort("30323123454");
 
       runTestNegative(testBean, SHORT_BEAN_NAME);
 
    }
 
    public void testNullShort() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, SHORT_BEAN_NAME);
 
    }
 
    public void testInteger() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setInteger("12");
       runTest(testBean, INT_BEAN_NAME);
 
    }
 
    public void testNegativeInteger() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setInteger("A");
 
       runTestNegative(testBean, INT_BEAN_NAME);
 
    }
 
    public void testNullInteger() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, INT_BEAN_NAME);
 
    }
 
    public void testLong() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setLong("12");
       runTest(testBean, LONG_BEAN_NAME);
 
    }
 
    public void testNegativeLong() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setLong("A");
 
       runTestNegative(testBean, LONG_BEAN_NAME);
 
    }
 
    public void testNullLong() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, LONG_BEAN_NAME);
 
    }
 
    public void testFloat() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setFloat("12");
       runTest(testBean, FLOAT_BEAN_NAME);
 
    }
 
    public void testNegativeFloat() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setFloat("A");
 
       runTestNegative(testBean, FLOAT_BEAN_NAME);
 
    }
 
    public void testNullFloat() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, FLOAT_BEAN_NAME);
 
    }
 
    public void testDouble() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setDouble("12");
       runTest(testBean, DOUBLE_BEAN_NAME);
 
    }
 
    public void testNegativeDouble() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setDouble("A");
 
       runTestNegative(testBean, DOUBLE_BEAN_NAME);
 
    }
 
    public void testNullDouble() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, DOUBLE_BEAN_NAME);
 
    }
 
    public void testDate() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setDate("2/22/1999");
       runTest(testBean, DATE_BEAN_NAME);
    }
 
    public void testNegativeDate() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setDate("A");
 
       runTestNegative(testBean, DATE_BEAN_NAME);
 
    }
 
    public void testNullDate() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, DATE_BEAN_NAME);
 
    }
 
    public void testStrictDate() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setDate("02/22/1999");
       runTest(testBean, STRICT_DATE_BEAN_NAME);
    }
 
    public void testNegativeStrictDate() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setDate("A");
 
       runTestNegative(testBean, STRICT_DATE_BEAN_NAME);
 
    }
 
    public void testNullStrictDate() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, STRICT_DATE_BEAN_NAME);
 
    }
 
    public void testIntRange() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setInteger("15");
       runTest(testBean, INT_RANGE_BEAN_NAME);
    }
 
    public void testNegativeIntRange() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setInteger("30");
 
       runTestNegative(testBean, INT_RANGE_BEAN_NAME,"Is this 5 Frame2?20", "tag.question.with.two.parms");
 
    }
 
    public void testNullIntRange() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, INT_RANGE_BEAN_NAME);
 
    }
 
    public void testFloatRange() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setFloat("2.37");
       runTest(testBean, FLOAT_RANGE_BEAN_NAME);
    }
 
    public void testNegativeFloatRange() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setFloat("155.00");
 
       runTestNegative(testBean, FLOAT_RANGE_BEAN_NAME,"Is this -1.00 Frame2?37.88", "tag.question.with.two.parms");
 
    }
 
    public void testNullFloatRange() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, FLOAT_RANGE_BEAN_NAME);
 
    }
 
    public void testDoubleRange() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setDouble("2.37");
       runTest(testBean, DOUBLE_RANGE_BEAN_NAME);
    }
 
    public void testNegativeDoubleRange() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setDouble("155.00");
 
       runTestNegative(testBean, DOUBLE_RANGE_BEAN_NAME,"Is this -1.00 Frame2?37.88", "tag.question.with.two.parms");
 
    }
 
    public void testNullDoubleRange() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, DOUBLE_RANGE_BEAN_NAME);
 
    }
 
    public void testCreditCard() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();    
       testBean.setCreditCard("4111111111111111");
       runTest(testBean, CREDIT_CARD_BEAN_NAME);
    }
 
    public void testNegativeCreditCard() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setCreditCard("1234-1234-1234-12");
 
       runTestNegative(testBean, CREDIT_CARD_BEAN_NAME);
 
    }
 
    public void testNullCreditCard() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, CREDIT_CARD_BEAN_NAME);
 
    }
    
    
    public void testMinLength() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setMinLength("1234");
       runTest(testBean, MIN_LEN_BEAN_NAME);
    }
 
    public void testNegativeMinLength() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setMinLength("12");
       
       runTestNegative(testBean, MIN_LEN_BEAN_NAME, "Is this 3 Frame2?", "tag.question.with.parm");    
    }
 
    public void testNullMinLength() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, MIN_LEN_BEAN_NAME);
 
    }
    
    public void testMaxLength() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setMaxLength("123");
       runTest(testBean, MAX_LEN_BEAN_NAME);
    }
 
    public void testNegativeMaxLength() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setMaxLength("dudasdfdsfsadfdsfsdafsdf");
 
       runTestNegative(testBean, MAX_LEN_BEAN_NAME, "Is this 5 Frame2?", "tag.question.with.parm");
 
    }
 
    public void testNullMaxLength() {
 
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, MAX_LEN_BEAN_NAME);
 
    }
    
    public void testTwoFields() {
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setTwoField1("value");
       testBean.setTwoField2("value");
       runTest(testBean, TWO_FIELD_BEAN_NAME);
    }
    
    public void testNegativeTwoFields() {
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setTwoField1("value");
       testBean.setTwoField2("not_value");
       runTestNegative(testBean, TWO_FIELD_BEAN_NAME, "Is this append Frame2?", "tag.question.with.parm");
    }
    
    public void testNullTwoFields() {
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTest(testBean, TWO_FIELD_BEAN_NAME);
    }
    
    public void testRequiredIfNull() {
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setRequired("value");
       runTest(testBean, REQUIRED_IF_NULL_BEAN_NAME);
       
       testBean.setTwoField1("something");
       testBean.setRequired(null);
       runTest(testBean, REQUIRED_IF_NULL_BEAN_NAME);
    }
    
    public void testNegativeRequiredIfNull() {
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       runTestNegative(testBean, REQUIRED_IF_NULL_BEAN_NAME, "Is this Frame2?", "tag.question");
    }
    
    public void testRequiredIfNotNull() {
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setTwoField1("Something");
       testBean.setRequired("value");
       runTest(testBean, REQUIRED_IF_NOT_NULL_BEAN_NAME);
       
       testBean.setTwoField1(null);
       testBean.setRequired(null);
       runTest(testBean, REQUIRED_IF_NOT_NULL_BEAN_NAME);
    }
    
    public void testNegativeRequiredIfNotNull() {
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setTwoField1("Something");
       runTestNegative(testBean, REQUIRED_IF_NOT_NULL_BEAN_NAME, "Is this Frame2?", "tag.question");
    }
    
    public void testRequiredIfEqual() {
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setTwoField1("value");
       testBean.setRequired("value");
       runTest(testBean, REQUIRED_IF_EQUAL_BEAN_NAME);
       
       testBean.setTwoField1("not_equal_value");
       testBean.setRequired(null);
       runTest(testBean, REQUIRED_IF_EQUAL_BEAN_NAME);
    }
    
    public void testNegativeRequiredIfEqual() {
       CommonsValidatorBean testBean = new CommonsValidatorBean();
       testBean.setTwoField1("value");
       runTestNegative(testBean, REQUIRED_IF_EQUAL_BEAN_NAME, "Is this Frame2?", "tag.question");
    }
 
 }

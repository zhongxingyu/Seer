 package com.mpower.test.controller.validator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.collections.FactoryUtils;
 import org.apache.commons.collections.list.LazyList;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.springframework.validation.BindException;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.mpower.controller.validator.CodeValidator;
 import com.mpower.domain.DistributionLine;
 import com.mpower.domain.Gift;
 import com.mpower.domain.Person;
 import com.mpower.domain.Site;
 import com.mpower.domain.customization.Code;
import com.mpower.domain.customization.FieldDefinition;
 import com.mpower.service.CodeService;
 import com.mpower.service.SessionService;
 import com.mpower.test.BaseTest;
 import com.mpower.type.FieldType;
 
 public class CodeValidatorTest extends BaseTest {
 
     private CodeValidator validator;
     private DistributionLine distributionLine;
     private Gift gift;
     private BindException errors;
     private Mockery mockery;
     private Code projCode;
     private Code motivationCode;
     private Code currencyCode;
     private Site site;
     private Person person;
 
     @SuppressWarnings("unchecked")
     @BeforeMethod
     public void setupMocks() {
         projCode = new Code();
         projCode.setValue("001000");
         motivationCode = new Code();
         motivationCode.setValue("XYZ");
         currencyCode = new Code();
         currencyCode.setValue("USD");
         validator = new CodeValidator();
         mockery = new Mockery();
         final CodeService codeService = mockery.mock(CodeService.class);
         final SessionService sessionService = mockery.mock(SessionService.class);
         validator.setCodeService(codeService);
         validator.setSessionService(sessionService);
 
         mockery.checking(new Expectations() {{
             allowing (codeService).readCodeBySiteTypeValue("company1", "currencyCode", "USD"); will(returnValue(currencyCode));
             allowing (codeService).readCodeBySiteTypeValue("company1", "currencyCode", "foo"); will(returnValue(null));
             allowing (codeService).readCodeBySiteTypeValue("company1", "currencyCode", " "); will(returnValue(null));
             allowing (codeService).readCodeBySiteTypeValue("company1", "projectCode", "001000"); will(returnValue(projCode));
             allowing (codeService).readCodeBySiteTypeValue("company1", "projectCode", "foo"); will(returnValue(null));
             allowing (codeService).readCodeBySiteTypeValue("company1", "projectCode", " "); will(returnValue(null));
             allowing (codeService).readCodeBySiteTypeValue("company1", "motivationCode", "XYZ"); will(returnValue(motivationCode));
             allowing (codeService).readCodeBySiteTypeValue("company1", "motivationCode", "foo"); will(returnValue(null));
             allowing (codeService).readCodeBySiteTypeValue("company1", "motivationCode", " "); will(returnValue(null));
             allowing (sessionService).lookupSiteName(); will(returnValue("company1"));
         }});
 
         gift = new Gift();
         distributionLine = new DistributionLine();
         Map<String, FieldDefinition> map = new HashMap<String, FieldDefinition>();
         FieldDefinition fieldDef = new FieldDefinition();
         fieldDef.setFieldName("projectCode");
         fieldDef.setFieldType(FieldType.CODE);
         fieldDef.setDefaultLabel("Project Code");
         map.put("projectCode", fieldDef);
         
         fieldDef = new FieldDefinition();
         fieldDef.setFieldName("motivationCode");
         fieldDef.setFieldType(FieldType.CODE_OTHER);
         fieldDef.setDefaultLabel("Motivation Code");
         map.put("motivationCode", fieldDef);
 
         fieldDef = new FieldDefinition();
         fieldDef.setFieldName("currencyCode");
         fieldDef.setFieldType(FieldType.CODE);
         fieldDef.setDefaultLabel("Currency Code");
         map.put("currencyCode", fieldDef);
         gift.setFieldTypeMap(map);
         
         site = new Site();
         site.setName("company1");
         person = new Person();
         person.setSite(site);
         
         gift.setPerson(person);
         distributionLine.setGift(gift);
         List<DistributionLine> lines = LazyList.decorate(new ArrayList<DistributionLine>(), FactoryUtils.instantiateFactory(DistributionLine.class, new Class[] { Gift.class }, new Object[] { this }));
         lines.add(distributionLine);
         gift.setDistributionLines(lines);
     }
 
     @Test(groups = { "validateCode" })
     public void testValidCode() throws Exception {
         assert validator.supports(Gift.class);
         errors = new BindException(gift, "gift");       
         gift.setCurrencyCode("USD");
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors() == false;
 
         assert validator.supports(Gift.class);
         errors = new BindException(gift, "gift");       
         gift.setCurrencyCode(null);
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors() == false;
 
         assert validator.supports(Gift.class);
         errors = new BindException(gift, "gift");       
         gift.getDistributionLines().get(0).setProjectCode("001000");
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors() == false;
 
         errors = new BindException(gift, "gift");       
         gift.getDistributionLines().get(0).setProjectCode(null);
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors() == false;
     }
 
     @Test(groups = { "validateCode" })
     public void testInvalidCode() throws Exception {
         assert validator.supports(Gift.class);
         errors = new BindException(gift, "gift");       
         gift.setCurrencyCode("foo");
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors();
 
         assert validator.supports(Gift.class);
         errors = new BindException(gift, "gift");       
         gift.setCurrencyCode(" ");
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors();
 
         errors = new BindException(gift, "gift");        
         gift.getDistributionLines().get(0).setProjectCode("foo");
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors();
 
         errors = new BindException(gift, "gift"); 
         gift.getDistributionLines().get(0).setProjectCode(" ");
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors();
     }
 
     @Test(groups = { "validateCodeOther" })
     public void testValidCodeOther() throws Exception {
         errors = new BindException(gift, "gift");
         gift.getDistributionLines().get(0).setMotivationCode("XYZ");
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors() == false;
 
         errors = new BindException(gift, "gift");
         gift.getDistributionLines().get(0).setMotivationCode(null);
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors() == false;
 
         errors = new BindException(gift, "gift");
         gift.getDistributionLines().get(0).setMotivationCode("foo");
         gift.getDistributionLines().get(0).setOther_motivationCode("blarg");
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors() == false;
 
         errors = new BindException(gift, "gift");
         gift.getDistributionLines().get(0).setMotivationCode("foo");
         gift.getDistributionLines().get(0).setOther_motivationCode(" ");
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors() == false;
     }
 
     @Test(groups = { "validateCodeOther" })
     public void testInvalidCodeOther() throws Exception {
         errors = new BindException(gift, "gift");        
         gift.getDistributionLines().get(0).setMotivationCode("foo");
         gift.getDistributionLines().get(0).setOther_motivationCode(null);
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors();
 
         errors = new BindException(gift, "gift");        
         gift.getDistributionLines().get(0).setMotivationCode(" ");
         gift.getDistributionLines().get(0).setOther_motivationCode(null);
         validator.validate(gift, errors);
         mockery.assertIsSatisfied();
         assert errors.hasErrors();
    }
 
 }

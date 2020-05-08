 package org.motechproject.care.service;
 
 import junit.framework.Assert;
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.motechproject.care.request.CareCase;
 import org.motechproject.casexml.exception.CaseParserException;
 import org.motechproject.casexml.parser.CommcareCaseParser;
 import org.motechproject.casexml.service.exception.CaseException;
 import org.springframework.http.HttpEntity;
 
 import java.io.File;
 import java.io.IOException;
 
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 @RunWith(MockitoJUnitRunner.class)
 public class CareCaseServiceTest  {
 
     @Mock
     private MotherService motherService;
     @Mock
     private ChildService childService;
     private CareCaseService careCaseService;
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         careCaseService = new CareCaseService(motherService, childService);
     }
 
     @Test
     public void shouldRedirectToMotherServiceIfCaseTypeBelongsToMother() throws IOException {
         String xml = readFile("/sampleMotherCase.xml");
         careCaseService.processCase(new HttpEntity<String>(xml));
         verify(motherService).process((CareCase) Matchers.any());

     }
 
     @Test
     public void shouldSetMotherAsNotActiveIfCaseIsClosedAndCaseIdIsAMother() throws IOException {
         String xml = readFile("/sampleMotherCaseForClose.xml");
         careCaseService.processCase(new HttpEntity<String>(xml));
         verify(motherService).closeCase("caseId");
     }
 
     @Test
     public void shouldRedirectToChildServiceIfCaseTypeBelongsToChild() throws IOException {
         String xml = readFile("/sampleChildCase.xml");
         careCaseService.processCase(new HttpEntity<String>(xml));
         verify(childService).process((CareCase) Matchers.any());
 
     }
 
     private String readFile(String resourcePath) throws IOException {
         String path = getClass().getResource(resourcePath).getPath();
         File file = new File(path);
         return FileUtils.readFileToString(file);
     }
 
 
     @Test
     public void shouldThrowCaseValidationExceptionIfCaseIdIsNotPresentInCreateCareCase() throws IOException, CaseParserException {
         String xml = readFile("/sampleMotherCase.xml");
 
         CommcareCaseParser<CareCase> caseParser = new CommcareCaseParser<CareCase>(CareCase.class, xml);
         CareCase careCase = caseParser.parseCase();
         careCase.setCase_id(null);
         try {
             careCaseService.createCase(careCase);
             Assert.fail("Should have thrown CaseValidationException for null user id");
         } catch (CaseException ex) {
             Assert.assertEquals("Case Id is a mandatory field.", ex.getMessage());
             Assert.assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getHttpStatusCode());
         }
     }
 
     @Test
     public void shouldThrowCaseValidationExceptionIfCaseIdIsEmptyInCreateCareCase() throws IOException, CaseParserException {
         String xml = readFile("/sampleMotherCase.xml");
 
         CommcareCaseParser<CareCase> caseParser = new CommcareCaseParser<CareCase>(CareCase.class, xml);
         CareCase careCase = caseParser.parseCase();
         careCase.setCase_id("");
         try {
             careCaseService.createCase(careCase);
             Assert.fail("Should have thrown CaseValidationException for null user id");
         } catch (CaseException ex) {
             Assert.assertEquals("Case Id is a mandatory field.", ex.getMessage());
             Assert.assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getHttpStatusCode());
         }
     }
 
     @Test
     public void shouldThrowCaseValidationExceptionIfOwnerIdIsNotPresentInCreateCareCase() throws IOException, CaseParserException {
         String xml = readFile("/sampleMotherCase.xml");
 
         CommcareCaseParser<CareCase> caseParser = new CommcareCaseParser<CareCase>(CareCase.class, xml);
         CareCase careCase = caseParser.parseCase();
         careCase.setOwner_id(null);
         try {
             careCaseService.createCase(careCase);
             Assert.fail("Should have thrown CaseValidationException for null user id");
         } catch (CaseException ex) {
             Assert.assertEquals("Owner Id is a mandatory field.", ex.getMessage());
             Assert.assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getHttpStatusCode());
         }
     }
 
     @Test
     public void shouldThrowCaseValidationExceptionIfOwnerIdIsEmptyInCreateCareCase() throws IOException, CaseParserException {
         String xml = readFile("/sampleMotherCase.xml");
 
         CommcareCaseParser<CareCase> caseParser = new CommcareCaseParser<CareCase>(CareCase.class, xml);
         CareCase careCase = caseParser.parseCase();
         careCase.setOwner_id("");
         try {
             careCaseService.createCase(careCase);
             Assert.fail("Should have thrown CaseValidationException for null user id");
         } catch (CaseException ex) {
             Assert.assertEquals("Owner Id is a mandatory field.", ex.getMessage());
             Assert.assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getHttpStatusCode());
         }
     }
 
 
     @Test
     public void shouldThrowCaseValidationExceptionIfCaseIdIsNotPresentInCloseCareCase() throws IOException, CaseParserException {
         String xml = readFile("/sampleMotherCaseForClose.xml");
 
         CommcareCaseParser<CareCase> caseParser = new CommcareCaseParser<CareCase>(CareCase.class, xml);
         CareCase careCase = caseParser.parseCase();
         careCase.setCase_id(null);
         try {
             careCaseService.closeCase(careCase);
             Assert.fail("Should have thrown CaseValidationException for null user id");
         } catch (CaseException ex) {
             Assert.assertEquals("Case Id is a mandatory field.", ex.getMessage());
             Assert.assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getHttpStatusCode());
         }
     }
 
     @Test
     public void shouldThrowCaseValidationExceptionIfCaseIdIsEmptyInCloseCareCase() throws IOException, CaseParserException {
         String xml = readFile("/sampleMotherCaseForClose.xml");
 
         CommcareCaseParser<CareCase> caseParser = new CommcareCaseParser<CareCase>(CareCase.class, xml);
         CareCase careCase = caseParser.parseCase();
         careCase.setCase_id("");
         try {
             careCaseService.closeCase(careCase);
             Assert.fail("Should have thrown CaseValidationException for null user id");
         } catch (CaseException ex) {
             Assert.assertEquals("Case Id is a mandatory field.", ex.getMessage());
             Assert.assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getHttpStatusCode());
         }
     }
 
     @Test
     public void shouldCloseCaseForMotherWhenCommcareCloseIsReceivedForMother() throws IOException {
         CareCase careCase = new CareCase();
         String caseId = "caseId";
         careCase.setCase_id(caseId);
         when(motherService.closeCase(caseId)).thenReturn(true);
         careCaseService.closeCase(careCase);
 
         verify(motherService).closeCase(caseId);
         verify(childService, never()).closeCase(caseId);
     }
 
     @Test
     public void shouldCloseCaseForChildWhenCommcareCloseIsReceivedForChild() throws IOException {
         CareCase careCase = new CareCase();
         String caseId = "caseId";
         careCase.setCase_id(caseId);
         when(motherService.closeCase(caseId)).thenReturn(false);
         careCaseService.closeCase(careCase);
 
         verify(motherService).closeCase(caseId);
         verify(childService).closeCase(caseId);
     }
 }

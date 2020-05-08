 package org.synyx.minos.skillz.web;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 import static org.synyx.minos.core.web.WebTestUtils.*;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.mock.web.MockHttpSession;
 import org.springframework.mock.web.MockMultipartFile;
 import org.springframework.ui.ExtendedModelMap;
 import org.springframework.ui.Model;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.Errors;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.multipart.MultipartFile;
 import org.synyx.minos.core.Core;
 import org.synyx.minos.core.domain.User;
 import org.synyx.minos.core.web.Message;
 import org.synyx.minos.skillz.domain.Level;
 import org.synyx.minos.skillz.domain.Resume;
 import org.synyx.minos.skillz.domain.resume.ResumeAttributeFilter;
 import org.synyx.minos.skillz.service.PdfDocbookCreator;
 import org.synyx.minos.skillz.service.ResumeManagement;
 import org.synyx.minos.skillz.service.SkillManagement;
 
 
 /**
  * Unit test for {@link ResumeController}.
  * 
  * @author Oliver Gierke - gierke@synyx.de
  * @author Markus Knittig - knittig@synyx.de
  */
 @RunWith(MockitoJUnitRunner.class)
 public class ResumeControllerUnitTest {
 
     private ResumeController controller;
 
     @Mock
     private ResumeManagement resumeManagement;
     @Mock
     private SkillManagement skillManagement;
     @Mock
     private PdfDocbookCreator pdfDocbookCreator;
     @Mock
     private Resume resume;
 
     private Errors errors;
     private Model model;
 
 
     @Before
     public void setUp() {
 
         controller =
                 new ResumeController(resumeManagement, skillManagement, null,
                         pdfDocbookCreator, null);
 
         errors = new BeanPropertyBindingResult(null, "");
         model = new ExtendedModelMap();
     }
 
 
     @Test
     public void rejectsInvalidResumeId() throws Exception {
 
         String view = controller.resume(5L, model);
 
         verify(resumeManagement).getResume(5L);
         assertNull(view);
         assertTrue(model.asMap().get(Core.MESSAGE) instanceof Message);
     }
 
 
     @Test
     public void rejectsInvalidFileExtension() throws Exception {
 
         MultipartFile multipartFile =
                 new MockMultipartFile("foobar.exe", "foobar.exe", "", ""
                         .getBytes());
 
         controller.saveResumePhoto(resume, errors, model, multipartFile);
 
         assertErrorMessage(model);
     }
 
 
    @SuppressWarnings({ "unchecked", "rawtypes" })
     @Test
     public void processesAttributeFiltersCorrectly() throws Exception {
 
         ResumeAttributeFilter filter = mock(ResumeAttributeFilter.class);
         WebRequest webRequest = mock(WebRequest.class);
         File file = mock(File.class);
         when(filter.getMessageKey()).thenReturn("xyz");
         when(webRequest.getParameter(filter.getMessageKey())).thenReturn("1");
         when(resumeManagement.getResumeAttributeFilters()).thenReturn(
                 Collections.singletonList(filter));
         when(
                 pdfDocbookCreator.createTempPdfFile((File) anyObject(),
                         (Resume) anyObject(), (List<Level>) anyObject()))
                 .thenReturn(file);
 
         controller.resumePdf(null, null, new MockHttpSession(), webRequest);
 
         ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
         verify(resumeManagement).getFilteredResume((User) anyObject(),
                 argument.capture());
         assertEquals(filter.getMessageKey(), ((ResumeAttributeFilter) argument
                 .getValue().get(0)).getMessageKey());
     }
 }

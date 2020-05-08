 package org.synyx.skills.web;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import static org.synyx.minos.core.web.WebTestUtils.*;
 
 import java.io.File;
 import java.io.OutputStream;
 import java.util.Collections;
 import java.util.List;
 import javax.servlet.http.HttpSession;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Answers;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.mock.web.MockHttpServletResponse;
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
 import org.synyx.skills.domain.Level;
 import org.synyx.skills.domain.Resume;
 import org.synyx.skills.domain.resume.ResumeAttributeFilter;
 import org.synyx.skills.service.PdfDocbookCreator;
 import org.synyx.skills.service.ResumeManagement;
 import org.synyx.skills.service.SkillManagement;
 import org.synyx.skills.service.SkillsAuthenticationServiceWrapper;
 
 
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
     @Mock(answer= Answers.RETURNS_MOCKS)
     private SkillsAuthenticationServiceWrapper authenticationService;
     @Mock
     private Resume resume;
 
     private HttpSession mockSession = new MockHttpSession();
 
     private Errors errors;
     private Model model;
 
 
     @Before
     public void setUp() throws Exception {
 
         controller = new ResumeController(authenticationService, resumeManagement, skillManagement, null, pdfDocbookCreator, null, null);
 
         errors = new BeanPropertyBindingResult(null, "");
         model = new ExtendedModelMap();
 
         mockSession.getServletContext().setAttribute("javax.servlet.context.tempdir", new File("/tmp/"));
 
         new File("/tmp/resume.pdf").createNewFile();
     }
 
 
     @Test
     public void rejectsInvalidResumeUser() throws Exception {
 
         String view = controller.resume(null, model);
 
         verify(resumeManagement).getResume(any(User.class));
         assertNull(view);
     }
 
 
     @Test
     public void rejectsInvalidFileExtension() throws Exception {
 
         MultipartFile multipartFile = new MockMultipartFile("foobar.exe", "foobar.exe", "", "".getBytes());
 
         controller.saveResumePhoto("dude", model, multipartFile);
 
         assertErrorMessage(model);
     }
 
 
     @SuppressWarnings("unchecked")
     @Test
     public void processesAttributeFiltersCorrectly() throws Exception {
 
         ResumeAttributeFilter filter = mock(ResumeAttributeFilter.class);
         WebRequest webRequest = mock(WebRequest.class);
         File file = new File("resume.pdf");
         when(filter.getMessageKey()).thenReturn("xyz");
         when(webRequest.getParameter(filter.getMessageKey())).thenReturn("1");
         when(resumeManagement.getResumeAttributeFilters()).thenReturn(Collections.singletonList(filter));
         when(pdfDocbookCreator.createTempPdfFile((File) anyObject(), (Resume) anyObject(), (List<Level>) anyObject()))
                 .thenReturn(file);
 
         controller.resumePdf(null, new MockHttpServletResponse(), mockSession, mock(OutputStream.class), webRequest);
 
         ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
         verify(resumeManagement).getFilteredResume((User) anyObject(), argument.capture());
         assertEquals(filter.getMessageKey(), ((ResumeAttributeFilter) argument.getValue().get(0)).getMessageKey());
     }
 }

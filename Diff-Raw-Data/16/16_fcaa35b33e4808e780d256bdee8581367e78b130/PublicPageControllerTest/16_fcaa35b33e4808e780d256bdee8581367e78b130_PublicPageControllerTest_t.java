 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Web Questionnaires 2
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Anton Dmitrijev
  */
 package eionet.webq.web.controller;
 
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 
 import eionet.webq.dto.ProjectFile;
 import eionet.webq.dto.UserFile;
 import eionet.webq.service.ProjectFileService;
import eionet.webq.service.RemoteFileService;
 import eionet.webq.service.UserFileService;
 
 /**
  */
 public class PublicPageControllerTest {
 
     public static final int WEB_FORM_ID = 1;
     @Mock
     private ProjectFileService projectFileService;
     @Mock
     private UserFileService userFileService;
     @InjectMocks
     private PublicPageController publicPageController = new PublicPageController();
    @Mock
    private RemoteFileService remoteFileService;
 
     @Before
     public void setUp() throws Exception {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
     public void savesNewUserFileToStorageAndRedirectsToWebForm() throws Exception {
         ProjectFile projectFile = new ProjectFile();
         projectFile.setNewXmlFileName("test_file.xml");
         projectFile.setXmlSchema("xml-schema");
         UserFile userToSave = saveFileAndGetParameterFromServiceCall(projectFile);
 
         assertThat(userToSave.getXmlSchema(), equalTo(projectFile.getXmlSchema()));
         assertThat(userToSave.getName(), equalTo(projectFile.getNewXmlFileName()));
     }
 
     @Test
     public void ifNewFileNameNotSetUseDefault() throws Exception {
         UserFile userFile = saveFileAndGetParameterFromServiceCall(new ProjectFile());
         assertThat(userFile.getName(), equalTo("new_form.xml"));
     }
 
     @Test
     public void writesWebFormContentForResponseWithRightHeaders() throws Exception {
         ProjectFile projectFile = new ProjectFile();
         byte[] testContent = "test-content".getBytes();
         projectFile.setFileContent(testContent);
         when(projectFileService.getById(WEB_FORM_ID)).thenReturn(projectFile);
 
         MockHttpServletResponse response = new MockHttpServletResponse();
         publicPageController.startWebFormWriteFormToResponse(WEB_FORM_ID, response);
 
         assertThat(response.getContentType(), equalTo("application/xhtml+html"));
         assertThat(response.getContentLength(), equalTo(testContent.length));
         assertThat(response.getContentAsByteArray(), equalTo(testContent));
     }
 
     private UserFile saveFileAndGetParameterFromServiceCall(ProjectFile projectFile) throws Exception{
         when(projectFileService.getById(WEB_FORM_ID)).thenReturn(projectFile);
        byte[] testContent = "test-content".getBytes();
        when(remoteFileService.fileContent(projectFile.getEmptyInstanceUrl())).thenReturn(testContent);
 
         publicPageController.startWebFormSaveFile(WEB_FORM_ID, new MockHttpServletRequest());
 
         ArgumentCaptor<UserFile> userFileArgumentCaptor = ArgumentCaptor.forClass(UserFile.class);
         verify(userFileService).save(userFileArgumentCaptor.capture());
         return userFileArgumentCaptor.getValue();
     }
 }

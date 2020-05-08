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
 
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
 
import eionet.webq.dao.MergeModules;
import eionet.webq.dao.orm.MergeModule;
import eionet.webq.dao.orm.MergeModuleXmlSchema;
import eionet.webq.dao.orm.UploadedFile;
import eionet.webq.dao.orm.UserFile;
import eionet.webq.service.UserFileService;
import eionet.webq.web.AbstractContextControllerTests;
 import java.util.Arrays;
 import org.hamcrest.core.IsEqual;
 import org.hibernate.FlushMode;
 import org.hibernate.SessionFactory;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.mock.web.MockHttpSession;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
 import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @Transactional
 public class FileDownloadControllerIntegrationTest extends AbstractContextControllerTests {
     public static final String XML_SCHEMA = "http://xml.schema";
     @Autowired
     private MergeModules modules;
     @Autowired
     private UserFileService userFileService;
     @Autowired
     private SessionFactory sessionFactory;
     @Autowired
     private MockHttpSession session;
 
     @Before
     public void before() throws Exception {
         sessionFactory.getCurrentSession().setFlushMode(FlushMode.ALWAYS);
     }
 
     @Test
     public void allowsToDownloadMergeFiles() throws Exception {
         UploadedFile xslFile = saveMergeModule("merge.xsl").getXslFile();
 
         request(MockMvcRequestBuilders.get("/download/merge/file/" + xslFile.getName()))
                 .andExpect(MockMvcResultMatchers.content().bytes(xslFile.getContent().getFileContent()));
     }
 
     @Test
     public void whenMergingUserFiles_ifMoreThatOneMergeModuleFound_showMergeModuleSelectPage() throws Exception {
         saveMergeModule("merge1.xsl");
         saveMergeModule("merge2.xsl");
 
         int id1 = saveUserFile();
         int id2 = saveUserFile();
 
         request(MockMvcRequestBuilders.post("/download/merge/files")
                 .param("selectedUserFile", Integer.toString(id1), Integer.toString(id2))
                 .session(session))
                 .andExpect(view().name("merge_options"));
     }
 
     @Test
     public void whenAskingForUserFileJson_returnsResponseInJsonFormat() throws Exception {
        int id = userFileStorage.save(new UserFile(new UploadedFile("test.xml",
                ("<?xml version=\"1.0\"?><html><head>headText</head><body>bodyText</body></html>").getBytes()), "someSchema"), session.getId());
         request(get("/download/converted_user_file?fileId={id}", id)
                 .accept(MediaType.APPLICATION_JSON)
                 .session(session))
                 .andExpect(jsonPath("$.html.head", IsEqual.equalTo("headText")))
                 .andExpect(jsonPath("$.html.body", IsEqual.equalTo("bodyText")))
                 .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
     }
 
     @Test
     public void convertsFileToJsonAndThanToXml() throws Exception {
         String xml = "<?xml version=\"1.0\"?><html><head>headText</head><body>bodyText</body></html>";
        int id = userFileStorage.save(new UserFile(new UploadedFile("test.xml", xml.getBytes()), "someSchema"), session.getId());
         request(get("/download/converted_user_file?fileId={id}", id)
                 .accept(MediaType.APPLICATION_XML)
                 .session(session))
                 .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_XML))
                 .andExpect(MockMvcResultMatchers.handler().methodName("downloadUserFileJsonToXml"));
     }
 
     private int saveUserFile() {
         return userFileService.save(new UserFile(new UploadedFile(), XML_SCHEMA));
     }
 
     private MergeModule saveMergeModule(String fileName) {
         MergeModule module = new MergeModule();
         module.setXmlSchemas(Arrays.asList(new MergeModuleXmlSchema(XML_SCHEMA)));
         UploadedFile xslFile = new UploadedFile(fileName, "merge-file-content".getBytes());
         module.setXslFile(xslFile);
         modules.save(module);
         return module;
     }
 
 }

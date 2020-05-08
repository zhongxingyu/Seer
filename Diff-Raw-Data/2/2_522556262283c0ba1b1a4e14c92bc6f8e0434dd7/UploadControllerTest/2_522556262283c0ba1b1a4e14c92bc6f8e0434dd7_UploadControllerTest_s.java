 package net.benelog.uploader;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 import org.springframework.mock.web.MockMultipartFile;
 import org.springframework.web.multipart.MultipartFile;
 
 /**
  * @author benelog
  */
 public class UploadControllerTest {
 
 	@Rule
	TemporaryFolder uploadFolder = new TemporaryFolder();
 	UploadController ctrl = new UploadController();
 
 	@Test
 	public void fileShouldBeUploaded() throws IOException {
 		// given 
 		String fileName = "test.txt";
 		String fileContent = "my works";
 		String serverPath = uploadFolder.getRoot().getAbsolutePath() + "/";
 		Map<String, Object> out = new HashMap<String, Object>();
 
 		// when
 		MultipartFile fileToTransfer = new MockMultipartFile(fileName, fileName, null, fileContent.getBytes());
 		String viewName = ctrl.upload(fileToTransfer, serverPath, out);
 
 		// then
 		String uploadedContent = FileUtils.readFileToString(new File(serverPath + fileName));
 		assertThat(uploadedContent, is(fileContent));
 
 		String message = (String)out.get("message");
 		assertThat(message, is(notNullValue()));
 		System.out.println(message);
 
 		assertThat(viewName, is("uploadForm"));
 	}
 }

 package ixcode.platform.http.server.resource;
 
 import org.junit.Test;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 public class FileToTemplatePathTest {
 
     @Test
     public void converts_unix_like_path_to_template_path() {
         FileToTemplatePath fileToTemplatePath = new FileToTemplatePath("/some/root", ".jade");
 
         String result = fileToTemplatePath.pathFrom("/some/root/my-section/a-template.jade");
 
         assertThat(result, is("/my-section/a-template"));
     }
 }

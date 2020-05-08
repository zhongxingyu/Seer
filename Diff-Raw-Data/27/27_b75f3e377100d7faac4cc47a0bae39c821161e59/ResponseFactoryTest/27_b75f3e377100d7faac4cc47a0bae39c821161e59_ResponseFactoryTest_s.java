 package com.tjh.swivel.model;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.entity.FileEntity;
 import org.apache.http.util.EntityUtils;
 import org.junit.Before;
 import org.junit.Test;
 import vanderbilt.util.Maps;
 
 import java.io.File;
 import java.io.IOException;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.instanceOf;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 
 public class ResponseFactoryTest {
 
     private ResponseFactory responseFactory;
 
     @Before
     public void setUp() throws Exception {
         responseFactory = new ResponseFactory();
     }
 
     @Test
     public void createResponsePopulatesStatusCode() {
         HttpResponse response = responseFactory.createResponse(200, "entity", "text/plain");
 
         assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
     }
 
     @Test
     public void createResponsePopulatesEntity() {
         HttpResponse response = responseFactory.createResponse(200, "entity", "text/plain");
 
         assertThat(response.getEntity().getContentType().getValue(), equalTo("text/plain"));
     }
 
     @Test
     public void createResponseWithStringContentCreatesEntity() throws IOException {
         HttpResponse response = responseFactory.createResponse(200, "entity", "text/plain");
 
        assertThat(EntityUtils.toString(response.getEntity()), equalTo("entity"));
     }
 
     @Test
     public void createResponseWithCodeInitializesCode() {
         HttpResponse response = responseFactory.createResponse(201);
 
         assertThat(response.getStatusLine().getStatusCode(), equalTo(201));
     }
 
     @Test
     public void createResponseWithCodeHasNullEntity() {
         HttpResponse response = responseFactory.createResponse(201);
 
         assertThat(response.getEntity(), nullValue());
     }
 
     @Test
     public void createResponseWithFileWorks() {
         File mockFile = mock(File.class);
         HttpResponse response = responseFactory.createResponse(
                 Maps.<String, Object>asMap(
                         ResponseFactory.STATUS_CODE_KEY, 200,
                         ResponseFactory.CONTENT_TYPE_KEY, "application/pdf",
                         ResponseFactory.FILE_NAME_KEY, "fileName.pdf"),
                 mockFile);
 
         assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
         assertThat(response.getEntity(), instanceOf(FileEntity.class));
     }
 }

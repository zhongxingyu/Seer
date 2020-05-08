 /*
  * Copyright (c) 2013, MaestroDev. All rights reserved.
  */
 package com.maestrodev.maestro.plugins.gitblit;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 import static org.mockito.Mockito.*;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.fusesource.stomp.client.BlockingConnection;
 import org.json.simple.JSONObject;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Matchers;
 import org.mockito.Mockito;
 
 import com.gitblit.client.GitblitClient;
 import com.gitblit.models.RepositoryModel;
 import com.maestrodev.maestro.plugins.StompConnectionFactory;
 
 public class GitblitWorkerTest {
 
     
     HashMap<String, Object> stompConfig;
     StompConnectionFactory stompConnectionFactory;
     BlockingConnection blockingConnection;
 
     JSONObject workitem;
     JSONObject fields;
     GitblitWorker worker;
     
     @SuppressWarnings("unchecked")
     @Before
     public void setUp() throws Exception {
 	 stompConfig = new HashMap<String, Object>();
 	 stompConfig.put("host", "localhost");
 	 stompConfig.put("port", "61613");
 	 stompConfig.put("queue", "test");
 
 	 // Setup the mock stomp connection
 	 stompConnectionFactory = mock(StompConnectionFactory.class);
 	 blockingConnection = mock(BlockingConnection.class);
 	 when(stompConnectionFactory.getConnection(Matchers.any(String.class),
 	                Matchers.any(int.class))).thenReturn(blockingConnection);
 	
 	fields = new JSONObject();
 	fields.put("server_name", "localhost");
 	fields.put("url", "https://localhost:8443");
 	fields.put("account", "admin");
 	fields.put("password", "admin");
 	fields.put("repository_name", "junit.git");
 	fields.put("owner", "admin");
 	fields.put("description", "junit");
 	
 	workitem = new JSONObject();
         workitem.put("fields", fields);
         
         worker = new GitblitWorker();	
         worker.setStompConnectionFactory(stompConnectionFactory);
         worker.setStompConfig(stompConfig);
         worker.setWorkitem(workitem);        
     }
     
     
     
     @SuppressWarnings("unchecked")
     @Test
     public void shouldCreateNewRepository() throws IOException {
 	GitblitWorker spy = spy(worker);
 	GitblitClient clientMock = mock(GitblitClient.class);
 	
 	when(clientMock.createRepository(Mockito.any(RepositoryModel.class), Mockito.anyList())).thenReturn(true);
 	when(spy.getGitblitClient()).thenReturn(clientMock);
         spy.createRepository();        
         verify(spy).writeOutput("Created junit.git repository owned by admin on Gitblit server at https://localhost:8443");
         assertThat(spy.getError(), nullValue());
         
     }
     
     @SuppressWarnings("unchecked")
     @Test
     public void shouldFailWhenTryingToCreateExistingRepository() throws IOException {
 	GitblitClient clientMock = mock(GitblitClient.class);	
 	when(clientMock.createRepository(Mockito.any(RepositoryModel.class), Mockito.anyList())).thenReturn(false);
 
 	GitblitWorker workerSpy = spy(worker);
 	when(workerSpy.getGitblitClient()).thenReturn(clientMock);
 
         workerSpy.createRepository();        
        assertThat(workerSpy.getError(), is("Unable to create repository"));
         
     }
     
     
     @Test
     public void shouldCreateClientUsingWorkItemFieldData() {
 	GitblitClient client = worker.getGitblitClient();	
 	assertThat(client.account, is("admin"));
 	assertThat(client.url, is("https://localhost:8443"));
     }
     
 
 }

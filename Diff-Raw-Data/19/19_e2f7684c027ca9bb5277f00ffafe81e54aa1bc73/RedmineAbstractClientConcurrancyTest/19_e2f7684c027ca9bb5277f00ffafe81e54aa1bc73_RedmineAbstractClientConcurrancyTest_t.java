 /*******************************************************************************
  * Copyright (c) 2004, 2007 Mylyn project committers and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Mylyn project committers
  *******************************************************************************/
 /*******************************************************************************
  * Copyright (c) 2008 Sven Krzyzak
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Sven Krzyzak - adapted Trac implementation for Redmine
  *******************************************************************************/
 
 package org.svenk.redmine.core.client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Date;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.httpclient.HostConfiguration;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.mylyn.commons.net.AbstractWebLocation;
 import org.eclipse.mylyn.commons.net.WebLocation;
 import org.eclipse.mylyn.commons.net.WebUtil;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.junit.Assert;
 import org.svenk.redmine.core.RedmineCorePlugin;
 import org.svenk.redmine.core.exception.RedmineException;
 import org.svenk.redmine.core.model.RedmineTicket;
 
 public class RedmineAbstractClientConcurrancyTest extends TestCase {
 
 	private final static String URI = "http://localhost:1234/";
 	
 	private final static String RESPONSE_HEADER = "HTTP/1.0 200 OK\n\n";
 	
 	private final static String TEST_STRING = "123456";
 	
 	private HttpMethod firstMethod;
 	
 	private HttpMethod secondMethod;
 	
 	private AbstractWebLocation location;
 	
 	private TaskRepository repository;
 	
 	private ClientTestImpl testee;
 	
 	private Thread serverThread;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		location = new WebLocation(URI, "jsmith", "jsmith");
 		repository = new TaskRepository(RedmineCorePlugin.REPOSITORY_KIND, URI);
 		
 		firstMethod = new GetMethod(URI);
 		secondMethod = new GetMethod(URI);
 		
 		testee = new ClientTestImpl(location, repository);
 		
 		serverThread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					ServerSocket server = new ServerSocket(1234);
 					while(!Thread.interrupted()) {
 						Socket socket = server.accept();
 						
 						socket.getInputStream().read(new byte[4096]);
 						socket.getOutputStream().write(RESPONSE_HEADER.getBytes());
 						socket.getOutputStream().write(TEST_STRING.getBytes());
 						socket.close();
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				
 			}
 		});
 		
 		serverThread.start();
 	}
 
 	protected void tearDown() throws Exception {
 		serverThread.interrupt();
 		super.tearDown();
 	}
 	
 	public void testConcurrencyRequests() throws Exception {
 		
 		byte[] firstBuffer = new byte[TEST_STRING.length()];
 		byte[] secondBuffer = new byte[TEST_STRING.length()];
 		
 		try {
 			testee.executeMethod(firstMethod);
 			InputStream firstIn = firstMethod.getResponseBodyAsStream();
 			firstIn.read(firstBuffer, 0, 4);
 			
 			testee.executeMethod(secondMethod);
 			InputStream secondIn = secondMethod.getResponseBodyAsStream();
 			secondIn.read(secondBuffer, 0, 6);
 			
 			firstIn.read(firstBuffer, 4, 2);
 		} finally {
 			Assert.assertArrayEquals(TEST_STRING.getBytes(), firstBuffer);
 			Assert.assertArrayEquals(TEST_STRING.getBytes(), secondBuffer);
 		}
 		
 
 	}
 
 	private class ClientTestImpl extends AbstractRedmineClient {
 		
 		ClientTestImpl(AbstractWebLocation location, /*RedmineClientData clientData,*/ TaskRepository repository) {
 			super(location, null, repository);
 		}
 
 		int executeMethod(HttpMethod method) throws RedmineException {
 			IProgressMonitor monitor = new NullProgressMonitor();
 			method.setFollowRedirects(false);
 			HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
 
 			return executeMethod(method, hostConfiguration, monitor);
 		}
 		
 		/* */
 		
 		@Override
 		protected String checkClientVersion(IProgressMonitor monitor)
 				throws RedmineException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public List<Integer> getChangedTicketId(Integer projectId,
 				Date changedSince, IProgressMonitor monitor)
 				throws RedmineException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public RedmineClientData getClientData() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public RedmineTicket getTicket(int id, IProgressMonitor monitor)
 				throws RedmineException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public boolean hasAttributes() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public void search(String searchParam, String projectId,
 				String storedQueryId, List<RedmineTicket> tickets,
 				IProgressMonitor monitor) throws RedmineException {
 			// TODO Auto-generated method stub
 			
 		}
 		@Override
 		public void updateAttributes(boolean force, IProgressMonitor monitor)
 				throws RedmineException {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 }

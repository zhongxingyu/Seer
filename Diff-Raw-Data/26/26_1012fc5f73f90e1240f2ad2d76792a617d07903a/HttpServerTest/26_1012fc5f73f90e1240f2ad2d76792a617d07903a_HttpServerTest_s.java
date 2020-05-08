 /*
  * #%L
  * Bitrepository Protocol
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.protocol.http;
 
 import java.io.File;
 
 import org.bitrepository.common.IntegrationTest;
 import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
 import org.bitrepository.protocol.fileexchange.HttpServerConnector;
 import org.bitrepository.protocol.fileexchange.HttpsServerConfiguration;
 import org.bitrepository.protocol.fileexchange.HttpsServerConnector;
 import org.bitrepository.protocol.fileexchange.TestFileStore;
 import org.testng.annotations.Test;
 
 /**
  * Tests the functionality of the different HttpServers. 
  */
 public class HttpServerTest extends IntegrationTest {
    private TestFileStore fileStore = new TestFileStore("HttpServerTest");;
 
     /**
      * Tests the functionality of the default http server.
      */
     @Test(groups = { "regressiontest", "external-httpserver" })
     public final void defaultHttpServerTest() throws Exception {
         addDescription("Tests whether it is possible to upload a file to the "
                 + "default http-server, and then download the same file again.");
 
         addStep("Initialising the http server configuration to use the default server.","");
         HttpServerConfiguration config = new HttpServerConfiguration();
         runHttpServerCoreTest(new HttpServerConnector(config, testEventManager));
     }
 
     /**
      * Tests the functionality of the default https server.
      */
     @Test(groups = { "testfirst", "httpsserver" })
     public final void httpsServerTest() throws Exception {
         addDescription("Tests whether it is possible to upload a file to the "
                 + "default https-server, and then download the same file again.");
 
         addStep("Initialising the https server configuration to use the htpps server.","");
         HttpsServerConfiguration config = new HttpsServerConfiguration();
         config.setProtocol("https");
         config.setHttpsCertificateAlias("sandkasse");
         config.setHttpsCertificatePath("src/test/resources/cert");
         config.setHttpsKeystorePath(new File("target", "keystore").getPath());
         config.setHttpsKeyStorePassword("1234");
 
         runHttpServerCoreTest(new HttpsServerConnector(config, testEventManager));
     }
 
     /**
      * Tests the functionality of the default http server.
      */
     @Test(groups = { "testfirst", "embedded-httpserver" })
     public final void embeddedHttpServerTest() throws Exception {
         addDescription("Tests whether it is possible to upload a file to the "
                 + "default http-server, and then download the same file again.");
         addStep("Start the embedded http server", "");
         EmbeddedHttpServer server = new EmbeddedHttpServer();
         server.start();
 
         addStep("Initialising the http server configuration to use the default server.","");
         HttpServerConfiguration config = new HttpServerConfiguration();
         config.setPortNumber(EmbeddedHttpServer.PORT_NUMBER);
         config.setHttpServerName(EmbeddedHttpServer.HTTP_SERVER_NAME);
         config.setHttpServerPath(EmbeddedHttpServer.HTTP_SERVER_PATH);
 
         runHttpServerCoreTest(new HttpServerConnector(config, testEventManager));
     }
 
     /**
      * Contains the configuration agnostic part of the test. The configuration is defined and injected by 
      * the concrete testcases.
      */
 
     private void runHttpServerCoreTest(HttpServerConnector httpServer) throws Exception {
         String httpTestFile = "httpTest";
 
         addStep("Remove the file if already presentalready","");
         httpServer.removeFile(httpTestFile);
 
         addStep("Uploading file", 
         "The file can now de download from the file server."); 
         httpServer.uploadFile(fileStore.getFileAsInputstream(
                 TestFileStore.DEFAULT_FILE_ID), 
                 httpServer.getURL(httpTestFile));
         httpServer.assertFileEquals(
                 fileStore.getFile(TestFileStore.DEFAULT_FILE_ID), 
                 httpServer.getURL(httpTestFile).toExternalForm());
     }
 }

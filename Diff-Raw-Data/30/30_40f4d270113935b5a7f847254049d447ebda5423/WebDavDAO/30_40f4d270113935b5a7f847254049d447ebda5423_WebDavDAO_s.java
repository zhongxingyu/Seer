 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.sixdimensions.wcm.cq.dao.webdav;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.DeleteMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.maven.plugin.logging.Log;
 
 import com.sixdimensions.wcm.cq.service.CQServiceConfig;
 
 /** Client functions to interact with Sling in integration tests */
 public class WebDavDAO {
 	private HttpClient httpClient;
 	private CQServiceConfig config;
 	private Log log;
 
 	/**
 	 * Construct a new PackageManagerAPIDAO with the specified configuration.
 	 * 
 	 * @param config
 	 *            the configuration to use
 	 */
 	public WebDavDAO(CQServiceConfig config) {
 		this.config = config;
 		this.log = config.getLog();
 	}
 
 	/**
 	 * Initializes the connection.
 	 */
 	public void init() {
 		// setup HTTP client, with authentication
 		httpClient = new HttpClient();
 		httpClient.getParams().setAuthenticationPreemptive(true);
 		Credentials defaultcreds = new UsernamePasswordCredentials(
 				config.getUser(), config.getPassword());
 		httpClient.getState()
 				.setCredentials(
 						new AuthScope(config.getHost().substring(
 								config.getHost().lastIndexOf("/") + 1),
 								Integer.parseInt(config.getPort()),
 								AuthScope.ANY_REALM), defaultcreds);
 
 	}
 
 	/**
 	 * Delete a file from the Sling repository
 	 * 
 	 * @return the HTTP status code
 	 */
 	public int delete(String path) throws IOException {
 		log.debug("delete");
 		final DeleteMethod delete = new DeleteMethod(config.getHost() + ":"
 				+ config.getPort() + path);
 		return httpClient.executeMethod(delete);
 	}
 
 	/**
 	 * Creates a folder at the specified path if one does not already exist.
 	 * 
 	 * @param path
 	 *            the path at which to create the folder
 	 * @throws IOException
 	 */
 	public void createFolder(String path) throws IOException {
 		log.debug("createFolder");
 		int status = 0;
 		String url = config.getHost() + ":" + config.getPort() + path;
 		status = httpClient.executeMethod(new GetMethod(url + ".txt"));
 		if (status != 200) {
 			status = httpClient.executeMethod(new HttpAnyMethod("MKCOL", url));
 			if (status != 201) {
 				throw new IOException("mkdir(" + url + ") failed, status code="
 						+ status);
 			}
 		}
 	}
 
 	/**
 	 * Checks to see if a folder exists at the specified path.
 	 * 
 	 * @param path
 	 *            the path to check if a folder exists, an invalid path will not
 	 *            trigger an error
 	 * @return true if the folder exists or false otherwise
 	 * @throws IOException
 	 * @throws HttpException
 	 */
 	public boolean folderExists(String path) throws HttpException, IOException {
 		log.debug("folderExists");
 		int status = 0;
 		if (!path.endsWith("/")) {
 			path += "/";
 		}
 		String url = config.getHost() + ":" + config.getPort() + path;
 		log.debug("Checking URL: " + url);
 		status = httpClient.executeMethod(new GetMethod(url));
 		return status == 200;
 	}
 
 	/**
	 * Upload a file to the Sling repository
 	 * 
	 * @return the HTTP status code
 	 */
 	public void uploadFile(File file, String path) throws IOException {
 		log.debug("uploadFile");
		String url = config.getHost() + ":" + config.getPort() + path+"/"+file.getName();
 		final PutMethod put = new PutMethod(url);
 
 		put.setRequestEntity(new InputStreamRequestEntity(new FileInputStream(
 				file)));
 		int status = httpClient.executeMethod(put);
		if(status != 201  && status != 204){
			throw new IOException("uploadFile(" + url + ") failed, status code="
					+ status);
 		}
 	}
 
 }

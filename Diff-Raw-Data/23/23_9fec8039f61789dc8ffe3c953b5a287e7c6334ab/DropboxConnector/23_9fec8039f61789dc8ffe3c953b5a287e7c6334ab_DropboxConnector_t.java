 /*
  * Copyright 2011 - 2012 by the CloudRAID Team
  * see AUTHORS for more details
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
 
  * http://www.apache.org/licenses/LICENSE-2.0
 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package de.dhbw_mannheim.cloudraid.dropbox.impl.net.connector;
 
 import static org.scribe.model.Verb.GET;
 import static org.scribe.model.Verb.POST;
 import static org.scribe.model.Verb.PUT;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Scanner;
 
 import javax.activation.MimetypesFileTypeMap;
 
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.builder.api.DropBoxApi;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verifier;
 import org.scribe.oauth.OAuthService;
 
 import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
 import de.dhbw_mannheim.cloudraid.config.exceptions.MissingConfigValueException;
 import de.dhbw_mannheim.cloudraid.core.net.connector.IStorageConnector;
 import de.dhbw_mannheim.cloudraid.core.net.model.IVolumeModel;
 
 /**
  * The API wrapper for Dropbox (API version 1).
  * 
  * @author Florian Bausch
  * 
  */
 public class DropboxConnector implements IStorageConnector {
 	private final static String ROOT_NAME = "sandbox";
 	private final static String DELETE_URL = "https://api.dropbox.com/1/fileops/delete?root="
 			+ ROOT_NAME + "&path=";
 	private final static String GET_URL = "https://api-content.dropbox.com/1/files/"
 			+ ROOT_NAME + "/";
 	private final static String PUT_URL = "https://api-content.dropbox.com/1/files_put/"
 			+ ROOT_NAME + "/";
 	private final static String META_URL = "https://api.dropbox.com/1/metadata/"
 			+ ROOT_NAME + "/";
 
 	private final static MimetypesFileTypeMap MIME_MAP = new MimetypesFileTypeMap();
 
 	/**
 	 * A reference to the current config;
 	 */
 	private ICloudRAIDConfig config = null;
 
 	private String accessTokenValue = null;
 	private String appKey = null;
 	private String appSecret = null;
 	private String accessTokenSecret = null;
 	private String splitOutputDir = null;
 
 	private OAuthService service = null;
 
 	private Token accessToken = null;
 	private int id = -1;
 
 	@Override
 	public boolean connect() {
 		service = new ServiceBuilder().provider(DropBoxApi.class)
 				.apiKey(this.appKey).apiSecret(this.appSecret).build();
 		if (this.accessTokenValue == null || this.accessTokenSecret == null) {
 			Scanner in = new Scanner(System.in);
 			Token requestToken = service.getRequestToken();
 			try {
 				Desktop.getDesktop().browse(
 						new URI(service.getAuthorizationUrl(requestToken)));
 				System.out
 						.println("Please authorize the app and then press enter in this window.");
 			} catch (Exception e) {
 				System.out
 						.println("Please go to "
 								+ service.getAuthorizationUrl(requestToken)
 								+ " , authorize the app and then press enter in this window.");
 			}
 			in.nextLine();
 			Verifier verifier = new Verifier("");
 			System.out.println();
 			this.accessToken = service.getAccessToken(requestToken, verifier);
 			this.accessTokenSecret = this.accessToken.getSecret();
 			this.accessTokenValue = this.accessToken.getToken();
 			System.out.println("Your secret access token: "
 					+ this.accessTokenSecret);
 			System.out.println("Your public access token: "
 					+ this.accessTokenValue);
 		} else {
 			this.accessToken = new Token(this.accessTokenValue,
 					this.accessTokenSecret);
 		}
 		return true;
 	}
 
 	/**
 	 * This function initializes the {@link DropboxConnector} with the customer
 	 * and application tokens. During the {@link #connect()} process various
 	 * tokens are used. If {@link #connect()} returns <code>false</code>, this
 	 * class has to be re-instantiated and initialized with proper credentials.
 	 * </br>
 	 * 
 	 * The {@link ICloudRAIDConfig} must contain following keys:
 	 * <ul>
 	 * <li><code>connector.ID.appKey</code></li>
 	 * <li><code>connector.ID.appSecret</code></li>
 	 * <li><i><code>connector.ID.accessTokenValue</code></i> (optional)</li>
 	 * <li><i><code>connector.ID.accessTokenSecret</code></i> (optional)</li>
 	 * </ul>
 	 * 
 	 * @param connectorid
 	 *            The internal id of this connector.
 	 * @param config
 	 *            The reference to a running {@link ICloudRAIDConfig} service.
 	 * 
 	 * @throws InstantiationException
 	 *             Thrown if not all required parameters are passed.
 	 */
 	@Override
 	public IStorageConnector create(int connectorid, ICloudRAIDConfig config)
 			throws InstantiationException {
 		this.id = connectorid;
 		this.config = config;
 		String kAccessTokenSecret = String.format(
 				"connector.%d.accessTokenSecret", this.id);
 		String kAccessTokenValue = String.format(
 				"connector.%d.accessTokenValue", this.id);
 		String kAppKey = String.format("connector.%d.appKey", this.id);
 		String kAppSecret = String.format("connector.%d.appSecret", this.id);
 		try {
 			splitOutputDir = this.config.getString("split.output.dir");
 			if (this.config.keyExists(kAccessTokenSecret)
 					&& this.config.keyExists(kAccessTokenValue)
 					&& this.config.keyExists(kAppKey)
 					&& this.config.keyExists(kAppSecret)) {
 				this.accessTokenSecret = this.config
 						.getString(kAccessTokenSecret);
 				this.accessTokenValue = this.config
 						.getString(kAccessTokenValue);
 				this.appKey = this.config.getString(kAppKey);
 				this.appSecret = this.config.getString(kAppSecret);
 			} else if (this.config.keyExists(kAppKey)
 					&& this.config.keyExists(kAppSecret)) {
 				this.appKey = this.config.getString(kAppKey);
 				this.appSecret = this.config.getString(kAppSecret);
 			} else {
 				throw new InstantiationException("At least " + kAppKey
 						+ " and " + kAppSecret
 						+ " have to be set in the config. " + kAccessTokenValue
 						+ " and " + kAccessTokenSecret + " are optional!");
 			}
 		} catch (MissingConfigValueException e) {
 			e.printStackTrace();
 			throw new InstantiationException(e.getMessage());
 		}
 		return this;
 	}
 
 	@Override
 	public IVolumeModel createVolume(String name) {
 		return null;
 	}
 
 	@Override
 	public boolean delete(String resource) {
 		System.out.println("DELETE " + resource);
 		// This request has to be sent as "POST" not as "DELETE"
		OAuthRequest request = new OAuthRequest(POST, DELETE_URL + resource
				+ "." + this.id);
 		this.service.signRequest(this.accessToken, request);
 		Response response = request.send();
 		System.out.println(response.getCode() + " " + response.getBody());
 		if (response.getCode() == 406) {
			System.err.println("Would delete too many files");
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void deleteVolume(String name) {
 	}
 
 	@Override
 	public void disconnect() {
 	}
 
 	@Override
 	public InputStream get(String resource) {
 		System.out.println("GET " + resource);
		OAuthRequest request = new OAuthRequest(GET, GET_URL + resource + "."
				+ this.id);
 		this.service.signRequest(this.accessToken, request);
 		Response response = request.send();
 		System.out.println(response.getCode());
 		if (response.getCode() == 404) {
 			return null;
 		}
 		return response.getStream();
 	}
 
 	@Override
 	public String getMetadata(String resource) {
 		// TODO implementation
 
 		return ""; // TODO return null on error
 	}
 
 	@Override
 	public IVolumeModel getVolume(String name) {
 		return null;
 	}
 
 	@Override
 	public void loadVolumes() {
 	}
 
 	/**
 	 * Uploads a file to the Dropbox servers.
 	 * 
 	 * @param resource
 	 *            The file file name in the split.output.dir
 	 * @return true, if the file could be uploaded; false, if not.
 	 */
 	private boolean performUpload(String resource) {
 		File f = new File(splitOutputDir + "/" + resource + "." + this.id);
 		if (!f.exists()) {
 			System.err.println("File does not exist.");
 			return false;
 		} else {
 			int maxFilesize;
 			try {
 				maxFilesize = this.config.getInt("filesize.max", null);
 				if (f.length() > maxFilesize) {
 					System.err.println("File too big");
 					return false;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 
 		byte[] fileBytes = new byte[(int) f.length()];
 		InputStream fis;
 		try {
 			fis = new FileInputStream(f);
 			fis.read(fileBytes);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
		OAuthRequest request = new OAuthRequest(PUT, PUT_URL + resource + "."
				+ this.id + "?overwrite=true");
 		request.addHeader("Content-Type", MIME_MAP.getContentType(f));
 		this.service.signRequest(this.accessToken, request);
 		request.addPayload(fileBytes);
 		Response response = request.send();
 		System.out.println(response.getCode() + " " + response.getBody());
 		if (response.getCode() == 411 || response.getCode() == 400) {
 			System.err.println("Could not PUT file to Dropbox.");
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean update(String resource) {
 		System.out.println("Update " + resource);
		OAuthRequest request = new OAuthRequest(GET, META_URL + resource + "."
				+ this.id);
 		this.service.signRequest(this.accessToken, request);
 		Response response = request.send();
 		System.out.println(response.getCode());
 		if (response.getCode() == 404) {
 			return false;
 		}
 		return performUpload(resource);
 	}
 
 	@Override
 	public boolean upload(String resource) {
 		System.out.println("Update " + resource);
		OAuthRequest request = new OAuthRequest(GET, META_URL + resource + "."
				+ this.id);
 		this.service.signRequest(this.accessToken, request);
 		Response response = request.send();
 		System.out.println(response.getCode());
 		if (response.getCode() == 404) {
 			return performUpload(resource);
 		}
 		return false;
 	}
 }

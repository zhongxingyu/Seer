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
 
 package de.dhbw_mannheim.cloudraid.ubuntuone.impl.net.connector;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 import org.scribe.oauth.OAuthService;
 import org.scribe.utils.OAuthEncoder;
 
 import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
 import de.dhbw_mannheim.cloudraid.config.exceptions.MissingConfigValueException;
 import de.dhbw_mannheim.cloudraid.core.net.connector.IStorageConnector;
 import de.dhbw_mannheim.cloudraid.ubuntuone.impl.net.oauth.UbuntuOneApi;
 import de.dhbw_mannheim.cloudraid.ubuntuone.impl.net.oauth.UbuntuOneService;
 
 /**
  * @author Markus Holtermann
  */
 public class UbuntuOneConnector implements IStorageConnector {
 
 	/**
 	 * The user name at UbuntuOne. This has to be the users email address
 	 */
 	private String username = null;
 	/**
 	 * The users password
 	 */
 	private String password = null;
 	/**
 	 * Customer token
 	 */
 	private Token ctoken = null;
 	/**
 	 * Standard application token
 	 */
 	private Token stoken = null;
 
 	/**
 	 * The {@link OAuthService}
 	 */
 	private UbuntuOneService service;
 
 	/**
 	 * A reference to the current config;
 	 */
 	private ICloudRAIDConfig config = null;
 	private int id = -1;
 	private String splitOutputDir = null;
 
 	@Override
 	public boolean connect() {
 		if (this.ctoken != null && this.stoken != null) {
 			// We already have the two token sets and will try to use them
 			this.service = (UbuntuOneService) new ServiceBuilder()
 					.provider(UbuntuOneApi.class)
 					.apiKey(this.ctoken.getToken())
 					.apiSecret(this.ctoken.getSecret()).build();
 		} else {
 			this.service = (UbuntuOneService) new ServiceBuilder()
 					.provider(UbuntuOneApi.class).apiKey(this.username)
 					.apiSecret(this.password).build();
 			this.stoken = this.service.getRequestToken();
 			this.ctoken = this.service.getAccessToken(this.stoken);
 		}
 
 		Response response = sendRequest(Verb.GET,
 				this.service.getApiBaseEndpoint() + "/account/");
 		if (response.getCode() == 200) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * This function initializes the {@link UbuntuOneConnector} with the
 	 * customer and application tokens. During the {@link #connect()} various
 	 * tokens are used. If {@link #connect()} returns <code>false</code>, this
 	 * class has to be re-instantiated and initialized with user name and
 	 * password.
 	 * 
 	 * The {@link ICloudRAIDConfig} must either contain:
 	 * <ul>
 	 * <li><code>connector.ID.customer_key</li>
 	 * <li><code>connector.ID.customer_secret</code></li>
 	 * <li><code>connector.ID.token_key</code></li>
 	 * <li><code>connector.ID.token_secret</code></li>
 	 * </ul>
 	 * or
 	 * <ul>
 	 * <li><code>connector.ID.username</code></li>
 	 * <li><code>connector.ID.password</code></li>
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
 		String kCustomerKey = String.format("connector.%d.customer_key",
 				this.id);
 		String kCustomerSecret = String.format("connector.%d.customer_secret",
 				this.id);
 		String kTokenKey = String.format("connector.%d.token_key", this.id);
 		String kTokenSecret = String.format("connector.%d.token_secret",
 				this.id);
 		String kUsername = String.format("connector.%d.username", this.id);
 		String kPassword = String.format("connector.%d.password", this.id);
 		try {
 			this.splitOutputDir = this.config.getString("split.output.dir");
 			if (this.config.keyExists(kCustomerKey)
 					&& this.config.keyExists(kCustomerSecret)
 					&& this.config.keyExists(kTokenKey)
 					&& this.config.keyExists(kTokenSecret)) {
 				this.ctoken = new Token(this.config.getString(kCustomerKey),
 						this.config.getString(kCustomerSecret));
 				this.stoken = new Token(this.config.getString(kTokenKey),
 						this.config.getString(kTokenSecret));
 			} else if (this.config.keyExists(kUsername)
 					&& this.config.keyExists(kPassword)) {
 				this.username = this.config.getString(kUsername);
 				this.password = this.config.getString(kPassword);
 			} else {
 				throw new InstantiationException("Either " + kCustomerKey
 						+ ", " + kCustomerSecret + ", " + kTokenKey + " and "
 						+ kTokenSecret + " or " + kUsername + " and "
 						+ kPassword + " have to be set in the config!");
 			}
 		} catch (MissingConfigValueException e) {
 			e.printStackTrace();
 			throw new InstantiationException(e.getMessage());
 		}
 		return this;
 	}
 
 	@Override
 	public boolean delete(String resource) {
 		Response response = sendRequest(
 				Verb.DELETE,
 				this.service.getFileStorageEndpoint()
 						+ "/~/Ubuntu%20One/"
 						+ OAuthEncoder.encode(resource + "."
 								+ String.valueOf(this.id)));
 		boolean ret = (response.getCode() == 200 || response.getCode() == 404);
 		sendRequest(Verb.DELETE, this.service.getFileStorageEndpoint()
 				+ "/~/Ubuntu%20One/" + OAuthEncoder.encode(resource + ".m"));
 		return ret;
 	}
 
 	@Override
 	public void disconnect() {
 	}
 
 	@Override
 	public InputStream get(String resource) {
 		Response response = sendRequest(Verb.GET,
 				this.service.getContentRootEndpoint() + "/~/Ubuntu%20One/"
 						+ resource + "." + this.id);
 		if (response.getCode() == 200) {
 			return response.getStream();
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public String getMetadata(String resource) {
 		Response response = sendRequest(Verb.GET,
 				this.service.getContentRootEndpoint() + "/~/Ubuntu%20One/"
 						+ resource + ".m");
 		if (response.getCode() == 200) {
 			return response.getBody();
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Executes the actual upload to the UbuntuOne severs.
 	 * 
 	 * @param resource
 	 *            The resource.
 	 * @param extension
 	 *            The resource's extension.
 	 * @return true, if the upload succeeded; false, if not.
 	 */
 	private boolean performUpload(String resource, String extension) {
		File f = new File(this.splitOutputDir + "/" + resource + "."
				+ extension);
 		int maxFilesize;
 		try {
 			maxFilesize = this.config.getInt("filesize.max", null);
 			if (f.length() > maxFilesize) {
 				System.err.println("File too big");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		if (f.length() > maxFilesize) {
 			System.err.println("File too big.");
 		} else {
 			byte[] fileBytes = new byte[(int) f.length()];
 			InputStream fis = null;
 			try {
 				fis = new FileInputStream(f);
 				fis.read(fileBytes);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			} finally {
 				try {
 					if (fis != null) {
 						fis.close();
 					}
 				} catch (IOException ignore) {
 				}
 			}
 			Response response = sendRequest(Verb.PUT,
 					this.service.getContentRootEndpoint() + "/~/Ubuntu%20One/"
 							+ OAuthEncoder.encode(resource), fileBytes);
			if (response.getCode() == 201) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Creates a {@link org.scribe.model.OAuthRequest} to <code>endpoint</code>
 	 * as a HTTP <code>verb</code> Request Method. The request is signed with
 	 * the secret customer and application keys.
 	 * 
 	 * HTTP Request Methods: http://tools.ietf.org/html/rfc2616#section-5.1.1
 	 * 
 	 * @param verb
 	 *            The HTTP Request Method
 	 * @param endpoint
 	 *            The endpoint URL
 	 * @return Returns the corresponding response object to the request
 	 */
 	private Response sendRequest(Verb verb, String endpoint) {
 		System.err.flush();
 		OAuthRequest request = new OAuthRequest(verb, endpoint);
 		System.err.println(request);
 		this.service.signRequest(this.stoken, request);
 		Response response = request.send();
 		System.err.println(String.format("@Response(%d, %s, %s)",
 				response.getCode(), verb, endpoint));
 		System.err.flush();
 		return response;
 	}
 
 	/**
 	 * Add a payload / body content to the request
 	 * 
 	 * @see de.dhbw_mannheim.cloudraid.core.impl.net.connector.UbuntuOneConnector#sendRequest(Verb,
 	 *      String)
 	 * @param verb
 	 *            The HTTP method
 	 * @param endpoint
 	 *            The URL that is called
 	 * @param body
 	 *            The payload
 	 * @return Returns the response of getting the specified endpoint with the
 	 *         given method.
 	 */
 	private Response sendRequest(Verb verb, String endpoint, byte[] body) {
 		System.err.flush();
 		OAuthRequest request = new OAuthRequest(verb, endpoint);
 		System.err.println(request);
 		this.service.signRequest(this.stoken, request);
 		request.addPayload(body);
 		Response response = request.send();
 		System.err.println(String.format("@Response(%d, %s, %s)",
 				response.getCode(), verb, endpoint));
 		System.err.flush();
 		return response;
 	}
 
 	@Override
 	public boolean update(String resource) {
 		InputStream is = get(resource);
 		if (is == null) {
 			return false;
 		}
 		try {
 			is.close();
 		} catch (IOException ignore) {
 		}
 		if (performUpload(resource, String.valueOf(this.id))) {
 			if (performUpload(resource, "m")) {
 				return true;
 			} else {// TODO: check
 				delete(resource);
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public boolean upload(String resource) {
 		InputStream is = get(resource);
 		if (is != null) {
 			try {
 				is.close();
 			} catch (IOException ignore) {
 			}
 			return false;
 		}
 		if (performUpload(resource, String.valueOf(this.id))) {
 			if (performUpload(resource, "m")) {
 				return true;
 			} else { // TODO: check
 				delete(resource);
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 }

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
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 
 import javax.activation.MimetypesFileTypeMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
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
 
 /**
  * The API wrapper for Dropbox (API version 1).
  * 
  * @author Florian Bausch
  * 
  */
 public class DropboxConnector implements IStorageConnector {
 	private static final String ROOT_NAME = "sandbox";
 	private static final String DELETE_URL = "https://api.dropbox.com/1/fileops/delete?root="
 			+ DropboxConnector.ROOT_NAME + "&path=";
 	private static final String GET_URL = "https://api-content.dropbox.com/1/files/"
 			+ DropboxConnector.ROOT_NAME + "/";
 	private static final String PUT_URL = "https://api-content.dropbox.com/1/files_put/"
 			+ DropboxConnector.ROOT_NAME + "/";
 	private static final String META_URL = "https://api.dropbox.com/1/metadata/"
 			+ DropboxConnector.ROOT_NAME + "/";
 	private static final String RESTORE_URL = "https://api.dropbox.com/1/restore/"
 			+ DropboxConnector.ROOT_NAME + "/";
 	private static final String REVISION_URL = "https://api.dropbox.com/1/revisions/"
 			+ DropboxConnector.ROOT_NAME + "/";
 
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
 		this.service = new ServiceBuilder().provider(DropBoxApi.class)
 				.apiKey(this.appKey).apiSecret(this.appSecret).build();
 		if (this.accessTokenValue == null || this.accessTokenSecret == null) {
 			Token requestToken = this.service.getRequestToken();
 			System.out
 					.println("Please go to "
 							+ this.service.getAuthorizationUrl(requestToken)
 							+ " , authorize the app and then press enter in this window.");
 			try {
 				Thread.sleep(9000);
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 			Verifier verifier = new Verifier("");
 			this.accessToken = this.service.getAccessToken(requestToken,
 					verifier);
 			this.accessTokenSecret = this.accessToken.getSecret();
 			this.accessTokenValue = this.accessToken.getToken();
 			this.config.put(
 					String.format("connector.%d.accessTokenValue", this.id),
 					this.accessTokenValue, true);
 			this.config.put(
 					String.format("connector.%d.accessTokenSecret", this.id),
 					this.accessTokenSecret, true);
 			this.config.save();
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
 			this.splitOutputDir = this.config.getString("split.output.dir");
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
 	public boolean delete(String resource) {
 		boolean ret = performDelete(resource, String.valueOf(this.id));
 		if (ret) {
 			if (!performDelete(resource, "m")) {
 				System.err
 						.println("The data file has been removed. But unfortunately the meta data file has not been removed!");
 			}
 		}
 		return ret;
 	}
 
 	@Override
 	public void disconnect() {
 	}
 
 	@Override
 	public InputStream get(String resource) {
 		Response response = performGet(resource, String.valueOf(this.id));
 		if (response == null) {
 			return null;
 		}
 		return response.getStream();
 	}
 
 	@Override
 	public byte[] getMetadata(String resource, int size) {
 		Response response = performGet(resource, "m");
 		if (response == null) {
 			return null;
 		}
 		BufferedInputStream bis = new BufferedInputStream(response.getStream());
 		byte meta[] = new byte[size];
 		Arrays.fill(meta, (byte) 0);
 		try {
 			bis.read(meta, 0, size);
 		} catch (IOException ignore) {
 			meta = null;
 		} finally {
 			try {
 				bis.close();
 			} catch (Exception ignore) {
 			}
 		}
 		return meta;
 	}
 
 	/**
 	 * Restores a file, if the meta data say that that it is deleted
 	 * ("is_deleted" must be "true"). This must be done to be able to overwrite
 	 * a deleted file on the Dropbox servers.
 	 * 
 	 * @param resource
 	 *            The resource name.
 	 * @param extension
 	 *            The file extension.
 	 */
 	private void maybeRestoreFile(String resource, String extension) {
 		OAuthRequest request = new OAuthRequest(GET,
 				DropboxConnector.REVISION_URL + resource + "." + extension);
 		this.service.signRequest(this.accessToken, request);
 		Response response = request.send();
 		System.out.println("Retrieve metadata for " + resource + "."
 				+ extension);
 		System.out.println(response.getCode());
 		System.out.println(response.getBody());
 		if (response.getCode() != 200) {
 			return;
 		}
 		try {
 			JSONArray jsonarray = new JSONArray(response.getBody());
			for (int i = 0; i < jsonarray.length(); i++) {
 				JSONObject jsonbody = jsonarray.getJSONObject(i);
 				System.out.println(jsonbody);
				if (jsonbody.optBoolean("is_deleted")) {
					continue;
 				}
				String rev = jsonbody.getString("rev");
				request = new OAuthRequest(POST, DropboxConnector.RESTORE_URL
						+ resource + "." + extension + "?rev=" + rev);
				this.service.signRequest(this.accessToken, request);
				response = request.send();
				System.out.println("Restore revision " + rev + " for "
						+ resource + "." + extension);
				System.out.println(response.getCode());
				System.out.println(response.getBody());
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return;
 	}
 
 	/**
 	 * Deletes a file from the Dropbox servers.
 	 * 
 	 * @param resource
 	 *            The hash of the file
 	 * @return true, if the file could be deleted; false, if not.
 	 */
 	private boolean performDelete(String resource, String extension) {
 		System.out.println("DELETE " + resource + "." + extension);
 		// This request has to be sent as "POST" not as "DELETE"
 		OAuthRequest request = new OAuthRequest(POST,
 				DropboxConnector.DELETE_URL + resource + "." + extension);
 		this.service.signRequest(this.accessToken, request);
 		Response response = request.send();
 		System.out.println(response.getCode() + " " + response.getBody());
 		if (response.getCode() == 406) {
 			System.err.println("Would delete too many files");
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Gets a file from the Dropbox servers.
 	 * 
 	 * @param resource
 	 *            The hash in the merge.input.dir
 	 * @return the {@link Response} of the GET request on success, else null.
 	 */
 	private Response performGet(String resource, String extension) {
 		System.out.println("GET " + resource + "." + extension);
 		OAuthRequest request = new OAuthRequest(GET, DropboxConnector.GET_URL
 				+ resource + "." + extension);
 		this.service.signRequest(this.accessToken, request);
 		Response response = request.send();
 		System.out.println(response.getCode());
 		if (response.getCode() == 404) {
 			return null;
 		}
 		return response;
 	}
 
 	/**
 	 * Uploads a file to the Dropbox servers.
 	 * 
 	 * @param resource
 	 *            The hash in the split.output.dir
 	 * @return true, if the file could be uploaded; false, if not.
 	 */
 	private boolean performUpload(String resource, String extension) {
 		File f = new File(this.splitOutputDir + "/" + resource + "."
 				+ extension);
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
 		OAuthRequest request = new OAuthRequest(PUT, DropboxConnector.PUT_URL
 				+ resource + "." + extension + "?overwrite=true");
 		request.addHeader("Content-Type",
 				DropboxConnector.MIME_MAP.getContentType(f));
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
 		System.out.println("Update " + resource + "." + this.id);
 		OAuthRequest request = new OAuthRequest(GET, DropboxConnector.META_URL
 				+ resource + "." + this.id);
 		this.service.signRequest(this.accessToken, request);
 		Response response = request.send();
 		System.out.println(response.getCode());
 		boolean ret;
 		if (response.getCode() == 404) {
 			return false;
 		}
 		ret = performUpload(resource, String.valueOf(this.id));
 		if (ret) {
 			System.out.println("Upload (and overwrite) " + resource + ".m");
 			// If the upload of the data file succeeded, the meta data file must
 			// be uploaded
 			ret = performUpload(resource, "m");
 			if (!ret) {
 				// If the meta data cannot be uploaded we will remove the data
 				// file
 				delete(resource);
 			}
 		}
 		return ret;
 	}
 
 	@Override
 	public boolean upload(String resource) {
 		maybeRestoreFile(resource, String.valueOf(this.id));
 		maybeRestoreFile(resource, "m");
 		System.out.println("Upload " + resource + "." + this.id);
 		OAuthRequest request = new OAuthRequest(GET, DropboxConnector.META_URL
 				+ resource + "." + this.id);
 		this.service.signRequest(this.accessToken, request);
 		Response response = request.send();
 		System.out.println(response.getCode());
 		System.out.println(response.getBody());
 		boolean ret = false;
 		if (response.getCode() == 404) {
 			ret = performUpload(resource, String.valueOf(this.id));
 			if (ret) {
 				System.out.println("Upload (and overwrite) " + resource + ".m");
 				// If the upload of the data file succeeded, the meta data file
 				// must be uploaded
 				ret = performUpload(resource, "m");
 				if (!ret) {
 					// If the meta data cannot be uploaded we will remove the
 					// data file
 					delete(resource);
 				}
 			}
 		}
 		return ret;
 	}
 }

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
 
 package de.dhbw_mannheim.cloudraid.api.impl;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.ServiceReference;
 
 import de.dhbw_mannheim.cloudraid.api.impl.RestApiUrlMapping.MatchResult;
 import de.dhbw_mannheim.cloudraid.api.impl.responses.IRestApiResponse;
 import de.dhbw_mannheim.cloudraid.api.impl.responses.PlainApiResponse;
 import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
 import de.dhbw_mannheim.cloudraid.core.ICloudRAIDService;
 import de.dhbw_mannheim.cloudraid.core.ICoreAccess;
 import de.dhbw_mannheim.cloudraid.metadatamgr.IMetadataManager;
 
 /**
  * @author Markus Holtermann, Florian Bausch
  * 
  */
 public class RestApiServlet extends HttpServlet {
 
 	/**
 	 * Stores all URL mappings.
 	 */
 	private static ArrayList<RestApiUrlMapping> mappings = new ArrayList<RestApiUrlMapping>();
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1967811240645738359L;
 
 	/**
 	 * Indicates the version of the API.
 	 */
 	private static final String API_VERSION = "0.1";
 
 	private ICloudRAIDConfig config;
 	private Pattern userpattern;
 
 	/**
 	 * A reference to the database that is used
 	 */
 	private IMetadataManager metadata = null;
 
 	private ICloudRAIDService coreService = null;
 
 	/**
 	 * Initializes all URL mappings and stores a reference to the
 	 * {@link IMetadataManager}
 	 * 
 	 * @throws IllegalArgumentException
 	 *             Thrown if the pattern or the function is invalid.
 	 * @throws SecurityException
 	 *             Thrown if the function cannot be accessed
 	 * @throws NoSuchMethodException
 	 *             Thrown if no such function can be found
 	 * @throws InstantiationException
 	 *             Thrown if at least one of {@link IMetadataManager},
 	 *             {@link ICloudRAIDConfig} or {@link ICloudRAIDService} cannot
 	 *             be found.
 	 */
 	public RestApiServlet() throws IllegalArgumentException, SecurityException,
 			NoSuchMethodException, InstantiationException {
 		RestApiServlet.mappings.add(new RestApiUrlMapping("^/api/info/$",
 				"GET", RestApiServlet.class, "apiInfo"));
 		RestApiServlet.mappings.add(new RestApiUrlMapping("^/file/([^/]+)/$",
 				"DELETE", RestApiServlet.class, "fileDelete"));
 		RestApiServlet.mappings.add(new RestApiUrlMapping("^/file/([^/]+)/$",
 				"GET", RestApiServlet.class, "fileDownload"));
 		RestApiServlet.mappings.add(new RestApiUrlMapping("^/file/([^/]+)/$",
 				"PUT", RestApiServlet.class, "fileNew"));
 		RestApiServlet.mappings.add(new RestApiUrlMapping(
 				"^/file/([^/]+)/info/$", "GET", RestApiServlet.class,
 				"fileInfo"));
 		RestApiServlet.mappings.add(new RestApiUrlMapping(
 				"^/file/([^/]+)/update/$", "PUT", RestApiServlet.class,
 				"fileUpdate"));
 		RestApiServlet.mappings.add(new RestApiUrlMapping("^/list/$", "GET",
 				RestApiServlet.class, "list"));
 		RestApiServlet.mappings.add(new RestApiUrlMapping("^/user/add/$",
 				"POST", RestApiServlet.class, "userAdd"));
 		RestApiServlet.mappings.add(new RestApiUrlMapping("^/user/auth/$",
 				"POST", RestApiServlet.class, "userAuth"));
 		RestApiServlet.mappings.add(new RestApiUrlMapping(
 				"^/user/auth/logout/$", "GET", RestApiServlet.class,
 				"userLogout"));
 		RestApiServlet.mappings.add(new RestApiUrlMapping("^/user/chgpw/$",
 				"POST", RestApiServlet.class, "userChangePass"));
 		RestApiServlet.mappings.add(new RestApiUrlMapping("^/user/del/$",
 				"DELETE", RestApiServlet.class, "userDelete"));
 
 		BundleContext ctx = FrameworkUtil.getBundle(RestApiServlet.class)
 				.getBundleContext();
 
 		ServiceReference<IMetadataManager> srm = ctx
 				.getServiceReference(IMetadataManager.class);
 		this.metadata = ctx.getService(srm);
 		if (this.metadata == null) {
 			throw new InstantiationException(
 					"No running metadata manager found");
 		}
 
 		ServiceReference<ICloudRAIDConfig> src = ctx
 				.getServiceReference(ICloudRAIDConfig.class);
 		this.config = ctx.getService(src);
 		if (this.config == null) {
 			throw new InstantiationException("No running config found");
 		}
 
 		ServiceReference<ICloudRAIDService> srcore = ctx
 				.getServiceReference(ICloudRAIDService.class);
 		this.coreService = ctx.getService(srcore);
 		if (this.coreService == null) {
 			throw new InstantiationException("No core service found");
 		}
 
		this.userpattern = Pattern.compile("[a-zA-Z0-9]+");
 	}
 
 	/**
 	 * View to get the API information. Method must be <code>GET</code> and path
 	 * pattern <code>^/api/info/$</code>.
 	 * 
 	 * @param req
 	 *            The request. Needs no HTTP header attributes.
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>200 - Success</li>
 	 *            </ul>
 	 * @param args
 	 *            No arguments.
 	 */
 	public void apiInfo(HttpServletRequest req, IRestApiResponse resp,
 			ArrayList<String> args) {
 		resp.setStatusCode(200);
 		resp.writeField("Core-Service",
 				bundleVersionFromClass(this.coreService.getClass()));
 		resp.writeField(
 				"RAID-Version",
 				this.coreService.getRAIDName() + " v"
 						+ this.coreService.getRAIDVersion() + " by "
 						+ this.coreService.getRAIDVendor());
 		resp.writeField("Metadata-Service",
 				bundleVersionFromClass(this.metadata.getClass()));
 		resp.writeField("Configuration-Service",
 				bundleVersionFromClass(this.config.getClass()));
 		resp.writeField("API-Service", bundleVersionFromClass(this.getClass()));
 
 		Object[] services = this.coreService.getStorageConnectors();
 		for (int i = 0; i < 3; i++) {
 			resp.writeField("Storage-Connector-" + i,
 					bundleVersionFromClass(services[i].getClass()));
 		}
 
 		resp.writeField("API-Version", RestApiServlet.API_VERSION);
 	}
 
 	private String bundleVersionFromClass(Class<?> klass) {
 		try {
 			Bundle b = FrameworkUtil.getBundle(klass);
 			return b.getSymbolicName() + " v" + b.getVersion() + " by "
 					+ b.getHeaders().get("Bundle-Vendor");
 		} catch (Exception e) {
 			return "unknown";
 		}
 	}
 
 	@Override
 	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		this.doRequest(req, resp);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		this.doRequest(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		this.doRequest(req, resp);
 	}
 
 	@Override
 	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		this.doRequest(req, resp);
 	}
 
 	/**
 	 * @param req
 	 *            The request
 	 * @param resp
 	 *            The response. The status code of the response might be
 	 *            overwritten by views
 	 * @throws IOException
 	 *             Thrown if the response cannot be written and send
 	 */
 	protected void doRequest(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		IRestApiResponse r;
 		r = new PlainApiResponse();
 		r.setResponseObject(resp);
 		resp.addHeader("X-Powered-By", "CloudRAID/"
 				+ RestApiServlet.API_VERSION);
 
 		MatchResult mr = null;
 		for (RestApiUrlMapping mapping : RestApiServlet.mappings) {
 			mr = mapping.match(req);
 			if (null != mr) {
 				System.out.println(mapping);
 				try {
 					mr.getFunction().invoke(this, req, r, mr.getArgs());
 				} catch (IllegalArgumentException e) {
 					RestApiComponent.error500(req, r, e.getMessage());
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					RestApiComponent.error500(req, r, e.getMessage());
 					e.printStackTrace();
 				} catch (InvocationTargetException e) {
 					RestApiComponent.error500(req, r, e.getMessage());
 					e.printStackTrace();
 				}
 				break;
 			}
 		}
 		if (null == mr) { // nothing matched
 			RestApiComponent.error404(req, r);
 		}
 
 		r.send();
 	}
 
 	/**
 	 * View to delete a file. Method must be <code>DELETE</code> and path
 	 * pattern <code>^/file/([^/]+)/$</code>.
 	 * 
 	 * @param req
 	 *            The request. Needs following HTTP header attributes:
 	 *            <ul>
 	 *            <li><code>Cookie: NAME=VALUE</code></li>
 	 *            </ul>
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>200 - Success</li>
 	 *            <li>401 - Not logged in</li>
 	 *            <li>404 - File not found</li>
 	 *            <li>405 - Session id not submitted via cookie</li>
 	 *            <li>500 - Error deleting the file</li>
 	 *            <li>503 - Session does not exist</li>
 	 *            </ul>
 	 * @param args
 	 *            <ol>
 	 *            <li>The filename</li>
 	 *            </ol>
 	 */
 	public void fileDelete(HttpServletRequest req, IRestApiResponse resp,
 			ArrayList<String> args) {
 		if (!this.validateSession(req, resp)) {
 			return;
 		}
 		String path = args.get(0);
 		HttpSession s = req.getSession();
 		int userid = (Integer) s.getAttribute("userid");
 		ResultSet rs = this.metadata.fileGet(path, userid);
 		if (rs == null) {
 			resp.setStatusCode(404);
 			return;
 		}
 		try {
 			int fileid = rs.getInt("id");
 			ICoreAccess slot = this.coreService.getSlot();
 			if (slot.deleteData(fileid)) {
 				resp.setStatusCode(200);
 				return;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		}
 		resp.setStatusCode(500);
 	}
 
 	/**
 	 * View to download a file. Method must be <code>GET</code> and path pattern
 	 * <code>^/file/([^/]+)/$</code>.
 	 * 
 	 * @param req
 	 *            The request. Needs following HTTP header attributes:
 	 *            <ul>
 	 *            <li><code>Cookie: NAME=VALUE</code></li>
 	 *            </ul>
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>200 - Success</li>
 	 *            <li>401 - Not logged in</li>
 	 *            <li>404 - File not found</li>
 	 *            <li>405 - Session id not submitted via cookie</li>
 	 *            <li>500 - Error retrieving the file data</li>
 	 *            <li>503 - Session does not exist</li>
 	 *            </ul>
 	 * @param args
 	 *            <ol>
 	 *            <li>The filename</li>
 	 *            </ol>
 	 */
 	public void fileDownload(HttpServletRequest req, IRestApiResponse resp,
 			ArrayList<String> args) {
 		if (!this.validateSession(req, resp)) {
 			return;
 		}
 
 		String path = args.get(0);
 		HttpSession s = req.getSession();
 		int userid = (Integer) s.getAttribute("userid");
 		ResultSet rs = this.metadata.fileGet(path, userid);
 		if (rs == null) {
 			resp.setStatusCode(404);
 			return;
 		}
 		ICoreAccess slot = null;
 		int statusCode = 500;
 		BufferedInputStream bis = null;
 		BufferedOutputStream bos = null;
 		try {
 			slot = this.coreService.getSlot();
 			int fileid = rs.getInt("id");
 			int bufsize = 4096;
 
 			InputStream is = slot.getData(fileid);
 			bis = new BufferedInputStream(is, bufsize);
 			bos = new BufferedOutputStream(resp.getOutputStream(), bufsize);
 			byte[] inputBytes = new byte[bufsize];
 			int readLength;
 			int written = 0;
 			while ((readLength = bis.read(inputBytes)) != -1) {
 				bos.write(inputBytes, 0, readLength);
 				written += readLength;
 			}
 			resp.setContentLength(written);
 			resp.setContentType("application/octet-stream");
 			resp.flush();
 
 			statusCode = 200;
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (bis != null) {
 					bis.close();
 				}
 			} catch (IOException ignore) {
 			}
 			try {
 				if (bos != null) {
 					bos.close();
 				}
 			} catch (IOException ignore) {
 			}
 		}
 		resp.setStatusCode(statusCode);
 	}
 
 	/**
 	 * View to show information about a file. Method must be <code>GET</code>
 	 * and path pattern <code>^/file/([^/]+)/info/$</code>.
 	 * 
 	 * @param req
 	 *            The request. Needs following HTTP header attributes:
 	 *            <ul>
 	 *            <li><code>Cookie: NAME=VALUE</code></li>
 	 *            </ul>
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>200 - Success</li>
 	 *            <li>401 - Not logged in</li>
 	 *            <li>404 - File not found</li>
 	 *            <li>405 - Session id not submitted via cookie</li>
 	 *            <li>500 - Error getting the file information</li>
 	 *            <li>503 - Session does not exist</li>
 	 *            </ul>
 	 * @param args
 	 *            <ol>
 	 *            <li>The filename</li>
 	 *            </ol>
 	 */
 	public void fileInfo(HttpServletRequest req, IRestApiResponse resp,
 			ArrayList<String> args) {
 		if (!this.validateSession(req, resp)) {
 			return;
 		}
 		HttpSession s = req.getSession();
 		ResultSet rs = this.metadata.fileGet(args.get(0),
 				(Integer) s.getAttribute("userid"));
 		try {
 			if (rs == null) {
 				resp.setStatusCode(404);
 				return;
 			}
 			resp.writeField("path", rs.getString("path_name"));
 			resp.writeField("hash", rs.getString("hash_name"));
 			resp.writeField("last modification", rs.getString("last_mod"));
 			resp.writeField("status", rs.getString("status"));
 		} catch (SQLException e) {
 			resp.setStatusCode(500);
 			e.printStackTrace();
 			return;
 		}
 		resp.setStatusCode(200);
 	}
 
 	/**
 	 * View to upload a new file. Method must be <code>PUT</code> and path
 	 * pattern <code>^/file/([^/]+)/$</code>.
 	 * 
 	 * @param req
 	 *            The request. Needs following HTTP header attributes:
 	 *            <ul>
 	 *            <li><code>Cookie: NAME=VALUE</code></li>
 	 *            </ul>
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>201 - Success</li>
 	 *            <li>401 - Not logged in</li>
 	 *            <li>405 - Session id not submitted via cookie</li>
 	 *            <li>409 - File already exists</li>
 	 *            <li>411 - Length Required</li>
 	 *            <li>500 - Error adding the file</li>
 	 *            <li>503 - Session does not exist</li>
 	 *            </ul>
 	 * @param args
 	 *            <ol>
 	 *            <li>The filename</li>
 	 *            </ol>
 	 */
 	public void fileNew(HttpServletRequest req, IRestApiResponse resp,
 			ArrayList<String> args) {
 		if (!this.validateSession(req, resp)) {
 			return;
 		}
 		String path = args.get(0);
 		HttpSession s = req.getSession();
 		int userid = (Integer) s.getAttribute("userid");
 		ResultSet rs = this.metadata.fileGet(path, userid);
 		if (rs != null) {
 			resp.setStatusCode(409);
 			return;
 		}
 
 		if (req.getContentLength() < 0) {
 			resp.setStatusCode(411);
 			return;
 		}
 
 		try {
 			ICoreAccess slot = this.coreService.getSlot();
 			int fileid = this.metadata.fileNew(path, "", 0L, userid);
 			if (fileid >= 0) {
 				slot.putData(req.getInputStream(), fileid);
 				resp.setStatusCode(201);
 				return;
 			}
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		resp.setStatusCode(500);
 	}
 
 	/**
 	 * View to update a file. Method must be <code>PUT</code> and path pattern
 	 * <code>^/file/([^/]+)/update/$</code>.
 	 * 
 	 * @param req
 	 *            The request. Needs following HTTP header attributes:
 	 *            <ul>
 	 *            <li><code>Cookie: NAME=VALUE</code></li>
 	 *            </ul>
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>200 - Success</li>
 	 *            <li>401 - Not logged in</li>
 	 *            <li>404 - File not found</li>
 	 *            <li>405 - Session id not submitted via cookie</li>
 	 *            <li>411 - Length Required</li>
 	 *            <li>500 - Error deleting the file</li>
 	 *            <li>503 - Session does not exist</li>
 	 *            </ul>
 	 * @param args
 	 *            <ol>
 	 *            <li>The filename</li>
 	 *            </ol>
 	 */
 	public void fileUpdate(HttpServletRequest req, IRestApiResponse resp,
 			ArrayList<String> args) {
 		if (!this.validateSession(req, resp)) {
 			return;
 		}
 		String path = args.get(0);
 		HttpSession s = req.getSession();
 		int userid = (Integer) s.getAttribute("userid");
 		ResultSet rs = this.metadata.fileGet(path, userid);
 		if (rs == null) {
 			resp.setStatusCode(404);
 			return;
 		}
 		if (req.getContentLength() < 0) {
 			resp.setStatusCode(411);
 			return;
 		}
 
 		try {
 			int fileid = rs.getInt("id");
 			ICoreAccess slot = this.coreService.getSlot();
 			this.metadata.fileUpdate(fileid, path, "", 0L, userid);
 			if (fileid >= 0) {
 				slot.putData(req.getInputStream(), fileid, true);
 				resp.setStatusCode(201);
 				return;
 			}
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		resp.setStatusCode(500);
 	}
 
 	/**
 	 * View to list all files. Method must be <code>GET</code> and path pattern
 	 * <code>^/list/$</code>.
 	 * 
 	 * @param req
 	 *            The request
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>200 - Success</li>
 	 *            <li>401 - Not logged in</li>
 	 *            <li>405 - Session id not submitted via cookie</li>
 	 *            <li>500 - Error getting the list</li>
 	 *            <li>503 - Session does not exists</li>
 	 *            </ul>
 	 * @param args
 	 *            No arguments
 	 */
 	public void list(HttpServletRequest req, IRestApiResponse resp,
 			ArrayList<String> args) {
 		if (!this.validateSession(req, resp)) {
 			return;
 		}
 		HttpSession s = req.getSession();
 		int userid = (Integer) s.getAttribute("userid");
 		ResultSet rs = this.metadata.fileList(userid);
 		if (rs == null) {
 			resp.writeLine("No files uploaded yet.");
 		} else {
 			try {
 				LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
 				while (rs.next()) {
 					map.put("path_name", rs.getString("path_name"));
 					map.put("hash_name", rs.getString("hash_name"));
 					map.put("last_mod", rs.getTimestamp("last_mod").toString());
 					map.put("status", rs.getString("status"));
 					resp.addRow(map);
 				}
 			} catch (SQLException e) {
 				resp.setStatusCode(500);
 				e.printStackTrace();
 				return;
 			}
 		}
 		resp.setStatusCode(200);
 	}
 
 	/**
 	 * View to add a user. Method must be <code>POST</code> and path pattern
 	 * <code>^/user/add/$</code>.
 	 * 
 	 * @param req
 	 *            The request. Needs following HTTP header attributes:
 	 *            <ul>
 	 *            <li><code>X-Username: USERNAME</code></li>
 	 *            <li><code>X-Password: PASSWORD</code></li>
 	 *            <li><code>X-Confirm: CONFIRMATION</code></li>
 	 *            </ul>
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>200 - Success</li>
 	 *            <li>400 - User name and/or password and/or confirmation
 	 *            missing/wrong.</li>
 	 *            <li>406 - Already logged in</li>
 	 *            <li>500 - Error while adding the user to the database</li>
 	 *            </ul>
 	 * @param args
 	 *            No arguments
 	 */
 	public void userAdd(HttpServletRequest req, IRestApiResponse resp,
 			ArrayList<String> args) {
 		if (this.validateSession(req, resp)) {
 			resp.setStatusCode(403);
 			resp.writeLine("Already logged in!");
 			return;
 		}
 		String username = req.getHeader("X-Username");
 		if (!this.userpattern.matcher(username).matches()) {
 			resp.setStatusCode(500);
 			resp.writeLine("Invalid characters in username. Only a-z, A-Z and 0-9 allowed.");
 			return;
 		}
 		String password = req.getHeader("X-Password");
 		if (username == null || password == null) {
 			resp.setStatusCode(400);
 			resp.writeLine("Username or password missing!");
 			return;
 		}
 		if (this.metadata.addUser(username, password)) {
 			resp.setStatusCode(200);
 			resp.writeLine("User created");
 		} else {
 			resp.setStatusCode(500);
 			resp.writeLine("An error occured");
 		}
 	}
 
 	/**
 	 * View to login a user. Method must be <code>POST</code> and path pattern
 	 * <code>^/user/auth/$</code>.
 	 * 
 	 * @param req
 	 *            The request. Needs following HTTP header attributes:
 	 *            <ul>
 	 *            <li><code>X-Username: USERNAME</code></li>
 	 *            <li><code>X-Password: PASSWORD</code></li>
 	 *            </ul>
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>202 - Success</li>
 	 *            <li>400 - User name and/or password missing/wrong.</li>
 	 *            <li>406 - Already logged in / Session exists</li>
 	 *            <li>503 - Session could not be created.</li>
 	 *            </ul>
 	 * @param args
 	 *            No arguments
 	 */
 	public void userAuth(HttpServletRequest req, IRestApiResponse resp,
 			ArrayList<String> args) {
 		String username = req.getHeader("X-Username");
 		String password = req.getHeader("X-Password");
 		password = password + "";
 		HttpSession session = req.getSession(false);
 		if (session != null) {
 			resp.setStatusCode(406);
 			return;
 		}
 		session = req.getSession(true);
 		if (session == null) {
 			resp.setStatusCode(503);
 			return;
 		}
 		int id = this.metadata.authUser(username, password);
 		if (id > -1) {
 			session.setAttribute("auth", true);
 			session.setAttribute("username", username);
 			session.setAttribute("userid", id);
 			resp.setStatusCode(202);
 			return;
 		} else {
 			session.invalidate();
 			resp.setStatusCode(403);
 			resp.writeLine("Credentials invalid!");
 			return;
 		}
 	}
 
 	/**
 	 * View to change the password of a user. Method must be <code>POST</code>
 	 * and path pattern <code>^/user/chgpw/$</code>.
 	 * 
 	 * @param req
 	 *            The request. Needs following HTTP header attributes:
 	 *            <ul>
 	 *            <li><code>Cookie: NAME=VALUE</code></li>
 	 *            <li><code>X-Username: USERNAME</code></li>
 	 *            <li><code>X-Password: PASSWORD</code></li>
 	 *            <li><code>X-Confirm: CONFIRMATION</code></li>
 	 *            </ul>
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>200 - Success</li>
 	 *            <li>400 - User name and/or password and/or confirmation
 	 *            missing/wrong.</li>
 	 *            <li>401 - Not logged in</li>
 	 *            <li>405 - Session id not submitted via cookie</li>
 	 *            <li>500 - Error while updating the user record</li>
 	 *            <li>503 - Session does not exist</li>
 	 *            </ul>
 	 * @param args
 	 *            No arguments
 	 */
 	public void userChangePass(HttpServletRequest req, IRestApiResponse resp,
 			ArrayList<String> args) {
 		if (!this.validateSession(req, resp)) {
 			return;
 		}
 		HttpSession s = req.getSession();
 		int userId = (Integer) s.getAttribute("userid");
 		String username = req.getHeader("X-Username");
 		String password = req.getHeader("X-Password");
 		String confirm = req.getHeader("X-Confirm");
 		if (username == null || password == null || confirm == null
 				|| !password.equals(confirm)) {
 			resp.setStatusCode(400);
 			resp.writeLine("Username, password, or password confirmation missing or passwords do not match!");
 		}
 		if (this.metadata.changeUserPwd(username, password, userId)) {
 			resp.setStatusCode(200);
 			resp.writeLine("The password was changed successfully");
 		} else {
 			resp.setStatusCode(500);
 			resp.writeLine("Error while updating the user record");
 		}
 	}
 
 	/**
 	 * View to delete a user. Method must be <code>DELETE</code> and path
 	 * pattern <code>^/user/del/$</code>.
 	 * 
 	 * @param req
 	 *            The request. Needs following HTTP header attributes:
 	 *            <ul>
 	 *            <li><code>Cookie: NAME=VALUE</code></li>
 	 *            <li><code>X-Username: USERNAME</code></li>
 	 *            <li><code>X-Password: PASSWORD</code></li>
 	 *            </ul>
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>200 - Success</li>
 	 *            <li>400 - User name and/or password missing/wrong.</li>
 	 *            <li>401 - Not logged in405Session id not submitted via cookie</li>
 	 *            <li>500 - Error while updating the user record</li>
 	 *            <li>503 - Session does not exist</li>
 	 *            </ul>
 	 * @param args
 	 *            No arguments
 	 */
 	public void userDelete(HttpServletRequest req, IRestApiResponse resp,
 			ArrayList<String> args) {
 		if (!this.validateSession(req, resp)) {
 			return;
 		}
 		resp.setStatusCode(501);
 		resp.writeLine("Not implemented!");
 	}
 
 	/**
 	 * View to logout a user. Method must be <code>GET</code> and path pattern
 	 * <code>^/user/auth/logout/$</code>.
 	 * 
 	 * @param req
 	 *            The request. Needs following HTTP header attributes:
 	 *            <ul>
 	 *            <li><code>Cookie: NAME=VALUE</code></li>
 	 *            </ul>
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>200 - Success</li>
 	 *            <li>401 - Not logged in</li>
 	 *            <li>405 - Session id not submitted via cookie</li>
 	 *            <li>503 - Session does not exist</li>
 	 *            </ul>
 	 * @param args
 	 *            No arguments
 	 */
 	public void userLogout(HttpServletRequest req, IRestApiResponse resp,
 			ArrayList<String> args) {
 		if (!this.validateSession(req, resp)) {
 			return;
 		}
 		req.getSession().invalidate();
 		resp.setStatusCode(200);
 		return;
 	}
 
 	/**
 	 * @param req
 	 *            The request
 	 * @param resp
 	 *            Status codes:
 	 *            <ul>
 	 *            <li>401 - Not logged in</li>
 	 *            <li>405 - Session id not submitted via cookie</li>
 	 *            <li>503 - Session does not exist</li>
 	 *            </ul>
 	 * @return True if and only if all of the following points are true:
 	 *         <ul>
 	 *         <li>There is an existing session</li>
 	 *         <li>The session id is taken from a cookie</li>
 	 *         <li>The value of the session attribute <code>auth</code> is
 	 *         <code>true</code></li>
 	 *         </ul>
 	 */
 	private boolean validateSession(HttpServletRequest req,
 			IRestApiResponse resp) {
 		HttpSession session = req.getSession(false);
 		if (session == null) {
 			resp.setStatusCode(503);
 			resp.writeLine("Session does not exist!");
 			return false;
 		}
 		if (!req.isRequestedSessionIdFromCookie()) {
 			resp.setStatusCode(405);
 			resp.writeLine("Session not submitted via Cookie!");
 			return false;
 		}
 		if (!((Boolean) session.getAttribute("auth"))) {
 			resp.setStatusCode(401);
 			resp.writeLine("Not logged in!");
 			return false;
 		}
 		return true;
 	}
 
 }

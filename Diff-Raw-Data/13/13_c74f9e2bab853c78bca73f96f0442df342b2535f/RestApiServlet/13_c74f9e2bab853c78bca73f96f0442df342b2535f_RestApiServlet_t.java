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
 
 package de.dhbw.mannheim.cloudraid.api;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import de.dhbw.mannheim.cloudraid.api.RestApiUrlMapping.MatchResult;
 import de.dhbw.mannheim.cloudraid.api.responses.IRestApiResponse;
 import de.dhbw.mannheim.cloudraid.api.responses.JsonApiResponse;
 import de.dhbw.mannheim.cloudraid.api.responses.PlainApiResponse;
 import de.dhbw.mannheim.cloudraid.persistence.IDatabaseConnector;
 import de.dhbw.mannheim.cloudraid.util.Config;
 import de.dhbw.mannheim.cloudraid.util.exceptions.InvalidConfigValueException;
 
 /**
  * @author Markus Holtermann
  * 
  */
 public class RestApiServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1967811240645738359L;
 
 	/**
 	 * Stores all URL mappings.
 	 */
 	private static ArrayList<RestApiUrlMapping> mappings = new ArrayList<RestApiUrlMapping>();
 
 	/**
 	 * A reference to the database that is used
 	 */
 	private IDatabaseConnector database = null;
 
 	/**
 	 * Initializes all URL mappings and stores a reference to the
 	 * {@link IDatabaseConnector}
 	 * 
 	 * @param database
 	 *            The {@link IDatabaseConnector} that will be used for all
 	 *            database requests
 	 * @throws IllegalArgumentException
 	 *             Thrown if the pattern or the function is invalid.
 	 * @throws SecurityException
 	 *             Thrown if the function cannot be accessed
 	 * @throws NoSuchMethodException
 	 *             Thrown if no such function can be found
 	 */
 	public RestApiServlet(IDatabaseConnector database)
 			throws IllegalArgumentException, SecurityException,
 			NoSuchMethodException {
 		mappings.add(new RestApiUrlMapping("^/file/([^/]+)/$", "DELETE",
 				RestApiServlet.class, "fileDelete"));
 		mappings.add(new RestApiUrlMapping("^/file/([^/]+)/$", "GET",
 				RestApiServlet.class, "fileDownload"));
 		mappings.add(new RestApiUrlMapping("^/file/([^/]+)/$", "PUT",
 				RestApiServlet.class, "fileNew"));
 		mappings.add(new RestApiUrlMapping("^/file/([^/]+)/info/$", "GET",
 				RestApiServlet.class, "fileInfo"));
 		mappings.add(new RestApiUrlMapping("^/file/([^/]+)/update/$", "PUT",
 				RestApiServlet.class, "fileUpdate"));
 		mappings.add(new RestApiUrlMapping("^/list/$", "GET",
 				RestApiServlet.class, "list"));
 		mappings.add(new RestApiUrlMapping("^/user/add/$", "POST",
 				RestApiServlet.class, "userAdd"));
 		mappings.add(new RestApiUrlMapping("^/user/auth/$", "POST",
 				RestApiServlet.class, "userAuth"));
 		mappings.add(new RestApiUrlMapping("^/user/auth/logout/$", "GET",
 				RestApiServlet.class, "userLogout"));
 		mappings.add(new RestApiUrlMapping("^/user/chgpw/$", "POST",
 				RestApiServlet.class, "userChangePass"));
 		mappings.add(new RestApiUrlMapping("^/user/del/$", "DELETE",
 				RestApiServlet.class, "userDelete"));
 		this.database = database;
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
 		String mime = req.getHeader("Accept");
 		IRestApiResponse r;
 		if (JsonApiResponse.MIMETYPE.startsWith(mime)) {
 			r = new JsonApiResponse();
 		} else if (PlainApiResponse.MIMETYPE.startsWith(mime)) {
 			r = new PlainApiResponse();
 		} else {
 			r = new PlainApiResponse();
 		}
 		r.setResponseObject(resp);
 
 		MatchResult mr = null;
 		for (RestApiUrlMapping mapping : mappings) {
 			System.out.println(mapping);
 			mr = mapping.match(req);
 			if (null != mr) {
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
 		HttpSession s = req.getSession();
 		ResultSet rs = database.fileGet(args.get(0),
 				(Integer) s.getAttribute("userid"));
 		try {
 			if (rs == null) {
 				resp.setStatusCode(404);
 				return;
 			}
 			rs.deleteRow();
 		} catch (SQLException e) {
 			resp.setStatusCode(500);
 			e.printStackTrace();
 			return;
 		}
 		resp.setStatusCode(200);
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
 		HttpSession s = req.getSession();
 		ResultSet rs = database.fileGet(args.get(0),
 				(Integer) s.getAttribute("userid"));
 		try {
 			if (rs == null) {
 				resp.setStatusCode(404);
 				return;
 			}
 			resp.addPayload(rs.getString("hash_name"));
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
 		ResultSet rs = database.fileGet(path, userid);
 		if (rs != null) {
 			resp.setStatusCode(409);
 			return;
 		}
 
 		int bufsize = Math.min(1024, req.getContentLength());
 		if (bufsize < 0) {
 			resp.setStatusCode(411);
 			return;
 		}
 
 		try {
 			BufferedInputStream bis = new BufferedInputStream(
 					req.getInputStream(), bufsize);
 
 			File f = new File(Config.getInstance().getString("split.input.dir",
 					null)
 					+ File.separator + userid + File.separator + path);
 			f.getParentFile().mkdirs();
 
 			BufferedOutputStream bos = new BufferedOutputStream(
 					new FileOutputStream(f), bufsize);
 
 			byte[] inputBytes = new byte[bufsize];
 			int readLength;
 			while ((readLength = bis.read(inputBytes)) >= 0) {
 				bos.write(inputBytes, 0, readLength);
 			}
 			if (database.fileNew(path, "", 0L, userid)) {
 				resp.setStatusCode(201);
 				return;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (NoSuchElementException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigValueException e) {
 			e.printStackTrace();
 		}
 		resp.setStatusCode(500);
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
 	 *            <li>405 - Session id not submitteded via cookie</li>
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
 		ResultSet rs = database.fileGet(args.get(0),
 				(Integer) s.getAttribute("userid"));
 		try {
 			if (rs == null) {
 				resp.setStatusCode(404);
 				return;
 			}
 			resp.addField("path", rs.getString("path_name"));
 			resp.addField("hash", rs.getString("hash_name"));
 			resp.addField("last modification", rs.getString("last_mod"));
 			resp.addField("status", rs.getString("status"));
 		} catch (SQLException e) {
 			resp.setStatusCode(500);
 			e.printStackTrace();
 			return;
 		}
 		resp.setStatusCode(200);
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
 		ResultSet rs = database.fileGet(path, userid);
 		if (rs == null) {
 			resp.setStatusCode(404);
 			return;
 		}
 
 		int bufsize = Math.min(1024, req.getContentLength());
 		if (bufsize < 0) {
 			resp.setStatusCode(411);
 			return;
 		}
 
 		try {
 			BufferedInputStream bis = new BufferedInputStream(
 					req.getInputStream(), bufsize);
 
 			File f = new File(Config.getInstance().getString("split.input.dir",
 					null)
 					+ File.separator + userid + File.separator + path);
 			f.getParentFile().mkdirs();
 
 			BufferedOutputStream bos = new BufferedOutputStream(
 					new FileOutputStream(f), bufsize);
 
 			byte[] inputBytes = new byte[bufsize];
 			int readLength;
 			while ((readLength = bis.read(inputBytes)) >= 0) {
 				bos.write(inputBytes, 0, readLength);
 			}
 			if (database.fileNew(path, "", 0L, userid)) {
 				resp.setStatusCode(200);
 				return;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (NoSuchElementException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidConfigValueException e) {
 			// TODO Auto-generated catch block
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
 		ResultSet rs = database.fileList(userid);
 		if (rs == null) {
 			resp.addPayload("No files uploaded yet.");
 		} else {
 			try {
 				HashMap<String, Object> map = new HashMap<String, Object>();
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
 			resp.addPayload("Already logged in!");
 			return;
 		}
 		String username = req.getHeader("X-Username");
 		String password = req.getHeader("X-Password");
 		if (username == null || password == null) {
 			resp.setStatusCode(401);
 			resp.addPayload("Username or password missing!");
 			return;
 		}
 		if (database.addUser(username, password)) {
 			resp.setStatusCode(200);
 			resp.addPayload("User created");
 		} else {
 			resp.setStatusCode(500);
 			resp.addPayload("An error occured");
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
 		int id = database.authUser(username, password);
 		if (id > -1) {
 			session.setAttribute("auth", true);
 			session.setAttribute("username", username);
 			session.setAttribute("userid", id);
			resp.setStatusCode(202);
			return;
 		} else {
 			session.invalidate();
 			resp.setStatusCode(403);
 			resp.addPayload("Credentials invalid!");
 			return;
 		}
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
 		resp.setStatusCode(501);
 		resp.addPayload("Not implemented!");
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
 	 *            <li>500 - Error while updating the userser record</li>
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
 		resp.addPayload("Not implemented!");
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
 			resp.addPayload("Session does not exist!");
 			return false;
 		}
 		if (!req.isRequestedSessionIdFromCookie()) {
 			resp.setStatusCode(405);
 			resp.addPayload("Session not submitted via Cookie!");
 			return false;
 		}
 		if (!((Boolean) session.getAttribute("auth"))) {
 			resp.setStatusCode(401);
 			resp.addPayload("Not logged in!");
 			return false;
 		}
 		return true;
 	}
 
 }

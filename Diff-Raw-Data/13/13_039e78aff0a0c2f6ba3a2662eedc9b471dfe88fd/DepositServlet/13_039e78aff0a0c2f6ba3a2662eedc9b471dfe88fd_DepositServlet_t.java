 /**
  * Copyright (c) 2007, Aberystwyth University
  *
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions 
  * are met:
  * 
  *  - Redistributions of source code must retain the above 
  *    copyright notice, this list of conditions and the 
  *    following disclaimer.
  *  
  *  - Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in 
  *    the documentation and/or other materials provided with the 
  *    distribution.
  *    
  *  - Neither the name of the Centre for Advanced Software and 
  *    Intelligent Systems (CASIS) nor the names of its 
  *    contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF 
  * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF 
  * SUCH DAMAGE.
  */
 
 package org.purl.sword.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.StringTokenizer;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 import org.purl.sword.base.ChecksumUtils;
 import org.purl.sword.base.Deposit;
 import org.purl.sword.base.DepositResponse;
 import org.purl.sword.base.HttpHeaders;
 import org.purl.sword.base.SWORDAuthenticationException;
 import org.purl.sword.base.SWORDContentTypeException;
 import org.purl.sword.base.SWORDException;
import org.purl.sword.server.SWORDServer;
 
 /**
  * DepositServlet
  * 
  * @author Stuart Lewis
  */
 public class DepositServlet extends HttpServlet {
 
 	/** Sword repository */
 	private SWORDServer myRepository;
 
 	/** Authentication type */
 	private String authN;
 
 	/** Temp directory */
 	private String tempDirectory;
 
 	/** Counter */
 	private static int counter = 0;
 
 	/** Logger */
 	private static Logger log = Logger.getLogger(DepositServlet.class);
 
 	/**
 	 * Initialise the servlet
 	 * 
 	 * @throws ServletException
 	 */
 	public void init() throws ServletException {
 		// Instantiate the correct SWORD Server class
 		String className = getServletContext().getInitParameter("server-class");
 		if (className == null) {
 			log
 					.fatal("Unable to read value of 'sword-server-class' from Servlet context");
 		} else {
 			try {
 				myRepository = (SWORDServer) Class.forName(className)
 						.newInstance();
 				log.info("Using " + className + " as the SWORDServer");
 			} catch (Exception e) {
 				log
 						.fatal("Unable to instantiate class from 'sword-server-class': "
 								+ className);
 				throw new ServletException(
 						"Unable to instantiate class from 'sword-server-class': "
 								+ className);
 			}
 		}
 
 		authN = getServletContext().getInitParameter("authentication-method");
 		if ((authN == null) || (authN.equals(""))) {
 			authN = "None";
 		}
 		log.info("Authentication type set to: " + authN);
 
 		tempDirectory = getServletContext().getInitParameter(
 				"upload-temp-directory");
 		if ((tempDirectory == null) || (tempDirectory.equals(""))) {
 			tempDirectory = System.getProperty("java.io.tmpdir");
 		}
 		File tempDir = new File(tempDirectory);
 		log.info("Upload temporary directory set to: " + tempDir);
 		if (!tempDir.exists()) {
 			if (!tempDir.mkdirs()) {
 				throw new ServletException(
 						"Upload directory did not exist and I can't create it. "
 								+ tempDir);
 			}
 		}
 		if (!tempDir.isDirectory()) {
 			log.fatal("Upload temporary directory is not a directory: "
 					+ tempDir);
 			throw new ServletException(
 					"Upload temporary directory is not a directory: " + tempDir);
 		}
 		if (!tempDir.canWrite()) {
 			log.fatal("Upload temporary directory cannot be written to: "
 					+ tempDir);
 			throw new ServletException(
 					"Upload temporary directory cannot be written to: "
 							+ tempDir);
 		}
 	}
 
 	/**
 	 * Process the Get request. This will return an unimplemented response.
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// Send a '501 Not Implemented'
 		response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
 	}
 
 	/**
 	 * Process a post request.
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// Create the Deposit request
 		Deposit d = new Deposit();
 		Date date = new Date();
 		log.debug("Starting deposit processing at " + date.toString() + " by "
 				+ request.getRemoteAddr());
 
 		// Are there any authentication details?
 		String usernamePassword = getUsernamePassword(request);
 		if ((usernamePassword != null) && (!usernamePassword.equals(""))) {
 			int p = usernamePassword.indexOf(":");
 			if (p != -1) {
 				d.setUsername(usernamePassword.substring(0, p));
 				d.setPassword(usernamePassword.substring(p + 1));
 			}
 		} else if (authenticateWithBasic()) {
 			String s = "Basic realm=\"SWORD\"";
 			response.setHeader("WWW-Authenticate", s);
 			response.setStatus(401);
 			return;
 		}
 
 		// Do the processing
 		try {
 			// Write the file to the temp directory
 			// TODO: Improve the filename creation
 			String filename = tempDirectory + "SWORD-"
 					+ request.getRemoteAddr() + "-" + counter++;
 			InputStream inputStream = request.getInputStream();
 			OutputStream outputStream = new FileOutputStream(filename);
 			int data;
 			while ((data = inputStream.read()) != -1) {
 				outputStream.write(data);
 			}
 			inputStream.close();
 			outputStream.close();
 
 			// Check the MD5 hash
 			String receivedMD5 = ChecksumUtils.generateMD5(filename);
 			log.debug("Received filechecksum: " + receivedMD5);
 			d.setMd5(receivedMD5);
 			String md5 = request.getHeader("Content-MD5");
 			log.debug("Received file checksum header: " + md5);
 			if ((md5 != null) && (!md5.equals(receivedMD5))) {
 				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
 				response.setHeader(HttpHeaders.X_ERROR_CODE,
 						"ErrorChecksumMismatch");
 				log
 						.debug("Bad MD5 for file. Aborting with appropriate error message");
 			} else {
 				// Set the file
 				File f = new File(filename);
 				FileInputStream fis = new FileInputStream(f);
 				d.setFile(fis);
 
 				// Set the X-On-Behalf-Of header
 				d.setOnBehalfOf(request.getHeader(HttpHeaders.X_ON_BEHALF_OF
 						.toString()));
 
 				// Set the X-Format-Namespace header
 				d.setFormatNamespace(request
 						.getHeader(HttpHeaders.X_FORMAT_NAMESPACE));
 
 				// Set the X-No-Op header
 				String noop = request.getHeader(HttpHeaders.X_NO_OP);
 				if ((noop != null) && (noop.equals("true"))) {
 					d.setNoOp(true);
 				} else {
 					d.setNoOp(false);
 				}
 
 				// Set the X-Verbose header
 				String verbose = request.getHeader(HttpHeaders.X_VERBOSE);
 				if ((verbose != null) && (verbose.equals("true"))) {
 					d.setVerbose(true);
 				} else {
 					d.setVerbose(false);
 				}
 
 				// Set the slug
 				String slug = request.getHeader(HttpHeaders.SLUG);
 				if (slug != null) {
 					d.setSlug(slug);
 				}
 
 				// Set the content disposition
 				d.setContentDisposition(request
 						.getHeader(HttpHeaders.CONTENT_DISPOSITION));
 
 				// Set the IP address
 				d.setIPAddress(request.getRemoteAddr());
 
 				// Set the deposit location
 				d.setLocation(getUrl(request));
 
 				// Set the content type
 				d.setContentType(request.getContentType());
 
 				// Set the content length
 				String cl = request.getHeader(HttpHeaders.CONTENT_LENGTH);
 				if ((cl != null) && (!cl.equals(""))) {
 					d.setContentLength(Integer.parseInt(cl));
 				}
 
 				// Get the DepositResponse
 				DepositResponse dr = myRepository.doDeposit(d);
 
 				// Print out the Deposit Response
 				response.setStatus(dr.getHttpResponse());
				response.setContentType("application/atom+xml; charset=UTF-8");
 				PrintWriter out = response.getWriter();
 				out.write(dr.marshall());
 				out.flush();
 
 				// Close the input stream if it still open
 				fis.close();
 
 				// Try deleting the temp file
 				f = new File(filename);
 				f.delete();
 			}
 		} catch (SWORDAuthenticationException sae) {
 			// Ask for credentials
 			if (authN.equals("Basic")) {
 				String s = "Basic realm=\"SWORD\"";
 				response.setHeader("WWW-Authenticate", s);
 				response.setStatus(401);
 			}
 		} catch (SWORDContentTypeException scte) {
 			// Throw a 415
 			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
 		} catch (SWORDException se) {
 			// Throw a HTTP 500
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, se
 					.getMessage());
 
 			// Is there an appropriate error header to return?
 			if (se.getErrorCode() != null) {
 				response.setHeader(HttpHeaders.X_ERROR_CODE, se.getErrorCode());
 			}
 			se.printStackTrace();
 			log.error(se.toString());
 		} catch (IOException ioe) {
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			log.error(ioe.toString());
 		} catch (NoSuchAlgorithmException nsae) {
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			log.error(nsae.toString());
 		}
 
 	}
 
 	/**
 	 * Utiliy method to return the username and password (separated by a colon
 	 * ':')
 	 * 
 	 * @param request
 	 * @return The username and password combination
 	 */
 	private String getUsernamePassword(HttpServletRequest request) {
 		try {
 			String authHeader = request.getHeader("Authorization");
 			if (authHeader != null) {
 				StringTokenizer st = new StringTokenizer(authHeader);
 				if (st.hasMoreTokens()) {
 					String basic = st.nextToken();
 					if (basic.equalsIgnoreCase("Basic")) {
 						String credentials = st.nextToken();
 						String userPass = new String(Base64
 								.decodeBase64(credentials.getBytes()));
 						return userPass;
 					}
 				}
 			}
 		} catch (Exception e) {
 			log.debug(e.toString());
 		}
 		return null;
 	}
 
 	/**
 	 * Utility method to deicde if we are using HTTP Basic authentication
 	 * 
 	 * @return if HTTP Basic authentication is in use or not
 	 */
 	private boolean authenticateWithBasic() {
 		if (authN.equalsIgnoreCase("Basic")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Utility method to construct the URL called for this Servlet
 	 * 
 	 * @param req
 	 *            The request object
 	 * @return The URL
 	 */
 	private static String getUrl(HttpServletRequest req) {
 		String reqUrl = req.getRequestURL().toString();
 		String queryString = req.getQueryString(); // d=789
 		if (queryString != null) {
 			reqUrl += "?" + queryString;
 		}
 		return reqUrl;
 	}
 }

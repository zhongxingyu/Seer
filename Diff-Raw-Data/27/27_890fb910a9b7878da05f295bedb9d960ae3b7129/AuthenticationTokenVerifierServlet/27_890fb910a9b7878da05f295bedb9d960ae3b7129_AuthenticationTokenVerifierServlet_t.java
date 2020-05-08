 package com.netdimensions.servlet;
 
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.Charsets;
 import org.apache.commons.codec.binary.Hex;
 
 public class AuthenticationTokenVerifierServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		try {
 			String userId = request.getParameter("userId");
 			String digestAlgorithm = this.getInitParameter("digestAlgorithm");

			if (key() == null) {
				throw new ServletException("Unconfigured key");
			}
			
			if (relayState(request) == null) {
				throw new ServletException("Unsolicited authentication response");
			}

 			String expected = Hex.encodeHexString(MessageDigest.getInstance(
 					digestAlgorithm == null ? "MD5" : digestAlgorithm).digest(
					(userId + key() + relayState(request).nonce)
 							.getBytes(Charsets.UTF_8)));
 			String actual = request.getParameter("sig");
 
 			if (expected.equals(actual)) {
 				request.getSession().setAttribute("userId", userId);
				response.sendRedirect(relayState(request).target);
 			} else {
 				getServletContext().log(
 						"Expected sig " + expected
 								+ " did not match actual sig " + actual);
 				throw new ServletException("Signature validation failed");
 			}
 		} catch (NoSuchAlgorithmException e) {
 			throw new ServletException(e);
 		}
 	}

	private RelayState relayState(HttpServletRequest request) {
		return (RelayState) request.getSession().getAttribute("relayState");
	}

	private String key() {
		return this.getInitParameter("key");
	}
 }

 /*
  * Copyright 2010, Zepheira LLC Some rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution. 
  * - Neither the name of the openrdf.org nor the names of its contributors may
  *   be used to endorse or promote products derived from this software without
  *   specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * 
  */
 package org.openrdf.http.object.handlers;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.PublicKey;
 import java.security.cert.CertificateParsingException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.nio.entity.NByteArrayEntity;
 import org.openrdf.http.object.model.Handler;
 import org.openrdf.http.object.model.ResourceOperation;
 import org.openrdf.http.object.model.Response;
 import org.openrdf.http.object.traits.Realm;
 import org.openrdf.http.object.util.ChannelUtil;
 import org.openrdf.query.QueryEvaluationException;
 import org.openrdf.repository.RepositoryException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Ensures the request is authentic if protected.
  * 
  * @author James Leigh
  * 
  */
 public class AuthenticationHandler implements Handler {
 	private final Logger logger = LoggerFactory
 			.getLogger(AuthenticationHandler.class);
 	private final Handler delegate;
 	private final String passwd;
 
 	public AuthenticationHandler(Handler delegate, String passwd) {
 		this.delegate = delegate;
 		this.passwd = passwd;
 	}
 
 	public Response verify(ResourceOperation request) throws Exception {
 		String method = request.getMethod();
 		if (request.isAuthenticating()
 				&& !isBoot(method, request.getHeader("Authorization"))) {
 			if (!isAuthorized(request)) {
 				HttpResponse message = unauthorized(request);
 				return new Response().unauthorized(message);
 			}
 		}
 		return delegate.verify(request);
 	}
 
 	public Response handle(ResourceOperation request) throws Exception {
 		String method = request.getMethod();
 		Response rb = delegate.handle(request);
 		if ("GET".equals(method) || "HEAD".equals(method)
 				|| "POST".equals(method) || "OPTIONS".equals(method)) {
 			String origins = allowOrigin(request);
 			if (origins == null || origins.length() < 1) {
 				rb = rb.header("Access-Control-Allow-Origin", "*");
 			} else {
 				rb = rb.header("Access-Control-Allow-Origin", origins);
 			}
 		}
 		return rb;
 	}
 
 	private HttpResponse unauthorized(ResourceOperation request)
 			throws QueryEvaluationException, RepositoryException, IOException {
 		for (Object r : request.getRealms()) {
 			if (r instanceof Realm) {
 				Realm realm = (Realm) r;
 				HttpResponse auth = realm.unauthorized();
 				if (auth != null)
 					return auth;
 			}
 		}
 		return null;
 	}
 
 	private boolean isAuthorized(ResourceOperation request)
 			throws QueryEvaluationException, RepositoryException, IOException {
 		Set<String> names = Collections.emptySet();
 		String al = null;
 		byte[] e = null;
 		X509Certificate cret = request.getX509Certificate();
 		if (cret != null) {
 			names = getSubjectNames(cret);
 			PublicKey pk = cret.getPublicKey();
 			al = pk.getAlgorithm();
 			e = pk.getEncoded();
 		}
 		String[] via = getRequestSource(request);
 		for (Object r : request.getRealms()) {
 			if (r instanceof Realm) {
 				Realm realm = (Realm) r;
 				String allowed = realm.allowOrigin();
 				if (allowed != null && allowed.length() > 0) {
 					String or = request.getVaryHeader("Origin");
 					if (or != null && or.length() > 0
 							&& !isOriginAllowed(allowed, or))
 						continue;
 				}
 				String m = request.getMethod();
 				String au = request.getVaryHeader("Authorization");
 				if (au == null) {
 					if (realm.authorizeAgent(via, names, al, e, m))
 						return true;
 				} else {
 					Map<String, String> map = getAuthorizationMap(request, au);
 					if (realm.authorizeRequest(via, names, al, e, m, map))
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private Map<String, String> getAuthorizationMap(ResourceOperation request,
 			String au) throws IOException {
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("request-target", request.getRequestTarget());
 		map.put("request-uri", request.getURI());
 		map.put("authorization", au);
 		String md5 = request.getHeader("Content-MD5");
 		if (md5 == null) {
 			md5 = computeMD5(request);
 		}
 		if (md5 != null) {
 			map.put("content-md5", md5);
 		}
 		return map;
 	}
 
 	private Set<String> getSubjectNames(X509Certificate cret) {
 		Set<String> names = new LinkedHashSet<String>();
 		try {
 			for (List<?> pair : cret.getSubjectAlternativeNames()) {
 				if (pair.size() == 2 && pair.get(1) instanceof String) {
 					names.add((String) pair.get(1));
 				}
 			}
 		} catch (CertificateParsingException e1) {
 			logger.warn(e1.toString());
 		}
 		return names;
 	}
 
 	private String[] getRequestSource(ResourceOperation request) {
 		List<String> via = new ArrayList<String>();
 		for (String hd : request.getVaryHeaders("Via", "X-Forwarded-For")) {
 			for (String entry : hd.split("\\s*,\\s*")) {
 				via.add(entry);
 			}
 		}
 		InetAddress remoteAddr = request.getRemoteAddr();
 		if (remoteAddr != null) {
 			via.add("1.1 " + remoteAddr.getCanonicalHostName());
 		}
 		return via.toArray(new String[via.size()]);
 	}
 
 	private String computeMD5(ResourceOperation request) throws IOException {
 		HttpEntity entity = request.getEntity();
 		if (entity != null) {
 			try {
 				MessageDigest digest = MessageDigest.getInstance("MD5");
 				ByteArrayOutputStream out = new ByteArrayOutputStream();
 				InputStream in = entity.getContent();
 				try {
 					ChannelUtil.transfer(in, out, digest);
 					byte[] bar = out.toByteArray();
 					NByteArrayEntity replacement = new NByteArrayEntity(bar);
 					replacement.setChunked(entity.isChunked());
 					replacement.setContentEncoding(entity.getContentEncoding());
 					replacement.setContentType(entity.getContentType());
 					request.setEntity(replacement);
 				} finally {
 					in.close();
 				}
 				byte[] hash = Base64.encodeBase64(digest.digest());
 				return new String(hash, "UTF-8");
 			} catch (NoSuchAlgorithmException e) {
 				logger.error(e.toString(), e);
 				return null;
 			}
 		}
 		return null;
 	}
 
 	private boolean isOriginAllowed(String allowed, String o) {
 		for (String ao : allowed.split("\\s*,\\s*")) {
 			if (o.startsWith(ao))
 				return true;
 		}
 		return false;
 	}
 
 	private String allowOrigin(ResourceOperation request)
 			throws QueryEvaluationException, RepositoryException {
 		StringBuilder sb = new StringBuilder();
 		for (Object o : request.getRealms()) {
 			if (o instanceof Realm) {
 				Realm realm = (Realm) o;
 				if (sb.length() > 0) {
 					sb.append(", ");
 				}
 				String origin = realm.allowOrigin();
 				if ("*".equals(origin))
 					return origin;
 				if (origin != null && origin.length() > 0) {
 					sb.append(origin);
 				}
 			}
 		}
 		return sb.toString();
 	}
 
 	private boolean isBoot(String method, String auth)
 			throws UnsupportedEncodingException {
 		if (passwd == null || auth == null)
 			return false;
 		if (auth.startsWith("Basic")) {
 			byte[] bytes = ("boot:" + passwd).getBytes("UTF-8");
 			String encoded = new String(Base64.encodeBase64(bytes));
 			return auth.equals("Basic " + encoded);
 		} else if (auth.startsWith("Digest")) {
 			String string = auth.substring("Digest ".length());
 			Map<String, String> options = parseOptions(string);
 			if (options == null)
 				return false;
 			if (!"boot".equals(options.get("username")))
 				return false;
 			String realm = options.get("realm");
 			String nonce = options.get("nonce");
 			String response = options.get("response");
 			String a1 = "boot:" + realm + ":" + passwd;
 			String a2 = method + ":" + options.get("uri");
 			String legacy = md5(a1) + ":" + nonce + ":" + md5(a2);
 			if (md5(legacy).equals(response))
 				return true;
 			String digest = md5(a1) + ":" + nonce + ":" + options.get("nc")
 					+ ":" + options.get("cnonce") + ":" + options.get("qop")
 					+ ":" + md5(a2);
 			return md5(digest).equals(response);
 		}
 		return false;
 	}
 
 	private String md5(String a2) {
 		return DigestUtils.md5Hex(a2);
 	}
 
 	private Map<String, String> parseOptions(String options) {
 		Map<String, String> result = new HashMap<String, String>();
 		for (String keyvalue : options.split("\\s*,\\s*")) {
 			int idx = keyvalue.indexOf('=');
 			if (idx < 0)
 				return null;
 			String key = keyvalue.substring(0, idx);
			if (keyvalue.charAt(idx + 1) == '"') {
 				int eq = keyvalue.lastIndexOf('"');
 				if (eq <= idx + 2)
 					return null;
 				String value = keyvalue.substring(idx + 2, eq);
 				result.put(key, value);
 			} else {
 				String value = keyvalue.substring(idx + 1);
 				result.put(key, value);
 			}
 		}
 		return result;
 	}
 
 }

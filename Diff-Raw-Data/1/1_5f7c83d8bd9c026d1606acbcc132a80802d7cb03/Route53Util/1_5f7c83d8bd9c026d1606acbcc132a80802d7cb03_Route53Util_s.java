 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.straight.modules.admin.util;
 
 import java.io.IOException;
import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.spec.InvalidKeySpecException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Scanner;
 import java.util.TimeZone;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.crypto.Mac;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.commons.codec.binary.Base64;
 
 import com.google.appengine.api.urlfetch.HTTPHeader;
 import com.google.appengine.api.urlfetch.HTTPMethod;
 import com.google.appengine.api.urlfetch.HTTPRequest;
 import com.google.appengine.api.urlfetch.HTTPResponse;
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 
 /**
  * @author Danilo Penna Queiroz
  */
 public class Route53Util {
 
 	private static final Logger logger = Logger.getLogger(Route53Util.class.getName());
 	private static final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
 	private static final SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
 
 	private static final String ROUTE53_SERVER = "https://route53.amazonaws.com";
 	private static final String DATE_COMMAND = "/date";
 	private static final String VERSION_SPEC = "/2011-05-05";
 	private static final String HOSTED_ZONE_COMMAND = "/hostedzone/";
 	private static final String RRSET = "/rrset";
 
 	private static final String ACCESS_ID_TOKEN = "{accessid}";
 	private static final String SIGNATURE_TOKEN = "{sign}";
 	private static final String DOMAIN_TOKEN = "{domain}";
 	private static final String CREATE_DOMAIN_TEMPLATE = "create_domain.xml";
 	private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
 	private static final String MAC_ALGORITHM = "HmacSHA1";
 
 	private static final String AUTH_TOKEN = "AWS3-HTTPS AWSAccessKeyId={accessid},Algorithm=" + MAC_ALGORITHM + ",Signature={sign}";
 	private static final String FETCH_DATE_HEADER = "Date";
 	private static final String SUBMIT_DATE_HEADER = "x-amz-date";
 	private static final String AUTHORIZATION_HEADER = "X-Amzn-Authorization";
 
 	static {
 		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
 	}
 
 	public static void createDomain(String domain, String accessId, String accessKey, String hostedZoneId) throws Route53Exception {
 		try {
 			HTTPRequest request = new HTTPRequest(new URL(ROUTE53_SERVER + VERSION_SPEC + HOSTED_ZONE_COMMAND + hostedZoneId + RRSET), HTTPMethod.POST);
 			String requestBody = generateRequestBody(domain);
 			logger.fine(requestBody);
 			request.setPayload(requestBody.getBytes());
 			signRequest(request, accessId, accessKey);
 			HTTPResponse response = urlFetchService.fetch(request);
 			if (response.getResponseCode() != 200) {
 				String out = new String(response.getContent());
 				logger.fine("Unable to create domain: " + domain + " - " + out);
 				throw new Route53Exception(out);
 			}
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Unexpected error accessing Route53: " + e.getMessage(), e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	protected static void signRequest(HTTPRequest request, String accessID, String key) throws IOException, InvalidKeyException,
 	InvalidKeySpecException, NoSuchAlgorithmException {
 		String date = fetchDate();
 		String sign = sign(date, key);
 		String signature = AUTH_TOKEN.replace(ACCESS_ID_TOKEN, accessID);
 		signature = signature.replace(SIGNATURE_TOKEN, sign);
 
 		request.addHeader(new HTTPHeader(SUBMIT_DATE_HEADER, date));
 		request.addHeader(new HTTPHeader(AUTHORIZATION_HEADER, signature));
 		request.addHeader(new HTTPHeader("Content-Type", "text/xml; charset=UTF-8"));
 	}
 
 	protected static String sign(String content, String key) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, IllegalStateException {
 		Mac mac = Mac.getInstance(MAC_ALGORITHM);
 		SecretKey skey = new SecretKeySpec(key.getBytes(), KEY_ALGORITHM);
 		mac.init(skey);
 		mac.update(content.getBytes());
 		return new String(Base64.encodeBase64((mac.doFinal())));
 	}
 
 	protected static String fetchDate() throws IOException {
 		HttpURLConnection connection = (HttpURLConnection) new URL(ROUTE53_SERVER + DATE_COMMAND).openConnection();
 		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
 			return formatter.format(new Date(connection.getHeaderFieldDate(FETCH_DATE_HEADER,0)));
 		} else {
 			return null;
 		}
 	}
 
 	protected static String generateRequestBody(String domain) {
 		Scanner sc = new Scanner(Route53Util.class.getClassLoader().getResourceAsStream(CREATE_DOMAIN_TEMPLATE));
 		try {
 			StringBuilder buf = new StringBuilder();
 			while (sc.hasNext()) {
 				String token = sc.next();
 				if (token.contains(DOMAIN_TOKEN)) {
 					buf.append(token.replace(DOMAIN_TOKEN, domain));
 				} else {
 					buf.append(token);
 				}
 				buf.append(' ');
 			}
 			return buf.toString();
 		} finally {
 			sc.close();
 		}
 	}
 }
 

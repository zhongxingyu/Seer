 /*  HellowJava, alpha version
  *  (c) 2005-2010 Gustavo Maia Neto (gutomaia)
  *
  *  HellowJava and all other Hellow flavors will be always
  *  freely distributed under the terms of an GPLv3 license.
  *
  *  Human Knowledge belongs to the World!
  *--------------------------------------------------------------------------*/
 
 package net.guto.hellow.auth;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.URLEncoder;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.net.ssl.SSLSocketFactory;
 
 import net.guto.hellow.core.Authentication;
 
 public class TweenerAuthentication implements Authentication {
 
 	public static final String EL = "\r\n";
 	Map<String, String> passportProps = new HashMap<String, String>();
 
 	public Map<String, String> extractHttpResponseHeader(String httpResponse) {
 		Map<String, String> props = new HashMap<String, String>();
 		int cutter = httpResponse.indexOf(EL + EL);
 		String header = httpResponse.substring(0, cutter);
 		String parameters_values[] = header.split(EL);
 		for (String parameter_value : parameters_values) {
 			if (parameter_value.startsWith("HTTP")) {
 				// TODO: decode http response 200;
 			} else {
 				cutter = parameter_value.indexOf(":");
 				String parameter = parameter_value.substring(0, cutter).trim();
 				String value = parameter_value.substring(cutter + 1,
 						parameter_value.length()).trim();
 				props.put(parameter, value);
 			}
 		}
 		return props;
 	}
 
 	public Map<String, String> extractVarParams(String params) {
 		Map<String, String> props = new HashMap<String, String>();
 		String parameters_values[] = params.split(",");
 		for (String parameter_value : parameters_values) {
 			int cutter = parameter_value.indexOf("=");
 			String parameter = parameter_value.substring(0, cutter);
 			String value = parameter_value.substring(cutter + 1,
 					parameter_value.length());
 			props.put(parameter, value);
 		}
 		return props;
 	}
 
 	// TODO: do a better encode code
 	public String encode(String s) {
 		for (int i = 0; i < s.length(); i++)
 			if (s.charAt(i) == '+') {
 				String s1 = s.substring(0, i);
 				s1 = s1 + "%20" + s.substring(i + 1);
 				s = s1;
 			}
 		return s;
 	}
 
 	public String fetchStream(BufferedReader reader) throws IOException {
 		StringBuilder sb = new StringBuilder();
 		String line;
 		while ((line = reader.readLine()) != null) {
 			System.out.println(line);
 			sb.append(line).append(EL);
 		}
 		return sb.toString();
 	}
 
 	public void connectToTheNexus() throws UnknownHostException, IOException {
 		// TODO: extract the connection to a method;
 		Socket socket = new Socket("nexus.passport.com", 443);
 		socket = ((SSLSocketFactory) SSLSocketFactory.getDefault())
 				.createSocket(socket, "nexus.passport.com", 443, true);
 		BufferedReader bufferedreader = new BufferedReader(
 				new InputStreamReader(socket.getInputStream()));
 		PrintWriter printwriter = new PrintWriter(socket.getOutputStream(),
 				true);
		printwriter.println("GET /rdr/pprdr.asp HTTP/1.0\r\n\r\n");
 		String httpResponse = null;
 		try {
 			httpResponse = fetchStream(bufferedreader);
 		} finally {
 			socket.close();
 		}
 		String passportURLs = extractHttpResponseHeader(httpResponse).get(
 				"PassportURLs");
 		passportProps = extractVarParams(passportURLs);
 	}
 
 	public String buildHttpRequestHeader(String url, Map<String, String> params) {
 		StringBuilder sb = new StringBuilder();
 		String getRequest = url.substring(url.indexOf("/"));
 		sb.append("GET ").append(getRequest);
 		sb.append(" HTTP/1.1").append(EL);
 		for (Entry<String, String> paramValue : params.entrySet()) {
 			sb.append(paramValue.getKey()).append(": ")
 					.append(paramValue.getValue()).append(EL);
 		}
 		return sb.toString();
 	}
 
 	public String buildParamVars(Map<String, String> params) {
 		StringBuilder sb = new StringBuilder();
 		Set<Entry<String, String>> paramsValues = params.entrySet();
 		Iterator<Entry<String, String>> iterator = paramsValues.iterator();
 		while (iterator.hasNext()) {
 			Entry<String, String> paramValue = iterator.next();
 			if (paramValue.getKey().equals("lc")) {
 				sb.append(paramValue.getValue());
 			} else {
 				sb.append(paramValue.getKey()).append("=")
 						.append(paramValue.getValue());
 			}
 			if (iterator.hasNext())
 				sb.append(",");
 		}
 		return sb.toString();
 	}
 
 	public String performTheLogin(String username, String password, String lc)
 			throws IOException {
 		String DALogin = passportProps.get("DALogin");
 		String host = DALogin.substring(0, DALogin.indexOf("/"));
 		Socket socket = new Socket(host, 443);
 		socket = ((SSLSocketFactory) SSLSocketFactory.getDefault())
 				.createSocket(socket, host, 443, true);
 		BufferedReader bufferedreader = new BufferedReader(
 				new InputStreamReader(socket.getInputStream()));
 		PrintWriter printwriter = new PrintWriter(socket.getOutputStream(),
 				true);
 
 		Map<String, String> authParams = new LinkedHashMap<String, String>();
 		authParams.put("Passport1.4 OrgVerb", "GET");
 		authParams.put("OrgURL", "http%3A%2F%2Fmessenger%2Emsn%2Ecom");
 		authParams.put("sign-in", encode(URLEncoder.encode(username, "UTF-8")));
 		authParams.put("pwd", encode(URLEncoder.encode(password, "UTF-8")));
 		authParams.put("lc", lc); // TODO: ver null;
 
 		String authorization = buildParamVars(authParams);
 
 		Map<String, String> requestParams = new LinkedHashMap<String, String>();
 		requestParams.put("Authorization", authorization);
 		requestParams.put("Host", host); // TODO: see
 
 		String requestHeader = buildHttpRequestHeader(DALogin, requestParams);
 		System.out.println("--" + requestHeader + "--" + EL + EL);
 		printwriter.println(requestHeader);
 		String httpResponse;
 		try {
 			httpResponse = fetchStream(bufferedreader);
 		} finally {
 			socket.close();
 		}
 		System.out.println(httpResponse);
 		Map<String, String> httpHeader = extractHttpResponseHeader(httpResponse);
 		String authenticationInfo = httpHeader.get("Authentication-Info");
 		Map<String, String> authResponse = extractVarParams(authenticationInfo);
 		String fromPP = authResponse.get("from-PP");
 		return fromPP;
 	}
 
 	public String authenticate(String username, String password, String lc) {
 		try {
 			connectToTheNexus();
 			String token = performTheLogin(username, password, lc);
 			return token.substring(1, token.length() - 1);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }

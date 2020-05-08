 /*******************************************************************************
  * Copyright 2012 Yonatan Graber
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package me.yonatan.spicyurl;
 
 import java.net.URI;
 import java.net.URL;
 
 import lombok.Getter;
 import lombok.ToString;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.google.common.base.Preconditions;
 
 @Getter
 @ToString
 public class Url {
 
 	public Url(String url) {
 		Preconditions.checkArgument(StringUtils.isNotBlank(url));
 		raw = url;
 		Parser p = new Parser(this);
 		p.parse();
 	}
 
 	public Url(URL url) {
 		this(Preconditions.checkNotNull(url).toString());
 	}
 
 	public Url(URI uri) {
 		this(Preconditions.checkNotNull(uri).toString());
 	}
 
 	private String raw;
 	private String host;
 	private String scheme;
 	private String path;
 	private int port = -1;
 	private String username;
 	private String password;
 	private String query;
 	private String fragment;
 
 	protected class Parser {
 		private final static String SCHEME_SEP = "://";
 		private final static char QUERY_SEP_CHAR = '?';
 		private final static char FRAGMENT_SEP_CHAR = '#';
 		private final static char PATH_SEP_CHAR = '/';
 		private final static String LOGIN_SEP = "@";
 		private final static String USER_PASS_SEP = ":";
 		private final static String PORT_SEP = ":";
 		private final static int MIN_PORT_VALUE = 1;
 		private final static int MAX_PORT_VALUE = 65535;
 
 		private final static String QUERY_SEP = "" + QUERY_SEP_CHAR;
 		private final static String PATH_SEP = "" + PATH_SEP_CHAR;
 		private final static String FRAGMENT_SEP = "" + FRAGMENT_SEP_CHAR;
 
 		private final Url url;
 
 		protected Parser(Url url) {
 			this.url = url;
 		}
 
 		protected void parse() {
 			String[] stage0 = StringUtils
 					.splitByWholeSeparatorPreserveAllTokens(url.getRaw(),
 							SCHEME_SEP, 2);
 			if (stage0.length != 2)
 				throw new MalformedUrlException();
 
 			url.scheme = stage0[0];
 			if (StringUtils.isEmpty(scheme))
 				throw new MalformedUrlException();
 
 			// Check for first speprator, to split host from path/query/fragment
 			int hostSeperatorIdx = StringUtils.indexOfAny(stage0[1], PATH_SEP,
 					QUERY_SEP, FRAGMENT_SEP);
 			if (hostSeperatorIdx == -1) {
 				// Just host
 				parseLoginHostPort(stage0[1]);
 			} else {
 				parseLoginHostPort(StringUtils.substring(stage0[1], 0,
 						hostSeperatorIdx));
 				parsePathQueryFregment(StringUtils.substring(stage0[1],
 						hostSeperatorIdx));
 			}
 
 		}
 
 		private void parseLoginHostPort(String loginHostPort) {
 			if (StringUtils.isEmpty(loginHostPort))
 				throw new MalformedUrlException();
 			if (loginHostPort.contains(LOGIN_SEP)) {
 				parseLogin(StringUtils.substringBeforeLast(loginHostPort,
 						LOGIN_SEP));
 				parseHostPort(StringUtils.substringAfterLast(loginHostPort,
 						LOGIN_SEP));
 			} else {
 				parseHostPort(loginHostPort);
 			}
 		}
 
 		private void parseLogin(String login) {
 			if (StringUtils.isEmpty(login))
 				return;
 
 			if (login.contains(USER_PASS_SEP)) {
 				url.username = StringUtils
 						.substringBefore(login, USER_PASS_SEP);
 				url.password = StringUtils.substringAfter(login, USER_PASS_SEP);
 			} else {
 				url.username = login;
 			}
 		}
 
 		private void parseHostPort(String hostPort) {
 			String[] stage0 = StringUtils.splitPreserveAllTokens(hostPort,
 					PORT_SEP);
 			if (stage0.length > 2)
 				throw new MalformedUrlException();
 			url.host = stage0[0];
 			if (StringUtils.isEmpty(url.host))
 				throw new MalformedUrlException();
 			if (stage0.length == 2) {
 				parsePort(stage0[1]);
 			}
 		}
 
 		private void parsePort(String port) {
 			if (!StringUtils.isNumeric(port)) {
 				throw new MalformedUrlException();
 			}
 			try {
 				url.port = Integer.parseInt(port);
 			} catch (NumberFormatException e) {
 				throw new MalformedUrlException();
 			}
 			if (url.port < MIN_PORT_VALUE || url.port > MAX_PORT_VALUE)
 				throw new MalformedUrlException();
 		}
 
 		/**
 		 * 
 		 * @param pathQueryFregment
 		 *            - first char should be the seperator (PATH '/', QUERY '?'
 		 *            or FRAGMENT '?')
 		 */
 		private void parsePathQueryFregment(String sepPathQueryFregment) {
 			if (StringUtils.isEmpty(sepPathQueryFregment))
 				return;
 			char firstSeperator = sepPathQueryFregment.charAt(0);
 			String pathQueryFregment = StringUtils.substring(
 					sepPathQueryFregment, 1);
 
 			switch (firstSeperator) {
 			case PATH_SEP_CHAR:
 				int secondSeperatorIdx = StringUtils.indexOfAny(
 						pathQueryFregment, QUERY_SEP, FRAGMENT_SEP);
 				if (secondSeperatorIdx == -1) {
 					url.path = pathQueryFregment;
 				} else {
 					url.path = StringUtils.substring(pathQueryFregment, 0,
 							secondSeperatorIdx);
					if (pathQueryFregment.charAt(secondSeperatorIdx) == FRAGMENT_SEP_CHAR) {
						url.fragment = StringUtils.substring(pathQueryFregment,
								secondSeperatorIdx + 1);
					} else {
						parseQueryFregmant(StringUtils.substring(
								pathQueryFregment, secondSeperatorIdx + 1));
					}
 				}
 				break;
 			case QUERY_SEP_CHAR:
 				parseQueryFregmant(pathQueryFregment);
 				break;
 			case FRAGMENT_SEP_CHAR:
 				url.fragment = pathQueryFregment;
 				break;
 			default:
 				throw new MalformedUrlException();
 			}
 		}
 
 		private void parseQueryFregmant(String queryFragment) {
 			if (StringUtils.isEmpty(queryFragment))
 				return;
 
			String[] parts = StringUtils.splitPreserveAllTokens(queryFragment,
					FRAGMENT_SEP, 2);
			url.query = parts[0];
			if (parts.length == 2) {
				url.fragment = parts[1];
			}
 		}
 
 	}
 
 }

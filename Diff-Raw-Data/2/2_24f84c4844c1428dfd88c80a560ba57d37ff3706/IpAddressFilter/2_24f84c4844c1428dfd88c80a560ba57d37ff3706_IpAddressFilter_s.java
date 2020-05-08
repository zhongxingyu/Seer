 /* Copyright 2006-2009 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.codehaus.groovy.grails.plugins.springsecurity;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Arrays;
 import java.util.Map;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.security.ui.FilterChainOrder;
 import org.springframework.security.ui.SpringSecurityFilter;
 import org.springframework.util.AntPathMatcher;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 
 /**
  * Blocks access to protected resources based on IP address. Sends 404 rather than
  * reporting error to hide visibility of the resources.
  * <br/>
  * Supports either Ant-style patterns (e.g. 10.**) or masked patterns
  * (e.g. 192.168.1.0/24 or 202.24.0.0/14).
  *
  * @author <a href='mailto:beckwithb@studentsonly.com'>Burt Beckwith</a>
  */
 public class IpAddressFilter extends SpringSecurityFilter implements InitializingBean {
 
 	private final Logger _log = Logger.getLogger(getClass());
 
 	private final AntPathMatcher _pathMatcher = new AntPathMatcher();
 
 	private Map<String, String> _restrictions;
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.springframework.security.ui.SpringSecurityFilter#doFilterHttp(
 	 * 	javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
 	 * 	javax.servlet.FilterChain)
 	 */
 	@Override
 	protected void doFilterHttp(
 			  final HttpServletRequest request,
 			  final HttpServletResponse response,
 			  final FilterChain chain) throws IOException, ServletException {
 
 		if (!isAllowed(request.getRemoteAddr(), request.getRequestURI())) {
 			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404
 			return;
 		}
 
 		chain.doFilter(request, response);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.springframework.security.ui.SpringSecurityFilter#getOrder()
 	 */
 	public int getOrder() {
 		return FilterChainOrder.LOGOUT_FILTER + 1;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
 	 */
 	public void afterPropertiesSet() {
 		Assert.notNull(_restrictions, "ipRestrictions is required");
 	}
 
 	/**
 	 * Dependency injection for the ip/pattern restriction map.
 	 * @param restrictions  the map
 	 */
 	@Required
 	public void setIpRestrictions(final Map<String, String> restrictions) {
 		_restrictions = restrictions;
 	}
 
 	private boolean isAllowed(final String ip, final String requestURI) {
 
 		if ("127.0.0.1".equals(ip)) {
 			return true;
 		}
 
 		String reason = null;
 
 		for (Map.Entry<String, String> entry : _restrictions.entrySet()) {
 			String uriPattern = entry.getKey();
 			if (!_pathMatcher.match(uriPattern, requestURI)) {
 				continue;
 			}
			
 			String ipPattern = entry.getValue();
 			if (ipPattern.contains("/")) {
 				try {
 					if (!matchesUsingMask(ipPattern, ip)) {
 						reason = ipPattern;
 						break;
 					}
 				}
 				catch (UnknownHostException e) {
 					reason = e.getMessage();
 					break;
 				}
 			}
 			else if (!_pathMatcher.match(ipPattern, ip)) {
 				reason = ipPattern;
 				break;
 			}
 		}
 
 		if (reason != null) {
 			_log.error("disallowed request " + requestURI + " from " + ip + ": " + reason);
 			return false;
 		}
 
 		return true;
 	}
 
 	private boolean matchesUsingMask(final String ipPattern, final String ip) throws UnknownHostException {
 
 		String[] addressAndMask = StringUtils.split(ipPattern, "/");
 
 		InetAddress requiredAddress = parseAddress(addressAndMask[0]);
 		InetAddress remoteAddress = parseAddress(ip);
 		if (!requiredAddress.getClass().equals(remoteAddress.getClass())) {
 			throw new IllegalArgumentException(
 					"IP Address in expression must be the same type as version returned by request");
 		}
 
 		int maskBits = Integer.parseInt(addressAndMask[1]);
 		if (maskBits == 0) {
 			return remoteAddress.equals(requiredAddress);
 		}
 
 		int oddBits = maskBits % 8;
 		byte[] mask = new byte[maskBits / 8 + (oddBits == 0 ? 0 : 1)];
 
 		Arrays.fill(mask, 0, oddBits == 0 ? mask.length : mask.length - 1, (byte)0xFF);
 
 		if (oddBits != 0) {
 			int finalByte = (1 << oddBits) - 1;
 			finalByte <<= 8 - oddBits;
 			mask[mask.length - 1] = (byte) finalByte;
 		}
 
 		byte[] remoteAddressBytes = remoteAddress.getAddress();
 		byte[] requiredAddressBytes = requiredAddress.getAddress();
 		for (int i = 0; i < mask.length; i++) {
 			if ((remoteAddressBytes[i] & mask[i]) != (requiredAddressBytes[i] & mask[i])) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	private InetAddress parseAddress(final String address) throws UnknownHostException {
 		try {
 			return InetAddress.getByName(address);
 		}
 		catch (UnknownHostException e) {
 			_log.error("unable to parse " + address);
 			throw e;
 		}
 	}
 }

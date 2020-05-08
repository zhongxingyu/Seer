 package edu.umn.auth;
 
 import java.io.IOException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
 import org.springframework.util.Assert;
 	/* 
 	 * Grails Spring Security Mock Plugin - Fake Authentication for Spring Security
      * Copyright (C) 2012 Aaron J. Zirbes
 	 *
      * This program is free software: you can redistribute it and/or modify
      * it under the terms of the GNU General Public License as published by
      * the Free Software Foundation, either version 3 of the License, or
      * (at your option) any later version.
 	 *
      * This program is distributed in the hope that it will be useful,
      * but WITHOUT ANY WARRANTY; without even the implied warranty of
      * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      * GNU General Public License for more details.
 	 *
      * You should have received a copy of the GNU General Public License
      * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 	 */
 
 /**
  * Processes a {@link MockAuthenticationToken}, and authenticates via Mock authenticator
  * 
  * @author <a href="mailto:ajz@umn.edu">Aaron J. Zirbes</a>
  */
 class MockAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
 
 	private String mockUsername;
 
 	/** The default constructor */
 	public MockAuthenticationFilter() {
 		super("/j_spring_mock_security_check");
 	}
 
 	/** Try to login the user using a mock authenticator */
 	@Override
 	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
 			throws AuthenticationException, IOException {
 
 		logger.debug("attemptAuthentication():: loading mock security environment");
 
 		MockAuthenticationToken mockAuthenticationToken = new MockAuthenticationToken(mockUsername);
 
 		logger.debug("attemptAuthentication():: calling authenticate");
 
 		return this.getAuthenticationManager().authenticate(mockAuthenticationToken);
 	}
 
 	public void setMockUsername(String mockUsername) {
 		this.mockUsername = mockUsername;
 	}
 
 	@Override
 	public void afterPropertiesSet() {
 		super.afterPropertiesSet();
 		Assert.notNull(mockUsername, "mockUsername cannot be null");
 	}
 }

 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.web.csrf;
 
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.junit.Test;
 
 public class CsrfHandlerInterceptorTest {
 
   private CsrfHandlerInterceptor subject = new CsrfHandlerInterceptor();
 
   @Test
   public void getReqeustsShouldBeIgnored() throws Exception {
     HttpServletRequest request = mock(HttpServletRequest.class);
     HttpServletResponse response = mock(HttpServletResponse.class);
 
     when(request.getMethod()).thenReturn("GET", "get");
 
     boolean result = subject.preHandle(request, response, new Object());
     assertThat(result, is(true));
 
     result = subject.preHandle(request, response, new Object());
     assertThat(result, is(true));
   }
 
   @Test
   public void changingRequestsShouldChecked() throws Exception {
     HttpServletRequest request = mock(HttpServletRequest.class);
     HttpServletResponse response = mock(HttpServletResponse.class);
     HttpSession session = mock(HttpSession.class);
 
     String[] methods = { "post", "DELETE", "delete", "PUT", "put" };
 
     when(request.getMethod()).thenReturn("POST", methods);
     when(request.getSession()).thenReturn(session);
    when(session.getAttribute(CsrfTokenManager.CSRF_SESSION_ATTR_NAME)).thenReturn("csrf-token-value");
 
     for (int i = 0; i < methods.length + 1; i++) {
       boolean result = subject.preHandle(request, response, new Object());
       assertThat(result, is(false));
     }
   }
 }

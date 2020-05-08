 /**
  * Copyright (c) 2012, SURFnet BV
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
 package nl.surfnet.bod.service;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import javax.servlet.http.HttpServletRequest;
 
 import nl.surfnet.bod.domain.VirtualPortRequestLink;
 import nl.surfnet.bod.support.RichUserDetailsFactory;
 import nl.surfnet.bod.support.VirtualPortRequestLinkFactory;
 import nl.surfnet.bod.web.security.RichUserDetails;
 
 import org.junit.Test;
 
 import com.google.common.base.Optional;
 
 public class EmailsTest {
 
   @Test
   public void requestVirtualPortMailWithEmail() {
     RichUserDetails from = new RichUserDetailsFactory().setDisplayname("Henk").setEmail("henk@henk.nl").create();
     VirtualPortRequestLink requestLink = new VirtualPortRequestLinkFactory().create();
     String link = "http://localhost";
 
     String body = Emails.VirtualPortRequestMail.body(from, requestLink, link);
 
     assertThat(body, containsString("From: Henk (henk@henk.nl)"));
   }
 
   @Test
   public void requestVirtualPortMailWithoutEmail() {
     RichUserDetails from = new RichUserDetailsFactory().setDisplayname("Henk").setEmail(null).create();
     VirtualPortRequestLink requestLink = new VirtualPortRequestLinkFactory().create();
     String link = "http://localhost";
 
     String body = Emails.VirtualPortRequestMail.body(from, requestLink, link);
 
     assertThat(body, containsString("From: Henk (Unknown Email)"));
   }
 
 
   @Test
   public void errorMailWithEmail() {
     RichUserDetails user = new RichUserDetailsFactory().setDisplayname("Henk").setEmail("henk@henk.nl").create();
     RuntimeException throwable = new RuntimeException();
     HttpServletRequest request = mock(HttpServletRequest.class);
     when(request.getMethod()).thenReturn("GET");
     when(request.getQueryString()).thenReturn("");
     when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/wrong"));
 
     String body = Emails.ErrorMail.body(throwable, Optional.of(user), Optional.of(request));
 
     assertThat(body, containsString("User: Henk (henk@henk.nl)"));
   }
 
   @Test
   public void errorMailWithoutEmail() {
     RichUserDetails user = new RichUserDetailsFactory().setDisplayname("Henk").setEmail(null).create();
     RuntimeException throwable = new RuntimeException();
     HttpServletRequest request = mock(HttpServletRequest.class);
     when(request.getMethod()).thenReturn("GET");
     when(request.getQueryString()).thenReturn("");
     when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/wrong"));
 
     String body = Emails.ErrorMail.body(throwable, Optional.of(user), Optional.of(request));
 
     assertThat(body, containsString("User: Henk (Email not known)"));
   }
 
   @Test
   public void errorMailWithoutUserAndRequest() {
     RuntimeException throwable = new RuntimeException();
     String body = Emails.ErrorMail.body(throwable, Optional.<RichUserDetails>absent(), Optional.<HttpServletRequest>absent());
 
     assertThat(body, containsString("User: Unknown (Unknown)"));
     assertThat(body, containsString("Request: No request available (No request available)"));
   }

  @Test
  public void errorMailSubjectShouldContainExceptionMessage() {
    String subject = Emails.ErrorMail.subject("http://localhost:8080/bod", new AssertionError("Er ging iets goed mis"));

    assertThat(subject, containsString("Er ging iets goed mis"));
  }

  @Test
  public void errorMailSubjectShouldContainExceptionClassIfMessageIsMissing() {
    String subject = Emails.ErrorMail.subject("http://localhost:8080/bod", new AssertionError());

    assertThat(subject, containsString("AssertionError"));
  }

  @Test
  public void errorMailSubjectShouldContainEnvironmentUrl() {
    String subject = Emails.ErrorMail.subject("http://localhost:8080/bod", new AssertionError());

    assertThat(subject, containsString("http://localhost:8080/bod"));
  }
 }

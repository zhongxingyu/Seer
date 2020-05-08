 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.filter;
 
 import com.google.common.collect.Lists;
 import com.sun.jersey.core.header.InBoundHeaders;
 import com.sun.jersey.spi.container.ContainerRequest;
 import com.sun.jersey.spi.container.ContainerRequestFilter;
 import com.sun.jersey.spi.container.ContainerResponse;
 import com.sun.jersey.spi.container.ContainerResponseFilter;
 import com.sun.jersey.spi.container.WebApplication;
 import java.security.NoSuchAlgorithmException;
 import java.security.Principal;
 import java.util.Collections;
 import java.util.List;
 import javax.servlet.ServletContext;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.SecurityContext;
 import org.junit.Test;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 import org.junit.Before;
 import org.lafayette.server.Registry;
 import org.lafayette.server.ServerContextListener;
 import org.lafayette.server.domain.User;
 import org.lafayette.server.http.ForbiddenException;
 import org.lafayette.server.http.UnauthorizedException;
 import org.lafayette.server.mapper.Mappers;
import org.lafayette.server.mapper.RoleMapper;
 import org.lafayette.server.mapper.UserMapper;
 import static org.mockito.Mockito.*;
 
 /**
  * Tests for {@link SecurityContextFilterDigest}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class SecurityContextFilterDigestTest {
 
     private final ServletContext servlet = mock(ServletContext.class);
     private final HttpHeaders headers = mock(HttpHeaders.class);
     private final Registry registry = new Registry();
 
     public SecurityContextFilterDigestTest() throws NoSuchAlgorithmException {
         super();
     }
 
     @Before
     public void setUpRegistry() {
         when(servlet.getAttribute(ServerContextListener.REGISRTY)).thenReturn(registry);
     }
 
     @Test
     public void getRequestFilter() {
         final SecurityContextFilterDigest sut = new SecurityContextFilterDigest(servlet, headers);
         assertThat(sut, is(instanceOf(ContainerRequestFilter.class)));
         assertThat(sut.getRequestFilter(), is(sameInstance((ContainerRequestFilter) sut)));
     }
 
     @Test
     public void getResponseFilter() {
         final SecurityContextFilterDigest sut = new SecurityContextFilterDigest(servlet, headers);
         assertThat(sut, is(instanceOf(ContainerResponseFilter.class)));
         assertThat(sut.getResponseFilter(), is(sameInstance((ContainerResponseFilter) sut)));
     }
 
     @Test
     public void registry() throws NoSuchAlgorithmException {
         final SecurityContextFilterDigest sut = new SecurityContextFilterDigest(servlet, headers);
         assertThat(sut.registry(), is(sameInstance(registry)));
     }
 
     @Test
     public void filterResponse_alwaysReturnSameInstanceAsPassedIn() {
         final ContainerRequest req = new ContainerRequest(
                 mock(WebApplication.class),
                 null, null, null,
                 new InBoundHeaders(), null);
         final ContainerResponse res = new ContainerResponse(mock(WebApplication.class), req, null);
         final SecurityContextFilterDigest sut = new SecurityContextFilterDigest(servlet, headers);
         assertThat(sut.filter(req, res), is(sameInstance(res)));
     }
 
     @Test
     public void getAuthorizationHeader_headerIsNull() {
         when(headers.getRequestHeader("Authorization")).thenReturn(null);
         final SecurityContextFilterDigest sut = new SecurityContextFilterDigest(servlet, headers);
         assertThat(sut.getAuthorizationHeader(), is(equalTo("")));
     }
 
     @Test
     public void getAuthorizationHeader_headerIsEmpty() {
         when(headers.getRequestHeader("Authorization")).thenReturn(Collections.<String>emptyList());
         final SecurityContextFilterDigest sut = new SecurityContextFilterDigest(servlet, headers);
         assertThat(sut.getAuthorizationHeader(), is(equalTo("")));
     }
 
     @Test
     public void getAuthorizationHeader_headerIsPresent() {
         final List<String> headerList = Lists.newArrayList();
         headerList.add("foo");
         when(headers.getRequestHeader("Authorization")).thenReturn(headerList);
         final SecurityContextFilterDigest sut = new SecurityContextFilterDigest(servlet, headers);
         assertThat(sut.getAuthorizationHeader(), is(equalTo("foo")));
     }
 
     @Test
     public void getAuthorizationHeader_multipleHeaderArePresent() {
         final List<String> headerList = Lists.newArrayList();
         headerList.add("foo");
         headerList.add("bar");
         headerList.add("baz");
         when(headers.getRequestHeader("Authorization")).thenReturn(headerList);
         final SecurityContextFilterDigest sut = new SecurityContextFilterDigest(servlet, headers);
         assertThat(sut.getAuthorizationHeader(), is(equalTo("foo")));
     }
 
     @Test
     public void filterRequest_throwsUnauthorizedExceptionIfHeaderEmpty() {
         when(headers.getRequestHeader("Authorization")).thenReturn(null);
         final SecurityContextFilterDigest sut = new SecurityContextFilterDigest(servlet, headers);
 
         try {
             sut.filter(mock(ContainerRequest.class));
             fail("Expected exception not thrown!");
         } catch (UnauthorizedException ex) {
             final Response res = ex.getResponse();
             assertThat(res.getStatus(), is(401));
             assertThat((String) res.getEntity(), is(equalTo("")));
             // TODO Verify header field.
         }
     }
 
     @Test
     public void filterRequest_throwsForbiddenExceptionIfAuthenticationNotValid() {
         final SecurityContextFilterDigest sut = new SecurityContextFilterDigest(servlet, headers);
         when(headers.getRequestHeader("Authorization")).thenReturn(Lists.<String>newArrayList("No valid header"));
 
         try {
             sut.filter(mock(ContainerRequest.class));
             fail("Expected exception not thrown!");
         } catch (ForbiddenException ex) {
             final Response res = ex.getResponse();
             assertThat(res.getStatus(), is(403));
         }
     }
 
     @Test
     public void filterRequest_returnRequestWithSecurityContextIfValidAuthentication() {
         final SecurityContextFilterDigest sut = new SecurityContextFilterDigest(servlet, headers);
         when(headers.getRequestHeader("Authorization"))
            .thenReturn(Lists.<String>newArrayList("Digest "
                 + "username=\"Foo\", realm=\"Private Area\", nonce=\"IrTfjizEdXmIdlwHwkDJx0\", "
                 + "httpMethod=\"GET\", uri=\"/\", response=\"85b78939376de982650217f6b9c6783d\""));
         // hashedPassword = md5 ( Foo:Private Area:Bar )
         final User user = new User(1, "Foo", "5f9cc3ee75165842ac6efce65cc77ce9");
         final UserMapper userMapper = mock(UserMapper.class);
         when(userMapper.findByLoginName(user.getLoginName())).thenReturn(user);
         final Mappers mappers = mock(Mappers.class);
         when(mappers.createUserMapper()).thenReturn(userMapper);
        when(mappers.createRoleMapper()).thenReturn(mock(RoleMapper.class));
         registry.setMappers(mappers);
         final ContainerRequest req = new ContainerRequest(
                 mock(WebApplication.class),
                 null, null, null,
                 new InBoundHeaders(), null);
        req.setSecurityContext(mock(SecurityContext.class));
         assertThat(sut.filter(req), is(sameInstance(req)));
         final SecurityContext securityContext = req.getSecurityContext();
         assertThat(securityContext.isSecure(), is(false));
         assertThat(securityContext.getAuthenticationScheme(), is(equalTo(SecurityContext.DIGEST_AUTH)));
         assertThat(securityContext.getUserPrincipal(), is(sameInstance((Principal) user)));
     }
 }

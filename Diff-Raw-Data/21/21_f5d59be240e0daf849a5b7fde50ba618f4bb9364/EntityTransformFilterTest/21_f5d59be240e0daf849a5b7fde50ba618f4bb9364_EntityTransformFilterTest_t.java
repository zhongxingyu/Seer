 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.falcon.filter;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.ServletInputStream;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.IOUtils;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * test Entity xml filter.
  */
 public class EntityTransformFilterTest {
     private static final String IVORY_XML = "<cluster name=\"local\" xmlns=\"uri:ivory:cluster:0.1\"> "
                                                 + "<path=\"/ivory/data\"/> </cluster>";
     private static final String FALCON_XML = "<cluster name=\"local\" xmlns=\"uri:falcon:cluster:0.1\"> "
                                                 + "<path=\"/ivory/data\"/> </cluster>";
 
     @Mock
     private HttpServletRequest servletRequest;
 
     @Mock
     private HttpServletResponse servletResponse;
 
     @BeforeClass
     public void init() {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
     public void testIvoryXmlFilter() throws IOException, ServletException {
         Mockito.when(servletRequest.getInputStream()).thenReturn(
                 getServletInputStream(IOUtils.toInputStream(IVORY_XML)));
        Mockito.when(servletRequest.getPathInfo()).thenReturn("api/submit");
 
         EntityTransformFilter filter = new EntityTransformFilter();
         FilterChain chain = new FilterChain() {
             @Override
             public void doFilter(ServletRequest request, ServletResponse response) throws
                                                                                     IOException, ServletException {
                 Assert.assertEquals(FALCON_XML, getString(request.getInputStream()));
             }
         };
         filter.doFilter(servletRequest, servletResponse, chain);
     }
 
     @Test
     public void testFalconXmlFilter() throws IOException, ServletException {
         Mockito.when(servletRequest.getInputStream()).thenReturn(
                 getServletInputStream(IOUtils.toInputStream(FALCON_XML)));
        Mockito.when(servletRequest.getPathInfo()).thenReturn("api/update");
 
         EntityTransformFilter filter = new EntityTransformFilter();
         FilterChain chain = new FilterChain() {
             @Override
             public void doFilter(ServletRequest request, ServletResponse response) throws
                                                                                     IOException, ServletException {
                 Assert.assertEquals(FALCON_XML, getString(request.getInputStream()));
             }
         };
         filter.doFilter(servletRequest, servletResponse, chain);
     }
 
     private String getString(InputStream stream) throws IOException {
         StringWriter writer = new StringWriter();
         IOUtils.copy(stream, writer);
         return writer.toString();
     }
 
     private ServletInputStream getServletInputStream(final InputStream stream) {
         return new ServletInputStream() {
 
             @Override
             public int read() throws IOException {
                 return stream.read();
             }
         };
     }
 
 }

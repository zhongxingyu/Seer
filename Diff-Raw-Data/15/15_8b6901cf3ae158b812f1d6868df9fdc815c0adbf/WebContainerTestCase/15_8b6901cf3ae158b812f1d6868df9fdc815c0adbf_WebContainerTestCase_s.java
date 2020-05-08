 /*
  * Copyright 2004-2014 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dbflute.unit.spring.web;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.seasar.dbflute.unit.mocklet.MockletHttpServletRequest;
 import org.seasar.dbflute.unit.mocklet.MockletHttpServletRequestImpl;
 import org.seasar.dbflute.unit.mocklet.MockletHttpServletResponse;
 import org.seasar.dbflute.unit.mocklet.MockletHttpServletResponseImpl;
 import org.seasar.dbflute.unit.mocklet.MockletHttpSession;
 import org.seasar.dbflute.unit.mocklet.MockletServletConfig;
 import org.seasar.dbflute.unit.mocklet.MockletServletConfigImpl;
 import org.seasar.dbflute.unit.mocklet.MockletServletContext;
 import org.seasar.dbflute.unit.mocklet.MockletServletContextImpl;
 import org.seasar.dbflute.unit.spring.ContainerTestCase;
 
 /**
  * @author jflute
  * @since 0.4.0 (2014/03/16 Sunday)
  */
 public abstract class WebContainerTestCase extends ContainerTestCase {
 
     // ===================================================================================
     //                                                                           Attribute
     //                                                                           =========
     // -----------------------------------------------------
     //                                         Static Cached
     //                                         -------------
     /** The cached determination of suppressing web mock. (NullAllowed: null means beginning or ending) */
     protected static Boolean _xcachedSuppressWebMock;
 
     // -----------------------------------------------------
     //                                              Web Mock
     //                                              --------
     /** The mock request of the test case execution. (NullAllowed: when no web mock or beginning or ending) */
     protected MockletHttpServletRequest _xmockRequest;
 
     /** The mock response of the test case execution. (NullAllowed: when no web mock or beginning or ending) */
     protected MockletHttpServletResponse _xmockResponse;
 
     // ===================================================================================
     //                                                                            Settings
     //                                                                            ========
     // -----------------------------------------------------
     //                                     Prepare Container
     //                                     -----------------
     @Override
     protected void xprepareTestCaseContainer() {
         super.xprepareTestCaseContainer();
         xdoPrepareWebMockContext();
     }
 
     @Override
     protected boolean xcanRecycleContainer(String[] configFiles) {
         return super.xcanRecycleContainer(configFiles) && xwebMockCanAcceptContainerRecycle();
     }
 
     protected boolean xwebMockCanAcceptContainerRecycle() {
         // no mark or no change
         return _xcachedSuppressWebMock == null || _xcachedSuppressWebMock.equals(isSuppressWebMock());
     }
 
     @Override
     protected void xsaveCachedInstance(String[] configFiles) {
         super.xsaveCachedInstance(configFiles);
         _xcachedSuppressWebMock = isSuppressWebMock();
     }
 
     /**
      * Does it suppress web mock? e.g. HttpServletRequest, HttpSession
      * @return The determination, true or false.
      */
     protected boolean isSuppressWebMock() {
         return false;
     }
 
     protected void xdoPrepareWebMockContext() {
         if (isSuppressWebMock()) {
             return;
         }
         final MockletServletConfig servletConfig = createMockletServletConfig();
         servletConfig.setServletContext(createMockletServletContext());
         xregisterWebMockContext(servletConfig);
     }
 
     @Override
     public void tearDown() throws Exception {
         _xmockRequest = null;
         _xmockResponse = null;
     }
 
     // ===================================================================================
     //                                                                     Spring Handling
     //                                                                     ===============
     // -----------------------------------------------------
     //                                              Web Mock
     //                                              --------
     protected void xregisterWebMockContext(MockletServletConfig servletConfig) {
         final MockletHttpServletRequest request = createMockletHttpServletRequest(servletConfig.getServletContext());
         final MockletHttpServletResponse response = createMockletHttpServletResponse(request);
         final HttpSession session = request.getSession(true);
         // unknown how to set request and response to Spring DI system
         // and unknown how to new-create mocks per test case execution
         // so register them as mock instance for now
         // (but they cannot be injected to normal component)
         registerMockInstance(request);
         registerMockInstance(response);
         registerMockInstance(session);
         xkeepMockRequestInstance(request, response); // for web mock handling methods
     }
 
     protected MockletServletConfig createMockletServletConfig() {
         return new MockletServletConfigImpl();
     }
 
     protected MockletServletContext createMockletServletContext() {
         return new MockletServletContextImpl("utservlet");
     }
 
     protected MockletHttpServletRequest createMockletHttpServletRequest(ServletContext servletContext) {
         return new MockletHttpServletRequestImpl(servletContext, prepareServletPath());
     }
 
     protected MockletHttpServletResponse createMockletHttpServletResponse(HttpServletRequest request) {
         return new MockletHttpServletResponseImpl(request);
     }
 
     protected String prepareServletPath() { // customize point
         return "/utflute";
     }
 
     protected void xkeepMockRequestInstance(MockletHttpServletRequest request, MockletHttpServletResponse response) {
         _xmockRequest = request;
         _xmockResponse = response;
     }
 
     // ===================================================================================
     //                                                                   Web Mock Handling
     //                                                                   =================
     // -----------------------------------------------------
     //                                               Request
     //                                               -------
     protected MockletHttpServletRequest getMockRequest() {
         return (MockletHttpServletRequest) _xmockRequest;
     }
 
     protected void addMockRequestHeader(String name, String value) {
         final MockletHttpServletRequest request = getMockRequest();
         if (request != null) {
             request.addHeader(name, value);
         }
     }
 
     @SuppressWarnings("unchecked")
     protected <ATTRIBUTE> ATTRIBUTE getMockRequestParameter(String name) {
         final MockletHttpServletRequest request = getMockRequest();
         return request != null ? (ATTRIBUTE) request.getParameter(name) : null;
     }
 
     protected void addMockRequestParameter(String name, String value) {
         final MockletHttpServletRequest request = getMockRequest();
         if (request != null) {
             request.addParameter(name, value);
         }
     }
 
     @SuppressWarnings("unchecked")
     protected <ATTRIBUTE> ATTRIBUTE getMockRequestAttribute(String name) {
         final MockletHttpServletRequest request = getMockRequest();
         return request != null ? (ATTRIBUTE) request.getAttribute(name) : null;
     }
 
     protected void setMockRequestAttribute(String name, Object value) {
         final MockletHttpServletRequest request = getMockRequest();
         if (request != null) {
             request.setAttribute(name, value);
         }
     }
 
     // -----------------------------------------------------
     //                                              Response
     //                                              --------
     protected MockletHttpServletResponse getMockResponse() {
         return (MockletHttpServletResponse) _xmockResponse;
     }
 
     protected Cookie[] getMockResponseCookies() {
         final MockletHttpServletResponse response = getMockResponse();
         return response != null ? response.getCookies() : null;
     }
 
     protected int getMockResponseStatus() {
         final MockletHttpServletResponse response = getMockResponse();
         return response != null ? response.getStatus() : null;
     }
 
     protected String getMockResponseString() {
         final MockletHttpServletResponse response = getMockResponse();
         return response != null ? response.getResponseString() : null;
     }
 
     // -----------------------------------------------------
     //                                               Session
     //                                               -------
     /**
      * @return The instance of mock session. (NotNull: if no session, new-created)
      */
     protected MockletHttpSession getMockSession() {
         return _xmockRequest != null ? (MockletHttpSession) _xmockRequest.getSession(true) : null;
     }
 
     protected void invalidateMockSession() {
         final MockletHttpSession session = getMockSession();
         if (session != null) {
             session.invalidate();
         }
     }
 
     @SuppressWarnings("unchecked")
     protected <ATTRIBUTE> ATTRIBUTE getMockSessionAttribute(String name) {
         final MockletHttpSession session = getMockSession();
         return session != null ? (ATTRIBUTE) session.getAttribute(name) : null;
     }
 
     protected void setMockSessionAttribute(String name, Object value) {
         final MockletHttpSession session = getMockSession();
         if (session != null) {
             session.setAttribute(name, value);
         }
     }
 }

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
 package org.seasar.dbflute.unit.seasar;
 
 import java.lang.annotation.Annotation;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.transaction.NotSupportedException;
 import javax.transaction.Status;
 import javax.transaction.SystemException;
 import javax.transaction.Transaction;
 import javax.transaction.TransactionManager;
 
 import org.seasar.dbflute.unit.core.InjectionTestCase;
 import org.seasar.dbflute.unit.core.binding.BindingAnnotationRule;
 import org.seasar.dbflute.unit.core.binding.ComponentBinder;
 import org.seasar.dbflute.unit.core.binding.NonBindingDeterminer;
 import org.seasar.dbflute.unit.core.transaction.TransactionFailureException;
 import org.seasar.dbflute.unit.core.transaction.TransactionResource;
 import org.seasar.dbflute.unit.mocklet.MockletHttpServletRequest;
 import org.seasar.dbflute.unit.mocklet.MockletHttpServletRequestImpl;
 import org.seasar.dbflute.unit.mocklet.MockletHttpServletResponse;
 import org.seasar.dbflute.unit.mocklet.MockletHttpServletResponseImpl;
 import org.seasar.dbflute.unit.mocklet.MockletHttpSession;
 import org.seasar.dbflute.unit.mocklet.MockletServletConfig;
 import org.seasar.dbflute.unit.mocklet.MockletServletConfigImpl;
 import org.seasar.dbflute.unit.mocklet.MockletServletContext;
 import org.seasar.dbflute.unit.mocklet.MockletServletContextImpl;
 import org.seasar.dbflute.util.Srl;
 import org.seasar.framework.container.ComponentNotFoundRuntimeException;
 import org.seasar.framework.container.ExternalContext;
 import org.seasar.framework.container.S2Container;
 import org.seasar.framework.container.SingletonS2Container;
 import org.seasar.framework.container.TooManyRegistrationRuntimeException;
 import org.seasar.framework.container.annotation.tiger.Binding;
 import org.seasar.framework.container.annotation.tiger.BindingType;
 import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
 import org.seasar.framework.container.servlet.S2ContainerServlet;
 import org.seasar.framework.convention.NamingConvention;
 import org.seasar.framework.env.Env;
 
 /**
  * @author jflute
  * @since 0.1.0 (2011/07/24 Sunday)
  */
 public abstract class SeasarTestCase extends InjectionTestCase {
 
     // ===================================================================================
     //                                                                           Attribute
     //                                                                           =========
     // -----------------------------------------------------
     //                                         Static Cached
     //                                         -------------
     /** The cached configuration file of DI container. (NullAllowed: null means beginning or ending) */
     protected static String _xcachedConfigFile;
 
     /** The cached determination of suppressing web mock. (NullAllowed: null means beginning or ending) */
     protected static Boolean _xcachedSuppressWebMock;
 
     /** The cached configuration of servlet. (NullAllowed: when no web mock or beginning or ending) */
     protected static MockletServletConfig _xcachedServletConfig;
 
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
     //                                      Before Container
     //                                      ----------------
     @Override
     protected void xsetupBeforeContainer() {
         super.xsetupBeforeContainer();
         xprepareUnitTestEnv();
     }
 
     // -----------------------------------------------------
     //                                     Prepare Container
     //                                     -----------------
     @Override
     protected void xprepareTestCaseContainer() {
         final String configFile = xdoPrepareTestCaseContainer();
         xsaveCachedInstance(configFile);
         xdoPrepareWebMockContext();
     }
 
     protected String xdoPrepareTestCaseContainer() {
         if (isUseOneTimeContainer()) {
             xdestroyContainer();
         }
         final String configFile = prepareConfigFile();
         if (xisInitializedContainer()) {
             if (xcanRecycleContainer(configFile)) {
                 log("...Recycling seasar: " + configFile);
                 xrecycleContainerInstance(configFile);
                 return configFile; // no need to initialize
             } else { // changed
                 xdestroyContainer(); // to re-initialize
             }
         }
         xinitializeContainer(configFile);
         return configFile;
     }
 
     protected boolean xcanRecycleContainer(String configFile) {
         return xconfigCanAcceptContainerRecycle(configFile) && xwebMockCanAcceptContainerRecycle();
     }
 
     protected boolean xconfigCanAcceptContainerRecycle(String configFile) {
         return configFile.equals(_xcachedConfigFile); // no change
     }
 
     protected void xrecycleContainerInstance(String configFile) {
         // managed as singleton so caching is unneeded here
     }
 
     protected boolean xwebMockCanAcceptContainerRecycle() {
         // no mark or no change
         return _xcachedSuppressWebMock == null || _xcachedSuppressWebMock.equals(isSuppressWebMock());
     }
 
     protected void xsaveCachedInstance(String configFile) {
         _xcachedConfigFile = configFile;
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
         if (_xcachedServletConfig != null) {
             // the servletConfig has been already created when container initialization
             xregisterWebMockContext(_xcachedServletConfig);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     protected boolean isUseOneTimeContainer() {
         return false;
     }
 
     /**
      * Prepare configuration file as root for Seasar.
      * @return The pure file name of root dicon. (NotNull)
      */
     protected String prepareConfigFile() { // customize point
         return "app.dicon"; // as default
     }
 
     @Override
     protected void xclearCachedContainer() {
         _xcachedConfigFile = null;
     }
 
     @Override
     public void tearDown() throws Exception {
         _xmockRequest = null;
         _xmockResponse = null;
        super.tearDown();
     }
 
     // ===================================================================================
     //                                                                         Transaction
     //                                                                         ===========
     /**
      * {@inheritDoc}
      */
     @Override
     protected TransactionResource beginNewTransaction() { // user method
         final Class<TransactionManager> managerType = TransactionManager.class;
         if (!hasComponent(managerType)) {
             return null;
         }
         final TransactionManager manager = getComponent(managerType);
         final Transaction suspendedTx;
         try {
             if (manager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                 suspendedTx = manager.suspend(); // because Seasar's DBCP doesn't support nested transaction
             } else {
                 suspendedTx = null;
             }
         } catch (SystemException e) {
             throw new TransactionFailureException("Failed to suspend current", e);
         }
         TransactionResource resource = null;
         try {
             manager.begin();
             resource = new TransactionResource() {
                 public void commit() {
                     try {
                         manager.commit();
                     } catch (Exception e) {
                         throw new TransactionFailureException("Failed to commit the transaction.", e);
                     } finally {
                         xresumeSuspendedTxQuietly(manager, suspendedTx);
                     }
                 }
 
                 public void rollback() {
                     try {
                         manager.rollback();
                     } catch (Exception e) {
                         throw new TransactionFailureException("Failed to roll-back the transaction.", e);
                     } finally {
                         xresumeSuspendedTxQuietly(manager, suspendedTx);
                     }
                 }
             }; // for thread-fire's transaction or manual transaction
         } catch (NotSupportedException e) {
             throw new TransactionFailureException("Failed to begin new transaction.", e);
         } catch (SystemException e) {
             throw new TransactionFailureException("Failed to begin new transaction.", e);
         }
         return resource;
     }
 
     protected void xresumeSuspendedTxQuietly(TransactionManager manager, Transaction suspendedTx) {
         try {
             if (suspendedTx != null) {
                 manager.resume(suspendedTx);
             }
         } catch (Exception e) {
             log(e.getMessage());
         }
     }
 
     // ===================================================================================
     //                                                                   Component Binding
     //                                                                   =================
     @Override
     protected ComponentBinder createOuterComponentBinder(Object bean) {
         final ComponentBinder binder = super.createOuterComponentBinder(bean);
         binder.byTypeInterfaceOnly();
         return binder;
     }
 
     @Override
     protected Map<Class<? extends Annotation>, BindingAnnotationRule> xprovideBindingAnnotationRuleMap() {
         final Map<Class<? extends Annotation>, BindingAnnotationRule> ruleMap = newHashMap();
         ruleMap.put(Resource.class, new BindingAnnotationRule());
         ruleMap.put(Binding.class, new BindingAnnotationRule().determineNonBinding(new NonBindingDeterminer() {
             public boolean isNonBinding(Annotation bindingAnno) {
                 return BindingType.NONE.equals(((Binding) bindingAnno).bindingType());
             }
         }));
         return ruleMap;
     }
 
     @Override
     protected String xfilterByBindingNamingRule(String propertyName, Class<?> propertyType) {
         if (propertyType.getSimpleName().contains("_")) { // e.g. (org.dbflute.maihama.) Foo_BarLogic
             return null; // simple name that contains '_' is unsupported
         }
         // e.g. [root].logic.foo.bar.QuxLogic
         final NamingConvention convention = getComponent(NamingConvention.class);
         final String componentName;
         try {
             // e.g. foo_bar_quxLogic -> foo_bar_quxLogic ends with [property name] -> returns foo_bar_quxLogic
             componentName = convention.fromClassNameToComponentName(propertyType.getName());
         } catch (RuntimeException ignored) { // just in case e.g. org.dbflute.maihama.foo
             return null;
         }
         if (xcanUseComponentNameByBindingNamingRule(componentName, propertyName)) {
             return componentName;
         }
         // not smart deploy component or name wrong e.g. (foo_bar_) quxLogic does not equal quxService
         return null;
     }
 
     protected boolean xcanUseComponentNameByBindingNamingRule(String componentName, String propertyName) {
         if (componentName.contains("_")) { // means smart deploy component
             if (componentName.endsWith(propertyName)) {
                 final String front = Srl.substringLastFront(componentName, propertyName); // e.g. foo_bar_
                 if (front.equals("") || front.endsWith("_")) {
                     // e.g.
                     //  foo_bar_quxLogic ends with foo_bar_quxLogic
                     //  foo_bar_quxLogic ends with quxLogic
                     //  foo_bar_quxLogic ends with bar_quxLogic
                     return true;
                 }
                 // e.g. foo_bar_quxLogic ends with ar_quxLogic
             }
         }
         return false;
     }
 
     // ===================================================================================
     //                                                                     Seasar Handling
     //                                                                     ===============
     protected void xprepareUnitTestEnv() {
         Env.setFilePath("env_ut.txt");
         Env.setValueIfAbsent("ut");
     }
 
     // -----------------------------------------------------
     //                                            Initialize
     //                                            ----------
     protected boolean xisInitializedContainer() {
         return SingletonS2ContainerFactory.hasContainer();
     }
 
     protected void xinitializeContainer(String configFile) {
         if (isSuppressWebMock()) { // library
             log("...Initializing seasar as library: " + configFile);
             xdoInitializeContainerAsLibrary(configFile);
         } else { // web (Seasar contains web components as default)
             log("...Initializing seasar as web: " + configFile);
             xdoInitializeContainerAsWeb(configFile);
         }
     }
 
     protected void xdoInitializeContainerAsLibrary(String configFile) {
         SingletonS2ContainerFactory.setConfigPath(configFile);
         SingletonS2ContainerFactory.init();
     }
 
     protected void xdoInitializeContainerAsWeb(String configFile) {
         final ServletConfig servletConfig = xprepareMockServletConfig(configFile);
         final S2ContainerServlet containerServlet = xcreateS2ContainerServlet();
         try {
             containerServlet.init(servletConfig);
         } catch (ServletException e) {
             String msg = "Failed to initialize servlet config to servlet: " + servletConfig;
             throw new IllegalStateException(msg, e.getRootCause());
         }
     }
 
     // -----------------------------------------------------
     //                                              Web Mock
     //                                              --------
     protected ServletConfig xprepareMockServletConfig(String configFile) {
         _xcachedServletConfig = createMockletServletConfig(); // cache for request mocks
         _xcachedServletConfig.setServletContext(createMockletServletContext());
         _xcachedServletConfig.setInitParameter(S2ContainerServlet.CONFIG_PATH_KEY, configFile);
         return _xcachedServletConfig;
     }
 
     protected S2ContainerServlet xcreateS2ContainerServlet() {
         return new S2ContainerServlet();
     }
 
     protected void xregisterWebMockContext(MockletServletConfig servletConfig) { // like S2ContainerFilter
         final S2Container container = SingletonS2ContainerFactory.getContainer();
         final ExternalContext externalContext = container.getExternalContext();
         final MockletHttpServletRequest request = createMockletHttpServletRequest(servletConfig.getServletContext());
         final MockletHttpServletResponse response = createMockletHttpServletResponse(request);
         externalContext.setRequest(request);
         externalContext.setResponse(response);
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
 
     // -----------------------------------------------------
     //                                               Destroy
     //                                               -------
     protected void xdestroyContainer() {
         SingletonS2ContainerFactory.destroy();
         SingletonS2ContainerFactory.setExternalContext(null); // destroy() does not contain this
         _xcachedServletConfig = null;
     }
 
     // -----------------------------------------------------
     //                                             Component
     //                                             ---------
     /**
      * {@inheritDoc}
      */
     protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) { // user method
         return SingletonS2Container.getComponent(type);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     protected <COMPONENT> COMPONENT getComponent(String name) { // user method
         return (COMPONENT) SingletonS2Container.getComponent(name);
     }
 
     /**
      * {@inheritDoc}
      */
     protected boolean hasComponent(Class<?> type) { // user method
         try {
             SingletonS2Container.getComponent(type);
             return true;
         } catch (ComponentNotFoundRuntimeException e) {
             return false;
         } catch (TooManyRegistrationRuntimeException e) {
             return false;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     protected boolean hasComponent(String name) { // user method
         try {
             SingletonS2Container.getComponent(name);
             return true;
         } catch (ComponentNotFoundRuntimeException e) {
             return false;
         }
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

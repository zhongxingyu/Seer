 package com.wixpress.fjarr.server;
 
 
 import com.wixpress.fjarr.server.example.ParamObject;
 import com.wixpress.fjarr.server.example.TestService;
 import com.wixpress.fjarr.server.example.TestServiceImpl;
 import com.wixpress.fjarr.server.exceptions.BadRequestException;
 import com.wixpress.fjarr.server.exceptions.HttpMethodNotAllowedException;
 import com.wixpress.fjarr.server.exceptions.UnsupportedContentTypeException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InOrder;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.mock.web.MockServletConfig;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.AnnotationConfigContextLoader;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ObjectError;
 import org.springframework.validation.Validator;
 import org.springframework.web.context.support.StaticWebApplicationContext;
 import org.springframework.web.servlet.DispatcherServlet;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 
 /**
  * @author alex
  * @since 12/24/12 12:51 PM
  */
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {ITConfiguration.class})
 public class SpringMVCIntegrationTest
 {
 
     @Autowired
     protected ApplicationContext applicationContext;
 
     protected DispatcherServlet dispatcherServlet;
 
     @Autowired
 
     RpcProtocol protocol; // mock
 
     @Autowired
     Validator validator;// mock
 
     @Autowired
     TestService testService; // spy
 
 
     @Before
     public void initMvc() throws ServletException
     {
         StaticWebApplicationContext wac = new StaticWebApplicationContext();
         wac.setParent(applicationContext);
         MockServletConfig servletConfig = new MockServletConfig("springapp");
         dispatcherServlet = new DispatcherServlet(wac);
         dispatcherServlet.init(servletConfig);
 
     }
 
     @After
     public void fini()
     {
         reset(protocol, validator);
     }
 
     @Test
     public void testNormal() throws IOException, ServletException, UnsupportedContentTypeException, BadRequestException, HttpMethodNotAllowedException
     {
         when(protocol.parseRequest(any(RpcRequest.class))).thenAnswer(new Answer<ParsedRpcRequest>()
         {
             @Override
             public ParsedRpcRequest answer(InvocationOnMock invocationOnMock) throws Throwable
             {
                 RpcInvocation invocation = new RpcInvocation("getData", new PositionalRpcParameters(new Object[]{}));
                 invocation.setResolvedMethod(TestServiceImpl.class.getMethod("getData", new Class[0]));
                 invocation.setResolvedParameters(new Object[0]);
                 return ParsedRpcRequest.from((RpcRequest) invocationOnMock.getArguments()[0], invocation);
             }
         });
         doAnswer(new Answer<Object>()
         {
             @Override
             public Object answer(InvocationOnMock invocationOnMock) throws Throwable
             {
 
                 RpcResponse response = (RpcResponse) invocationOnMock.getArguments()[0];
 
                 response.getOutputStream().write("test".getBytes("UTF8"));
 
                 return null;
             }
         }).when(protocol).writeResponse(any(RpcResponse.class), any(ParsedRpcRequest.class));
 
 
         MockHttpServletRequest request = new MockHttpServletRequest("POST", "/TestService");
         //request.addHeader("Accept", "text/*");
         request.setCharacterEncoding("UTF8");
         request.setContent("".getBytes("UTF8"));
         MockHttpServletResponse response = new MockHttpServletResponse();
         dispatcherServlet.service(request, response);
 
         String body = new String(response.getContentAsByteArray());
         assertThat(body, is("test"));
 
         verify(validator, times(0)).validate(any(), any(Errors.class));
 
         InOrder io = inOrder(protocol, validator, testService);
 
         io.verify(protocol).parseRequest(any(RpcRequest.class));
         io.verify(protocol).resolveMethod(anyList(), any(RpcInvocation.class), any(ParsedRpcRequest.class));
         io.verify(testService).getData();
         io.verify(protocol).writeResponse(any(RpcResponse.class), any(ParsedRpcRequest.class));
         io.verifyNoMoreInteractions();
     }
 
     @Test
     public void testWithValidation() throws IOException, ServletException, UnsupportedContentTypeException, BadRequestException, HttpMethodNotAllowedException
     {
         final ParamObject paramObject = new ParamObject("testValidation");
         when(protocol.parseRequest(any(RpcRequest.class))).thenAnswer(new Answer<ParsedRpcRequest>()
         {
             @Override
             public ParsedRpcRequest answer(InvocationOnMock invocationOnMock) throws Throwable
             {
                 RpcInvocation invocation = new RpcInvocation("getDataWithParam", new PositionalRpcParameters(new Object[]{}));
                 invocation.setResolvedMethod(TestServiceImpl.class.getMethod("getDataWithParam", new Class[]{ParamObject.class}));
                 invocation.setResolvedParameters(new Object[]{paramObject});
                 return ParsedRpcRequest.from((RpcRequest) invocationOnMock.getArguments()[0], invocation);
             }
         });
         doAnswer(new Answer<Object>()
         {
             @Override
             public Object answer(InvocationOnMock invocationOnMock) throws Throwable
             {
 
                 RpcResponse response = (RpcResponse) invocationOnMock.getArguments()[0];
 
                 response.getOutputStream().write("testValidation".getBytes("UTF8"));
 
                 return null;
             }
         }).when(protocol).writeResponse(any(RpcResponse.class), any(ParsedRpcRequest.class));
 
 
         MockHttpServletRequest request = new MockHttpServletRequest("POST", "/TestService");
         //request.addHeader("Accept", "text/*");
         request.setCharacterEncoding("UTF8");
         request.setContent("".getBytes("UTF8"));
         MockHttpServletResponse response = new MockHttpServletResponse();
         dispatcherServlet.service(request, response);
 
         String body = new String(response.getContentAsByteArray());
         assertThat(body, is("testValidation"));
 
         InOrder io = inOrder(protocol, validator, testService);
 
         io.verify(protocol).parseRequest(any(RpcRequest.class));
         io.verify(protocol).resolveMethod(anyList(), any(RpcInvocation.class), any(ParsedRpcRequest.class));
         io.verify(validator).validate(eq(paramObject), any(Errors.class));
         io.verify(testService).getDataWithParam(eq(paramObject));
         io.verify(protocol).writeResponse(any(RpcResponse.class), any(ParsedRpcRequest.class));
         io.verifyNoMoreInteractions();
     }
 
 
     @Test
     public void testWithValidationError() throws IOException, ServletException, UnsupportedContentTypeException, BadRequestException, HttpMethodNotAllowedException
     {
         final ParamObject paramObject = new ParamObject("testValidation");
         when(protocol.parseRequest(any(RpcRequest.class))).thenAnswer(new Answer<ParsedRpcRequest>()
         {
             @Override
             public ParsedRpcRequest answer(InvocationOnMock invocationOnMock) throws Throwable
             {
                 RpcInvocation invocation = new RpcInvocation("getDataWithParam", new PositionalRpcParameters(new Object[]{}));
                 invocation.setResolvedMethod(TestServiceImpl.class.getMethod("getDataWithParam", new Class[]{ParamObject.class}));
                 invocation.setResolvedParameters(new Object[]{paramObject});
                 return ParsedRpcRequest.from((RpcRequest) invocationOnMock.getArguments()[0], invocation);
             }
         });
         doAnswer(new Answer<Object>()
         {
             @Override
             public Object answer(InvocationOnMock invocationOnMock) throws Throwable
             {
 
                 RpcResponse response = (RpcResponse) invocationOnMock.getArguments()[0];
                 ParsedRpcRequest request = (ParsedRpcRequest) invocationOnMock.getArguments()[1];
 
                 assertTrue(request.getInvocations().get(0).isError());
                 response.getOutputStream().write("error".getBytes("UTF8"));
                 response.setStatusCode(312);
 
                 return null;
             }
         }).when(protocol).writeResponse(any(RpcResponse.class), any(ParsedRpcRequest.class));
 
 
         doAnswer(new Answer<Object>()
         {
             @Override
             public Object answer(InvocationOnMock invocationOnMock) throws Throwable
             {
                 ((BindingResult) invocationOnMock.getArguments()[1]).addError(new ObjectError("p", "error"));
                 return null;
             }
         }).when(validator).validate(eq(paramObject), any(Errors.class));
 
 
         MockHttpServletRequest request = new MockHttpServletRequest("POST", "/TestService");
         //request.addHeader("Accept", "text/*");
         request.setCharacterEncoding("UTF8");
         request.setContent("".getBytes("UTF8"));
         MockHttpServletResponse response = new MockHttpServletResponse();
         dispatcherServlet.service(request, response);
 
         String body = new String(response.getContentAsByteArray());
         assertThat(response.getStatus(), is(312));
         assertThat(body, is("error"));
 
 
         InOrder io = inOrder(protocol, validator, testService);
 
         io.verify(protocol).parseRequest(any(RpcRequest.class));
         io.verify(protocol).resolveMethod(anyList(), any(RpcInvocation.class), any(ParsedRpcRequest.class));
         io.verify(validator).validate(eq(paramObject), any(Errors.class));
         //io.verify(testService).getDataWithParam(eq(paramObject));
         io.verify(protocol).writeResponse(any(RpcResponse.class), any(ParsedRpcRequest.class));
         io.verifyNoMoreInteractions();
     }
 }

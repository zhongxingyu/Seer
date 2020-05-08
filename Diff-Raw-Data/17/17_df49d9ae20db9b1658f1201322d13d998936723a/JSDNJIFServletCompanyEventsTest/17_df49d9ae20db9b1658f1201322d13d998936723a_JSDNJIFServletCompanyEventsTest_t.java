  /************************************************************************ 
  *   Copyright [2013] [Jamcracker Inc]
  * 
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *  limitations under the License.
  *   
  *   
  * @ClassName com.jamcracker.jif.servlet.JSDNJIFServletCompanyEventsTest
  * @version 1.0
  * @author Prasad P Nair
  * @date 13-Dec-2012
  * @see
  *   
  /*************************************************************************/
 package com.jamcracker.jif.servlet;
 import static org.easymock.EasyMock.expect;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletInputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.easymock.EasyMock;
 import org.easymock.IMocksControl;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 import com.jamcracker.jif.common.JIFConstants;
 import com.jamcracker.jif.util.JIFUtil;
 
 /**
  * @author ppnair
  *
  */
 public class JSDNJIFServletCompanyEventsTest {
 	private static IMocksControl mocks;
 	private static JSDNJIFServlet servlet;
 
 	@BeforeClass
 	public static void setUpBeforeClass() {
 	    mocks = EasyMock.createControl();
 	    servlet = new JSDNJIFServlet();
 	}
 
 	@After
 	public void tearDown() {
 	    mocks.reset();
 	}
 	
 	/**
 	 * Test method for {@link com.jamcracker.jif.servlet.JSDNJIFServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
 	 * @throws Exception 
 	 */
 	@Test
 	public void testCreateCompany() throws Exception {
 	    String xml = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/requests/request_createcompany.xml"));
 	    String expected = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/responses/response_createcompany.xml"));
 	    executeTest(xml, expected);
 	}
 
 	@Test
 	public void testUpdateCompany() throws Exception {
 	    String xml = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/requests/request_updatecompany.xml"));
 	    String expected = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/responses/response_updatecompany.xml"));
 	    executeTest(xml, expected);
 	}	
 
 	@Test
 	public void testDeleteCompany() throws Exception {
 	    String xml = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/requests/request_deletecompany.xml"));
 	    String expected = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/responses/response_deletecompany.xml"));
 	    executeTest(xml, expected);
 	}
 
 	/**
 	 * Test method for {@link com.jamcracker.jif.servlet.JSDNJIFServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
 	 * @throws Exception 
 	 */
 	@Test
 	public void testFailCreateCompany() throws Exception {
 	    String xml = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/requests/fail_request_createcompany.xml"));
 	    String expected = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/responses/fail_response_createcompany.xml"));
 	    executeTest(xml, expected);
 	}
 
 	@Test
 	public void testFailUpdateCompany() throws Exception {
 	    String xml = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/requests/fail_request_updatecompany.xml"));
 	    String expected = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/responses/fail_response_updatecompany.xml"));
 	    executeTest(xml, expected);
 	}	
 
 	@Test
 	public void testFailDeleteCompany() throws Exception {
 	    String xml = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/requests/fail_request_deletecompany.xml"));
 	    String expected = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/responses/fail_response_deletecompany.xml"));
 	    executeTest(xml, expected);
 	}
 
 	/**
 	 * Test method for {@link com.jamcracker.jif.servlet.JSDNJIFServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
 	 * @throws Exception 
 	 */
 	@Test
 	public void testWaitCreateCompany() throws Exception {
 	    String xml = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/requests/wait_request_createcompany.xml"));
 	    String expected = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/responses/wait_response_createcompany.xml"));
 	    executeTest(xml, expected);
 	}
 
 	@Test
 	public void testWaitUpdateCompany() throws Exception {
 	    String xml = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/requests/wait_request_updatecompany.xml"));
 	    String expected = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/responses/wait_response_updatecompany.xml"));
 	    executeTest(xml, expected);
 	}	
 
 	@Test
 	public void testWaitDeleteCompany() throws Exception {
 	    String xml = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/requests/wait_request_deletecompany.xml"));
 	    String expected = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/responses/wait_response_deletecompany.xml"));
 	    executeTest(xml, expected);
 	}
 
 	@Test
 	public void testRequestAsBody() throws Exception {
 	    HttpServletRequest request = mocks.createMock(HttpServletRequest.class);
 	    HttpServletResponse response = mocks.createMock(HttpServletResponse.class);
 	    String inputXML = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/requests/request_createcompany.xml"));
 	    String expectedXML = JIFUtil.readXMLFromFile(JSDNJIFServletCompanyEventsTest.class.getResourceAsStream("/xmls/responses/response_createcompany.xml"));
 	    final ByteArrayInputStream bais = new ByteArrayInputStream(inputXML.getBytes());
 	    ServletInputStream mockInputStream = new ServletInputStream() {
 	      @Override
 	      public int read() throws IOException {
 	        return bais.read();
 	      }
 	    };
 	    expect(request.getParameter(JIFConstants.PARAM_NAME)).andReturn(null);
 
 	    expect(request.getInputStream()).andReturn(mockInputStream);
 	    ByteArrayOutputStream oStream = new ByteArrayOutputStream();
 
 	    expect(response.getWriter()).andReturn((new PrintWriter(oStream)));
 	    mocks.replay();
 	    servlet.doPost(request, response);
 	    mocks.verify();
 	    String actual = readStream(oStream);
 	    Assert.assertEquals(stripWhiteSpace(expectedXML.trim()),stripWhiteSpace(actual.trim()));
 	    mocks.reset();
 	}
 	
 	
 	private void executeTest(String inputXML, String expectedXML)throws Exception {
 	    HttpServletRequest request = mocks.createMock(HttpServletRequest.class);
 	    HttpServletResponse response = mocks.createMock(HttpServletResponse.class);
 	    expect(request.getParameter(JIFConstants.PARAM_NAME)).andReturn(inputXML);
 	    ByteArrayOutputStream oStream = new ByteArrayOutputStream();
 	    expect(response.getWriter()).andReturn((new PrintWriter(oStream)));
 	    mocks.replay();
 	    servlet.doPost(request, response);
 	    mocks.verify();
 	    String actual = readStream(oStream);
 	    Assert.assertEquals(stripWhiteSpace(expectedXML.trim()),stripWhiteSpace(actual.trim()));
 	    mocks.reset();
 	}
 	
 	public static String readStream(ByteArrayOutputStream os) throws Exception {
     	String out = new String(os.toByteArray(), "UTF-8");
     	return out;
     }
     
     public static Document getXMLDocument(String xml) throws Exception{
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
         Document doc = docBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
     	return doc;
     }
     public String stripWhiteSpace(String xml){
     	xml = xml.replaceAll(">[ \r\t\n]*<", "><");
     	return xml;
     }
 	
 }

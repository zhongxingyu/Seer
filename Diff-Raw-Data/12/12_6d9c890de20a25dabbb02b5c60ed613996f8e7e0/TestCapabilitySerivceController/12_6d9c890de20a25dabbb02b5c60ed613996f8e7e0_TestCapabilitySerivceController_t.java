 /*
  * Copyright (C) 2010  Catalyst IT Limited
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package nz.net.catalyst.mobile.mdl.device;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 
 public class TestCapabilitySerivceController  extends TestCase {
 
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private CapabilityServiceController csController;
    
    private ObjectMapper mapper = new ObjectMapper();
 
 
    public void setUp() throws IOException {
       request = new MockHttpServletRequest();
       response = new MockHttpServletResponse();
      ApplicationContext ac = new FileSystemXmlApplicationContext("main/test/mdl-context.xml");
       
       csController = (CapabilityServiceController) ac.getBean("capabilityServiceController");
    }
    
    public void testRequiredParams() throws Exception {
       request = new MockHttpServletRequest();
       response = new MockHttpServletResponse();
       csController.get_capabilities(request, response);
       String content = response.getContentAsString();
       assertEquals("ERROR: Missing required parameter 'headers'", content);
       
 
       request = new MockHttpServletRequest();
       response = new MockHttpServletResponse();
       request.setParameter("headers", "some headers");
       csController.get_capabilities(request, response);
       content = response.getContentAsString();
       assertEquals("ERROR: Missing required parameter 'capability'", content);
       
    }
    
    public void testGetDeviceInfo() throws Exception {
       HashMap<String, String> headers = new HashMap<String, String> ();
       headers.put("user-agent", "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaE71-1/100.07.57; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413");
       ByteArrayOutputStream outStream = new ByteArrayOutputStream();
       mapper.writeValue(outStream, headers);
       request.setParameter("headers", outStream.toString());
       request.setParameter("capability", "model_name");
            
       csController.get_capabilities(request, response);
       String content = response.getContentAsString();
       Map<String, Object> capabilityMap = mapper.readValue(content, new TypeReference<Map<String, Object>> () {});
       assertEquals("E71", capabilityMap.get("model_name"));
       
    }
 
    public void testParseError() throws Exception {
       HashMap<String, String> headers = new HashMap<String, String> ();
       headers.put("user-agent", "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaE71-1/100.07.57; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413");
       ByteArrayOutputStream outStream = new ByteArrayOutputStream();
       mapper.writeValue(outStream, headers);
       request.setParameter("headers", "corrupted string" + outStream.toString());
       request.setParameter("capability", "model_name");
            
       csController.get_capabilities(request, response);
       String content = response.getContentAsString();
       assertTrue(content.contains("ERROR: Parsing problem:"));
       
    }
    
    public void testMissingUserAgent() throws Exception {
       HashMap<String, String> headers = new HashMap<String, String> ();
       headers.put("user-agent-missing", "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaE71-1/100.07.57; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413");
       ByteArrayOutputStream outStream = new ByteArrayOutputStream();
       mapper.writeValue(outStream, headers);
       request.setParameter("headers", outStream.toString());
       request.setParameter("capability", "model_name");
            
       csController.get_capabilities(request, response);
       String content = response.getContentAsString();
       assertTrue(content.contains("ERROR: Parsing problem: required http header 'user-agent' is missing"));
       
    }
 
 
 }

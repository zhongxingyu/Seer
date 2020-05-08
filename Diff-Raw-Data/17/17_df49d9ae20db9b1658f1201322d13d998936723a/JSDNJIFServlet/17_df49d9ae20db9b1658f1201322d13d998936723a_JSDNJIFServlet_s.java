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
  * @ClassName com.jamcracker.jif.servlet.JSDNJIFServlet
  * @version 1.0
  * @author Prasad P Nair
  * @date 13-Dec-2012
  * @see
  *   
 /*************************************************************************/
 package com.jamcracker.jif.servlet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.jamcracker.jif.adapter.IJIFAdapter;
 import com.jamcracker.jif.common.AdapterFactory;
 import com.jamcracker.jif.common.JIFConstants;
 import com.jamcracker.jif.dataobject.FailureResponse;
 import com.jamcracker.jif.dataobject.JIFRequest;
 import com.jamcracker.jif.dataobject.JIFResponse;
 import com.jamcracker.jif.exception.AuthenticationException;
 import com.jamcracker.jif.exception.JIFException;
 import com.jamcracker.jif.util.JIFUtil;
 
 /**
  * This Class is the template for writing a web interface adapter which receives
  * the JIF requests from JSDN marketplace.
  * 
  * The doPost() method authenticate and process the RequestXML from JSDN, create
  * an JIFRequest object and delegate it to the corresponding methods in
  * {@link IJIFAdapter}
  * 
  * The following methods need to be implemented to integrate with your
  * application <br>
  * authenticate()<br>
  * createCompany()<br>
  * updateCompany()<br>
  * deleteCompany()<br>
  * createUser()<br>
  * updateUser()<br>
  * deleteUser()<br>
  * 
  * 
  */
 public class JSDNJIFServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
 			.getLogger(JSDNJIFServlet.class);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
 	 * , javax.servlet.http.HttpServletResponse)
 	 */
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException {
 		LOG.debug("START:<<doPost>>");
 		JIFResponse jifResponse = null;
 		JIFRequest jifRequest = null;
 		IJIFAdapter adapter = null;
 		PrintWriter out = response.getWriter();
 		String xmlMessage = readXML(request);
 		try {
 			try {
 				jifRequest = JIFUtil.processRequest(xmlMessage);
 				if (jifRequest == null) {
					out.println("Either XML not found or Invalid XML");
					out.println("XML recieved:");
					out.println(xmlMessage);
					out.flush();
 					return;
 				}
 				adapter = AdapterFactory.getAdapterImpl(jifRequest
 						.getEntityType());
 				// Authenticate the request
 				AdapterFactory.getAuthenticationModule().authenticate(
 						jifRequest.getAuthInfo());
 			} catch (AuthenticationException e) {
 				FailureResponse failureResponse = new FailureResponse(
 						e.getFaultCode(), e.getFaultString());
 				failureResponse.setEntityType(jifRequest.getEntityType());
 				failureResponse.setEventName(jifRequest.getEventName());
 				String responseMessage = JIFUtil
 						.createResponseXML(failureResponse);
 				out.println(responseMessage);
 				out.flush();
 				return;
 			}
 			jifResponse = adapter.processRequest(jifRequest);
 		} catch (JIFException e) {
 			jifResponse = new FailureResponse(e.getFaultCode(),
 					e.getFaultString());
 		} catch (Exception exec) {
 			jifResponse = new FailureResponse("12345", exec.getMessage());
 		}
 		jifResponse.setEntityType(jifRequest.getEntityType());
 		jifResponse.setEventName(jifRequest.getEventName());
 		String responseMessage = JIFUtil.createResponseXML(jifResponse);
 		out.println(responseMessage);
 		out.flush();
 		return;
 	}
 
 	/**
 	 * @param request
 	 * @return
 	 * @throws IOException
 	 */
 	protected String readXML(HttpServletRequest request){
 		//Read from the parameter
 		String xmlMessage = (String) request
 				.getParameter(JIFConstants.PARAM_NAME);
 		//If not read from body
 		if (xmlMessage == null) {
 			try{
 				BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
 				StringBuilder sb = new StringBuilder();
 				String line = "";
 				while ((line = reader.readLine()) != null){
 					sb.append(line);
 				}
 				xmlMessage = sb.toString();
 			}catch(Exception e){
 				LOG.error(e.getMessage(), e);
 			}
 		}
 		return xmlMessage;
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		doPost(req, resp);
 	}
 
 }

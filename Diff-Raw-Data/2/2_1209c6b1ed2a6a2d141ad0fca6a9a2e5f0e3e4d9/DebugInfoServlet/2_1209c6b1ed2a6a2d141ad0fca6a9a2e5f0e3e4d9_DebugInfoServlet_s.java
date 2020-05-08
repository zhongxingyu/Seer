 /*
  * Copyright 2010-2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.cloudfoundry.runjdwpinfo;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * @author Phillip Webb
  */
 @WebServlet(urlPatterns = "/*", loadOnStartup = 1)
 public class DebugInfoServlet extends HttpServlet {
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String port = System.getProperty("cloudfoundry.debug.port");
 		if (port != null && port.length() > 0) {
 			String host = request.getLocalAddr();
 			PrintWriter writer = response.getWriter();
			writer.append("{\"host\":\"" + host + "\",\"port\":" + port + "}");
 			writer.flush();
 		}
 	}
 }

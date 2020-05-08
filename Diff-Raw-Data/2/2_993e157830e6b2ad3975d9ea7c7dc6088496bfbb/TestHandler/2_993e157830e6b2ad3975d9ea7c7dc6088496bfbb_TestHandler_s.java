 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.acceptance.websever;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.Enumeration;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 
 public class TestHandler extends AbstractHandler {
 
 	@Override
 	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException,
 			ServletException {
 		StringBuilder b = new StringBuilder();
 		//
 		final String path = request.getPathInfo();
 		final String query = request.getQueryString();
 		final String method = request.getMethod();
 		//
 		b = new StringBuilder();
 		for (Enumeration<String> it = request.getHeaderNames(); it.hasMoreElements();) {
             String name = it.nextElement();
             String value = request.getHeader(name);
             b.append(", \"").append(name.toLowerCase()).append("\": ");
             if ("undefined".equals(value)) {
                 b.append(request.getHeader(name));
             } else {
                 b.append("\"").append(request.getHeader(name)).append("\"");
             }
         }
         final String headers = b.length() == 0 ? "{ }" : "{" + b.substring(1) + "}";
         //
         b = new StringBuilder();
         final BufferedReader bodyReader = request.getReader();
         b.append("");
         char[] charBuffer = new char[128];
         int bytesRead = -1;
         while ((bytesRead = bodyReader.read(charBuffer)) > 0) {
             b.append(charBuffer, 0, bytesRead);
         }
         final String body = b.length() == 0 ? null : b.toString();
         //
         if (path.indexOf("/redirect:") != -1) {
             String respCode = path.substring(10, path.indexOf("/", 9));
             response.setHeader("Location", "/");
             response.setStatus(Integer.parseInt(respCode));
         } else {
             response.setStatus(HttpServletResponse.SC_OK);
         }
         response.setContentType("text/html;charset=utf-8");
         baseRequest.setHandled(true);
         //
         b = new StringBuilder();
         b.append("{\n");
         b.append("\"path\": \"").append(path).append("\",\n");
         if (query == null) {
            b.append("\"query\": null,\n");
         } else {
             b.append("\"query\": \"").append(query).append("\",\n");
         }
         b.append("\"method\": \"").append(method).append("\",\n");
         b.append("\"headers\": ").append(headers).append(",\n");
         if (body == null) {
             b.append("\"body\": null\n");
         } else {
             b.append("\"body\": \"").append(body).append("\n");
         }
         b.append("}");
         response.getWriter().println(b.toString());
 	}
 }

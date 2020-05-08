 /*
  * Copyright (C) 2010 Evgeny Mandrikov
  *
  * Sonar-IDE is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar-IDE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar-IDE; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.ide.test;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 
 import javax.servlet.GenericServlet;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 /**
  * @author Evgeny Mandrikov
  */
 public abstract class TestServlet extends GenericServlet {
   private static final String DEFAULT_PACKAGE_NAME = "[default]";
 
   protected abstract String getResource(String classKey);
 
   protected File getResourceAsFile(String testName, String classKey) {
    String baseDir = StringUtils.defaultString(getServletConfig().getInitParameter("baseDir"), "target/sonar-data");
     return new File(baseDir + "/" + testName + "/" + getResource(classKey));
   }
 
   @Override
   public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
     PrintWriter out = response.getWriter();
     String json;
     try {
       String resourceKey = request.getParameter("resource");
       String[] parts = resourceKey.split(":");
       String groupId = parts[0];
       String artifactId = parts[1];
       String classKey = parts[2];
       SonarTestServer.LOG.debug("Loading data for {}:{}:{}", new Object[]{groupId, artifactId, classKey});
       if (classKey.startsWith(DEFAULT_PACKAGE_NAME)) {
         classKey = StringUtils.substringAfter(classKey, ".");
       }
       String testName;
       if (StringUtils.contains(groupId, ".")) {
         testName = StringUtils.substringAfterLast(groupId, ".");
       } else {
         testName = groupId;
       }
       File resourceFile = getResourceAsFile(testName, classKey);
       SonarTestServer.LOG.info("Resource file: {}", resourceFile);
       json = FileUtils.readFileToString(resourceFile);
     } catch (Exception e) {
       SonarTestServer.LOG.error(e.getMessage(), e);
       json = "[]";
     }
     out.println(json);
   }
 }

 package uk.ac.ebi.arrayexpress.servlets;
 
 /*
  * Copyright 2009-2011 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.InputSource;
 import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
 import uk.ac.ebi.arrayexpress.components.Files;
 import uk.ac.ebi.arrayexpress.components.SaxonEngine;
 import uk.ac.ebi.arrayexpress.components.Users;
 import uk.ac.ebi.arrayexpress.utils.CookieMap;
 import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
 import uk.ac.ebi.arrayexpress.utils.RegexHelper;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 import uk.ac.ebi.arrayexpress.utils.saxon.FlatFileXMLReader;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.transform.sax.SAXSource;
import java.io.*;
 import java.net.URLDecoder;
 
 public class FlatFileTransformationServlet extends ApplicationServlet
 {
     private static final long serialVersionUID = -2909054413280338250L;
 
     private transient final Logger logger = LoggerFactory.getLogger(getClass());
 
     protected boolean canAcceptRequest( HttpServletRequest request, RequestType requestType )
     {
         return (requestType == RequestType.GET || requestType == RequestType.POST);
     }
 
     // Respond to HTTP requests from browsers.
     protected void doRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType )
             throws ServletException, IOException
     {
         RegexHelper PARSE_ARGUMENTS_REGEX = new RegexHelper("/([^/]+)/([^/]+)/([^/]+)/([^/]+)$", "i");
 
         logRequest(logger, request, requestType);
 
         String[] requestArgs = PARSE_ARGUMENTS_REGEX.match(request.getRequestURL().toString());
 
         if (null == requestArgs || requestArgs.length != 4
                 || "".equals(requestArgs[0]) || "".equals(requestArgs[1])
                 || "".equals(requestArgs[2]) || "".equals(requestArgs[3])) {
             throw new ServletException("Bad arguments passed via request URL [" + request.getRequestURL().toString() + "]");
         }
 
         String accession = requestArgs[0];
         String fileName = requestArgs[1];
         String stylesheet = requestArgs[2];
         String outputType = requestArgs[3];
 
         HttpServletRequestParameterMap params = new HttpServletRequestParameterMap(request);
 
         // adding "host" request header so we can dynamically create FQDN URLs
         params.put("host", request.getHeader("host"));
         params.put("basepath", request.getContextPath());
 
         params.put("accession", accession);
         params.put("filename", fileName);
 
         // to make sure nobody sneaks in the other value w/o proper authentication
         params.put("userid", "1");
 
         PrintWriter out = null;
         try {
             SaxonEngine saxonEngine = (SaxonEngine) getComponent("SaxonEngine");
             Files files = (Files) getComponent("Files");
 
             CookieMap cookies = new CookieMap(request.getCookies());
             if (cookies.containsKey("AeLoggedUser") && cookies.containsKey("AeLoginToken")) {
                 Users users = (Users) getComponent("Users");
                 String user = URLDecoder.decode(cookies.get("AeLoggedUser").getValue(), "UTF-8");
                 String passwordHash = cookies.get("AeLoginToken").getValue();
                 if (users.verifyLogin(user, passwordHash, request.getRemoteAddr().concat(request.getHeader("User-Agent")))) {
                     if ((users.isPrivileged(user))) { // superuser logged in -> remove user restriction
                             params.remove("userid");
                         } else {
                             params.put("userid", StringTools.listToString(users.getUserIDs(user), " OR "));
                         }
                 } else {
                     logger.warn("Removing invalid session cookie for user [{}]", user);
                     // resetting cookies
                     Cookie userCookie = new Cookie("AeLoggedUser", "");
                     userCookie.setPath("/");
                     userCookie.setMaxAge(0);
 
                     response.addCookie(userCookie);
                 }
             }
 
             String stylesheetName = new StringBuilder(stylesheet)
                     .append('-').append(outputType).append(".xsl").toString();
 
             String flatFileLocation = files.getLocation(accession, fileName);
             SAXSource source = new SAXSource();
             File flatFile = new File(files.getRootFolder(), flatFileLocation);
 
             if (null == flatFile || !flatFile.exists()) {
                 logger.error("Requested transformation of [{}] which is not found", flatFile.getAbsolutePath());
                 response.sendError(HttpServletResponse.SC_NOT_FOUND);
             } else {
                source.setInputSource(
                        new InputSource(
                                new InputStreamReader(
                                        new FileInputStream(flatFile)
                                        , "UTF-8"
                                )
                        )
                );
                 source.setXMLReader(new FlatFileXMLReader());
 
                 if (outputType.equals("html")) {
                     response.setContentType("text/html; charset=ISO-8859-1");
                 } else {
                     response.setContentType("text/" + outputType + "; charset=UTF-8");
                 }
                 // Disable cache no matter what (or we're fucked on IE side)
                 response.addHeader("Pragma", "no-cache");
                 response.addHeader("Cache-Control", "no-cache");
                 response.addHeader("Cache-Control", "must-revalidate");
                 response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some date in the past
 
                 // Output goes to the response PrintWriter.
                 out = response.getWriter();
 
                 if (!saxonEngine.transformToWriter(
                         source
                         , stylesheetName
                         , params
                         , out
                 )) {                     // where to dump resulting text
                         throw new Exception("Transformation returned an error");
                 }
             }
         } catch (Exception x) {
             throw new RuntimeException(x);
         } finally {
             if (null != out) {
                 out.close();
             }
         }
     }
 }

 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.connect;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.List;
 
 import org.eclipse.core.internal.preferences.Base64;
 import org.nuxeo.ide.common.IOUtils;
 import org.nuxeo.ide.connect.studio.StudioProject;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class Connector {
 
     public static Connector getDefault() throws Exception {
         ConnectPreferences prefs = ConnectPreferences.load();
         return new Connector(prefs.getHost(), prefs.getUsername(),
                 prefs.getPassword());
     }
 
     public final static String DEFAULT_URL = "http://localhost:8080/nuxeo/site/studio/api";
 
     // private final static String DEFAULT_URL =
     // "https://connect.nuxeo.com/nuxeo/site/studio/api";
 
     protected URL baseUrl;
 
     protected String username;
 
     protected String password;
 
     protected String auth;
 
     public Connector(String url, String username, String password)
             throws MalformedURLException {
         this(new URL(url), username, password);
     }
 
     public Connector(URL url, String username, String password) {
         this.baseUrl = url;
         this.username = username;
         this.password = password;
         if (username != null) {
             this.auth = basicAuth(username, password);
         }
     }
 
     public URL getUrl() {
         return baseUrl;
     }
 
     public String getUsername() {
         return username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public InputStream getProjects() throws IOException {
         if (auth == null) {
             return null;
         }
         URL url = new URL(this.baseUrl, "api/projects");
         URLConnection conn = url.openConnection();
         conn.setDoInput(true);
         conn.setDoOutput(false);
         conn.setRequestProperty("Authorization", "Basic " + auth);
         int status = ((HttpURLConnection) conn).getResponseCode();
         if (status > 399) {
             throw new IOException("Server error: " + status);
         }
         return conn.getInputStream();
     }
 
     public List<StudioProject> getProjectList() throws IOException {
         InputStream in = getProjects();
         try {
             return StudioProject.readProjects(in);
         } finally {
             in.close();
         }
     }
 
     public String getProjectContent(String projectId) throws Exception {
         if (auth == null) {
             return null;
         }
         URL url = new URL(baseUrl, "api/projects/" + projectId);
         URLConnection conn = url.openConnection();
         conn.setDoInput(true);
         conn.setDoOutput(false);
         conn.setRequestProperty("Authorization", "Basic " + auth);
         InputStream in = conn.getInputStream();
         return IOUtils.read(in);
     }
 
     public boolean exportOperationRegistry(String projectId, String reg)
             throws Exception {
         if (auth == null) {
             return false;
         }
         URL url = new URL(baseUrl, "api/projects/" + projectId + "/operations");
         URLConnection conn = url.openConnection();
         conn.setRequestProperty("Content-Type", "application/studio-registry");
         conn.setDoInput(true);
         conn.setDoOutput(true);
         conn.setRequestProperty("Authorization", "Basic " + auth);
         OutputStream out = conn.getOutputStream();
         out.write(reg.getBytes("UTF-8"));
         out.flush();
         int status = ((HttpURLConnection) conn).getResponseCode();
         out.close();
         if (status > 399) {
             throw new RuntimeException("Server Error: " + status);
         }
         return true;
     }
 
     public static String basicAuth(String username, String pwd) {
         return new String(Base64.encode((username + ":" + pwd).getBytes()));
     }
 
 }

 /*
  * Copyright (C) 2003-2009 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.jcr.benchmark.jcrapi.webdav;
 
 import org.exoplatform.common.http.client.HTTPConnection;
 import org.exoplatform.common.http.client.HTTPResponse;
 import org.exoplatform.common.http.client.HttpOutputStream;
 import org.exoplatform.common.http.client.ModuleException;
 import org.exoplatform.common.http.client.NVPair;
 import org.exoplatform.services.rest.ExtHttpHeaders;
 
 import java.io.IOException;
 
 import javax.ws.rs.core.HttpHeaders;
 
 /**
  * @author <a href="mailto:dmitry.kataev@exoplatform.com">Dmytro Katayev</a>
  * @version $Id$
  *
  */
 public class JCRWebdavConnection extends HTTPConnection
 {
 
    private String realm;
 
    private String user;
 
    private String pass;
 
    private String workspacePath;
 
    public JCRWebdavConnection(WebdavTestContext context)
    {
       super(context.getHost(), context.getPort());
 
       user = context.getUser();
       pass = context.getPassword();
       realm = context.getRealm();
 
       addBasicAuthorization(realm, user, pass);
 
       workspacePath = context.getWorkspacePath();
    }
    
    public void addNode(String name, byte[] data) throws IOException, ModuleException
    {
       Put(workspacePath + name, data).getStatusCode();
    }
    
    public void addNode(String name, String nodeType, byte[] data) throws IOException, ModuleException
    {
       NVPair[] headers = new NVPair[1];
       headers[0] = new NVPair("File-NodeType", nodeType);
       Put(workspacePath + name, data).getStatusCode();
    }
    
    public HTTPResponse addNode(String name, HttpOutputStream stream) throws IOException, ModuleException
    {
       return Put(workspacePath + name, stream);
    }
    
    public void removeNode(String name) throws IOException, ModuleException
    {
       Delete(workspacePath + name).getStatusCode();
    }
    
    public void getNode(String name) throws IOException, ModuleException
    {
       Get(workspacePath + name).getStatusCode();
    }
    
    public void addProperty(String nodeName, String property) throws IOException, ModuleException
    {
      String xmlBody = "<?xml version='1.0' encoding='utf-8' ?>" +
      "<D:propertyupdate xmlns:D='DAV:' xmlns:Z='http://www.w3.com/standards/z39.50/'>" +
          "<D:set>" +
             "<D:prop>" +
               "<" + property + ">default value</" + property + ">" +
            "</D:prop>" +
          "</D:set>" +
       "</D:propertyupdate>";
 
       NVPair[] headers = new NVPair[2];
       headers[0] = new NVPair(HttpHeaders.CONTENT_TYPE, "text/xml; charset='utf-8'");
       headers[1] = new NVPair(HttpHeaders.CONTENT_LENGTH, Integer.toString(xmlBody.length()));
 
       ExtensionMethod("PROPPATCH", workspacePath + nodeName, xmlBody.getBytes(), headers).getStatusCode();
    }
 
    
    public void setProperty(String nodeName, String property, String value) throws IOException, ModuleException
    {
       String xmlBody = "<?xml version='1.0' encoding='utf-8' ?>" +
      "<D:propertyupdate xmlns:D='DAV:' xmlns:Z='http://www.w3.com/standards/z39.50/'>" +
          "<D:set>" +
             "<D:prop>" +
                "<" + property + ">" +value + "</" + property + ">" +
             "</D:prop>" +
          "</D:set>" +
       "</D:propertyupdate>";
 
       NVPair[] headers = new NVPair[2];
       headers[0] = new NVPair(HttpHeaders.CONTENT_TYPE, "text/xml; charset='utf-8'");
       headers[1] = new NVPair(HttpHeaders.CONTENT_LENGTH, Integer.toString(xmlBody.length()));
 
      ExtensionMethod("PROPPATCH", workspacePath + nodeName, xmlBody.getBytes(), headers).getStatusCode();
    }
    
    public void removeProperty(String nodeName, String property) throws IOException, ModuleException
    {
       String xmlBody = "<?xml version='1.0' encoding='utf-8' ?>" +
             "<D:propertyupdate xmlns:D='DAV:' xmlns:Z='http://www.w3.com/standards/z39.50/'>" +
                "<D:remove>" +
                   "<D:prop><" + property + "/></D:prop>" +
                "</D:remove>" +
             "</D:propertyupdate>";
       
       NVPair[] headers = new NVPair[2];
       headers[0] = new NVPair(HttpHeaders.CONTENT_TYPE, "text/xml; charset='utf-8'");
       headers[1] = new NVPair(HttpHeaders.CONTENT_LENGTH, Integer.toString(xmlBody.length()));
       
       ExtensionMethod("PROPPATCH", workspacePath + nodeName, xmlBody.getBytes(), headers).getStatusCode();
    }
 
    public String lock(String nodeName) throws IOException, ModuleException
    {
       String xmlBody = "<?xml version='1.0' encoding='utf-8' ?>" +
       		"<D:lockinfo xmlns:D='DAV:'>" +
       		   "<D:lockscope>" +
       		      "<D:exclusive/>" +
       		   "</D:lockscope>" +
       		   "<D:locktype>" +
       		      "<D:write/>" +
       		   "</D:locktype>" +
       		   "<D:owner>owner</D:owner>" +
       		"</D:lockinfo>";
 
       NVPair[] headers = new NVPair[2];
       headers[0] = new NVPair(HttpHeaders.CONTENT_TYPE, "text/xml; charset='utf-8'");
       headers[1] = new NVPair(HttpHeaders.CONTENT_LENGTH, Integer.toString(xmlBody.length()));
 
       HTTPResponse response = ExtensionMethod("LOCK", workspacePath + nodeName, xmlBody.getBytes(), headers);
       
       response.getStatusCode();
       StringBuffer resp = new StringBuffer(new String(response.getData(), "UTF-8"));
       
       final String lockPrffix = "opaquelocktoken:";
       
       int pos = resp.lastIndexOf(lockPrffix);
       
       String lockToken = resp.substring(pos + lockPrffix.length() , pos + lockPrffix.length() + 32);
       
       return lockToken;
    }
 
    public void unlock(String nodeName, String lockToken) throws IOException, ModuleException
    {
       NVPair[] headers = new NVPair[3];
       headers[0] = new NVPair(HttpHeaders.CONTENT_TYPE, "text/xml; charset='utf-8'");
       headers[1] = new NVPair(HttpHeaders.CONTENT_LENGTH, Integer.toString("".length()));
       headers[2] = new NVPair(ExtHttpHeaders.LOCKTOKEN, "<" + lockToken + ">");
 
       HTTPResponse response = ExtensionMethod("UNLOCK", workspacePath + nodeName, "".getBytes(), headers);
       response.getStatusCode();
    }
    
    public void addVersionControl(String nodeName) throws IOException, ModuleException 
    {
       NVPair[] headers = new NVPair[1];
 //      headers[0] = new NVPair(HttpHeaders.CONTENT_TYPE, "text/xml; charset='utf-8'");
       headers[0] = new NVPair(HttpHeaders.CONTENT_LENGTH, Integer.toString("".length()));
 
       HTTPResponse response = ExtensionMethod("VERSION-CONTROL", workspacePath + nodeName, "".getBytes(), headers);
       response.getStatusCode();
    }
 
    public void checkIn(String nodeName) throws IOException, ModuleException 
    {
       NVPair[] headers = new NVPair[1];
 //    headers[0] = new NVPair(HttpHeaders.CONTENT_TYPE, "text/xml; charset='utf-8'");
     headers[0] = new NVPair(HttpHeaders.CONTENT_LENGTH, Integer.toString("".length()));
 
     HTTPResponse response = ExtensionMethod("CHECKIN", workspacePath + nodeName, "".getBytes(), headers);
     response.getStatusCode();  
    }
 
    public void checkOut(String nodeName) throws IOException, ModuleException
    {
       NVPair[] headers = new NVPair[1];
 //      headers[0] = new NVPair(HttpHeaders.CONTENT_TYPE, "text/xml; charset='utf-8'");
       headers[0] = new NVPair(HttpHeaders.CONTENT_LENGTH, Integer.toString("".length()));
 
       HTTPResponse response = ExtensionMethod("CHECKOUT", workspacePath + nodeName, "".getBytes(), headers);
       response.getStatusCode();  
    }
 
 }

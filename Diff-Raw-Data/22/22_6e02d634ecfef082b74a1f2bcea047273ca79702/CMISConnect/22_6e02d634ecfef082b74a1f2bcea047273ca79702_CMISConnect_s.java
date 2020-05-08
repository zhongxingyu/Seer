 /**************************************************************
  * 
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  * 
  *************************************************************/
 package org.apache.aoo.cmisucp.cmis;
 
 import com.sun.star.uno.XComponentContext;
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.chemistry.opencmis.client.api.CmisObject;
 import org.apache.chemistry.opencmis.client.api.Folder;
 import org.apache.chemistry.opencmis.client.api.Session;
 import org.apache.chemistry.opencmis.client.api.SessionFactory;
 import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
 import org.apache.chemistry.opencmis.commons.SessionParameter;
 import org.apache.chemistry.opencmis.commons.enums.BindingType;
 import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
 
 /**
  *
  * @author rajath
  */
 public class CMISConnect {
     
     private String server_url;
     private String username;
     private String password;
     private String repositoryID;
     private String nameObj;
     private String parentURL;
     private String parentName;
     private String repositoryURL;
     private String url;
     
     private Session connected_session;
     private Folder root;
     private CmisObject content;
     
     private String contentType;
     private XComponentContext m_Context;
     
     private final String cmisHTTP = "cmis";
     private final String cmisHTTPS = "cmiss";
     
     // To be removed
     public CMISConnect(XComponentContext context, String address, String user, String pwd, String repo_id , String relative_path )
     {
         m_Context = context;
         server_url = address;
         password = pwd;
         username = user;
         repositoryID = repo_id;
         
         Map<String,String> session_parameter = new HashMap<String, String>();
         
         session_parameter.put(SessionParameter.ATOMPUB_URL, address);
         session_parameter.put(SessionParameter.USER, username);
         session_parameter.put(SessionParameter.PASSWORD, password);
         session_parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
         session_parameter.put(SessionParameter.REPOSITORY_ID, repositoryID);
         
         SessionFactory factory = SessionFactoryImpl.newInstance();
                 
         connected_session = factory.createSession(session_parameter);
                 
         root = connected_session.getRootFolder();
         
         content = connected_session.getObjectByPath(relative_path);
         contentType = content.getBaseTypeId().value();
         
     }
     
     public CMISConnect(XComponentContext xContext, CmisObject ob, Session s)
     {
         content = ob;
         connected_session = s;
     }
     public CMISConnect(XComponentContext xComponentContext, String uri, String user, String pwd )
     {
         m_Context = xComponentContext;
         username = user;
         password = pwd;
         url = uri;
         decodeURI(uri);
         contentType = content.getBaseTypeId().value();
     }
         
     private boolean connectToRepository( String address, String repoID )
     {
         Map<String,String> sessionParameters = new HashMap<String, String>();
         
         //Authentication parameters
         sessionParameters.put(SessionParameter.USER, username);
         sessionParameters.put(SessionParameter.PASSWORD, password);
         
         //Repository Information
        sessionParameters.put(SessionParameter.ATOMPUB_URL, address);
        sessionParameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        sessionParameters.put(SessionParameter.REPOSITORY_ID, repoID);
        
        Session session;
        
        try
        {
           SessionFactory factory = SessionFactoryImpl.newInstance();
           session = factory.createSession(sessionParameters);
           server_url = address;
           repositoryID = repoID;
        }
        catch(CmisBaseException ex)           
        {
           return false;
        }
        
        connected_session = session;
                      
        return true;
     }
     
     public String getURL()
     {
         return url;
     }
     private void decodeURI(String URI)        
     {        
         if(URI.startsWith(cmisHTTP))
         {
             repositoryURL = "cmis://";
            URI = URI.replaceFirst(cmisHTTP, "http");
            String tempURL = URI.substring(7);
            tempURL = tempURL.replaceAll("//", "/");
            URI = "http://" + tempURL;        
         }
         else if(URI.startsWith(cmisHTTPS))
         {
             repositoryURL = "cmiss://";
            URI = URI.replaceFirst(cmisHTTPS, "https");
            String tempURL = URI.substring(8);
            tempURL = tempURL.replaceAll("//", "/");
            URI = "http://" + tempURL;        
         }               
         if(URI.endsWith("/"))
             URI = URI.substring(0,URI.length()-1);
         
         int prompt = URI.indexOf("://")+3;      
         int indexOfServerPath = URI.indexOf('/', prompt);
         int indexOfRepoID = URI.indexOf('/', indexOfServerPath+1);
         while(!connectToRepository(URI.substring(0, indexOfServerPath),URI.substring(indexOfServerPath+1, indexOfRepoID)))        
         {
             prompt = indexOfServerPath;
             indexOfServerPath = URI.indexOf('/', prompt+1);
             indexOfRepoID = URI.indexOf('/', indexOfServerPath+1);
             if(indexOfRepoID<0)
             {    
                 URI = URI+"/";
                 indexOfRepoID = URI.indexOf('/', indexOfServerPath+1);
             }
         }          
         String localpath = URI.substring(indexOfRepoID);
         repositoryURL = URI.substring(0,indexOfRepoID);
         repositoryURL = repositoryURL.replaceFirst("http", "cmis");
         int lastSlash = URI.lastIndexOf('/');
         nameObj = URI.substring(lastSlash+1);
         parentURL = URI.substring(0, lastSlash);
         parentURL = parentURL.replaceFirst("http", "cmis");
        /* if(parentURL.equals(repositoryURL))
         {
             parentURL = parentURL+"/";
         }*/
         connectToObject(localpath);
         
     }    
     private void connectToObject(String path)
     {        
         try
         {
             content = connected_session.getObjectByPath(path);            
             contentType = content.getBaseTypeId().value();
         }
         catch(CmisBaseException ex)
         {
             content = null;
             contentType = null;
         }
     }
     private void connectToID(String id)
     {
         try
         {
             content = connected_session.getObject(id);
             contentType = content.getBaseTypeId().value();
         }
         catch(CmisBaseException ex)
         {
               content = null;
               contentType = null;
         }
     }
     public Session getSession()
     {
         return connected_session;
     }
     
     public CmisObject getObject()
     {
         return content;
     }
     
     public String getContentType()
     {
         return contentType;
     }
     
     public String getParentURL()
     {
         return parentURL;
     }
     
     public String getRepositoryURL()
     {
         return repositoryURL;
     }
 }

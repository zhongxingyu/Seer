 package org.apache.maven.plugins.patchtracker.tracking.jenkins;
 /*
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
  */
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.AuthCache;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.BasicAuthCache;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.util.EntityUtils;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.plugins.patchtracker.tracking.PatchTracker;
 import org.apache.maven.plugins.patchtracker.tracking.PatchTrackerException;
 import org.apache.maven.plugins.patchtracker.tracking.PatchTrackerRequest;
 import org.apache.maven.plugins.patchtracker.tracking.PatchTrackerResult;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * @author Olivier Lamy
  * @plexus.component role="org.apache.maven.plugins.patchtracker.tracking.PatchTracker" role-hint="jenkins"
  */
 public class JenkinsPatchTracker
     implements PatchTracker
 {
 
 
     public PatchTrackerResult createPatch( PatchTrackerRequest patchTrackerRequest, Log log )
         throws PatchTrackerException
     {
         DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
         File tmpPathFile = null;
         try
         {
             tmpPathFile = File.createTempFile( "jenkins", "patch" );
             FileUtils.write( tmpPathFile, patchTrackerRequest.getPatchContent() );
 
             HttpPost post = new HttpPost( patchTrackerRequest.getUrl() + "/buildWithParameters?delay=0sec" );
 
             MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
 
             FileBody fileBody = new FileBody( tmpPathFile );
             entity.addPart( "patch.diff", fileBody );
 
             // jenkins need this format ?
             //Content-Disposition: form-data; name="name" patch.summary
             //Content-Disposition: form-data; name="value" sum
 
             // charset ?
             /*  post additionnal parameters with summary and description.
                 fail currently !
             entity.addPart( "patch.summary", new StringBody( patchTrackerRequest.getSummary() ) );
             entity.addPart( "patch.description", new StringBody( patchTrackerRequest.getDescription() ) );
 
 
             entity.addPart( "name", new StringBody( "patch.summary" ) );
             entity.addPart( "value", new StringBody( patchTrackerRequest.getSummary() ) );
 
             entity.addPart( "name", new StringBody( "patch.description" ) );
             entity.addPart( "value", new StringBody( patchTrackerRequest.getDescription() ) );
             */
             post.setEntity( entity );
 
             BasicHttpContext context = null;
 
             if ( StringUtils.isNotEmpty( patchTrackerRequest.getUserName() ) )
             {
 
                 defaultHttpClient.getCredentialsProvider().setCredentials(
                    new AuthScope( new AuthScope( post.getURI().getHost(), post.getURI().getPort() ) ),
                     new UsernamePasswordCredentials( patchTrackerRequest.getUserName(),
                                                      patchTrackerRequest.getPassword() ) );
 
                 // Jenkins doesn't challenge so use a preemptive mode
                 AuthCache authCache = new BasicAuthCache();
                 BasicScheme basicAuth = new BasicScheme();
                HttpHost targetHost =
                    new HttpHost( post.getURI().getHost(), post.getURI().getPort(), post.getURI().getScheme() );
                 authCache.put( targetHost, basicAuth );
 
                 context = new BasicHttpContext();
                 context.setAttribute( ClientContext.AUTH_CACHE, authCache );
             }
             HttpResponse r;
             if ( context == null )
             {
                 r = defaultHttpClient.execute( post );
             }
             else
             {
                 r = defaultHttpClient.execute( post, context );
             }
             log.debug(
                 "r:" + r.getStatusLine().getStatusCode() + ", status: " + r.getStatusLine().getReasonPhrase() + ","
                     + EntityUtils.toString( r.getEntity() ) );
 
             int statusCode = r.getStatusLine().getStatusCode();
 
             // Jenkins returns 302
             if ( statusCode != 200 && statusCode != 302 )
             {
                 throw new PatchTrackerException(
                     "Jenkins returned :" + statusCode + " with ReasonPhrase :" + r.getStatusLine().getReasonPhrase() );
             }
 
             return new PatchTrackerResult();
         }
         catch ( IOException e )
         {
             throw new PatchTrackerException( e.getMessage(), e );
         }
         finally
         {
             FileUtils.deleteQuietly( tmpPathFile );
         }
     }
 
     public PatchTrackerResult updatePatch( PatchTrackerRequest patchTrackerRequest, Log log )
         throws PatchTrackerException
     {
         throw new RuntimeException( "updatePatch is not implemented with Jenkins" );
     }
 
 }

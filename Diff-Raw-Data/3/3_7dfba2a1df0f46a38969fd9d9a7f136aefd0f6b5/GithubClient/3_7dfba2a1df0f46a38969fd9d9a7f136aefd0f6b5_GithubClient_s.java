 /*
  * Copyright 2011 Kevin Pollet
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.maven.plugin.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.github.maven.plugin.client.exceptions.GithubDownloadAlreadyExistException;
 import com.github.maven.plugin.client.exceptions.GithubException;
 import com.github.maven.plugin.client.exceptions.GithubDownloadNotFoundException;
 import com.github.maven.plugin.client.exceptions.GithubRepositoryNotFoundException;
 import com.github.maven.plugin.util.Contract;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.apache.commons.httpclient.methods.multipart.StringPart;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  * @author Kevin Pollet
  */
 //TODO client configuration proxy
 //TODO Use an object to map json response
 public class GithubClient {
 
 	private final static String GITHUB_S3_URL = "https://github.s3.amazonaws.com/";
 
 	private final static String GITHUB_DOWNLOADS_URL_FORMAT = "https://github.com/%s/%s/downloads";
 
 	private final String login;
 
 	private final String token;
 
 	public GithubClient(String login, String token) {
 		this.login = login;
 		this.token = token;
 	}
 
 	public Set<String> listDownloads(String repository) {
 		return getDownloadsInfos( repository ).keySet();
 	}
 
 	public void upload(File file, String description, String repository) {
 		Contract.assertNotNull( repository, "repository" );
 		Contract.assertNotNull( file, "file" );
 
 		final String downloadsUrl = String.format( GITHUB_DOWNLOADS_URL_FORMAT, login, repository );
 
 		HttpClient githubClient = new HttpClient();
 		PostMethod githubPost = new PostMethod( downloadsUrl );
 		githubPost.setRequestBody(
 				new NameValuePair[] {
 						new NameValuePair( "login", login ),
 						new NameValuePair( "token", token ),
 						new NameValuePair( "file_name", file.getName() ),
						new NameValuePair( "file_size", String.valueOf( file.length() ) )
 				}
 		);
 
 		try {
 
 			int response = githubClient.executeMethod( githubPost );
 
 			if ( response == HttpStatus.SC_OK ) {
 
 				ObjectMapper mapper = new ObjectMapper();
 				JsonNode node = mapper.readValue( githubPost.getResponseBodyAsString(), JsonNode.class );
 
 				PostMethod s3Post = new PostMethod( GITHUB_S3_URL );
 
 				Part[] parts = {
 						new StringPart( "Filename", file.getName() ),
 						new StringPart( "policy", node.path( "policy" ).getTextValue() ),
 						new StringPart( "success_action_status", "201" ),
 						new StringPart( "key", node.path( "path" ).getTextValue() ),
 						new StringPart( "AWSAccessKeyId", node.path( "accesskeyid" ).getTextValue() ),
 						new StringPart( "Content-Type", node.path( "mime_type" ).getTextValue() ),
 						new StringPart( "signature", node.path( "signature" ).getTextValue() ),
 						new StringPart( "acl", node.path( "acl" ).getTextValue() ),
 						new FilePart( "file", file )
 				};
 
 				HttpClient s3client = new HttpClient();
 				MultipartRequestEntity partEntity = new MultipartRequestEntity( parts, s3Post.getParams() );
 				s3Post.setRequestEntity( partEntity );
 
 				int s3Response = s3client.executeMethod( s3Post );
 				if ( s3Response != HttpStatus.SC_CREATED ) {
 					throw new GithubException( "Cannot upload " + file.getName() + " to repository " + repository );
 				}
 
 			}
 			else if ( response == HttpStatus.SC_NOT_FOUND ) {
 				throw new GithubRepositoryNotFoundException( "Cannot found repository " + repository );
 			}
 			else if ( response == HttpStatus.SC_UNPROCESSABLE_ENTITY ) {
 				throw new GithubDownloadAlreadyExistException( "File " + file.getName() + "already exist in " + repository + " repository" );
 			}
 			else {
 				throw new GithubException( "Error " + HttpStatus.getStatusText( response ) );
 			}
 		}
 		catch ( IOException e ) {
 			throw new GithubException( e );
 		}
 
 	}
 
 	public void replace(String downloadName, File file, String description, String repository) {
 		Contract.assertNotNull( downloadName, "downloadName" );
 		Contract.assertNotNull( file, "file" );
 		Contract.assertNotNull( description, "description" );
 		Contract.assertNotNull( repository, "repository" );
 
 		delete( repository, downloadName );
 		upload( file, description, repository );
 	}
 
 	private void delete(String repository, String downloadName) {
 		Contract.assertNotNull( repository, "repository" );
 		Contract.assertNotNull( downloadName, "downloadName" );
 
 		final Map<String, Integer> downloads = getDownloadsInfos( repository );
 		final String downloadUrl = String.format(
 				GITHUB_DOWNLOADS_URL_FORMAT + "/%d", login, repository, downloads.get( downloadName )
 		);
 
 		if ( !downloads.containsKey( downloadName ) ) {
 			throw new GithubDownloadNotFoundException( "The download " + downloadName + " cannot be found in " + repository );
 		}
 
 		HttpClient client = new HttpClient();
 		PostMethod githubDelete = new PostMethod( downloadUrl );
 		githubDelete.setRequestBody(
 				new NameValuePair[] {
 						new NameValuePair( "_method", "delete" ),
 						new NameValuePair( "login", login ),
 						new NameValuePair( "token", token )
 				}
 		);
 
 		try {
 
 			int response = client.executeMethod( githubDelete );
 			if ( response != HttpStatus.SC_MOVED_TEMPORARILY ) {
 				throw new GithubException( "Unexpected error" + HttpStatus.getStatusText( response ) );
 			}
 
 		}
 		catch ( IOException e ) {
 			throw new GithubException( e );
 		}
 
 	}
 
 	private Map<String, Integer> getDownloadsInfos(String repository) {
 		Map<String, Integer> downloads = new HashMap<String, Integer>();
 
 		final String downloadsUrl = String.format( GITHUB_DOWNLOADS_URL_FORMAT, login, repository );
 
 		HttpClient githubClient = new HttpClient();
 		GetMethod githubGet = new GetMethod( downloadsUrl );
 		githubGet.setQueryString(
 				new NameValuePair[] {
 						new NameValuePair( "login", login ),
 						new NameValuePair( "token", token )
 				}
 		);
 
 		int response;
 
 		try {
 
 			response = githubClient.executeMethod( githubGet );
 		}
 		catch ( IOException e ) {
 			throw new GithubRepositoryNotFoundException(
 					"Cannot retrieve github repository " + repository + " informations", e
 			);
 		}
 
 
 		if ( response == HttpStatus.SC_OK ) {
 
 			String githubResponse;
 
 			try {
 
 				githubResponse = githubGet.getResponseBodyAsString();
 			}
 			catch ( IOException e ) {
 				throw new GithubRepositoryNotFoundException(
 						"Cannot retrieve github repository " + repository + " informations", e
 				);
 			}
 
 			Pattern pattern = Pattern.compile(
 					String.format(
 							"<a href=\"(/downloads)?/%s/%s/([^\"]+)\"", login, repository
 					)
 			);
 
 			Matcher matcher = pattern.matcher( githubResponse );
 
 			while ( matcher.find() ) {
 				String tmp = matcher.group( 2 );
 
 				if ( tmp.contains( "downloads" ) ) {
 					String id = matcher.group( 2 ).substring( tmp.lastIndexOf( '/' ) + 1, tmp.length() );
 					Integer downloadId = Integer.parseInt( id );
 
 					if ( matcher.find() ) {
 						downloads.put( matcher.group( 2 ), downloadId );
 					}
 
 				}
 
 			}
 
 		}
 		else if ( response == HttpStatus.SC_NOT_FOUND ) {
 			throw new GithubRepositoryNotFoundException( "Cannot found repository " + repository );
 		}
 		else {
 			throw new GithubRepositoryNotFoundException( "Cannot retrieve github repository " + repository + " informations" );
 		}
 
 		return downloads;
 
 	}
 
 }

 /**
  *    Copyright 2012 meltmedia
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
 package com.meltmedia.cadmium.servlets.jersey;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 
 import javax.inject.Inject;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
 import com.meltmedia.cadmium.core.ConfigurationGitService;
 import com.meltmedia.cadmium.core.ContentGitService;
 import com.meltmedia.cadmium.core.FileSystemManager;
 import com.meltmedia.cadmium.core.SiteDownService;
 import com.meltmedia.cadmium.core.config.ConfigManager;
 import com.meltmedia.cadmium.core.git.DelayedGitServiceInitializer;
 import com.meltmedia.cadmium.core.git.GitService;
 import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
 import com.meltmedia.cadmium.core.messaging.ChannelMember;
 import com.meltmedia.cadmium.core.messaging.MessageSender;
 import com.meltmedia.cadmium.status.Status;
 import com.meltmedia.cadmium.status.StatusMember;
 
 @CadmiumSystemEndpoint
 @Path("/status")
 public class StatusService extends AuthorizationService {
   private final Logger logger = LoggerFactory.getLogger(getClass());
 
 	//constants
 	public final String ENVIRON_DEV = "development";
 	public final String ENVIRON_QA = "qa";
   public final String ENVIRON_STAGING = "staging";
 	public final String ENVIRON_GENE = "gene";
 	public final String ENVIRON_PROD = "prod";
 	public final String MAINT_ON = "on";
 	public final String MAINT_OFF = "off";
 	
 	@Inject
 	protected MessageSender sender;
 	
 	@Inject
 	protected SiteDownService maintService;
 	
 	@Inject
 	protected LifecycleService lifecycleService;
 	
 	@Inject
 	@ContentGitService
 	protected DelayedGitServiceInitializer gitService;
   
   @Inject
   @ConfigurationGitService
   protected DelayedGitServiceInitializer configGitService;
 
 	@Inject
 	protected ConfigManager configManager;
 	
 	@GET
 	@Path("/Ping")
 	@Produces("text/plain")
 	public String ping() {
 	  return "Ok";
 	}
 	
   @GET
 	@Path("/health")
 	@Produces("text/plain")
 	public String health(@Context HttpServletRequest request) {
 	  StringBuilder builder = new StringBuilder();
 	  builder.append("Server: "+request.getServerName() + "\n");
 	  builder.append("Scheme: "+request.getScheme() + "\n");
     builder.append("Port: "+request.getServerPort() + "\n");
     builder.append("ContextPath:  " + request.getContextPath() + "\n");
     builder.append("ServletPath: " + request.getServletPath() + "\n");
     builder.append("Uri: " + request.getRequestURI() + "\n");
     builder.append("Query: " + request.getQueryString() + "\n");
 	  Enumeration<?> headerNames = request.getHeaderNames();
 	  builder.append("Headers:\n");
 	  while(headerNames.hasMoreElements()) {
 	    String name = (String) headerNames.nextElement();
 	    Enumeration<?> headers = request.getHeaders(name);
 	    builder.append("  '" + name + "':\n");
 	    while(headers.hasMoreElements()) {
 	      String headerValue = (String) headers.nextElement();
 	      builder.append("    -"+headerValue+"\n");
 	    }
 	  }
 	  if(request.getCookies() != null) {
   	  builder.append("Cookies:\n");
   	  for(Cookie cookie : request.getCookies()) {
   	    builder.append("  '" + cookie.getName() + "':\n");
         builder.append("    value: " + cookie.getValue() + "\n");
   	    builder.append("    domain: " + cookie.getDomain() + "\n");
         builder.append("    path: " + cookie.getPath() + "\n");
         builder.append("    maxAge: " + cookie.getMaxAge() + "\n");
         builder.append("    version: " + cookie.getVersion() + "\n");
         builder.append("    comment: " + cookie.getComment() + "\n");
         builder.append("    secure: " + cookie.getSecure() + "\n");
   	  }
 	  }
 	  return builder.toString();
 	}
 	
 	@GET	
 	@Produces(MediaType.APPLICATION_JSON)
 	public Status status(@HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
 	  if(!this.isAuth(auth)) {
       throw new Exception("Unauthorized!");
     }
 		Status returnObj = new Status();
 		String rev = null;
 		String branch = null;
 		String repo = null;
     String configRev = null;
     String configBranch = null;
     String configRepo = null;
 		
 		Properties configProperties = configManager.getDefaultProperties();
 		
 		// Get content directory
 		String contentDir = configProperties.getProperty("com.meltmedia.cadmium.lastUpdated", "");
     // Get config directory
     String configDir = configProperties.getProperty("com.meltmedia.cadmium.config.lastUpdated", "");
 	
 	
 	  GitService git = null;
     try {
       git = gitService.getGitServiceNoBlock();
       rev = git.getCurrentRevision();
       branch = git.getBranchName();
       repo = git.getRemoteRepository();
     } catch(IllegalStateException e) {
       logger.debug("Content git service is not yet set: " + e.getMessage());
     } finally {
      try {
        gitService.releaseGitService();
      } catch(IllegalMonitorStateException e) {
        logger.debug("Released unattained read lock.");
      }
     }
 			
 		// Get cadmium project info (branch, repo and revision)
 		rev = configProperties.getProperty("git.ref.sha", rev);
 		branch = configProperties.getProperty("branch", 
 		    branch == null ? configProperties.getProperty("com.meltmedia.cadmium.branch") : branch);
 		repo = configProperties.getProperty("repo", 
 		    repo == null ? configProperties.getProperty("com.meltmedia.cadmium.git.uri") : repo);
 		
 		git = null;
     try {
       git = configGitService.getGitServiceNoBlock();
       configRev = git.getCurrentRevision();
       configBranch = git.getBranchName();
       configRepo = git.getRemoteRepository();
     } catch(IllegalStateException e) {
       logger.debug("Config git service is not yet set: " + e.getMessage());
     } finally {
      try {
        configGitService.releaseGitService();
      } catch(IllegalMonitorStateException e) {
        logger.debug("Released unattained read lock.");
      }
     }
       
     // Get cadmium project info (branch, repo and revision)
     configRev = configProperties.getProperty("config.git.ref.sha", configRev);
     configBranch = configProperties.getProperty("config.branch", 
         configBranch == null ? configProperties.getProperty("com.meltmedia.cadmium.config.branch") : configBranch);
     configRepo = configProperties.getProperty("config.repo", 
         configRepo == null ? configProperties.getProperty("com.meltmedia.cadmium.config.git.uri", repo) : configRepo);
 	
 		// Get source project info (branch, repo and revision)
 		String sourceFile = contentDir + File.separator + "META-INF" + File.separator + "source";
 		String source = "{}";
 		if(FileSystemManager.canRead(sourceFile)) {
 		  source = FileSystemManager.getFileContents(sourceFile);
 		} else {
 		  logger.debug("No source file [{}]", sourceFile);
 		}
     logger.debug("Source [{}] is from [{}]", source, sourceFile);
 		
 		
 		
 		// Get cluster members' status
 		List<ChannelMember> members = lifecycleService.getPeirStates();
 		
 		if(members != null) {
 			
 			List<StatusMember> peers = new ArrayList<StatusMember>();
 			
 			for(ChannelMember member : members) {
 				
 			  StatusMember peer = new StatusMember();
 				peer.setAddress(member.getAddress().toString());
 				peer.setCoordinator(member.isCoordinator());
 				peer.setState(member.getState());
 				peer.setConfigState(member.getConfigState());
 				peer.setMine(member.isMine());
 				peers.add(peer);			
 				
 			}
 			
 			returnObj.setMembers(peers);
 		}
 		
 		// Get environment status 
 		String environFromConfig = System.getProperty("com.meltmedia.cadmium.environment");
 		
 		if(environFromConfig != null && environFromConfig.trim().length() > 0) {			
 			
 			returnObj.setEnvironment(environFromConfig);
 			
 		}
 		else {
 			
 			returnObj.setEnvironment(ENVIRON_DEV);	
 		}
 		
 		// Get Maintanence page status (on or off)
 		String maintStatus;
 		if(maintService.isOn()) {
 			
 			maintStatus = MAINT_ON;
 		}
 		else {
 			
 			maintStatus = MAINT_OFF;
 		}
 		
 		returnObj.setGroupName(sender.getGroupName());
 		returnObj.setContentDir(contentDir);
 		returnObj.setConfigDir(configDir);
 		returnObj.setBranch(branch);
 		returnObj.setRevision(rev);		
 		returnObj.setRepo(repo);
     returnObj.setConfigBranch(configBranch);
     returnObj.setConfigRevision(configRev);   
     returnObj.setConfigRepo(configRepo);
 		returnObj.setMaintPageState(maintStatus);
 		returnObj.setSource(source);
 		
 		return returnObj;
 	}
 
 }

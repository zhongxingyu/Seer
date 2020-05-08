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
 import java.util.List;
 import java.util.Properties;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
 import com.meltmedia.cadmium.core.FileSystemManager;
 import com.meltmedia.cadmium.core.SiteDownService;
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
 	protected DelayedGitServiceInitializer gitService;
 
 	@Inject
 	@Named("config.properties")
 	protected Properties configProperties;
 	
 	@Inject
 	@Named("com.meltmedia.cadmium.git.uri")
 	protected String repoUri;
 	
 	@Inject
 	@Named("contentDir")
 	protected String initialContentDir;
 	
 	private GitService git;
 
 	@GET
 	@Path("/Ping")
 	@Produces("text/plain")
 	public String ping() {
 	  return "Ok";
 	}
 	
 	@GET	
 	@Produces(MediaType.APPLICATION_JSON)
 	public Status status(@HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
 	  if(!this.isAuth(auth)) {
       throw new Exception("Unauthorized!");
     }
 		Status returnObj = new Status();
 		
 		// Get content directory
 		String contentDir = this.initialContentDir;
 		if(configProperties.containsKey("com.meltmedia.cadmium.lastUpdated")) {
 			
 			contentDir = configProperties.getProperty("com.meltmedia.cadmium.lastUpdated");			
 		}
 		
 		GitService git = null;
 		if(this.git == null && !configProperties.containsKey("git.ref.sha") && !configProperties.containsKey("branch")) {
   		try {
   		  git = gitService.getGitService();
   		} catch(Exception e){
         logger.info("Interrupted while waiting on git service to initialize.", e);
       }
 		}
 		
 		// Get cadmium project info (branch, repo and revision)
 		String rev = configProperties.getProperty("git.ref.sha", (git != null ? git.getCurrentRevision() : null));
 		String branch = configProperties.getProperty("branch", (git != null ? git.getBranchName() : null));
 		String repo = repoUri;
 		
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
 		returnObj.setBranch(branch);
 		returnObj.setRevision(rev);		
 		returnObj.setRepo(repo);
 		returnObj.setMaintPageState(maintStatus);
 		returnObj.setSource(source);
 		
 		return returnObj;
 	}
 
 }

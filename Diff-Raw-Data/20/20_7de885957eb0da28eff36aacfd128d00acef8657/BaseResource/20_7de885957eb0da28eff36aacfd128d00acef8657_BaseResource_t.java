 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INSFO-RI-261552.
 
  Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 package eu.stratuslab.storage.disk.resources;
 
 import static org.restlet.data.MediaType.TEXT_HTML;
 import static org.restlet.data.MediaType.APPLICATION_JSON;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.restlet.data.ChallengeResponse;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.ext.freemarker.TemplateRepresentation;
 import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.ServerResource;
 import eu.stratuslab.storage.disk.main.PersistentDiskApplication;
 import eu.stratuslab.storage.disk.utils.DiskProperties;
 import eu.stratuslab.storage.disk.utils.Messages;
 import freemarker.template.Configuration;
 
 public class BaseResource extends ServerResource {
 
 	protected static final DiskProperties zk = new DiskProperties();
 	protected static final Logger LOGGER = Logger.getLogger("org.restlet");
 	protected static final Messages MESSAGES = new Messages();
 	private String username = "";	
 
 	public enum DiskVisibility {
 		PRIVATE,
 		// RESTRICTED,
 		PUBLIC,
 	}
	
	@Get
	public Representation getNotAllowed() {
		return respondError(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "Method not allowed");
	}

	@Post
	public Representation postNotAllowed() {
		return respondError(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "Method not allowed");
	}
 
	@Delete
	public Representation deleteNotAllowed() {
		return respondError(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "Method not allowed");
	}
	
 	private Configuration getFreeMarkerConfiguration() {
 		return ((PersistentDiskApplication) getApplication())
 				.getFreeMarkerConfiguration();
 	}
 
 	protected TemplateRepresentation createTemplateRepresentation(String tpl,
 			Map<String, Object> info, MediaType mediaType) {
 
 		Configuration freeMarkerConfig = getFreeMarkerConfiguration();
 
 		return new TemplateRepresentation(tpl, freeMarkerConfig, info,
 				mediaType);
 	}
 	
 	protected TemplateRepresentation directTemplateRepresentation(String tpl, Map<String, Object> info) {
 		return createTemplateRepresentation(getTemplateType() + "/" + tpl, info, getReturnedMediaType());
 	}
 
 	protected Map<String, Object> createInfoStructure(String title) {
 		Map<String, Object> info = new HashMap<String, Object>();
 
 		// Add the standard base URL declaration.
 		info.put("baseurl", getBaseUrl());
 
 		// Add the title if appropriate.
 		if (title != null && !"".equals(title)) {
 			info.put("title", title);
 		}
 
 		// Add user name information
 		info.put("username", getUsername());
 		
 		// Display message if available
 		info.put("success", MESSAGES.pop());
 
 		return info;
 	}
 
 	public String getUsername() {
 		if (!username.isEmpty()) {
 			return username;
 		}
 
 		ChallengeResponse cr = getRequest().getChallengeResponse();
 
 		if (cr == null) {
 			username = "UNKNOWN";
 		} else {
 			username = cr.getIdentifier();
 		}
 
 		return username;
 	}
 
 	protected String getBaseUrl() {
 		return getRequest().getRootRef().toString();
 	}
 
 	protected String getCurrentUrl() {
 		return getCurrentUrlWithQueryString().replaceAll("\\?.*", "");
 	}
 
 	protected String getCurrentUrlWithQueryString() {
 		return getRequest().getResourceRef().toString();
 	}
 
 	protected String getQueryString() {
 		return getRequest().getResourceRef().getQuery();
 	}
 
 	protected Boolean hasQueryString(String key) {
 		String queryString = getQueryString();
 		return (queryString != null && queryString.equals(key));
 	}
 
 	protected static String getDiskZkPath(String uuid) {
 		return PersistentDiskApplication.ZK_DISKS_PATH + "/" + uuid;
 	}
 	
 	protected Boolean diskExists(String uuid) {
 		return zk.pathExists(getDiskZkPath(uuid));
 	}
 
 	protected Boolean hasSuficientRightsToView(Properties properties) {
 		// Is disk owner
 		if (properties.get(DiskProperties.DISK_OWNER_KEY).toString()
 				.equals(getUsername())) {
 			return true;
 		}
 
 		DiskVisibility currentVisibility = diskVisibilityFromString(properties
 				.get(DiskProperties.DISK_VISIBILITY_KEY).toString());
 
 		// Is disk public
 		if (currentVisibility.equals(DiskVisibility.PUBLIC)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	protected Boolean hasSuficientRightsToDelete(Properties properties) {
 		// Need to be the owner to delete the disk
 		return properties.get(DiskProperties.DISK_OWNER_KEY).toString()
 				.equals(getUsername());
 	}
 
 	protected String diskVisibilityToString(DiskVisibility visibility) {
 		switch (visibility) {
 		case PUBLIC:
 			return "public";
 			// case RESTRICTED:
 			// return "restricted";
 		case PRIVATE:
 			return "private";
 		default:
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
 					"Invalide disk visibility");
 		}
 	}
 
 	protected DiskVisibility diskVisibilityFromString(String visibility) {
 		if ("public".equals(visibility)) {
 			return DiskVisibility.PUBLIC;
 		} else if ("private".equals(visibility)) {
 			return DiskVisibility.PRIVATE;
 			// } else if ("restricted".equals(visibility)) {
 			// return DiskVisibility.RESTRICTED;
 		} else {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
 					"Invalide disk visibility: " + visibility);
 		}
 	}
 
 	protected String getDateTime() {
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		Date date = new Date();
 
 		return dateFormat.format(date);
 	}
 
 	protected Boolean useAPI() {
 		return getRequest().getResourceRef().getPath().startsWith("/api/");
 	}
 	
 	protected String getTemplateType() {
 		if (useAPI()) {
 			return "json";
 		} else {
 			return "html";
 		}
 	}
 	
 	protected MediaType getReturnedMediaType() {
 		if (useAPI()) {
 			return APPLICATION_JSON;
 		} else {
 			return TEXT_HTML;
 		}
 	}
 	
 	protected Representation respondError(Status errorCode, String errorMsg) {
 		setStatus(errorCode);
 		Map<String, Object> error = createInfoStructure("An error occured");
 		
 		if (useAPI()) {
 			errorMsg = "\"" + errorMsg + "\"";
 		}
 		
 		error.put("errorMsg", errorMsg);
 		
 		return directTemplateRepresentation("error.ftl", error);
 	}
 	
 	protected static String join(List<String> list, String conjunction)
 	{
 	   StringBuilder sb = new StringBuilder();
 	   boolean first = true;
 	   for (String item : list)
 	   {
 	      if (first)
 	         first = false;
 	      else
 	         sb.append(conjunction);
 	      sb.append(item);
 	   }
 	   return sb.toString();
 	}
 
 	public static DiskProperties getZooKeeper() {
 		return zk;
 	}
 
 }

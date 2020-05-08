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
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.UUID;
 
 import org.restlet.data.Form;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import eu.stratuslab.storage.disk.main.PersistentDiskApplication;
 import eu.stratuslab.storage.disk.utils.DiskProperties;
 import eu.stratuslab.storage.disk.utils.DiskUtils;
 
 public class CreateResource extends BaseResource {
     @Get
     public Representation displayCreationForm() {
     	if (useAPI()) {
     		return respondError(Status.CLIENT_ERROR_BAD_REQUEST, "Method not allowed");
     	}
     	
     	return createResourceForm(getEmptyFormProperties(), null);
     }
     
     private Representation createResourceForm(Properties fieldsValues, List<String> errors) {
     	Map<String, Object> infos = createInfoStructure("Create a disk");
     	
     	infos.put("visibilities", getDiskVisibilities());
     	infos.put("errors", errors);
     	infos.put("values", fieldsValues);
     	
 		return directTemplateRepresentation("create.ftl", infos);
     }
     
     private Properties getEmptyFormProperties() {
     	Properties properties = new Properties();
     	
     	properties.put("size", "");
     	properties.put("tag", "");
     	properties.put("visibility", "private");
     	
     	return properties;
     }
     
     private ArrayList<String> getDiskVisibilities() {
     	ArrayList<String> visibilities = new ArrayList<String>();
     	for (DiskVisibility v : DiskVisibility.values()) {
     		visibilities.add(diskVisibilityToString(v));
     	}
     	
     	return visibilities;
     }
     
 	@Post
 	public Representation createDiskRequest(Representation entity) {
 		Representation response = null;
 		
 		PersistentDiskApplication.checkEntity(entity);
 		PersistentDiskApplication.checkMediaType(entity.getMediaType());
 
 		Properties diskProperties = processWebForm();
 		List<String> errors = validateDiskProperties(diskProperties);
 		String uuid = diskProperties.getProperty(DiskProperties.UUID_KEY);
 		
 		// Display form again if we have error(s)
 		if (errors.size() > 0) {
 			if (useAPI()) {
 				return respondError(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, join(errors, "\", \""));
 			}
 			
 			return createResourceForm(diskProperties, errors);
 		}
 		
 		createDisk(diskProperties);
 
 		if (!useAPI()) {
 			MESSAGES.push("Your disk have been created successfully.");
 			redirectSeeOther(getBaseUrl() + "/disks/" + uuid + "/");
 		} else {
 			setStatus(Status.SUCCESS_CREATED);
 			Map<String, Object> info = new HashMap<String, Object>();
 			info.put("key", "uuid");
 			info.put("value", uuid);
 			response = directTemplateRepresentation("keyvalue.ftl", info);
 		}
 		
 		return response;
 	}
 
 	private Properties processWebForm() {
 		Properties properties = initializeProperties();
 		Representation entity = getRequest().getEntity();
 		Form form = new Form(entity);
 
 		for (String name : form.getNames()) {
 			String value = form.getFirstValue(name);
 			if (value != null) {
 				properties.put(name, value);
 			}
 		}
 
 		return properties;
 	}
 
 	private Properties initializeProperties() {
 		Properties properties = getEmptyFormProperties();
 		properties.put(DiskProperties.UUID_KEY, generateUUID());
 		properties.put(DiskProperties.DISK_OWNER_KEY, getUsername());
 		properties.put(DiskProperties.DISK_CREATION_DATE_KEY, getDateTime());
 		properties.put(DiskProperties.DISK_USERS_KEY, "0");
 		
 		return properties;
 	}
 
 	private static String generateUUID() {
 		return UUID.randomUUID().toString();
 	}
 
 	private List<String> validateDiskProperties(Properties diskProperties) {
 		List<String> errors = new LinkedList<String>();
 		
 		try {
 			diskVisibilityFromString(diskProperties.getProperty("visibility", "None"));
 		} catch (RuntimeException e) {
 			errors.add("Invalid disk visibility");
 		}
 
 		try {
 			String size = diskProperties.getProperty("size", "None");
 			int gigabytes = Integer.parseInt(size);
 
 			if (gigabytes < PersistentDiskApplication.DISK_SIZE_MIN
 					|| gigabytes > PersistentDiskApplication.DISK_SIZE_MAX) {
 				errors.add("Size must be an integer between " +
 							PersistentDiskApplication.DISK_SIZE_MIN +
 							" and " + PersistentDiskApplication.DISK_SIZE_MAX);
 			}
 		} catch (NumberFormatException e) {
 			errors.add("Size must be a valid positive integer");
 		}
 
 		return errors;
 	}
 	
 	private void createDisk(Properties properties) {		
 		String diskRoot = getDiskZkPath(properties.get(DiskProperties.UUID_KEY).toString());
 		
 		zk.saveDiskProperties(diskRoot, properties);
		DiskUtils.createDisk(properties);
 	}
 }

 /**
  *    Copyright 2012-2013 Trento RISE
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
 
 package eu.trentorise.smartcampus.filestorage.rest;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 import eu.trentorise.smartcampus.ac.provider.model.User;
 import eu.trentorise.smartcampus.filestorage.managers.MediaManager;
 import eu.trentorise.smartcampus.filestorage.managers.PermissionManager;
 import eu.trentorise.smartcampus.filestorage.model.AlreadyStoredException;
 import eu.trentorise.smartcampus.filestorage.model.NotFoundException;
 import eu.trentorise.smartcampus.filestorage.model.Operation;
 import eu.trentorise.smartcampus.filestorage.model.Resource;
 import eu.trentorise.smartcampus.filestorage.model.SmartcampusException;
 import eu.trentorise.smartcampus.filestorage.model.Token;
 import eu.trentorise.smartcampus.filestorage.services.ACLService;
 
 @Controller
 public class MediaController extends RestController {
 
 	@Autowired
 	MediaManager mediaManager;
 
 	@Autowired
 	PermissionManager permissionManager;
 
 	@Autowired
 	ACLService scAcl;
 
 	@RequestMapping(method = RequestMethod.POST, value = "/eu.trentorise.smartcampus.filestorage.Resource/{accountId}")
 	public @ResponseBody
 	String storeResource(HttpServletRequest request,
 			@PathVariable("accountId") String accountId,
			@RequestParam("file") MultipartFile resource)
			throws AlreadyStoredException, SmartcampusException,
			NotFoundException {
 		User user = retrieveUser(request);
 
 		if (!permissionManager.checkAccountPermission(user, accountId)) {
 			throw new SecurityException();
 		}
		try {
			return mediaManager.storage(accountId, user, getResource(resource))
					.getId();
		} catch (IOException e) {
			throw new SmartcampusException(e);
		}
 	}
 
 	@RequestMapping(method = RequestMethod.POST, value = "/eu.trentorise.smartcampus.filestorage.Resource/{accountId}/{rid}")
 	public @ResponseBody
 	void replaceResource(HttpServletRequest request,
 			@PathVariable("rid") String rid,
 			@PathVariable("accountId") String accountId,
 			@RequestParam("file") MultipartFile resource)
			throws SmartcampusException, NotFoundException {
 		User user = retrieveUser(request);
 
 		if (!permissionManager.checkResourcePermission(user, rid)) {
 			throw new SecurityException();
 		}
 
		try {
			mediaManager.replace(accountId, user, getResource(rid, resource));
		} catch (IOException e) {
			throw new SmartcampusException(e);
		}
 	}
 
 	@RequestMapping(method = RequestMethod.DELETE, value = "/eu.trentorise.smartcampus.filestorage.Resource/{accountId}/{rid}")
 	public @ResponseBody
 	void removeResource(HttpServletRequest request,
 			@PathVariable("accountId") String accountId,
 			@PathVariable("rid") String rid) throws SmartcampusException,
 			NotFoundException {
 		User user = retrieveUser(request);
 
 		if (!permissionManager.checkResourcePermission(user, rid)) {
 			throw new SecurityException();
 		}
 		mediaManager.remove(accountId, user, rid);
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.filestorage.Resource/{rid}")
 	public @ResponseBody
 	Token getResource(HttpServletRequest request, @PathVariable String rid)
 			throws SmartcampusException, SecurityException {
 		User user = retrieveUser(request);
 
 		return scAcl.getSessionToken(Operation.DOWNLOAD, user, rid);
 	}
 
 	private Resource getResource(String rid, MultipartFile file)
 			throws IOException {
 		Resource res = getResource(file);
 		res.setId(rid);
 		return res;
 	}
 
 	private Resource getResource(MultipartFile file) throws IOException {
 		Resource res = new Resource();
 		res.setContent(file.getBytes());
 		res.setContentType(file.getContentType());
 		res.setName(file.getOriginalFilename());
 		return res;
 	}
 }

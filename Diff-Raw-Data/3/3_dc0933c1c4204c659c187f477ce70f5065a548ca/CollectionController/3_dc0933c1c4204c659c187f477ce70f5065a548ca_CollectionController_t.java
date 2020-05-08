 package org.netvogue.server.webmvc.controllers;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.netvogue.server.aws.core.ImageType;
 import org.netvogue.server.aws.core.UploadManager;
 import org.netvogue.server.neo4japi.common.ProductLines;
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.netvogue.server.neo4japi.domain.Category;
 import org.netvogue.server.neo4japi.domain.CollectionPhoto;
 import org.netvogue.server.neo4japi.domain.User;
 import org.netvogue.server.neo4japi.service.BoutiqueService;
 import org.netvogue.server.neo4japi.service.CollectionData;
 import org.netvogue.server.neo4japi.service.CollectionService;
 import org.netvogue.server.neo4japi.service.UserService;
 import org.netvogue.server.webmvc.domain.Collection;
 import org.netvogue.server.webmvc.domain.CollectionJSONRequest;
 import org.netvogue.server.webmvc.domain.Collections;
 import org.netvogue.server.webmvc.domain.ImageURLsResponse;
 import org.netvogue.server.webmvc.domain.JsonRequest;
 import org.netvogue.server.webmvc.domain.JsonResponse;
 import org.netvogue.server.webmvc.domain.PhotoInfoJsonRequest;
 import org.netvogue.server.webmvc.domain.PhotoWeb;
 import org.netvogue.server.webmvc.domain.Photos;
 import org.netvogue.server.webmvc.domain.UploadedFileResponse;
 import org.netvogue.server.webmvc.security.NetvogueUserDetailsService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.convert.ConversionService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 @Controller
 public class CollectionController {
 	@Autowired NetvogueUserDetailsService 	userDetailsService;
 	@Autowired BoutiqueService  boutiqueService;
 	@Autowired UserService 					userService;
 	@Autowired CollectionService			collectionService;
 	@Autowired ConversionService			conversionService;
 
 	@Autowired
 	private UploadManager uploadManager;
 
 	@RequestMapping(value={"/getcollections", "/getcollections/{profileid}"}, method=RequestMethod.GET)
 	public @ResponseBody Collections GetCollections(@ModelAttribute("profileid") String profileid, 
 												@RequestParam("galleryname") String galleryname,
 												@RequestParam("category") String categoryname,
 												@RequestParam("brandname") String brandname) {
 		System.out.println("Get Collections: " + galleryname);
 		Collections collections = new Collections();
 		
 		User user;
 		if(!profileid.isEmpty()) {
 			user = userService.getUserByUsername(profileid);
 			if(user == null || USER_TYPE.BOUTIQUE == user.getUserType()) {
 				return collections;
 			}
 		} else {
 			 user = userDetailsService.getUserFromSession();
 		}
 		
 		collections.setName(user.getName());
 		collections.setProfilepic(conversionService.convert(user.getProfilePicLink(), ImageURLsResponse.class));
 		Set<Collection> collectionTemp = new LinkedHashSet<Collection>();
 		Iterable<CollectionData> dbCollections;
 		if(galleryname.isEmpty()) {
 			dbCollections = userService.getCollections(user);
 		} else {
 			dbCollections = userService.searchCollections(user.getUsername(), galleryname, categoryname, brandname);
 		}
 		if(null == dbCollections) {
 			return collections;
 		}
 		Iterator<CollectionData> first = dbCollections.iterator();
 		while ( first.hasNext() ){
 			CollectionData dbCollection = first.next();
 			
 			Collection collection = conversionService.convert(dbCollection.getCollection(), Collection.class);
 			collection.setBrandname(dbCollection.getName());
 			collectionTemp.add(collection);
 			
 		}
 		collections.setCollections(collectionTemp);
 		
 		return collections;
 	}
 	
 	@RequestMapping(value={"/collection/getphotos", "/collection/getphotos/{profileid}"}, method=RequestMethod.GET)
 	public @ResponseBody Photos GetPhotos(@ModelAttribute("profileid") String profileid, 
 										  @RequestParam("galleryid") String galleryid,
 										  @RequestParam("photoname") String photoname
 											 ) {
 		System.out.println("Get collection Photos: " + photoname);
 		Photos photos = new Photos();
 		if(galleryid.isEmpty()) {
 			return photos;
 		}
 		
 		User user;
 		if(!profileid.isEmpty()) {
 			user = userService.getUserByUsername(profileid);
 			if(user == null || USER_TYPE.BOUTIQUE == user.getUserType()) {
 				return photos;
 			}
 		} else {
 			 user = userDetailsService.getUserFromSession();
 		}
 		
 		photos.setName(user.getName());
 		photos.setProfilepic(conversionService.convert(user.getProfilePicLink(), ImageURLsResponse.class));
 		photos.setGalleryname(collectionService.getCollection(galleryid).getCollectionseasonname());
 		Set<PhotoWeb> photosTemp = new LinkedHashSet<PhotoWeb>();
 		Iterable<CollectionPhoto> dbPhotos;
 		if(photoname.isEmpty()) {
 			dbPhotos = collectionService.getPhotos(galleryid);
 		} else {
 			dbPhotos = collectionService.searchPhotoByName(galleryid, photoname);
 		}
 		if(null == dbPhotos) {
 			return photos;
 		}
 		Iterator<CollectionPhoto> first = dbPhotos.iterator();
 		while ( first.hasNext() ){
 			CollectionPhoto dbPhoto = first.next() ;
 			photosTemp.add(conversionService.convert(dbPhoto, PhotoWeb.class));
 		}
 		photos.setPhotos(photosTemp);
 		
 		return photos;
 	}
 	
 	@RequestMapping(value="collection/create", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse CreateCollection(@RequestBody CollectionJSONRequest request) {
 		System.out.println("Create Collection");
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		
 		User loggedinUser = userDetailsService.getUserFromSession();
 		if(loggedinUser.getUserType() != USER_TYPE.BRAND) {
 			response.setError("Only brands can create collections");
 			return response;
 		}
 		org.netvogue.server.neo4japi.domain.Collection newCollection = 
 				new org.netvogue.server.neo4japi.domain.Collection(request.getSeasonname(), request.getDesc(), loggedinUser);
 		
 		if(null != request.getCategory()) {
 			ProductLines productLine = ProductLines.getValueOf(request.getCategory());
 			Category cat = boutiqueService.getOrCreateCategory(productLine);
 			newCollection.setProductcategory(cat);
 		}
 		if(ResultStatus.SUCCESS == collectionService.SaveCollection(newCollection, error)) {  
 			response.setStatus(true);
 			response.setIdcreated(newCollection.getCollectionid());
 		}
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="collection/edit", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditCollection(@RequestBody CollectionJSONRequest request) {
 		System.out.println("Edit Collection");
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		
 		if(null == request.getId() || request.getId().isEmpty()) {
 			response.setError("editorial Id is empty");
 			return response;
		} else if(null == request.getSeasonname() || request.getSeasonname().isEmpty() || 
				  null == request.getDesc()		  || request.getDesc().isEmpty()) {
 			response.setError("new name or description is empty");
 			return response;
 		}
 		
 		org.netvogue.server.neo4japi.domain.Collection collection = collectionService.getCollection(request.getId());
 		if(null == collection) {
 			response.setError("There is no collection with this Id");
 			return response;
 		}
 		if(collection.getProductcategory().getProductLine().getDesc() == request.getCategory()) {
 			if(ResultStatus.SUCCESS == collectionService.editCollection(request.getId(), 
 								request.getSeasonname(), request.getDesc(), error))   
 				response.setStatus(true);
 			else
 				response.setError(error.toString());
 		} else { //If we can write a cypher query for the below operation, then we can replace
 			//this else part
 			collection.setCollectionseasonname(request.getSeasonname());
 			collection.setDescription(request.getDesc());
 			ProductLines productLine = ProductLines.getValueOf(request.getCategory());
 			Category cat = boutiqueService.getOrCreateCategory(productLine);
 			collection.setProductcategory(cat);
 			if(ResultStatus.SUCCESS == collectionService.SaveCollection(collection, error)) {  
 				response.setStatus(true);
 				response.setIdcreated(collection.getCollectionid());
 			}
 			else
 				response.setError(error.toString());
 		}
 		
 		return response;
 	}
 	
 	@RequestMapping(value="collection/setprofilepic", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse setProfilepic(@RequestBody JsonRequest profilepic) {
 		System.out.println("Set Profile pic for collection:" + profilepic.getId());
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		if(profilepic.getId().isEmpty() || profilepic.getValue().isEmpty()) {
 			response.setError("collectionid or profile pic is empty");
 			return response;
 		}
 		if(ResultStatus.SUCCESS == collectionService.setProfilepic(profilepic.getId(), profilepic.getValue(), error)) 
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	//Think about the categories as well
 	@RequestMapping(value="collection/delete", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse DeleteCollection(@RequestBody String galleryid) {
 		System.out.println("Delete Collection:"+ galleryid);
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		
 		if(null == galleryid || galleryid.isEmpty()) {
 			response.setError("Galleryid is empty");
 			return response;
 		}
 		if(ResultStatus.SUCCESS == collectionService.deleteCollection(galleryid, error)) {  
 			response.setStatus(true);
 		}
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="collection/addphotos", method=RequestMethod.POST)
 	public @ResponseBody UploadedFileResponse AddPhotostoGallery(Model model, 
 			@RequestParam("files[]") List<MultipartFile> fileuploads, @RequestParam("galleryid") String galleryId) {
 		System.out.println("Add photos: Gallery Id:" + galleryId + "No:of Photos:" + fileuploads.size());
 		UploadedFileResponse response = new UploadedFileResponse();
 		
 		if(galleryId.isEmpty()) {
 			response.setError("Gallery Id is empty");
 			return response;
 		}
 		org.netvogue.server.neo4japi.domain.Collection collection = collectionService.getCollection(galleryId);
 		if(null == collection) {
 			response.setError("No collection is present with this id");
 			return response;
 		}
 		
 		List<PhotoWeb> JSONFileData= new ArrayList<PhotoWeb>();
 		
 		for ( MultipartFile fileupload : fileuploads ) {
 			System.out.println("Came here" + fileupload.getOriginalFilename());
 			Map<String, Object> uploadMap  = uploadManager.processUpload(fileupload, ImageType.COLLECTION);
 			CollectionPhoto newPhoto = new CollectionPhoto((String)uploadMap.get(UploadManager.FILE_ID));
 			collection.addPhotos(newPhoto);
 			
 			JSONFileData.add(conversionService.convert(newPhoto, PhotoWeb.class));
 		}
 		StringBuffer error = new StringBuffer();
 		if(ResultStatus.SUCCESS == collectionService.SaveCollection(collection, error)) {  
 			response.setStatus(true);
 			response.setFilesuploaded(JSONFileData);
 		}
 		else
 			response.setError(error.toString());
 		return response;
 	}
 	
 	@RequestMapping(value="collection/editphotoinfo", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditPhotoInfo(@RequestBody PhotoInfoJsonRequest photoInfo) {
 		System.out.println("Edit Photo Info:" + photoInfo.toString());
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		String photoId = photoInfo.getPhotoid();
 		if(null == photoId || photoId.isEmpty())
 			response.setError("photoid is empty");
 		if(ResultStatus.SUCCESS == collectionService.editPhotoInfo(photoInfo.getPhotoid(), photoInfo.getPhotoname(), 
 													photoInfo.getSeasonname(), error))
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="collection/editphotoname", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditPhotoName(@RequestBody JsonRequest request) {
 		System.out.println("Edit Photo Name");
 		StringBuffer error = new StringBuffer();
 		
 		JsonResponse response = new JsonResponse();
 		
 		if(ResultStatus.SUCCESS == collectionService.editPhotoName(request.getId(), request.getValue(), error))   
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="collection/editphotoseasonname", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditPhotoSeasonName(@RequestParam("photoname") String photoname,
 														  @RequestParam("seasonname") String seasonname, 
 														  @RequestParam("photoid") String photoid) {
 		System.out.println("Edit Photo Name");
 		StringBuffer error = new StringBuffer();
 		
 		JsonResponse response = new JsonResponse();
 		
 		if(ResultStatus.SUCCESS == collectionService.editPhotoInfo(photoid, photoname, seasonname, error))   
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 
 	@RequestMapping(value="collection/deletephoto", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse DeletePhoto(@RequestBody String photoid) {
 		System.out.println("Delete Photo:" + photoid);
 		StringBuffer error = new StringBuffer();
 		
 		JsonResponse response = new JsonResponse();
 		if(!photoid.isEmpty()) {
 			if(ResultStatus.SUCCESS == collectionService.deletePhoto(photoid, error)) {  
 				response.setStatus(true);
 			}
 			else
 				response.setError(error.toString());
 		} else {
 			response.setError("photoid is empty");
 		}
 		
 		return response;
 	}
 }

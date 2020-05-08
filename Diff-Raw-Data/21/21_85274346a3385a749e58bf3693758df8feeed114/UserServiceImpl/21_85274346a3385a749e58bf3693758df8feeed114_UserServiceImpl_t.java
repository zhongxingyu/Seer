 package org.netvogue.server.neo4japi.service;
 
 import java.util.Set;
 
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.netvogue.server.neo4japi.domain.Gallery;
 import org.netvogue.server.neo4japi.domain.Photo;
 import org.netvogue.server.neo4japi.domain.User;
 import org.netvogue.server.neo4japi.repository.GalleryRepository;
 import org.netvogue.server.neo4japi.repository.UserRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.neo4j.support.Neo4jTemplate;
 
 public class UserServiceImpl implements UserService{
 	@Autowired Neo4jTemplate		neo4jTemplate;
 	@Autowired UserRepository		userRepo;
 	@Autowired GalleryRepository	galleryRepo;
 	
 	public ResultStatus SaveUser(User user, String error){
 		try {
 			//New Categories node will be created an relationship will also be added for this.
 			//Saving it through Template instead of boutiquerepo so that categories node can also be saved
 			neo4jTemplate.save(user);
 			System.out.println("Updated User Successfully");
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error for" + user.getEmail() + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 	
 	public ResultStatus ValidateEmailAndId(String email, Long id) {
 		if(email.isEmpty()) {
 			return ResultStatus.FAILURE;
 		}
 		User b = userRepo.findByemailAndId(email, id);
 		if(null == b) {
 			return ResultStatus.SUCCESS;
 		}
 		return ResultStatus.USER_EXISTS;
 	}
 	
 	public ResultStatus SaveGallery(Gallery newGallery, String error) {
 		try {
 			//New Categories node will be created an relationship will also be added for this.
 			//Saving it through Template instead of boutiquerepo so that categories node can also be saved
 			neo4jTemplate.save(newGallery);
 			System.out.println("Saved Gallery Successfully with Photos:" + newGallery.getPhotosAdded().size());
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error for" + newGallery.getGalleryname() + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 	
 	public Iterable<Gallery> GetGalleries(User user) {
 		if(null != user) {
 			return userRepo.getGalleries(user.getUsername());
 		}
 		return null;
 	}
 	
 	public Gallery GetGallery(String galleryId) {
 		//galleryRepo.findByPropertyValue(arg0, galleryId);
 		return galleryRepo.getGallery(galleryId);
 	}
 	
 	public Iterable<Gallery> searchGalleryByName(User user, String name) {
 		return searchGalleryByName(user.getUsername(), name);
 	}
 	
 	public Iterable<Gallery> searchGalleryByName(String username, String name) {
 		String query = "(?i).*" + name + ".*";
 		return userRepo.searchGalleryByName(username, query);
 	}
 	
 	public ResultStatus editGalleryName(String galleryId, String name, String error) {
 		try {
 			galleryRepo.editGalleryName(galleryId, name);
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error while editing gallery" + galleryId + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 	
 	public ResultStatus deleteGallery(String galleryId, String error)  {
 		Gallery gallery = GetGallery(galleryId);
 		try {
 			Set<Photo> photos = gallery.getPhotosAdded();
 			galleryRepo.delete(GetGallery(galleryId));
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error while deleting gallery" + gallery.getGalleryname() + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 	
 	public Iterable<Photo> GetPhotos(String galleryId) {
 		if(!galleryId.isEmpty()) {
 			return galleryRepo.getPhotos(galleryId);
 		}
 		return null;
 	}
 	
 	public Iterable<Photo> searchPhotoByName(Gallery gallery, String name) {
 		return searchPhotoByName(gallery.getGalleryid(), name);
 	}
 	
 	public Iterable<Photo> searchPhotoByName(String galleryid, String name) {
 		String query = "(?i).*" + name + ".*";
 		return galleryRepo.searchPhotosByName(galleryid, query);
 	}
 	
 	public ResultStatus editPhotoInfo(String photoId, String name, String seasonname, String error) {
		if(null == photoId || photoId.isEmpty()) {
 			error = "photoid is empty";
 			return ResultStatus.FAILURE;
		} else if(null == name || null == seasonname) {
			error = "name/season name is empty";
			return ResultStatus.FAILURE;
 		}
 		try {
			galleryRepo.editPhotoInfo(photoId, name, seasonname);
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error while editing photo name" + photoId + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 	
 	public ResultStatus editPhotoName(String photoId, String name, String error) {
 		try {
 			galleryRepo.editPhotoName(photoId, name);
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error while editing photo name" + photoId + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 	
 	public ResultStatus editPhotoSeasonName(String photoId, String name, String error) {
 		try {
 			galleryRepo.editPhotoSeasonName(photoId, name);
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error while editing photo name" + photoId + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 	
 	
 	public ResultStatus deletePhoto(String photoId, String error)  {
		if(null == photoId || photoId.isEmpty()){
 			error = "Photoid is empty";
 			return ResultStatus.FAILURE;
 		}
 		try {
 			galleryRepo.deletePhoto(photoId);
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error while deleting photo" + photoId + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 }

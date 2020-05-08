 package se.bhg.photos.service;
 
 import java.io.IOException;
 
 import se.bhg.photos.exception.PhotoAlreadyExistsException;
 import se.bhg.photos.model.Photo;
 
 import com.drew.imaging.ImageProcessingException;
 
 public interface PhotoService {
 	Photo addPhoto(String originalFilename, byte[] binaryData, String uploader, String uuid) throws ImageProcessingException, IOException, PhotoAlreadyExistsException;
	Photo addPhoto(String originalFilename, byte[] binaryData, String uploader, String uuid, String directoryHint) throws ImageProcessingException, IOException, PhotoAlreadyExistsException;
 	Photo getPhoto(String id);
 	Iterable<Photo> getPhotos();
 }

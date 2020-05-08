 package org.mdissjava.mdisscore.model.dao.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 import org.mdissjava.mdisscore.model.dao.PhotoDao;
import org.mdissjava.mdisscore.model.dao.UserDao;
 import org.mdissjava.mdisscore.model.pojo.Album;
 import org.mdissjava.mdisscore.model.pojo.Photo;
import org.mdissjava.mdisscore.model.pojo.User;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.dao.BasicDAO;
 import com.google.code.morphia.mapping.Mapper;
 import com.google.code.morphia.query.Query;
 import com.google.code.morphia.query.UpdateOperations;
 
 public class PhotoDaoImpl extends BasicDAO<Photo, ObjectId> implements PhotoDao {
 	
 	public PhotoDaoImpl(Datastore ds) {
 		super(ds);
 	}
 
 	private Query<Photo> queryToFindMe(ObjectId id) {
 		return ds.createQuery(Photo.class).field(Mapper.ID_KEY).equal(id);
 	}
 
 	@Override
 	public void insertPhoto(Photo photo) {
 		this.save(photo);
 	}
 
 	@Override
 	public List<Photo> findPhoto(Photo photo) {
 		Query<Photo> query = ds.createQuery(Photo.class);
 
 		if (photo.getId() != null) {
 			query.field("id").equal(photo.getId());
 		}
 		if (photo.getPhotoId() != null) {
 			query.field("photoId").equal(photo.getPhotoId());
 		}
 		if (photo.getTitle() != null) {
 			query.field("title").equal(photo.getTitle());
 		}
 		if (photo.getAlbum() != null) {
 			//query.field("album").equal(photo.getAlbum());
 			//don't let find by album , user the album entity instead
 			throw new IllegalStateException("Use album to search photos by album");
 		}
 		if (photo.getPublicPhoto() != null) {
 			query.field("publicPhoto").equal(photo.getPublicPhoto());
 		}
 		if (!photo.getVotes().isEmpty()) {
 			query.field("votes").equal(photo.getVotes());
 		}
 		if (photo.getUploadDate() != null) {
 			query.field("uploadDate").equal(photo.getUploadDate());
 		}
 		if (photo.getNextPhoto() != null) {
 			query.field("nextPhoto").equal(photo.getNextPhoto());
 		}
 		if (photo.getBackwardPhoto() != null) {
 			query.field("backwardPhoto").equal(photo.getBackwardPhoto());
 		}
 		if (photo.getMetadata() != null) {
 			query.field("metadata").equal(photo.getMetadata());
 		}
 		if ((photo.getTags()!=null)&&(!photo.getTags().isEmpty())) {
 			query.field("tags").equal(photo.getTags());
 		}
 		if (photo.getDataId() != null) {
 			query.field("dataId").equal(photo.getDataId());
 		}
 		if (photo.getPlus18() != null) {
 			query.field("plus18").equal(photo.getPlus18());
 		}
 		if (photo.getPublicToken() != null) {
 			query.field("publicToken").equal(photo.getPublicToken());
 		}
 		if (photo.getLicense() != null) {
 			query.field("license").equal(photo.getLicense());
 		}
 		//not search with random in thsi method, we use to search random photos with a custom query
 		/*if (photo.getRandom() != 0) {
 			query.field("random").equal(photo.getRandom());
 		}*/
 		
 		List<Photo> photos = query.asList();
 
 		return photos;
 	}
 
 	@Override
 	public void updatePhoto(Photo photo) {
 		UpdateOperations<Photo> ops = ds.createUpdateOperations(Photo.class);
 		if (photo.getTitle() != null) {
 			ops.set("title", photo.getTitle());
 		}
 		if (photo.getPhotoId() != null) {
 			ops.set("photoId", photo.getPhotoId());
 		}
 		if (photo.getAlbum() != null) {
 			ops.set("album", photo.getAlbum());
 		}
 		if (photo.getPublicPhoto() != null) {
 			ops.set("publicPhoto", photo.getPublicPhoto());
 		}
 		if (photo.getVotes() != null) {
 			ops.set("votes", photo.getVotes());
 		}
 		if (photo.getUploadDate() != null) {
 			ops.set("uploadDate", photo.getUploadDate());
 		}
 		if (photo.getNextPhoto() != null) {
 			ops.set("nextPhoto", photo.getNextPhoto());
 		}
 		if (photo.getBackwardPhoto() != null) {
 			ops.set("backwardPhoto", photo.getBackwardPhoto());
 		}
 		if (photo.getMetadata() != null) {
 			ops.set("metadata", photo.getMetadata());
 		}
 		if (photo.getTags() != null) {
 			ops.set("tags", photo.getTags());
 		}
 		if (photo.getDataId() != null) {
 			ops.set("dataId", photo.getDataId());
 		}
 		if (photo.getPlus18() != null) {
 			ops.set("plus18", photo.getPlus18());
 		}
 		if (photo.getPublicToken() != null) {
 			ops.set("publicToken", photo.getPublicToken());
 		}
 		if (photo.getLicense() != null) {
 			ops.set("license", photo.getLicense());
 		}
 		//don't update random 
 		ds.update(this.queryToFindMe(photo.getId()), ops);
 
 	}
 	
 	public List<Photo> getRandomPhotos(int quantity) throws IllegalStateException{
 		
 		List<Photo> photos = new ArrayList<Photo>();
		int i = 0;
		int tries = quantity*2;
 		
 		//get randomly elements
		while ((i < quantity) && (tries > quantity)){
		//for (int i=0; i<quantity; i++){
 			//create random token
 			double randomNumber = Math.random();
 			@SuppressWarnings("rawtypes")
 			Query query = null;
 			Photo photo = null;
 			
 			//get greater than the random number
 			query = ds.createQuery(Photo.class).field("random").greaterThanOrEq(randomNumber);
 			photo = (Photo) query.get();
 			
 			//if there aren't greater than then search for lesser than
 			if (photo == null){
 				 query = ds.createQuery(Photo.class).field("random").lessThanOrEq(randomNumber);
 				 photo = (Photo) query.get();
 			}
 			
 			//check again (no photos)
 			if (photo == null){
 				throw new IllegalStateException("No photos in database!");
 			}
 			
			User user = new User();
			UserDao userDao = new UserDaoImpl();
			
			user = userDao.getUserByNick(photo.getAlbum().getUserNick());
			
			if (!(user.getConfiguration().isPrivate()))
			{
				//add the photo to the list
				photos.add(photo);
				i++;
			}
			
			tries --;
 		}
 		
 		return photos;
 	}
 	
 	@Override
 	public List<Photo> getPhotos(Album album, int quantityNumberPhotos, int skipNumberPhotos){
 		List<Photo> photos;
 		Query<Photo> query = ds.createQuery(Photo.class);
 		query.field("album").equal(album);
 		query.offset(skipNumberPhotos); //skip the first X
 		if (quantityNumberPhotos > 0) //If 0 then get all 
 			query.limit(quantityNumberPhotos); //limit the number
 		
 		photos = query.asList();
 		return photos;
 	}
 	
 	@Override
 	public List<Photo> getPhotosFromTag(String tag){
 		List<Photo> photos;
 		Query<Photo> query = ds.createQuery(Photo.class);
 		query.field("tags").contains(tag);
 		photos = query.asList();
 		return photos;
 	}
 	
 	@Override
 	public void deletePhoto(Photo photo) {
 
 		ds.delete(photo);
 
 	}
 }

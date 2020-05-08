 package com.zotyo.photos.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Order;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.data.mongodb.core.query.Update;
 import org.springframework.stereotype.Repository;
 
 import static org.springframework.data.mongodb.core.query.Criteria.*;
 
 import com.zotyo.photos.pojo.Photo;
 import com.zotyo.photos.pojo.PhotoData;
 import com.zotyo.photos.util.PhotoDataEnum;
 
 @Repository
 public class PhotoDAOImpl implements PhotoDAO {
 
 	@Autowired
 	private MongoTemplate mongoTemplate;
 	
 	@Override
 	public void save(Photo photo, PhotoData photoData) {
 		mongoTemplate.insert(photoData);
 		String data_id = photoData.getId();
 		photo.setDataId(data_id);
 		mongoTemplate.insert(photo);
 	}
 
 
 	@Override
 	public void deleteByFilename(String filename) {
 		Photo photo = mongoTemplate.findAndRemove(new Query(where("filename").is(filename)), Photo.class);
 		if (photo != null) {
 			String data_id = photo.getDataId();
 			mongoTemplate.findAndRemove(new Query(where("id").is(data_id)), PhotoData.class);
 		}
 	}
 
 	@Override
 	public void update(Photo photo) {
 		// TODO Auto-generated method stub
 		//mongoTemplate.updateFirst(new Query(where("filename").is(photo.getFilename())), Update.update("keywords", photo.getKeywords()), Photo.class);
 		
 	}
 
 	@Override
 	public List<Photo> findByCategory(String category) {
 		Query query = new Query(where("category").is(category));
 		query.sort().on("createdate", Order.DESCENDING);
 		List<Photo> photos = mongoTemplate.find(query, Photo.class);
 		return photos;
 	}
 
 	@Override
 	public List<Photo> findLatestsByCategory(String category, int count) {
 		Query query = new Query(where("category").is(category));
 		query.sort().on("createdate", Order.DESCENDING);
 		query.limit(count);
 		List<Photo> photos = mongoTemplate.find(query, Photo.class);
 		return photos;
 	}
 	
 	@Override
 	public List<PhotoData> getAllThumbsByCategory(String category) {
 		Query query = new Query(where("category").is(category));
 		query.sort().on("createdate", Order.DESCENDING);
 		List<Photo> photos = mongoTemplate.find(query, Photo.class);
 		List<String> data_ids = new ArrayList<String>();
 		for (Photo p : photos) {
 			data_ids.add(p.getDataId());
 		}
 		Query dataQuery = new Query();
 		dataQuery.fields().include("thumbdata");
 		dataQuery.addCriteria(where("id").in(data_ids));
 		List<PhotoData> thumbs = mongoTemplate.find(dataQuery, PhotoData.class);
 		return thumbs;
 	}
 	
 	@Override
 	public List<Photo> findAll() {
 		List<Photo> photos = mongoTemplate.findAll(Photo.class);
 		return photos;
 	}
 	
 	@Override
 	public Photo findByFilename(String filename) {
 		Photo photo = mongoTemplate.findOne(new Query(where("filename").is(filename)), Photo.class);
 		return photo;
 	}
 
 	@Override
 	public PhotoData getDataByFilename(String filename, PhotoDataEnum dataFlag) {
 		Photo photo = mongoTemplate.findOne(new Query(where("filename").is(filename)), Photo.class);
 		String data_id = photo.getDataId();
 		
 		Query dataQuery = new Query();
 		switch (dataFlag) {
 			case THUMB_ONLY:
 				dataQuery.fields().include("thumbdata");
 				break;
 			case PICTURE_ONLY:
 				dataQuery.fields().include("data");
 				break;
 			default: 
 				break;
 		}
 		dataQuery.addCriteria(where("id").is(data_id));
 		PhotoData photoData = mongoTemplate.findOne(dataQuery, PhotoData.class);
 		
 		return photoData;
 	}
 
 
 	@Override
 	public List<Photo> searchPhotos(String searchTerm) {
 		List<Photo> photos = mongoTemplate.find(new Query(where("keywords").is(searchTerm)), Photo.class);
 		return photos;
 	}
 
 
 	@Override
 	public List<String> searchKeywords(String term) {
 		List<String> keywords = new ArrayList<String>();
 		Query query = new Query(where("keywords").regex("^"+term));
 		query.fields().include("keywords");
 		List<Photo> photos = mongoTemplate.find(query, Photo.class);
 		for (Photo p : photos) {
 			List<String> k = p.getKeywords();
 			for (String s : k) {
 				if (s.startsWith(term)) {
 					keywords.add(s);
 				}
 			}
 		}
 		return keywords;
 	}
 
 	@Override
 	public long count() {
 		return mongoTemplate.count(new Query(), Photo.class);
 	}
 }

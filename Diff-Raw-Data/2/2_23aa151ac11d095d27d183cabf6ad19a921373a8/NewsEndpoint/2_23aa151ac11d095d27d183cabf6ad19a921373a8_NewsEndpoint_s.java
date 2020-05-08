 package com.gatech.faceme.endpoints;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Named;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 
 
 import com.gatech.faceme.entity.CharacterFaceEntity;
 import com.gatech.faceme.entity.PairTableEntity;
 import com.gatech.faceme.entity.PosterEntity;
 import com.gatech.faceme.entity.UserFaceEntity;
 import com.gatech.faceme.mediastore.PMF;
 import com.google.api.server.spi.config.Api;
 import com.google.api.server.spi.config.ApiMethod;
 import com.google.appengine.api.datastore.Key;
 
 
 @Api(name = "newsendpoint", description = "Used for getting news", version = "v1")
 public class NewsEndpoint {
 
 	@ApiMethod(httpMethod = "GET", name = "news.wholelist", path = "news/list")
 	@SuppressWarnings({ "cast", "unchecked" })
 	public List<News> listNews() {
 		PersistenceManager mgr = PMF.get().getPersistenceManager();
 		List<News> result = new ArrayList<News>();
 		try {
 			Query query1 = mgr.newQuery(UserFaceEntity.class);
 			Query query2 = mgr.newQuery(CharacterFaceEntity.class);
 			Query query3 =mgr.newQuery(PairTableEntity.class); 
 
 			for (UserFaceEntity obj : (List<UserFaceEntity>) query1.execute()) {
 				String posterkey = obj.getPosterKey();
 				PosterEntity posterEntity = mgr.getObjectById(PosterEntity.class, Long.parseLong(posterkey));
 				Key posterKey = posterEntity.getKey();
 				
 				query2 = mgr.newQuery(CharacterFaceEntity.class);
 				query2.setFilter("posterID == posterIDparam");
 				query2.declareParameters(Key.class.getName() + " posterIDparam");
 				
 				ArrayList<UserFaceEntity> userfaces = new ArrayList<UserFaceEntity>();
 				userfaces.add(obj);
 				ArrayList<CharacterFaceEntity> characters = new ArrayList<CharacterFaceEntity>();
 				for (CharacterFaceEntity object : (List<CharacterFaceEntity>) query2.execute(posterKey)) {
 					characters.add(object);
 					
 				}
 				result.add(new News(posterkey, posterEntity.getOriginalPosterKey(), posterEntity.getNonfacePosterKey(), posterEntity.getMovieName(),
 						posterEntity.getPosterName(), userfaces, characters));
 			}
 			for (PairTableEntity obj : (List<PairTableEntity>) query3.execute()) {
 				if(obj.getUserFaces().size()==0) continue;
 				UserFaceEntity userface = mgr.getObjectById(UserFaceEntity.class, 
 						Long.parseLong(obj.getUserFaces().get(0)));
 				
 				String posterkey = userface.getPosterKey();
 				PosterEntity posterEntity = mgr.getObjectById(PosterEntity.class, Long.parseLong(posterkey));
 				Key posterKey = posterEntity.getKey();
 				
 				query2 = mgr.newQuery(CharacterFaceEntity.class);
 				query2.setFilter("posterID == posterIDparam");
 				query2.declareParameters(Key.class.getName() + " posterIDparam");
 				
 				ArrayList<UserFaceEntity> userfaces = new ArrayList<UserFaceEntity>();
 				for (String uf: obj.getUserFaces()) {
					userfaces.add(mgr.getObjectById(UserFaceEntity.class, uf));
 				}
 			
 				ArrayList<CharacterFaceEntity> characters = new ArrayList<CharacterFaceEntity>();
 				for (CharacterFaceEntity object : (List<CharacterFaceEntity>) query2.execute(posterKey)) {
 					characters.add(object);
 					
 				}
 				result.add(new News(posterkey, posterEntity.getOriginalPosterKey(), posterEntity.getNonfacePosterKey(), posterEntity.getMovieName(),
 						posterEntity.getPosterName(), userfaces, characters));
 			}
 		} finally {
 			//mgr.close();
 		}
 		return result;
 	}
 	
 	@ApiMethod(httpMethod = "GET", name = "news.certainlist", path = "news/list/{startPoint}/{quantity}")
 	@SuppressWarnings({ "cast", "unchecked" })
 	public List<News> certainListNews(@Named("startPoint") int start, 
 			@Named("quantity") int number) {
 		PersistenceManager mgr = PMF.get().getPersistenceManager();
 		List<News> result = new ArrayList<News>();
 		if(start<=0||number==0) return result;
 		try {
 			int count=0;
 			Query query1 = mgr.newQuery(UserFaceEntity.class);
 			Query query2 = mgr.newQuery(CharacterFaceEntity.class);
 			Query query3 =mgr.newQuery(PairTableEntity.class); 
 			for (UserFaceEntity obj : (List<UserFaceEntity>) query1.execute()) {
 				if(count<start-1) continue;
 				String posterkey = obj.getPosterKey();
 				PosterEntity posterEntity = mgr.getObjectById(PosterEntity.class, Long.parseLong(posterkey));
 				
 				Key posterKey = posterEntity.getKey();
 				
 				query2 = mgr.newQuery(CharacterFaceEntity.class);
 				query2.setFilter("posterID == posterIDparam");
 				query2.declareParameters(Key.class.getName() + " posterIDparam");
 				
 				ArrayList<UserFaceEntity> userfaces = new ArrayList<UserFaceEntity>();
 				userfaces.add(obj);
 				ArrayList<CharacterFaceEntity> characters = new ArrayList<CharacterFaceEntity>();
 				for (CharacterFaceEntity object : (List<CharacterFaceEntity>) query2.execute(posterKey)) {
 					characters.add(object);
 					
 				}
 				
 				result.add(new News(posterkey, posterEntity.getOriginalPosterKey(),
 						posterEntity.getNonfacePosterKey(), posterEntity.getMovieName(),
 						posterEntity.getMovieName(), userfaces, characters));
 				count++;
 				if(count==(start+number-1)) break;
 			}
 		} finally {
 			mgr.close();
 		}
 		return result;
 	}
 	
 	
 	public class News{
 
 		public String posterKey;
 		public String originalPosterImageKey;
 		public String nonfacePosterImageKey;
 		public String movieName;
 		public String posterName;
 		
 		// to be done
 		public String updateDate;
 		public List<UserFaceEntity> userfaces;
 		public List<CharacterFaceEntity> characters;
 
 		
 		public News(String posterKey, String originalPosterImageKey, String nonfacePosterImageKey,
 				String movieName, String posterName, List<UserFaceEntity> userfaces
 				, List<CharacterFaceEntity> characters){
 			this.setPosterKey(posterKey);
 			this.setOriginalPosterImageKey(originalPosterImageKey);
 			this.setNonfacePosterImageKey(nonfacePosterImageKey);
 			this.setMovieName(movieName);
 			this.setPosterName(posterName);
 //			this.updateDate = updateDate;
 			this.userfaces = userfaces;
 			this.characters = characters;
 		}
 
 
 		public String getPosterKey() {
 			return posterKey;
 		}
 
 
 		public void setPosterKey(String posterKey) {
 			this.posterKey = posterKey;
 		}
 
 
 		public String getOriginalPosterImageKey() {
 			return originalPosterImageKey;
 		}
 
 
 		public void setOriginalPosterImageKey(String originalPosterImageKey) {
 			this.originalPosterImageKey = originalPosterImageKey;
 		}
 
 
 		public String getNonfacePosterImageKey() {
 			return nonfacePosterImageKey;
 		}
 
 
 		public void setNonfacePosterImageKey(String nonfacePosterImageKey) {
 			this.nonfacePosterImageKey = nonfacePosterImageKey;
 		}
 
 
 		public String getMovieName() {
 			return movieName;
 		}
 
 
 		public void setMovieName(String movieName) {
 			this.movieName = movieName;
 		}
 
 
 		public String getPosterName() {
 			return posterName;
 		}
 
 
 		public void setPosterName(String posterName) {
 			this.posterName = posterName;
 		}
 
 
 		public String getUpdateDate() {
 			return updateDate;
 		}
 
 
 		public void setUpdateDate(String updateDate) {
 			this.updateDate = updateDate;
 		}
 	}
 	
 }

 package edu.cmu.photogenome.business;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.cmu.photogenome.domain.Photo;
 import edu.cmu.photogenome.util.ConfigUtil;
 
 public class SearchDataRetrieval {
 	
 	final Logger log = LoggerFactory.getLogger(SearchDataRetrieval.class);
 	
 	protected Session session;
 	
 	public SearchDataRetrieval() {
 		
 	}
 	
 	public SearchDataRetrieval(Session session) {
 		this();
 		setSession(session);
 	}
 	
 	/**
 	 * Search for associated photos
 	 * 
 	 * @param photoId		the source photo
 	 * @param maxMatches	the max number of matching photos to return
 	 * @return a list of matching photos up to the specified maximum number
 	 */
 	public List<Photo> searchAssociatedPhotos(int photoId, int maxMatches) {
 		// load application properties
 		Properties config = ConfigUtil.getApplicationProperties();
 		if(config == null)
 			return null;
 		
 		// use photo associations query
 		String queryString = config.getProperty("search.sql.query.associations");
 		
 		Query query = session.createSQLQuery(queryString)
 				.addEntity(Photo.class) // set return type of objects to photo entity
 				.setParameter("photoId", photoId) // set photo id
 				.setParameter("maxMatches", maxMatches); // set maximum number of matches
 		
 		// execute query and retrieve list of ordered photos
 		List<Photo> result = query.list();
 		return result;
 	}
 	
 	/**
 	 * Search for the photos which match the list of categories
 	 * 
 	 * @param photoId		the source photo
 	 * @param categories	the categories to match against
 	 * @param maxMatches	the max number of matching photos to return
 	 * @return a list of matching photos up to the specified maximum number
 	 */
 	public List<Photo> searchFilteredAssociatedPhotos(int photoId, List<String> categories, int maxMatches) {
 		// load application properties
 		Properties config = ConfigUtil.getApplicationProperties();
 		if(config == null)
 			return null;
 		
		// parse list of categories
		StringBuilder categoriesString = new StringBuilder();
		for(String category : categories) {
			categoriesString.append(category);
			categoriesString.append(" ");
		}
		System.out.println("CATEGORIES: " + categoriesString.toString());
 		// use filtered photo associations query
 		String queryString = config.getProperty("search.sql.query.filteredAssociations");
 		
 		Query query = session.createSQLQuery(queryString)
 				.addEntity(Photo.class) // set return type of objects to photo entity
 				.setParameter("photoId", photoId) // set photo id
 				.setParameter("maxMatches", maxMatches) // set maximum number of matches
				.setParameter("categories", categoriesString.toString()); // set categories to match against
 		
 		// execute query and retrieve list of ordered photos
 		List<Photo> result = query.list();
 		return result;
 	}
 	
 	/**
 	 * Search for photos whose categories or comments match the list of keywords
 	 * 
 	 * @param keywords	the list of keywords to match against
 	 * @param maxMatches	the max number of matching photos to return
 	 * @return a list of matching photos up to the specified maximum number
 	 */
 	public List<Photo> searchPhotosByKeyword(List<String> keywords, int maxMatches) {
 		// load application properties
 		Properties config = ConfigUtil.getApplicationProperties();
 		if(config == null)
 			return null;
 		
		// parse keywords list
 		StringBuilder keywordString = new StringBuilder();
 		for(String keyword : keywords) {
 			keywordString.append(keyword);
 			keywordString.append("* ");
 		}
 		
 		// use keyword-based search query
 		String queryString = config.getProperty("search.sql.query.keywords");
 		
 		Query query = session.createSQLQuery(queryString)
 				.addEntity(Photo.class) // set return type of objects to photo entity
 				.setParameter("maxMatches", maxMatches) // set maximum number of matches
 				.setParameter("keywords", keywordString.toString()); // set keywords to match against
 		
 		// execute query and retrieve list of ordered photos
 		List<Photo> result = query.list();
 		return result;
 	}
 	
 	public Session getSession() {
 		return session;
 	}
 	
 	public void setSession(Session session) {
 		this.session = session;
 	}
 }

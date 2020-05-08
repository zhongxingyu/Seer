 package edu.sjsu.cmpe.library.repository;
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentHashMap;
 
 import edu.sjsu.cmpe.library.domain.Book;
 import edu.sjsu.cmpe.library.domain.Review;
 
 public class ReviewRepository implements ReviewRepositoryInterface{
 
 	private final ConcurrentHashMap<Long, Review> reviewInMemoryMap;
 	
 	private long reviewKey;
 	
 		
 	public ReviewRepository(ConcurrentHashMap<Long,Review> reviewMap) {
 			checkNotNull(reviewMap, "bookMap must not be null for BookRepository");
 			reviewInMemoryMap = reviewMap;
 			reviewKey = 0;
 		    }
 	 private final Long generateReviewKey() {
 			// increment existing reviewKey and return the new value
 			return Long.valueOf(++reviewKey);
 		    }
 
 	public Review saveReview(Review newReview, Long id){
 		
 		checkNotNull(newReview, "newReview instance must not be null");
 		Long reviewKey = generateReviewKey();
 		newReview.setId(id);
 		newReview.setRating(newReview.getRating());
 		newReview.setComment(newReview.getComment());
 	    
 		reviewInMemoryMap.putIfAbsent(reviewKey, newReview);
 		
 		return newReview;
 	
 	}
 	
 	public  Review getReviewByReviewId(Long reviewId, Long isbn){
 		checkArgument(reviewId > 0,
 				"reviewID was %s but expected greater than zero value", reviewId);
 		if(reviewInMemoryMap.get(reviewId).getId()==isbn)	
 		return reviewInMemoryMap.get(reviewId);
 		return null;
 	}
 	
     public ArrayList<Review> getAllReviews(Long isbn){
     	
     ArrayList<Review> review = new ArrayList<Review>();
     
    for(long i=1;i<reviewInMemoryMap.size(); i++){
     	if(reviewInMemoryMap.get(i).getId()==isbn)
    	     review.add(reviewInMemoryMap.get(i));
     }
     return review;
     
 	}
 		
    	
     	
     }
 
 
 

 package com.alphadog.grapevine.models.test;
 
 import junit.framework.TestCase;
 
 import com.alphadog.grapevine.models.Review;
 
 
 public class ReviewTest extends TestCase {
 	
 	private Review testReview;
 	
 	public void testReviewObjectAttributesGettersSetters() {
		testReview = new Review(-1, "Heading", "Description", "http://temp.com/image","123456", "456733",1, null, null, null);
 		assertNotNull(testReview);
 		assertEquals("Heading", testReview.getHeading());
 		assertEquals("Description", testReview.getDescription());
 		assertEquals("http://temp.com/image", testReview.getImageUrl());
 		assertEquals("456733", testReview.getLatitude());
 		assertEquals("123456", testReview.getLongitude());
 		assertEquals(true, testReview.isLike());
 		testReview.setId(12);
 		assertEquals(12, testReview.getId());
 	}
 	
 	public void testEqualizationOfReview() {
		Review review = new Review(-1, "Heading", "Description", "http://temp.com/image","123456", "456733",1, null, null, null);
		Review duplicateReview = new Review(-1, "Heading", "Description", "http://temp.com/image","123456", "456733",1, null, null, null);
 		assertFalse(review.equals(new StringBuffer("Random Object")));
 		assertTrue(review.equals(duplicateReview));
 		assertTrue(review.equals(review));
 		duplicateReview.setId(122);
 		assertFalse(review.equals(duplicateReview));
 	}
 }

 package org.recipesearch.hibernatesearch.util;
 
 import org.apache.lucene.search.Filter;
import org.apache.lucene.search.RangeFilter;
 import org.apache.lucene.search.TermRangeFilter;
 import org.hibernate.search.annotations.Factory;
 import org.hibernate.search.annotations.Key;
 import org.hibernate.search.filter.FilterKey;
 import org.hibernate.search.filter.StandardFilterKey;
 
 public class MaximumPriceFilterFactory {
 	private static int PAD = 10;
 	private long maxPrice = -1;
 	
 	public void setMaxPrice(long maxPrice) {  //inject max price
 		this.maxPrice = maxPrice;
 	}
 	
 	@Factory
 	public Filter getMaximumPriceFilter() {
 		if ( maxPrice == -1) {
 			throw new IllegalStateException("MaximumPriceFilterFactory.maxPrice is mandatory");
 		}
 		Filter filter = TermRangeFilter.Less("price", pad(maxPrice) );  //build a range filter
 		return filter;
 	}
 	
 	@Key
 	public FilterKey getKey() {
 		StandardFilterKey key = new StandardFilterKey();
 		key.addParameter(maxPrice);
 		return key;
 	}
 	
 	private String pad(long price) {  //apply same padding strategy as bridge
 		String rawLong = Long.toString(price);
 		if (rawLong.length() > PAD) 
 			throw new IllegalArgumentException( "Try to pad on a number too big" );
 		
 		StringBuilder paddedLong = new StringBuilder( );
 		for ( int padIndex = rawLong.length() ; padIndex < PAD ; padIndex++ ) {
 			paddedLong.append('0');
 		}
 		return paddedLong.append( rawLong ).toString();
 	}
 }

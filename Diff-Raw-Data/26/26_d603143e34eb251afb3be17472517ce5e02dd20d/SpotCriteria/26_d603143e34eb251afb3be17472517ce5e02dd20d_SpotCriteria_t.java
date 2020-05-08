 /**
  * Copyright (c) 2010, Todd Ginsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    * Neither the name of Todd Ginsberg, or Gowalla nor the
  *      names of any contributors may be used to endorse or promote products
  *      derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  *  Also, please use this for Good and not Evil.  
  */
 package com.ginsberg.gowalla;
 
 import java.util.Comparator;
 
 import com.ginsberg.gowalla.dto.Locatable;
 import com.ginsberg.gowalla.dto.SimpleSpot;
 import com.ginsberg.gowalla.dto.compare.SpotDistanceComparator;
 
 /**
  * Immutable class for creating Spot Finding Criteria, with enclosed builder.
  * 
  * @author Todd Ginsberg
  */
 public class SpotCriteria {
 	 
 	private PagingSupport pagingSupport;
 	private Locatable location; 
 	private int radiusMeters; 
 	private Integer numberOfSpots; 
 	private boolean featured = false; 
 	private Integer parentCategoryId;
 	private Comparator<SimpleSpot> sortBy;
 	
 	private String request;
 	
 	/**
 	 * Keep this private so people will go through the builder.
 	 */
 	private SpotCriteria() {
 		super();
 	}
 	
 	/**
 	 * WARNING: May not be supported by Gowalla.
 	 */
 	public PagingSupport getPagingSupport() {
 		return pagingSupport;
 	}
 
 	public Locatable getLocation() {
 		return location;
 	}
 
 	public int getRadiusMeters() {
 		return Math.abs(radiusMeters);
 	}
 
 	/**
 	 * WARNING: May not be supported by Gowalla.
 	 */
 	public int getNumberOfSpots() {
 		return numberOfSpots == null ? 0 : Math.abs(numberOfSpots);
 	}
 
 	/**
 	 * WARNING: May not be supported by Gowalla.
 	 */
 	public boolean isFeatured() {
 		return featured;
 	}
 
 	/**
 	 * WARNING: May not be supported by Gowalla.
 	 */
 	public Integer getParentCategoryId() {
 		return parentCategoryId;
 	}
 	
 	public Comparator<SimpleSpot> getSortBy() {
 		return sortBy;
 	}
 
 
 	public String getRequestWithArguments(int offset) {
 		if(request == null) {
 			StringBuilder buf = new StringBuilder();
 			buf.append(String.format("/spots?lat=%f&lng=%f&radius=%d", 
 					location.getGeoLocation().getLatitude(),
 					location.getGeoLocation().getLongitude(),
 					getRadiusMeters()));
 			if(featured) {
 				buf.append("&featured=1");
 			}
 			if(numberOfSpots != null && numberOfSpots != 0) {
 				buf.append(String.format("&limit=%d", getNumberOfSpots()));
 			}
 			if(parentCategoryId != null) {
 				buf.append(String.format("&category_id=%d", parentCategoryId));
 			}
 			request = buf.toString(); // This part shouldn't ever change.
 		}
 		return offset == 0 ? request : String.format("%s&offset=%d", request, offset);
 	}
 
 	/**
 	 * Builder class for SpotCriteria objects.
 	 *
 	 * @author Todd Ginsberg
 	 */
 	public static class Builder {
 		private PagingSupport pagingSupport = PagingSupport.SINGLE_REQUEST_ONLY;
 		private Locatable location; 
 		private int radiusMeters; 
 		private Integer numberOfSpots; 
 		private boolean featured = false; 
 		private Integer parentCategoryId = null;
 		private Comparator<SimpleSpot> sortBy = null; 
 		
 		/**
 		 * Constructor with must-have fields.
 		 */
 		public Builder(Locatable location, int radiusMeters) {
 			super();
 			this.location = location;
 			this.radiusMeters = radiusMeters;
 		}
 
 		/**
 		 * Cause the SpotCriteria object to be built.
		 * @return An immutable instance of SpotCriteria
 		 */
 		public SpotCriteria build() {
 			if(location == null) {
 				throw new IllegalArgumentException("Cannot build SpotCriteria without a Location");
 			}
 			if(radiusMeters == 0) {
 				throw new IllegalArgumentException("Cannot build SpotCriteria without a radius");
 			}
 			
 			SpotCriteria criteria = new SpotCriteria();
 			criteria.pagingSupport = this.pagingSupport;
 			criteria.location = this.location;
 			criteria.radiusMeters = this.radiusMeters;
 			criteria.numberOfSpots = this.numberOfSpots;
 			criteria.featured = this.featured;
 			criteria.parentCategoryId = this.parentCategoryId;
 			
 			// Ordering.
 			if(sortBy != null) {
 				criteria.sortBy = sortBy;
 			} else {
 				criteria.sortBy = new SpotDistanceComparator(location);
 			}
 			return criteria;
 		}
 
 		public SpotCriteria.Builder pagingSupport(PagingSupport pagingSupport) {
 			this.pagingSupport = pagingSupport;
 			return this;
 		}
 		public SpotCriteria.Builder location(Locatable location) {
 			this.location = location;
 			return this;
 		}
 
 		public SpotCriteria.Builder radiusMeters(int radiusMeters) {
 			this.radiusMeters = radiusMeters;
 			return this;
 		}
 
 		public SpotCriteria.Builder numberOfSpots(int numberOfSpots) {
 			this.numberOfSpots = numberOfSpots;
 			return this;
 		}
 
 		public SpotCriteria.Builder featured(boolean featured) {
 			this.featured = featured;
 			return this;
 		}
 
 		public SpotCriteria.Builder parentCategoryId(int parentCategoryId) {
 			this.parentCategoryId = parentCategoryId;
 			return this;
 		}
 		
 		public SpotCriteria.Builder clearParentCategoryId() {
 			this.parentCategoryId = null;
 			return this;
 		}
 		
 		public SpotCriteria.Builder sortBy(Comparator<SimpleSpot> sortBy) {
 			this.sortBy = sortBy;
 			return this;
 		}
 	}
 	
 	
 }

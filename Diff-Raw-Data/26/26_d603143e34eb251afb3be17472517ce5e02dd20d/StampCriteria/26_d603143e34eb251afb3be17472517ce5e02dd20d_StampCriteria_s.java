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
 
 import com.ginsberg.gowalla.dto.Id;
 import com.ginsberg.gowalla.dto.User;
 
 /**
  *
  * @author Todd Ginsberg
  */
 public class StampCriteria {
 	private String username;
 	private Integer id;
 	private Integer limit;
 	private StampContext context;
 	
 	private String request;
 	
 	// Keep private.
 	private StampCriteria() {
 		super();
 	}
 	
 	public String getUsername() {
 		return username;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public Integer getLimit() {
 		return limit;
 	}
 
 	public StampContext getContext() {
 		return context;
 	}
 
 	public String getRequest() {
 		if(request == null) {
 			StringBuilder buf = new StringBuilder();
 			if(id != null) {
 				buf.append(String.format("/users/%d/stamps", id));
 			} else if(username == null) {
 				buf.append(String.format("/users/%s/stamps", username));
 			}
 			
 			boolean arg = false;
 			if(limit != null && limit != 0) {
 				buf.append(String.format("?limit=%d", limit));
 				arg = true;
 			}
 			if(context != null && context != StampContext.ALL) {
 				buf.append(arg ? "&" : "?");
 				buf.append(String.format("context=%s", context.name().toLowerCase()));
 			}
 			
 			request = buf.toString();
 		}
 		return request;
 	}
 	
 	public static class Builder {
 		private String username;
 		private Integer id;
 		private Integer limit;
 		private StampContext context = StampContext.ALL;
 		
 		public Builder(Id<User> identity) {
 			this.id = identity.getId();
 			this.username = null;
 		}
 		
 		public Builder(String username) {
 			this.username = username;
 			this.id = null;
 		}
 		
 		public Builder(int id) {
 			this.id = id;
 			this.username = null;
 		}
 		
 		public StampCriteria build() {
 			StampCriteria criteria = new StampCriteria();
 			if(username != null) {
 				criteria.username = this.username;
 			} else if(id != null) {
 				criteria.id = this.id;
 			} else {
 				throw new IllegalArgumentException("Cannot create Stamp Criteria without username or id.");
 			}
 			criteria.limit = this.limit;
 			criteria.context = this.context;
 			return criteria;
 		}
 		
 		public Builder stampContext(StampContext context) {
 			this.context = context;
 			return this;
 		}
 		
 		public Builder limit(int limit) {
 			this.limit = limit;
 			return this;
 		}
 	}
 
 }

 package com.tuit.ar.api.request;
 
 public enum Options {
 	LOGIN {
 		public String toString() {
 			return "account/verify_credentials";
 		}
 	}, FRIENDS_TIMELINE {
 		public String toString() {
 			return "statuses/home_timeline";
 		}
 	}, REPLIES_TIMELINE {
 		public String toString() {
 			return "statuses/mentions";
 		}
 	}, POST_TWEET {
 		public String toString() {
 			return "statuses/update";
 		}
	}, POST_TWEET_WITH_PHOTO {
		public String toString() {
			return "statuses/update_with_photo";
		}
 	}
 }

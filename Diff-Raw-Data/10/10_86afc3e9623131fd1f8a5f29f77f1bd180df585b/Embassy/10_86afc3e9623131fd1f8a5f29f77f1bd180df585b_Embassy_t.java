 package com.limewoodMedia.nsapi.holders;
 
 public class Embassy {
 	/**
 	 * Embassy status
 	 */
 	public enum EmbassyStatus {
 		/** An established embassy */
 		ESTABLISHED(null),
 		/** An embassy being created */
 		PENDING("pending"),
 		/** An embassy invitation from another region */
 		INVITED("invited"),
 		/** An embassy this region requested with another region */
 		REQUESTED("requested"),
 		/** An requested embassy that was denied recently */
 		DENIED("denied");
 		
 		public static EmbassyStatus parse(String description) {
 			for(EmbassyStatus es : values()) {
 				if(description == null) {
 					return ESTABLISHED;
 				}
				else if(description.equalsIgnoreCase(es.description)) {
 					return es;
 				}
 			}
 			return null;
 		}
 		
 		private String description;
 		
 		private EmbassyStatus(String description) {
 			this.description = description;
 		}
 		
 		public String getDescription() {
 			return description;
 		}
 	};
 	
 	public String region;
 	public EmbassyStatus status;
 	
 	public Embassy() {}
 	
 	public Embassy(String region, EmbassyStatus status) {
 		this.region = region;
 		this.status = status;
 	}
 }

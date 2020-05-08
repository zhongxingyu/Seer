 package gov.usgs;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class HTTPParameters {
 	public static enum ExtParam{
 			AGENCY_CODE("agency_cd"),
 			SITE_NO("siteNo"),
			DOWNLOAD_TOKEN("token"),
 			WATER_LEVEL_FLAG("wlSnFlag"),
 			WATER_QUALITY_FLAG("qwSnFlag")
 			;
 	
 		public final String key;
 
 		private ExtParam(String key) {
 			this.key = key;
 		}
 		
 		public String parse(HttpServletRequest req) {
 			return req.getParameter(key);
 		}
 	
 	}
 	
 	public static enum CacheParam{
 		AGENCY_ID("agencyID");
 		
 		public final String key;
 
 		private CacheParam(String key) {
 			this.key = key;
 		}
 		
 	}
 }

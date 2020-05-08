 package plugins;
 
 import java.lang.reflect.Method;
 import java.util.List;
 
 import models.AppProps;
 import play.Logger;
 import play.PlayPlugin;
 import play.cache.Cache;
 import play.libs.Time;
 import play.mvc.Http.Request;
 import play.mvc.Http.Response;
 import util.Utils;
 import util.Utils.MatchAction;
 
 /**
  * This plugin intercepts invocation on T-Coffee bundle index page and will store a copy in 
  * the application cache, that copy will be presented as application main root page ('/') in 
  * order to optimized SEO ranking. 
  * 
  * 
  * @author Paolo Di Tommaso
  *
  */
 public class HomeCachePlugin  extends PlayPlugin {
 
 	private long time;
 	
 	@Override
     public void beforeActionInvocation(Method actionMethod) {
 		
 	}
 	
 	@Override
 	public void afterActionInvocation() { 
 		try { 
 			safeCache();
 		}
 		catch( Exception e ) { 
 			Logger.warn(e, "Erron on home page caching");
 		}
 	
 	}
 	
 	private void safeCache() { 
 		/*
 		 * this feature is active only on T-Coffee home page
 		 */
 		if( !"/apps/tcoffee/index.html".equals(Request.current().path) ) 
 		{ 
 			return;
 		}
 		
 		/* 
 		 * Check if homepage cache is active and how long is its duration
 		 */
		String sDuration = AppProps.instance().getString("homepage.cache.duration");
 		if( Utils.isEmpty(sDuration) || "0".equals(sDuration.trim())) { 
 			Logger.debug("Resetting tcoffee_index_page cache");
 			Cache.delete("tcoffee_index_page");
 			return;
 		}
 		
 		if( Cache.get("sysmsg") != null && Cache.get("tcoffee_index_page") != null ) { 
 			Logger.info("Invalidating home page because a system message is available");
 			Cache.delete("tcoffee_index_page");
 			return;
 		}
 		
 		
 		/* check how much time is passed */
 		long delta = System.currentTimeMillis()-time;
 		if( sDuration == null || delta  < Time.parseDuration(sDuration)*1000 ) { 
 			Logger.debug("Homepage cache skipping (cache duration: %s - elapsed time: %s)", sDuration, Utils.asDuration(delta));
 			return;
 		}
 		
 		Logger.debug("Caching tcoffee_index_page element (cache duration: %s - elapsed time: %s)", sDuration, Utils.asDuration(delta));
 		String home = Response.current().out.toString();
 		Cache.set("tcoffee_index_page", fixPaths(home));		
 		time = System.currentTimeMillis();
 	}
 	
 	static boolean isAbsolute( String path ) { 
 		return path != null && 
 			( path.startsWith("/") || path.startsWith("http:") || path.startsWith("https:") || path.startsWith("#"));
 	}
 	
 	/**
 	 * Fix all relative paths adding the prefix "/apps/tcoffee/"
 	 * 
 	 * @param body the page html string 
 	 * @return html with fixed relative paths 
 	 */
 	static String fixPaths( String body ) { 
 		
 		/*
 		 * fix 'src' attributes
 		 */
 		body = Utils.match(body, " src=(['\"]?)([^'\" ]+)['\"]?", new MatchAction() {
 			
 			public String replace(List<String> groups) {
 				String path = groups.get(2);
 				return isAbsolute(path) ? groups.get(0) : " src=$1/apps/tcoffee/$2$1";
 			}
 		});
 		
 
 		/*
 		 * fix 'href' attributes
 		 */
 		body = Utils.match(body, " href=(['\"]?)([^'\" ]+)['\"]?", new MatchAction() {
 			
 			public String replace(List<String> groups) {
 				String path = groups.get(2);
 				return isAbsolute(path) ? groups.get(0) : " href=$1/apps/tcoffee/$2$1";
 			}
 		});
 		
 		/*
 		 * fix urls 
 		 */
 		body = Utils.match(body, "url\\((['\"]?)([^'\" ]+)['\"]?\\)", new MatchAction() {
 			
     		public String replace(List<String> groups) {
     			String path = groups.get(2);
 				return isAbsolute(path) ? groups.get(0) : " url($1/apps/tcoffee/$2$1)";
     		}
 		});
 		
 		
 		return body;
 	}
 		
 }

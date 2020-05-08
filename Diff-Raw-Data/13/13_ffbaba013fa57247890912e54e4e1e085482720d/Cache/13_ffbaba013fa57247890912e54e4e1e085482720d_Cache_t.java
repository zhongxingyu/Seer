 package dolda.jsvc.util;
 
 import dolda.jsvc.*;
 import java.util.*;
 
 public class Cache {
     public static void checkmtime(Request req, long mtime) {
 	/* Since the HTTP time format is (reasonably enough) precise
 	 * only to seconds, any extra milliseconds must be trimmed
 	 * off, or the mtime will almost certainly not match. */
	final Date mdate = new Date((mtime / 1000) * 1000);
 	String ims = req.inheaders().get("If-Modified-Since");
 	if(ims != null) {
 	    Date cldate;
 	    try {
 		cldate = Http.parsedate(ims);
 	    } catch(java.text.ParseException e) {
 		throw(Restarts.stdresponse(400, "The If-Modified-Since header is not parseable."));
 	    }
 	    if(mdate.compareTo(cldate) <= 0) {
		throw(new RequestRestart() {
			public void respond(Request req) {
			    req.status(304);
			    req.outheaders().put("Content-Length", "0");
			    req.outheaders().put("Last-Modified", Http.fmtdate(mdate));
			}
		    });
 	    }
 	}
 	req.outheaders().put("Last-Modified", Http.fmtdate(mdate));
     }
 }

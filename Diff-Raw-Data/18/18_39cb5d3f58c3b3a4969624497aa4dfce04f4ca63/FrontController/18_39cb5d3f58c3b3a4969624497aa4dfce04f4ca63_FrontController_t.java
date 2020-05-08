 package ch.epfl.bbcf.gdv.control.http;
 
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.protocol.http.WebResponse;
 import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;
 
 import ch.epfl.bbcf.gdv.config.Configuration;
 import ch.epfl.bbcf.gdv.config.Logs;
 import ch.epfl.bbcf.gdv.config.UserSession;
 import ch.epfl.bbcf.gdv.control.http.command.Command;
 import ch.epfl.bbcf.gdv.control.http.command.PostAccess;
 import ch.epfl.bbcf.gdv.control.http.command.TrackError;
 import ch.epfl.bbcf.gdv.control.http.command.TrackParsingSuccess;
 import ch.epfl.bbcf.gdv.control.http.command.TrackStatus;
 import ch.epfl.bbcf.gdv.html.PostPage;
 
 public class FrontController {
 
 	private static Logger log = Logs.initPOSTLogger(PostPage.class.getName());
 	private RequestParameters params;
 	private UserSession session;
 	private WebResponse webResponse;
 
 	public FrontController(RequestParameters params,UserSession session, WebResponse webResponse){
 		this.session = session;
 		this.params = params;
 		this.webResponse = webResponse;
 	}
 
 	public void doRequest() {
		log.debug("do Request");
 		if(params.getId()!=null){
 			Command command = null;
 			//CHANGE STATUS OF A TRACK
 			if(params.getId().equalsIgnoreCase("track_status")){
 				command = new TrackStatus(session,params,webResponse);
 				//ERROR IN TRACK PROCESSING
 			} else if(params.getId().equalsIgnoreCase("track_error")){
 				command = new TrackError(session,params,webResponse);
 				//TRANSFORM TO SQLITE SUCCED
 			} else if(params.getId().equalsIgnoreCase("track_parsing_success")){
 				log.debug("parsing success");
 				command = new TrackParsingSuccess(session, params, webResponse);
 				//POST ACCESS TO GDV
 			} else if(params.getId().equalsIgnoreCase(Configuration.getGdv_post_access())){
 				command = new PostAccess(session, params, webResponse);
 			}
 			if(null!=command){
 				command.doRequest();
 			} else {
				log.error("no command");
 				new AbortWithHttpStatusException(400,true);
 			}
 		} else {
			log.error("no id");
 			throw new AbortWithHttpStatusException(400,true);
 		}
 
 	}
 }

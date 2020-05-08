 package com.kdcloud.server.rest.application;
 
 import java.util.logging.Level;
 
 import org.restlet.Application;
 import org.restlet.Context;
 import org.restlet.Restlet;
 import org.restlet.data.ChallengeScheme;
 import org.restlet.resource.Directory;
 import org.restlet.routing.Router;
 import org.restlet.security.ChallengeAuthenticator;
 
 import com.kdcloud.lib.domain.ServerParameter;
 import com.kdcloud.server.rest.resource.QueueWorkerServerResource;
 import com.kdcloud.server.tasks.TaskQueue;
 
 public class MainApplication extends Application {
 	
 	@Override
 	public Restlet createInboundRoot() {
 		getLogger().setLevel(Level.FINEST);
 		
 		Context applicationContext = new GAEContext(getLogger());
 
 		Router router = new Router(getContext());
 		
		router.attach("/XML", new Directory(getContext(), "war:///"));
 		
 		router.attach(TaskQueue.WORKER_URI + ServerParameter.TASK_ID, QueueWorkerServerResource.class);
 
 		ChallengeAuthenticator guard = new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "testRealm");
 		guard.setVerifier(new OAuthVerifier(getLogger(), true));
 		guard.setNext(new KDApplication(applicationContext));
 		router.attachDefault(guard);
 
 		return router;
 
 	}
 
 }

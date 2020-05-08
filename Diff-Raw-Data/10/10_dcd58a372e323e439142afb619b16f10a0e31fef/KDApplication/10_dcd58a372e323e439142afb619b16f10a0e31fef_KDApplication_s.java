 package com.kdcloud.server.rest.application;
 
 import org.restlet.Application;
 import org.restlet.Restlet;
 import org.restlet.routing.Router;
 
 import com.kdcloud.server.rest.api.AnalysisResource;
 import com.kdcloud.server.rest.api.DatasetResource;
 import com.kdcloud.server.rest.api.DeviceResource;
 import com.kdcloud.server.rest.api.GlobalAnalysisResource;
 import com.kdcloud.server.rest.api.GlobalDataResource;
 import com.kdcloud.server.rest.api.ModalitiesResource;
 import com.kdcloud.server.rest.api.ReportResource;
 import com.kdcloud.server.rest.api.SchedulerResource;
<<<<<<< HEAD
 import com.kdcloud.server.rest.api.UserDataResource;
 import com.kdcloud.server.rest.api.UserDetailsResource;
 import com.kdcloud.server.rest.resource.AnalysisServerResource;
=======
import com.kdcloud.server.rest.api.UserDetailsResource;
>>>>>>> 9ce796218abe195517128ff80a215af55b79d39a
 import com.kdcloud.server.rest.resource.DatasetServerResource;
 import com.kdcloud.server.rest.resource.DeviceServerResource;
 import com.kdcloud.server.rest.resource.GlobalAnalysisServerResource;
 import com.kdcloud.server.rest.resource.GlobalDataServerResource;
 import com.kdcloud.server.rest.resource.ModalitiesServerResource;
 import com.kdcloud.server.rest.resource.ReportServerResource;
 import com.kdcloud.server.rest.resource.SchedulerServerResource;
<<<<<<< HEAD
 import com.kdcloud.server.rest.resource.UserDataServerResource;
=======
>>>>>>> 9ce796218abe195517128ff80a215af55b79d39a
 import com.kdcloud.server.rest.resource.UserDetailsServerResource;
 
 public class KDApplication extends Application {
 
 
 	/**
 	 * Creates a root Restlet that will receive all incoming calls.
 	 */
 	@Override
 	public Restlet createInboundRoot() {
 		
 		Router router = new Router(getContext());
 		
 		router.attach(UserDataResource.URI, UserDataServerResource.class);
 		router.attach(DatasetResource.URI, DatasetServerResource.class);
 		router.attach(AnalysisResource.URI, AnalysisServerResource.class);
 		router.attach(SchedulerResource.URI, SchedulerServerResource.class);
 		router.attach(ReportResource.URI, ReportServerResource.class);
 		router.attach(DeviceResource.URI, DeviceServerResource.class);
 		router.attach(UserDetailsResource.URI, UserDetailsServerResource.class);
 		router.attach(ModalitiesResource.URI, ModalitiesServerResource.class);
 		router.attach(GlobalAnalysisResource.URI, GlobalAnalysisServerResource.class);
 		router.attach(GlobalDataResource.URI, GlobalDataServerResource.class);
 		
 		return router;
 	}
 }

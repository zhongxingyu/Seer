 /**
  * Copyright 2011 Henric Persson (henric.persson@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package burrito;
 
 import taco.CachePolicy;
 import taco.Router;
 import burrito.controller.AddFeedsSubscriptionFeedController;
 import burrito.controller.AdminController;
 import burrito.controller.BBPreviewController;
 import burrito.controller.BroadcastMessageAsyncController;
 import burrito.controller.BroadcastMessageController;
 import burrito.controller.DropFeedsSubscriptionChannelController;
 import burrito.controller.FeedsInactivateController;
 import burrito.controller.KeepFeedsSubscriptionAliveController;
 import burrito.controller.NewFeedsSubscriptionChannelController;
 import burrito.controller.NewFeedsSubscriptionController;
 import burrito.controller.PollSiteletBoxController;
 import burrito.controller.PollSubscriptionController;
 import burrito.controller.RefreshAllSiteletsController;
 import burrito.controller.RefreshSiteletController;
 import burrito.controller.RefreshSiteletsController;
 import burrito.protector.BackgroundTaskProtector;
 import burrito.protector.BroadcastProtector;
 import burrito.render.MessagesRenderer;
 import burrito.render.RefreshSiteletRenderer;
 import burrito.server.blobstore.BlobServiceImpl;
 import burrito.server.blobstore.BlobStoreImageServlet;
 import burrito.server.blobstore.BlobStoreServlet;
 import burrito.services.BBCodeServiceImpl;
 import burrito.services.CrudServiceImpl;
 import burrito.services.SiteletServiceImpl;
 import burrito.util.BBCodeCreator;
 
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class BurritoRouter extends Router {
 
 	UserService service = UserServiceFactory.getUserService();
 	
	public static final CachePolicy ONE_HOUR_CACHE_POLICY = new CachePolicy(60);
 	
 	@Override
 	public void init() {
 		
 		BackgroundTaskProtector btProtector = new BackgroundTaskProtector();
 		route("/burrito/admin").through(AdminController.class).renderedBy("/Admin.jsp").protect(Configurator.getAdminProtector());
 		route("/burrito/crudmessages.js").through(MessagesController.class).renderedBy(new MessagesRenderer()).protect(Configurator.getAdminProtector());
 		route("/burrito/crud").throughServlet(CrudServiceImpl.class).protect(Configurator.getAdminProtector());
 		route("/burrito/sitelets").throughServlet(SiteletServiceImpl.class).protect(Configurator.getAdminProtector());
 		route("/burrito/sitelets/refresh/sitelet").through(RefreshSiteletController.class).renderedBy(new RefreshSiteletRenderer()).protect(btProtector); 
 		route("/burrito/sitelets/refresh/{siteletPropertiesId:long}").through(RefreshSiteletController.class).renderedBy(new RefreshSiteletRenderer()).protect(btProtector);
 		route("/burrito/blobService").throughServlet(BlobServiceImpl.class).protect(Configurator.getAdminProtector());
 		route("/burrito/bbCodeService").throughServlet(BBCodeServiceImpl.class).protect(Configurator.getAdminProtector());
 		
 		//Change to one year? Is there any problem doing so?
 		route("/blobstore/image").throughServlet(BlobStoreImageServlet.class).setCachePolicy(ONE_HOUR_CACHE_POLICY);
 		route("/blobstore/serve").throughServlet(BlobStoreServlet.class);
 
 		route("/burrito/sitelets/refresh").through(RefreshSiteletsController.class).renderAsJson().protect(btProtector);
 		route("/burrito/sitelets/refresh/all").through(RefreshAllSiteletsController.class).renderAsJson().protect(btProtector); 
 		route("/burrito/sitelets/box/{containerId}/poll").through(PollSiteletBoxController.class).renderAsJson();
 
 		route("/burrito/feeds/subscription/new/{method}").through(NewFeedsSubscriptionController.class).renderAsJson(); 
 		route("/burrito/feeds/subscription/new/{method}/{channelId}").through(NewFeedsSubscriptionController.class).renderAsJson(); 
 		route("/burrito/feeds/subscription/{subscriptionId:long}/addFeed/{feedId}").through(AddFeedsSubscriptionFeedController.class).renderAsJson();
 		route("/burrito/feeds/subscription/{subscriptionId:long}/keepAlive").through(KeepFeedsSubscriptionAliveController.class).renderAsJson();
 		route("/burrito/feeds/subscription/{subscriptionId:long}/newChannel").through(NewFeedsSubscriptionChannelController.class).renderAsJson();
 		route("/burrito/feeds/subscription/{subscriptionId:long}/dropChannel").through(DropFeedsSubscriptionChannelController.class).renderAsJson();
 		route("/burrito/feeds/subscription/{subscriptionId:long}/poll").through(PollSubscriptionController.class).renderAsJson();
 		route("/burrito/feeds/cleanup/{nrOfDays:int}").through(FeedsCleanupController.class).renderAsJson().protect(btProtector);
 		route("/burrito/feeds/inactivate").through(FeedsInactivateController.class).renderAsJson().protect(btProtector);
 		
 		BroadcastProtector bcProtector = new BroadcastProtector();
 		route("/burrito/feeds/{feedId}/broadcast/async").through(BroadcastMessageAsyncController.class).renderAsJson().protect(bcProtector);
 		route("/burrito/feeds/{feedId}/broadcast").through(BroadcastMessageController.class).renderAsJson().protect(bcProtector);
 		
 		route("/burrito/bbCodePreview").through(BBPreviewController.class).renderedBy(BBCodeCreator.getPreviewJspUrl());
 	}
 }

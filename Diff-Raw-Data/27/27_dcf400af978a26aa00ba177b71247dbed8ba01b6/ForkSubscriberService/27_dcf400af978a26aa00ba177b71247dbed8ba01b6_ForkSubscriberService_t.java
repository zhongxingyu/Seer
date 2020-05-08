 /*
  * Copyright 2012 Christophe Hamerling
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package controllers;
 
 import org.eclipse.egit.github.core.client.GsonUtils;
 import org.eclipse.egit.github.core.event.ForkPayload;
 
 import notifiers.MailNotifier;
 import client.PubSubHubbubClient;
 import play.Logger;
 import play.Play;
 import play.libs.IO;
 import play.mvc.Controller;
 
 /**
  * @author chamerling
  * 
  */
 public class ForkSubscriberService extends Controller {
 
 	/**
 	 * Called by github pubsubhubbub for fork events
 	 */
 	public static void service() {
		String payload = params.get("payload");
		if (payload == null || payload.length() == 0) {
 	        Logger.error("No payload for fork event");
 			return;
 		}
 		
		Logger.info(payload);
 
		MailNotifier.forked(GsonUtils.fromJson(payload, ForkPayload.class));
 	}
 }

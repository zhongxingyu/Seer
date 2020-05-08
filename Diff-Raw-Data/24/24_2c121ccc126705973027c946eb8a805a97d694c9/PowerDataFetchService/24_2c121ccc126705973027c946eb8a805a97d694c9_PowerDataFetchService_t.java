 /*******************************************************************************
  * Copyright 2011 Google Inc. All Rights Reserved.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package com.capstonecontrol.server;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 public class PowerDataFetchService {
 
 	private static final Logger log = Logger
 			.getLogger(PowerDataFetchService.class.getName());
 
 	public PowerDataFetchService() {
 	}
 
 	public static List<PowerData> getPowerData() {
 		UserService userService = UserServiceFactory.getUserService();
 		User user = userService.getCurrentUser();
 		List<Entity> powerData;
 		List<PowerData> powerDataArray = new ArrayList<PowerData>();
 
 		String firstModuleName = null;
 		if (user == null) {
 			// no user loged in so nothing to return
 			// initialize modules to nothing
 			powerData = null;
 		} else {
 			// get modules that match user
 			String username = user.getNickname();
			/*
 			DatastoreService datastore = DatastoreServiceFactory
 					.getDatastoreService();
 			Key moduleKey = KeyFactory.createKey("user", username);
 			// Run an ancestor query to ensure we see the most up-to-date
 			Query query = new Query("PowerMonitorData", moduleKey).addSort("date",
 					Query.SortDirection.DESCENDING);
 			powerData = datastore.prepare(query).asList(
 					FetchOptions.Builder.withLimit(2147483647));
					*/
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            //QUERY FOR UN-ACKED ALERTS    
           Query q = new Query("PowerMonitorData");
           //filter for events on that day     
           q.addFilter("user", FilterOperator.EQUAL, username);             
           q.addSort("date", SortDirection.DESCENDING);   
           powerData = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
			
			
 			if (powerData.isEmpty()) {
 
 				// There are no modules added to your account.
 
 			} else {
 
 				// There modules on your account are
 				for (Entity singlePowerData : powerData) {
 					// create moduleInfo and add it to the list
 					PowerData myPowerData = new PowerData(
 							(String) singlePowerData.getProperty("moduleName"),
 							(String) singlePowerData.getProperty("data"),
 							(String) singlePowerData.getProperty("user"),
 							(Date) singlePowerData.getProperty("date"));
 					powerDataArray.add(myPowerData);
 				}
 			}
 
 		}
		
 		return powerDataArray;
 	}
 }

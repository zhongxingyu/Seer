 /*
  * Copyright 2011 Matthias van der Vlies
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package controllers;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import models.Application;
 import models.ApplicationProperty;
 import play.libs.F.Promise;
 import play.mvc.WebSocketController;
 import core.LogGenerator;
 
 public class LogController extends WebSocketController {
 
 	public static void application(final Long id) throws IOException, Exception {
 		final Application application = Application.findById(id);
 		if(application == null) {
 			disconnect();
 		}
 		else {
 			try {
 				final ApplicationProperty logFileProperty = ApplicationProperty.findLogFileProperty(application);
				logToOutbound("apps/" + application.pid + "/" + logFileProperty.value, true);
 			}
 			catch(FileNotFoundException e) {
 				// ignore, logs may have been removed
 			}
 		}
 	}
 	
 	public static void manager() throws Exception {
 		logToOutbound("logs/play-as.log", true);
 	}
 
 	private static void logToOutbound(String filePath, boolean skipToEnd) throws Exception, IOException {
 		final LogGenerator generator = new LogGenerator(filePath, skipToEnd);
 		try {
 			while(inbound.isOpen()) {
 				final Promise<String> promise = generator.now();
 				final String data = await(promise);			
 				outbound.send(data);
 		    }
 		}
 		catch(Exception e) {
 			// swallow
 		}
 		generator.close();
 	}
 }

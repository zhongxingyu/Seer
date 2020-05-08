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
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
import net.sf.oval.constraint.Email;

 import models.Application;
 import models.ApplicationProperty;
 import play.Logger;
 import play.Play;
 import play.data.validation.Valid;
import play.db.jpa.JPA;
 import play.mvc.Controller;
 import core.ConfigurationManager;
 import core.ProcessManager;
 
 public class ApplicationController extends Controller {
 	
 	public static void show(final Long id) {
 		final Application application = Application.findById(id);
 		if(application == null) {
 			notFound();
 		}
 		else {
 			final List<ApplicationProperty> properties = ApplicationProperty.find("application = ? and priority > ? order by priority", application, ConfigurationManager.PRIORITY_MARGIN).fetch();
 			render(application, properties);
 		}
 	}
 	
 	public static void edit(final Long id, final String configuration) throws Exception {
 		final Application application = Application.findById(id);
 		if(application == null) {
 			notFound();
 		}
 		else {
 			ConfigurationManager.readCurrentConfigurationFromString(application, configuration);
 			show(id);
 		}
 	}
 	
 	public static void create(@Valid final Application application) throws Exception {
 		if(!validation.hasErrors()) {
 			application.enabled = false;
 			application.checkedOut = false;
 			application.clean(); // override checkout if already exists
 			application.checkout();
 		}
 		else {
 			params.flash();
 			validation.keep();
 		}
 
 		ManagerController.index();
 	}
 	
 	public static void start(final Long id) throws InterruptedException, Exception {
 		final Application application = Application.findById(id);
 		
 		if(application == null) {
 			notFound();
 		}
 		else {
 			Logger.info("Starting %s, this could take up to %s seconds when application fails to start", application.pid, Application.getCommandTimeout());
 			// forced start
 			application.start(true, true);
 
 			// make the action persistent upon crash
 			application.enabled = true;
 			application.save();
 			
 			ManagerController.index();
 		}
 	}
 
 	public static void stop(final Long id) throws InterruptedException, Exception {
 		final Application application = Application.findById(id);
 		
 		if(application == null) {
 			notFound();
 		}
 		else {
 			application.enabled = false;
 			application.save();
 			
 			Logger.info("Requesting to stop %s", application.pid);
 
 			ManagerController.index();
 		}
 	}
 	
 	public static void update(final Long id) throws Exception {
 		final Application application = Application.findById(id);
 		
 		if(application == null) {
 			notFound();
 		}
 		else {
 			application.pull();
 			ManagerController.index();
 		}
 	}
 	
 	public static void restart(final Long id) throws InterruptedException, Exception {
 		final Application application = Application.findById(id);
 		
 		if(application == null) {
 			notFound();
 		}
 		else {
 			Logger.info("Requesting to restart %s", application.pid);
 			application.restart();
 
 			ManagerController.index();
 		}
 	}
 	
 	public static void remove(final Long id) throws Exception {
 		final Application application = Application.findById(id);
 		
 		if(application == null) {
 			notFound();
 		}
 		else {
			application.stop();
			
			// make sure it gets stopped
			application.enabled = false;
			JPA.em().flush();
			
 			// First we remove the entity to avoid the process manager to start it back up again
 			for(final ApplicationProperty property : application.properties) {
 				property.delete();
 			}
 			
 			application.delete();
 			application.clean();
 	
			Logger.info("Removed %s", application.pid);
			
 			ManagerController.index();
 		}
 	}
 	
 	public static void status(final Long id) throws Exception {
 		final Application application = Application.findById(id);
 		
 		if(application == null) {
 			notFound();
 		}
 		else {
 			final String status = application.status();
 			render(status);
 		}
 	}
 }

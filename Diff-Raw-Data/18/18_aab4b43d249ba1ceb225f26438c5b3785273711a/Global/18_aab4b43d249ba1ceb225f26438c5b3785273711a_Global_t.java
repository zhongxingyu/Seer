/*******************************************************************************
 * Copyright (c) 2013 Beligum b.v.b.a. (http://www.beligum.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Beligum - initial implementation
 *******************************************************************************/
 import play.Application;
 import play.GlobalSettings;
 import play.Logger;
 
 import com.beligum.core.models.User;
 import com.beligum.core.repositories.UserRepository;
 import com.beligum.core.utils.security.UserRoles;
 
 
 public class Global extends GlobalSettings
 {
     // -----CONSTANTS-----
 
     // -----VARIABLES-----
 
     // -----CONSTRUCTORS-----
 
     // -----PUBLIC FUNCTIONS-----
     @Override
     public void onStart(Application app)
     {
 	Logger.info("Cms has started");
 	if (play.Play.application().configuration().getBoolean("com.beligum.cms.demo")) {
 	    //Create dem user
 	    Logger.info("Creating demo user");
 	    User user = UserRepository.findByEmail("demo");
 	    if (user == null) {
 		user = new User();
 		user.setEmail("demo");
 		user.setFirstName("demo");
 		user.setLastName("account");
 		user.setNewPassword("demo");
 		user.setRoleLevel(UserRoles.ADMIN_ROLE.getLevel());
 		UserRepository.save(user);
 	    }
 	}
 	
 
     }
 
 
     // -----PROTECTED FUNCTIONS-----
 
     // -----PRIVATE FUNCTIONS-----
 }

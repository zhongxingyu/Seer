 /*
  * Copyright (c) 2012, Indraprastha Institute of Information Technology,
  * Delhi (IIIT-D) and The Regents of the University of California.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above
  *    copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided
  *    with the distribution.
  * 3. Neither the names of the Indraprastha Institute of Information
  *    Technology, Delhi and the University of California nor the names
  *    of their contributors may be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE IIIT-D, THE REGENTS, AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE IIITD-D, THE REGENTS
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  */
 /*
  * Name: UserProfile.java
  * Project: SensorAct-VPDS
  * Version: 1.0
  * Date: 2012-04-14
  * Author: Pandarasamy Arjunan
  */
 package edu.pc3.sensoract.vpds.profile.mongo;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
 import edu.pc3.sensoract.vpds.api.request.UserRegisterFormat;
 import edu.pc3.sensoract.vpds.model.ShareAccessModel;
 import edu.pc3.sensoract.vpds.model.UserKeyModel;
 import edu.pc3.sensoract.vpds.model.UserProfileModel;
 import edu.pc3.sensoract.vpds.profile.ShareProfile;
 import edu.pc3.sensoract.vpds.profile.UserProfile;
 
 /**
  * User profile management, provides methods for managing user profiles.
  * 
  * @author Pandarasamy Arjunan
  * @version 1.0
  */
 
 public class ShareProfileImpl implements ShareProfile {
 
 	public boolean isAccessKeyExists(final String accesskey) {
 
 		List<ShareAccessModel> userList = ShareAccessModel.find("byAccesskey",
 				accesskey).fetchAll();
 
		if (null == userList || 1 != userList.size()) {
			return false; // something went wrong if size > 1
 		}
 		return true;
 	}
 
 	public String getUsername(final String accesskey) {
 
 		List<ShareAccessModel> userList = ShareAccessModel.find("byAccesskey",
 				accesskey).fetchAll();
 		if (null != userList && userList.size() > 0) {
 			return userList.get(0).username;
 		}
 		return null;
 	}
 
 	
 }

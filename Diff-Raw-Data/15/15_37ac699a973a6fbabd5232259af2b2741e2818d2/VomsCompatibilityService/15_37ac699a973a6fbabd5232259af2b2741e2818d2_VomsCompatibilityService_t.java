 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
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
  * Authors:
  * 	Andrea Ceccanti (INFN)
  */
 package org.glite.security.voms.admin.service;
 
 import java.rmi.RemoteException;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.glite.security.voms.VOMSException;
 import org.glite.security.voms.admin.configuration.VOMSConfiguration;
 import org.glite.security.voms.admin.operations.groups.ListMemberNamesOperation;
import org.glite.security.voms.admin.persistence.error.NoSuchGroupException;
import org.glite.security.voms.admin.persistence.error.NoSuchRoleException;
 import org.glite.security.voms.service.compatibility.VOMSCompatibility;
 
 public class VomsCompatibilityService implements VOMSCompatibility {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(VomsCompatibilityService.class);
 
 	public int getMajorVersionNumber() throws RemoteException {
 		// TODO Auto-generated method stub
 		return 2;
 	}
 
 	public int getMinorVersionNumber() throws RemoteException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public int getPatchVersionNumber() throws RemoteException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public String[] getGridmapUsers() throws RemoteException, VOMSException {
 
 		log.info("getGridmapUsers();");
 
 		try {
 
 			String voName = VOMSConfiguration.instance().getVOName();
 			List members = (List) ListMemberNamesOperation.instance(
 					"/" + voName).execute();
 
 			if (VOMSConfiguration.instance().getBoolean(
 					"voms.mkgridmap.translate_dn_email_format", false))
 				members = ServiceUtils.decorateDNList((List<String>) members);
 
 			return ServiceUtils.toStringArray(members);
 
 		} catch (RuntimeException e) {
 
 			ServiceExceptionHelper.handleServiceException(log, e);
 			throw e;
 		}
 
 	}
 
 	public String[] getGridmapUsers(String container) throws RemoteException,
 			VOMSException {
 
 		log.info("getGridmapUsers(" + container + ");");
 		try {
 
 			List members = (List) ListMemberNamesOperation.instance(container)
 					.execute();
 
 			if (VOMSConfiguration.instance().getBoolean(
 					"voms.mkgridmap.translate_dn_email_format", false))
 				members = ServiceUtils.decorateDNList((List<String>) members);
 
 			return ServiceUtils.toStringArray(members);
 
		} catch (NoSuchRoleException e){ 
		    
		    log.warn("Role '{}' is not defined for this VO.", container);
		    return null;
		
		} catch (NoSuchGroupException e){
		    
		    log.warn("Group '{}' is not defined for this VO.", container);
		    return null;
		
		}catch (RuntimeException e) {
 
 			ServiceExceptionHelper.handleServiceException(log, e);
 			throw e;
 		}
 
 	}
 
 }

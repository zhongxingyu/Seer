 /**********************************************************************************
  *
  * $URL: https://source.sakaiproject.org/contrib/etudes/melete/trunk/melete-impl/src/java/org/etudes/component/app/melete/SpecialAccessServiceImpl.java $
  * $Id: SpecialAccessServiceImpl.java 56408 2008-12-19 21:16:52Z rashmi@etudes.org $
  ***********************************************************************************
  *
  * Copyright (c) 2010 Etudes, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  **********************************************************************************/
 
 package org.etudes.component.app.melete;
 
 import java.io.Serializable;
 import java.util.ListIterator;
 import java.util.Set;
 import java.util.List;
 import java.util.Collection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.io.File;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.util.SqlHelper;
 import org.etudes.util.Different;
 import org.etudes.api.app.melete.SpecialAccessService;
 import org.etudes.api.app.melete.SpecialAccessObjService;
 import org.etudes.api.app.melete.ModuleObjService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.etudes.component.app.melete.SpecialAccess;
 
 
 public class SpecialAccessServiceImpl implements Serializable, SpecialAccessService{
 private Log logger = LogFactory.getLog(SpecialAccessServiceImpl.class);
 private SpecialAccessDB specialAccessDb;
 
 /*This method is used to insert or update special access
  * At the db layer, we determine if this is an insert or update
  */
 public void insertSpecialAccess(List saList, SpecialAccessObjService sa, ModuleObjService mod) throws Exception
 	{
 	  if (moduleDatesDiffer(sa, mod) == true)
 	  {
 		//If the module has no other accesses, no checking is needed
 	    if ((saList == null)||saList.size() == 0)
 	    {
 		setSpecialAccess(sa, mod);
 	    }
 	    else
 	    {
 		 //Iterate through each access
 		  for (ListIterator i = saList.listIterator(); i.hasNext();)
 			{
 			  SpecialAccess saObj = (SpecialAccess) i.next();
 			  //Perform checks on accesses that aren't the current one
 			  //sa is the current object
 			  if (saObj != sa)
 			  {
 				 String[] userIds = SqlHelper.decodeStringArray(saObj.getUsers());
 			    if (userIds.length > 0)
 			    {
 			     String[] targetUserIds = SqlHelper.decodeStringArray(sa.getUsers());
 				  if (targetUserIds.length > 0)
 				  {
 					  Collection userIdsColl = new ArrayList(Arrays.asList(userIds));
 					  Collection targetUserIdsColl = new ArrayList(Arrays.asList(targetUserIds));
 					 //Remove current(target) users from this special access's
 					  //user list
 					  userIdsColl.removeAll(targetUserIdsColl);
 					  if (userIdsColl != null)
 					  {
 						userIds = (String[])userIdsColl.toArray(new String[userIdsColl.size()]);
 					  }
 					  //If there are still userids remaining, update the special access
 					  //Otherwise, delete the special access
 					  if (userIds.length > 0)
 					  {
 						  saObj.setUsers(SqlHelper.encodeStringArray(userIds));
						  updateSpecialAccess(saObj);
 					  }
 					  else
 					  {
 						  //delete access
 						  List delList = new ArrayList();
 						  delList.add(saObj.getAccessId());
 						  deleteSpecialAccess(delList);
 					  }
 				  }
 			    }
 			  }
 			}
 		    //Finally, insert or update the current special access
 		    setSpecialAccess(sa, mod);
 	    }
 	  }
 		return;
 	}
 
   private boolean moduleDatesDiffer(SpecialAccessObjService sa, ModuleObjService mod) throws Exception
   {
 	//Compare special access dates to module dates
 	//If they are the same, no need to add this access and if it exists delete it
 	if ((!Different.different(sa.getStartDate(), mod.getModuleshdate().getStartDate()))&&(!Different.different(sa.getEndDate(), mod.getModuleshdate().getEndDate())))
 	{
 		//New access, so don't add it
 		if (sa.getAccessId() == 0)
 		{
 			return false;
 		}
 		else
 		{
 			//Existing access, delete it
 			List delList = new ArrayList();
 			delList.add(sa.getAccessId());
 			deleteSpecialAccess(delList);
 			return false;
 		}
 	}
 	else
 	{
 		//Access dates are different, proceed
 		return true;
 	}
   }
 
   private void setSpecialAccess(SpecialAccessObjService sa, ModuleObjService mod) throws Exception
   {
 		  if (Different.different(sa.getStartDate(), mod.getModuleshdate().getStartDate()))
 		  {
 			  sa.setOverrideStart(true);
 		  }
 		  else
 		  {
 			  sa.setOverrideStart(false);
 			  sa.setStartDate(null);
 		  }
 		  if (Different.different(sa.getEndDate(), mod.getModuleshdate().getEndDate()))
 		  {
 			  sa.setOverrideEnd(true);
 		  }
 		  else
 		  {
 			  sa.setOverrideEnd(false);
 			  sa.setEndDate(null);
 		  }  
 
	  updateSpecialAccess(sa);
  }
  
  private void updateSpecialAccess(SpecialAccessObjService sa) throws Exception
  {
	  try{
 		  specialAccessDb.setSpecialAccess((SpecialAccess)sa);
 	  }catch(Exception e)
 	  {
 		  logger.debug("melete specialAccess business --add specialAccess failed");
 		  throw new MeleteException("add_special_access_fail");
 
 	  }
   }
 
 public List getSpecialAccess(int moduleId)
 {
   List saList = null;
 	try{
 		saList = specialAccessDb.getSpecialAccess(moduleId);
 	}catch(Exception e)
 		{
 		logger.debug("melete specialAccess business --get specialAccess failed");
 		}
 	return saList;
 }
 
 /*  public List getSpecialAccesss(String userId, String siteId)
 	{
 	List mbList = null;
 		try{
 			mbList = specialAccessDb.getSpecialAccesss(userId, siteId);
 		}catch(Exception e)
 			{
 			logger.debug("melete specialAccess business --get specialAccesss failed");
 			}
 		return mbList;
 	}*/
 
 
   public void deleteSpecialAccess(List saList) throws Exception
   {
 	  try{
 			specialAccessDb.deleteSpecialAccess(saList);
 		}catch(Exception e)
 			{
 			logger.debug("melete specialAccess business -- delete specialAccess failed");
 			throw new MeleteException("delete_special_access_fail");
 			}
   }
 
 
 	/**
 	 * @return Returns the specialAccessDb
 	 */
 	public SpecialAccessDB getSpecialAccessDb() {
 		return specialAccessDb;
 	}
 	/**
 	 * @param specialAccessDb The specialAccessDb to set.
 	 */
 	public void setSpecialAccessDb(SpecialAccessDB specialAccessDb) {
 		this.specialAccessDb = specialAccessDb;
 	}
 
 }

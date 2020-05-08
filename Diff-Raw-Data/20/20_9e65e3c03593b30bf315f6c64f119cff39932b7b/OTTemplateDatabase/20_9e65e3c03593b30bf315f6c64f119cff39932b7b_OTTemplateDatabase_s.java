 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
 * $Revision: 1.7 $
 * $Date: 2005-08-22 21:09:52 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.user;
 
 import java.util.Hashtable;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTResourceMap;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.datamodel.OTIDFactory;
 import org.concord.otrunk.datamodel.OTRelativeID;
 import org.concord.otrunk.datamodel.OTUUID;
 
 /**
  * OTTemplateDatabase
  * Class name and description
  *
  * Date created: Apr 21, 2005
  *
  * @author scott<p>
  *
  */
 public class OTTemplateDatabase
     implements OTDatabase
 {
 	protected Hashtable userDataObjectMap = new Hashtable();
 	protected Hashtable mappedIdCache = new Hashtable();
 	OTDatabase rootDb;
 	OTDatabase stateDb;
 	OTReferenceMap map;
 	OTID databaseId;
 	
 	public OTTemplateDatabase(OTDatabase rootDb, OTDatabase stateDb,
 	        OTReferenceMap map)
 	{
 	    this.rootDb = rootDb;
 	    this.stateDb = stateDb;
 	    this.map = map;
 	    
 	    databaseId = OTUUID.createOTUUID();
 	}
 	
 	public OTID getDatabaseId()
 	{
 	    return databaseId;
 	}
 	
     /* (non-Javadoc)
      * @see org.concord.otrunk.datamodel.OTDatabase#setRoot(org.concord.framework.otrunk.OTID)
      */
     public void setRoot(OTID rootId) throws Exception
     {
         // Do nothing
     }
 
     /* (non-Javadoc)
      * @see org.concord.otrunk.datamodel.OTDatabase#getRoot()
      */
     public OTDataObject getRoot() throws Exception
     {
         // Do nothing
         return null;
     }
 
     /* (non-Javadoc)
      * @see org.concord.otrunk.datamodel.OTDatabase#createDataObject()
      */
     public OTDataObject createDataObject() throws Exception
     {        
         // in this case we need to create a new state object and wrap it
         OTUserDataObject userDataObject = new OTUserDataObject(null, this);
         OTDataObject childObject = stateDb.createDataObject();
         userDataObject.setStateObject(childObject);
         //System.out.println("v3. " + userDataObject.getGlobalId());
         return userDataObject;
     }
 
     /* (non-Javadoc)
      * @see org.concord.otrunk.datamodel.OTDatabase#createDataObject(org.concord.framework.otrunk.OTID)
      */
     public OTDataObject createDataObject(OTID id) throws Exception
     {
         // in this case we need to create a new state object and wrap it
         OTUserDataObject userDataObject = new OTUserDataObject(null, this);
         OTDataObject childObject = stateDb.createDataObject(id);
         userDataObject.setStateObject(childObject);
         //System.out.println("v3. " + userDataObject.getGlobalId());
         return userDataObject;
     }
 
     public OTDataObject getStateObject(OTDataObject template)
     {
         OTDataObject doObject = map.getStateObject(template, stateDb);
         return doObject;
     }
     
     public OTDataObject createStateObject(OTDataObject template)
     {
         return map.createStateObject(template, stateDb);        
     }
     
     /**
      * FIXME:  This method uses the state database to determine whether
      * an object can be directly changed or it needs to be wrapped by 
      * a template (user object) and just that object is changed. 
      * This should not be dependent on the state database because in some
      * cases there will not be a separate database just for the state 
      * objects.  So in this case there should be another way to know if
      * an object should be directly changed when it is access through
      * this database.  The reference map should probably be used for 
      * this.
      * 
      * @see org.concord.otrunk.datamodel.OTDatabase#getOTDataObject(org.concord.otrunk.datamodel.OTDataObject, org.concord.framework.otrunk.OTID)
      */
     public OTDataObject getOTDataObject(OTDataObject dataParent, OTID childId)
             throws Exception
     {
     	if(childId instanceof OTRelativeID) {
     		OTID childRootId = ((OTRelativeID)childId).getRootId();
     		if(childRootId.equals(getDatabaseId()))
     			childId = ((OTRelativeID)childId).getRelativeId();
     	}
         OTUserDataObject userDataObject = (OTUserDataObject)userDataObjectMap.get(childId);
         if(userDataObject != null) {
         	//System.out.println("v1. " + userDataObject.getGlobalId());
             return userDataObject;
         }
         
         userDataObject = (OTUserDataObject)mappedIdCache.get(childId);
         if(userDataObject != null) {
         	//System.out.println("v2. " + userDataObject.getGlobalId());
             return userDataObject;
         }
         
         
         if(stateDb.contains(childId)) {
             // the requested object is in the creationDb
             // this object might have references. so we need to 
             // wrap it so the returned data object has us as
             // the database
             userDataObject = new OTUserDataObject(null, this);
             OTDataObject childObject = stateDb.getOTDataObject(null, childId);
             userDataObject.setStateObject(childObject);
         	//System.out.println("v3. " + userDataObject.getGlobalId());
             return userDataObject;
         }
         
         // this object isn't in the creationDb and we haven't accessed
         // it before so we need to make a new one
         OTDataObject templateObject = rootDb.getOTDataObject(null, childId);
         if(templateObject == null) {
             // we are in a bad state here.  there was a request for a child
             // object that we can't find.  Instead of making a bogus user
             // object lets throw a runtime exception
         	System.err.println("can't find user object: " + childId);
         	return null;
             //throw new RuntimeException("can't find user object: " + childId);
         }
          userDataObject = new OTUserDataObject(templateObject, this);
 
         mappedIdCache.put(childId, userDataObject);
         userDataObjectMap.put(userDataObject.getGlobalId(), userDataObject);
         
         // System.err.println("created userDataObject template-id: " + childId + 
         //         " userDataObject-id: " + userDataObject.getGlobalId());
         
     	//System.out.println("v5. " + userDataObject.getGlobalId());
         return userDataObject;
     }
     
     /* (non-Javadoc)
      * @see org.concord.otrunk.datamodel.OTDatabase#contains(org.concord.framework.otrunk.OTID)
      */
     public boolean contains(OTID id)
     {
         // we only contain one id which is our database id 
         // this id will be used by OTUserDataObject in their
         // globalIds
         
         return id.equals(databaseId);
     }
 
     /* (non-Javadoc)
      * @see org.concord.otrunk.datamodel.OTDatabase#close()
      */
     public void close()
     {
         // do nothing
 
     }
 
 }

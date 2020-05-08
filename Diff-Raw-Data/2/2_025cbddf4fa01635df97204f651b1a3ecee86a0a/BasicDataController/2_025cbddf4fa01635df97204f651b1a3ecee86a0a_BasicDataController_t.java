 /*
  * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 package org.opensubsystems.core.logic;
 
 import java.rmi.RemoteException;
 
 import org.opensubsystems.core.data.DataObject;
 import org.opensubsystems.core.error.OSSException;
 
 /**
  * Interface that groups basic operations to perform with data objects by the 
  * application. Each data object needs to be somehow created and removed (if not 
  * for anything else at least for automated tests). 
  *
  * @author bastafidli
  */
 public interface BasicDataController extends DataController
 {
    /**
     * Create data object.
     *
     * @param  data - data object to create
     * @return DataObject - newly created data object, null if user doesn't have  
     *                      access to that data object granted
     * @throws OSSException - an error has occurred 
     * @throws RemoteException - required since this method can be called remotely
     */
    DataObject create(
       DataObject data
    ) throws OSSException,
             RemoteException;
 
    /**
     * Delete data object. This method will succeed only if the data object 
     * identified by specified id exists in the current domain, which is a domain 
     * identified by 
     * CallContext.getInstance().getCurrentDomainId(). 
     * If the object doesn't exist in the current domain, this method should 
     * throw an exception and shouldn't delete anything. If the client needs to 
     * delete data object in a different domain than the current one, it needs to 
    * provide its own specific interface. 
     *
     * @param lId - id of the data object to delete
     * @throws OSSException - an error has occurred 
     * @throws RemoteException - required since this method can be called remotely
     */
    void delete(
       long lId
    ) throws OSSException,
             RemoteException;
 }

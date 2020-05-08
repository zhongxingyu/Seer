 /**
  * @file   IFedoraAdministration.java
  * @brief  interface for interacting with the Fedora Commons repository
  */
 
 package dk.dbc.opensearch.common.fedora;
 
 /**
    This file is part of opensearch.
    Copyright © 2009, Dansk Bibliotekscenter a/s,
    Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
    opensearch is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    opensearch is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import dk.dbc.opensearch.xsd.DigitalObject;
 
 
public interface IFedoraAdm
 {
     /**
      * method to delete DigitalObjects for good, based on their pids
      * @param pids, the list of pids to be removed
      * @param start tells what the minimum date should be to qualify for removeal, optional
      * @param end tells what the maximum date should be to qualify for removeal, optinal
      * @return true if all specified DigitalObjects are removed from the base
      */
     public boolean removeDOsWithPids( ArrayList<String> pids, Date start, Date end );
 
     /**
      * method to delete DigitalObjects for good, based on submitter, format, date, 
      * @param submitter, the owner of the objects, 
      * @param format, the format of the content of the DOs to be removed, optional
      * @param start, the minimum date to qualify for removal optional
      * @param end, the maximum date to qualify for removal, optional
      * @return true if all the specified DOs was removed
      */
     public boolean removeDOs( String submitter, String format, Date start, Date end );
 
     /**
      * method for setting the delete flag on DigitalObjects
      * @param pids, the list of pids to be removed
      * @param since tells what the minimum date should be to qualify for removeal, optional
      * @param until tells what the maximum date should be to qualify for removeal, optinal
      * @return true if all specified DigitalObjects are hidden
      */
     public boolean hideDOsWithPids( ArrayList<String> pids, Date start, Date end );
 
     /**
      * method for getting the pids of DigitalObjects
      * @param submitter, the owner of the objects, 
      * @param format, the format of the content of the DOs to get pid from, optional
      * @param start, the minimum date to qualify for pid retrieval, optional
      * @param end, the maximum date to qualify for pid retrieval, optional
      * @return ArrayList<String> containing the pids of the specified DOs
      * */
     public ArrayList<String> getDOPids( String submitter, String format, Date start, Date end );
 
     /**
      * method for getting DigitalObjects based on their pids
      * @param pidList contains the pids of the DOs to be retrieved
      * @return ArrayList<DigitalObject> containing the specified DOs
      */
     public ArrayList<DigitalObject> getDOsWithPids( ArrayList<String> pidList);
 
     /**
      * method for getting DigitalObjects based on submitter, format and a date interval
      * @param submitter, the owner of the objects, 
      * @param format, the format of the content of the DOs to be retrieved, optional
      * @param start, the minimum date to qualify for retrieval, optional
      * @param end, the maximum date to qualify for retrieval, optional
      * @return an ArrayList<DigitalObject> of the DOs matching the arguments
      */
     public ArrayList<DigitalObject> getDOs( String submitter, String format, Date start, Date end );
 
     /**
      * method for modifying a DigitalObject with a certain pid
      * @param pid specifies the pid to search for
      * @param args is a list of modifications and the arguments belonging to them 
      * to be performed on the DO \todo: how is this done the best way? Bug 8901
      * @return true if the modifications succeded
      */
     public boolean modifyDOsWithPid( ArrayList<String> pids, ArrayList<String> args ); 
     
     /**
      * method for modifying a DigitalObject with a certain pid
      * @param pid specifies the pid to search for
      * @param args is a list of modifications and the arguments belonging to them 
      * to be performed on the DO \todo: how is this done the best way? Bug 8901
      * @param start, the minimum date to have to qualify for modification
      * @param end, the maximum date to have to qualify for modification
      * @return true if the modifications succeded
      */
     public boolean modifyDOs( String submitter, String format , ArrayList<String> args );
 
     /**
      * method for storing a DigitalObject in the Fedora base
      * @param theDO the DigitalObject to store
      * @return true if the DigitalObject is stored
      */
     public boolean storeDO( DigitalObject theDO );
 }

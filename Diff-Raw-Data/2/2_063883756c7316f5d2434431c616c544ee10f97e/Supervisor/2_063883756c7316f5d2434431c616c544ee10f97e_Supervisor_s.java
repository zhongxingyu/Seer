 /*
  * Supervisor.java
  *
  * Version: $Revision$
  *
  * Date: $Date$
  *
  * Copyright (c) 2002-2005, Hewlett-Packard Company and Massachusetts
  * Institute of Technology.  All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * - Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * - Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * - Neither the name of the Hewlett-Packard Company nor the name of the
  * Massachusetts Institute of Technology nor the names of their
  * contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
  * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
  * DAMAGE.
  */
 
 package org.dspace.eperson;
 
 import java.lang.StringBuffer;
 
 import java.sql.SQLException;
 
 import org.apache.log4j.Logger;
 
 import org.dspace.authorize.AuthorizeException;
 import org.dspace.authorize.ResourcePolicy;
 import org.dspace.content.Bitstream;
 import org.dspace.content.Bundle;
 import org.dspace.content.Item;
 import org.dspace.content.WorkspaceItem;
 import org.dspace.core.Constants;
 import org.dspace.core.Context;
 import org.dspace.core.LogManager;
 import org.dspace.eperson.Group;
 import org.dspace.storage.rdbms.TableRow;
 import org.dspace.storage.rdbms.TableRowIterator;
 import org.dspace.storage.rdbms.DatabaseManager;
 
 /**
  * Class to represent the supervisor, primarily for use in applying supervisor
  * activities to the database, such as setting and unsetting supervision
  * orders and so forth.
  *
  * @author  Richard Jones
  * @version $Revision$
  */
 public class Supervisor {
     
     /** log4j category */
     private static Logger log = Logger.getLogger(Supervisor.class);
     
     /** value to use for no policy set */
     public static int POLICY_NONE = 0;
     
     /** value to use for editor policies */
     public static int POLICY_EDITOR = 1;
     
     /** value to use for observer policies */
     public static int POLICY_OBSERVER = 2;
     
     /** Creates a new instance of Supervisor */
     private Supervisor() 
     {
     }
     
     /**
      * finds out if there is a supervision order that matches this set
      * of values
      *
      * @param context   the context this object exists in
      * @param wsItemID  the workspace item to be supervised
      * @param groupID   the group to be doing the supervising
      *
      * @return boolean  true if there is an order that matches, false if not
      */
     public static boolean isOrder(Context context, int wsItemID, int groupID)
         throws SQLException
     {
         String query = "SELECT epersongroup2workspaceitem.* " +
                        "FROM epersongroup2workspaceitem " +
                        "WHERE epersongroup2workspaceitem.eperson_group_id = "
                        + groupID +
                        " AND epersongroup2workspaceitem.workspace_item_id = "
                       + wsItemID + ";";
         
         TableRowIterator tri = DatabaseManager.query(context, 
                                     "epersongroup2workspaceitem", 
                                     query);
         
         if (tri.hasNext())
         {
             return true;
         } 
         else
         {
             return false;
         }
     }
     
     /**
      * removes the requested group from the requested workspace item in terms
      * of supervision.  This also removes all the policies that group has
      * associated with the item
      * 
      * @param context   the context this object exists in
      * @param wsItemID  the ID of the workspace item
      * @param groupID   the ID of the group to be removed from the item
      */
     public static void remove(Context context, int wsItemID, int groupID)
         throws SQLException, AuthorizeException
     {
         // get the workspace item and the group from the request values
         WorkspaceItem wsItem = WorkspaceItem.find(context, wsItemID);
         Group group = Group.find(context, groupID);
         
         // remove the link from the supervisory database
         String query = "DELETE FROM epersongroup2workspaceitem " +
                        "WHERE workspace_item_id = " + wsItemID +
                        " AND eperson_group_id = " + groupID;
         
         DatabaseManager.updateQuery(context, query);
         
         // get the item and have it remove the policies for the group
         Item item = wsItem.getItem();
         item.removeGroupPolicies(group);
     }
     
     /**
      * removes redundant entries in the supervision orders database
      * 
      * @param context   the context this object exists in
      */
     public static void removeRedundant(Context context)
         throws SQLException
     {
         // this horrid looking query tests to see if there are any groups or
         // workspace items which match up to the ones in the linking database
         // table.  If there aren't, we know that the link is out of date, and 
         // it can be deleted.
         String query = "DELETE FROM epersongroup2workspaceitem " +
                        "WHERE NOT EXISTS ( " +
                        "SELECT 1 FROM workspaceitem WHERE workspace_item_id " +
                        "= epersongroup2workspaceitem.workspace_item_id " +
                        ") OR NOT EXISTS ( " +
                        "SELECT 1 FROM epersongroup WHERE eperson_group_id " +
                        "= epersongroup2workspaceitem.eperson_group_id " +
                        ")";
         
         DatabaseManager.updateQuery(context, query);
     }
     
     /**
      * adds a supervision order to the database
      * 
      * @param context   the context this object exists in
      * @param groupID   the ID of the group which will supervise
      * @param wsItemID  the ID of the workspace item to be supervised
      * @param policy    String containing the policy type to be used
      */
     public static void add(Context context, int groupID, int wsItemID, int policy)
         throws SQLException, AuthorizeException
     {
         // make a table row in the database table, and update with the relevant
         // details
         TableRow row = DatabaseManager.create(context, 
                             "epersongroup2workspaceitem");
         row.setColumn("workspace_item_id", wsItemID);
         row.setColumn("eperson_group_id", groupID);
         DatabaseManager.update(context,row);
         
         // If a default policy type has been requested, apply the policies using
         // the DSpace API for doing so
         if (policy != POLICY_NONE)
         {
             WorkspaceItem wsItem = WorkspaceItem.find(context, wsItemID);
             Item item = wsItem.getItem();
             Group group = Group.find(context, groupID);
             
             // "Editor" implies READ, WRITE, ADD permissions
             // "Observer" implies READ permissions
             if (policy == POLICY_EDITOR)
             {
                 ResourcePolicy r = ResourcePolicy.create(context);
                 r.setResource(item);
                 r.setGroup(group);
                 r.setAction(Constants.READ);
                 r.update();
                 
                 r = ResourcePolicy.create(context);
                 r.setResource(item);
                 r.setGroup(group);
                 r.setAction(Constants.WRITE);
                 r.update();
                 
                 r = ResourcePolicy.create(context);
                 r.setResource(item);
                 r.setGroup(group);
                 r.setAction(Constants.ADD);
                 r.update();
                 
             } 
             else if (policy == POLICY_OBSERVER)
             {
                 ResourcePolicy r = ResourcePolicy.create(context);
                 r.setResource(item);
                 r.setGroup(group);
                 r.setAction(Constants.READ);
                 r.update();
             }
         }
     }
     
 }

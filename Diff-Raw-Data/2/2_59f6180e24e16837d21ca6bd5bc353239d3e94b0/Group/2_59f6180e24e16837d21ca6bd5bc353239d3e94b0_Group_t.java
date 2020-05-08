 /**
  *  CGateInterface - A library to allow interaction with Clipsal C-Gate.
  *  Copyright (C) 2008  Dave Oxley <dave@daveoxley.co.uk>.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.daveoxley.cbus;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 public class Group extends CGateObject
 {
     private Application application;
 
     private int group_id;
 
     private boolean on_network;
 
     Group(CGateSession cgate_session, Application application, String cgate_response, boolean tree_resp)
     {
         super(cgate_session);
         this.application = application;
         this.on_network = tree_resp;
         if (tree_resp)
             this.group_id = getGroupID(application.getNetwork(), cgate_response);
         else
         {
             int index = cgate_response.indexOf("=");
             this.group_id = Integer.parseInt(cgate_response.substring(index + 1));
         }
     }
 
     @Override
     protected String getKey()
     {
         return String.valueOf(group_id);
     }
 
     @Override
     public CGateObject getCGateObject(String address) throws CGateException
     {
         throw new IllegalArgumentException("There are no CGateObjects owned by a Group");
     }
 
     @Override
     public String getAddress()
     {
         return "//" + getNetwork().getProjectName() + "/" + getNetwork().getNetworkID() +
                 "/" + application.getApplicationID() + "/" + getGroupID();
     }
 
     static Group getOrCreateGroup(CGateSession cgate_session, Network network, String response) throws CGateException
     {
         String application_type = Network.getApplicationType(network, response);
         int group_id = getGroupID(network, response);
 
         if (!application_type.equals("p"))
         {
             Application application = network.getApplication(Integer.parseInt(application_type));
             if (application != null)
             {
                 Group group = (Group)application.getCachedObject("group", String.valueOf(group_id));
                 if (group == null)
                 {
                     group = new Group(cgate_session, application, response, true);
                     application.cacheObject("group", group);
                 }
                 return group;
             }
         }
         return null;
     }
 
     static Group getOrCreateGroup(CGateSession cgate_session, Application application, String response) throws CGateException
     {
         int index = response.indexOf("=");
         String group_id = response.substring(index + 1);
 
         if (group_id.equals("255"))
             return null;
 
         Group group = (Group)application.getCachedObject("group", group_id);
         if (group == null)
         {
             group = new Group(cgate_session, application, response, false);
             application.cacheObject("group", group);
         }
         return group;
     }
 
     static int getGroupID(Network network, String response)
     {
         String application_type = Network.getApplicationType(network, response);
         String application_address = network.getAddress() + "/" + application_type + "/";
         int index = response.indexOf(application_address);
         int unit_index = response.indexOf(" ", index + 1);
         return Integer.parseInt(response.substring(index + application_address.length(), unit_index).trim());
     }
 
     /**
      *
      * @return
      */
     public int getGroupID()
     {
         return group_id;
     }
 
     /**
      *
      * @return
      */
     private Network getNetwork()
     {
         return application.getNetwork();
     }
 
     public String getName() throws CGateException
     {
         String address = getAddress() + "/TagName";
         ArrayList<String> resp_array = getCGateSession().sendCommand("dbget " + address).toArray();
         return responseToMap(resp_array.get(0), true).get(address);
     }
 
     /**
      * Issue a <code>on //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
      * 
      * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
      *      <i>C-Gate Server Guide 4.3.79</i></a>
      * @throws CGateException
      */
     public Response on() throws CGateException
     {
         return getCGateSession().sendCommand("on " + getAddress());
     }
 
     /**
      * Issue a <code>off //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
      * 
      * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
      *      <i>C-Gate Server Guide 4.3.77</i></a>
      * @throws CGateException
      */
     public Response off() throws CGateException
     {
         return getCGateSession().sendCommand("off " + getAddress());
     }
 
     /**
      * Issue a <code>ramp //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
      *
      * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
      *      <i>C-Gate Server Guide 4.3.100</i></a>
      * @param level
      * @param seconds
      * @throws CGateException
      */
     public Response ramp(int level, int seconds) throws CGateException
     {
         return getCGateSession().sendCommand("ramp " + getAddress() + " " + level + " " + seconds + "s");
     }
 
     /**
      * Issue a <code>get //PROJECT/NET_ID/GROUP_ID Level</code> to the C-Gate server.
      *
      * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
      *      <i>C-Gate Server Guide 4.3.44</i></a>
      * @throws CGateException
      */
     public int getLevel() throws CGateException
     {
         ArrayList<String> resp_array = getCGateSession().sendCommand("get " + getAddress() + " Level").toArray();
         String level_str = responseToMap(resp_array.get(0)).get("Level");
        return level_str == null ? 0 : Integer.valueOf(level_str);
     }
 }

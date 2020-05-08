 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
  *
  * This program is free software: you can redistribute it and/or modify
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
 package com.elasticgrid.rackspace.cloudservers;
 
 import com.elasticgrid.rackspace.BackupSchedule;
 import com.elasticgrid.rackspace.BackupSchedule.WeeklyBackup;
 import com.elasticgrid.rackspace.BackupSchedule.DailyBackup;
 import java.util.List;
 import java.util.Map;
 import java.net.InetAddress;
 
 /**
  * Rackspace Cloud Servers API.
  *
  * @author Jerome Bernard
  */
 public interface CloudServers {
 
     /**
      * Retrieve the list of servers (only IDs and names) associated with the Rackspace account.
      *
      * @return the list of servers
      * @throws CloudServersException
      */
     List<Server> getServers() throws CloudServersException;
 
     /**
      * Retrieve the list of servers (with details) associated with the Rackspace account.
      *
      * @return the list of servers
      * @throws CloudServersException
      */
     List<Server> getServersWithDetails() throws CloudServersException;
 
     /**
      * Retrieve the server details.
      *
      * @param serverID the ID of the server for which details should be retrieved
      * @return the server details
      * @throws CloudServersException
      */
     Server getServerDetails(int serverID) throws CloudServersException;
 
     /**
      * Retrieve server addresses.
      *
      * @param serverID the ID of the server for which addresses should be retrieved
      * @return the server addresses
      * @throws CloudServersException
      */
     Addresses getServerAddresses(int serverID) throws CloudServersException;
 
     /**
      * Retrieve public server addresses.
      *
      * @param serverID the ID of the server for which addresses should be retrieved
      * @return the server addresses
      * @throws CloudServersException
      */
     List<InetAddress> getServerPublicAddresses(int serverID) throws CloudServersException;
 
     /**
      * Retrieve private server addresses.
      *
      * @param serverID the ID of the server for which addresses should be retrieved
      * @return the server addresses
      * @throws CloudServersException
      */
     List<InetAddress> getServerPrivateAddresses(int serverID) throws CloudServersException;
 
     /**
      * Share an IP address to the specified server.
      *
      * @param serverID the ID of the server for which the IP should be shared
      * @param address  the IP¨address to share with the server
      * @throws CloudServersException
      */
     void shareAddress(int serverID, InetAddress address) throws CloudServersException;
 
     /**
      * Remove a shared IP address from the specified server.
      *
      * @param serverID the ID of the server for which the IP should be not be shared anymore
      * @param address  the IP¨address to stop sharing with the server
      * @throws CloudServersException
      */
     void unshareAddress(int serverID, InetAddress address) throws CloudServersException;
 
     /**
      * Provision a new server.
      *
      * @param name     the name of the server to create
      * @param imageID  the image from which the server should be created
      * @param flavorID the kind of hardware to use
      * @return the created server with precious information such as admin password for that server
      * @throws CloudServersException
      */
     Server createServer(String name, int imageID, int flavorID) throws CloudServersException;
 
     /**
      * Provision a new server.
      *
      * @param name     the name of the server to create
      * @param imageID  the image from which the server should be created
      * @param flavorID the kind of hardware to use
      * @param metadata the launch metadata
      * @return the created server with precious information such as admin password for that server
      * @throws CloudServersException
      */
     Server createServer(String name, int imageID, int flavorID, Map<String, String> metadata) throws CloudServersException;
 
     /**
      * Reboot the specified server.
      *
      * @param serverID the ID of the server to reboot
      * @throws CloudServersException
      * @see #rebootServer(int, RebootType)
      */
     void rebootServer(int serverID) throws CloudServersException;
 
     /**
      * Reboot the specified server.
      *
      * @param serverID the ID of the server to reboot
      * @param type     the type of reboot to perform
      * @throws CloudServersException
      * @see #rebootServer(int, RebootType)
      */
     void rebootServer(int serverID, RebootType type) throws CloudServersException;
 
     /**
      * Rebuild the specified server.
      *
      * @param serverID the ID of the server to rebuild
      * @throws CloudServersException
      * @see #rebuildServer(int, int)
      */
     void rebuildServer(int serverID) throws CloudServersException;
 
     /**
      * Rebuild the specified server from an different image than the one initially used.
      *
      * @param serverID the ID of the server to rebuild
      * @param imageID  the new image to use
      * @throws CloudServersException
      * @see #rebuildServer(int)
      */
     void rebuildServer(int serverID, int imageID) throws CloudServersException;
 
     /**
      * Resize the specified server.
      *
      * @param serverID the ID of the server to resize
      * @param flavorID the new flavor of hardware which should be used
      * @throws CloudServersException
      */
     void resizeServer(int serverID, int flavorID) throws CloudServersException;
 
     /**
      * Confirm a pending resize action.
      *
      * @param serverID the ID of the server for which the resize should be confirmed
      * @throws CloudServersException
      */
     void confirmResize(int serverID) throws CloudServersException;
 
     /**
      * Cancel and revert a pending resize action.
      *
      * @param serverID the ID of the server for which the resize should be cancelled
      * @throws CloudServersException
      */
     void revertResize(int serverID) throws CloudServersException;
 
     /**
      * Update the specified server's name and/or administrative password. This operation allows you to update the name
      * of the server and change the administrative password. This operation changes the name of the server in the Cloud
      * Servers system and does not change the server host name itself.
      *
      * @param serverID the ID of the server to update
      * @param name     the new name for the server
      * @throws CloudServersException
      */
     void updateServerName(int serverID, String name) throws CloudServersException;
 
     /**
      * Update the specified server's name and/or administrative password. This operation allows you to update the name
      * of the server and change the administrative password. This operation changes the name of the server in the Cloud
      * Servers system and does not change the server host name itself.
      *
      * @param serverID the ID of the server to update
      * @param password the new password
      * @throws CloudServersException
      */
     void updateServerPassword(int serverID, String password) throws CloudServersException;
 
     /**
      * Update the specified server's name and/or administrative password. This operation allows you to update the name
      * of the server and change the administrative password. This operation changes the name of the server in the Cloud
      * Servers system and does not change the server host name itself.
      *
      * @param serverID the ID of the server to update
      * @param name     the new name for the server
      * @param password the new password
      * @throws CloudServersException
      */
     void updateServerNameAndPassword(int serverID, String name, String password) throws CloudServersException;
 
     /**
      * Deletes a cloud server instance from the system
      *
      * @param serverID the ID of the server to delete
      * @throws CloudServersException
      */
     void deleteServer(int serverID) throws CloudServersException;
 
     /**
      * Return the limits for the Rackspace API account.
      *
      * @return the limits
      * @throws CloudServersException
      */
     Limits getLimits() throws CloudServersException;
 
     /**
      * Retrieve the list of flavors (only IDs and names) associated with the Rackspace account.
      *
      * @return the flavors
      * @throws CloudServersException
      */
     List<Flavor> getFlavors() throws CloudServersException;
 
     /**
      * Retrieve the list of flavors (with details) associated with the Rackspace account.
      *
      * @return the flavors
      * @throws CloudServersException
      */
     List<Flavor> getFlavorsWithDetails() throws CloudServersException;
 
     /**
      * Retrieve the flavor details.
      *
      * @param flavorID the ID of the flavor for which details should be retrieved
      * @return the flavor details
      * @throws CloudServersException
      */
     Flavor getFlavorDetails(int flavorID) throws CloudServersException;
 
     /**
      * Retrieve the list of images (only IDs and names) associated with the Rackspace account.
      *
      * @return the images
      * @throws CloudServersException
      */
     List<Image> getImages() throws CloudServersException;
 
     /**
      * Retrieve the list of images (with details) associated with the Rackspace account.
      *
      * @return the images
      * @throws CloudServersException
      */
     List<Image> getImagesWithDetails() throws CloudServersException;
 
     /**
      * Retrieve the image details.
      *
      * @param imageID the ID of the image for which details should be retrieved
      * @return the image details
      * @throws CloudServersException
      */
     Image getImageDetails(int imageID) throws CloudServersException;
 
     /**
      * Create a new image from a server.
      *
      * @param name     the name of the image to create
      * @param serverID the ID of the server whose content will be used for creating the image
      * @return the created image details
      * @throws CloudServersException
      */
     Image createImage(String name, int serverID) throws CloudServersException;
 
     /**
      * Retrieve the backup schedule for a server.
      *
      * @param serverID the ID of the server for which the backup schedule should be retrieved
      * @return the backup schedule
      * @throws CloudServersException
      */
     BackupSchedule getBackupSchedule(int serverID) throws CloudServersException;
 
     /**
      * Create or update backup schedule for a server.
      *
      * @param serverID the ID of the server for which the backup schedule should be created/updated
      * @param schedule the backup schedule
      * @throws CloudServersException
      */
     void scheduleBackup(int serverID, BackupSchedule schedule) throws CloudServersException;
 
     /**
      * Delete backup schedule for a server.
      *
      * @param serverID the ID of the server for which the backup schedule should be deleted
      * @throws CloudServersException
      */
     void deleteBackupSchedule(int serverID) throws CloudServersException;
 
//    List getSharedIPGroups() throws CloudServersException;
 
 }

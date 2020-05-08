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
 
 import com.elasticgrid.utils.rackspace.RackspaceUtils;
 import com.elasticgrid.rackspace.common.RackspaceException;
 import com.elasticgrid.rackspace.BackupSchedule;
 import com.elasticgrid.rackspace.BackupSchedule.WeeklyBackup;
 import com.elasticgrid.rackspace.BackupSchedule.DailyBackup;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 import java.io.IOException;
 import java.util.List;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 /**
  * Tests for the Rackspace Cloud Servers API.
  *
  * @author Jerome Bernard
  */
 public class CloudServersTest {
     private CloudServers api;
 
     @Test
     public void testListServersWhenNoneRunning() throws CloudServersException {
         List<Server> servers = api.getServers();
         assert servers != null;
         assert servers.size() == 0;
     }
 
     @Test(expectedExceptions = CloudServersException.class)
     public void testRetrieveServerDetailsOfNonExistingServer() throws CloudServersException {
         api.getServerDetails(123);
     }
 
 //    @Test(expectedExceptions = CloudServersException.class)
 //    public void testUpdateServerNameAndPasswordOnNonExistingServer() throws CloudServersException {
 //        api.updateServerNameAndPassword(123, "new-name", "new-password");
 //    }
 
 //    @Test
 //    public void testCreateServerWithInvalidParameters() throws CloudServersException {
 //        api.createServer("test", 123, 123);
 //    }
 
     @Test(expectedExceptions = CloudServersException.class)
     public void testDeleteNonExistingServer() throws CloudServersException {
         api.deleteServer(123);
     }
 
     /* WTF: I'm getting IP addresses I shouldn't have!!!!! There is clearly something wrong going on!!!!!! */
     /*
     @Test(expectedExceptions = CloudServersException.class)
     public void testGetServerAddressesForNonExistingServer() throws CloudServersException {
         Addresses addresses = api.getServerAddresses(123);
         assert addresses != null;
         assert addresses.getPublicAddresses() != null;
         assert addresses.getPublicAddresses().size() == 0;
         assert addresses.getPrivateAddresses() != null;
         assert addresses.getPrivateAddresses().size() == 0;
     }
     */
 
     /* WTF: I'm getting IP addresses I shouldn't have!!!!! There is clearly something wrong going on!!!!!! */
     /*
     @Test(expectedExceptions = CloudServersException.class)
     public void testGetServerPublicAddressesForNonExistingServer() throws CloudServersException {
         List<InetAddress> addresses = api.getServerPublicAddresses(123);
         assert addresses != null;
         assert addresses.size() == 0;
     }
     */
 
    @Test(expectedExceptions = CloudServersException.class)
     public void testGetServerPrivateAddressesForNonExistingServer() throws CloudServersException {
         List<InetAddress> addresses = api.getServerPrivateAddresses(123);
         assert addresses != null;
         assert addresses.size() == 0;
     }
 
 //    @Test
 //    public void testShareIpAddressWithNonExistingServer() throws CloudServersException, UnknownHostException {
 //        api.shareAddress(123, InetAddress.getLocalHost());
 //    }
 
     @Test(expectedExceptions = CloudServersException.class)
     public void testRebootNonExistingServer() throws CloudServersException {
         api.rebootServer(123);
     }
 
     @Test(expectedExceptions = CloudServersException.class)
     public void rebuildNonExistingServer() throws CloudServersException {
         api.rebuildServer(123);
     }
 
     @Test(expectedExceptions = CloudServersException.class)
     public void resizeNonExistingServer() throws CloudServersException {
         api.resizeServer(123, 456);
     }
 
     @Test
     public void testLimits() throws CloudServersException {
         Limits limits = api.getLimits();
         assert limits != null;
         List<RateLimit> rateLimits = limits.getRateLimits();
         assert rateLimits.size() > 0;
         List<AbsoluteLimit> absoluteLimits = limits.getAbsoluteLimits();
         assert absoluteLimits.size() > 0;
     }
 
     @Test
     public void testRetrieveAllFlavors() throws CloudServersException {
         List<Flavor> flavors = api.getFlavors();
         assert flavors != null;
         assert flavors.size() > 0;
         for (Flavor f : flavors) {
             f = api.getFlavorDetails(f.getId());
             System.out.println("Flavor: " + f);
         }
     }
 
 //    @Test
 //    public void testRetrieveDetailsOfAllFlavors() throws CloudServersException {
 //        List<Flavor> flavors = api.getFlavorsWithDetails();
 //        assert flavors != null;
 //        assert flavors.size() > 0;
 //        for (Flavor f : flavors)
 //            System.out.println("Flavor: " + f);
 //    }
 
 //    @Test
 //    public void testRetrieveAllImages() throws CloudServersException {
 //        List<Image> images = api.getImages();
 //        assert images != null;
 //        assert images.size() > 0;
 //        for (Image image : images) {
 //            image = api.getImageDetails(image.getId());
 //            System.out.println("Image: " + image);
 //        }
 //    }
 
 //    @Test
 //    public void testRetrieveDetailsOfAllImages() throws CloudServersException {
 //        List<Image> images = api.getImagesWithDetails();
 //        assert images != null;
 //        assert images.size() > 0;
 //        for (Image image : images)
 //            System.out.println("Image: " + image);
 //    }
 
     @Test(expectedExceptions = CloudServersException.class)
     public void testCreateImageWithInvalidParameters() throws CloudServersException {
         api.createImage("test", 123);
     }
 
     @Test(expectedExceptions = CloudServersException.class)
     public void testBackupScheduleOfNonExistingServer() throws CloudServersException {
         BackupSchedule backupSchedule = api.getBackupSchedule(123);
         System.out.println("Backup schedule: " + backupSchedule);
     }
 
     @Test(expectedExceptions = CloudServersException.class)
     public void testScheduleBackupForNonExistingServer() throws CloudServersException {
         api.scheduleBackup(123, new BackupSchedule(true, WeeklyBackup.SUNDAY, DailyBackup.H_0400_0600));
     }
 
     @Test(expectedExceptions = CloudServersException.class)
     public void testDeleteBackupScheduleForNonExistingServer() throws CloudServersException {
         api.deleteBackupSchedule(123);
     }
 
     @BeforeTest
     private void setupAPI() throws RackspaceException, IOException {
         String username = RackspaceUtils.getUsername();
         String apiKey = RackspaceUtils.getApiKey();
         api = new XMLCloudServers(username, apiKey);
     }
 
     /*
     static {
         System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
         System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
         System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
         System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "info");
         System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "info");
         System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.impl.client.ClientParamsStack", "info");
     }
     */
 }

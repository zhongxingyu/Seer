 /**
  * Copyright (C) 2007-2008 Elastic Grid, LLC.
  * 
  * Licensed under the GNU Lesser General Public License, Version 3.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.gnu.org/licenses/lgpl-3.0.html
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.elasticgrid.grid.ec2;
 
 import com.elasticgrid.amazon.ec2.EC2Instantiator;
 import com.elasticgrid.amazon.ec2.InstanceType;
 import com.elasticgrid.amazon.ec2.EC2GridLocator;
 import com.elasticgrid.grid.GridManager;
 import com.elasticgrid.model.Grid;
 import com.elasticgrid.model.GridAlreadyRunningException;
 import com.elasticgrid.model.GridNotFoundException;
 import com.elasticgrid.model.NodeProfile;
 import com.elasticgrid.model.GridException;
 import com.elasticgrid.model.ec2.EC2Grid;
 import com.elasticgrid.model.ec2.EC2Node;
 import com.elasticgrid.model.ec2.impl.EC2GridImpl;
 import static java.lang.String.format;
 import java.rmi.RemoteException;
 import java.util.List;
 import java.util.Arrays;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 public class EC2GridManager implements GridManager<EC2Grid> {
     private EC2Instantiator ec2;
     private EC2GridLocator locator;
     private String keyName;
     private String ami32 = "", ami64 = "";
     private static final Logger logger = Logger.getLogger(EC2GridManager.class.getName());
 
     public void startGrid(String gridName) throws GridException, RemoteException {
         startGrid(gridName, 1);
     }
 
     public void startGrid(String gridName, int size) throws GridException, RemoteException {
         logger.log(Level.INFO, "Starting grid ''{0}'' with {1} node(s)", new Object[] { gridName, size });
         // ensure the grid is not already running
         Grid grid = grid(gridName);
        logger.info("Grid is " + grid.isRunning());        
         if (grid != null && grid.isRunning()) {
             throw new GridAlreadyRunningException(grid);
         }
         // todo: start the grid accordingly to the parameters
         InstanceType instanceType = InstanceType.SMALL;
         String ami = null;
         switch (instanceType) {
             case SMALL:
                 ami = ami32;
                 break;
             case MEDIUM_HIGH_CPU:
                 ami = ami32;
                 break;
             case LARGE:
                 ami = ami64;
                 break;
             case EXTRA_LARGE:
                 ami = ami64;
                 break;
             case EXTRA_LARGE_HIGH_CPU:
                 ami = ami64;
                 break;
             default:
                 throw new IllegalArgumentException(format("Unexpected Amazon EC2 instance type '%s'", instanceType.getName()));
         }
         String userData = "";
         for (int i = 0; i < size; i++) {
             // first first two nodes are {@link NodeProfile.MONITOR}s
             // and all the other ones are {@link NodeProfile.AGENT}s
             NodeProfile profile = i < 2 ? NodeProfile.MONITOR : NodeProfile.AGENT;
             // build the groups list
             List<String> groups = Arrays.asList(gridName, profile.toString());
             // start the node
             logger.log(Level.INFO, "Starting 1 EC2 instance from AMI {0} using groups {1}",
                                    new Object[] { ami, groups.toString() });
             ec2.startInstances(ami, 1, 1, groups, userData, keyName, true, instanceType);
         }
     }
 
     public void stopGrid(String gridName) throws GridException, RemoteException {
         // locate all nodes in the grid
         List<EC2Node> nodes = locator.findNodes(gridName);
         // stop each node one by one
         for (EC2Node node : nodes) {
             ec2.shutdownInstance(node.getInstanceID());
         }
     }
 
     public EC2Grid grid(String name) throws RemoteException, GridException {
         EC2Grid grid = new EC2GridImpl();
         List<EC2Node> nodes = locator.findNodes(name);
         if (nodes == null)
             return grid;
         else
             return (EC2Grid) grid.name(name).addNodes(nodes);
     }
 
     public void resizeGrid(String gridName, int newSize) throws GridNotFoundException, RemoteException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void setEc2(EC2Instantiator ec2) {
         this.ec2 = ec2;
     }
 
     public void setLocator(EC2GridLocator locator) {
         this.locator = locator;
     }
 
     public void setKeyName(String keyName) {
         this.keyName = keyName;
     }
 
     public void setAmi32(String ami32) {
         this.ami32 = ami32;
     }
 
     public void setAmi64(String ami64) {
         this.ami64 = ami64;
     }
 }

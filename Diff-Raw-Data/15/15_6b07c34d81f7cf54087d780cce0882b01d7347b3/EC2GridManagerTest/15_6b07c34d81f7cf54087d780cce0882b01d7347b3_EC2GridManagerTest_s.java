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
 
 import org.testng.annotations.Test;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeTest;
 import static org.easymock.EasyMock.*;
 import com.elasticgrid.amazon.ec2.EC2Instantiator;
 import com.elasticgrid.amazon.ec2.InstanceType;
 import com.elasticgrid.amazon.ec2.EC2GridLocator;
 import com.elasticgrid.model.GridNotFoundException;
 import com.elasticgrid.model.GridAlreadyRunningException;
 import com.elasticgrid.model.GridException;
 import com.elasticgrid.model.NodeProfile;
 import com.elasticgrid.model.ec2.EC2Node;
 import com.elasticgrid.model.ec2.impl.EC2NodeImpl;
 
 import java.rmi.RemoteException;
 import java.util.Collections;
 import java.util.Arrays;
 import java.util.ArrayList;
 
 public class EC2GridManagerTest {
     private EC2GridManager gridManager;
     private EC2Instantiator mockEC2;
     private EC2GridLocator mockLocator;
 
     @Test(expectedExceptions = GridAlreadyRunningException.class)
     public void testStartingARunningGrid() throws GridException, RemoteException {
//        expect(mockEC2.startInstances("", 1, 1, Arrays.asList("test", "eg-monitor"), "", null, true, InstanceType.SMALL))
//                .andReturn(null);
         expect(mockLocator.findNodes("test"))
                .andReturn(Arrays.asList(new EC2NodeImpl(NodeProfile.MONITOR).instanceID("123"))).times(1);
         replay(mockEC2, mockLocator);
         gridManager.startGrid("test");
         gridManager.startGrid("test");
     }
 
     @BeforeTest
     public void setUpGridManager() {
         gridManager = new EC2GridManager();
         mockEC2 = createMock(EC2Instantiator.class);
         mockLocator = createMock(EC2GridLocator.class);
         gridManager.setEc2(mockEC2);
         gridManager.setLocator(mockLocator);
     }
 
     @AfterMethod
     public void verifyMocks() {
         verify(mockEC2, mockLocator);
         reset(mockEC2, mockLocator);
     }
 }

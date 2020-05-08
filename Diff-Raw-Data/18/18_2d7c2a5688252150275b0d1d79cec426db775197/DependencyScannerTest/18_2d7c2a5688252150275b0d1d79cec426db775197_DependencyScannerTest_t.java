 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.extensions.scripting.dependencyScan;
 
 import org.apache.myfaces.scripting.core.dependencyScan.DefaultDependencyScanner;
 import org.junit.Test;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 public class DependencyScannerTest {
 
     @Test
     public void testScan() {
         Set<String> whiteList = new HashSet<String>();
 
         whiteList.add("org.apache.xxx");
         whiteList.add("org.apache.myfaces.extensions.scripting");
 
        //long before = System.currentTimeMillis();
 
         Set<String> retVal = (new DefaultDependencyScanner()).fetchDependencies("org.apache.myfaces.extensions.scripting.dependencyScan.probes.Probe", whiteList);
        //long after = System.currentTimeMillis();

        //System.out.println(after-before);

         assertTrue(retVal.size() > 0);
 
         assertFalse(retVal.contains("java.lang.String"));
 
         assertTrue(retVal.contains("org.apache.myfaces.extensions.scripting.dependencyScan.probes.Probe2"));
         assertTrue(retVal.contains("org.apache.myfaces.extensions.scripting.dependencyScan.probes.Probe3"));
         assertTrue(retVal.contains("org.apache.myfaces.extensions.scripting.dependencyScan.probes.Probe4"));
         assertTrue(retVal.contains("org.apache.myfaces.extensions.scripting.dependencyScan.probes.ProbeParent"));
 
     }
 }

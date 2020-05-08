 /*
  * Copyright (c) 2010, Soar Technology, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * 
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  * 
  * * Neither the name of Soar Technology, Inc. nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without the specific prior written permission of Soar Technology, Inc.
  * 
  * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * Created on Jan 30, 2010
  */
 package com.soartech.simjr.core;
 
 import junit.framework.Test;
import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import com.soartech.simjr.SimJrPropsTest;
 import com.soartech.simjr.console.ConsoleManagerTest;
 import com.soartech.simjr.controllers.SegmentFollowerTest;
 import com.soartech.simjr.controllers.SegmentInfoTest;
 import com.soartech.simjr.scenario.EntityElementListTest;
 import com.soartech.simjr.scenario.EntityElementTest;
 import com.soartech.simjr.scenario.LocationElementTest;
 import com.soartech.simjr.scenario.MetadataElementTest;
 import com.soartech.simjr.scenario.ModelTest;
 import com.soartech.simjr.scenario.OrientationElementTest;
 import com.soartech.simjr.scenario.PointElementListTest;
 import com.soartech.simjr.scenario.TerrainElementTest;
 import com.soartech.simjr.scenario.TerrainImageElementTest;
 import com.soartech.simjr.scripting.DefaultScriptProviderManagerTest;
 import com.soartech.simjr.scripting.ResourceScriptProviderTest;
 import com.soartech.simjr.scripting.ScriptRunnerTest;
 import com.soartech.simjr.services.DefaultServiceManagerTest;
 import com.soartech.simjr.sim.AbstractEntityCapabilityTest;
 import com.soartech.simjr.sim.DefaultEntityPrototypeTest;
 import com.soartech.simjr.sim.EntityPropertyAdaptersTest;
 import com.soartech.simjr.sim.EntityPrototypeDatabaseTest;
 import com.soartech.simjr.sim.EntityToolsTest;
 import com.soartech.simjr.sim.LazyGeodeticPropertyTest;
 import com.soartech.simjr.sim.LazyMgrsPropertyTest;
 import com.soartech.simjr.sim.SimpleTerrainTest;
 import com.soartech.simjr.sim.SimulationTest;
 import com.soartech.simjr.sim.SimulationThreadTest;
 import com.soartech.simjr.sim.StrictSimulationTickPolicyTest;
 import com.soartech.simjr.sim.entities.AbstractEntityTest;
 import com.soartech.simjr.sim.entities.DisableRadarWhenDestroyedTest;
 import com.soartech.simjr.sim.entities.EntityVisibleRangeTest;
 import com.soartech.simjr.sim.entities.MissileTest;
 import com.soartech.simjr.sim.entities.VehicleTest;
 import com.soartech.simjr.util.ExtendedPropertiesTest;
 import com.soartech.simjr.util.FileToolsTest;
 import com.soartech.simjr.weapons.BombWeaponTest;
 import com.soartech.simjr.weapons.MissileWeaponTest;
 import com.soartech.simjr.weapons.WeaponTest;
 
 /**
  * Test suite for this plugin. Add all test classes here so they'll be included
  * when the com.soartech.simjr.test master test suite is run.
  * 
  * @author ray
  */
 public class AllTests
 {
     public static Test suite()
     {
    	final Class<? extends TestCase>[] tests = new Class[] {
             SimJrPropsTest.class,
             ConsoleManagerTest.class,
             SegmentFollowerTest.class,
             SegmentInfoTest.class,
             EntityElementListTest.class,
             EntityElementTest.class,
             LocationElementTest.class,
             MetadataElementTest.class,
             ModelTest.class,
             OrientationElementTest.class,
             PointElementListTest.class,
             TerrainElementTest.class,
             TerrainImageElementTest.class,
             
             DefaultScriptProviderManagerTest.class,
             ResourceScriptProviderTest.class,
             ScriptRunnerTest.class,
             
             DefaultServiceManagerTest.class,
             
             AbstractEntityCapabilityTest.class,
             DefaultEntityPrototypeTest.class,
             EntityPropertyAdaptersTest.class,
             EntityPrototypeDatabaseTest.class,
             EntityToolsTest.class,
             LazyGeodeticPropertyTest.class,
             LazyMgrsPropertyTest.class,
             SimpleTerrainTest.class,
             SimulationTest.class,
             SimulationThreadTest.class,
             StrictSimulationTickPolicyTest.class,
             
             AbstractEntityTest.class,
             DisableRadarWhenDestroyedTest.class,
             EntityVisibleRangeTest.class,
             MissileTest.class,
             VehicleTest.class,
             
             ExtendedPropertiesTest.class,
             FileToolsTest.class,
             
             BombWeaponTest.class,
             MissileWeaponTest.class,
             WeaponTest.class
         };
         return new TestSuite(tests, AllTests.class.getName());
     }
 }

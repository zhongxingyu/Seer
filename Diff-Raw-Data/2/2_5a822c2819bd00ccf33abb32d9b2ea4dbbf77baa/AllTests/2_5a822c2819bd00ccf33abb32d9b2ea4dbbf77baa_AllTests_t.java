 /*
  * Copyright 2011 by the CloudRAID Team, see AUTHORS for more details.
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
 
  * http://www.apache.org/licenses/LICENSE-2.0
 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package de.dhbw.mannheim.cloudraid;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 /**
  * @author Markus Holtermann
  */
 @RunWith(Suite.class)
 @SuiteClasses({ de.dhbw.mannheim.cloudraid.util.TestConfig.class,
 		de.dhbw.mannheim.cloudraid.jni.TestRaidAccessInterface.class,
 		de.dhbw.mannheim.cloudraid.fs.TestFileSystemUtilities.class,
 		de.dhbw.mannheim.cloudraid.fs.TestFileLock.class,
		de.dhbw.mannheim.cloudraid.persistence.TestHSQLDatabaseConnector.class })
 public class AllTests {
 
 }

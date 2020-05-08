 /*
  * Copyright 2007 Sebastien Brunot (sbrunot@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.sourceforge.buildmonitor.monitor;
 
 import net.sourceforge.buildmonitor.monitors.BambooProperties;
 
 import java.io.IOException;
 import junit.framework.TestCase;
 
 /**
  * Unit tests for the RssFeedReader class.
  * @author vegarwe
  *
  */
 public class BambooPropertiesTest extends TestCase
 {
 	
 	public void testloadFromFileBase64Password()
 	{
 		BambooProperties bambooProperties = new BambooProperties();
 		try {
			System.setProperty("user.home", "/home/vegarwe/devel/build-monitor/src/test/resources/base64pass");
 			bambooProperties.loadFromFile();
 		} catch (IOException e) {}
 		assertEquals(bambooProperties.getPassword(), "testpassord");
 	}
 	
 	public void testloadFromFilePlaintextPassword()
 	{
 		BambooProperties bambooProperties = new BambooProperties();
 		try {
			System.setProperty("user.home", "/home/vegarwe/devel/build-monitor/src/test/resources/plainpass");
 			bambooProperties.loadFromFile();
 		} catch (IOException e) {}
 		assertEquals(bambooProperties.getPassword(), "testpassord");
 	}
 	
 	public void testSaveToFilePlaintextPassword()
 	{
 		BambooProperties bambooProperties = new BambooProperties();
 		try {
			System.setProperty("user.home", "/home/vegarwe/devel/build-monitor/src/test/resources/plainpass");
 			bambooProperties.loadFromFile();
			System.setProperty("user.home", "/home/vegarwe/devel/build-monitor/src/test/resources/tmp");
 			bambooProperties.saveToFile();
 			bambooProperties.loadFromFile();
 		} catch (IOException e) {}
 		assertEquals(bambooProperties.getPassword(), "testpassord");
 	}
 }

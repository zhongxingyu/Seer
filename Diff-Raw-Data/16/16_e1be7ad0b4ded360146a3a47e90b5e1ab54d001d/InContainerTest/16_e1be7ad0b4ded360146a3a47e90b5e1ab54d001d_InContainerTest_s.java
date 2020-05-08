 package org.vertx.maven.plugin.integration.java;
 
 /*
  * Copyright 2013 Red Hat, Inc.
  *
  * Red Hat licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  *
  * @author <a href="http://tfox.org">Tim Fox</a>
  */
 
 import static org.hamcrest.core.StringContains.containsString;
 import static org.junit.Assert.assertThat;
 import static org.openqa.selenium.By.tagName;
 import static org.vertx.testtools.VertxAssert.assertNotNull;
 import static org.vertx.testtools.VertxAssert.testComplete;
 
 import org.junit.Test;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.vertx.java.core.Handler;
 import org.vertx.testtools.TestVerticle;
 
 /**
  * Example Java integration test
  * 
  * You should extend TestVerticle.
  * 
  * We do a bit of magic and the test will actually be run _inside_ the Vert.x
  * container as a Verticle.
  * 
  * You can use the standard JUnit Assert API in your test by using the
  * VertxAssert class
  */
 public class InContainerTest extends TestVerticle {
 
 	private WebDriver driver;
 
 	// @Before
 	public void setUp() {
 		driver = new FirefoxDriver();
 	}
 
 	// @After
 	public void tearDown() {
 		driver.quit();
 
 		testComplete();
 	}
 
 	@Test
 	public void shouldSayHello() {
 		setUp();
 
		container.deployModule(System.getProperty("vertx.modulename"), new Handler<String>() {
 			@Override
			public void handle(final String deploymentID) {
 				assertNotNull("deploymentID should not be null", deploymentID);
 				driver.get("http://localhost:8080");
 
 				final WebElement h1 = driver.findElement(tagName("h1"));
 
 				assertThat("Should say hello", h1.getText(), containsString("vert.x"));
 
 				tearDown();
 			}
 		});
 	}
 
 }

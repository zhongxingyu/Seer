 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.pg.cli.test.functional;
 
 import org.junit.runner.RunWith;
 
 import cucumber.junit.Cucumber;
 
 /**
 * Single cucumber test runner. Its sole purpose is to serve as an entry point for junit. Step
 * definitions and hooks are defined in their own classes so they can be reused across features.
  * 
  */
 @RunWith(Cucumber.class)
 @Cucumber.Options(format = { "pretty", "html:target/cucumber-report" }, strict = true)
 public class RunPGFunctionalOnlineTest {
 }

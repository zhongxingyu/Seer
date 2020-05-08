 package uk.co.unclealex.music.configuration;
 
 import static org.hamcrest.Matchers.containsInAnyOrder;
 import static org.junit.Assert.assertThat;
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.List;
 import java.util.Set;
 
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.validator.constraints.NotEmpty;
 import org.junit.Test;
 
 import uk.co.unclealex.music.ValidatorImpl;
 import uk.co.unclealex.music.configuration.json.AmazonConfigurationBean;
 import uk.co.unclealex.music.configuration.json.ConfigurationBean;
 import uk.co.unclealex.music.configuration.json.CowonX7DeviceBean;
 import uk.co.unclealex.music.configuration.json.FileSystemDeviceBean;
 import uk.co.unclealex.music.configuration.json.IpodDeviceBean;
 import uk.co.unclealex.music.configuration.json.PathsBean;
 import uk.co.unclealex.music.configuration.json.UserBean;
 import uk.co.unclealex.music.violations.Violation;
 import uk.co.unclealex.validator.paths.CanRead;
 import uk.co.unclealex.validator.paths.CanWrite;
 import uk.co.unclealex.validator.paths.IsDirectory;
 
 import com.google.common.collect.Lists;
 
 /**
  * Copyright 2012 Alex Jones
  *
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
  *
  * @author unclealex72
  *
  */
 
 /**
  * @author alex
  * 
  */
 public class ConfigurationValidationTest {
 
   Path homeDir = Paths.get(System.getProperty("user.home"));
   PathsBean defaultPathBean = new PathsBean(homeDir, homeDir, homeDir, homeDir);
   AmazonConfigurationBean defaultAmazonBean = new AmazonConfigurationBean("endpoint", "accessKey", "secretKey");
   List<UserBean> defaultUsers = Lists.newArrayList(new UserBean("alex", "MeMeMe", "pwd", Lists
       .newArrayList((Device) new FileSystemDeviceBean("WALKMAN", "123456", Paths.get("Music")))));
 
   @Test
   public void testConfigurationRequiresPathAndUsers() throws Exception {
     testValidate(
         new ConfigurationBean(null, null, null),
         Violation.expect(NotNull.class, "directories"),
         Violation.expect(NotNull.class, "amazon"),
         Violation.expect(NotEmpty.class, "users"));
   }
 
   @Test
   public void testConfigurationRequiresAllPaths() throws Exception {
     testValidate(
         new ConfigurationBean(new PathsBean(null, null, null, null), defaultUsers, defaultAmazonBean),
         Violation.expect(NotNull.class, "directories", "stagingPath"),
         Violation.expect(NotNull.class, "directories", "encodedPath"),
         Violation.expect(NotNull.class, "directories", "flacPath"),
         Violation.expect(NotNull.class, "directories", "devicesPath"));
   }
 
   @Test
   public void testConfigurationRequiresValidPaths() throws Exception {
     testValidate(new ConfigurationBean(
         new PathsBean(Paths.get("a"), Paths.get("a"), Paths.get("a"), Paths.get("a")),
         defaultUsers,
         defaultAmazonBean), Violation.expect(IsDirectory.class, "directories", "stagingPath"), Violation.expect(
         CanRead.class,
         "directories",
         "stagingPath"), Violation.expect(CanWrite.class, "directories", "stagingPath"), Violation.expect(
         IsDirectory.class,
         "directories",
         "encodedPath"), Violation.expect(CanRead.class, "directories", "encodedPath"), Violation.expect(
         IsDirectory.class,
         "directories",
         "flacPath"), Violation.expect(CanRead.class, "directories", "flacPath"), Violation.expect(
         IsDirectory.class,
         "directories",
         "devicesPath"), Violation.expect(CanRead.class, "directories", "devicesPath"));
   }
 
   @Test
   public void testUserRequiresUserNamePasswordAndDevices() throws Exception {
     testValidate(new ConfigurationBean(
         defaultPathBean,
         Lists.newArrayList(new UserBean(null, null, null, null)),
         defaultAmazonBean), Violation.expect(NotEmpty.class, "users[0]", "name"), Violation.expect(
         NotEmpty.class,
         "users[0]",
         "musicBrainzUserName"), Violation.expect(NotEmpty.class, "users[0]", "musicBrainzPassword"), Violation.expect(
         NotEmpty.class,
         "users[0]",
         "devices"));
   }
 
   @Test
   public void testAmazonRequiresUrlAndKeys() throws Exception {
     testValidate(
         new ConfigurationBean(defaultPathBean, defaultUsers, new AmazonConfigurationBean(null, null, null)),
         Violation.expect(NotEmpty.class, "amazon", "endpoint"),
         Violation.expect(NotEmpty.class, "amazon", "accessKey"),
         Violation.expect(NotEmpty.class, "amazon", "secretKey"));
   }
 
   @Test
   public void testDevices() throws Exception {
     testValidate(
         new ConfigurationBean(defaultPathBean, Lists.newArrayList(new UserBean("aj", "aj", "aj", Lists.newArrayList(
             (Device) new FileSystemDeviceBean(null, null, null),
             new CowonX7DeviceBean(null),
             new IpodDeviceBean(null)))), defaultAmazonBean),
         Violation.expect(NotEmpty.class, "users[0]", "devices[0]", "name"),
        Violation.expect(NotNull.class, "users[0]", "devices[0]", "mountPoint"),
        Violation.expect(NotNull.class, "users[0]", "devices[1]", "mountPoint"));
   }
 
   public void testValidate(final ConfigurationBean configurationBean, final Violation... expectedViolations) {
     final ValidatorImpl validator = new ValidatorImpl();
     final Set<Violation> actualViolations = Violation.typedViolations(validator.generateViolations(configurationBean));
     assertThat("The wrong violations were found.", actualViolations, containsInAnyOrder(expectedViolations));
   }
 }

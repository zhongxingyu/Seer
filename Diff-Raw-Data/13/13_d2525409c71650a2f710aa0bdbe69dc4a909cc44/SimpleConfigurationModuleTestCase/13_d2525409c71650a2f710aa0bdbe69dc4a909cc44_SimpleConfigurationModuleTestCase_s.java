 /*
  *    Copyright 2009-2010 The Rocoto Team
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.rocoto.simpleconfig;
 
 import java.io.File;
 
 import lombok.Setter;
 
 import org.testng.annotations.Test;
 
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 /**
  * 
  *
  * @author Simone Tripodi
  * @version $Id$
  */
 public final class SimpleConfigurationModuleTestCase {
 
     private final SimpleConfigurationModule module = new SimpleConfigurationModule();
 
     @Inject
     @Setter
     private IBatisConfiguration iBatisConfiguration;
 
     @Inject
     @Setter
     private JDBCConfiguration jdbcConfiguration;
 
     @Inject
     @Setter
     private LdapConfiguration ldapConfiguration;
 
     @Inject
     @Setter
     private MemcachedConfiguration memcachedConfiguration;
 
     @Inject
     @Setter
     private ProxyConfiguration proxyConfiguration;
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void loadNonExistentResource() {
         this.module.addProperties("doesNotExist.properties");
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void loadNonExistentXMLResource() {
         this.module.addProperties("doesNotExist.xml");
     }
 
     @Test
     public void loadFromClasspath() {
         this.module.addProperties("/com/rocoto/simpleconfig/ldap.properties");
     }
 
     @Test
     public void loadFromRootClasspath() {
         this.module.addXMLProperties("proxy.xml");
     }
 
     @Test
     public void loadFromDirUsingDefaulTraversal() {
         this.module.addProperties(new File("test-data"));
     }
 
     @Test(dependsOnMethods = {
             "loadNonExistentResource",
             "loadNonExistentXMLResource",
             "loadFromClasspath",
             "loadFromRootClasspath",
             "loadFromDirUsingDefaulTraversal"
     })
     public void doInject() {
         Injector injector = Guice.createInjector(this.module);
         injector.injectMembers(this);
     }
 
     @Test(dependsOnMethods = "doInject")
     public void verifyIBatisConfiguration() {
         assert "test".equals(this.iBatisConfiguration.getEnvironmentId());
         assert this.iBatisConfiguration.isLazyLoadingEnabled();
     }
 
     @Test(dependsOnMethods = "doInject")
     public void verifyJDBCConfiguration() {
         assert "com.mysql.jdbc.Driver".equals(this.jdbcConfiguration.getDriver());
         assert "jdbc:mysql://localhost:3306/rocoto".equals(this.jdbcConfiguration.getUrl());
         assert "simone".equals(this.jdbcConfiguration.getUsername());
         assert "rocoto2010".equals(this.jdbcConfiguration.getPassword());
         assert this.jdbcConfiguration.isAutoCommit();
     }
 
     @Test(dependsOnMethods = "doInject")
     public void verifyLdapConfiguration() {
        assert "ldap.myuniv.edu".equals(this.ldapConfiguration.getHost());
         assert 389 == this.ldapConfiguration.getPort();
         assert "ou=People, dc=myuniv, dc=edu".equals(this.ldapConfiguration.getBaseDN());
     }
 
     @Test(dependsOnMethods = "doInject")
     public void verifyMemcachedConfiguration() {
         assert "test_".equals(this.memcachedConfiguration.getKeyPrefix());
         assert this.memcachedConfiguration.isCompressionEnabled();
     }
 
     @Test(dependsOnMethods = "doInject")
     public void verifyProxyConfiguration() {
         assert "localhost".equals(this.proxyConfiguration.getHost());
         assert 8180 == this.proxyConfiguration.getPort();
     }
 
 }

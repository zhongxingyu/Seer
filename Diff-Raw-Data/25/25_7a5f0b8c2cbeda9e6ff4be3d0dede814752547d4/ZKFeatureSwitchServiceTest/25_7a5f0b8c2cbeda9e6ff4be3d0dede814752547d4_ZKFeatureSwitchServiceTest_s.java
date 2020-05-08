 package com.wotifgroup.zkfss;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import org.apache.curator.framework.CuratorFramework;
 import org.apache.curator.framework.CuratorFrameworkFactory;
 import org.apache.curator.retry.ExponentialBackoffRetry;
 import org.apache.curator.test.TestingServer;
 
 public class ZKFeatureSwitchServiceTest {
 
     private static final byte[] FALSE = "false".getBytes();
     private static final byte[] TRUE = "true".getBytes();
     private static final String FEATURE_SWITCH1 = "Test";
     private static final String FEATURE_SWITCH2 = "blah";
     private static final String TEST_APPLICATION_NAME = "XYZ";
    private static final String TEST_FEATURE_SWITCH_NAMESPACE = "/";
     private static final String ZK_PATH_FS2 = TEST_FEATURE_SWITCH_NAMESPACE + FEATURE_SWITCH2;
     private TestingServer ts;
     private String hostname;
 
     @Before
     public void setup() throws Exception {
         ts = new TestingServer(2181);
         hostname = java.net.InetAddress.getLocalHost().getHostName();
     }
 
     @After
     public void teardown() throws IOException {
         ts.close();
     }
 
     @Test
    public void testIsEnabledForFeatureNotSet() {
         ZKFeatureSwitchService cfs = new ZKFeatureSwitchService().setFeatureSwitchNamespace(TEST_FEATURE_SWITCH_NAMESPACE).start();
 
         assertFalse(cfs.isEnabled(FEATURE_SWITCH1));
         cfs.stop();
     }
 
     @Test
     public void testIsEnabledForFeatureSet() throws Exception {
         ZKFeatureSwitchService cfs = new ZKFeatureSwitchService().setFeatureSwitchNamespace(TEST_FEATURE_SWITCH_NAMESPACE).start();

         CuratorFramework curatorFrameworkClient = cfs.getCuratorFrameworkClient();
         curatorFrameworkClient.create().forPath(ZK_PATH_FS2, TRUE);
         assertTrue(cfs.isEnabled(FEATURE_SWITCH2));
         curatorFrameworkClient.setData().forPath(ZK_PATH_FS2, FALSE);
 
         // no easy way to sync on change - just wait for a bit
         Thread.sleep(200);
 
         assertFalse(cfs.isEnabled(FEATURE_SWITCH2));
         cfs.stop();
     }
 
     @Test
     public void testHostnameOverrideForDisableSubKey() throws Exception {
         ZKFeatureSwitchService cfs =
                 new ZKFeatureSwitchService().setFeatureSwitchNamespace(TEST_FEATURE_SWITCH_NAMESPACE).disableHostnameSubKey()
                         .start();
         CuratorFramework curatorFrameworkClient = cfs.getCuratorFrameworkClient();
         curatorFrameworkClient.create().forPath(ZK_PATH_FS2, TRUE);
         curatorFrameworkClient.create().forPath(ZK_PATH_FS2 + "/" + hostname, FALSE);
         assertTrue(cfs.isEnabled(FEATURE_SWITCH2));
         cfs.stop();
     }
 
     @Test
     public void testHostnameOverrideForEnableSubKey() throws Exception {
         ZKFeatureSwitchService cfs =
                 new ZKFeatureSwitchService().setFeatureSwitchNamespace(TEST_FEATURE_SWITCH_NAMESPACE).enableHostnameSubKey()
                         .start();
         CuratorFramework curatorFrameworkClient = cfs.getCuratorFrameworkClient();
         curatorFrameworkClient.create().forPath(ZK_PATH_FS2, TRUE);
         curatorFrameworkClient.create().forPath(ZK_PATH_FS2 + "/" + hostname, FALSE);
         assertFalse(cfs.isEnabled(FEATURE_SWITCH2));
         cfs.stop();
     }
 
     @Test
     public void testApplicationNameOverrideWithDisabledHostnameSubKey() throws Exception {
         ZKFeatureSwitchService cfs =
                 new ZKFeatureSwitchService().setFeatureSwitchNamespace(TEST_FEATURE_SWITCH_NAMESPACE)
                         .setApplicationName(TEST_APPLICATION_NAME).disableHostnameSubKey().start();
         CuratorFramework curatorFrameworkClient = cfs.getCuratorFrameworkClient();
         curatorFrameworkClient.create().forPath(ZK_PATH_FS2, TRUE);
         curatorFrameworkClient.create().forPath(ZK_PATH_FS2 + "/" + TEST_APPLICATION_NAME, FALSE);
         assertFalse(cfs.isEnabled(FEATURE_SWITCH2));
         cfs.stop();
     }
 
     @Test
     public void testApplicationNameOverrideWithEnabledHostnameSubKey() throws Exception {
         ZKFeatureSwitchService cfs =
                 new ZKFeatureSwitchService().setFeatureSwitchNamespace(TEST_FEATURE_SWITCH_NAMESPACE)
                         .setApplicationName(TEST_APPLICATION_NAME).enableHostnameSubKey().start();
         CuratorFramework curatorFrameworkClient = cfs.getCuratorFrameworkClient();
         curatorFrameworkClient.create().forPath(ZK_PATH_FS2, TRUE);
         curatorFrameworkClient.create().forPath(ZK_PATH_FS2 + "/" + hostname, TRUE);
         curatorFrameworkClient.create().forPath(ZK_PATH_FS2 + "/" + TEST_APPLICATION_NAME, FALSE);
         assertFalse(cfs.isEnabled(FEATURE_SWITCH2));
         cfs.stop();
     }
 
     @Test(expected = IllegalStateException.class)
     public void testNoConfigurationChangeAllowedForRunningSystem() throws Exception {
         ZKFeatureSwitchService cfs = new ZKFeatureSwitchService().setFeatureSwitchNamespace(TEST_FEATURE_SWITCH_NAMESPACE).start();
        cfs.disableHostnameSubKey();
         fail("Should not be able to change configuration while service is running");
     }
 
     @Test(expected = IllegalStateException.class)
     public void testServiceMustBeRunningForIsEnabled() throws Exception {
         ZKFeatureSwitchService cfs = new ZKFeatureSwitchService().setFeatureSwitchNamespace(TEST_FEATURE_SWITCH_NAMESPACE).start();
         cfs.stop();
         cfs.isEnabled(FEATURE_SWITCH2);
         fail("Should not be able to call isEnabled when service has not been started");
     }
 
     @Test(expected = IllegalStateException.class)
     public void testServiceMustBeStartedForIsEnabled() throws Exception {
         CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", new ExponentialBackoffRetry(100, 1));
         ZKFeatureSwitchService cfs =
                 new ZKFeatureSwitchService().setFeatureSwitchNamespace(TEST_FEATURE_SWITCH_NAMESPACE).setCuratorFrameworkClient(
                         client);
         cfs.isEnabled(FEATURE_SWITCH2);
         fail("Should not be able to call isEnabled when service has not been started");
     }
 }

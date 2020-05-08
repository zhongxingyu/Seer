 package com.cibot.feedreader;
 
 import com.cibot.cimodel.BuildStatus;
 import com.cibot.config.CIBotConfiguration;
 import org.apache.commons.lang.StringUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.URL;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.when;
 
 /**
  * @author Uefix
  */
 @RunWith(MockitoJUnitRunner.class)
 public class JenkinsBuildStatusCheckerTest {
 
 
     private static final Logger LOG = LoggerFactory.getLogger(JenkinsBuildStatusCheckerTest.class);
 
 
     private JenkinsBuildStatusChecker checker;
 
     @Mock
     private CIBotConfiguration configuration;
 
     private String url;
     private String user;
     private String password;
 
     @Before
     public void setup() {
         checker  = Mockito.spy(new JenkinsBuildStatusChecker());
         mockMapBuildStatusString(BuildStatus.BUILD_OK);
 
         url = System.getProperty("url");
         user = System.getProperty("user");
         password = System.getProperty("password");
 
         mockConfiguration_getLogin();
         checker.setConfiguration(configuration);
     }
 
 
 
     @Test
     public void getBuildStatus_withCredentials_returnsMockedResult() throws Exception {
         if (!isLoginConfigAvailable()) {
             LOG.debug("Skipping test because login configuration is not available");
             return;
         }
 
         CIBotConfiguration.Feed feed = new CIBotConfiguration.Feed();
         feed.setUrl(new URL(url));
        feed.setLogin("test");
 
         BuildStatus result = checker.getBuildStatus(feed);
         assertEquals(BuildStatus.BUILD_OK, result);
     }
 
 
 
 
     private void mockMapBuildStatusString(BuildStatus result) {
         when(checker.mapBuildStatusString(anyString())).thenReturn(result);
     }
 
 
     private void mockConfiguration_getLogin() {
         CIBotConfiguration.Login loginConfig = new CIBotConfiguration.Login("test", user, password);
         when(configuration.getLogin(any(CIBotConfiguration.Feed.class))).thenReturn(loginConfig);
     }
 
 
     private boolean isLoginConfigAvailable() {
         if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
             return true;
         }
         return false;
     }
 }

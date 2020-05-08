 package com.github.codingricky.runkeeperclient;
 
 import com.github.codingricky.runkeeperclient.model.User;
import com.github.codingricky.runkeeperclient.model.WeightFeed;
 import org.apache.commons.lang.StringUtils;
 import org.junit.Assume;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class ClientIntegrationTest {
 
     private String accessToken;
 
     @Before
     public void before() {
         accessToken = System.getProperty("testing.accessToken");
         Assume.assumeTrue(StringUtils.isNotBlank(accessToken));
     }
 
     @Test
     public void getUser() {
         User user = new Client(accessToken).getUser();
         assertThat(user).isNotNull();
     }

    @Test
    public void getWeight() {
        WeightFeed weightFeed = new Client(accessToken).getWeightFeed();
        assertThat(weightFeed).isNotNull();
    }
 }

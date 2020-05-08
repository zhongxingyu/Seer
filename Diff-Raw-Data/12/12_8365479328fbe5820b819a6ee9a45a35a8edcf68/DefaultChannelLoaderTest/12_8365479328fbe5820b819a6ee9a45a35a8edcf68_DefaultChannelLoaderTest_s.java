 package org.lazydevs.veetle.api;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.lazydevs.veetle.api.model.Channel;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Sascha
  * Date: 05.02.12
  * Time: 14:08
  * To change this template use File | Settings | File Templates.
  */
 public class DefaultChannelLoaderTest {
 
     @Test(expected = IllegalArgumentException.class)
     public void testEmptyUrl() throws LoadChannelException {
 
         DefaultChannelLoader loader = new DefaultChannelLoader();
 
         loader.setVeetleChannelDetailsUrl(null);
 
         loader.load("48697987e6a7b");
     }
 
     @Test(expected = LoadChannelException.class)
     public void testInvalidUrl() throws LoadChannelException {
 
         DefaultChannelLoader loader = new DefaultChannelLoader();
 
         loader.setVeetleChannelDetailsUrl("http://blablabla.bla");
 
         loader.load("48697987e6a7b");
     }
 
     @Test()
     public void testInvalidChannel() throws LoadChannelException {
 
         DefaultChannelLoader loader = new DefaultChannelLoader();
 
         String json = loader.load("blablabla");
 
         Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("no channel with channel ID blablabla exists"));
     }
 
     @Test
     public void testLoad() throws LoadChannelException {
 
         DefaultChannelLoader loader = new DefaultChannelLoader();
 
         String json = loader.load("48697987e6a7b");
 
         Assert.assertNotNull(json);
         Assert.assertTrue(json.contains("Public Interest Feed"));
     }
 }

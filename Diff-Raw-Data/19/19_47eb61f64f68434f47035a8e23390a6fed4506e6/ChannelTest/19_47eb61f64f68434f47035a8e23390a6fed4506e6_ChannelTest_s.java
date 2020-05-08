 package com.thedevteam.thechatmod;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.spout.api.util.config.ConfigurationHolder;
 
 import com.thedevteam.thechatmod.Channels.Channel;
 
 public class ChannelTest {
 	
 	@Test
 	public void newChannelTest(){
		ConfigurationHolder config = new ConfigurationHolder("test","name");
		ConfigurationHolder le = new ConfigurationHolder("local.enable","false");
		ConfigurationHolder ld = new ConfigurationHolder("local.distance","100");
		config.addChild(le);
		config.addChild(ld);
 		
		Channel test = new Channel("test", config);
 		
 		assertTrue(test.getName() == "test");
 		assertEquals(test.isLocal(),false);
 		assertEquals(test.getDistance(),0);
 		
 	}
 	
 
 }

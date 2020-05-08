 package com.thedevteam.thechatmod;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
import org.spout.api.util.config.Configuration;
 import org.spout.api.util.config.ConfigurationHolder;
import org.spout.api.util.config.MemoryConfiguration;
 
 import com.thedevteam.thechatmod.Channels.Channel;
 
 public class ChannelTest {
 	
 	@Test
 	public void newChannelTest(){
		Configuration config = new MemoryConfiguration();
		ConfigurationHolder chcfg = new ConfigurationHolder("test","name");
		chcfg.setConfiguration(config);
		ConfigurationHolder le = new ConfigurationHolder("false","local","enable");
		ConfigurationHolder ld = new ConfigurationHolder("100","local","distance");
		chcfg.addChild(le);
		chcfg.addChild(ld);
 		
		Channel test = new Channel("test", chcfg);
 		
 		assertTrue(test.getName() == "test");
 		assertEquals(test.isLocal(),false);
 		assertEquals(test.getDistance(),0);
 		
 	}
 	
 
 }

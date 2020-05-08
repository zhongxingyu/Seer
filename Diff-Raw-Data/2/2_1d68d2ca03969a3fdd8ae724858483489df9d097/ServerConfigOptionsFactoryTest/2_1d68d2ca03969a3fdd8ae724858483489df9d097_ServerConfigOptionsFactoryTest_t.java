 package org.eluder.jetty.cli;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.apache.commons.cli.Options;
 import org.eluder.jetty.server.ServerConfig;
 import org.junit.Test;
 
 public class ServerConfigOptionsFactoryTest {
     
     @Test
     public void testCreate() {
         Options options = new ServerConfigOptionsFactory(ServerConfig.class).create();
         assertEquals(10, options.getOptions().size());
         assertTrue(options.getOption("webApp").isRequired());
         assertTrue(options.getOption("maxThreads").hasArg());
        assertFalse(options.getOption("classpath").hasArg());
     }
     
 }

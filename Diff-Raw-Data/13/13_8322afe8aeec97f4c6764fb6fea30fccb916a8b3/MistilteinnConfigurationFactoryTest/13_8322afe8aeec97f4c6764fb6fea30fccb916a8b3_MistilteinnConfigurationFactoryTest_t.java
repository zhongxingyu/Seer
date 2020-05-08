 package org.codefirst.mistilteinn.config;
 
import org.codefirst.mistilteinn.MistilteinnException;
 import org.junit.Test;
 
 public class MistilteinnConfigurationFactoryTest {
 
    @Test(expected = MistilteinnException.class)
    public void testCreateConfigurationNotFound() throws Exception {
        MistilteinnConfigurationFactory.createConfiguration("notExist");
     }
 
 }

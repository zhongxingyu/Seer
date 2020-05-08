 package org.codefirst.mistilteinn.config;
 
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

 import org.junit.Test;
 
 public class MistilteinnConfigurationFactoryTest {
 
    @Test
    public void testCreateConfiguration() throws Exception {
        MistilteinnConfiguration configuration = MistilteinnConfigurationFactory.createConfiguration(".");
        String result = configuration.getClass().getName();
        assertThat(result, is(YAMLMistilteinnConfiguration.class.getName()));
     }
 
 }

 package org.ccci.deployment;
 
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.allOf;
 import static org.hamcrest.Matchers.contains;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.instanceOf;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.typeCompatibleWith;
 import static org.testng.Assert.*;
 
 import com.google.common.io.Resources;
 import org.testng.annotations.Test;
 import org.yaml.snakeyaml.error.YAMLException;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 import java.net.URL;
 import java.util.Set;
 
 /**
  * @author Matt Drees
  */
 public class BasicApplicationBuilderTest {
 
     BasicApplicationBuilder builder = new BasicApplicationBuilder();
 
     @Test
    public void testInvalidConfigValue()
     {
         URL invalidConfig = Resources.getResource(getClass(), "/invalid-config-value-deploy.yml");
         try {
             builder.buildFrom(invalidConfig);
             fail("should have thrown an exception");
         }
         catch (Exception expected)
         {
             assertThat(expected, instanceOf(IllegalArgumentException.class));
             assertThat(expected.getMessage(), allOf(
                 containsString("invalid-config-value-deploy.yml"),
 
                 containsString("environments[test].port: must be less than or equal to 65535 (value is 84800)"),
                 containsString("applicationName: may not be null"),
                 containsString("os: may not be null"),
                 containsString("environments[test].nodes[0].name: may not be empty (value is  )"),
                 containsString("deploymentVerification.ping.secondsBeforeTimeout: must be greater than or equal to 0 (value is -1)")
             ));
 
             Throwable cause = expected.getCause();
             assertThat(cause, instanceOf(ConstraintViolationException.class));
             Set<ConstraintViolation<?>> constraintViolations =
                 ((ConstraintViolationException) cause).getConstraintViolations();
 
             assertThat(constraintViolations, hasSize(5));
         }
     }
 
 
     @Test
     public void testInvalidYamlStructure()
     {
         URL invalidConfig = Resources.getResource(getClass(), "/invalid-structure-deploy.yml");
         try {
             builder.buildFrom(invalidConfig);
             fail("should have thrown an exception");
         }
         catch (Exception expected)
         {
            expected.printStackTrace();

             assertThat(expected, instanceOf(IllegalArgumentException.class));
             assertThat(expected.getMessage(), containsString("invalid-structure-deploy.yml"));
 
             Throwable cause = expected.getCause();
             assertThat(cause, instanceOf(YAMLException.class));
 
             assertThat(cause.getMessage(), containsString("aplicationName"));
         }
     }
 }

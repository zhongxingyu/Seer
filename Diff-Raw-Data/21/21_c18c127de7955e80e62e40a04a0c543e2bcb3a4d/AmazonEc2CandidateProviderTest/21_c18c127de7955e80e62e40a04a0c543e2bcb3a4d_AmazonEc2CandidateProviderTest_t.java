 package org.lastbamboo.common.amazon.ec2;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.util.Collection;
 import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
 * Test for Amazon EC2 utilities.
  */
 public class AmazonEc2CandidateProviderTest
     {
     
     private final Logger m_log = LoggerFactory.getLogger(getClass());
 
     @Test public void testDescribeInstances() throws Exception
         {
         final Properties props = new Properties();
         final File file = new File(System.getProperty("user.home"), 
             "/.ec2/ec2.properties");
         if (!file.exists())
             {
             m_log.debug("No EC2 props file at: "+file.getAbsolutePath());
             return;
             }
         final InputStream is = new FileInputStream(file);
         props.load(is);
         final String keyId = props.getProperty("accessKeyId");
         final String key = props.getProperty("accessKey");
         if (StringUtils.isBlank(keyId) || StringUtils.isBlank(key))
             {
             m_log.debug("EC2 props aren't set!!");
             return;
             }
         
         final AmazonEc2CandidateProvider ec2 = 
             new AmazonEc2CandidateProvider(keyId, key);
         
        final Collection<InetAddress> instances = ec2.getInstanceAddresses("sip-turn");
         
         assertEquals(
             InetAddress.getByName("ec2-67-202-6-199.z-1.compute-1.amazonaws.com"), 
             instances.iterator().next());
         }
     }

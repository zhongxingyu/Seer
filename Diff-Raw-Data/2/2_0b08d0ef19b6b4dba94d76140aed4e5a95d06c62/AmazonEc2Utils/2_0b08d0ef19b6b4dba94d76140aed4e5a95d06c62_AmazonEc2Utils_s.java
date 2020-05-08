 package org.lastbamboo.common.amazon.ec2;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.lang.time.DateUtils;
 import org.lastbamboo.common.util.DefaultHttpClient;
 import org.lastbamboo.common.util.DefaultHttpClientImpl;
 import org.lastbamboo.common.util.NetworkUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Utility methods for Amazon EC2. 
  */
 public class AmazonEc2Utils
     {
 
     private static final Logger LOG = 
         LoggerFactory.getLogger(AmazonEc2Utils.class);
     
     private static InetAddress s_cachedAddress;
 
     private static long s_lastUpdateTime = 0L;
     
     private AmazonEc2Utils()
         {
         // Should not be constructed.
         }
     
     /**
      * Accesses the public address for the EC2 instance.  This is necessary
      * because InetAddress.getLocalHost() will yield the private, NATted
      * address.
      * 
      * @return The public address for the EC2 instance, or <code>null</code> if
      * there's an error accessing the address.
      */
     public static InetAddress getPublicAddress()
         {
         // First just check if we're even on Amazon -- we could be testing
         // locally, for example.
         LOG.debug("Getting public address");
         
         final long now = System.currentTimeMillis();
         if ((now - s_lastUpdateTime) < DateUtils.MILLIS_PER_MINUTE)
             {
             LOG.debug("Using cached address...");
             return s_cachedAddress;
             }
         
         // Check to see if we're running on EC2.  If we're not, we're probably 
         // testing.  This technique could be a problem if the EC2 internal 
         // addressing is ever different from 10.253.
         try
             {
             if (!onEc2())
                 {
                 // Not running on EC2.  We might be testing, or this might be
                 // a server running on another system.
                 LOG.debug("Not running on EC2.");
                 return NetworkUtils.getLocalHost();
                 }
             }
         catch (final UnknownHostException e)
             {
             LOG.error("Could not get host.", e);
             return null;
             }
         final String url = "http://169.254.169.254/latest/meta-data/public-ipv4";
         final DefaultHttpClient client = new DefaultHttpClientImpl();
         client.getHttpConnectionManager().getParams().setConnectionTimeout(
             20 * 1000);
         final GetMethod method = new GetMethod(url);
         try
             {
             LOG.debug("Executing method...");
             final int statusCode = client.executeMethod(method);
             if (statusCode != HttpStatus.SC_OK)
                 {
                 LOG.warn("ERROR ISSUING REQUEST:\n" + method.getStatusLine() + 
                     "\n" + method.getResponseBodyAsString());
                 return null;
                 }
             else
                 {
                 LOG.debug("Successfully received response...");
                 }
             final String host = method.getResponseBodyAsString();
             LOG.debug("Got address: "+host);
             s_cachedAddress = InetAddress.getByName(host);
             s_lastUpdateTime = System.currentTimeMillis();
             return s_cachedAddress;
             }
         catch (final HttpException e)
             {
             LOG.error("Could not access EC2 service", e);
             return null;
             }
         catch (final IOException e)
             {
             LOG.error("Could not access EC2 service", e);
             return null;
             }
         finally 
             {
             method.releaseConnection();
             }
         }
 
     /**
      * Returns whether or not we're running on EC2.
      * 
      * @return <code>true</code> if we're running on EC2, otherwise 
      * <code>false</code>.
      */
     public static boolean onEc2()
         {
         // Good enough for now to determine if we're running on EC2.
         try
             {
             return NetworkUtils.getLocalHost().getHostAddress().startsWith("10.25");
             }
         catch (final UnknownHostException e)
             {
             LOG.warn("Unknown host", e);
             return false;
             }
         }
     }

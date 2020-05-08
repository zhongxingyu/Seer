 package com.cibot.feedreader;
 
 import com.cibot.cimodel.BuildStatus;
 import com.cibot.config.CIBotConfiguration;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import sun.misc.BASE64Encoder;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URLConnection;
 import java.util.List;
 
 /**
  * Implementation that checks the build status of Jenkins CI builds.
  *
  * @author Uefix
  * @author j-n00b
  */
 public class JenkinsBuildStatusChecker implements BuildStatusChecker {
 
 
     private static final Logger LOG = LoggerFactory.getLogger(JenkinsBuildStatusChecker.class);
 
 
     @Autowired
     private CIBotConfiguration configuration;
 
 
     @Override
     public BuildStatus getBuildStatus(CIBotConfiguration.Feed feed) {
         BuildStatus status = BuildStatus.UNKNOWN;
         Reader reader = null;
         try {
             URLConnection con = initializedConnection(feed);
             con.connect();
             reader = new InputStreamReader(con.getInputStream());
 
             final SyndFeedInput input = new SyndFeedInput();
             final SyndFeed syncFeed = input.build(reader);
 
             @SuppressWarnings("unchecked")
             final List<SyndEntry> feedEntries = syncFeed.getEntries();
             if (feedEntries.size() > 0) {
                 final String title = feedEntries.iterator().next().getTitle();
 
                 // FIXME das muss schöner gehen!
                 final int beginIndex = title.indexOf('(') + 1;
                 final int endIndex = title.lastIndexOf(')');
                 String statusString = title.substring(beginIndex, endIndex);
 
                 status = mapBuildStatusString(statusString);
                if (status == null) {
                    LOG.error("Unmapped statusString: {}", statusString);
                    status = BuildStatus.BUILD_FAILED;
                }
 
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Status for {}: {}", feed.getJobName(), status.toString());
                 }
             }
             return status;
         } catch (Exception e) {
             LOG.error("{} while reading feed for {}", e.getClass().getSimpleName(), feed.getJobName());
             status = BuildStatus.UNKNOWN;
         } finally {
             IOUtils.closeQuietly(reader);
         }
         return status;
     }
 
 
     private URLConnection initializedConnection(CIBotConfiguration.Feed feed) throws IOException {
         URLConnection con = feed.getUrl().openConnection();
         if (feed.hasLogin()) {
             CIBotConfiguration.Login loginConfig = configuration.getLogin(feed);
             String encodedAuth = new BASE64Encoder().encode(
                     (loginConfig.getUser() + ":" + loginConfig.getPassword()).getBytes());
 
             con.setRequestProperty("Authorization", "Basic " + encodedAuth);
         }
         return con;
     }
 
 
     /**
       * @param statusString
       * @return
       */
     BuildStatus mapBuildStatusString(String statusString) {
         if (StringUtils.isNotEmpty(statusString)) {
             BuildStatus result = configuration.getFeedReader().mapBuildStatus(statusString);
             if (result != null) {
                 return result;
             }
         }
        return null;
     }
 
 
     void setConfiguration(CIBotConfiguration configuration) {
         this.configuration = configuration;
     }
 }

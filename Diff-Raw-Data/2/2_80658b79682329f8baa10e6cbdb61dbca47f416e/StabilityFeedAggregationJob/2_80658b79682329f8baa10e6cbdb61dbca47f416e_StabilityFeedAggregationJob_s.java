 package pl.psnc.dl.wf4ever.monitoring;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.log4j.Logger;
 import org.quartz.Job;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.quartz.JobListener;
 
 import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
 import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
 import pl.psnc.dl.wf4ever.notifications.Notification;
 
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 /**
  * This job gets a list of all research objects and for each them schedules monitoring jobs.
  * 
  * @author pejot
  * 
  */
 public class StabilityFeedAggregationJob implements Job {
 
     /** Service Uri. */
     private URI checklistNotificationsUri = null;
 
     /** Id of checksum verification job group. */
     static final String CHECKSUM_CHECKING_GROUP_NAME = "stabilityFeedAdgregatrion";
 
     /** Map key. */
     static final String RESEARCH_OBJECT_URI = "researchObjectUri";
 
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(StabilityFeedAggregationJob.class);
 
 
     /**
      * Default constructor.
      * 
      * @throws IOException
      *             in case properties can't be loaded
      */
     public StabilityFeedAggregationJob()
             throws IOException {
         Properties properties = new Properties();
         try {
             properties.load(getClass().getClassLoader().getResourceAsStream("connection.properties"));
            checklistNotificationsUri = URI.create(properties.getProperty("checklist_notifications_uri"));
         } catch (IOException e) {
             throw new IOException("Configuration for stability service couldn't be loaded", e);
         }
     }
 
 
     @Override
     public void execute(JobExecutionContext context)
             throws JobExecutionException {
         if (checklistNotificationsUri == null) {
             return;
         }
         boolean started = !HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive();
         if (started) {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
         }
         try {
             URI researchObjectUri = (URI) context.getMergedJobDataMap().get(RESEARCH_OBJECT_URI);
             SyndFeedInput input = new SyndFeedInput();
             URI requestedUri = createQueryUri(getTheLastFeedDate(researchObjectUri), researchObjectUri);
             try {
                 context.setResult(input.build(new XmlReader(requestedUri.toURL())));
             } catch (IllegalArgumentException | FeedException | IOException e) {
                 LOGGER.error("Can't get the feed " + requestedUri.toString());
             }
         } finally {
             if (started) {
                 HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
             }
         }
 
     }
 
 
     /**
      * Create a new listener. Override to change the default behaviour.
      * 
      * @return a new {@link ChecksumVerificationJobListener}
      */
     protected JobListener newChecksumVerificationJobListener() {
         return new ChecksumVerificationJobListener();
     }
 
 
     //helpers
     /**
      * Built a proper uri with query param for checklist stability service.
      * 
      * @param from
      *            query paramter - bottom date range
      * @param researchObjectUri
      *            subject
      * @return build uri
      */
     private URI createQueryUri(Date from, URI researchObjectUri) {
         URI resultUri = checklistNotificationsUri;
         resultUri = (researchObjectUri != null) ? UriBuilder.fromUri(resultUri)
                 .queryParam("ro", researchObjectUri.toString()).build() : resultUri;
         resultUri = (from != null) ? UriBuilder.fromUri(resultUri).queryParam("from", from.toString()).build()
                 : resultUri;
         return resultUri;
     }
 
 
     /**
      * Get the date of the last stored feed in rodl.
      * 
      * @param researchObjectUri
      *            subject
      * @return the oldest date, null if there in no notifications
      */
     private Date getTheLastFeedDate(URI researchObjectUri) {
         AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
         List<Notification> notifications = dao.find(researchObjectUri, null, null, checklistNotificationsUri, 1);
         if (notifications == null || notifications.size() != 1) {
             return null;
         }
         return notifications.get(0).getCreated();
     }
 }

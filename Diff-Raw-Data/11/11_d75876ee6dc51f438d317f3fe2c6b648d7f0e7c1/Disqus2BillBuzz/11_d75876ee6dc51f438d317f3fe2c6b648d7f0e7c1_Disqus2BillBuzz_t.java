 package gov.nysenate.billbuzz.util;
 
 import gov.nysenate.billbuzz.disqus.DisqusAuthor;
 import gov.nysenate.billbuzz.disqus.DisqusPost;
 import gov.nysenate.billbuzz.disqus.DisqusThread;
 import gov.nysenate.billbuzz.model.BillBuzzAuthor;
 import gov.nysenate.billbuzz.model.BillBuzzPost;
 import gov.nysenate.billbuzz.model.BillBuzzThread;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.client.fluent.Request;
 import org.apache.http.client.fluent.Response;
import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 public class Disqus2BillBuzz
 {
    private static Logger logger = Logger.getLogger(Disqus2BillBuzz.class);

     public static Pattern billPattern = Pattern.compile("/bill/([A-Z])0*([0-9]+)([A-Z])?(?:-([0-9]+))?", Pattern.CASE_INSENSITIVE);
 
     /**
      * Pretty straight forward mapping of attributes. Billbuzz uses *Id for all
      * of its object references to make their value more apparent.
      * @param post
      * @return
      */
     public static BillBuzzPost post(DisqusPost post)
     {
         BillBuzzPost bbPost = new BillBuzzPost();
 
         bbPost.setId(post.getId());
         bbPost.setForumId(post.getForum());
         bbPost.setThreadId(post.getThread());
         bbPost.setParentId(post.getParent());
 
         // Wrap the disqus author and do save the id separate for DB storage
         BillBuzzAuthor author = Disqus2BillBuzz.author(post.getAuthor());
         bbPost.setAuthor(author);
         bbPost.setAuthorId(author.getId());
 
         bbPost.setPoints(post.getPoints());
         bbPost.setLikes(post.getLikes());
         bbPost.setDislikes(post.getDislikes());
         bbPost.setUserScore(post.getUserScore());
         bbPost.setNumReports(post.getUserScore());
 
         bbPost.setRawMessage(post.getRaw_message());
         bbPost.setMessage(post.getMessage());
 
         bbPost.setJuliaFlagged(post.isJuliaFlagged());
         bbPost.setIsFlagged(post.getIsFlagged());
         bbPost.setIsApproved(post.getIsApproved());
         bbPost.setIsSpam(post.getIsSpam());
         bbPost.setIsEdited(post.getIsEdited());
         bbPost.setIsHighlighted(post.getIsHighlighted());
         bbPost.setIsDeleted(post.getIsDeleted());
 
         bbPost.setCreatedAt(post.getCreatedAt());
         bbPost.setUpdatedAt(new Date());
 
         return bbPost;
     }
 
     public static BillBuzzThread thread(DisqusThread thread) throws IOException
     {
         BillBuzzThread bbThread = new BillBuzzThread();
 
         // Extract the billId from the link
         if (thread.getLink() != null) {
             Matcher billMatcher = billPattern.matcher(thread.getLink());
             if (billMatcher.find()) {
                 String billType = billMatcher.group(1);
                 String billNo = billMatcher.group(2);
                 String billAmendment = billMatcher.group(3) == null ? "" : billMatcher.group(3);
                 String billSession = billMatcher.group(4) == null ? "2009" : billMatcher.group(4);
                 String billId = billType+billNo+billAmendment+"-"+billSession;
                 bbThread.setBillId(billId);
 
                 // Fetch relevant metadata from that bill
                 Response response = Request.Get("http://open.nysenate.gov/legislation/api/2.0/bill/"+billId+".json").execute();
                 String content = response.returnContent().asString();
                 JsonNode root = new ObjectMapper().readTree(content);
                 if (root.get("response").get("metadata").get("totalresults").asInt() == 1) {
                     JsonNode result = root.get("response").get("results").get(0);
                     String sponsor = result.get("data").get("bill").get("sponsor").get("fullname").asText();
                     sponsor = sponsor.replaceAll("RULES COM ", "").replaceAll(" \\(MS\\)", "").split(",")[0];
                     bbThread.setSponsor(sponsor);
                 }
                 else {
                     // Bill doesn't exist
                    logger.error("Could not retrieve billId "+billId+" from OpenLeg. Thread link was: "+thread.getLink());
                 }
             }
             else {
                logger.error("Could not extract billId from "+thread.getLink());
             }
         }
         else {
             // Thread doesn't have a link? what?
            logger.error("The thread ["+thread.getId()+"] doesn't have a link. This can't happen?");
         }
 
         bbThread.setId(thread.getId());
         bbThread.setForumId(thread.getForum());
         bbThread.setAuthorId(thread.getAuthor());
         bbThread.setIsDeleted(thread.getIsDeleted());
         bbThread.setIsClosed(thread.getIsClosed());
         bbThread.setUserSubscription(thread.getUserSubscription());
         bbThread.setCategory(thread.getCategory());
         bbThread.setFeed(thread.getFeed());
         bbThread.setLink(thread.getLink());
         bbThread.setSlug(thread.getSlug());
         bbThread.setTitle(thread.getTitle());
         bbThread.setPosts(thread.getPosts());
         bbThread.setMessage(thread.getMessage());
         bbThread.setCreatedAt(thread.getCreatedAt());
         bbThread.setIdentifiers(thread.getIdentifiers());
         bbThread.setLikes(thread.getLikes());
         bbThread.setDislikes(thread.getDislikes());
         bbThread.setUserScore(thread.getUserScore());
         bbThread.setUpdatedAt(new Date());
 
         return bbThread;
     }
 
     public static BillBuzzAuthor author(DisqusAuthor author)
     {
         BillBuzzAuthor bbAuthor = new BillBuzzAuthor();
 
         if (author.getId() == null) {
             bbAuthor.setId(author.getEmailHash());
         }
         else {
             bbAuthor.setId(author.getId());
         }
 
         // Anonymous users are reported to join at the time of query
         if (!author.getIsAnonymous()) {
             bbAuthor.setJoinedAt(author.getJoinedAt());
         }
 
         // Save a link to the large avatar
         bbAuthor.setAvatarUrl(author.getAvatar().getPermalink());
 
         bbAuthor.setUsername(author.getUsername());
         bbAuthor.setAbout(author.getAbout());
         bbAuthor.setName(author.getName());
         bbAuthor.setUrl(author.getUrl());
         bbAuthor.setIsAnonymous(author.getIsAnonymous());
         bbAuthor.setRep(author.getRep());
         bbAuthor.setReputation(author.getReputation());
         bbAuthor.setIsFollowing(author.getIsFollowing());
         bbAuthor.setIsFollowedBy(author.getIsFollowedBy());
         bbAuthor.setProfileUrl(author.getProfileUrl());
         bbAuthor.setEmailHash(author.getEmailHash());
         bbAuthor.setLocation(author.getLocation());
         bbAuthor.setIsPrivate(author.getIsPrivate());
         bbAuthor.setIsPrimary(author.getIsPrimary());
 
         // Mark the time updated as now.
         bbAuthor.setUpdatedAt(new Date());
 
         return bbAuthor;
     }
 }

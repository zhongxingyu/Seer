 package jobs;
 
 import java.util.List;
 
 import models.BlogCampaign;
 import models.FeedFetchQueue;
 import play.Logger;
 import play.jobs.Every;
 import play.jobs.Job;
 
 import common.Constants;
 import common.TimeUtil;
 
 //@Every("15mn")
 @Every("30s")
 public class AddToFetchQueue extends Job {
 
 	public void doJob() {
 		Logger.info("Checking to see if any feeds are to be fetched");
 		Long currTime = TimeUtil.getCurrTime();
 		Long currPlus1 = currTime + 1 * 60 * 1000;
 
 		List<BlogCampaign> blogs = BlogCampaign.find(
 				"status =  ? and dateofnextscheduledemail < ?",
 				Constants.CAMPAIGN_STATUS_ACTIVE, currTime).fetch();
 		Logger.debug("Following blog campaigns are active, but unscheduled today");
 		for (BlogCampaign blog : blogs) {
 			String dW = TimeUtil.dayOfWeek(blog.emailTZ);
 			Logger.debug("Checking for blogs on : %s", dW);
 			if (blog.emailOnDays.contains(dW)) {
				Logger.debug("Blog[%d] - emailAt:%s tz:%s", blog.id,
						blog.emailAtTime, blog.emailTZ);
 				Long blogAt = TimeUtil.getTime(blog.emailAtTime, blog.emailTZ);
 				Logger.debug("Blog[%d] at : %d, current Time: %d", blog.id,
 						blogAt, currTime);
 				if ((blogAt > currTime) && (blogAt < currPlus1)) {
 					Logger.debug("blog [%d] url[%s] is active soon", blog.id,
 							blog.blogUrl);
 					insertIntoFetchQueue(blog, blogAt);
 				} else {
 					Logger.debug("blog [%d] url[%s] is active at [%s] today",
 							blog.id, blog.blogUrl, blog.emailAtTime);
 				}
 			}
 		}
 		Logger.debug("Done printing active blog campaign list");
 
 	}
 
 	private void insertIntoFetchQueue(BlogCampaign blog, Long blogAt) {
 		FeedFetchQueue qItem = new FeedFetchQueue();
 		qItem.bc = blog;
 		qItem.status = Constants.CAMPAIGN_STATUS_ACTIVE;
 		qItem.numRecvd = 0;
 		qItem.numSent = 0;
 		qItem.save();
 
 		blog.queue.add(qItem);
 		blog.dateOfNextScheduledEmail = blogAt;
 		blog.save();
 		Logger.debug("Inserted blog[%d] into fetch Q", blog.id);
 	}
 }

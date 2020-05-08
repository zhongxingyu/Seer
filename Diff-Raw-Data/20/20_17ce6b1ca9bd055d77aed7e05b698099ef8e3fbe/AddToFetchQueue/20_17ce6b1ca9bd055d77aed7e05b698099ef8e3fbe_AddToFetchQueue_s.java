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
 @Every("3mn")
 public class AddToFetchQueue extends Job {
 
 	public void doJob() {
 		Logger.info("Checking to see if any feeds are to be fetched");
 		Long currTime = TimeUtil.getCurrTime();
 		Long currPlus3 = currTime + 3 * 60 * 1000;
 
		String dW = TimeUtil.dayOfWeek();

 		List<BlogCampaign> blogs = BlogCampaign.find(
 				"status =  ? and dateofnextscheduledemail < ?",
 				Constants.CAMPAIGN_STATUS_ACTIVE, currTime).fetch();
 		Logger.debug("Following blog campaigns are active, but unscheduled today");
 		for (BlogCampaign blog : blogs) {
 			Logger.debug("Checking for blogs on :" + dW);
 			if (blog.emailOnDays.contains(dW)) {
 				Long blogAt = TimeUtil.getTime(blog.emailAtTime, blog.emailTZ);
 				if (blogAt > currTime && blogAt < currPlus3) {
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

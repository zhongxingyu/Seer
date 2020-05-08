 package jobs;
 
 import exception.NoAvailableTokenException;
 import exception.UserProtectedException;
 import graph.GraphDatabase;
 import graph.IMGraphDatabase;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.commons.io.FileUtils;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import models.FollowingList;
 import models.Item;
 import models.Link;
 import models.User;
 import models.UserGraph;
 import play.Logger;
 import play.Play;
 import play.cache.Cache;
 import play.db.jpa.JPA;
 import play.db.jpa.NoTransaction;
 import play.jobs.Every;
 import play.jobs.Job;
 import twitter.TwitterProxy;
 import twitter.TwitterProxyFactory;
 import util.LinkShortener;
 import util.UserLookup;
 
 public class GetFollowingsJob extends GraphJobBase {
 
 	private static int BATCH_OP_SIZE = 50;
 	public Long ownerId;
 	
 	public GetFollowingsJob(Long ownerId) {
 		this.ownerId = ownerId;
 	}
 	@Override
 	public void doJob() {
 		try {
 			getFollowings(ownerId);
 			GetFollowingsBootstrap.threadCounter--;
 		}
 		catch (Exception e) {
 			Logger.error(e.getMessage());
 		}
 
 	}
 	protected Set<Long> getFollowings(Long ownerId) {
 
 		FollowingList fl = FollowingList.getByOwnerId(ownerId);
 
 		Set<Long> followingList = new HashSet<Long>();
 
 
 		GraphDatabase.clearFollowings(ownerId);
 		followingList = new HashSet<Long>();
 
 		try {
 
 			TwitterProxy twitter = TwitterProxyFactory.defaultInstance();

			for (Long followingId : twitter.getFollowingIds(ownerId)) {

				followingList.add(followingId);
 			}
			GraphDatabase.addFriendships(ownerId,followingList);

			fl.setStatusSuccessful();
 
 		} catch (NoAvailableTokenException e) {	
 			fl.setStatusWaiting();
 			Logger.error(e, e.getMessage());
 
 		} catch (UserProtectedException e) {
 			fl.setStatusProtected();
 			Logger.info(e.getMessage());
 		}
 
 		catch (Exception e1) {
 			fl.setStatusWaiting();
 			Logger.error(e1, e1.getMessage());
 
 		}finally{
 	      try {
             fl.save();
          } catch (Exception e) {
             Logger.error(e, e.getMessage());
          }
 		   
 		}
 		return followingList;
 	}
 }

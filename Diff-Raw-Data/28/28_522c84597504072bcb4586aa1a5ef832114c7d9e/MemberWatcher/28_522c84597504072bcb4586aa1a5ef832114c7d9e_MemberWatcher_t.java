 package com.vzw.hackthon.scheduler;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.vzw.hackathon.Channel;
 import com.vzw.hackathon.GroupEvent;
 import com.vzw.hackathon.GroupEventManager;
 import com.vzw.hackathon.Member;
 import com.vzw.hackathon.MemberStatus;
 import com.vzw.hackathon.apihandler.SendVMAMessage;
 import com.vzw.util.db.DBManager;
 import com.vzw.util.db.DBPool;
 
 public class MemberWatcher implements Runnable {
 	private static final Logger		logger = Logger.getLogger(EventReminder.class);
 
 	private static final DBPool 	dbPool = DBManager.getDBPool();
 	
 	private static final int CHECK_INTERVAL_SECONDS		= 10;
 	
 	
 	private static final String SQL_SEL_MEMBERS = 
 			"select g.group_event_id, g.mdn, g.member_status, g.last_channel_id, u.name, u.channel_id"
 			+ " from GROUP_MEMBER g left outer join users u on g.mdn = u.mdn"
 			+ " where g.member_status = 'MASTER' or g.member_status = 'ACCEPTED'"
 			+ " order by g.GROUP_EVENT_ID, g.mdn";
 	
 		
 	private ScheduledExecutorService		executor = null;
 	
 
 	
 	public MemberWatcher() {
 	}
 	
 	public void start() {
 		
 		// check the database every 10 seconds
 		executor = Executors.newScheduledThreadPool(10);
 		executor.scheduleAtFixedRate(this, 1, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
 		
 		
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 
 			@Override
 			public void run() {
 				if (executor != null) {
 					executor.shutdown();
 				}
 			}
 			
 		});
 		
	}

	public void await() {
 		
 		try {
 			executor.awaitTermination(1, TimeUnit.DAYS);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
	}	
 	
 	private void watchMembers() {
 		
 		logger.info("Start watching");
 		
 		// get all the events first
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		
 		GroupEventManager gem = GroupEventManager.getInstance();
 		try {
 			
 			conn = dbPool.getConnection();
 			ps = conn.prepareStatement(SQL_SEL_MEMBERS);
 			rs = ps.executeQuery();
 			
 			
 			
 			int lastGeId = -1;
 			int curGeId = -1;
 			GroupEvent ge = null;
 			List<Member> memberList = null;
 			while (rs.next()) {
 				curGeId = rs.getInt("group_event_id");
 				
 				if (curGeId != lastGeId) {
 					// new group event
 					if (ge != null) {
 						processWatch(ge);
 					}
 					
 					ge = gem.loadGroupEventFromDb(curGeId);
 					memberList = new ArrayList<Member>();
 					ge.setMemberList(memberList);
 					
 					
 				}
 				
 				Member member = new Member();
 				member.setStatus(MemberStatus.valueOf(rs.getString("member_status")));
 				member.setChannelId(rs.getString("channel_id"));
 				member.setMdn(rs.getString("mdn"));
 				member.setName(rs.getString("name"));
 				member.setLastChannelId(rs.getString("last_channel_id"));
 				memberList.add(member);
 				
 				lastGeId = curGeId;
 					
 			}
 			
 			
 
 			// new group event
 			if (ge != null) {
 				processWatch(ge);
 			}
 
 		
 		}
 		catch (Exception e) {
 			logger.error("Failed to watch members", e);
 		}
 		finally {
 			DBManager.release(rs, ps, conn);
 		}
 	}
 	
 	private void processWatch(GroupEvent ge) {
 		logger.info("Got ge: " + ge);
 		
 		
 		GroupEventManager gem = GroupEventManager.getInstance();
 		String[] tos = ge.getMemberMdns();
 		String toStr = StringUtils.join(tos, ",");
 		for (Member m : ge.getMemberList()) {
 			Channel channel = gem.getChannel(m.getChannelId());
 			
 			logger.info("member: " + m);
 			
 			if (!StringUtils.equals(m.getChannelId(), m.getLastChannelId())) {
 				String msg = m.getName() + " changed channel to " + channel.getName();
 				logger.info("channel changed, to=" + toStr + ", msg = " + msg);
 				
 				// need to send the group message of this change
 				SendVMAMessage.sendMMS(m.getMdn(), toStr, msg);
 				
 				// update last channel id
 				gem.updateMemberLastChannelId(ge.getId(), m.getMdn(), m.getChannelId());
 				logger.info("updated lastChannelId to " + m.getChannelId());
 			}
 		}
 	}
 
 	@Override
 	public void run() {
 		watchMembers();
 		
 	}
 }

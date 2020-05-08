 /**
  * 
  */
 package com.k99k.wb.acts;
 
 import java.util.ArrayList;
 
 import com.k99k.khunter.Action;
 import com.k99k.khunter.ActionMsg;
 import com.k99k.khunter.dao.WBUserDao;
 
 /**
  * 发表msg的异步后续操作,作为任务执行.(附:此action的初始化中执行WBUserDao.initKCs)<br />
  * 依次执行以下操作：<br />
  * 1.处理话题;<br />
  * 2.处理提到的用户;<br />
  * 3.处理fans的消息推送;<br />
  * @author keel
  *
  */
 public class WBTalkTask extends Action {
 
 	/**
 	 * @param name
 	 */
 	public WBTalkTask(String name) {
 		super(name);
 	}
 
 	//private static WBUserDao dao = (WBUserDao)DaoManager.findDao("wbUserDao");
 	
 	
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public ActionMsg act(ActionMsg msg) {
 		//
 		long userId = (Long)msg.getData("userId");
 		String txt = (String)msg.getData("txt");
 		long msgId = (Long)msg.getData("msgId");
 		boolean isRT = (Boolean)msg.getData("isRT");
 		int state = (Integer)msg.getData("state");
 		Object rt_o = msg.getData("rt_id");
 		long rt_id = (rt_o==null)?0:(Long)rt_o;
		
 		if (isRT) {
 			long rt_userId = (Long)msg.getData("rt_userId");
 			String rt_name = (String) msg.getData("rt_name");
 			String source = (String)msg.getData("source");
 			String place = (String)msg.getData("place");
 			ArrayList<String> ts = (ArrayList<String>)msg.getData("topics");
 			ArrayList<String> ms = (ArrayList<String>)msg.getData("mentions");
 			//被转用户提到消息+1,addMention
 			WBUserDao.addMention(rt_name, rt_id);
 			//更新原消息的转发次数,addComm
 			WBUserDao.addComm(WBUserDao.newComm(), rt_id, rt_userId, userId, txt, source, place, ts, ms,true);
 		}
 		if (state == 0) {
			//更新消息发表者和sent
			WBUserDao.updateUserAndSentForNewMsg(userId, txt,msgId,isRT,rt_id);

 			//向所有的fans发送消息,处理fans的inbox
 			WBUserDao.pushMsgToFans(userId, msgId,state,rt_id);
 		}
 		
 		
 		
 		return super.act(msg);
 	}
 
 
 
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#exit()
 	 */
 	@Override
 	public void exit() {
 
 	}
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#getIniPath()
 	 */
 	@Override
 	public String getIniPath() {
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#init()
 	 */
 	@Override
 	public void init() {
 		
 		//此处初始化WBUserDao
 		WBUserDao.initKCs();
 	}
 
 }

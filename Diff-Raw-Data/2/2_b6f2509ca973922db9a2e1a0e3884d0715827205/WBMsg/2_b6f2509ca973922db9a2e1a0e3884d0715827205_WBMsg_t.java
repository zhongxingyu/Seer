 /**
  * 
  */
 package com.k99k.wb.acts;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 
 import com.k99k.khunter.Action;
 import com.k99k.khunter.ActionMsg;
 import com.k99k.khunter.HttpActionMsg;
 import com.k99k.khunter.KFilter;
 import com.k99k.khunter.KObject;
 import com.k99k.khunter.dao.WBUserDao;
 import com.k99k.tools.StringUtil;
 
 /**
  * 
  * @author keel
  *
  */
 public class WBMsg extends Action {
 
 	/**
 	 * @param name
 	 */
 	public WBMsg(String name) {
 		super(name);
 	}
 	
 	
 	private int pageSize = 20;
 	
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
 	 */
 	@Override
 	public ActionMsg act(ActionMsg msg) {
 		String subact = KFilter.actPath(msg, 2, "inbox");
 		HttpActionMsg httpmsg = (HttpActionMsg)msg;
 		
 		String re = null;
 		
 		
 		String uid_str = httpmsg.getHttpReq().getParameter("uid");
 		if (!StringUtil.isDigits(uid_str)) {
 			JOut.err(400, httpmsg);
 			return super.act(msg);
 		}
 		long userId = Long.parseLong(uid_str);
 		
 		
 		if (subact.equals("inbox")) {
 			String p_str = httpmsg.getHttpReq().getParameter("p");
 			String pz_str = httpmsg.getHttpReq().getParameter("pz");
 			int page = (StringUtil.isDigits(p_str))?Integer.parseInt(p_str):1;
 			int pageSize = (StringUtil.isDigits(pz_str))?Integer.parseInt(pz_str):this.pageSize;
 			
 			
 			//re = JSONTool.writeFormatedJsonString(WBUserDao.readOnePageMsgs(userId, page, pageSize));
 			re = writeKObjList(WBUserDao.readOnePageMsgs(userId, page, pageSize));
			 
 		}else if(subact.equals("unread")){
 			String max_str = httpmsg.getHttpReq().getParameter("max");
 //			if (!StringUtil.isDigits(max_str)) {
 //				JOut.err(400, httpmsg);
 //				return super.act(msg);
 //			}
 			int max = (StringUtil.isDigits(max_str))?Integer.parseInt(max_str):this.pageSize;
 			re = writeKObjList(WBUserDao.readUnReadMsgs(userId, max));
 		}else if(subact.equals("sent")){
 			String p_str = httpmsg.getHttpReq().getParameter("p");
 			String pz_str = httpmsg.getHttpReq().getParameter("pz");
 			int page = (StringUtil.isDigits(p_str))?Integer.parseInt(p_str):1;
 			int pageSize = (StringUtil.isDigits(pz_str))?Integer.parseInt(pz_str):this.pageSize;
 			
 			re = writeKObjList(WBUserDao.readSentMsgs(userId, page, pageSize));
 		}
 //		else if(subact.equals("one")){
 //			String msg_str = httpmsg.getHttpReq().getParameter("mid");
 //			if (!StringUtil.isDigits(msg_str) ) {
 //				JOut.err(400, httpmsg);
 //				return super.act(msg);
 //			}
 //			String p_str = httpmsg.getHttpReq().getParameter("p");
 //			String pz_str = httpmsg.getHttpReq().getParameter("pz");
 //			int page = (StringUtil.isDigits(p_str))?Integer.parseInt(p_str):1;
 //			int pageSize = (StringUtil.isDigits(pz_str))?Integer.parseInt(pz_str):this.pageSize;
 //			
 //			re = writeKObjList(WBUserDao.readOneMsgList(Long.parseLong(msg_str), page, pageSize));
 //		}
 		else if(subact.equals("comms")){
 			String msg_str = httpmsg.getHttpReq().getParameter("mid");
 			if (!StringUtil.isDigits(msg_str) ) {
 				JOut.err(400, httpmsg);
 				return super.act(msg);
 			}
 			re = writeKObjList(WBUserDao.readComms(Long.parseLong(msg_str), 1, 10));
 		}
 		
 		msg.addData("[print]", re);
 		return super.act(msg);
 	}
 
 
 	/**
 	 * 输出ArrayList<KObject>
 	 * @param list ArrayList<KObject>
 	 * @return String
 	 */
 	public static final String writeKObjList(ArrayList<KObject> list){
 		StringBuilder sb = new StringBuilder();
 		sb.append("[");
 		if (list == null || list.isEmpty()) {
 			sb.append("]");
 			return sb.toString();
 		}
 		for (Iterator<KObject> it = list.iterator(); it.hasNext();) {
 			KObject kobj = it.next();
 			sb.append(kobj.toString());
 			sb.append(",");
 		}
 		sb.deleteCharAt(sb.length()-1);
 		sb.append("]");
 		return sb.toString();
 	}
 
 
 
 	/**
 	 * @return the pageSize
 	 */
 	public final int getPageSize() {
 		return pageSize;
 	}
 
 
 	/**
 	 * @param pageSize the pageSize to set
 	 */
 	public final void setPageSize(int pageSize) {
 		this.pageSize = pageSize;
 	}
 
 
 }

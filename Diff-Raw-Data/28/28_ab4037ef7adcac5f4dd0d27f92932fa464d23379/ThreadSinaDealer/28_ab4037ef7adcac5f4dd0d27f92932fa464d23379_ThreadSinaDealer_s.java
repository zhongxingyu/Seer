 package com.zrd.zr.weiboes;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import weibo4android.Count;
 import weibo4android.Paging;
 import weibo4android.Status;
 import weibo4android.User;
 import weibo4android.WeiboException;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 
 public class ThreadSinaDealer implements Runnable {
 	
 	public static final int SHOW_USER = 0x7320001;
 	public static final int GET_USER_TIMELINE = 0x7320002;
 	public static final int CREATE_FRIENDSHIP = 0x7320003;
 	public static final int CREATE_FAVORITE = 0x7320004;
 	public static final int REPOST = 0x7320005;
 	public static final String KEY_DATA = "data";
 	public static final String KEY_SINA = "sina";
 	public static final String KEY_WEIBO_ERR = "err";
 	
 	private Sina mSina = null;
 	private int mAction;
 	private String[] mParams = null;
 	private Handler mHandler = null;
 	
 	/*
 	 * if parameter handler is null, or action is illegal,
 	 * then the instance of the class will do nothing, nothing at all!!
 	 * so, please make sure that the 2 parameters mentioned above
 	 * are available.
 	 */
 	public ThreadSinaDealer(Sina sina, int action, String[] params, Handler handler) {
 		this.mSina = sina;
 		this.mAction = action;
 		this.mParams = params;
 		this.mHandler = handler;
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		if (mSina == null) {
 			mSina = new Sina();
 		}
 		if (mAction != SHOW_USER
 			&& mAction != GET_USER_TIMELINE
 			&& mAction != CREATE_FRIENDSHIP
 			&& mAction != CREATE_FAVORITE
 			&& mAction != REPOST)
 			return;
 		if (mHandler == null) return;
 		
 		Message msg = new Message();
 		msg.what = mAction;
 		User user;
 		Status status;
		ArrayList<Sina.XStatus> statuses = new ArrayList<Sina.XStatus>();
 		Bundle bundle = new Bundle();
 		bundle.putSerializable(KEY_SINA, mSina);
 		bundle.putSerializable(KEY_DATA, null);
 		bundle.putSerializable(KEY_WEIBO_ERR, null);
 		switch (mAction) {
 		case SHOW_USER:
 			if (mParams != null && mParams.length != 1 && mParams[0] != null) return;
 			try {
 				user = mSina.getWeibo().showUser(mParams[0]);
 				bundle.putSerializable(KEY_DATA, user);
 			} catch (WeiboException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				bundle.putSerializable(KEY_WEIBO_ERR, e);
 			}
 			msg.setData(bundle);
 			mHandler.sendMessage(msg);
 			break;
 		case GET_USER_TIMELINE:
 			if (mParams != null && mParams.length != 3 && mParams[0] != null) return;
 			try {
 				Paging paging = new Paging(
 					Integer.parseInt(mParams[1]),
 					Integer.parseInt(mParams[2])
 				);
 				List<Status> list = mSina.getWeibo().getUserTimeline(mParams[0], paging);
 				String sids = "";
 				for (int i = 0; i < list.size(); i++) {
 					Sina.XStatus xstatus = mSina.getXStatus();
 					xstatus.setStatus(list.get(i));
					statuses.add(xstatus);
 					sids += list.get(i).getId();
 					if (i != list.size() - 1) sids += ",";
 				}
				List<Count> counts = mSina.getWeibo().getCounts(sids);
				for (int i = 0; i < counts.size() & i < statuses.size(); i++) {
					statuses.get(i).setComments(counts.get(i).getComments());
					statuses.get(i).setReposts(counts.get(i).getRt());
 				}
				bundle.putSerializable(KEY_DATA, statuses);
 			} catch (WeiboException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				bundle.putSerializable(KEY_WEIBO_ERR, e);
 			}
 			msg.setData(bundle);
 			mHandler.sendMessage(msg);
 			break;
 		case CREATE_FRIENDSHIP:
 			if (mParams != null && mParams.length != 1 && mParams[0] != null) return;
 			try {
 				if (mSina.getWeibo().existsFriendship("" + mSina.getLoggedInUser().getId(), mParams[0])) {
 					bundle.putSerializable(KEY_DATA, mSina.getLoggedInUser());
 				} else {
 					bundle.putSerializable(KEY_DATA, mSina.getWeibo().createFriendship(mParams[0]));
 				}
 			} catch (WeiboException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				bundle.putSerializable(KEY_WEIBO_ERR, e);
 			}
 			msg.setData(bundle);
 			mHandler.sendMessage(msg);
 			break;
 		case CREATE_FAVORITE:
 			if (mParams != null && mParams.length != 1 && mParams[0] != null) return;
 			try {
 				status = mSina.getWeibo().createFavorite(Long.parseLong(mParams[0]));
 				bundle.putSerializable(KEY_DATA, status);
 			} catch (WeiboException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				bundle.putSerializable(KEY_WEIBO_ERR, e);
 			}
 			msg.setData(bundle);
 			mHandler.sendMessage(msg);
 			break;
 		case REPOST:
 			if (mParams != null && mParams.length != 2 && mParams[0] != null) return;
 			try {
 				bundle.putSerializable(
 					KEY_DATA,
 					mSina.getWeibo().repost(mParams[0], mParams[1])
 				);
 			} catch (WeiboException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				bundle.putSerializable(KEY_WEIBO_ERR, e);
 			}
 			msg.setData(bundle);
 			mHandler.sendMessage(msg);
 			break;
 		}
 	}
 
 }

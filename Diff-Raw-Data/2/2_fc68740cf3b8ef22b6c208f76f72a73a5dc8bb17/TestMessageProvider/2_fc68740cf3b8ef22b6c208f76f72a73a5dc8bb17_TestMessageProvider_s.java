 package com.phdroid.smsb.storage;
 
 import com.phdroid.smsb.SmsPojo;
 import com.phdroid.smsb.TestSmsPojo;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Hashtable;
 import java.util.List;
 
 public class TestMessageProvider implements IMessageProvider {
 	private ArrayList<SmsPojo> mList;
 	private Hashtable<SmsPojo, SmsAction> mActions;
 	private int mUnreadCount = 0;
 
 	public TestMessageProvider() {
 		mActions = new Hashtable<SmsPojo, SmsAction>();
 		mList = new ArrayList<SmsPojo>();
 		Calendar c = Calendar.getInstance();
 
		SmsPojo sms = new TestSmsPojo();
 		sms.setMessage("Hey you there! How you doin'?");
 		sms.setSender("+380971122333");
 		sms.setReceived(c.getTime().getTime());
 		mList.add(sms);
 
 		sms = new TestSmsPojo();
 		sms.setMessage("Nova aktsia vid magazinu Target! Kupuy 2 kartopli ta otrymay 1 v podarunok!");
 		sms.setSender("TARGET");
 		c.add(Calendar.SECOND, -30);
 		sms.setReceived(c.getTime().getTime());
 		mList.add(sms);
 
 		sms = new TestSmsPojo();
 		sms.setMessage("This is my new number. B. Obama.");
 		sms.setSender("+1570333444555");
 		c.add(Calendar.MINUTE, -5);
 		sms.setReceived(c.getTime().getTime());
 		mList.add(sms);
 
 		sms = new TestSmsPojo();
 		sms.setMessage("How about having some beer tonight? Stranger.");
 		sms.setSender("+380509998887");
 		sms.setRead(true);
 		c.add(Calendar.MINUTE, -30);
 		sms.setReceived(c.getTime().getTime());
 		mList.add(sms);
 
 		sms = new TestSmsPojo();
 		sms.setMessage("Vash rakhunok na 9.05.2011 stanovyt 27,35 uah.");
 		sms.setSender("KYIVSTAR");
 		c.add(Calendar.MINUTE, -20);
 		sms.setReceived(c.getTime().getTime());
 		mList.add(sms);
 
 		sms = new TestSmsPojo();
 		sms.setMessage("Skydky v ALDO. Kupuy 1 ked ta otrymuy dryguy v podarunok!");
 		sms.setSender("ALDO");
 		c.add(Calendar.HOUR, -2);
 		sms.setReceived(c.getTime().getTime());
 		mList.add(sms);
 
 		sms = new TestSmsPojo();
 		sms.setMessage("Big football tonight v taverne chili!");
 		sms.setSender("+380991001010");
 		sms.setRead(true);
 		c.add(Calendar.HOUR, -30);
 		sms.setReceived(c.getTime().getTime());
 		mList.add(sms);
 
 		sms = new TestSmsPojo();
 		sms.setMessage("15:43 ZABLOKOVANO 17,99 UAH, SUPERMARKET PORTAL");
 		sms.setSender("PUMB");
 		c.add(Calendar.HOUR, -400);
 		sms.setReceived(c.getTime().getTime());
 		mList.add(sms);
 
 		sms = new TestSmsPojo();
 		sms.setMessage("Vash rakhunok na 8.05.2011 stanovyt 24 uah.");
 		sms.setSender("KYIVSTAR");
 		sms.setRead(true);
 		c.add(Calendar.MINUTE, -20);
 		sms.setReceived(c.getTime().getTime());
 		mList.add(sms);
 
 		mUnreadCount = 6;
 	}
 
 	public int size(){
 		return mList.size();
 	}
 
 	public List<SmsPojo> getMessages() {
 		return mList;
 	}
 
 	public Hashtable<SmsPojo, SmsAction> getActionMessages() {
 		return mActions;
 	}
 
 	public void delete(long id) {
 		mActions.clear();
 		SmsPojo sms = get(id);
 		if (sms != null){
 			mActions.put(sms, SmsAction.Deleted);
 			mList.remove((int) id);
 		}
 	}
 
 	public void delete(long[] ids) {
 		mActions.clear();
 		for (long id = ids.length - 1; id >= 0; id--) {
 			SmsPojo sms = get(ids[(int)id]);
 			if (sms != null){
 				if (!sms.isRead())
 					mUnreadCount--;
 				mActions.put(sms, SmsAction.Deleted);
 				mList.remove((int) id);
 			}
 		}
 	}
 
 	public void deleteAll() {
 		for (SmsPojo sms : mList) {
 			mActions.put(sms, SmsAction.Deleted);
 		}
 		mList.clear();
 	}
 
 	public void notSpam(long id) {
 		mActions.clear();
 		SmsPojo sms = get(id);
 		if(sms != null){
 			mActions.put(sms, SmsAction.MarkedAsNotSpam);
 			mList.remove((int) id);
 		}
 	}
 
 	public void notSpam(long[] ids) {
 		mActions.clear();
 		for (long id = ids.length - 1; id >= 0; id--) {
 			SmsPojo sms = get(ids[(int)id]);
 			if (sms != null){
 				if (!sms.isRead())
 					mUnreadCount--;
 				mActions.put(sms, SmsAction.MarkedAsNotSpam);
 				mList.remove((int) id);
 			}
 		}
 	}
 
 	@Override
 	public int getIndex(SmsPojo message) {
 		return mList.indexOf(message);
 	}
 
 	@Override
 	public boolean isFirstMessage(SmsPojo message) {
 		return mList.indexOf(message) == 0;
 	}
 
 	@Override
 	public boolean isLastMessage(SmsPojo message) {
 		return mList.indexOf(message) == mList.size()-1;
 	}
 
 	@Override
 	public SmsPojo getPreviousMessage(SmsPojo message) {
 		int index = mList.indexOf(message);
 		if (index <= 0) {
 			return null;
 		}
 		return mList.get(--index);
 	}
 
 	@Override
 	public SmsPojo getNextMessage(SmsPojo message) {
 		int index = mList.indexOf(message);
 		if (index >= mList.size()-1) {
 			return null;
 		}
 		return mList.get(++index);
 	}
 
 	public SmsPojo getMessage(long id) {
 		return get(id);
 	}
 
 	public void read(long id) {
 		SmsPojo smsPojo = get(id);
 		if (smsPojo != null && !smsPojo.isRead()) {
 			smsPojo.setRead(true);
 			mUnreadCount--;
 		}
 	}
 
 	public int getUnreadCount() {
 		return mUnreadCount;
 	}
 
 	public void undo() {
 		for (SmsPojo sms : mActions.keySet()) {
 			mList.add(0, sms);
 		}
 		mActions.clear();
 
 		mUnreadCount = 0;
 		for (SmsPojo sms : mList) {
 			if (!sms.isRead()) {
 				mUnreadCount++;
 			}
 		}
 	}
 
 	public void performActions() {
 		mActions.clear();
 	}
 
 	private SmsPojo get(long id) {
 		if(id < 0) return null;
 		if(id > mList.size()) return null;
 		return mList.get((int) id);
 	}
 }

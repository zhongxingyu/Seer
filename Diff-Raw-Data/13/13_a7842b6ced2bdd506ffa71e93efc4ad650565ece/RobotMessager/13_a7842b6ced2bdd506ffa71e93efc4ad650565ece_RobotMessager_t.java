 package util;
 
 import models.Paster;
 import net.sf.jml.Email;
 import net.sf.jml.MsnContact;
 import net.sf.jml.MsnContactPending;
 import net.sf.jml.MsnGroup;
 import net.sf.jml.MsnList;
 import net.sf.jml.MsnMessenger;
 import net.sf.jml.MsnSwitchboard;
 import net.sf.jml.event.MsnContactListListener;
 import net.sf.jml.event.MsnMessageListener;
 import net.sf.jml.event.MsnMessengerListener;
 import net.sf.jml.impl.MsnMessengerFactory;
 import net.sf.jml.message.MsnControlMessage;
 import net.sf.jml.message.MsnDatacastMessage;
 import net.sf.jml.message.MsnInstantMessage;
 import net.sf.jml.message.MsnSystemMessage;
 import net.sf.jml.message.MsnUnknownMessage;
 import net.sf.jml.message.p2p.MsnP2PMessage;
 import play.Logger;
 
 public class RobotMessager {
 	//sso login.live.com
 	private static RobotMessager instance;
 	private MsnMessenger messenger;
 
	public synchronized static void init(String email, String password) {
 		if (instance == null) {
 			instance = new RobotMessager();
 			instance.messenger = MsnMessengerFactory.createMsnMessenger(email, password);
 			instance.initListener();
 		}
 	}
 
 	public static void login() {
 		Logger.info("登陆");
 		instance.messenger.login();
 	}
 
 	public static void sendMessage(String email, String text) {
 		Logger.info("send text %s %s", email,text );
 		instance.messenger.sendText(Email.parseStr(email), text);
 	}
 
 	public static void logout() {
 		instance.messenger.logout();
 	}
 
 	private void initListener() {
 		instance.messenger.addMessageListener(new MsnMessageListener() {
 
 			
 			public void unknownMessageReceived(MsnSwitchboard arg0, MsnUnknownMessage arg1, MsnContact arg2) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void systemMessageReceived(MsnMessenger arg0, MsnSystemMessage arg1) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void p2pMessageReceived(MsnSwitchboard arg0, MsnP2PMessage arg1, MsnContact arg2) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void offlineMessageReceived(String arg0, String arg1, String arg2, MsnContact arg3) {
 				// TODO Auto-generated method stub
 
 			}
 
 			public void instantMessageReceived(MsnSwitchboard arg0, MsnInstantMessage arg1, MsnContact arg2) {
 				String emailAddress = arg2.getEmail().getEmailAddress();
 				String content = arg1.getContent();
 				Logger.debug("接受消息 %s \n %s", emailAddress,content);
 				Paster.createAndSave(content,emailAddress);
 			}
 
 			
 			public void datacastMessageReceived(MsnSwitchboard arg0, MsnDatacastMessage arg1, MsnContact arg2) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void controlMessageReceived(MsnSwitchboard arg0, MsnControlMessage arg1, MsnContact arg2) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 		instance.messenger.addContactListListener(new MsnContactListListener() {
 
 			
 			public void ownerStatusChanged(MsnMessenger arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void ownerDisplayNameChanged(MsnMessenger arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void groupRemoveCompleted(MsnMessenger arg0, MsnGroup arg1) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void groupAddCompleted(MsnMessenger arg0, MsnGroup arg1) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void contactStatusChanged(MsnMessenger arg0, MsnContact arg1) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void contactRemovedMe(MsnMessenger arg0, MsnContact arg1) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void contactRemoveFromGroupCompleted(MsnMessenger arg0, MsnContact arg1, MsnGroup arg2) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void contactRemoveCompleted(MsnMessenger arg0, MsnContact arg1, MsnList arg2) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void contactPersonalMessageChanged(MsnMessenger arg0, MsnContact arg1) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void contactListSyncCompleted(MsnMessenger arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void contactListInitCompleted(MsnMessenger arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void contactAddedMe(MsnMessenger arg0, MsnContactPending[] arg1) {
 				for (MsnContactPending msnContactPending : arg1) {
 					Logger.info("添加好友%s %s", msnContactPending.getEmail(), msnContactPending.getDisplayName());
 					arg0.addFriend(msnContactPending.getEmail(), msnContactPending.getDisplayName());
 				}
 			}
 
 			
 			public void contactAddedMe(MsnMessenger arg0, MsnContact arg1) {
 				Logger.info("添加好友%s %s", arg1.getEmail(), arg1.getFriendlyName());
 				arg0.addFriend(arg1.getEmail(), arg1.getFriendlyName());
 			}
 
 			
 			public void contactAddInGroupCompleted(MsnMessenger arg0, MsnContact arg1, MsnGroup arg2) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void contactAddCompleted(MsnMessenger arg0, MsnContact arg1, MsnList arg2) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 		instance.messenger.addMessengerListener(new MsnMessengerListener() {
 
 			
 			public void logout(MsnMessenger arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			
 			public void loginCompleted(MsnMessenger arg0) {
 				Logger.info("登录成功");
 			}
 
 			
 			public void exceptionCaught(MsnMessenger arg0, Throwable arg1) {
 				Logger.error(arg1,"异常");
 			}
 		});
 	}
 }

 package server;
 
 import gui.drawings.Drawing;
 
 import java.awt.Color;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.SocketChannel;
 import java.util.Hashtable;
 
 import network.s2c.DrawingMessage;
 import network.s2c.ErrorNoticeMessage;
 import network.s2c.LogInResponse;
 import network.s2c.OpenColorDialogMessage;
 import network.s2c.OpenPanelMessage;
 import network.s2c.S2CEmailMessage;
 import network.s2c.TreeStatusChange;
 import network.s2c.UpdateDrawings;
 import network.s2c.UpdateHistory;
 import network.s2c.UpdateLegend;
 import network.s2c.UpdateTextMessage;
 import network.s2c.UserStatusChange;
 
 public class ServerMediator {
 	Server serv;
 	SAuthenticator sauth;
 	GroupManager gm;
 	Hashtable<SocketChannel,String> hash;
 	public ServerMediator(Server s) {
 		serv = s;
 		sauth = new SAuthenticator();
 		gm = new GroupManager();
 		hash = new Hashtable<SocketChannel, String>();
 	}
 
 	public void login(SelectionKey key, String user, String pass) {
 		boolean valid = sauth.authenticate(user, pass) && !gm.userConnected(user); 
 		serv.write(key,new LogInResponse(user,valid));
 		if (valid) {
 			gm.connectUser(user);
 			serv.broadcast(new UserStatusChange(gm.getListModel()));
 			serv.write(key, new TreeStatusChange(gm.getTreeModel()));
 			hash.put((SocketChannel) key.channel(), user);
		}
 	}
 
 	public void disconnectUser(SocketChannel chan) {
 		this.logOff(null, hash.get(chan));
 	}
 	
 	public void logOff(SelectionKey key, String user) {
 		gm.logOffUser(user);
 		serv.broadcast(new UserStatusChange(gm.getListModel()));
 		serv.broadcast(new TreeStatusChange(gm.getTreeModel()));
 		
 		for (int i=0;i<gm.groups.size();i++)
 			if (gm.getColor(user, gm.groups.get(i).getName())!=null)
 				serv.broadcast(new UpdateLegend(gm.groups.get(i).getName(), gm.getGroupLegend(gm.groups.get(i).getName())));
 	}
 
 	public void newGroup(SelectionKey key, String groupName, String userName, Color c) {
 		gm.addGroup(groupName, userName,c); 
 		serv.broadcast(new TreeStatusChange(gm.getTreeModel()));
 		serv.write(key, new OpenPanelMessage(userName,groupName, c));
 	}
 
 	public void joinGroup(SelectionKey key, String groupName, String userName, Color c, String email) {
 		if (gm.joinGroupCommand(userName, groupName, c)) {
 			serv.broadcast(new TreeStatusChange(gm.getTreeModel()));
 			serv.write(key, new OpenPanelMessage(userName,groupName, c));
 			serv.broadcast(new UpdateLegend(groupName, gm.getGroupLegend(groupName)));
 		} else
 			serv.write(key, new ErrorNoticeMessage("Could not join group"));
 	}
 
 	public void leaveGroup(SelectionKey key, String groupName, String userName) {
 		gm.leaveGroupCommand(userName, groupName);
 		serv.broadcast(new TreeStatusChange(gm.getTreeModel()));
 		serv.broadcast(new UpdateLegend(groupName, gm.getGroupLegend(groupName)));
 	}
 
 	public void addUser(SelectionKey key, String groupName, String userName, String by) {
 		String s = gm.addUserCommand(by, userName, groupName);
 		if (s==null) {
 			serv.broadcast(new TreeStatusChange(gm.getTreeModel()));
 			serv.broadcast(new OpenPanelMessage(userName,groupName, gm.getColor(userName, groupName)));
 			serv.broadcast(new UpdateLegend(groupName, gm.getGroupLegend(groupName)));
 		} else
 			serv.write(key, new ErrorNoticeMessage(s));
 	} 
 
 	public void probeGroupDialog(SelectionKey k, String user, String group) {
 		if (gm.getColor(user, group)==null)
 			serv.write(k, new OpenColorDialogMessage(gm.getAvailableColors(group),group));
 		else
 			joinGroup(k,group,user,Color.black, null); // Color is already set.
 	}
 
 	public void getGroupHistory(SelectionKey k, String groupName) {
 		serv.write(k, new UpdateHistory(groupName, gm.getDocument(groupName)));
 	} 
 	
 	public void getGroupLegend(SelectionKey k, String groupName) {
 		serv.write(k, new UpdateLegend(groupName, gm.getGroupLegend(groupName)));
 	}
 	
 	public void getGroupDrawings(SelectionKey k, String groupName) {
 		serv.write(k, new UpdateDrawings(groupName, gm.getDrawings(groupName)));
 	}
 
 	public void addDrawing(String username, String group, Drawing d) {
 		gm.addDrawing(username, group, d);
 		serv.broadcast(new DrawingMessage(group, d));
 	}
 	
 	public void addTextMessage(String groupName, String userName, String text, int fontSize, Color fontColor) {
 		gm.addTextMessage(groupName, userName, text, fontSize,fontColor);
 		serv.broadcast(new UpdateTextMessage(groupName, userName, text, fontSize, fontColor));
 	}
 
 	public void receivedEmail(String email, String group) {
 		serv.broadcast(new S2CEmailMessage(email, group));
 	} 
 	
 }

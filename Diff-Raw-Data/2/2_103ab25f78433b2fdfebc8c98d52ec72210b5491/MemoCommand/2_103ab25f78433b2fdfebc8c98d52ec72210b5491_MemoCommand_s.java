 package org.siery.irc.command;
 
 import java.util.Date;
 import java.util.List;
 
 import org.siery.irc.action.memo.Memo;
 import org.siery.irc.action.memo.MemoHolder;
 import org.siery.irc.command.abstracts.ArgumentCommand;
 import org.siery.irc.command.abstracts.UserCommand;
 import org.siery.irc.user.ChannelUser;
 import org.siery.irc.user.User;
 
 public class MemoCommand extends UserCommand implements ArgumentCommand {
 
 	private int numberOfNicks;
 	private List<String> args = null;
 	
 	@Override
 	public void setAgruments(List<String> args) {
 		this.args = args;
 	}
 
 	@Override
 	public String getCommand() {
 		return "memo";
 	}
 
 	@Override
 	public String getUsage() {
 		return getCommandPrefix() + getCommand() + " nick[, nick2, nick3, ...] wiadomość";
 	}
 
 	@Override
 	public String getInfo() {
 		return "Pozwala na pozostawienie wiadomości dla nieobecnego użytkownika. " +
 				"Można podać kilka nicków, rozdzielając je przecinkami";
 	}
 
 	@Override
 	protected void onSuccess() {
 		if(args.size() < 2) {
 			getContext().sendMessage("Podaj wiadomość");
 		} else {
 			addNewMemo();
 		}
 	}
 
 	private void addNewMemo() {
 		
 		numberOfNicks = getNumberOfNicks();
 		
 		if(numberOfNicks == args.size()) {
 			getContext().sendMessage("Podaj wiadomość");
 		} else {
 			addMemoForAllUsers();
 		}
 		
 	}
 
 	private void addMemoForAllUsers() {
 		String infoNicks = "";
 		
 		for(int i=0; i<numberOfNicks; i++) {
 			String nick = args.get(i);
 			addMemoFor(nick.replace(",", ""));
 			infoNicks += nick + " ";
 		}
 		
 		getContext().sendMessage(getContext().getUser().getNick() + ", dodano memo dla " + infoNicks);
 	}
 
 	private int getNumberOfNicks() {
 		int numberOfNicks = 1;
 		for(int i=0; i<args.size(); i++) {
 			if(args.get(i).endsWith(","))
 				numberOfNicks = i+2;
 		}
 		return numberOfNicks;
 	}
 	
 	
 
 	private void addMemoFor(String nick) {
 		User user = new User(nick, "*", "*");
 		ChannelUser channelUser = getChannelUser(user);
 		
 		Memo memo = createNewMemo(channelUser);
 		MemoHolder.getInstance().addMemo(memo);
 	}
 
 	private Memo createNewMemo(ChannelUser channelUser) {
 		Memo memo = new Memo();
 		memo.setDate(new Date());
 		memo.setNewMemo(true);
 		memo.setReciever(channelUser);
 		memo.setSender(getContext().getChannelUser());
 		
 		String message = "";
 		
 		for(int i=numberOfNicks; i<args.size(); i++)
 			message += args.get(i) + " ";
 		
 		memo.setMessage(message);
 		return memo;
 	}
 
 	private ChannelUser getChannelUser(User user) {
 		String channel = getContext().getChannel();
 		String server = getContext().getServer();
 		ChannelUser channelUser = new ChannelUser(user, channel, server);
 		return channelUser;
 	}
 
 	@Override
 	protected void onFailiture() {}
 
 }

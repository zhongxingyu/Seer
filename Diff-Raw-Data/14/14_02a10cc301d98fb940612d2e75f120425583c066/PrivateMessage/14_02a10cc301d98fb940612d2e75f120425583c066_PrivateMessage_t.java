 package com.titankingdoms.dev.titanchat.core.participant.privmsg;
 
 import com.titankingdoms.dev.titanchat.core.channel.Channel;
 import com.titankingdoms.dev.titanchat.core.channel.info.Range;
 import com.titankingdoms.dev.titanchat.core.channel.info.Status;
 import com.titankingdoms.dev.titanchat.core.participant.Participant;
 import com.titankingdoms.dev.titanchat.format.Format;
 
 public final class PrivateMessage extends Channel {
 	
 	public PrivateMessage(Participant participant) {
 		super(participant.getName(), "Conversation", Status.CONVERSATION);
 	}
 	
 	@Override
 	public String[] getAliases() {
 		return new String[0];
 	}
 	
 	@Override
 	public String getDisplayColour() {
 		return plugin.getConfig().getString("pm.display-colour", "");
 	}
 	
 	@Override
 	public String getFormat() {
 		return plugin.getConfig().getString("pm.format", Format.getFormat());
 	}
 	
 	@Override
 	public Range getRange() {
 		return Range.CHANNEL;
 	}
 	
 	@Override
 	public String getTag() {
 		return "";
 	}
 	
 	@Override
 	public boolean isParticipating(String name) {
 		return (name != null) ? name.equalsIgnoreCase(getName()) : false;
 	}
 	
 	@Override
	public void join(Participant participant) {
		if (participant != null && participant.getName().equals(getName()))
			super.join(participant);
	}
 	
 	@Override
 	public void leave(Participant participant) {}
 }

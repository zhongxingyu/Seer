 import java.io.*;
 import java.util.*;
 
 public class WantedPlayer implements Serializable {
 	
 	private static final long serialVersionUID = 8416404587916155554L;
 	private String playerUsername = null;
 	private List<PlayerReward> playerRewards = new ArrayList<PlayerReward>();
 	
 	WantedPlayer(String playerUsername, PlayerReward playerReward) {
 		this.playerUsername = playerUsername;
 		this.playerRewards.add(playerReward);
 	}
 	
 	WantedPlayer(String playerUsername, List<PlayerReward> playerRewards) {
 		this.playerUsername = playerUsername;
 		this.playerRewards = playerRewards;
 	}
 	
 	public String getName() {
 		return this.playerUsername;
 	}
 	
 	public void setName(String playerUsername) {
 		this.playerUsername = playerUsername;
 	}
 	
 	public void addReward(PlayerReward playerReward) {
 		playerRewards.add(playerReward);
 	}
 	
 	public void removeReward(String playerUsername) {
		for (Iterator<PlayerReward> it = playerRewards.iterator(); it.hasNext();) {
			PlayerReward reward = it.next();
 			if (reward.getPlacer().equalsIgnoreCase(playerUsername)) {
				it.remove();
 			}
 		}
 	}
 
 	public List<PlayerReward> getPlayerRewards() {
 		return playerRewards;
 	}
 
 	public void setPlayerRewards(List<PlayerReward> playerRewards) {
 		this.playerRewards = playerRewards;
 	}
 	
 	public Integer getRewardCount() {
 		return playerRewards.size();
 	}
 	
 }

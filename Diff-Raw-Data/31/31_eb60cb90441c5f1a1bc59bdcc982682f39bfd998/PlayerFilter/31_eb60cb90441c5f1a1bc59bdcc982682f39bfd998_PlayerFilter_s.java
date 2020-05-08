 package tk.blackwolf12333.grieflog.utils.filters;
 
 import tk.blackwolf12333.grieflog.data.BaseData;
 
 public class PlayerFilter implements Filter {
 
 	String player;
 	
 	public PlayerFilter(String player) {
 		this.player = player;
 	}
 	
 	@Override
 	public boolean doFilter(BaseData data) {
		return data.getPlayerName().equals(player);
 	}
 }

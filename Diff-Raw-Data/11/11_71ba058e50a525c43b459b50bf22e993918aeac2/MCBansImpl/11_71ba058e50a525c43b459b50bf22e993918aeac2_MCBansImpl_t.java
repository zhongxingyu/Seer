 /**
  * 
  */
 package org.morganm.loginlimiter.bans;
 
 import java.util.HashMap;
 
 import com.mcbans.firestar.mcbans.BukkitInterface;
 import com.mcbans.firestar.mcbans.pluginInterface.Connect;
 
 /**
  * @author morganm
  *
  */
 public class MCBansImpl implements BanInterface {
 	private BukkitInterface mcbans;
 	
 	private HashMap<String, Boolean> banCache = new HashMap<String,Boolean>();
 	
 	public MCBansImpl(BukkitInterface mcbans) {
 		this.mcbans = mcbans;
 	}
 
 	public void updateCache(String playerName, boolean isBanned) {
 		banCache.put(playerName, Boolean.valueOf(isBanned));
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.morganm.loginlimiter.bans.BanInterface#isBanned(java.lang.String)
 	 */
 	@Override
 	public boolean isBanned(String playerName, String playerIP) {
 		if( !mcbans.isEnabled() || !mcbans.isInitialized() )
 			return false;
 		
 		Boolean banned = banCache.get(playerName);
 		if( banned == null )
 			banned = Boolean.FALSE;
		else
			return banned;		// return cached value
 		
 		Connect playerConnect = new Connect( mcbans );
 		String result = playerConnect.exec( playerName, playerIP );
 		if( result != null ){
 			banned = Boolean.TRUE;
 			banCache.put(playerName, banned);
 		}
 		else
 			banCache.put(playerName, Boolean.FALSE);
 		
 		return banned.booleanValue();
 	}
 
 }

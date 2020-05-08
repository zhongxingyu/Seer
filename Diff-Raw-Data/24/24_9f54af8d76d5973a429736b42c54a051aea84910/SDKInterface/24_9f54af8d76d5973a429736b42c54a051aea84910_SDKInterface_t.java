 package org.tophat.qrzar.sdkinterface;
 
 import java.util.HashMap;
 
import org.tophat.QRzar.mapper.AliveMapper;
 import org.tophat.QRzar.mapper.KillMapper;
 import org.tophat.QRzar.mapper.PlayerMapper;
 import org.tophat.QRzar.models.Alive;
 import org.tophat.QRzar.models.Kill;
 import org.tophat.QRzar.models.Player;
 import org.tophat.android.exceptions.HttpException;
 import org.tophat.android.mapping.Game;
 import org.tophat.android.model.ApiTokenMapper;
 import org.tophat.android.networking.ApiCommunicator;
 import org.tophat.qrzar.activities.mainactivity.MainActivity;
 
 import android.util.Log;
 
 public class SDKInterface 
 {
 
 	private static final String TAG = SDKInterface.class.getSimpleName();
 	private ApiCommunicator apic;
 	private Integer score = 0;
 	
 	public SDKInterface()
 	{
 		apic = new ApiCommunicator(new Constants());
 	}
 	
 	/**
 	 * Test 1
 	 * @return
 	 */
 	public void anonymous_connect() throws HttpException
 	{		
 		ApiTokenMapper atm = new ApiTokenMapper(apic);
 		apic.setApitoken(atm.getAnonymousToken());
 	}
 	
 	/**
 	 * Test 2
 	 */
 	public Player joinGame(String qrCode, Integer gameId) throws HttpException
 	{
 		  Game g = new Game();
 		  
 		  g.setId(gameId);
 		  
 		  Player p = new Player();
 		  p.setGame(g);
 		  p.setName("I'm the best");
 		  p.setQrcode(qrCode);
 		  
 		  PlayerMapper pm = new PlayerMapper(apic);
 
 		  return (Player)pm.create(p);
 	}
 	
 	/**
 	 * This method provides direct access to the kill request of the server.
 	 * @param killer
 	 * @param victimCode
 	 * @throws HttpException
 	 */
 	public void kill(Player killer, String victimCode) throws HttpException
 	{
 		  Kill k = new Kill();
 		  k.setKiller(killer);
 		  k.setVictimQrcode(victimCode);
 		  
 		  KillMapper km = new KillMapper(apic);
 
 		  km.create(k); 
 	}
 	
 	/**
 	 * Test 4
 	 * @throws HttpException 
 	 */
 	public void respawn(Player me) throws HttpException
 	{	  
 		PlayerMapper pm = new PlayerMapper(apic);
 		
 		me.setRespawn_code("RESPAW1");
 		me.setAccessUrl("players");
 		pm.update(me);
 	}
 	
 	
 	/**
 	 * Added to class for full decoupling
 	 */
 	
 	private String mTShirtCode;
 	private int mGameCode;
 	private Player mPlayer;
 	
 	
 	public SDKInterface(Player player)
 	{
 		mPlayer = player;
 		apic = new ApiCommunicator(new Constants());
 	}
 	
 	public HashMap<String,Integer> getTeamScoresAndRemainingTime()
 	{
 		
 		HashMap<String,Integer> map = new HashMap<String,Integer>();
 		map.put("team1Score", 3400);
 		map.put("team2Score", 5000);
 		map.put("timer", 290);
 		return map;
 	}
 	
 	public void setGameCode(int gameCode){
 		mGameCode = gameCode;
 	}
 	
 	public void setTShirtCode(String tShirtCode){
 		mTShirtCode = tShirtCode;
 	}
 	
 	public boolean playerIsAlive()
 	{
		AliveMapper am = new AliveMapper(this.apic);
		
		Alive alive = new Alive();
		alive.setId(mPlayer.getId());
 		
 		try {
			alive = (Alive)am.get(alive);
 		} catch (HttpException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
		this.score = alive.getScore();
		
		boolean aliveBool = alive.getAlive();
 		
		Log.i(TAG, "Alive:"+aliveBool);
		return aliveBool;
 	}
 	
 	public boolean joinGame(){
 		if(mGameCode==0||!isValidPlayerCode(mTShirtCode))
 				return false;
     	try {
 			mPlayer = this.joinGame(mTShirtCode, mGameCode);
 			return true;
 		} catch (HttpException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
     }
 	
 	public org.tophat.QRzar.models.Player getPlayer(){
 		return mPlayer;
 	}
 	
 	public boolean validToProcessTShirt(String s){
 		return isValidPlayerCode(s)&&mTShirtCode==null;
 	}
 	
 	public boolean validToProcessGameCode(String s){
 		return isValidGameCode(s)&&mTShirtCode!=null;
 	}
 	
 	/**
 	 * Static validation / parsing methods
 	 */
 	
 	
     public static boolean isValidPlayerCode(String s){
     	if(s.length()!=6)
     		return false;
     	if(!Character.isUpperCase(s.charAt(0)))
     		return false;
    
     	return true;
     }
     
     public static boolean isValidGameCode(String s){
     	try{
     		Integer.parseInt(s);
     		return true;
     	}catch(Exception e){
     		return false;
     	}
     }
     
     
     public static int decodeGameCode(String s){
     	return Integer.parseInt(s);
     }
     
     
     
     
 	
 }

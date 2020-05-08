 /**
  * TagGame - Package: syam.taggame.enums
  * Created: 2012/11/07 19:15:08
  */
 package syam.taggame.enums;
 
 /**
  * GameTeam (GameTeam.java)
  * @author syam(syamn)
  */
 public enum GameTeam {
	TAGGER ("鬼", 35, 5, "&4"),
 	RUNNER ("プレイヤー", 35, 14, "&a"),
 	;
 
 	private String teamName;
 	private int blockID;
 	private byte blockData;
 	private String colorTag;
 
 	GameTeam(final String teamName, int blockID, int blockData, String colorTag){
 		this.teamName = teamName;
 
 		// 例外回避
 		if (blockID < 0)
 			blockID = 0;
 		if (blockData < 0 || blockData > 127)
 			blockData = 0;
 
 		this.blockID = blockID;
 		this.blockData = (byte) blockData;
 		this.colorTag = colorTag;
 	}
 
 	public String getTeamName(){
 		return teamName;
 	}
 
 	public int getBlockID(){
 		return blockID;
 	}
 	public byte getBlockData(){
 		return blockData;
 	}
 
 	public String getColor(){
 		return colorTag;
 	}
 }

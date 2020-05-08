 package tiles;
 
 public class WallTile extends BoardTile{
 	public WallTile(){
 		super(false, false, '#');
 	}
	
 	public String getFilePath() {
 		return getFilePrefix()+"wall16x16.png";
 	}
}

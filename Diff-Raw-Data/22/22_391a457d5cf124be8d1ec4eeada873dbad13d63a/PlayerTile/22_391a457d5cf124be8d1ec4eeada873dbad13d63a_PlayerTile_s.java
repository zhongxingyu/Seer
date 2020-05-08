 package tiles;
 
 public class PlayerTile extends BoardTile{
 
 	public PlayerTile() {
		super(false, true, '@');
 	}
 	
 	@Override
 	public String getFilePath() {
		return getFilePrefix()+"movable16x16.png";
 	}
 }

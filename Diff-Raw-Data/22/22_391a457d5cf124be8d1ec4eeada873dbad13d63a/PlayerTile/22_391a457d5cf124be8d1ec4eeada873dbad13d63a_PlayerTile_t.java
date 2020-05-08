 package tiles;
 
 public class PlayerTile extends BoardTile{
 
 	public PlayerTile() {
		super(true, true, '@');
 	}
 	
 	@Override
 	public String getFilePath() {
		return getFilePrefix()+"mover16x16.png";
 	}
 }

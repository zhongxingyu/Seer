 package tiles;
 
 public class TargetTile extends BoardTile{
 	public TargetTile(){
		super(true, false, '+');
 	}
 
 	public String getFilePath() {
 		return getFilePrefix()+"target16x16.png";
 	}
 }

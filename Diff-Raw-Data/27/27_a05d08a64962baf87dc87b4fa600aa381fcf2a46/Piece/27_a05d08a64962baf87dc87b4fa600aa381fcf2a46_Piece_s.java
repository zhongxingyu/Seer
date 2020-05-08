 package twelve.team;
 
 import java.awt.Image;
 
 public class Piece {
 	
 	public Image image;
 	public int x;
 	public int y;
 	public boolean sacrificed = false;
 	
 	public enum Team {BLACK, WHITE }
 	
 	private Team team;
 	
 	public Piece() {
 		team = null;
 	}
 	
 	public Piece(Team t) {
 		team = t;
 	}
 	
 	public Team getOppositeTeam()
 	{
 		switch(this.team)
 		{
 		case WHITE:
 			return Team.BLACK;
 		case BLACK:
 			return Team.WHITE;
 		default:
 				return null;
 		}
 	}
 	
 	public Team getTeam() {
 		return team;
 	}
 }

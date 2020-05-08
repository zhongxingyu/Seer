 package project;
 
 import project.Game.WallDirections;
 
 public interface Board {
 	//abstract board for quoridor game
 	
 	public void MakeWall (int Xpos, int YPos, WallDirections d);
 
 
 	public boolean isUnique (Board newBoard);
	
	public boolean isEdge;
 }

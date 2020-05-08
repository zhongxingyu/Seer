 package fi.rinkkasatiainen.doos;
 
 public interface Cell {
 
	boolean isAlive();
	
	void addNeighbour(Cell c);
 
	void born();

	void die();

	void refreshAliveStatus();

	void setAliveNeighboursCount(int i);

	int getAliveNeighboursCount();

	void setANeighbourDead();
}

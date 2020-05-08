 package adp2.interfaces;
 
 import java.util.List;
 
 public interface Ant {
	List<Integer> traveledPath();
 	int position();
 	void step();
 	boolean hasFinished();
 	int weglaenge();
 	double alpha();
 	int getWaitingTime();
 	
 }
 
 

 package adp2.implementations;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import adp2.interfaces.Ant;
import adp2.interfaces.Path;
 
 public class NaA implements Ant {
 	public static NaA instance;
 
 	public static Ant valueOf() {
 		if (instance == null) {
             instance = new NaA();
         }
         return instance;
 	}
 
 	private NaA() {}
 
 	@Override
	public Path traveledPath() {
		return Values.NaP();
 	}
 
 	@Override
 	public int position() {
 		return -1;
 	}
 
 	@Override
 	public void step() {}
 
 	@Override
 	public boolean hasFinished() {
 		return false;
 	}
 
 	@Override
 	public int weglaenge() {
 		return -1;
 	}
 
 	@Override
 	public double alpha() {
 		return 0;
 	}
 
 	@Override
 	public int getWaitingTime() {
 		return 0;
 	}
 }

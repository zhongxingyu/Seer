 package biosim.core.sim;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.io.BufferedReader;
 import java.io.IOException;
 
 import sim.util.Double2D;
 import sim.util.MutableDouble2D;
 
 import biosim.core.body.Body;
 import biosim.core.util.BTFData;
 
 public class InitiallyPlacedEnvironment extends Environment{
 	public ArrayList<Double2D> initialPositions = new ArrayList<Double2D>();
 	public ArrayList<Double2D> initialOrientations = new ArrayList<Double2D>();
 	public ArrayList<String> initialIDs = new ArrayList<String>();
 	public HashSet<String> usedIDs = new HashSet<String>();
 
 	public InitiallyPlacedEnvironment(double width, double height,double resolution){
 		super(width,height,resolution);
 	}
 	public void addInitialPose(Double2D p, Double2D dir, String id){
 		initialPositions.add(p);
 		initialOrientations.add(dir);
 		initialIDs.add(id);
 	}
 
 	public void addBody(Body b){
 		initialBodies.add(b);
 		int lastAddedIdx = initialBodies.size()-1;
 		if(lastAddedIdx<initialIDs.size()){
 			b.label = initialIDs.get(lastAddedIdx);
 		} else {
			System.err.println("Warning: default ID created");
 			int newID = initialIDs.size();
 			while(usedIDs.contains(((Integer)newID).toString())){
 				newID++;
 			}
 			b.label = ((Integer)newID).toString();
 		}
 		usedIDs.add(b.label);
 	}
 
 	public void parseInitialPoses(BTFData btf) throws IOException{
 		String[] id = btf.loadColumn("id");
 		String[] xpos = btf.loadColumn("xpos");
 		String[] ypos = btf.loadColumn("ypos");
 		String[] timage = btf.loadColumn("timage");
 		String[] clocktime = btf.loadColumn("clocktime");
 		String[] dbool = btf.loadColumn("dbool");
 		double firstTime = Double.parseDouble(clocktime[0]);
 		for(int i=0; Double.parseDouble(clocktime[i]) == firstTime; i++){
 			if(!Boolean.parseBoolean(dbool[i])) continue;
 			initialPositions.add(new Double2D(Double.parseDouble(xpos[i]), Double.parseDouble(ypos[i])));
 			MutableDouble2D tmp = new MutableDouble2D(1,0);
 			tmp.rotate(Double.parseDouble(timage[i]));
 			initialOrientations.add(new Double2D(tmp));
 			initialIDs.add(id[i]);
 		}
 
 	}
 
 	public int parseInitialPoses(BufferedReader poseSrc) throws IOException{
 		int count = 0;
 		while(poseSrc.ready()){
 			String[] line = poseSrc.readLine().split(" ");
 			String id = line[0];
 			Double2D p = new Double2D(Double.parseDouble(line[1]),Double.parseDouble(line[2]));
 			Double2D dir = (new Double2D(1,0)).rotate(Double.parseDouble(line[3]));
 			addInitialPose(p,dir,id);
 			count++;
 		}
 		return count;
 	}
 
 	protected void initSimField(Simulation sim){
 		for(int i=0;i<obstacles.size();i++){
 			Double2D tmp = obstacleLocations.get(i);
 			sim.setObjectLocation(obstacles.get(i),tmp);
 		}
 		for(int i=0;i<poi.size();i++){
 			sim.setObjectLocation(poi.get(i),poiLocations.get(i));
 		}
 		for(int i=0;i<initialBodies.size();i++){
 			Body b = initialBodies.get(i);
 			Double2D pos = null, ori = null;
 			boolean found = false;
 			for(int j=0;j<initialBodies.size();j++){
 				if(initialIDs.get(i).equalsIgnoreCase(b.label)){
 					found = true;
 					pos = initialPositions.get(i);
 					ori = initialOrientations.get(i);
 					break;
 				}
 			}
 			if(!found){
 				placeRandomly(sim,b);
				System.err.println("Warning: Placing body randomly");
 			} else {
 				sim.setObjectLocation(b,pos);
 				sim.bodyOrientations.add(ori);
 			}
 		}
 	}
}

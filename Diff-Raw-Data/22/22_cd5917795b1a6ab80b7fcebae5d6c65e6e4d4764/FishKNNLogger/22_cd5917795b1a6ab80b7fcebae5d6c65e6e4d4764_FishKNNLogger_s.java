 package biosim.app.fishknn;
 
 import biosim.core.body.Body;
 import biosim.core.body.NotemigonusCrysoleucas;
 import biosim.core.sim.Simulation;
 import biosim.core.util.BTFLogger;
 
 import sim.engine.SimState;
 import sim.util.Double2D;
 import sim.util.MutableDouble2D;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class FishKNNLogger extends BTFLogger {
 	public BufferedWriter 	wallout, wallboolout,
 							zoneout, zoneboolout,
 							desiredout, desiredboolout;
 	public FishKNNLogger(){
 		this(new File(System.getProperties().getProperty("user.dir")));
 	}
 	public FishKNNLogger(File dir){
 		super(dir);
 		tmpFilePrefix = "FishKNNLogger-";
 	}
 	public void initFiles() throws IOException {
 		super.initFiles();
 		wallout = new BufferedWriter(new FileWriter(new File(tmpDir,"wallvec.btf")));
 		wallboolout = new BufferedWriter(new FileWriter(new File(tmpDir,"wallbool.btf")));
 		zoneout = new BufferedWriter(new FileWriter(new File(tmpDir,"zonevecs.btf")));
 		zoneboolout = new BufferedWriter(new FileWriter(new File(tmpDir,"zonebool.btf")));
 		desiredout = new BufferedWriter(new FileWriter(new File(tmpDir,"dvel.btf")));
 		desiredboolout = new BufferedWriter(new FileWriter(new File(tmpDir,"dbool.btf")));
 	}
 	
 	public void nullFiles(){
 		super.nullFiles();
 		wallout = wallboolout = zoneout = zoneboolout = desiredout = desiredboolout = null;
 	}
 
 	public void closeFiles() throws IOException{
 		wallout.close();
 		wallboolout.close();
 		zoneout.close();
 		zoneboolout.close();
 		desiredout.close();
 		desiredboolout.close();
 	}
 		
 	public void step(SimState simstate){
 		super.step(simstate);
 		if(wallout == null) return;
 		if(simstate instanceof Simulation){
 			Simulation sim = (Simulation)simstate;
 			for(int i=0;i<sim.bodies.size();i++){
 				Body b = sim.bodies.get(i);
 				if(b instanceof NotemigonusCrysoleucas){
 					NotemigonusCrysoleucas fish = (NotemigonusCrysoleucas)b;
 					MutableDouble2D wallSensorVec = new MutableDouble2D();
 					boolean wallSensorRv = fish.getNearestObstacleVec(wallSensorVec);;
 					MutableDouble2D[] zoneSensorVec = new MutableDouble2D[fish.getNumZones()];
 					boolean zoneSensorRv = fish.getZoneCoMVecs(zoneSensorVec);
 					double[] qux = {0.0, 0.0, 0.0};
 					boolean baz = false;
 					double[] foo = {0.0, 0.0, 0.0};
 					boolean bar = false;
 					System.arraycopy(fish.desiredVelXYT,0,foo,0,3);
 					bar = true;
 					try{
 						wallout.write(wallSensorVec.x+" "+wallSensorVec.y+"\n");
 						wallboolout.write(wallSensorRv+"\n");
 						zoneboolout.write(zoneSensorRv+"\n");
 						if(zoneSensorRv){
 							for(int z=0;z<zoneSensorVec.length;z++){
 								zoneout.write(zoneSensorVec[z]+" ");
 							}
 							zoneout.write("\n");
 						} else {
 							for(int z=0;z<zoneSensorVec.length;z++){
 								zoneout.write("0 ");
 							}
 							zoneout.write("\n");
 						}
 						
 						desiredout.write(foo[0]+" "+foo[1]+" "+foo[2]+"\n");
 						desiredboolout.write(bar+"\n");
 					} catch(IOException ioe){
 						System.err.println("[FishKNNLogger] Error writing to log files: "+ioe);
 					}
 				}
 
 			}
 		}
 	}
 }

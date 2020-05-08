 package champ2011client;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.ArrayRealVector;
 import org.apache.commons.math.linear.RealMatrix;
 import org.apache.commons.math.linear.RealVector;
 
 import champ2011client.behaviour.*;
 
 /**
  * Our controller
  * @author Derk
  *
  */
 public class Schumacher extends Controller {
 
     final double targetSpeed = 95;
 
     protected StandardGearChangeBehaviour gearBehaviour = new StandardGearChangeBehaviour();
     protected ClutchBehaviour clutchBehaviour = new ClutchBehaviour();
     
     protected RealMatrix a, b, actions;
     protected RealVector theta;
     
     public Schumacher() throws FileNotFoundException {
     	double[][] dataA = readMatrixFromCsv("../TORCS-Dynamics/A.csv");
     	double[][] dataB = readMatrixFromCsv("../TORCS-Dynamics/B.csv");
     	double[][] dataTheta = readMatrixFromCsv("../TORCS-Dynamics/theta.csv");
     	double[][] dataActions = readMatrixFromCsv("../TORCS-Dynamics/actions.csv");
     	
     	a = new Array2DRowRealMatrix(dataA);
     	b = new Array2DRowRealMatrix(dataB);
     	theta = new Array2DRowRealMatrix(dataTheta).getColumnVector(0);
     	actions = new Array2DRowRealMatrix(dataActions);
     }
     
     protected double[][] readMatrixFromCsv(String filename) throws FileNotFoundException {
     	List<String> data = new ArrayList<String>();
     	FileReader fin = new FileReader(filename);
     	Scanner sc = new Scanner(fin);
     	while(sc.hasNext()) {
     		data.add(sc.nextLine());
     	}
     	sc.close();
 
     	double[][] matrix = new double[data.size()][];
     	int count = -1;
     	for(String s : data) {
     		String[] temp = data.get(++count).split(",");
     		matrix[count] = new double[temp.length];
     		for(int i = 0; i < temp.length; i++) {
     			matrix[count][i] = Double.valueOf(temp[i]);
     		}
     	}
     	
     	return matrix;
     }
     
     public Action control(SensorModel sensorModel) {
         Action action = new Action ();
         
        if (Math.abs(sensorModel.getTrackPosition()) > 1) {
        	action.restartRace = true;
        	return action;
        }
        
         /*
         if (sensorModel.getSpeed () < targetSpeed) {
             action.accelerate = 1;
         }
         if (sensorModel.getAngleToTrackAxis() < 0) {
             action.steering = -0.1;
         }
         else {
             action.steering = 0.1;
         }
         */
         
         double[] dataS = {
        		sensorModel.getDistanceFromStartLine() / 2057.56,
        		sensorModel.getTrackPosition() > 0 ? sensorModel.getTrackPosition() : 0,
        		sensorModel.getTrackPosition() < 0 ? sensorModel.getTrackPosition() * -1 : 0,
        		sensorModel.getAngleToTrackAxis() > 0 ? sensorModel.getAngleToTrackAxis() : 0,
        		sensorModel.getAngleToTrackAxis() < 0 ? sensorModel.getAngleToTrackAxis() * -1 : 0,
        		(sensorModel.getSpeed() * 1000 / 3600) / 2057.56,
        		(sensorModel.getLateralSpeed() * 1000 / 3600) / (sensorModel.getTrackEdgeSensors()[0] + sensorModel.getTrackEdgeSensors()[18])
         };
         
         RealVector s = new ArrayRealVector(dataS);
         RealVector t1 = a.operate(s);
         
         int num_actions = actions.getRowDimension();
         
         //RealVector values = new ArrayRealVector(num_actions);
         double maxValue = 0;
         int maxIndex = 0;
         for (int i = 0; i < num_actions; i++) {
         	RealVector t2 = b.operate(actions.getRowVector(i));
         	RealVector sPrime = t1.add(t2);
         	
         	double value = theta.dotProduct(sPrime);
         	if (value > maxValue) {
         		maxIndex = i;
         		maxValue = value;
         	}
         }
         
         RealVector selectedAction = actions.getRowVector(maxIndex);
         
        double velocity = selectedAction.getEntry(0);
        double steering = selectedAction.getEntry(1);
         
         
         
         action.accelerate = Math.max(velocity, 0.0);
         action.brake = Math.min(velocity, 0.0);
         action.steering = steering;
         
         gearBehaviour.execute(sensorModel, action);
         clutchBehaviour.execute(sensorModel, action);
         
         return action;
     }
 
     public void reset() {
 		System.out.println("Restarting the race!");
 	}
 
 	public void shutdown() {
 		System.out.println("Bye bye!");		
 	}
 }

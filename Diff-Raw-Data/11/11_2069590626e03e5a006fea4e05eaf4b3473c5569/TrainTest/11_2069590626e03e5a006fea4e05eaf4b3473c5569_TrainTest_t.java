 package asgn2Test;
 
 import asgn2RollingStock.FreightCar;
 import asgn2RollingStock.Locomotive;
 import asgn2RollingStock.PassengerCar;
 import asgn2Train.DepartingTrain;
 
 public class TrainTest {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			DepartingTrain train = new DepartingTrain();
 			
 			System.out.println("train: "+train +"\n");
 			
 			train.addCarriage(new Locomotive(100, "4S"));
 			
 			train.addCarriage(new PassengerCar(40, 15));
 			train.addCarriage(new PassengerCar(40, 20));
 			train.addCarriage(new FreightCar(20, "D"));
			train.addCarriage(new FreightCar(20, "D"));
 						
 			
 			train.removeCarriage();
 
 			train.board(23);
 			
 			System.out.println("train: "+train +"\n");
 			System.out.println("train can move: "+train.trainCanMove() +"\n");
 			
 		} catch (Exception e) {
 			System.out.println("error: " + e);
 		}
 
 	}
 
 }

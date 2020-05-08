 import java.util.concurrent.TimeUnit;
 
 public class Start {
 
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 
 		SubwayMap.getInstance().start();
 		Line linea = new Line();
 		Station first = linea.stations.get(0);
 
 		Person p = null;
 
 		int random = 0;
 		Train train = new Train("Start");
 		for (int i = 0; i < 70; i++) {
 			random = (int) (Math.random() * linea.stations.size());
 			p = new Person(linea.stations.get(random));
 			train.passengerIn(p);
 		}
 
 		first.trainArrival(train, 0L);
		int k = 1;
 		for (int i = 0; i < 1000; i++) {
 			// System.out.println("tick");
 			TimeUnit.MILLISECONDS.sleep(100);
 			if (i % 100 == 99) {
				k++;
				System.out.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
				System.out.println("Trains in the system:" + k);
 				train = new Train("Start#" + i);
 				for (int j = 0; j < 70; j++) {
 					random = (int) (Math.random() * linea.stations.size());
 					p = new Person(linea.stations.get(random));
 					train.passengerIn(p);
 				}
 				first.trainArrival(train, SimulatorScheduler.getInstance()
 						.getTime() + 1);
 			}
 
 			SimulatorScheduler.getInstance().advanceTime();
 		}
 	}
 }

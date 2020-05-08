 /**
  * 
  */
 package Subway;
 
 import java.util.ArrayList;
 
 /**
  * @author David
  * 
  */
 public class SubwaySim {
 	public static void main(String[] args) {
 		ArrayList<Wagon> train = new ArrayList<Wagon>();
 		ArrayList<Station> track = new ArrayList<Station>();
 		int rand = (3 + (int) (Math.random() * ((8 - 3) + 1)));
 		for (int wagonIter = 0; wagonIter <= rand-1; wagonIter++) {
 			train.add(new Wagon());
 		}
 		System.out.print("Sehr geehrter Fahrgste, dieser Zug hat ");
 		System.out.print(train.size());
 		System.out.println(" Wagons!");
 		for (int stationIter = 0; stationIter <= 9; stationIter++) {
 			track.add(new Station(stationIter));
 		}
 
 		for (int j = 0; j < track.size() - 1; j++) {
 			Station station = track.get(j);
 			ArrayList<Passenger> gotOut = new ArrayList<Passenger>();
 			int hurryOut = 0;
 			System.out.print("---");
 			System.out.print(j);
 			System.out.println("---");
 			System.out.print("Wartende: ");
 			System.out.println(station.waitingAt().size());
 			for (int l = 0; l < train.size() - 1; l++) {
 				for (int m = 0; m < train.get(l).passengerCount(); m++) {
 					Passenger leavingPassenger = train.get(l).getPassenger(m)
 							.getOut(j, train.get(l));
 					if (leavingPassenger != null) {
 						gotOut.add(leavingPassenger);
 						if(m > 0){
 							m--;
 						}
 						else{
 							m = 0;
 						}
 						if (train.get(l).getPassenger(m).isInAHurry()) {
 							hurryOut++;
 						}
 					}
 				}
 			}
 
 			System.out.print("Passagiere ausgestiegen: ");
 			System.out.print(gotOut.size());
 			System.out.print(", davon ");
 			System.out.print(hurryOut);
 			System.out.println(" in Eile.");
 
 			ArrayList<Passenger> gotIn = new ArrayList<Passenger>();
 			int hurryIn = 0;
 			for (int k = 0; k < ((station.waitingAt().size())); k++) {
 				// All waiting passengers try to get into a random wagon
 				Passenger schroedingersPassenger = station
 						.waitingAt()
 						.get(k)
 						.getIn(train.get((int) (0 + (int) (Math.random() * ((train
 								.size() - 1 - 0) + 1)))), track.get(j));
 				// If it worked, add Passenger to gotIn List
 				if (schroedingersPassenger != null) {
 					gotIn.add(schroedingersPassenger);
 					k--;
 					if (schroedingersPassenger.isInAHurry()) {
 						hurryIn++;
 					} else {
 						// /nothingsness
 					}
 				} else {
 					if (station.waitingAt().get(k).isInAHurry()) {
 						for (int i = 0; i < train.size() - 1; i++) {
 							Passenger schroedingersPassengerInAHurry = station
 									.waitingAt().get(k)
 									.getIn(train.get(i), track.get(j));
 							if (schroedingersPassengerInAHurry != null) {
 								gotIn.add(schroedingersPassengerInAHurry);
 								k--;
 								hurryIn++;
 								break;
 							}
 						}
 					}
 				}
 			}
 
 			int hurryStay = 0;
 			for (int i = 0; i < track.get(j).waitingAt().size(); i++) {
 				if (track.get(j).waitingAt().get(i).isInAHurry()) {
 					hurryStay++;
 				}
 			}
 
 			System.out.print("Passagiere eingestiegen: ");
 			System.out.print(gotIn.size());
 			System.out.print(", davon ");
 			System.out.print(hurryIn);
 			System.out.println(" in Eile.");
 
 			System.out.print("Passagiere zurckgelassen: ");
 			System.out.print(track.get(j).waitingAt().size());
 			System.out.print(", davon ");
 			System.out.print(hurryStay);
 			System.out.println(" in Eile.");
 
 			int alloverCounter = 0;
 			for (int alloverIter = 0; alloverIter < train.size(); alloverIter++) {
				/*
				 * System.out.print("Sehr geehrte Fahrgste, Wagon ");
				 * System.out.print(alloverIter); System.out.print(" enthlt ");
				 * System.out.print(train.get(alloverIter).passengerCount());
				 * System.out.println(" Passagiere.");
				 */
 				alloverCounter += train.get(alloverIter).passengerCount();
 			}
 			System.out.print("Also alles in allem ");
 			System.out.print(alloverCounter);
 			System.out.println(" Fahrgste!");
 		}
 	}
 }

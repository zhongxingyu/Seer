 import java.util.Scanner;
 
 import part2.company.CompanyBuySell;
 
 import company.*;
 import vehicle.UserInterface;
 import vehicle.VehicleType;
 
 public class Part1 {
 
 	public static String getUserEntry() {
 		Scanner input = new Scanner(System.in);
 		return input.nextLine();
 	}
 
 	public static void menuSelection(boolean i_listAnswer) {
 		String input;
 		input = getUserEntry().toUpperCase();
 		while ((!i_listAnswer && !input.equals("1") && !input.equals("2") && !input
 				.equals("3")) || (i_listAnswer && !input.equals("4"))) {
 			System.out.println("Selection not valid, please choose again.");
 			input = getUserEntry().toUpperCase();
 		}
 	}
 
 	public static void main(String[] args) {
 		String drive = System.getProperty("os.name").substring(0, 6)
 				.equals("Windows") ? "C:" : "";
 		String projectPath = System.getProperty("user.dir");
 		String vehicleList = drive + projectPath + "/part1/part1vehicles.txt";
 
 		Company theCompany = new Company(vehicleList, 10);
 
 		// Main menu interface
 		String printMain = null;
 		UserInterface main = new UserInterface(printMain);
 		main.printMain();
 
 		Scanner kbPrintAll = new Scanner(System.in);
 		Scanner kbPrintNumber = new Scanner(System.in);
 		Scanner kbPrintChoice = new Scanner(System.in);
 
 		/******************************************************************************************
 		 * OPTION 1: PRINT ALL VEHICLES
 		 ******************************************************************************************/

 		String printChoice = kbPrintChoice.nextLine();
 
 		while ((!printChoice.equals("1") && !printChoice.equals("2") && !printChoice
				.equals("3")) || !printChoice.equals("4")) {

			printChoice = getUserEntry().toUpperCase();
 
 			if (printChoice.equalsIgnoreCase("1")) {
 
 				main.printCarMenu();
 				theCompany.printVehicles(VehicleType.Car);
 
 				main.printTruckMenu();
 				theCompany.printVehicles(VehicleType.Truck);
 
 				main.printBikeMenu();
 				theCompany.printVehicles(VehicleType.Bike);
 
 				main.printMain();

 				/******************************************************************************************
 				 * OPTION 2: PRINT VEHICLES BY TYPE
 				 ******************************************************************************************/
 
 			} else if (printChoice.equalsIgnoreCase("2")) {
 				Scanner kbPrintType = new Scanner(System.in);
 				System.out
 						.println("Which type? (Type 'Car,' 'Truck,' or 'Bike')");
 				String printType = kbPrintType.nextLine();
 
 				// PRINT CAR TYPE
 
 				if (printType.equalsIgnoreCase("Car")) {
 					main.printCarType();
 					theCompany.printVehicles(VehicleType.Car);
 					main.printMain();
 				} else if (printType.equalsIgnoreCase("Truck")) {
 					main.printTruckType();
 					theCompany.printVehicles(VehicleType.Truck);
 					main.printMain();
 				} else if (printType.equalsIgnoreCase("Bike")) {
 					main.printBikeType();
 					theCompany.printVehicles(VehicleType.Bike);
 					main.printMain();
 				}

 				/******************************************************************************************
 				 * OPTION 3: NUMBER OF VEHICLES
 				 ******************************************************************************************/
 
 			} else if (printChoice.equalsIgnoreCase("3")) {
 				System.out
 						.println("Which type? (Type 'Car,' 'Truck,' or 'Bike')");
 				String printNumber = kbPrintNumber.nextLine();
 
 				if (printNumber.equalsIgnoreCase("Car")) {
 					System.out.print(theCompany.getTotal(VehicleType.Car));
 					main.printCarNumber();
 					main.printMain();
 
 				} else if (printNumber.equalsIgnoreCase("Truck")) {
 					System.out.print(theCompany.getTotal(VehicleType.Truck));
 					main.printTruckNumber();
 					main.printMain();
 				} else if (printNumber.equalsIgnoreCase("Bike")) {
 					System.out.print(theCompany.getTotal(VehicleType.Bike));
 					main.printBikeNumber();
 					main.printMain();
 				}

 				/******************************************************************************************
 				 * OPTION 4: QUIT PROGRAM
 				 ******************************************************************************************/
 
 			} else if (printChoice.equalsIgnoreCase("4")) {
 				System.out.println("Exiting program...");
 				System.exit(0);
 			}
 
 			else {
 				System.out.println("Selection not valid, please choose again.");
 			}
 		}
 
 	}
 }

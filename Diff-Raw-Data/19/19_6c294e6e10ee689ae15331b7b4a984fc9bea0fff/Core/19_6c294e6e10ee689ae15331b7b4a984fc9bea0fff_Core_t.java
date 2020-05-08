 package softwareEngineering;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.util.Scanner;
 import java.util.Calendar;
 
 public class Core {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Calendar calendar = Calendar.getInstance();
 		File inputFile = null;
 		FileWriter logFile = null;
 		BufferedWriter log = null;
 		try {
 			logFile = new FileWriter("log/countLOC-log.txt");
 			log = new BufferedWriter(logFile);
 		} catch (Exception e) {
 			System.out.println("Failure opening log file. Make sure countLOC-log.txt is created in the log folder.\r\n");
 			System.exit(-1);
 		}
 		String inputFileName = null;
 		Scanner input = new Scanner(System.in);
 		
 		System.out.println("Welcome to the NHEIDEN and JFISCHER Line of Code Counter\r\n");
 		System.out.print("Enter a File for Input: ");
 		try {
 			inputFileName = input.next();
 		} catch (Exception e) {
 			System.out.println("\r\nInput error: User entry is not a valid file path.");
 			try {
 				log.write(calendar.get(Calendar.YEAR)+" - "+(calendar.get(Calendar.MONTH)+1)+" - "+calendar.get(Calendar.DAY_OF_MONTH)+
 						": "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+" -\r\n"+
 						"ERROR DETECTED: Input error: User entry is not a valid file path.\r\n"
 				);
 				log.close();
 			} catch (Exception ex) {
 				// Log write failure. Exiting anyway...
 				// Bad practice, you want to do something here if logging fails in order to avoid misinterpretation of errors.
 			}
 			System.exit(-1);
 		}
 		if (inputFileName != null) {
			try {
				inputFile = new File("dropoff/"+inputFileName);
			} catch (Exception e) {
				System.out.println("java.io.File error, cannot open "+inputFileName);
				try {
					log.write(calendar.get(Calendar.YEAR)+" - "+(calendar.get(Calendar.MONTH)+1)+" - "+calendar.get(Calendar.DAY_OF_MONTH)+
							": "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+" -\r\n"+
							"ERROR DETECTED: java.io.File error, cannot open "+inputFileName+"\r\n"
					);
					log.close();
				} catch (Exception ex) {
					// Log write failure. Exiting anyway...
					// Bad practice, you want to do something here if logging fails in order to avoid misinterpretation of errors.
				}
				System.exit(-1);
			}
 		}
 		int count = 0;
 		if(inputFile.exists()){
 			// System.out.println("Location Exists...");
 			if(inputFile.isFile()){
 				// System.out.println("Location is a File");				
 				if(inputFile.canRead()){
 					// System.out.println("File is readable");
 					//count = CountLOC.countLoc(inputFile);
 				}
 				else{
 					System.out.println("File is not readable, exiting...");
 					try {
 						log.write(calendar.get(Calendar.YEAR)+" - "+(calendar.get(Calendar.MONTH)+1)+" - "+calendar.get(Calendar.DAY_OF_MONTH)+
 								": "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+" -\r\n"+
 								"ERROR DETECTED: File is not readable, exiting...\r\n"
 						);
 						log.close();
 					} catch (Exception e) {
 						// Log write failure. Exiting anyway...
 						// Bad practice, you want to do something here if logging fails in order to avoid misinterpretation of errors.
 					}
 					System.exit(-1);
 				}
 				
 			}
 			else{
 				System.out.println("Location specified is not a File, exiting...");
 				try {
 					log.write(calendar.get(Calendar.YEAR)+" - "+(calendar.get(Calendar.MONTH)+1)+" - "+calendar.get(Calendar.DAY_OF_MONTH)+
 							": "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+" -\r\n"+
 							"ERROR DETECTED: Location specified is not a File, exiting...\r\n"
 					);
 					log.close();
 				} catch (Exception e) {
 					// Log write failure. Exiting anyway...
 					// Bad practice, you want to do something here if logging fails in order to avoid misinterpretation of errors.
 				}
 				System.exit(-1);
 			}
 			
 		}
 		else{
			System.out.println("Files are checked for under the 'dropoff' directory where the code is located.");
 			System.out.println("File does not Exist, exiting...");
 			try {
 				log.write(calendar.get(Calendar.YEAR)+" - "+(calendar.get(Calendar.MONTH)+1)+" - "+calendar.get(Calendar.DAY_OF_MONTH)+
 						": "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+" -\r\n"+
 						"ERROR DETECTED: File does not Exist, exiting...\r\n"
 				);
 				log.close();
 			} catch (Exception e) {
 				// Log write failure. Exiting anyway...
 				// Bad practice, you want to do something here if logging fails in order to avoid misinterpretation of errors.
 			}
 			System.exit(-1);
 		}
 		System.out.println("File "+inputFileName+" has "+count+" non-commented lines.");
 		try {
 			log.write(calendar.get(Calendar.YEAR)+" - "+(calendar.get(Calendar.MONTH)+1)+" - "+calendar.get(Calendar.DAY_OF_MONTH)+
 					": "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+" -\r\n"+
 					"  File "+inputFileName+" has "+count+" non-commented lines.\r\n"
 			);
 			log.close();
 		} catch (Exception e) {
 			// Log write failure. Exiting anyway...
 			// Bad practice, you want to do something here if logging fails in order to avoid misinterpretation of errors.
 		}
 		System.exit(0);
 	}
 
 }

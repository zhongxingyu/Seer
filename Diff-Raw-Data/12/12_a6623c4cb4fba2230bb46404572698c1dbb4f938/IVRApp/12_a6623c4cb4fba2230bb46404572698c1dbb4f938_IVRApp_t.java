 package edu.berkeley.cs160.groupp;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.app.Application;
 
 public class IVRApp extends Application {
 	private static IVRNode root;
 	private static IVRNode current;
 	public void onCreate() {
 		initialize();
 	}
 	
 	public static void initialize() {
 		IVRNode r = new IVRNode(null, "menu", "Main menu", "relax");
 		r.addChildNumber("Some number", "1234567890");
 		r.addChildNumber("some other number", "9876543210");
 		r.addChildURL("some url", "http://www.google.com");
 		r.addChildMenu("some unfinished menu", "some unfinished menu descr");
 		setRootNode(r);
 	}
 	
 	public static IVRNode getRootNode() {
 		return root;
 	}
 	
 	public static void setRootNode(IVRNode r) {
 		root = r;
 	}
 	
 	public static IVRNode getCurrentBranch() {
 		return current;
 	}
 	
 	public static void setCurrentBranch(IVRNode c) {
 		current = c;
 	}
 	
 	public static void initializePGE() {
 		IVRNode pge = new IVRNode(null, "menu", "PG&E: Report an Outage", "Thank you for calling the Pacific Gas and Electric Company. Please select a language.");
 		IVRNode pgeAnnouncement = pge.addChildMenu("English", "Are you experiencing a hazardous situation such as a gas leak or a downed power line?");
 		IVRNode pgeSpanish = pge.addChildMenu("Spanish", "unimplemented");
 		
 		IVRNode pgePowerLine = pgeAnnouncement.addChildMenu("Yes - I am experiencing a hazardous situation", "unimplemented");
 		IVRNode pgeMainMenu = pgeAnnouncement.addChildMenu("No - I am experiencing a non-hazardous situation", "Are you calling about an electric outage or a gas outage?");
 		IVRNode pgeElectricOutage = pgeMainMenu.addChildMenu("I want to report an electric outage", "You can report an outage or look up information about one if you supply either your account number or phone number. Which number would you like to give?");
 		IVRNode pgeGasOutage = pgeMainMenu.addChildMenu("I want to report a gas outage", "unimplemented");
 		
 		IVRNode pgeElectricOutageAcct = pgeElectricOutage.addChildMenu("Give my account number", "unimplemented");
 		IVRNode pgeElectricOutagePhone = pgeElectricOutage.addChildTextInput("Give my phone number", "Please enter the 10-digit phone number associated with your account, area code first.", "18007435002,,,,,2,,1,,2,,");
 		
 		setRootNode(pge);
 	}
 
 	public static void initializeDMV() {
 		IVRNode dmv = new IVRNode(null, "menu", "California DMV", "Please select a language.");
 		IVRNode announcement = dmv.addChildMenu("English", "For DMV hours and closures, please listen to DMV News. Please note: " +
 				"due to heavy volume processing, for Califormia's new security enhanced " +
 				"driver licenses, please allow up to six weeks for delivery. For license " +
 				"renewals by mail or internet, payments are processed and your record is updated within one week.");
 		IVRNode spanishAnnouncement = dmv.addChildMenu("Espanol", "No habla Espanol");
 		IVRNode mainMenu = announcement.addChildMenu("Main Menu", "Please select an option.");
 		IVRNode vehiclesVessels = mainMenu.addChildMenu("Vehicles and Vessels", "unimplemented");
 		IVRNode driverLicenseOption = mainMenu.addChildMenu("Driver License", "Driver license and ID information: please select one of the following:");
 		IVRNode orderForms = mainMenu.addChildMenu("Order Forms", "unimplemented");
 		IVRNode makeAppt = mainMenu.addChildMenu("Make an Appointment", "Is this a behind-the-wheel driving test, or an office visit?");
		IVRNode findOffice = mainMenu.addChildMenu("Find an Office", "You can either place a call to the system to find the office closest to your zip code, or choose from a list of locations. Which would you like?");
 		IVRNode dmvNews = mainMenu.addChildMenu("Latest DMV News", "Latest DMV news");
 
 		IVRNode findOfficeCall = findOffice.addChildTextInput("Call the System", "Please enter your 5-digit zip code.", "18007770133,,1,,,,,,,,5,,");
 		IVRNode findOfficeMenu = findOffice.addChildMenu("Choose from a List", "Please choose one of the following options. You will be able to load the web page of the DMV location that you have chosen.");
 		IVRNode findOfficeMenuOakland = findOfficeMenu.addChildURL("Oakland DMV", "http://apps.dmv.ca.gov/fo/offices/appl/fo_data_read.jsp?foNumb=504");
 		IVRNode findOfficeMenuElCerrito = findOfficeMenu.addChildURL("El Cerrito DMV", "http://apps.dmv.ca.gov/fo/offices/appl/fo_data_read.jsp?foNumb=556");
 		
 		
 		//driver license branch
 		IVRNode driverLicenseMenu1 = driverLicenseOption.addChildMenu("Apply for original driver license or ID card", "System unavailable.");
 		IVRNode driverLicenseMenu2 = driverLicenseOption.addChildMenu("Renew a driver license or ID card", "System unavailable.");
 		IVRNode driverLicenseMenu4 = driverLicenseOption.addChildMenu("Replace a lost or stolen one", "System unavailable.");
 		IVRNode driverLicenseMenu3 = driverLicenseOption.addChildMenu("Check the status of a request", "System unavailable.");
 		IVRNode driverLicenseMenu5 = driverLicenseOption.addChildMenu("Name or address change", "System unavailable.");
 		IVRNode driverLicenseMenu6 = driverLicenseOption.addChildMenu("More options...", "System unavailable.");		
 		IVRNode driverLicenseMenu61 = driverLicenseMenu6.addChildMenu("Accidents", "System unavailable.");
 		IVRNode driverLicenseMenu62 = driverLicenseMenu6.addChildMenu("DUI", "System unavailable.");
 		IVRNode driverLicenseMenu63 = driverLicenseMenu6.addChildMenu("Child Support", "System unavailable.");
 		IVRNode driverLicenseMenu64 = driverLicenseMenu6.addChildMenu("New to California", "System unavailable.");
 
 		//Make Appt branch
 		IVRNode drivingTest = makeAppt.addChildMenu("Driving Test", "Do you already have a driver license or permit number?");
 		IVRNode officeVisit = makeAppt.addChildMenu("Office Visit", "We offer a number of services related to driver license " +
 				"or vehicle registration. Which would you like?");
 		
 		IVRNode vehicleType = drivingTest.addChildMenu("Yes, I have a driver's license or permit number", "Please enter your license or permit number, which should be a letter followed by a seven-digit number, such as F1234567,");
 		IVRNode alsoofficeVisit = drivingTest.addChildMenu("No, I do not have a driver's license or permit number", "unimplemented");
 		IVRNode drivingTestNum = vehicleType.addChildMenu("El Cerrito DMV", "Please select which test you are planning to take.");
 		IVRNode drivingTestLoc2 = vehicleType.addChildMenu("Oakland DMV", "Please select which test you are planning to take.");
 		IVRNode drivingTestLoc3 = vehicleType.addChildMenu("More DMV Locations", "Please select which test you are planning to take.");
 		IVRNode enterVehicleNum = drivingTestNum.addChildNumber("Automobile Driving Test", "18007770133,,1,,,,,,,,4,,1,,1,,1,,111234567");
 		IVRNode motorcycleTraining = drivingTestNum.addChildMenu("Motorcycle Driving Test", "Have you completed the California motorcycle safety course?");
 		IVRNode enterVehicleNum2 = motorcycleTraining.addChildNumber("Yes - I have completed the California motorcycle safety course", "1-800-777-0133");
 		IVRNode enterVehicleNum3 = motorcycleTraining.addChildNumber("No - I have not completed the California motorcycle safety course", "1-800-777-0133");
 
 		IVRNode enterNumRegistrations1 = makeAppt.addChildMenu("Driver License", "California DMV will process a maximum of 3 items per visit. " +
 				"How many vehicle registrations or services do you need during this appointment?");
 		IVRNode enterNumRegistrations2 = makeAppt.addChildMenu("Vehicle Registration", "California DMV will process a maximum of 3 items per visit. " +
 				"How many vehicle registrations or services do you need during this appointment?");
 		IVRNode enterNumRegistrations3 = makeAppt.addChildMenu("Both", "California DMV will process a maximum of 3 items per visit. " +
 				"How many vehicle registrations or services do you need during this appointment?");
 		
 		IVRNode enterphone11 = enterNumRegistrations1.addChildMenu("1 Service or Registration", "Please enter your phone number.");
 		IVRNode enterphone12 = enterNumRegistrations1.addChildMenu("2 Services or Registrations", "Please enter your phone number.");
 		IVRNode enterphone13 = enterNumRegistrations1.addChildMenu("3 Services or Registrations", "Please enter your phone number.");
 		IVRNode enterphone21 = enterNumRegistrations2.addChildMenu("1 Service or Registration", "Please enter your phone number.");
 		IVRNode enterphone22 = enterNumRegistrations2.addChildMenu("2 Services or Registrations", "Please enter your phone number.");
 		IVRNode enterphone23 = enterNumRegistrations2.addChildMenu("3 Services or Registrations", "Please enter your phone number.");
 		IVRNode enterphone31 = enterNumRegistrations3.addChildMenu("1 Service or Registration", "Please enter your phone number.");
 		IVRNode enterphone32 = enterNumRegistrations3.addChildMenu("2 Services or Registrations", "Please enter your phone number.");
 		IVRNode enterphone33 = enterNumRegistrations3.addChildMenu("3 Services or Registrations", "Please enter your phone number.");
 		
 		IVRNode enterNumber = enterphone11.addChildNumber("what now", "18007770133,,1,,,,,,,,4,,2,,1,,4083155264");
 
 		//DMV news
 		IVRNode officeHours = makeAppt.addChildMenu("DMV office hours", "Offices are open Monday, Tuesday, Thursday, and Friday from 8 AM to 5 PM, " +
 				"and Wednesdays from 9 AM to 5 PM.");
 		IVRNode closures = makeAppt.addChildMenu("DMV office closure information", "system unavailable");
 	
 		setRootNode(dmv);
 	}
 	
 	public static void initializeFavorites() {
 		IVRNode fav = new IVRNode(null, "menu", "Favorite Searches", "This is a list of searches you have marked as your favorite. " +
 				"Click any search to be transported directly to the end page.");
 		fav.addChildNumber("ATT Customer Service: More Options", "18003310500pppppp1");
		fav.addChildNumber("Oakland Coliseum DMV: Directions", "18007770133pp1ppppppppp5pp94709pp3pp1");
 		fav.addChildNumber("Oakland Claremont DMV: Directions", "18007770133pp1ppppppppp5pp94709pp2pp1");
 		setRootNode(fav);
 	}
 	
 	public static void initializeRecents() {
 		IVRNode recent = new IVRNode(null, "menu", "Recent Searches", "This is a list of recent searches you have done.");
 		recent.addChildNumber("Powell Apple Store: Store Hours and Directions", "14153920202ppp1");
 		recent.addChildNumber("Oakland Claremont DMV: Directions", "18007770133pp1ppppppppp5pp94709pp2pp1");
 		recent.addChildURL("Powell Apple Store; Address (Web)", "http://www.apple.com/retail/sanfrancisco/map/");
 		setRootNode(recent);
 	}
 	
 	
 }

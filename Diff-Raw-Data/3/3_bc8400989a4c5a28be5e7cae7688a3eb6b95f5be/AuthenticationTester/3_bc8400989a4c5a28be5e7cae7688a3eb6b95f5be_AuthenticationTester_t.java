 package testing;
 
 import helper.InsufficientPermissionsException;
 import helper.InvalidBandObjectException;
 import helper.InvalidDateException;
 import helper.Validator;
 
 import java.lang.reflect.Method;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import auth.Authenticatable.Permission;
 import auth.Authenticator;
 import band.Band;
 import band.Gig;
 import band.Member;
 
 public class AuthenticationTester implements Tester {
 	private static final String moduleName = "Authentication";
 
 	// number of successful/failed tests
 	private Integer successfulTests;
 	private Integer failedTests;
 
 	// the test case numbers of all failed tests
 	private ArrayList<Integer> failedTestNumbers;
 
 	private HashMap<Integer, String> testCases;
 
 	public AuthenticationTester() {
 		successfulTests = 0;
 		failedTests = 0;
 
 		failedTestNumbers = new ArrayList<Integer>();
 		testCases = new HashMap<Integer, String>();
 		
 		testCases.put(1, "Every Authenticatable must be its own OWNER");
 		testCases.put(2, "The roles of each Auth. to each other can be changed");
 		testCases.put(3, "Once permissions have been granted, they can be removed again");
 		testCases.put(4, "Objects whose permissions have not been set are denied access");
 		testCases.put(5, "A method cannot be invoked without permissions");
 		testCases.put(6, "After the permissions where granted, the method can be invoked");
 		testCases.put(7, "Once the members join a band their permissions to each other are set to GROUP");
 		testCases.put(8, "Once the members leave a band their permissions to each other are set to NONE");
 		testCases.put(9, "Once the members join a band their permission to all of the band's events are set to GROUP");
 		testCases.put(10, "Once the members leave a band their permission to all of the band's events are set to NONE");
 		testCases.put(11, "A band member can only invoke a method of it's own band");
 		testCases.put(12, "Everyone has the permission to get the band's members");
 	}
 
 	@Override
 	public void printTestDescription() {
 		System.out.println("Test cases for the " + moduleName + " module: ");
 		for (Integer i : testCases.keySet()) {
 			System.out.println("#" + ((i < 10) ? "0" + i : i) + ": " + testCases.get(i));
 		}
 		System.out.println();
 	}
 
 	@Override
 	public void printTestResults() {
 		System.out.println("Test results for the " + moduleName + " module: ");
 		System.out.println("Successful tests: " + successfulTests);
 		System.out.println("Failed tests: " + failedTests);
 
 		if (!failedTestNumbers.isEmpty()) {
 			System.out.println("The following test cases have failed: ");
 			for (Integer i : failedTestNumbers) {
 				System.out.println("#" + i + " " + testCases.get(i));
 			}
 		}
 		System.out.println();
 	}
 	
 	/*
 	 * deprecation suppression is only used to ignore the warning of the methods we declared 
 	 * deprecated ourselves due to the enhancements made in assignment #2
 	 * 
 	 * (non-Javadoc)
 	 * @see testing.Tester#runTests()
 	 */
 	@SuppressWarnings("deprecation")
 	@Override
 	public void runTests() {
 		SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy");
 		
 		Member thomas = new Member("Thomas", "Rieder", "Guitar", "123456",
 				false);
 		Member markus = new Member("Markus", "Zisser", "Bass", "532423", true);
 		Member dominic = new Member("Dominic", "Riedl", "Keyboard", "389235",
 				true);
 		
 		Band tempBand = new Band("/tmp", "Math Rock");
 		Band anotherBand = new Band("/bin", "Progressive Metal");
 		
 		Gig tempGig = null;
 		try {
 			tempGig = new Gig(formatDate.parse("10.10.2010"), "Wien", 10, new BigDecimal(500));
 		} catch (ParseException e1) { e1.printStackTrace(); }
 		
 		try {
 			tempBand.addEvent(tempGig);
 		} catch (InvalidBandObjectException e1) { e1.printStackTrace(); }
 		
 
 		Method isSubstituteMemberMethod = null, bandAddMember = null, bandGetMembers = null;
 		try {
 			isSubstituteMemberMethod = Member.class.getMethod("isSubstituteMember",
 					new Class[] { });
 			bandAddMember = Band.class.getMethod("addMember", new Class[] {
 					Member.class, Date.class });
 			bandGetMembers = Band.class.getMethod("getMembers", new Class[] { });
 		} catch (SecurityException e) {	e.printStackTrace(); 
 		} catch (NoSuchMethodException e) {	e.printStackTrace(); }
 		
 
 		if (Validator.check(Permission.OWNER, thomas.getRole(thomas), 1)) {
 			successfulTests++;
 		} else {
 			failedTests++;
 			failedTestNumbers.add(1);
 		}
 
 		thomas.setRole(markus, Permission.GROUP);
 		if (Validator.check(Permission.GROUP, thomas.getRole(markus), 2)) {
 			successfulTests++;
 		} else {
 			failedTests++;
 			failedTestNumbers.add(2);
 		}
 
 		thomas.setRole(markus, Permission.NONE);
 		if (Validator.check(Permission.NONE, thomas.getRole(markus), 3)) {
 			successfulTests++;
 		} else {
 			failedTests++;
 			failedTestNumbers.add(3);
 		}
 
 		if (Validator.check(Permission.NONE, thomas.getRole(dominic), 4)) {
 			successfulTests++;
 		} else {
 			failedTests++;
 			failedTestNumbers.add(4);
 		}
 		
 		try {
 			Authenticator.checkPermissions(markus, thomas, isSubstituteMemberMethod);
 			failedTests++;
 			failedTestNumbers.add(5);
			Validator.report(false);
 		} catch (InsufficientPermissionsException e) {
 			successfulTests++;
			Validator.report(true);
 		}
 		
 		thomas.setRole(markus, Permission.GROUP);
 		try {
 			Authenticator.checkPermissions(markus, thomas, isSubstituteMemberMethod);
 			successfulTests++;
 		} catch (InsufficientPermissionsException e) {
 			failedTests++;
 			failedTestNumbers.add(6);
 		}
 		
 		try {
 			tempBand.addMember(thomas, formatDate.parse("01.01.2010"));
 			tempBand.addMember(markus, formatDate.parse("01.01.2010"));
 			tempBand.addMember(dominic, formatDate.parse("01.01.2010"));
 		} catch (InvalidDateException e) { e.printStackTrace();
 		} catch (InvalidBandObjectException e) { e.printStackTrace();
 		} catch (ParseException e) { e.printStackTrace(); }
 		
 		if (Validator.check(Permission.GROUP, thomas.getRole(dominic), 7)) {
 			successfulTests++;
 		} else {
 			failedTests++;
 			failedTestNumbers.add(7);
 		}
 		
 		try {
 			tempBand.removeMember(dominic, formatDate.parse("01.01.2011"));
 		} catch (InvalidDateException e) { e.printStackTrace();
 		} catch (InvalidBandObjectException e) { e.printStackTrace();
 		} catch (ParseException e) { e.printStackTrace(); }
 		
 		if (Validator.check(Permission.NONE, thomas.getRole(dominic), 8)) {
 			successfulTests++;
 		} else {
 			failedTests++;
 			failedTestNumbers.add(8);
 		}
 		
 		// not really beautiful
 		Gig t = (Gig) tempBand.getEvents().get(0);
 		
 		if (Validator.check(Permission.GROUP, t.getRole(thomas), 9)) {
 			successfulTests++;
 		} else {
 			failedTests++;
 			failedTestNumbers.add(9);
 		}
 		
 		if (Validator.check(Permission.NONE, t.getRole(dominic), 10)) {
 			successfulTests++;
 		} else {
 			failedTests++;
 			failedTestNumbers.add(10);
 		}
 		
 		try {
 			Authenticator.checkPermissions(thomas, anotherBand, bandAddMember);
 			failedTests++;
 			failedTestNumbers.add(11);
 		} catch (InsufficientPermissionsException e) {
 			successfulTests++;
 			
 		}
 		
 		try {
 			Authenticator.checkPermissions(dominic, tempBand, bandGetMembers);
 			successfulTests++;
 		} catch (InsufficientPermissionsException e) {
 			failedTests++;
 			failedTestNumbers.add(12);
 		}
 		
 	}
 
 }

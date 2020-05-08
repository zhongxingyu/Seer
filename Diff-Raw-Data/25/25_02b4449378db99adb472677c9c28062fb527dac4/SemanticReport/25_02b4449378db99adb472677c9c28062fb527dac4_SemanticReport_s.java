 package testutils.tester;
 
 
 public class SemanticReport extends SimpleReport {
 	
 	boolean shouldBeOk;
	String normalMsgs;
	String errorMsgs;
	String warningMsgs;
	boolean containsPTPackage;
 
 	public SemanticReport(String name, String[] filenames, boolean shouldBeOk,
 			boolean actual, String normalMsgs, String warningMsgs,
 			String errorMsgs, boolean containsPTPackage) {
 		super(name,filenames,actual);
 		this.shouldBeOk = shouldBeOk;
 		this.normalMsgs = normalMsgs;
 		this.errorMsgs = errorMsgs;
 		this.warningMsgs = warningMsgs;
 		this.containsPTPackage = containsPTPackage;
 	}
 
 	private void printNormalDataIfVerbose() {
 		if (!normalMsgs.isEmpty())
 			System.out.println("verbose normal:\n" + normalMsgs);
 
 		if (!warningMsgs.isEmpty())
 			System.out.println("verbose warning:\n" + warningMsgs);
 
 		if (!errorMsgs.isEmpty())
 			System.out.println("verbose error:\n" + errorMsgs);
 	}
 
 	public boolean hasPassed() {
 		return shouldBeOk == actual;
 	}
 
 	}

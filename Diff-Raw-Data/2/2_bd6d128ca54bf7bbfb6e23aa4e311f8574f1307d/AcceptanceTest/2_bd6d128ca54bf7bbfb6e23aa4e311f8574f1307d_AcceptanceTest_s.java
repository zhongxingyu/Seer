 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics;
 
 /**
  * Acceptance test for a specific requirement
  * @author Dylan Kirby
  */
 public class AcceptanceTest {
 		
 	private static int testCount;
 	private final int testId;
 	private String testName;
 	private String testDescription;
 	private TestStatus testStatus;
 	
 	/**
 	 * Constructs a new acceptance test
 	 * @param testName Name of the acceptance test
 	 * @param testDescription Description of the test
 	 */
 	public AcceptanceTest(String testName, String testDescription) {
 		this.setName(testName);
 		this.testDescription = testDescription;
 		this.testId = testCount++;
 		this.testStatus = TestStatus.STATUS_BLANK;
 	}
 	
 	/**
 	 * Getter for the name
 	 * @return name name of the acceptance test
 	 */
 	public String getName() {
 		return this.testName;
 	}
 	
 	/**
 	 * Setter for the name
 	 * @param name name of the acceptance test
 	 */
 	public void setName(String name) {
 		System.out.println(name);
 		if (name.equals("")) {
 			throw new NullPointerException("Name must not be null");
 		} else {
 			// Limits name to 100 characters
 			if (name.length() > 100) {
 				this.testName = name.substring(0, 100);
 			} else {
 				this.testName = name;
 			}
 		}
 	}
 	
 	/**
 	 * Getter for the description
 	 * @return description of the acceptance test
 	 */
 	public String getDescription() {
 		return this.testDescription;
 	}
 	
 	/**
 	 * Setter for the description
 	 * @param description description of the acceptance test
 	 */
 	public void setDescription(String description) {
 		this.testDescription = description;
 	}
 	
 	/**
 	 * Getter for the ID
 	 * @return the ID of the acceptance test
 	 */
 	public int getId() {
 		return this.testId;
 	}
 	
 	/**
 	 * Getter for the status
 	 * @return status of the acceptance test
 	 */
	public TestStatus getStatus() {
 		return this.testStatus.toString();
 	}
 	
 	/**
 	 * Setter for the status
 	 * @param status status of the acceptance test
 	 */
 	public void setStatus(TestStatus status) {
 		this.testStatus = status;
 	}
 
 }

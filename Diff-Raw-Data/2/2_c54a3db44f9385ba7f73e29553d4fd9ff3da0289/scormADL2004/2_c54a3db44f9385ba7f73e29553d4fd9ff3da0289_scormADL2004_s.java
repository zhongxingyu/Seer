 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.Keys;
 import org.openqa.selenium.NoAlertPresentException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.Select;
 import org.openqa.selenium.support.ui.Wait;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 public class scormADL2004 {
 	private InternetExplorerDriver driver;
 	private Wait<WebDriver> wait;
 	private int loginSession;
 	private String username1 = System.getProperty("username1");
 	private String password1 = System.getProperty("password1");
 	private String username2 = System.getProperty("username2");
 	private String password2 = System.getProperty("password2");
 
 	@Before
 	public void setUp() throws IOException, InterruptedException {
 		driver = new InternetExplorerDriver();
 	    wait = new WebDriverWait(driver, 60);
 	    String testSuiteURL = System.getProperty("testSuiteURL");
 	    String moodleURL = System.getProperty("moodleURL");
 	    if (testSuiteURL == null) {
 			throw new IOException("ADL SCORM 2004 Test Suite URL not specified");
 		} 
 		if (moodleURL == null) {
 			throw new IOException("Moodle Test Site URL not specified");
 		}
 		if (username1 == null) {
 			username1 = "joestudent";
 			password1 = "password";
 		}
 		if (username2 == null) {
 			username2 = "marylearner";
 			password2 = "password";
 		}
 	    driver.get(testSuiteURL);
 	    // Overcome Selenium IE Page load Bug.
 		driver.switchTo().frame("instructions");
 		wait.until(presenceOfElementLocated(By.id("New")));
 		((JavascriptExecutor) driver).executeScript("newTest();");
 		wait.until(presenceOfElementLocated(By.id("pname")));
 		driver.findElement(By.id("pname")).sendKeys("Moodle");
 		driver.findElement(By.id("pversion")).sendKeys("2011033003");
 		driver.findElement(By.id("pvname")).sendKeys("MoodleHQ");
 		driver.switchTo().window("");
 		driver.switchTo().frame("controls");
 		driver.findElement(By.id("continue")).click();
 		driver.switchTo().window("");
 		driver.switchTo().frame("instructions");
 		wait.until(presenceOfElementLocated(By.id("learner1Name")));
 		driver.findElement(By.id("learner1Name")).sendKeys("Moodle Learner 1");
 		driver.findElement(By.id("learner1ID")).sendKeys(username1);
 		driver.findElement(By.id("learner2Name")).sendKeys("Moodle Learner 2");
 		driver.findElement(By.id("learner2ID")).sendKeys(username2);
 		driver.switchTo().window("");
 		driver.switchTo().frame("controls");
 		driver.findElement(By.id("continue")).click();
 		driver.switchTo().window("");
 		((JavascriptExecutor) driver).executeScript("window.open('"+moodleURL+"', 'moodleWindow');");
 		driver.switchTo().window("moodleWindow");
 	}
 	@Test
 	/**
 	 * navigate inside ADL SCORM 2004 Test Course to check for packages uploaded.
 	 */
 	public void loginCourse() throws IOException {
 		driver.findElement(By.linkText("ADL SCORM Test 2004")).sendKeys(Keys.ENTER);
 		wait.until(presenceOfElementLocated(By.id("username")));
 		driver.findElement(By.id("username")).clear();
 		driver.findElement(By.id("password")).clear();
 		//Login username password as specified
 		driver.findElement(By.id("username")).sendKeys(username1);
 		driver.findElement(By.id("password")).sendKeys(password1);
 		loginSession = 1;
 		driver.findElement(By.id("loginbtn")).click();		
 		fetchAllContent();
 	}
 	public void fetchAllContent () throws IOException {
 		if (System.getProperty("filePath") != null) {
 			// Execute tests specified in file
 			validateSCORM(readFile(System.getProperty("filePath")));
 		} else if (System.getProperty("test") != null){
 			// Execute tests specified as argument
 			validateSCORM(System.getProperty("test"));
 		} else {
 			// Execute all tests
 			validateSCORM(null);
 		}
 		//System.out.println(scormUploaded);
 		//driver.findElement(By.partialLinkText("SCORM package")).click();
 	}
 	/**
 	 * Compile SCO Array
 	 * @return
 	 */
 	public int fetchAllSCO() {
 		wait.until(presenceOfElementLocated(By.className("ygtvrow")));
 		String scoes = driver.findElement(By.className("ygtvchildren")).getText();
 		int scoCount = counter(scoes, "\n");
 		// Return 1, Java does not allow while(1) loop declared in defaultscohandler
 		return 1;
 	}
 	public void switchLoginSession(int toLogin) {
 		// To be safe and avoid Selenium Native click element, using submit wherever possible, to be fixed in 2.5.0
 		WebElement element;
 		driver.switchTo().window("moodleWindow");
 		// Check if already logged in
 		if (!isElementPresent(By.linkText("Login"))) {
 			//driver.findElement(By.linkText("Logout")).click();
 			// Workaround for Selenium bug - 1020, to be fixed in 2.5.0
 			driver.findElement(By.linkText("Logout")).sendKeys(Keys.ENTER);
 			wait.until(presenceOfElementLocated(By.linkText("ADL SCORM Test 2004")));
 			driver.findElement(By.linkText("ADL SCORM Test 2004")).click();
 		}
 		if (toLogin != 2) {
 			wait.until(presenceOfElementLocated(By.id("username")));
 			driver.findElement(By.id("username")).clear();
 			element = driver.findElement(By.id("password"));
 			element.clear();
 			driver.findElement(By.id("username")).sendKeys(username1);
 			element.sendKeys(password1);
 		} else {
 			wait.until(presenceOfElementLocated(By.id("username")));
 			driver.findElement(By.id("username")).clear();
 			element = driver.findElement(By.id("password"));
 			driver.findElement(By.id("username")).sendKeys(username2);
 			element.sendKeys(password2);
 		}
 		element.submit();
 	}
 	public String readUserInstructions() {
 		// Switch to window of TestSuite
 		driver.switchTo().window("");
 		driver.switchTo().frame("currentInstructions");
 		return driver.findElement(By.id("userInstruction")).getText();
 	}
 	public void checkLoginSession() {
 		// SwitchTo window before switching to frame.
 		String userInstruction = readUserInstructions();
 		if (userInstruction.contains("Moodle Learner 1") && loginSession != 1) {
 			switchLoginSession(1);
 		} else if(userInstruction.contains("Moodle Learner 2") && loginSession != 2) {
 			switchLoginSession(2);
 		}
 	}
 	public void answerDefaultUI(int scoNo) {
 		driver.switchTo().window("moodleWindow");
 		driver.switchTo().frame("scorm_object");
 		System.out.println("Testing Activity "+scoNo+". Please Wait... ");
 		driver.findElement(By.id("tRadio0")).click();
 		if (scoNo == 1) {
 			driver.findElement(By.id("fRadio1")).click();
 		} else {
 			driver.findElement(By.id("tRadio1")).click();
 		}
 		driver.findElement(By.id("submit")).click();
 		System.out.println("Testing of Activity Completed");
 	}
 	/**
 	 * Execute CM-01 Test
 	 */
 	public void handleCM01Test() {
 		String [] scoUrl;
 		scoUrl = new String [2];
 		String currentUrl = driver.getCurrentUrl();
 		wait.until(presenceOfElementLocated(By.id("scorm_object")));
 		driver.switchTo().frame("scorm_object");
 		wait.until(presenceOfElementLocated(By.id("tRadio0")));
 		//WebElement elementIR = driver.findElement(By.id("fRadio0"));
 		//elementIR.click();
 		wait.until(presenceOfElementLocated(By.xpath("//label[contains(.,'Did the LMS')]")));
 		driver.findElement(By.id("tRadio0")).click();
 		driver.findElement(By.id("fRadio1")).click();
 		driver.findElement(By.id("fRadio2")).click();
 		driver.findElement(By.id("submit")).click();
 		//elementIR.submit();
 		/*driver.switchTo().window("moodleWindow");
 		//driver.findElement(By.id("ygtvlabelel2")).click();
 		wait.until(presenceOfElementLocated(By.id("scorm_object")));
 		System.out.println("done 1st");
 		driver.switchTo().frame("scorm_object");
 		//wait.until(presenceOfElementLocated(By.id("submit")));
 		System.out.println("done 2nd");*/
 		wait.until(presenceOfElementLocated(By.xpath("//div[contains(.,'Completed.')]")));
 		System.out.println("Testing Activity 2. Please Wait...");
 		try {
 			Thread.sleep(3000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		driver.switchTo().window("moodleWindow");
 		scoUrl[0] = driver.findElement(By.xpath("//span[contains(.,'Activity 2')]")).getAttribute("title");
 		driver.get(currentUrl+'?'+scoUrl[0]);
 		wait.until(presenceOfElementLocated(By.id("scorm_object")));
 		driver.switchTo().frame("scorm_object");
 		wait.until(presenceOfElementLocated(By.xpath("//label[contains(.,'Did the LMS')]")));
 		driver.findElement(By.id("fRadio0")).click();
 		driver.findElement(By.id("tRadio1")).click();
 		driver.findElement(By.id("submit")).click();
 		driver.switchTo().window("moodleWindow");
 		System.out.println("Testing Activity 3. Please Wait...");
 		try {
 			Thread.sleep(3000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		scoUrl[1] = driver.findElement(By.xpath("//span[contains(.,'Activity 3')]")).getAttribute("title");
 		driver.get(currentUrl+'?'+scoUrl[1]);
 		wait.until(presenceOfElementLocated(By.id("scorm_object")));
 		wait.until(presenceOfElementLocated(By.xpath("//div[contains(.,'Completed.')]")));
 	}
 	/**
 	 * Handle / Launch SCO's
 	 */
 	public void defaultSCOHandler() {
 		// Handle Selenium IE Bug by explicit wait
 		int scoCount = fetchAllSCO();
 		int scoNo = 1;
 		String userInstruction, activityName;
 		// Not infinite loop, testing based on TestSuite Instrcutions
 		while (scoCount > 0) {
 			//First Activity does not need to be launched, launch after first
 			driver.switchTo().window("moodleWindow");
 			driver.switchTo().frame("scorm_object");
 			wait.until(presenceOfElementLocated(By.id("teststatus")));
 			//wait.until(presenceOfElementLocated(By.xpath("//*[contains(.,'progress...')]")));
 			// Allow loading time, specially for UI tests
 			try {
 				Thread.sleep(5000);
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 			userInstruction = readUserInstructions();
 			if (userInstruction.contains("LMS User Interface")) {
 				answerDefaultUI(scoNo);
 				driver.switchTo().window("moodleWindow");
 			} else {
 				// Print only for 1st Activity.
 				if (scoNo == 1) {
 					System.out.println("Testing Activity. Please Wait... ");
 				}
 				driver.switchTo().window("moodleWindow");
 				driver.switchTo().frame("scorm_object");
 				//Check only for "Completed." as some packages contain extra spaces
 				wait.until(presenceOfElementLocated(By.xpath("//div[contains(.,'Completed.')]")));
 				System.out.println("Testing of Activity Completed.");
 				driver.switchTo().window("moodleWindow");
 			}
 			// Read instructions to launch new activity
 			// First Activity does not need to be launched.
 			
 			userInstruction = readUserInstructions();
 			if (!userInstruction.contains("relaunch") && !(userInstruction.contains("please exit the package/course")) 
 					&& userInstruction.contains("launch")) {
 				activityName = userInstruction.substring((userInstruction.lastIndexOf("nch ")+4), userInstruction.lastIndexOf("."));
 				activityName = activityName.trim();
 				driver.switchTo().window("moodleWindow");
 				driver.findElement(By.xpath("//span[contains(.,'"+activityName+"')]")).click();
 				System.out.println("Testing Activity "+activityName+". Please Wait... ");
 				// Allow loading time of new Activity
 				try {
 					Thread.sleep(5000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			} else if (userInstruction.contains("trigger a Continue Navigation Event")) {
 				driver.switchTo().window("moodleWindow");
 				driver.findElement(By.id("nav_next-button")).sendKeys(Keys.ENTER);
 			} else if (userInstruction.contains("trigger a Previous Navigation Event")) {
 				driver.switchTo().window("moodleWindow");
 				driver.findElement(By.id("nav_prev-button")).sendKeys(Keys.ENTER);
 			} else if (userInstruction.contains("relaunch")) {
 				driver.switchTo().window("moodleWindow");
 				driver.findElement(By.linkText("ADL 2004")).sendKeys(Keys.ENTER);
 				launchTestPackage(userInstruction.substring((userInstruction.lastIndexOf("age ")+4), 
 						(userInstruction.lastIndexOf("to")-1)));
 			} else if (userInstruction.contains("please exit the package/course")) {
 				if (userInstruction.contains("launch")) {
 					driver.switchTo().window("moodleWindow");
 					driver.findElement(By.linkText("ADL 2004")).sendKeys(Keys.ENTER);
 					launchTestPackage(userInstruction.substring((userInstruction.lastIndexOf("age ")+4), 
 							userInstruction.lastIndexOf(".")));
 				} else {
 					System.out.println("Testing of Package Completed.");
 					break;
 				}
 			}
 			scoNo++;
 		}
 		// Old way of handling tests.
 		/*for (int i = 1; i <= scoCount; i++) {
 			//First Activity does not need to be launched.
 			if(i != 1) {
 				driver.findElement(By.id("ygtvlabelel"+i)).click();
 			}
 			driver.switchTo().frame("scorm_object");
 			wait.until(presenceOfElementLocated(By.id("teststatus")));
 			wait.until(presenceOfElementLocated(By.xpath("//*[contains(.,'progress...')]")));
 			boolean uiTestPresent = isInstructionForUIPresent();
 			if (uiTestPresent) {
 				answerDefaultUI(i);
 			} else {
 				System.out.println("Testing Activity "+i+". Please Wait... ");
 				//Check only for "Completed." as some packages contain extra spaces
 				wait.until(presenceOfElementLocated(By.xpath("//*[contains(.,'Completed.')]")));
 				System.out.println("Testing of Activity "+i+" Completed.");
 				driver.switchTo().window("moodleWindow");
 			}
 		}
 		// Some test packages need to launch 
 		System.out.println("Testing Completed.");
 		driver.switchTo().window("");
 		driver.switchTo().frame("controls");
 		wait.until(presenceOfElementLocated(By.id("continue")));
 		driver.findElement(By.id("continue")).click(); */
 	} 
 	/**
 	 * Select All valid tests to perform in ADL 2004 Test Suite
 	 * @param testsToExecute
 	 */
 	public void initialiseTestSuite(String[] testsToExecute){
 		driver.switchTo().window("");
 		driver.switchTo().frame("instructions");
 		Select multipleTestsSelect = new Select(driver.findElement(By.id("possible")));
 		multipleTestsSelect.deselectAll();
 		for(int i = 0; i < testsToExecute.length; i++) {
 			multipleTestsSelect.selectByVisibleText(testsToExecute[i]);
 		}
 		// Workaround for Selenium bug - 1020, to be fixed in 2.5.0
 		driver.findElement(By.id("add")).sendKeys(Keys.ENTER);
 		driver.switchTo().window("");
 		driver.switchTo().frame("controls");
 		driver.findElement(By.id("continue")).click();
 		try {
 			driver.switchTo().alert().accept();
			System.out.println("WARNING: Not all 2004 packages are being tested. " +
					"The SCORM 2004 Conformance Label will not be evaluated.");
 		} catch (NoAlertPresentException ex) {
 		}
 				
 	}
 	public void launchTestPackage (String testPackageName) {
 		// Check to see which user should launch the package.
 		checkLoginSession();
 		driver.switchTo().window("moodleWindow");
 		wait.until(presenceOfElementLocated(By.xpath("//*[contains(.,'Weekly outline')]")));
 		if (testPackageName.equalsIgnoreCase("DDM")) {
 			driver.findElement(By.partialLinkText(testPackageName+"a SCORM package")).sendKeys(Keys.ENTER);
 		} else {
 			driver.findElement(By.partialLinkText(testPackageName+" SCORM package")).sendKeys(Keys.ENTER);
 		}
 		// Workaround for WebDriver bug, Seleniu, IE.
 		wait.until(presenceOfElementLocated(By.id("page-footer")));
 		// Refresh Driver to crawl page again.
 		driver.switchTo().window("moodleWindow");
 		if(isElementPresent(By.id("n"))) {
 			WebElement element = driver.findElement(By.id("n"));
 			element.click();
 			element.submit();
 			//driver.findElement(By.xpath("//input[@value='Enter']")).click();
 		}
 	}
 	/**
 	 * Handle / Launches test packages in order they are specified.
 	 * @param String[] testsToExecute
 	 */
 	public void testsHandler(String[] testsToExecute) {
 		if (testsToExecute[0] == "") {
 			// No tests uploaded
 			throw new NullPointerException("No specified tests found or uploaded.");
 		}
 		initialiseTestSuite(testsToExecute);
 		driver.switchTo().window("");
 		driver.switchTo().frame("currentInstructions");
 		wait.until(presenceOfElementLocated(By.xpath("//*[contains(.,'Login to the LMS')]")));
 		// Check if current user login is the required user logged in
 		checkLoginSession();
 		driver.switchTo().window("moodleWindow");
 		// Wait till Page completely loads - Selenium Bug
 		wait.until(presenceOfElementLocated(By.xpath("//*[contains(.,'Weekly outline')]")));
 		for (int i = 0; i<testsToExecute.length; i++) {
 			System.out.println("Testing ADL SCORM 2004 "+testsToExecute[i]+" package...");
 			launchTestPackage(testsToExecute[i]);
 			//switch cannot compare string
 			// TODO add handle for CM-01 Test
 			if(testsToExecute[i].equalsIgnoreCase("CM-01")) {
 				handleCM01Test();
 			}
 			// default case
 			else {
 				defaultSCOHandler();
 			}
 			driver.switchTo().window("moodleWindow");
 			driver.findElement(By.linkText("Exit activity")).click();
 			wait.until(presenceOfElementLocated(By.partialLinkText(testsToExecute[i]+" SCORM package")));
 			driver.switchTo().window("");
 			driver.switchTo().frame("controls");
 			wait.until(presenceOfElementLocated(By.id("continue")));
 			driver.findElement(By.id("continue")).sendKeys(Keys.ENTER); 
 		}
 		driver.findElement(By.linkText("Logout")).click();
 		System.out.println("Tested All Packages. The Script will now exit.");
 	}
 	/**
 	 * Pulls out and formulates a list of SCORM packages uploaded.
 	 * @param tests
 	 */
 	public void validateSCORM (String tests) {
 		if (tests == null) {
 			//Default case, define all tests
 			tests = "API,DMI,DMB,DDM,CM-01,CM-02a,CM-02b,CM-03a,CM-03b,CM-04a," +
 					"CM-04b,CM-04c,CM-04d,CM-04a,CM-05,CM-06,CM-07a,CM-07b,CM-07c," +
 					"CM-07d,CM-07e,CM-07f,CM-07b,CM-08,CM-09aa,CM-09ab,CM-09ba," +
 					"CM-09bb,CM-09ca,CM-09cb,CM-09ab,CM-10,CM-11,CM-13,CM-14,CM-15," +
 					"CM-16,CM-17a,CM-17b,CO-01,CO-02a,CO-02b,CO-03,CO-04a,CO-04b,CO-04c," +
 					"CO-05a,CO-05b,CO-05a,CO-06,CO-07a,CO-07b,CO-08a,CO-08b," +
 					"CO-09,CO-10,CO-11,CO-12a,CO-12b,CO-12c,CO-12d,CO-13a,CO-13b," +
 					"CT-01,CT-02,CT-03,CT-04,CT-05,CT-06,CT-07," +
 					"MS-01,MS-02,MS-03,MS-04,MS-05a,MS-05b,MS-06,MS-07,OB-01a," +
 					"OB-01b,OB-01c,OB-02a,OB-02b,OB-03a,OB-03b,OB-03c,OB-04,OB-05a," +
 					"OB-05b,OB-05c,OB-06,OB-07a,OB-07b,OB-08a,OB-08b,OB-09a,OB-09b," +
 					"OB-10a,OB-10b,OB-10c,OB-10d,OB-11a,OB-11b,OB-12a,OB-12b,OB-12c," +
 					"OB-13a,OB-13b,OB-13c,OB-14a,OB-14b,OB-15,OB-16a,OB-16b,OB-16c," +
 					"OB-16d,OB-17a,OB-17b,RU-01aa,RU-01ab,RU-01ba,RU-01bb,RU-02a," +
 					"RU-02b,RU-03a,RU-03b,RU-04aa,RU-04ab,RU-04ba,RU-04bb,RU-04bc," +
 					"RU-04bd,RU-05a,RU-05b,RU-06a,RU-06b,RU-07a,RU-07b,RU-07c,RU-08a," +
 					"RU-08b,RU-09,RU-10,RU-11,RU-12a,RU-12b,RU-13a,RU-13b,RU-13c," +
 					"RU-13d,RU-13e,RU-14a,RU-14b,RU-14c,RU-14d,RU-15a,RU-15b,RU-15c,RU-15d," +
 					"RU-16,RU-17a,RU-17b,RU-18a,RU-18b,RU-19a,RU-19b,RU-12a,SX-02," +
 					"SX-03,SX-04a,SX-04b,SX-05,SX-06,SX-07a,SX-07b,SX-07c,SX-07d,SX-07e," +
 					"SX-08a,SX-08b,SX-09,SX-10a,SX-10b,SX-10c,SX-10d,SX-11a,SX-11b," +
 					"SX-11c,SX-12a,SX-12b,SX-12c,T-01a,T-01b";
 		}
 		// Check for Selenium page load bug in IE WebDriver
 		wait.until(presenceOfElementLocated(By.className("section")));
 		// Finds all content uploaded in the course.
 		String content = driver.findElement(By.className("section")).getText();
 		content = content.replace("\n", ",");
 		String[] testsToExecute = testsToExecute(content, tests);
 		testsHandler(testsToExecute);
 	}
 	/**
 	 * Build up array of tests to execute / display warning if specified test not found
 	 * @param content
 	 * @param tests
 	 * @return
 	 */
 	public String[] testsToExecute (String content, String tests) {
 		String sub = null, temp ="";
 		//Hack to find last test
 		tests += ","; 
 		content = content.toLowerCase();
 		int i = 0;
 		int j = tests.indexOf(",");
 		while (j != -1) {
 			sub = tests.substring(i, j);
 			//TODO add handle for CM-01
 			/*if(content.toLowerCase().contains("CM-01") && sub.equalsIgnoreCase("CM-01")) {
 				System.out.println("ADL SCORM 2004 CM-01 Test not supported");
 				i = j+1;
 				j = tests.indexOf(",", i);
 				continue;
 			}*/
 			if(sub.toLowerCase().contains("ddm") && content.contains("ddma") && content.contains("ddmb")) {
 				temp += sub+',';
 			}
 			else if (content.contains(sub.toLowerCase())) {
 				temp += sub+',';
 			} else {
 				System.out.println("WARNING: ADL SCORM 2004 "+sub+"- Not uploaded."
 						+" The test script will not test "+sub+"\n");
 			}
 			//Move to next test
 			i = j+1;
 			j = tests.indexOf(",", i);
 		}
 		String[] testsToExecute = temp.split(",");
 		return testsToExecute;
 	}
 	/**
 	 *  Count number, by manipulating String
 	 * @param content
 	 * @param delimeter
 	 * @return
 	 */
 	 public int counter(String content, String delimeter) {
 		 int len = delimeter.length();
 		 int count = 0;
 		 if (len > 0) {  
 			 int start = content.indexOf(delimeter);
 			 while (start != -1) {
 				 count++;
 				 start = content.indexOf(delimeter, start+len);
 			 }
 		 }
 		 return count+1;
 	 }
 	/**
 	 * Read File for tests specified. 
 	 * @param filePath
 	 * @return
 	 * @throws IOException
 	 */
 	public String readFile(String filePath) throws IOException {
 		String tests = null;
 		try {
 			FileReader fileReader = new FileReader(filePath);
 			BufferedReader br = new BufferedReader(fileReader);
 			while ((tests += br.readLine()) != null) {
 			 //read till end of file
 			}
 		} catch (IOException ex) {
 			System.out.println("Error reading file.\n" + ex.getMessage());
 		}
 		if(tests == null) {
 			throw new IOException("File Specified is either empty or un-readable");
 		}
 		tests = tests.replace("\n", ",");
 		return tests;
 	}
 	@After
 	public void tearDown() {
 		driver.close();
 		driver.switchTo().window("");
 		driver.close();
 	}
 	/**
 	 * Explicit Wait for element function
 	 * @param locator
 	 * @return
 	 */
 	public ExpectedCondition<WebElement> presenceOfElementLocated(final By locator) {
 	    return new ExpectedCondition<WebElement>() {
 	      public WebElement apply(WebDriver webDriver) {
 	        return webDriver.findElement(locator);
 	      }
 	    };
 	}
 	public boolean isElementPresent (final By locator) {
 		List<WebElement> elementPresent = driver.findElements(locator);
 		if (!elementPresent.isEmpty()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 }

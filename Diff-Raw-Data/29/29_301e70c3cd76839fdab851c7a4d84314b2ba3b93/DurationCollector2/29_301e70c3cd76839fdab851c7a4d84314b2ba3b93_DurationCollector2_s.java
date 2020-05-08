 package me.chriskeith.dc;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import org.openqa.selenium.*;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import java.util.regex.*;
 import java.text.SimpleDateFormat;
 
 /**
  * Ping google maps for estimated commute duration. Write duration and time stamp to tab-separated file.
  * To get better data while running:
  * - Don't manually close Firefox window.
  * - Don't switch networks (e.g., log into a VPN).
  * Assume : Java VM is running in the appropriate time zone.
  * @author ckeith
  */
 public class DurationCollector2 {
 	
 	class CollectionParams {
 		String personId;
 		String homeLocation;
 		String workLocation;
 		
 		CollectionParams(String personId, String homeLocation, String workLocation) {
 			this.personId = personId;
 			this.homeLocation = homeLocation;
 			this.workLocation = workLocation;
 		}
 	}
 	
 	List<CollectionParams> collectionParams;
 	final private String dirForResults = "/tmp";
 	final private Pattern digitPattern = Pattern.compile("[0-9]+");
 	
 	// A format that will convert to a date when pasted into a Google spreadsheet.
	final private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 
 	private int collectDuration(WebDriver driver, String origin, String destination)
 			throws Exception {
 		driver.get("https://maps.google.com/");
 		driver.findElement(By.id("d_launch")).click();
 		driver.findElement(By.id("d_d")).clear();
 		driver.findElement(By.id("d_d")).sendKeys(origin);
 		driver.findElement(By.id("d_daddr")).clear();
 		driver.findElement(By.id("d_daddr")).sendKeys(destination + "\n");
 // Started being erratic... Replaced with \n above.
 //		driver.findElement(By.id("d_sub")).click();
 		
 		// GMaps can show multiple alternative routes. Use the quickest.
 		List<WebElement> wList = driver.findElements(By
 				.xpath("//span[contains(text(), \"In current traffic: \")]"));
 		int minutes = Integer.MAX_VALUE;
 		for (WebElement w : wList) {
 			Matcher m = digitPattern.matcher(w.getText());
 			if (m.find()) {
 				int newVal = Integer.parseInt(m.group());
 				if (m.find()) {
 					// First number was hour(s).
 					newVal = (newVal * 60) + Integer.parseInt(m.group());
 				}
 				if (newVal < minutes) {
 					minutes = newVal;
 				}
 			}
 		}
 		return minutes;
 	}
 
 	private BufferedWriter getWrite(String personId) throws Exception {
 		String filePathString = dirForResults + "/" + personId + "_commuteTimes.txt";
 		FileWriter fstream;
 		File f = new File(filePathString);
 		if (f.exists()) {
 			fstream = new FileWriter(filePathString, true);
 		} else {
 			fstream = new FileWriter(filePathString);
 		}
 		return new BufferedWriter(fstream);
 	}
 
 	private void collectDurations(WebDriver driver) throws Exception {
 			String origin;
 			String destination;
 			while (true) {
 				sleepUntilStart();
 				int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 				for (CollectionParams cp : collectionParams) {
 					if (hour < 12) { // switch directions at 12 noon.
 						origin = cp.homeLocation;
 						destination = cp.workLocation;
 					} else {
 						origin = cp.workLocation;
 						destination = cp.homeLocation;
 					}
 					int newDuration = this.collectDuration(driver, origin, destination);
 					writeDuration(cp.personId, new Date(), newDuration);
 				}
				// Sample every two minutes.
				// For The Future : possibly randomize the sleep time.
				Thread.sleep(2 * 60 * 1000);
 			}
 	}
 	
 	private void sleepUntilStart() throws Exception {
 		Calendar now = Calendar.getInstance();
 		int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
 		int dayIncrement = 0;
 		if (dayOfWeek == Calendar.SATURDAY) {
 			dayIncrement = 2;
 		} else if (dayOfWeek == Calendar.SUNDAY) {
 			dayIncrement = 1;
 		} else {
 			int hour = now.get(Calendar.HOUR_OF_DAY);
 			// If outside the 8 p.m. to 4 a.m. range, sleep until 4 a.m.
 			if (19 < hour) {
 				dayIncrement = 1;
 			}
 			if (hour < 4) {
 				dayIncrement = 1;
 			}
 		}
 		if (dayIncrement > 0) {
			Calendar start = Calendar.getInstance();
 			// TODO : Will this work on Dec 31?
 			start.set(Calendar.DAY_OF_YEAR, start.get(Calendar.DAY_OF_YEAR) + dayIncrement);
 			start.set(Calendar.HOUR_OF_DAY, 4);
 			start.set(Calendar.MINUTE, 0);
			long millis = start.getTimeInMillis() - now.getTimeInMillis();
			long minutes = (millis / 60000);
			System.out.println("About to sleep for " + (minutes / 60) + " hours, " + (minutes % 60) + " minutes.");
			Thread.sleep(millis);
 		}
 	}
 	
 	private void writeDuration(String personId, Date date, int duration) throws Exception {
 		if ((0 < duration) && (duration < Integer.MAX_VALUE)) {
 			BufferedWriter out = this.getWrite(personId);
 			String s = dateFormat.format(date) + "\t" + duration;
 			out.write(s + System.getProperty("line.separator"));
 			out.flush();
 			System.out.println(s);
 			out.close();
 		}
 	}
 	
 	private void runWithRetries() {
 		for (int retries = 0; retries < 5; retries++) {
 			WebDriver driver = null;
 			try {
 				driver = new FirefoxDriver();
 				driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
 				collectDurations(driver);
 			} catch (Exception e) {
 				System.out.println(new Date().toString() + System.getProperty("line.separator") + e);
 			} finally {
 				if (driver != null) {
 					driver.quit();
 				}
 			}
 		}
 	}
 
 	private void loadCollectionParams(String[] args) throws Exception {
 		collectionParams = new ArrayList<CollectionParams>(1);
 		if (args.length == 1) {
 			File otherCollectionParams = new File(args[0]);
 			if (!otherCollectionParams.exists()) {
 				throw new RuntimeException("Can't find: " + otherCollectionParams.getAbsolutePath());
 			}
 			BufferedReader br = new BufferedReader(new FileReader(args[0]));
 		    try {
 		        String personId = br.readLine();
 		        while (personId != null) {
 		        	String home = br.readLine();
 		        	if (home == null) {
 						throw new RuntimeException("Home not specified for: " + personId);
 		        	}
 		        	String work = br.readLine();
 		        	if (work == null) {
 						throw new RuntimeException("Work not specified for: " + personId);
 		        	}
 		        	collectionParams.add(new CollectionParams(personId, home, work));
 		        	personId = br.readLine();
 		        }
 		    } finally {
 		        br.close();
 		    }
 		}
 		// Add this one last, so the browser shows it (to help debugging/monitoring).
 		collectionParams.add(new CollectionParams("ChristopherKeith", 
 				"368 MacArthur Blvd, San Leandro, CA 94577",
 				"3200 Bridge Pkwy Redwood City, CA"));
 	}
 	
 	public void run(String[] args) throws Exception {
 		File dir = new File(dirForResults);
 		if (!dir.exists()) {
 			if (!dir.mkdir()) {
 				throw new RuntimeException("Unable to create: " + dir.getAbsolutePath());
 			}
 		}
 		loadCollectionParams(args);
 		runWithRetries();
 	}
 
 	public static void main(String[] args) {
 		try {
 			new DurationCollector2().run(args);
 		} catch (Exception e) {
 			System.out.println(e);
 		}
 	}
 }

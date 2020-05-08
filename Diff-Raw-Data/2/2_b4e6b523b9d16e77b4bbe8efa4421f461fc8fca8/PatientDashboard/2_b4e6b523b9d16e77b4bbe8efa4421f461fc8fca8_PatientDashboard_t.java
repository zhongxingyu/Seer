 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 public class PatientDashboard extends AbstractPageObject {
 
 	public static final String CHECKIN = "Tcheke"; 
 	public static final String CONSULTATION = "Consultation";
 	public static final String VITALS = "Siy Vito";
 	public static final String RADIOLOGY = "Preskripsyon Radyoloji";
 	
 	public static final String ACTIVE_VISIT_MESSAGE = "Vizit aktiv";
 	
 	public PatientDashboard(WebDriver driver) {
 		super(driver);
 	}
 
 	public void orderXRay(String study1, String study2) {
 		driver.findElement(By.className("icon-x-ray")).click();
 		
 		driver.findElement(By.name("clinicalHistory")).sendKeys("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc eu neque ut mi auctor pulvinar. Mauris in orci non sem consequat posuere.");
 		setClearTextToField("study-search", study1);
 		driver.findElement(By.linkText(study1)).click();
 		
 		setClearTextToField("study-search", study2);
 		driver.findElement(By.linkText(study2)).click();
 		
 		driver.findElement(By.id("next")).click();
 	}
 
 	// TODO: add more data to compare?
 	public boolean hasOrder(String orderDetails) {
 		return driver.findElement(By.id("content")).getText().contains(orderDetails);
 	}
 
 	public Set<String> getIdentifiers() {
 		String temp = driver.findElement(By.className("identifiers")).getText().trim();
 		StringTokenizer st = new StringTokenizer(temp.substring(3), ",");
 		Set<String> result = new HashSet<String>();
 		while(st.hasMoreTokens()) {
 			result.add(st.nextToken());
 		}
 		return result;
 	}
 
     public boolean verifyIfSuccessfulMessageIsDisplayed(){
         if (driver.findElement(By.className("icon-ok")) != null){
             return true;
         }
         return false;
     }
 	
     
     public boolean hasActiveVisit() {
 		return driver.findElement(By.id("visit-details")).getText().contains(ACTIVE_VISIT_MESSAGE);
 	}
 
 	public void deleteEncounter(String encounterName) throws Exception {
 		String encounterId = findEncounterId(encounterName);
 		List<WebElement> encounters = driver.findElements(By.cssSelector("i.deleteEncounterId"));
 		for (WebElement encounter : encounters) {
 	        if (encounter.getAttribute("data-encounter-id").equals(encounterId))
 	        	encounter.click();
 	    }
 		driver.findElement(By.xpath("//*[@id='delete-encounter-dialog']/div[2]/button[1]")).click();
 	}
 	
 	public String findEncounterId(String encounterName) throws Exception {
 		List<WebElement> encounters = driver.findElements(By.cssSelector("span.encounter-name"));
 		for (WebElement encounter : encounters) {
 	        if (encounter.getText().equals(encounterName))
 	        	return encounter.getAttribute("data-encounter-id");
 	    }
 		
 		throw new Exception("No encounter of this type found.");
 	}
 
 	public Integer countEncouters(String encounterName) {
 		int count = 0;
 		List<WebElement> encounters = driver.findElements(By.cssSelector("span.encounter-name"));
 		for (WebElement encounter : encounters) {
 	        if (encounter.getText().equals(encounterName))
 	        	count++;
 	    }
 		return count;
 	}
 
 	public void startVisit() {
 		driver.findElement(By.cssSelector("i.icon-check-in")).click();
 		driver.findElement(By.cssSelector("#quick-visit-creation-dialog .confirm")).click();
 	}
 
 	public void addConsultationNote() {
 		driver.findElement(By.cssSelector("#visit-details .icon-stethoscope")).click();
 		setClearTextToField("diagnosis-search", "asthma");
 		driver.findElement(By.cssSelector("strong.matched-name")).click();
 		driver.findElement(By.cssSelector("#buttons .confirm")).click();
 	}
 
 	public void requestRecord() {
 		driver.findElement(By.cssSelector("i.icon-folder-open")).click();
 		driver.findElement(By.cssSelector("#request-paper-record-dialog .confirm")).click();
 	}
 
 	public String getDossieNumber() {
 		List<WebElement> elements = driver.findElements(By.cssSelector(".identifiers span"));
		return elements.get(1).getText();
 	}
 
 	public boolean canRequestRecord() {
 		return driver.findElement(By.className("icon-folder-open")).isDisplayed();
 	}
 }

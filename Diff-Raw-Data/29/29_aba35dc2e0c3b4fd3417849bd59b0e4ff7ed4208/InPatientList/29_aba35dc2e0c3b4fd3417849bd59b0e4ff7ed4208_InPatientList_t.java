 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
import com.google.common.base.Predicate;
 import org.openmrs.module.mirebalais.smoke.dataModel.Visit;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
 
 public class InPatientList extends AbstractPageObject {
 	
 	private static final By ACTIVE_VISIT_TABLE_DATAL = By.cssSelector("#active-visits td");
 	
 	private static final By INPATIENTS_LOCATION_OPTION = By.cssSelector("#inpatients-filterByLocation-field option");
 
 	public InPatientList(WebDriver driver) {
 		super(driver);
 	}
 	
 	public List<Visit> getVisits() {
 		List<Visit> visits = new ArrayList<Visit>();
 		List<WebElement> tds = driver.findElements(ACTIVE_VISIT_TABLE_DATAL);
 		for (int i = 0; i < tds.size(); i += 4) {
 			visits.add(new Visit(tds.get(i).getText(), tds.get(i + 1).getText(), cleanDispositionPlace(tds.get(i + 2)
 			        .getText()), cleanDispositionPlace(tds.get(i + 3).getText())));
 		}
 		return visits;
 	}
 	
 	public String getCurrentWard(String patientIdentifier) throws Exception {
 		return getVisit(patientIdentifier).getCurrentWard();
 	}
 	
 	public String getFirstAdmitted(String patientIdentifier) throws Exception {
 		return getVisit(patientIdentifier).getFirstAdmitted();
 	}
 	
 	public void filterBy(String ward) throws Exception {
 		clickOnOptionLookingForText(ward, INPATIENTS_LOCATION_OPTION);
 	}
 	
 	public void waitUntilInpatientListIsFilteredBy(final String ward) {
		//wait5seconds.until(stalenessOf(driver.findElement(By.className("inpatient-count"))));
 		wait5seconds.until(new Predicate<WebDriver>() {
 
 			@Override
 			public boolean apply(@Nullable WebDriver webDriver) {
 				return isListFilteredBy(ward);
 			}
 		});
 	}
 	
 	private boolean isListFilteredBy(String ward) {
 		for (Visit visit : getVisits()) {
 			if (!visit.getCurrentWard().contains(ward)) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	private Visit getVisit(String patientIdentifier) throws Exception {
 		List<Visit> visits = this.getVisits();
 		for (Visit visit : visits) {
 			if (visit.getPatientId().contains(patientIdentifier)) {
 				return visit;
 			}
 		}
 		throw new Exception(String.format("Visit not found for patient %s", patientIdentifier));
 	}
 	
 	private String cleanDispositionPlace(String rawText) {
 		return rawText.substring(0, rawText.indexOf('\n')).trim();
 	}
 	
 }

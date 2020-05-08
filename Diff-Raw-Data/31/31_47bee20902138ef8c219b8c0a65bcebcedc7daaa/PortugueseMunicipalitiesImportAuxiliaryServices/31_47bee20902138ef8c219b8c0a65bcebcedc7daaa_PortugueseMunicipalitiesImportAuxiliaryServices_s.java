 package module.geography.domain.task;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.util.ArrayList;
 
 import module.geography.domain.Country;
 import module.geography.domain.CountrySubdivision;
 import module.geography.util.StringsUtil;
 import module.organization.domain.Accountability;
 import myorg._development.PropertiesManager;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import pt.ist.fenixWebFramework.services.Service;
 import pt.utl.ist.fenix.tools.util.i18n.Language;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 public class PortugueseMunicipalitiesImportAuxiliaryServices {
 
     private static final String CTT_MUNICIPALITIESFILE = "/concelhos.txt";
 
     Country portugal;
 
     protected int additions = 0;
 
     protected int deletions = 0;
 
     protected int modifications = 0;
 
     protected int touches = 0;
 
     private final MultiLanguageString municipalityLevelName = StringsUtil.makeName("Concelho", "Municipality");
 
     private static PortugueseMunicipalitiesImportAuxiliaryServices singletonHolder;
 
     public PortugueseMunicipalitiesImportAuxiliaryServices() {
 	super();
     }
 
     protected static PortugueseMunicipalitiesImportAuxiliaryServices getInstance() {
 	if (singletonHolder == null) {
 	    singletonHolder = new PortugueseMunicipalitiesImportAuxiliaryServices();
 	}
 	return singletonHolder;
     }
 
     @Service
     public void executeTask(PortugueseMunicipalitiesImport originalTask) {
 	// let's retrieve Portugal, if it doesn't exist, we must create it
 	portugal = Country.getPortugal();
 	LineNumberReader reader = null;
 	ArrayList<CountrySubdivision> municipalitiesOnFile = new ArrayList<CountrySubdivision>();
 	ArrayList<CountrySubdivision> existingMunicipalities = new ArrayList<CountrySubdivision>();
 
 	try {
 	    File file = new File(PropertiesManager.getProperty("modules.geography.file.import.location") + CTT_MUNICIPALITIESFILE);
 	    if (originalTask.getLastRun() == null || file.lastModified() > originalTask.getLastRun().getMillis()) {
 		DateTime lastReview = new DateTime(file.lastModified());
 		FileInputStream fileReader = new FileInputStream(file);
 		reader = new LineNumberReader(new InputStreamReader(fileReader, "ISO-8859-1"));
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 		    String[] parts = line.split(";");
 		    String districtCode = parts[0];
 		    String municipalityCode = parts[1];
 		    String municipalityName = parts[2];
 		    CountrySubdivision district = portugal.getChildByCode(districtCode);
 		    CountrySubdivision municipality = null;
 		    if (district == null) {
 			//abort the transaction!! throw some kind of exception and warn the user of the outcome
 			originalTask.auxLogInfo("Script aborted because the district with code: " + districtCode
 				+ " couldn't be found");
 			throw new RuntimeException("Script aborted because the district with code: " + districtCode
 				+ " couldn't be found");
 		    } else {
 			//get the municipality
 			municipality = district.getChildByCode(municipalityCode);
 			if (municipality == null) {
 			    municipality = createMunicipality(district, municipalityCode, municipalityName, lastReview);
 			} else {
 			    modifyMunicipality(municipality, municipalityCode, municipalityName, "", lastReview);
 
 			}
 		    }
 		    municipalitiesOnFile.add(municipality);
 		}
 
 		// 'removing' all of the existing municipalities
 
 		// getting all of the existing districts
 		for (CountrySubdivision countrySubdivision : portugal.getChildren()) {
 		    if (countrySubdivision.getLevelName().getContent(Language.pt)
 			    .equalsIgnoreCase(municipalityLevelName.getContent(Language.pt))) {
 			existingMunicipalities.add(countrySubdivision);
 		    }
 		} // let's assert
 		  // which ones are in excess and remove them
 		existingMunicipalities.removeAll(municipalitiesOnFile);
 		for (CountrySubdivision countrySubdivision : existingMunicipalities) {
 		    removeMunicipality(countrySubdivision);
 		}
 		originalTask.auxLogInfo("File last modification was: " + lastReview);
 		originalTask.auxLogInfo(additions + " municipalities added.");
 		originalTask.auxLogInfo(touches + " municipalities unmodified, but whose date has changed.");
 		originalTask.auxLogInfo(modifications + " municipalities modified");
 	    } else {
 		originalTask.auxLogInfo("File unmodified, nothing imported.");
 	    }
 	} catch (FileNotFoundException e) {
 	    e.printStackTrace();
 	} catch (IOException e) {
 	    e.printStackTrace();
 	} finally {
 	    try {
 		if (reader != null) {
 		    reader.close();
 		}
 	    } catch (IOException e) {
 	    }
 	}
     }
 
     private void removeMunicipality(CountrySubdivision municipality) {
 	// modify the district by setting the accountability of the last one
 	// end before the date of the last review
 	Accountability activeAccountability = null;
 	for (Accountability accountability : municipality.getUnit().getParentAccountabilities(
 		municipality.getOrCreateAccountabilityType())) {
 	    if (accountability.isActiveNow()) {
 		activeAccountability = accountability;
 	    }
 	}
 
 	if (activeAccountability != null) {
 	    // make the accountability expire a minimal measure of time before
 	    // of
 	    // the current date
 	    LocalDate endDate = new LocalDate();
 	    // endDate = endDate.minusDays(1);
	    activeAccountability.setEndDate(endDate);
 	}
 	deletions++;
 
     }
 
     private void modifyMunicipality(CountrySubdivision municipality, String municipalityCode, String municipalityName,
 	    String municipalityAcronym, DateTime lastReview) {
 	String originalMunicipalityName = municipality.getName().getContent(Language.pt);
 	String originalMunicipalityAcronym = municipality.getAcronym();
 	// let's check if there are changes on the data:
 	if (!originalMunicipalityName.equals(municipalityName) || !originalMunicipalityAcronym.equals(municipalityAcronym)
 		|| !municipality.getLevelName().equals(municipalityLevelName)) {
 
 	    removeMunicipality(municipality);
 	    deletions--;
 	    // create the new district!
 	    // lets correct the counters afterwards!
 	    createMunicipality(municipality.getParentSubdivision(), municipalityCode, municipalityName, lastReview);
 	    additions--;
 	    modifications++;
 	}
 	// if not let's just 'touch' it
 	else {
 	    touchMunicipality(municipality, lastReview);
 	}
 
     }
 
     private void touchMunicipality(CountrySubdivision municipality, DateTime lastReview) {
 	municipality.setLastReview(lastReview);
 	touches++;
     }
 
     private CountrySubdivision createMunicipality(CountrySubdivision district, String municipalityCode, String municipalityName,
 	    DateTime lastReview) {
 	CountrySubdivision municipality = new CountrySubdivision(district, municipalityName, "", municipalityCode);
 	municipality.setLevelName(municipalityLevelName, false);
 	municipality.setLastReview(lastReview);
 	additions++;
 	return municipality;
     }
 
 }

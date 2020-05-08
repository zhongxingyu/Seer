 /**
  * 
  */
 package module.geography.domain.task;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import pt.ist.fenixWebFramework.services.Service;
 
 import module.geography.domain.Country;
 import module.geography.domain.CountrySubdivision;
 import module.geography.domain.CountrySubdivisionLevelName;
 import myorg.domain.scheduler.Task;
 import myorg.util.BundleUtil;
 
 /**
  * *WARNING* This task shouldn't be called to delete the districts as it removes
  * them without checking for connections that it might have with other domain
  * objects. The right way to remove a district is to put its accountibility date
  * end in this moment and maybe eventually if it's not connected to anything
  * delete it
  * 
  * @author João André Pereira Antunes (joao.antunes@tagus.ist.utl.pt)
  * 
  */
 public class CleanCountrySubLevels extends Task {
 
     /* (non-Javadoc)
      * @see myorg.domain.scheduler.Task#getLocalizedName()
      */
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle("resources/GeographyResources",
 		"label.task.clean.country.sublevels.with.note");
     }
 
     private int countrySubDivisionLevelNameDeletes = 0;
 
     private int countrySubDivisionDeletes = 0;
 
     /* (non-Javadoc)
      * @see myorg.domain.scheduler.Task#executeTask()
      */
     @Override
     @Service
     public void executeTask() {
 	// add to an array all of the countries one wants to clean the sublevels
 	ArrayList<Country> countriesToClean = new ArrayList<Country>();
 	countriesToClean.add(Country.getPortugal());
 	HashMap<String, ArrayList<Integer>> infoByCountry = new HashMap<String, ArrayList<Integer>>();
 	
 	for (Country country : countriesToClean) {
 	    ArrayList<CountrySubdivision> countrySubdivisions = new ArrayList<CountrySubdivision>();
 	    countrySubdivisions.addAll(country.getChildren());
 	    for (CountrySubdivision countrySubdivision : countrySubdivisions) {
		// countrySubdivision.removePhysicalAddress(); TODO implement it
		// in a listener in the Contacts module
 		countrySubdivision.delete();
 		countrySubDivisionDeletes++;
 	    }
 	    ArrayList<CountrySubdivisionLevelName> subdivisionLevelNames = new ArrayList<CountrySubdivisionLevelName>();
 	    subdivisionLevelNames.addAll(country.getLevelName());
 
 	    for (CountrySubdivisionLevelName countrySubdivisionLevelName : subdivisionLevelNames) {
 		countrySubdivisionLevelName.removeCountry();
 		countrySubdivisionLevelName.delete();
 		countrySubDivisionLevelNameDeletes++;
 	    }
 	    ArrayList<Integer> integers = new ArrayList<Integer>();
 	    integers.add(new Integer(countrySubDivisionDeletes));
 	    integers.add(new Integer(countrySubDivisionLevelNameDeletes));
 
 	    infoByCountry.put(country.getName().getContent(), integers);
 	}
 
 	for (String country : infoByCountry.keySet()) {
 	    logInfo("Cleaned the following registries for " + country + ":");
 	    ArrayList<Integer> integers = infoByCountry.get(country);
 	    logInfo("CountrySubDivision deletes: " + integers.get(0));
 	    logInfo("CountrySubDivisionLevelName deletes: " + integers.get(1));
 	}
 
     }
 
 }

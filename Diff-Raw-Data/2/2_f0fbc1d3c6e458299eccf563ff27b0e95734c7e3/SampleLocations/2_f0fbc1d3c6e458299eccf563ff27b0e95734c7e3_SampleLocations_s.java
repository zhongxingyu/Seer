 package onaboat.domain.model.location;
 
 import onaboat.domain.model.location.Location;
 import onaboat.domain.model.location.UnLocode;
 
 import org.apache.isis.applib.fixtures.AbstractFixture;
 
 /**
  * DOC: THIS CLASS HAS NO COMMENT!
  *
  * @author adamhoward
  */
 public class SampleLocations extends AbstractFixture {
 
 	@Override
 	public void install() {
 		getContainer().persistIfNotAlready(new Location(new UnLocode("CNHKG"), "Hongkong"));
 		getContainer().persistIfNotAlready(new Location(new UnLocode("AUMEL"), "Melbourne"));
 		getContainer().persistIfNotAlready(new Location(new UnLocode("SESTO"), "Stockholm"));
 		getContainer().persistIfNotAlready(new Location(new UnLocode("FIHEL"), "Helsinki"));
 		getContainer().persistIfNotAlready(new Location(new UnLocode("USCHI"), "Chicago"));
 		getContainer().persistIfNotAlready(new Location(new UnLocode("JNTKO"), "Tokyo"));
 		getContainer().persistIfNotAlready(new Location(new UnLocode("DEHAM"), "Hamburg"));
 		getContainer().persistIfNotAlready(new Location(new UnLocode("CNSHA"), "Shanghai"));
 		getContainer().persistIfNotAlready(new Location(new UnLocode("NLRTM"), "Rotterdam"));
		getContainer().persistIfNotAlready(new Location(new UnLocode("SEGOT"), "Gteborg"));
 		getContainer().persistIfNotAlready(new Location(new UnLocode("CNHGH"), "Hangzhou"));
 		getContainer().persistIfNotAlready(new Location(new UnLocode("USNYC"), "New York"));
 		getContainer().persistIfNotAlready(new Location(new UnLocode("USDAL"), "Dallas"));
 		getContainer().flush();
 	}
 
 }

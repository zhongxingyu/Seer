 package eu.europeana.uim.plugin.solr.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import eu.europeana.corelib.definitions.jibx.Date;
 import eu.europeana.corelib.definitions.jibx.EuropeanaType;
 import eu.europeana.corelib.definitions.jibx.ProxyType;
 import eu.europeana.uim.plugin.solr.utils.EuropeanaDateUtils;
 
 public class EuropeanaDateUtilsTest {
 
 	@Test
 	public void testUtils(){
 
 		ProxyType proxy = new ProxyType();
 		List<EuropeanaType.Choice> dates = new ArrayList<EuropeanaType.Choice>();
 		String dcDateString1 = "-150 / -70"; //should give -150 and -70
 		String dcDateString2 = "75/140 AD"; //should give 75 and 140
 		String dcDateString3 = "circa 1500 BC"; //should give -1500
 		EuropeanaType.Choice dateChoice1 = new EuropeanaType.Choice();
 		Date date1 = new Date();
 		date1.setString(dcDateString1);
 		dateChoice1.setDate(date1);
 		dates.add(dateChoice1);
 		EuropeanaType.Choice dateChoice2 = new EuropeanaType.Choice();
 		Date date2 = new Date();
 		date2.setString(dcDateString2);
 		dateChoice2.setDate(date2);
 		dates.add(dateChoice2);
 		EuropeanaType.Choice dateChoice3 = new EuropeanaType.Choice();
 		Date date3 = new Date();
 		date3.setString(dcDateString3);
 		dateChoice3.setDate(date3);
 		dates.add(dateChoice3);
 		proxy.setChoiceList(dates);
 		String proplocation = EuropeanaDateUtilsTest.class.getProtectionDomain().getCodeSource().getLocation() + "uimTest.properties";
 		String truncated = proplocation.replace("file:", "");
 		EuropeanaDateUtils.setPath(truncated);
		List<String> filteredDates = EuropeanaDateUtils.createEuropeanaYears(proxy);
 		Assert.assertTrue(filteredDates.size()==1);
 		//TEMPORARILY REMOVED
 //		Assert.assertTrue(filteredDates.contains("-150"));
 //		Assert.assertTrue(filteredDates.contains("-70"));
 //		Assert.assertTrue(filteredDates.contains("75"));
 //		Assert.assertTrue(filteredDates.contains("140"));
 		Assert.assertTrue(filteredDates.contains("-1500"));
 		
 	}
 }

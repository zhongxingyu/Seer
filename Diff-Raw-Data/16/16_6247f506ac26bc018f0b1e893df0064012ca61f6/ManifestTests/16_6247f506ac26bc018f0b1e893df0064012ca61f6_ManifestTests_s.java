 package au.edu.labshare.schedserver.scormpackager.lila.tests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import au.edu.labshare.schedserver.scormpackager.lila.Manifest;
 import au.edu.labshare.schedserver.scormpackager.manifest.Organization;
 
 public class ManifestTests 
 {
 	Manifest   manifest; 
 	File[]     testItems;
 	String[]   institutionNames;
 	String[]   titles;
 	
 	File       testItem;   //1st test item for applet.html
 	File       testItem2;  //2nd test item for applet2.html
 	File       testItem3;  //3rd test item for applet3.html
 	File       testResourceFile;
 	
 	/* These variables are pertaining to the file list of */
 	String compliantFilePath = "../../../../../../../../test/resources/lila/";
 	String nonCompliantPath = "../../../../../../../../test/resources/noncompliant/";
 	String expectedManifestFileName = "imsmanifest.xml";
 	String itemName = "applet.html";
 	String itemName2 = "applet2.html";
 	String itemName3 = "applet3.html";
 	String attachmentName = "lib/uts_tower.jpeg";
 	String assetName      = "lmsstub.js";
 	
 	@Before
 	public void Setup()
 	{
 		manifest = new Manifest();
 		
 		//Item as HTML files that are from the directory
 		testItem = new File(compliantFilePath + itemName);
 		testItem2 = new File(compliantFilePath + itemName2);
 		testItem3 = new File(compliantFilePath + itemName3);	
 		
 		//Other resources to be added to the system
 	}
 	
 	@Test
 	public void testGenerateOrganisations() 
 	{
 		Organization expectedOrganization = null;
 		testItems = new File[3];
 		titles    = new String[3];
 		institutionNames = new String[1];
 		
 		//Compare the actual vs. expected values.
 		Collection<Organization> actualValue;
 		ArrayList<Organization> expectedValue;
 		
 		
 		//Items to be added for testing
 		testItems[0] = testItem;
 		testItems[1] = testItem2;
 		testItems[2] = testItem3;
 		
 		titles[0] = "Ideal Gas Experiment";
 		titles[1] = "Another Experiment";
 		titles[2] = "";
 		
 		institutionNames[0] = "UTS";
 		
 		//Setup the expected values
 		expectedValue = new ArrayList<Organization>();
 		expectedOrganization = new Organization();
 		expectedOrganization.setID("UTS");
		expectedOrganization.setItem("item1", "applet");
 		expectedValue.add(expectedOrganization);
		expectedOrganization.setItem("item2", "applet2");
 		expectedValue.add(expectedOrganization);
		expectedOrganization.setItem("item3", "applet3");
 		expectedValue.add(expectedOrganization);
 
 		actualValue = manifest.generateOrganisations(institutionNames, null, testItems); 
		
		assertSame(expectedValue, actualValue);
 	}
 	
 	@Test 
 	public void testGenerateResources()
 	{
 		fail("Not implemented yet");
 	}
 	
 	@Test
 	public void testGenerateSubManifests()
 	{
 		fail("Not implemented yet");
 	}
 }

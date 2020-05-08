 package org.cloudifysource.quality.iTests.test.rest;
 
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.LogUtils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import junit.framework.Assert;
 
import org.cloudifysource.dsl.utils.IPUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.type.TypeFactory;
 import org.codehaus.jackson.type.JavaType;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitDeployment;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.pu.service.ServiceDetails;
 
 public class RestConsistencyTestUtil {
 	
 	private static final int RECURSIVE_ITERATIONS = 1;
 	protected static final int PROCESSINGUNIT_TIMEOUT_SEC = 20;
 	protected static final String RESTFUL_WAR_PATH = SGTestHelper.getBuildDir()+ 
 			MessageFormat.format("{0}tools{0}rest{0}rest.war", File.separatorChar);
 	private static Admin admin;
 	private static String restPrefix;
 	
 	
 	static public void runConsistencyTest(Admin admin , String restPrefix) throws IOException {
 		setAdmin(admin);
 		setRestPrefix(restPrefix);
 		String htmlPage = getHtmlFromURL(restPrefix);
 		List<String> urls = getUrlsFromHTML(htmlPage);
 		recurseThroughLinks(urls, RECURSIVE_ITERATIONS);
 	}
 
 	static private void recurseThroughLinks(List<String> urls, int rounds) throws IOException {
 		try{
 			if (rounds > 0){
 				for (String url : urls){
 					String[] urlTokens = url.split("/");
 					StringBuffer currendEnvElement = new StringBuffer(urlTokens[urlTokens.length - 1]);
 					// In case the first letter of currendAdminElement is lower case (happens in Zones)
 					char c = currendEnvElement.charAt(0);
 					c = Character.toUpperCase(c);
 					currendEnvElement.setCharAt(0, c);
 					
 					String htmlpage = getHtmlFromURL(url);
 					verifyConsistency(jsonToMap(htmlpage) , currendEnvElement.toString());
 					List<String> links = getUrlsFromHTML(htmlpage);
 					recurseThroughLinks(links, rounds - 1);
 				}
 			}
 		}catch(Exception e){
 			LogUtils.log("FAILED " + e.getMessage());
 			Assert.fail();
 		}
 	}
 	
 	/**
 	 * This method verifies the correctness of the html response from the REST server against the admin in two aspects:<p>
 	 * 1. The number of elements (e.g machine) in each wrapping element(e.g Machines)<p>
 	 * 2. The identifiers (Names or Uids) in each wrapping element.<p>
 	 * This method uses reflection to avoid hard coding.
 	 * 
 	 * @param jsonMap - The html response from REST as a Map<String , Object>.
 	 * @param currendEnvElement - One of the enviroment's elements: Machines , PUs , GSCs etc.
 	 * @throws Exception - One of the many exception sub-types that the reflection might throw. should never happen as 
 	 * 					   the methods asked should be there and when invoked get the right arguments.
 	 */
 	private static void verifyConsistency(Map<String, Object> jsonMap , String currendEnvElement) throws Exception {
 		final String UIDS_DELIMITER = "/Uids/" , GSM = "GridServiceManagers" , GSC = "GridServiceContainers" , ESM = "ElasticServiceManagers";
 		final String NAMES_DELIMITER = "/Names/" , CONTAINERS = "Containers" , MANAGERS = "Managers";
 		final String JSON_MAP_KEY_FOR_UID = new String("Uids-Elements");
 		final String JSON_MAP_KEY_FOR_NAME = new String("Names-Elements");
 		List<String> elementsWithInstances = Arrays.asList("ProcessingUnits" , "Spaces");
 		Set<String> identifiersKeySet = null;
 		int sizeInAdmin = -1;
 		Method getEnvElem_Method , getIdentifiers_Method , getInstances_Method;
 		Object envElem = null;
 		
 		// usually the propertyRetrivalString is the same as currendEnvElement
 		String propertyRetrivalString = currendEnvElement;
 		// except in these cases
 		if (currendEnvElement.compareTo(GSC) == 0)
 			propertyRetrivalString = CONTAINERS;
 		if ((currendEnvElement.compareTo(GSM) == 0) || (currendEnvElement.compareTo(ESM) == 0))
 			propertyRetrivalString = MANAGERS;
 		
 	// first lets harvest the size and identifiers info from the admin
 		
 		// use admin to retrieve the actual element
 		getEnvElem_Method = admin.getClass().getMethod("get" + currendEnvElement, (Class<?>[])null);
 		envElem = getEnvElem_Method.invoke(admin, (Object[])null);
 		// each element has either a name or uid identifier (not both)
 		// use jsonMap to determine what identifier the current element is using and return that identifier's getter
 		getIdentifiers_Method = jsonMap.containsKey(JSON_MAP_KEY_FOR_UID) ? 
 				envElem.getClass().getMethod("getUids", (Class<?>[])null) : 
 				envElem.getClass().getMethod("getNames", (Class<?>[])null);
 		// The identifiers of an element are stored as a Map<String , Element>. We want the keySet of this map
 		// as the key's are the actual identifiers
 		Object identifiers = getIdentifiers_Method.invoke(envElem, (Object[])null);
 		Method getIdentifierKeySet_Method = identifiers.getClass().getMethod("keySet", (Class<?>[])null);
 		getIdentifierKeySet_Method.setAccessible(true);
 		identifiersKeySet = (Set<String>)getIdentifierKeySet_Method.invoke(identifiers, (Object[])null);
 		
 		// getting the elements the element holds as an array and retrieve the size directly from the array
 		Method getElemFromEnvElem_Method = envElem.getClass().getMethod("get" + propertyRetrivalString, (Class<?>[])null);
 		Object[] arrayOfEnvElements = (Object[])getElemFromEnvElem_Method.invoke(envElem, (Object[])null);
 		sizeInAdmin = arrayOfEnvElements.length;
 		
 		// count the total number of instances for this element and compare to admin
 		if(elementsWithInstances.contains(currendEnvElement)){
 			int totInstanceNumInAdmin = 0;
 			for(Object obj : arrayOfEnvElements)
 				totInstanceNumInAdmin += (Integer)obj.getClass().getMethod("getNumberOfInstances", (Class<?>[])null).invoke(obj, (Object[])null);
 			
 			StringBuffer tempBuffer = new StringBuffer(currendEnvElement);
 			// remove the 's' of the plural form of this element(s) (e.g Spaces -> Space)
 			tempBuffer.deleteCharAt(tempBuffer.length()-1);
 			Assert.assertEquals("NO MATCH: " + currendEnvElement +"Instances-Size" , totInstanceNumInAdmin , jsonMap.get(new String(tempBuffer.toString() +"Instances-Size")));		
 		}
 	// Now it's time to harvest the size and identifiers info from the jsonMap
 		// assert the size is consistent
 		Assert.assertEquals("NO MATCH: " + currendEnvElement +"-Size" , sizeInAdmin , jsonMap.get(new String(propertyRetrivalString + "-Size")));		
 		// retrieving the identifiers from jsonMap
 		String identifierType = jsonMap.containsKey(JSON_MAP_KEY_FOR_NAME) ? JSON_MAP_KEY_FOR_NAME : JSON_MAP_KEY_FOR_UID;
 		String identifiersInJson = jsonMap.get(identifierType).toString();
 		String delimiter = jsonMap.containsKey(JSON_MAP_KEY_FOR_NAME) ? NAMES_DELIMITER : UIDS_DELIMITER;
 		// verify the identifiers are consistent
 		verifyIdentifierConsistency(identifiersKeySet, identifiersInJson, delimiter, currendEnvElement);
 		// retrieve the total instances from Json
 	}
 
 	// An identifier is either a name or a Uid. The delimiter argument is used to differentiate between the two
 	private static void verifyIdentifierConsistency(Set<String> identifiers , String identifiersInJson , String delimiter , String currendEnvElement) {
 		int PUindex = 0;
 		String[] elements = identifiersInJson.split(",");
 		
 		for(String identifier : identifiers){
 			String identifierFromJson = elements[PUindex].split(delimiter)[1];
 			identifierFromJson = identifierFromJson.endsWith("]") ? identifierFromJson.split("]")[0] : identifierFromJson;
 			Assert.assertEquals("NO CONSISTENCY: in " + currendEnvElement, identifier, identifierFromJson);
 			PUindex++;
 		}
 	}
 
 	private static List<String> getUrlsFromHTML(String htmlPage) {
 		String link;
 		List<String> links = new ArrayList<String>();
 		String regex = "\\b/admin/[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
 		Pattern pattern = Pattern.compile(regex);
 		Matcher matcher = pattern.matcher(htmlPage);
 		while (matcher.find()){
 			link = matcher.group();
 			links.add(restPrefix + link);
 		}
 		return links;
 	}
 
 	private static String getHtmlFromURL(String url) throws IOException{
 		URL urlObject = new URL(url);
 		BufferedReader in = new BufferedReader(new InputStreamReader(urlObject.openStream()));
 		StringBuilder sb = new StringBuilder();
 		String inputLine;
 		while ((inputLine = in.readLine()) != null){
 			sb.append(inputLine);
 		}
 		return sb.toString();
 	}
 	
 	public static Map<String, Object> jsonToMap(final String response) throws IOException {
 		final JavaType javaType = TypeFactory.type(Map.class);
 		ObjectMapper om = new ObjectMapper();
 		return om.readValue(response, javaType);
 	}
 	
 	// private constructor to prevent instantiation
 	private RestConsistencyTestUtil(){}
 			
 	private static void setAdmin(Admin admin) {
 		RestConsistencyTestUtil.admin = admin;
 	}
 	private static void setRestPrefix(String restPrefix) {
 		RestConsistencyTestUtil.restPrefix = restPrefix;
 	}
 
 	public static String deployRestServer(Admin admin){
 		GridServiceManager gsm = admin.getGridServiceManagers().waitForAtLeastOne();
 		ProcessingUnit pu = gsm.deploy(new ProcessingUnitDeployment(RESTFUL_WAR_PATH));
 		boolean deploymentResult = admin.getProcessingUnits().waitFor("rest",PROCESSINGUNIT_TIMEOUT_SEC, TimeUnit.SECONDS).waitFor(1, PROCESSINGUNIT_TIMEOUT_SEC, TimeUnit.SECONDS);
 
 		if (!deploymentResult){
 			Assert.assertTrue("in deployRestServer: failed to deploy rest server" , false);
 		}
 		ProcessingUnitInstance pui = pu.getInstances()[0];
 		ServiceDetails restServiceDetails = pui.getServiceDetailsByServiceId("jee-container");
 
 		return "http://" + IPUtils.getSafeIpAddress(restServiceDetails.getAttributes().get("host").toString()) 
 				+ ":" + restServiceDetails.getAttributes().get("port")
 				+ restServiceDetails.getAttributes().get("context-path");
 	}
 }
 
 

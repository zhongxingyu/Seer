 package org.openmrs.module.pdmp_query.web.controller;
 
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.Patient;
 import org.openmrs.Person;
 import org.openmrs.util.OpenmrsUtil;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import org.openmrs.module.pdmp_query.Config;
 import org.openmrs.module.pdmp_query.ConfigService;
 
 
 /**
  * The main controller.
  */
 @Controller
 public class PDMPPageController {
 
 	protected final Log log = LogFactory.getLog(getClass());
 
 	@RequestMapping(value = "/module/pdmp_query/pdmp", method = RequestMethod.GET)
 	public void manage(ModelMap model, @RequestParam("patientId") Integer patientId) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
             XPathFactory xPathfactory = XPathFactory.newInstance();
             Config c = Context.getService(ConfigService.class).getConfig();
             String userpassword = c.getUser() + ":" + c.getPassword();
             String baseURL = c.getUrl();
		String sNoRecordsFound = "No PDMP Records Found";
 		String sUrl = null;
		String sResponse = null;
 		String sType = null;
 		Patient patient = Context.getPatientService().getPatient(patientId);
 		Person person = Context.getPersonService().getPerson(patient);
 
 		String sGivenName = "";
 		String sFamilyName = "";
 		String sGender = "";
 		String sBirthdate = "";
 		String sAddress = "";
 		DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
 
 		sGivenName = person.getGivenName();
 		sFamilyName = person.getFamilyName();
 		if (person.getGender().equalsIgnoreCase("F")) {
 			sGender = "female";
 		} else if (person.getGender().equalsIgnoreCase("M")) {
 			sGender = "male";
 		}
 		sBirthdate = dateFormat.format(person.getBirthdate());
 		sAddress = person.getPersonAddress().getCityVillage();
 
 		Document doc = PDMPGet(baseURL + "search?given=" + sGivenName +
 				"&family=" + sFamilyName + "&gender=" + sGender +
 				"&loc=" + sAddress + "&dob=" + sBirthdate, userpassword, "Accept", "application/atom+xml");
                 {
 			XPath xpath = xPathfactory.newXPath();
 			XPathExpression expr = xpath.compile("/feed/entry/link[@type='application/atom+xml']");
 			Element hPeople = (Element) expr.evaluate(doc, XPathConstants.NODE);
			sUrl = baseURL + hPeople.getAttribute("href");
			sType = hPeople.getAttribute("type");
                 }
 
		if (sType == null)
		{
			sResponse = "No PDMP Records found.";
		}
		else
		{
 			doc = PDMPGet(sUrl, userpassword, "Accept", sType);
 			{
 				XPath xpath = xPathfactory.newXPath();
 				XPathExpression expr = xpath.compile("/feed/entry/link[@type='application/atom+xml']");
 				Element hPeopleSRPP = (Element) expr.evaluate(doc, XPathConstants.NODE);
 				sUrl = baseURL + hPeopleSRPP.getAttribute("href");
 				sType = hPeopleSRPP.getAttribute("type");
 			}
 
 			doc = PDMPGet(sUrl, userpassword, "Accept", sType);
 			{
 				XPath xpath = xPathfactory.newXPath();
 				XPathExpression expr = xpath.compile("/feed/entry/link[@rel='report']");
 				Element hPeopleSRPPReport = (Element) expr.evaluate(doc, XPathConstants.NODE);
 				sUrl = baseURL + hPeopleSRPPReport.getAttribute("href");
 				sType = hPeopleSRPPReport.getAttribute("type");
 			}
 
 			doc = PDMPGet(sUrl, userpassword, "Accept", sType);
 			{
 				XPath xpath = xPathfactory.newXPath();
 				XPathExpression expr = xpath.compile("/record/medicationOrder");
 				NodeList nLPeopleMedication = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
 				if (nLPeopleMedication.getLength() > 0)
 				{
 					sResponse = "";
 					for (int countMeds = 0; countMeds < nLPeopleMedication.getLength(); countMeds++)
 					{
 						Node nMed = nLPeopleMedication.item(countMeds);
 
 						// Prescriber Information
 						expr = xpath.compile("orderInformation/prescriber/name");
 						Element ePrescriberName = (Element) expr.evaluate(nMed, XPathConstants.NODE);
 						NodeList nLPrescriberAttrs = ePrescriberName.getChildNodes();
 						sResponse = sResponse + "<p><b>Prescriber:</b> ";
 						for (int countPrescriberAttrs = 0; countPrescriberAttrs < nLPrescriberAttrs.getLength(); countPrescriberAttrs++)
 						{
 							sResponse = sResponse + nLPrescriberAttrs.item(countPrescriberAttrs).getTextContent();
 						}
 						sResponse = sResponse + "</p>\n";
 
 						// Drug Information:
 						expr = xpath.compile("medicationInformation/code");
 						Element eMedicationCode = (Element) expr.evaluate(nMed, XPathConstants.NODE);
 						String sDisplayName = eMedicationCode.getAttribute("displayName");
 						sResponse = sResponse + "<p><b>Drug:</b> " + sDisplayName;
 						sResponse = sResponse + "</p>\n";
 
 						// Order Information
 						expr = xpath.compile("orderInformation/orderedDateTime");
 						Node nOrderedDateTime = (Node) expr.evaluate(nMed, XPathConstants.NODE);
 						sResponse = sResponse + "<p><b>When written: </b>" + nOrderedDateTime.getTextContent() + "</p>\n";
 
 						// Fullfillment Information
 						// Prescription Number
 						expr = xpath.compile("fulfillmentHistory/prescriptionNumber");
 						Node nPrescriptionNumber = (Node) expr.evaluate(nMed, XPathConstants.NODE);
 						sResponse = sResponse + "<table border='1'><tr><th>Rx Number</th><th>When Filled</th><th>Pharmacy</th><th>Pharmacist</th><th>Quantity</th><th>Status</th></tr>";
 						sResponse = sResponse + "<tr><td>" + nPrescriptionNumber.getTextContent() + "</td>\n";
 
 						// When Filled
 						expr = xpath.compile("fulfillmentHistory/dispenseDate");
 						Node nWhenFilled = (Node) expr.evaluate(nMed, XPathConstants.NODE);
 						sResponse = sResponse + "<td>" + nWhenFilled.getTextContent() + "</td>\n";
 
 						// Pharmacy
 						expr = xpath.compile("fulfillmentHistory/pharmacy/name");
 						Node nPharmacyName = (Node) expr.evaluate(nMed, XPathConstants.NODE);
 						sResponse = sResponse + "<td>" + nPharmacyName.getTextContent() + "</td>\n";
 
 						// Pharmacist  Name
 						expr = xpath.compile("fulfillmentHistory/pharmacist/name/givenName");
 						Node nPharmacistGivenName = (Node) expr.evaluate(nMed, XPathConstants.NODE);
 
 						expr = xpath.compile("fulfillmentHistory/pharmacist/name/familyName");
 						Node nPharmacistFamilyName = (Node) expr.evaluate(nMed, XPathConstants.NODE);
 
 						sResponse = sResponse + "<td>" + nPharmacistGivenName.getTextContent() + " " + nPharmacistGivenName.getTextContent() + "</td>\n";
 
 						// Quantity
 						expr = xpath.compile("fulfillmentHistory/quantityDispensed");
 						Element eQuantity = (Element) expr.evaluate(nMed, XPathConstants.NODE);
 						String sQuantity = eQuantity.getAttribute("amount");
 						sResponse = sResponse + "<td>" + sQuantity + "</td>\n";
 
 						// Status
 						expr = xpath.compile("fulfillmentHistory");
 						Element eStatus = (Element) expr.evaluate(nMed, XPathConstants.NODE);
 						String sStatus = eStatus.getAttribute("fillStatus");
 						sResponse = sResponse + "<td>" + sStatus + "</td></tr></table>\n<HR/>";
 
 					}
 				}
 			}
 		}
 
 		model.addAttribute("subsection", sResponse);
 
 	}
 
     private Document PDMPGet(String sURL, String userpassword, String hProp, String hPropVal) throws IOException, ParserConfigurationException, SAXException
 	{
 		HttpURLConnection connection = null;
 		URL serverAddress = null;
 
 		try {
 			serverAddress = new URL(sURL);
 
 			//Set up the initial connection
 			connection = (HttpURLConnection)serverAddress.openConnection();
 			connection.setRequestMethod("GET");
 
 			byte[] authEncBytes = Base64.encodeBase64(userpassword.getBytes());
 			String encodedAuthorization = new String(authEncBytes);
 			connection.setRequestProperty("Authorization", "Basic "+
 					encodedAuthorization);
 			if (hProp!=null) {
 				connection.addRequestProperty(hProp, hPropVal);
 			}
 			connection.setDoOutput(true);
 			connection.setReadTimeout(10000);
 			connection.connect();
 
                         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                         DocumentBuilder builder = factory.newDocumentBuilder();
                         return builder.parse(connection.getInputStream());
 		}
 		finally	{
 			connection.disconnect();
 		}
 	}
 }

 package it.freddyfire.fertyfire.datamanager;
 
 import it.freddyfire.fertyfire.datamodel.FertyCheck;
 import it.freddyfire.fertyfire.persistencemanager.PersistenceDBException;
 import it.freddyfire.fertyfire.persistencemanager.PersistenceManager;
 import it.freddyfire.fertyfire.persistencemanager.PersistenceManager.FertyFireCollection;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Node;
 
 public class DataManager {
 
 	private static final Logger logger = LoggerFactory.getLogger(DataManager.class);
 	
 	private static DataManager instance;
 	
 	private DateFormat reportDateFormatter;
 
 	private DataManager() {
 		reportDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
 	}
 	
 	public static DataManager getInstance() {
 		if (instance == null)
 			instance = new DataManager();
 		return instance;
 	}
 	
 	public Map<Calendar, FertyCheck> retrieveMonthReports(Calendar cal) {
 		
		Map<Calendar, FertyCheck> fertyChecks = new HashMap<Calendar, FertyCheck>();
 		
 		cal.set(Calendar.DAY_OF_MONTH, 1);
 		
 		int month = cal.get(Calendar.MONTH);
 		
 		while (cal.get(Calendar.MONTH) == month) {
 			String resID = reportDateFormatter.format(cal.getTime());
 			
 			try {
 				Node node = PersistenceManager.getInstance().readXMLResource(FertyFireCollection.REPORT_COLLECTION, resID);
 
 				FertyCheck fc = DataModelTool.getUnmarshaller(FertyCheck.class).unmarshal(node, FertyCheck.class).getValue();
 				fertyChecks.put((Calendar) cal.clone(), fc);
 			} catch (PersistenceDBException e) {
 				logger.debug("No report found for day " + resID + "...");
 				fertyChecks.put((Calendar) cal.clone(), null);
 			} catch (JAXBException e) {
 				e.printStackTrace();
 			}
 			
 			cal.roll(Calendar.DAY_OF_YEAR, true);
 		}
 		
 		return fertyChecks;
 	}
 	
 	public void report(FertyCheck fc) throws PersistenceDBException {
 		
 		try {
 			XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
 			fc.setDate(cal);
 		} catch (DatatypeConfigurationException e) {
 			e.printStackTrace();
 		}
 		
 		String resID = reportDateFormatter.format(new Date());
 		
 		try {
 			Node node = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 			
 			DataModelTool.getMarshaller(FertyCheck.class).marshal(fc, node);
 
 			PersistenceManager.getInstance().storeXMLResource(FertyFireCollection.REPORT_COLLECTION, resID, node);
 		} catch (ParserConfigurationException e) { 
 			e.printStackTrace();
 		} catch (JAXBException e) {
 			e.printStackTrace();
 		}
 	}
 }

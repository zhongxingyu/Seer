 package net.prep.solr.sensorml.impl;
 
 import static org.junit.Assert.fail;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.List;
 
 import net.opengis.sensorML.x101.AbstractProcessType;
 import net.opengis.sensorML.x101.KeywordsDocument.Keywords;
 import net.opengis.sensorML.x101.SensorMLDocument;
 import net.opengis.sensorML.x101.SensorMLDocument.SensorML;
 
 import org.apache.solr.common.SolrDocument;
 import org.apache.xmlbeans.XmlException;
 import org.junit.Test;
 
 public class SensorMLIndexImplTest {
 	/*
 	 * A simple test to test if it index without errors
 	 */
 	@Test
 	public void setup() {
 		try {
 			SensorMLDocument s = SensorMLDocument.Factory.parse(ClassLoader
 					.getSystemResourceAsStream("testsensor.xml"));
 			SensorML sensor = s.getSensorML();
 			/*
 			 * Just delete all the old indexed - for testing only
 			 */
 			SensorMLIndexImpl impl = new SensorMLIndexImpl();
 			impl.deleteAll();
 			System.out.println("Deleted successfully");
 
 			impl.index(sensor);
 
 			System.out.println("Successfully indexed");
 
 			try {
 				SensorMLSearchImpl searchImpl = new SensorMLSearchImpl();
 				
 				/*
 				 * Prepare the query
 				 */
 				AbstractProcessType process = sensor.getMemberArray()[0]
 						.getProcess();
 
 				/*
 				 * Compare fields
 				 */
 				String id = (process.getIdentificationArray()[0]
 						.getIdentifierList().getIdentifierArray()[0].getTerm()
 						.getValue());
 				String beginPosition = (process.getValidTime().getTimePeriod()
 						.getBeginPosition().getStringValue());
 				String endPosition = process.getValidTime().getTimePeriod()
 						.getEndPosition().getStringValue();
 				
 				StringBuilder sb = new StringBuilder();
				
 				sb.append("id:");
 				sb.append('"');
 				sb.append(id);
 				sb.append('"');
				
				
				
				
 				List<Object> list = searchImpl.getForQuery(sb.toString());
				if(list.size() < 1)
					fail("Document not found");
 				SolrDocument doc = (SolrDocument) list.get(0);
 				
 				
 				
 				if(!id.equals(doc.get("id")))
 					fail("id doesn't match");
 				if(!beginPosition.equals(doc.get("beginPosition")))
 					fail("BeginPositiontimes doesn't match");
 				if(!endPosition.equals(doc.get("endPosition")))
 					fail("endPositionTime doesn't match");
 				
 				System.out.println("Fields matched successfully");
 				
 			} catch (Exception e) {
 				fail("Couldn't retrieve data:" + e.toString());
 			}
 
 		} catch (XmlException e) {
 			// TODO Auto-generated catch block
 			fail(e.toString());
 			// e.printStackTrace();
 		} catch (IOException e) {
 			fail(e.toString());
 			// TODO Auto-generated catch block
 			// e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 
 			e.printStackTrace();
 			fail(e.toString());
 		}
 	}
 }

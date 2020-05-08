 package ru.xrm.app.config;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class ConfigHelperTest {
 
 	Document doc_namedQueries;
 	Document doc_entries;
 
 	String xmlSourceCssNamedQueries=""+
 			"<?xml version='1.0' encoding='UTF-8' ?>" +
 			"<properties>"+
 			"	<cssNamedQueries>" +
 			"		<cssNamedQuery name='query0'>"+
 			"			<cssQuery>strong</cssQuery>" +
 			"			<cssArgsCount>0</cssArgsCount>" +
 			"		</cssNamedQuery>" +
 			"		<cssNamedQuery name='query1'>"+
 			"			<cssQuery>strong:contains(%s)</cssQuery>" +
 			"			<cssArgsCount>1</cssArgsCount>" +
 			"		</cssNamedQuery>" +
 			"	</cssNamedQueries>" +
 			"</properties>";
 
 	String xmlSourceProperties=""+
 			"<?xml version='1.0' encoding='UTF-8' ?>" +
 			"<properties>"+
 			"	<vacancySectionProperties>" +
 			"		<entry>"+
 			"			<key>name</key>"+
 			"			<cssQuery>div[id~=secion_\\d] a[href~=section=\\d]</cssQuery>"+
			"			<elementEvaluator>ru.xrm.app.evaluators.VacancySectionNameElementEvaluator</elementEvaluator>"+
 			"		</entry>"+
 			"		<entry>" +
 			"			<key>date</key>" +
 			"			<cssQuery namedQuery='query1'/>"+
 			"			<cssQueryArgs>"+
 			"				<arg>Date</arg>"+
 			"			</cssQueryArgs>"+
			"			<elementWalker>ru.xrm.app.walkers.JobPropertyElementWalker</elementWalker>"+
			"			<elementEvaluator>ru.xrm.app.evaluators.JobPropertyElementEvaluator</elementEvaluator>"+
 			"			<propertyTransformer>ru.xrm.app.transformers.JobDatePropertyTransformer</propertyTransformer>"+
 			"		</entry>" +
 			"	</vacancySectionProperties>"+
 			"</properties>";
 			
 	@Before
 	public void setUp() throws Exception {
 		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder;
 		InputStream xmlStream;
 
 		builder = factory.newDocumentBuilder();
 		xmlStream=new ByteArrayInputStream(xmlSourceCssNamedQueries.getBytes());
 		doc_namedQueries=builder.parse(xmlStream);
 		xmlStream=new ByteArrayInputStream(xmlSourceProperties.getBytes());
 		doc_entries=builder.parse(xmlStream);
 	}
 
 	@Test
 	public void testLoadEntry() {
 		Map<String,NamedCSSQuery> namedQueries=new HashMap<String,NamedCSSQuery>();
 		Entry e;
 		NodeList nodes=doc_entries.getChildNodes();
 		Node firstEntry=null;
 		Node secondEntry=null;
 		int entryCounter=0;
 		for (int i=0;i<nodes.getLength();i++){
 			if (nodes.item(i).getNodeType()==Node.ELEMENT_NODE){
 				NodeList innerNodes=nodes.item(i).getChildNodes();
 				for (int j=0;j<innerNodes.getLength();j++){
 					if (innerNodes.item(j).getNodeType()==Node.ELEMENT_NODE){
 						NodeList zeroNodes=innerNodes.item(j).getChildNodes();
 						for (int k=0;k<zeroNodes.getLength();k++){
 							if (zeroNodes.item(k).getNodeType()==Node.ELEMENT_NODE){
 								if (firstEntry==null){
 									firstEntry=zeroNodes.item(k);
 								}else{
 									secondEntry=zeroNodes.item(k);
 								}
 								entryCounter++;
 								if (entryCounter==2)break;
 							}
 						}
 					}
 				}
 			}
 		}
 		try {
 			// first entry
 			e=ConfigHelper.loadEntry(firstEntry, namedQueries);
 			assertEquals("name", e.getKey());
 			assertEquals("div[id~=secion_\\d] a[href~=section=\\d]", e.getCssQuery());
			assertEquals("ru.xrm.app.evaluators.VacancySectionNameElementEvaluator", e.getElementEvaluator().getClass().getName());
 			assertNull(e.getElementWalker());
 			assertNull(e.getPropertyTransformer());
 			assertEquals(0,e.cssQueryArgs.size());
 			
 			namedQueries.put("query1", new NamedCSSQuery("query1", "Date=%s", 1));
 			// second entry
 			e=ConfigHelper.loadEntry(secondEntry, namedQueries);
 			assertEquals("date", e.getKey());
 			assertEquals("Date=Date", e.getCssQuery());
			assertEquals("ru.xrm.app.walkers.JobPropertyElementWalker", e.getElementWalker().getClass().getName());
			assertEquals("ru.xrm.app.evaluators.JobPropertyElementEvaluator", e.getElementEvaluator().getClass().getName());
 			assertEquals("ru.xrm.app.transformers.JobDatePropertyTransformer", e.getPropertyTransformer().getClass().getName());
 			assertEquals(1, e.cssQueryArgs.size());
 
 			
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 	}
 
 }

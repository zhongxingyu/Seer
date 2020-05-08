 package org.acme.embedded;
 
 import static org.dynamicvalues.Dynamic.*;
 import static org.sdmx.CodelistBuilder.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.acme.utils.TestUtils;
 import org.junit.Assert;
 import org.junit.Test;
 import org.sdmx.SdmxServiceFactory;
 import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
 import org.virtualrepository.Asset;
 import org.virtualrepository.csv.CsvCodelist;
 import org.virtualrepository.service.io.Binder;
 import org.virtualrepository.tabular.Table;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.StaxDriver;
 
 public class BinderTest {
 
 	Binder binder = new Binder(SdmxServiceFactory.writer());
 	
 	@Test
 	public void assetsCanBeBoundToJson() throws Exception {
 		
 		Asset a = 	new CsvCodelist("id1","standardName",0);
 		
 		binder.jsonMoM(valueOf(a));
 		
 	}
 	
 	@Test
 	public void assetsCanBeBoundToXml() throws Exception {
 		
 		Asset a1 = 	new CsvCodelist("id1","name1",0);
 		Asset a2 = 	new CsvCodelist("id2","name2",0);
 		
 		List<Asset> assets = new ArrayList<Asset>();
 		
 		assets.add(a1);
 		assets.add(a2);
 		
 		binder.xmlMoM(valueOf(assets));
 		
 	}
 	
 	
 	@Test
 	public void assetsCanBeBoundToVXml() throws Exception {
 		
 		Asset a1 = 	new CsvCodelist("id1","name1",0);
 		Asset a2 = 	new CsvCodelist("id2","name2",0);
 		
 		List<Asset> assets = new ArrayList<Asset>();
 		
 		assets.add(a1);
 		assets.add(a2);
 		
 		String bound = binder.vxml(assets);
 		
 		List<?> read = (List<?>) new XStream(new StaxDriver()).fromXML(bound);
 		
 		//System.out.println(read);
 		
 		Assert.assertEquals(assets.get(0),read.get(0));
 	}
 	
 	
 	@Test
 	public void sdmxBeansCanBeBoundToSdmxML() throws Exception {
 		
 		CodelistBean bean = list().add(code("id").name("name","en")).end();
 	
 		String bound = binder.sdmxMl(bean);
 		
 		System.out.println(bound);
 		
 		
 	}
 	
 	
 	@Test
 	public void tableCanBeBoundToJson() throws Exception {
 		
 		Table table = TestUtils.twoBytwoTable();
 		
 		//no errors
 		String bound = binder.jTable(table);
 		
 		System.out.println(bound);
 		
 	}
 	
 	@Test
 	public void tableCanBeBoundToVXML() throws Exception {
 		
 		Table table = TestUtils.twoBytwoTable();
 		
 		//no errors
 		String serialised = binder.vTable(table);
 		
 		Table bound = (Table) new XStream(new StaxDriver()).fromXML(serialised);
 		
 		System.out.println(bound);
 	}
 	
 	
 }

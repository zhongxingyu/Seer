 package org.cotrix;
 
 import static java.util.Arrays.*;
 import static org.cotrix.io.tabular.ColumnDirectives.*;
 import static org.junit.Assert.*;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.xml.namespace.QName;
 
 import org.cotrix.io.map.MapService;
 import org.cotrix.io.map.Outcome;
 import org.cotrix.io.sdmx.SdmxMapDirectives;
 import org.cotrix.io.sdmx.SdmxParseDirectives;
 import org.cotrix.io.sdmx.SdmxParseTask;
 import org.cotrix.io.tabular.TableMapDirectives;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
 import org.virtualrepository.tabular.Column;
 import org.virtualrepository.tabular.DefaultTable;
 import org.virtualrepository.tabular.Row;
 import org.virtualrepository.tabular.Table;
 
 import com.googlecode.jeeunit.JeeunitRunner;
 
 
 @RunWith(JeeunitRunner.class)
 public class MapServiceTest {
 
 	@Inject
 	MapService service;
 	
 	@Inject
 	SdmxParseTask parser;
 	
 	@Test
 	public void servicesAreInjected() throws Exception {
 			
 		assertNotNull(service);
 		
 		assertNotNull(parser);
 		
 	}
 	
 	@Test
 	public void mapTable() {
 		
 		//we only need to test dispatching, mapping is provided and tested in VR
 		
 		String[][] data = { { "11", "12" }, { "21", "22" } };
 
 		Table table = tableWith(data,"c1","c2");
 
 		TableMapDirectives directives = new TableMapDirectives(table.columns().get(0));
 		
 		directives.add(column("TAXOCODE"))
 		  .add(column("ISSCAAP"))
 		  .add(column("Scientific_name"))
 		  .add(column("English_name").language("en"))
 		  .add(column("French_name").language("fr"))
 		  .add(column("Spanish_name").language("es"))
 		  .add(column("Author"))
 		  .add(column("Family"))
 		  .add(column("Order"));
 
 		Outcome outcome = service.map(table, directives);
 		
 		assertNotNull(outcome.result());
 	}
 	
 	
 	@Test
 	public void mapSdmxBean() throws Exception {
 		
 		//we only need to test dispatching, mapping is tested separately
 		
 		InputStream stream = getClass().getClassLoader().getResourceAsStream("sampleasfissdmx.xml");
 		
 		CodelistBean bean = parser.parse(stream, SdmxParseDirectives.DEFAULT);
 		
 		Outcome outcome = service.map(bean, new SdmxMapDirectives().name("test"));
 		
 		assertNotNull(outcome.result());
 		assertEquals(new QName("test"), outcome.result().name());
 		
 	}
 	
 	// helper
 	private Table tableWith(String[][] data, String ... cols) {
 		
 		Column[] columns = columns(cols);
 		List<Row> rows = new ArrayList<Row>();
 		for (String[] row : data) {
 			Map<QName,String> map = new HashMap<QName, String>(); 
 			for (int i=0;i<row.length;i++)
 					map.put(columns[i].name(),row[i]);
 			rows.add(new Row(map));	
 		}
 		
 		return new DefaultTable(asList(columns),rows);
 	}
 	
 	Column[] columns(String ...names) {
 		List<Column> list = new ArrayList<Column>();
 		for (String name : names)
 			list.add(new Column(name));
 		return list.toArray(new Column[0]);
 	}
 
 }

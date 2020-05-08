 package org.cotrix;
 
 import static java.util.Arrays.*;
 import static junit.framework.Assert.*;
 import static org.cotrix.domain.dsl.Codes.*;
 import static org.cotrix.io.tabular.ColumnDirectives.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import org.cotrix.domain.Attribute;
 import org.cotrix.domain.Codelist;
 import org.cotrix.io.tabular.Table2Codelist;
 import org.cotrix.io.tabular.TableMapDirectives;
 import org.junit.Test;
 import org.virtualrepository.tabular.Column;
 import org.virtualrepository.tabular.DefaultTable;
 import org.virtualrepository.tabular.Row;
 import org.virtualrepository.tabular.Table;
 
 public class Table2CodelistTest {
 
 	@Test
 	public void mapBasicCodelist() throws Exception {
 
 		String[][] data = { { "11", "12" }, { "21", "22" } };
 
 		Table table = tableWith(data,"c1","c2");
 
 		TableMapDirectives directives = new TableMapDirectives("c1");
 		
 		Table2Codelist transform = new Table2Codelist(directives);
 		
 		Codelist list = transform.apply(table);
 
 		Codelist expected = codelist()
 				.name("c1")
 				.with(code().name("11").build(),
					  code().name("21").build()).version("1.0").build();
 
 		assertEquals(expected, list);
 
 	}
 	
 	@Test
 	public void mapCodelistWithNameAndAttributes() throws Exception {
 
 		String[][] data = {{ "11", "12" }, { "21", "22" } };
 
 		Table table = tableWith(data,"c1","c2");
 
 		TableMapDirectives directives = new TableMapDirectives("c1");
 		
 		String name = "list";
 		directives.name(name);
		directives.version("2.0");
 		
 		Attribute attribute = attr().name("a").value("v").build();
 		directives.attributes().add(attribute);
 		
 		Table2Codelist transform = new Table2Codelist(directives);
 		
 		Codelist list = transform.apply(table);
 		
 		Codelist expected = codelist()
 				.name(name)
 				.with(code().name("11").build(),
 					  code().name("21").build())
 				.attributes(attribute)
				.version("2.0")
 				.build();
 
 		assertEquals(expected, list);
 
 	}
 	
 	
 	@Test
 	public void mapCodelistWithAttributedCodes() throws Exception {
 
 		
 		String[][] data = { { "11", "12", "13" }, { "21", "22", "23" } };
 
 		Table table = tableWith(data,"c1","c2","c3");
 
 
 		TableMapDirectives directives = new TableMapDirectives("c1");
 		
 		directives.add(column("c2").language("en")).
 				   add(column("c3").name("attr").type("type"));
 		
 
 		Table2Codelist transform = new Table2Codelist(directives);
 		
 		Codelist list = transform.apply(table);
 
 		Codelist expected = codelist()
 				.name("c1")
 				.with(code().name("11").attributes(
 								attr().name("c2").value("12").in("en").build(),
 								attr().name("attr").value("13").ofType("type").build()
 							).build(),
 					  code().name("21").attributes(
 							  attr().name("c2").value("22").in("en").build(),
 							  attr().name("attr").value("23").ofType("type").build()
 					   )
 				      .build())
		.version("1.0").build();
 
 		assertEquals(expected, list);
 
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

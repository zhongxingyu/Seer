 package br.com.expense.parser.rules;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Map;
 
 import org.junit.Test;
 
 import br.com.expense.model.Category;
 
 public class CategoryRulesParserTest {
 	
 	@Test
 	public void shouldIgnoreBlankAndComments() {
 		String rulesLines = 
 				"# regras para alimentação e supermercado\r\n" +
 				".*PAO DE ACUCAR.* => supermercado\r\n" +
 				".*JARDIM SOPHIE.* => alimentação\r\n" +
 				"\r\n" +
 				"#regras outros gastos\r\n" +
 				".*NETMOVIES.* => casa\r\n";
 				
 		Map<String, Category> rules = new CategoryRulesParser().processRules(rulesLines);
 		assertNotNull(rules);
 		assertEquals(3, rules.keySet().size());
 		assertTrue(rules.keySet().contains(".*PAO DE ACUCAR.*"));
 	}
 
 }
